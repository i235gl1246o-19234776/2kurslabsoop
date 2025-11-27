<template>
  <!-- Модальное окно появляется, когда isOpen === true -->
  <div v-if="isOpen" class="error-modal-overlay">
    <div class="error-modal-content">
      <h3>Ошибка</h3>
      <p>{{ message }}</p>
      <button @click="closeModal">Закрыть</button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';

// Определяем входные параметры для компонента
const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true,
  },
  message: {
    type: String,
    default: 'Произошла неизвестная ошибка',
  },
});

// Определяем событие, которое будет генерировать компонент
const emit = defineEmits(['close']);

// Функция для закрытия модального окна
const closeModal = () => {
  emit('close'); // Сообщаем родительскому компоненту, что окно нужно закрыть
};

// Опционально: закрытие по клику вне окна
const handleOutsideClick = (event) => {
  // Проверяем, кликнули ли мы по overlay (а не по содержимому окна)
  if (event.target.classList.contains('error-modal-overlay')) {
    closeModal();
  }
};

// Следим за изменением isOpen, чтобы добавить/удалить обработчик клика
watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    document.addEventListener('click', handleOutsideClick);
  } else {
    document.removeEventListener('click', handleOutsideClick);
  }
});
</script>

<style scoped>
.error-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* Полупрозрачный фон */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000; /* Высокий z-index, чтобы перекрыть остальное содержимое */
}

.error-modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 90%;
  text-align: center;
}

.error-modal-content h3 {
  margin-top: 0;
  color: #d32f2f; /* Красный цвет для заголовка */
}

.error-modal-content p {
  margin: 15px 0;
  color: #555;
}

.error-modal-content button {
  background-color: #d32f2f; /* Красная кнопка */
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

.error-modal-content button:hover {
  background-color: #b71c1c; /* Более тёмный красный при наведении */
}
</style>