<!-- src/App.vue -->
<template>
  <div id="app">
    <header v-if="!$route.meta?.hideAuthHeader">
      <h1>Табулированные Функции</h1>
      <nav>
        <button v-if="!isLoggedIn" @click="showLogin = true">Войти</button>
        <button v-if="!isLoggedIn" @click="showRegister = true">Зарегистрироваться</button>
        <span v-if="isLoggedIn">Привет, {{ username }}!</span>
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
const username = ref('');
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
  username: computed(() => username.value),
  userId: computed(() => userId.value)
});

// === Обработчики аутентификации ===
const handleLoginSuccess = (userData) => {
  isLoggedIn.value = true;
  username.value = userData.username;
  userId.value = userData.id;
  showLogin.value = false;
};

const handleRegisterSuccess = () => {
  showRegister.value = false;
  showLogin.value = true;
};

const logout = () => {
  api.logout();
  isLoggedIn.value = false;
  username.value = '';
  userId.value = null;
};

// === Проверка сессии при загрузке ===
onMounted(async () => {
  if (api.isAuthenticated()) {
    try {
      const response = await fetch('/api/users/me', {
        headers: {
          'Authorization': `Basic ${api.getStoredCredentials()}`
        }
      });

      if (response.ok) {
        const userData = await response.json();
        handleLoginSuccess(userData);
      } else {
        api.logout();
      }
    } catch (err) {
      api.logout();
    }
  }
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
</style>