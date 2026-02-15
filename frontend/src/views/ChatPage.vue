<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- 好友列表 -->
      <div class="friends-list">
        <div class="friends-header">
          <h3>私信</h3>
          <input type="text" class="input search-input" placeholder="搜索好友" />
        </div>
        <div class="friend-items">
          <div 
            class="friend-item" 
            v-for="friend in friends" 
            :key="friend.id"
            :class="{ active: selectedFriendId === friend.id }"
            @click="selectFriend(friend.id)"
          >
            <div class="friend-avatar">
              <img :src="friend.avatar" alt="好友头像" />
              <span class="unread-badge" v-if="friend.unreadCount > 0">{{ friend.unreadCount }}</span>
            </div>
            <div class="friend-info">
              <div class="friend-name">{{ friend.name }}</div>
              <div class="last-message" v-if="friend.lastMessage">{{ friend.lastMessage }}</div>
              <div class="last-message-time" v-if="friend.lastMessageTime">{{ friend.lastMessageTime }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 聊天区域 -->
      <div class="chat-area" v-if="selectedFriend">
        <div class="chat-header">
          <div class="friend-info">
            <img :src="selectedFriend.avatar" alt="好友头像" class="friend-avatar" />
            <span class="friend-name">{{ selectedFriend.name }}</span>
          </div>
          <div class="chat-actions">
            <button class="btn btn-secondary">查看资料</button>
          </div>
        </div>
        
        <div class="messages-container">
          <div class="message-item" v-for="message in messages" :key="message.id" :class="{ sent: message.isSent }">
            <div class="message-content">
              <p>{{ message.content }}</p>
              <span class="message-time">{{ message.time }}</span>
            </div>
          </div>
        </div>
        
        <div class="message-input-container">
          <input type="text" class="input message-input" placeholder="输入消息..." v-model="newMessage" @keyup.enter="sendMessage" />
          <button class="btn btn-primary" @click="sendMessage">发送</button>
        </div>
      </div>
      
      <!-- 未选择好友时的提示 -->
      <div class="no-selection" v-else>
        <p>请选择一个好友开始聊天</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import messageApi from '../api/messageApi'

// 数据
const friends = ref([])
const selectedFriendId = ref(null)
const newMessage = ref('')
const messages = ref([])
const loading = ref(false)

// 模拟数据（当API调用失败时使用）
const mockFriends = [
  {
    id: 1,
    name: '科技达人',
    avatar: 'https://via.placeholder.com/40',
    lastMessage: 'iPhone 16 Pro Max真的很不错',
    lastMessageTime: '10:00',
    unreadCount: 2
  },
  {
    id: 2,
    name: '数码评测师',
    avatar: 'https://via.placeholder.com/40',
    lastMessage: '小米15 Ultra的相机表现如何？',
    lastMessageTime: '昨天',
    unreadCount: 0
  },
  {
    id: 3,
    name: '游戏爱好者',
    avatar: 'https://via.placeholder.com/40',
    lastMessage: '最近有什么好玩的游戏推荐吗？',
    lastMessageTime: '2天前',
    unreadCount: 1
  }
]

const mockMessages = [
  {
    id: 1,
    content: '你好，最近怎么样？',
    time: '09:00',
    isSent: false
  },
  {
    id: 2,
    content: '挺好的，刚入手了iPhone 16 Pro Max',
    time: '09:30',
    isSent: true
  },
  {
    id: 3,
    content: '哦，怎么样？好用吗？',
    time: '09:45',
    isSent: false
  },
  {
    id: 4,
    content: 'iPhone 16 Pro Max真的很不错',
    time: '10:00',
    isSent: true
  }
]

const selectedFriend = computed(() => {
  return friends.value.find(friend => friend.id === selectedFriendId.value) || null
})

// 获取好友列表（模拟数据，实际项目中需要从API获取）
const fetchFriends = async () => {
  try {
    // 实际项目中这里应该调用API获取好友列表
    // 暂时使用模拟数据
    friends.value = mockFriends
  } catch (error) {
    console.error('Failed to fetch friends:', error)
    friends.value = mockFriends
  }
}

// 获取消息记录
const fetchMessages = async (senderId, receiverId) => {
  try {
    const response = await messageApi.getMessagesBetweenUsers(senderId, receiverId)
    if (response && response.length > 0) {
      // 转换消息格式
      messages.value = response.map(msg => ({
        id: msg.id,
        content: msg.content,
        time: new Date(msg.createdAt).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
        isSent: msg.sender.id === 1 // 假设当前用户ID为1
      }))
    } else {
      messages.value = mockMessages
    }
  } catch (error) {
    console.error('Failed to fetch messages:', error)
    messages.value = mockMessages
  }
}

// 选择好友
const selectFriend = async (friendId) => {
  selectedFriendId.value = friendId
  // 清空未读消息数
  const friend = friends.value.find(f => f.id === friendId)
  if (friend) {
    friend.unreadCount = 0
  }
  // 获取消息记录
  await fetchMessages(1, friendId) // 假设当前用户ID为1
}

// 发送消息
const sendMessage = async () => {
  if (newMessage.value.trim() && selectedFriendId.value) {
    try {
      const messageData = {
        content: newMessage.value.trim(),
        sender: {
          id: 1 // 假设当前用户ID为1
        },
        receiver: {
          id: selectedFriendId.value
        }
      }
      const response = await messageApi.sendMessage(messageData)
      if (response) {
        // 使用返回的消息数据
        const newMessageItem = {
          id: response.id,
          content: response.content,
          time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
          isSent: true
        }
        messages.value.push(newMessageItem)
        
        // 更新好友列表中的最后消息
        const friend = friends.value.find(f => f.id === selectedFriendId.value)
        if (friend) {
          friend.lastMessage = newMessage.value.trim()
          friend.lastMessageTime = '刚刚'
        }
        
        newMessage.value = ''
      }
    } catch (error) {
      console.error('Failed to send message:', error)
      // 失败时使用本地数据
      const message = {
        id: messages.value.length + 1,
        content: newMessage.value.trim(),
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
        isSent: true
      }
      messages.value.push(message)
      
      // 更新好友列表中的最后消息
      const friend = friends.value.find(f => f.id === selectedFriendId.value)
      if (friend) {
        friend.lastMessage = newMessage.value.trim()
        friend.lastMessageTime = '刚刚'
      }
      
      newMessage.value = ''
    }
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await fetchFriends()
    // 默认选择第一个好友
    if (friends.value.length > 0) {
      await selectFriend(friends.value[0].id)
    }
  } catch (error) {
    console.error('Failed to initialize chat:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.chat-page {
  width: 100%;
  height: 80vh;
}

.chat-container {
  display: flex;
  width: 100%;
  height: 100%;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 好友列表 */
.friends-list {
  width: 300px;
  border-right: 1px solid #e5e5e5;
  display: flex;
  flex-direction: column;
}

.friends-header {
  padding: 20px;
  border-bottom: 1px solid #e5e5e5;
}

.friends-header h3 {
  margin-bottom: 16px;
  color: #333;
}

.search-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #e5e5e5;
  border-radius: 20px;
  font-size: 14px;
}

.friend-items {
  flex: 1;
  overflow-y: auto;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  border-bottom: 1px solid #f0f0f0;
}

.friend-item:hover {
  background-color: #f5f5f5;
}

.friend-item.active {
  background-color: #f0f0f0;
}

.friend-avatar {
  position: relative;
  margin-right: 12px;
}

.friend-avatar img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background-color: #ff6b6b;
  color: white;
  font-size: 12px;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.friend-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.last-message {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.last-message-time {
  font-size: 11px;
  color: #ccc;
  text-align: right;
}

/* 聊天区域 */
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
  background-color: #f9f9f9;
}

.chat-header .friend-info {
  display: flex;
  align-items: center;
}

.chat-header .friend-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 12px;
}

.chat-header .friend-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.chat-actions {
  display: flex;
  gap: 8px;
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
  background-color: #ff6b6b;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-content p {
  margin-bottom: 4px;
  line-height: 1.4;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
  text-align: right;
}

.message-input-container {
  display: flex;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid #e5e5e5;
  background-color: #fff;
}

.message-input {
  flex: 1;
  padding: 12px;
  border: 1px solid #e5e5e5;
  border-radius: 20px;
  font-size: 14px;
}

/* 未选择好友时的提示 */
.no-selection {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f9f9f9;
  color: #999;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-container {
    flex-direction: column;
  }
  
  .friends-list {
    width: 100%;
    height: 30%;
    border-right: none;
    border-bottom: 1px solid #e5e5e5;
  }
  
  .chat-area {
    height: 70%;
  }
  
  .message-item {
    max-width: 85%;
  }
}
</style>
