<template>
  <div class="form-container">
    <h2>Регистрация</h2>
    <form @submit.prevent="register">
      <input v-model="username" type="text" placeholder="Имя пользователя" required />
      <input v-model="password" type="password" placeholder="Пароль" required />
      <button type="submit">Зарегистрироваться</button>
    </form>
    <button @click="$emit('close')">Закрыть</button>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { api } from '../api.js';

const username = ref('');
const password = ref('');

const emit = defineEmits(['register-success', 'close']);

const register = async () => {
  try {
    await api.register(username.value, password.value);
    emit('register-success');
  } catch (e) {
    console.error("Registration error:", e);
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