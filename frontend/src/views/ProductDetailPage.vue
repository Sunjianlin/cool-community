<template>
  <div class="product-detail-page">
    <div class="product-header card">
      <div class="product-image-section">
        <img :src="product.image || defaultImage" class="product-image" />
      </div>
      <div class="product-info-section">
        <h1>{{ product.name }}</h1>
        <p class="brand">{{ product.brand }}</p>
        <p class="price">{{ product.price }}</p>
        <div class="stats">
          <span>{{ product.reviewCount || 0 }} 评测</span>
          <span v-if="product.avgRating">⭐ {{ product.avgRating.toFixed(1) }}</span>
        </div>
        <div v-if="product.specs" class="specs">
          <h3>规格配置</h3>
          <p>{{ product.specs }}</p>
        </div>
        <p v-if="product.description" class="description">{{ product.description }}</p>
      </div>
    </div>
    
    <div class="related-posts-section card">
      <h2>相关帖子</h2>
      <div v-if="loadingPosts" class="loading">加载中...</div>
      <div v-else-if="posts.length > 0" class="posts-list">
        <div v-for="post in posts" :key="post.id" class="post-item">
          <router-link :to="`/post/${post.id}`" class="post-link">
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-content">{{ truncate(post.content, 100) }}</p>
            <div class="post-meta">
              <span class="post-author">{{ post.username }}</span>
              <span class="post-time">{{ formatDate(post.createTime) }}</span>
              <span class="post-likes">{{ post.likeCount || 0 }} 赞</span>
            </div>
          </router-link>
        </div>
      </div>
      <div v-else class="empty">暂无相关帖子</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import productApi from '../api/productApi'
import postApi from '../api/postApi'

const route = useRoute()
const product = ref({})
const posts = ref([])
const loadingPosts = ref(false)
const defaultImage = 'https://cube.elemecdn.com/e/fd/0/yz33e8pE6VUm0fHQyUb7Z5Th4i4.png'

const updatePageTitle = () => {
  document.title = product.value.name ? `${product.value.name} - 酷安社区` : '产品详情 - 酷安社区'
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
  const cleanText = text.replace(/<[^>]+>/g, '')
  return cleanText.length > length ? cleanText.substring(0, length) + '...' : cleanText
}

const loadProduct = async () => {
  try {
    const response = await productApi.getProductDetail(route.params.id)
    product.value = response.data || {}
    updatePageTitle()
  } catch (error) {
    console.error('加载产品失败:', error)
  }
}

const loadRelatedPosts = async () => {
  loadingPosts.value = true
  try {
    const response = await postApi.getPostList({
      productId: route.params.id,
      page: 1,
      pageSize: 10
    })
    if (response.code === 200 && response.data) {
      posts.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载相关帖子失败:', error)
    posts.value = []
  } finally {
    loadingPosts.value = false
  }
}

onMounted(() => {
  loadProduct()
  loadRelatedPosts()
})
</script>

<style scoped>
.product-detail-page {
  max-width: 1000px;
  margin: 0 auto;
}

.card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.product-header {
  display: flex;
  gap: 40px;
}

.product-image-section {
  width: 400px;
  flex-shrink: 0;
}

.product-image {
  width: 100%;
  border-radius: 8px;
}

.product-info-section {
  flex: 1;
}

.product-info-section h1 {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: 700;
}

.brand {
  color: #666;
  font-size: 16px;
  margin: 0 0 15px 0;
}

.price {
  color: #e74c3c;
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 15px 0;
}

.stats {
  display: flex;
  gap: 20px;
  color: #666;
  margin-bottom: 20px;
}

.specs {
  margin-bottom: 20px;
}

.specs h3 {
  font-size: 16px;
  margin-bottom: 10px;
  font-weight: 600;
}

.specs p {
  color: #666;
  white-space: pre-wrap;
  line-height: 1.5;
}

.description {
  color: #666;
  line-height: 1.6;
}

/* 相关帖子样式 */
.related-posts-section h2 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.loading, .empty {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
}

.post-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.post-link {
  text-decoration: none;
  color: inherit;
  display: block;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #333;
}

.post-content {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

.post-author {
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-header {
    flex-direction: column;
  }
  
  .product-image-section {
    width: 100%;
  }
  
  .product-info-section {
    margin-top: 20px;
  }
  
  .card {
    padding: 20px;
  }
}
</style>
