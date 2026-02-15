<template>
  <div class="products-page">
    <div class="page-header">
      <h1>产品库</h1>
      <p class="subtitle">发现数码好物，查看真实评测</p>
    </div>

    <div class="hot-products" v-if="hotProducts.length > 0">
      <h3 class="section-title">热门产品</h3>
      <div class="hot-products-scroll">
        <a 
          v-for="product in hotProducts" 
          :key="product.id" 
          :href="`/product/${product.id}`"
          target="_blank"
          class="hot-product-card"
        >
          <img :src="product.image || defaultImage" class="product-image" />
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
    </div>

    <div class="all-products">
      <div class="filter-bar">
        <div class="category-filter">
          <select v-model="selectedCategory" @change="loadProducts(true)" class="filter-select">
            <option value="">全部分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
        </div>
        <div class="brand-filter">
          <select v-model="selectedBrand" @change="loadProducts(true)" class="filter-select">
            <option value="">全部品牌</option>
            <option v-for="brand in brands" :key="brand.id" :value="brand.name">{{ brand.name }}</option>
          </select>
        </div>
        <div class="search-filter">
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索产品..." 
            class="search-input"
            @keyup.enter="loadProducts(true)"
          />
        </div>
      </div>

      <div class="products-grid">
        <a 
          v-for="product in products" 
          :key="product.id" 
          :href="`/product/${product.id}`"
          target="_blank"
          class="product-card"
        >
          <img :src="product.image || defaultImage" class="product-image" />
          <div class="product-info">
            <h4 class="product-name">{{ product.name }}</h4>
            <p class="product-brand">{{ product.brand }}</p>
            <p class="product-category" v-if="product.categoryName">{{ product.categoryName }}</p>
            <div class="product-meta">
              <span class="product-price">{{ product.price }}</span>
              <span v-if="product.avgRating" class="product-rating">⭐ {{ product.avgRating.toFixed(1) }}</span>
            </div>
            <div class="product-stats">
              <span>{{ product.reviewCount || 0 }} 评测</span>
            </div>
          </div>
        </a>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="products.length === 0" class="empty">暂无产品</div>

      <div v-if="hasMore" class="pagination">
        <button @click="loadMore" class="btn btn-outline">加载更多</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import productApi from '../api/productApi'
import categoryApi from '../api/categoryApi'
import brandApi from '../api/brandApi'

const hotProducts = ref([])
const products = ref([])
const categories = ref([])
const brands = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 12
const total = ref(0)
const selectedCategory = ref('')
const selectedBrand = ref('')
const searchKeyword = ref('')
const defaultImage = 'https://cube.elemecdn.com/e/fd/0/yz33e8pE6VUm0fHQyUb7Z5Th4i4.png'

const hasMore = computed(() => products.value.length < total.value)

const loadCategories = async () => {
  try {
    const response = await categoryApi.getAllCategories()
    categories.value = response.data || []
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadBrands = async () => {
  try {
    const response = await brandApi.getAllBrands()
    brands.value = response.data || []
  } catch (error) {
    console.error('加载品牌失败:', error)
  }
}

const loadHotProducts = async () => {
  try {
    const response = await productApi.getProductList({ page: 1, pageSize: 6 })
    hotProducts.value = response.data?.records || []
  } catch (error) {
    console.error('加载热门产品失败:', error)
  }
}

const loadProducts = async (reset = false) => {
  if (reset) {
    page.value = 1
    products.value = []
  }
  
  loading.value = true
  try {
    const params = { page: page.value, pageSize }
    if (selectedCategory.value) params.categoryId = selectedCategory.value
    if (selectedBrand.value) params.brand = selectedBrand.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    
    const response = await productApi.getProductList(params)
    const records = response.data?.records || []
    total.value = response.data?.total || 0
    
    if (reset) {
      products.value = records
    } else {
      products.value.push(...records)
    }
  } catch (error) {
    console.error('加载产品失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  page.value++
  loadProducts()
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadBrands(), loadHotProducts()])
  loadProducts()
})
</script>

<style scoped>
.products-page {
  width: 100%;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.subtitle {
  color: #666;
  font-size: 15px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.hot-products {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.hot-products-scroll {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.hot-products-scroll::-webkit-scrollbar {
  height: 6px;
}

.hot-products-scroll::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.hot-product-card {
  flex-shrink: 0;
  width: 200px;
  background: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
  color: inherit;
  display: block;
}

.hot-product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.hot-product-card .product-image {
  width: 100%;
  height: 140px;
  object-fit: cover;
}

.hot-product-card .product-info {
  padding: 12px;
}

.all-products {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.filter-select {
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  min-width: 140px;
}

.search-input {
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  width: 200px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
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
  height: 160px;
  object-fit: cover;
}

.product-info {
  padding: 14px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-brand {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.product-category {
  font-size: 12px;
  color: #3498db;
  margin-bottom: 8px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.product-price {
  font-size: 16px;
  font-weight: 600;
  color: #e74c3c;
}

.product-rating {
  font-size: 12px;
  color: #f39c12;
}

.product-stats {
  font-size: 12px;
  color: #999;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

.pagination {
  text-align: center;
  margin-top: 24px;
}

.btn-outline {
  padding: 10px 32px;
  border: 1px solid #3498db;
  background: transparent;
  color: #3498db;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-outline:hover {
  background: #3498db;
  color: white;
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
  }
  
  .filter-select, .search-input {
    width: 100%;
  }
}
</style>
