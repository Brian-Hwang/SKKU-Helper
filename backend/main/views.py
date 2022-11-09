from codecs import CodecInfo

from django.shortcuts import render
from rest_framework.decorators import api_view
from django.http.response import JsonResponse, HttpResponse
import json
from pymongo import MongoClient


# Create your views here.

client = MongoClient(
        host='43.200.180.139',
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
        type = request.GET['type']

        db = client.noticeDB
        notice_collection = db.notice

        #id = notice_collection.find({"Name" : code, "Date" : { '$gte' : start_date , '$lt': end_date}}, {"_id" : 0, "Name" : 0, "High" : 0 , "Volume" : 0, "Change" : 0 , "Low" : 0 , "Open" : 0 })
        
        id = notice_collection.find({"tag" : tag} , {"_id" : 0})
        
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

        db = client.noticeDB
        notice_collection = db.notice
        data = {'title' : title, 'summarize' : summarize, "tag" : tag, "writer" : writer, "date" : date, "link" : link}
        print(data)
        post_id = notice_collection.insert_one(data)
        
        if post_id:
            return JsonResponse({ "Result" : True})
        else:
            return JsonResponse({"Result": False})