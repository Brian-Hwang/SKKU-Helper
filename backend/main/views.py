from codecs import CodecInfo

from django.shortcuts import render
from rest_framework.decorators import api_view
from django.http.response import JsonResponse, HttpResponse
import json
from pymongo import MongoClient


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
        tag = request.GET['tag']
        tag = int(tag)
        type = request.GET['type'] # app에서 요청하는 타입에 따름 0 : 최신 300개(읽지 않은 것 위주)
        type = int(type)
        
        db = client.noticeDB
        notice_collection = db.notice
        student_collection = db.student
        
        check_student = student_collection.find({"student_id" : sid}, {"_id":0})
        
        last_check_time = []
        
        if check_student :
            old_notice = check_student["read_notices"]
        else :
            new_student = {}
            new_student['student_id'] = sid
            new_student['read_notices'] = old_notice
            student_collection.insert(new_student)

        if type == 0:
            id = notice_collection.find({"tag" : tag} , {"_id" : 0})
        
        #id = notice_collection.find({"Name" : code, "Date" : { '$gte' : start_date , '$lt': end_date}}, {"_id" : 0, "Name" : 0, "High" : 0 , "Volume" : 0, "Change" : 0 , "Low" : 0 , "Open" : 0 })
        
        #id = notice_collection.find({"tag" : tag} , {"_id" : 0})
        
        result = list(id)
        print(result)
        if result == []:
            return JsonResponse({ "Result" : "None"})
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