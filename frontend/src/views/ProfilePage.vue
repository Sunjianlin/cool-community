<template>
  <div class="profile-page">
    <h2 class="page-title">个人中心</h2>
    
    <div class="user-profile card">
      <div class="profile-header">
        <img :src="user.avatar || defaultAvatar" alt="用户头像" class="profile-avatar" />
        <div class="profile-info">
          <h3>{{ user.nickname || user.username }}</h3>
          <p class="user-bio">{{ user.bio || '暂无个人简介' }}</p>
          <div class="user-stats">
            <span>{{ user.postCount || 0 }} 帖子</span>
            <span>{{ user.followerCount || 0 }} 关注者</span>
            <span>{{ user.followingCount || 0 }} 关注</span>
          </div>
        </div>
      </div>
      <div class="profile-actions">
        <button class="btn btn-primary" @click="showEditDialog = true">编辑资料</button>
        <button class="btn btn-secondary" @click="logout">退出登录</button>
      </div>
    </div>
    
    <div class="user-posts card">
      <h3 class="section-title">我的帖子</h3>
      <div class="post-list">
        <div v-if="userPosts.length === 0" class="empty-message">
          还没有发布过帖子
        </div>
        <div v-else class="post-card" v-for="post in userPosts" :key="post.id" @click="$router.push(`/post/${post.id}`)">
          <h4>{{ post.title }}</h4>
          <p class="post-content">{{ truncate(post.content, 100) }}</p>
          <div class="post-meta">
            <span>发布于 {{ formatDate(post.createTime) }}</span>
            <div class="post-stats">
              <span>{{ post.likeCount || 0 }} 赞</span>
              <span>{{ post.commentCount || 0 }} 评论</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <el-dialog v-model="showEditDialog" title="编辑资料" width="500px">
      <el-form :model="editForm" label-width="80px">
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
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="editForm.bio" type="textarea" rows="3" placeholder="请输入个人简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import userApi from '../api/userApi'
import postApi from '../api/postApi'
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
const editForm = ref({
  username: '',
  nickname: '',
  email: '',
  avatar: '',
  bio: ''
})

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
  } else {
    router.push('/login')
  }
}

const loadUserPosts = async () => {
  if (userStore.user?.id) {
    try {
      const response = await postApi.getPostList({ userId: userStore.user.id, page: 1, pageSize: 10 })
      userPosts.value = response.data?.records || []
    } catch (error) {
      console.error('加载帖子失败:', error)
    }
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

onMounted(() => {
  loadUserProfile()
  loadUserPosts()
})
</script>

<style scoped>
.profile-page {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  margin-bottom: 30px;
  color: #333;
  text-align: center;
}

.card {
  background-color: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.user-profile {
  text-align: center;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 24px;
}

.profile-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #3498db;
}

.profile-info {
  flex: 1;
  text-align: left;
}

.profile-info h3 {
  font-size: 20px;
  margin-bottom: 8px;
  color: #333;
}

.user-bio {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

.user-stats {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #333;
}

.profile-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.section-title {
  font-size: 18px;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 2px solid #3498db;
  padding-bottom: 8px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-message {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 16px;
}

.post-card {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.post-card h4 {
  font-size: 16px;
  margin-bottom: 8px;
  color: #333;
}

.post-content {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.4;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.post-stats {
  display: flex;
  gap: 16px;
}

.avatar-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
}

.btn-secondary {
  background: #95a5a6;
  color: white;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
  
  .profile-info {
    text-align: center;
  }
  
  .user-stats {
    justify-content: center;
  }
  
  .profile-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>
