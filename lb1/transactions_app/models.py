from django.db import models

class Category(models.Model):
    name = models.CharField(max_length=100, unique=True, verbose_name="Назва категорії")
    description = models.TextField(blank=True, verbose_name="Опис")
    monthly_limit = models.DecimalField(max_digits=10, decimal_places=2, default=0.00, verbose_name="Місячний ліміт (грн)")

    class Meta:
        verbose_name = "Категорія"
        verbose_name_plural = "Категорії"

    def __str__(self):
        return self.name


class Transaction(models.Model):
    TYPE_CHOICES = [
        ('debit', 'Дебет (Дохід)'),
        ('credit', 'Кредит (Витрата)'),
    ]

    description = models.CharField(max_length=255, verbose_name="Опис транзакції")
    amount = models.DecimalField(max_digits=10, decimal_places=2, verbose_name="Сума (грн)")
    type = models.CharField(max_length=10, choices=TYPE_CHOICES, verbose_name="Тип транзакції")
    category = models.ForeignKey(Category, on_delete=models.SET_NULL, null=True, blank=True, related_name='transactions', verbose_name="Категорія")
    date = models.DateField(verbose_name="Дата транзакції")
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = "Транзакція"
        verbose_name_plural = "Транзакції"
        ordering = ['-date', '-created_at']

    def __str__(self):
        return f"{self.get_type_display()}: {self.amount} грн ({self.description})"
