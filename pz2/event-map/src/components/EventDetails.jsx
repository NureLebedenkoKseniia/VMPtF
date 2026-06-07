import React, { useState } from 'react';

export default function EventDetails({ event, onBack, onDelete }) {
  const [registered, setRegistered] = useState(false);

  const getCategoryClass = (category) => {
    switch (category) {
      case 'concert': return 'tag-concert';
      case 'festival': return 'tag-festival';
      case 'exhibition': return 'tag-exhibition';
      default: return '';
    }
  };

  const getCategoryName = (category) => {
    switch (category) {
      case 'concert': return 'Концерт';
      case 'festival': return 'Фестиваль';
      case 'exhibition': return 'Виставка';
      default: return category;
    }
  };

  const handleRegister = () => {
    setRegistered(true);
  };

  return (
    <div className="glass-panel detail-view" style={{ padding: '24px' }}>
      <button className="btn" onClick={onBack} style={{ marginBottom: '20px' }}>
        &larr; Назад до карти
      </button>

      <div 
        className="detail-banner" 
        style={{ backgroundImage: `url(${event.bannerUrl})` }}
      >
        <div className="detail-banner-overlay">
          <span className={`category-tag ${getCategoryClass(event.category)} detail-category`}>
            {getCategoryName(event.category)}
          </span>
          <h1 className="detail-title">{event.title}</h1>
          <p className="detail-date-badge">Дата проведення: {event.date}</p>
        </div>
      </div>

      <div className="detail-content-grid">
        <div>
          <div className="detail-card glass-panel" style={{ background: 'rgba(255,255,255,0.01)', border: 'none', padding: '0px' }}>
            <h3 className="detail-section-title">Опис події</h3>
            <p className="detail-description">{event.description}</p>
          </div>
        </div>

        <div>
          <div className="glass-panel" style={{ padding: '20px', background: 'rgba(255, 255, 255, 0.01)', borderRadius: '12px' }}>
            <h3 className="detail-section-title" style={{ fontSize: '14px' }}>Інформація</h3>
            
            <div className="info-item">
              <div className="info-icon" style={{ color: 'var(--color-festival)' }}>✉</div>
              <div>
                <p className="info-label">Організатор</p>
                <p className="info-val">{event.email}</p>
              </div>
            </div>

            <div className="info-item">
              <div className="info-icon">📍</div>
              <div>
                <p className="info-label">Локація на карті</p>
                <p className="info-val">X: {event.x}%, Y: {event.y}%</p>
              </div>
            </div>
            
            <div className="info-item">
              <div className="info-icon" style={{ color: 'var(--color-exhibition)' }}>⚡</div>
              <div>
                <p className="info-label">Статус</p>
                <p className="info-val" style={{ color: '#34d399' }}>Активний</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="detail-footer">
        <button 
          className="btn btn-danger" 
          onClick={() => onDelete(event.id)}
        >
          Видалити подію
        </button>

        {registered ? (
          <div style={{ color: '#34d399', fontWeight: '600', display: 'flex', alignItems: 'center', gap: '8px' }}>
            ✓ Ви успішно зареєструвалися на цю подію!
          </div>
        ) : (
          <button 
            className="btn btn-primary" 
            onClick={handleRegister}
          >
            Зареєструватися на подію
          </button>
        )}
      </div>
    </div>
  );
}
