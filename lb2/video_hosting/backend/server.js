const express = require('express');
const cors = require('cors');
const multer = require('multer');
const compression = require('compression');
const path = require('path');
const fs = require('fs');
const http = require('http');
const { WebSocketServer } = require('ws');

const app = express();
const PORT = process.env.PORT || 3000;

const server = http.createServer(app);
const wss = new WebSocketServer({ server });

// Mapped clients: username -> ws socket connection
const clients = new Map();

wss.on('connection', (ws) => {
  console.log('New WebSocket client connected');
  
  ws.on('message', (message) => {
    try {
      const messageString = message.toString();
      console.log('Received WebSocket message:', messageString);
      const data = JSON.parse(messageString);
      
      if (data.type === 'register' && data.username) {
        clients.set(data.username, ws);
        console.log(`Successfully registered WebSocket connection for user: ${data.username}`);
      }
    } catch (err) {
      console.error('WebSocket message parsing error:', err);
    }
  });

  ws.on('close', () => {
    console.log('WebSocket client connection closed');
    for (let [username, clientWs] of clients.entries()) {
      if (clientWs === ws) {
        clients.delete(username);
        console.log(`Cleaned up WebSocket connection mapping for user: ${username}`);
        break;
      }
    }
  });
});

// Enable compression (gzip) - Level 4 Requirement
app.use(compression());
app.use(cors());
app.use(express.json());

// Set up uploads directory
const UPLOADS_DIR = path.join(__dirname, 'uploads');
if (!fs.existsSync(UPLOADS_DIR)) {
  fs.mkdirSync(UPLOADS_DIR);
}
app.use('/uploads', express.static(UPLOADS_DIR));

// Database paths
const VIDEOS_DB_PATH = path.join(__dirname, 'db_videos.json');
const USERS_DB_PATH = path.join(__dirname, 'db_users.json');

// Helper functions for DB
function readJSON(filePath, defaultData) {
  try {
    if (!fs.existsSync(filePath)) {
      fs.writeFileSync(filePath, JSON.stringify(defaultData, null, 2));
      return defaultData;
    }
    const data = fs.readFileSync(filePath, 'utf-8');
    return JSON.parse(data);
  } catch (error) {
    console.error(`Error reading ${filePath}:`, error);
    return defaultData;
  }
}

function writeJSON(filePath, data) {
  try {
    fs.writeFileSync(filePath, JSON.stringify(data, null, 2));
  } catch (error) {
    console.error(`Error writing ${filePath}:`, error);
  }
}

// Initial Data
const defaultVideos = [
  {
    id: 'video_1',
    title: 'Кумедна пригода кролика та метелика',
    description: 'Короткий кумедний анімаційний уривок, у якому пухнастий кролик спостерігає за чарівним метеликом, аж поки йому на голову раптово не падає яблуко.',
    category: 'creative',
    videoUrl: 'https://www.w3schools.com/html/mov_bbb.mp4',
    thumbnailUrl: 'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?auto=format&fit=crop&q=80&w=400',
    creator: 'Анімаційна Студія',
    views: 1240,
    likes: 342,
    uploadedAt: '2026-05-20',
    comments: [
      { id: 'c1', user: 'Олексій', text: 'Ахах, кролик отримав яблуком по голові! Дуже кумедно.', timestamp: '2026-05-21' },
      { id: 'c2', user: 'Марія', text: 'Чудова класична анімація, дуже піднімає настрій.', timestamp: '2026-05-22' }
    ]
  },
  {
    id: 'video_2',
    title: 'Пришвидшена макрозйомка: як розпускається квітка',
    description: 'Неймовірні кадри пришвидшеної зйомки (time-lapse), що фіксують таїнство розкриття квіткового бутона в макрорежимі.',
    category: 'creative',
    videoUrl: 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
    thumbnailUrl: 'https://images.unsplash.com/photo-1534067783941-51c9c23ecefd?auto=format&fit=crop&q=80&w=400',
    creator: 'Mozilla MDN',
    views: 852,
    likes: 120,
    uploadedAt: '2026-05-25',
    comments: [
      { id: 'c3', user: 'Ігор', text: 'Неймовірна краса макрозйомки! Природа — найкращий художник.', timestamp: '2026-05-26' },
      { id: 'c4', user: 'Світлана', text: 'Можна дивитися нескінченно, пелюстки розгортаються дуже граціозно.', timestamp: '2026-05-27' }
    ]
  },
  {
    id: 'video_3',
    title: 'Бурий ведмідь-рибалка посеред річки в оточенні чайок',
    description: 'Кадри дикої природи: великий бурий ведмідь намагається зловити рибу в бурхливих потоках води, тоді як навколо нього збирається зграя білих чайок, сподіваючись отримати легку здобич.',
    category: 'education',
    videoUrl: 'https://www.w3schools.com/html/movie.mp4',
    thumbnailUrl: 'https://images.unsplash.com/photo-1530595467537-0b5996c41f2d?auto=format&fit=crop&q=80&w=400',
    creator: 'Дикий Світ',
    views: 2310,
    likes: 540,
    uploadedAt: '2026-05-30',
    comments: [
      { id: 'c5', user: 'Дмитро', text: 'Оце так риболовля! Чайки просто чекають на халяву.', timestamp: '2026-05-31' },
      { id: 'c6', user: 'Ганна', text: 'Дика природа дивовижна. Ведмідь такий зосереджений.', timestamp: '2026-06-01' }
    ]
  }
];


const defaultUsers = {
  // Key represents username/id
  "Kseniya": {
    role: "admin", // admin, creator, viewer
    subscriptions: ["Професор IT"], // subscribed channels
    likedVideos: []
  },
  "Ivan": {
    role: "creator",
    subscriptions: [],
    likedVideos: []
  },
  "Petro": {
    role: "viewer",
    subscriptions: ["Blender Foundation"],
    likedVideos: []
  }
};

// Initialize DBs
let videos = readJSON(VIDEOS_DB_PATH, defaultVideos);
let users = readJSON(USERS_DB_PATH, defaultUsers);

// Multer storage configuration for uploads (Level 2 Requirement)
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, UPLOADS_DIR);
  },
  filename: function (req, file, cb) {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
  }
});
const upload = multer({ 
  storage: storage,
  fileFilter: function (req, file, cb) {
    // accept video files only
    const filetypes = /mp4|mov|avi|mkv/i;
    const extname = filetypes.test(path.extname(file.originalname).toLowerCase());
    const mimetype = filetypes.test(file.mimetype);
    if (mimetype && extname) {
      return cb(null, true);
    } else {
      cb(new Error('Помилка: Завантажувати можна тільки відеофайли!'));
    }
  }
});

// REST API Routes

// 1. Get all videos (Level 1)
app.get('/api/videos', (req, res) => {
  res.json(videos);
});

// 2. Upload video (Level 2)
app.post('/api/videos/upload', upload.single('video'), (req, res) => {
  try {
    const { title, description, category, creator } = req.body;
    
    if (!title || !category || !creator) {
      return res.status(400).json({ error: 'Заповніть усі обов\'язкові поля' });
    }
    
    let videoUrl = '';
    if (req.file) {
      videoUrl = `/uploads/${req.file.filename}`;
    } else {
      return res.status(400).json({ error: 'Відео файл не завантажено' });
    }

    // Default thumbnails based on categories
    let thumbnailUrl = 'https://images.unsplash.com/photo-1485846234645-a62644f84728?auto=format&fit=crop&q=80&w=400';
    if (category === 'education') {
      thumbnailUrl = 'https://images.unsplash.com/photo-1618401471353-b98aedd07871?auto=format&fit=crop&q=80&w=400';
    } else if (category === 'tech') {
      thumbnailUrl = 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&q=80&w=400';
    } else if (category === 'creative') {
      thumbnailUrl = 'https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?auto=format&fit=crop&q=80&w=400';
    }

    const newVideo = {
      id: 'video_' + Date.now(),
      title,
      description: description || 'Опис відсутній',
      category,
      videoUrl,
      thumbnailUrl,
      creator,
      views: 0,
      likes: 0,
      uploadedAt: new Date().toISOString().split('T')[0],
      comments: []
    };

    videos.unshift(newVideo); // add to top
    writeJSON(VIDEOS_DB_PATH, videos);

    // Send real-time notification and full video data to all online clients
    wss.clients.forEach(client => {
      if (client.readyState === 1) { // 1 = OPEN
        // Find which username belongs to this client
        let clientUsername = null;
        for (let [username, ws] of clients.entries()) {
          if (ws === client) {
            clientUsername = username;
            break;
          }
        }

        // Check if this client is subscribed to the creator of the uploaded video
        let isSubscribed = false;
        if (clientUsername && users[clientUsername]) {
          const user = users[clientUsername];
          if (user.subscriptions && user.subscriptions.includes(creator)) {
            isSubscribed = true;
          }
        }

        console.log(`Broadcasting new video upload to client: ${clientUsername || 'anonymous'}, isSubscribed: ${isSubscribed}`);
        client.send(JSON.stringify({
          type: 'new_video',
          video: newVideo,
          isSubscribed
        }));
      }
    });
    
    res.status(201).json(newVideo);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 3. Increment views
app.post('/api/videos/:id/view', (req, res) => {
  const { id } = req.params;
  const video = videos.find(v => v.id === id);
  if (video) {
    video.views += 1;
    writeJSON(VIDEOS_DB_PATH, videos);
    return res.json({ success: true, views: video.views });
  }
  res.status(404).json({ error: 'Відео не знайдено' });
});

// 4. Like/Unlike video (Toggle)
app.post('/api/videos/:id/like', (req, res) => {
  const { id } = req.params;
  const { username } = req.body;
  
  const video = videos.find(v => v.id === id);
  if (!video) {
    return res.status(404).json({ error: 'Відео не знайдено' });
  }
  
  if (!username) {
    return res.status(400).json({ error: 'Користувача не вказано' });
  }
  
  if (!users[username]) {
    users[username] = { role: 'viewer', subscriptions: [], likedVideos: [] };
  }
  
  const user = users[username];
  if (!user.likedVideos) {
    user.likedVideos = [];
  }
  
  const index = user.likedVideos.indexOf(id);
  let isLiked = false;
  
  if (index === -1) {
    user.likedVideos.push(id);
    video.likes += 1;
    isLiked = true;
  } else {
    user.likedVideos.splice(index, 1);
    video.likes = Math.max(0, video.likes - 1);
    isLiked = false;
  }
  
  writeJSON(VIDEOS_DB_PATH, videos);
  writeJSON(USERS_DB_PATH, users);
  
  res.json({ success: true, likes: video.likes, liked: isLiked });
});

// 5. Add Comment (Level 2)
app.post('/api/videos/:id/comments', (req, res) => {
  const { id } = req.params;
  const { user, text } = req.body;

  if (!user || !text) {
    return res.status(400).json({ error: 'Вкажіть ім\'я користувача та текст коментаря' });
  }

  const video = videos.find(v => v.id === id);
  if (video) {
    const newComment = {
      id: 'comment_' + Date.now(),
      user,
      text,
      timestamp: new Date().toISOString().split('T')[0]
    };
    video.comments.push(newComment);
    writeJSON(VIDEOS_DB_PATH, videos);
    return res.status(201).json(newComment);
  }
  res.status(404).json({ error: 'Відео не знайдено' });
});

// 5.5. Delete Video
app.delete('/api/videos/:id', (req, res) => {
  const { id } = req.params;
  const index = videos.findIndex(v => v.id === id);
  if (index !== -1) {
    const videoToDelete = videos[index];
    // Delete physical file if it was uploaded locally
    if (videoToDelete.videoUrl.startsWith('/uploads/')) {
      const filePath = path.join(__dirname, videoToDelete.videoUrl);
      if (fs.existsSync(filePath)) {
        try {
          fs.unlinkSync(filePath);
        } catch (err) {
          console.error('Error deleting video file:', err);
        }
      }
    }
    videos.splice(index, 1);
    writeJSON(VIDEOS_DB_PATH, videos);
    return res.json({ success: true });
  }
  res.status(404).json({ error: 'Відео не знайдено' });
});

// 6. Subscriptions (Level 4 Requirement)
app.post('/api/users/:userId/subscribe/:creatorName', (req, res) => {
  const { userId, creatorName } = req.params;
  
  if (!users[userId]) {
    // dynamically create viewer if not present
    users[userId] = { role: 'viewer', subscriptions: [] };
  }
  
  const user = users[userId];
  const index = user.subscriptions.indexOf(creatorName);
  let status = '';
  
  if (index === -1) {
    user.subscriptions.push(creatorName);
    status = 'subscribed';
  } else {
    user.subscriptions.splice(index, 1);
    status = 'unsubscribed';
  }
  
  writeJSON(USERS_DB_PATH, users);
  res.json({ success: true, status, subscriptions: user.subscriptions });
});

// 7. Get user profile and subscriptions
app.get('/api/users/:userId', (req, res) => {
  const { userId } = req.params;
  if (users[userId]) {
    res.json(users[userId]);
  } else {
    const newUser = { role: 'viewer', subscriptions: [] };
    users[userId] = newUser;
    writeJSON(USERS_DB_PATH, users);
    res.json(newUser);
  }
});

// Serve frontend in production static build mode
app.use(express.static(path.join(__dirname, '../frontend/dist')));
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, '../frontend/dist/index.html'));
});

server.listen(PORT, () => {
  console.log(`Express Server running on port ${PORT}`);
  console.log(`Uploads folder configured at ${UPLOADS_DIR}`);
});
