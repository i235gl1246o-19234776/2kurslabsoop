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
          <button @click="loadFunctionFromJson('A')">Загрузить из JSON</button>
          <button @click="exportFunctionToJson('A')" :disabled="!selectedFunctionA || functionAPoints.length === 0">
            Экспорт в JSON
          </button>
          <button @click="saveFunctionPoints('A')" :disabled="!selectedFunctionA || !hasUnsavedChangesA">
            Сохранить изменения
          </button>
        </div>
        <div v-if="selectedFunctionA" class="function-details">
          <p><strong>Имя:</strong> {{ selectedFunctionA.functionName }}</p>
          <p><strong>ID:</strong> {{ selectedFunctionA.functionId }}</p>
          <p><strong>Точек:</strong> {{ functionAPoints.length }}</p>
          <p v-if="functionCompatibility.aError" class="error-message">{{ functionCompatibility.aError }}</p>
          <button @click="clearFunction('A')" class="clear-button">Очистить</button>
        </div>
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
                    :value="getYValue(point, index, 'A')"
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
          <button @click="loadFunctionFromJson('B')">Загрузить из JSON</button>
          <button @click="exportFunctionToJson('B')" :disabled="!selectedFunctionB || functionBPoints.length === 0">
            Экспорт в JSON
          </button>
          <button @click="saveFunctionPoints('B')" :disabled="!selectedFunctionB || !hasUnsavedChangesB">
            Сохранить изменения
          </button>
        </div>
        <div v-if="selectedFunctionB" class="function-details">
          <p><strong>Имя:</strong> {{ selectedFunctionB.functionName }}</p>
          <p><strong>ID:</strong> {{ selectedFunctionB.functionId }}</p>
          <p><strong>Точек:</strong> {{ functionBPoints.length }}</p>
          <p v-if="functionCompatibility.bError" class="error-message">{{ functionCompatibility.bError }}</p>
          <button @click="clearFunction('B')" class="clear-button">Очистить</button>
        </div>
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
                    :value="getYValue(point, index, 'B')"
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
        <button v-if="resultPoints.length > 0" @click="exportResultToJson" class="export-button">
          Экспорт в json
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { api } from '../api.js';
const emit = defineEmits(['close', 'create-function']);
// --- State ---
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
// --- Operations state ---
const canExecute = ref(false);
const functionCompatibility = ref({
  isCompatible: true,
  warning: '',
  aError: '',
  bError: ''
});
const operationTypeMap = {
  add: 1,
  subtract: 2,
  multiply: 3,
  divide: 4
};
// --- Point helpers ---
const createPointObject = (x, y) => ({
  _x: x,
  _y: y,
  getX: function () { return this._x; },
  getY: function () { return this._y; },
  setY: function (newValue) { this._y = newValue; }
});
const getXValue = (point, index = null) => {
  if (point && typeof point.getX === 'function') return point.getX();
  return point?.x ?? (index !== null ? `Точка ${index + 1}` : 0);
};
const getYValue = (point, index, target) => {
  if (tempYValues.value[target]?.[index] !== undefined) {
    return tempYValues.value[target][index];
  }
  if (point && typeof point.getY === 'function') return point.getY();
  return point?.y ?? 0;
};
const handleYInput = (target, index, value) => {
  if (!tempYValues.value[target]) tempYValues.value[target] = {};
  tempYValues.value[target][index] = value;
};
const setYValue = (target, index, value) => {
  if (isNaN(value)) return;
  const points = target === 'A' ? functionAPoints.value : functionBPoints.value;
  if (index >= 0 && index < points.length && points[index].setY) {
    points[index].setY(value);
  }
  if (tempYValues.value[target]?.[index] !== undefined) {
    delete tempYValues.value[target][index];
  }
  checkFunctionCompatibility();
};
// --- Computed ---
const hasUnsavedChangesA = computed(() => {
  if (!selectedFunctionA.value || functionAPoints.value.length === 0) return false;
  if (Object.keys(tempYValues.value.A).length > 0) return true;
  return functionAPoints.value.some((point, i) => {
    const orig = originalPointsA.value[i];
    return orig && Math.abs(getYValue(point, i, 'A') - orig.getY()) > 0.0001;
  });
});
const hasUnsavedChangesB = computed(() => {
  if (!selectedFunctionB.value || functionBPoints.value.length === 0) return false;
  if (Object.keys(tempYValues.value.B).length > 0) return true;
  return functionBPoints.value.some((point, i) => {
    const orig = originalPointsB.value[i];
    return orig && Math.abs(getYValue(point, i, 'B') - orig.getY()) > 0.0001;
  });
});
const isCompatible = computed(() => functionCompatibility.value.isCompatible);
// --- JSON import/export ---
const loadFunctionFromJson = (target) => {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = '.json';
  input.onchange = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const data = JSON.parse(event.target.result);
        if (!data.functionName) throw new Error('Отсутствует functionName');
        if (!Array.isArray(data.points) || data.points.length === 0) throw new Error('Нет точек');
        const points = data.points.map(p => {
          const x = parseFloat(p.x);
          const y = parseFloat(p.y);
          if (isNaN(x) || isNaN(y)) throw new Error('Некорректные x/y');
          return createPointObject(x, y);
        });
        const sorted = [...points].sort((a, b) => a.getX() - b.getX());
        const fakeFunc = {
          functionId: null,
          functionName: data.functionName,
          typeFunction: 'tabular',
          pointCount: points.length
        };
        if (target === 'A') {
          selectedFunctionA.value = fakeFunc;
          functionAPoints.value = [...sorted];
          originalPointsA.value = sorted.map(p => createPointObject(p.getX(), p.getY()));
          tempYValues.value.A = {};
        } else {
          selectedFunctionB.value = fakeFunc;
          functionBPoints.value = [...sorted];
          originalPointsB.value = sorted.map(p => createPointObject(p.getX(), p.getY()));
          tempYValues.value.B = {};
        }
        checkFunctionCompatibility();
        updateCanExecute();
        alert(`Функция "${data.functionName}" загружена из JSON!`);
      } catch (err) {
        alert('Ошибка загрузки JSON: ' + (err.message || 'некорректный файл'));
        console.error(err);
      }
    };
    reader.readAsText(file);
  };
  input.click();
};
const exportFunctionToJson = (target) => {
  const func = target === 'A' ? selectedFunctionA.value : selectedFunctionB.value;
  const points = target === 'A' ? functionAPoints.value : functionBPoints.value;
  if (!func || points.length === 0) return;
  const currentPoints = points.map((p, i) => ({
    x: getXValue(p, i),
    y: getYValue(p, i, target)
  }));
  const json = JSON.stringify({
    functionName: func.functionName,
    typeFunction: 'tabular',
    points: currentPoints
  }, null, 2);
  const blob = new Blob([json], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${func.functionName || 'function'}.json`;
  link.click();
  URL.revokeObjectURL(url);
};
// --- Compatibility & Utils ---
const hasDuplicateX = (target) => {
  const pts = target === 'A' ? functionAPoints.value : functionBPoints.value;
  const xs = pts.map(p => getXValue(p));
  return new Set(xs).size !== xs.length;
};
const updateCanExecute = () => {
  canExecute.value = !!(
    selectedFunctionA.value &&
    selectedFunctionB.value &&
    functionAPoints.value.length > 0 &&
    functionBPoints.value.length > 0
  );
};
const checkFunctionCompatibility = () => {
  if (functionAPoints.value.length === 0 || functionBPoints.value.length === 0) {
    functionCompatibility.value = { isCompatible: false, warning: '', aError: '', bError: '' };
    return;
  }
  let warning = '';
  let aError = '', bError = '';
  let isCompatible = true;
  if (hasDuplicateX('A')) { aError = 'Дублирующиеся X'; isCompatible = false; }
  if (hasDuplicateX('B')) { bError = 'Дублирующиеся X'; isCompatible = false; }
  if (isCompatible) {
    const xsA = functionAPoints.value.map(p => getXValue(p));
    const xsB = functionBPoints.value.map(p => getXValue(p));
    if (xsA.length !== xsB.length) {
      warning = 'Разное количество точек';
      isCompatible = false;
    } else if (!xsA.every((x, i) => Math.abs(x - xsB[i]) < 1e-4)) {
      warning = 'X-значения не совпадают';
      isCompatible = false;
    }
  }
  functionCompatibility.value = { isCompatible, warning, aError, bError };
};
// --- UI Methods ---
const createFunction = (target) => {
  emit('create-function', target);
};
const openFunctionSelector = async (target) => {
  selectorTarget.value = target;
  showFunctionSelector.value = true;
  loadingFunctions.value = true;
  try {
    const userId = api.getStoredUserId();
    const funcs = await api.getFunctionsByUserId(userId);
    availableFunctions.value = funcs.filter(f => f.typeFunction === 'tabular');
  } catch (err) {
    console.error(err);
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
  loadFunctionPoints(func.functionId, selectorTarget.value);
  closeFunctionSelector();
};
const getSelectorTargetName = (target) => {
  return { A: 'Функция A', B: 'Функция B' }[target] || target;
};
// --- Load/Save ---
const loadFunctionPoints = async (id, target) => {
  try {
    if (target === 'A') loadingPointsA.value = true;
    if (target === 'B') loadingPointsB.value = true;
    const pointsRes = await api.getTabulatedPointsByFunctionId(id);
    const pts = pointsRes.map(p => createPointObject(parseFloat(p.xval), parseFloat(p.yval)));
    const sorted = [...pts].sort((a, b) => a.getX() - b.getX());
    const userId = api.getStoredUserId();
    const all = await api.getFunctionsByUserId(userId);
    const func = all.find(f => f.functionId === id);
    if (!func) throw new Error('Функция не найдена');
    func.pointCount = pts.length;
    if (target === 'A') {
      selectedFunctionA.value = func;
      functionAPoints.value = [...sorted];
      originalPointsA.value = sorted.map(p => createPointObject(p.getX(), p.getY()));
      tempYValues.value.A = {};
    } else {
      selectedFunctionB.value = func;
      functionBPoints.value = [...sorted];
      originalPointsB.value = sorted.map(p => createPointObject(p.getX(), p.getY()));
      tempYValues.value.B = {};
    }
    checkFunctionCompatibility();
    updateCanExecute();
  } catch (err) {
    alert('Ошибка загрузки: ' + (err.message || ''));
    console.error(err);
  } finally {
    if (target === 'A') loadingPointsA.value = false;
    if (target === 'B') loadingPointsB.value = false;
  }
};
const saveFunctionPoints = async (target) => {
  const func = target === 'A' ? selectedFunctionA.value : selectedFunctionB.value;
  const pts = target === 'A' ? functionAPoints.value : functionBPoints.value;
  if (!func || !pts.length) return;
  const xs = new Set();
  for (const p of pts) {
    const x = getXValue(p);
    if (xs.has(x)) {
      alert('Невозможно сохранить: дублирующиеся X');
      return;
    }
    xs.add(x);
  }
  try {
    await api.deleteTabulatedPointsByFunctionId(func.functionId);
    for (let i = 0; i < pts.length; i++) {
      const y = getYValue(pts[i], i, target);
      await api.createTabulatedPoints(func.functionId, getXValue(pts[i], i), y);
    }
    const updated = pts.map((p, i) => createPointObject(getXValue(p, i), getYValue(p, i, target)));
    if (target === 'A') {
      originalPointsA.value = updated;
      tempYValues.value.A = {};
    } else {
      originalPointsB.value = updated;
      tempYValues.value.B = {};
    }
    alert('Изменения сохранены!');
  } catch (err) {
    alert('Ошибка сохранения: ' + (err.message || ''));
    console.error(err);
  }
};
const clearFunction = (target) => {
  if (target === 'A') {
    selectedFunctionA.value = null;
    functionAPoints.value = [];
    originalPointsA.value = [];
    tempYValues.value.A = {};
  } else {
    selectedFunctionB.value = null;
    functionBPoints.value = [];
    originalPointsB.value = [];
    tempYValues.value.B = {};
  }
  checkFunctionCompatibility();
  updateCanExecute();
};
// --- LOCAL OPERATION HELPER (должна быть ДО executeOperation!) ---
const performLocalOperation = (pointsA, pointsB, operation) => {
  if (pointsA.length !== pointsB.length) {
    throw new Error(`Несовпадение количества точек: A=${pointsA.length}, B=${pointsB.length}`);
  }
  for (let i = 0; i < pointsA.length; i++) {
    if (Math.abs(pointsA[i].getX() - pointsB[i].getX()) > 1e-6) {
      throw new Error(`Несовпадение X в точке ${i}: A=${pointsA[i].getX()}, B=${pointsB[i].getX()}`);
    }
  }
  return pointsA.map((pA, i) => {
    const pB = pointsB[i];
    const x = pA.getX();
    let y;
    switch (operation) {
      case 'add':       y = pA.getY() + pB.getY(); break;
      case 'subtract':  y = pA.getY() - pB.getY(); break;
      case 'multiply':  y = pA.getY() * pB.getY(); break;
      case 'divide':
        if (Math.abs(pB.getY()) < 1e-12) throw new Error(`Деление на ноль при x=${x}`);
        y = pA.getY() / pB.getY();
        break;
      default:
        throw new Error(`Неизвестная операция: ${operation}`);
    }
    return { x, y };
  });
};
// --- Operations ---
const executeOperation = async (op) => {
  if (!canExecute.value || !isCompatible.value || hasDuplicateX('A') || hasDuplicateX('B')) {
    alert('Невозможно выполнить операцию: функции несовместимы или содержат дублирующиеся X-значения.');
    return;
  }
  const funcA = selectedFunctionA.value;
  const funcB = selectedFunctionB.value;
  const ptsA = functionAPoints.value.map((p, i) =>
    createPointObject(getXValue(p, i), getYValue(p, i, 'A'))
  ).sort((a, b) => a.getX() - b.getX());
  const ptsB = functionBPoints.value.map((p, i) =>
    createPointObject(getXValue(p, i), getYValue(p, i, 'B'))
  ).sort((a, b) => a.getX() - b.getX());
  const useLocalOnly = !funcA?.functionId || !funcB?.functionId;
  try {
    let resultPts;
    if (useLocalOnly) {
      resultPts = performLocalOperation(ptsA, ptsB, op);
    } else {
      const res = await api.performOperation(
        funcA.functionId,
        funcB.functionId,
        operationTypeMap[op]
      );
      resultPts = res.points.map(p => ({
        x: parseFloat(p.x !== undefined ? p.x : p.xval),
        y: parseFloat(p.y !== undefined ? p.y : p.yval)
      }));
    }
    resultPoints.value = resultPts;
    resultName.value = `Результат_${op}_${funcA?.functionName || 'A'}_${funcB?.functionName || 'B'}`;
    resultOperationType.value = operationTypeMap[op];
    alert(`Операция "${op}" успешно выполнена!`);
  } catch (err) {
    console.error('Ошибка при выполнении операции:', err);
    let msg = err.message || 'Неизвестная ошибка';
    if (err.response?.data?.message) msg = err.response.data.message;
    if (err.response?.data?.error) msg = err.response.data.error;
    alert('Ошибка операции: ' + msg);
  }
};
// --- Result ---
const exportToCSV = () => {
  if (resultPoints.value.length === 0) return;
  const csv = [
    ['X', 'Y'],
    ...resultPoints.value.map(p => [p.x.toFixed(6), p.y.toFixed(6)])
  ].map(row => row.join(',')).join('\n');
  const blob = new Blob([csv], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${resultName.value}.csv`;
  a.click();
  URL.revokeObjectURL(url);
};
const exportResultToJson = () => {
  if (resultPoints.value.length === 0) return;
  const jsonData = {
    functionName: resultName.value,
    typeFunction: 'tabular',
    points: resultPoints.value.map(point => ({
      x: point.x,
      y: point.y
    }))
  };
  const jsonStr = JSON.stringify(jsonData, null, 2);
  const blob = new Blob([jsonStr], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${resultName.value.replace(/\s+/g, '_')}.json`;
  link.click();
  URL.revokeObjectURL(url);
};
const saveResult = async () => {
  if (resultPoints.value.length === 0) return;
  try {
    const meta = await api.createFunction({
      functionName: resultName.value,
      functionExpression: `Результат операции`,
      typeFunction: 'tabular'
    });
    for (const p of resultPoints.value) {
      await api.createTabulatedPoints(meta.functionId, p.x, p.y);
    }
    alert('Результат сохранён!');
  } catch (err) {
    alert('Ошибка сохранения результата: ' + (err.message || ''));
    console.error(err);
  }
};
const clearResult = () => {
  resultPoints.value = [];
  resultName.value = '';
  resultFunctionId.value = null;
  resultOperationType.value = null;
};
// --- Lifecycle ---
onMounted(() => {
  window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && showFunctionSelector.value) closeFunctionSelector();
  });
  updateCanExecute();
  checkFunctionCompatibility();
});
onUnmounted(() => {
  window.removeEventListener('keydown', (e) => {
    if (e.key === 'Escape' && showFunctionSelector.value) closeFunctionSelector();
  });
});
watch([functionAPoints, functionBPoints], () => {
  checkFunctionCompatibility();
  updateCanExecute();
});
</script>
<style scoped>
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
.function-controls button:nth-child(3) {
  background-color: #9c27b0;
}
.function-controls button:nth-child(4) {
  background-color: #607d8b;
}
.function-controls button:hover {
  background-color: #1976d2;
}
.function-controls button:nth-child(3):hover {
  background-color: #7b1fa2;
}
.function-controls button:nth-child(4):hover {
  background-color: #546e7a;
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
.save-button, .clear-button, .export-button {
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
.export-button {
  background-color: #607d8b;
  color: white;
}
.export-button:hover {
  background-color: #546e7a;
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
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.function-section, .operations-section, .result-section {
  animation: fadeIn 0.3s ease-out;
}
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
</style>