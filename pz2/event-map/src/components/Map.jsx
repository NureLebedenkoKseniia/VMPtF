import React from 'react';

export default function Map({ events, onAddMarker, onSelectEvent }) {
  const handleMapClick = (e) => {
    // Prevent trigger when clicking on marker
    if (e.target.closest('.map-marker')) return;
    
    const rect = e.currentTarget.getBoundingClientRect();
    const x = ((e.clientX - rect.left) / rect.width) * 100;
    const y = ((e.clientY - rect.top) / rect.height) * 100;
    
    // Pass normalized percentage coordinates (0 to 100)
    onAddMarker(Math.round(x * 10) / 10, Math.round(y * 10) / 10);
  };

  const getMarkerColor = (category) => {
    switch (category) {
      case 'concert': return 'var(--color-concert)';
      case 'festival': return 'var(--color-festival)';
      case 'exhibition': return 'var(--color-exhibition)';
      default: return 'var(--color-primary)';
    }
  };

  return (
    <div className="glass-panel map-card" style={{ flexGrow: 1 }}>
      <div className="map-header">
        <div>
          <h2 className="map-title">Інтерактивна карта міста</h2>
          <p style={{ fontSize: '12px', color: 'var(--color-text-muted)', marginTop: '4px' }}>
            Клацніть на карту, щоб додати нову подію, або оберіть існуючий маркер
          </p>
        </div>
      </div>
      
      <div className="map-wrapper">
        <svg className="map-svg" viewBox="0 0 800 500" onClick={handleMapClick}>
          {/* Base Grid */}
          <rect width="800" height="500" className="map-bg" />
          
          <defs>
            <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
              <path d="M 40 0 L 0 0 0 40" fill="none" className="map-grid-line" />
            </pattern>
            
            {/* Glowing filters for markers */}
            <filter id="glow-concert-svg" x="-20%" y="-20%" width="140%" height="140%">
              <feGaussianBlur stdDeviation="5" result="blur" />
              <feComposite in="SourceGraphic" in2="blur" operator="over" />
            </filter>
          </defs>
          <rect width="800" height="500" fill="url(#grid)" />

          {/* Radar Waves and Sweep Line */}
          <circle cx="400" cy="250" r="100" className="radar-wave" />
          <circle cx="400" cy="250" r="200" className="radar-wave" />
          <circle cx="400" cy="250" r="300" className="radar-wave" />
          <circle cx="400" cy="250" r="10" className="radar-wave-ring" />
          <line x1="400" y1="250" x2="800" y2="250" className="radar-sweep-line" />

          {/* Parks (Background decorations) */}
          <rect x="50" y="60" width="180" height="100" rx="10" className="map-park" />
          <rect x="550" y="320" width="200" height="130" rx="15" className="map-park" />
          <circle cx="450" cy="120" r="70" className="map-park" />

          {/* River / Bay (glowing dark blue body) */}
          <path 
            d="M -20,250 C 150,220 280,300 400,280 C 520,260 620,180 820,150 L 820,0 L -20,0 Z" 
            className="map-water" 
          />

          {/* Styled City Blocks (Buildings) */}
          <rect x="80" y="240" width="60" height="80" rx="5" className="map-building" />
          <rect x="160" y="240" width="80" height="50" rx="5" className="map-building" />
          <rect x="80" y="340" width="140" height="60" rx="5" className="map-building" />
          
          <rect x="340" y="360" width="120" height="80" rx="8" className="map-building" />
          <rect x="480" y="360" width="50" height="50" rx="5" className="map-building" />

          <rect x="600" y="180" width="100" height="60" rx="5" className="map-building" />
          <rect x="710" y="210" width="50" height="80" rx="5" className="map-building" />

          {/* Roads Grid (Lines) */}
          <path d="M 0,200 L 800,200" className="map-road" />
          <path d="M 300,0 L 300,500" className="map-road" />
          <path d="M 570,0 L 570,500" className="map-road" />
          <path d="M 0,430 L 800,430" className="map-road" />
          
          <path d="M 120,200 L 120,500" className="map-road-sub" />
          <path d="M 720,200 L 720,500" className="map-road-sub" />
          <path d="M 300,330 L 570,330" className="map-road-sub" />

          {/* Render markers */}
          {events.map((event) => {
            const markerColor = getMarkerColor(event.category);
            const xPos = (event.x / 100) * 800;
            const yPos = (event.y / 100) * 500;
            
            return (
              <g 
                key={event.id} 
                className="map-marker"
                transform={`translate(${xPos}, ${yPos})`}
                onClick={() => onSelectEvent(event.id)}
              >
                {/* Glowing Pulse Ring */}
                <circle 
                  cx="0" 
                  cy="0" 
                  r="8" 
                  fill="none" 
                  stroke={markerColor} 
                  strokeWidth="2" 
                  className="pulse-ring" 
                />
                {/* Central Dot */}
                <circle 
                  cx="0" 
                  cy="0" 
                  r="6" 
                  fill={markerColor} 
                  stroke="#ffffff" 
                  strokeWidth="1.5"
                  style={{ filter: 'drop-shadow(0px 0px 6px ' + markerColor + ')' }}
                />
                
                {/* Tooltip on hover */}
                <title>{`${event.title} (${event.date})`}</title>
              </g>
            );
          })}
        </svg>
      </div>
    </div>
  );
}
