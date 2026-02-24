<template>
  <div class="app">
    <header class="top-nav">
      <div class="top-nav-content">
        <div class="top-nav-left">
          <a href="/" class="top-nav-logo" @click.prevent="navigateTo('/')">CoolCommunity</a>
          <nav class="nav">
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
          <template v-if="userStore.isLoggedIn">
            <el-dropdown trigger="click">
              <div class="avatar-wrapper">
                <img :src="userStore.userAvatar" alt="用户头像" class="avatar-img" />
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
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
        <keep-alive>
          <component :is="Component" :key="$route.fullPath" />
        </keep-alive>
      </router-view>
    </main>
    <footer class="footer">
      <div class="footer-content">
        <p>© 2026 CoolCommunity - 高仿酷安社区</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from './store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchKeyword = ref('')
let tokenValidationInterval = null

const isAdmin = computed(() => {
  return userStore.isAdmin
})

const navigateTo = (path) => {
  if (route.path === path) {
    window.location.reload()
  } else {
    router.push(path)
  }
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    const url = `/search?keyword=${encodeURIComponent(searchKeyword.value.trim())}`
    if (route.path === '/search' && route.query.keyword === searchKeyword.value.trim()) {
      window.location.reload()
    } else {
      router.push(url)
    }
  }
}

const handleLogout = async () => {
  await userStore.logout()
  router.push('/')
}

const startTokenValidation = () => {
  // 每30秒验证一次token的有效性
  tokenValidationInterval = setInterval(async () => {
    if (userStore.isLoggedIn) {
      await userStore.validateToken()
    }
  }, 30000)
}

onMounted(async () => {
  await userStore.initAuth()
  startTokenValidation()
})

onUnmounted(() => {
  if (tokenValidationInterval) {
    clearInterval(tokenValidationInterval)
  }
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f7fa;
  color: #333;
  line-height: 1.6;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.top-nav {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.top-nav-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.top-nav-left {
  display: flex;
  align-items: center;
  gap: 32px;
}

.top-nav-logo {
  font-size: 20px;
  font-weight: bold;
  color: #3498db;
  text-decoration: none;
  cursor: pointer;
}

.nav {
  display: flex;
  gap: 8px;
}

.nav-item {
  text-decoration: none;
  color: #666;
  font-size: 15px;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.nav-item:hover {
  background-color: #f0f8ff;
  color: #3498db;
}

.nav-item.active {
  background-color: #e8f4fc;
  color: #3498db;
}

.top-nav-center {
  flex: 1;
  max-width: 400px;
  margin: 0 24px;
}

.search-container {
  position: relative;
  width: 100%;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 14px;
}

.search-input {
  width: 100%;
  padding: 8px 12px 8px 36px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  font-size: 14px;
  background: #f5f7fa;
  transition: all 0.3s;
}

.search-input:focus {
  outline: none;
  border-color: #3498db;
  background: white;
}

.top-nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.btn {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
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

.btn-outline {
  background: transparent;
  border: 1px solid #3498db;
  color: #3498db;
}

.btn-outline:hover {
  background: #3498db;
  color: white;
}

.avatar-wrapper {
  cursor: pointer;
}

.avatar-img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.main {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  width: 100%;
}

.footer {
  background-color: white;
  border-top: 1px solid #e3f2fd;
  margin-top: 48px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  text-align: center;
  font-size: 14px;
  color: #7f8c8d;
}
</style>
