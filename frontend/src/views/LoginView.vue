<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-shape shape1"></div>
      <div class="bg-shape shape2"></div>
      <div class="bg-shape shape3"></div>
    </div>

    <div class="login-card">
      <!-- Logo & Title -->
      <div class="login-header">
        <el-icon class="login-logo"><DataAnalysis /></el-icon>
        <h1 class="login-title">ABMSA</h1>
        <p class="login-subtitle">方面级多模态情感分析系统</p>
      </div>

      <!-- Tab Switch -->
      <div class="login-tabs">
        <span :class="['tab-btn', { active: mode === 'login' }]" @click="mode = 'login'">登录</span>
        <span class="tab-divider">|</span>
        <span :class="['tab-btn', { active: mode === 'register' }]" @click="mode = 'register'">注册</span>
      </div>

      <!-- Login Form -->
      <el-form
        v-if="mode === 'login'"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        class="auth-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          class="submit-btn"
          :loading="loading"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>

      <!-- Register Form -->
      <el-form
        v-else
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
        class="auth-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱地址"
            size="large"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请设置密码（至少6位）"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          class="submit-btn"
          :loading="loading"
          @click="handleRegister"
        >
          注 册
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { register } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const mode = ref('login')
const loading = ref(false)
const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', email: '', password: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    await userStore.login(loginForm.username, loginForm.password)
    router.push('/analysis')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  await registerFormRef.value.validate()
  loading.value = true
  try {
    await register(registerForm.username, registerForm.password, registerForm.email)
    ElMessage.success('注册成功，请登录')
    mode.value = 'login'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
  background: #409EFF;
}

.shape1 {
  width: 500px;
  height: 500px;
  top: -150px;
  left: -150px;
}

.shape2 {
  width: 400px;
  height: 400px;
  bottom: -100px;
  right: -100px;
  background: #67C23A;
}

.shape3 {
  width: 250px;
  height: 250px;
  top: 40%;
  left: 60%;
  background: #E6A23C;
}

.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px 48px;
  width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.login-logo {
  font-size: 52px;
  color: #409EFF;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-top: 8px;
  letter-spacing: 4px;
}

.login-subtitle {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.login-tabs {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.tab-btn {
  font-size: 15px;
  color: #909399;
  cursor: pointer;
  padding-bottom: 4px;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.tab-btn.active {
  color: #409EFF;
  font-weight: 600;
  border-bottom-color: #409EFF;
}

.tab-divider {
  color: #dcdfe6;
}

.auth-form {
  display: flex;
  flex-direction: column;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
  letter-spacing: 6px;
  font-size: 16px;
  height: 46px;
}
</style>
