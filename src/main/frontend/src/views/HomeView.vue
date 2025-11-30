<!-- src/views/HomeView.vue -->
<template>
  <div class="home">
    <h1>Добро пожаловать!</h1>
    <p>Выберите действие:</p>
    <div class="button-grid">
      <button @click="openFunctionCreator">Создать функцию</button>
      <button @click="openSettings">Настройки</button>
      <!-- Добавьте другие кнопки по мере необходимости -->
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const openFunctionCreator = () => {
  router.push('/function-creator');
};

const openSettings = () => {
  router.push('/settings');
};
</script>

<style scoped>
.home {
  padding: 2rem;
  text-align: center;
}

.button-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
}

button {
  padding: 12px 24px;
  font-size: 1rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  width: 200px;
}

button:hover {
  background-color: #0056b3;
}
</style>