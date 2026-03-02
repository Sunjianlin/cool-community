<template>
  <div class="activities-page">
    <h2 class="page-title">活动中心</h2>
    
    <div class="activity-tabs">
      <button 
        v-for="tab in activityTabs" 
        :key="tab.id"
        class="activity-tab"
        :class="{ active: currentTab === tab.id }"
        @click="currentTab = tab.id"
      >
        {{ tab.name }}
      </button>
    </div>
    
    <div v-if="currentTab === 'seckill'" class="activity-section">
      <h3 class="section-title">秒杀活动</h3>
      
      <div v-if="seckillActivities.length > 0" class="seckill-list">
        <div 
          v-for="activity in seckillActivities" 
          :key="activity.id"
          class="seckill-card"
        >
          <div class="seckill-image">
            <img :src="activity.backgroundImage" :alt="activity.activityName" />
          </div>
          <div class="seckill-info">
            <h4 class="seckill-title">{{ activity.activityName }}</h4>
            <div class="seckill-time">
              <span class="time-label">开始时间：</span>
              <span class="time-value">{{ formatDate(activity.startTime) }}</span>
            </div>
            <div class="seckill-time">
              <span class="time-label">结束时间：</span>
              <span class="time-value">{{ formatDate(activity.endTime) }}</span>
            </div>
            <div class="seckill-stock">
              <span class="stock-label">库存：</span>
              <span class="stock-value">{{ activity.stock }}</span>
            </div>
            <div class="seckill-status">
              <el-tag v-if="activity.status === 1" type="success" size="small">进行中</el-tag>
              <el-tag v-else-if="activity.status === 2" type="info" size="small">已结束</el-tag>
              <el-tag v-else type="warning" size="small">未开始</el-tag>
            </div>
            <div class="seckill-actions">
              <router-link :to="`/seckill/${activity.id}`" class="btn btn-primary">
                立即参与
              </router-link>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="no-activity">
        <p>当前暂无秒杀活动</p>
      </div>
    </div>
    
    <div v-else class="activity-section">
      <h3 class="section-title">{{ getTabName(currentTab) }}</h3>
      <div class="no-activity">
        <p>功能开发中，敬请期待</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import seckillApi from '../api/seckillApi'

const currentTab = ref('seckill')
const seckillActivities = ref([])

const activityTabs = [
  { id: 'seckill', name: '秒杀活动' },
  { id: 'new', name: '新增活动' },
  { id: 'coming', name: '即将开始' }
]

const loadSeckillActivities = async () => {
  try {
    const response = await seckillApi.getSeckillList()
    seckillActivities.value = response.data || []
  } catch (error) {
    console.error('加载秒杀活动失败:', error)
    ElMessage.error('加载活动失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const getTabName = (tabId) => {
  const tab = activityTabs.find(t => t.id === tabId)
  return tab ? tab.name : ''
}

onMounted(() => {
  loadSeckillActivities()
})
</script>

<style scoped>
.activities-page {
  width: 100%;
}

.page-title {
  font-size: 24px;
  margin-bottom: 30px;
  color: #333;
  text-align: center;
}

.activity-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
  background-color: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  flex-wrap: wrap;
}

.activity-tab {
  padding: 10px 24px;
  border: 1px solid #3498db;
  border-radius: 8px;
  background-color: white;
  color: #3498db;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.activity-tab:hover {
  background-color: #f0f8ff;
}

.activity-tab.active {
  background-color: #3498db;
  color: white;
}

.activity-section {
  background-color: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  color: #333;
  border-bottom: 2px solid #3498db;
  padding-bottom: 8px;
  margin-bottom: 20px;
}

.seckill-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.seckill-card {
  display: flex;
  gap: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.seckill-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
}

.seckill-image {
  width: 200px;
  height: 150px;
  flex-shrink: 0;
}

.seckill-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.seckill-info {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.seckill-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.seckill-time {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
}

.time-label {
  color: #666;
}

.time-value {
  color: #333;
  font-weight: 500;
}

.seckill-stock {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
}

.stock-label {
  color: #666;
}

.stock-value {
  color: #e74c3c;
  font-weight: 600;
}

.seckill-status {
  margin-bottom: 16px;
}

.seckill-actions {
  align-self: flex-start;
}

.no-activity {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .activity-tabs {
    flex-direction: column;
  }
  
  .seckill-card {
    flex-direction: column;
  }
  
  .seckill-image {
    width: 100%;
    height: 200px;
  }
  
  .seckill-info {
    padding: 16px;
  }
  
  .seckill-actions {
    align-self: stretch;
  }
  
  .seckill-actions .btn {
    width: 100%;
    text-align: center;
  }
}
</style>