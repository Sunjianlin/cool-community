<template>
  <div class="product-page">
    <div class="product-header">
      <div class="product-info">
        <img :src="product.image || defaultProductImage" alt="产品图片" class="product-image" />
        <div class="product-details">
          <h1 class="product-name">{{ product.name || '产品名称' }}</h1>
          <div class="product-rating" v-if="product.rating">
            <span class="rating">{{ product.rating }}</span>
            <span class="rating-count">({{ product.ratingCount || 0 }}人评分)</span>
            <div class="rating-stars">
              <span class="star" v-for="i in 5" :key="i" :class="{ active: i <= Math.floor(product.rating) }">{{ i <= Math.floor(product.rating) ? '★' : '☆' }}</span>
            </div>
          </div>
          <p class="product-description">{{ product.description || '暂无描述' }}</p>
          <div class="product-price" v-if="product.price">
            <span class="price-label">参考价格：</span>
            <span class="price-value">{{ product.price }}</span>
          </div>
          <div class="product-actions">
            <button class="btn btn-primary" @click="toggleFavorite">
              {{ isFavorite ? '已收藏' : '收藏' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 产品配置信息 -->
    <div class="product-specs" v-if="product.specs && Object.keys(product.specs).length > 0">
      <h2 class="section-title">产品配置</h2>
      <div class="specs-grid">
        <div class="spec-item" v-for="(value, key) in product.specs" :key="key">
          <span class="spec-label">{{ formatSpecLabel(key) }}</span>
          <span class="spec-value">{{ value }}</span>
        </div>
      </div>
    </div>
    
    <!-- 产品评分详情 -->
    <div class="product-rating-detail" v-if="product.rating">
      <h2 class="section-title">评分详情</h2>
      <div class="rating-overview">
        <div class="overall-rating">
          <span class="rating-number">{{ product.rating }}</span>
          <span class="rating-text">综合评分</span>
        </div>
        <div class="rating-breakdown">
          <div class="rating-item" v-for="(item, index) in ratingBreakdown" :key="index">
            <span class="rating-category">{{ item.category }}</span>
            <div class="rating-bar">
              <div class="rating-fill" :style="{ width: (item.score / 5) * 100 + '%' }"></div>
            </div>
            <span class="rating-score">{{ item.score }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 用户评价 -->
    <div class="user-reviews">
      <h2 class="section-title">用户评价 ({{ reviews.length }})</h2>
      
      <!-- 评价输入 -->
      <div class="review-input">
        <img src="https://via.placeholder.com/40" alt="我的头像" class="user-avatar" />
        <div class="input-container">
          <div class="rating-input">
            <span class="rating-label">评分：</span>
            <div class="rating-stars">
              <span class="star" v-for="i in 5" :key="i" @click="setUserRating(i)" :class="{ active: i <= userRating }">{{ i <= userRating ? '★' : '☆' }}</span>
            </div>
          </div>
          <textarea class="input review-textarea" placeholder="写下你的评价..." v-model="userReview"></textarea>
          <button class="btn btn-primary" @click="submitReview">发表评价</button>
        </div>
      </div>
      
      <!-- 评价列表 -->
      <div class="reviews-list">
        <div class="review-item" v-for="review in reviews" :key="review.id">
          <img :src="review.userAvatar" alt="用户头像" class="user-avatar" />
          <div class="review-content">
            <div class="review-header">
              <span class="username">{{ review.username }}</span>
              <span class="review-time">{{ review.createdAt }}</span>
              <div class="review-rating">
                <span class="star" v-for="i in 5" :key="i" :class="{ active: i <= review.rating }">{{ i <= review.rating ? '★' : '☆' }}</span>
              </div>
            </div>
            <p class="review-text">{{ review.content }}</p>
            <div class="review-actions">
              <button class="review-action-btn">有用 ({{ review.usefulCount }})</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 讨论贴 -->
    <div class="product-discussions">
      <h2 class="section-title">讨论贴</h2>
      
      <!-- 讨论贴输入 -->
      <div class="discussion-input">
        <img src="https://via.placeholder.com/40" alt="我的头像" class="user-avatar" />
        <div class="input-container">
          <input type="text" class="input discussion-title-input" placeholder="讨论标题..." v-model="discussionTitle" />
          <textarea class="input discussion-textarea" placeholder="写下你的讨论内容..." v-model="discussionContent"></textarea>
          <button class="btn btn-primary" @click="submitDiscussion">发表讨论</button>
        </div>
      </div>
      
      <!-- 讨论贴列表 -->
      <div class="discussions-list">
        <div class="discussion-item" v-for="discussion in discussions" :key="discussion.id">
          <img :src="discussion.userAvatar" alt="用户头像" class="user-avatar" />
          <div class="discussion-content">
            <div class="discussion-header">
              <span class="username">{{ discussion.username }}</span>
              <span class="discussion-time">{{ discussion.createdAt }}</span>
            </div>
            <h4 class="discussion-title">{{ discussion.title }}</h4>
            <p class="discussion-text">{{ discussion.content }}</p>
            <div class="discussion-actions">
              <button class="discussion-action-btn">回复 ({{ discussion.replyCount }})</button>
              <button class="discussion-action-btn">点赞 ({{ discussion.likeCount }})</button>
              <button class="discussion-action-btn">查看详情</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import productApi from '../api/productApi'
import productReviewApi from '../api/productReviewApi'

const route = useRoute()
const productId = computed(() => route.params.id)

// 数据
const product = ref({})
const ratingBreakdown = ref([])
const reviews = ref([])
const discussions = ref([])
const isFavorite = ref(false)
const userRating = ref(0)
const defaultProductImage = 'https://cube.elemecdn.com/e/fd/0/yz33e8pE6VUm0fHQyUb7Z5Th4i4.png'

// 格式化配置标签
const formatSpecLabel = (key) => {
  const labelMap = {
    'processor': '处理器',
    'ram': '内存',
    'storage': '存储',
    'screen': '屏幕',
    'battery': '电池',
    'camera': '摄像头',
    'type': '类型',
    'noiseReduction': '降噪',
    'connection': '连接',
    'backlight': '背光',
    'switch': '轴体',
    'layout': '布局',
    'speed': '速率',
    'band': '频段',
    'ports': '网口',
    'devices': '带机量',
    'weight': '重量',
    'waterproof': '防水',
    'features': '功能'
  }
  return labelMap[key] || key
}
const userReview = ref('')
const discussionTitle = ref('')
const discussionContent = ref('')
const loading = ref(false)

// 模拟数据（当API调用失败时使用）
const mockProduct = {
  id: 1,
  name: 'iPhone 16 Pro Max',
  image: 'https://via.placeholder.com/300',
  rating: 4.8,
  ratingCount: 1234,
  description: 'iPhone 16 Pro Max是苹果公司于2025年发布的旗舰智能手机，搭载A18 Pro芯片，拥有6.7英寸超视网膜XDR显示屏，支持Pro级摄像头系统，包括4800万像素主摄、1200万像素超广角和长焦镜头。',
  price: '¥9999起',
  specs: [
    { label: '处理器', value: 'A18 Pro' },
    { label: '屏幕', value: '6.7英寸超视网膜XDR显示屏，2796×1290像素' },
    { label: '存储', value: '256GB/512GB/1TB/2TB' },
    { label: '相机', value: '4800万像素主摄 + 1200万像素超广角 + 1200万像素长焦' },
    { label: '电池', value: '4422mAh' },
    { label: '系统', value: 'iOS 18' },
    { label: '尺寸', value: '160.8 × 78.1 × 8.25 mm' },
    { label: '重量', value: '221g' }
  ]
}

const mockRatingBreakdown = [
  { category: '性能', score: 4.9 },
  { category: '相机', score: 4.8 },
  { category: '续航', score: 4.7 },
  { category: '屏幕', score: 4.9 },
  { category: '外观', score: 4.6 }
]

const mockReviews = [
  {
    id: 1,
    userAvatar: 'https://via.placeholder.com/40',
    username: '科技达人',
    rating: 5,
    content: 'iPhone 16 Pro Max的性能真的太强了，A18 Pro芯片处理任何任务都毫无压力。相机系统也有很大提升，尤其是夜景模式，拍出来的照片非常清晰。续航方面比上一代有明显提升，一天重度使用完全没问题。',
    createdAt: '2026-02-12',
    usefulCount: 45
  },
  {
    id: 2,
    userAvatar: 'https://via.placeholder.com/40',
    username: '摄影爱好者',
    rating: 4,
    content: '相机系统表现出色，4800万像素主摄细节丰富，长焦镜头的光学变焦效果也很好。不过价格确实有点高，而且重量对单手使用不太友好。',
    createdAt: '2026-02-11',
    usefulCount: 32
  },
  {
    id: 3,
    userAvatar: 'https://via.placeholder.com/40',
    username: '数码评测师',
    rating: 5,
    content: '作为一款旗舰手机，iPhone 16 Pro Max几乎没有缺点。屏幕素质顶级，性能强劲，相机优秀，系统流畅。虽然价格高，但确实物有所值。',
    createdAt: '2026-02-10',
    usefulCount: 56
  }
]

const mockDiscussions = [
  {
    id: 1,
    userAvatar: 'https://via.placeholder.com/40',
    username: '数码迷',
    title: 'iPhone 16 Pro Max续航表现如何？',
    content: '大家好，想了解一下iPhone 16 Pro Max的实际续航表现，尤其是重度使用情况下能坚持多久？另外，快充速度如何？',
    createdAt: '2026-02-13',
    replyCount: 12,
    likeCount: 28
  },
  {
    id: 2,
    userAvatar: 'https://via.placeholder.com/40',
    username: '游戏爱好者',
    title: 'iPhone 16 Pro Max玩游戏发热严重吗？',
    content: '打算用iPhone 16 Pro Max玩游戏，想知道长时间玩大型游戏时发热情况如何，会不会影响性能？',
    createdAt: '2026-02-12',
    replyCount: 8,
    likeCount: 15
  },
  {
    id: 3,
    userAvatar: 'https://via.placeholder.com/40',
    username: '摄影达人',
    title: 'iPhone 16 Pro Max相机对比华为Mate 70 Pro',
    content: '最近在纠结这两款手机，主要看重相机性能，有没有用过的朋友来对比一下？',
    createdAt: '2026-02-11',
    replyCount: 23,
    likeCount: 45
  }
]

// 获取产品详情
const fetchProductDetail = async () => {
  try {
    const response = await productApi.getProductById(productId.value)
    if (response) {
      // 处理产品数据
      let specs = null
      if (response.specs) {
        try {
          specs = typeof response.specs === 'string' ? JSON.parse(response.specs) : response.specs
        } catch (e) {
          specs = null
        }
      }
      product.value = {
        ...response,
        specs: specs
      }
    } else {
      product.value = mockProduct
    }
  } catch (error) {
    console.error('Failed to fetch product detail:', error)
    product.value = mockProduct
  }
}

// 获取产品评价
const fetchProductReviews = async () => {
  try {
    const response = await productReviewApi.getReviewsByProductId(productId.value)
    reviews.value = response || mockReviews
  } catch (error) {
    console.error('Failed to fetch product reviews:', error)
    reviews.value = mockReviews
  }
}

// 获取产品讨论贴
const fetchProductDiscussions = async () => {
  try {
    // 这里假设存在获取讨论贴的API
    // const response = await productApi.getDiscussionsByProductId(productId.value)
    // discussions.value = response || mockDiscussions
    // 暂时直接使用模拟数据
    discussions.value = mockDiscussions
  } catch (error) {
    console.error('Failed to fetch product discussions:', error)
    discussions.value = mockDiscussions
  }
}

// 设置评分
const setUserRating = (rating) => {
  userRating.value = rating
}

// 提交评价
const submitReview = async () => {
  if (userRating.value > 0 && userReview.value.trim()) {
    try {
      const reviewData = {
        content: userReview.value.trim(),
        rating: userRating.value,
        product: {
          id: productId.value
        },
        user: {
          id: 1 // 假设当前用户ID为1
        }
      }
      const response = await productReviewApi.createReview(reviewData)
      if (response) {
        // 使用返回的评价数据
        const newReview = {
          id: response.id,
          userAvatar: 'https://via.placeholder.com/40',
          username: '我',
          rating: response.rating,
          content: response.content,
          createdAt: new Date().toLocaleDateString(),
          usefulCount: 0
        }
        reviews.value.unshift(newReview)
        // 更新产品评分（这里应该从API重新获取，暂时使用本地计算）
        product.value.ratingCount++
        const totalRating = product.value.rating * (product.value.ratingCount - 1) + userRating.value
        product.value.rating = (totalRating / product.value.ratingCount).toFixed(1)
        userRating.value = 0
        userReview.value = ''
      }
    } catch (error) {
      console.error('Failed to submit review:', error)
      // 失败时使用本地数据
      const review = {
        id: reviews.value.length + 1,
        userAvatar: 'https://via.placeholder.com/40',
        username: '我',
        rating: userRating.value,
        content: userReview.value.trim(),
        createdAt: new Date().toLocaleDateString(),
        usefulCount: 0
      }
      reviews.value.unshift(review)
      product.value.ratingCount++
      const totalRating = product.value.rating * (product.value.ratingCount - 1) + userRating.value
      product.value.rating = (totalRating / product.value.ratingCount).toFixed(1)
      userRating.value = 0
      userReview.value = ''
    }
  }
}

// 提交讨论
const submitDiscussion = async () => {
  if (discussionTitle.value.trim() && discussionContent.value.trim()) {
    try {
      // 这里假设存在创建讨论贴的API
      // const discussionData = {
      //   title: discussionTitle.value.trim(),
      //   content: discussionContent.value.trim(),
      //   product: {
      //     id: productId.value
      //   },
      //   user: {
      //     id: 1 // 假设当前用户ID为1
      //   }
      // }
      // const response = await productApi.createDiscussion(discussionData)
      // if (response) {
      //   // 使用返回的讨论贴数据
      //   const newDiscussion = {
      //     id: response.id,
      //     userAvatar: 'https://via.placeholder.com/40',
      //     username: '我',
      //     title: response.title,
      //     content: response.content,
      //     createdAt: new Date().toLocaleDateString(),
      //     replyCount: 0,
      //     likeCount: 0
      //   }
      //   discussions.value.unshift(newDiscussion)
      //   discussionTitle.value = ''
      //   discussionContent.value = ''
      // }
      // 暂时使用本地数据
      const newDiscussion = {
        id: discussions.value.length + 1,
        userAvatar: 'https://via.placeholder.com/40',
        username: '我',
        title: discussionTitle.value.trim(),
        content: discussionContent.value.trim(),
        createdAt: new Date().toLocaleDateString(),
        replyCount: 0,
        likeCount: 0
      }
      discussions.value.unshift(newDiscussion)
      discussionTitle.value = ''
      discussionContent.value = ''
    } catch (error) {
      console.error('Failed to submit discussion:', error)
      // 失败时使用本地数据
      const newDiscussion = {
        id: discussions.value.length + 1,
        userAvatar: 'https://via.placeholder.com/40',
        username: '我',
        title: discussionTitle.value.trim(),
        content: discussionContent.value.trim(),
        createdAt: new Date().toLocaleDateString(),
        replyCount: 0,
        likeCount: 0
      }
      discussions.value.unshift(newDiscussion)
      discussionTitle.value = ''
      discussionContent.value = ''
    }
  }
}

// 收藏/取消收藏
const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchProductDetail(),
      fetchProductReviews(),
      fetchProductDiscussions()
    ])
    // 设置评分详情（模拟数据）
    ratingBreakdown.value = mockRatingBreakdown
  } catch (error) {
    console.error('Failed to fetch data:', error)
  } finally {
    loading.value = false
  }
  console.log(`ProductPage mounted for product ${productId.value}`)
})
</script>

<style scoped>
.product-page {
  width: 100%;
}

.product-header {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.product-info {
  display: flex;
  gap: 30px;
}

.product-image {
  width: 300px;
  height: 300px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.product-details {
  flex: 1;
}

.product-name {
  font-size: 28px;
  margin-bottom: 16px;
  color: #333;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.rating {
  font-size: 24px;
  font-weight: bold;
  color: #ff6b6b;
}

.rating-count {
  font-size: 14px;
  color: #999;
}

.rating-stars {
  display: flex;
  gap: 4px;
}

.star {
  font-size: 16px;
  color: #e5e5e5;
  cursor: pointer;
  transition: color 0.3s;
}

.star.active {
  color: #ff6b6b;
}

.product-description {
  font-size: 16px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 20px;
}

.product-price {
  margin-bottom: 20px;
  font-size: 18px;
}

.price-label {
  color: #999;
  margin-right: 8px;
}

.price-value {
  color: #ff6b6b;
  font-weight: 500;
}

.product-actions {
  display: flex;
  gap: 12px;
}

.product-specs {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 20px;
  margin-bottom: 24px;
  color: #333;
  border-bottom: 2px solid #ff6b6b;
  padding-bottom: 8px;
}

.specs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.spec-item {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.spec-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.spec-value {
  font-size: 14px;
  color: #333;
}

.product-rating-detail {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.rating-overview {
  display: flex;
  gap: 40px;
}

.overall-rating {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.rating-number {
  font-size: 32px;
  font-weight: bold;
  color: #ff6b6b;
  margin-bottom: 8px;
}

.rating-text {
  font-size: 14px;
  color: #666;
}

.rating-breakdown {
  flex: 1;
}

.rating-item {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.rating-category {
  width: 80px;
  font-size: 14px;
  color: #666;
}

.rating-bar {
  flex: 1;
  height: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.rating-fill {
  height: 100%;
  background-color: #ff6b6b;
  border-radius: 4px;
  transition: width 0.3s;
}

.rating-score {
  width: 40px;
  font-size: 14px;
  color: #333;
  text-align: right;
}

.user-reviews {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.review-input {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.review-input .user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  flex-shrink: 0;
}

.input-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rating-input {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating-label {
  font-size: 14px;
  color: #666;
}

.review-textarea {
  width: 100%;
  min-height: 100px;
  padding: 12px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  font-size: 14px;
  resize: vertical;
  font-family: inherit;
}

.review-textarea:focus {
  outline: none;
  border-color: #ff6b6b;
}

.input-container button {
  align-self: flex-end;
  padding: 8px 24px;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.review-item {
  display: flex;
  gap: 16px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.review-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.review-item .user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  flex-shrink: 0;
}

.review-content {
  flex: 1;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.review-header .username {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.review-time {
  font-size: 12px;
  color: #999;
}

.review-rating {
  display: flex;
  gap: 4px;
  margin-left: auto;
}

.review-text {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 12px;
}

.review-actions {
  display: flex;
  gap: 16px;
}

.review-action-btn {
  background: none;
  border: none;
  font-size: 12px;
  color: #999;
  cursor: pointer;
  padding: 4px 0;
  transition: color 0.3s;
}

.review-action-btn:hover {
  color: #ff6b6b;
}

/* 讨论贴样式 */
.product-discussions {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.discussion-input {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.discussion-input .user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  flex-shrink: 0;
}

.discussion-title-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  font-size: 14px;
  margin-bottom: 12px;
}

.discussion-title-input:focus {
  outline: none;
  border-color: #3498db;
}

.discussion-textarea {
  width: 100%;
  min-height: 100px;
  padding: 12px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  font-size: 14px;
  resize: vertical;
  font-family: inherit;
}

.discussion-textarea:focus {
  outline: none;
  border-color: #3498db;
}

.discussions-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.discussion-item {
  display: flex;
  gap: 16px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.discussion-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.discussion-item .user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  flex-shrink: 0;
}

.discussion-content {
  flex: 1;
}

.discussion-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.discussion-header .username {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.discussion-time {
  font-size: 12px;
  color: #999;
}

.discussion-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.discussion-text {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 12px;
}

.discussion-actions {
  display: flex;
  gap: 16px;
}

.discussion-action-btn {
  background: none;
  border: none;
  font-size: 12px;
  color: #999;
  cursor: pointer;
  padding: 4px 0;
  transition: color 0.3s;
}

.discussion-action-btn:hover {
  color: #3498db;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-header {
    padding: 20px;
  }
  
  .product-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .product-image {
    width: 200px;
    height: 200px;
  }
  
  .product-specs,
  .product-rating-detail,
  .user-reviews {
    padding: 20px;
  }
  
  .specs-grid {
    grid-template-columns: 1fr;
  }
  
  .rating-overview {
    flex-direction: column;
    align-items: center;
  }
  
  .rating-item {
    width: 100%;
  }
  
  .review-input {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .input-container button {
    align-self: stretch;
  }
}
</style>
