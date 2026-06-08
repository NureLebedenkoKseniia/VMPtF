import json
from django.test import TestCase, Client
from django.urls import reverse
from django.core.cache import cache
from .models import Teacher, Student, Course, Grade

class UniversitySystemTests(TestCase):
    def setUp(self):
        # Clear cache before each test
        cache.clear()
        
        # Seed test data
        self.teacher = Teacher.objects.create(
            first_name="Марія",
            last_name="Петренко",
            email="petrenko@nure.ua",
            department="ПІ"
        )
        self.student1 = Student.objects.create(
            first_name="Ксенія",
            last_name="Лебеденко",
            email="kseniya.lebedenko@nure.ua",
            enrollment_date="2023-09-01"
        )
        self.student2 = Student.objects.create(
            first_name="Іван",
            last_name="Сидоренко",
            email="ivan.sydorenko@nure.ua",
            enrollment_date="2023-09-01"
        )
        self.course = Course.objects.create(
            title="ВМПтФ",
            code="IP-01",
            description="High-level languages",
            teacher=self.teacher
        )
        self.client = Client()

    def test_course_list_caching(self):
        """Test that courses are cached on subsequent list requests"""
        url = reverse('api_course_list')
        
        # First request (Cache Miss)
        resp1 = self.client.get(url)
        self.assertEqual(resp1.status_code, 200)
        data1 = json.loads(resp1.content)
        self.assertFalse(data1['cached'])
        
        # Second request (Cache Hit)
        resp2 = self.client.get(url)
        self.assertEqual(resp2.status_code, 200)
        data2 = json.loads(resp2.content)
        self.assertTrue(data2['cached'])
        self.assertEqual(data2['query_count'], 0)

    def test_bulk_grade_transaction_success(self):
        """Test that a valid bulk grade list succeeds and updates the database"""
        url = reverse('api_bulk_grade_update')
        payload = {
            "updates": [
                {"student_id": self.student1.id, "course_id": self.course.id, "score": 95.0},
                {"student_id": self.student2.id, "course_id": self.course.id, "score": 88.5}
            ]
        }
        
        response = self.client.post(url, data=json.dumps(payload), content_type='application/json')
        self.assertEqual(response.status_code, 200)
        data = json.loads(response.content)
        self.assertEqual(data['status'], 'success')
        
        # Verify changes committed
        g1 = Grade.objects.get(student=self.student1, course=self.course)
        g2 = Grade.objects.get(student=self.student2, course=self.course)
        self.assertEqual(float(g1.score), 95.0)
        self.assertEqual(float(g2.score), 88.5)

    def test_bulk_grade_transaction_rollback(self):
        """Test that an invalid score in bulk updates rolls back all operations in the transaction"""
        url = reverse('api_bulk_grade_update')
        
        # Initial grade status: no grades recorded yet
        self.assertFalse(Grade.objects.filter(student=self.student1, course=self.course).exists())
        
        payload = {
            "updates": [
                {"student_id": self.student1.id, "course_id": self.course.id, "score": 99.0}, # Valid
                {"student_id": self.student2.id, "course_id": self.course.id, "score": 105.0} # INVALID (too high)
            ]
        }
        
        response = self.client.post(url, data=json.dumps(payload), content_type='application/json')
        self.assertEqual(response.status_code, 400)
        data = json.loads(response.content)
        self.assertEqual(data['status'], 'rolled_back')
        self.assertIn("Validation Error", data['error'])
        
        # Verify that even the first valid grade was NOT recorded due to atomic rollback
        self.assertFalse(Grade.objects.filter(student=self.student1, course=self.course).exists())

    def test_student_deletion_cascade(self):
        """Test that deleting a student deletes all their grades in a transaction"""
        # Create a grade
        Grade.objects.create(student=self.student1, course=self.course, score=90.0)
        self.assertTrue(Grade.objects.filter(student=self.student1).exists())
        
        # Delete student via detail endpoint
        url = reverse('api_student_detail', args=[self.student1.id])
        response = self.client.delete(url)
        self.assertEqual(response.status_code, 200)
        
        # Verify cascading deletion of grade
        self.assertFalse(Grade.objects.filter(student=self.student1).exists())
        self.assertFalse(Student.objects.filter(id=self.student1.id).exists())
