<template>
  <div class="login-container">
    <div class="login-form card">
      <h2>欢迎登录 CoolCommunity</h2>
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
          <button type="submit" class="btn btn-primary">登录</button>
        </div>
        <div class="form-footer">
          <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
          <p>忘记密码？<a href="#">点击找回</a></p>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../store/user';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const formData = ref({
  username: '',
  password: ''
});

const handleLogin = async () => {
  try {
    // 使用store中的登录方法
    await userStore.login(formData.value.username, formData.value.password);
    console.log('登录成功');
    ElMessage.success('登录成功');
    // 登录成功后跳转到首页
    router.push('/');
  } catch (error) {
    console.error('登录失败:', error);
    ElMessage.error('登录失败，请检查用户名和密码');
  }
};
</script>

<style scoped>
/* 登录页面样式已在style.css中定义 */
</style>