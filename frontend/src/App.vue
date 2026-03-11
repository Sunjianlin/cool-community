<template>
  <div class="app">
    <header class="top-nav">
      <div class="top-nav-content">
        <div class="top-nav-left">
          <a href="/" class="top-nav-logo" @click.prevent="navigateTo('/')">CoolCommunity</a>
          <nav class="nav">
            <a href="/activities" class="nav-item" :class="{ active: $route.path === '/activities' }" @click.prevent="navigateTo('/activities')">活动</a>
            <a href="/" class="nav-item" :class="{ active: $route.path === '/' }" @click.prevent="navigateTo('/')">首页</a>
            <a href="/topics" class="nav-item" :class="{ active: $route.path === '/topics' }" @click.prevent="navigateTo('/topics')">话题</a>
            <a href="/products" class="nav-item" :class="{ active: $route.path === '/products' }" @click.prevent="navigateTo('/products')">产品</a>
            <a href="/admin" v-if="isAdmin" class="nav-item" :class="{ active: $route.path === '/admin' }" @click.prevent="navigateTo('/admin')">管理</a>
          </nav>
        </div>
        <div class="top-nav-center">
          <div class="search-container">
            <span class="search-icon">🔍</span>
            <input type="text" class="search-input" placeholder="搜索话题、帖子、用户..." v-model="searchKeyword" @keyup.enter="handleSearch" />
          </div>
        </div>
        <div class="top-nav-right">
          <router-link to="/create-post" class="btn btn-primary" v-if="userStore.isLoggedIn">发帖</router-link>
          <button class="btn btn-secondary" v-if="userStore.isLoggedIn" @click="handleCheckin" :class="{ 'checked-in': hasCheckedInToday }">
            {{ hasCheckedInToday ? '已签到' : '签到' }}
          </button>
          <div class="points-display" v-if="userStore.isLoggedIn">
            <span class="points-icon">🎁</span>
            <span class="points-count">{{ userPoints }}</span>
          </div>
          <template v-if="userStore.isLoggedIn">
            <!-- 消息通知入口 -->
            <a href="javascript:void(0)" class="message-icon-wrapper" @click="openMessageCenter">
              <span class="message-icon">🔔</span>
              <span v-if="totalUnreadCount > 0" class="message-badge">{{ totalUnreadCount > 99 ? '99+' : totalUnreadCount }}</span>
            </a>
            <el-dropdown trigger="click">
              <div class="avatar-wrapper">
                <img :src="userStore.userAvatar" alt="用户头像" class="avatar-img" />
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push(`/user/${userStore.user?.id}`)">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="editProfile">编辑资料</el-dropdown-item>
                  <el-dropdown-item @click="openMessageCenter">消息通知</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="btn btn-outline">登录</router-link>
          </template>
        </div>
      </div>
    </header>
    <main class="main">
      <router-view v-slot="{ Component }">
        <component :is="Component" :key="$route.fullPath + refreshKey" />
      </router-view>
    </main>
    <footer class="footer">
      <div class="footer-content">
        <h3 class="footer-logo" style="font-size: 24px; font-weight: 700; color: var(--primary-color); margin-bottom: 32px; font-family: var(--font-display);">CoolCommunity</h3>
        <div class="footer-links">
          <a href="/" class="footer-link">首页</a>
          <a href="/topics" class="footer-link">话题</a>
          <a href="/products" class="footer-link">产品</a>
          <a href="/about" class="footer-link">关于我们</a>
          <a href="/privacy" class="footer-link">隐私政策</a>
          <a href="/terms" class="footer-link">使用条款</a>
        </div>
        <div class="footer-social">
          <a href="#" class="footer-social-link">📱</a>
          <a href="#" class="footer-social-link">🐦</a>
          <a href="#" class="footer-social-link">📘</a>
          <a href="#" class="footer-social-link">📧</a>
        </div>
        <p>© 2026 CoolCommunity - 高仿酷安社区</p>
        <p style="margin-top: 8px; font-size: 12px; color: var(--text-light);">保留所有权利</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, computed, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from './store/user'
import { useChatStore } from './store/chat'
import heartbeatService from './services/heartbeatService'
import checkinApi from './api/checkinApi'
import pointsApi from './api/pointsApi'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const chatStore = useChatStore()
const searchKeyword = ref('')
let messageCheckInterval = null
const refreshKey = ref(0)

// 签到相关状态
const hasCheckedInToday = ref(false)
const userPoints = ref(0)

const isAdmin = computed(() => {
  return userStore.isAdmin
})

const totalUnreadCount = computed(() => {
  return chatStore.unreadCount || 0
})

const navigateTo = (path) => {
  if (route.path === path) {
    refreshKey.value++
  } else {
    router.push(path)
  }
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    const url = `/search?keyword=${encodeURIComponent(searchKeyword.value.trim())}`
    if (route.path === '/search' && route.query.keyword === searchKeyword.value.trim()) {
      refreshKey.value++
    } else {
      router.push(url)
    }
  }
}

const handleLogout = async () => {
  await userStore.logout()
  router.push('/')
}

const editProfile = () => {
  if (userStore.user?.id) {
    router.push(`/user/${userStore.user.id}?edit=true`)
  }
}

const openMessageCenter = () => {
  window.open('/message', '_blank')
}

// 处理签到
const handleCheckin = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  try {
    const response = await checkinApi.checkin()
    if (response.code === 200) {
      const pointsEarned = response.data
      if (pointsEarned > 0) {
        ElMessage.success(`签到成功！获得 ${pointsEarned} 积分`)
        hasCheckedInToday.value = true
        userPoints.value += pointsEarned
      } else {
        ElMessage.info('您今天已经签到过了')
      }
    }
  } catch (error) {
    console.error('签到失败:', error)
    ElMessage.error('签到失败，请稍后重试')
  }
}

// 检查签到状态
const checkCheckinStatus = async () => {
  if (!userStore.isLoggedIn) return
  
  try {
    const response = await checkinApi.hasCheckedInToday()
    if (response.code === 200) {
      hasCheckedInToday.value = response.data
    }
  } catch (error) {
    console.error('检查签到状态失败:', error)
  }
}

// 获取用户积分
const getUserPoints = async () => {
  if (!userStore.isLoggedIn) return
  
  try {
    const response = await pointsApi.getUserPoints()
    if (response.code === 200) {
      userPoints.value = response.data
    }
  } catch (error) {
    console.error('获取用户积分失败:', error)
  }
}

const checkUnreadMessages = async () => {
  // 使用chat store中的未读消息计数
  if (userStore.isLoggedIn) {
    await chatStore.loadUnreadCount()
  }
}

const startMessageCheck = () => {
  // 每30秒检查一次未读消息
  messageCheckInterval = setInterval(() => {
    checkUnreadMessages()
  }, 30000)
}

// 监听用户登录状态变化
watch(
  () => userStore.isLoggedIn,
  (isLoggedIn) => {
    if (isLoggedIn) {
      // 用户登录后启动心跳服务
      heartbeatService.start()
      console.log('用户登录，启动心跳服务')
      // 检查签到状态和获取积分
      checkCheckinStatus()
      getUserPoints()
    } else {
      // 用户登出后停止心跳服务
      heartbeatService.stop()
      console.log('用户登出，停止心跳服务')
      // 重置状态
      hasCheckedInToday.value = false
      userPoints.value = 0
    }
  },
  { immediate: true }
)

onMounted(async () => {
  await userStore.initAuth()
  startMessageCheck()
  await checkUnreadMessages()
  
  if (userStore.isLoggedIn) {
    heartbeatService.start()
    checkCheckinStatus()
    getUserPoints()
  }
})

onUnmounted(() => {
  if (messageCheckInterval) {
    clearInterval(messageCheckInterval)
  }
  heartbeatService.stop()
})
</script>

<style>
:root {
  --primary-color: #3498db;
  --primary-hover: #2980b9;
  --secondary-color: #8e44ad;
  --secondary-hover: #7d3c98;
  --accent-color: #e74c3c;
  --accent-hover: #c0392b;
  --success-color: #2ecc71;
  --success-hover: #27ae60;
  --warning-color: #f39c12;
  --warning-hover: #e67e22;
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

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: var(--font-primary);
  background-color: var(--background-light);
  color: var(--text-primary);
  line-height: 1.6;
  font-size: 14px;
  overflow-x: hidden;
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 导航栏设计 */
.top-nav {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: var(--transition);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.top-nav:hover {
  box-shadow: var(--shadow-md);
  background: rgba(255, 255, 255, 0.98);
}

.top-nav-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 68px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.top-nav-left {
  display: flex;
  align-items: center;
  gap: 32px;
}

.top-nav-logo {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary-color);
  text-decoration: none;
  cursor: pointer;
  transition: var(--transition);
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: var(--font-display);
  position: relative;
}

.top-nav-logo::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--primary-color);
  transition: width 0.3s ease;
}

.top-nav-logo:hover::after {
  width: 100%;
}

.top-nav-logo:hover {
  color: var(--primary-hover);
  transform: translateY(-1px);
}

.nav {
  display: flex;
  gap: 8px;
}

.nav-item {
  text-decoration: none;
  color: var(--text-secondary);
  font-size: 15px;
  font-weight: 500;
  padding: 10px 20px;
  border-radius: 25px;
  transition: var(--transition);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  background: transparent;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(52, 152, 219, 0.1), transparent);
  transition: left 0.5s;
}

.nav-item:hover::before {
  left: 100%;
}

.nav-item:hover {
  background-color: rgba(52, 152, 219, 0.08);
  color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(52, 152, 219, 0.15);
}

.nav-item.active {
  background: var(--primary-color);
  color: white;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(52, 152, 219, 0.3);
}

.nav-item.active:hover {
  background: var(--primary-hover);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.4);
}

/* 搜索框设计 */
.top-nav-center {
  flex: 1;
  max-width: 450px;
  margin: 0 32px;
}

.search-container {
  position: relative;
  width: 100%;
  transition: var(--transition);
}

.search-container:focus-within {
  transform: translateY(-2px);
}

.search-icon {
  position: absolute;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  color: var(--text-light);
  transition: var(--transition);
  z-index: 1;
}

.search-input {
  width: 100%;
  padding: 12px 20px 12px 48px;
  border: 1px solid var(--border-color);
  border-radius: 30px;
  font-size: 14px;
  background: var(--background-white);
  transition: var(--transition);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(5px);
}

.search-input::placeholder {
  color: var(--text-light);
  transition: var(--transition);
}

.search-input:focus {
  outline: none;
  border-color: var(--primary-color);
  background: var(--background-white);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.2);
  transform: scale(1.02);
}

.search-input:focus::placeholder {
  color: transparent;
}

/* 右侧操作区设计 */
.top-nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 按钮设计 */
.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 30px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: var(--transition);
  position: relative;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-display);
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
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.btn-primary:hover {
  background: var(--primary-hover);
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(52, 152, 219, 0.4);
}

.btn-outline {
  background: transparent;
  border: 2px solid var(--primary-color);
  color: var(--primary-color);
  transition: var(--transition);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.btn-outline:hover {
  background: var(--primary-color);
  color: white;
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(52, 152, 219, 0.4);
}

/* 用户头像设计 */
.avatar-wrapper {
  cursor: pointer;
  transition: var(--transition);
  position: relative;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-wrapper:hover {
  transform: translateY(-3px) scale(1.05);
}

.avatar-img {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--background-white);
  transition: var(--transition);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.avatar-wrapper:hover .avatar-img {
  border-color: var(--primary-color);
  box-shadow: 0 4px 16px rgba(52, 152, 219, 0.3);
}

/* 私信图标样式 */
.message-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  transition: var(--transition);
  text-decoration: none;
  background: var(--background-white);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
}

.message-icon-wrapper:hover {
  background: var(--primary-color);
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 4px 16px rgba(52, 152, 219, 0.3);
  border-color: var(--primary-color);
}

.message-icon {
  font-size: 22px;
  cursor: pointer;
  transition: var(--transition);
  color: var(--text-secondary);
}

.message-icon-wrapper:hover .message-icon {
  transform: scale(1.1);
  color: white;
}

/* 消息数量角标样式 */
.message-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  min-width: 24px;
  height: 24px;
  padding: 0 8px;
  background: var(--accent-color);
  color: white;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.4);
  animation: pulse 2s infinite;
  border: 2px solid white;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

/* 主内容区设计 */
.main {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
  width: 100%;
}

/* 页脚设计 */
.footer {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-top: 1px solid var(--border-color);
  margin-top: 64px;
  transition: var(--transition);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.footer:hover {
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.1);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px;
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
  transition: var(--transition);
}

.footer-content:hover {
  color: var(--text-primary);
}

.footer-content p {
  margin-bottom: 16px;
  font-weight: 500;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.footer-link {
  color: var(--text-secondary);
  text-decoration: none;
  transition: var(--transition);
  font-weight: 500;
  position: relative;
}

.footer-link::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--primary-color);
  transition: width 0.3s ease;
}

.footer-link:hover::after {
  width: 100%;
}

.footer-link:hover {
  color: var(--primary-color);
  transform: translateY(-2px);
}

.footer-social {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 32px;
}

.footer-social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--background-white);
  color: var(--text-secondary);
  transition: var(--transition);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-decoration: none;
}

.footer-social-link:hover {
  background: var(--primary-color);
  color: white;
  transform: translateY(-3px) scale(1.1);
  box-shadow: 0 4px 16px rgba(52, 152, 219, 0.3);
}

/* 响应式设计 */
@media (max-width: 992px) {
  .top-nav-content {
    padding: 0 16px;
  }
  
  .top-nav-left {
    gap: 24px;
  }
  
  .top-nav-center {
    margin: 0 24px;
    max-width: 300px;
  }
  
  .main {
    padding: 24px 16px;
  }
}

@media (max-width: 768px) {
  .top-nav-content {
    height: 56px;
  }
  
  .top-nav-left {
    gap: 16px;
  }
  
  .top-nav-logo {
    font-size: 18px;
  }
  
  .nav {
    gap: 8px;
  }
  
  .nav-item {
    padding: 6px 12px;
    font-size: 14px;
  }
  
  .top-nav-center {
    margin: 0 16px;
    max-width: 200px;
  }
  
  .search-input {
    padding: 8px 12px 8px 36px;
    font-size: 13px;
  }
  
  .top-nav-right {
    gap: 12px;
  }
  
  .btn {
    padding: 8px 16px;
    font-size: 13px;
  }
  
  .avatar-img {
    width: 36px;
    height: 36px;
  }
  
  .message-icon-wrapper {
    width: 36px;
    height: 36px;
  }
}
</style>
