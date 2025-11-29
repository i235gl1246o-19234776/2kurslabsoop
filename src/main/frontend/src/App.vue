<!-- src/App.vue -->
<template>
  <div id="app">
    <!-- Основной контент -->
    <main>
      <!-- Если вошёл — показываем Dashboard -->
      <div v-if="isLoggedIn">
        <Dashboard />
      </div>

      <!-- Если НЕ вошёл — красивый прямоугольник -->
      <div v-else class="auth-card-wrapper">
        <div class="auth-card">
          <h2>Пожалуйста, войдите в систему</h2>

          <div class="auth-buttons">
            <button @click="showLogin = true" class="auth-btn">Войти</button>
            <button @click="showRegister = true" class="auth-btn">Зарегистрироваться</button>
          </div>
        </div>
      </div>

      <!-- Модальное окно входа -->
      <div v-if="showLogin" class="modal">
        <LoginForm @login-success="handleLoginSuccess" @close="showLogin = false" />
      </div>

      <!-- Модальное окно регистрации -->
      <div v-if="showRegister" class="modal">
        <RegisterForm @register-success="handleRegisterSuccess" @close="showRegister = false" />
      </div>

      <!-- Центральное окно ошибок -->
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

const route = useRoute();

// Статус
const isLoggedIn = ref(false);
const name = ref('');
const userId = ref(null);

// Модалки
const showLogin = ref(false);
const showRegister = ref(false);

// Глобальные ошибки
const showErrorModal = ref(false);
const errorMessage = ref('');

const showError = (message) => {
  errorMessage.value = message;
  showErrorModal.value = true;
};

const closeErrorModal = () => {
  showErrorModal.value = false;
  errorMessage.value = '';
};

// Делаем доступным дочерним компонентам
provide('showError', showError);
provide('auth', {
  isLoggedIn: computed(() => isLoggedIn.value),
  name: computed(() => name.value),
  userId: computed(() => userId.value)
});

// Успешный логин
const handleLoginSuccess = (userData) => {
  isLoggedIn.value = true;
  name.value = userData.name;
  userId.value = userData.id;
  showLogin.value = false;
};

// После регистрации — сразу открываем логин
const handleRegisterSuccess = () => {
  showRegister.value = false;
  showLogin.value = true;
};

// Выход
const logout = () => {
  api.logout();
  isLoggedIn.value = false;
  name.value = '';
  userId.value = null;
};

// Проверяем авторизацию при загрузке страницы
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
/* Глобальная тёмная тема */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html, body {
  margin: 0;
  padding: 0;
  overflow-x: hidden; /* Убрать горизонтальный скролл */
}

body {
  font-family: Arial, sans-serif;
  background-color: #230942;
  color: #ffffff;
}

#app {
  min-height: 100vh;
  margin: 0;
  padding: 0;
}

/* Шапка */
header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 1rem 2rem;
  background: linear-gradient(
    135deg,
    #210b41 0%,
    #5b1fa8 40%,
    #ff4fc4 100%
  );

  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
}

header h1 {
  font-size: 1.5rem;
  font-weight: bold;
}

nav button {
  margin-left: 1rem;
  padding: 0.45rem 0.9rem;

  background: #2f105c;
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;

  font-weight: bold;
  transition: 0.25s;
}

nav button:hover {
  background: #ff4fc4;
}

/* Основной контент */
main {
  padding: 0; /* Убрали отступы */
  margin: 0;
}

/* === ЦЕНТРАЛЬНАЯ КАРТОЧКА === */
.auth-card-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh; /* Занимает всю высоту */
  margin: 0;
  padding: 0;
}

.auth-card {
  background: linear-gradient(
    160deg,
    #210b41 0%,
    #5b1fa8 45%,
    #ff4fc4 100%
  );
  padding: 2.5rem 3rem;
  border-radius: 16px;
  text-align: center;

  box-shadow:
    0 8px 20px rgba(0, 0, 0, 0.45),
    0 0 25px rgba(255, 79, 196, 0.4);

  border: 1px solid rgba(255, 255, 255, 0.15);
  animation: fadeIn 0.5s ease;
}

.auth-card h2 {
  margin-bottom: 1.8rem;
  color: #ffffff;
  font-size: 1.4rem;
  font-weight: 600;
}

/* Кнопки внутри карточки */
.auth-buttons {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.auth-btn {
  padding: 0.8rem 1.2rem;
  border-radius: 10px;
  border: none;
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  color: #fff;

  background: linear-gradient(
    135deg,
    #2f105c 0%,
    #7b1fa8 40%,
    #ff4fc4 100%
  );

  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.3);
  transition: 0.25s ease;
}

.auth-btn:hover {
  transform: translateY(-3px);

  background: linear-gradient(
    135deg,
    #3d1474 0%,
    #9e27c8 40%,
    #ff6fda 100%
  );

  box-shadow: 0 8px 22px rgba(0,0,0,0.45);
}

.auth-btn:active {
  transform: scale(0.97);
}

/* Модальное окно */
.modal {
  position: fixed;
  inset: 0;

  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(3px);

  display: flex;
  justify-content: center;
  align-items: center;

  z-index: 1000;
}

/* Анимация появления */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>