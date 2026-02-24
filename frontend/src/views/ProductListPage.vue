<template>
  <div class="product-list-page">
    <h2 class="page-title">产品中心</h2>
    
    <!-- 筛选和搜索 -->
    <section class="section filter-section">
      <div class="filter-container">
        <div class="filter-group">
          <label class="filter-label">分类：</label>
          <select v-model="filter.categoryId" @change="fetchProducts">
            <option value="">全部分类</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>
        </div>
        <div class="filter-group">
          <label class="filter-label">品牌：</label>
          <select v-model="filter.brand" @change="fetchProducts">
            <option value="">全部品牌</option>
            <option v-for="brand in brands" :key="brand" :value="brand">
              {{ brand }}
            </option>
          </select>
        </div>
        <div class="filter-group search-group">
          <input 
            type="text" 
            v-model="filter.keyword" 
            placeholder="搜索产品名称或品牌" 
            @keyup.enter="fetchProducts"
            class="search-input"
          />
          <button @click="fetchProducts" class="search-btn">搜索</button>
        </div>
      </div>
    </section>
    
    <!-- 产品列表 -->
    <section class="section">
      <div class="section-header">
        <h3 class="section-title">产品列表</h3>
        <div class="section-info">
          <span>共 {{ total }} 个产品</span>
        </div>
      </div>
      
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="products.length === 0" class="empty-container">
        <p>暂无产品</p>
      </div>
      
      <div v-else class="product-grid">
        <div class="product-card" v-for="product in products" :key="product.id">
          <router-link :to="`/product/${product.id}`">
            <div class="product-card-img">
              <img :src="product.image" alt="产品图片" />
            </div>
            <div class="product-card-body">
              <h4 class="product-name">{{ product.name }}</h4>
              <div class="product-category">{{ product.category_name }}</div>
              <div class="product-rating">
                <span class="rating">{{ product.avg_rating || 0 }}</span>
                <div class="rating-stars">
                  <span class="star" v-for="i in 5" :key="i" :class="{ active: i <= Math.floor(product.avg_rating || 0) }">{{ i <= Math.floor(product.avg_rating || 0) ? '★' : '☆' }}</span>
                </div>
                <span class="rating-count">({{ product.review_count }}人)</span>
              </div>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-price" v-if="product.price">
                {{ product.price }}
              </div>
            </div>
          </router-link>
        </div>
      </div>
      
      <!-- 分页 -->
      <div v-if="products.length > 0" class="pagination">
        <button 
          class="pagination-btn" 
          @click="changePage(1)"
          :disabled="currentPage === 1"
        >
          首页
        </button>
        <button 
          class="pagination-btn" 
          @click="changePage(currentPage - 1)"
          :disabled="currentPage === 1"
        >
          上一页
        </button>
        <span class="pagination-info">
          第 {{ currentPage }} 页，共 {{ totalPages }} 页
        </span>
        <button 
          class="pagination-btn" 
          @click="changePage(currentPage + 1)"
          :disabled="currentPage === totalPages"
        >
          下一页
        </button>
        <button 
          class="pagination-btn" 
          @click="changePage(totalPages)"
          :disabled="currentPage === totalPages"
        >
          末页
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import productApi from '../api/productApi'
import categoryApi from '../api/categoryApi'

// 数据
const products = ref([])
const total = ref(0)
const currentPage = ref(1)
const totalPages = ref(1)
const filter = ref({
  categoryId: '',
  brand: '',
  keyword: ''
})
const categories = ref([])
const brands = ref(['Apple', '小米', '华为', 'ThinkPad', '索尼', '罗技', '雷蛇'])
const loading = ref(false)

// 模拟数据（当API调用失败时使用）
const mockProducts = [
  {
    id: 1,
    name: 'iPhone 16 Pro Max',
    image: 'https://via.placeholder.com/300',
    avg_rating: 4.8,
    review_count: 1234,
    description: '苹果最新旗舰手机，搭载A18 Pro芯片',
    price: '¥9999起',
    brand: 'Apple',
    category_name: '手机'
  },
  {
    id: 2,
    name: '小米15 Ultra',
    image: 'https://via.placeholder.com/300',
    avg_rating: 4.7,
    review_count: 987,
    description: '小米最新旗舰手机，徕卡影像',
    price: '¥6999起',
    brand: '小米',
    category_name: '手机'
  },
  {
    id: 3,
    name: 'MacBook Pro M3',
    image: 'https://via.placeholder.com/300',
    avg_rating: 4.9,
    review_count: 765,
    description: '苹果最新笔记本电脑，M3芯片',
    price: '¥12999起',
    brand: 'Apple',
    category_name: '笔记本电脑'
  },
  {
    id: 4,
    name: 'AirPods Pro 3',
    image: 'https://via.placeholder.com/300',
    avg_rating: 4.6,
    review_count: 876,
    description: '苹果最新降噪耳机，主动降噪',
    price: '¥1899起',
    brand: 'Apple',
    category_name: '耳机'
  },
  {
    id: 5,
    name: '华为Mate 70 Pro',
    image: 'https://via.placeholder.com/300',
    avg_rating: 4.5,
    review_count: 654,
    description: '华为旗舰手机，鸿蒙系统',
    price: '¥6499起',
    brand: '华为',
    category_name: '手机'
  },
  {
    id: 6,
    name: 'ThinkPad X1 Carbon',
    image: 'https://via.placeholder.com/300',
    avg_rating: 4.7,
    review_count: 543,
    description: '商务笔记本电脑，轻薄便携',
    price: '¥9999起',
    brand: 'ThinkPad',
    category_name: '笔记本电脑'
  },
  {
    id: 7,
    name: '小米路由器AX9000',
    image: 'https://via.placeholder.com/300',
    avg_rating: 4.4,
    review_count: 432,
    description: '小米旗舰路由器，Wi-Fi 6',
    price: '¥1299起',
    brand: '小米',
    category_name: '网络设备'
  },
  {
    id: 8,
    name: '索尼WH-1000XM5',
    image: 'https://via.placeholder.com/300',
    avg_rating: 4.8,
    review_count: 321,
    description: '索尼旗舰降噪耳机',
    price: '¥2999起',
    brand: '索尼',
    category_name: '耳机'
  },
  {
    id: 9,
    name: '罗技G913 TKL',
    image: 'https://via.placeholder.com/300',
    avg_rating: 4.6,
    review_count: 210,
    description: '罗技机械键盘，无线连接',
    price: '¥1299起',
    brand: '罗技',
    category_name: '键盘'
  },
  {
    id: 10,
    name: '雷蛇炼狱蝰蛇V3',
    image: 'https://via.placeholder.com/300',
    avg_rating: 4.5,
    review_count: 198,
    description: '雷蛇游戏鼠标，高精度传感器',
    price: '¥399起',
    brand: '雷蛇',
    category_name: '鼠标'
  }
]

// 获取产品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: 10,
      categoryId: filter.value.categoryId || undefined,
      brand: filter.value.brand || undefined,
      keyword: filter.value.keyword || undefined
    }
    
    const response = await productApi.getProductList(params)
    
    if (response && response.data) {
      products.value = response.data.records || []
      total.value = response.data.total || 0
      totalPages.value = Math.ceil(total.value / 10)
    } else {
      // 使用模拟数据
      products.value = mockProducts
      total.value = mockProducts.length
      totalPages.value = Math.ceil(total.value / 10)
    }
  } catch (error) {
    console.error('Failed to fetch products:', error)
    // 使用模拟数据
    products.value = mockProducts
    total.value = mockProducts.length
    totalPages.value = Math.ceil(total.value / 10)
  } finally {
    loading.value = false
  }
}

// 切换页码
const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchProducts()
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await categoryApi.getAllCategories()
    if (response && response.data) {
      categories.value = response.data
    } else {
      // 使用模拟分类
      categories.value = [
        { id: 1, name: '手机' },
        { id: 2, name: '笔记本电脑' },
        { id: 3, name: '耳机' },
        { id: 4, name: '网络设备' },
        { id: 5, name: '键盘' },
        { id: 6, name: '鼠标' }
      ]
    }
  } catch (error) {
    console.error('Failed to fetch categories:', error)
    // 使用模拟分类
    categories.value = [
      { id: 1, name: '手机' },
      { id: 2, name: '笔记本电脑' },
      { id: 3, name: '耳机' },
      { id: 4, name: '网络设备' },
      { id: 5, name: '键盘' },
      { id: 6, name: '鼠标' }
    ]
  }
}

onMounted(async () => {
  await fetchCategories()
  await fetchProducts()
  console.log('ProductListPage mounted')
})
</script>

<style scoped>
.product-list-page {
  width: 100%;
}

.page-title {
  font-size: 24px;
  margin-bottom: 30px;
  color: #333;
  text-align: center;
}

.section {
  background-color: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 筛选区域 */
.filter-section {
  padding: 16px 24px;
}

.filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.filter-group select {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 120px;
}

.search-group {
  display: flex;
  gap: 8px;
  flex: 1;
  min-width: 200px;
}

.search-input {
  flex: 1;
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.search-btn {
  padding: 6px 16px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-btn:hover {
  background-color: #2980b9;
}

/* 产品列表区域 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  color: #333;
  border-bottom: 2px solid #3498db;
  padding-bottom: 8px;
  flex: 1;
}

.section-info {
  font-size: 14px;
  color: #666;
}

/* 加载和空状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

/* 产品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

/* 产品卡片 */
.product-card {
  background-color: #f9f9f9;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid #e3f2fd;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  border-color: #3498db;
}

.product-card a {
  text-decoration: none;
  color: #333;
  display: block;
}

.product-card-img {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-card-img img {
  transform: scale(1.05);
}

.product-card-body {
  padding: 16px;
}

.product-name {
  font-size: 16px;
  margin-bottom: 8px;
  font-weight: 600;
  height: 48px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-category {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.rating {
  font-size: 14px;
  font-weight: bold;
  color: #ff6b6b;
  min-width: 30px;
}

.rating-stars {
  display: flex;
  gap: 2px;
}

.star {
  font-size: 12px;
  color: #e5e5e5;
  transition: color 0.3s;
}

.star.active {
  color: #ff6b6b;
}

.rating-count {
  font-size: 12px;
  color: #999;
  margin-left: 4px;
}

.product-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.4;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff6b6b;
  margin-top: 8px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.pagination-btn {
  padding: 6px 12px;
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.pagination-btn:hover:not(:disabled) {
  background-color: #e9ecef;
  border-color: #adb5bd;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  padding: 6px 12px;
  font-size: 14px;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #f8f9fa;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    flex-direction: column;
    align-items: stretch;
    gap: 4px;
  }
  
  .search-group {
    width: 100%;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }
  
  .section {
    padding: 16px;
  }
  
  .product-card-img {
    height: 160px;
  }
  
  .product-card-body {
    padding: 12px;
  }
  
  .product-name {
    font-size: 14px;
    margin-bottom: 8px;
  }
  
  .product-desc {
    font-size: 12px;
    margin-bottom: 8px;
  }
  
  .product-price {
    font-size: 14px;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 4px;
  }
  
  .pagination-btn {
    font-size: 12px;
    padding: 4px 8px;
  }
  
  .pagination-info {
    font-size: 12px;
    padding: 4px 8px;
  }
}
</style>