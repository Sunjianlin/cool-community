<template>
  <div class="topic-detail-page">
    <div class="loading" v-if="loading">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    
    <template v-else-if="topic">
      <div class="topic-card">
        <div class="topic-cover" :style="{ background: gradientColor }">
          <span class="topic-icon">#</span>
        </div>
        <div class="topic-info">
          <h1 class="topic-name">{{ topic.name }}</h1>
          <p class="topic-description">{{ topic.description || '暂无描述' }}</p>
          <div class="topic-stats">
            <div class="stat-item">
              <span class="stat-value">{{ topic.postCount || 0 }}</span>
              <span class="stat-label">帖子</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ topic.followCount || 0 }}</span>
              <span class="stat-label">关注</span>
            </div>
          </div>
          <div class="topic-actions">
            <button 
              class="btn" 
              :class="topic.isFollowed ? 'btn-following' : 'btn-primary'"
              @click="toggleFollow"
              :disabled="loadingFollow"
            >
              {{ loadingFollow ? '处理中...' : (topic.isFollowed ? '已关注' : '关注话题') }}
            </button>
            <router-link to="/create-post" class="btn btn-outline">
              发帖
            </router-link>
          </div>
        </div>
      </div>
      
      <div class="posts-section">
        <div class="section-header">
          <h3 class="section-title">相关帖子</h3>
          <span class="post-count">共 {{ pagination.total }} 篇</span>
        </div>
        
        <div class="post-list" v-if="posts.length > 0">
          <router-link 
            v-for="post in posts" 
            :key="post.id" 
            :to="`/post/${post.id}`"
            class="post-card"
          >
            <div class="post-header">
              <img :src="post.userAvatar || defaultAvatar" class="user-avatar" />
              <div class="user-info">
                <span class="username">{{ post.userNickname || post.username }}</span>
                <span class="post-time">{{ formatDate(post.createTime) }}</span>
              </div>
            </div>
            <h4 class="post-title">{{ post.title }}</h4>
            <p class="post-content">{{ truncate(post.content, 150) }}</p>
            <div class="post-footer">
              <div class="post-stats">
                <span>{{ post.likeCount || 0 }} 赞</span>
                <span>{{ post.commentCount || 0 }} 评论</span>
                <span>{{ post.viewCount || 0 }} 浏览</span>
              </div>
              <div class="post-tags" v-if="post.isTop || post.isEssence">
                <span class="tag tag-top" v-if="post.isTop">置顶</span>
                <span class="tag tag-essence" v-if="post.isEssence">精华</span>
              </div>
            </div>
          </router-link>
        </div>
        
        <div class="empty-state" v-else>
          <p>暂无帖子，快来发布第一篇吧！</p>
          <router-link to="/create-post" class="btn btn-primary">发布帖子</router-link>
        </div>
        
        <el-pagination
          v-if="pagination.total > pagination.pageSize"
          v-model:current-page="pagination.page"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          layout="prev, pager, next"
          @current-change="loadPosts"
          style="margin-top: 20px; justify-content: center;"
        />
      </div>
    </template>
    
    <div class="empty-state" v-else>
      <p>话题不存在或已被删除</p>
      <router-link to="/topics" class="btn btn-primary">返回话题列表</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import topicApi from '../api/topicApi'
import postApi from '../api/postApi'
import { useUserStore } from '../store/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const loadingFollow = ref(false)
const topic = ref(null)
const posts = ref([])
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

const gradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
]

const gradientColor = computed(() => {
  if (!topic.value) return gradients[0]
  const id = topic.value.id || 1
  return gradients[id % gradients.length]
})

const updatePageTitle = () => {
  document.title = topic.value ? `${topic.value.name} - CoolCommunity` : '话题详情 - CoolCommunity'
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

const truncate = (text, length) => {
  if (!text) return ''
  const cleanText = text.replace(/<[^>]+>/g, '')
  return cleanText.length > length ? cleanText.substring(0, length) + '...' : cleanText
}

const loadTopic = async () => {
  try {
    const response = await topicApi.getTopicDetail(route.params.id)
    if (response.code === 200 && response.data) {
      topic.value = response.data
      updatePageTitle()
    } else {
      topic.value = null
    }
  } catch (error) {
    console.error('加载话题失败:', error)
    topic.value = null
  }
}

const loadPosts = async () => {
  try {
    const response = await postApi.getPostList({
      topicId: route.params.id,
      page: pagination.value.page,
      pageSize: pagination.value.pageSize
    })
    if (response.code === 200 && response.data) {
      posts.value = response.data.records || []
      pagination.value.total = response.data.total || 0
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
    posts.value = []
  }
}

const toggleFollow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  if (loadingFollow.value) return
  
  if (topic.value.isFollowed) {
    ElMessageBox.confirm(
      `确定要取消关注「${topic.value.name}」吗？`,
      '取消关注',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      loadingFollow.value = true
      try {
        const response = await topicApi.unfollowTopic(topic.value.id)
        if (response.code === 200) {
          topic.value.isFollowed = false
          topic.value.followCount = response.data
          ElMessage.success('已取消关注')
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      } finally {
        loadingFollow.value = false
      }
    }).catch(() => {})
  } else {
    loadingFollow.value = true
    try {
      const response = await topicApi.followTopic(topic.value.id)
      if (response.code === 200) {
        topic.value.isFollowed = true
        topic.value.followCount = response.data
        ElMessage.success('关注成功')
      }
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    } finally {
      loadingFollow.value = false
    }
  }
}

const loadData = async () => {
  loading.value = true
  await Promise.all([loadTopic(), loadPosts()])
  loading.value = false
}

onMounted(() => {
  loadData()
})

watch(() => route.params.id, () => {
  pagination.value.page = 1
  loadData()
})
</script>

<style scoped>
.topic-detail-page {
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

.topic-card {
  display: flex;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
}

.topic-cover {
  width: 200px;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.topic-icon {
  font-size: 80px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: bold;
}

.topic-info {
  flex: 1;
  padding: 24px 32px;
  display: flex;
  flex-direction: column;
}

.topic-name {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.topic-description {
  font-size: 15px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 20px 0;
  flex: 1;
}

.topic-stats {
  display: flex;
  gap: 32px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

.topic-actions {
  display: flex;
  gap: 12px;
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
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
}

.btn-following {
  background: #e8f4fc;
  color: #3498db;
}

.btn-following:hover {
  background: #d0e8f7;
}

.btn-outline {
  background: transparent;
  border: 1px solid #ddd;
  color: #666;
}

.btn-outline:hover {
  border-color: #3498db;
  color: #3498db;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.posts-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.post-count {
  font-size: 14px;
  color: #999;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  display: block;
  padding: 20px;
  border-radius: 12px;
  background: #f8f9fa;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s;
}

.post-card:hover {
  background: #f0f2f5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.post-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 12px 0;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

.post-tags {
  display: flex;
  gap: 8px;
}

.tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.tag-top {
  background: #e74c3c;
  color: white;
}

.tag-essence {
  background: #f39c12;
  color: white;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-state p {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .topic-card {
    flex-direction: column;
  }
  
  .topic-cover {
    width: 100%;
    min-height: 120px;
  }
  
  .topic-info {
    padding: 20px;
  }
  
  .topic-name {
    font-size: 22px;
  }
  
  .topic-stats {
    gap: 24px;
  }
}
</style>
