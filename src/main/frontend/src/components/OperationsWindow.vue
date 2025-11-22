<!-- src/components/OperationsWindow.vue -->
<template>
  <div class="operations-window">
    <div class="window-header">
      <h2>Операции над функциями</h2>
      <button class="close-button" @click="$emit('close')">&times;</button>
    </div>

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

    <!-- Таблица для результата -->
    <div class="result-section">
      <h3>Результат операции</h3>
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
              <td>{{ point.x }}</td>
              <td>{{ point.y }}</td>
            </tr>
            <tr v-if="resultPoints.length === 0">
              <td colspan="2" class="empty-table">Результат отсутствует</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="result-actions">
        <button @click="saveResult" class="save-button" :disabled="resultPoints.length === 0">Сохранить результат</button>
        <button @click="clearResult" class="clear-button">Очистить результат</button>
      </div>
    </div>

    <!-- Модальное окно для выбора функции -->
    <div v-if="showFunctionSelector" class="modal-overlay" @click="closeFunctionSelector">
      <div class="function-selector-modal" @click.stop>
        <div class="modal-header">
          <h3>Выберите функцию для {{ selectorTarget }}</h3>
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { api } from '../api.js';

const selectedFunctionA = ref(null);
const selectedFunctionB = ref(null);
const functionAPoints = ref([]);
const functionBPoints = ref([]);
const resultPoints = ref([]);
const resultName = ref('');
const resultFunctionId = ref(null);
const resultOperationType = ref(null);
const availableFunctions = ref([]);
const showFunctionSelector = ref(false);
const selectorTarget = ref(null);
const loadingFunctions = ref(false);
const loadingPointsA = ref(false);
const loadingPointsB = ref(false);
const factoryType = ref(localStorage.getItem('tabulatedFunctionFactory') || 'array');
const originalPointsA = ref([]);
const originalPointsB = ref([]);
const tempYValues = ref({ A: {}, B: {} });

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

// Проверка наличия дубликатов X-значений
const hasDuplicateX = (target) => {
  const points = target === 'A' ? functionAPoints.value : functionBPoints.value;
  const xValues = new Set();

  for (const point of points) {
    const x = getXValue(point);
    if (xValues.has(x)) {
      return true;
    }
    xValues.add(x);
  }
  return false;
};

// Вычисляемые свойства для отслеживания изменений
const hasUnsavedChangesA = computed(() => {
  if (!selectedFunctionA.value || functionAPoints.value.length === 0) return false;

  // Проверяем изменения в временных значениях
  if (Object.keys(tempYValues.value.A).length > 0) return true;

  return functionAPoints.value.some((point, index) => {
    const originalPoint = originalPointsA.value[index];
    return originalPoint && Math.abs(getYValue(point, index) - originalPoint.getY()) > 0.0001;
  });
});

const hasUnsavedChangesB = computed(() => {
  if (!selectedFunctionB.value || functionBPoints.value.length === 0) return false;

  // Проверяем изменения в временных значениях
  if (Object.keys(tempYValues.value.B).length > 0) return true;

  return functionBPoints.value.some((point, index) => {
    const originalPoint = originalPointsB.value[index];
    return originalPoint && Math.abs(getYValue(point, index) - originalPoint.getY()) > 0.0001;
  });
});

// Вычисляемое свойство для совместимости
const isCompatible = computed(() => {
  return functionCompatibility.value.isCompatible &&
         !hasDuplicateX('A') &&
         !hasDuplicateX('B');
});

// Проверка совместимости функций
const checkFunctionCompatibility = () => {
  if (!selectedFunctionA.value || !selectedFunctionB.value) {
    functionCompatibility.value = {
      isCompatible: false,
      warning: '',
      aError: '',
      bError: ''
    };
    return;
  }

  let isCompatible = true;
  let warning = '';
  let aError = '';
  let bError = '';

  // Проверка наличия дубликатов X
  if (hasDuplicateX('A')) {
    aError = 'Дублирующиеся X-значения в функции A';
    isCompatible = false;
  }

  if (hasDuplicateX('B')) {
    bError = 'Дублирующиеся X-значения в функции B';
    isCompatible = false;
  }

  // Проверка количества точек
  if (!hasDuplicateX('A') && !hasDuplicateX('B') &&
      functionAPoints.value.length !== functionBPoints.value.length) {
    isCompatible = false;
    warning = `Функции имеют разное количество точек (A: ${functionAPoints.value.length}, B: ${functionBPoints.value.length}). Операции могут быть некорректными.`;
  }

  // Проверка совпадения X-значений
  let xValuesMatch = true;
  if (functionAPoints.value.length > 0 && functionBPoints.value.length > 0 &&
      !hasDuplicateX('A') && !hasDuplicateX('B')) {
    // Сортируем точки по X для корректного сравнения
    const sortedAPoints = [...functionAPoints.value].sort((a, b) => getXValue(a) - getXValue(b));
    const sortedBPoints = [...functionBPoints.value].sort((a, b) => getXValue(a) - getXValue(b));

    for (let i = 0; i < sortedAPoints.length; i++) {
      const pointA = sortedAPoints[i];
      const pointB = sortedBPoints[i];

      if (!pointB || Math.abs(getXValue(pointA) - getXValue(pointB)) > 0.0001) {
        xValuesMatch = false;
        break;
      }
    }
  }

  if (!xValuesMatch && !hasDuplicateX('A') && !hasDuplicateX('B')) {
    isCompatible = false;
    if (warning) {
      warning += ' Кроме того, ';
    }
    warning += 'X-значения точек не совпадают. Операции могут быть некорректными.';
  }

  // Проверка на пустые функции
  if (functionAPoints.value.length === 0) {
    aError = 'Функция не содержит точек';
    isCompatible = false;
  }

  if (functionBPoints.value.length === 0) {
    bError = 'Функция не содержит точек';
    isCompatible = false;
  }

  functionCompatibility.value = {
    isCompatible,
    warning: warning || '',
    aError,
    bError
  };
};

// Слушатель события создания функции
const handleFunctionCreated = (event) => {
  const { operand, points, functionId, functionName } = event.detail;

  if (!functionId) {
    console.error('Function created without ID', event.detail);
    alert('Ошибка: не удалось получить ID созданной функции');
    return;
  }

  // Загружаем точки функции напрямую
  loadFunctionPoints(functionId, operand);
};

// Обработчик ввода значения Y
const handleYInput = (target, index, value) => {
  // Сохраняем временное значение
  if (!tempYValues.value[target]) {
    tempYValues.value[target] = {};
  }
  tempYValues.value[target][index] = value;
};

// Сохранение изменений точек функции
const saveFunctionPoints = async (target) => {
  if (target === 'A' && (!selectedFunctionA.value || !hasUnsavedChangesA.value)) return;
  if (target === 'B' && (!selectedFunctionB.value || !hasUnsavedChangesB.value)) return;

  try {
    const functionData = target === 'A' ? selectedFunctionA.value : selectedFunctionB.value;
    const points = target === 'A' ? functionAPoints.value : functionBPoints.value;
    const originalPoints = target === 'A' ? originalPointsA.value : originalPointsB.value;

    // Проверка наличия дубликатов X перед сохранением
    const xValues = new Set();
    for (const point of points) {
      const x = getXValue(point);
      if (xValues.has(x)) {
        alert('Невозможно сохранить функцию: обнаружены дублирующиеся X-значения.');
        return;
      }
      xValues.add(x);
    }

    // Обновляем точки функции
    for (let i = 0; i < points.length; i++) {
      const point = points[i];
      const originalPoint = originalPoints[i];

      // Получаем значение Y: сначала проверяем временные значения, потом оригинальные
      let newYValue = getYValue(point, i);
      if (tempYValues.value[target] && tempYValues.value[target][i] !== undefined) {
        newYValue = parseFloat(tempYValues.value[target][i]);
      }

      // Обновляем точку в базе
      await api.createTabulatedPoints(
        functionData.functionId,
        getXValue(point, i),
        newYValue
      );

      // Обновляем Y в объекте точки
      if (typeof point.setY === 'function') {
        point.setY(newYValue);
      }
    }

    // Обновляем оригинальные точки после сохранения
    const updatedPoints = points.map((point, i) => {
      let newYValue = getYValue(point, i);
      if (tempYValues.value[target] && tempYValues.value[target][i] !== undefined) {
        newYValue = parseFloat(tempYValues.value[target][i]);
      }
      return createPointObject(getXValue(point, i), newYValue);
    });

    if (target === 'A') {
      originalPointsA.value = updatedPoints;
      // Очищаем временные значения
      tempYValues.value.A = {};
    } else if (target === 'B') {
      originalPointsB.value = updatedPoints;
      // Очищаем временные значения
      tempYValues.value.B = {};
    }

    alert(`Изменения для функции "${functionData.functionName}" успешно сохранены!`);
  } catch (e) {
    console.error(`Error saving changes for function ${target}:`, e);
    alert(`Ошибка сохранения изменений: ${e.message}`);
  }
};

// Загрузка точек функции по ID
const loadFunctionPoints = async (functionId, target) => {
  try {
    console.log(`Загрузка точек для функции ID=${functionId}, target=${target}`);

    if (target === 'A') loadingPointsA.value = true;
    if (target === 'B') loadingPointsB.value = true;

    // Получаем точки функции
    const pointsResponse = await api.getTabulatedPointsByFunctionId(functionId);

    // Создаем объекты точек с методами getX и getY
    const points = pointsResponse.map(p => createPointObject(
      parseFloat(p.xval),
      parseFloat(p.yval)
    ));

    // Сортируем точки по X-значению
    const sortedPoints = [...points].sort((a, b) => a.getX() - b.getX());

    // Получаем данные о функции из списка доступных функций
    const userId = api.getStoredUserId();
    const allFunctions = await api.getFunctionsByUserId(userId);
    const functionData = allFunctions.find(f => f.functionId === functionId);

    if (!functionData) {
      throw new Error(`Функция с ID ${functionId} не найдена`);
    }

    // Добавляем количество точек для отображения
    functionData.pointCount = points.length;

    // Обновляем состояние в зависимости от цели
    if (target === 'A') {
      selectedFunctionA.value = functionData;
      functionAPoints.value = [...sortedPoints];
      // Сохраняем оригинальные точки для отслеживания изменений
      originalPointsA.value = sortedPoints.map(p => createPointObject(p.getX(), p.getY()));
      // Очищаем временные значения
      tempYValues.value.A = {};
    } else if (target === 'B') {
      selectedFunctionB.value = functionData;
      functionBPoints.value = [...sortedPoints];
      // Сохраняем оригинальные точки для отслеживания изменений
      originalPointsB.value = sortedPoints.map(p => createPointObject(p.getX(), p.getY()));
      // Очищаем временные значения
      tempYValues.value.B = {};
    }

    checkFunctionCompatibility();
    updateCanExecute();
    console.log(`Успешно загружены данные для функции ${target} с ID=${functionId}`);

  } catch (e) {
    console.error(`Error loading function ${target} with ID ${functionId}:`, e);
    alert(`Ошибка загрузки функции: ${e.message}`);
  } finally {
    if (target === 'A') loadingPointsA.value = false;
    if (target === 'B') loadingPointsB.value = false;
  }
};

// Создание объекта точки с методами getX и getY
const createPointObject = (x, y) => {
  return {
    _x: x,
    _y: y,
    getX: function() { return this._x; },
    getY: function() { return this._y; },
    setY: function(newValue) { this._y = newValue; }
  };
};

// Получение X-значения через метод getX()
const getXValue = (point, index = null) => {
  if (point && typeof point.getX === 'function') {
    return point.getX();
  }
  return point.x !== undefined ? point.x : (index !== null ? `Точка ${index + 1}` : 0);
};

// Получение Y-значения через метод getY() или из временных значений
const getYValue = (point, index) => {
  if (selectorTarget.value === 'A' && tempYValues.value.A[index] !== undefined) {
    return tempYValues.value.A[index];
  }
  if (selectorTarget.value === 'B' && tempYValues.value.B[index] !== undefined) {
    return tempYValues.value.B[index];
  }

  if (point && typeof point.getY === 'function') {
    return point.getY();
  }
  return point.y !== undefined ? point.y : 0;
};

// Установка Y-значения через метод setY() с сохранением в оригинальные точки
const setYValue = (target, index, newValue) => {
  if (isNaN(newValue)) return;

  if (target === 'A' && functionAPoints.value[index]) {
    if (typeof functionAPoints.value[index].setY === 'function') {
      functionAPoints.value[index].setY(newValue);
    }
    console.log(`Значение Y для функции A, точки ${index} изменено на ${newValue}`);
  } else if (target === 'B' && functionBPoints.value[index]) {
    if (typeof functionBPoints.value[index].setY === 'function') {
      functionBPoints.value[index].setY(newValue);
    }
    console.log(`Значение Y для функции B, точки ${index} изменено на ${newValue}`);
  }

  // Обновляем совместимость после изменения
  checkFunctionCompatibility();
};

// Выполнение операции (execute) с учетом изменений
const executeOperation = async (operation) => {
  if (!canExecute.value || !isCompatible.value || hasDuplicateX('A') || hasDuplicateX('B')) return;

  try {
    // Проверяем совместимость еще раз перед выполнением
    if (!isCompatible.value && !confirm('Функции не полностью совместимы. Продолжить выполнение операции?')) {
      return;
    }

    // Проверка наличия дубликатов X
    if (hasDuplicateX('A') || hasDuplicateX('B')) {
      alert('Невозможно выполнить операцию: обнаружены дублирующиеся X-значения в таблицах функций.');
      return;
    }

    let functionAPointsForOperation = [...functionAPoints.value];
    let functionBPointsForOperation = [...functionBPoints.value];

    // Применяем временные изменения для операции, если они есть
    if (Object.keys(tempYValues.value.A).length > 0) {
      functionAPointsForOperation = functionAPoints.value.map((point, i) => {
        const newY = tempYValues.value.A[i] !== undefined ? parseFloat(tempYValues.value.A[i]) : getYValue(point, i);
        return createPointObject(getXValue(point, i), newY);
      });
    }

    if (Object.keys(tempYValues.value.B).length > 0) {
      functionBPointsForOperation = functionBPoints.value.map((point, i) => {
        const newY = tempYValues.value.B[i] !== undefined ? parseFloat(tempYValues.value.B[i]) : getYValue(point, i);
        return createPointObject(getXValue(point, i), newY);
      });
    }

    // Сортируем точки по X для корректного выполнения операции
    functionAPointsForOperation = [...functionAPointsForOperation].sort((a, b) => a.getX() - b.getX());
    functionBPointsForOperation = [...functionBPointsForOperation].sort((a, b) => a.getX() - b.getX());

    // Если есть несохраненные изменения, сначала сохраняем их
    if (hasUnsavedChangesA.value || hasUnsavedChangesB.value) {
      const shouldSave = confirm('Есть несохраненные изменения. Сохранить их перед выполнением операции?');

      if (shouldSave) {
        if (hasUnsavedChangesA.value) {
          await saveFunctionPoints('A');
          await loadFunctionPoints(selectedFunctionA.value.functionId, 'A');
          functionAPointsForOperation = [...functionAPoints.value];
        }
        if (hasUnsavedChangesB.value) {
          await saveFunctionPoints('B');
          await loadFunctionPoints(selectedFunctionB.value.functionId, 'B');
          functionBPointsForOperation = [...functionBPoints.value];
        }
      } else {
        // Если пользователь не хочет сохранять изменения в базу,
        // выполняем операцию с локальными данными без сохранения
        console.log('Выполнение операции с локальными несохраненными данными');
      }
    }

    // Проверяем совместимость локальных данных
    let isLocallyCompatible = true;
    let localWarning = '';

    if (functionAPointsForOperation.length !== functionBPointsForOperation.length) {
      isLocallyCompatible = false;
      localWarning = `Локальные данные имеют разное количество точек (A: ${functionAPointsForOperation.length}, B: ${functionBPointsForOperation.length})`;
    } else {
      for (let i = 0; i < functionAPointsForOperation.length; i++) {
        if (Math.abs(functionAPointsForOperation[i].getX() - functionBPointsForOperation[i].getX()) > 0.0001) {
          isLocallyCompatible = false;
          localWarning = 'Локальные X-значения точек не совпадают';
          break;
        }
      }
    }

    if (!isLocallyCompatible && !confirm(`Предупреждение: ${localWarning}. Продолжить выполнение операции?`)) {
      return;
    }

    console.log('Выполнение операции:', {
      operation,
      factoryType: factoryType.value,
      usingLocalData: hasUnsavedChangesA.value || hasUnsavedChangesB.value
    });

    let response;
    try {
      // Если есть несохраненные изменения и пользователь не хочет их сохранять,
      // выполняем операцию локально на клиенте
      if ((hasUnsavedChangesA.value || hasUnsavedChangesB.value) && !confirm('Есть несохраненные изменения. Сохранить их перед выполнением операции?')) {
        response = {
          points: performLocalOperation(functionAPointsForOperation, functionBPointsForOperation, operation),
          functionId: null
        };
        console.log('Локальное выполнение операции:', response);
      } else {
        // Стандартное выполнение через сервер
        response = await api.executeOperation(
          selectedFunctionA.value.functionId,
          selectedFunctionB.value.functionId,
          operation,
          factoryType.value
        );
      }
    } catch (err) {
      console.error('Ошибка при выполнении операции:', err);

      // Если ошибка 500 и используем linked-list, пробуем array
      if (factoryType.value === 'linked-list' &&
          (err.status === 500 || err.message.includes('Internal Server Error') || err.message.includes('Внутренняя ошибка сервера'))) {
        console.warn('Переключение на array фабрику из-за ошибки linked-list');

        try {
          response = await api.executeOperation(
            selectedFunctionA.value.functionId,
            selectedFunctionB.value.functionId,
            operation,
            'array'
          );
          // Сохраняем успешный тип фабрики
          factoryType.value = 'array';
          localStorage.setItem('tabulatedFunctionFactory', 'array');
          alert('Произошла ошибка с linked-list фабрикой. Переключено на array фабрику.');
        } catch (fallbackErr) {
          console.error('Ошибка при выполнении операции с array фабрикой:', fallbackErr);

          // Последняя попытка - выполнить локально
          if (confirm('Ошибка сервера. Попробовать выполнить операцию локально с текущими данными?')) {
            response = {
              points: performLocalOperation(functionAPointsForOperation, functionBPointsForOperation, operation),
              functionId: null
            };
            console.log('Локальное выполнение операции после ошибки сервера:', response);
          } else {
            throw fallbackErr;
          }
        }
      } else {
        throw err;
      }
    }

    // Формируем имя результата
    const operationNames = {
      add: 'сложение',
      subtract: 'вычитание',
      multiply: 'умножение',
      divide: 'деление'
    };

    resultName.value = `Результат_${operationNames[operation]}_${selectedFunctionA.value.functionName}_и_${selectedFunctionB.value.functionName}`;
    resultPoints.value = response.points.map(p => ({
      x: parseFloat(p.x !== undefined ? p.x : p.xval),
      y: parseFloat(p.y !== undefined ? p.y : p.yval)
    }));
    resultFunctionId.value = response.functionId || null;
    resultOperationType.value = operationTypeMap[operation];

    console.log('Операция успешно выполнена:', response);
    alert(`Операция "${operationNames[operation]}" успешно выполнена!`);

  } catch (e) {
    console.error('Operation execution error:', e);

    // Попытка получить детальное сообщение об ошибке
    let errorMessage = e.message;
    if (e.response && e.response.data && e.response.data.message) {
      errorMessage = e.response.data.message;
    } else if (e.response && e.response.data && e.response.data.error) {
      errorMessage = e.response.data.error;
    } else if (e.error) {
      errorMessage = e.error;
    }

    console.log('Обработанное сообщение об ошибке:', errorMessage);

    // Анализируем типичные ошибки
    if (errorMessage.includes('different number of points') ||
        errorMessage.includes('разное количество точек') ||
        errorMessage.includes('количество точек не совпадает')) {
      alert('Ошибка: Функции имеют разное количество точек. Операции над функциями требуют совпадения количества точек.');
    } else if (errorMessage.includes('x values do not match') ||
               errorMessage.includes('X-значения не совпадают') ||
               errorMessage.includes('значения x не совпадают')) {
      alert('Ошибка: X-значения точек функций не совпадают. Для операций необходимо, чтобы X-значения были идентичны.');
    } else if (errorMessage.includes('division by zero') ||
               errorMessage.includes('деление на ноль') ||
               errorMessage.includes('на ноль')) {
      alert('Ошибка: Попытка деления на ноль. Проверьте значения Y второй функции.');
    } else if (errorMessage.includes('Internal Server Error') ||
               errorMessage.includes('Внутренняя ошибка сервера')) {
      alert('Внутренняя ошибка сервера. Попробуйте использовать другую фабрику (array вместо linked-list) в настройках.');
    } else {
      alert(`Ошибка выполнения операции: ${errorMessage}`);
    }
  }
};

// Локальное выполнение операции на клиенте
const performLocalOperation = (pointsA, pointsB, operation) => {
  // Сортируем точки по X перед выполнением операции
  const sortedPointsA = [...pointsA].sort((a, b) => a.getX() - b.getX());
  const sortedPointsB = [...pointsB].sort((a, b) => a.getX() - b.getX());

  if (sortedPointsA.length !== sortedPointsB.length) {
    throw new Error('Функции имеют разное количество точек');
  }

  return sortedPointsA.map((pointA, i) => {
    const pointB = sortedPointsB[i];
    const x = pointA.getX();
    let y;

    switch (operation) {
      case 'add':
        y = pointA.getY() + pointB.getY();
        break;
      case 'subtract':
        y = pointA.getY() - pointB.getY();
        break;
      case 'multiply':
        y = pointA.getY() * pointB.getY();
        break;
      case 'divide':
        if (Math.abs(pointB.getY()) < 0.0001) {
          throw new Error('Деление на ноль в точке с x = ' + x);
        }
        y = pointA.getY() / pointB.getY();
        break;
      default:
        throw new Error('Неизвестная операция: ' + operation);
    }

    return { x, y };
  });
};

// Загрузка всех функций пользователя
const loadAvailableFunctions = async () => {
  try {
    loadingFunctions.value = true;
    const userId = api.getStoredUserId();
    const functions = await api.getFunctionsByUserId(userId);

    // Загружаем количество точек для каждой функции
    const functionsWithPoints = await Promise.all(functions.map(async (func) => {
      try {
        const points = await api.getTabulatedPointsByFunctionId(func.functionId);
        return {
          ...func,
          pointCount: points.length
        };
      } catch (e) {
        console.warn(`Не удалось загрузить точки для функции ID=${func.functionId}`, e);
        return {
          ...func,
          pointCount: 0
        };
      }
    }));

    availableFunctions.value = functionsWithPoints;
  } catch (e) {
    console.error('Error loading available functions:', e);
    availableFunctions.value = [];
    alert(`Ошибка загрузки списка функций: ${e.message}`);
  } finally {
    loadingFunctions.value = false;
  }
};

// Обновление возможности выполнения операции
const updateCanExecute = () => {
  canExecute.value = !!(
    selectedFunctionA.value &&
    selectedFunctionB.value &&
    functionAPoints.value.length > 0 &&
    functionBPoints.value.length > 0 &&
    !hasDuplicateX('A') &&
    !hasDuplicateX('B')
  );
};

// Открытие селектора функций
const openFunctionSelector = (target) => {
  selectorTarget.value = target;
  showFunctionSelector.value = true;
  loadAvailableFunctions();
};

// Закрытие селектора функций
const closeFunctionSelector = () => {
  showFunctionSelector.value = false;
  selectorTarget.value = null;
};

// Выбор функции из списка
const selectFunction = (func) => {
  loadFunctionPoints(func.functionId, selectorTarget.value);
  closeFunctionSelector();
};

// Создание новой функции
const createFunction = (operand) => {
  window.dispatchEvent(new CustomEvent('open-create-function', {
    detail: { operand }
  }));
};

// Очистка выбранной функции
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
  }
  checkFunctionCompatibility();
  updateCanExecute();
};

// Сохранение результата (operations)
const saveResult = async () => {
  if (resultPoints.value.length === 0) {
    alert('Нет данных для сохранения');
    return;
  }

  try {
    // Сначала создаем функцию для результата
    const functionData = {
      functionName: resultName.value,
      functionExpression: `Результат операции "${resultName.value}"`,
      typeFunction: 'tabular'
    };

    // Создаем функцию
    await api.createFunction(functionData);

    // Получаем ID созданной функции
    const userId = api.getStoredUserId();
    const allFunctions = await api.getFunctionsByUserId(userId);

    const createdFunction = allFunctions
      .filter(f => f.functionName === resultName.value)
      .sort((a, b) => b.functionId - a.functionId)[0];

    if (!createdFunction) {
      throw new Error("Не удалось найти созданную функцию");
    }

    const functionId = createdFunction.functionId;
    console.log('Найден ID созданной функции:', functionId);

    // Сохраняем точки
    for (const point of resultPoints.value) {
      await api.createTabulatedPoints(functionId, point.x, point.y);
    }

    // Теперь отправляем запрос на operations API
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
};

// Инициализация
onMounted(() => {
  window.addEventListener('function-created', handleFunctionCreated);

  // Проверяем совместимость при изменении точек
  watch([functionAPoints, functionBPoints], () => {
    checkFunctionCompatibility();
  });

  // Добавляем обработчик для закрытия модального окна на ESC
  window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && showFunctionSelector.value) {
      closeFunctionSelector();
    }
  });
});

onUnmounted(() => {
  window.removeEventListener('function-created', handleFunctionCreated);
  window.removeEventListener('keydown', (e) => {
    if (e.key === 'Escape' && showFunctionSelector.value) {
      closeFunctionSelector();
    }
  });
});

// Инициализируем проверку
updateCanExecute();
checkFunctionCompatibility();
</script>

<style scoped>
/* Стили остаются без изменений */
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
}

.result-actions {
  display: flex;
  gap: 15px;
  margin-top: 15px;
  justify-content: center;
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

.function-section, .operations-section, .result-section {
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