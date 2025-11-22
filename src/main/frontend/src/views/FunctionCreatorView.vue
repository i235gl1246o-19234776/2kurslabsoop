<!-- src/views/FunctionCreatorView.vue -->
<template>
  <div class="modal-overlay" @click="goBack">
    <div class="modal-content" @click.stop>
      <FunctionCreator />
      <button @click="goBack">Закрыть</button>
    </div>
  </div>
</template>

<script setup>
import FunctionCreator from '../components/FunctionCreator.vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const goBack = () => {
  router.go(-1); // Возвращаемся на предыдущую страницу
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
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 90%;
  max-width: 800px; /* Можно изменить размер */
  max-height: 90vh;
  overflow-y: auto;
  text-align: center;
}

button {
  margin-top: 20px;
  padding: 8px 16px;
  background-color: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #5a6268;
}
</style>