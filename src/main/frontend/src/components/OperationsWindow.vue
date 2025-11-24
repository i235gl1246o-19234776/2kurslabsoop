<template>
  <div v-if="show" class="operations-window">
    <div class="window-header">
      <h2>Операции над функциями</h2>
      <button class="close-button" @click="$emit('close')">&times;</button>
    </div>

    <div class="operations-container">
      <!-- Первая функция -->
      <div class="function-section">
        <h3>Функция 1</h3>
        <div class="function-controls">
          <button @click="openCreateDialog('first')">Создать</button>
          <button @click="openLoadDialog('first')">Загрузить</button>
          <button @click="saveFunction('first')" :disabled="!firstFunction">Сохранить</button>
          <button @click="clearFunction('first')">Очистить</button>
        </div>

        <div v-if="firstFunction" class="function-details">
          <p><strong>Имя:</strong> {{ firstFunction.name }}</p>
          <p><strong>ID:</strong> {{ firstFunction.id }}</p>
          <p><strong>Точек:</strong> {{ firstFunction.points ? firstFunction.points.length : 0 }}</p>
        </div>

        <FunctionTable
          v-if="firstFunction && firstFunction.points"
          :points="firstFunction.points"
          :editable="true"
          @points-updated="updateFirstFunctionPoints"
        />
      </div>

      <!-- Вторая функция -->
      <div class="function-section">
        <h3>Функция 2</h3>
        <div class="function-controls">
          <button @click="openCreateDialog('second')">Создать</button>
          <button @click="openLoadDialog('second')">Загрузить</button>
          <button @click="saveFunction('second')" :disabled="!secondFunction">Сохранить</button>
          <button @click="clearFunction('second')">Очистить</button>
        </div>

        <div v-if="secondFunction" class="function-details">
          <p><strong>Имя:</strong> {{ secondFunction.name }}</p>
          <p><strong>ID:</strong> {{ secondFunction.id }}</p>
          <p><strong>Точек:</strong> {{ secondFunction.points ? secondFunction.points.length : 0 }}</p>
        </div>

        <FunctionTable
          v-if="secondFunction && secondFunction.points"
          :points="secondFunction.points"
          :editable="true"
          @points-updated="updateSecondFunctionPoints"
        />
      </div>

      <!-- Результат -->
      <div class="function-section result-section">
        <h3>Результат</h3>
        <div class="operation-controls">
          <button @click="addFunctions" :disabled="!canOperate">+ Сложить</button>
          <button @click="subtractFunctions" :disabled="!canOperate">- Вычесть</button>
          <button @click="multiplyFunctions" :disabled="!canOperate">× Умножить</button>
          <button @click="divideFunctions" :disabled="!canOperate">÷ Разделить</button>
          <button @click="saveResult" :disabled="!resultFunction">Сохранить результат</button>
          <button @click="clearResult">Очистить результат</button>
        </div>

        <div v-if="resultFunction" class="function-details">
          <p><strong>Имя:</strong> {{ resultFunction.name }}</p>
          <p><strong>Точек:</strong> {{ resultFunction.points ? resultFunction.points.length : 0 }}</p>
        </div>

        <FunctionTable
          v-if="resultFunction && resultFunction.points"
          :points="resultFunction.points"
          :editable="false"
        />
      </div>
    </div>

    <!-- Диалоги -->
    <CreateTabulatedFunctionDialog
      :is-open="showCreateDialog"
      :current-user-id="currentUserId"
      @close="closeCreateDialog"
      @function-created="onFunctionCreated"
      @error="handleError"
    />

    <CreateTabulatedFunctionFromMathDialog
      :is-open="showCreateMathDialog"
      :current-user-id="currentUserId"
      @close="closeCreateMathDialog"
      @function-created="onFunctionCreated"
      @error="handleError"
    />

    <LoadFunctionDialog
      :is-open="showLoadDialog"
      :current-user-id="currentUserId"
      @close="closeLoadDialog"
      @function-loaded="onFunctionLoaded"
      @error="handleError"
    />

    <div v-if="operationError" class="error-message">
      {{ operationError }}
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import { api } from '../api.js';
import CreateTabulatedFunctionDialog from './CreateTabulatedFunctionDialog.vue';
import CreateTabulatedFunctionFromMathDialog from './CreateTabulatedFunctionFromMathDialog.vue';
import LoadFunctionDialog from './LoadFunctionDialog.vue';
import FunctionTable from './FunctionTable.vue';

export default {
  name: 'OperationsWindow',
  components: {
    CreateTabulatedFunctionDialog,
    CreateTabulatedFunctionFromMathDialog,
    LoadFunctionDialog,
    FunctionTable
  },
  props: {
    show: Boolean,
    currentUserId: {
      type: Number,
      required: true
    }
  },
  emits: ['close', 'error'],

  setup(props, { emit }) {
    // Состояние функций
    const firstFunction = ref(null);
    const secondFunction = ref(null);
    const resultFunction = ref(null);

    // Состояние диалогов
    const showCreateDialog = ref(false);
    const showCreateMathDialog = ref(false);
    const showLoadDialog = ref(false);
    const currentTarget = ref('');

    // Ошибки
    const operationError = ref('');

    const canOperate = computed(() => {
      return firstFunction.value &&
             secondFunction.value &&
             firstFunction.value.points &&
             secondFunction.value.points &&
             firstFunction.value.points.length > 0 &&
             secondFunction.value.points.length > 0;
    });

    const openCreateDialog = (target) => {
      currentTarget.value = target;
      showCreateDialog.value = true;
    };

    const openCreateMathDialog = (target) => {
      currentTarget.value = target;
      showCreateMathDialog.value = true;
    };

    const openLoadDialog = (target) => {
      currentTarget.value = target;
      showLoadDialog.value = true;
    };

    const closeCreateDialog = () => {
      showCreateDialog.value = false;
      currentTarget.value = '';
    };

    const closeCreateMathDialog = () => {
      showCreateMathDialog.value = false;
      currentTarget.value = '';
    };

    const closeLoadDialog = () => {
      showLoadDialog.value = false;
      currentTarget.value = '';
    };

    const onFunctionCreated = async ({ functionId, functionName }) => {
      try {
        await loadFunctionData(functionId, functionName, currentTarget.value);
        closeCreateDialog();
        closeCreateMathDialog();
      } catch (error) {
        handleError(error.message);
      }
    };

    const onFunctionLoaded = async ({ functionId, functionName }) => {
      try {
        await loadFunctionData(functionId, functionName, currentTarget.value);
        closeLoadDialog();
      } catch (error) {
        handleError(error.message);
      }
    };

    const loadFunctionData = async (functionId, functionName, target) => {
      try {
        const pointsResponse = await api.getTabulatedPointsByFunctionId(functionId);
        const points = pointsResponse.points || pointsResponse;

        const functionData = {
          id: functionId,
          name: functionName,
          points: points
        };

        if (target === 'first') {
          firstFunction.value = functionData;
        } else if (target === 'second') {
          secondFunction.value = functionData;
        }
      } catch (error) {
        throw new Error(`Ошибка загрузки функции: ${error.message}`);
      }
    };

    const updateFirstFunctionPoints = (points) => {
      if (firstFunction.value) {
        firstFunction.value.points = points;
      }
    };

    const updateSecondFunctionPoints = (points) => {
      if (secondFunction.value) {
        secondFunction.value.points = points;
      }
    };

    const addFunctions = () => {
      if (!canOperate.value) return;

      try {
        const resultPoints = performOperation('add');
        resultFunction.value = {
          name: `(${firstFunction.value.name} + ${secondFunction.value.name})`,
          points: resultPoints
        };
        operationError.value = '';
      } catch (error) {
        operationError.value = `Ошибка сложения: ${error.message}`;
      }
    };

    const subtractFunctions = () => {
      if (!canOperate.value) return;

      try {
        const resultPoints = performOperation('subtract');
        resultFunction.value = {
          name: `(${firstFunction.value.name} - ${secondFunction.value.name})`,
          points: resultPoints
        };
        operationError.value = '';
      } catch (error) {
        operationError.value = `Ошибка вычитания: ${error.message}`;
      }
    };

    const multiplyFunctions = () => {
      if (!canOperate.value) return;

      try {
        const resultPoints = performOperation('multiply');
        resultFunction.value = {
          name: `(${firstFunction.value.name} × ${secondFunction.value.name})`,
          points: resultPoints
        };
        operationError.value = '';
      } catch (error) {
        operationError.value = `Ошибка умножения: ${error.message}`;
      }
    };

    const divideFunctions = () => {
      if (!canOperate.value) return;

      try {
        const resultPoints = performOperation('divide');
        resultFunction.value = {
          name: `(${firstFunction.value.name} ÷ ${secondFunction.value.name})`,
          points: resultPoints
        };
        operationError.value = '';
      } catch (error) {
        operationError.value = `Ошибка деления: ${error.message}`;
      }
    };

    const performOperation = (operation) => {
      const points1 = firstFunction.value.points;
      const points2 = secondFunction.value.points;

      // Простая реализация операций (в реальном приложении нужно согласовать домены)
      const minLength = Math.min(points1.length, points2.length);
      const result = [];

      for (let i = 0; i < minLength; i++) {
        const x = points1[i].x;
        let y;

        switch (operation) {
          case 'add':
            y = points1[i].y + points2[i].y;
            break;
          case 'subtract':
            y = points1[i].y - points2[i].y;
            break;
          case 'multiply':
            y = points1[i].y * points2[i].y;
            break;
          case 'divide':
            if (points2[i].y === 0) {
              throw new Error('Деление на ноль');
            }
            y = points1[i].y / points2[i].y;
            break;
          default:
            y = points1[i].y;
        }

        result.push({ x, y });
      }

      return result;
    };

    const saveFunction = async (target) => {
      // Реализация сохранения функции
      alert(`Функция ${target} будет сохранена`);
    };

    const saveResult = async () => {
      if (!resultFunction.value) return;

      try {
        const functionDto = {
          userId: props.currentUserId,
          typeFunction: 'tabular',
          functionName: resultFunction.value.name,
          functionExpression: null,
          factoryType: localStorage.getItem('selectedTabulatedFunctionFactory') || 'array'
        };

        const createdFunction = await api.createFunction(functionDto);
        await api.createTabulatedPoints(createdFunction.id,
          resultFunction.value.points.map(p => p.x),
          resultFunction.value.points.map(p => p.y)
        );

        alert(`Результат сохранен как функция "${createdFunction.functionName}"`);
      } catch (error) {
        handleError(`Ошибка сохранения результата: ${error.message}`);
      }
    };

    const clearFunction = (target) => {
      if (target === 'first') {
        firstFunction.value = null;
      } else if (target === 'second') {
        secondFunction.value = null;
      }
    };

    const clearResult = () => {
      resultFunction.value = null;
    };

    const handleError = (message) => {
      operationError.value = message;
      emit('error', message);
    };

    return {
      firstFunction,
      secondFunction,
      resultFunction,
      showCreateDialog,
      showCreateMathDialog,
      showLoadDialog,
      currentTarget,
      operationError,
      canOperate,
      openCreateDialog,
      openCreateMathDialog,
      openLoadDialog,
      closeCreateDialog,
      closeCreateMathDialog,
      closeLoadDialog,
      onFunctionCreated,
      onFunctionLoaded,
      updateFirstFunctionPoints,
      updateSecondFunctionPoints,
      addFunctions,
      subtractFunctions,
      multiplyFunctions,
      divideFunctions,
      saveFunction,
      saveResult,
      clearFunction,
      clearResult,
      handleError
    };
  }
};
</script>

<style scoped>
.operations-window {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: white;
  z-index: 1000;
  overflow-y: auto;
  padding: 20px;
}

.window-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.close-button {
  font-size: 24px;
  cursor: pointer;
  background: none;
  border: none;
  color: #666;
}

.operations-container {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.function-section {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  background-color: #f9f9f9;
}

.result-section {
  background-color: #f0f7ff;
  border-color: #3498db;
}

.function-controls {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.operation-controls {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.function-controls button,
.operation-controls button {
  padding: 8px 15px;
  background-color: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.function-controls button:hover,
.operation-controls button:hover {
  background-color: #1976d2;
}

.function-controls button:disabled,
.operation-controls button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.function-details {
  background-color: white;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
  border: 1px solid #ddd;
}

.error-message {
  color: #d32f2f;
  background-color: #ffebee;
  padding: 10px;
  border-radius: 4px;
  margin-top: 10px;
  border-left: 3px solid #d32f2f;
}

@media (max-width: 1200px) {
  .operations-container {
    grid-template-columns: 1fr;
  }
}
</style>