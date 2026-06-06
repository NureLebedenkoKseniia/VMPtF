import React, { useState } from 'react';

const mockFriends = ['Олексій', 'Марія', 'Іван', 'Петро', 'Ольга'];

export default function ShareModal({ video, onShareSuccess, onCancel }) {
  const [selectedFriend, setSelectedFriend] = useState(mockFriends[0]);
  const [copied, setCopied] = useState(false);

  const videoUrl = window.location.origin + (video.videoUrl.startsWith('http') ? '' : '/api') + video.videoUrl;

  const handleCopyLink = () => {
    navigator.clipboard.writeText(videoUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleSendToFriend = () => {
    onShareSuccess(selectedFriend);
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-title">
          <span>Поділитися відео</span>
          <button 
            type="button" 
            onClick={onCancel}
            style={{ background: 'none', border: 'none', color: 'var(--color-text-muted)', fontSize: '20px', cursor: 'pointer' }}
          >
            &times;
          </button>
        </div>

        <div className="form-group">
          <label className="form-label">Посилання на відео</label>
          <div style={{ display: 'flex', gap: '8px' }}>
            <input 
              type="text" 
              className="form-input" 
              value={videoUrl} 
              readOnly 
              style={{ fontFamily: 'monospace', fontSize: '12px' }}
            />
            <button className="btn btn-primary" onClick={handleCopyLink} style={{ flexShrink: 0 }}>
              {copied ? 'Скопійовано' : 'Копіювати'}
            </button>
          </div>
        </div>

        <div style={{ margin: '20px 0', borderTop: '1px solid var(--border-color)', paddingTop: '20px' }}>
          <div className="form-group">
            <label className="form-label">Надіслати другу (Внутрішній чат)</label>
            <select 
              className="form-input" 
              value={selectedFriend} 
              onChange={(e) => setSelectedFriend(e.target.value)}
            >
              {mockFriends.map(friend => (
                <option key={friend} value={friend}>{friend}</option>
              ))}
            </select>
          </div>
          
          <button className="btn btn-accent" onClick={handleSendToFriend} style={{ width: '100%', justifyContent: 'center' }}>
            Надіслати другові
          </button>
        </div>
      </div>
    </div>
  );
}
