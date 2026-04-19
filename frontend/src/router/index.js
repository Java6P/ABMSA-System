import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/analysis'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/analysis',
    name: 'Analysis',
    component: () => import('@/views/AnalysisView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/batch',
    name: 'Batch',
    component: () => import('@/views/BatchView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/keyword',
    name: 'KeywordMonitor',
    component: () => import('@/views/KeywordMonitor.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/keyword/:id',
    name: 'MonitorDetail',
    component: () => import('@/views/MonitorDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/history',
    name: 'History',
    component: () => import('@/views/HistoryView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/analysis')
  } else {
    next()
  }
})

export default router
