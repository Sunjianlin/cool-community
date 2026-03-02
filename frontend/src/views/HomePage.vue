<template>
  <div class="home-page">
    <div class="carousel">
      <div class="carousel-container">
        <div 
          class="carousel-item" 
          v-for="(item, index) in carouselItems" 
          :key="item.id"
          :class="{ active: currentIndex === index }"
          :style="{ background: item.background }"
        >
          <div class="carousel-content">
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
            <a :href="item.link" class="btn btn-primary" target="_blank">查看详情</a>
          </div>
        </div>
      </div>
      <div class="carousel-indicators">
        <span 
          v-for="(item, index) in carouselItems" 
          :key="item.id"
          class="indicator"
          :class="{ active: currentIndex === index }"
          @click="currentIndex = index"
        ></span>
      </div>
    </div>
    
    <div class="main-content">
      <section class="section">
        <h3 class="section-title">热门产品</h3>
        <div class="product-grid">
          <a 
            v-for="product in hotProducts" 
            :key="product.id" 
            :href="`/product/${product.id}`"
            target="_blank"
            class="product-card"
          >
            <img :src="product.image || defaultProductImage" class="product-image" />
            <div class="product-info">
              <h4 class="product-name">{{ product.name }}</h4>
              <p class="product-brand">{{ product.brand }}</p>
              <div class="product-meta">
                <span class="product-price">{{ product.price }}</span>
                <span v-if="product.avgRating" class="product-rating">⭐ {{ product.avgRating.toFixed(1) }}</span>
              </div>
            </div>
          </a>
        </div>
      </section>
      
      <section class="section">
        <h3 class="section-title">热门推荐</h3>
        <div class="post-list">
          <a 
            v-for="post in hotPosts" 
            :key="post.id" 
            :href="`/post/${post.id}`"
            target="_blank"
            class="post-card"
          >
            <div class="post-header">
              <router-link 
                :to="post.userId === userStore.user?.id ? `/user/${userStore.user?.id}` : (post.userId ? `/user/${post.userId}` : '#')"
                class="user-link"
                :disabled="!post.userId"
              >
                <img :src="post.userAvatar || defaultAvatar" class="user-avatar" />
                <div class="user-info">
                  <span class="username">{{ post.userNickname || post.username }}</span>
                  <span class="post-time">{{ formatDate(post.createTime) }}</span>
                </div>
              </router-link>
              <span v-if="post.topicName" class="topic-tag">{{ post.topicName }}</span>
            </div>
            <h4 class="post-title">{{ post.title }}</h4>
            <p class="post-content">{{ truncateWithStripHtml(post.content, 150) }}</p>
            <div class="post-stats">
              <span>{{ post.likeCount || 0 }} 赞</span>
              <span>{{ post.commentCount || 0 }} 评论</span>
              <span>{{ post.viewCount || 0 }} 浏览</span>
            </div>
          </a>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import topicApi from '../api/topicApi'
import postApi from '../api/postApi'
import productApi from '../api/productApi'
import { truncateWithStripHtml } from '../utils/textUtils'

const carouselItems = ref([])
const hotProducts = ref([])
const hotPosts = ref([])
const currentIndex = ref(0)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const defaultProductImage = 'https://cube.elemecdn.com/e/fd/0/yz33e8pE6VUm0fHQyUb7Z5Th4i4.png'
const userStore = useUserStore()

const backgrounds = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
]

let carouselInterval = null

const loadHotTopics = async () => {
  try {
    const response = await topicApi.getHotTopics({ page: 1, pageSize: 5 })
    const topics = response.data?.records || []
    carouselItems.value = topics.map((topic, index) => ({
      id: topic.id,
      title: topic.name,
      description: topic.description || '热门话题讨论',
      link: `/topic/${topic.id}`,
      background: backgrounds[index % backgrounds.length]
    }))
  } catch (error) {
    console.error('加载热门话题失败:', error)
    carouselItems.value = [
      { id: 1, title: 'iPhone 16系列讨论', description: '分享iPhone 16的使用体验和技巧', link: '/topic/1', background: backgrounds[0] },
      { id: 2, title: 'Android 15新系统体验', description: 'Android 15带来了哪些新功能？', link: '/topic/2', background: backgrounds[1] },
      { id: 3, title: '2026年笔记本电脑推荐', description: '各价位笔记本电脑推荐', link: '/topic/3', background: backgrounds[2] }
    ]
  }
}

const loadHotProducts = async () => {
  try {
    const response = await productApi.getProductList({ page: 1, pageSize: 8 })
    hotProducts.value = response.data?.records || []
  } catch (error) {
    console.error('加载热门产品失败:', error)
  }
}

const loadHotPosts = async () => {
  try {
    const response = await postApi.getPostList({ page: 1, pageSize: 10 })
    hotPosts.value = response.data?.records || []
    console.log('加载热门帖子成功:', hotPosts.value)
  } catch (error) {
    console.error('加载热门帖子失败:', error)
  }
}

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



const startCarousel = () => {
  stopCarousel()
  carouselInterval = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % carouselItems.value.length
  }, 5000)
}

const stopCarousel = () => {
  if (carouselInterval) {
    clearInterval(carouselInterval)
    carouselInterval = null
  }
}

onMounted(async () => {
  await Promise.all([loadHotTopics(), loadHotProducts(), loadHotPosts()])
  startCarousel()
})

onUnmounted(() => {
  stopCarousel()
})
</script>

<style scoped>
.home-page {
  width: 100%;
}

.user-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: inherit;
  flex: 1;
  transition: var(--transition);
}

.user-link:hover {
  transform: translateX(4px);
}

.user-link:hover .username {
  color: var(--primary-color);
}

/* 轮播图设计 */
.carousel {
  position: relative;
  width: 100%;
  height: 400px;
  border-radius: var(--border-radius);
  overflow: hidden;
  margin-bottom: 32px;
  box-shadow: var(--shadow-lg);
  background: var(--background-gradient);
}

.carousel-container {
  width: 100%;
  height: 100%;
  position: relative;
}

.carousel-item {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 80px;
  opacity: 0;
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
  background-size: cover;
  background-position: center;
}

.carousel-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(0,0,0,0.5) 0%, rgba(0,0,0,0.2) 100%);
  z-index: 1;
}

.carousel-item.active {
  opacity: 1;
  z-index: 2;
  transform: scale(1.05);
}

.carousel-content {
  text-align: center;
  max-width: 800px;
  z-index: 2;
  position: relative;
  animation: fadeInUp 1s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.carousel-content h3 {
  font-size: 36px;
  margin-bottom: 20px;
  font-weight: 700;
  font-family: var(--font-display);
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.carousel-content p {
  font-size: 18px;
  margin-bottom: 32px;
  opacity: 0.95;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
}

.carousel-content .btn {
  padding: 12px 32px;
  font-size: 16px;
  background: white;
  color: var(--primary-color);
  font-weight: 600;
  border-radius: 30px;
  transition: var(--transition);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.carousel-content .btn:hover {
  background: var(--primary-color);
  color: white;
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 8px 24px rgba(52, 152, 219, 0.4);
}

.carousel-indicators {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
  z-index: 3;
}

.indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
}

.indicator:hover,
.indicator.active {
  background-color: white;
  transform: scale(1.3);
  border-color: var(--primary-color);
  box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.3);
}

/* 主内容区设计 */
.main-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.section {
  background: var(--card-gradient);
  border-radius: var(--border-radius);
  padding: 32px;
  box-shadow: var(--shadow-md);
  transition: var(--transition);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.section:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 28px;
  padding-bottom: 16px;
  border-bottom: 3px solid var(--primary-color);
  color: var(--text-primary);
  font-family: var(--font-display);
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 80px;
  height: 3px;
  background: var(--secondary-color);
}

/* 产品网格设计 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.product-card {
  background: var(--background-white);
  border-radius: var(--border-radius);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  text-decoration: none;
  color: inherit;
  display: block;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.product-card::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(52, 152, 219, 0.1), transparent);
  transform: rotate(45deg);
  transition: all 0.6s ease;
  opacity: 0;
}

.product-card:hover::before {
  top: -30%;
  left: -30%;
  opacity: 1;
}

.product-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  transition: all 0.4s ease;
}

.product-card:hover .product-image {
  transform: scale(1.1);
}

.product-info {
  padding: 20px;
  position: relative;
  z-index: 1;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary);
}

.product-brand {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--accent-color);
  font-family: var(--font-display);
}

.product-rating {
  font-size: 14px;
  color: var(--warning-color);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 帖子列表设计 */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-card {
  background: var(--background-white);
  border-radius: var(--border-radius);
  padding: 24px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  text-decoration: none;
  color: inherit;
  display: block;
  box-shadow: var(--shadow-sm);
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

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  gap: 16px;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(52, 152, 219, 0.1);
  transition: var(--transition);
}

.post-card:hover .user-avatar {
  border-color: var(--primary-color);
  transform: scale(1.1);
}

.user-info {
  flex: 1;
}

.username {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  transition: var(--transition);
}

.post-time {
  display: block;
  font-size: 13px;
  color: var(--text-light);
  margin-top: 4px;
}

.topic-tag {
  padding: 6px 16px;
  background: rgba(52, 152, 219, 0.1);
  color: var(--primary-color);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  transition: var(--transition);
}

.post-card:hover .topic-tag {
  background: var(--primary-color);
  color: white;
  transform: scale(1.05);
}

.post-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-primary);
  font-family: var(--font-display);
  transition: var(--transition);
}

.post-card:hover .post-title {
  color: var(--primary-color);
  transform: translateX(8px);
}

.post-content {
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
  transition: var(--transition);
}

.post-card:hover .post-content {
  color: var(--text-primary);
}

.post-stats {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: var(--text-light);
  font-weight: 500;
  transition: var(--transition);
}

.post-stats span {
  display: flex;
  align-items: center;
  gap: 6px;
  transition: var(--transition);
}

.post-card:hover .post-stats span {
  color: var(--primary-color);
  transform: translateY(-2px);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .carousel {
    height: 320px;
  }
  
  .carousel-content h3 {
    font-size: 28px;
  }
  
  .carousel-content p {
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
  
  .carousel {
    height: 280px;
  }
  
  .carousel-content h3 {
    font-size: 24px;
  }
  
  .carousel-content p {
    font-size: 14px;
  }
  
  .section {
    padding: 24px;
  }
  
  .section-title {
    font-size: 20px;
  }
  
  .post-card {
    padding: 20px;
  }
  
  .post-title {
    font-size: 18px;
  }
}
</style>
