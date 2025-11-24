<template>
  <Teleport to="body">
    <div v-if="isOpen" class="modal-overlay" @click="closeDialog">
      <div class="modal-content" @click.stop style="width: 60%; max-width: 800px;">
        <h2>Загрузить табулированную функцию</h2>
        <div class="input-section">
          <label for="functionSelect">Выберите функцию:</label>
          <select
            id="functionSelect"
            v-model="selectedFunctionId"
            :disabled="loadingFunctions || !availableFunctions.length"
          >
            <option value="" disabled>Загрузите список функций...</option>
            <option
              v-for="func in availableFunctions"
              :key="func.id"
              :value="func.id"
            >
              {{ func.functionName }} (ID: {{ func.id }})
            </option>
          </select>
          <button @click="loadFunctionsList" :disabled="loadingFunctions" class="load-list-btn">
            {{ loadingFunctions ? 'Загрузка...' : 'Обновить список' }}
          </button>
        </div>
        <div class="points-info" v-if="selectedFunction">
          <p>Количество точек: {{ selectedFunction.pointsCount }}</p>
          <p v-if="selectedFunction.pointsCount < 3" class="warning">
            ⚠️ Для отображения графика требуется минимум 3 точки
          </p>
        </div>
        <div class="button-group">
          <button @click="loadSelectedFunction" :disabled="!selectedFunctionId || loadingFunctions" class="load-btn">
            Загрузить
          </button>
          <button @click="closeDialog" class="cancel-btn">Отмена</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
<script>
import { Teleport } from 'vue';
import * as api from '@/api.js';

export default {
  name: 'LoadTabulatedFunctionDialog',
  components: {
    Teleport,
  },
  props: {
    isOpen: {
      type: Boolean,
      required: true,
    },
    currentUserId: {
      type: Number,
      required: true,
    }
  },
  emits: ['close', 'function-loaded', 'error'],
  data() {
    return {
      availableFunctions: [], // [{id: number, functionName: string}, ...]
      selectedFunctionId: null,
      loadingFunctions: false,
      selectedFunction: null
    };
  },
  watch: {
    // Загружаем список функций при открытии диалога
    isOpen(newVal) {
      if (newVal) {
        this.loadFunctionsList();
      } else {
        // Сбрасываем состояние при закрытии
        this.availableFunctions = [];
        this.selectedFunctionId = null;
        this.selectedFunction = null;
      }
    },
    selectedFunctionId(newVal) {
      if (newVal) {
        this.checkFunctionPoints(newVal);
      } else {
        this.selectedFunction = null;
      }
    }
  },
  methods: {
    async loadFunctionsList() {
      if (this.loadingFunctions) return; // Предотвращаем повторный вызов

      this.loadingFunctions = true;
      this.selectedFunctionId = null; // Сбрасываем выбор при обновлении списка
      this.selectedFunction = null;

      try {
        // Получаем уже распарсенные данные из API
        this.availableFunctions = await api.getAllFunctionsByUserId(this.currentUserId);
        console.log('Список функций для загрузки загружен:', this.availableFunctions);

        // Проверяем, что получили массив функций
        if (!Array.isArray(this.availableFunctions)) {
          throw new Error('Некорректный формат данных: ожидается массив функций');
        }

        // Добавляем информацию о количестве точек для каждой функции
        const promises = this.availableFunctions.map(async (func) => {
          try {
            const points = await api.getAllPointsByFunctionId(func.id);
            func.pointsCount = points.length;
          } catch (err) {
            console.warn(`Не удалось получить точки для функции с ID ${func.id}:`, err);
            func.pointsCount = 0;
          }
        });

        await Promise.all(promises);
      } catch (err) {
        console.error('Ошибка при загрузке списка функций для загрузки:', err);
        this.handleError(`Ошибка при загрузке списка функций: ${err.message}`);
        this.availableFunctions = []; // Очищаем список при ошибке
      } finally {
        this.loadingFunctions = false;
      }
    },

    async checkFunctionPoints(functionId) {
      try {
        const points = await api.getAllPointsByFunctionId(functionId);
        this.selectedFunction = {
          id: functionId,
          pointsCount: points.length
        };
      } catch (err) {
        console.warn(`Не удалось получить информацию о точках для функции с ID ${functionId}:`, err);
        this.selectedFunction = {
          id: functionId,
          pointsCount: 0
        };
      }
    },

    async loadSelectedFunction() {
      // Проверяем, что функция действительно выбрана
      if (!this.selectedFunctionId) {
        this.handleError('Функция не выбрана. Пожалуйста, выберите функцию из списка.');
        return;
      }

      // Проверяем количество точек
      if (this.selectedFunction && this.selectedFunction.pointsCount < 3) {
        if (!confirm(`Выбранная функция содержит всего ${this.selectedFunction.pointsCount} точек. ` +
            'Для отображения графика требуется минимум 3 точки. Хотите продолжить загрузку?')) {
          return;
        }
      }

      try {
        // Получаем данные функции
        const functionData = await api.getFunctionById(this.selectedFunctionId);
        console.log('Данные функции загружены:', functionData);

        // Эмитим событие с ID и именем функции
        this.$emit('function-loaded', {
          functionId: this.selectedFunctionId,
          functionName: functionData.functionName
        });

        // Закрываем диалог после успешной загрузки
        this.closeDialog();
      } catch (err) {
        console.error('Ошибка при загрузке выбранной функции:', err);
        this.handleError(`Ошибка при загрузке функции: ${err.message}`);
        // Оставляем диалог открытым, чтобы пользователь мог выбрать другую функцию или повторить попытку
      }
    },

    closeDialog() {
      // Сбрасываем состояния перед закрытием
      this.availableFunctions = [];
      this.selectedFunctionId = null;
      this.selectedFunction = null;
      this.$emit('close');
    },

    handleError(message) {
      console.error('LoadTabulatedFunctionDialog error:', message);
      this.$emit('error', message);
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
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  text-align: center;
}
.input-section {
  margin-bottom: 15px;
}
.input-section label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}
.input-section select {
  width: 100%;
  padding: 8px;
  margin-bottom: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.load-list-btn {
  padding: 8px 16px;
  cursor: pointer;
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 4px;
  margin-top: 5px;
}
.points-info {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  text-align: left;
}
.warning {
  color: #f44336;
  font-weight: bold;
}
.button-group {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 15px;
}
.load-btn, .cancel-btn {
  padding: 10px 20px;
  cursor: pointer;
  border: none;
  border-radius: 4px;
  font-weight: bold;
}
.load-btn {
  background-color: #4CAF50;
  color: white;
}
.cancel-btn {
  background-color: #f44336;
  color: white;
}
.load-btn:hover:not(:disabled),
.cancel-btn:hover {
  opacity: 0.9;
}
.load-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>