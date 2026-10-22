<template>
  <div class="login-bg">
    <el-card class="login-card">
      <h2 style="text-align:center">虎丘婚纱城 · 库存订单系统</h2>
      <el-form :model="form" @keyup.enter="onLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="账号" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-button type="primary" style="width:100%" :loading="loading" @click="onLogin">登录</el-button>
      </el-form>
      <p style="color:#999;font-size:12px;text-align:center;margin-top:12px">
        演示账号：admin/123456（管理员）、sales01/123456（销售员）
      </p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const form = reactive({ username: '', password: '' })
const loading = ref(false)

async function onLogin() {
  if (!form.username || !form.password) return ElMessage.warning('请输入账号密码')
  loading.value = true
  try {
    const data = await login(form)
    userStore.setLogin(data)
    router.push(data.role === 1 ? '/dashboard' : '/sale-order')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-bg { height:100vh; display:flex; align-items:center; justify-content:center;
  background:linear-gradient(135deg,#667eea,#764ba2); }
.login-card { width:400px; padding:20px; }
</style>