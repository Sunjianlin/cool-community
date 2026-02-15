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
              <img :src="post.userAvatar || defaultAvatar" class="user-avatar" />
              <div class="user-info">
                <span class="username">{{ post.userNickname || post.username }}</span>
                <span class="post-time">{{ formatDate(post.createTime) }}</span>
              </div>
              <span v-if="post.topicName" class="topic-tag">{{ post.topicName }}</span>
            </div>
            <h4 class="post-title">{{ post.title }}</h4>
            <p class="post-content">{{ truncate(post.content, 150) }}</p>
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
import topicApi from '../api/topicApi'
import postApi from '../api/postApi'
import productApi from '../api/productApi'

const carouselItems = ref([])
const hotProducts = ref([])
const hotPosts = ref([])
const currentIndex = ref(0)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const defaultProductImage = 'https://cube.elemecdn.com/e/fd/0/yz33e8pE6VUm0fHQyUb7Z5Th4i4.png'

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

const truncate = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
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

.carousel {
  position: relative;
  width: 100%;
  height: 320px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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
  padding: 0 60px;
  opacity: 0;
  transition: opacity 0.5s ease;
  z-index: 1;
}

.carousel-item.active {
  opacity: 1;
  z-index: 2;
}

.carousel-content {
  text-align: center;
  max-width: 700px;
}

.carousel-content h3 {
  font-size: 28px;
  margin-bottom: 16px;
  font-weight: 700;
}

.carousel-content p {
  font-size: 16px;
  margin-bottom: 24px;
  opacity: 0.9;
}

.carousel-content .btn {
  padding: 10px 28px;
  font-size: 15px;
  background-color: white;
  color: #333;
}

.carousel-indicators {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
  z-index: 3;
}

.indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s;
}

.indicator:hover,
.indicator.active {
  background-color: white;
  transform: scale(1.2);
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #3498db;
  color: #333;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.product-card {
  background: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
  color: inherit;
  display: block;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100%;
  height: 140px;
  object-fit: cover;
}

.product-info {
  padding: 12px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-brand {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 14px;
  font-weight: 600;
  color: #e74c3c;
}

.product-rating {
  font-size: 12px;
  color: #f39c12;
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
  text-decoration: none;
  color: inherit;
  display: block;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 10px;
}

.user-info {
  flex: 1;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.post-time {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.topic-tag {
  padding: 4px 12px;
  background: #e8f4fc;
  color: #3498db;
  border-radius: 12px;
  font-size: 12px;
}

.post-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #333;
}

.post-content {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
}

.post-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

@media (max-width: 992px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .carousel {
    height: 260px;
  }
  
  .carousel-content h3 {
    font-size: 22px;
  }
}
</style>
