from django.urls import include, path
from main import views

urlpatterns = [
    path('update',views.update_notice),
    path('get_all',views.get_notice),
    path('test_input',views.test_input),
    path('start_background', views.start_background),
    path('stop_background', views.stop_background),
]
