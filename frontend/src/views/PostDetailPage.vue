<template>
  <div class="post-detail-page">
    <div class="loading" v-if="loading">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    
    <template v-else-if="post">
      <div class="post-card">
        <div class="post-header">
          <div class="author-info">
            <img :src="post.userAvatar || defaultAvatar" class="author-avatar" />
            <div class="author-details">
              <span class="author-name">{{ post.userNickname || post.username }}</span>
              <span class="post-time">{{ formatDate(post.createTime) }}</span>
            </div>
          </div>
          <div class="post-meta">
            <span class="topic-tag" v-if="post.topicName">
              <router-link :to="`/topic/${post.topicId}`">{{ post.topicName }}</router-link>
            </span>
            <span class="view-count">{{ post.viewCount || 0 }} 浏览</span>
          </div>
        </div>
        
        <h1 class="post-title">{{ post.title }}</h1>
        
        <div class="post-content" v-html="post.content"></div>
        
        <div class="post-images" v-if="postImages.length > 0">
          <img 
            v-for="(img, index) in postImages" 
            :key="index" 
            :src="img" 
            class="post-image"
            @click="previewImage(img)"
          />
        </div>
        
        <div class="post-tags" v-if="post.isTop || post.isEssence">
          <span class="tag tag-top" v-if="post.isTop">置顶</span>
          <span class="tag tag-essence" v-if="post.isEssence">精华</span>
        </div>
        
        <div class="post-actions">
          <button class="action-btn" :class="{ active: isLiked }" @click="toggleLike">
            <span class="action-icon">{{ isLiked ? '❤️' : '🤍' }}</span>
            <span>{{ post.likeCount || 0 }}</span>
          </button>
          <button class="action-btn">
            <span class="action-icon">💬</span>
            <span>{{ post.commentCount || 0 }}</span>
          </button>
          <button class="action-btn" :class="{ active: isCollected }" @click="toggleCollect">
            <span class="action-icon">{{ isCollected ? '⭐' : '☆' }}</span>
            <span>{{ post.collectCount || 0 }}</span>
          </button>
          <button class="action-btn" @click="sharePost">
            <span class="action-icon">🔗</span>
            <span>分享</span>
          </button>
        </div>
      </div>
      
      <div class="comments-section">
        <h3 class="section-title">评论 ({{ comments.length }})</h3>
        
        <div class="comment-input" v-if="userStore.isLoggedIn">
          <img :src="userStore.userAvatar" class="user-avatar" />
          <div class="input-wrapper">
            <el-input
              v-model="newComment"
              type="textarea"
              :rows="3"
              placeholder="写下你的评论..."
              maxlength="500"
              show-word-limit
            />
            <el-button type="primary" @click="submitComment" :loading="submitting" style="margin-top: 12px;">
              发表评论
            </el-button>
          </div>
        </div>
        <div class="login-tip" v-else>
          <router-link to="/login">登录</router-link> 后参与评论
        </div>
        
        <div class="comments-list" v-if="comments.length > 0">
          <div class="comment-item" v-for="comment in comments" :key="comment.id">
            <img :src="comment.userAvatar || defaultAvatar" class="user-avatar" />
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-author">{{ comment.userNickname || comment.username }}</span>
                <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
              </div>
              <p class="comment-text">{{ comment.content }}</p>
              <div class="comment-actions">
                <button class="comment-action-btn">👍 {{ comment.likeCount || 0 }}</button>
                <button class="comment-action-btn">回复</button>
              </div>
            </div>
          </div>
        </div>
        
        <div class="empty-comments" v-else>
          <p>暂无评论，快来发表第一条评论吧！</p>
        </div>
      </div>
    </template>
    
    <div class="empty-state" v-else>
      <p>帖子不存在或已被删除</p>
      <router-link to="/" class="btn btn-primary">返回首页</router-link>
    </div>
    
    <el-dialog v-model="imagePreviewVisible" width="80%" :show-close="true">
      <img :src="previewImageUrl" style="width: 100%;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import postApi from '../api/postApi'
import commentApi from '../api/commentApi'
import { useUserStore } from '../store/user'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const submitting = ref(false)
const post = ref(null)
const comments = ref([])
const isLiked = ref(false)
const isCollected = ref(false)
const newComment = ref('')
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')

const postImages = computed(() => {
  if (!post.value?.images) return []
  if (typeof post.value.images === 'string') {
    return post.value.images.split(',').filter(img => img.trim())
  }
  return post.value.images
})

const updatePageTitle = () => {
  document.title = post.value ? `${post.value.title} - CoolCommunity` : '帖子详情 - CoolCommunity'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

const previewImage = (url) => {
  previewImageUrl.value = url
  imagePreviewVisible.value = true
}

const loadPost = async () => {
  try {
    const response = await postApi.getPostDetail(route.params.id)
    if (response.code === 200 && response.data) {
      post.value = response.data
      updatePageTitle()
    } else {
      post.value = null
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
    post.value = null
  }
}

const loadComments = async () => {
  try {
    const response = await commentApi.getCommentsByPostId(route.params.id)
    if (response.code === 200 && response.data) {
      comments.value = response.data || []
    }
  } catch (error) {
    console.error('加载评论失败:', error)
    comments.value = []
  }
}

const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    if (isLiked.value) {
      await postApi.unlikePost(post.value.id)
      post.value.likeCount = Math.max(0, (post.value.likeCount || 1) - 1)
    } else {
      await postApi.likePost(post.value.id)
      post.value.likeCount = (post.value.likeCount || 0) + 1
    }
    isLiked.value = !isLiked.value
  } catch (error) {
    console.error('点赞操作失败:', error)
  }
}

const toggleCollect = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    if (isCollected.value) {
      await postApi.uncollectPost(post.value.id)
      post.value.collectCount = Math.max(0, (post.value.collectCount || 1) - 1)
    } else {
      await postApi.collectPost(post.value.id)
      post.value.collectCount = (post.value.collectCount || 0) + 1
    }
    isCollected.value = !isCollected.value
    ElMessage.success(isCollected.value ? '收藏成功' : '已取消收藏')
  } catch (error) {
    console.error('收藏操作失败:', error)
  }
}

const sharePost = () => {
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  submitting.value = true
  try {
    const response = await commentApi.createComment({
      postId: post.value.id,
      content: newComment.value.trim()
    })
    
    if (response.code === 200) {
      comments.value.unshift({
        id: response.data?.id || Date.now(),
        userAvatar: userStore.userAvatar,
        userNickname: userStore.user?.nickname,
        content: newComment.value.trim(),
        createTime: new Date().toISOString(),
        likeCount: 0
      })
      post.value.commentCount = (post.value.commentCount || 0) + 1
      newComment.value = ''
      ElMessage.success('评论发表成功')
    }
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('评论发表失败')
  } finally {
    submitting.value = false
  }
}

const loadData = async () => {
  loading.value = true
  await Promise.all([loadPost(), loadComments()])
  loading.value = false
}

onMounted(() => {
  loadData()
})

watch(() => route.params.id, () => {
  loadData()
})
</script>

<style scoped>
.post-detail-page {
  width: 100%;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  color: #999;
  gap: 16px;
}

.post-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.post-time {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.topic-tag {
  padding: 4px 12px;
  background: #e8f4fc;
  border-radius: 16px;
  font-size: 13px;
}

.topic-tag a {
  color: #3498db;
  text-decoration: none;
}

.topic-tag a:hover {
  text-decoration: underline;
}

.view-count {
  font-size: 13px;
  color: #999;
}

.post-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px 0;
  line-height: 1.4;
}

.post-content {
  font-size: 16px;
  color: #333;
  line-height: 1.8;
  margin-bottom: 20px;
}

.post-content :deep(p) {
  margin-bottom: 16px;
}

.post-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 12px 0;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.post-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.post-image:hover {
  transform: scale(1.02);
}

.post-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.tag {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.tag-top {
  background: #e74c3c;
  color: white;
}

.tag-essence {
  background: #f39c12;
  color: white;
}

.post-actions {
  display: flex;
  gap: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  background: #f5f5f5;
  border-radius: 24px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn:hover {
  background: #e8e8e8;
}

.action-btn.active {
  background: #fff0f0;
  color: #e74c3c;
}

.action-icon {
  font-size: 18px;
}

.comments-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #3498db;
}

.comment-input {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
}

.comment-input .user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.input-wrapper {
  flex: 1;
}

.login-tip {
  text-align: center;
  padding: 20px;
  color: #999;
  margin-bottom: 20px;
}

.login-tip a {
  color: #3498db;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-item .user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-author {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin: 0 0 8px 0;
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
}

.comment-action-btn:hover {
  color: #3498db;
}

.empty-comments {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 100px 20px;
  color: #999;
}

.empty-state p {
  margin-bottom: 20px;
}

.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.3s;
  display: inline-block;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
}

@media (max-width: 768px) {
  .post-card {
    padding: 16px;
  }
  
  .post-title {
    font-size: 20px;
  }
  
  .post-actions {
    gap: 12px;
    flex-wrap: wrap;
  }
  
  .action-btn {
    padding: 8px 16px;
    font-size: 13px;
  }
}
</style>
