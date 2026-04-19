<template>
  <div id="app-root">
    <template v-if="isLoggedIn && !isLoginPage">
      <el-container class="app-container">
        <!-- Sidebar -->
        <el-aside width="220px" class="app-sidebar">
          <div class="sidebar-logo">
            <el-icon class="logo-icon"><DataAnalysis /></el-icon>
            <span class="logo-text">ABMSA</span>
          </div>
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            router
            background-color="#001529"
            text-color="#ffffffa0"
            active-text-color="#ffffff"
          >
            <el-menu-item index="/analysis">
              <el-icon><ChatDotRound /></el-icon>
              <span>情感分析</span>
            </el-menu-item>
            <el-menu-item index="/batch">
              <el-icon><Files /></el-icon>
              <span>批量分析</span>
            </el-menu-item>
            <el-menu-item index="/keyword">
              <el-icon><Monitor /></el-icon>
              <span>关键词监测</span>
            </el-menu-item>
            <el-menu-item index="/history">
              <el-icon><Clock /></el-icon>
              <span>历史记录</span>
            </el-menu-item>
            <el-menu-item index="/dashboard">
              <el-icon><TrendCharts /></el-icon>
              <span>数据大屏</span>
            </el-menu-item>
          </el-menu>
          <div class="sidebar-footer">
            <el-tag type="success" size="small">系统运行中</el-tag>
          </div>
        </el-aside>

        <!-- Main content area -->
        <el-container class="main-area">
          <el-header class="app-header">
            <div class="header-left">
              <span class="header-title">ABMSA — 方面级多模态情感分析系统</span>
            </div>
            <div class="header-right">
              <el-icon style="margin-right:6px;color:#409EFF"><UserFilled /></el-icon>
              <span class="username">{{ user?.username || '用户' }}</span>
              <el-divider direction="vertical" />
              <el-button text type="danger" @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                退出
              </el-button>
            </div>
          </el-header>
          <el-main class="app-main">
            <router-view />
          </el-main>
        </el-container>
      </el-container>
    </template>

    <!-- Login page renders without layout -->
    <template v-else-if="isLoginPage">
      <router-view />
    </template>

    <!-- Fallback for unauthenticated non-login routes -->
    <template v-else>
      <router-view />
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const isLoginPage = computed(() => route.path === '/login')
const activeMenu = computed(() => route.path)
const user = computed(() => userStore.user)

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html, body, #app-root {
  height: 100%;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.app-container {
  height: 100vh;
}

.app-sidebar {
  background-color: #001529;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
  cursor: default;
}

.logo-icon {
  font-size: 28px;
  color: #409EFF;
  margin-right: 10px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2px;
}

.sidebar-menu {
  flex: 1;
  border-right: none !important;
}

.sidebar-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  font-size: 14px;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #1890ff !important;
  border-radius: 4px;
  margin: 0 8px;
  width: calc(100% - 16px);
}

.sidebar-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(255,255,255,0.08);
}

.main-area {
  flex-direction: column;
  overflow: hidden;
}

.app-header {
  height: 56px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.username {
  font-size: 14px;
  color: #606266;
}

.app-main {
  background: #f0f2f5;
  overflow-y: auto;
  padding: 20px;
}
</style>
