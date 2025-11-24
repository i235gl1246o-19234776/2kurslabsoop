<template>
  <div v-if="isOpen" class="modal-overlay" @click="closeDialog">
    <div class="modal-content" @click.stop>
      <h2>Настройки</h2>

      <div class="settings-section">
        <h3>Фабрика табулированных функций</h3>
        <div class="setting-group">
          <label>
            <input
              type="radio"
              v-model="selectedFactory"
              value="array"
            />
            Массив
          </label>
          <label>
            <input
              type="radio"
              v-model="selectedFactory"
              value="linkedlist"
            />
            Связный список
          </label>
        </div>
      </div>

      <div class="settings-section">
        <h3>Настройки отображения</h3>
        <div class="setting-group">
          <label>
            Количество точек для предпросмотра:
            <input
              type="number"
              v-model.number="previewPoints"
              min="5"
              max="50"
            />
          </label>
        </div>
      </div>

      <div class="button-group">
        <button @click="saveSettings" class="save-btn">Сохранить</button>
        <button @click="closeDialog" class="cancel-btn">Отмена</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SettingsDialog',
  props: {
    isOpen: {
      type: Boolean,
      required: true
    }
  },
  emits: ['close'],

  data() {
    return {
      selectedFactory: 'array',
      previewPoints: 10
    };
  },

  mounted() {
    this.loadSettings();
  },

  methods: {
    loadSettings() {
      const savedFactory = localStorage.getItem('selectedTabulatedFunctionFactory');
      const savedPreviewPoints = localStorage.getItem('previewPoints');

      if (savedFactory) {
        this.selectedFactory = savedFactory;
      }

      if (savedPreviewPoints) {
        this.previewPoints = parseInt(savedPreviewPoints);
      }
    },

    saveSettings() {
      localStorage.setItem('selectedTabulatedFunctionFactory', this.selectedFactory);
      localStorage.setItem('previewPoints', this.previewPoints.toString());
      alert('Настройки сохранены!');
      this.closeDialog();
    },

    closeDialog() {
      this.$emit('close');
    }
  }
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
  min-width: 500px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.settings-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.settings-section h3 {
  margin-bottom: 10px;
  color: #333;
}

.setting-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.setting-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.button-group {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.save-btn {
  background-color: #4CAF50;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.save-btn:hover {
  background-color: #45a049;
}

.cancel-btn {
  background-color: #f44336;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.cancel-btn:hover {
  background-color: #da190b;
}
</style>