<template>
  <div class="graph-section">
    <h3>{{ title }}</h3>
    <div class="function-info">
      <span v-if="functionData && functionData.name">{{ functionData.name }} (ID: {{ functionData.id }})</span>
      <span v-else>Функция не загружена</span>
    </div>
    <div class="graph-container">
      <canvas ref="graphCanvas" width="600" height="400"></canvas>
      <div v-if="showWarning" class="warning-overlay">
        <p>Требуется минимум 3 точки для отображения графика</p>
      </div>
    </div>
    <div class="function-actions">
      <button @click="openCreateFromXYDialog" class="action-btn">Создать (X/Y)</button>
      <button @click="openCreateFromMathDialog" class="action-btn">Создать (Math)</button>
      <button @click="openLoadDialog" class="action-btn">Загрузить</button>
      <button @click="saveFunction" :disabled="!functionData || isSaving" class="action-btn">
        {{ isSaving ? 'Сохранение...' : 'Сохранить' }}
      </button>
      <button @click="clearFunction" class="action-btn">Очистить</button>
      <div v-if="functionData" class="calculate-section">
        <label for="xInput">X:</label>
        <input id="xInput" v-model.number="xValue" type="number" step="any" @keyup.enter="calculateY" />
        <button @click="calculateY">Вычислить</button>
        <span v-if="calculatedY !== null" class="result">f({{ xValue }}) = {{ calculatedY.toFixed(6) }}</span>
      </div>
      <button v-if="canInsert" @click="openInsertDialog" class="action-btn insert-btn">Вставка точки</button>
      <button v-if="canRemove" @click="openRemoveDialog" class="action-btn remove-btn">Удалить точку</button>
    </div>
  </div>
</template>

<script>
import { Chart, registerables } from 'chart.js';
import CreateTabulatedFunctionDialog from './CreateTabulatedFunctionDialog.vue';
import CreateTabulatedFunctionFromMathDialog from './CreateTabulatedFunctionFromMathDialog.vue';
import LoadTabulatedFunctionDialog from './LoadTabulatedFunctionDialog.vue';
import InsertPointDialog from './InsertPointDialog.vue';
import RemovePointDialog from './RemovePointDialog.vue';
import * as api from '@/api.js';

Chart.register(...registerables);

export default {
  name: 'FunctionSection',
  components: {
    CreateTabulatedFunctionDialog,
    CreateTabulatedFunctionFromMathDialog,
    LoadTabulatedFunctionDialog,
    InsertPointDialog,
    RemovePointDialog
  },
  props: {
    title: {
      type: String,
      required: true
    },
    functionData: {
      type: Object,
      default: null
    },
    isResult: {
      type: Boolean,
      required: true
    },
    currentUserId: {
      type: Number,
      required: true
    }
  },
  emits: ['function-loaded', 'function-cleared', 'error'],
  data() {
    return {
      chart: null,
      isCreateFromXYDialogOpen: false,
      isCreateFromMathDialogOpen: false,
      isLoadDialogOpen: false,
      isInsertDialogOpen: false,
      isRemoveDialogOpen: false,
      isSaving: false,
      xValue: 0,
      calculatedY: null,
      chartInitialized: false,
      showWarning: false,
      isHandlingError: false
    };
  },
  computed: {
    canInsert() {
      return this.functionData &&
             this.functionData.implementationType &&
             ['array', 'linkedlist'].includes(this.functionData.implementationType.toLowerCase());
    },
    canRemove() {
      return this.functionData &&
             this.functionData.implementationType &&
             ['array', 'linkedlist'].includes(this.functionData.implementationType.toLowerCase());
    },
    canRenderChart() {
      return this.functionData &&
             this.functionData.points &&
             this.functionData.points.length >= 3;
    }
  },
  watch: {
    functionData: {
      handler(newVal) {
        // Защита от рекурсивных обновлений
        if (this.isHandlingError) return;

        this.$nextTick(() => {
          if (this.canRenderChart) {
            this.renderChart();
            this.showWarning = false;
          } else if (newVal) {
            this.showWarning = true;
            this.clearChart();
          }
        });
        this.calculatedY = null;
      },
      deep: true
    }
  },
  mounted() {
    this.initializeChart();
  },
  beforeUnmount() {
    this.destroyChart();
  },
  methods: {
    initializeChart() {
      if (!this.$refs.graphCanvas || this.chartInitialized) return;

      try {
        const ctx = this.$refs.graphCanvas.getContext('2d');
        this.chart = new Chart(ctx, {
          type: 'line',
          data: {
            datasets: [{
              label: 'Функция',
              data: [],
              borderColor: 'rgb(75, 192, 192)',
              tension: 0.1,
              fill: false,
              pointRadius: 3
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                display: true,
                position: 'top'
              },
              tooltip: {
                enabled: true
              }
            },
            scales: {
              x: {
                title: {
                  display: true,
                  text: 'X'
                },
                grid: {
                  color: 'rgba(0, 0, 0, 0.1)'
                }
              },
              y: {
                title: {
                  display: true,
                  text: 'Y'
                },
                grid: {
                  color: 'rgba(0, 0, 0, 0.1)'
                }
              }
            }
          }
        });
        this.chartInitialized = true;

        // Если данные уже загружены, отображаем их
        if (this.canRenderChart) {
          this.renderChart();
        }
      } catch (error) {
        console.error('Ошибка инициализации графика:', error);
        this.handleError('Не удалось создать график. Пожалуйста, попробуйте перезагрузить страницу.');
      }
    },

    destroyChart() {
      if (this.chart) {
        try {
          this.chart.destroy();
        } catch (error) {
          console.error('Ошибка уничтожения графика:', error);
        } finally {
          this.chart = null;
          this.chartInitialized = false;
        }
      }
    },

    clearChart() {
      if (!this.chart || !this.chartInitialized) return;

      try {
        this.chart.data.datasets[0].data = [];
        this.chart.data.datasets[0].label = 'Функция';
        this.chart.update();
      } catch (error) {
        console.error('Ошибка очистки графика:', error);
      }
    },

    renderChart() {
      // Проверяем, что компонент еще существует и canvas не уничтожен
      if (!this.$el || !this.$refs.graphCanvas || !this.chart || !this.canRenderChart) {
        return;
      }

      try {
        // Сортируем точки по X
        const sortedPoints = [...this.functionData.points].sort((a, b) => a.x - b.x);

        // Обновляем данные графика
        this.chart.data.datasets[0].data = sortedPoints.map(point => ({
          x: point.x,
          y: point.y
        }));
        this.chart.data.datasets[0].label = this.functionData.name || 'Функция';
        this.chart.update();
      } catch (error) {
        console.error('Ошибка обновления графика:', error);
        this.handleError('Не удалось обновить график. Попробуйте перезагрузить страницу.');
      }
    },

    calculateY() {
      if (!this.functionData || !this.functionData.points || this.functionData.points.length < 2) {
        this.handleError('Функция не загружена или содержит недостаточно точек');
        return;
      }

      const x = this.xValue;
      const points = this.functionData.points;

      // Проверка на корректность точек
      const hasInvalidPoints = points.some(point =>
        typeof point.x !== 'number' ||
        typeof point.y !== 'number' ||
        isNaN(point.x) ||
        isNaN(point.y) ||
        !isFinite(point.x) ||
        !isFinite(point.y)
      );

      if (hasInvalidPoints) {
        this.handleError('Функция содержит некорректные значения. Проверьте данные.');
        return;
      }

      // Простая интерполяция для демонстрации
      if (x < points[0].x) {
        const dx = points[1].x - points[0].x;
        const dy = points[1].y - points[0].y;
        this.calculatedY = points[0].y + (x - points[0].x) * dy / dx;
      } else if (x > points[points.length - 1].x) {
        const lastIndex = points.length - 1;
        const dx = points[lastIndex].x - points[lastIndex - 1].x;
        const dy = points[lastIndex].y - points[lastIndex - 1].y;
        this.calculatedY = points[lastIndex].y + (x - points[lastIndex].x) * dy / dx;
      } else {
        for (let i = 0; i < points.length - 1; i++) {
          if (x >= points[i].x && x <= points[i + 1].x) {
            const t = (x - points[i].x) / (points[i + 1].x - points[i].x);
            this.calculatedY = points[i].y + t * (points[i + 1].y - points[i].y);
            break;
          }
        }
      }
    },

    openCreateFromXYDialog() {
      this.isCreateFromXYDialogOpen = true;
    },

    closeCreateFromXYDialog() {
      this.isCreateFromXYDialogOpen = false;
    },

    openCreateFromMathDialog() {
      this.isCreateFromMathDialogOpen = true;
    },

    closeCreateFromMathDialog() {
      this.isCreateFromMathDialogOpen = false;
    },

    openLoadDialog() {
      this.isLoadDialogOpen = true;
    },

    closeLoadDialog() {
      this.isLoadDialogOpen = false;
    },

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

    async onFunctionCreated({ functionId, functionName }) {
      console.log('FunctionSection: onFunctionCreated вызван с ID:', functionId, 'и именем:', functionName);
      this.closeCreateFromXYDialog();
      this.closeCreateFromMathDialog();

      if (isNaN(functionId)) {
        this.handleError(`Неверный ID функции: ${functionId}. Ожидалось число.`);
        return;
      }

      await this.loadFunctionData(Number(functionId), functionName);
    },

    async onFunctionLoaded({ functionId, functionName }) {
      console.log('FunctionSection: onFunctionLoaded вызван с ID:', functionId, 'и именем:', functionName);
      this.closeLoadDialog();

      if (isNaN(functionId)) {
        this.handleError(`Неверный ID функции: ${functionId}. Ожидалось число.`);
        return;
      }

      await this.loadFunctionData(Number(functionId), functionName);
    },

    async loadFunctionData(functionId, functionName) {
      console.log('FunctionSection: loadFunctionData вызван с ID:', functionId, 'и именем:', functionName);

      if (typeof functionId !== 'number' || isNaN(functionId) || functionId <= 0) {
        this.handleError(`Неверный ID функции: ${functionId}. Ожидалось положительное число.`);
        return;
      }

      try {
        const points = await api.getAllPointsByFunctionId(functionId);

        // Проверяем каждую точку на корректность
        const validPoints = [];
        let hasInvalid = false;

        for (let i = 0; i < points.length; i++) {
          const point = points[i];
          const x = Number(point.xVal !== undefined ? point.xVal : point.x);
          const y = Number(point.yVal !== undefined ? point.yVal : point.y);

          if (isNaN(x) || isNaN(y) || !isFinite(x) || !isFinite(y)) {
            console.warn(`Точка ${i} содержит некорректные значения: x=${point.xVal}, y=${point.yVal}`);
            hasInvalid = true;
            continue;
          }

          validPoints.push({ x, y });
        }

        if (hasInvalid) {
          this.handleError("Функция содержит некорректные значения (NaN/Infinity). Некорректные точки были проигнорированы.");
        }

        // Проверяем, что точки упорядочены по возрастанию X
        for (let i = 1; i < validPoints.length; i++) {
          if (validPoints[i].x <= validPoints[i-1].x) {
            // Сортируем точки по X
            validPoints.sort((a, b) => a.x - b.x);
            this.handleError("Точки функции были автоматически отсортированы по возрастанию X.");
            break;
          }
        }

        if (validPoints.length === 0) {
          this.handleError("Функция не содержит корректных точек.");
          return;
        }

        // Проверяем минимальное количество точек
        if (validPoints.length < 3) {
          this.showWarning = true;
          this.handleError(`Для отображения графика необходимо минимум 3 точки. Текущее количество: ${validPoints.length}`);
        }

        // Определяем реализацию функции
        let implementationType = 'array';
        if (localStorage.getItem('selectedTabulatedFunctionFactory')) {
          implementationType = localStorage.getItem('selectedTabulatedFunctionFactory');
        }

        const fullFunctionData = {
          id: functionId,
          name: functionName,
          points: validPoints,
          implementationType: implementationType
        };

        this.$emit('function-loaded', fullFunctionData);
      } catch (err) {
        console.error('FunctionSection: Ошибка при загрузке точек функции:', err);
        this.handleError(`Ошибка при загрузке точек функции: ${err.message || err.toString()}`);
      }
    },

    async saveFunction() {
      if (!this.functionData || this.functionData.points.length === 0) {
        this.handleError('Нет данных для сохранения');
        return;
      }

      // Проверяем, что точки упорядочены и не содержат некорректных значений
      const hasInvalidPoints = this.functionData.points.some(point =>
        typeof point.x !== 'number' ||
        typeof point.y !== 'number' ||
        isNaN(point.x) ||
        isNaN(point.y) ||
        !isFinite(point.x) ||
        !isFinite(point.y)
      );

      if (hasInvalidPoints) {
        this.handleError('Функция содержит некорректные значения. Проверьте данные.');
        return;
      }

      // Сортируем точки по X
      const sortedPoints = [...this.functionData.points].sort((a, b) => a.x - b.x);

      // Проверяем, что точки упорядочены по возрастанию X
      for (let i = 1; i < sortedPoints.length; i++) {
        if (sortedPoints[i].x <= sortedPoints[i-1].x) {
          this.handleError('Точки функции должны быть упорядочены по возрастанию X.');
          return;
        }
      }

      this.isSaving = true;

      try {
        const factoryType = localStorage.getItem('selectedTabulatedFunctionFactory') || 'array';

        if (this.functionData.id) {
          console.log(`Обновление существующей функции с ID: ${this.functionData.id}`);
          await api.deleteAllPointsByFunctionId(this.functionData.id);

          const xValues = sortedPoints.map(p => p.x);
          const yValues = sortedPoints.map(p => p.y);

          await api.createTabulatedPoints(this.functionData.id, xValues, yValues);
          console.log(`Точки обновлены для функции с ID: ${this.functionData.id}`);
          this.$emit('error', `Функция "${this.functionData.name}" успешно обновлена!`);
        } else {
          console.log('Создание новой функции');
          const functionDto = {
            userId: this.currentUserId,
            typeFunction: 'tabular',
            functionName: this.functionData.name || `Function_${Date.now()}`,
            functionExpression: null,
            factoryType: factoryType
          };

          const createdFunction = await api.createFunction(functionDto);

          if (!createdFunction || !createdFunction.id) {
            throw new Error('Сервер не вернул корректный ID функции');
          }

          const newFunctionId = createdFunction.id;
          console.log(`Создана новая функция с ID: ${newFunctionId}`);

          const xValues = sortedPoints.map(p => p.x);
          const yValues = sortedPoints.map(p => p.y);

          await api.createTabulatedPoints(newFunctionId, xValues, yValues);
          console.log(`Точки созданы для функции с ID: ${newFunctionId}`);

          this.functionData.id = newFunctionId;
          this.functionData.name = createdFunction.functionName;

          this.$emit('function-loaded', {
            ...this.functionData,
            id: newFunctionId,
            name: createdFunction.functionName
          });

          alert(`Функция "${createdFunction.functionName}" (ID: ${newFunctionId}) успешно создана и сохранена!`);
        }
      } catch (error) {
        console.error('Ошибка при сохранении функции:', error);
        this.handleError(`Ошибка при сохранении функции: ${error.message || error.toString()}`);
      } finally {
        this.isSaving = false;
      }
    },

    clearFunction() {
      console.log('FunctionSection: вызван clearFunction');
      this.destroyChart();
      this.$emit('function-cleared');
      this.calculatedY = null;
      this.xValue = 0;
      this.showWarning = false;

      // Пересоздаем график
      this.$nextTick(() => {
        this.initializeChart();
      });
    },

    insertPoint({ x, y }) {
      if (!this.functionData || !this.functionData.points) return;

      if (typeof x !== 'number' || typeof y !== 'number' || isNaN(x) || isNaN(y) || !isFinite(x) || !isFinite(y)) {
        this.handleError('Попытка вставить некорректные значения точки.');
        return;
      }

      // Создаем новую точку и вставляем ее в правильную позицию
      const newPoint = { x, y };
      const updatedPoints = [...this.functionData.points, newPoint];

      // Сортируем точки по X
      updatedPoints.sort((a, b) => a.x - b.x);

      const updatedFunctionData = {
        ...this.functionData,
        points: updatedPoints
      };

      this.$emit('function-loaded', updatedFunctionData);
      this.closeInsertDialog();
    },

    removePoint(index) {
      if (!this.functionData || !this.functionData.points || index < 0 || index >= this.functionData.points.length) return;

      // Создаем копию массива без удаляемой точки
      const updatedPoints = [...this.functionData.points];
      updatedPoints.splice(index, 1);

      const updatedFunctionData = {
        ...this.functionData,
        points: updatedPoints
      };

      this.$emit('function-loaded', updatedFunctionData);
      this.closeRemoveDialog();
    },

    handleError(message) {
      // Защита от рекурсивного вызова ошибки
      if (this.isHandlingError) return;

      this.isHandlingError = true;
      console.error('FunctionSection error:', message);
      this.$emit('error', message);

      // Сбрасываем флаг обработки ошибки через короткую задержку
      setTimeout(() => {
        this.isHandlingError = false;
      }, 100);
    }
  }
};
</script>

<style scoped>
.graph-section {
  border: 1px solid #ccc;
  border-radius: 4px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.function-info {
  margin-bottom: 10px;
  font-weight: bold;
  color: #333;
}
.graph-container {
  flex: 1;
  min-height: 300px;
  margin-bottom: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f9f9f9;
  border: 1px solid #ddd;
  border-radius: 4px;
  position: relative;
}
.warning-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(255, 240, 240, 0.9);
  color: #f44336;
  font-weight: bold;
  text-align: center;
  padding: 20px;
  z-index: 10;
  border: 2px dashed #f44336;
}
.function-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 5px 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 10px;
}
.calculate-section {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
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
.action-btn {
  padding: 6px 12px;
  font-size: 0.9em;
  cursor: pointer;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}
.action-btn:hover {
  background-color: #45a049;
}
.action-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}
.insert-btn {
  background-color: #2196F3;
}
.insert-btn:hover {
  background-color: #0b7dda;
}
.remove-btn {
  background-color: #f44336;
}
.remove-btn:hover {
  background-color: #da190b;
}
</style>