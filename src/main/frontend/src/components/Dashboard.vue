<template>
  <div class="dashboard dark-mode">
    <!-- Шапка -->
    <div class="app-header-bar">
      <div class="user-actions">
        <h3>Добро пожаловать в приложение "Табулированные Функции"</h3>
      </div>
    </div>

    <!-- Основной контент -->
    <main class="dashboard-content">
      <p>Выберите действие:</p>

      <div class="content-with-ads">
        <!-- Левая реклама -->
        <aside class="ad-banner left" @click="rickroll">
          <img src="/images/1.png" alt="Реклама" class="ad-image" />
        </aside>

        <!-- Кнопки приложения -->
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

        <!-- Правая реклама -->
        <aside class="ad-banner right" @click="rickroll">
          <img src="/images/2.png" alt="Реклама" class="ad-image" />
        </aside>
      </div>
    </main>

    <!-- === МОДАЛЬНЫЕ ОКНА === -->

    <!-- FunctionCreator -->
    <div v-if="showFunctionCreator" class="modal-overlay">
      <div class="modal-content">
        <FunctionCreator
          :target-operand="creatorTarget"
          @function-created="handleFunctionCreated"
          @close="closeFunctionCreator"
        />
      </div>
    </div>

    <!-- FunctionExplorer -->
    <div v-if="showFunctionExplorer" class="modal-overlay">
      <div class="modal-content">
        <FunctionExplorer
          :function-id="selectedFunctionId"
          :function-name="selectedFunctionName"
          :initial-points="selectedPoints"
          :insertable="selectedInsertable"
          :removable="selectedRemovable"
          @close="closeFunctionExplorer"
          @create-new-function="handleCreateNewFunctionFromExplorer"
        />
      </div>
    </div>

    <!-- OperationsWindow -->
    <div v-if="showOperationsWindow" class="modal-overlay">
      <div class="modal-content">
        <OperationsWindow
          @close="closeWindow('operations')"
          @create-function="openFunctionCreator"
        />
      </div>
    </div>

    <!-- IntegrationWindow -->
    <div v-if="showIntegrationWindow" class="modal-overlay">
      <div class="modal-content">
        <IntegrationWindow
          @close="closeWindow('integration')"
        />
      </div>
    </div>

    <!-- DifferentiationWindow -->
    <div v-if="showDifferentiationWindow" class="modal-overlay">
      <div class="modal-content">
        <DifferentiationWindow
          @close="closeWindow('differentiation')"
          @create-function="openFunctionCreator"
        />
      </div>
    </div>

    <!-- SettingsModal -->
    <div v-if="showSettingsModal" class="modal-overlay">
      <div class="modal-content">
        <SettingsModal
          @close="closeWindow('settings')"
        />
      </div>
    </div>
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

// === Состояние окон ===
const showFunctionCreator = ref(false);
const showFunctionExplorer = ref(false);
const showOperationsWindow = ref(false);
const showIntegrationWindow = ref(false);
const showDifferentiationWindow = ref(false);
const showSettingsModal = ref(false);
const creatorTarget = ref(null);

// === Состояние выбранной функции ===
const selectedFunctionId = ref(null);
const selectedFunctionName = ref('');
const selectedPoints = ref([]);
const selectedInsertable = ref(false);
const selectedRemovable = ref(false);

// Глобальная функция отображения ошибок
const showError = inject('showError');

// === Управление окнами ===
const openWindow = async (name) => {
  await closeAllWindows();
  await nextTick();
  switch (name) {
    case 'operations': showOperationsWindow.value = true; break;
    case 'integration': showIntegrationWindow.value = true; break;
    case 'differentiation': showDifferentiationWindow.value = true; break;
    case 'settings': showSettingsModal.value = true; break;
  }
};

const closeWindow = async (name) => {
  switch (name) {
    case 'operations': showOperationsWindow.value = false; break;
    case 'integration': showIntegrationWindow.value = false; break;
    case 'differentiation': showDifferentiationWindow.value = false; break;
    case 'settings': showSettingsModal.value = false; break;
  }
  await nextTick();
};

const openFunctionCreator = async (target) => {
  await closeAllWindows();
  await nextTick();
  creatorTarget.value = target;
  showFunctionCreator.value = true;
};

const closeFunctionCreator = async () => {
  showFunctionCreator.value = false;
  creatorTarget.value = null;
  await nextTick();
};

const openFunctionExplorer = async () => {
  try {
    await closeAllWindows();
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
    selectedPoints.value = points.map(p => ({ x: parseFloat(p.xval), y: parseFloat(p.yval) }));

    showFunctionExplorer.value = true;
  } catch (error) {
    showError(error.message || 'Ошибка при загрузке функций');
  }
};

const closeFunctionExplorer = async () => {
  showFunctionExplorer.value = false;
  selectedFunctionId.value = null;
  selectedPoints.value = [];
  await nextTick();
};

const handleCreateNewFunctionFromExplorer = async () => {
  await closeFunctionExplorer();
  await nextTick();
  creatorTarget.value = null;
  showFunctionCreator.value = true;
};

const handleFunctionCreated = async ({ functionId, functionName, points }) => {
  if (creatorTarget.value) {
    const eventName = creatorTarget.value === 'diff' ? 'diff-function-created' : 'operation-function-created';
    const detail = { operand: creatorTarget.value, functionId, functionName, points };
    window.dispatchEvent(new CustomEvent(eventName, { detail }));
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

const closeAllWindows = async () => {
  showFunctionCreator.value = false;
  showFunctionExplorer.value = false;
  showOperationsWindow.value = false;
  showIntegrationWindow.value = false;
  showDifferentiationWindow.value = false;
  showSettingsModal.value = false;
  creatorTarget.value = null;
  await nextTick();
};

// Рикролл
const rickroll = () => {
  window.open('https://www.youtube.com/watch?v=dQw4w9WgXcQ', '_blank', 'noopener,noreferrer');
};
</script>

<style scoped>
/* --- базовые стили (как у тебя) --- */
.dashboard { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; min-height: 100vh; background-color: #230942; color: #fff; position: relative; }
.app-header-bar { display:flex; justify-content:center; align-items:center; padding:1.2rem 2rem; background: rgba(255,255,255,0.06); backdrop-filter: blur(12px); border-bottom:1px solid rgba(255,255,255,0.15); border-radius:0 0 12px 12px; box-shadow:0 6px 20px rgba(0,0,0,0.4); }
.app-header-bar h3 { margin:0; font-size:1.4rem; font-weight:600; }
.dashboard-content { max-width:1200px; margin:3rem auto; padding:2rem; text-align:center; }
.dashboard-content p { margin-bottom:2rem; color:#ffd9fb; font-size:1.2rem; }
.content-with-ads { display:flex; justify-content:space-between; align-items:flex-start; gap:2rem; max-width:1100px; margin:0 auto; }
.ad-banner { flex:0 0 250px; background:transparent; border-radius:12px; cursor:pointer; transition:all 0.3s ease; box-shadow:0 4px 15px rgba(0,0,0,0.2); border:1px solid rgba(255,255,255,0.3); min-height:300px; display:flex; align-items:center; justify-content:center; }
.ad-banner:hover { transform: translateY(-5px); box-shadow: 0 8px 25px rgba(0,0,0,0.3); }
.ad-image { width:100%; height:100%; object-fit:cover; border-radius:10px; display:block; }
.dashboard-buttons { display:flex; flex-direction:column; gap:1.1rem; align-items:center; flex:1; max-width:400px; }
.app-button { width:100%; max-width:320px; padding:0.9rem 1.5rem; font-size:1.05rem; font-weight:600; background:linear-gradient(135deg,#2f105c 0%,#7b1fa8 40%,#ff4fc4 100%); color:#fff; border:none; border-radius:12px; cursor:pointer; transition: all 0.25s ease; box-shadow:0 4px 14px rgba(0,0,0,0.35); }
.app-button:hover { transform: translateY(-3px); background: linear-gradient(135deg,#3d1474 0%,#9e27c8 40%,#ff6fda 100%); box-shadow:0 8px 22px rgba(0,0,0,0.45); }
.app-button:active { transform: scale(0.97); }
.modal-overlay { position: fixed; inset:0; background: rgba(0,0,0,0.7); display:flex; justify-content:center; align-items:center; z-index:1001; }
.modal-content { background: linear-gradient(160deg,#2a0c55 0%,#551e90 45%,#ff4fc494 100%); color:#fff; border-radius:14px; padding:25px; width:90%; max-width:850px; max-height:90vh; overflow-y:auto; box-shadow:0 8px 28px rgba(0,0,0,0.6); border:1px solid rgba(255,255,255,0.15); }
@media (max-width:768px) {
  .content-with-ads { flex-direction:column; align-items:center; gap:1.5rem; }
  .ad-banner { flex:none; width:100%; max-width:300px; min-height:200px; }
  .dashboard-buttons { order:-1; }
}
</style>
