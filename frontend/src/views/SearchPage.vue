<template>
  <div class="search-page">
    <div class="search-header">
      <form class="search-form" @submit.prevent="search">
        <div class="search-container">
          <span class="search-icon">🔍</span>
          <input
            v-model="keyword"
            @input="handleInput"
            placeholder="搜索帖子、用户、话题..."
            class="search-input"
            autocomplete="off"
          />
          <button type="submit" class="search-btn">搜索</button>
        </div>
      </form>

      <div class="suggestions" v-if="suggestions.length > 0 && showSuggestions">
        <div
          v-for="sug in suggestions"
          :key="sug"
          class="suggestion-item"
          @click="selectSuggestion(sug)"
        >
          🔍 {{ sug }}
        </div>
      </div>
    </div>

    <div class="hot-keywords" v-if="hotKeywords.length > 0 && !keyword">
      <h4>热门搜索</h4>
      <div class="keyword-list">
        <span
          v-for="(kw, index) in hotKeywords"
          :key="index"
          class="keyword-tag"
          @click="searchKeyword(kw)"
        >
          {{ kw }}
        </span>
      </div>
    </div>

    <div class="search-tabs" v-if="keyword && hasSearched">
      <button
        :class="['tab-btn', { active: type === 'all' }]"
        @click="switchType('all')"
      >
        综合
      </button>
      <button
        :class="['tab-btn', { active: type === 'posts' }]"
        @click="switchType('posts')"
      >
        帖子 {{ results.posts?.total || 0 }}
      </button>
      <button
        :class="['tab-btn', { active: type === 'users' }]"
        @click="switchType('users')"
      >
        用户 {{ results.users?.total || 0 }}
      </button>
      <button
        :class="['tab-btn', { active: type === 'topics' }]"
        @click="switchType('topics')"
      >
        话题 {{ results.topics?.total || 0 }}
      </button>
    </div>

    <div class="search-results" v-if="keyword && hasSearched">
      <div v-if="loading" class="loading">
        <span>搜索中...</span>
      </div>

      <template v-else>
        <div v-if="type === 'all' || type === 'posts'" class="result-section">
          <h3 v-if="type === 'all'" class="section-title">帖子</h3>
          <div v-if="results.posts?.records?.length > 0" class="post-list">
            <router-link
              v-for="post in results.posts.records"
              :key="post.id"
              :to="`/post/${post.id}`"
              class="post-card"
            >
              <div class="post-header">
                <img :src="post.userAvatar || defaultAvatar" class="user-avatar" />
                <div class="user-info">
                  <span class="username">{{ post.userNickname || post.username }}</span>
                  <span class="post-time">{{ formatDate(post.createTime) }}</span>
                </div>
              </div>
              <h4 class="post-title" v-html="post.highlightTitle || post.title"></h4>
              <p class="post-content" v-html="post.highlightContent || truncate(post.content, 150)"></p>
              <div class="post-footer">
                <span>{{ post.likeCount || 0 }} 赞</span>
                <span>{{ post.commentCount || 0 }} 评论</span>
                <span v-if="post.topicName">#{{ post.topicName }}</span>
              </div>
            </router-link>
          </div>
          <div v-else class="empty">暂无相关帖子</div>

          <div v-if="type === 'posts' && results.posts?.total > pageSize" class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="results.posts?.total || 0"
              layout="prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </div>

        <div v-if="type === 'all' || type === 'users'" class="result-section">
          <h3 v-if="type === 'all'" class="section-title">用户</h3>
          <div v-if="results.users?.records?.length > 0" class="user-list">
            <router-link
              v-for="user in results.users.records"
              :key="user.id"
              :to="`/user/${user.id}`"
              class="user-card"
            >
              <img :src="user.avatar || defaultAvatar" class="user-avatar-lg" />
              <div class="user-info-lg">
                <span class="user-nickname">{{ user.nickname }}</span>
                <span class="user-bio">{{ user.bio || '这个人很懒，什么都没写' }}</span>
                <div class="user-stats">
                  <span>{{ user.followerCount || 0 }} 粉丝</span>
                </div>
              </div>
            </router-link>
          </div>
          <div v-else class="empty">暂无相关用户</div>

          <div v-if="type === 'users' && results.users?.total > pageSize" class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="results.users?.total || 0"
              layout="prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </div>

        <div v-if="type === 'all' || type === 'topics'" class="result-section">
          <h3 v-if="type === 'all'" class="section-title">话题</h3>
          <div v-if="results.topics?.records?.length > 0" class="topic-list">
            <router-link
              v-for="topic in results.topics.records"
              :key="topic.id"
              :to="`/topic/${topic.id}`"
              class="topic-card"
            >
              <div class="topic-icon">{{ topic.icon || '#' }}</div>
              <div class="topic-info">
                <span class="topic-name">{{ topic.name }}</span>
                <span class="topic-desc">{{ topic.description || '暂无描述' }}</span>
                <div class="topic-stats">
                  <span>{{ topic.postCount || 0 }} 帖子</span>
                  <span>{{ topic.followCount || 0 }} 关注</span>
                </div>
              </div>
            </router-link>
          </div>
          <div v-else class="empty">暂无相关话题</div>

          <div v-if="type === 'topics' && results.topics?.total > pageSize" class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="results.topics?.total || 0"
              layout="prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import searchApi from '../api/searchApi'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const type = ref('all')
const loading = ref(false)
const hasSearched = ref(false)
const showSuggestions = ref(false)
const suggestions = ref([])
const hotKeywords = ref([])
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const currentPage = ref(1)
const pageSize = ref(10)

const results = ref({
  posts: { records: [], total: 0 },
  users: { records: [], total: 0 },
  topics: { records: [], total: 0 }
})

let debounceTimer = null

const search = async () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  showSuggestions.value = false
  loading.value = true
  hasSearched.value = true

  router.push({ query: { keyword: keyword.value, type: type.value, page: currentPage.value } })

  try {
    let response
    if (type.value === 'all') {
      response = await searchApi.searchAll(keyword.value)
    } else if (type.value === 'posts') {
      response = await searchApi.searchPosts(keyword.value, currentPage.value, pageSize.value)
    } else if (type.value === 'users') {
      response = await searchApi.searchUsers(keyword.value, currentPage.value, pageSize.value)
    } else if (type.value === 'topics') {
      response = await searchApi.searchTopics(keyword.value, currentPage.value, pageSize.value)
    }

    if (response.code === 200) {
      if (type.value === 'all') {
        results.value = response.data
      } else {
        results.value[type.value] = response.data
      }
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const searchKeyword = (kw) => {
  keyword.value = kw
  currentPage.value = 1
  search()
}

const switchType = (newType) => {
  type.value = newType
  currentPage.value = 1
  router.push({ query: { keyword: keyword.value, type: newType, page: 1 } })
  if (newType !== 'all') {
    search()
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  search()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleInput = () => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }

  if (keyword.value.length >= 2) {
    debounceTimer = setTimeout(async () => {
      try {
        const response = await searchApi.getSuggestions(keyword.value)
        if (response.code === 200) {
          suggestions.value = response.data
          showSuggestions.value = true
        }
      } catch (error) {
        console.error('获取建议失败:', error)
      }
    }, 300)
  } else {
    suggestions.value = []
    showSuggestions.value = false
  }
}

const selectSuggestion = (sug) => {
  keyword.value = sug
  showSuggestions.value = false
  currentPage.value = 1
  search()
}

const loadHotKeywords = async () => {
  try {
    const response = await searchApi.getHotKeywords()
    if (response.code === 200) {
      hotKeywords.value = response.data
    }
  } catch (error) {
    console.error('获取热门搜索失败:', error)
  }
}

const formatDate = (dateStr) => {
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

const truncate = (text, length) => {
  if (!text) return ''
  const cleanText = text.replace(/<[^>]+>/g, '')
  return cleanText.length > length ? cleanText.substring(0, length) + '...' : cleanText
}

onMounted(() => {
  loadHotKeywords()

  if (route.query.keyword) {
    keyword.value = route.query.keyword
    if (route.query.type) {
      type.value = route.query.type
    }
    if (route.query.page) {
      currentPage.value = parseInt(route.query.page)
    }
    search()
  }
})

watch(() => route.query, (newQuery) => {
  if (newQuery.keyword && newQuery.keyword !== keyword.value) {
    keyword.value = newQuery.keyword
    type.value = newQuery.type || 'all'
    currentPage.value = parseInt(newQuery.page) || 1
    search()
  }
}, { immediate: false })
</script>

<style scoped>
.search-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px 20px;
}

.search-header {
  padding: 20px 0;
  background: transparent;
  position: relative;
}

.search-form {
  width: 100%;
}

.search-container {
  position: relative;
  display: flex;
  align-items: center;
  background: white;
  border-radius: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.search-icon {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  color: #999;
  pointer-events: none;
  z-index: 1;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 16px;
  padding: 14px 100px 14px 46px;
  min-width: 0;
  line-height: 1.5;
  background: transparent;
}

.search-input::placeholder {
  color: #999;
}

.search-btn {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s;
  z-index: 1;
}

.search-btn:hover {
  background: linear-gradient(135deg, #2980b9, #1a5276);
}

.suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  margin-top: 8px;
  z-index: 100;
  overflow: hidden;
}

.suggestion-item {
  padding: 12px 20px;
  cursor: pointer;
  transition: background 0.2s;
}

.suggestion-item:hover {
  background: #f5f7fa;
}

.hot-keywords {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.hot-keywords h4 {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #666;
}

.keyword-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.keyword-tag {
  padding: 6px 16px;
  background: #f0f2f5;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.keyword-tag:hover {
  background: #e8f4fc;
  color: #3498db;
}

.search-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.tab-btn {
  padding: 8px 20px;
  border: none;
  background: transparent;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.2s;
}

.tab-btn:hover {
  background: #f5f7fa;
}

.tab-btn.active {
  background: #3498db;
  color: white;
}

.search-results {
  min-height: 300px;
}

.loading {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.result-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #3498db;
}

.empty {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 20px 0;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.post-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #333;
}

.post-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 12px 0;
}

.post-footer {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

.user-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 15px;
}

.user-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s;
}

.user-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.user-avatar-lg {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info-lg {
  flex: 1;
}

.user-nickname {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 4px;
}

.user-bio {
  font-size: 13px;
  color: #666;
  display: block;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-stats {
  font-size: 12px;
  color: #999;
}

.topic-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.topic-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s;
}

.topic-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.topic-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.topic-info {
  flex: 1;
}

.topic-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 4px;
}

.topic-desc {
  font-size: 13px;
  color: #666;
  display: block;
  margin-bottom: 8px;
}

.topic-stats {
  font-size: 12px;
  color: #999;
  display: flex;
  gap: 15px;
}

:deep(.highlight) {
  color: #e74c3c;
  font-style: normal;
  font-weight: 600;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .search-header {
    padding: 16px;
  }
  
  .search-input {
    padding: 12px 90px 12px 42px;
    font-size: 15px;
  }
  
  .search-icon {
    left: 14px;
  }
  
  .search-btn {
    padding: 8px 18px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .search-header {
    padding: 12px;
  }
  
  .search-input {
    padding: 10px 80px 10px 38px;
    font-size: 14px;
  }
  
  .search-input::placeholder {
    font-size: 13px;
  }
  
  .search-icon {
    left: 12px;
    font-size: 14px;
  }
  
  .search-btn {
    padding: 8px 14px;
    font-size: 12px;
  }
}
</style>
