from django.urls import include, path
from main import views

urlpatterns = [
    path('update',views.update_notice),
    path('get_all',views.get_notice),
    path('test_input',views.test_input),
]
