from codecs import CodecInfo

from django.shortcuts import render
from rest_framework.decorators import api_view
from django.http.response import JsonResponse, HttpResponse
import json
from pymongo import MongoClient
from .crawler import start_background_process
from .crawler import stop_background_process

# Create your views here.

client = MongoClient(
        host='13.124.68.141', # aws 연동할때 마다 가져오기
        port = 27017,
        username = 'se',
        password = '1234'
    )


@api_view(['GET', 'POST'])
def update_notice(request):
    if request.method == 'GET':
        title = request.GET['title']
        summarize = request.GET['summarize']
        tag  = request.GET['tag']
        writer  = request.GET['writer']
        date = request.GET['date']
        link  = request.GET['link']

        db = client.noticeDB
        notice_collection = db.notice
        data = {}
        post_id = notice_collection.insert_many(data)
        
        if result == []:
            return JsonResponse({ "Result" : post_id})
        else:
            return JsonResponse({"Result": False})
        
@api_view(['GET', 'POST'])
def get_notice(request):
    if request.method == 'GET':
        sid = request.GET['student_id']
        tag = request.GET['tag_num']
        tag = int(tag)
        type = request.GET['type']
        type = int(type)
        
        print(int(tag))
        print(tag)
        
        result = []
        
        db = client.noticeDB
        notice_collection = db.notice_crawling
        student_collection = db.student
        new_notice = notice_collection.find({'id_server_notice' : {'$gt' : 2000}}).sort('id_server_notice',-1).limit(1)
        new_notice = list(new_notice)
        new_notice = new_notice[0]
        new_notice = new_notice['id_server_notice']
        
        check_student = student_collection.find({"student_id" : sid}, {"_id":0})
        check_student = list(check_student)
        last_notice = new_notice
        
        if check_student != []:
            print("check student in")
            check_student = check_student[0]
            last_notice = check_student["last_server_id"]
            student_collection.update_one({'student_id' : sid}, {'$set' : {"last_server_id" : last_notice} } )
        else :
            new_student = {}
            new_student['student_id'] = sid
            new_student['last_server_id'] = new_notice
            student_collection.insert(new_student)

        if type == 0: #tag별 + 최신 업데이트 100개
            id = notice_collection.find({"tag_num" : tag} , {"_id" : 0, "id_homepage_notice" : 0}).sort("id_server_notice",-1).limit(100)
            result = list(id)[:100]
        if type == 1: #tag별 + 최신 업데이트 100개 중 조회수 순
            id = notice_collection.find({"tag_num" : tag} , {"_id" : 0, "id_homepage_notice" : 0}).sort("id_server_notice",-1).limit(100).sort("watch",-1)
            result = list(id)[:100]
        if type == 2: #tag 상관 없이 + 최신 업데이트 100개
            id = notice_collection.find({"tag_num" : {'$lt': 10}}, {"_id" : 0, "id_homepage_notice" : 0}).sort("id_server_notice",-1).limit(100)
            result = list(id)[:100]
        if type == 3: #tag 상관 없이 +최신 업데이트 100개 중 조회수 순
            id = notice_collection.find({"tag_num" : {'$lt': 10}} , {"_id" : 0, "id_homepage_notice" : 0}).sort("id_server_notice",-1).limit(100).sort("watch",-1)
            result = list(id)[:100]
        
        if result == []:
            return JsonResponse({ "data" : "None"})
        else:
            return JsonResponse({"data" : result})
        
        
def test_input(request):
    if request.method == 'GET':
        title = request.GET['title']
        summarize = request.GET['summarize']
        tag  = request.GET['tag']
        writer  = request.GET['writer']
        date = request.GET['date']
        link  = request.GET['link']

        tag = int(tag)
        
        db = client.noticeDB
        notice_collection = db.notice
        data = {'title' : title, 'summarize' : summarize, "tag" : tag, "writer" : writer, "date" : date, "link" : link}
        print(data)
        post_id = notice_collection.insert_one(data)
        
        if post_id:
            return JsonResponse({"Result" : True})
        else:
            return JsonResponse({"Result": False})

@api_view(['GET', 'POST'])
def start_background(request):
    if request.method == 'GET':
        start_background_process()
        return JsonResponse({"Result": True})

@api_view(['GET', 'POST'])
def stop_background(request):
    if request.method == 'GET':
        stop_background_process()
        return JsonResponse({"Result": True})