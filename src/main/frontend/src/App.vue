<!-- src/App.vue -->
<template>
  <div id="app" :class="{ dark: isDarkMode }">
    <div class="app-header">
      <div class="logo-container">
        <div class="logo">📊</div>
        <h1>Приложение табулированных функций</h1>
      </div>
      <div class="theme-switcher">
        <button @click="toggleDarkMode" class="theme-btn">
          {{ isDarkMode ? '☀️ Светлая тема' : '🌙 Темная тема' }}
        </button>
      </div>
    </div>

    <main class="app-content">
      <div class="auth-info">
        <p v-if="isAuthenticated">
          <span class="auth-label">Текущий пользователь ID:</span>
          <span class="auth-value">{{ currentUserId }}</span>
        </p>
        <p v-else class="auth-warning">
          <span>⚠️</span> Пользователь не аутентифицирован
        </p>
      </div>

      <!-- HomeView теперь будет содержать все кнопки -->
      <!-- Передаём ему currentUserId и обрабатываем ошибки -->
      <HomeView
        :current-user-id="currentUserId"
        @error="showError"
      />

      <!-- Информационная панель -->
      <div class="info-panel">
        <h2>О приложении</h2>
        <p>Это веб-приложение позволяет создавать, редактировать и выполнять операции над табулированными функциями.</p>
        <div class="features">
          <h3>Основные возможности:</h3>
          <ul>
            <li>Создание функций из массивов X/Y или математических функций</li>
            <li>Выполнение операций: сложение, вычитание, умножение, деление</li>
            <li>Дифференцирование функций</li>
            <li>Вычисление интегралов</li>
            <li>Сохранение и загрузка функций в/из файлов</li>
          </ul>
        </div>
      </div>
    </main>

    <footer class="app-footer">
      <p>© 2024 Приложение табулированных функций. Все права защищены.</p>
      <p v-if="isAuthenticated">Пользователь: admin (ID: {{ currentUserId }})</p>
    </footer>

    <!-- Общий модальный компонент для ошибок -->
    <ErrorModal
      :is-visible="showErrorModal"
      :message="errorMessage"
      @close="closeErrorModal"
    ></ErrorModal>

    <!-- Индикатор загрузки -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>Загрузка...</p>
    </div>
  </div>
</template>

<script>
import HomeView from './components/HomeView.vue'; // Импортируем HomeView
import ErrorModal from './components/ErrorModal.vue'; // Путь может отличаться
import { setCredentials, isAuthenticated } from './auth.js';

export default {
  name: 'App',
  components: {
    HomeView, // Регистрируем HomeView
    ErrorModal
  },
  data() {
    return {
      currentUserId: 1, // Это значение будет перезаписано в created
      createdFunctions: [],
      showErrorModal: false,
      errorMessage: '',
      isDarkMode: false,
      isLoading: false,
    };
  },
  computed: {
    isAuthenticated() {
      return isAuthenticated();
    }
  },
  created() {
    // Установка учетных данных по умолчанию
    setCredentials('admin', 'admin123');
    this.currentUserId = 4; // Установка конкретного ID

    // Проверка темы из localStorage
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
      this.isDarkMode = true;
      document.documentElement.classList.add('dark');
    }

    // Установка фабрики по умолчанию
    if (!localStorage.getItem('selectedTabulatedFunctionFactory')) {
      localStorage.setItem('selectedTabulatedFunctionFactory', 'array');
      console.log('Установлена фабрика по умолчанию: array');
    }
  },
  methods: {
    showError(message) {
      this.errorMessage = message;
      this.showErrorModal = true;
      // Автоматически скрываем модальное окно через 5 секунд
      setTimeout(() => {
        this.closeErrorModal();
      }, 5000);
    },

    closeErrorModal() {
      this.showErrorModal = false;
      this.errorMessage = '';
    },

    toggleDarkMode() {
      this.isDarkMode = !this.isDarkMode;
      localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light');

      if (this.isDarkMode) {
        document.documentElement.classList.add('dark');
      } else {
        document.documentElement.classList.remove('dark');
      }
    },

    startLoading() {
      this.isLoading = true;
    },

    stopLoading() {
      setTimeout(() => {
        this.isLoading = false;
      }, 300);
    },
  },
  mounted() {
    // Добавляем обработчики для индикации загрузки
    document.addEventListener('startLoading', this.startLoading);
    document.addEventListener('stopLoading', this.stopLoading);

    // Инициализируем аутентификацию
    try {
      console.log('Попытка аутентификации...');
      setCredentials('admin', 'admin123');
      console.log('Аутентификация успешна');
    } catch (error) {
      console.error('Ошибка аутентификации:', error);
      this.showError('Ошибка аутентификации. Пожалуйста, войдите в систему.');
    }
  },
  beforeUnmount() {
    // Удаляем обработчики событий
    document.removeEventListener('startLoading', this.startLoading);
    document.removeEventListener('stopLoading', this.stopLoading);
  }
};
</script>

<style>
:root {
  --bg-color: #ffffff;
  --text-color: #2c3e50;
  --border-color: #ddd;
  --header-bg: #f8f9fa;
  --button-bg: #007bff;
  --button-hover: #0056b3;
  --error-bg: #ffebee;
  --error-text: #f44336;
  --info-bg: #e3f2fd;
  --info-text: #1976d2;
  --footer-bg: #f5f5f5;
}

.dark {
  --bg-color: #1a1a1a;
  --text-color: #f8f9fa;
  --border-color: #444;
  --header-bg: #2d2d2d;
  --button-bg: #1e88e5;
  --button-hover: #1565c0;
  --error-bg: #4a1414;
  --error-text: #ffcdd2;
  --info-bg: #0d47a1;
  --info-text: #bbdefb;
  --footer-bg: #252525;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  background-color: var(--bg-color);
  color: var(--text-color);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  line-height: 1.6;
  transition: background-color 0.3s, color 0.3s;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-color);
  color: var(--text-color);
  transition: background-color 0.3s, color 0.3s;
}

.app-header {
  background-color: var(--header-bg);
  padding: 1rem 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.logo {
  font-size: 2rem;
}

.logo-container h1 {
  font-size: 1.8rem;
  color: var(--text-color);
}

.theme-switcher {
  margin-left: auto;
}

.theme-btn {
  background-color: var(--button-bg);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
}

.theme-btn:hover {
  background-color: var(--button-hover);
}

.app-content {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.auth-info {
  margin-bottom: 1.5rem;
  padding: 1rem;
  border-radius: 4px;
  background-color: var(--info-bg);
  color: var(--info-text);
}

.auth-label {
  font-weight: bold;
}

.auth-warning {
  color: var(--error-text);
  font-weight: bold;
}

.info-panel {
  margin-top: 2rem;
  padding: 1.5rem;
  border-radius: 8px;
  background-color: var(--bg-color);
  border: 1px solid var(--border-color);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.info-panel h2 {
  margin-bottom: 1rem;
  color: var(--text-color);
}

.info-panel h3 {
  margin: 1rem 0 0.5rem;
  color: var(--text-color);
}

.info-panel ul {
  list-style-type: none;
  padding-left: 1.5rem;
}

.info-panel li {
  margin-bottom: 0.5rem;
  position: relative;
  padding-left: 1.2rem;
}

.info-panel li:before {
  content: "✓";
  position: absolute;
  left: 0;
  color: var(--button-bg);
}

.app-footer {
  background-color: var(--footer-bg);
  color: var(--text-color);
  text-align: center;
  padding: 1rem;
  margin-top: 2rem;
  border-top: 1px solid var(--border-color);
}

/* Стили для кнопок будут в HomeView или здесь */
.main-buttons {
  margin: 2rem 0;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.main-buttons button {
  margin: 0 5px;
  padding: 10px 20px;
  background-color: var(--button-bg);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
}

.main-buttons button:hover {
  background-color: var(--button-hover);
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 5px solid rgba(255, 255, 255, 0.3);
  border-top: 5px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-overlay p {
  color: white;
  margin-top: 15px;
  font-size: 1.2rem;
}

/* Адаптивные стили */
@media (max-width: 768px) {
  .app-header {
    flex-direction: column;
    text-align: center;
  }

  .logo-container {
    margin-bottom: 1rem;
  }

  .theme-switcher {
    margin-left: 0;
    margin-top: 10px;
  }

  .auth-info {
    font-size: 0.9rem;
  }

  .main-buttons {
    flex-direction: column;
    align-items: center;
  }

  .main-buttons button {
    width: 100%;
    max-width: 300px;
  }

  .info-panel {
    padding: 1rem;
  }
}

/* Стили для доступности */
.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  margin: -1px;
  border: 0;
  padding: 0;
  clip: rect(0 0 0 0);
  overflow: hidden;
}

:focus {
  outline: 2px solid var(--button-bg);
  outline-offset: 2px;
}

/* Анимация появления контента */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>