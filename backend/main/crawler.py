import multiprocessing
import pprint

import requests
from bs4 import BeautifulSoup  # analyze html text
import time
import json
import re
import copy
import logging
from pymongo import MongoClient

crawl_and_updateDB_interval = 30 * 60 # sec
time_interval_get_notice_list_skku = 1 # sec
time_interval_get_notice_summary_skku = 1 # sec
time_interval_get_notice_summary_cs = 1 # sec

start_page_get_notice_list_skku, end_page_get_notice_list_skku = 0, 35 # 각 페이지당 글 10개  350
start_num_get_notice_list_cs , end_num_get_notice_list_cs = 0, 150 # 글 개수  150
#num_rows_all_fetch = 1000

max_line_summary = 7

host_address =  '13.124.68.141' # aws 연동할때 마다 가져오기
port_num =  27017
user_name = 'se'
pass_word = '1234'

headers = requests.utils.default_headers()

headers.update(
    {
        'User-Agent': 'My User Agent 1.0',
    }
)

logger = logging.getLogger(__name__)
formatter = logging.Formatter('[%(asctime)s][%(levelname)s|%(filename)s:%(lineno)s] >> %(message)s')
streamHandler = logging.StreamHandler()
fileHandler = logging.FileHandler('./crawler_log.log')

streamHandler.setFormatter(formatter)
fileHandler.setFormatter(formatter)

logger.addHandler(streamHandler)
logger.addHandler(fileHandler)
logger.setLevel(level=logging.DEBUG)

is_start = False
process_list = []

def start_background_process():
    global is_start

    if is_start : return

    #process_list = []
    process_crawler = multiprocessing.Process(target=crawl_and_updateDB)

    process_list.append(process_crawler)
    for proc in process_list:
        proc.start()

    is_start = True
    logger.info(f"start_background_process()")
    return

def stop_background_process():
    global is_start
    if not is_start : return

    for proc in process_list:
        proc.terminate()
    for proc in process_list:
        proc.join()
    process_list.clear()

    is_start = False
    logger.info(f"stop_background_process()")
    return

def crawl_and_updateDB():
    while True:
        logger.info(f"start to get notice_list")
        notice_list_skku = get_notice_list_skku()

        notice_list_cs = get_notice_list_cs()
        notice_list_total = notice_list_skku + notice_list_cs
        notice_list_total.sort(key=lambda x: (x['date'], x['id_homepage_notice']))
        logger.info(f"got all notice_list... skku : {len(notice_list_skku)}, cs : {len(notice_list_cs)}, total : {len(notice_list_total)}")

        update_DB( notice_list_total )

        time.sleep(crawl_and_updateDB_interval)

category_name_num_dict = {
'성균관대_학사' : 0,
'성균관대_입학' : 1,
'성균관대_취업' : 2,
'성균관대_채용/모집' : 3,
'성균관대_장학' : 4,
'성균관대_행사/세미나' : 5,
'성균관대_일반' : 6,
'소프트웨어대학_학부' : 7,
'소프트웨어대학_일반' : 8,
'소프트웨어대학_대학원' : 9
}


"""
[5 functions here]
1. get_notice_list_skku() # get 100 recent notice list : 10 sec
2. get_notice_summary_skku(url) # sleep 1 second for each notice
3. get_notice_list_cs() # get 100 recent notice list : 1sec
4. get_notice_summary_cs(rest_url) # sleep 1 second for each notice
5. def updateDB(notices)

[USE]
notices = get_notice_list_skku() 
notices += get_notice_list_cs()
updateDB(notices)  # get_notice_summary function is used in updateDB(notices)

[예상 소요 시간]
맨 처음에 DB에 이전 정보(총 4000개 정도 글)들까지 저장 --> 2~3시간 정도?
주기적으로 확인 : 
    1회 진행할 때 : 12 sec + x sec (x : 새로 올라온 공지/수정된 공지 글)
    20분 정도에 1회씩 진행하면 괜찮을듯?
"""

skku_url_base = "https://www.skku.edu/skku/campus/skk_comm/notice01.do"
skku_homepage_list_url_default = skku_url_base + "?mode=list&&articleLimit=10&article.offset="
cs_homepage_list_rest_url = "https://cs.skku.edu/rest/board/list/notice?init=true&page=9"


def get_notice_list_skku():
    notice_list = []

    for offset_num in range(start_page_get_notice_list_skku, end_page_get_notice_list_skku):  # 10 pages * 10 notices = 100 notices
        time.sleep(time_interval_get_notice_list_skku)  # sleep 1 second

        skku_notice_homepage_url = skku_homepage_list_url_default + str(offset_num * 10)
        response = requests.get(skku_notice_homepage_url, headers=headers)
        html = response.text
        soup = BeautifulSoup(html, 'html.parser')

        notice_tags = soup.select('.board-list-content-wrap')

        for notice_tag in notice_tags:

            category_title_tag = notice_tag.select_one('.board-list-content-title')
            num_writer_date_views_tag = notice_tag.select_one('.board-list-content-info')

            category = category_title_tag.select_one('.c-board-list-category').text.strip()
            title = category_title_tag.select_one('a').text.strip()
            link = skku_url_base + category_title_tag.select_one('a').attrs['href']
            category = category.strip('[')
            category = category.rstrip(']')
            category = "성균관대_" + category
            category_num = category_name_num_dict[category]

            li_tags = num_writer_date_views_tag.select('li')

            id_homepage_notice, writer, date, views = [x.text.strip() for x in li_tags]
            id_homepage_notice = int(id_homepage_notice[id_homepage_notice.index('.') + 1:])
            views = int(views[3:])

            notice_dict = {"id_homepage_notice": id_homepage_notice,
                           "title": title,
                           "tag_name": category,
                           "tag_num" : category_num,
                           "writer": writer,
                           "date": date,
                           "watch": views,
                           "link": link}
            notice_list.append(notice_dict)

    notice_list.reverse()  # reverse to check from the last
    return notice_list


def get_notice_summary_skku(skku_homepage_content_url):
    time.sleep(time_interval_get_notice_summary_skku)  # sleep 1 second
    response = requests.get(skku_homepage_content_url, headers=headers)
    html = response.text
    soup = BeautifulSoup(html, 'html.parser')
    body_tag = soup.select_one('.pre')
    if body_tag:
        summary = body_tag.text.strip()
    else:
        summary = ""

    summary_list = re.split('\r|\n', summary)

    result = ""
    count = 0
    for sentence in summary_list:
        if sentence:
            if not contains_meaningful_letter(sentence): continue
            if count == max_line_summary: break
            result += sentence.strip()
            result += "\r\n"
            count += 1
    if not result:
        return "[내용 요약 생략] 글에 글자 없이 포스터만 있어서 내용 요약 없음"

    return result


def get_notice_list_cs():
    notice_list = []

    response = requests.get(cs_homepage_list_rest_url, headers=headers)  # request object
    html = response.text  # html text from response object.
    soup = BeautifulSoup(html, 'html.parser')  # use soup library to analyze html text
    data = soup.text

    json_data_list = json.loads(data).get('aaData')
    for json_data in json_data_list[start_num_get_notice_list_cs : end_num_get_notice_list_cs]:  # first 100
        id_homepage_notice = int(json_data.get('id'))
        title = json_data.get('title').strip()
        category = json_data.get('category')
        writer = json_data.get('Username')
        date = json_data.get('time')
        views = json_data.get('views')
        link = "https://cs.skku.edu/rest/board/view/notice/" + str(id_homepage_notice)

        category = "소프트웨어대학_" + category
        category_num = category_name_num_dict[category]
        date = date.replace('.', '-')

        notice_dict = {"id_homepage_notice": id_homepage_notice,
                       "title": title,
                       "tag_name": category,
                       "tag_num": category_num,
                       "writer": writer,
                       "date": date,
                       "watch": views,
                       "link": link}

        notice_list.append(notice_dict)
    notice_list.reverse()
    return notice_list


def get_notice_summary_cs(cs_homepage_content_rest_url):
    time.sleep(time_interval_get_notice_summary_cs)  # sleep 1 second
    response = requests.get(cs_homepage_content_rest_url, headers=headers)
    html = response.text
    soup = BeautifulSoup(html, 'html.parser')
    data = soup.text
    try:
        json_data = json.loads(data)
    except json.decoder.JSONDecodeError:
        return "[내용 요약 생략] 소프트웨어대학 홈페이지 공지사항 rest api(cs.skku.edu/rest/board/view/notice/)에서 escape 문자(\\\") 처리가 안 되어있어서 json parsing 안됨."

    summary = json_data.get('post').get('text')
    summary_list = re.split('\r|\n', summary)

    result = ""
    count = 0
    for sentence in summary_list:
        if sentence:
            if not contains_meaningful_letter(sentence): continue
            if count == max_line_summary: break
            result += sentence.strip()
            result += "\r\n"
            count += 1

    if not result:
        return "[내용 요약 생략] 글에 글자 없이 포스터만 있어서 내용 요약 없음"
    return result


def contains_meaningful_letter(text):
    kor = re.search(u'[\u3130-\u318F\uAC00-\uD7A3]+', text)
    eng = re.search('[a-zA-Z]', text)
    number = re.search('\d', text)
    return kor or eng or number

def make_start_end_index_dict(date_list):
    start_end_index_dict = dict()
    for index, date in enumerate(date_list):
        if date in start_end_index_dict:
            start_end_index_dict[date].append(index)

        else:
            start_end_index_dict[date] = [index]

    return start_end_index_dict

def update_DB(notice_list):  # SKKU, SWE 공통!
    """
    notice_dict = {"id_homepage_notice": id_homepage_notice, str
                   "title": title, str
                   "tag_name": category, str
                   "tag_num": category_num, int
                   "writer": writer, str
                   "date": date, str
                   "watch": views, int
                   "link": link, str
                   }
    +++ id_server_notice int , summary str
    IN DB = {
            id_server_notice int # 서버에서 구분용으로 사용하는 공지글 번호
            id_homepage_notice  int # 학교 홈페이지에 써있는 공지글 번호
            title str # 글 제목
            tag_name str # 태그(카테고리) 이름
            tag_num int # 태그 번호
            writer str # 작성자
            date str # str 날짜형식 : 0000-00-00
            watch int # 조회수
            summary str # 요약 내용
    }
    """
    client = MongoClient(
        host= host_address,  # aws 연동할때 마다 가져오기
        port= port_num, #27017
        username= user_name, #'se'
        password=pass_word # '1234'
    )

    logger.info(f"start update DB")
    db = client.noticeDB
    notice_collection = db.notice_crawling # change to notice_crawling
    global_max_id_collection = db.global_max_id_collection
    row_list = list(notice_collection.find().sort("date", -1))# .limit(num_rows_all_fetch))


    if row_list:
        date_list = [x['date'] for x in row_list]
    else:
        date_list = []

    date_index_dict = make_start_end_index_dict(date_list)

    count1 = 0 # in DB, not changed
    count2 = 0 # same date, no same title
    count3 = 0 # no same date
    for notice_dict in notice_list:
        cur_title = notice_dict['title']
        cur_date = notice_dict['date']
        if cur_date in date_index_dict: # same date
            candid_index_list = date_index_dict[cur_date]
            did_find_same = False
            for candid_index in candid_index_list: # check the candids
                searched_title = row_list[candid_index]['title']

                if cur_title == searched_title: # same name  --> same date + same title = same article!!!
                    # update 'watch'
                    cur_watch = notice_dict["watch"]
                    in_db_id_server_notice = row_list[candid_index]["id_server_notice"]
                    notice_collection.update_one({"id_server_notice": in_db_id_server_notice},
                                                    {"$set": {"watch": cur_watch } } )
                    count1 += 1
                    did_find_same = True # found the same article
                    break

            if did_find_same: # found the same article
                continue
            else:
                count2 += 1 # same date but no same title

        else :
            count3 += 1 # no single same date


        # did not find the same article.. this is a new article or edited article.
        # if (in DB and changed)  OR (not in DB)

        global_max_id = list(global_max_id_collection.find())[0]['global_max_id_server_notice']
        global_max_id += 1

        new_notice_dict = copy.deepcopy(notice_dict)
        # add id_server_notice int , summary str
        new_notice_dict['id_server_notice'] = global_max_id
        if new_notice_dict['tag_name'][:4] == "성균관대":
            new_notice_dict['summary'] = get_notice_summary_skku(new_notice_dict['link'])
        else:
            new_notice_dict['summary'] = get_notice_summary_cs(new_notice_dict['link'])

        notice_collection.insert_one(new_notice_dict)
        global_max_id_collection.update_one({"global_max_id_server_notice": global_max_id - 1},
                                            {"$set": {'global_max_id_server_notice': global_max_id}})

        logger.info(f"insert new info to DB / id_server_notice : {global_max_id - 1} ")

    logger.info(f"all fin... count1 : {count1}, count2 : {count2}, count3 : {count3}, 전체 notice_list : {len(notice_list)}")
    logger.info(f"crawl_and_updateDB_interval : {crawl_and_updateDB_interval}")
    date_index_dict.clear()