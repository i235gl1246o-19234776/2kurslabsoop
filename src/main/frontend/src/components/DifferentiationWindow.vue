<template>
  <Teleport to="body">
    <div v-if="isOpen" class="modal-overlay" @click="closeDialog">
      <div class="modal-content" @click.stop style="width: 90%; max-width: 1200px; height: 80vh; max-height: 800px; display: flex; flex-direction: column;">
        <h2>Дифференцирование функции</h2>
        <div class="functions-container">
          <div class="function-wrapper">
            <FunctionSection
              title="Исходная функция"
              :function-data="sourceFunction"
              :is-result="false"
              :current-user-id="currentUserId"
              @function-loaded="handleSourceFunctionLoaded"
              @function-cleared="handleSourceFunctionCleared"
              @error="handleError"
            />
            <div class="action-buttons">
              <button v-if="canInsert" @click="openInsertDialog" class="action-btn insert-btn">Вставка точки</button>
              <button v-if="canRemove" @click="openRemoveDialog" class="action-btn remove-btn">Удалить точку</button>
            </div>
          </div>
          <div class="function-wrapper">
            <FunctionSection
              title="Производная функция"
              :function-data="derivativeFunction"
              :is-result="true"
              :current-user-id="currentUserId"
              @error="handleError"
            />
            <div class="calculate-section" v-if="derivativeFunction">
              <label for="xInput">X:</label>
              <input id="xInput" v-model.number="xValue" type="number" step="any" @keyup.enter="calculateY" />
              <button @click="calculateY">Вычислить</button>
              <span v-if="calculatedY !== null" class="result">f'({{ xValue }}) = {{ calculatedY.toFixed(6) }}</span>
            </div>
          </div>
        </div>
        <div class="button-group">
          <button @click="differentiateFunction" :disabled="!canDifferentiate || isDifferentiating" class="action-btn">
            <span v-if="isDifferentiating">Вычисление...</span>
            <span v-else>Вычислить производную</span>
          </button>
          <button @click="saveResult" :disabled="!derivativeFunction || isSaving" class="save-btn">
            <span v-if="isSaving">Сохранение...</span>
            <span v-else>Сохранить результат</span>
          </button>
          <button @click="closeDialog" class="cancel-btn">Закрыть</button>
        </div>
        <!-- Диалоги вставки и удаления -->
        <InsertPointDialog
          v-if="canInsert"
          :is-open="isInsertDialogOpen"
          @close="closeInsertDialog"
          @point-inserted="insertPoint"
        />
        <RemovePointDialog
          v-if="canRemove"
          :is-open="isRemoveDialogOpen"
          :point-count="sourceFunction ? sourceFunction.points.length : 0"
          :function-points="sourceFunction ? sourceFunction.points : []"
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
  name: 'DifferentiationWindow',
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
      sourceFunction: null,
      derivativeFunction: null,
      isDifferentiating: false,
      isSaving: false,
      isInsertDialogOpen: false,
      isRemoveDialogOpen: false,
      xValue: 0,
      calculatedY: null
    };
  },
  computed: {
    canDifferentiate() {
      return this.sourceFunction &&
             this.sourceFunction.id &&
             this.sourceFunction.points &&
             this.sourceFunction.points.length >= 3;
    },
    canInsert() {
      return this.sourceFunction &&
             this.sourceFunction.implementationType &&
             ['array', 'linkedlist'].includes(this.sourceFunction.implementationType.toLowerCase());
    },
    canRemove() {
      return this.sourceFunction &&
             this.sourceFunction.implementationType &&
             ['array', 'linkedlist'].includes(this.sourceFunction.implementationType.toLowerCase());
    }
  },
  methods: {
    closeDialog() {
      this.sourceFunction = null;
      this.derivativeFunction = null;
      this.$emit('close');
    },
    handleSourceFunctionCleared() {
      this.sourceFunction = null;
      this.derivativeFunction = null;
    },
    handleSourceFunctionLoaded(fullFunctionData) {
      if (!fullFunctionData || !fullFunctionData.id || !fullFunctionData.points) {
        this.handleError('Получены некорректные данные функции. Попробуйте загрузить функцию заново.');
        return;
      }

      // Проверяем, что точки упорядочены по X
      const points = fullFunctionData.points;
      for (let i = 1; i < points.length; i++) {
        if (points[i].x <= points[i-1].x) {
          // Пытаемся исправить порядок точек
          const sortedPoints = [...points].sort((a, b) => a.x - b.x);
          fullFunctionData.points = sortedPoints;
          this.handleError('Точки функции были автоматически отсортированы по возрастанию X.');
        }
      }

      // Проверяем корректность точек
      const hasInvalidPoints = fullFunctionData.points.some(point =>
        typeof point.x !== 'number' ||
        typeof point.y !== 'number' ||
        isNaN(point.x) ||
        isNaN(point.y) ||
        !isFinite(point.x) ||
        !isFinite(point.y)
      );

      if (hasInvalidPoints) {
        this.handleError('Функция содержит некорректные значения. Проверьте данные функции.');
        return;
      }

      if (fullFunctionData.points.length < 3) {
        this.handleError('Для дифференцирования необходимо минимум 3 точки. Пожалуйста, добавьте точки или загрузите функцию с достаточным количеством точек.');
        return;
      }

      this.sourceFunction = fullFunctionData;
      this.derivativeFunction = null;
    },
    async differentiateFunction() {
      if (!this.canDifferentiate || this.isDifferentiating) return;
      if (!this.sourceFunction.id) {
        this.handleError('ID исходной функции не определен. Пожалуйста, загрузите функцию заново.');
        return;
      }
      if (this.sourceFunction.points.length < 3) {
        this.handleError('Для дифференцирования необходимо минимум 3 точки. Пожалуйста, добавьте точки или загрузите функцию с достаточным количеством точек.');
        return;
      }

      this.isDifferentiating = true;
      try {
        const factoryType = localStorage.getItem('selectedTabulatedFunctionFactory') || 'array';
        console.log('Отправляем запрос на дифференцирование:', {
          functionId: this.sourceFunction.id,
          factoryType
        });

        // Проверяем, что точки упорядочены по X
        const points = this.sourceFunction.points;
        for (let i = 1; i < points.length; i++) {
          if (points[i].x <= points[i-1].x) {
            // Сортируем точки по X
            const sortedPoints = [...points].sort((a, b) => a.x - b.x);
            this.sourceFunction.points = sortedPoints;
            this.handleError('Точки функции были автоматически отсортированы по возрастанию X.');
          }
        }

        const derivativePoints = await api.performDifferentiation(this.sourceFunction.id, factoryType);
        if (!derivativePoints || !Array.isArray(derivativePoints) || derivativePoints.length === 0) {
          throw new Error('Сервер вернул пустой результат дифференцирования.');
        }

        // Проверяем корректность точек в результате
        const hasInvalidResultPoints = derivativePoints.some(p => {
          const x = p.x !== undefined ? p.x : (p.xVal !== undefined ? p.xVal : undefined);
          const y = p.y !== undefined ? p.y : (p.yVal !== undefined ? p.yVal : undefined);
          return x === undefined || y === undefined ||
                 typeof x !== 'number' || typeof y !== 'number' ||
                 isNaN(x) || isNaN(y) ||
                 !isFinite(x) || !isFinite(y);
        });

        if (hasInvalidResultPoints) {
          const invalidPoints = derivativePoints.filter(p => {
            const x = p.x !== undefined ? p.x : (p.xVal !== undefined ? p.xVal : 'undefined');
            const y = p.y !== undefined ? p.y : (p.yVal !== undefined ? p.yVal : 'undefined');
            return x === 'undefined' || y === 'undefined';
          });
          throw new Error(`Результат содержит некорректные точки: ${JSON.stringify(invalidPoints)}`);
        }

        this.derivativeFunction = {
          id: null,
          name: `Производная (${this.sourceFunction.name})`,
          points: derivativePoints.map(p => ({
            x: p.x !== undefined ? p.x : (p.xVal !== undefined ? p.xVal : 0),
            y: p.y !== undefined ? p.y : (p.yVal !== undefined ? p.yVal : 0)
          }))
        };
        console.log('Дифференцирование успешно выполнено:', this.derivativeFunction);
      } catch (err) {
        console.error('Ошибка при вычислении производной:', err);
        let errorMessage = 'Произошла внутренняя ошибка. Пожалуйста, попробуйте позже.';
        if (err.response) {
          errorMessage = `Ошибка сервера (${err.response.status}): ${err.response.data?.message || 'Нет деталей'}`;
        } else if (err.message) {
          errorMessage = err.message;
        }
        this.handleError(
          `Ошибка при вычислении производной: ${errorMessage}.` +
          `
Возможно, функция содержит недостаточно точек для дифференцирования.` +
          `
Проверьте, что функция имеет минимум 3 точки.`
        );
      } finally {
        this.isDifferentiating = false;
      }
    },
    async saveResult() {
      if (!this.derivativeFunction || this.isSaving) return;
      this.isSaving = true;
      try {
        // Генерируем имя файла
        const timestamp = Date.now();
        const baseName = this.derivativeFunction.name || 'derivative';
        const safeName = baseName.replace(/[^a-z0-9]/gi, '_').toLowerCase();
        const fileName = `${safeName}_${timestamp}.json`;

        // Сохраняем результат
        const points = this.derivativeFunction.points.map(p => ({
          x: p.x,
          y: p.y
        }));
        const blob = new Blob([JSON.stringify(points, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();

        // Очистка
        setTimeout(() => {
          document.body.removeChild(a);
          URL.revokeObjectURL(url);
        }, 0);

        alert(`Производная успешно сохранена в файл ${fileName}`);
      } catch (err) {
        console.error('Ошибка при сохранении производной:', err);
        this.handleError(`Ошибка при сохранении производной: ${err.message}`);
      } finally {
        this.isSaving = false;
      }
    },
    // Методы для работы с Insertable/Removable
    openInsertDialog() {
      this.isInsertDialogOpen = true;
    },
    closeInsertDialog() {
      this.isInsertDialogOpen = false;
    },
    openRemoveDialog() {
      this.isRemoveDialogOpen = true;
    },
    closeRemoveDialog() {
      this.isRemoveDialogOpen = false;
    },
    insertPoint({ x, y }) {
      if (!this.sourceFunction || !this.sourceFunction.points) return;
      // Создаем новую точку и вставляем ее в правильную позицию
      const newPoint = { x, y };
      const updatedPoints = [...this.sourceFunction.points];
      updatedPoints.sort((a, b) => a.x - b.x);

      // Обновляем данные функции
      const updatedFunctionData = {
        ...this.sourceFunction,
        points: updatedPoints
      };
      this.sourceFunction = updatedFunctionData;
      this.closeInsertDialog();
    },
    removePoint(index) {
      if (!this.sourceFunction || !this.sourceFunction.points || index < 0 || index >= this.sourceFunction.points.length) return;
      // Создаем копию массива без удаляемой точки
      const updatedPoints = [...this.sourceFunction.points];
      updatedPoints.splice(index, 1);

      // Обновляем данные функции
      const updatedFunctionData = {
        ...this.sourceFunction,
        points: updatedPoints
      };
      this.sourceFunction = updatedFunctionData;
      this.closeRemoveDialog();
    },
    // Методы для вычисления значения в точке
    calculateY() {
      if (!this.derivativeFunction || !this.derivativeFunction.points || this.derivativeFunction.points.length < 2) {
        this.handleError('Производная функция не загружена или содержит недостаточно точек');
        return;
      }
      // Простая интерполяция для демонстрации
      const x = this.xValue;
      const points = this.derivativeFunction.points;
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
      console.error('DifferentiationWindow error:', message);
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
  overflow: hidden;
}
.functions-container {
  display: flex;
  gap: 20px;
  flex: 1;
  min-height: 400px;
  margin-bottom: 20px;
}
.function-wrapper {
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
  display: flex;
  gap: 10px;
  justify-content: center;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
.action-btn, .save-btn, .cancel-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}
.action-btn {
  background-color: #2196F3;
  color: white;
}
.action-btn:disabled {
  background-color: #bbdefb;
  cursor: not-allowed;
}
.save-btn {
  background-color: #4CAF50;
  color: white;
}
.cancel-btn {
  background-color: #f44336;
  color: white;
}
.action-btn:hover:not(:disabled),
.save-btn:hover:not(:disabled),
.cancel-btn:hover {
  opacity: 0.9;
}
</style>