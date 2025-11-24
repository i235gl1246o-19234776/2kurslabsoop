import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue')
  },
  {
    path: '/functions',
    name: 'Functions',
    component: () => import('@/views/FunctionsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/functions/create',
    name: 'CreateFunction',
    component: () => import('@/views/CreateFunctionView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/functions/create-from-math',
    name: 'CreateFromMath',
    component: () => import('@/views/CreateFromMathView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/operations',
    name: 'Operations',
    component: () => import('@/components/OperationsWithFunctions.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/integration',
    name: 'Integration',
    component: () => import('@/components/IntegralCalculator.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/functions/composite',
    name: 'CompositeFunction',
    component: () => import('@/components/CompositeFunctionCreator.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/graphs',
    name: 'Graphs',
    component: () => import('@/views/GraphView.vue')
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/SettingsView.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router