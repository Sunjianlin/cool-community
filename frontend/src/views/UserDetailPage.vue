<template>
  <div class="user-detail-page">
    <div class="user-header card">
      <div class="avatar-section">
        <img :src="userInfo.avatar || defaultAvatar" class="avatar" />
        <div class="user-info">
          <h2>{{ userInfo.nickname || '用户' }}</h2>
          <p class="username">@{{ userInfo.username }}</p>
          <p class="role-tag">{{ getRoleName(userInfo.role) }}</p>
          <p v-if="userInfo.bio" class="bio">{{ userInfo.bio }}</p>
        </div>
      </div>
    </div>

    <div class="user-posts">
      <h3>发布的帖子</h3>
      <div v-if="posts.length === 0" class="empty">暂无帖子</div>
      <div v-else class="post-list">
        <div v-for="post in posts" :key="post.id" class="post-card" @click="$router.push(`/post/${post.id}`)">
          <h4>{{ post.title }}</h4>
          <p>{{ truncate(post.content, 100) }}</p>
          <div class="post-meta">
            <span>{{ formatDate(post.createTime) }}</span>
            <span>{{ post.likeCount || 0 }} 赞</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import userApi from '../api/userApi'
import postApi from '../api/postApi'

const route = useRoute()
const userInfo = ref({})
const posts = ref([])
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const getRoleName = (role) => {
  const roles = ['普通用户', '版主', '管理员', '系统管理员']
  return roles[role] || '普通用户'
}

const loadUserInfo = async () => {
  try {
    const response = await userApi.getUserInfoById(route.params.id)
    userInfo.value = response.data || {}
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const loadUserPosts = async () => {
  try {
    const response = await postApi.getPostList({ userId: route.params.id, page: 1, pageSize: 10 })
    posts.value = response.data?.records || []
  } catch (error) {
    console.error('加载用户帖子失败:', error)
  }
}

const formatDate = (time) => {
  if (!time) return ''
  return time.split(' ')[0]
}

const truncate = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

onMounted(() => {
  loadUserInfo()
  loadUserPosts()
})
</script>

<style scoped>
.user-detail-page {
  max-width: 800px;
  margin: 0 auto;
}

.user-header {
  padding: 30px;
  margin-bottom: 25px;
  background: white;
  border-radius: 12px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
}

.user-info h2 {
  margin: 0 0 5px 0;
}

.username {
  color: #666;
  margin: 0;
}

.role-tag {
  display: inline-block;
  padding: 2px 10px;
  background: #e8f4fc;
  color: #3498db;
  border-radius: 10px;
  font-size: 12px;
  margin-top: 5px;
}

.bio {
  color: #666;
  margin-top: 10px;
}

.user-posts {
  background: white;
  border-radius: 12px;
  padding: 24px;
}

.user-posts h3 {
  margin-bottom: 20px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

.post-card h4 {
  margin: 0 0 10px 0;
}

.post-card p {
  color: #666;
  margin: 0 0 10px 0;
}

.post-meta {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 12px;
}

.empty {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>
