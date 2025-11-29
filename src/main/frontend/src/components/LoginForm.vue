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
  background: linear-gradient(
    160deg,
    #2a0c55 0%,
    #551e90 45%,
    #ff4fc494 100%
  );
  padding: 2rem;
  border-radius: 14px;
  max-width: 400px;
  width: 90%;
  margin: auto;
  color: #ffffff;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.15);
  display: flex;
  flex-direction: column;
  gap: 1.5rem; /* Увеличено с 1rem */
  text-align: center;
}

.form-container h2 {
  margin-bottom: 1rem;
  font-size: 1.6rem;
  font-weight: 600;
}

.form-container input {
  width: 100%;
  padding: 0.8rem 1rem;
  border-radius: 10px;
  border: none;
  font-size: 1rem;
  outline: none;
  color: #ffffff;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(6px);
  transition: all 0.25s ease;
  margin-bottom: 1rem; /* Добавлено дополнительное расстояние между полями */
}

.form-container input::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

.form-container input:focus {
  background: rgba(255, 255, 255, 0.15);
}

.form-container button {
  width: 100%;
  padding: 0.9rem 1.5rem;
  font-size: 1rem;
  font-weight: 600;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  color: #ffffff;
  background: linear-gradient(
    135deg,
    #2f105c 0%,
    #7b1fa8 40%,
    #ff4fc4 100%
  );
  transition: all 0.25s ease;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.35);
  margin-top: 0.5rem; /* Добавлено расстояние перед кнопками */
}

.form-container button:hover {
  transform: translateY(-3px);
  background: linear-gradient(
    135deg,
    #3d1474 0%,
    #9e27c8 40%,
    #ff6fda 100%
  );
  box-shadow: 0 8px 22px rgba(0, 0, 0, 0.45);
}

.form-container button:active {
  transform: scale(0.97);
}

.error-message {
  background: #ffebee;
  border: 2px solid #f44336;
  border-radius: 8px;
  padding: 1rem;
  color: #c62828;
  text-align: left;
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