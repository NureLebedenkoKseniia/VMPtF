import React, { useState, useEffect } from 'react';
import VideoList from './components/VideoList';
import VideoPlayer from './components/VideoPlayer';
import UploadModal from './components/UploadModal';
import ShareModal from './components/ShareModal';

export default function App() {
  const [videos, setVideos] = useState([]);
  const [selectedVideo, setSelectedVideo] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterCategory, setFilterCategory] = useState('all');
  const [activeTab, setActiveTab] = useState('home');
  const [currentUser, setCurrentUser] = useState('Kseniya');
  const [profile, setProfile] = useState({ role: 'admin', subscriptions: [] });
  const [isUploading, setIsUploading] = useState(false);
  const [isSharing, setIsSharing] = useState(false);
  const [sharedVideo, setSharedVideo] = useState(null);
  const [notifications, setNotifications] = useState([]);

  // Fetch videos on load
  const fetchVideos = async () => {
    try {
      const response = await fetch('/api/videos');
      if (response.ok) {
        const data = await response.json();
        setVideos(data);
      }
    } catch (err) {
      console.error('Error fetching videos:', err);
    }
  };

  // Fetch user profile on load
  const fetchProfile = async () => {
    try {
      const response = await fetch(`/api/users/${currentUser}`);
      if (response.ok) {
        const data = await response.json();
        setProfile(data);
      }
    } catch (err) {
      console.error('Error fetching profile:', err);
    }
  };

  useEffect(() => {
    fetchVideos();
    fetchProfile();
  }, [currentUser]);

  // WebSocket connection for real-time video notifications
  useEffect(() => {
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsHost = window.location.port === '5173' ? 'localhost:3000' : window.location.host;
    const wsUrl = `${wsProtocol}//${wsHost}`;
    
    console.log('Attempting to connect to WebSocket at:', wsUrl);
    let socket;
    try {
      socket = new WebSocket(wsUrl);
      
      socket.onopen = () => {
        console.log('WebSocket connection successfully opened');
        // Register current user session with WebSocket server
        socket.send(JSON.stringify({ type: 'register', username: currentUser }));
      };
      
      socket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          console.log('Received WebSocket broadcast event:', data);
          if (data.type === 'new_video' && data.video) {
            // Update local videos array in real-time if not already present (prevents duplicates for uploader)
            setVideos(prev => {
              if (prev.some(v => v.id === data.video.id)) {
                return prev;
              }
              console.log('Adding new video to list in real-time:', data.video.title);
              return [data.video, ...prev];
            });

            // Trigger visual toast notification if user is subscribed to creator
            if (data.isSubscribed) {
              addNotification(`🔔 Нове відео від ${data.video.creator}: "${data.video.title}"!`);
            }
          }
        } catch (err) {
          console.error('Error parsing WebSocket message:', err);
        }
      };
      
      socket.onerror = (err) => {
        console.error('WebSocket connection error event:', err);
      };

      socket.onclose = (e) => {
        console.log('WebSocket connection closed event:', e);
      };
    } catch (e) {
      console.error('Failed to initialize WebSocket client:', e);
    }
    
    return () => {
      if (socket) {
        socket.close();
      }
    };
  }, [currentUser]);

  // Handle subscriptions (Level 4 Requirement)
  const handleSubscribeToggle = async (creatorName) => {
    try {
      const response = await fetch(`/api/users/${currentUser}/subscribe/${creatorName}`, {
        method: 'POST'
      });
      if (response.ok) {
        const data = await response.json();
        setProfile(prev => ({ ...prev, subscriptions: data.subscriptions }));
        
        // Show notification
        const statusMsg = data.status === 'subscribed' 
          ? `Ви підписалися на канал: ${creatorName}` 
          : `Ви скасували підписку на канал: ${creatorName}`;
          
        addNotification(statusMsg);
      }
    } catch (err) {
      console.error('Error toggling subscription:', err);
    }
  };

  const addNotification = (message) => {
    const newNotif = { id: Date.now(), message };
    setNotifications(prev => [...prev, newNotif]);
    setTimeout(() => {
      setNotifications(prev => prev.filter(n => n.id !== newNotif.id));
    }, 4000);
  };

  // Handle upload success (Level 2)
  const handleUploadSuccess = (newVideo) => {
    setVideos([newVideo, ...videos]);
    setIsUploading(false);
    
    // Notification for subscribers (Level 4)
    // If the creator of the video matches a channel the user is subscribed to, notify immediately
    if (profile.subscriptions.includes(newVideo.creator)) {
      addNotification(`🔔 Нове відео від ${newVideo.creator}: "${newVideo.title}"!`);
    } else {
      addNotification(`Успішно завантажено відео: "${newVideo.title}"`);
    }
  };

  // Handle mock video share (Level 3)
  const handleShareSuccess = (friendName) => {
    setIsSharing(false);
    addNotification(`📤 Поділилися відео з користувачем: ${friendName}!`);
  };

  // Handle toggling like in frontend profile state
  const handleLikeToggle = (videoId, isLiked) => {
    setProfile(prev => {
      const likedVideos = prev.likedVideos ? [...prev.likedVideos] : [];
      if (isLiked) {
        if (!likedVideos.includes(videoId)) likedVideos.push(videoId);
      } else {
        const index = likedVideos.indexOf(videoId);
        if (index !== -1) likedVideos.splice(index, 1);
      }
      return { ...prev, likedVideos };
    });
  };

  // Filter videos
  const filteredVideos = videos.filter((video) => {
    const matchesSearch = video.title.toLowerCase().includes(searchQuery.toLowerCase()) || 
                          video.description.toLowerCase().includes(searchQuery.toLowerCase());
    
    let matchesTab = true;
    if (activeTab === 'subscriptions') {
      matchesTab = profile.subscriptions.includes(video.creator);
    } else if (activeTab === 'my_channel') {
      matchesTab = video.creator === currentUser;
    }

    const matchesCategory = filterCategory === 'all' || video.category === filterCategory;

    return matchesSearch && matchesTab && matchesCategory;
  });

  return (
    <div className="layout">
      {/* Sidebar Nav */}
      <aside className="sidebar">
        <div className="sidebar-top">
          <div className="logo">
            <span style={{ fontSize: '24px' }}>⚡</span> TubeRadar
          </div>
          
          <ul className="nav-list">
            <li 
              className={`nav-item ${activeTab === 'home' ? 'active' : ''}`}
              onClick={() => { setActiveTab('home'); setSelectedVideo(null); }}
            >
              🏠 Головна
            </li>
            <li 
              className={`nav-item ${activeTab === 'subscriptions' ? 'active' : ''}`}
              onClick={() => { setActiveTab('subscriptions'); setSelectedVideo(null); }}
            >
              🔔 Підписки ({profile.subscriptions.length})
            </li>
            <li 
              className={`nav-item ${activeTab === 'my_channel' ? 'active' : ''}`}
              onClick={() => { setActiveTab('my_channel'); setSelectedVideo(null); }}
            >
              👤 Мій канал
            </li>
          </ul>
        </div>

        <div>
          {/* Change User Mock */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <label style={{ fontSize: '11px', color: 'var(--color-text-muted)', textTransform: 'uppercase', fontWeight: 'bold' }}>
              Змінити акаунт
            </label>
            <select 
              className="form-input" 
              value={currentUser} 
              onChange={(e) => setCurrentUser(e.target.value)}
              style={{ padding: '6px 12px', fontSize: '12px' }}
            >
              <option value="Kseniya">Ксенія (Адмін)</option>
              <option value="Ivan">Іван (Креатор)</option>
              <option value="Petro">Петро (Глядач)</option>
            </select>
          </div>
        </div>
      </aside>

      {/* Main Container */}
      <div className="main-content">
        {/* Top Header */}
        <header className="topbar">
          <div className="search-container">
            <input 
              type="text" 
              className="form-input" 
              placeholder="Пошук відео..." 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{ padding: '8px 16px', borderRadius: '20px' }}
            />
          </div>

          <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
            {/* Show Upload Button for Creator or Admin role (Level 3 Role Checks) */}
            {(profile.role === 'admin' || profile.role === 'creator') && (
              <button 
                className="btn btn-primary" 
                onClick={() => setIsUploading(true)}
                style={{ padding: '8px 16px', borderRadius: '20px' }}
              >
                + Завантажити
              </button>
            )}

            <div className="user-badge">
              <span className={`role-tag role-${profile.role}`}>{profile.role}</span>
              <div className="avatar">{currentUser[0]}</div>
              <div style={{ fontSize: '13px', fontWeight: '600' }}>{currentUser}</div>
            </div>
          </div>
        </header>

        {/* Dashboard Workspace */}
        <main className="workspace-scroll">
          {selectedVideo ? (
            <VideoPlayer 
              video={selectedVideo}
              currentUser={currentUser}
              userRole={profile.role}
              isSubscribed={profile.subscriptions.includes(selectedVideo.creator)}
              onSubscribeToggle={handleSubscribeToggle}
              initialLiked={profile.likedVideos && profile.likedVideos.includes(selectedVideo.id)}
              onLikeToggle={handleLikeToggle}
              onShareClick={() => { setSharedVideo(selectedVideo); setIsSharing(true); }}
              onBack={() => { setSelectedVideo(null); fetchVideos(); }}
            />
          ) : (
            <div>
              {/* Category tags */}
              <div style={{ display: 'flex', gap: '10px', marginBottom: '24px' }}>
                <button 
                  className={`btn ${filterCategory === 'all' ? 'btn-primary' : ''}`}
                  onClick={() => setFilterCategory('all')}
                  style={{ padding: '6px 14px', borderRadius: '15px', fontSize: '12.5px' }}
                >
                  Всі відео
                </button>
                <button 
                  className={`btn ${filterCategory === 'education' ? 'btn-primary' : ''}`}
                  onClick={() => setFilterCategory('education')}
                  style={{ padding: '6px 14px', borderRadius: '15px', fontSize: '12.5px' }}
                >
                  Навчання
                </button>
                <button 
                  className={`btn ${filterCategory === 'tech' ? 'btn-primary' : ''}`}
                  onClick={() => setFilterCategory('tech')}
                  style={{ padding: '6px 14px', borderRadius: '15px', fontSize: '12.5px' }}
                >
                  Технології
                </button>
                <button 
                  className={`btn ${filterCategory === 'creative' ? 'btn-primary' : ''}`}
                  onClick={() => setFilterCategory('creative')}
                  style={{ padding: '6px 14px', borderRadius: '15px', fontSize: '12.5px' }}
                >
                  Творчість
                </button>
              </div>

              {/* Title Section */}
              <h2 style={{ fontFamily: 'var(--font-title)', fontSize: '20px', marginBottom: '20px', fontWeight: '700' }}>
                {activeTab === 'home' && 'Рекомендовані відео'}
                {activeTab === 'subscriptions' && 'Відео ваших підписок'}
                {activeTab === 'my_channel' && 'Завантажені вами відео'}
              </h2>

              <VideoList 
                videos={filteredVideos} 
                onSelectVideo={setSelectedVideo} 
              />

              {filteredVideos.length === 0 && (
                <div style={{ textAlign: 'center', color: 'var(--color-text-muted)', padding: '50px 0' }}>
                  <h3>Відео не знайдено</h3>
                  <p style={{ fontSize: '13px', marginTop: '6px' }}>Спробуйте обрати іншу вкладку або категорію фільтрації</p>
                </div>
              )}
            </div>
          )}
        </main>
      </div>

      {/* Upload Modal Overlay */}
      {isUploading && (
        <UploadModal 
          creatorName={currentUser}
          onUploadSuccess={handleUploadSuccess}
          onCancel={() => setIsUploading(false)}
        />
      )}

      {/* Share Modal Overlay */}
      {isSharing && sharedVideo && (
        <ShareModal 
          video={sharedVideo}
          onShareSuccess={handleShareSuccess}
          onCancel={() => setIsSharing(false)}
        />
      )}

      {/* Notification Toast Box (Level 4 System Alerts) */}
      <div className="notif-box">
        {notifications.map(n => (
          <div key={n.id} className="notif-item">
            <span className="notif-icon">🔔</span>
            <div className="notif-body">{n.message}</div>
          </div>
        ))}
      </div>
    </div>
  );
}
