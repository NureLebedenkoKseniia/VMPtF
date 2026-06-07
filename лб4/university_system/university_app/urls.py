from django.urls import path
from . import views

urlpatterns = [
    path('', views.dashboard, name='dashboard'),
    path('api/seed/', views.seed_db, name='api_seed_db'),
    path('api/teachers/', views.teachers_api, name='api_teachers'),
    path('api/courses/', views.course_list, name='api_course_list'),
    path('api/courses/<int:course_id>/', views.course_detail, name='api_course_detail'),
    path('api/students/', views.student_list, name='api_student_list'),
    path('api/students/<int:student_id>/', views.student_detail, name='api_student_detail'),
    path('api/grades/', views.grade_list, name='api_grade_list'),
    path('api/grades/<int:grade_id>/', views.grade_detail, name='api_grade_detail'),
    path('api/grades/bulk/', views.bulk_grade_update, name='api_bulk_grade_update'),
]
