import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'transactions_project.settings')
django.setup()

from transactions_app.models import Category, Transaction

def seed():
    # Clear old data if any
    Transaction.objects.all().delete()
    Category.objects.all().delete()

    # Create Categories
    c1 = Category.objects.create(name="Продукти", description="Витрати на харчування та супермаркети", monthly_limit=5000.00)
    c2 = Category.objects.create(name="Комунальні", description="Оплата комунальних послуг, інтернет", monthly_limit=3000.00)
    c3 = Category.objects.create(name="Транспорт", description="Проїзд у громадському транспорті, пальне", monthly_limit=1500.00)
    c4 = Category.objects.create(name="Розваги", description="Кіно, кафе, відпочинок", monthly_limit=2000.00)
    c5 = Category.objects.create(name="Зарплата", description="Надходження заробітної плати", monthly_limit=0.00)

    # Create Transactions
    Transaction.objects.create(description="Аванс за травень", amount=12000.00, type="debit", category=c5, date="2026-05-15")
    Transaction.objects.create(description="Зарплата за травень", amount=18000.00, type="debit", category=c5, date="2026-05-30")
    
    Transaction.objects.create(description="Сільпо - покупка їжі", amount=1450.00, type="credit", category=c1, date="2026-06-01")
    Transaction.objects.create(description="Оплата за електроенергію", amount=820.00, type="credit", category=c2, date="2026-06-02")
    Transaction.objects.create(description="Поповнення картки метро", amount=200.00, type="credit", category=c3, date="2026-06-02")
    Transaction.objects.create(description="Вечеря в кафе", amount=950.00, type="credit", category=c4, date="2026-06-03")
    Transaction.objects.create(description="АТБ - закупка", amount=1100.00, type="credit", category=c1, date="2026-06-03")
    Transaction.objects.create(description="Покупка квитків", amount=650.00, type="credit", category=c3, date="2026-06-03")

    print("Transactions Database seeded successfully!")

if __name__ == '__main__':
    seed()
