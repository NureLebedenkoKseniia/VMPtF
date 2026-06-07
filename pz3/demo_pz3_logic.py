import os
import re
import hashlib
import random

def demo_level_1():
    print("\n--- Рівень 1: Частка чисел (a / b) ---")
    try:
        a_str = input("Введіть число a: ")
        b_str = input("Введіть число b: ")
        a = float(a_str)
        b = float(b_str)
        if b == 0:
            print("Помилка: Ділення на нуль неможливе!")
        else:
            res = a / b
            print(f"Результат (a / b): {res:.4f}")
    except ValueError:
        print("Помилка: Будь ласка, введіть коректні числа.")

def demo_level_2():
    print("\n--- Рівень 2: Гра «Кістки» ---")
    try:
        count_str = input("Введіть кількість кубиків (1-4, за замовчуванням 2): ")
        count = int(count_str) if count_str.strip() else 2
        if count < 1 or count > 4:
            count = 2
    except ValueError:
        count = 2

    print(f"Кидаємо {count} кубиків...")
    p1_dice = [random.randint(1, 6) for _ in range(count)]
    p2_dice = [random.randint(1, 6) for _ in range(count)]
    
    sum1 = sum(p1_dice)
    sum2 = sum(p2_dice)
    
    print(f"Гравець: {p1_dice} (Сума: {sum1})")
    print(f"Суперник: {p2_dice} (Сума: {sum2})")
    
    if sum1 > sum2:
        print(f"Переміг Гравець! ({sum1} > {sum2})")
    elif sum2 > sum1:
        print(f"Переміг Суперник! ({sum2} > {sum1})")
    else:
        print(f"Нічия! ({sum1} = {sum2})")

def demo_level_3():
    print("\n--- Рівень 3: Аналізатор тексту (Кількість речень) ---")
    print("Створюємо тимчасовий текстовий файл 'temp_demo.txt' для аналізу...")
    sample_text = (
        "Привіт! Це демонстраційний файл для ПЗ3. "
        "Він містить кілька речень... Наприклад, це третє речення! "
        "А це вже четверте?"
    )
    with open("temp_demo.txt", "w", encoding="utf-8") as f:
        f.write(sample_text)
    
    print(f"Вміст файлу:\n\"{sample_text}\"")
    
    # Analyze text
    letters = 0
    spaces = 0
    symbols = 0
    
    for char in sample_text:
        if char.isalpha():
            letters += 1
        elif char.isspace() and char not in ['\n', '\r']:
            spaces += 1
        elif char not in ['\n', '\r']:
            symbols += 1
            
    # Regex matching sentence endings similar to Kotlin: (?<=[.!?])\s+|(?<=\.\.\.)\s+
    sentences = re.split(r'(?<=[.!?])\s+|(?<=\.\.\.)\s+', sample_text.strip())
    sentences = [s for s in sentences if s.strip()]
    sentence_count = len(sentences)
    
    print("-" * 40)
    print(f"Всього символів: {len(sample_text)}")
    print(f"Кількість літер: {letters}")
    print(f"Кількість пробілів: {spaces}")
    print(f"Кількість інших знаків: {symbols}")
    print(f"Кількість речень: {sentence_count}")
    print("-" * 40)
    
    # Cleanup
    if os.path.exists("temp_demo.txt"):
        os.remove("temp_demo.txt")

def calculate_hash(filepath):
    digest = hashlib.sha256()
    with open(filepath, "rb") as f:
        while chunk := f.read(8192):
            digest.update(chunk)
    return digest.hexdigest()

def demo_level_4():
    print("\n--- Рівень 4: Пошук та видалення дублікатів ---")
    print("Створюємо тимчасову папку 'temp_duplicates' з копіями...")
    os.makedirs("temp_duplicates", exist_ok=True)
    os.makedirs("temp_duplicates/subfolder", exist_ok=True)
    
    # Create duplicate files
    content1 = "Хлорофіл та фотосинтез"
    content2 = "Унікальні дані"
    
    with open("temp_duplicates/file1.txt", "w", encoding="utf-8") as f:
        f.write(content1)
    with open("temp_duplicates/subfolder/file1_copy.txt", "w", encoding="utf-8") as f:
        f.write(content1) # duplicate
    with open("temp_duplicates/file2.txt", "w", encoding="utf-8") as f:
        f.write(content2) # unique
        
    print("Файлова структура створена. Скануємо...")
    
    # Scan directory recursively
    files_info = []
    for root, dirs, filenames in os.walk("temp_duplicates"):
        for filename in filenames:
            filepath = os.path.join(root, filename)
            filehash = calculate_hash(filepath)
            files_info.append({
                "path": filepath,
                "name": filename,
                "hash": filehash,
                "size": os.path.getsize(filepath)
            })
            
    # Group by hash
    groups = {}
    for f in files_info:
        groups.setdefault(f["hash"], []).append(f)
        
    duplicates = {h: g for h, g in groups.items() if len(g) > 1}
    
    if not duplicates:
        print("Дублікатів не знайдено.")
    else:
        for filehash, files_list in duplicates.items():
            print(f"\nЗнайдено дублікати для файлу '{files_list[0]['name']}' (хеш: {filehash[:16]}...):")
            for idx, file in enumerate(files_list):
                if idx == 0:
                    print(f"  [Оригінал] -> {file['path']}")
                else:
                    print(f"  [Копія]    -> {file['path']} (видаляємо...)")
                    os.remove(file["path"]) # delete copy
                    print("  Успішно видалено.")
                    
    # Cleanup temp folder
    import shutil
    if os.path.exists("temp_duplicates"):
        shutil.rmtree("temp_duplicates")

def main():
    print("====================================================")
    print("      Демонстрація алгоритмів ПЗ3 на комп'ютері")
    print("====================================================")
    while True:
        print("\nОберіть рівень для перевірки:")
        print("1. Рівень 1 (Частка a / b)")
        print("2. Рівень 2 (Гра «Кістки»)")
        print("3. Рівень 3 (Аналізатор файлу)")
        print("4. Рівень 4 (Пошук дублікатів)")
        print("5. Вийти з демонстрації")
        
        choice = input("Ваш вибір (1-5): ").strip()
        if choice == '1':
            demo_level_1()
        elif choice == '2':
            demo_level_2()
        elif choice == '3':
            demo_level_3()
        elif choice == '4':
            demo_level_4()
        elif choice == '5':
            print("Вихід з демонстрації.")
            break
        else:
            print("Некоректний вибір. Спробуйте ще раз.")

if __name__ == '__main__':
    main()
