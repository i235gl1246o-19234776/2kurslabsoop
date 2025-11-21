<!-- src/main/frontend/src/components/ui/CreateTabulatedFunctionFromMathDialog.vue -->
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
        <label for="countInput">Количество точек:</label>
        <input
          id="countInput"
          v-model.number="count"
          type="number"
          min="2"
          placeholder="Введите количество точек (>= 2)"
          :disabled="!selectedFunctionName"
        />
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

      <!-- Отображение ошибки, если она есть (локально в диалоге, если нужно) -->
      <div v-if="localErrorMessage" class="error-display">
        <p>{{ localErrorMessage }}</p>
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
  emits: ['close', 'function-created', 'error'], // Добавлено 'error' в emits
  data() {
    return {
      availableFunctionNames: [],
      selectedFunctionName: '',
      xFrom: null,
      xTo: null,
      count: 10,
      localErrorMessage: '', // Локальное сообщение об ошибке для валидации ввода
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
        this.count >= 2
      );
    }
  },
  mounted() {
    console.log('🟢 Компонент смонтирован. Вызываем loadFunctionNames...');
    this.loadFunctionNames();
  },
  methods: {
    resetForm() {
      this.selectedFunctionName = '';
      this.xFrom = null;
      this.xTo = null;
      this.count = 10;
      this.localErrorMessage = '';
    },
    async loadFunctionNames() {
      console.log('🔥🔥🔥 loadFunctionNames СРАБОТАЛ! 🔥🔥🔥');
      this.loadingFunctions = true;
      this.localErrorMessage = ''; // Исправлено: было errorMessage
      try {
        console.log('loadFunctionNames: вызываем api.getAvailableMathFunctionNames...'); // <-- Отладка
        // Вызовем новый API метод для получения имён функций
        const names = await api.getAvailableMathFunctionNames(); // <- Теперь используем реальный API
        console.log('loadFunctionNames: полученные имена:', names); // <-- Отладка
        this.availableFunctionNames = names;
        console.log('loadFunctionNames: availableFunctionNames установлен в', this.availableFunctionNames); // <-- Отладка

      } catch (error) {
        console.error('loadFunctionNames: Ошибка при загрузке имён функций:', error);
        this.localErrorMessage = `Ошибка загрузки функций: ${error.message}`;
        // Оставляем диалог открытым
      } finally {
        console.log('loadFunctionNames: finally, сбрасываем loadingFunctions'); // <-- Отладка
        this.loadingFunctions = false;
      }
    },
    async createFunction() {
      if (!this.isFormValid) {
        this.localErrorMessage = 'Пожалуйста, заполните все поля корректно.';
        return;
      }

      try {
        const creationDto = {
          mathFunctionName: this.selectedFunctionName,
          xFrom: this.xFrom,
          xTo: this.xTo,
          count: this.count,
          userId: this.currentUserId
        };

        console.log("Отправляем DTO на бэкенд:", creationDto);

        // Вызовем API для создания функции из MathFunction
        const createdFunction = await api.createFunctionFromMath(creationDto);

        console.log('Функция из MathFunction создана на бэкенде:', createdFunction);

        // Эмитим событие с созданным объектом функции
        this.$emit('function-created', createdFunction);

        // Закрываем диалог
        this.closeDialog();

        // Показываем сообщение об успехе (можно через alert или передать в App.vue)
        alert(`Функция "${createdFunction.functionName}" (ID: ${createdFunction.id}) успешно создана из ${this.selectedFunctionName}!`);

      } catch (error) {
        console.error('Ошибка при создании функции из MathFunction:', error);
        // Передаём ошибку родительскому компоненту (App.vue) для отображения через ErrorModal
        this.$emit('error', error.message); // Новое событие 'error'
        // Оставляем диалог открытым, чтобы пользователь мог увидеть ошибку
        // alert(`Ошибка при создании функции: ${error.message}`); // Убираем alert
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
/* Стили аналогичны CreateTabulatedFunctionDialog.vue */
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
</style>