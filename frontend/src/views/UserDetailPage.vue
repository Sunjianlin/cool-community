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
          <div class="user-stats">
            <span class="stat-item" @click="showFollowers">
              <strong>{{ userInfo.followerCount || 0 }}</strong> 粉丝
            </span>
            <span class="stat-item" @click="showFollowing">
              <strong>{{ userInfo.followingCount || 0 }}</strong> 关注
            </span>
            <span class="stat-item">
              <strong>{{ userInfo.postCount || 0 }}</strong> 帖子
            </span>
          </div>
        </div>
      </div>
      <div class="user-actions" v-if="!isCurrentUser">
        <button 
          class="btn" 
          :class="userInfo.isFollowing ? 'btn-following' : 'btn-primary'"
          @click="toggleFollow"
        >
          {{ userInfo.isFollowing ? '已关注' : '关注' }}
        </button>
        <button class="btn btn-outline" @click="startChat">
          私信
        </button>
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
    
    <el-dialog v-model="showFollowDialog" :title="followDialogTitle" width="500px">
      <div class="follow-list" v-if="followList.length > 0">
        <div class="follow-item" v-for="user in followList" :key="user.id">
          <img :src="user.avatar || defaultAvatar" class="follow-avatar" />
          <div class="follow-info">
            <span class="follow-name">{{ user.nickname || user.username }}</span>
            <span class="follow-bio">{{ user.bio || '暂无简介' }}</span>
          </div>
          <button 
            v-if="!isCurrentUser || followType === 'followers'"
            class="btn btn-sm"
            :class="user.isFollowing ? 'btn-following' : 'btn-primary'"
            @click="toggleFollowUser(user)"
          >
            {{ user.isFollowing ? '已关注' : '关注' }}
          </button>
        </div>
      </div>
      <div v-else class="empty-follow">
        {{ followType === 'following' ? '还没有关注任何人' : '还没有粉丝' }}
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import userApi from '../api/userApi'
import postApi from '../api/postApi'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userInfo = ref({})
const posts = ref([])
const showFollowDialog = ref(false)
const followType = ref('following')
const followList = ref([])

const isCurrentUser = computed(() => {
  return userStore.user?.id === Number(route.params.id)
})

const followDialogTitle = computed(() => {
  return followType.value === 'following' ? '关注列表' : '粉丝列表'
})

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

const toggleFollow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    if (userInfo.value.isFollowing) {
      await userApi.unfollowUser(userInfo.value.id)
      userInfo.value.isFollowing = false
      userInfo.value.followerCount = Math.max(0, (userInfo.value.followerCount || 1) - 1)
      ElMessage.success('已取消关注')
    } else {
      await userApi.followUser(userInfo.value.id)
      userInfo.value.isFollowing = true
      userInfo.value.followerCount = (userInfo.value.followerCount || 0) + 1
      ElMessage.success('关注成功')
    }
  } catch (error) {
    console.error('关注操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

const startChat = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push(`/chat?userId=${userInfo.value.id}`)
}

const showFollowing = async () => {
  followType.value = 'following'
  showFollowDialog.value = true
  await loadFollowList()
}

const showFollowers = async () => {
  followType.value = 'followers'
  showFollowDialog.value = true
  await loadFollowList()
}

const loadFollowList = async () => {
  try {
    const userId = route.params.id
    const response = followType.value === 'following' 
      ? await userApi.getFollowingList(userId, { page: 1, pageSize: 50 })
      : await userApi.getFollowerList(userId, { page: 1, pageSize: 50 })
    followList.value = response.data?.records || []
  } catch (error) {
    console.error('加载关注列表失败:', error)
    followList.value = []
  }
}

const toggleFollowUser = async (user) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    if (user.isFollowing) {
      await userApi.unfollowUser(user.id)
      user.isFollowing = false
    } else {
      await userApi.followUser(user.id)
      user.isFollowing = true
    }
  } catch (error) {
    console.error('关注操作失败:', error)
    ElMessage.error(error.message || '操作失败')
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

watch(() => route.params.id, () => {
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
  border: 3px solid #3498db;
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

.user-stats {
  display: flex;
  gap: 24px;
  margin-top: 15px;
}

.stat-item {
  cursor: pointer;
  transition: color 0.3s;
}

.stat-item:hover {
  color: #3498db;
}

.stat-item strong {
  font-size: 18px;
  margin-right: 4px;
}

.user-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
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

.btn-sm {
  padding: 6px 16px;
  font-size: 12px;
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

.follow-list {
  max-height: 400px;
  overflow-y: auto;
}

.follow-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.follow-item:last-child {
  border-bottom: none;
}

.follow-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 12px;
}

.follow-info {
  flex: 1;
}

.follow-name {
  font-weight: 500;
  display: block;
}

.follow-bio {
  font-size: 12px;
  color: #999;
  display: block;
  margin-top: 4px;
}

.empty-follow {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>
