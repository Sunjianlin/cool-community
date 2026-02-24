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
import './style.css'

const routes = [
  { path: '/', component: HomePage },
  { path: '/topics', component: TopicsPage },
  { path: '/topic/:id', component: TopicDetailPage },
  { path: '/post/:id', component: PostDetailPage },
  { path: '/products', component: ProductsPage },
  { path: '/product/:id', component: ProductDetailPage },
  { path: '/login', component: LoginPage },
  { path: '/register', component: RegisterPage },
  { path: '/admin', component: AdminPage, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/profile', component: ProfilePage, meta: { requiresAuth: true } },
  { path: '/user/:id', component: UserDetailPage },
  { path: '/create-post', component: CreatePostPage, meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStr = localStorage.getItem('user')
  const token = localStorage.getItem('token')
  const isLoggedIn = !!(userStr && token)
  
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  } else if (to.meta.requiresAdmin) {
    let user = null
    try {
      user = JSON.parse(userStr)
    } catch (e) {}
    
    if (!user || user.role < 1) {
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
