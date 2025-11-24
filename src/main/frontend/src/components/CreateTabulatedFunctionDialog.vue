<template>
  <div v-if="isOpen" class="modal-overlay" @click="closeDialog">
    <div class="modal-content" @click.stop>
      <h2>Создать табулированную функцию</h2>

      <div class="input-section">
        <label for="pointCount">Количество точек:</label>
        <input
          id="pointCount"
          v-model.number="pointCountInput"
          type="number"
          min="2"
          :max="maxPoints"
          placeholder="Введите количество точек (2-1000)"
        />
        <button @click="generateTable" :disabled="isTableGenerated || !isValidCount">
          Сгенерировать таблицу
        </button>
        <!-- Отображение сообщения при превышении лимита -->
        <p v-if="pointCountInput > maxPoints" class="error-message">
          Введено слишком большое значение. Максимум: {{ maxPoints }}.
        </p>
      </div>

      <div v-if="isTableGenerated" class="table-section">
        <h3>Введите значения X и Y:</h3>
        <table class="points-table">
          <thead>
            <tr>
              <th>Индекс</th>
              <th>X</th>
              <th>Y</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(point, index) in points" :key="index">
              <td>{{ index }}</td>
              <td>
                <input
                  v-model.number="point.x"
                  type="number"
                  step="any"
                  :placeholder="`X${index}`"
                />
              </td>
              <td>
                <input
                  v-model.number="point.y"
                  type="number"
                  step="any"
                  :placeholder="`Y${index}`"
                />
              </td>
            </tr>
          </tbody>
        </table>
        <div class="button-group">
          <!-- Кнопка "Создать" -->
          <button @click="createFunction" :disabled="isCreating" class="create-btn">
            {{ isCreating ? 'Создание...' : 'Создать' }}
          </button>
          <!-- Кнопка "Отмена" -->
          <button @click="closeDialog" class="cancel-btn">Отмена</button>
        </div>
      </div>

      <!-- Отображение ошибки, если она есть -->
      <div v-if="errorMessage" class="error-display">
        <p>{{ errorMessage }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import * as api from '@/api.js'; // Импортируем api для вызова сервера

export default {
  name: 'CreateTabulatedFunctionDialog',
  props: {
    isOpen: {
      type: Boolean,
      required: true
    },
    currentUserId: { // Добавляем пропс для ID пользователя
      type: Number,
      required: true
    }
  },
  emits: ['close', 'function-created', 'error'], // Добавляем 'error' в emits
  data() {
    return {
      pointCountInput: 5, // Значение по умолчанию
      maxPoints: 1000, // Максимальное количество точек
      points: [], // Массив для хранения точек {x, y}
      errorMessage: '',
      isCreating: false // Флаг для состояния создания
    };
  },
  computed: {
    isValidCount() {
      // Проверяем, что введенное значение - число и в допустимом диапазоне
      return Number.isInteger(this.pointCountInput) && this.pointCountInput >= 2 && this.pointCountInput <= this.maxPoints;
    },
    isTableGenerated() {
      return this.points.length > 0;
    }
  },
  watch: {
    // Сбрасываем ошибки при изменении ввода
    pointCountInput() {
      this.errorMessage = '';
    },
    // Сбрасываем таблицу, если окно закрывается
    isOpen(isOpen) {
      if (!isOpen) {
        this.points = [];
        this.isCreating = false; // Сбрасываем флаг при закрытии
      }
    }
  },
  methods: {
    generateTable() {
      if (!this.isValidCount) {
        this.errorMessage = 'Пожалуйста, введите корректное количество точек (от 2 до 1000).';
        return;
      }
      // Создаем массив объектов {x: null, y: null} для ввода
      this.points = Array.from({ length: this.pointCountInput }, () => ({ x: null, y: null }));
      this.errorMessage = ''; // Сбрасываем ошибку при успешной генерации
    },
    closeDialog() {
      // Сбрасываем состояние при закрытии
      this.points = [];
      this.pointCountInput = 5;
      this.errorMessage = '';
      this.isCreating = false;
      this.$emit('close');
    },
    validatePoints() {
      // Проверяем, что все поля заполнены и X - числа
      for (let i = 0; i < this.points.length; i++) {
        const point = this.points[i];
        if (typeof point.x !== 'number' || typeof point.y !== 'number' || isNaN(point.x) || isNaN(point.y)) {
          this.errorMessage = `Пожалуйста, заполните все поля X и Y корректными числами. Ошибка в строке ${i}.`;
          return false;
        }
      }
      // Проверяем, что X идут по возрастанию (требование для TabulatedFunction)
      for (let i = 1; i < this.points.length; i++) {
        if (this.points[i].x <= this.points[i - 1].x) {
          this.errorMessage = `Значения X должны идти по возрастанию. Проверьте строки ${i-1} и ${i}.`;
          return false;
        }
      }
      return true;
    },
    async createFunction() {
      if (this.isCreating) return; // Предотвращаем повторный клик

      if (!this.validatePoints()) {
        return; // Если валидация не прошла, не создаем функцию
      }

      this.isCreating = true; // Устанавливаем флаг создания
      this.errorMessage = ''; // Сбрасываем предыдущую ошибку

      // Подготовим массивы x и y
      const xValues = this.points.map(p => p.x);
      const yValues = this.points.map(p => p.y);

      try {
        // 1. Подготовить DTO для создания функции (без точек)
        const functionDto = {
          userId: this.currentUserId, // Используем переданный ID пользователя
          typeFunction: 'tabular',
          functionName: `TabulatedFunction_${Date.now()}`, // Сгенерировать имя
          functionExpression: null,
          // --- ДОБАВЛЕНО: factoryType из localStorage ---
          factoryType: localStorage.getItem('selectedTabulatedFunctionFactory') || 'array'
          // ---
        };

        console.log("Отправляем DTO на бэкенд (X/Y):", functionDto);

        // 2. Вызвать API для создания функции (это создаст FunctionEntity)
        // api.createFunction уже возвращает JSON при успехе и бросает ошибку при неудаче
        const createdFunctionData = await api.createFunction(functionDto); // <-- Присваиваем JSON напрямую

        const newFunctionId = createdFunctionData.id; // <-- Работаем с JSON
        // Проверяем, что сервер вернул объект с id
        if (typeof newFunctionId !== 'number') {
            throw new Error(`Сервер вернул некорректный ID функции (X/Y): ${newFunctionId}`);
        }

        // 3. Вызвать API для создания точек
        await api.createTabulatedPoints(newFunctionId, xValues, yValues); // <-- Вызов метода для создания точек

        console.log(`Функция с ID ${newFunctionId} и точками успешно создана.`);

        // 4. Испустить событие с ID и именем
        this.$emit('function-created', {
            functionId: newFunctionId, // <-- Прямое соответствие
            functionName: createdFunctionData.functionName
        });

        // 5. Закрыть диалог
        this.closeDialog();

        // 6. Показать сообщение об успехе
        alert(`Функция "${createdFunctionData.functionName}" (ID: ${newFunctionId}) успешно создана из X/Y!`);

      } catch (error) {
        console.error('Ошибка при создании функции из X/Y:', error);
        // Передаём ошибку родительскому компоненту (App.vue или FunctionSection.vue) для отображения через ErrorModal
        this.$emit('error', error.message);
        // Не закрываем диалог, чтобы пользователь видел ошибку
        this.errorMessage = `Ошибка при создании функции: ${error.message}`;
      } finally {
        this.isCreating = false; // Сбрасываем флаг в любом случае
      }
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
  background-color: rgba(0, 0, 0, 0.5); /* Полупрозрачный фон */
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

.input-section input {
  width: 100%;
  padding: 8px;
  margin-bottom: 10px;
  box-sizing: border-box;
}

.table-section {
  margin-top: 20px;
}

.points-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.points-table, .points-table th, .points-table td {
  border: 1px solid #ccc;
}

.points-table th, .points-table td {
  padding: 8px;
  text-align: center;
}

.points-table input {
  width: 100%;
  padding: 4px;
  box-sizing: border-box;
}

.button-group {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
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

.error-message {
  color: #f44336;
  font-size: 0.9em;
  margin-top: 5px;
}

.error-display {
  color: #f44336;
  background-color: #ffebee;
  padding: 10px;
  border-radius: 4px;
  margin-top: 10px;
}
</style>