<template>
  <div class="background-management">
    <h2>背景图管理</h2>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="backgrounds.length === 0" class="no-backgrounds">
      暂无背景图，可通过秒杀活动获取
    </div>
    <div v-else class="background-list">
      <div v-for="bg in backgrounds" :key="bg.id" class="background-item">
        <div class="background-preview">
          <img :src="getImageUrl(bg.backgroundImage)" alt="背景图" />
          <div v-if="bg.isCurrent" class="current-badge">当前使用</div>
        </div>
        <div class="background-info">
          <p class="acquire-time">获取时间: {{ formatTime(bg.acquireTime) }}</p>
          <el-button 
            :type="bg.isCurrent ? 'primary' : 'default'"
            :disabled="bg.isCurrent"
            @click="setCurrentBackground(bg.id)"
          >
            {{ bg.isCurrent ? '当前使用' : '设为当前' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import backgroundApi from '../api/backgroundApi'

const backgrounds = ref([])
const loading = ref(true)

const loadBackgrounds = async () => {
  try {
    const response = await backgroundApi.getBackgroundList()
    if (response.code === 200) {
      backgrounds.value = response.data
    }
  } catch (error) {
    console.error('加载背景图失败:', error)
    ElMessage.error('加载背景图失败')
  } finally {
    loading.value = false
  }
}

const setCurrentBackground = async (backgroundId) => {
  try {
    const response = await backgroundApi.setCurrentBackground(backgroundId)
    if (response.code === 200) {
      ElMessage.success('背景图设置成功')
      // 重新加载背景图列表
      loadBackgrounds()
    }
  } catch (error) {
    console.error('设置背景图失败:', error)
    ElMessage.error('设置背景图失败')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const getImageUrl = (imagePath) => {
  if (!imagePath) return ''
  if (imagePath.startsWith('http')) {
    return imagePath
  }
  return `http://localhost:8082${imagePath}`
}

onMounted(() => {
  loadBackgrounds()
})
</script>

<style scoped>
.background-management {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.background-management h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.loading {
  text-align: center;
  padding: 100px 0;
  color: #666;
}

.no-backgrounds {
  text-align: center;
  padding: 100px 0;
  color: #999;
  font-size: 18px;
}

.background-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.background-item {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

.background-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
}

.background-preview {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.background-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.current-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #4CAF50;
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.background-info {
  padding: 15px;
}

.acquire-time {
  margin: 0 0 15px 0;
  color: #666;
  font-size: 14px;
}

@media (max-width: 768px) {
  .background-list {
    grid-template-columns: 1fr;
  }
}
</style>
