<template>
  <div class="chat-page">
    <div class="chat-container">
      <div class="conversations-list">
        <div class="conversations-header">
          <h3>私信</h3>
          <span class="unread-count" v-if="unreadCount > 0">{{ unreadCount }}</span>
        </div>
        <div class="conversation-items" v-if="conversations.length > 0">
          <div 
            class="conversation-item" 
            v-for="conv in conversations" 
            :key="conv.fromUserId"
            :class="{ active: selectedUserId === conv.fromUserId }"
            @click="selectConversation(conv)"
          >
            <div class="conv-avatar">
              <img :src="conv.fromUserAvatar || defaultAvatar" alt="头像" />
            </div>
            <div class="conv-info">
              <div class="conv-name">{{ conv.fromUserNickname || conv.fromUsername }}</div>
              <div class="conv-last-message">{{ conv.content }}</div>
            </div>
            <div class="conv-time">{{ formatTime(conv.createTime) }}</div>
          </div>
        </div>
        <div class="empty-conversations" v-else>
          <p>暂无私信</p>
        </div>
      </div>
      
      <div class="chat-area" v-if="selectedUser">
        <div class="chat-header">
          <div class="user-info">
            <img :src="selectedUser.avatar || defaultAvatar" class="user-avatar" />
            <div class="user-details">
              <span class="user-name">{{ selectedUser.nickname || selectedUser.username }}</span>
              <router-link :to="`/user/${selectedUser.id}`" class="view-profile">查看资料</router-link>
            </div>
          </div>
        </div>
        
        <div class="messages-container" ref="messagesContainer">
          <div class="message-item" 
               v-for="msg in messages" 
               :key="msg.id" 
               :class="{ sent: msg.fromUserId === currentUserId }">
            <div class="message-content">
              <p>{{ msg.content }}</p>
              <span class="message-time">{{ formatTime(msg.createTime) }}</span>
            </div>
          </div>
        </div>
        
        <div class="message-input-container">
          <input 
            type="text" 
            class="input message-input" 
            placeholder="输入消息..." 
            v-model="newMessage" 
            @keyup.enter="sendMessage" 
          />
          <button class="btn btn-primary" @click="sendMessage" :disabled="!newMessage.trim()">发送</button>
        </div>
      </div>
      
      <div class="no-selection" v-else>
        <p>选择一个会话开始聊天</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useUserStore } from '../store/user'
import messageApi from '../api/messageApi'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const conversations = ref([])
const selectedUserId = ref(null)
const selectedUser = ref(null)
const messages = ref([])
const newMessage = ref('')
const unreadCount = ref(0)
const messagesContainer = ref(null)
const currentUserId = ref(userStore.user?.id)

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const loadConversations = async () => {
  try {
    const response = await messageApi.getConversationList()
    conversations.value = response.data || []
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

const loadUnreadCount = async () => {
  try {
    const response = await messageApi.getUnreadCount()
    unreadCount.value = response.data || 0
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

const loadMessages = async (toUserId) => {
  try {
    const response = await messageApi.getConversation(toUserId, { page: 1, pageSize: 100 })
    messages.value = response.data?.records || []
    scrollToBottom()
  } catch (error) {
    console.error('加载消息失败:', error)
    messages.value = []
  }
}

const selectConversation = async (conv) => {
  selectedUserId.value = conv.fromUserId
  selectedUser.value = {
    id: conv.fromUserId,
    username: conv.fromUsername,
    nickname: conv.fromUserNickname,
    avatar: conv.fromUserAvatar
  }
  
  await loadMessages(conv.fromUserId)
  
  try {
    await messageApi.markConversationAsRead(conv.fromUserId)
    await loadUnreadCount()
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || !selectedUserId.value) return
  
  const content = newMessage.value.trim()
  
  try {
    await messageApi.sendMessage({
      toUserId: selectedUserId.value,
      content: content
    })
    
    messages.value.push({
      id: Date.now(),
      fromUserId: currentUserId.value,
      toUserId: selectedUserId.value,
      content: content,
      createTime: new Date().toISOString()
    })
    
    newMessage.value = ''
    scrollToBottom()
    
    await loadConversations()
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败')
  }
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    return
  }
  
  currentUserId.value = userStore.user?.id
  await Promise.all([loadConversations(), loadUnreadCount()])
})
</script>

<style scoped>
.chat-page {
  width: 100%;
  height: calc(100vh - 200px);
  min-height: 500px;
}

.chat-container {
  display: flex;
  width: 100%;
  height: 100%;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.conversations-list {
  width: 320px;
  border-right: 1px solid #e5e5e5;
  display: flex;
  flex-direction: column;
}

.conversations-header {
  padding: 20px;
  border-bottom: 1px solid #e5e5e5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.conversations-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.unread-count {
  background: #ff6b6b;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.conversation-items {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.conversation-item:hover {
  background-color: #f5f5f5;
}

.conversation-item.active {
  background-color: #e8f4fc;
}

.conv-avatar img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 12px;
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.conv-last-message {
  font-size: 13px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-time {
  font-size: 12px;
  color: #ccc;
  margin-left: 8px;
}

.empty-conversations {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e5e5;
  background-color: #fafafa;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 12px;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.view-profile {
  font-size: 12px;
  color: #3498db;
  margin-top: 4px;
  display: block;
  text-decoration: none;
}

.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f9f9f9;
}

.message-item {
  margin-bottom: 16px;
  display: flex;
  max-width: 70%;
}

.message-item.sent {
  justify-content: flex-end;
}

.message-content {
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
}

.message-item:not(.sent) .message-content {
  background-color: #fff;
  border-bottom-left-radius: 4px;
}

.message-item.sent .message-content {
  background-color: #3498db;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-content p {
  margin: 0 0 4px 0;
  line-height: 1.4;
  word-break: break-word;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
}

.message-input-container {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #e5e5e5;
  background-color: #fff;
}

.message-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e5e5e5;
  border-radius: 24px;
  font-size: 14px;
  outline: none;
}

.message-input:focus {
  border-color: #3498db;
}

.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.no-selection {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f9f9f9;
  color: #999;
  font-size: 16px;
}

@media (max-width: 768px) {
  .chat-container {
    flex-direction: column;
  }
  
  .conversations-list {
    width: 100%;
    height: 40%;
    border-right: none;
    border-bottom: 1px solid #e5e5e5;
  }
  
  .chat-area {
    height: 60%;
  }
}
</style>
