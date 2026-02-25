import { defineStore } from 'pinia'
import messageApi from '../api/messageApi'
import { ElMessage } from 'element-plus'
import { useUserStore } from './user'

export const useChatStore = defineStore('chat', {
  state: () => ({
    // 聊天列表
    chatList: [],
    // 当前选中的聊天
    selectedChat: null,
    // 消息列表（按会话ID存储）
    messages: {},
    // WebSocket连接状态
    wsConnected: false,
    // WebSocket实例
    ws: null,
    // 未读消息计数
    unreadCount: 0,
    // 加载状态
    loading: {
      chatList: false,
      messages: false,
      sending: false
    },
    // 错误状态
    error: null
  }),

  getters: {
    // 获取指定会话的消息
    getMessagesBySessionId: (state) => (sessionId) => {
      return state.messages[sessionId] || []
    },
    // 获取WebSocket连接状态
    isWsConnected: (state) => state.wsConnected,
    // 获取总未读消息数
    getUnreadCount: (state) => state.unreadCount
  },

  actions: {
    // 初始化聊天状态
    async initChatState() {
      this.loadFromLocalStorage()
      await this.loadChatList()
      await this.loadUnreadCount()
    },

    // 从localStorage加载状态
    loadFromLocalStorage() {
      try {
        const chatList = localStorage.getItem('chatList')
        const selectedChat = localStorage.getItem('selectedChat')
        const messages = localStorage.getItem('chatMessages')
        const unreadCount = localStorage.getItem('chatUnreadCount')

        if (chatList) {
          this.chatList = JSON.parse(chatList)
        }
        if (selectedChat) {
          this.selectedChat = JSON.parse(selectedChat)
        }
        if (messages) {
          this.messages = JSON.parse(messages)
        }
        if (unreadCount) {
          this.unreadCount = parseInt(unreadCount)
        }
      } catch (error) {
        console.error('从localStorage加载聊天状态失败:', error)
      }
    },

    // 保存状态到localStorage
    saveToLocalStorage() {
      try {
        localStorage.setItem('chatList', JSON.stringify(this.chatList))
        if (this.selectedChat) {
          localStorage.setItem('selectedChat', JSON.stringify(this.selectedChat))
        }
        localStorage.setItem('chatMessages', JSON.stringify(this.messages))
        localStorage.setItem('chatUnreadCount', this.unreadCount.toString())
      } catch (error) {
        console.error('保存聊天状态到localStorage失败:', error)
      }
    },

    // 加载聊天列表
    async loadChatList() {
      if (this.loading.chatList) return

      this.loading.chatList = true
      try {
        const response = await messageApi.getChatList()
        if (response.code === 200 && response.data) {
          this.chatList = response.data
          this.saveToLocalStorage()
        }
      } catch (error) {
        console.error('加载聊天列表失败:', error)
        ElMessage.error('加载聊天列表失败')
      } finally {
        this.loading.chatList = false
      }
    },

    // 加载会话消息
    async loadMessages(sessionId) {
      if (this.loading.messages) return

      // 检查本地缓存
      if (this.messages[sessionId] && this.messages[sessionId].length > 0) {
        return
      }

      this.loading.messages = true
      try {
        const response = await messageApi.getSessionMessages(sessionId, { page: 1, pageSize: 50 })
        if (response.code === 200 && response.data) {
          this.messages[sessionId] = response.data.records
          this.saveToLocalStorage()
        }
      } catch (error) {
        console.error('加载聊天记录失败:', error)
        ElMessage.error('加载聊天记录失败')
      } finally {
        this.loading.messages = false
      }
    },

    // 选择聊天
    selectChat(chat) {
      this.selectedChat = chat
      this.saveToLocalStorage()
    },

    // 发送消息
    async sendMessage(content) {
      if (!content.trim() || !this.selectedChat) return

      this.loading.sending = true
      try {
        const userStore = useUserStore()
        const messageId = Date.now()
        const message = {
          messageId,
          userId: userStore.user?.id,
          targetUserId: this.selectedChat.userId,
          content
        }

        // 本地添加消息
        const sessionId = this.selectedChat.id
        if (!this.messages[sessionId]) {
          this.messages[sessionId] = []
        }
        this.messages[sessionId].push({
          id: messageId,
          content,
          createTime: new Date().toISOString(),
          isMine: true,
          senderId: userStore.user?.id,
          receiverId: this.selectedChat.userId,
          status: 1
        })
        this.saveToLocalStorage()

        // 发送消息
        await messageApi.sendMessages(this.selectedChat.userId, { content })
        
        // 更新聊天列表
        await this.loadChatList()
      } catch (error) {
        console.error('发送消息失败:', error)
        ElMessage.error('发送消息失败')
      } finally {
        this.loading.sending = false
      }
    },

    // 初始化WebSocket连接
    initWebSocket(userId) {
      if (!userId) return

      // 建立WebSocket连接
      const wsUrl = `ws://localhost:8082/ws/chat?userId=${userId}`
      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        console.log('WebSocket连接已建立')
        this.wsConnected = true
      }

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          if (data.type === 'message') {
            // 处理新消息
            this.handleNewMessage(data)
          }
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      }

      this.ws.onclose = () => {
        console.log('WebSocket连接已关闭')
        this.wsConnected = false
        // 重连
        setTimeout(() => {
          console.log('尝试重新连接WebSocket...')
          this.initWebSocket(userId)
        }, 3000)
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error)
        this.wsConnected = false
      }
    },

    // 处理新消息
    handleNewMessage(data) {
      const { fromUserId, content, time, messageId } = data
      const userStore = useUserStore()
      
      // 更新消息列表
      if (this.selectedChat && this.selectedChat.userId === fromUserId) {
        const sessionId = this.selectedChat.id
        if (!this.messages[sessionId]) {
          this.messages[sessionId] = []
        }
        this.messages[sessionId].push({
          id: messageId || Date.now(),
          content,
          createTime: new Date(time).toISOString(),
          isMine: false,
          senderId: fromUserId,
          receiverId: userStore.user?.id
        })
        this.saveToLocalStorage()
      }

      // 更新聊天列表
      this.loadChatList()
      
      // 更新未读消息计数
      this.loadUnreadCount()
    },

    // 加载未读消息计数
    async loadUnreadCount() {
      try {
        const response = await messageApi.getUnreadCount()
        if (response.code === 200) {
          this.unreadCount = response.data
          this.saveToLocalStorage()
        }
      } catch (error) {
        console.error('加载未读消息数失败:', error)
      }
    },

    // 标记会话为已读
    async markSessionAsRead(sessionId) {
      try {
        await messageApi.markSessionAsRead(sessionId)
        // 更新未读消息计数
        await this.loadUnreadCount()
        // 更新聊天列表
        await this.loadChatList()
      } catch (error) {
        console.error('标记会话为已读失败:', error)
      }
    },

    // 关闭WebSocket连接
    closeWebSocket() {
      if (this.ws) {
        this.ws.close()
        this.wsConnected = false
      }
    },

    // 清除聊天状态
    clearChatState() {
      this.chatList = []
      this.selectedChat = null
      this.messages = {}
      this.unreadCount = 0
      this.closeWebSocket()
      localStorage.removeItem('chatList')
      localStorage.removeItem('selectedChat')
      localStorage.removeItem('chatMessages')
      localStorage.removeItem('chatUnreadCount')
    }
  }
})
