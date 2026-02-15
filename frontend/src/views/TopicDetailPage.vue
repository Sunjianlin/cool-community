<template>
  <div class="topic-detail-page" v-if="loading">
    <div class="loading">加载中...</div>
  </div>
  <div class="topic-detail-page" v-else-if="topic && topic.id">
    <div class="topic-header">
      <h2>{{ topic.name }}</h2>
      <p>{{ topic.description }}</p>
      <div class="topic-meta">
        <span>{{ topic.postCount || 0 }} 帖子</span>
        <span>{{ topic.followCount || 0 }} 关注</span>
        <button class="btn btn-primary" @click="toggleFollow">
          {{ isFollowing ? '已关注' : '关注话题' }}
        </button>
      </div>
    </div>
    
    <div class="topic-content">
      <div class="post-list" v-if="topicPosts.length > 0">
        <div class="post-card" v-for="post in topicPosts" :key="post.id">
          <a :href="`/post/${post.id}`" target="_blank">
            <div class="post-header">
              <div class="user-info">
                <img :src="post.user?.avatar || defaultAvatar" alt="用户头像" class="user-avatar" />
                <span class="username">{{ post.user?.username || '未知用户' }}</span>
              </div>
              <span class="post-time">{{ formatDate(post.createdAt) }}</span>
            </div>
            <h4 class="post-title">{{ post.title }}</h4>
            <p class="post-content">{{ post.content }}</p>
            <div class="post-stats">
              <span class="stat-item">{{ post.likeCount || 0 }} 赞</span>
              <span class="stat-item">{{ post.commentCount || 0 }} 评论</span>
              <span class="stat-item">{{ post.viewCount || 0 }} 浏览</span>
            </div>
          </a>
        </div>
      </div>
      <div v-else class="empty-state">
        <p>暂无帖子，快来发布第一个帖子吧！</p>
      </div>
    </div>
  </div>
  <div v-else class="empty-state">
    <p>话题不存在</p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import topicApi from '../api/topicApi'
import postApi from '../api/postApi'

const route = useRoute()
const topicId = computed(() => route.params.id)

const loading = ref(true)
const topic = ref(null)
const topicPosts = ref([])
const isFollowing = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

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

const toggleFollow = () => {
  isFollowing.value = !isFollowing.value
  if (isFollowing.value) {
    topic.value.followCount = (topic.value.followCount || 0) + 1
  } else {
    topic.value.followCount = Math.max(0, (topic.value.followCount || 1) - 1)
  }
}

const loadTopic = async () => {
  try {
    const response = await topicApi.getTopicById(topicId.value)
    topic.value = response || null
  } catch (error) {
    console.error('加载话题失败:', error)
    topic.value = null
  }
}

const loadTopicPosts = async () => {
  try {
    const response = await postApi.getPostsByTopicId(topicId.value)
    topicPosts.value = response || []
  } catch (error) {
    console.error('加载帖子失败:', error)
    topicPosts.value = []
  }
}

const loadData = async () => {
  loading.value = true
  await Promise.all([loadTopic(), loadTopicPosts()])
  loading.value = false
}

onMounted(() => {
  loadData()
})

watch(topicId, () => {
  loadData()
})
</script>

<style scoped>
.topic-detail-page {
  width: 100%;
}

.topic-header {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.topic-header h2 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 24px;
}

.topic-header p {
  color: #666;
  margin: 0 0 20px 0;
  line-height: 1.6;
}

.topic-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #999;
  font-size: 14px;
}

.topic-content {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
}

.post-card:hover {
  border-color: #3498db;
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.15);
}

.post-card a {
  text-decoration: none;
  color: inherit;
  display: block;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.username {
  font-weight: 500;
  color: #333;
}

.post-time {
  color: #999;
  font-size: 13px;
}

.post-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
}

.post-content {
  color: #666;
  margin: 0 0 15px 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-stats {
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 13px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.loading {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

@media (max-width: 768px) {
  .topic-header {
    padding: 20px;
  }
  
  .topic-header h2 {
    font-size: 20px;
  }
  
  .post-card {
    padding: 16px;
  }
  
  .post-stats {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
