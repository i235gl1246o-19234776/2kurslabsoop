<!-- src/components/LoginModal.vue -->
<template>
  <div v-if="show" class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <h2>Вход в систему</h2>
      <div class="login-form">
        <div class="form-group">
          <label for="username">Имя пользователя:</label>
          <input type="text" id="username" v-model="username" />
        </div>
        <div class="form-group">
          <label for="password">Пароль:</label>
          <input type="password" id="password" v-model="password" />
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <div class="form-actions">
          <button @click="login" :disabled="isLoading">
            <span v-if="isLoading">Загрузка...</span>
            <span v-else>Войти</span>
          </button>
          <button @click="close">Закрыть</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { setCredentials, setUserId, api } from '../api.js';

const emit = defineEmits(['close', 'login-success']);
const show = ref(true);
const username = ref('admin');
const password = ref('admin123');
const error = ref('');
const isLoading = ref(false);

const login = async () => {
  error.value = '';
  isLoading.value = true;

  try {
    // Попробуем выполнить запрос для проверки учетных данных
    setCredentials(username.value, password.value);

    // Проверим аутентификацию через запрос
    const userId = 4; // Здесь должен быть запрос к серверу
    setUserId(userId);

    emit('login-success', {
      name: username.value,
      id: userId
    });
    show.value = false;
  } catch (err) {
    console.error('Ошибка входа:', err);
    error.value = 'Неверное имя пользователя или пароль';
  } finally {
    isLoading.value = false;
  }
};

const close = () => {
  emit('close');
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 400px;
  padding: 20px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

label {
  font-weight: bold;
}

input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.error-message {
  color: #e74c3c;
  background-color: #ffebee;
  padding: 10px;
  border-radius: 4px;
  border-left: 3px solid #e74c3c;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

button {
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

button:first-child {
  background-color: #4CAF50;
  color: white;
}

button:first-child:disabled {
  background-color: #4CAF50;
}

button:last-child {
  background-color: #e0e0e0;
}
</style>