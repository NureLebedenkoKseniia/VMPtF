import json
from django.shortcuts import render
from django.http import JsonResponse, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.core.cache import cache
from django.db import transaction, connection, reset_queries
from django.utils.decorators import method_decorator
from .models import Teacher, Student, Course, ClassSession, Grade

# Helper to format queries
def get_query_log():
    return [q['sql'] for q in connection.queries]

def dashboard(request):
    """Render the dashboard UI"""
    return render(request, 'university_app/dashboard.html')

@csrf_exempt
def seed_db(request):
    """Seed the database with initial university records inside a transaction"""
    if request.method != 'POST':
        return JsonResponse({"error": "Method not allowed"}, status=405)
    
    try:
        with transaction.atomic():
            # Clear old data
            Grade.objects.all().delete()
            ClassSession.objects.all().delete()
            Course.objects.all().delete()
            Student.objects.all().delete()
            Teacher.objects.all().delete()
            
            # Create Teachers
            t1 = Teacher.objects.create(first_name="Олександр", last_name="Коваленко", email="kovalenko@nure.ua", department="КІУ")
            t2 = Teacher.objects.create(first_name="Марія", last_name="Петренко", email="petrenko@nure.ua", department="ПІ")
            t3 = Teacher.objects.create(first_name="Дмитро", last_name="Іванов", email="ivanov@nure.ua", department="ЕОМ")
            
            # Create Students
            s1 = Student.objects.create(first_name="Ксенія", last_name="Лебеденко", email="kseniya.lebedenko@nure.ua", enrollment_date="2023-09-01")
            s2 = Student.objects.create(first_name="Іван", last_name="Сидоренко", email="ivan.sydorenko@nure.ua", enrollment_date="2023-09-01")
            s3 = Student.objects.create(first_name="Анна", last_name="Шевченко", email="anna.shevchenko@nure.ua", enrollment_date="2024-09-01")
            s4 = Student.objects.create(first_name="Михайло", last_name="Бойко", email="mykhailo.boyko@nure.ua", enrollment_date="2024-09-01")
            
            # Create Courses
            c1 = Course.objects.create(title="Високорівневі мови програмування", code="IP-01", description="Курс з розробки на Django та Kotlin Compose", teacher=t2)
            c2 = Course.objects.create(title="Бази даних та SQL", code="DB-02", description="Основи проектування реляційних БД", teacher=t1)
            c3 = Course.objects.create(title="Операційні системи", code="OS-03", description="Організація роботи сучасних ОС", teacher=t3)
            
            # Create Class Sessions
            ClassSession.objects.create(course=c1, date_time="2026-06-03T10:15:00Z", room="315-і")
            ClassSession.objects.create(course=c1, date_time="2026-06-10T10:15:00Z", room="315-і")
            ClassSession.objects.create(course=c2, date_time="2026-06-04T12:00:00Z", room="240-е")
            ClassSession.objects.create(course=c3, date_time="2026-06-05T08:30:00Z", room="412-і")
            
            # Create Grades
            Grade.objects.create(student=s1, course=c1, score=98.50)
            Grade.objects.create(student=s1, course=c2, score=95.00)
            Grade.objects.create(student=s2, course=c1, score=85.00)
            Grade.objects.create(student=s3, course=c3, score=74.50)
            
            # Clear cache
            cache.delete_many(['course_list', 'grade_list', 'student_list'])
            
            return JsonResponse({"message": "Database successfully seeded with fresh records!"})
    except Exception as e:
        return JsonResponse({"error": f"Failed to seed database: {str(e)}"}, status=500)


# --- COURSE API ENDPOINTS (with Level 3 Select/Prefetch Related and Caching) ---

@csrf_exempt
def course_list(request):
    """GET list of all courses (Cached and Optimized) or POST to create new course"""
    reset_queries()
    
    if request.method == 'GET':
        cache_key = 'course_list'
        cached_data = cache.get(cache_key)
        
        if cached_data is not None:
            return JsonResponse({
                "courses": cached_data,
                "cached": True,
                "query_count": 0,
                "queries": []
            })
        
        # Optimize DB access with select_related('teacher') to prevent N+1 queries
        courses = Course.objects.all().select_related('teacher')
        data = []
        for course in courses:
            data.append({
                "id": course.id,
                "title": course.title,
                "code": course.code,
                "description": course.description,
                "teacher": {
                    "id": course.teacher.id if course.teacher else None,
                    "name": f"{course.teacher.first_name} {course.teacher.last_name}" if course.teacher else "No Teacher",
                    "department": course.teacher.department if course.teacher else ""
                }
            })
            
        # Cache for 60 seconds
        cache.set(cache_key, data, 60)
        
        return JsonResponse({
            "courses": data,
            "cached": False,
            "query_count": len(connection.queries),
            "queries": get_query_log()
        })
        
    elif request.method == 'POST':
        try:
            body = json.loads(request.body)
            teacher_id = body.get('teacher_id')
            teacher = Teacher.objects.get(id=teacher_id) if teacher_id else None
            
            course = Course.objects.create(
                title=body['title'],
                code=body['code'],
                description=body.get('description', ''),
                teacher=teacher
            )
            
            # Invalidate Cache
            cache.delete('course_list')
            
            return JsonResponse({
                "id": course.id,
                "title": course.title,
                "code": course.code,
                "message": "Course created successfully!"
            }, status=201)
        except Exception as e:
            return JsonResponse({"error": str(e)}, status=400)


@csrf_exempt
def course_detail(request, course_id):
    """GET, PUT, DELETE individual course"""
    try:
        course = Course.objects.get(id=course_id)
    except Course.DoesNotExist:
        return JsonResponse({"error": "Course not found"}, status=404)
        
    if request.method == 'GET':
        return JsonResponse({
            "id": course.id,
            "title": course.title,
            "code": course.code,
            "description": course.description,
            "teacher_id": course.teacher.id if course.teacher else None
        })
        
    elif request.method == 'PUT':
        try:
            body = json.loads(request.body)
            course.title = body.get('title', course.title)
            course.code = body.get('code', course.code)
            course.description = body.get('description', course.description)
            
            if 'teacher_id' in body:
                teacher_id = body['teacher_id']
                course.teacher = Teacher.objects.get(id=teacher_id) if teacher_id else None
                
            course.save()
            # Invalidate cache
            cache.delete('course_list')
            return JsonResponse({"message": "Course updated successfully!"})
        except Exception as e:
            return JsonResponse({"error": str(e)}, status=400)
            
    elif request.method == 'DELETE':
        course.delete()
        # Invalidate cache
        cache.delete('course_list')
        return JsonResponse({"message": "Course deleted successfully!"})


# --- STUDENT API ENDPOINTS ---

@csrf_exempt
def student_list(request):
    """GET list of students or POST a new student"""
    if request.method == 'GET':
        cache_key = 'student_list'
        cached_data = cache.get(cache_key)
        if cached_data is not None:
            return JsonResponse({"students": cached_data, "cached": True})
            
        students = Student.objects.all()
        data = [{
            "id": s.id,
            "first_name": s.first_name,
            "last_name": s.last_name,
            "email": s.email,
            "enrollment_date": s.enrollment_date.strftime("%Y-%m-%d")
        } for s in students]
        
        cache.set(cache_key, data, 60)
        return JsonResponse({"students": data, "cached": False})
        
    elif request.method == 'POST':
        try:
            body = json.loads(request.body)
            student = Student.objects.create(
                first_name=body['first_name'],
                last_name=body['last_name'],
                email=body['email'],
                enrollment_date=body['enrollment_date']
            )
            cache.delete('student_list')
            return JsonResponse({
                "id": student.id,
                "first_name": student.first_name,
                "last_name": student.last_name,
                "message": "Student created successfully!"
            }, status=201)
        except Exception as e:
            return JsonResponse({"error": str(e)}, status=400)


@csrf_exempt
def student_detail(request, student_id):
    """GET, PUT, DELETE student"""
    try:
        student = Student.objects.get(id=student_id)
    except Student.DoesNotExist:
        return JsonResponse({"error": "Student not found"}, status=404)
        
    if request.method == 'GET':
        return JsonResponse({
            "id": student.id,
            "first_name": student.first_name,
            "last_name": student.last_name,
            "email": student.email,
            "enrollment_date": student.enrollment_date.strftime("%Y-%m-%d")
        })
        
    elif request.method == 'PUT':
        try:
            body = json.loads(request.body)
            student.first_name = body.get('first_name', student.first_name)
            student.last_name = body.get('last_name', student.last_name)
            student.email = body.get('email', student.email)
            student.enrollment_date = body.get('enrollment_date', student.enrollment_date)
            student.save()
            cache.delete('student_list')
            return JsonResponse({"message": "Student updated successfully!"})
        except Exception as e:
            return JsonResponse({"error": str(e)}, status=400)
            
    elif request.method == 'DELETE':
        # Student deletion with grade cleanup is cascaded, but let's run it in a transaction
        with transaction.atomic():
            student.delete()
        cache.delete_many(['student_list', 'grade_list'])
        return JsonResponse({"message": "Student deleted (cascading cleanup completed)."})


# --- GRADE API ENDPOINTS (with Caching & Select/Prefetch Related) ---

@csrf_exempt
def grade_list(request):
    """GET grades list (Cached & Optimized) or POST a new grade"""
    reset_queries()
    
    if request.method == 'GET':
        cache_key = 'grade_list'
        cached_data = cache.get(cache_key)
        
        if cached_data is not None:
            return JsonResponse({
                "grades": cached_data,
                "cached": True,
                "query_count": 0,
                "queries": []
            })
            
        # Optimize with select_related to get student and course info in one query
        grades = Grade.objects.all().select_related('student', 'course')
        data = []
        for grade in grades:
            data.append({
                "id": grade.id,
                "student": {
                    "id": grade.student.id,
                    "name": f"{grade.student.first_name} {grade.student.last_name}"
                },
                "course": {
                    "id": grade.course.id,
                    "code": grade.course.code,
                    "title": grade.course.title
                },
                "score": float(grade.score),
                "date_recorded": grade.date_recorded.strftime("%Y-%m-%d")
            })
            
        cache.set(cache_key, data, 60)
        return JsonResponse({
            "grades": data,
            "cached": False,
            "query_count": len(connection.queries),
            "queries": get_query_log()
        })
        
    elif request.method == 'POST':
        try:
            body = json.loads(request.body)
            student = Student.objects.get(id=body['student_id'])
            course = Course.objects.get(id=body['course_id'])
            score = float(body['score'])
            
            if score < 0 or score > 100:
                return JsonResponse({"error": "Score must be between 0 and 100"}, status=400)
                
            grade, created = Grade.objects.update_or_create(
                student=student,
                course=course,
                defaults={"score": score}
            )
            
            cache.delete('grade_list')
            return JsonResponse({
                "id": grade.id,
                "created": created,
                "message": "Grade recorded successfully!"
            }, status=201)
        except Exception as e:
            return JsonResponse({"error": str(e)}, status=400)


@csrf_exempt
def grade_detail(request, grade_id):
    """GET, PUT, DELETE individual grade"""
    try:
        grade = Grade.objects.get(id=grade_id)
    except Grade.DoesNotExist:
        return JsonResponse({"error": "Grade not found"}, status=404)
        
    if request.method == 'GET':
        return JsonResponse({
            "id": grade.id,
            "student_id": grade.student.id,
            "course_id": grade.course.id,
            "score": float(grade.score)
        })
        
    elif request.method == 'PUT':
        try:
            body = json.loads(request.body)
            score = float(body.get('score', grade.score))
            if score < 0 or score > 100:
                return JsonResponse({"error": "Score must be between 0 and 100"}, status=400)
            grade.score = score
            grade.save()
            cache.delete('grade_list')
            return JsonResponse({"message": "Grade updated successfully!"})
        except Exception as e:
            return JsonResponse({"error": str(e)}, status=400)
            
    elif request.method == 'DELETE':
        grade.delete()
        cache.delete('grade_list')
        return JsonResponse({"message": "Grade deleted successfully!"})


# --- LEVEL 4 DATA INTEGRITY & TRANSACTIONS ---

@csrf_exempt
def bulk_grade_update(request):
    """
    POST a list of grades to insert/update in a single transaction.
    If any grade record fails database validations or custom business logic constraints,
    the entire transaction is rolled back.
    Format:
    [
        {"student_id": 1, "course_id": 1, "score": 95},
        {"student_id": 2, "course_id": 1, "score": -5}  <-- This triggers rollback!
    ]
    """
    if request.method != 'POST':
        return JsonResponse({"error": "Method not allowed"}, status=405)
        
    try:
        body = json.loads(request.body)
        updates = body.get("updates", [])
        if not updates:
            return JsonResponse({"error": "No updates list provided"}, status=400)
            
        success = True
        error_msg = ""
        
        # Open transaction block
        with transaction.atomic():
            for update in updates:
                student_id = update.get("student_id")
                course_id = update.get("course_id")
                score = float(update.get("score"))
                
                # Check business logic validation rule
                if score < 0 or score > 100:
                    raise ValueError(f"Score {score} is invalid. Scores must be between 0 and 100.")
                    
                # Fetch objects
                student = Student.objects.get(id=student_id)
                course = Course.objects.get(id=course_id)
                
                # Update or create grade
                Grade.objects.update_or_create(
                    student=student,
                    course=course,
                    defaults={'score': score}
                )
                
        # If transaction succeeds
        cache.delete('grade_list')
        return JsonResponse({
            "status": "success",
            "message": f"Successfully updated {len(updates)} grades in a single transaction."
        })
        
    except ValueError as e:
        # Business logic validation failure
        return JsonResponse({
            "status": "rolled_back",
            "error": f"Validation Error: {str(e)}. Transaction was rolled back successfully."
        }, status=400)
    except (Student.DoesNotExist, Course.DoesNotExist) as e:
        # DB Referential integrity failure
        return JsonResponse({
            "status": "rolled_back",
            "error": f"Database Constraint Error: Student or Course does not exist. {str(e)}. Transaction was rolled back successfully."
        }, status=400)
    except Exception as e:
        return JsonResponse({
            "status": "rolled_back",
            "error": f"Unexpected error: {str(e)}. Transaction was rolled back."
        }, status=500)


def teachers_api(request):
    """GET list of teachers (for dropdowns)"""
    teachers = Teacher.objects.all()
    return JsonResponse({
        "teachers": [{
            "id": t.id,
            "name": f"{t.first_name} {t.last_name}",
            "department": t.department
        } for t in teachers]
    })
