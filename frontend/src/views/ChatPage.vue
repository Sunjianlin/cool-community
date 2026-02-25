<template>
  <div class="chat-page">
    <h2 class="page-title">私信</h2>
    
    <div class="chat-container">
      <!-- 聊天列表 -->
      <div class="chat-list">
        <h3 class="section-title">最近聊天</h3>
        <div v-if="chatStore.loading.chatList" class="loading-message">
          加载中...
        </div>
        <div v-else-if="chatStore.chatList.length === 0" class="empty-message">
          还没有私信记录
        </div>
        <div v-else class="chat-item" v-for="chat in chatStore.chatList" :key="chat.id" @click="selectChat(chat)">
          <div class="chat-avatar">
            <img :src="chat.avatar" alt="用户头像" />
          </div>
          <div class="chat-info">
            <div class="chat-header">
              <div class="chat-name-container">
                <span class="chat-name">{{ chat.name }}</span>
                <OnlineStatus :status="userOnlineStatus[chat.userId] || 0" />
              </div>
              <span class="chat-time">{{ formatDate(chat.lastMessageTime) }}</span>
            </div>
            <div class="chat-preview">
              <span v-if="chat.unreadCount > 0" class="unread-badge">{{ chat.unreadCount }}</span>
              <span class="message-preview">{{ chat.lastMessage }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 聊天界面 -->
      <div v-if="chatStore.selectedChat" class="chat-content">
        <div class="chat-header">
          <div class="chat-user-info">
            <img :src="chatStore.selectedChat.avatar" alt="用户头像" class="user-avatar" />
            <div class="user-name-container">
              <span class="user-name">{{ chatStore.selectedChat.name }}</span>
              <OnlineStatus :status="userOnlineStatus[chatStore.selectedChat.userId] || 0" />
            </div>
            <button v-if="!isFollowing" class="follow-btn" @click="followUser(chatStore.selectedChat.userId)">关注</button>
          </div>
        </div>
        <div class="message-list" ref="messageList">
          <div v-if="chatStore.loading.messages" class="loading-messages">
            加载消息中...
          </div>
          <div v-else v-for="message in chatStore.getMessagesBySessionId(chatStore.selectedChat.id)" :key="message.id" :class="['message-item', message.isMine ? 'message-mine' : 'message-other']">
            <div class="message-content">
              <span class="message-text">{{ message.content }}</span>
              <span class="message-time">{{ formatTime(message.createTime) }}</span>
            </div>
          </div>
        </div>
        <div class="message-input-area">
          <input type="text" v-model="messageInput" placeholder="输入消息..." @keyup.enter="sendMessage" />
          <button class="send-btn" @click="sendMessage" :disabled="chatStore.loading.sending">发送</button>
        </div>
      </div>
      <div v-else class="chat-placeholder">
        <p>选择一个聊天开始对话</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { useChatStore } from '../store/chat'
import { ElMessage } from 'element-plus'
import OnlineStatus from '../components/OnlineStatus.vue'
import axios from '../api/axios'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const chatStore = useChatStore()
const messageInput = ref('')
const isFollowing = ref(false)
const messageList = ref(null)
// 用户在线状态映射
const userOnlineStatus = ref({})

// 初始化WebSocket连接
const initWebSocket = () => {
  if (!userStore.user?.id) return
  chatStore.initWebSocket(userStore.user.id)
}

// 获取用户在线状态
const getUserOnlineStatus = async (userId) => {
  try {
    const response = await axios.get(`/online-status/${userId}`)
    if (response.code === 200) {
      userOnlineStatus.value[userId] = response.data.status
    }
  } catch (error) {
    console.error('获取用户在线状态失败:', error)
  }
}

// 批量获取用户在线状态
const batchGetUserOnlineStatus = async () => {
  for (const chat of chatStore.chatList) {
    await getUserOnlineStatus(chat.userId)
  }
}

// 选择聊天
const selectChat = async (chat) => {
  chatStore.selectChat(chat)
  isFollowing.value = chat.isFollowing
  chatStore.loadMessages(chat.id)
  // 获取该用户的在线状态
  await getUserOnlineStatus(chat.userId)
}

// 发送消息
const sendMessage = async () => {
  if (!messageInput.value.trim() || !chatStore.selectedChat) return
  
  const content = messageInput.value.trim()
  await chatStore.sendMessage(content)
  messageInput.value = ''
  
  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
}

// 滚动到底部
const scrollToBottom = () => {
  if (messageList.value) {
    messageList.value.scrollTop = messageList.value.scrollHeight
  }
}

// 关注用户
const followUser = async (userId) => {
  if (!userStore.user?.id) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    // 实际项目中应该调用API关注用户
    // const response = await userApi.followUser(userId)
    isFollowing.value = true
    ElMessage.success('关注成功')
  } catch (error) {
    console.error('关注用户失败:', error)
    ElMessage.error('关注失败')
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    return dateStr.slice(11, 16)
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return dateStr.slice(0, 10)
  }
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.slice(11, 16)
}

// 检查URL参数，是否需要直接进入特定聊天
const checkUrlParams = async () => {
  const userId = route.query.userId
  if (userId) {
    try {
      // 这里可以在chat store中添加getOrCreateSession方法
      // 暂时使用原有逻辑
      const messageApi = await import('../api/messageApi')
      const response = await messageApi.default.getOrCreateSession(parseInt(userId))
      if (response.code === 200 && response.data) {
        chatStore.selectChat(response.data)
        isFollowing.value = response.data.isFollowing
        await chatStore.loadMessages(response.data.id)
      }
    } catch (error) {
      console.error('创建会话失败:', error)
      ElMessage.error('打开聊天失败')
    }
  }
}

onMounted(async () => {
  // 确保用户状态已初始化
  if (!userStore.isInitialized) {
    await userStore.initAuth()
  }
  
  if (userStore.isLoggedIn) {
    await chatStore.initChatState()
    initWebSocket()
    await checkUrlParams()
    // 批量获取用户在线状态
    await batchGetUserOnlineStatus()
  } else {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})

onUnmounted(() => {
  chatStore.closeWebSocket()
})
</script>

<style scoped>
.chat-page {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  margin-bottom: 30px;
  color: #333;
  text-align: center;
}

.chat-container {
  display: flex;
  gap: 24px;
  min-height: 600px;
}

/* 聊天列表 */
.chat-list {
  flex: 1;
  max-width: 350px;
  background-color: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 18px;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 2px solid #3498db;
  padding-bottom: 8px;
}

.loading-message {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 16px;
}

.empty-message {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 16px;
}

.chat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 8px;
}

.chat-item:hover {
  background-color: #f8f9fa;
}

.chat-avatar img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.chat-name-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-name {
  font-weight: 500;
  color: #333;
  font-size: 16px;
}

.chat-time {
  font-size: 12px;
  color: #999;
}

.chat-preview {
  display: flex;
  align-items: center;
  gap: 8px;
}

.unread-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  background-color: #e74c3c;
  color: white;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-preview {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

/* 聊天内容 */
.chat-content {
  flex: 2;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
}

.chat-content .chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8f9fa;
  border-radius: 12px 12px 0 0;
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  font-weight: 500;
  color: #333;
  font-size: 16px;
}

.follow-btn {
  padding: 6px 16px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.follow-btn:hover {
  background-color: #2980b9;
}

.message-list {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.loading-messages {
  text-align: center;
  color: #999;
  padding: 20px;
}

.message-item {
  display: flex;
  max-width: 70%;
}

.message-mine {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-mine .message-content {
  align-items: flex-end;
}

.message-text {
  padding: 12px 16px;
  border-radius: 18px;
  line-height: 1.4;
}

.message-mine .message-text {
  background-color: #3498db;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-other .message-text {
  background-color: #f1f1f1;
  color: #333;
  border-bottom-left-radius: 4px;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-input-area {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;
  background-color: #f8f9fa;
  border-radius: 0 0 12px 12px;
}

.message-input-area input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 24px;
  font-size: 14px;
  background-color: white;
  outline: none;
  transition: all 0.3s ease;
}

.message-input-area input:focus {
  border-color: #3498db;
}

.send-btn {
  padding: 12px 24px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.send-btn:hover:not(:disabled) {
  background-color: #2980b9;
}

.send-btn:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
}

/* 聊天占位符 */
.chat-placeholder {
  flex: 2;
  background-color: #f8f9fa;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-container {
    flex-direction: column;
  }
  
  .chat-list {
    max-width: 100%;
  }
  
  .chat-content {
    min-height: 500px;
  }
}
</style>