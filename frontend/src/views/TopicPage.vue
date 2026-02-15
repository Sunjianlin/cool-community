<template>
  <div class="topic-page">
    <h2 class="page-title">话题分类</h2>
    
    <!-- 分类导航 -->
    <div class="category-nav">
      <button 
        class="category-btn" 
        v-for="category in categories" 
        :key="category.id"
        :class="{ active: currentCategory === category.id }"
        @click="switchCategory(category.id)"
      >
        {{ category.name }}
      </button>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>
    
    <!-- 分类话题列表 -->
    <div class="category-topics" v-else>
      <h3 class="category-title">{{ getCurrentCategoryName() }}</h3>
      <div class="topic-grid" v-if="currentTopics.length > 0">
        <div class="topic-card" v-for="topic in currentTopics" :key="topic.id">
          <a :href="`/topic/${topic.id}`" target="_blank">
            <div class="topic-card-header">
              <h4>{{ topic.name }}</h4>
              <span class="topic-tag">{{ topic.category || '热门' }}</span>
            </div>
            <p class="topic-desc">{{ topic.description || '暂无描述' }}</p>
            <div class="topic-stats">
              <span>{{ topic.postCount || 0 }} 帖子</span>
              <span>{{ topic.followCount || 0 }} 关注</span>
            </div>
          </a>
        </div>
      </div>
      <div v-else class="empty-state">
        暂无话题
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import topicApi from '../api/topicApi'

// 分类数据
const categories = ref([
  { id: 'all', name: '全部' },
  { id: '手机', name: '手机' },
  { id: '电脑', name: '电脑' },
  { id: '数码', name: '数码' },
  { id: '综合', name: '综合' }
])

// 当前选中的分类
const currentCategory = ref('all')

// 所有话题数据
const allTopics = ref([])

const loading = ref(true)

// 当前分类的话题
const currentTopics = computed(() => {
  if (currentCategory.value === 'all') {
    return allTopics.value
  }
  return allTopics.value.filter(t => t.category === currentCategory.value)
})

// 获取当前分类名称
const getCurrentCategoryName = () => {
  const category = categories.value.find(c => c.id === currentCategory.value)
  return category ? category.name : '全部'
}

// 切换分类
const switchCategory = (categoryId) => {
  currentCategory.value = categoryId
}

// 获取话题数据
const fetchTopics = async () => {
  loading.value = true
  try {
    const response = await topicApi.getAllTopics()
    allTopics.value = response || []
  } catch (error) {
    console.error('获取话题失败:', error)
    allTopics.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchTopics()
})
</script>

<style scoped>
.topic-page {
  width: 100%;
}

.page-title {
  font-size: 24px;
  margin-bottom: 30px;
  color: #333;
  text-align: center;
}

/* 加载状态 */
.loading {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}

/* 分类导航 */
.category-nav {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
  overflow-x: auto;
  padding-bottom: 10px;
  background-color: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.category-btn {
  padding: 8px 20px;
  border: 1px solid #3498db;
  border-radius: 20px;
  background-color: white;
  color: #3498db;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.category-btn:hover {
  background-color: #f0f8ff;
  transform: translateY(-2px);
}

.category-btn.active {
  background-color: #3498db;
  color: white;
}

/* 分类话题列表 */
.category-topics {
  background-color: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.category-title {
  font-size: 18px;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 2px solid #3498db;
  padding-bottom: 8px;
}

/* 话题网格 */
.topic-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

/* 话题卡片 */
.topic-card {
  background-color: #f9fcff;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
  border: 1px solid #e3f2fd;
}

.topic-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
  border-color: #3498db;
}

.topic-card a {
  text-decoration: none;
  color: inherit;
  display: block;
  height: 100%;
}

.topic-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.topic-card-header h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  flex: 1;
  margin-right: 12px;
}

.topic-tag {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background-color: #3498db;
  color: white;
}

.topic-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.topic-stats {
  font-size: 12px;
  color: #999;
  display: flex;
  gap: 16px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .category-nav {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .topic-grid {
    grid-template-columns: 1fr;
  }
  
  .category-topics {
    padding: 16px;
  }
}
</style>
