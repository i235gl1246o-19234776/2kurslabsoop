<!-- src/components/RegisterForm.vue -->
<template>
  <div class="form-container">
    <h2>Регистрация</h2>
    <!-- ТОЛЬКО ОДИН блок для отображения ошибки 500 -->
    <div v-if="showServerError" class="error-message">
      <strong>Ошибка 500: Внутренняя ошибка сервера</strong>
      <p>Сервер не отвечает или возвращает ошибку. Проверьте:</p>
      <ul>
        <li>Консоль сервера на наличие ошибок</li>
      </ul>
      <button @click="showServerError = false">Скрыть</button>
    </div>
    <form @submit.prevent="register">
      <input v-model="name" type="text" placeholder="Имя пользователя" required />
      <input v-model="password" type="password" placeholder="Пароль" required />
      <button type="submit">Зарегистрироваться</button>
    </form>
    <button @click="$emit('close')">Закрыть</button>
  </div>
</template>
<script setup>
import { ref, inject } from 'vue';
import { api } from '../api.js';
const showError = inject('showError');
const name = ref('');
const password = ref('');
const showServerError = ref(false); // Флаг для отображения ошибки 500
const emit = defineEmits(['register-success', 'close']);
const register = async () => {
  try {
    console.log('🔧 Отправка запроса на регистрацию...');
    await api.register(name.value, password.value);
    emit('register-success');
  } catch (e) {
    console.error("❌ Registration error:", e);
    // Проверяем, является ли ошибка ошибкой 500
    if (e.message.includes('500') ||
        e.message.includes('HTML instead of JSON') ||
        e.message.includes('Unexpected token') ||
        e.message.includes('Network Error') ||
        e.message.includes('Failed to fetch')) {
      // Показываем специальное сообщение об ошибке 500 в КОМПОНЕНТЕ
      showServerError.value = true;
      // НЕ вызываем showError() чтобы избежать дублирования!
    } else {
      // Для других ошибок используем стандартный обработчик
      showError(e.message);
    }
  }
};
</script>
<style scoped>
.form-container {
  background: white;
  padding: 1rem;
  border-radius: 0.5rem;
  position: relative;
}
.error-message {
  background: #ffebee;
  border: 2px solid #f44336;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
  color: #c62828;
}
.error-message strong {
  font-size: 1.1em;
}
.error-message ul {
  margin: 0.5rem 0;
  padding-left: 1.5rem;
}
.error-message li {
  margin: 0.25rem 0;
}
.error-message button {
  background: #f44336;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 0.5rem;
}
.error-message button:hover {
  background: #d32f2f;
}
</style>