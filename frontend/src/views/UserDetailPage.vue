<template>
  <div class="user-detail-page">
    <div class="user-header card">
      <div class="avatar-section">
        <img :src="userInfo.avatar || defaultAvatar" class="avatar" />
        <div class="user-info">
          <h2>{{ userInfo.nickname || '用户' }}</h2>
          <p class="username">@{{ userInfo.username }}</p>
          <div class="role-status-container">
            <p class="role-tag">{{ getRoleName(userInfo.role) }}</p>
            <p class="status-tag" :class="getOnlineStatusClass(userInfo.onlineStatus)">
              <span class="status-icon">{{ getStatusIcon(userInfo.onlineStatus) }}</span>
              {{ getOnlineStatusName(userInfo.onlineStatus) }}
            </p>
          </div>
          <p v-if="userInfo.bio" class="bio">{{ userInfo.bio }}</p>
          <div v-if="isCurrentUser" class="user-contact">
            <p v-if="userInfo.email" class="contact-item">📧 {{ userInfo.email }}</p>
            <p v-if="userInfo.phone" class="contact-item">📱 {{ userInfo.phone }}</p>
            <p v-if="userInfo.gender !== undefined" class="contact-item">
              {{ userInfo.gender === 0 ? '性别: 未知' : userInfo.gender === 1 ? '性别: 男' : '性别: 女' }}
            </p>
          </div>
          <div class="user-stats">
            <span v-if="isCurrentUser" class="stat-item stat-item-clickable" @click="showFollowers">
              <strong>{{ userInfo.followerCount || 0 }}</strong> 粉丝
            </span>
            <span v-else class="stat-item">
              <strong>{{ userInfo.followerCount || 0 }}</strong> 粉丝
            </span>
            <span v-if="isCurrentUser" class="stat-item stat-item-clickable" @click="showFollowing">
              <strong>{{ userInfo.followingCount || 0 }}</strong> 关注
            </span>
            <span v-else class="stat-item">
              <strong>{{ userInfo.followingCount || 0 }}</strong> 关注
            </span>
            <span class="stat-item">
              <strong>{{ userInfo.postCount || 0 }}</strong> 帖子
            </span>
          </div>
        </div>
      </div>
      <div class="user-actions">
        <template v-if="isCurrentUser">
          <button class="btn btn-primary" @click="editUserProfile">
            编辑资料
          </button>
          <button class="btn btn-outline" @click="showStatusDialog = true">
            设置状态
          </button>
        </template>
        <template v-else>
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
        </template>
      </div>
    </div>

    <div class="user-posts">
      <h3>发布的帖子</h3>
      <div v-if="posts.length === 0" class="empty">暂无帖子</div>
      <div v-else class="post-list">
        <div v-for="post in posts" :key="post.id" class="post-card" @click="$router.push(`/post/${post.id}`)">
          <div class="post-header">
            <h4>{{ post.title }}</h4>
            <span v-if="isCurrentUser" :class="['post-status', getStatusClass(post.status)]">
              <span v-if="post.status === 0" class="status-icon">⏳</span>
              <span v-else-if="post.status === 1" class="status-icon">✅</span>
              <span v-else-if="post.status === 2" class="status-icon">❌</span>
              <span v-else class="status-icon">❓</span>
              {{ getStatusName(post.status) }}
            </span>
          </div>
          <p>{{ truncateWithStripHtml(post.content, 100) }}</p>
          <div class="post-meta">
            <span>{{ formatDate(post.createTime) }}</span>
            <span>{{ post.likeCount || 0 }} 赞</span>
            <span>{{ post.commentCount || 0 }} 评论</span>
            <span>{{ post.viewCount || 0 }} 浏览</span>
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

    <!-- 编辑资料模态框 -->
    <el-dialog v-model="showEditDialog" title="编辑资料" width="500px">
      <div class="edit-profile-form">
        <div class="form-item">
          <label>昵称</label>
          <input type="text" v-model="editForm.nickname" placeholder="请输入昵称" />
        </div>
        <div class="form-item">
          <label>邮箱</label>
          <input type="email" v-model="editForm.email" placeholder="请输入邮箱" />
        </div>
        <div class="form-item">
          <label>手机号</label>
          <input type="tel" v-model="editForm.phone" placeholder="请输入手机号" />
        </div>
        <div class="form-item">
          <label>性别</label>
          <div class="gender-select">
            <label class="gender-option">
              <input type="radio" v-model="editForm.gender" value="0" />
              <span>未知</span>
            </label>
            <label class="gender-option">
              <input type="radio" v-model="editForm.gender" value="1" />
              <span>男</span>
            </label>
            <label class="gender-option">
              <input type="radio" v-model="editForm.gender" value="2" />
              <span>女</span>
            </label>
          </div>
        </div>
        <div class="form-item">
          <label>个人简介</label>
          <textarea v-model="editForm.bio" placeholder="请输入个人简介" rows="3"></textarea>
        </div>
        <div class="form-item">
          <label>头像</label>
          <div class="avatar-upload">
            <img :src="editForm.avatar || defaultAvatar" class="edit-avatar" />
            <input type="file" accept="image/*" @change="handleAvatarUpload" />
            <span class="upload-text">点击上传头像</span>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <button class="btn btn-outline" @click="showEditDialog = false">取消</button>
          <button class="btn btn-primary" @click="saveProfile" :disabled="savingProfile">
            {{ savingProfile ? '保存中...' : '保存' }}
          </button>
        </span>
      </template>
    </el-dialog>

    <!-- 设置状态模态框 -->
    <el-dialog v-model="showStatusDialog" title="设置在线状态" width="400px">
      <div class="status-setting-form">
        <div class="form-item">
          <label>当前状态</label>
          <div class="status-select">
            <label class="status-option" v-for="status in statusOptions" :key="status.value">
              <input type="radio" v-model="selectedStatus" :value="status.value" />
              <span class="status-icon">{{ status.icon }}</span>
              <span class="status-text">{{ status.label }}</span>
            </label>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <button class="btn btn-outline" @click="showStatusDialog = false">取消</button>
          <button class="btn btn-primary" @click="saveStatus" :disabled="savingStatus">
            {{ savingStatus ? '保存中...' : '保存' }}
          </button>
        </span>
      </template>
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
import { truncateWithStripHtml } from '../utils/textUtils'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userInfo = ref({})
const posts = ref([])
const showFollowDialog = ref(false)
const followType = ref('following')
const followList = ref([])
const showEditDialog = ref(false)
const editForm = ref({})
const savingProfile = ref(false)
const showStatusDialog = ref(false)
const selectedStatus = ref(0)
const statusOptions = [
  { value: 1, label: '在线', icon: '🟢' },
  { value: 2, label: '忙碌', icon: '🔴' },
  { value: 3, label: '离开', icon: '🟡' }
]

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
    const userId = route.params.id
    if (!userId) {
      console.error('用户ID不存在')
      return
    }
    const response = await userApi.getUserInfoById(userId)
    userInfo.value = response.data || {}
  } catch (error) {
    console.error('加载用户信息失败:', error)
    userInfo.value = {}
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
  if (!userInfo.value.id) {
    ElMessage.warning('用户信息加载中，请稍后再试')
    return
  }
  router.push(`/chat?userId=${userInfo.value.id}`)
}

const editUserProfile = () => {
  // 初始化编辑表单
  editForm.value = {
    nickname: userInfo.value.nickname || '',
    email: userInfo.value.email || '',
    phone: userInfo.value.phone || '',
    gender: userInfo.value.gender !== undefined ? userInfo.value.gender : 0,
    bio: userInfo.value.bio || '',
    avatar: userInfo.value.avatar || ''
  }
  showEditDialog.value = true
}

// 处理头像上传
const handleAvatarUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    // 预览图片
    const reader = new FileReader()
    reader.onload = (e) => {
      editForm.value.avatar = e.target.result
    }
    reader.readAsDataURL(file)

    // 上传文件到服务器
    const formData = new FormData()
    formData.append('file', file)

    userApi.uploadAvatar(file).then(response => {
      if (response.code === 200) {
        // 使用服务器返回的头像URL
        editForm.value.avatar = response.data
        ElMessage.success('头像上传成功')
      }
    }).catch(error => {
      console.error('头像上传失败:', error)
      ElMessage.error('头像上传失败')
    })
  }
}

// 保存个人资料
const saveProfile = async () => {
  if (!editForm.value.nickname.trim()) {
    ElMessage.warning('请输入昵称')
    return
  }

  savingProfile.value = true
  try {
    // 实际项目中应该调用API更新用户资料
    const response = await userApi.updateUserInfo(editForm.value)

    // 更新本地用户信息
    userInfo.value = {
      ...userInfo.value,
      ...editForm.value
    }

    // 更新用户存储中的头像
    if (editForm.value.avatar) {
      userStore.updateAvatar(editForm.value.avatar)
    }

    showEditDialog.value = false
    ElMessage.success('资料更新成功')
  } catch (error) {
    console.error('更新资料失败:', error)
    ElMessage.error('更新资料失败')
  } finally {
    savingProfile.value = false
  }
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

const getStatusName = (status) => {
  switch (status) {
    case 0:
      return '待审核'
    case 1:
      return '已发布'
    case 2:
      return '已拒绝'
    default:
      return '未知状态'
  }
}

// 获取在线状态的样式类
const getOnlineStatusClass = (status) => {
  switch (status) {
    case 1:
      return 'status-online'
    case 2:
      return 'status-busy'
    case 3:
      return 'status-away'
    default:
      return 'status-offline'
  }
}

// 获取在线状态的图标
const getStatusIcon = (status) => {
  switch (status) {
    case 1:
      return '🟢'
    case 2:
      return '🔴'
    case 3:
      return '🟡'
    default:
      return '⚪'
  }
}

// 获取在线状态的名称
const getOnlineStatusName = (status) => {
  switch (status) {
    case 1:
      return '在线'
    case 2:
      return '忙碌'
    case 3:
      return '离开'
    default:
      return '离线'
  }
}

// 保存状态
const saveStatus = async () => {
  savingProfile.value = true
  try {
    // 调用API更新用户在线状态
    await userApi.updateOnlineStatus(selectedStatus.value)

    // 更新本地用户信息
    userInfo.value = {
      ...userInfo.value,
      onlineStatus: selectedStatus.value
    }

    ElMessage.success('状态更新成功')
    showStatusDialog.value = false
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  } finally {
    savingProfile.value = false
  }
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
/* 导入全局CSS变量 */
:root {
  --primary-color: #3498db;
  --primary-hover: #2980b9;
  --secondary-color: #8e44ad;
  --accent-color: #e74c3c;
  --success-color: #2ecc71;
  --warning-color: #f39c12;
  --text-primary: #2c3e50;
  --text-secondary: #7f8c8d;
  --text-light: #95a5a6;
  --background-light: #f8f9fa;
  --background-white: #ffffff;
  --background-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --card-gradient: linear-gradient(145deg, #ffffff 0%, #f8f9fa 100%);
  --border-color: #e0e0e0;
  --border-radius: 12px;
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.08);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 8px 32px rgba(0, 0, 0, 0.12);
  --transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  --font-primary: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  --font-display: 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.user-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 用户头部卡片 */
.user-header {
  padding: 40px;
  margin-bottom: 32px;
  background: var(--card-gradient);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-md);
  transition: var(--transition);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.user-header:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 40px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

/* 头像 */
.avatar {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  border: 4px solid var(--primary-color);
  box-shadow: 0 8px 24px rgba(52, 152, 219, 0.3);
  transition: var(--transition);
  object-fit: cover;
}

.avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 32px rgba(52, 152, 219, 0.4);
}

/* 用户信息 */
.user-info h2 {
  font-size: 28px;
  margin: 0 0 8px 0;
  color: var(--text-primary);
  font-family: var(--font-display);
}

.username {
  color: var(--text-secondary);
  margin: 0 0 12px 0;
  font-size: 16px;
  background: rgba(0, 0, 0, 0.05);
  padding: 4px 12px;
  border-radius: 16px;
  display: inline-block;
  transition: var(--transition);
}

.username:hover {
  background: rgba(52, 152, 219, 0.1);
  color: var(--primary-color);
  transform: translateY(-2px);
}

.role-tag {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(52, 152, 219, 0.1);
  color: var(--primary-color);
  border-radius: 20px;
  font-size: 14px;
  margin-top: 8px;
  font-weight: 600;
  transition: var(--transition);
}

.role-tag:hover {
  background: var(--primary-color);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.bio {
  color: var(--text-secondary);
  margin-top: 16px;
  font-size: 16px;
  line-height: 1.6;
  max-width: 100%;
}

/* 联系信息 */
.user-contact {
  margin: 20px 0;
  padding: 20px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: var(--border-radius);
  font-size: 16px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.contact-item {
  margin: 8px 0;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 12px;
  transition: var(--transition);
}

.contact-item:hover {
  color: var(--primary-color);
  transform: translateX(8px);
}

/* 统计数据 */
.user-stats {
  display: flex;
  gap: 32px;
  margin-top: 20px;
}

.stat-item {
  transition: var(--transition);
  padding: 12px 20px;
  border-radius: 24px;
  background: rgba(0, 0, 0, 0.02);
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-item-clickable {
  cursor: pointer;
}

.stat-item-clickable:hover {
  color: var(--primary-color);
  background: rgba(52, 152, 219, 0.1);
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.2);
}

.stat-item strong {
  font-size: 20px;
  font-weight: 700;
  font-family: var(--font-display);
  color: var(--text-primary);
}

/* 用户操作按钮 */
.user-actions {
  display: flex;
  gap: 20px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  justify-content: center;
}

.btn {
  padding: 12px 32px;
  border: none;
  border-radius: 30px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition);
  font-family: var(--font-display);
  position: relative;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.btn:hover::before {
  left: 100%;
}

.btn-primary {
  background: var(--primary-color);
  color: white;
  box-shadow: 0 4px 16px rgba(52, 152, 219, 0.3);
}

.btn-primary:hover {
  background: var(--primary-hover);
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(52, 152, 219, 0.4);
}

.btn-following {
  background: rgba(52, 152, 219, 0.1);
  color: var(--primary-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.btn-following:hover {
  background: var(--primary-color);
  color: white;
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(52, 152, 219, 0.4);
}

.btn-outline {
  background: transparent;
  border: 2px solid var(--border-color);
  color: var(--text-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.btn-outline:hover {
  background: var(--text-secondary);
  color: white;
  border-color: var(--text-secondary);
  transform: translateY(-3px);
  box-shadow: 0 4px 16px rgba(127, 140, 141, 0.3);
}

.btn-sm {
  padding: 8px 20px;
  font-size: 14px;
  border-radius: 20px;
}

/* 帖子区域 */
.user-posts {
  background: var(--card-gradient);
  border-radius: var(--border-radius);
  padding: 32px;
  box-shadow: var(--shadow-md);
  transition: var(--transition);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.user-posts:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
}

.user-posts h3 {
  margin-bottom: 28px;
  font-size: 24px;
  color: var(--text-primary);
  font-family: var(--font-display);
  padding-bottom: 16px;
  border-bottom: 3px solid var(--primary-color);
  position: relative;
}

.user-posts h3::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 80px;
  height: 3px;
  background: var(--secondary-color);
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 帖子卡片 */
.post-card {
  background: var(--background-white);
  border-radius: var(--border-radius);
  padding: 24px;
  cursor: pointer;
  transition: var(--transition);
  border: 1px solid rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.post-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 0;
  background: var(--primary-color);
  transition: height 0.4s ease;
}

.post-card:hover::before {
  height: 100%;
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
}

/* 帖子头部 */
.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.post-card h4 {
  margin: 0 0 12px 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  font-family: var(--font-display);
  transition: var(--transition);
}

.post-card:hover h4 {
  color: var(--primary-color);
  transform: translateX(8px);
}

/* 帖子内容 */
.post-card p {
  color: var(--text-secondary);
  margin: 0 0 16px 0;
  line-height: 1.6;
  font-size: 16px;
  transition: var(--transition);
}

.post-card:hover p {
  color: var(--text-primary);
}

/* 帖子状态 */
.post-status {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  transition: var(--transition);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.status-pending {
  background-color: rgba(243, 156, 18, 0.1);
  color: var(--warning-color);
  border: 1px solid rgba(243, 156, 18, 0.2);
}

.status-published {
  background-color: rgba(46, 204, 113, 0.1);
  color: var(--success-color);
  border: 1px solid rgba(46, 204, 113, 0.2);
}

.status-rejected {
  background-color: rgba(231, 76, 60, 0.1);
  color: var(--accent-color);
  border: 1px solid rgba(231, 76, 60, 0.2);
}

.status-unknown {
  background-color: rgba(149, 165, 166, 0.1);
  color: var(--text-secondary);
  border: 1px solid rgba(149, 165, 166, 0.2);
}

.post-card:hover .post-status {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.status-icon {
  font-size: 14px;
}

/* 帖子元信息 */
.post-meta {
  display: flex;
  gap: 20px;
  color: var(--text-light);
  font-size: 14px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.post-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
  transition: var(--transition);
  font-weight: 500;
}

.post-card:hover .post-meta span {
  color: var(--primary-color);
  transform: translateY(-2px);
}

/* 空状态 */
.empty {
  text-align: center;
  padding: 80px 40px;
  color: var(--text-light);
  background: rgba(0, 0, 0, 0.02);
  border-radius: var(--border-radius);
  border: 2px dashed rgba(0, 0, 0, 0.1);
  transition: var(--transition);
}

.empty:hover {
  background: rgba(52, 152, 219, 0.05);
  border-color: var(--primary-color);
}

.empty::before {
  content: '📝';
  font-size: 64px;
  display: block;
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

.empty h4 {
  font-size: 20px;
  color: var(--text-primary);
  margin-bottom: 8px;
  font-family: var(--font-display);
}

.empty p {
  font-size: 16px;
  color: var(--text-secondary);
}

/* 关注列表 */
.follow-list {
  max-height: 400px;
  overflow-y: auto;
}

.follow-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  transition: var(--transition);
}

.follow-item:hover {
  background: rgba(52, 152, 219, 0.05);
  padding-left: 16px;
  border-radius: 8px;
}

.follow-item:last-child {
  border-bottom: none;
}

.follow-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  margin-right: 16px;
  border: 2px solid var(--border-color);
  transition: var(--transition);
  object-fit: cover;
}

.follow-item:hover .follow-avatar {
  border-color: var(--primary-color);
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.2);
}

.follow-info {
  flex: 1;
}

.follow-name {
  font-weight: 600;
  display: block;
  font-size: 16px;
  color: var(--text-primary);
  margin-bottom: 4px;
  transition: var(--transition);
}

.follow-item:hover .follow-name {
  color: var(--primary-color);
}

.follow-bio {
  font-size: 14px;
  color: var(--text-secondary);
  display: block;
  line-height: 1.4;
}

.empty-follow {
  text-align: center;
  padding: 80px 40px;
  color: var(--text-light);
  background: rgba(0, 0, 0, 0.02);
  border-radius: var(--border-radius);
  border: 2px dashed rgba(0, 0, 0, 0.1);
}

.empty-follow::before {
  content: '👥';
  font-size: 64px;
  display: block;
  margin-bottom: 24px;
  animation: float 3s ease-in-out infinite;
}

/* 编辑资料表单样式 */
.edit-profile-form {
  margin-bottom: 24px;
}

.form-item {
  margin-bottom: 24px;
}

.form-item label {
  display: block;
  margin-bottom: 12px;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 16px;
}

.form-item input[type="text"],
.form-item input[type="email"],
.form-item input[type="tel"],
.form-item textarea {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  font-size: 16px;
  transition: var(--transition);
  background: var(--background-white);
}

.form-item input[type="text"]:focus,
.form-item input[type="email"]:focus,
.form-item input[type="tel"]:focus,
.form-item textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
  transform: translateY(-2px);
}

.form-item textarea {
  resize: vertical;
  min-height: 120px;
}

/* 性别选择样式 */
.gender-select {
  display: flex;
  gap: 32px;
}

.gender-option {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 12px 20px;
  border-radius: 12px;
  transition: var(--transition);
  border: 2px solid var(--border-color);
  background: var(--background-white);
}

.gender-option:hover {
  border-color: var(--primary-color);
  background: rgba(52, 152, 219, 0.05);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.15);
}

.gender-option input[type="radio"] {
  cursor: pointer;
  width: 20px;
  height: 20px;
  accent-color: var(--primary-color);
}

.gender-option span {
  font-size: 16px;
  color: var(--text-primary);
  font-weight: 500;
}

/* 头像上传样式 */
.avatar-upload {
  position: relative;
  width: 140px;
  height: 140px;
  margin: 0 auto;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
  transition: var(--transition);
  border: 3px solid var(--border-color);
}

.avatar-upload:hover {
  border-color: var(--primary-color);
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(52, 152, 219, 0.2);
}

.edit-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  transition: var(--transition);
}

.avatar-upload:hover .edit-avatar {
  transform: scale(1.1);
}

.avatar-upload input[type="file"] {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  z-index: 2;
}

.upload-text {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  color: white;
  text-align: center;
  padding: 20px 0;
  font-size: 14px;
  font-weight: 600;
  z-index: 1;
  transition: var(--transition);
}

.avatar-upload:hover .upload-text {
  background: linear-gradient(transparent, var(--primary-color));
  padding: 24px 0;
}

/* 角色和状态容器样式 */
.role-status-container {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 8px;
  flex-wrap: wrap;
}

/* 状态标签样式 */
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  transition: var(--transition);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.status-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.status-online {
  background-color: rgba(46, 204, 113, 0.1);
  color: var(--success-color);
  border-color: rgba(46, 204, 113, 0.2);
}

.status-busy {
  background-color: rgba(231, 76, 60, 0.1);
  color: var(--accent-color);
  border-color: rgba(231, 76, 60, 0.2);
}

.status-away {
  background-color: rgba(243, 156, 18, 0.1);
  color: var(--warning-color);
  border-color: rgba(243, 156, 18, 0.2);
}

.status-offline {
  background-color: rgba(149, 165, 166, 0.1);
  color: var(--text-secondary);
  border-color: rgba(149, 165, 166, 0.2);
}

.status-icon {
  font-size: 14px;
}

/* 状态设置样式 */
.status-setting-form {
  margin-bottom: 24px;
}

.status-select {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-option {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  padding: 16px 20px;
  border-radius: 12px;
  transition: var(--transition);
  border: 2px solid var(--border-color);
  background: var(--background-white);
}

.status-option:hover {
  border-color: var(--primary-color);
  background: rgba(52, 152, 219, 0.05);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.15);
}

.status-option input[type="radio"] {
  cursor: pointer;
  width: 20px;
  height: 20px;
  accent-color: var(--primary-color);
}

.status-text {
  font-size: 16px;
  color: var(--text-primary);
  font-weight: 500;
  flex: 1;
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: var(--border-radius);
  overflow: hidden;
  box-shadow: var(--shadow-lg) !important;
  border: none !important;
}

:deep(.el-dialog__header) {
  background: var(--primary-color);
  color: white;
  padding: 24px 32px;
  text-align: center;
}

:deep(.el-dialog__title) {
  color: white;
  font-size: 20px;
  font-weight: 700;
  font-family: var(--font-display);
}

:deep(.el-dialog__body) {
  padding: 32px;
  background: var(--background-white);
}

:deep(.el-dialog__footer) {
  padding: 24px 32px;
  background: var(--background-light);
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: center;
  gap: 20px;
}

/* 对话框底部按钮样式 */
.dialog-footer {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.dialog-footer .btn {
  padding: 10px 24px;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .user-detail-page {
    max-width: 100%;
  }

  .avatar-section {
    gap: 32px;
  }

  .user-info h2 {
    font-size: 24px;
  }
}

@media (max-width: 768px) {
  .user-header {
    padding: 24px;
  }

  .avatar-section {
    flex-direction: column;
    text-align: center;
    gap: 24px;
  }

  .avatar {
    width: 120px;
    height: 120px;
  }

  .user-info {
    text-align: center;
  }

  .user-stats {
    justify-content: center;
    gap: 24px;
    flex-wrap: wrap;
  }

  .user-actions {
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }

  .btn {
    width: 240px;
    text-align: center;
    justify-content: center;
  }

  .user-posts {
    padding: 24px;
  }

  .post-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .post-status {
    align-self: flex-start;
  }

  .gender-select {
    flex-direction: column;
    gap: 16px;
  }

  .gender-option {
    justify-content: center;
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

  :deep(.el-dialog__footer) .btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .user-header {
    padding: 16px;
  }

  .avatar {
    width: 100px;
    height: 100px;
  }

  .user-info h2 {
    font-size: 20px;
  }

  .user-stats {
    flex-direction: column;
    gap: 12px;
    align-items: center;
  }

  .stat-item {
    width: 200px;
    justify-content: center;
  }

  .user-posts {
    padding: 16px;
  }

  .post-card {
    padding: 16px;
  }

  .post-card h4 {
    font-size: 18px;
  }

  .post-meta {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
