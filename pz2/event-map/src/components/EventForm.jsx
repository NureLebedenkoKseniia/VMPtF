import React, { useState } from 'react';

export default function EventForm({ x, y, onSave, onCancel }) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('concert');
  const [date, setDate] = useState('');
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!title.trim()) {
      setError('Будь ласка, введіть назву події.');
      return;
    }
    if (!date) {
      setError('Будь ласка, оберіть дату проведення.');
      return;
    }
    if (!email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
      setError('Будь ласка, введіть коректну email адресу.');
      return;
    }

    // Generate a beautiful placeholder banner based on category
    let bannerUrl = '';
    if (category === 'concert') {
      bannerUrl = 'https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&q=80&w=800';
    } else if (category === 'festival') {
      bannerUrl = 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&q=80&w=800';
    } else {
      bannerUrl = 'https://images.unsplash.com/photo-1492037766660-2a56f9eb3fcb?auto=format&fit=crop&q=80&w=800';
    }

    onSave({
      title,
      description: description || 'Опис події відсутній.',
      category,
      date,
      email,
      bannerUrl,
      x,
      y
    });
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-title">
          <span>Створення нової події</span>
          <button 
            type="button" 
            onClick={onCancel}
            style={{ background: 'none', border: 'none', color: 'var(--color-text-muted)', fontSize: '20px', cursor: 'pointer' }}
          >
            &times;
          </button>
        </div>
        
        {error && (
          <div style={{ color: '#842029', fontSize: '13px', marginBottom: '14px', padding: '10px', background: '#f8d7da', borderRadius: '6px', border: '1px solid #f5c2c7' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Назва події *</label>
            <input 
              type="text" 
              className="form-input" 
              placeholder="Наприклад: Рок-концерт 'Енергія'"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Опис події</label>
            <textarea 
              className="form-input" 
              rows="3"
              placeholder="Деталі про подію..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              style={{ resize: 'none' }}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Категорія *</label>
              <select 
                className="form-input" 
                value={category}
                onChange={(e) => setCategory(e.target.value)}
              >
                <option value="concert">Концерт</option>
                <option value="festival">Фестиваль</option>
                <option value="exhibition">Виставка</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Дата проведення *</label>
              <input 
                type="date" 
                className="form-input" 
                value={date}
                onChange={(e) => setDate(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Контактний Email організатора *</label>
            <input 
              type="email" 
              className="form-input" 
              placeholder="organizer@nure.ua"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div style={{ fontSize: '11px', color: 'var(--color-text-muted)', marginBottom: '20px' }}>
            Координати на карті: X: {x}%, Y: {y}%
          </div>

          <div className="modal-actions">
            <button type="button" className="btn" onClick={onCancel}>
              Скасувати
            </button>
            <button type="submit" className="btn btn-primary">
              Створити маркер
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
