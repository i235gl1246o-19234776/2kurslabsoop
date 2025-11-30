<!-- src/components/SettingsModal.vue -->
<template>
  <div class="modal-overlay" @click.self="close">
    <div class="settings-modal">
      <div class="window-header">
        <h2>Настройки</h2>
        <button class="close-button" @click="close">&times;</button>
      </div>

      <div class="settings-content">
        <div class="setting-section">
          <h3>Фабрика табулированных функций</h3>
          <div class="setting-item">
            <label for="factory-select" class="setting-label">
              Выберите тип реализации:
            </label>
            <select
              id="factory-select"
              v-model="selectedFactoryKey"
              @change="saveSettings"
              class="setting-select"
            >
              <option value="array">Массив (ArrayTabulatedFunction)</option>
              <option value="linked-list">Связный список (LinkedListTabulatedFunction)</option>
            </select>
            <div class="setting-description">
              <p><strong>Массив</strong> - быстрый доступ по индексу, медленная вставка/удаление</p>
              <p><strong>Связный список</strong> - быстрая вставка/удаление, медленный доступ по индексу</p>
            </div>
          </div>
        </div>

        <div class="settings-actions">
          <button @click="close" class="close-btn">Закрыть</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const emit = defineEmits(['close']);

// Ключ выбранной фабрики
const selectedFactoryKey = ref('array');

// Загружаем настройки при монтировании
onMounted(() => {
  const savedFactory = localStorage.getItem('tabulatedFunctionFactory');
  if (savedFactory && (savedFactory === 'array' || savedFactory === 'linked-list')) {
    selectedFactoryKey.value = savedFactory;
  }
});

// Сохраняем настройки в localStorage
const saveSettings = () => {
  localStorage.setItem('tabulatedFunctionFactory', selectedFactoryKey.value);
};

const close = () => {
  emit('close');
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.settings-modal {
  position: relative;
  padding: 25px 20px;
  border-radius: 16px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.6);
  max-width: 600px;
  max-height: 90vh;
  margin: 20px auto;
  color: #ffffff;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: #1a0a2e;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #5b1fa8;
}

/* Крестик */
.close-button {
  position: absolute;
  top: 12px;
  right: 12px;
  cursor: pointer;
  font-size: 24px;
  color: #ffffff;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
  z-index: 10;
  background: none;
  border: none;
}
.close-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
  color: #ff6fda;
  transform: rotate(90deg);
}

.window-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #5b1fa8;
}

h2 {
  color: #ffffff;
  margin: 0;
  font-size: 1.8rem;
}

h3 {
  color: #ffffff;
  margin: 0 0 15px 0;
  font-size: 1.3rem;
}

.settings-content {
  flex: 1;
  overflow-y: auto;
  padding: 0 10px;
}

.setting-section {
  margin-bottom: 25px;
  padding: 20px;
  background: rgba(47, 16, 92, 0.3);
  border-radius: 12px;
  border: 1px solid #5b1fa8;
}

.setting-item {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.setting-label {
  font-weight: 600;
  color: #ffffff;
  font-size: 1.1rem;
}

.setting-select {
  padding: 12px 16px;
  border: 1px solid #5b1fa8;
  border-radius: 8px;
  background-color: #2f105c;
  color: #ffffff;
  font-size: 1rem;
  transition: border-color 0.25s ease;
  cursor: pointer;
}

.setting-select:focus {
  outline: none;
  border-color: #ff4fc4;
  box-shadow: 0 0 0 2px rgba(255, 79, 196, 0.2);
}

.setting-description {
  padding: 15px;
  background: rgba(91, 31, 168, 0.2);
  border-radius: 8px;
  border-left: 3px solid #ff4fc4;
}

.setting-description p {
  margin: 8px 0;
  color: #cccccc;
  font-size: 0.95rem;
  line-height: 1.4;
}

.setting-description strong {
  color: #ff6fda;
}

.settings-actions {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.close-btn {
  padding: 12px 30px;
  background: linear-gradient(135deg, #5b1fa8 0%, #ff4fc4 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.25s ease;
  box-shadow: 0 4px 15px rgba(91, 31, 168, 0.3);
}

.close-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(91, 31, 168, 0.4);
  background: linear-gradient(135deg, #7b1fa8 0%, #ff6fda 100%);
}

@media (max-width: 768px) {
  .settings-modal {
    width: 95%;
    margin: 10px;
    max-height: 95vh;
    padding: 20px 15px;
  }

  .setting-section {
    padding: 15px;
  }

  .settings-content {
    max-height: calc(95vh - 100px);
  }
}

@media (max-width: 480px) {
  .settings-modal {
    padding: 15px 10px;
  }

  .setting-section {
    padding: 12px;
  }

  .setting-select {
    padding: 10px 12px;
  }

  .close-btn {
    padding: 10px 25px;
    width: 100%;
  }
}

/* Кастомный скроллбар */
.settings-content::-webkit-scrollbar {
  width: 6px;
}

.settings-content::-webkit-scrollbar-track {
  background: #2f105c;
  border-radius: 3px;
}

.settings-content::-webkit-scrollbar-thumb {
  background: #5b1fa8;
  border-radius: 3px;
}

.settings-content::-webkit-scrollbar-thumb:hover {
  background: #ff4fc4;
}
</style>