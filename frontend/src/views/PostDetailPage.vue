<template>
  <div class="post-detail-page">
    <div class="post-detail">
      <div class="post-header">
        <div class="user-info">
          <img :src="post.user?.avatar || post.userAvatar || defaultAvatar" alt="用户头像" class="user-avatar" />
          <div class="user-details">
            <span class="username">{{ post.user?.username || post.username || '未知用户' }}</span>
            <span class="post-time">{{ formatDate(post.createdAt) }}</span>
          </div>
        </div>
        <div class="follow-btn" v-if="!post.isFollowing">
          <button class="btn btn-secondary" @click="followUser">关注</button>
        </div>
        <div class="topic-tag" v-if="post.topic">
          <router-link :to="`/topic/${post.topic.id}`">{{ post.topic.name }}</router-link>
        </div>
      </div>
      
      <h1 class="post-title">{{ post.title }}</h1>
      <div class="post-content">
        <p>{{ post.content }}</p>
        <!-- 帖子图片 -->
        <div class="post-images" v-if="post.images && post.images.length > 0">
          <img :src="img" alt="帖子图片" v-for="(img, index) in post.images" :key="index" class="post-image" />
        </div>
      </div>
      
      <div class="post-actions">
        <button class="action-btn" @click="toggleLike">
          <span class="action-icon">{{ isLiked ? '❤️' : '🤍' }}</span>
          <span>{{ post.likeCount }}</span>
        </button>
        <button class="action-btn">
          <span class="action-icon">💬</span>
          <span>{{ post.commentCount }}</span>
        </button>
        <button class="action-btn">
          <span class="action-icon">🔗</span>
          <span>分享</span>
        </button>
        <button class="action-btn">
          <span class="action-icon">⭐</span>
          <span>收藏</span>
        </button>
      </div>
    </div>
    
    <!-- 评论区 -->
    <div class="comments-section">
      <h3 class="section-title">评论 ({{ comments.length }})</h3>
      
      <!-- 评论输入 -->
      <div class="comment-input">
        <img src="https://via.placeholder.com/40" alt="我的头像" class="user-avatar" />
        <div class="input-container">
          <textarea class="input comment-textarea" placeholder="写下你的评论..." v-model="newComment"></textarea>
          <button class="btn btn-primary" @click="submitComment">发表评论</button>
        </div>
      </div>
      
      <!-- 评论列表 -->
      <div class="comments-list">
        <div class="comment-item" v-for="comment in comments" :key="comment.id">
          <img :src="comment.userAvatar" alt="评论用户头像" class="user-avatar" />
          <div class="comment-content">
            <div class="comment-header">
              <span class="username">{{ comment.username }}</span>
              <span class="comment-time">{{ comment.createdAt }}</span>
            </div>
            <p class="comment-text">{{ comment.content }}</p>
            <div class="comment-actions">
              <button class="comment-action-btn">点赞 ({{ comment.likeCount }})</button>
              <button class="comment-action-btn">回复</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import postApi from '../api/postApi'
import commentApi from '../api/commentApi'

const route = useRoute()
const postId = computed(() => route.params.id)

// 数据
const post = ref({})
const comments = ref([])
const isLiked = ref(false)
const newComment = ref('')
const loading = ref(false)

// 模拟数据（当API调用失败时使用）
const mockPost = {
  id: 1,
  title: 'iPhone 16 Pro Max使用一周体验',
  content: '入手iPhone 16 Pro Max已经一周了，来说说我的使用感受。首先，外观方面，这次的设计变化不大，但是钛金属边框的质感确实提升了不少。屏幕方面，Pro Max的大屏体验非常棒，尤其是在看视频和玩游戏的时候。相机方面，4800万像素的主摄表现确实出色，夜景模式也有很大提升。电池续航方面，比上一代有明显提升，一天重度使用完全没问题。系统方面，iOS 18的新功能也很实用，尤其是AI相关的功能。总体来说，iPhone 16 Pro Max是一款非常出色的旗舰手机，值得入手。',
  userAvatar: 'https://via.placeholder.com/40',
  username: '科技达人',
  createdAt: '2026-02-13 10:00:00',
  likeCount: 123,
  commentCount: 45,
  viewCount: 678,
  images: [
    'https://via.placeholder.com/400x300',
    'https://via.placeholder.com/400x300'
  ]
}

const mockComments = [
  {
    id: 1,
    userAvatar: 'https://via.placeholder.com/40',
    username: '数码爱好者',
    content: '请问续航具体能坚持多久？',
    createdAt: '2026-02-13 10:30:00',
    likeCount: 12
  },
  {
    id: 2,
    userAvatar: 'https://via.placeholder.com/40',
    username: '科技达人',
    content: '重度使用一天没问题，我早上8点充满电，晚上10点还有20%左右的电。',
    createdAt: '2026-02-13 10:45:00',
    likeCount: 8
  },
  {
    id: 3,
    userAvatar: 'https://via.placeholder.com/40',
    username: '摄影爱好者',
    content: '相机表现如何？尤其是夜景和长焦？',
    createdAt: '2026-02-13 11:00:00',
    likeCount: 5
  }
]

// 获取帖子详情
const fetchPostDetail = async () => {
  try {
    const response = await postApi.getPostById(postId.value)
    post.value = response || mockPost
  } catch (error) {
    console.error('Failed to fetch post detail:', error)
    post.value = mockPost
  }
}

// 获取评论列表
const fetchComments = async () => {
  try {
    const response = await commentApi.getCommentsByPostId(postId.value)
    comments.value = response || mockComments
  } catch (error) {
    console.error('Failed to fetch comments:', error)
    comments.value = mockComments
  }
}

// 点赞/取消点赞帖子
const toggleLike = async () => {
  try {
    await postApi.likePost(postId.value)
    isLiked.value = !isLiked.value
    if (isLiked.value) {
      post.value.likeCount++
    } else {
      post.value.likeCount--
    }
  } catch (error) {
    console.error('Failed to toggle like:', error)
    // 失败时回滚状态
    isLiked.value = !isLiked.value
  }
}

// 提交评论
const submitComment = async () => {
  if (newComment.value.trim()) {
    try {
      const commentData = {
        content: newComment.value.trim(),
        post: {
          id: postId.value
        },
        user: {
          id: 1 // 假设当前用户ID为1
        }
      }
      const response = await commentApi.createComment(commentData)
      if (response) {
        // 使用返回的评论数据
        const newCommentItem = {
          id: response.id,
          userAvatar: 'https://via.placeholder.com/40',
          username: '我',
          content: response.content,
          createdAt: new Date().toLocaleString(),
          likeCount: 0
        }
        comments.value.unshift(newCommentItem)
        post.value.commentCount++
        newComment.value = ''
      }
    } catch (error) {
      console.error('Failed to submit comment:', error)
      // 失败时使用本地数据
      const comment = {
        id: comments.value.length + 1,
        userAvatar: 'https://via.placeholder.com/40',
        username: '我',
        content: newComment.value.trim(),
        createdAt: new Date().toLocaleString(),
        likeCount: 0
      }
      comments.value.unshift(comment)
      post.value.commentCount++
      newComment.value = ''
    }
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchPostDetail(),
      fetchComments()
    ])
  } catch (error) {
    console.error('Failed to fetch data:', error)
  } finally {
    loading.value = false
  }
  console.log(`PostDetailPage mounted for post ${postId.value}`)
})
</script>

<style scoped>
.post-detail-page {
  width: 100%;
}

.post-detail {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.post-time {
  font-size: 14px;
  color: #999;
}

.post-title {
  font-size: 24px;
  margin-bottom: 24px;
  color: #333;
  line-height: 1.3;
}

.post-content {
  font-size: 16px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 32px;
}

.post-content p {
  margin-bottom: 16px;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.post-image {
  width: 100%;
  height: auto;
  border-radius: 8px;
  object-fit: cover;
}

.post-actions {
  display: flex;
  gap: 32px;
  padding-top: 20px;
  border-top: 1px solid #e5e5e5;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s;
}

.action-btn:hover {
  background-color: #f5f5f5;
  color: #333;
}

.action-icon {
  font-size: 18px;
}

.comments-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 18px;
  margin-bottom: 24px;
  color: #333;
  border-bottom: 2px solid #ff6b6b;
  padding-bottom: 8px;
}

.comment-input {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.comment-input .user-avatar {
  width: 40px;
  height: 40px;
}

.input-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-textarea {
  width: 100%;
  min-height: 80px;
  padding: 12px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  font-size: 14px;
  resize: vertical;
  font-family: inherit;
}

.comment-textarea:focus {
  outline: none;
  border-color: #ff6b6b;
}

.input-container button {
  align-self: flex-end;
  padding: 8px 24px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.comment-item .user-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-header .username {
  font-size: 14px;
  font-weight: 500;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 12px;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.comment-action-btn {
  background: none;
  border: none;
  font-size: 12px;
  color: #999;
  cursor: pointer;
  padding: 4px 0;
  transition: color 0.3s;
}

.comment-action-btn:hover {
  color: #ff6b6b;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .post-detail {
    padding: 20px;
  }
  
  .comments-section {
    padding: 20px;
  }
  
  .post-title {
    font-size: 20px;
  }
  
  .post-actions {
    gap: 16px;
  }
  
  .comment-input {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .input-container button {
    align-self: stretch;
  }
}
</style>
