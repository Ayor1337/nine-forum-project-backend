# Spotify集成快速指南

## 环境变量配置

在运行应用前,请配置以下环境变量:

```bash
export SPOTIFY_CLIENT_ID="your_spotify_client_id"
export SPOTIFY_CLIENT_SECRET="your_spotify_client_secret"
```

获取Spotify凭证:
1. 访问 https://developer.spotify.com/dashboard
2. 创建应用
3. 在Dashboard中配置Redirect URI: `http://localhost:9966/api/spotify/callback`
4. 复制Client ID和Client Secret

## 数据库初始化

执行以下SQL脚本创建表:

```bash
mysql -u root -p -P 16033 nine_forum < db_spotify_token.sql
```

## API使用指南

### 1. 获取授权URL

```bash
GET /api/spotify/auth
Authorization: Bearer <your_jwt_token>

响应:
{
  "code": 200,
  "message": "成功",
  "data": {
    "authUrl": "https://accounts.spotify.com/authorize?client_id=...",
    "isConnected": false,
    "connectedAt": null
  }
}
```

### 2. 用户授权流程

1. 前端将用户重定向到 `authUrl`
2. 用户在Spotify页面授权
3. Spotify自动回调到 `/api/spotify/callback?code=xxx&state=userId`
4. 后端处理回调并重定向到 `http://localhost:9966/profile?spotify_success=true`

### 3. 获取当前播放状态

```bash
# 获取自己的播放状态
GET /api/spotify/currently-playing
Authorization: Bearer <your_jwt_token>

# 获取他人的播放状态
GET /api/spotify/currently-playing/by_user_id?user_id=123

响应:
{
  "code": 200,
  "data": {
    "isPlaying": true,
    "trackName": "Shape of You",
    "artistName": "Ed Sheeran",
    "albumName": "÷ (Deluxe)",
    "albumImageUrl": "https://i.scdn.co/image/...",
    "spotifyUrl": "https://open.spotify.com/track/...",
    "progressMs": 125000,
    "durationMs": 233713,
    "cachedAt": "2025-01-29T10:30:00"
  }
}
```

### 4. 解除绑定

```bash
DELETE /api/spotify/disconnect
Authorization: Bearer <your_jwt_token>
```

### 5. 手动刷新(调试用)

```bash
POST /api/spotify/refresh
Authorization: Bearer <your_jwt_token>
```

## Redis缓存键格式

```
spotify:currently_playing:{accountId}
TTL: 30秒
```

查看缓存:
```bash
redis-cli -p 16379
keys spotify:currently_playing:*
get spotify:currently_playing:1
```

## 定时任务

- **执行间隔**: 每30秒
- **功能**: 自动刷新所有已绑定用户的播放状态到Redis
- **类**: `com.ayor.scheduled.SpotifyTask`

## 日志监控

关键日志:
- `用户 {id} 成功绑定Spotify账号` - 绑定成功
- `开始刷新 {count} 个用户的Spotify播放状态` - 定时任务执行
- `刷新Token失败` - Token刷新失败(需要用户重新授权)

## 故障排查

### 问题1: Token刷新失败

**症状**: 日志显示 "刷新Token失败"
**原因**: Refresh token被Spotify撤销
**解决**: 用户需要重新授权绑定

### 问题2: 获取播放状态返回404

**症状**: API返回null
**原因**: 用户当前未播放任何内容
**解决**: 这是正常情况,前端展示"当前未播放"

### 问题3: OAuth回调失败

**症状**: 回调后重定向到错误页面
**原因**:
- Spotify Dashboard中的Redirect URI配置不正确
- Client ID或Client Secret错误
**解决**: 检查环境变量和Spotify Dashboard配置

## 安全注意事项

1. **CSRF防护**: 使用state参数验证OAuth回调来源
2. **Token安全**: 考虑对数据库中的token字段加密存储
3. **限流保护**: Spotify API限制每30秒最多1次调用 `/currently-playing`
4. **隐私设置**: 考虑添加用户隐私开关,控制播放状态是否公开展示

## 集成到前端

在个人资料页面,`UserInfoVO` 现在包含 `currentlyPlaying` 字段:

```typescript
interface UserInfo {
  accountId: number;
  username: string;
  nickname: string;
  avatarUrl: string;
  bannerUrl: string;
  bio: string;
  currentlyPlaying?: {
    isPlaying: boolean;
    trackName: string;
    artistName: string;
    albumName: string;
    albumImageUrl: string;
    spotifyUrl: string;
    progressMs: number;
    durationMs: number;
    cachedAt: string;
  };
}
```

前端展示示例:
```jsx
{userInfo.currentlyPlaying?.isPlaying && (
  <div className="spotify-widget">
    <img src={userInfo.currentlyPlaying.albumImageUrl} alt="Album" />
    <div>
      <a href={userInfo.currentlyPlaying.spotifyUrl} target="_blank">
        {userInfo.currentlyPlaying.trackName}
      </a>
      <p>{userInfo.currentlyPlaying.artistName}</p>
    </div>
  </div>
)}
```
