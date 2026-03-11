<template>
  <div class="message-center-page">
    <h2 class="page-title">消息中心</h2>
    
    <div class="message-container">
      <!-- 左侧导航 -->
      <div class="message-nav">
        <div class="nav-item" 
             :class="{ active: activeTab === 'comment' }"
             @click="switchTab('comment')">
          <span class="nav-icon">💬</span>
          <span class="nav-text">我的评论</span>
          <span v-if="summary.unreadComment > 0" class="nav-badge">{{ summary.unreadComment }}</span>
        </div>
        <div class="nav-item" 
             :class="{ active: activeTab === 'like' }"
             @click="switchTab('like')">
          <span class="nav-icon">❤️</span>
          <span class="nav-text">收到的赞</span>
          <span v-if="summary.unreadLike > 0" class="nav-badge">{{ summary.unreadLike }}</span>
        </div>
        <div class="nav-item" 
             :class="{ active: activeTab === 'follow' }"
             @click="switchTab('follow')">
          <span class="nav-icon">👥</span>
          <span class="nav-text">好友关注</span>
          <span v-if="summary.unreadFollow > 0" class="nav-badge">{{ summary.unreadFollow }}</span>
        </div>
        <div class="nav-item" 
             :class="{ active: activeTab === 'system' }"
             @click="switchTab('system')">
          <span class="nav-icon">🔔</span>
          <span class="nav-text">系统通知</span>
          <span v-if="summary.unreadSystem > 0" class="nav-badge">{{ summary.unreadSystem }}</span>
        </div>
        
        <div class="nav-divider"></div>
        
        <!-- 聊天记录列表 -->
        <div class="chat-section">
          <div class="chat-section-header">
            <span class="chat-section-title">聊天记录</span>
            <span v-if="summary.unreadPrivate > 0" class="chat-section-badge">{{ summary.unreadPrivate }}</span>
          </div>
          <div class="chat-section-list">
            <div v-if="chatStore.loading.chatList" class="chat-loading">加载中...</div>
            <div v-else-if="chatStore.chatList.length === 0" class="chat-empty">暂无聊天</div>
            <div v-else>
              <div class="chat-nav-item" 
                   v-for="chat in chatStore.chatList" 
                   :key="chat.id" 
                   :class="{ active: activeTab === 'chat' && selectedChat?.id === chat.id }"
                   @click="selectChat(chat)">
                <img :src="chat.avatar" class="chat-nav-avatar" />
                <div class="chat-nav-info">
                  <div class="chat-nav-header">
                    <span class="chat-nav-name">{{ chat.name }}</span>
                    <span class="chat-nav-time">{{ formatDate(chat.lastMessageTime) }}</span>
                  </div>
                  <div class="chat-nav-preview">
                    <span v-if="chat.unreadCount > 0" class="chat-nav-badge">{{ chat.unreadCount }}</span>
                    <span class="chat-nav-message">{{ chat.lastMessage }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧内容区 -->
      <div class="message-content">
        <!-- 我的评论 -->
        <div v-if="activeTab === 'comment'" class="content-panel">
          <div class="panel-header">
            <h3>我的评论</h3>
            <div class="header-actions">
              <span class="unread-count" v-if="summary.unreadComment > 0">{{ summary.unreadComment }} 条未读</span>
              <button v-if="summary.unreadComment > 0" class="btn-text" @click="markAllCommentRead">全部已读</button>
            </div>
          </div>
          <div class="notification-list-container">
            <div v-if="loading.comment" class="loading">加载中...</div>
            <div v-else-if="commentList.length === 0" class="empty">暂无评论通知</div>
            <div v-else class="notification-list">
              <div class="notification-item" 
                   v-for="item in commentList" 
                   :key="item.id"
                   :class="{ unread: item.isRead === 0 }"
                   @click="handleCommentClick(item)">
                <img :src="item.commenterAvatar || defaultAvatar" class="notification-avatar" />
                <div class="notification-content">
                  <div class="notification-header">
                    <span class="notification-title">
                      <strong>{{ item.commenterName }}</strong> 
                      <span v-if="item.type === 'COMMENT_REPLY'">回复了你在</span>
                      <span v-else>评论了你的帖子</span>
                      <span class="post-link">《{{ item.postTitle }}》</span>
                    </span>
                    <span class="notification-time">{{ formatDateTime(item.createTime) }}</span>
                  </div>
                  <p class="notification-text">{{ item.content }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 收到的赞 -->
        <div v-if="activeTab === 'like'" class="content-panel">
          <div class="panel-header">
            <h3>收到的赞</h3>
            <div class="header-actions">
              <span class="unread-count" v-if="summary.unreadLike > 0">{{ summary.unreadLike }} 条未读</span>
              <button v-if="summary.unreadLike > 0" class="btn-text" @click="markAllLikeRead">全部已读</button>
            </div>
          </div>
          <div class="notification-list-container">
            <div v-if="loading.like" class="loading">加载中...</div>
            <div v-else-if="likeList.length === 0" class="empty">暂无点赞通知</div>
            <div v-else class="notification-list">
              <div class="notification-item like-item" 
                   v-for="item in likeList" 
                   :key="item.id"
                   :class="{ unread: item.isRead === 0 }"
                   @click="handleLikeClick(item)">
                <img :src="item.likerAvatar || defaultAvatar" class="notification-avatar" />
                <div class="notification-content">
                  <div class="notification-header">
                    <span class="notification-title">
                      <strong>{{ item.likerName }}</strong> 
                      <span v-if="item.type === 'COMMENT_LIKE'">赞了你的评论</span>
                      <span v-else>赞了你的帖子</span>
                      <span class="post-link">《{{ item.postTitle }}》</span>
                    </span>
                    <span class="notification-time">{{ formatDateTime(item.createTime) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 好友关注 -->
        <div v-if="activeTab === 'follow'" class="content-panel">
          <div class="panel-header">
            <h3>好友关注</h3>
            <div class="header-actions">
              <span class="unread-count" v-if="summary.unreadFollow > 0">{{ summary.unreadFollow }} 条未读</span>
              <button v-if="summary.unreadFollow > 0" class="btn-text" @click="markAllFollowRead">全部已读</button>
            </div>
          </div>
          <div class="notification-list-container">
            <div v-if="loading.follow" class="loading">加载中...</div>
            <div v-else-if="followList.length === 0" class="empty">暂无关注通知</div>
            <div v-else class="notification-list">
              <div class="notification-item follow-item" 
                   v-for="item in followList" 
                   :key="item.id"
                   :class="{ unread: item.isRead === 0 }"
                   @click="handleFollowClick(item)">
                <img :src="item.followerAvatar || defaultAvatar" class="notification-avatar" />
                <div class="notification-content">
                  <div class="notification-header">
                    <span class="notification-title">
                      <strong>{{ item.followerName }}</strong> 关注了你
                    </span>
                    <span class="notification-time">{{ formatDateTime(item.createTime) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 系统通知 -->
        <div v-if="activeTab === 'system'" class="content-panel">
          <div class="panel-header">
            <h3>系统通知</h3>
            <div class="header-actions">
              <span class="unread-count" v-if="summary.unreadSystem > 0">{{ summary.unreadSystem }} 条未读</span>
              <button v-if="summary.unreadSystem > 0" class="btn-text" @click="markAllSystemRead">全部已读</button>
            </div>
          </div>
          <div class="notification-list-container">
            <div v-if="loading.system" class="loading">加载中...</div>
            <div v-else-if="systemList.length === 0" class="empty">暂无系统通知</div>
            <div v-else class="notification-list">
              <div class="notification-item system-item" 
                   v-for="item in systemList" 
                   :key="item.id"
                   :class="{ unread: item.isRead === 0 }"
                   @click="handleSystemClick(item)">
                <div class="system-icon">
                  <span v-if="item.type === 'ANNOUNCEMENT'">📢</span>
                  <span v-else-if="item.type === 'PROMOTION'">🎉</span>
                  <span v-else>ℹ️</span>
                </div>
                <div class="notification-content">
                  <div class="notification-header">
                    <span class="notification-title"><strong>{{ getSystemTitle(item) }}</strong></span>
                    <span class="notification-time">{{ formatDateTime(item.createTime) }}</span>
                  </div>
                  <p class="notification-text">{{ item.content }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 聊天详情 -->
        <div v-if="activeTab === 'chat'" class="chat-detail-panel">
          <div v-if="!selectedChat" class="no-chat-selected">
            <div class="no-chat-icon">💬</div>
            <p>选择一个聊天开始对话</p>
          </div>
          <template v-else>
            <div class="chat-detail-header">
              <img :src="selectedChat.avatar" class="chat-detail-avatar" />
              <div class="chat-detail-info">
                <span class="chat-detail-name">{{ selectedChat.name }}</span>
                <span class="chat-detail-status">在线</span>
              </div>
            </div>
            <div class="message-list" ref="messageListRef">
              <div v-if="chatStore.loading.messages" class="loading">加载消息中...</div>
              <div v-else class="message-group" v-for="group in groupedMessages" :key="group.date">
                <div class="message-date-divider">
                  <span class="message-date">{{ group.dateLabel }}</span>
                </div>
                <div v-for="message in group.messages" 
                     :key="message.id" 
                     :class="['message-item', message.isMine ? 'message-mine' : 'message-other']">
                  <img v-if="!message.isMine" :src="selectedChat.avatar" class="message-avatar" />
                  <div class="message-bubble">
                    <span class="message-text">{{ message.content }}</span>
                    <span class="message-time">{{ formatMessageTime(message.createTime) }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="message-input-area">
              <div class="input-wrapper">
                <input type="text" v-model="messageInput" placeholder="输入消息..." @keyup.enter="sendMessage" />
              </div>
              <button class="send-btn" @click="sendMessage" :disabled="chatStore.loading.sending || !messageInput.trim()">
                <span class="send-icon">➤</span>
                发送
              </button>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { useChatStore } from '../store/chat'
import { ElMessage } from 'element-plus'
import messageCenterApi from '../api/messageCenterApi'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const activeTab = ref('comment')
const summary = ref({
  unreadPrivate: 0,
  unreadComment: 0,
  unreadLike: 0,
  unreadFollow: 0,
  unreadSystem: 0,
  totalUnread: 0
})

const loading = ref({
  comment: false,
  like: false,
  follow: false,
  system: false
})

const commentList = ref([])
const likeList = ref([])
const followList = ref([])
const systemList = ref([])

const selectedChat = ref(null)
const messageInput = ref('')
const messageListRef = ref(null)

const groupedMessages = computed(() => {
  if (!selectedChat.value) return []
  
  const messages = chatStore.getMessagesBySessionId(selectedChat.value.id)
  if (!messages || messages.length === 0) return []
  
  const groups = {}
  const today = new Date()
  const todayStr = `${today.getFullYear()}-${(today.getMonth() + 1).toString().padStart(2, '0')}-${today.getDate().toString().padStart(2, '0')}`
  
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  const yesterdayStr = `${yesterday.getFullYear()}-${(yesterday.getMonth() + 1).toString().padStart(2, '0')}-${yesterday.getDate().toString().padStart(2, '0')}`
  
  messages.forEach(msg => {
    const date = new Date(msg.createTime)
    const dateStr = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
    
    if (!groups[dateStr]) {
      let dateLabel
      if (dateStr === todayStr) {
        dateLabel = '今天'
      } else if (dateStr === yesterdayStr) {
        dateLabel = '昨天'
      } else if (date.getFullYear() === today.getFullYear()) {
        dateLabel = `${date.getMonth() + 1}月${date.getDate()}日`
      } else {
        dateLabel = `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
      }
      groups[dateStr] = { date: dateStr, dateLabel, messages: [] }
    }
    groups[dateStr].messages.push(msg)
  })
  
  return Object.values(groups).sort((a, b) => a.date.localeCompare(b.date))
})

const switchTab = (tab) => {
  activeTab.value = tab
  selectedChat.value = null
  if (tab === 'comment' && commentList.value.length === 0) {
    loadCommentNotifications()
  } else if (tab === 'like' && likeList.value.length === 0) {
    loadLikeNotifications()
  } else if (tab === 'follow' && followList.value.length === 0) {
    loadFollowNotifications()
  } else if (tab === 'system' && systemList.value.length === 0) {
    loadSystemNotifications()
  }
}

const loadSummary = async () => {
  try {
    const response = await messageCenterApi.getNotificationSummary()
    if (response.code === 200) {
      summary.value = response.data
    }
  } catch (error) {
    console.error('加载消息汇总失败:', error)
  }
}

const loadCommentNotifications = async () => {
  if (loading.value.comment) return
  loading.value.comment = true
  try {
    const response = await messageCenterApi.getCommentNotifications()
    if (response.code === 200) {
      const replyList = response.data.replyList || []
      const postCommentList = response.data.commentList || []
      const allComments = [...replyList, ...postCommentList].sort((a, b) => 
        new Date(b.createTime) - new Date(a.createTime)
      )
      commentList.value = allComments
    }
  } catch (error) {
    console.error('加载评论通知失败:', error)
  } finally {
    loading.value.comment = false
  }
}

const loadLikeNotifications = async () => {
  if (loading.value.like) return
  loading.value.like = true
  try {
    const response = await messageCenterApi.getLikeNotifications()
    if (response.code === 200) {
      likeList.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载点赞通知失败:', error)
  } finally {
    loading.value.like = false
  }
}

const loadFollowNotifications = async () => {
  if (loading.value.follow) return
  loading.value.follow = true
  try {
    const response = await messageCenterApi.getFollowNotifications()
    if (response.code === 200) {
      followList.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载关注通知失败:', error)
  } finally {
    loading.value.follow = false
  }
}

const loadSystemNotifications = async () => {
  if (loading.value.system) return
  loading.value.system = true
  try {
    const response = await messageCenterApi.getSystemNotifications()
    if (response.code === 200) {
      systemList.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载系统通知失败:', error)
  } finally {
    loading.value.system = false
  }
}

const getSystemTitle = (item) => {
  if (item.type === 'ANNOUNCEMENT') return '系统公告'
  if (item.type === 'PROMOTION') return '活动通知'
  return '系统通知'
}

const handleCommentClick = async (item) => {
  if (item.isRead === 0) {
    try {
      if (item.type === 'COMMENT_REPLY') {
        await messageCenterApi.markCommentReplyAsRead(item.id)
      } else {
        await messageCenterApi.markPostCommentAsRead(item.id)
      }
      item.isRead = 1
      loadSummary()
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  router.push(`/post/${item.postId}`)
}

const handleLikeClick = async (item) => {
  if (item.isRead === 0) {
    try {
      await messageCenterApi.markLikeAsRead(item.id)
      item.isRead = 1
      loadSummary()
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  router.push(`/post/${item.postId}`)
}

const handleFollowClick = async (item) => {
  if (item.isRead === 0) {
    try {
      await messageCenterApi.markFollowAsRead(item.id)
      item.isRead = 1
      loadSummary()
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  router.push(`/user/${item.followerId}`)
}

const handleSystemClick = async (item) => {
  if (item.isRead === 0) {
    try {
      await messageCenterApi.markSystemAsRead(item.id)
      item.isRead = 1
      loadSummary()
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
}

const markAllCommentRead = async () => {
  try {
    await messageCenterApi.markAllCommentAsRead()
    commentList.value.forEach(item => item.isRead = 1)
    loadSummary()
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

const markAllLikeRead = async () => {
  try {
    await messageCenterApi.markAllLikeAsRead()
    likeList.value.forEach(item => item.isRead = 1)
    loadSummary()
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

const markAllFollowRead = async () => {
  try {
    await messageCenterApi.markAllFollowAsRead()
    followList.value.forEach(item => item.isRead = 1)
    loadSummary()
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

const markAllSystemRead = async () => {
  try {
    await messageCenterApi.markAllSystemAsRead()
    systemList.value.forEach(item => item.isRead = 1)
    loadSummary()
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

const selectChat = async (chat) => {
  selectedChat.value = chat
  activeTab.value = 'chat'
  await chatStore.loadMessages(chat.id)
  nextTick(() => {
    scrollToBottom()
  })
}

const sendMessage = async () => {
  if (!messageInput.value.trim() || !selectedChat.value) return
  
  const content = messageInput.value.trim()
  await chatStore.sendMessage(content)
  messageInput.value = ''
  
  nextTick(() => {
    scrollToBottom()
  })
}

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    return formatTime(dateStr)
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString()
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

const formatMessageTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  
  const isToday = date.toDateString() === now.toDateString()
  const isThisYear = date.getFullYear() === now.getFullYear()
  
  if (isToday) {
    return `${hours}:${minutes}`
  } else if (isThisYear) {
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    return `${month}-${day} ${hours}:${minutes}`
  } else {
    const year = date.getFullYear()
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  }
}

const formatDateTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

watch(() => chatStore.chatList, () => {
  if (activeTab.value === 'chat' && selectedChat.value) {
    nextTick(() => {
      scrollToBottom()
    })
  }
}, { deep: true })

onMounted(async () => {
  if (!userStore.isInitialized) {
    await userStore.initAuth()
  }
  
  if (userStore.isLoggedIn) {
    await chatStore.initChatState()
    chatStore.initWebSocket(userStore.user.id)
    await loadSummary()
    await loadCommentNotifications()
  } else {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})
</script>

<style scoped>
.message-center-page {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 28px;
  margin-bottom: 24px;
  color: var(--text-primary);
  text-align: center;
}

.message-container {
  display: flex;
  gap: 24px;
  height: calc(100vh - 200px);
  min-height: 500px;
}

/* 左侧导航 */
.message-nav {
  width: 260px;
  flex-shrink: 0;
  background: var(--card-gradient);
  border-radius: var(--border-radius);
  padding: 16px;
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: var(--transition);
  margin-bottom: 6px;
}

.nav-item:hover {
  background: rgba(52, 152, 219, 0.1);
}

.nav-item.active {
  background: var(--primary-color);
  color: white;
}

.nav-icon {
  font-size: 18px;
}

.nav-text {
  flex: 1;
  font-weight: 500;
  font-size: 14px;
}

.nav-badge {
  background: #e74c3c;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}

.nav-divider {
  height: 1px;
  background: var(--border-color);
  margin: 12px 0;
}

/* 聊天记录区域 */
.chat-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  margin-top: 8px;
}

.chat-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  margin-bottom: 8px;
}

.chat-section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.chat-section-badge {
  background: #e74c3c;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}

.chat-section-list {
  flex: 1;
  overflow-y: auto;
}

.chat-loading,
.chat-empty {
  text-align: center;
  padding: 20px;
  color: var(--text-light);
  font-size: 13px;
}

.chat-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: var(--transition);
}

.chat-nav-item:hover {
  background: rgba(52, 152, 219, 0.1);
}

.chat-nav-item.active {
  background: rgba(52, 152, 219, 0.15);
}

.chat-nav-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.chat-nav-info {
  flex: 1;
  min-width: 0;
}

.chat-nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2px;
}

.chat-nav-name {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-nav-time {
  font-size: 10px;
  color: var(--text-light);
  flex-shrink: 0;
}

.chat-nav-preview {
  display: flex;
  align-items: center;
  gap: 6px;
}

.chat-nav-badge {
  background: #e74c3c;
  color: white;
  padding: 1px 5px;
  border-radius: 8px;
  font-size: 10px;
  font-weight: 600;
  min-width: 16px;
  text-align: center;
  flex-shrink: 0;
}

.chat-nav-message {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 右侧内容区 */
.message-content {
  flex: 1;
  background: var(--card-gradient);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-md);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.content-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.panel-header h3 {
  margin: 0;
  font-size: 20px;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.unread-count {
  font-size: 14px;
  color: var(--text-secondary);
}

.btn-text {
  background: none;
  border: none;
  color: var(--primary-color);
  cursor: pointer;
  font-size: 14px;
  padding: 4px 12px;
  border-radius: 4px;
  transition: var(--transition);
}

.btn-text:hover {
  background: rgba(52, 152, 219, 0.1);
}

.notification-list-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.loading,
.empty {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notification-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: var(--transition);
  border: 1px solid transparent;
}

.notification-item:hover {
  background: rgba(52, 152, 219, 0.05);
  border-color: rgba(52, 152, 219, 0.2);
}

.notification-item.unread {
  background: rgba(52, 152, 219, 0.08);
}

.notification-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.system-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(52, 152, 219, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.notification-title {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.5;
}

.post-link {
  color: var(--primary-color);
  font-weight: 500;
}

.notification-time {
  font-size: 12px;
  color: var(--text-light);
  white-space: nowrap;
}

.notification-text {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 聊天详情面板 */
.chat-detail-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
}

.no-chat-selected {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-light);
}

.no-chat-icon {
  font-size: 80px;
  margin-bottom: 20px;
  opacity: 0.4;
}

.no-chat-selected p {
  font-size: 16px;
  opacity: 0.8;
}

.chat-detail-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 24px;
  background: white;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

.chat-detail-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(52, 152, 219, 0.2);
}

.chat-detail-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-detail-name {
  font-weight: 600;
  font-size: 17px;
  color: var(--text-primary);
}

.chat-detail-status {
  font-size: 12px;
  color: #27ae60;
  display: flex;
  align-items: center;
  gap: 4px;
}

.chat-detail-status::before {
  content: '';
  width: 6px;
  height: 6px;
  background: #27ae60;
  border-radius: 50%;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-date-divider {
  display: flex;
  justify-content: center;
  padding: 16px 0 8px;
}

.message-date {
  font-size: 12px;
  color: var(--text-light);
  background: rgba(0, 0, 0, 0.04);
  padding: 4px 14px;
  border-radius: 12px;
}

.message-item {
  display: flex;
  gap: 10px;
  max-width: 75%;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-mine {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  margin-top: auto;
}

.message-bubble {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-mine .message-bubble {
  align-items: flex-end;
}

.message-text {
  padding: 12px 16px;
  border-radius: 18px;
  line-height: 1.5;
  word-break: break-word;
  font-size: 14px;
  max-width: 400px;
}

.message-mine .message-text {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border-bottom-right-radius: 6px;
  box-shadow: 0 2px 8px rgba(52, 152, 219, 0.25);
}

.message-other .message-text {
  background: white;
  color: var(--text-primary);
  border-bottom-left-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.message-time {
  font-size: 11px;
  color: var(--text-light);
  padding: 0 4px;
}

.message-input-area {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: white;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.03);
}

.input-wrapper {
  flex: 1;
  position: relative;
}

.input-wrapper input {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid rgba(0, 0, 0, 0.08);
  border-radius: 24px;
  font-size: 14px;
  outline: none;
  transition: var(--transition);
  background: #f8fafc;
}

.input-wrapper input:focus {
  border-color: var(--primary-color);
  background: white;
  box-shadow: 0 0 0 4px rgba(52, 152, 219, 0.1);
}

.input-wrapper input::placeholder {
  color: var(--text-light);
}

.send-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 14px 24px;
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(52, 152, 219, 0.4);
}

.send-btn:active:not(:disabled) {
  transform: translateY(0);
}

.send-btn:disabled {
  background: linear-gradient(135deg, #bdc3c7, #95a5a6);
  cursor: not-allowed;
  box-shadow: none;
}

.send-icon {
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .message-container {
    flex-direction: column;
    height: auto;
  }
  
  .message-nav {
    width: 100%;
    max-height: 300px;
  }
  
  .nav-item {
    padding: 12px 14px;
  }
  
  .nav-text {
    font-size: 13px;
  }
  
  .chat-section {
    max-height: 150px;
  }
  
  .message-content {
    min-height: 400px;
  }
}
</style>
