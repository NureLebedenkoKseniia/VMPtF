from django.shortcuts import render, get_object_or_404, redirect
from django.db.models import Sum, Q
from .models import Transaction, Category
from .forms import TransactionForm, CategoryForm

def transaction_list(request):
    """View to list, search, filter, and sort transactions"""
    transactions = Transaction.objects.all()
    categories = Category.objects.all()

    # Search (Level 3)
    q = request.GET.get('q', '').strip()
    if q:
        transactions = transactions.filter(Q(description__icontains=q))

    # Category Filter (Level 3)
    category_id = request.GET.get('category', '')
    if category_id:
        transactions = transactions.filter(category_id=category_id)

    # Type Filter (Level 3)
    tx_type = request.GET.get('type', '')
    if tx_type:
        transactions = transactions.filter(type=tx_type)

    # Sorting (Level 3)
    sort_by = request.GET.get('sort', '-date')
    valid_sorts = ['amount', '-amount', 'date', '-date']
    if sort_by in valid_sorts:
        transactions = transactions.order_by(sort_by)

    context = {
        'transactions': transactions,
        'categories': categories,
        'q': q,
        'selected_category': category_id,
        'selected_type': tx_type,
        'selected_sort': sort_by,
    }
    return render(request, 'transactions_app/transaction_list.html', context)


def transaction_create(request):
    """Create new transaction (Level 2)"""
    if request.method == 'POST':
        form = TransactionForm(request.POST)
        if form.is_valid():
            form.save()
            return redirect('transaction_list')
    else:
        form = TransactionForm()
    return render(request, 'transactions_app/transaction_form.html', {'form': form, 'title': 'Додати транзакцію'})


def transaction_edit(request, pk):
    """Edit transaction (Level 2)"""
    transaction_obj = get_object_or_404(Transaction, pk=pk)
    if request.method == 'POST':
        form = TransactionForm(request.POST, instance=transaction_obj)
        if form.is_valid():
            form.save()
            return redirect('transaction_list')
    else:
        form = TransactionForm(instance=transaction_obj)
    return render(request, 'transactions_app/transaction_form.html', {'form': form, 'title': 'Редагувати транзакцію'})


def transaction_delete(request, pk):
    """Delete transaction (Level 2)"""
    transaction_obj = get_object_or_404(Transaction, pk=pk)
    if request.method == 'POST':
        transaction_obj.delete()
        return redirect('transaction_list')
    return render(request, 'transactions_app/transaction_confirm_delete.html', {'transaction': transaction_obj})


def category_list(request):
    """View transactions grouped by category (Level 2)"""
    categories = Category.objects.all()
    # We will build a list of categories with their transactions
    category_data = []
    for cat in categories:
        txs = cat.transactions.all()
        total_spent = txs.filter(type='credit').aggregate(s=Sum('amount'))['s'] or 0
        category_data.append({
            'category': cat,
            'transactions': txs,
            'total_spent': total_spent,
        })
    
    # Categoriless transactions
    uncategorized_txs = Transaction.objects.filter(category__isnull=True)
    
    return render(request, 'transactions_app/category_list.html', {
        'category_data': category_data,
        'uncategorized_transactions': uncategorized_txs
    })


def category_create(request):
    """Create a new category"""
    if request.method == 'POST':
        form = CategoryForm(request.POST)
        if form.is_valid():
            form.save()
            return redirect('category_list')
    else:
        form = CategoryForm()
    return render(request, 'transactions_app/category_form.html', {'form': form})


def budgeting_dashboard(request):
    """Budgeting system dashboard to track income & expenses (Level 4)"""
    # Totals
    total_income = Transaction.objects.filter(type='debit').aggregate(s=Sum('amount'))['s'] or 0
    total_expense = Transaction.objects.filter(type='credit').aggregate(s=Sum('amount'))['s'] or 0
    balance = total_income - total_expense
    
    # Categories limit vs actual
    categories = Category.objects.all()
    budget_data = []
    
    for idx, cat in enumerate(categories):
        actual_expense = cat.transactions.filter(type='credit').aggregate(s=Sum('amount'))['s'] or 0
        limit = cat.monthly_limit
        
        # Calculate heights/percentages for progress bars and SVG charts
        pct = 0
        if limit > 0:
            pct = min((actual_expense / limit) * 100, 100)
            
        budget_data.append({
            'name': cat.name,
            'limit': float(limit),
            'actual': float(actual_expense),
            'percentage': round(pct, 1),
            # SVG layout params
            'bar_x': idx * 80 + 70,
            'text_x': idx * 80 + 90,
            'actual_height': (float(actual_expense) / float(limit) * 150) if limit > 0 else 0,
            'limit_height': 150
        })

    # Prepare SVG graph metrics (Income vs Expense simple comparison bar chart)
    # Scaled to 150px height
    max_val = max(float(total_income), float(total_expense), 100.0)
    inc_height = (float(total_income) / max_val) * 150
    exp_height = (float(total_expense) / max_val) * 150
    
    context = {
        'total_income': total_income,
        'total_expense': total_expense,
        'balance': balance,
        'budget_data': budget_data,
        'inc_height': inc_height,
        'exp_height': exp_height,
        'inc_y': 180 - inc_height,
        'exp_y': 180 - exp_height,
    }
    return render(request, 'transactions_app/reports.html', context)
