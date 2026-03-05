<template>
  <div class="profile-page">
    <!-- 页面标题 -->
    <h2 class="page-title">
      <span class="title-icon">👤</span>
      个人中心
    </h2>
    
    <!-- 用户资料卡片 -->
    <div class="user-profile card">
      <div class="profile-header">
        <!-- 用户头像 -->
        <div class="avatar-container">
          <img :src="user.avatar || defaultAvatar" alt="用户头像" class="profile-avatar" />
          <div class="avatar-badge" v-if="userStore.isLoggedIn">
            <span class="badge-dot"></span>
          </div>
        </div>
        
        <!-- 用户信息 -->
        <div class="profile-info">
          <div class="info-header">
            <h3>{{ user.nickname || user.username }}</h3>
            <span class="user-username" v-if="user.username">@{{ user.username }}</span>
          </div>
          <p class="user-bio" v-if="user.bio">{{ user.bio }}</p>
          <p class="user-bio empty-bio" v-else>
            <span class="empty-icon">💭</span>
            暂无个人简介，点击编辑资料添加
          </p>
          
          <!-- 用户统计数据 -->
          <div class="user-stats">
            <span class="stat-item">
              <span class="stat-icon">📝</span>
              <span class="stat-number">{{ user.postCount || 0 }}</span>
              <span class="stat-label">帖子</span>
            </span>
            <span class="stat-item">
              <span class="stat-icon">👥</span>
              <span class="stat-number">{{ user.followerCount || 0 }}</span>
              <span class="stat-label">关注者</span>
            </span>
            <span class="stat-item">
              <span class="stat-icon">❤️</span>
              <span class="stat-number">{{ user.followingCount || 0 }}</span>
              <span class="stat-label">关注</span>
            </span>
          </div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
    <div class="profile-actions">
      <button class="btn btn-primary" @click="showEditDialog = true">
        <span class="btn-icon">✏️</span>
        编辑资料
      </button>
      <button class="btn btn-secondary" @click="logout">
        <span class="btn-icon">🚪</span>
        退出登录
      </button>
      <button 
        :class="['btn', checkinStatus ? 'btn-success' : 'btn-warning']" 
        @click="handleCheckin"
        :disabled="checkinStatus || checkingIn"
      >
        <span class="btn-icon" v-if="!checkinStatus">🎁</span>
        <span class="btn-icon" v-else>✅</span>
        {{ checkinStatus ? '已签到' : '每日签到' }}
      </button>
    </div>
    
    <!-- 签到状态 -->
    <div class="checkin-status" v-if="userStore.isLoggedIn">
      <div class="checkin-info">
        <span class="checkin-icon">📅</span>
        <span v-if="checkinStatus" class="checkin-text success">今日已签到，获得 {{ dailyPoints }} 积分</span>
        <span v-else class="checkin-text">每日签到可获得 {{ dailyPoints }} 积分</span>
        <span class="consecutive-days" v-if="consecutiveDays > 0">
          连续签到 {{ consecutiveDays }} 天
        </span>
      </div>
    </div>
    </div>
    
    <!-- 我的帖子 -->
    <div class="user-posts card">
      <h3 class="section-title">
        <span class="section-icon">📋</span>
        我的帖子
      </h3>
      <div class="post-list">
        <!-- 空状态 -->
        <div v-if="userPosts.length === 0" class="empty-message">
          <div class="empty-icon-large">📝</div>
          <h4>还没有发布过帖子</h4>
          <p>分享你的第一个想法吧！</p>
          <button class="btn btn-primary" @click="$router.push('/create-post')">
            <span class="btn-icon">+</span>
            发布帖子
          </button>
        </div>
        
        <!-- 帖子列表 -->
        <div v-else class="post-card" v-for="post in userPosts" :key="post.id" @click="$router.push(`/post/${post.id}`)">
          <div class="post-header">
            <h4 class="post-title">{{ post.title }}</h4>
            <span :class="['post-status', getStatusClass(post.status)]">
              <span v-if="post.status === 0" class="status-icon">⏳</span>
              <span v-else-if="post.status === 1" class="status-icon">✅</span>
              <span v-else-if="post.status === 2" class="status-icon">❌</span>
              <span v-else class="status-icon">❓</span>
              {{ post.statusName || '未知状态' }}
            </span>
          </div>
          <p class="post-content">{{ truncate(post.content, 120) }}</p>
          <div class="post-meta">
            <span class="post-date">
              <span class="meta-icon">📅</span>
              {{ formatDate(post.createTime) }}
            </span>
            <div class="post-stats">
              <span class="stat-item">
                <span class="stat-icon">👍</span>
                {{ post.likeCount || 0 }}
              </span>
              <span class="stat-item">
                <span class="stat-icon">💬</span>
                {{ post.commentCount || 0 }}
              </span>
              <span class="stat-item">
                <span class="stat-icon">👁️</span>
                {{ post.viewCount || 0 }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 编辑资料对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑资料" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :on-error="handleAvatarError"
            :before-upload="beforeAvatarUpload"
            name="file"
          >
            <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar-preview" />
            <div v-else class="avatar-uploader-icon">
              <span class="upload-icon">📷</span>
              <span class="upload-text">点击上传</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" type="email" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="editForm.bio" type="textarea" rows="4" placeholder="请输入个人简介，介绍一下自己吧！" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import userApi from '../api/userApi'
import postApi from '../api/postApi'
import checkinApi from '../api/checkinApi'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const user = ref({
  username: '',
  nickname: '',
  email: '',
  avatar: '',
  bio: '',
  postCount: 0,
  followerCount: 0,
  followingCount: 0
})

const userPosts = ref([])
const showEditDialog = ref(false)
const saving = ref(false)
const loading = ref(true)
const editForm = ref({
  username: '',
  nickname: '',
  email: '',
  avatar: '',
  bio: ''
})

// 签到相关状态
const checkinStatus = ref(false) // 今日是否已签到
const checkingIn = ref(false) // 签到中
const dailyPoints = ref(10) // 每日签到获得的积分
const consecutiveDays = ref(0) // 连续签到天数

const uploadUrl = computed(() => 'http://localhost:8082/api/user/avatar')

const uploadHeaders = computed(() => ({
  Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
}))

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    editForm.value.avatar = response.data
    userStore.updateAvatar(response.data)
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

const handleAvatarError = () => {
  ElMessage.error('头像上传失败，请稍后重试')
}

const loadUserProfile = async () => {
  loading.value = true
  try {
    // 确保 userStore 已初始化
    if (!userStore.isInitialized) {
      await userStore.initAuth()
    }
    
    if (userStore.isLoggedIn && userStore.user) {
      user.value = {
        ...userStore.user,
        postCount: userStore.user.postCount || 0,
        followerCount: userStore.user.followerCount || 0,
        followingCount: userStore.user.followingCount || 0
      }
      editForm.value = {
        username: userStore.user.username || '',
        nickname: userStore.user.nickname || '',
        email: userStore.user.email || '',
        avatar: userStore.user.avatar || '',
        bio: userStore.user.bio || ''
      }
      return true
    } else {
      router.push('/login')
      return false
    }
  } catch (error) {
    console.error('加载用户资料失败:', error)
    router.push('/login')
    return false
  } finally {
    loading.value = false
  }
}

const loadUserPosts = async () => {
  if (!userStore.isLoggedIn || !userStore.user || !userStore.user.id) {
    return
  }
  
  try {
    const response = await postApi.getPostList({ userId: userStore.user.id, page: 1, pageSize: 10 });
    userPosts.value = response.data?.records || [];
    console.log('加载用户帖子成功:', userPosts.value);
  } catch (error) {
    console.error('加载帖子失败:', error);
    userPosts.value = [];
  }
}

const saveProfile = async () => {
  saving.value = true
  try {
    await userApi.updateUserInfo(editForm.value)
    userStore.updateUser(editForm.value)
    user.value = { ...user.value, ...editForm.value }
    showEditDialog.value = false
    ElMessage.success('资料更新成功')
  } catch (error) {
    console.error('更新资料失败:', error)
    ElMessage.error('资料更新失败')
  } finally {
    saving.value = false
  }
}

const logout = () => {
  userStore.logout()
  router.push('/')
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}

const truncate = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

const getStatusClass = (status) => {
  switch (status) {
    case 0:
      return 'status-pending'
    case 1:
      return 'status-published'
    case 2:
      return 'status-rejected'
    default:
      return 'status-unknown'
  }
}

// 加载签到状态
const loadCheckinStatus = async () => {
  if (!userStore.isLoggedIn) return
  
  try {
    const response = await checkinApi.hasCheckedInToday()
    checkinStatus.value = response.data
  } catch (error) {
    console.error('加载签到状态失败:', error)
  }
}

// 执行签到
const handleCheckin = async () => {
  if (checkinStatus.value || checkingIn.value) return
  
  checkingIn.value = true
  try {
    const response = await checkinApi.checkin()
    const points = response.data
    checkinStatus.value = true
    dailyPoints.value = points
    
    // 显示签到成功弹窗
    ElMessage.success({
      message: `签到成功！获得 ${points} 积分`,
      duration: 3000,
      showClose: true
    })
  } catch (error) {
    console.error('签到失败:', error)
    ElMessage.error('签到失败，请稍后重试')
  } finally {
    checkingIn.value = false
  }
}

// 监听登录状态变化，重新加载数据
watch(
  () => [userStore.isLoggedIn, userStore.user],
  async ([isLoggedIn, user]) => {
    if (isLoggedIn && user) {
      await loadUserProfile()
      await loadUserPosts()
      await loadCheckinStatus()
    }
  },
  { deep: true }
)

onMounted(async () => {
  const isLoggedIn = await loadUserProfile()
  if (isLoggedIn) {
    await loadUserPosts()
    await loadCheckinStatus()
  }
})
</script>

<style scoped>
.profile-page {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 32px;
  margin-bottom: 40px;
  color: var(--text-primary);
  text-align: center;
  font-family: var(--font-display);
  position: relative;
  padding-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.title-icon {
  font-size: 36px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

.page-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: var(--primary-color);
  border-radius: 2px;
}

/* 用户资料卡片 */
.card {
  background: var(--card-gradient);
  border-radius: var(--border-radius);
  padding: 32px;
  margin-bottom: 32px;
  box-shadow: var(--shadow-md);
  transition: var(--transition);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
}

.user-profile {
  text-align: center;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 40px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

/* 头像容器 */
.avatar-container {
  position: relative;
  display: inline-block;
}

.profile-avatar {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--primary-color);
  box-shadow: 0 8px 24px rgba(52, 152, 219, 0.3);
  transition: var(--transition);
}

.profile-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 32px rgba(52, 152, 219, 0.4);
}

/* 在线徽章 */
.avatar-badge {
  position: absolute;
  bottom: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  background: var(--success-color);
  border-radius: 50%;
  border: 3px solid white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(46, 204, 113, 0.4);
  animation: pulse-ring 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.badge-dot {
  width: 12px;
  height: 12px;
  background: white;
  border-radius: 50%;
  animation: pulse-dot 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse-ring {
  0%, 100% {
    transform: scale(0.8);
    opacity: 0.8;
  }
  50% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes pulse-dot {
  0%, 100% {
    transform: scale(0.6);
  }
  50% {
    transform: scale(1);
  }
}

.profile-info {
  flex: 1;
  text-align: left;
}

/* 用户信息头部 */
.info-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.profile-info h3 {
  font-size: 28px;
  margin-bottom: 0;
  color: var(--text-primary);
  font-family: var(--font-display);
}

.user-username {
  font-size: 16px;
  color: var(--text-secondary);
  font-weight: 500;
  background: rgba(0, 0, 0, 0.05);
  padding: 4px 12px;
  border-radius: 16px;
  transition: var(--transition);
}

.user-username:hover {
  background: rgba(52, 152, 219, 0.1);
  color: var(--primary-color);
  transform: translateY(-2px);
}

/* 个人简介 */
.user-bio {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 20px;
  line-height: 1.6;
  max-width: 100%;
  transition: var(--transition);
}

.empty-bio {
  color: var(--text-light);
  font-style: italic;
  display: flex;
  align-items: center;
  gap: 8px;
}

.empty-icon {
  font-size: 18px;
}

/* 统计数据 */
.user-stats {
  display: flex;
  gap: 32px;
  font-size: 16px;
  color: var(--text-primary);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  transition: var(--transition);
  padding: 8px 16px;
  border-radius: 20px;
  background: rgba(0, 0, 0, 0.02);
}

.stat-item:hover {
  color: var(--primary-color);
  transform: translateY(-2px);
  background: rgba(52, 152, 219, 0.1);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.2);
}

.stat-icon {
  font-size: 18px;
}

.stat-number {
  font-size: 18px;
  font-weight: 700;
  font-family: var(--font-display);
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  transition: var(--transition);
}

.stat-item:hover .stat-label {
  color: var(--primary-color);
}

/* 操作按钮 */
.profile-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  padding-top: 24px;
}

.btn-icon {
  font-size: 18px;
}

/* 签到状态样式 */
.checkin-status {
  margin-top: 24px;
  padding: 16px;
  background: rgba(46, 204, 113, 0.05);
  border-radius: var(--border-radius);
  border: 1px solid rgba(46, 204, 113, 0.2);
  transition: var(--transition);
}

.checkin-status:hover {
  background: rgba(46, 204, 113, 0.1);
  border-color: rgba(46, 204, 113, 0.4);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.2);
}

.checkin-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.checkin-icon {
  font-size: 20px;
  color: var(--success-color);
}

.checkin-text {
  font-size: 16px;
  color: var(--text-secondary);
  font-weight: 500;
}

.checkin-text.success {
  color: var(--success-color);
  font-weight: 600;
}

.consecutive-days {
  font-size: 14px;
  color: var(--text-light);
  background: rgba(46, 204, 113, 0.1);
  padding: 4px 12px;
  border-radius: 16px;
  margin-left: auto;
  transition: var(--transition);
}

.consecutive-days:hover {
  background: rgba(46, 204, 113, 0.2);
  color: var(--success-color);
  transform: translateY(-2px);
}

/* 签到按钮样式 */
.btn-warning {
  background: linear-gradient(135deg, #f39c12, #e67e22);
  border: 1px solid #e67e22;
  color: white;
}

.btn-warning:hover:not(:disabled) {
  background: linear-gradient(135deg, #e67e22, #d35400);
  border-color: #d35400;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(243, 156, 18, 0.4);
}

.btn-success {
  background: linear-gradient(135deg, #27ae60, #229954);
  border: 1px solid #229954;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background: linear-gradient(135deg, #229954, #1e8449);
  border-color: #1e8449;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(39, 174, 96, 0.4);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

/* 帖子区域 */
.section-title {
  font-size: 24px;
  margin-bottom: 28px;
  color: var(--text-primary);
  border-bottom: 3px solid var(--primary-color);
  padding-bottom: 12px;
  font-family: var(--font-display);
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-icon {
  font-size: 20px;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 60px;
  height: 3px;
  background: var(--secondary-color);
}

/* 帖子卡片 */
.post-title {
  font-size: 20px;
  margin-bottom: 12px;
  color: var(--text-primary);
  font-family: var(--font-display);
  transition: var(--transition);
}

.post-card:hover .post-title {
  color: var(--primary-color);
  transform: translateX(8px);
}

.status-icon {
  font-size: 14px;
}

.post-date {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-light);
  transition: var(--transition);
}

.post-card:hover .post-date {
  color: var(--text-secondary);
}

.meta-icon {
  font-size: 14px;
}

.post-stats .stat-item {
  padding: 4px 8px;
  border-radius: 12px;
  background: transparent;
}

.post-stats .stat-item:hover {
  background: rgba(52, 152, 219, 0.1);
}

/* 空状态 */
.empty-message {
  text-align: center;
  color: var(--text-light);
  padding: 80px 40px;
  font-size: 18px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: var(--border-radius);
  border: 2px dashed rgba(0, 0, 0, 0.1);
  transition: var(--transition);
}

.empty-message:hover {
  background: rgba(52, 152, 219, 0.05);
  border-color: var(--primary-color);
}

.empty-icon-large {
  font-size: 64px;
  margin-bottom: 24px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.empty-message h4 {
  font-size: 20px;
  color: var(--text-primary);
  margin-bottom: 8px;
  font-family: var(--font-display);
}

.empty-message p {
  margin-bottom: 32px;
  color: var(--text-secondary);
}

/* 头像上传区域 */
.avatar-uploader-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--text-light);
  transition: var(--transition);
}

.upload-icon {
  font-size: 32px;
}

.upload-text {
  font-size: 14px;
  font-weight: 500;
}

.avatar-uploader:hover .upload-icon,
.avatar-uploader:hover .upload-text {
  color: var(--primary-color);
}

/* 响应式设计 */
@media (max-width: 992px) {
  .profile-page {
    max-width: 100%;
  }
  
  .profile-header {
    gap: 32px;
  }
  
  .profile-info h3 {
    font-size: 24px;
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 24px;
  }
  
  .card {
    padding: 24px;
  }
  
  .profile-header {
    flex-direction: column;
    text-align: center;
    gap: 24px;
  }
  
  .profile-avatar {
    width: 120px;
    height: 120px;
  }
  
  .profile-info {
    text-align: center;
  }
  
  .user-stats {
    justify-content: center;
    gap: 24px;
  }
  
  .profile-actions {
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }
  
  .btn {
    width: 200px;
    text-align: center;
    justify-content: center;
  }
  
  .post-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .post-status {
    align-self: flex-start;
  }
  
  :deep(.el-dialog__header) {
    padding: 20px 24px;
  }
  
  :deep(.el-dialog__body) {
    padding: 24px;
  }
  
  :deep(.el-dialog__footer) {
    padding: 20px 24px;
    flex-direction: column;
    gap: 12px;
  }
  
  :deep(.el-button) {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .card {
    padding: 16px;
  }
  
  .profile-avatar {
    width: 100px;
    height: 100px;
  }
  
  .profile-info h3 {
    font-size: 20px;
  }
  
  .user-stats {
    flex-direction: column;
    gap: 8px;
    align-items: center;
  }
  
  .user-stats span {
    font-size: 14px;
  }
  
  .post-card h4 {
    font-size: 18px;
  }
  
  .post-content {
    font-size: 14px;
  }
}
</style>
