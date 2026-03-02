<template>
  <div class="seckill-carousel">
    <el-carousel :interval="5000" type="card" height="400px">
      <el-carousel-item v-if="seckillActivity" :key="seckillActivity.id">
        <img :src="getImageUrl(seckillActivity.backgroundImage)" alt="秒杀活动" class="carousel-image" />
        <div class="carousel-overlay">
          <h3>明日秒杀活动</h3>
          <p>明天10:00限量抢购个人中心背景图</p>
          <el-button type="primary" @click="goToSeckill(seckillActivity.id)">
            立即查看
          </el-button>
        </div>
      </el-carousel-item>
      <el-carousel-item v-else>
        <div class="no-activity">
          <h3>当前暂无活动</h3>
          <p>敬请期待</p>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import seckillApi from '../api/seckillApi'

const router = useRouter()
const seckillActivity = ref(null)

const loadSeckillActivity = async () => {
  try {
    // 检查是否有次日活动
    const hasActivityResponse = await seckillApi.hasNextDayActivity()
    if (hasActivityResponse.code === 200 && hasActivityResponse.data) {
      // 获取次日活动详情
      const activityResponse = await seckillApi.getNextDayActivity()
      if (activityResponse.code === 200 && activityResponse.data) {
        seckillActivity.value = activityResponse.data
      }
    }
  } catch (error) {
    console.error('加载秒杀活动失败:', error)
  }
}

const goToSeckill = (activityId) => {
  router.push(`/seckill/${activityId}`)
}

const getImageUrl = (imagePath) => {
  if (!imagePath) return ''
  // 如果是完整URL，直接返回
  if (imagePath.startsWith('http')) {
    return imagePath
  }
  // 否则拼接完整路径
  return `http://localhost:8082${imagePath}`
}

onMounted(() => {
  loadSeckillActivity()
})
</script>

<style scoped>
.seckill-carousel {
  margin: 20px 0;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.carousel-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.7), transparent);
  color: white;
  padding: 30px;
  border-radius: 0 0 8px 8px;
}

.carousel-overlay h3 {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: bold;
}

.carousel-overlay p {
  margin: 0 0 20px 0;
  font-size: 16px;
  opacity: 0.9;
}

.no-activity {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.no-activity h3 {
  margin: 0 0 10px 0;
  color: #666;
}

.no-activity p {
  margin: 0;
  color: #999;
}
</style>
