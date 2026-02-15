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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import productApi from '../api/productApi'

const route = useRoute()
const product = ref({})
const defaultImage = 'https://cube.elemecdn.com/e/fd/0/yz33e8pE6VUm0fHQyUb7Z5Th4i4.png'

const loadProduct = async () => {
  try {
    const response = await productApi.getProductDetail(route.params.id)
    product.value = response.data || {}
  } catch (error) {
    console.error('加载产品失败:', error)
  }
}

onMounted(() => {
  loadProduct()
})
</script>

<style scoped>
.product-detail-page {
  max-width: 1000px;
  margin: 0 auto;
}

.product-header {
  display: flex;
  gap: 40px;
  padding: 30px;
  margin-bottom: 25px;
  background: white;
  border-radius: 12px;
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
}

.specs p {
  color: #666;
  white-space: pre-wrap;
}

.description {
  color: #666;
  line-height: 1.6;
}
</style>
