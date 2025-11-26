<!-- src/components/LoginForm.vue -->
<template>
  <div class="modal-content">
    <h2>Вход</h2>
    <form @submit.prevent="login">
      <input v-model="username" type="text" placeholder="Имя пользователя" required />
      <input v-model="password" type="password" placeholder="Пароль" required />
      <button type="submit">Войти</button>
    </form>
    <button @click="$emit('close')">Закрыть</button>
  </div>
</template>
<script setup>
import { ref, inject } from 'vue';
import { api } from '../api.js';

const showError = inject('showError');
const username = ref('admin');
const password = ref('admin123');
const emit = defineEmits(['login-success', 'close']);

const login = async () => {
  try {
    console.log("Попытка входа:", username.value);
    const userData = await api.login(username.value, password.value);
    console.log("Успешный вход, данные:", userData);

    // Проверяем, что получили корректные данные пользователя
    if (!userData || (!userData.id && !userData.userId)) {
      throw new Error('Некорректный ответ сервера при аутентификации');
    }

    emit('login-success', userData);
  } catch (e) {
    console.error("Ошибка входа:", e);
    showError(e.message || 'Ошибка при входе в систему');
  }
};
</script>
<style scoped>
.form-container {
  background: white;
  padding: 1rem;
  border-radius: 0.5rem;
}
</style>