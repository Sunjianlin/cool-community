<template>
  <div class="seckill-page">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="!activity" class="no-activity">活动不存在</div>
    <div v-else class="seckill-content">
      <h2>{{ activity.activityName }}</h2>
      <div class="seckill-info">
        <div class="seckill-image">
          <img :src="getImageUrl(activity.backgroundImage)" alt="背景图" />
        </div>
        <div class="seckill-details">
          <p class="detail-item">
            <span class="label">开始时间:</span>
            <span class="value">{{ formatTime(activity.startTime) }}</span>
          </p>
          <p class="detail-item">
            <span class="label">结束时间:</span>
            <span class="value">{{ formatTime(activity.endTime) }}</span>
          </p>
          <p class="detail-item">
            <span class="label">库存:</span>
            <span class="value">{{ activity.stock }}</span>
          </p>
          <p class="detail-item">
            <span class="label">状态:</span>
            <span class="value status-{{ activity.status }}">{{ getStatusText(activity.status) }}</span>
          </p>
          <el-button 
            type="primary" 
            size="large" 
            :disabled="!canSeckill || isSeckilling"
            @click="doSeckill"
            class="seckill-btn"
          >
            {{ isSeckilling ? '抢购中...' : (canSeckill ? '立即抢购' : '活动未开始') }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import seckillApi from '../api/seckillApi'

const route = useRoute()
const router = useRouter()
const activityId = computed(() => route.params.id)
const activity = ref(null)
const loading = ref(true)
const isSeckilling = ref(false)

const canSeckill = computed(() => {
  if (!activity.value) return false
  const now = new Date().getTime()
  const startTime = new Date(activity.value.startTime).getTime()
  return now >= startTime && activity.value.status === 1
})

const loadActivity = async () => {
  try {
    const response = await seckillApi.getActivityDetail(activityId.value)
    if (response.code === 200) {
      activity.value = response.data
    }
  } catch (error) {
    console.error('加载活动详情失败:', error)
    ElMessage.error('加载活动详情失败')
  } finally {
    loading.value = false
  }
}

const doSeckill = async () => {
  if (!canSeckill.value) return
  
  isSeckilling.value = true
  try {
    const response = await seckillApi.doSeckill(activityId.value)
    if (response.code === 200 && response.data) {
      ElMessage.success('抢购成功！')
      // 跳转到个人中心背景图设置页面
      router.push('/profile/background')
    } else {
      ElMessage.error('抢购失败，请重试')
    }
  } catch (error) {
    console.error('抢购失败:', error)
    ElMessage.error('抢购失败，请重试')
  } finally {
    isSeckilling.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const getStatusText = (status) => {
  switch (status) {
    case 0: return '未开始'
    case 1: return '进行中'
    case 2: return '已结束'
    case 3: return '已下架'
    default: return '未知'
  }
}

const getImageUrl = (imagePath) => {
  if (!imagePath) return ''
  if (imagePath.startsWith('http')) {
    return imagePath
  }
  return `http://localhost:8082${imagePath}`
}

onMounted(() => {
  loadActivity()
})
</script>

<style scoped>
.seckill-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.loading {
  text-align: center;
  padding: 100px 0;
  color: #666;
}

.no-activity {
  text-align: center;
  padding: 100px 0;
  color: #999;
  font-size: 18px;
}

.seckill-content h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 28px;
}

.seckill-info {
  display: flex;
  gap: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  padding: 30px;
}

.seckill-image {
  flex: 1;
  max-width: 400px;
}

.seckill-image img {
  width: 100%;
  height: auto;
  border-radius: 8px;
  object-fit: cover;
}

.seckill-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 16px;
}

.detail-item .label {
  font-weight: 500;
  color: #666;
  min-width: 80px;
}

.detail-item .value {
  color: #333;
  font-size: 18px;
}

.status-0 {
  color: #999;
}

.status-1 {
  color: #4CAF50;
  font-weight: 500;
}

.status-2 {
  color: #999;
  text-decoration: line-through;
}

.status-3 {
  color: #F44336;
}

.seckill-btn {
  margin-top: 20px;
  padding: 15px 40px;
  font-size: 18px;
  width: 200px;
}

@media (max-width: 768px) {
  .seckill-info {
    flex-direction: column;
    align-items: center;
  }
  
  .seckill-image {
    max-width: 100%;
  }
  
  .seckill-details {
    width: 100%;
    align-items: center;
  }
  
  .detail-item {
    justify-content: center;
  }
}
</style>
