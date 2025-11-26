<template>
  <div class="dashboard">
    <h2>Добро пожаловать в приложение "Табулированные Функции"</h2>
    <p>Выберите действие:</p>
    <div class="dashboard-buttons">
      <!-- КНОПКА ДОБАВЛЕНИЯ ФУНКЦИИ -->
      <button @click="openFunctionCreator(null)" class="primary-button">
        <i class="fas fa-plus"></i> Добавить функцию
      </button>
      <button @click="openFunctionExplorer" class="secondary-button">
        <i class="fas fa-chart-line"></i> Изучить функцию
      </button>
      <button @click="openWindow('operations')" class="secondary-button">
        <i class="fas fa-calculator"></i> Операции над функциями
      </button>
      <button @click="openWindow('integration')" class="secondary-button">
        <i class="fas fa-integral"></i> Вычисление интеграла
      </button>
      <button @click="openWindow('composite')" class="secondary-button">
        <i class="fas fa-project-diagram"></i> Составные функции
      </button>
      <button @click="openWindow('differentiation')" class="secondary-button">
        <i class="fas fa-derivative"></i> Дифференцирование
      </button>
      <button @click="openWindow('settings')" class="secondary-button">
        <i class="fas fa-cog"></i> Настройки
      </button>
    </div>
    <!-- Модальные окна -->
    <OperationsWindow
      v-if="activeWindow === 'operations'"
      :show="true"
      @close="closeWindow"
      @create-function="openFunctionCreator"
    />
    <IntegrationWindow
      v-if="activeWindow === 'integration'"
      :show="true"
      @close="closeWindow"
    />
    <CompositeFunctionCreator
      v-if="activeWindow === 'composite'"
      :show="true"
      @close="closeWindow"
      @function-created="handleCompositeFunctionCreated"
    />
    <DifferentiationWindow
      v-if="activeWindow === 'differentiation'"
      :show="true"
      @close="closeWindow"
      @create-function="openFunctionCreator"
    />
    <SettingsModal
      v-if="activeWindow === 'settings'"
      :is-open="true"
      @close="closeWindow"
    />
    <!-- Модальное окно создания функции -->
    <div v-if="showFunctionCreator" class="modal-overlay">
      <div class="modal-content">
        <FunctionCreator
          :target-operand="creatorTarget"
          @function-created="handleFunctionCreated"
          @close="closeFunctionCreator"
        />
      </div>
    </div>
    <!-- Модальное окно изучения функции -->
    <FunctionExplorer
      v-if="showFunctionExplorer"
      :function-id="selectedFunctionId"
      :function-name="selectedFunctionName"
      :initial-points="selectedPoints"
      :insertable="selectedInsertable"
      :removable="selectedRemovable"
      @close="closeFunctionExplorer"
      @create-new-function="handleCreateNewFunctionFromExplorer"
    />
  </div>
</template>
<script setup>
import { ref, onMounted, inject, nextTick } from 'vue';
import OperationsWindow from './OperationsWindow.vue';
import IntegrationWindow from './IntegrationWindow.vue';
import CompositeFunctionCreator from './CompositeFunctionCreator.vue';
import DifferentiationWindow from './DifferentiationWindow.vue';
import SettingsModal from './SettingsModal.vue';
import FunctionCreator from './FunctionCreator.vue';
import FunctionExplorer from './FunctionExplorer.vue';
import { api } from '@/api.js';
// Состояние окон
const activeWindow = ref(null);
const showFunctionCreator = ref(false);
const showFunctionExplorer = ref(false);
const creatorTarget = ref(null); // null, 'A', 'B', 'diff' и т.д.
// Состояние выбранной функции для FunctionExplorer
const selectedFunctionId = ref(null);
const selectedFunctionName = ref('');
const selectedPoints = ref([]);
const selectedInsertable = ref(false);
const selectedRemovable = ref(false);
// Глобальная функция отображения ошибок
const showError = inject('showError');
// === УПРАВЛЕНИЕ ОКНАМИ ===
const openWindow = async (windowName) => {
  // Сначала закрываем все окна
  closeAllWindows();
  // Даем Vue обновиться перед открытием нового окна
  await nextTick();
  activeWindow.value = windowName;
};
const closeWindow = async () => {
  activeWindow.value = null;
  await nextTick(); // Даем Vue завершить обновление DOM
};
const openFunctionCreator = async (target) => {
  // Сначала закрываем все окна
  closeAllWindows();
  // Даем Vue обновиться
  await nextTick();
  creatorTarget.value = target;
  showFunctionCreator.value = true;
};
const closeFunctionCreator = async () => {
  showFunctionCreator.value = false;
  creatorTarget.value = null;
  await nextTick();
};
// Обработка события "Новая функция" из FunctionExplorer
const handleCreateNewFunctionFromExplorer = async () => {
  await closeFunctionExplorer();
  await nextTick();
  creatorTarget.value = null;
  showFunctionCreator.value = true;
};
const closeFunctionExplorer = async () => {
  showFunctionExplorer.value = false;
  selectedFunctionId.value = null;
  selectedPoints.value = [];
  await nextTick();
};
// === ИЗУЧЕНИЕ ФУНКЦИИ ===
const openFunctionExplorer = async () => {
  try {
    // Сначала закрываем все окна
    closeAllWindows();
    await nextTick();
    const userId = api.getStoredUserId();
    const functions = await api.getFunctionsByUserId(userId);
    if (functions.length === 0) {
      showError('У вас нет сохраненных функций. Сначала создайте функцию.');
      return;
    }
    const firstFunction = functions[0];
    selectedFunctionId.value = firstFunction.functionId;
    selectedFunctionName.value = firstFunction.functionName;
    selectedInsertable.value = firstFunction.insertable || false;
    selectedRemovable.value = firstFunction.removable || false;
    const points = await api.getTabulatedPointsByFunctionId(firstFunction.functionId);
    selectedPoints.value = points.map(p => ({
      x: parseFloat(p.xval),
      y: parseFloat(p.yval)
    }));
    showFunctionExplorer.value = true;
  } catch (error) {
    showError(error.message || 'Ошибка при загрузке функций');
  }
};
// === ОБРАБОТКА СОЗДАННОЙ ФУНКЦИИ ===
const handleFunctionCreated = async (eventData) => {
  const { functionId, functionName, points } = eventData;
  if (creatorTarget.value) {
    // Если функция создана для операции — передаём данные через глобальное событие
    let eventName, detail;
    if (creatorTarget.value === 'A' || creatorTarget.value === 'B') {
      eventName = 'operation-function-created';
      detail = { operand: creatorTarget.value, functionId, functionName, points };
    } else if (creatorTarget.value === 'diff') {
      eventName = 'diff-function-created';
      detail = { functionId, functionName, points };
    }
    if (eventName) {
      // Используем setTimeout для гарантии, что событие будет обработано после обновления DOM
      setTimeout(() => {
        window.dispatchEvent(new CustomEvent(eventName, { detail }));
      }, 0);
    }
  } else {
    // Обычное создание — предлагаем открыть FunctionExplorer
    if (confirm('Функция успешно создана! Хотите перейти в окно изучения функции?')) {
      selectedFunctionId.value = functionId;
      selectedFunctionName.value = functionName;
      selectedPoints.value = points;
      selectedInsertable.value = true;
      selectedRemovable.value = true;
      // Закрываем создатель функций перед открытием explorer
      await closeFunctionCreator();
      await nextTick();
      showFunctionExplorer.value = true;
    } else {
      await closeFunctionCreator();
    }
  }
};
// === СОСТАВНЫЕ ФУНКЦИИ ===
const handleCompositeFunctionCreated = () => {
  alert('Составная функция создана! Перейдите в "Изучить функцию" для табуляции.');
};
// === ЗАКРЫТИЕ ВСЕХ ОКОН ===
const closeAllWindows = async () => {
  activeWindow.value = null;
  showFunctionCreator.value = false;
  showFunctionExplorer.value = false;
  creatorTarget.value = null;
  // Даем Vue время на обновление
  await nextTick();
};
// При монтировании — ничего не делаем (окна открываются по кнопкам)
onMounted(() => {
  // Можно добавить логику, если нужно
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
  width: 100%;
  max-width: 300px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.dashboard-buttons button:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}
.dashboard-buttons .primary-button {
  background-color: #2196f3;
  font-weight: bold;
  font-size: 1.1rem;
}
.dashboard-buttons .primary-button:hover {
  background-color: #1976d2;
}
.dashboard-buttons .secondary-button {
  background-color: #3498db;
}
.dashboard-buttons .secondary-button:hover {
  background-color: #2980b9;
}
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
  z-index: 1001;
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
  z-index: 1002;
}
</style>