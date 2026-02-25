import axios from '../api/axios'

class HeartbeatService {
  constructor() {
    this.interval = null
    this.heartbeatInterval = 10000 // 10秒
  }

  // 开始心跳
  start() {
    this.stop() // 先停止之前的心跳
    this.interval = setInterval(async () => {
      await this.sendHeartbeat()
    }, this.heartbeatInterval)
    console.log('心跳服务已启动，间隔:', this.heartbeatInterval, 'ms')
  }

  // 发送心跳
  async sendHeartbeat() {
    try {
      await axios.post('/heartbeat')
      console.log('心跳发送成功:', new Date().toLocaleTimeString())
    } catch (error) {
      console.error('心跳发送失败:', error)
    }
  }

  // 停止心跳
  stop() {
    if (this.interval) {
      clearInterval(this.interval)
      this.interval = null
      console.log('心跳服务已停止')
    }
  }

  // 手动发送一次心跳
  async ping() {
    await this.sendHeartbeat()
  }
}

// 导出单例
export default new HeartbeatService()
