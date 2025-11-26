<!-- src/App.vue -->
<template>
  <div id="app">
    <header v-if="!$route.meta?.hideAuthHeader">
      <h1>Табулированные Функции</h1>
      <nav>
        <button v-if="!isLoggedIn" @click="showLogin = true">Войти</button>
        <button v-if="!isLoggedIn" @click="showRegister = true">Зарегистрироваться</button>
        <span v-if="isLoggedIn">Привет, {{ name }}!</span>
        <button v-if="isLoggedIn" @click="logout">Выйти</button>
      </nav>
    </header>
    <main>
      <div v-if="isLoggedIn">
        <Dashboard />
      </div>
      <div v-else>
        <p>Пожалуйста, войдите в систему.</p>
      </div>
      <!-- Модальное окно входа -->
      <div v-if="showLogin" class="modal">
        <LoginForm @login-success="handleLoginSuccess" @close="showLogin = false" />
      </div>
      <!-- Модальное окно регистрации -->
      <div v-if="showRegister" class="modal">
        <RegisterForm @register-success="handleRegisterSuccess" @close="showRegister = false" />
      </div>
      <!-- Центральное модальное окно ошибок -->
      <ErrorModal
        :is-open="showErrorModal"
        :message="errorMessage"
        @close="closeErrorModal"
      />
    </main>
  </div>
</template>
<script setup>
import { ref, provide, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import LoginForm from './components/LoginForm.vue';
import RegisterForm from './components/RegisterForm.vue';
import Dashboard from './components/Dashboard.vue';
import ErrorModal from './components/ErrorModal.vue';
import { api } from './api.js';

// Роутер
const route = useRoute();
// Состояние аутентификации
const isLoggedIn = ref(false);
const name = ref('');
const userId = ref(null);
// Модальные окна
const showLogin = ref(false);
const showRegister = ref(false);
// Глобальное окно ошибок
const showErrorModal = ref(false);
const errorMessage = ref('');

// === Глобальный обработчик ошибок ===
const showError = (message) => {
  errorMessage.value = message;
  showErrorModal.value = true;
};

const closeErrorModal = () => {
  showErrorModal.value = false;
  errorMessage.value = '';
};

// === Предоставление данных дочерним компонентам ===
provide('showError', showError);
provide('auth', {
  isLoggedIn: computed(() => isLoggedIn.value),
  name: computed(() => name.value),
  userId: computed(() => userId.value)
});

// === Обработчики аутентификации ===
const handleLoginSuccess = (userData) => {
  isLoggedIn.value = true;
  name.value = userData.name || userData.username || 'Пользователь';
  userId.value = userData.id || userData.userId;
  showLogin.value = false;

  // Сохраняем данные в localStorage для восстановления сессии
  localStorage.setItem('auth_user', JSON.stringify({
    id: userId.value,
    name: name.value,
    credentials: api.getStoredCredentials()
  }));
};

const handleRegisterSuccess = () => {
  showRegister.value = false;
  showLogin.value = true;
};

const logout = () => {
  api.logout();
  isLoggedIn.value = false;
  name.value = '';
  userId.value = null;
  localStorage.removeItem('auth_user');
};

// === Проверка сессии при загрузке ===
const checkSession = async () => {
  try {
    // Проверяем, есть ли сохраненные данные в localStorage
    const savedUser = localStorage.getItem('auth_user');
    if (savedUser) {
      const userData = JSON.parse(savedUser);
      api.logout(); // Очищаем текущую сессию
      isLoggedIn.value = true;
      name.value = userData.name;
      userId.value = userData.id;

      // Пытаемся восстановить сессию
      try {
        const response = await fetch('/api/users/me', {
          headers: {
            'Authorization': `Basic ${userData.credentials}`
          }
        });

        if (response.ok) {
          const userData = await response.json();
          handleLoginSuccess(userData);
        } else {
          throw new Error('Сессия недействительна');
        }
      } catch (err) {
        console.warn('Сессия недействительна:', err);
        api.logout();
        isLoggedIn.value = false;
        userId.value = null;
        localStorage.removeItem('auth_user');
      }
    }
  } catch (error) {
    console.error('Ошибка при проверке сессии:', error);
    api.logout();
    isLoggedIn.value = false;
    userId.value = null;
    localStorage.removeItem('auth_user');
  }
};

onMounted(() => {
  checkSession();
});
</script>
<style>
/* Глобальные стили */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}
body {
  font-family: Arial, sans-serif;
  background-color: #f9f9f9;
  color: #333;
}
#app {
  min-height: 100vh;
}
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: #42b983;
  color: white;
}
header h1 {
  font-size: 1.5rem;
}
nav button {
  margin-left: 1rem;
  padding: 0.4rem 0.8rem;
  background: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}
nav span {
  margin-left: 1rem;
}
main {
  padding: 1.5rem;
}
/* Модальные окна */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-content {
  background: white;
  padding: 1rem;
  border-radius: 0.5rem;
  box-shadow: 0 0.25rem 0.5rem rgba(0, 0, 0, 0.2);
  max-width: 500px;
  width: 90%;
}
/* Стили темы */
:root {
  --bg-primary: #ffffff;
  --bg-secondary: #f9f9f9;
  --bg-tertiary: #f5f5f5;
  --bg-modal: #ffffff;
  --text-primary: #333333;
  --text-secondary: #666666;
  --border-color: #ddd;
  --card-bg: #ffffff;
  --header-bg: #42b983;
  --header-text: #ffffff;
  --button-primary-bg: #2196f3;
  --button-primary-hover: #1976d2;
  --button-secondary-bg: #3498db;
  --button-secondary-hover: #2980b9;
  --error-bg: #ffebee;
  --error-border: #fcc;
  --error-text: #d32f2f;
  --warning-bg: #fff8e1;
  --warning-border: #ffc107;
  --warning-text: #ed6c02;
  --success-bg: #e8f5e8;
  --success-border: #4CAF50;
  --modal-overlay-bg: rgba(0, 0, 0, 0.5);
  --scrollbar-thumb: #c1c1c1;
  --scrollbar-track: #f1f1f1;
}
@media (prefers-color-scheme: dark) {
  :root {
    --bg-primary: #121212;
    --bg-secondary: #1e1e1e;
    --bg-tertiary: #252525;
    --bg-modal: #2d2d2d;
    --text-primary: #e0e0e0;
    --text-secondary: #aaaaaa;
    --border-color: #444;
    --card-bg: #1e1e1e;
    --header-bg: #388e3c;
    --header-text: #ffffff;
    --button-primary-bg: #1976d2;
    --button-primary-hover: #1565c0;
    --button-secondary-bg: #2980b9;
    --button-secondary-hover: #2472a4;
    --error-bg: #2c1a1a;
    --error-border: #5c1a1a;
    --error-text: #ff6b6b;
    --warning-bg: #2e281a;
    --warning-border: #b8860b;
    --warning-text: #ffcc00;
    --success-bg: #1e2c1e;
    --success-border: #4CAF50;
    --modal-overlay-bg: rgba(0, 0, 0, 0.7);
    --scrollbar-thumb: #555;
    --scrollbar-track: #333;
  }
}
</style>