<!-- src/components/LoginForm.vue -->
<template>
  <div class="form-container">
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

// --- ИНЪЕКТИРУЕМ showError ---
const showError = inject('showError');
// --- КОНЕЦ ИНЪЕКЦИИ ---

const username = ref('');
const password = ref('');

const emit = defineEmits(['login-success', 'close']);

const login = async () => {
  try {
    console.log("Попытка входа:", username.value);
    const userData = await api.login(username.value, password.value);
    console.log("Успешный вход, данные:", userData);
    emit('login-success', userData);
  } catch (e) {
    console.error("Ошибка входа:", e);
    // --- ВЫЗОВ ЦЕНТРАЛИЗОВАННОГО ОБРАБОТЧИКА ОШИБОК ---
    showError(e.message);
    // --- КОНЕЦ ВЫЗОВА ---
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