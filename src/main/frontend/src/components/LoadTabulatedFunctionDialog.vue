<template>
  <div v-if="isOpen" class="modal-overlay" @click="closeDialog">
    <div class="modal-content" @click.stop>
      <h2>Загрузить функцию</h2>

      <div class="input-section">
        <label for="functionSelect">Выберите функцию:</label>
        <select
          id="functionSelect"
          v-model="selectedFunctionId"
          :disabled="loadingFunctions"
        >
          <option value="" disabled>Выберите функцию...</option>
          <option
            v-for="func in availableFunctions"
            :key="func.functionId"
            :value="func.functionId"
          >
            {{ func.functionName }} (ID: {{ func.functionId }}) - {{ func.typeFunction === 'tabular' ? 'Табличная' : func.typeFunction }}
          </option>
        </select>
        <p v-if="loadingFunctions">Загрузка функций...</p>
        <p v-else-if="availableFunctions.length === 0">Функции не найдены.</p>
      </div>

      <div class="button-group">
        <button
          @click="loadFunction"
          :disabled="!selectedFunctionId || loadingFunctions"
          class="load-btn"
        >
          Загрузить
        </button>
        <button @click="closeDialog" class="cancel-btn">Отмена</button>
      </div>

      <div v-if="errorMessage" class="error-display">
        <p>{{ errorMessage }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { api } from '../api.js';

export default {
  name: 'LoadFunctionDialog',
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
  emits: ['close', 'function-loaded', 'error'],

  data() {
    return {
      availableFunctions: [],
      selectedFunctionId: '',
      errorMessage: '',
      loadingFunctions: false
    };
  },

  watch: {
    isOpen(newVal) {
      if (newVal) {
        this.loadAvailableFunctions();
      }
    }
  },

  methods: {
    async loadAvailableFunctions() {
      this.loadingFunctions = true;
      this.errorMessage = '';

      try {
        const functions = await api.getFunctionsByUserId(this.currentUserId);
        this.availableFunctions = functions;
      } catch (error) {
        console.error('Ошибка при загрузке функций:', error);
        this.errorMessage = `Ошибка загрузки функций: ${error.message}`;
      } finally {
        this.loadingFunctions = false;
      }
    },

    async loadFunction() {
      if (!this.selectedFunctionId) return;

      try {
        const selectedFunction = this.availableFunctions.find(
          func => func.functionId === this.selectedFunctionId
        );

        if (!selectedFunction) {
          throw new Error('Выбранная функция не найдена');
        }

        this.$emit('function-loaded', {
          functionId: selectedFunction.functionId,
          functionName: selectedFunction.functionName
        });

        this.closeDialog();
      } catch (error) {
        console.error('Ошибка при загрузке функции:', error);
        this.errorMessage = `Ошибка при загрузке функции: ${error.message}`;
        this.$emit('error', error.message);
      }
    },

    closeDialog() {
      this.selectedFunctionId = '';
      this.errorMessage = '';
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
  margin-top: 20px;
}

.load-btn {
  background-color: #4CAF50;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.load-btn:hover:not(:disabled) {
  background-color: #45a049;
}

.load-btn:disabled {
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