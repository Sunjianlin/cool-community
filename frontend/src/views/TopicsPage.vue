<template>
  <div class="topics-page">
    <div class="page-header">
      <h1>话题广场</h1>
      <p class="subtitle">发现感兴趣的话题，参与讨论</p>
    </div>

    <div class="category-tabs">
      <button 
        v-for="cat in categories" 
        :key="cat.value"
        :class="['tab-btn', { active: selectedCategory === cat.value }]"
        @click="selectCategory(cat.value)"
      >
        {{ cat.label }}
      </button>
    </div>

    <div class="topics-grid">
      <a 
        v-for="topic in topics" 
        :key="topic.id" 
        :href="`/topic/${topic.id}`"
        target="_blank"
        class="topic-card"
      >
        <div class="topic-cover" :style="{ background: getRandomColor(topic.id) }">
          <span class="topic-icon">#</span>
        </div>
        <div class="topic-content">
          <h3 class="topic-name">{{ topic.name }}</h3>
          <p class="topic-desc">{{ topic.description || '暂无描述' }}</p>
          <div class="topic-meta">
            <span class="meta-item">
              <span class="meta-value">{{ topic.postCount || 0 }}</span>
              <span class="meta-label">帖子</span>
            </span>
            <span class="meta-item">
              <span class="meta-value">{{ topic.followCount || 0 }}</span>
              <span class="meta-label">关注</span>
            </span>
          </div>
          <div class="topic-category" v-if="topic.category">{{ topic.category }}</div>
        </div>
      </a>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="topics.length === 0" class="empty">暂无话题</div>

    <div v-if="hasMore" class="load-more">
      <button @click="loadMore" class="btn btn-outline">加载更多</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import topicApi from '../api/topicApi'

const topics = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 20
const total = ref(0)
const selectedCategory = ref('')

const categories = [
  { label: '全部', value: '' },
  { label: '手机', value: '手机' },
  { label: '笔记本', value: '笔记本' },
  { label: '平板电脑', value: '平板电脑' },
  { label: '耳机', value: '耳机' },
  { label: '路由器', value: '路由器' },
  { label: '机械键盘', value: '机械键盘' },
  { label: '显示器', value: '显示器' },
  { label: '相机', value: '相机' },
  { label: '其他', value: '其他' }
]

const hasMore = computed(() => topics.value.length < total.value)

const colors = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
  'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
  'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)'
]

const getRandomColor = (id) => {
  return colors[id % colors.length]
}

const loadTopics = async (reset = false) => {
  if (reset) {
    page.value = 1
    topics.value = []
  }
  
  loading.value = true
  try {
    const params = { page: page.value, pageSize }
    if (selectedCategory.value) {
      params.keyword = selectedCategory.value
    }
    
    const response = await topicApi.getTopicList(params)
    const records = response.data?.records || []
    total.value = response.data?.total || 0
    
    if (reset) {
      topics.value = records
    } else {
      topics.value.push(...records)
    }
  } catch (error) {
    console.error('加载话题失败:', error)
  } finally {
    loading.value = false
  }
}

const selectCategory = (category) => {
  selectedCategory.value = category
  loadTopics(true)
}

const loadMore = () => {
  page.value++
  loadTopics()
}

onMounted(() => {
  loadTopics()
})
</script>

<style scoped>
.topics-page {
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

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.tab-btn {
  padding: 8px 20px;
  border: none;
  background: white;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.tab-btn:hover {
  background: #f0f8ff;
  color: #3498db;
}

.tab-btn.active {
  background: #3498db;
  color: white;
}

.topics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.topic-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  text-decoration: none;
  color: inherit;
  display: block;
}

.topic-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.topic-cover {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.topic-icon {
  font-size: 48px;
  color: white;
  opacity: 0.8;
}

.topic-content {
  padding: 16px;
}

.topic-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
}

.topic-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.topic-meta {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  flex-direction: column;
}

.meta-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.meta-label {
  font-size: 12px;
  color: #999;
}

.topic-category {
  display: inline-block;
  padding: 4px 12px;
  background: #f0f8ff;
  color: #3498db;
  border-radius: 12px;
  font-size: 12px;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

.load-more {
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
</style>
