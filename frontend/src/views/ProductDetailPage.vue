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
          <span>{{ product.followCount || 0 }} 关注</span>
        </div>
        <div class="action-buttons">
          <button 
            v-if="userStore.isLoggedIn" 
            :class="['follow-button', { 'following': isFollowing }]"
            @click="toggleFollow"
            :disabled="loadingFollow"
          >
            {{ loadingFollow ? '处理中...' : (isFollowing ? '已关注' : '关注') }}
          </button>
          <router-link v-else to="/login" class="login-button">登录后关注</router-link>
        </div>
        <div v-if="product.description" class="description">{{ product.description }}</div>
      </div>
    </div>
    
    <div v-if="product.specs" class="specs-section card">
      <h2>规格配置</h2>
      <div v-if="parsedSpecs" class="specs-table-wrapper">
        <table class="specs-table">
          <tbody>
            <template v-for="(value, key) in parsedSpecs" :key="key">
              <tr v-if="typeof value !== 'object'">
                <td class="spec-label">{{ formatSpecKey(key) }}</td>
                <td class="spec-value">{{ value }}</td>
              </tr>
              <template v-else>
                <tr class="spec-group-header">
                  <td colspan="2">{{ formatSpecKey(key) }}</td>
                </tr>
                <tr v-for="(subValue, subKey) in value" :key="subKey">
                  <td class="spec-label sub-label">{{ formatSpecKey(subKey) }}</td>
                  <td class="spec-value">{{ subValue }}</td>
                </tr>
              </template>
            </template>
          </tbody>
        </table>
      </div>
      <div v-else class="specs-text">
        <p>{{ product.specs }}</p>
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
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import productApi from '../api/productApi'
import postApi from '../api/postApi'
import followApi from '../api/followApi'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const product = ref({})
const posts = ref([])
const loadingPosts = ref(false)
const defaultImage = 'https://cube.elemecdn.com/e/fd/0/yz33e8pE6VUm0fHQyUb7Z5Th4i4.png'

const userStore = useUserStore()
const isFollowing = ref(false)
const loadingFollow = ref(false)

const parsedSpecs = computed(() => {
  if (!product.value.specs) return null
  try {
    const parsed = JSON.parse(product.value.specs)
    if (typeof parsed === 'object' && parsed !== null) {
      return parsed
    }
    return null
  } catch (e) {
    return null
  }
})

const formatSpecKey = (key) => {
  if (!key) return ''
  const keyMap = {
    'cpu': 'CPU',
    'gpu': 'GPU',
    'ram': '内存',
    'storage': '存储',
    'screen': '屏幕',
    'battery': '电池',
    'camera': '摄像头',
    'os': '操作系统',
    'weight': '重量',
    'size': '尺寸',
    'resolution': '分辨率',
    'refreshRate': '刷新率',
    'processor': '处理器',
    'memory': '内存',
    'disk': '硬盘',
    'display': '显示器',
    'graphics': '显卡',
    'motherboard': '主板',
    'power': '电源',
    'cooling': '散热',
    'network': '网络',
    'interface': '接口',
    'audio': '音频',
    'sensor': '传感器',
    'material': '材质',
    'color': '颜色',
    'warranty': '保修',
    'price': '价格',
    'releaseDate': '发布日期',
    'model': '型号',
    'brand': '品牌',
    'type': '类型',
    'capacity': '容量',
    'speed': '速度',
    'powerConsumption': '功耗',
    'noise': '噪音',
    'brightness': '亮度',
    'contrast': '对比度',
    'viewAngle': '可视角度',
    'responseTime': '响应时间',
    'charging': '充电',
    'wireless': '无线',
    'bluetooth': '蓝牙',
    'wifi': 'WiFi',
    'nfc': 'NFC',
    'gps': 'GPS',
    'fingerprint': '指纹识别',
    'faceId': '面部识别',
    'waterResist': '防水等级',
    'dustResist': '防尘等级'
  }
  
  if (keyMap[key.toLowerCase()]) {
    return keyMap[key.toLowerCase()]
  }
  
  return key
    .replace(/([A-Z])/g, ' $1')
    .replace(/_/g, ' ')
    .replace(/^\w/, c => c.toUpperCase())
    .trim()
}

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
    
    if (userStore.isLoggedIn) {
      await checkFollowStatus()
    }
  } catch (error) {
    console.error('加载产品失败:', error)
  }
}

const checkFollowStatus = async () => {
  try {
    const response = await followApi.checkFollow(route.params.id, 2)
    isFollowing.value = response.data || false
  } catch (error) {
    console.error('检查关注状态失败:', error)
    isFollowing.value = false
  }
}

const toggleFollow = async () => {
  if (loadingFollow.value) return
  
  if (isFollowing.value) {
    ElMessageBox.confirm(
      `确定要取消关注「${product.value.name}」吗？`,
      '取消关注',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      loadingFollow.value = true
      try {
        const response = await followApi.unfollowProduct(route.params.id)
        if (response.code === 200) {
          product.value.followCount = response.data
          isFollowing.value = false
          ElMessage.success('已取消关注')
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      } finally {
        loadingFollow.value = false
      }
    }).catch(() => {})
  } else {
    loadingFollow.value = true
    try {
      const response = await followApi.followProduct(route.params.id)
      if (response.code === 200) {
        product.value.followCount = response.data
        isFollowing.value = true
        ElMessage.success('关注成功')
      }
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    } finally {
      loadingFollow.value = false
    }
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

.specs-section {
  margin-bottom: 25px;
}

.specs-section h2 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.specs-table-wrapper {
  overflow-x: auto;
}

.specs-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.specs-table tbody tr {
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.specs-table tbody tr:last-child {
  border-bottom: none;
}

.specs-table tbody tr:hover {
  background-color: #fafafa;
}

.spec-group-header {
  background-color: #f5f7fa;
}

.spec-group-header td {
  font-weight: 600;
  color: #333;
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
}

.spec-label {
  width: 180px;
  min-width: 120px;
  padding: 14px 16px;
  background-color: #fafafa;
  color: #666;
  font-weight: 500;
  font-size: 14px;
  vertical-align: top;
  border-right: 1px solid #f0f0f0;
}

.spec-label.sub-label {
  padding-left: 32px;
  background-color: #fff;
}

.spec-value {
  padding: 14px 16px;
  color: #333;
  font-size: 14px;
  line-height: 1.6;
}

.specs-text {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.specs-text p {
  margin: 0;
  color: #666;
  white-space: pre-wrap;
  line-height: 1.8;
}

.description {
  color: #666;
  line-height: 1.6;
}

.action-buttons {
  margin: 20px 0;
  display: flex;
  gap: 12px;
}

.follow-button {
  padding: 10px 24px;
  border: 2px solid #007bff;
  border-radius: 20px;
  background: white;
  color: #007bff;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.follow-button:hover {
  background: #007bff;
  color: white;
}

.follow-button.following {
  border-color: #28a745;
  color: #28a745;
}

.follow-button.following:hover {
  background: #28a745;
  color: white;
}

.follow-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-button {
  padding: 10px 24px;
  border: 2px solid #6c757d;
  border-radius: 20px;
  background: white;
  color: #6c757d;
  font-weight: 600;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
  display: inline-block;
  text-align: center;
}

.login-button:hover {
  background: #6c757d;
  color: white;
}

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
  
  .specs-table {
    display: block;
  }
  
  .specs-table tbody {
    display: block;
  }
  
  .specs-table tbody tr {
    display: flex;
    flex-direction: column;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
  }
  
  .specs-table tbody tr:last-child {
    border-bottom: none;
  }
  
  .spec-group-header {
    background-color: #f5f7fa;
    padding: 8px 12px;
    margin-bottom: 8px;
  }
  
  .spec-label {
    width: 100%;
    min-width: auto;
    padding: 4px 12px;
    background-color: transparent;
    border-right: none;
    font-size: 13px;
    color: #999;
  }
  
  .spec-label.sub-label {
    padding-left: 12px;
  }
  
  .spec-value {
    padding: 4px 12px;
    font-size: 14px;
    font-weight: 500;
  }
}
</style>
