<template>
  <div class="login-container">
    <div class="login-form card">
      <h2>登录 CoolCommunity</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input type="text" id="username" v-model="formData.username" class="input" placeholder="请输入用户名" required />
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input type="password" id="password" v-model="formData.password" class="input" placeholder="请输入密码" required />
        </div>
        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </div>
        <div class="form-footer">
          <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formData = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!formData.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!formData.value.password) {
    ElMessage.warning('请输入密码')
    return
  }

  loading.value = true
  try {
    await userStore.login(formData.value.username, formData.value.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.response?.data?.message || '用户名或密码错误')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  formData.value = {
    username: '',
    password: ''
  }
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
  padding: 40px 20px;
}

.login-form {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.login-form h2 {
  text-align: center;
  margin-bottom: 32px;
  color: #333;
  font-size: 24px;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 15px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.input::placeholder {
  color: #aaa;
}

.form-actions {
  margin-top: 32px;
  width: 100%; /* 确保容器宽度正确 */
}

.form-actions .btn {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  font-weight: 500;
  border: none; /* 确保按钮无边框 */
  border-radius: 8px; /* 与输入框保持一致的圆角 */
  background-color: #3498db; /* 设置按钮背景色 */
  color: white; /* 设置按钮文字颜色 */
  cursor: pointer; /* 设置鼠标指针 */
  transition: all 0.3s; /* 添加过渡效果 */
  text-align: center; /* 文字水平居中 */
  line-height: 1; /* 确保文字垂直居中 */
  display: inline-block; /* 确保文字对齐生效 */
}

.form-actions .btn:hover:not(:disabled) {
  background-color: #2980b9; /*  hover 效果 */
}

.form-actions .btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-footer {
  margin-top: 24px;
  text-align: center;
  color: #666;
  font-size: 14px;
}

.form-footer a {
  color: #3498db;
  text-decoration: none;
  font-weight: 500;
}

.form-footer a:hover {
  text-decoration: underline;
}
</style>
