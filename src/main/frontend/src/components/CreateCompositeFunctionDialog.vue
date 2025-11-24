<template>
  <div v-if="isOpen" class="modal-overlay" @click="closeDialog">
    <div class="modal-content" @click.stop>
      <h2>Создание сложной функции</h2>
      <div class="input-section">
        <label for="functionSelect">Выберите базовую функцию:</label>
        <select
          id="functionSelect"
          v-model="baseFunctionName"
          :disabled="loadingFunctions"
        >
          <option value="" disabled>Выберите функцию...</option>
          <option
            v-for="name in availableFunctionNames"
            :key="name"
            :value="name"
          >
            {{ name }}
          </option>
        </select>
      </div>
      <div class="input-section">
        <label for="outerFunctionSelect">Выберите внешнюю функцию:</label>
        <select
          id="outerFunctionSelect"
          v-model="outerFunctionName"
          :disabled="loadingFunctions"
        >
          <option value="" disabled>Выберите функцию...</option>
          <option
            v-for="name in availableFunctionNames"
            :key="name"
            :value="name"
          >
            {{ name }}
          </option>
        </select>
      </div>
      <div class="input-section">
        <label for="customName">Локализованное название (для отображения):</label>
        <input
          id="customName"
          v-model="customFunctionName"
          type="text"
          placeholder="Введите название функции"
          :disabled="!baseFunctionName || !outerFunctionName"
        />
      </div>
      <div class="preview-section" v-if="baseFunctionName && outerFunctionName">
        <h4>Предпросмотр функции:</h4>
        <p>{{ outerFunctionName }}({{ baseFunctionName }}(x))</p>
      </div>
      <div class="button-group">
        <button
          @click="createCompositeFunction"
          :disabled="!canCreate || loadingFunctions"
          class="create-btn"
        >
          Создать
        </button>
        <button @click="closeDialog" class="cancel-btn">Отмена</button>
      </div>
      <div v-if="localErrorMessage" class="error-display">
        <p>{{ localErrorMessage }}</p>
      </div>
    </div>
  </div>
</template>
<script>
import * as api from '@/api.js';

export default {
  name: 'CreateCompositeFunctionDialog',
  props: {
    isOpen: {
      type: Boolean,
      required: true
    },
    currentUserId: {
      type: Number,
      required: true
    }
  },
  emits: ['close', 'composite-function-created', 'error'],
  data() {
    return {
      availableFunctionNames: [],
      baseFunctionName: '',
      outerFunctionName: '',
      customFunctionName: '',
      localErrorMessage: '',
      loadingFunctions: false
    };
  },
  computed: {
    canCreate() {
      return this.baseFunctionName &&
             this.outerFunctionName &&
             this.customFunctionName &&
             this.customFunctionName.trim() !== '';
    }
  },
  watch: {
    isOpen(newVal) {
      if (newVal) {
        this.loadFunctionNames();
      }
    }
  },
  methods: {
    async loadFunctionNames() {
      this.loadingFunctions = true;
      this.localErrorMessage = '';
      try {
        // Получаем имена функций с сервера
        const names = await api.getAvailableMathFunctionNames();
        this.availableFunctionNames = names;
      } catch (error) {
        console.error('Ошибка при загрузке имён функций:', error);
        this.localErrorMessage = `Ошибка загрузки функций: ${error.message}`;
      } finally {
        this.loadingFunctions = false;
      }
    },
    async createCompositeFunction() {
      if (!this.canCreate) {
        this.localErrorMessage = 'Пожалуйста, заполните все поля корректно.';
        return;
      }
      try {
        const compositeDto = {
          baseFunctionName: this.baseFunctionName,
          outerFunctionName: this.outerFunctionName,
          customName: this.customFunctionName.trim(),
          userId: this.currentUserId
        };
        console.log('Отправка DTO для создания сложной функции:', compositeDto);

        // Создаем сложную функцию
        const createdFunction = await api.createCompositeFunction(compositeDto);
        console.log('Сложная функция создана:', createdFunction);

        // Эмитим событие о создании сложной функции
        this.$emit('composite-function-created', createdFunction);

        // Закрываем диалог
        this.closeDialog();

        // Показываем сообщение об успехе
        alert(`Сложная функция "${createdFunction.displayName}" успешно создана!`);
      } catch (error) {
        console.error('Ошибка при создании сложной функции:', error);
        this.localErrorMessage = `Ошибка при создании функции: ${error.message}`;
        this.$emit('error', error.message);
      }
    },
    closeDialog() {
      this.baseFunctionName = '';
      this.outerFunctionName = '';
      this.customFunctionName = '';
      this.localErrorMessage = '';
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
.input-section {
  margin-bottom: 15px;
}
.input-section label {
  display: block;
  margin-bottom: 5px;
}
.input-section select,
.input-section input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.preview-section {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
  border-left: 3px solid #2196F3;
}
.button-group {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}
.create-btn {
  background-color: #4CAF50;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.create-btn:hover:not(:disabled) {
  background-color: #45a049;
}
.create-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
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
.error-display {
  color: #f44336;
  background-color: #ffebee;
  padding: 10px;
  border-radius: 4px;
  margin-top: 10px;
}
</style>