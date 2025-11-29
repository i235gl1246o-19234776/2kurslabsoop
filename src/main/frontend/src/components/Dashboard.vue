<template>
  <div class="dashboard dark-mode">
    <div class="app-header-bar">
      <div class="user-actions">
        <h3>Добро пожаловать в приложение "Табулированные Функции"</h3>
      </div>
    </div>

    <!-- Основной контент -->
    <main class="dashboard-content">
      <p>Выберите действие:</p>

      <div class="content-with-ads">
        <aside class="ad-banner left" @click="rickroll">
          <img
            src="/images/1.png"
            alt="Реклама"
            class="ad-image"
            @error="() => console.error('Картинка не загрузилась: /images/1.png')"
          />
        </aside>

        <!-- Основные кнопки приложения -->
        <div class="dashboard-buttons">
          <button @click="openFunctionCreator(null)" class="app-button">
            Добавить функцию
          </button>

          <button @click="openFunctionExplorer" class="app-button">
            Изучить функцию
          </button>

          <button @click="openWindow('operations')" class="app-button">
            Операции над функциями
          </button>

          <button @click="openWindow('integration')" class="app-button">
            Вычисление интеграла
          </button>

          <button @click="openWindow('differentiation')" class="app-button">
            Дифференцирование
          </button>

          <button @click="openWindow('settings')" class="app-button">
            Настройки
          </button>
        </div>

        <!-- Рекламный баннер справа — с картинкой -->
        <aside class="ad-banner right" @click="rickroll">
          <img
            src="/images/2.png"
            alt="Реклама"
            class="ad-image"
            @error="() => console.error('Картинка не загрузилась: /images/2.png')"
          />
        </aside>

      </div>
    </main>

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
import { ref, inject, nextTick } from 'vue';
import OperationsWindow from './OperationsWindow.vue';
import IntegrationWindow from './IntegrationWindow.vue';
import DifferentiationWindow from './DifferentiationWindow.vue';
import SettingsModal from './SettingsModal.vue';
import FunctionCreator from './FunctionCreator.vue';
import FunctionExplorer from './FunctionExplorer.vue';
import { api } from '../api.js';

// Состояние окон
const activeWindow = ref(null);
const showFunctionCreator = ref(false);
const showFunctionExplorer = ref(false);
const creatorTarget = ref(null);

// Состояние выбранной функции для FunctionExplorer
const selectedFunctionId = ref(null);
const selectedFunctionName = ref('');
const selectedPoints = ref([]);
const selectedInsertable = ref(false);
const selectedRemovable = ref(false);

// Глобальная функция отображения ошибок
const showError = inject('showError');

// Получаем данные аутентификации из App.vue
const auth = inject('auth');
const isLoggedIn = auth?.isLoggedIn || ref(false);
const userName = auth?.name || ref('');

// === УПРАВЛЕНИЕ ОКНАМИ ===
const openWindow = async (windowName) => {
  closeAllWindows();
  await nextTick();
  activeWindow.value = windowName;
};

const closeWindow = async () => {
  activeWindow.value = null;
  await nextTick();
};

const openFunctionCreator = async (target) => {
  closeAllWindows();
  await nextTick();
  creatorTarget.value = target;
  showFunctionCreator.value = true;
};

const closeFunctionCreator = async () => {
  showFunctionCreator.value = false;
  creatorTarget.value = null;
  await nextTick();
};

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
    let eventName, detail;
    if (creatorTarget.value === 'A' || creatorTarget.value === 'B') {
      eventName = 'operation-function-created';
      detail = { operand: creatorTarget.value, functionId, functionName, points };
    } else if (creatorTarget.value === 'diff') {
      eventName = 'diff-function-created';
      detail = { functionId, functionName, points };
    }

    if (eventName) {
      setTimeout(() => {
        window.dispatchEvent(new CustomEvent(eventName, { detail }));
      }, 0);
    }
  } else {
    if (confirm('Функция успешно создана! Хотите перейти в окно изучения функции?')) {
      selectedFunctionId.value = functionId;
      selectedFunctionName.value = functionName;
      selectedPoints.value = points;
      selectedInsertable.value = true;
      selectedRemovable.value = true;

      await closeFunctionCreator();
      await nextTick();
      showFunctionExplorer.value = true;
    } else {
      await closeFunctionCreator();
    }
  }
};

// === ЗАКРЫТИЕ ВСЕХ ОКОН ===
const closeAllWindows = async () => {
  activeWindow.value = null;
  showFunctionCreator.value = false;
  showFunctionExplorer.value = false;
  creatorTarget.value = null;
  await nextTick();
};

// === РИКРОЛЛ ===
const rickroll = () => {
  window.open('https://www.youtube.com/watch?v=dQw4w9WgXcQ', '_blank', 'noopener,noreferrer');
};

// Метод для выхода (заглушка)
const logout = () => {
  window.location.reload();
};
</script>

<style scoped>
/* Градиентный фон */
.dashboard {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  min-height: 100vh;
  background-color: #230942;
  color: #ffffff;
  position: relative;
}

/* Шапка */
.app-header-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1.2rem 2rem;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 0 0 12px 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4);
}

.app-header-bar h3 {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 600;
}

/* Основной контент */
.dashboard-content {
  max-width: 1200px;
  margin: 3rem auto;
  padding: 2rem;
  text-align: center;
}

.dashboard-content p {
  margin-bottom: 2rem;
  color: #ffd9fb;
  font-size: 1.2rem;
}

/* Контейнер с рекламой и кнопками */
.content-with-ads {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
  max-width: 1100px;
  margin: 0 auto;
}

/* Рекламные баннеры */
.ad-banner {
  flex: 0 0 250px;
  /* Убран градиентный фон для проверки картинки */
  background: transparent;
  color: #333;
  border-radius: 12px;
  padding: 0; /* убран padding, чтобы не мешать картинке */
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ad-banner:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
}

.ad-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem; /* минимальные отступы */
}

/* Стиль для картинки в баннере */
.ad-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
  display: block;
}

/* Текстовая заглушка (для левого баннера) */
.ad-text {
  font-weight: bold;
  font-size: 1.1rem;
  color: #7b1fa8;
  text-align: center;
  padding: 1rem;
}

/* Основные кнопки */
.dashboard-buttons {
  display: flex;
  flex-direction: column;
  gap: 1.1rem;
  align-items: center;
  flex: 1;
  max-width: 400px;
}

.app-button {
  width: 100%;
  max-width: 320px;
  padding: 0.9rem 1.5rem;
  font-size: 1.05rem;
  font-weight: 600;
  background: linear-gradient(135deg, #2f105c 0%, #7b1fa8 40%, #ff4fc4 100%);
  color: #ffffff;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.35);
}

.app-button:hover {
  transform: translateY(-3px);
  background: linear-gradient(135deg, #3d1474 0%, #9e27c8 40%, #ff6fda 100%);
  box-shadow: 0 8px 22px rgba(0, 0, 0, 0.45);
}

.app-button:active {
  transform: scale(0.97);
}

/* Модальные окна */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1001;
}

.modal-content {
  background: linear-gradient(160deg, #2a0c55 0%, #551e90 45%, #ff4fc494 100%);
  color: #ffffff;
  border-radius: 14px;
  padding: 25px;
  width: 90%;
  max-width: 850px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

/* Адаптивность */
@media (max-width: 768px) {
  .content-with-ads {
    flex-direction: column;
    align-items: center;
    gap: 1.5rem;
  }

  .ad-banner {
    flex: none;
    width: 100%;
    max-width: 300px;
    min-height: 200px;
  }

  .dashboard-buttons {
    order: -1;
  }
}
</style>