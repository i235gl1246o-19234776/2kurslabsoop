import { createRouter, createWebHistory } from 'vue-router';
import Dashboard from '../components/Dashboard.vue';
import FunctionCreator from '../components/FunctionCreator.vue';
import FunctionExplorer from '../components/FunctionExplorer.vue';
import OperationsWindow from '../components/OperationsWindow.vue';
import IntegrationWindow from '../components/IntegrationWindow.vue';
import DifferentiationWindow from '../components/DifferentiationWindow.vue';
import CompositeFunctionCreator from '../components/CompositeFunctionCreator.vue';
import SettingsModal from '../components/SettingsModal.vue';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import FunctionCreatorView from '../views/FunctionCreatorView.vue';
import SettingsView from '../views/SettingsView.vue';

const routes = [
  {
    path: '/',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { hideAuthHeader: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView,
    meta: { hideAuthHeader: true }
  },
  {
    path: '/functions/create',
    name: 'FunctionCreator',
    component: FunctionCreatorView,
    meta: { requiresAuth: true }
  },
  {
    path: '/functions/explore/:id?',
    name: 'FunctionExplorer',
    component: FunctionExplorer,
    meta: { requiresAuth: true }
  },
  {
    path: '/operations',
    name: 'Operations',
    component: OperationsWindow,
    meta: { requiresAuth: true }
  },
  {
    path: '/integration',
    name: 'Integration',
    component: IntegrationWindow,
    meta: { requiresAuth: true }
  },
  {
    path: '/differentiation',
    name: 'Differentiation',
    component: DifferentiationWindow,
    meta: { requiresAuth: true }
  },
  {
    path: '/composite',
    name: 'CompositeFunctions',
    component: CompositeFunctionCreator,
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: SettingsView,
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: SettingsModal,
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const isAuthenticated = localStorage.getItem('authCredentials');

  if (requiresAuth && !isAuthenticated) {
    next('/login');
  } else if (to.path === '/login' && isAuthenticated) {
    next('/');
  } else if (to.path === '/register' && isAuthenticated) {
    next('/');
  } else {
    next();
  }
});

export default router;