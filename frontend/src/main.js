import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import App from './App.vue'
import HomePage from './views/HomePage.vue'
import TopicsPage from './views/TopicsPage.vue'
import TopicPage from './views/TopicPage.vue'
import TopicDetailPage from './views/TopicDetailPage.vue'
import PostDetailPage from './views/PostDetailPage.vue'
import ProductsPage from './views/ProductsPage.vue'
import ProductDetailPage from './views/ProductDetailPage.vue'
import LoginPage from './views/LoginPage.vue'
import RegisterPage from './views/RegisterPage.vue'
import AdminPage from './views/AdminPage.vue'
import ProfilePage from './views/ProfilePage.vue'
import CreatePostPage from './views/CreatePostPage.vue'
import UserDetailPage from './views/UserDetailPage.vue'
import ChatPage from './views/ChatPage.vue'
import SeckillPage from './views/SeckillPage.vue'
import BackgroundManagementPage from './views/BackgroundManagementPage.vue'
import ActivitiesPage from './views/ActivitiesPage.vue'
import './style.css'

const routes = [
  { path: '/', component: HomePage, meta: { requiresAuth: true } },
  { path: '/topics', component: TopicsPage, meta: { requiresAuth: true } },
  { path: '/topic/:id', component: TopicDetailPage, meta: { requiresAuth: true } },
  { path: '/post/:id', component: PostDetailPage, meta: { requiresAuth: true } },
  { path: '/products', component: ProductsPage, meta: { requiresAuth: true } },
  { path: '/product/:id', component: ProductDetailPage, meta: { requiresAuth: true } },
  { path: '/login', component: LoginPage },
  { path: '/register', component: RegisterPage },
  { path: '/admin', component: AdminPage, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/user/:id', component: UserDetailPage, meta: { requiresAuth: true } },
  { path: '/create-post', component: CreatePostPage, meta: { requiresAuth: true } },
  { path: '/chat', component: ChatPage, meta: { requiresAuth: true } },
  { path: '/activities', component: ActivitiesPage, meta: { requiresAuth: true } },
  { path: '/seckill/:id', component: SeckillPage, meta: { requiresAuth: true } },
  { path: '/profile/background', component: BackgroundManagementPage, meta: { requiresAuth: true } }
]

// 先初始化用户状态，再处理路由守卫
import { useUserStore } from './store/user'

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 确保用户状态已初始化
  if (!userStore.isInitialized) {
    await userStore.initAuth()
  }
  
  const isLoggedIn = userStore.isLoggedIn
  
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  } else if (to.meta.requiresAdmin) {
    if (!userStore.isAdmin) {
      next('/')
    } else {
      next()
    }
  } else {
    next()
  }
})

const pinia = createPinia()

const app = createApp(App)
app.use(router)
app.use(pinia)
app.use(ElementPlus)
app.component('QuillEditor', QuillEditor)
app.mount('#app')
