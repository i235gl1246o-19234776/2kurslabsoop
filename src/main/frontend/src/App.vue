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
      <RouterView />

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
import { useRoute, useRouter } from 'vue-router';
import ErrorModal from './components/ErrorModal.vue';
import { api } from './api.js';

const route = useRoute();
const router = useRouter();

// Состояние аутентификации
const isLoggedIn = ref(false);
const name = ref('');
const userId = ref(null);

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

// === Проверка сессии при загрузке ===
onMounted(async () => {
  if (localStorage.getItem('authCredentials')) {
    try {
      const response = await fetch('/api/users/me', {
        headers: {
          'Authorization': `Basic ${localStorage.getItem('authCredentials')}`
        }
      });

      if (response.ok) {
        const userData = await response.json();
        isLoggedIn.value = true;
        name.value = userData.name;
        userId.value = userData.id;
      } else {
        // Если ответ не 200, очищаем хранилище
        localStorage.removeItem('authCredentials');
        localStorage.removeItem('userId');
      }
    } catch (err) {
      console.error("Ошибка проверки сессии:", err);
      // При ошибке сети тоже очищаем хранилище
      localStorage.removeItem('authCredentials');
      localStorage.removeItem('userId');
    }
  }

  // Добавляем глобальный обработчик ошибок 500
  window.addEventListener('unhandledrejection', (event) => {
    if (event.reason?.message?.includes('500') ||
        event.reason?.message?.includes('HTML instead of JSON') ||
        event.reason?.message?.includes('Unexpected token')) {

      showError('Ошибка 500: Сервер не отвечает или возвращает ошибку. Проверьте консоль сервера на наличие ошибок.');
      event.preventDefault(); // Предотвращаем вывод в консоль
    }
  });
});

// === Обработчики аутентификации ===
const logout = () => {
  localStorage.removeItem('authCredentials');
  localStorage.removeItem('userId');
  isLoggedIn.value = false;
  name.value = '';
  userId.value = null;
  router.push('/login'); // Перенаправляем на страницу входа
};
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
  background-color: var(--bg-primary);
  color: var(--text-primary);
  min-height: 100vh;
}
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: var(--header-bg);
  color: var(--header-text);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}
header h1 {
  font-size: 1.5rem;
  font-weight: 600;
}
nav {
  display: flex;
  align-items: center;
  gap: 1rem;
}
button {
  cursor: pointer;
  padding: 0.6rem 1.2rem;
  border-radius: 4px;
  border: none;
  font-weight: 500;
  transition: all 0.2s ease;
}
.primary-button {
  background-color: var(--button-primary);
  color: white;
}
.primary-button:hover {
  background-color: var(--button-primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
.secondary-button {
  background-color: var(--button-secondary);
  color: white;
}
.secondary-button:hover {
  background-color: var(--button-secondary-hover);
  transform: translateY(-1px);
}
.danger-button {
  background-color: var(--button-danger);
  color: white;
}
.danger-button:hover {
  background-color: #d32f2f;
}
.success-button {
  background-color: var(--button-success);
  color: white;
}
.success-button:hover {
  background-color: #388e3c;
}
main {
  flex: 1;
  padding: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}
/* Модальное окно */
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
/* Адаптивность */
@media (max-width: 768px) {
  header {
    flex-direction: column;
    gap: 1rem;
  }
  nav {
    flex-wrap: wrap;
    justify-content: center;
  }
  main {
    padding: 1rem;
  }
}
</style>