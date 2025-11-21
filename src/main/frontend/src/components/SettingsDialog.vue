<template>
  <div v-if="isOpen" class="modal-overlay" @click="closeDialog">
    <div class="modal-content" @click.stop>
      <h2>Настройки</h2>

      <div class="input-section">
        <label for="factorySelect">Выберите фабрику для создания табулированных функций:</label>
        <select
          id="factorySelect"
          v-model="selectedFactoryKey"
        >
          <option value="array">ArrayTabulatedFunctionFactory (на основе массива)</option>
          <option value="linkedlist">LinkedListTabulatedFunctionFactory (на основе связного списка)</option>
        </select>
      </div>

      <div class="button-group">
        <button @click="saveSettings" class="save-btn">Сохранить</button>
        <button @click="closeDialog" class="cancel-btn">Отмена</button>
      </div>
    </div>
  </div>
</template>

<script>
// Импортируем фабрики (предположим, они доступны в браузере через js-модули или глобально)
// В реальности, ты бы передавал фабрику как строку/ключ и создавал её на бэкенде.
// Для фронтенда, можно хранить ключ или использовать библиотеку состояния.
// Здесь мы просто будем сохранять ключ в localStorage.

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
      selectedFactoryKey: 'array' // Значение по умолчанию
    };
  },
  created() {
    // Загружаем сохранённую фабрику при создании компонента
    const savedFactory = localStorage.getItem('selectedTabulatedFunctionFactory');
    if (savedFactory) {
      this.selectedFactoryKey = savedFactory;
    }
  },
  methods: {
    saveSettings() {
      // Сохраняем выбранный ключ фабрики в localStorage
      localStorage.setItem('selectedTabulatedFunctionFactory', this.selectedFactoryKey);
      // Здесь можно эмитить событие, если родительский компонент должен что-то сделать
      // this.$emit('factory-changed', this.selectedFactoryKey);
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
  min-width: 400px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.input-section {
  margin-bottom: 20px;
}

.input-section label {
  display: block;
  margin-bottom: 5px;
}

.input-section select {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}

.button-group {
  display: flex;
  justify-content: space-between;
}

.save-btn {
  background-color: #2196F3;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.save-btn:hover {
  background-color: #1976D2;
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