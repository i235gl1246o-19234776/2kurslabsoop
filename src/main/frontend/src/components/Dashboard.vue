<!-- src/components/Dashboard.vue -->
<template>
  <div class="dashboard">
    <h2>Добро пожаловать в приложение "Табулированные Функции"</h2>
    <p>Выберите действие:</p>

    <div class="dashboard-buttons">
      <!-- КНОПКА ДОБАВЛЕНИЯ ФУНКЦИИ -->
      <button @click="openFunctionCreator" class="primary-button">Добавить функцию</button>

      <button @click="openWindow('operations')">Операции над функциями</button>
      <button @click="openWindow('differentiation')">Дифференцирование функции</button>
      <button @click="openWindow('settings')">Настройки</button>
    </div>

    <!-- Модальные окна -->
    <OperationsWindow
      v-if="activeWindow === 'operations'"
      :show="true"
      @close="closeWindow('operations')"
    />
    <DifferentiationWindow
      v-if="activeWindow === 'differentiation'"
      :show="true"
      @close="closeWindow('differentiation')"
    />
    <SettingsModal
      v-if="activeWindow === 'settings'"
      :is-open="true"
      @close="closeWindow('settings')"
    />

    <!-- Модальное окно создания функции -->
    <div v-if="showFunctionCreator" class="modal-overlay">
      <div class="modal-content">
        <FunctionCreator
          :is-for-operation="!!creatorOperand"
          @function-created="handleFunctionCreated"
          @close="closeFunctionCreator"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import OperationsWindow from './OperationsWindow.vue';
import DifferentiationWindow from './DifferentiationWindow.vue';
import SettingsModal from './SettingsModal.vue';
import FunctionCreator from './FunctionCreator.vue';

const activeWindow = ref(null);
const showFunctionCreator = ref(false);
const creatorOperand = ref(null); // Храним, для какого операнда создается функция

// Открытие окна создания функции
const openFunctionCreator = () => {
  // Закрываем все активные окна перед открытием создателя
  if (activeWindow.value) {
    closeWindow(activeWindow.value);
  }
  showFunctionCreator.value = true;
  creatorOperand.value = null; // Создаем функцию не для операнда, а для базы
};

// Открытие других окон
const openWindow = (windowName) => {
  if (activeWindow.value) {
    if (activeWindow.value === windowName) return;
    closeWindow(activeWindow.value);
  }
  activeWindow.value = windowName;
};

// Закрытие окна
const closeWindow = (windowName) => {
  if (activeWindow.value === windowName) {
    activeWindow.value = null;
  }
};

// === Обработка открытия FunctionCreator из Operations/Diff windows ===
const handleOpenCreateFunction = (event) => {
  const { operand } = event.detail;
  creatorOperand.value = operand;
  showFunctionCreator.value = true;
};

// Закрытие окна создания функции
const closeFunctionCreator = () => {
  showFunctionCreator.value = false;
  creatorOperand.value = null;
};

// Обработка создания функции
const handleFunctionCreated = (eventData) => {
  const { points, functionId, functionName } = eventData;

  // Эмитим событие, которое будет перехвачено в Operations/Diff windows
  if (creatorOperand.value) {
    window.dispatchEvent(new CustomEvent('function-created', {
      detail: {
        operand: creatorOperand.value,
        points,
        functionId,
        functionName
      }
    }));
  }

  closeFunctionCreator();
};

// === Подписка/отписка на глобальное событие ===
onMounted(() => {
  window.addEventListener('open-create-function', handleOpenCreateFunction);
});

onUnmounted(() => {
  window.removeEventListener('open-create-function', handleOpenCreateFunction);
});
</script>

<style scoped>
.dashboard {
  text-align: center;
  padding: 2rem;
}

.dashboard h2 {
  margin-bottom: 1rem;
  color: #333;
}

.dashboard p {
  margin-bottom: 1.5rem;
  color: #666;
}

.dashboard-buttons {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
}

.dashboard-buttons button {
  padding: 0.8rem 1.5rem;
  font-size: 1rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  width: 300px;
  transition: all 0.2s;
}

.dashboard-buttons button:hover {
  background-color: #359c6d;
  transform: translateY(-2px);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

/* СТИЛЬ ДЛЯ ПЕРВОЙ КНОПКИ "ДОБАВИТЬ ФУНКЦИЮ" */
.dashboard-buttons .primary-button {
  background-color: #2196f3;
  width: 100%;
  max-width: 300px;
  font-weight: bold;
  font-size: 1.1rem;
}

.dashboard-buttons .primary-button:hover {
  background-color: #1976d2;
}

/* Стили для модального окна FunctionCreator */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1001; /* Выше остальных модальных окон */
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
}
</style>