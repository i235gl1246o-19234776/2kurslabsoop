<!-- src/components/OperationsWindow.vue -->
<template>
  <div class="operations-window">
    <div class="window-header">
      <h2>Операции над функциями</h2>
      <button class="close-button" @click="$emit('close')">&times;</button>
    </div>

    <!-- Вкладки для переключения между операциями и интерполяцией -->
    <div class="tabs-section">
      <div class="tabs">
        <button
          class="tab-button"
          :class="{ active: activeTab === 'operations' }"
          @click="activeTab = 'operations'"
        >
          Элементарные операции
        </button>
        <button
          class="tab-button"
          :class="{ active: activeTab === 'interpolation' }"
          @click="activeTab = 'interpolation'"
        >
          Интерполяция функций
        </button>
      </div>
    </div>

    <!-- Секция элементарных операций -->
    <div v-if="activeTab === 'operations'" class="operations-content">
      <div class="functions-container">
        <!-- Функция A -->
        <div class="function-section">
          <h3>Функция A</h3>
          <div class="function-controls">
            <button @click="createFunction('A')">Создать</button>
            <button @click="openFunctionSelector('A')">Загрузить</button>
            <button @click="saveFunctionPoints('A')" :disabled="!selectedFunctionA || !hasUnsavedChangesA">Сохранить изменения</button>
          </div>

          <div v-if="selectedFunctionA" class="function-details">
            <p><strong>Имя:</strong> {{ selectedFunctionA.functionName }}</p>
            <p><strong>ID:</strong> {{ selectedFunctionA.functionId }}</p>
            <p><strong>Точек:</strong> {{ functionAPoints.length }}</p>
            <p v-if="functionCompatibility.aError" class="error-message">{{ functionCompatibility.aError }}</p>
            <button @click="clearFunction('A')" class="clear-button">Очистить</button>
          </div>

          <!-- Таблица точек для функции A -->
          <div class="function-table">
            <h4>Точки функции</h4>
            <table>
              <thead>
                <tr>
                  <th>X</th>
                  <th>Y</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(point, index) in functionAPoints" :key="index">
                  <td>{{ getXValue(point, index) }}</td>
                  <td>
                    <input
                      type="number"
                      :value="getYValue(point, index)"
                      @input="event => handleYInput('A', index, event.target.value)"
                      @change="event => setYValue('A', index, parseFloat(event.target.value))"
                      class="point-y-input"
                      :disabled="!selectedFunctionA"
                    />
                  </td>
                </tr>
                <tr v-if="functionAPoints.length === 0 && !loadingPointsA">
                  <td colspan="2" class="empty-table">Нет точек для отображения</td>
                </tr>
              </tbody>
            </table>
            <div v-if="hasDuplicateX('A')" class="error-message">
              Ошибка: обнаружены дублирующиеся X-значения.
              Для корректной работы операций X-значения должны быть уникальными и упорядоченными.
            </div>
          </div>
        </div>

        <!-- Функция B -->
        <div class="function-section">
          <h3>Функция B</h3>
          <div class="function-controls">
            <button @click="createFunction('B')">Создать</button>
            <button @click="openFunctionSelector('B')">Загрузить</button>
            <button @click="saveFunctionPoints('B')" :disabled="!selectedFunctionB || !hasUnsavedChangesB">Сохранить изменения</button>
          </div>

          <div v-if="selectedFunctionB" class="function-details">
            <p><strong>Имя:</strong> {{ selectedFunctionB.functionName }}</p>
            <p><strong>ID:</strong> {{ selectedFunctionB.functionId }}</p>
            <p><strong>Точек:</strong> {{ functionBPoints.length }}</p>
            <p v-if="functionCompatibility.bError" class="error-message">{{ functionCompatibility.bError }}</p>
            <button @click="clearFunction('B')" class="clear-button">Очистить</button>
          </div>

          <!-- Таблица точек для функции B -->
          <div class="function-table">
            <h4>Точки функции</h4>
            <table>
              <thead>
                <tr>
                  <th>X</th>
                  <th>Y</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(point, index) in functionBPoints" :key="index">
                  <td>{{ getXValue(point, index) }}</td>
                  <td>
                    <input
                      type="number"
                      :value="getYValue(point, index)"
                      @input="event => handleYInput('B', index, event.target.value)"
                      @change="event => setYValue('B', index, parseFloat(event.target.value))"
                      class="point-y-input"
                      :disabled="!selectedFunctionB"
                    />
                  </td>
                </tr>
                <tr v-if="functionBPoints.length === 0 && !loadingPointsB">
                  <td colspan="2" class="empty-table">Нет точек для отображения</td>
                </tr>
              </tbody>
            </table>
            <div v-if="hasDuplicateX('B')" class="error-message">
              Ошибка: обнаружены дублирующиеся X-значения.
              Для корректной работы операций X-значения должны быть уникальными и упорядоченными.
            </div>
          </div>
        </div>
      </div>

      <div class="compatibility-warning" v-if="functionCompatibility.warning">
        <p class="warning-message">⚠️ {{ functionCompatibility.warning }}</p>
      </div>

      <div class="operations-section">
        <h3>Доступные операции</h3>
        <div class="operations-grid">
          <button
            @click="executeOperation('add')"
            :disabled="!canExecute || !isCompatible || hasDuplicateX('A') || hasDuplicateX('B')"
            class="operation-button add"
            :title="!isCompatible ? 'Функции несовместимы для операций' : ''"
          >
            Сложить (A + B)
          </button>
          <button
            @click="executeOperation('subtract')"
            :disabled="!canExecute || !isCompatible || hasDuplicateX('A') || hasDuplicateX('B')"
            class="operation-button subtract"
            :title="!isCompatible ? 'Функции несовместимы для операций' : ''"
          >
            Вычесть (A - B)
          </button>
          <button
            @click="executeOperation('multiply')"
            :disabled="!canExecute || !isCompatible || hasDuplicateX('A') || hasDuplicateX('B')"
            class="operation-button multiply"
            :title="!isCompatible ? 'Функции несовместимы для операций' : ''"
          >
            Умножить (A × B)
          </button>
          <button
            @click="executeOperation('divide')"
            :disabled="!canExecute || !isCompatible || hasDuplicateX('A') || hasDuplicateX('B')"
            class="operation-button divide"
            :title="!isCompatible ? 'Функции несовместимы для операций' : ''"
          >
            Разделить (A ÷ B)
          </button>
        </div>
        <p v-if="!isCompatible" class="compatibility-message">
          Для выполнения операций функции должны иметь одинаковое количество точек и совпадающие X-значения
        </p>
        <p v-if="hasDuplicateX('A') || hasDuplicateX('B')" class="compatibility-message" style="color: #d32f2f;">
          Операции невозможны из-за дублирующихся X-значений в таблицах функций
        </p>
      </div>
    </div>

    <!-- Секция интерполяции -->
    <div v-if="activeTab === 'interpolation'" class="interpolation-content">
      <div class="interpolation-controls">
        <div class="interpolation-source">
          <h3>Исходная функция</h3>
          <div class="function-controls">
            <button @click="createFunction('source')">Создать</button>
            <button @click="openFunctionSelector('source')">Загрузить</button>
            <button @click="saveFunctionPoints('source')" :disabled="!selectedSourceFunction || !hasUnsavedChangesSource">Сохранить изменения</button>
          </div>

          <div v-if="selectedSourceFunction" class="function-details">
            <p><strong>Имя:</strong> {{ selectedSourceFunction.functionName }}</p>
            <p><strong>ID:</strong> {{ selectedSourceFunction.functionId }}</p>
            <p><strong>Точек:</strong> {{ sourceFunctionPoints.length }}</p>
            <button @click="clearFunction('source')" class="clear-button">Очистить</button>
          </div>
        </div>

        <div class="interpolation-params">
          <h3>Параметры интерполяции</h3>
          <div class="param-group">
            <label>Метод интерполяции:</label>
            <select v-model="interpolationMethod">
              <option value="linear">Линейная</option>
              <option value="lagrange">Полином Лагранжа</option>
              <option value="newton">Полином Ньютона</option>
              <option value="cubic_spline">Кубический сплайн</option>
            </select>
          </div>

          <div class="param-group">
            <label>Диапазон интерполяции:</label>
            <div class="range-inputs">
              <input type="number" v-model="interpolationRange.start" placeholder="Начало" step="any">
              <span>до</span>
              <input type="number" v-model="interpolationRange.end" placeholder="Конец" step="any">
            </div>
          </div>

          <div class="param-group">
            <label>Количество точек:</label>
            <input type="number" v-model="interpolationPointsCount" min="2" max="1000">
          </div>

          <div class="param-group">
            <label>Шаг интерполяции:</label>
            <input type="number" v-model="interpolationStep" step="any" :disabled="interpolationPointsCount > 0">
            <span class="hint">или укажите количество точек</span>
          </div>

          <button
            @click="executeInterpolation"
            :disabled="!selectedSourceFunction || sourceFunctionPoints.length < 2"
            class="interpolation-button"
          >
            Выполнить интерполяцию
          </button>
        </div>
      </div>

      <div v-if="interpolationError" class="error-message interpolation-error">
        {{ interpolationError }}
      </div>
    </div>

    <!-- Таблица для результата -->
    <div class="result-section">
      <h3>Результат {{ activeTab === 'operations' ? 'операции' : 'интерполяции' }}</h3>

      <!-- График результата -->
      <div v-if="resultPoints.length > 0" class="result-chart">
        <canvas ref="chartCanvas" width="800" height="400"></canvas>
      </div>

      <div class="result-table">
        <table>
          <thead>
            <tr>
              <th>X</th>
              <th>Y</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(point, index) in resultPoints" :key="index">
              <td>{{ point.x.toFixed(4) }}</td>
              <td>{{ point.y.toFixed(6) }}</td>
            </tr>
            <tr v-if="resultPoints.length === 0">
              <td colspan="2" class="empty-table">Результат отсутствует</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="result-actions">
        <button @click="saveResult" class="save-button" :disabled="resultPoints.length === 0">
          Сохранить результат
        </button>
        <button @click="clearResult" class="clear-button">Очистить результат</button>
        <button v-if="resultPoints.length > 0" @click="exportToCSV" class="export-button">
          Экспорт в CSV
        </button>
      </div>
    </div>

    <!-- Модальное окно для выбора функции -->
    <div v-if="showFunctionSelector" class="modal-overlay" @click="closeFunctionSelector">
      <div class="function-selector-modal" @click.stop>
        <div class="modal-header">
          <h3>Выберите функцию для {{ getSelectorTargetName(selectorTarget) }}</h3>
          <button class="close-button" @click="closeFunctionSelector">&times;</button>
        </div>
        <div class="modal-body">
          <p v-if="loadingFunctions">Загрузка функций...</p>
          <p v-else-if="availableFunctions.length === 0">Нет доступных функций</p>
          <ul v-else class="functions-list">
            <li
              v-for="func in availableFunctions"
              :key="func.functionId"
              @click="selectFunction(func)"
              class="function-item"
            >
              <div>
                <strong>{{ func.functionName }}</strong>
                <span class="function-id">(ID: {{ func.functionId }})</span>
              </div>
              <div class="function-meta">
                <span>Точек: {{ func.pointCount || 0 }}</span>
                <span>Тип: {{ func.typeFunction === 'tabular' ? 'Табличная' : func.typeFunction }}</span>
              </div>
            </li>
          </ul>
        </div>
        <div class="modal-footer">
          <button @click="closeFunctionSelector" class="cancel-button">Отмена</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { api } from '../api.js';

// Основные ссылки
const activeTab = ref('operations');
const selectedFunctionA = ref(null);
const selectedFunctionB = ref(null);
const selectedSourceFunction = ref(null);
const functionAPoints = ref([]);
const functionBPoints = ref([]);
const sourceFunctionPoints = ref([]);
const resultPoints = ref([]);
const resultName = ref('');
const resultFunctionId = ref(null);
const resultOperationType = ref(null);

// Интерполяция
const interpolationMethod = ref('linear');
const interpolationRange = ref({ start: null, end: null });
const interpolationPointsCount = ref(50);
const interpolationStep = ref(null);
const interpolationError = ref('');

// График
const chartCanvas = ref(null);
let chartInstance = null;

// Остальные ссылки остаются без изменений
const availableFunctions = ref([]);
const showFunctionSelector = ref(false);
const selectorTarget = ref(null);
const loadingFunctions = ref(false);
const loadingPointsA = ref(false);
const loadingPointsB = ref(false);
const loadingSourcePoints = ref(false);
const factoryType = ref(localStorage.getItem('tabulatedFunctionFactory') || 'array');
const originalPointsA = ref([]);
const originalPointsB = ref([]);
const originalSourcePoints = ref([]);
const tempYValues = ref({ A: {}, B: {}, source: {} });

// Состояние для проверки возможности выполнения операции
const canExecute = ref(false);
const functionCompatibility = ref({
  isCompatible: true,
  warning: '',
  aError: '',
  bError: ''
});

// Сопоставление операций с типами
const operationTypeMap = {
  add: 1,
  subtract: 2,
  multiply: 3,
  divide: 4
};

// Вычисляемые свойства
const hasUnsavedChangesA = computed(() => {
  if (!selectedFunctionA.value || functionAPoints.value.length === 0) return false;
  if (Object.keys(tempYValues.value.A).length > 0) return true;
  return functionAPoints.value.some((point, index) => {
    const originalPoint = originalPointsA.value[index];
    return originalPoint && Math.abs(getYValue(point, index) - originalPoint.getY()) > 0.0001;
  });
});

const hasUnsavedChangesB = computed(() => {
  if (!selectedFunctionB.value || functionBPoints.value.length === 0) return false;
  if (Object.keys(tempYValues.value.B).length > 0) return true;
  return functionBPoints.value.some((point, index) => {
    const originalPoint = originalPointsB.value[index];
    return originalPoint && Math.abs(getYValue(point, index) - originalPoint.getY()) > 0.0001;
  });
});

const hasUnsavedChangesSource = computed(() => {
  if (!selectedSourceFunction.value || sourceFunctionPoints.value.length === 0) return false;
  if (Object.keys(tempYValues.value.source).length > 0) return true;
  return sourceFunctionPoints.value.some((point, index) => {
    const originalPoint = originalSourcePoints.value[index];
    return originalPoint && Math.abs(getYValue(point, index) - originalPoint.getY()) > 0.0001;
  });
});

const isCompatible = computed(() => functionCompatibility.value.isCompatible);

// Методы для работы с функциями
const createPointObject = (x, y) => {
  if (factoryType.value === 'class') {
    return new window.TabulatedFunctionPoint(x, y);
  } else {
    return { getX: () => x, getY: () => y };
  }
};

const getXValue = (point, index) => {
  if (typeof point === 'object' && point !== null) {
    if (typeof point.getX === 'function') {
      return point.getX();
    } else if (point.x !== undefined) {
      return point.x;
    }
  }
  return point;
};

const getYValue = (point, index) => {
  const target = index !== undefined ? index.toString() : 'current';

  if (typeof point === 'object' && point !== null) {
    if (tempYValues.value[selectorTarget.value]?.[target] !== undefined) {
      return tempYValues.value[selectorTarget.value][target];
    }
    if (typeof point.getY === 'function') {
      return point.getY();
    } else if (point.y !== undefined) {
      return point.y;
    }
  }
  return point;
};

const handleYInput = (target, index, value) => {
  if (!tempYValues.value[target]) {
    tempYValues.value[target] = {};
  }
  tempYValues.value[target][index] = parseFloat(value) || 0;
};

const setYValue = (target, index, value) => {
  const pointsArray = target === 'A' ? functionAPoints.value :
                     target === 'B' ? functionBPoints.value :
                     sourceFunctionPoints.value;

  if (index >= 0 && index < pointsArray.length) {
    const point = pointsArray[index];
    if (typeof point === 'object' && point !== null && typeof point.setY === 'function') {
      point.setY(value);
    }

    // Очищаем временное значение
    if (tempYValues.value[target]?.[index] !== undefined) {
      delete tempYValues.value[target][index];
    }
  }
};

const hasDuplicateX = (target) => {
  const points = target === 'A' ? functionAPoints.value :
                target === 'B' ? functionBPoints.value :
                sourceFunctionPoints.value;

  const xValues = points.map(point => getXValue(point));
  const uniqueXValues = new Set(xValues);
  return xValues.length !== uniqueXValues.size;
};

// ДОБАВЛЕННЫЙ МЕТОД: Обновление возможности выполнения операций
const updateCanExecute = () => {
  canExecute.value = selectedFunctionA.value !== null &&
                    selectedFunctionB.value !== null &&
                    functionAPoints.value.length > 0 &&
                    functionBPoints.value.length > 0;
};

// Проверка совместимости функций
const checkFunctionCompatibility = () => {
  if (functionAPoints.value.length === 0 || functionBPoints.value.length === 0) {
    functionCompatibility.value = {
      isCompatible: false,
      warning: '',
      aError: '',
      bError: ''
    };
    return;
  }

  const xValuesA = functionAPoints.value.map(point => getXValue(point));
  const xValuesB = functionBPoints.value.map(point => getXValue(point));

  // Проверяем совпадение X-значений
  const allXMatch = xValuesA.length === xValuesB.length &&
                   xValuesA.every((x, i) => Math.abs(x - xValuesB[i]) < 0.0001);

  functionCompatibility.value = {
    isCompatible: allXMatch,
    warning: allXMatch ? '' : 'Функции имеют разные X-значения. Для операций требуется полное совпадение X-сетки.',
    aError: '',
    bError: ''
  };
};

// Методы для работы с модальными окнами
const createFunction = (target) => {
  selectorTarget.value = target;
  showFunctionSelector.value = false;
  window.dispatchEvent(new CustomEvent('create-function', { detail: target }));
};

const openFunctionSelector = async (target) => {
  selectorTarget.value = target;
  loadingFunctions.value = true;
  showFunctionSelector.value = true;

  try {
    const userId = api.getStoredUserId();
    const functions = await api.getFunctionsByUserId(userId);
    availableFunctions.value = functions.filter(f => f.typeFunction === 'tabular');
  } catch (error) {
    console.error('Error loading functions:', error);
    availableFunctions.value = [];
  } finally {
    loadingFunctions.value = false;
  }
};

const closeFunctionSelector = () => {
  showFunctionSelector.value = false;
  selectorTarget.value = null;
};

const selectFunction = (func) => {
  if (selectorTarget.value === 'A') {
    loadFunctionPoints(func.functionId, 'A');
  } else if (selectorTarget.value === 'B') {
    loadFunctionPoints(func.functionId, 'B');
  } else if (selectorTarget.value === 'source') {
    loadFunctionPoints(func.functionId, 'source');
  }
  closeFunctionSelector();
};

const getSelectorTargetName = (target) => {
  const names = {
    'A': 'Функция A',
    'B': 'Функция B',
    'source': 'Исходная функция'
  };
  return names[target] || target;
};

// Загрузка точек функции
const loadFunctionPoints = async (functionId, target) => {
  try {
    console.log(`Загрузка точек для функции ID=${functionId}, target=${target}`);

    if (target === 'A') loadingPointsA.value = true;
    if (target === 'B') loadingPointsB.value = true;
    if (target === 'source') loadingSourcePoints.value = true;

    const pointsResponse = await api.getTabulatedPointsByFunctionId(functionId);
    const points = pointsResponse.map(p => createPointObject(
      parseFloat(p.xval),
      parseFloat(p.yval)
    ));
    const sortedPoints = [...points].sort((a, b) => a.getX() - b.getX());

    const userId = api.getStoredUserId();
    const allFunctions = await api.getFunctionsByUserId(userId);
    const functionData = allFunctions.find(f => f.functionId === functionId);

    if (!functionData) {
      throw new Error(`Функция с ID ${functionId} не найдена`);
    }

    functionData.pointCount = points.length;

    if (target === 'A') {
      selectedFunctionA.value = functionData;
      functionAPoints.value = [...sortedPoints];
      originalPointsA.value = sortedPoints.map(p => createPointObject(p.getX(), p.getY()));
      tempYValues.value.A = {};
    } else if (target === 'B') {
      selectedFunctionB.value = functionData;
      functionBPoints.value = [...sortedPoints];
      originalPointsB.value = sortedPoints.map(p => createPointObject(p.getX(), p.getY()));
      tempYValues.value.B = {};
    } else if (target === 'source') {
      selectedSourceFunction.value = functionData;
      sourceFunctionPoints.value = [...sortedPoints];
      originalSourcePoints.value = sortedPoints.map(p => createPointObject(p.getX(), p.getY()));
      tempYValues.value.source = {};

      // Автоматически устанавливаем диапазон интерполяции
      if (sortedPoints.length > 0) {
        const xValues = sortedPoints.map(p => p.getX());
        interpolationRange.value.start = Math.min(...xValues);
        interpolationRange.value.end = Math.max(...xValues);
      }
    }

    if (target === 'A' || target === 'B') {
      checkFunctionCompatibility();
      updateCanExecute();
    }

    console.log(`Успешно загружены данные для функции ${target} с ID=${functionId}`);

  } catch (e) {
    console.error(`Error loading function ${target} with ID ${functionId}:`, e);
    alert(`Ошибка загрузки функции: ${e.message}`);
  } finally {
    if (target === 'A') loadingPointsA.value = false;
    if (target === 'B') loadingPointsB.value = false;
    if (target === 'source') loadingSourcePoints.value = false;
  }
};

// Сохранение измененных точек
const saveFunctionPoints = async (target) => {
  const functionData = target === 'A' ? selectedFunctionA.value :
                      target === 'B' ? selectedFunctionB.value :
                      selectedSourceFunction.value;

  const points = target === 'A' ? functionAPoints.value :
                target === 'B' ? functionBPoints.value :
                sourceFunctionPoints.value;

  if (!functionData) {
    alert('Функция не выбрана');
    return;
  }

  try {
    // Удаляем все старые точки
    await api.deleteTabulatedPointsByFunctionId(functionData.functionId);

    // Добавляем новые точки
    for (const point of points) {
      await api.createTabulatedPoints(
        functionData.functionId,
        getXValue(point),
        getYValue(point)
      );
    }

    // Обновляем оригинальные точки
    if (target === 'A') {
      originalPointsA.value = points.map(p => createPointObject(getXValue(p), getYValue(p)));
      tempYValues.value.A = {};
    } else if (target === 'B') {
      originalPointsB.value = points.map(p => createPointObject(getXValue(p), getYValue(p)));
      tempYValues.value.B = {};
    } else if (target === 'source') {
      originalSourcePoints.value = points.map(p => createPointObject(getXValue(p), getYValue(p)));
      tempYValues.value.source = {};
    }

    alert('Изменения успешно сохранены!');
  } catch (error) {
    console.error('Error saving points:', error);
    alert(`Ошибка сохранения: ${error.message}`);
  }
};

// Очистка функции
const clearFunction = (target) => {
  if (target === 'A') {
    selectedFunctionA.value = null;
    functionAPoints.value = [];
    originalPointsA.value = [];
    tempYValues.value.A = {};
  } else if (target === 'B') {
    selectedFunctionB.value = null;
    functionBPoints.value = [];
    originalPointsB.value = [];
    tempYValues.value.B = {};
  } else if (target === 'source') {
    selectedSourceFunction.value = null;
    sourceFunctionPoints.value = [];
    originalSourcePoints.value = [];
    tempYValues.value.source = {};
    interpolationRange.value = { start: null, end: null };
  }

  if (target === 'A' || target === 'B') {
    checkFunctionCompatibility();
    updateCanExecute();
  }
};

// Выполнение операций
const executeOperation = async (operation) => {
  if (!selectedFunctionA.value || !selectedFunctionB.value) {
    alert('Выберите обе функции для выполнения операции');
    return;
  }

  if (!isCompatible.value) {
    alert('Функции несовместимы для выполнения операций');
    return;
  }

  try {
    const functionIdA = selectedFunctionA.value.functionId;
    const functionIdB = selectedFunctionB.value.functionId;
    const operationTypeId = operationTypeMap[operation];

    const result = await api.performOperation(functionIdA, functionIdB, operationTypeId);

    resultPoints.value = result.points.map(p => ({
      x: parseFloat(p.x),
      y: parseFloat(p.y)
    }));

    resultName.value = `Результат_${operation}_${selectedFunctionA.value.functionName}_${selectedFunctionB.value.functionName}`;
    resultOperationType.value = operationTypeId;

    updateChart();

    alert(`Операция выполнена успешно! Создано ${resultPoints.value.length} точек.`);

  } catch (error) {
    console.error('Operation error:', error);
    alert(`Ошибка выполнения операции: ${error.message}`);
  }
};

// Интерполяция
const executeInterpolation = async () => {
  if (!selectedSourceFunction.value || sourceFunctionPoints.value.length < 2) {
    interpolationError.value = 'Выберите исходную функцию с как минимум 2 точками';
    return;
  }

  try {
    interpolationError.value = '';

    // Определяем диапазон интерполяции
    let start = interpolationRange.value.start;
    let end = interpolationRange.value.end;

    if (start === null || end === null) {
      // Автоматически определяем диапазон по точкам функции
      const xValues = sourceFunctionPoints.value.map(p => getXValue(p));
      start = Math.min(...xValues);
      end = Math.max(...xValues);
      interpolationRange.value = { start, end };
    }

    if (start >= end) {
      interpolationError.value = 'Начало диапазона должно быть меньше конца';
      return;
    }

    // Определяем шаг или количество точек
    let step;
    if (interpolationPointsCount.value > 0) {
      step = (end - start) / (interpolationPointsCount.value - 1);
    } else if (interpolationStep.value > 0) {
      step = interpolationStep.value;
    } else {
      interpolationError.value = 'Укажите количество точек или шаг интерполяции';
      return;
    }

    // Выполняем интерполяцию через API
    const response = await api.interpolateFunction(
      selectedSourceFunction.value.functionId,
      interpolationMethod.value,
      start,
      end,
      step,
      interpolationPointsCount.value
    );

    resultPoints.value = response.points.map(p => ({
      x: parseFloat(p.x),
      y: parseFloat(p.y)
    }));

    resultName.value = `Интерполяция_${interpolationMethod.value}_${selectedSourceFunction.value.functionName}`;
    resultOperationType.value = 5; // Тип операции для интерполяции

    // Обновляем график
    updateChart();

    alert(`Интерполяция выполнена успешно! Создано ${resultPoints.value.length} точек.`);

  } catch (error) {
    console.error('Interpolation error:', error);
    interpolationError.value = `Ошибка интерполяции: ${error.message}`;
  }
};

// Методы для работы с графиком
const updateChart = () => {
  if (!chartCanvas.value || resultPoints.value.length === 0) return;

  // Уничтожаем предыдущий график
  if (chartInstance) {
    chartInstance.destroy();
  }

  const ctx = chartCanvas.value.getContext('2d');

  // Сортируем точки по X для корректного отображения
  const sortedPoints = [...resultPoints.value].sort((a, b) => a.x - b.x);

  chartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels: sortedPoints.map(p => p.x.toFixed(2)),
      datasets: [{
        label: resultName.value,
        data: sortedPoints.map(p => p.y),
        borderColor: '#2196f3',
        backgroundColor: 'rgba(33, 150, 243, 0.1)',
        borderWidth: 2,
        fill: true,
        tension: 0.4
      }]
    },
    options: {
      responsive: false,
      plugins: {
        title: {
          display: true,
          text: resultName.value
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: 'X'
          }
        },
        y: {
          title: {
            display: true,
            text: 'Y'
          }
        }
      }
    }
  });
};

const exportToCSV = () => {
  if (resultPoints.value.length === 0) return;

  const headers = ['X', 'Y'];
  const csvContent = [
    headers.join(','),
    ...resultPoints.value.map(point =>
      [point.x.toFixed(6), point.y.toFixed(6)].join(',')
    )
  ].join('\n');

  const blob = new Blob([csvContent], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${resultName.value}.csv`;
  link.click();
  URL.revokeObjectURL(url);
};

// Сохранение результата
const saveResult = async () => {
  if (resultPoints.value.length === 0) {
    alert('Нет данных для сохранения');
    return;
  }

  try {
    const functionData = {
      functionName: resultName.value,
      functionExpression: `Результат ${activeTab.value === 'operations' ? 'операции' : 'интерполяции'} "${resultName.value}"`,
      typeFunction: 'tabular'
    };

    await api.createFunction(functionData);

    const userId = api.getStoredUserId();
    const allFunctions = await api.getFunctionsByUserId(userId);

    const createdFunction = allFunctions
      .filter(f => f.functionName === resultName.value)
      .sort((a, b) => b.functionId - a.functionId)[0];

    if (!createdFunction) {
      throw new Error("Не удалось найти созданную функцию");
    }

    const functionId = createdFunction.functionId;

    // Сохраняем точки
    for (const point of resultPoints.value) {
      await api.createTabulatedPoints(functionId, point.x, point.y);
    }

    // Сохраняем операцию в истории
    if (resultOperationType.value) {
      const operationsResponse = await fetch('http://localhost:8080/yourapp/api/operations', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Basic ${api.getStoredCredentials()}`
        },
        body: JSON.stringify({
          functionId: functionId,
          operationsTypeId: resultOperationType.value
        })
      });

      if (!operationsResponse.ok) {
        const errorData = await operationsResponse.json();
        throw new Error(errorData.error || errorData.message || 'Ошибка сохранения операции');
      }
    }

    resultFunctionId.value = functionId;
    alert('Результат успешно сохранен!');

  } catch (e) {
    console.error('Error saving result:', e);
    alert(`Ошибка сохранения результата: ${e.message}`);
  }
};

// Очистка результата
const clearResult = () => {
  resultPoints.value = [];
  resultName.value = '';
  resultFunctionId.value = null;
  resultOperationType.value = null;

  if (chartInstance) {
    chartInstance.destroy();
    chartInstance = null;
  }
};

// Обработчик события создания функции
const handleFunctionCreated = (event) => {
  const { operand, functionId, functionName, points } = event.detail;

  if (operand === selectorTarget.value) {
    if (operand === 'A') {
      selectedFunctionA.value = { functionId, functionName };
      functionAPoints.value = points.map(p => createPointObject(p.x, p.y));
      originalPointsA.value = [...functionAPoints.value];
    } else if (operand === 'B') {
      selectedFunctionB.value = { functionId, functionName };
      functionBPoints.value = points.map(p => createPointObject(p.x, p.y));
      originalPointsB.value = [...functionBPoints.value];
    } else if (operand === 'source') {
      selectedSourceFunction.value = { functionId, functionName };
      sourceFunctionPoints.value = points.map(p => createPointObject(p.x, p.y));
      originalSourcePoints.value = [...sourceFunctionPoints.value];
    }

    if (operand === 'A' || operand === 'B') {
      checkFunctionCompatibility();
      updateCanExecute();
    }
  }
};

// Watchers
watch(resultPoints, () => {
  if (resultPoints.value.length > 0) {
    nextTick(() => {
      updateChart();
    });
  }
});

watch([functionAPoints, functionBPoints], () => {
  checkFunctionCompatibility();
  updateCanExecute();
});

// Инициализация
onMounted(() => {
  window.addEventListener('operation-function-created', handleFunctionCreated);

  window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && showFunctionSelector.value) {
      closeFunctionSelector();
    }
  });

  // Инициализируем проверку
  updateCanExecute();
  checkFunctionCompatibility();
});

onUnmounted(() => {
  window.removeEventListener('operation-function-created', handleFunctionCreated);
  window.removeEventListener('keydown', (e) => {
    if (e.key === 'Escape' && showFunctionSelector.value) {
      closeFunctionSelector();
    }
  });

  if (chartInstance) {
    chartInstance.destroy();
  }
});
</script>

<style scoped>
/* Новые стили для вкладок */
.tabs-section {
  margin-bottom: 20px;
  border-bottom: 1px solid #ddd;
}

.tabs {
  display: flex;
  gap: 0;
}

.tab-button {
  padding: 12px 24px;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  transition: all 0.3s ease;
}

.tab-button:hover {
  background-color: #f5f5f5;
  color: #333;
}

.tab-button.active {
  color: #2196f3;
  border-bottom-color: #2196f3;
  background-color: #f8fdff;
}

/* Стили для секции интерполяции */
.interpolation-content {
  padding: 20px 0;
}

.interpolation-controls {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
}

.interpolation-source,
.interpolation-params {
  flex: 1;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.param-group {
  margin-bottom: 15px;
}

.param-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #333;
}

.param-group select,
.param-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.range-inputs {
  display: flex;
  align-items: center;
  gap: 10px;
}

.range-inputs input {
  flex: 1;
}

.hint {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
  display: block;
}

.interpolation-button {
  width: 100%;
  padding: 12px;
  background-color: #9c27b0;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.2s;
}

.interpolation-button:hover:not(:disabled) {
  background-color: #7b1fa2;
}

.interpolation-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.interpolation-error {
  background-color: #ffebee;
  border: 1px solid #f44336;
  border-radius: 4px;
  padding: 12px;
  margin: 15px 0;
}

/* Стили для графика */
.result-chart {
  margin: 20px 0;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: white;
  text-align: center;
}

.result-chart canvas {
  max-width: 100%;
  height: auto;
}

/* Стили для кнопки экспорта */
.export-button {
  background-color: #607d8b;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.2s;
}

.export-button:hover {
  background-color: #546e7a;
}

/* Адаптивность */
@media (max-width: 768px) {
  .interpolation-controls {
    flex-direction: column;
  }

  .tabs {
    flex-direction: column;
  }

  .tab-button {
    border-bottom: none;
    border-left: 3px solid transparent;
  }

  .tab-button.active {
    border-left-color: #2196f3;
    border-bottom-color: transparent;
  }
}

/* Остальные существующие стили остаются без изменений */
.operations-window {
  position: relative;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  max-width: 1200px;
  margin: 0 auto;
  font-family: Arial, sans-serif;
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
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-button:hover {
  background-color: #f0f0f0;
  color: #d32f2f;
}

.functions-container {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.function-section {
  flex: 1;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.function-controls {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.function-controls button {
  padding: 8px 15px;
  background-color: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.function-controls button:hover {
  background-color: #1976d2;
}

.function-details {
  background-color: white;
  padding: 15px;
  border-radius: 6px;
  border: 1px solid #ddd;
  margin-top: 10px;
}

.error-message {
  color: #d32f2f;
  font-size: 0.9em;
  margin: 5px 0;
}

.warning-message {
  color: #ed6c02;
  font-weight: bold;
  margin: 0;
}

.compatibility-warning {
  background-color: #fff8e1;
  border-left: 4px solid #ffc107;
  padding: 10px 15px;
  margin: 15px 0;
  border-radius: 0 4px 4px 0;
}

.compatibility-message {
  color: #ed6c02;
  font-size: 0.9em;
  margin-top: 8px;
  text-align: center;
}

.function-table, .result-table {
  margin-top: 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  overflow: hidden;
}

.function-table h4, .result-table h4 {
  margin: 0 0 10px 0;
  padding: 10px;
  background-color: #e9ecef;
  border-bottom: 1px solid #ddd;
}

.empty-table {
  text-align: center;
  padding: 20px;
  color: #999;
}

.point-y-input {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.point-y-input:focus {
  outline: none;
  border-color: #2196f3;
  box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.2);
}

.operations-section {
  margin: 30px 0;
}

.operations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.operation-button {
  padding: 12px;
  border: none;
  border-radius: 6px;
  color: white;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.operation-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.operation-button.add { background-color: #4caf50; }
.operation-button.subtract { background-color: #2196f3; }
.operation-button.multiply { background-color: #ff9800; }
.operation-button.divide { background-color: #f44336; }

.operation-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 2px 5px rgba(0,0,0,0.2);
}

.result-section {
  margin-top: 30px;
  padding: 20px;
  border-radius: 8px;
  background-color: #f8f9fa;
}

.result-table {
  background-color: white;
  max-height: 400px;
  overflow-y: auto;
}

.result-actions {
  display: flex;
  gap: 15px;
  margin-top: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.save-button, .clear-button {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.2s;
}

.save-button {
  background-color: #4caf50;
  color: white;
}

.save-button:hover:not(:disabled) {
  background-color: #45a049;
}

.save-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.clear-button {
  background-color: #f44336;
  color: white;
}

.clear-button:hover {
  background-color: #e53935;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 300px;
}

table th, table td {
  border: 1px solid #ddd;
  padding: 10px;
  text-align: left;
}

table th {
  background-color: #f5f5f5;
  font-weight: bold;
}

table td {
  background-color: white;
}

@media (max-width: 768px) {
  .functions-container {
    flex-direction: column;
  }

  .operations-grid {
    grid-template-columns: 1fr;
  }
}

/* Анимации для улучшения UX */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.function-section, .operations-section, .result-section, .interpolation-controls {
  animation: fadeIn 0.3s ease-out;
}

/* Стили для модального окна выбора функции */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.function-selector-modal {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  animation: modalFadeIn 0.3s ease-out;
}

.modal-header {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8f9fa;
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.2rem;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex-grow: 1;
}

.functions-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.function-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 4px;
}

.function-item:hover {
  background-color: #f0f7ff;
  transform: translateX(5px);
}

.function-item div {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.function-id {
  color: #666;
  font-size: 0.9rem;
  margin-left: 8px;
}

.function-meta {
  display: flex;
  gap: 15px;
  margin-top: 5px;
  font-size: 0.85rem;
  color: #666;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  text-align: right;
  background-color: #f8f9fa;
  border-radius: 0 0 8px 8px;
}

.cancel-button {
  padding: 8px 16px;
  background-color: #e0e0e0;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}

.cancel-button:hover {
  background-color: #d5d5d5;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 600px) {
  .function-selector-modal {
    width: 95%;
    margin: 10px;
  }

  .function-item div {
    flex-direction: column;
    align-items: flex-start;
  }

  .function-meta {
    margin-top: 8px;
    width: 100%;
  }
}

/* Стили для выделения измененных значений */
.changed-value {
  background-color: #fff8e1;
  font-weight: bold;
}
</style>