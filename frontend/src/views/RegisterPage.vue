<template>
  <div class="login-container">
    <div class="login-form card">
      <h2>注册 CoolCommunity 账号</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">用户名</label>
          <input type="text" id="username" v-model="formData.username" class="input" placeholder="请输入用户名 (3-20个字符)" required />
        </div>
        <div class="form-group">
          <label for="nickname">昵称</label>
          <input type="text" id="nickname" v-model="formData.nickname" class="input" placeholder="请输入昵称 (2-20个字符)" required />
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input type="password" id="password" v-model="formData.password" class="input" placeholder="请输入密码 (6-20个字符)" required />
        </div>
        <div class="form-group">
          <label for="confirmPassword">确认密码</label>
          <input type="password" id="confirmPassword" v-model="formData.confirmPassword" class="input" placeholder="请确认密码" required />
          <div v-if="formData.password && formData.confirmPassword && formData.password !== formData.confirmPassword" class="error">两次输入的密码不一致</div>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="!isFormValid">注册</button>
        </div>
        <div class="form-footer">
          <p>已有账号？<router-link to="/login">立即登录</router-link></p>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formData = ref({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const isFormValid = computed(() => {
  return formData.value.username.length >= 3 &&
         formData.value.nickname.length >= 2 &&
         formData.value.password.length >= 6 &&
         formData.value.password === formData.value.confirmPassword
})

const handleRegister = async () => {
  if (formData.value.password !== formData.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  if (formData.value.username.length < 3 || formData.value.username.length > 20) {
    ElMessage.warning('用户名长度为3-20个字符')
    return
  }
  
  if (formData.value.nickname.length < 2 || formData.value.nickname.length > 20) {
    ElMessage.warning('昵称长度为2-20个字符')
    return
  }
  
  if (formData.value.password.length < 6 || formData.value.password.length > 20) {
    ElMessage.warning('密码长度为6-20个字符')
    return
  }
  
  try {
    await userStore.register({
      username: formData.value.username,
      nickname: formData.value.nickname,
      password: formData.value.password
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.response?.data?.message || '注册失败，请稍后重试')
  }
}
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
  max-width: 420px;
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

.error {
  color: #e74c3c;
  font-size: 12px;
  margin-top: 6px;
}

.form-actions {
  margin-top: 32px;
}

.login-form .form-actions button[type="submit"] {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  font-weight: 500;
  text-align: center !important;
  display: flex;
  justify-content: center;
  align-items: center;
  border: none;
  border-radius: 8px;
  background-color: #3498db;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.login-form .form-actions button[type="submit"]:hover {
  background-color: #2980b9;
  transform: translateY(-1px);
}

.login-form .form-actions button[type="submit"]:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  text-align: center !important;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #3498db;
  color: white;
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
