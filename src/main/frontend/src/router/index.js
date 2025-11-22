// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import FunctionCreatorView from '../views/FunctionCreatorView.vue';
import SettingsView from '../views/SettingsView.vue';

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomeView,
  },
  {
    path: '/function-creator',
    name: 'FunctionCreator',
    component: FunctionCreatorView,
  },
  {
    path: '/settings',
    name: 'Settings',
    component: SettingsView,
  },
  // Добавьте другие маршруты
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;