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

      <!-- ЦЕНТРАЛЬНОЕ МОДАЛЬНОЕ ОКНО ДЛЯ ОШИБОК -->
      <ErrorModal
        :is-open="showErrorModal"
        :message="errorMessage"
        @close="closeErrorModal"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, provide } from 'vue';
import LoginForm from './components/LoginForm.vue';
import RegisterForm from './components/RegisterForm.vue';
import FunctionCreator from './components/FunctionCreator.vue';
import ErrorModal from './components/ErrorModal.vue'; // Импортируем компонент
import { api } from './api.js';

const isLoggedIn = ref(false);
const username = ref('');
const showLogin = ref(false);
const showRegister = ref(false);

// --- НОВЫЕ СОСТОЯНИЯ ДЛЯ МОДАЛЬНОГО ОКНА ОШИБКИ ---
const showErrorModal = ref(false);
const errorMessage = ref('');
// --- КОНЕЦ НОВЫХ СОСТОЯНИЙ ---

// --- ЦЕНТРАЛИЗОВАННЫЙ ОБРАБОТЧИК ОШИБОК ---
const showError = (message) => {
  errorMessage.value = message;
  showErrorModal.value = true;
};

// Функция для закрытия модального окна ошибки
const closeErrorModal = () => {
  showErrorModal.value = false;
  errorMessage.value = '';
};
// --- КОНЕЦ ЦЕНТРАЛИЗОВАННОГО ОБРАБОТЧИКА ---

// --- ПРЕДОСТАВЛЕНИЕ ФУНКЦИИ showError КОМПОНЕНТАМ ---
// Это позволяет дочерним компонентам (и их потомкам) получить доступ к showError
provide('showError', showError);
// --- КОНЕЦ ПРЕДОСТАВЛЕНИЯ ---

const handleLoginSuccess = (userData) => {
  isLoggedIn.value = true;
  username.value = userData.username;
  showLogin.value = false;
};

const handleRegisterSuccess = () => {
  showRegister.value = false;
  showLogin.value = true;
};

const logout = () => {
  api.logout(); // Очищаем данные аутентификации в api.js
  isLoggedIn.value = false;
  username.value = '';
};

// onMounted можно оставить, если нужно проверять сессию при загрузке
// import { onMounted } from 'vue';
// onMounted(() => {
//   // Проверить, есть ли активная сессия (например, через API /api/me)
//   // fetch('/api/me') ...
// });
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
/* Стили для ErrorModal уже определены в ErrorModal.vue */
</style>