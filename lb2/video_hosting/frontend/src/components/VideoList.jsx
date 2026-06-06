import React from 'react';

export default function VideoList({ videos, onSelectVideo }) {
  
  const getCategoryName = (category) => {
    switch (category) {
      case 'education': return 'Навчання';
      case 'tech': return 'Технології';
      case 'creative': return 'Творчість';
      default: return category;
    }
  };

  const getCategoryColor = (category) => {
    switch (category) {
      case 'education': return 'var(--color-primary)';
      case 'tech': return 'var(--color-secondary)';
      case 'creative': return 'var(--color-accent)';
      default: return 'white';
    }
  };

  const handleVideoClick = async (video) => {
    onSelectVideo(video);
    try {
      // Increment view count on backend
      await fetch(`/api/videos/${video.id}/view`, { method: 'POST' });
    } catch (err) {
      console.error('Error incrementing views:', err);
    }
  };

  return (
    <div className="video-grid">
      {videos.map((video) => (
        <div 
          key={video.id} 
          className="glass-panel video-card" 
          onClick={() => handleVideoClick(video)}
        >
          <div className="thumbnail-wrapper">
            <video 
              src={`${video.videoUrl}#t=0.1`} 
              className="thumbnail-img" 
              preload="metadata"
              muted
              playsInline
              style={{ objectFit: 'cover', width: '100%', height: '100%', display: 'block' }}
            />
            <span className="duration-tag">Відео</span>
          </div>

          <div className="video-info">
            <h3 className="video-title">{video.title}</h3>
            
            <div className="video-meta">
              <span className="creator-name" style={{ fontSize: '13px', margin: '2px 0 6px 0' }}>
                👤 {video.creator}
              </span>
              
              <div className="video-meta-row">
                <span>👁 {video.views} переглядів</span>
                <span 
                  className="role-tag" 
                  style={{ 
                    color: getCategoryColor(video.category), 
                    borderColor: 'rgba(255,255,255,0.06)', 
                    background: 'rgba(255,255,255,0.02)',
                    fontSize: '9px'
                  }}
                >
                  {getCategoryName(video.category)}
                </span>
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
