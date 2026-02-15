<template>
  <div class="product-list-page">
    <h2 class="page-title">产品中心</h2>
    
    <!-- 热门产品 -->
    <section class="section">
      <h3 class="section-title">热门产品</h3>
      <div class="product-grid hot-products">
        <div class="product-card" v-for="product in hotProducts" :key="product.id">
          <router-link :to="`/product/${product.id}`">
            <div class="product-card-img">
              <img :src="product.image" alt="产品图片" />
              <span class="hot-badge">热门</span>
            </div>
            <div class="product-card-body">
              <h4 class="product-name">{{ product.name }}</h4>
              <div class="product-rating">
                <span class="rating">{{ product.rating }}</span>
                <div class="rating-stars">
                  <span class="star" v-for="i in 5" :key="i" :class="{ active: i <= Math.floor(product.rating) }">{{ i <= Math.floor(product.rating) ? '★' : '☆' }}</span>
                </div>
                <span class="rating-count">({{ product.ratingCount }}人)</span>
              </div>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-price" v-if="product.price">
                {{ product.price }}
              </div>
            </div>
          </router-link>
        </div>
      </div>
    </section>
    
    <!-- 全部产品 -->
    <section class="section">
      <h3 class="section-title">全部产品</h3>
      <div class="product-grid all-products">
        <div class="product-card" v-for="product in allProducts" :key="product.id">
          <router-link :to="`/product/${product.id}`">
            <div class="product-card-img">
              <img :src="product.image" alt="产品图片" />
            </div>
            <div class="product-card-body">
              <h4 class="product-name">{{ product.name }}</h4>
              <div class="product-rating">
                <span class="rating">{{ product.rating }}</span>
                <div class="rating-stars">
                  <span class="star" v-for="i in 5" :key="i" :class="{ active: i <= Math.floor(product.rating) }">{{ i <= Math.floor(product.rating) ? '★' : '☆' }}</span>
                </div>
                <span class="rating-count">({{ product.ratingCount }}人)</span>
              </div>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-price" v-if="product.price">
                {{ product.price }}
              </div>
            </div>
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import productApi from '../api/productApi'

// 数据
const hotProducts = ref([])
const allProducts = ref([])
const loading = ref(false)

// 模拟数据（当API调用失败时使用）
const mockHotProducts = [
  {
    id: 1,
    name: 'iPhone 16 Pro Max',
    image: 'https://via.placeholder.com/300',
    rating: 4.8,
    ratingCount: 1234,
    description: '苹果最新旗舰手机，搭载A18 Pro芯片',
    price: '¥9999起'
  },
  {
    id: 2,
    name: '小米15 Ultra',
    image: 'https://via.placeholder.com/300',
    rating: 4.7,
    ratingCount: 987,
    description: '小米最新旗舰手机，徕卡影像',
    price: '¥6999起'
  },
  {
    id: 3,
    name: 'MacBook Pro M3',
    image: 'https://via.placeholder.com/300',
    rating: 4.9,
    ratingCount: 765,
    description: '苹果最新笔记本电脑，M3芯片',
    price: '¥12999起'
  },
  {
    id: 4,
    name: 'AirPods Pro 3',
    image: 'https://via.placeholder.com/300',
    rating: 4.6,
    ratingCount: 876,
    description: '苹果最新降噪耳机，主动降噪',
    price: '¥1899起'
  }
]

const mockAllProducts = [
  ...mockHotProducts,
  {
    id: 5,
    name: '华为Mate 70 Pro',
    image: 'https://via.placeholder.com/300',
    rating: 4.5,
    ratingCount: 654,
    description: '华为旗舰手机，鸿蒙系统',
    price: '¥6499起'
  },
  {
    id: 6,
    name: 'ThinkPad X1 Carbon',
    image: 'https://via.placeholder.com/300',
    rating: 4.7,
    ratingCount: 543,
    description: '商务笔记本电脑，轻薄便携',
    price: '¥9999起'
  },
  {
    id: 7,
    name: '小米路由器AX9000',
    image: 'https://via.placeholder.com/300',
    rating: 4.4,
    ratingCount: 432,
    description: '小米旗舰路由器，Wi-Fi 6',
    price: '¥1299起'
  },
  {
    id: 8,
    name: '索尼WH-1000XM5',
    image: 'https://via.placeholder.com/300',
    rating: 4.8,
    ratingCount: 321,
    description: '索尼旗舰降噪耳机',
    price: '¥2999起'
  },
  {
    id: 9,
    name: '罗技G913 TKL',
    image: 'https://via.placeholder.com/300',
    rating: 4.6,
    ratingCount: 210,
    description: '罗技机械键盘，无线连接',
    price: '¥1299起'
  },
  {
    id: 10,
    name: '雷蛇炼狱蝰蛇V3',
    image: 'https://via.placeholder.com/300',
    rating: 4.5,
    ratingCount: 198,
    description: '雷蛇游戏鼠标，高精度传感器',
    price: '¥399起'
  }
]

// 获取热门产品
const fetchHotProducts = async () => {
  try {
    const response = await productApi.getHotProducts()
    hotProducts.value = response || mockHotProducts
  } catch (error) {
    console.error('Failed to fetch hot products:', error)
    hotProducts.value = mockHotProducts
  }
}

// 获取全部产品
const fetchAllProducts = async () => {
  try {
    const response = await productApi.getAllProducts()
    allProducts.value = response || mockAllProducts
  } catch (error) {
    console.error('Failed to fetch all products:', error)
    allProducts.value = mockAllProducts
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchHotProducts(),
      fetchAllProducts()
    ])
  } catch (error) {
    console.error('Failed to fetch products:', error)
  } finally {
    loading.value = false
  }
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

.section-title {
  font-size: 18px;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 2px solid #3498db;
  padding-bottom: 8px;
}

/* 产品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.hot-products {
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

.hot-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #ff6b6b;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.product-card-body {
  padding: 16px;
}

.product-name {
  font-size: 16px;
  margin-bottom: 12px;
  font-weight: 600;
  height: 48px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
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

/* 响应式设计 */
@media (max-width: 768px) {
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
}
</style>