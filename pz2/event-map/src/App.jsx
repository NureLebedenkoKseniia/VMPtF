import React, { useState } from 'react';
import Map from './components/Map';
import EventForm from './components/EventForm';
import EventDetails from './components/EventDetails';

const initialEvents = [
  {
    id: '1',
    title: 'NURE Tech Forum 2026',
    description: 'Головний технологічний форум університету ХНУРЕ. Презентації студентських стартапів, воркшопи з ШІ та зустрічі з ІТ-роботодавцями.',
    category: 'exhibition',
    date: '2026-06-15',
    email: 'tech.forum@nure.ua',
    bannerUrl: 'https://images.unsplash.com/photo-1492037766660-2a56f9eb3fcb?auto=format&fit=crop&q=80&w=800',
    x: 45,
    y: 35
  },
  {
    id: '2',
    title: 'Весняний Фестиваль Студентів',
    description: 'Велика вечірка на відкритому повітрі. Жива музика від студентських гуртів, смачна вулична їжа та конкурси.',
    category: 'festival',
    date: '2026-06-20',
    email: 'stud.union@nure.ua',
    bannerUrl: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&q=80&w=800',
    x: 18,
    y: 75
  },
  {
    id: '3',
    title: 'Благодійний Рок-концерт',
    description: 'Рок-концерт на підтримку Збройних Сил України. Виступ відомих харківських виконавців у актовій залі ХНУРЕ.',
    category: 'concert',
    date: '2026-06-25',
    email: 'charity.rock@nure.ua',
    bannerUrl: 'https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&q=80&w=800',
    x: 65,
    y: 22
  }
];

export default function App() {
  const [events, setEvents] = useState(initialEvents);
  const [selectedEventId, setSelectedEventId] = useState(null);
  const [isAdding, setIsAdding] = useState(false);
  const [newCoords, setNewCoords] = useState({ x: 0, y: 0 });
  const [searchQuery, setSearchQuery] = useState('');
  const [filterCategory, setFilterCategory] = useState('all');
  const [isListOpen, setIsListOpen] = useState(true);

  const handleMapClick = (x, y) => {
    setNewCoords({ x, y });
    setIsAdding(true);
  };

  const handleSaveEvent = (eventData) => {
    const newEvent = {
      id: Date.now().toString(),
      ...eventData
    };
    setEvents([...events, newEvent]);
    setIsAdding(false);
  };

  const handleDeleteEvent = (id) => {
    setEvents(events.filter((e) => e.id !== id));
    setSelectedEventId(null);
  };

  const selectedEvent = events.find((e) => e.id === selectedEventId);

  // Filter events based on search query and category
  const filteredEvents = events.filter((e) => {
    const matchesSearch = e.title.toLowerCase().includes(searchQuery.toLowerCase()) || 
                          e.description.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = filterCategory === 'all' || e.category === filterCategory;
    return matchesSearch && matchesCategory;
  });

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

  return (
    <div className="app-container">
      <header className="header">
        <div>
          <h1 className="header-title">EventRadar NURE</h1>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '13px', marginTop: '4px' }}>
            Інтерактивний менеджер міських та університетських подій
          </p>
        </div>
        <div className="header-meta" style={{ textAlign: 'right' }}>
          <div>Лебеденко Ксенія, ПЗПІ-23-9</div>
          <div style={{ color: 'var(--color-concert)', marginTop: '2px', fontWeight: 'bold' }}>Варіант 6</div>
        </div>
      </header>

      {selectedEvent ? (
        <EventDetails 
          event={selectedEvent} 
          onBack={() => setSelectedEventId(null)} 
          onDelete={handleDeleteEvent}
        />
      ) : (
        <div className="main-grid">
          {/* Map Section */}
          <Map 
            events={filteredEvents} 
            onAddMarker={handleMapClick} 
            onSelectEvent={setSelectedEventId}
          />

          {/* Floating Toggle Button */}
          <button 
            className="btn radar-toggle-btn" 
            onClick={() => setIsListOpen(!isListOpen)}
          >
            {isListOpen ? '✕ Сховати список' : '☰ Радарний список'}
          </button>

          {/* List Section (Floating Drawer) */}
          <div className={`glass-panel list-card ${isListOpen ? '' : 'collapsed'}`}>
            <h2 className="list-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span>Сигнали радара ({filteredEvents.length})</span>
            </h2>
            
            {/* Filter controls */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '16px' }}>
              <input 
                type="text" 
                className="form-input" 
                placeholder="Пошук сигналів..." 
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <div style={{ display: 'flex', gap: '8px' }}>
                <button 
                  className={`btn ${filterCategory === 'all' ? 'btn-primary' : ''}`} 
                  onClick={() => setFilterCategory('all')}
                  style={{ flexGrow: 1, padding: '6px 8px', fontSize: '11px' }}
                >
                  Всі
                </button>
                <button 
                  className={`btn ${filterCategory === 'concert' ? 'btn-primary' : ''}`} 
                  onClick={() => setFilterCategory('concert')}
                  style={{ flexGrow: 1, padding: '6px 8px', fontSize: '11px' }}
                >
                  Концерти
                </button>
                <button 
                  className={`btn ${filterCategory === 'festival' ? 'btn-primary' : ''}`} 
                  onClick={() => setFilterCategory('festival')}
                  style={{ flexGrow: 1, padding: '6px 8px', fontSize: '11px' }}
                >
                  Фестивалі
                </button>
                <button 
                  className={`btn ${filterCategory === 'exhibition' ? 'btn-primary' : ''}`} 
                  onClick={() => setFilterCategory('exhibition')}
                  style={{ flexGrow: 1, padding: '6px 8px', fontSize: '11px' }}
                >
                  Виставки
                </button>
              </div>
            </div>

            {/* Scrollable list */}
            <div className="event-items-scroll">
              {filteredEvents.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-icon">🛰️</div>
                  <p>Сигналів не знайдено</p>
                  <p style={{ fontSize: '11px' }}>Клацніть на карту, щоб додати новий маркер</p>
                </div>
              ) : (
                filteredEvents.map((e) => (
                  <div 
                    key={e.id} 
                    className="event-item-card"
                    onClick={() => setSelectedEventId(e.id)}
                  >
                    <div className="event-item-info">
                      <h4 className="event-item-title">{e.title}</h4>
                      <span className="event-item-meta">📅 {e.date}</span>
                    </div>
                    <span className={`category-tag ${getCategoryClass(e.category)}`}>
                      {getCategoryName(e.category)}
                    </span>
                  </div>
                ))
              )}
            </div>
            
            {/* Quick stats footer */}
            <div style={{ marginTop: '16px', paddingTop: '16px', borderTop: '1px solid var(--border-color)', fontSize: '12px', color: 'var(--color-text-muted)', display: 'flex', justifyContent: 'space-between' }}>
              <span>Всього точок: {events.length}</span>
              <span>Показано: {filteredEvents.length}</span>
            </div>
          </div>
        </div>
      )}

      {/* Modal Form */}
      {isAdding && (
        <EventForm 
          x={newCoords.x} 
          y={newCoords.y} 
          onSave={handleSaveEvent} 
          onCancel={() => setIsAdding(false)}
        />
      )}
    </div>
  );
}
