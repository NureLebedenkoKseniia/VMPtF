import React, { useState } from 'react';

export default function UploadModal({ creatorName, onUploadSuccess, onCancel }) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('education');
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      if (!selectedFile.type.startsWith('video/')) {
        setError('Будь ласка, оберіть відеофайл!');
        setFile(null);
        return;
      }
      setFile(selectedFile);
      setError('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file) {
      setError('Оберіть відеофайл для завантаження!');
      return;
    }
    if (!title.trim()) {
      setError('Вкажіть назву відео!');
      return;
    }

    setUploading(true);
    setError('');

    const formData = new FormData();
    formData.append('video', file);
    formData.append('title', title);
    formData.append('description', description);
    formData.append('category', category);
    formData.append('creator', creatorName);

    try {
      const response = await fetch('/api/videos/upload', {
        method: 'POST',
        body: formData
      });

      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.error || 'Помилка під час завантаження відео');
      }

      const newVideo = await response.json();
      onUploadSuccess(newVideo);
    } catch (err) {
      setError(err.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-title">
          <span>Завантажити нове відео</span>
          <button 
            type="button" 
            onClick={onCancel}
            style={{ background: 'none', border: 'none', color: 'var(--color-text-muted)', fontSize: '20px', cursor: 'pointer' }}
          >
            &times;
          </button>
        </div>

        {error && (
          <div style={{ color: 'var(--color-accent)', fontSize: '13px', marginBottom: '14px', padding: '10px', background: 'rgba(255, 42, 95, 0.1)', borderRadius: '6px', border: '1px solid rgba(255, 42, 95, 0.2)' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Відео файл *</label>
            <input 
              type="file" 
              accept="video/*" 
              className="form-input" 
              onChange={handleFileChange}
              required
            />
            {file && (
              <div style={{ fontSize: '11px', color: 'var(--color-secondary)', marginTop: '4px' }}>
                Розмір файлу: {(file.size / (1024 * 1024)).toFixed(2)} MB
              </div>
            )}
          </div>

          <div className="form-group">
            <label className="form-label">Назва відео *</label>
            <input 
              type="text" 
              className="form-input" 
              placeholder="Наприклад: Основи Node.js та Express"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Опис відео</label>
            <textarea 
              className="form-input" 
              rows="3" 
              placeholder="Короткий опис вмісту відео..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              style={{ resize: 'none' }}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Категорія *</label>
            <select 
              className="form-input" 
              value={category} 
              onChange={(e) => setCategory(e.target.value)}
            >
              <option value="education">Навчання</option>
              <option value="tech">Технології</option>
              <option value="creative">Творчість</option>
            </select>
          </div>

          <div className="modal-actions">
            <button type="button" className="btn" onClick={onCancel} disabled={uploading}>
              Скасувати
            </button>
            <button type="submit" className="btn btn-primary" disabled={uploading}>
              {uploading ? 'Завантаження...' : 'Завантажити відео'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
