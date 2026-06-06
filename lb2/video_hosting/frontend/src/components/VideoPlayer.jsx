import React, { useState, useEffect } from 'react';

export default function VideoPlayer({ video, currentUser, userRole, isSubscribed, onSubscribeToggle, initialLiked, onLikeToggle, onShareClick, onBack }) {
  const [commentText, setCommentText] = useState('');
  const [comments, setComments] = useState(video.comments || []);
  const [likes, setLikes] = useState(video.likes || 0);
  const [liked, setLiked] = useState(initialLiked || false);

  // Sync comments and likes when video changes
  useEffect(() => {
    setComments(video.comments || []);
    setLikes(video.likes || 0);
  }, [video.id]);

  // Sync liked status when the initialLiked prop changes (e.g. user switch)
  useEffect(() => {
    setLiked(initialLiked || false);
  }, [initialLiked]);

  const handleLike = async () => {
    try {
      const response = await fetch(`/api/videos/${video.id}/like`, { 
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: currentUser })
      });
      if (response.ok) {
        const data = await response.json();
        setLikes(data.likes);
        setLiked(data.liked);
        if (onLikeToggle) {
          onLikeToggle(video.id, data.liked);
        }
      }
    } catch (err) {
      console.error('Error toggling like:', err);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Ви впевнені, що хочете видалити це відео з платформи?')) return;
    try {
      const response = await fetch(`/api/videos/${video.id}`, {
        method: 'DELETE'
      });
      if (response.ok) {
        onBack();
      } else {
        alert('Помилка при видаленні відео');
      }
    } catch (err) {
      console.error('Error deleting video:', err);
      alert('Помилка при видаленні відео');
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!commentText.trim()) return;

    try {
      const response = await fetch(`/api/videos/${video.id}/comments`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          user: currentUser,
          text: commentText
        })
      });

      if (response.ok) {
        const newComment = await response.json();
        setComments([...comments, newComment]);
        setCommentText('');
      }
    } catch (err) {
      console.error('Error adding comment:', err);
    }
  };

  // Build source url. Serves from backend uploads if relative path, otherwise loads absolute http link
  const videoSourceUrl = video.videoUrl.startsWith('http') ? video.videoUrl : video.videoUrl;

  return (
    <div className="player-layout">
      <div>
        {/* HTML5 Video Player */}
        <div className="video-stage">
          <video 
            key={video.id}
            src={videoSourceUrl} 
            controls 
            autoPlay
            className="stage-video"
          />
        </div>

        {/* Video Header Details */}
        <div className="video-header-details">
          <h2 style={{ fontFamily: 'var(--font-title)', fontSize: '20px', color: 'white', fontWeight: 700 }}>
            {video.title}
          </h2>
          
          <div className="video-header-row">
            <div className="creator-info">
              <div className="avatar">{video.creator[0]}</div>
              <div>
                <div className="creator-name">{video.creator}</div>
                <div className="creator-sub-count">Студія контенту</div>
              </div>
              
              {/* Channel Subscription Toggle (Level 4 Requirement) */}
              <button 
                className={`btn ${isSubscribed ? '' : 'btn-primary'}`} 
                onClick={() => onSubscribeToggle(video.creator)}
                style={{ padding: '6px 12px', fontSize: '12.5px', marginLeft: '12px' }}
              >
                {isSubscribed ? 'Ви підписані' : 'Підписатися'}
              </button>
            </div>

            <div className="action-group">
              <span style={{ display: 'inline-flex', alignItems: 'center', fontSize: '13px', color: 'var(--color-text-muted)', marginRight: '16px' }}>
                👁 {video.views} переглядів
              </span>
              
              <button 
                className={`btn ${liked ? 'btn-primary' : ''}`} 
                onClick={handleLike}
              >
                👍 {likes}
              </button>
              
              <button className="btn" onClick={onShareClick}>
                🔗 Поділитися
              </button>
              
              {(userRole === 'admin' || userRole === 'creator') ? (
                <button className="btn btn-danger" onClick={handleDelete}>
                  🗑 Видалити
                </button>
              ) : (
                <button className="btn btn-danger" onClick={onBack}>
                  Закрити
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Description Panel */}
        <div className="glass-panel" style={{ padding: '20px', marginBottom: '24px' }}>
          <h4 style={{ fontSize: '13px', color: 'var(--color-text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '8px' }}>
            Опис відео
          </h4>
          <p style={{ fontSize: '14.5px', lineHeight: '1.6', color: '#d1d5db' }}>
            {video.description}
          </p>
        </div>
      </div>

      {/* Right Column: Comments Section (Level 2 Requirement) */}
      <div className="glass-panel comments-card">
        <h3 className="comments-header">Коментарі ({comments.length})</h3>

        {/* Comment form */}
        <form onSubmit={handleAddComment} className="comment-input-area">
          <input 
            type="text" 
            className="form-input" 
            placeholder="Напишіть коментар як користувач..." 
            value={commentText}
            onChange={(e) => setCommentText(e.target.value)}
            required
            style={{ fontSize: '13px' }}
          />
          <button type="submit" className="btn btn-primary" style={{ padding: '8px 14px', fontSize: '12.5px', alignSelf: 'flex-end' }}>
            Прокоментувати
          </button>
        </form>

        {/* Comments scrolling listing */}
        <div className="comments-list-scroll">
          {comments.length === 0 ? (
            <div style={{ textAlign: 'center', color: 'var(--color-text-muted)', fontSize: '13px', padding: '30px 0' }}>
              Коментарів немає. Будьте першим, хто прокоментує!
            </div>
          ) : (
            comments.map((comment) => (
              <div key={comment.id} className="comment-item">
                <div className="comment-avatar">{comment.user[0]}</div>
                <div className="comment-body">
                  <div>
                    <span className="comment-user">{comment.user}</span>
                    <span className="comment-date">{comment.timestamp}</span>
                  </div>
                  <p className="comment-text">{comment.text}</p>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
