<template>
  <div v-if="isOpen" class="modal-overlay" @click="closeDialog">
    <div class="modal-content" @click.stop>
      <h2>Создать табулированную функцию из MathFunction</h2>
      <div class="input-section">
        <label for="mathFunctionSelect">Выберите функцию:</label>
        <select
          id="mathFunctionSelect"
          v-model="selectedFunctionName"
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
        <p v-if="loadingFunctions">Загрузка функций...</p>
        <p v-else-if="availableFunctionNames.length === 0">Функции не найдены.</p>
      </div>
      <div class="input-section">
        <label for="xFromInput">X от:</label>
        <input
          id="xFromInput"
          v-model.number="xFrom"
          type="number"
          step="any"
          placeholder="Введите X от"
          :disabled="!selectedFunctionName"
        />
      </div>
      <div class="input-section">
        <label for="xToInput">X до:</label>
        <input
          id="xToInput"
          v-model.number="xTo"
          type="number"
          step="any"
          placeholder="Введите X до"
          :disabled="!selectedFunctionName"
        />
      </div>
      <div class="input-section">
        <label for="countInput">Количество точек (мин. 3):</label>
        <input
          id="countInput"
          v-model.number="count"
          type="number"
          min="3"
          max="1000"
          placeholder="Введите количество точек (мин. 3)"
          :disabled="!selectedFunctionName"
        />
        <div v-if="count < 3 && count !== null" class="error-message">
          Количество точек должно быть не менее 3 для дифференцирования
        </div>
      </div>
      <div class="button-group">
        <button
          @click="createFunction"
          :disabled="!isFormValid || loadingFunctions"
          class="create-btn"
        >
          Создать
        </button>
        <button @click="closeDialog" class="cancel-btn">Отмена</button>
      </div>
      <!-- Отображение ошибки, если она есть -->
      <div v-if="errorMessage" class="error-display">
        <p>{{ errorMessage }}</p>
      </div>
    </div>
  </div>
</template>
<script>
import * as api from '@/api.js';
export default {
  name: 'CreateTabulatedFunctionFromMathDialog',
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
  emits: ['close', 'function-created', 'error'],
  data() {
    return {
      availableFunctionNames: [],
      selectedFunctionName: '',
      xFrom: null,
      xTo: null,
      count: 10, // Увеличено с 10 до минимума 3
      errorMessage: '',
      loadingFunctions: false
    };
  },
  computed: {
    isFormValid() {
      return (
        this.selectedFunctionName &&
        typeof this.xFrom === 'number' &&
        typeof this.xTo === 'number' &&
        typeof this.count === 'number' &&
        this.xFrom < this.xTo &&
        this.count >= 3 // Изменено с 2 на 3
      );
    }
  },
  watch: {
    isOpen(newVal) {
      if (newVal) {
        this.loadFunctionNames();
      }
    }
  },
  mounted() {
    console.log('🟢 Компонент смонтирован. Вызываем loadFunctionNames...');
    if (this.isOpen) {
      this.loadFunctionNames();
    }
  },
  methods: {
    async loadFunctionNames() {
      console.log('🔥🔥🔥 loadFunctionNames СРАБОТАЛ! 🔥🔥🔥');
      this.loadingFunctions = true;
      this.errorMessage = '';
      try {
        console.log('loadFunctionNames: вызываем api.getAvailableMathFunctionNames...');
        const names = await api.getAvailableMathFunctionNames();
        console.log('loadFunctionNames: полученные имена:', names);
        this.availableFunctionNames = names;
      } catch (error) {
        console.error('loadFunctionNames: Ошибка при загрузке имён функций:', error);
        this.errorMessage = `Ошибка загрузки функций: ${error.message}`;
        this.$emit('error', error.message);
      } finally {
        console.log('loadFunctionNames: finally, сбрасываем loadingFunctions');
        this.loadingFunctions = false;
      }
    },
    resetForm() {
      this.selectedFunctionName = '';
      this.xFrom = null;
      this.xTo = null;
      this.count = 10;
      this.errorMessage = '';
    },
    async createFunction() {
      if (!this.isFormValid) {
        this.errorMessage = 'Пожалуйста, заполните все поля корректно (минимум 3 точки).';
        return;
      }
      try {
        const factoryType = localStorage.getItem('selectedTabulatedFunctionFactory') || 'array';
        const creationDto = {
          mathFunctionName: this.selectedFunctionName,
          xFrom: this.xFrom,
          xTo: this.xTo,
          count: this.count,
          userId: this.currentUserId,
          factoryType: factoryType
        };
        console.log("Отправляем DTO на бэкенд:", creationDto);
        const createdFunction = await api.createFunctionFromMath(creationDto);
        console.log('Функция из MathFunction создана на бэкенде:', createdFunction);
        if (typeof createdFunction.id !== 'number') {
            throw new Error(`Сервер вернул некорректный ID функции: ${createdFunction.id}`);
        }
        this.$emit('function-created', {
          functionId: createdFunction.id,
          functionName: createdFunction.functionName
        });
        this.closeDialog();
        alert(`Функция "${createdFunction.functionName}" (ID: ${createdFunction.id}) успешно создана из ${this.selectedFunctionName}!`);
      } catch (error) {
        console.error('Ошибка при создании функции из MathFunction:', error);
        this.errorMessage = `Ошибка при создании функции: ${error.message}`;
        this.$emit('error', error.message);
      }
    },
    closeDialog() {
      this.resetForm();
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
.create-btn:hover {
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
.error-message {
  color: #f44336;
  font-size: 0.9em;
  margin-top: 5px;
}
</style>