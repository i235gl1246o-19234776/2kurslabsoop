<!-- src/components/SettingsModal.vue -->
<template>
  <div v-if="isOpen" class="settings-modal">
    <div class="modal-content">
      <div class="modal-header">
        <h2>Настройки</h2>
        <button class="close-btn" @click="$emit('close')">&times;</button>
      </div>
      <div class="settings-container">
        <div class="settings-section">
          <h3>Фабрика табулированных функций</h3>
          <p>Выберите реализацию для создания табулированных функций</p>
          <div class="factory-options">
            <div
              class="factory-option"
              :class="{ selected: selectedFactory === 'array' }"
              @click="selectedFactory = 'array'"
            >
              <div class="factory-icon">
                <i class="fas fa-table"></i>
              </div>
              <div class="factory-info">
                <h4>Массив (ArrayTabulatedFunctionFactory)</h4>
                <p>Оптимальна для частого доступа к элементам по индексу</p>
              </div>
            </div>
            <div
              class="factory-option"
              :class="{ selected: selectedFactory === 'linkedlist' }"
              @click="selectedFactory = 'linkedlist'"
            >
              <div class="factory-icon">
                <i class="fas fa-link"></i>
              </div>
              <div class="factory-info">
                <h4>Связный список (LinkedListTabulatedFunctionFactory)</h4>
                <p>Оптимальна для частых вставок и удалений элементов</p>
              </div>
            </div>
          </div>
        </div>
        <div class="settings-section">
          <h3>Тема оформления</h3>
          <p>Выберите цветовую схему интерфейса</p>
          <div class="theme-options">
            <div
              class="theme-option"
              :class="{ selected: currentTheme === 'light' }"
              @click="setTheme('light')"
            >
              <div class="theme-preview light-theme"></div>
              <span>Светлая тема</span>
            </div>
            <div
              class="theme-option"
              :class="{ selected: currentTheme === 'dark' }"
              @click="setTheme('dark')"
            >
              <div class="theme-preview dark-theme"></div>
              <span>Темная тема</span>
            </div>
          </div>
        </div>
        <div class="settings-section">
          <h3>Другие настройки</h3>
          <div class="setting-item">
            <label for="autoSave">Автоматическое сохранение</label>
            <input type="checkbox" id="autoSave" v-model="autoSave">
          </div>
          <div class="setting-item">
            <label for="maxPoints">Максимальное количество точек для отображения</label>
            <input type="number" id="maxPoints" v-model="maxPoints" min="10" max="1000">
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button class="cancel-btn" @click="$emit('close')">Отмена</button>
        <button class="save-btn" @click="saveSettings">Сохранить настройки</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import { api } from '../api.js';

const props = defineProps({
  isOpen: Boolean
});
const emit = defineEmits(['close']);

// Настройки фабрики
const availableFactories = ref([]);
const selectedFactory = ref('array');

// Настройки темы
const currentTheme = ref('light');

// Другие настройки
const autoSave = ref(true);
const maxPoints = ref(100);

// Загрузка доступных фабрик
const loadAvailableFactories = async () => {
  try {
    if (!api.isAuthenticated()) {
      console.warn('Попытка загрузить фабрики без аутентификации');
      return;
    }

    const factories = await api.getAvailableFactories();
    availableFactories.value = factories;

    // Установка текущей фабрики из localStorage
    const savedFactory = localStorage.getItem('selectedTabulatedFunctionFactory');
    if (savedFactory) {
      selectedFactory.value = savedFactory;
    }
  } catch (error) {
    console.error('Ошибка при загрузке фабрик:', error);
  }
};

// Установка темы
const setTheme = (theme) => {
  currentTheme.value = theme;
  if (theme === 'dark') {
    document.documentElement.classList.add('dark');
  } else {
    document.documentElement.classList.remove('dark');
  }
};

// Сохранение настроек
const saveSettings = () => {
  // Сохранение фабрики
  localStorage.setItem('selectedTabulatedFunctionFactory', selectedFactory.value);
  // Сохранение темы
  localStorage.setItem('theme', currentTheme.value);
  // Сохранение других настроек
  localStorage.setItem('autoSave', autoSave.value.toString());
  localStorage.setItem('maxPoints', maxPoints.value.toString());
  emit('close');
};

// Инициализация
onMounted(() => {
  loadAvailableFactories();

  // Загрузка текущей темы
  const savedTheme = localStorage.getItem('theme');
  if (savedTheme) {
    currentTheme.value = savedTheme;
    setTheme(savedTheme);
  }

  // Загрузка других настроек
  const savedAutoSave = localStorage.getItem('autoSave');
  if (savedAutoSave !== null) {
    autoSave.value = savedAutoSave === 'true';
  }

  const savedMaxPoints = localStorage.getItem('maxPoints');
  if (savedMaxPoints !== null) {
    maxPoints.value = parseInt(savedMaxPoints);
  }
});
</script>
<style scoped>
.settings-modal {
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
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #2c3e50;
  color: white;
  border-bottom: 2px solid #34495e;
}
.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}
.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}
.settings-container {
  padding: 20px;
  overflow-y: auto;
  flex-grow: 1;
}
.settings-section {
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}
.settings-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}
.factory-options {
  display: flex;
  gap: 15px;
  margin-top: 15px;
}
.factory-option {
  flex: 1;
  border: 2px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.2s;
}
.factory-option:hover {
  border-color: #2196f3;
}
.factory-option.selected {
  border-color: #2196f3;
  background-color: #f0f7ff;
}
.factory-icon {
  font-size: 2rem;
  color: #2196f3;
  margin-bottom: 10px;
  text-align: center;
}
.factory-info h4 {
  margin: 0 0 5px 0;
  color: #333;
}
.factory-info p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}
.theme-options {
  display: flex;
  gap: 20px;
  margin-top: 15px;
}
.theme-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  padding: 10px;
  border-radius: 6px;
  transition: all 0.2s;
}
.theme-option:hover {
  background-color: #f0f0f0;
}
.theme-option.selected {
  background-color: #e3f2fd;
}
.theme-preview {
  width: 80px;
  height: 50px;
  border-radius: 4px;
  margin-bottom: 8px;
  border: 1px solid #ddd;
}
.light-theme {
  background: linear-gradient(to bottom, #ffffff 70%, #f0f0f0 30%);
}
.dark-theme {
  background: linear-gradient(to bottom, #1a1a1a 70%, #2a2a2a 30%);
}
.theme-option span {
  font-size: 0.9rem;
}
.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}
.setting-item:last-child {
  border-bottom: none;
}
.setting-item label {
  font-weight: 500;
}
.setting-item input[type="checkbox"] {
  width: auto;
  margin: 0;
}
.setting-item input[type="number"] {
  width: 80px;
  padding: 6px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.save-btn, .cancel-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.2s;
}
.save-btn {
  background-color: #2196f3;
  color: white;
}
.save-btn:hover {
  background-color: #1976d2;
}
.cancel-btn {
  background-color: #6c757d;
  color: white;
}
.cancel-btn:hover {
  background-color: #5a6268;
}
</style>