<!-- src/App.vue -->
<template>
  <div id="app">
    <header>
      <h1>Табулированные Функции</h1>
      <nav>
        <button v-if="!isLoggedIn" @click="showLogin = true">Войти</button>
        <button v-if="!isLoggedIn" @click="showRegister = true">Зарегистрироваться</button>
        <span v-if="isLoggedIn">Привет, {{ username }}!</span>
        <button v-if="isLoggedIn" @click="logout">Выйти</button>
      </nav>
    </header>

    <main>
      <!-- Модальное окно для входа -->
      <div v-if="showLogin" class="modal">
        <LoginForm @login-success="handleLoginSuccess" @close="showLogin = false" />
      </div>

      <!-- Модальное окно для регистрации -->
      <div v-if="showRegister" class="modal">
        <RegisterForm @register-success="handleRegisterSuccess" @close="showRegister = false" />
      </div>

      <!-- Компонент создания функции -->
      <FunctionCreator v-if="isLoggedIn" />

      <!-- Обработка ошибок -->
      <div v-if="error" class="error-modal">
        <p>{{ error }}</p>
        <button @click="clearError">Закрыть</button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import LoginForm from './components/LoginForm.vue';
import RegisterForm from './components/RegisterForm.vue';
import FunctionCreator from './components/FunctionCreator.vue';
import { api } from './api.js';

const isLoggedIn = ref(false);
const username = ref('');
const showLogin = ref(false);
const showRegister = ref(false);
const error = ref('');

const handleLoginSuccess = (userData) => {
  isLoggedIn.value = true;
  username.value = userData.username;
  showLogin.value = false;
};

const handleRegisterSuccess = () => {
  // После регистрации можно сразу попросить войти или автоматически залогинить, если сервер возвращает токен
  showRegister.value = false;
  showLogin.value = true; // Или сразу залогинить, если сервер возвращает сессию
};

const logout = () => {
  // Очистить сессию/токен на бэкенде (если нужно)
  isLoggedIn.value = false;
  username.value = '';
  // Тут может быть вызов api.logout()
};

const clearError = () => {
  error.value = '';
};

onMounted(() => {
  // Проверить, есть ли активная сессия (например, через API /api/me)
  // fetch('/api/me')
  //   .then(response => { if(response.ok) { /* восстановить сессию */ }})
  //   .catch(e => console.log("No active session"));
});
</script>

<style>
/* Добавьте свои стили */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}
.error-modal {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #f8d7da;
  color: #721c24;
  padding: 1rem;
  border: 1px solid #f5c6cb;
  border-radius: 0.3rem;
  z-index: 1000;
}
</style>