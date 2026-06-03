from django import forms
from .models import Transaction, Category

class TransactionForm(forms.ModelForm):
    class Meta:
        model = Transaction
        fields = ['description', 'amount', 'type', 'category', 'date']
        widgets = {
            'description': forms.TextInput(attrs={'placeholder': 'Опис транзакції'}),
            'amount': forms.NumberInput(attrs={'min': 0.01, 'step': '0.01'}),
            'type': forms.Select(),
            'category': forms.Select(),
            'date': forms.DateInput(attrs={'type': 'date'}),
        }


class CategoryForm(forms.ModelForm):
    class Meta:
        model = Category
        fields = ['name', 'description', 'monthly_limit']
        widgets = {
            'name': forms.TextInput(attrs={'placeholder': 'Наприклад: Продукти'}),
            'description': forms.Textarea(attrs={'rows': 3, 'placeholder': 'Опис категорії...'}),
            'monthly_limit': forms.NumberInput(attrs={'min': 0, 'step': '0.01'}),
        }
