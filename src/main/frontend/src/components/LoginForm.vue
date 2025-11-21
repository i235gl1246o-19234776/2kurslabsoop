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
import { ref } from 'vue';
import { api } from '../api.js';

const username = ref('');
const password = ref('');

const emit = defineEmits(['login-success', 'close']);

const login = async () => {
  try {
    const userData = await api.login(username.value, password.value);
    emit('login-success', userData);
  } catch (e) {
    // Здесь можно передать ошибку в родительский компонент через emit
    console.error("Login error:", e);
    // emit('error', e.message);
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