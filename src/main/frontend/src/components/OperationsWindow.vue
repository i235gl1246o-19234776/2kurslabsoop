<template>
  <Teleport to="body">
    <div v-if="isOpen" class="modal-overlay" @click="closeDialog">
      <div class="modal-content" @click.stop style="width: 90%; max-width: 1200px; height: 80vh; max-height: 800px; display: flex; flex-direction: column;">
        <h2>Операции над функциями</h2>
        <div class="operations-buttons">
          <button @click="performOperation('add')" :disabled="!operandsReady || loadingOperation" class="op-btn">Сложение (+)</button>
          <button @click="performOperation('subtract')" :disabled="!operandsReady || loadingOperation" class="op-btn">Вычитание (-)</button>
          <button @click="performOperation('multiply')" :disabled="!operandsReady || loadingOperation" class="op-btn">Умножение (*)</button>
          <button @click="performOperation('divide')" :disabled="!operandsReady || loadingOperation" class="op-btn">Деление (/)</button>
          <span v-if="loadingOperation" class="loading-text">Выполняется операция...</span>
        </div>
        <div class="functions-container">
          <FunctionSection
            title="Операнд 1"
            :function-data="operand1"
            :is-result="false"
            :current-user-id="currentUserId"
            @function-loaded="handleOperandLoaded(1, $event)"
            @function-cleared="handleOperandCleared(1)"
            @error="handleError"
          />
          <div class="action-buttons">
            <button v-if="canInsert(1)" @click="openInsertDialog(1)" class="action-btn insert-btn">Вставка точки</button>
            <button v-if="canRemove(1)" @click="openRemoveDialog(1)" class="action-btn remove-btn">Удалить точку</button>
          </div>
          <FunctionSection
            title="Операнд 2"
            :function-data="operand2"
            :is-result="false"
            :current-user-id="currentUserId"
            @function-loaded="handleOperandLoaded(2, $event)"
            @function-cleared="handleOperandCleared(2)"
            @error="handleError"
          />
          <div class="action-buttons">
            <button v-if="canInsert(2)" @click="openInsertDialog(2)" class="action-btn insert-btn">Вставка точки</button>
            <button v-if="canRemove(2)" @click="openRemoveDialog(2)" class="action-btn remove-btn">Удалить точку</button>
          </div>
          <FunctionSection
            title="Результат"
            :function-data="result"
            :is-result="true"
            :current-user-id="currentUserId"
            @error="handleError"
          />
          <div class="calculate-section" v-if="result">
            <label for="xInput">X:</label>
            <input id="xInput" v-model.number="xValue" type="number" step="any" @keyup.enter="calculateY" />
            <button @click="calculateY">Вычислить</button>
            <span v-if="calculatedY !== null" class="result">f({{ xValue }}) = {{ calculatedY.toFixed(6) }}</span>
          </div>
        </div>
        <div class="button-group">
          <button @click="closeDialog" class="cancel-btn">Закрыть</button>
        </div>
        <!-- Диалоги вставки и удаления -->
        <InsertPointDialog
          v-if="activeOperand !== null && canInsert(activeOperand)"
          :is-open="isInsertDialogOpen"
          @close="closeInsertDialog"
          @point-inserted="insertPoint"
        />
        <RemovePointDialog
          v-if="activeOperand !== null && canRemove(activeOperand)"
          :is-open="isRemoveDialogOpen"
          :point-count="getPointCount(activeOperand)"
          :function-points="getFunctionPoints(activeOperand)"
          @close="closeRemoveDialog"
          @point-removed="removePoint"
        />
      </div>
    </div>
  </Teleport>
</template>

<script>
import { Teleport } from 'vue';
import FunctionSection from './FunctionSection.vue';
import InsertPointDialog from './InsertPointDialog.vue';
import RemovePointDialog from './RemovePointDialog.vue';
import * as api from '@/api.js';

export default {
  name: 'OperationsWindow',
  components: {
    Teleport,
    FunctionSection,
    InsertPointDialog,
    RemovePointDialog
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
  emits: ['close', 'error'],
  data() {
    return {
      operand1: null,
      operand2: null,
      result: null,
      loadingOperation: false,
      isInsertDialogOpen: false,
      isRemoveDialogOpen: false,
      activeOperand: null, // 1 или 2 для определения, с какой функцией работаем
      xValue: 0,
      calculatedY: null
    };
  },
  computed: {
    operandsReady() {
      const operand1Valid = this.operand1 &&
                             this.operand1.id &&
                             typeof this.operand1.id === 'number' &&
                             this.operand1.points &&
                             this.operand1.points.length >= 2;

      const operand2Valid = this.operand2 &&
                             this.operand2.id &&
                             typeof this.operand2.id === 'number' &&
                             this.operand2.points &&
                             this.operand2.points.length >= 2;

      return operand1Valid && operand2Valid;
    },
  },
  methods: {
    closeDialog() {
      this.operand1 = null;
      this.operand2 = null;
      this.result = null;
      this.loadingOperation = false;
      this.$emit('close');
    },
    handleOperandCleared(operandNum) {
      if (operandNum === 1) {
        this.operand1 = null;
      } else if (operandNum === 2) {
        this.operand2 = null;
      }
      this.result = null;
    },
    async handleOperandLoaded(operandNum, fullFunctionData) {
      if (!fullFunctionData || !fullFunctionData.id || !fullFunctionData.points) {
        this.handleError('Получены некорректные данные функции.');
        return;
      }

      // Проверка количества точек
      if (fullFunctionData.points.length < 2) {
        this.handleError('Функция должна содержать как минимум 2 точки для выполнения операций.');
        return;
      }

      // Проверка корректности точек
      const hasInvalidPoints = fullFunctionData.points.some(p =>
        typeof p.x !== 'number' || typeof p.y !== 'number' ||
        isNaN(p.x) || isNaN(p.y) ||
        !isFinite(p.x) || !isFinite(p.y)
      );

      if (hasInvalidPoints) {
        this.handleError('Один из операндов содержит недопустимые значения (NaN/Infinity).');
        return;
      }

      // Проверка сортировки точек по X
      for (let i = 1; i < fullFunctionData.points.length; i++) {
        if (fullFunctionData.points[i].x <= fullFunctionData.points[i-1].x) {
          this.handleError('Точки функции должны быть упорядочены по возрастанию X.');
          return;
        }
      }

      if (operandNum === 1) {
        this.operand1 = fullFunctionData;
      } else if (operandNum === 2) {
        this.operand2 = fullFunctionData;
      }

      this.result = null;
    },
    async performOperation(operation) {
      if (!this.operandsReady || this.loadingOperation) return;

      const hasInvalidPoints = (points) => points.some(p =>
        typeof p.x !== 'number' || typeof p.y !== 'number' ||
        isNaN(p.x) || isNaN(p.y) ||
        !isFinite(p.x) || !isFinite(p.y)
      );

      if (hasInvalidPoints(this.operand1.points) || hasInvalidPoints(this.operand2.points)) {
        this.handleError("Один из операндов содержит недопустимые значения (NaN/Infinity).");
        return;
      }

      // Проверка на соответствие X-координат
      if (this.operand1.points.length !== this.operand2.points.length) {
        this.handleError(`Количество точек в операндах не совпадает: ${this.operand1.points.length} vs ${this.operand2.points.length}`);
        return;
      }

      for (let i = 0; i < this.operand1.points.length; i++) {
        const x1 = this.operand1.points[i].x;
        const x2 = this.operand2.points[i].x;
        if (Math.abs(x1 - x2) > 1e-9) {
          this.handleError(`X-координаты не совпадают в точке ${i}: ${x1} и ${x2}`);
          return;
        }
      }

      this.loadingOperation = true;
      try {
        const factoryType = localStorage.getItem('selectedTabulatedFunctionFactory') || 'array';
        let resultPoints;

        switch (operation) {
          case 'add':
            resultPoints = await api.performAddition(this.operand1.id, this.operand2.id, factoryType);
            break;
          case 'subtract':
            resultPoints = await api.performSubtraction(this.operand1.id, this.operand2.id, factoryType);
            break;
          case 'multiply':
            resultPoints = await api.performMultiplication(this.operand1.id, this.operand2.id, factoryType);
            break;
          case 'divide':
            resultPoints = await api.performDivision(this.operand1.id, this.operand2.id, factoryType);
            break;
          default:
            throw new Error('Неизвестная операция');
        }

        // Проверка результата
        const hasInvalidResultPoints = resultPoints.some(p => {
          const x = p.x !== undefined ? p.x : (p.xVal !== undefined ? p.xVal : undefined);
          const y = p.y !== undefined ? p.y : (p.yVal !== undefined ? p.yVal : undefined);
          return x === undefined || y === undefined ||
                 typeof x !== 'number' || typeof y !== 'number' ||
                 isNaN(x) || isNaN(y) ||
                 !isFinite(x) || !isFinite(y);
        });

        if (hasInvalidResultPoints) {
          const invalidPoints = resultPoints.filter(p => {
            const x = p.x !== undefined ? p.x : (p.xVal !== undefined ? p.xVal : 'undefined');
            const y = p.y !== undefined ? p.y : (p.yVal !== undefined ? p.yVal : 'undefined');
            return x === 'undefined' || y === 'undefined';
          });

          throw new Error(`Результат операции содержит недопустимые значения. Некорректные точки: ${JSON.stringify(invalidPoints)}`);
        }

        this.result = {
          id: null,
          name: `${this.operand1.name} ${operation} ${this.operand2.name}`,
          points: resultPoints.map(p => ({
            x: p.x !== undefined ? p.x : (p.xVal !== undefined ? p.xVal : 0),
            y: p.y !== undefined ? p.y : (p.yVal !== undefined ? p.yVal : 0)
          }))
        };
      } catch (err) {
        console.error(`Ошибка при выполнении операции ${operation}:`, err);
        this.handleError(`Ошибка при выполнении операции ${operation}: ${err.message}`);
        this.result = null;
      } finally {
        this.loadingOperation = false;
      }
    },

    // Методы для работы с Insertable/Removable
    canInsert(operandNum) {
      const functionData = operandNum === 1 ? this.operand1 : this.operand2;
      return functionData &&
             functionData.implementationType &&
             ['array', 'linkedlist'].includes(functionData.implementationType.toLowerCase());
    },
    canRemove(operandNum) {
      const functionData = operandNum === 1 ? this.operand1 : this.operand2;
      return functionData &&
             functionData.implementationType &&
             ['array', 'linkedlist'].includes(functionData.implementationType.toLowerCase());
    },
    openInsertDialog(operandNum) {
      this.activeOperand = operandNum;
      this.isInsertDialogOpen = true;
    },
    closeInsertDialog() {
      this.isInsertDialogOpen = false;
      this.activeOperand = null;
    },
    openRemoveDialog(operandNum) {
      this.activeOperand = operandNum;
      this.isRemoveDialogOpen = true;
    },
    closeRemoveDialog() {
      this.isRemoveDialogOpen = false;
      this.activeOperand = null;
    },
    getPointCount(operandNum) {
      const functionData = operandNum === 1 ? this.operand1 : this.operand2;
      return functionData ? functionData.points.length : 0;
    },
    getFunctionPoints(operandNum) {
      const functionData = operandNum === 1 ? this.operand1 : this.operand2;
      return functionData ? functionData.points : [];
    },
    insertPoint({ x, y }) {
      if (!this.activeOperand) return;
      const functionData = this.activeOperand === 1 ? this.operand1 : this.operand2;
      if (!functionData || !functionData.points) return;

      // Создаем новую точку
      const newPoint = { x, y };
      // Вставляем точку в правильную позицию
      const updatedPoints = [...functionData.points];

      // Находим позицию для вставки
      let insertIndex = 0;
      while (insertIndex < updatedPoints.length && updatedPoints[insertIndex].x < x) {
        insertIndex++;
      }

      // Вставляем точку
      updatedPoints.splice(insertIndex, 0, newPoint);

      // Обновляем данные функции
      const updatedFunctionData = {
        ...functionData,
        points: updatedPoints
      };

      // Обновляем соответствующий операнд
      if (this.activeOperand === 1) {
        this.operand1 = updatedFunctionData;
      } else {
        this.operand2 = updatedFunctionData;
      }

      this.closeInsertDialog();
    },
    removePoint(index) {
      if (!this.activeOperand) return;
      const functionData = this.activeOperand === 1 ? this.operand1 : this.operand2;
      if (!functionData || !functionData.points ||
          index < 0 || index >= functionData.points.length) return;

      // Создаем копию массива без удаляемой точки
      const updatedPoints = [...functionData.points];
      updatedPoints.splice(index, 1);

      // Обновляем данные функции
      const updatedFunctionData = {
        ...functionData,
        points: updatedPoints
      };

      // Обновляем соответствующий операнд
      if (this.activeOperand === 1) {
        this.operand1 = updatedFunctionData;
      } else {
        this.operand2 = updatedFunctionData;
      }

      this.closeRemoveDialog();
    },

    // Методы для вычисления значения в точке
    calculateY() {
      if (!this.result || !this.result.points || this.result.points.length < 2) {
        this.handleError('Результат не загружен или содержит недостаточно точек');
        return;
      }

      // Простая интерполяция
      const x = this.xValue;
      const points = this.result.points;

      if (x < points[0].x) {
        // Экстраполяция влево
        const dx = points[1].x - points[0].x;
        const dy = points[1].y - points[0].y;
        this.calculatedY = points[0].y + (x - points[0].x) * dy / dx;
      } else if (x > points[points.length - 1].x) {
        // Экстраполяция вправо
        const lastIndex = points.length - 1;
        const dx = points[lastIndex].x - points[lastIndex - 1].x;
        const dy = points[lastIndex].y - points[lastIndex - 1].y;
        this.calculatedY = points[lastIndex].y + (x - points[lastIndex].x) * dy / dx;
      } else {
        // Интерполяция между точками
        for (let i = 0; i < points.length - 1; i++) {
          if (x >= points[i].x && x <= points[i + 1].x) {
            const t = (x - points[i].x) / (points[i + 1].x - points[i].x);
            this.calculatedY = points[i].y + t * (points[i + 1].y - points[i].y);
            break;
          }
        }
      }
    },

    handleError(message) {
      console.error('OperationsWindow error:', message);
      this.$emit('error', message);
    }
  },
  mounted() {
    console.log('OperationsWindow: Компонент смонтирован');
  },
  beforeUnmount() {
    console.log('OperationsWindow: Компонент будет уничтожен');
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
  overflow: hidden;
}
.operations-buttons {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
}
.op-btn {
  padding: 10px 16px;
  margin: 0 5px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
}
.op-btn:hover:not(:disabled) {
  background-color: #45a049;
}
.op-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.loading-text {
  color: #2196F3;
  font-style: italic;
  font-weight: bold;
  margin-left: 10px;
}
.functions-container {
  display: flex;
  gap: 15px;
  flex: 1;
  overflow: hidden;
  min-height: 400px;
}
.functions-container > div {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 10px 0;
}
.action-btn {
  padding: 6px 12px;
  font-size: 0.9em;
  cursor: pointer;
  border: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}
.insert-btn {
  background-color: #2196F3;
  color: white;
}
.insert-btn:hover {
  background-color: #0b7dda;
}
.remove-btn {
  background-color: #f44336;
  color: white;
}
.remove-btn:hover {
  background-color: #da190b;
}
.calculate-section {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
.calculate-section input {
  width: 100px;
  padding: 4px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.calculate-section button {
  padding: 4px 8px;
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.result {
  margin-left: 10px;
  font-weight: bold;
  color: #4CAF50;
}
.button-group {
  margin-top: 15px;
  display: flex;
  justify-content: center;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
.cancel-btn {
  padding: 10px 20px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
}
.cancel-btn:hover {
  background-color: #da190b;
}
</style>