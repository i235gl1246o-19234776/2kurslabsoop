<template>
  <div class="modal-overlay" @click.self="close">
    <div class="differentiation-window">
      <div class="window-header">
        <h2>Дифференцирование функции</h2>
        <button class="close-button" @click="close">&times;</button>
      </div>

      <div class="window-body">
        <div class="functions-container">
          <!-- Исходная функция -->
          <div class="function-section">
            <h3>Исходная функция</h3>
            <div class="function-controls">
              <button @click="createFunction('source')">Создать</button>
              <button @click="openFunctionSelector('source')">Загрузить</button>
              <button @click="loadFunctionFromJson">Загрузить из JSON</button>
              <button @click="exportFunctionToJson" :disabled="!sourceFunction || sourcePoints.length === 0">
                Экспорт в JSON
              </button>
              <button @click="saveFunction('source')" :disabled="!sourceFunction || !hasUnsavedChanges">
                Сохранить изменения
              </button>
            </div>
            <div v-if="sourceFunction" class="function-details">
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">Имя:</span>
                  <span class="info-value">{{ sourceFunction.functionName || 'Ручная функция' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">ID:</span>
                  <span class="info-value">{{ sourceFunction.functionId || 'Не сохранено' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Точек:</span>
                  <span class="info-value">{{ sourcePoints.length }}</span>
                </div>
              </div>
              <p v-if="sourceError" class="error-message">{{ sourceError }}</p>
              <button @click="clearFunction('source')" class="clear-button">Очистить</button>
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
                  <tr v-for="(point, index) in sourcePoints" :key="index">
                    <td>{{ getXValue(point, index) }}</td>
                    <td>
                      <input
                        type="number"
                        :value="getYValue(point, index)"
                        @input="event => handleYInput('source', index, event.target.value)"
                        @change="event => setYValue('source', index, parseFloat(event.target.value))"
                        class="point-y-input"
                        :disabled="!sourceFunction"
                      />
                    </td>
                  </tr>
                  <tr v-if="sourcePoints.length === 0">
                    <td colspan="2" class="empty-table">Нет точек для отображения</td>
                  </tr>
                </tbody>
              </table>
              <div v-if="hasDuplicateX('source')" class="error-message">
                Ошибка: обнаружены дублирующиеся X-значения.
                Для корректного дифференцирования X-значения должны быть уникальными и упорядоченными.
              </div>
            </div>
          </div>

          <!-- Производная -->
          <div class="function-section">
            <h3>Производная</h3>
            <div class="function-controls">
              <button
                @click="differentiate"
                :disabled="!canDifferentiate || hasDuplicateX('source')"
                class="operation-button derivative"
              >
                Дифференцировать
              </button>
              <button @click="saveResult" :disabled="resultPoints.length === 0" class="save-button">Сохранить результат</button>
              <button @click="exportResultToJson" :disabled="resultPoints.length === 0" class="export-button">
                Экспорт в JSON
              </button>
              <button @click="clearResult" class="clear-button">Очистить результат</button>
            </div>
            <div class="result-table">
              <h4>Результат дифференцирования</h4>
              <table>
                <thead>
                  <tr>
                    <th>X</th>
                    <th>Y (производная)</th>
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
          </div>
        </div>
      </div>

      <!-- Модальное окно выбора функции -->
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { api } from '../api.js';

const emit = defineEmits(['close']);

// Состояния
const sourceFunction = ref(null);
const sourcePoints = ref([]);
const resultPoints = ref([]);
const availableFunctions = ref([]);
const showFunctionSelector = ref(false);
const selectorTarget = ref(null);
const loadingFunctions = ref(false);
const originalPoints = ref([]);
const tempYValues = ref({});
const sourceError = ref('');

// Вспомогательные функции
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

const getYValue = (point, index) => {
  if (tempYValues.value[index] !== undefined) return tempYValues.value[index];
  if (point && typeof point.getY === 'function') return point.getY();
  return point?.y ?? 0;
};

const handleYInput = (target, index, value) => {
  tempYValues.value[index] = value;
};

const setYValue = (target, index, newValue) => {
  if (isNaN(newValue)) return;
  if (sourcePoints.value[index] && typeof sourcePoints.value[index].setY === 'function') {
    sourcePoints.value[index].setY(newValue);
  }
};

// Проверка дубликатов X
const hasDuplicateX = (target) => {
  const points = sourcePoints.value;
  const xValues = new Set();
  for (const point of points) {
    const x = getXValue(point);
    if (xValues.has(x)) return true;
    xValues.add(x);
  }
  return false;
};

// Отслеживание изменений
const hasUnsavedChanges = computed(() => {
  if (!sourceFunction.value || sourcePoints.value.length === 0) return false;
  if (Object.keys(tempYValues.value).length > 0) return true;
  return sourcePoints.value.some((point, index) => {
    const original = originalPoints.value[index];
    return original && Math.abs(getYValue(point, index) - original.getY()) > 0.0001;
  });
});

const canDifferentiate = computed(() => {
  return sourceFunction.value &&
         sourcePoints.value.length >= 2 &&
         !hasDuplicateX('source');
});

// Загрузка функции
const loadFunctionPoints = async (functionId, target) => {
  try {
    const userId = api.getStoredUserId();
    const functions = await api.getFunctionsByUserId(userId);
    const func = functions.find(f => f.functionId === functionId);
    if (!func) throw new Error('Функция не найдена');

    const pointsResponse = await api.getTabulatedPointsByFunctionId(functionId);
    const points = pointsResponse.map(p => createPointObject(
      parseFloat(p.xval),
      parseFloat(p.yval)
    )).sort((a, b) => a.getX() - b.getX());

    func.pointCount = points.length;
    sourceFunction.value = func;
    sourcePoints.value = [...points];
    originalPoints.value = points.map(p => createPointObject(p.getX(), p.getY()));
    tempYValues.value = {};
    sourceError.value = '';
  } catch (e) {
    console.error('Ошибка загрузки функции:', e);
    alert(e.message || 'Неизвестная ошибка');
  }
};

// Открытие селектора
const openFunctionSelector = (target) => {
  selectorTarget.value = target;
  showFunctionSelector.value = true;
  loadAvailableFunctions();
};

const closeFunctionSelector = () => {
  showFunctionSelector.value = false;
  selectorTarget.value = null;
};

const loadAvailableFunctions = async () => {
  try {
    loadingFunctions.value = true;
    const userId = api.getStoredUserId();
    const functions = await api.getFunctionsByUserId(userId);
    const functionsWithPoints = await Promise.all(functions.map(async (func) => {
      try {
        const points = await api.getTabulatedPointsByFunctionId(func.functionId);
        return { ...func, pointCount: points.length };
      } catch (e) {
        return { ...func, pointCount: 0 };
      }
    }));
    availableFunctions.value = functionsWithPoints;
  } catch (e) {
    console.error('Ошибка загрузки функций:', e);
    availableFunctions.value = [];
    alert(e.message || 'Не удалось загрузить функции');
  } finally {
    loadingFunctions.value = false;
  }
};

const selectFunction = (func) => {
  loadFunctionPoints(func.functionId, 'source');
  closeFunctionSelector();
};

// Создание новой функции
const createFunction = (operand) => {
  window.dispatchEvent(new CustomEvent('open-create-function', { detail: { operand } }));
};

const handleFunctionCreated = (event) => {
  const { operand, points, functionId, functionName } = event.detail;
  if (operand !== 'source') return;
  if (functionId) {
    loadFunctionPoints(functionId, 'source');
  } else {
    const pointObjects = points.map(p => createPointObject(p.x, p.y));
    sourceFunction.value = { functionName: 'Новая функция (локальная)', functionId: null };
    sourcePoints.value = [...pointObjects];
    originalPoints.value = pointObjects.map(p => createPointObject(p.getX(), p.getY()));
    tempYValues.value = {};
  }
};

// Сохранение функции
const saveFunction = async (target) => {
  if (!sourceFunction.value || !hasUnsavedChanges.value) return;

  const xValues = new Set();
  for (const point of sourcePoints.value) {
    const x = getXValue(point);
    if (xValues.has(x)) {
      alert('Невозможно сохранить: обнаружены дублирующиеся X-значения.');
      return;
    }
    xValues.add(x);
  }

  try {
    const functionName = sourceFunction.value.functionName || `Функция_${Date.now()}`;
    const funcMeta = await api.createFunction({
      functionName,
      functionExpression: 'manual',
      typeFunction: 'tabular'
    });

    for (let i = 0; i < sourcePoints.value.length; i++) {
      const yVal = tempYValues.value[i] !== undefined
        ? parseFloat(tempYValues.value[i])
        : getYValue(sourcePoints.value[i], i);
      await api.createTabulatedPoints(funcMeta.functionId, getXValue(sourcePoints.value[i], i), yVal);
    }

    sourceFunction.value.functionId = funcMeta.functionId;
    originalPoints.value = sourcePoints.value.map((p, i) => {
      const y = tempYValues.value[i] !== undefined
        ? parseFloat(tempYValues.value[i])
        : getYValue(p, i);
      return createPointObject(getXValue(p, i), y);
    });
    tempYValues.value = {};
    alert('Функция успешно сохранена!');
  } catch (e) {
    console.error('Ошибка сохранения:', e);
    alert(`Ошибка сохранения функции: ${e.message}`);
  }
};

// --- JSON import/export ---
const loadFunctionFromJson = () => {
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
        sourceFunction.value = fakeFunc;
        sourcePoints.value = [...sorted];
        originalPoints.value = sorted.map(p => createPointObject(p.getX(), p.getY()));
        tempYValues.value = {};
        sourceError.value = '';
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

const exportFunctionToJson = () => {
  if (!sourceFunction.value || sourcePoints.value.length === 0) return;

  const currentPoints = sourcePoints.value.map((p, i) => ({
    x: getXValue(p, i),
    y: getYValue(p, i)
  }));

  const json = JSON.stringify({
    functionName: sourceFunction.value.functionName,
    typeFunction: 'tabular',
    points: currentPoints
  }, null, 2);

  const blob = new Blob([json], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${sourceFunction.value.functionName || 'function'}.json`;
  link.click();
  URL.revokeObjectURL(url);
};

const exportResultToJson = () => {
  if (resultPoints.value.length === 0) return;

  const jsonData = {
    functionName: `Производная от ${sourceFunction.value?.functionName || 'неизвестной функции'}`,
    typeFunction: 'tabular',
    points: resultPoints.value.map(p => ({
      x: p.x,
      y: p.y
    }))
  };

  const jsonStr = JSON.stringify(jsonData, null, 2);
  const blob = new Blob([jsonStr], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `derivative_${sourceFunction.value?.functionName || 'result'}.json`;
  link.click();
  URL.revokeObjectURL(url);
};

// Дифференцирование
const differentiate = async () => {
  if (!canDifferentiate.value || hasDuplicateX('source')) {
    alert('Невозможно выполнить дифференцирование: проверьте корректность исходной функции.');
    return;
  }

  let pointsForDiff = [...sourcePoints.value];
  if (Object.keys(tempYValues.value).length > 0) {
    pointsForDiff = sourcePoints.value.map((point, i) => {
      const y = tempYValues.value[i] !== undefined ? parseFloat(tempYValues.value[i]) : getYValue(point, i);
      return createPointObject(getXValue(point, i), y);
    });
  }
  pointsForDiff.sort((a, b) => a.getX() - b.getX());

  try {
    let resultPts;
    if (!sourceFunction.value?.functionId || hasUnsavedChanges.value) {
      resultPts = performLocalDifferentiation(pointsForDiff);
    } else {
      const res = await fetch('/api/operations/differentiate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Basic ${api.getStoredCredentials()}`
        },
        body: JSON.stringify({
          functionId: sourceFunction.value.functionId,
          factoryType: localStorage.getItem('tabulatedFunctionFactory') || 'array'
        })
      });
      if (!res.ok) {
        const err = await res.json();
        throw new Error(err.error || 'Ошибка сервера');
      }
      const response = await res.json();
      resultPts = response.points.map(p => ({
        x: parseFloat(p.x !== undefined ? p.x : p.xval),
        y: parseFloat(p.y !== undefined ? p.y : p.yval)
      }));
    }

    resultPoints.value = resultPts;
  } catch (e) {
    console.error('Ошибка дифференцирования:', e);
    alert(`Ошибка: ${e.message}`);
  }
};

const performLocalDifferentiation = (points) => {
  if (points.length < 2) throw new Error('Требуется минимум 2 точки');
  const result = [];
  for (let i = 1; i < points.length - 1; i++) {
    const xPrev = points[i - 1].getX();
    const xNext = points[i + 1].getX();
    const yPrev = points[i - 1].getY();
    const yNext = points[i + 1].getY();
    const dx = xNext - xPrev;
    if (Math.abs(dx) < 1e-10) throw new Error('Нулевой шаг по X');
    const dydx = (yNext - yPrev) / dx;
    result.push({ x: points[i].getX(), y: dydx });
  }
  return result;
};

// Очистка
const clearFunction = (target) => {
  if (target === 'source') {
    sourceFunction.value = null;
    sourcePoints.value = [];
    originalPoints.value = [];
    tempYValues.value = {};
    sourceError.value = '';
  }
};

const clearResult = () => {
  resultPoints.value = [];
};

const saveResult = async () => {
  if (resultPoints.value.length === 0) {
    alert('Нет данных для сохранения');
    return;
  }

  try {
    const functionName = `Производная от ${sourceFunction.value?.functionName || 'неизвестной функции'}`;
    const funcMeta = await api.createFunction({
      functionName,
      functionExpression: 'Производная',
      typeFunction: 'tabular'
    });
    for (const p of resultPoints.value) {
      await api.createTabulatedPoints(funcMeta.functionId, p.x, p.y);
    }
    alert(`Производная сохранена с ID: ${funcMeta.functionId}`);
  } catch (e) {
    console.error('Ошибка сохранения результата:', e);
    alert(`Ошибка: ${e.message}`);
  }
};

const close = () => {
  emit('close');
};

// Подписки
onMounted(() => {
  window.addEventListener('function-created', handleFunctionCreated);
});

onUnmounted(() => {
  window.removeEventListener('function-created', handleFunctionCreated);
});
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.differentiation-window {
  position: relative;
  padding: 25px 20px;
  border-radius: 16px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.6);
  max-width: 1200px;
  max-height: 90vh;
  margin: 20px auto;
  color: #ffffff;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: #1a0a2e;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Крестик */
.close-button {
  position: absolute;
  top: 12px;
  right: 12px;
  cursor: pointer;
  font-size: 24px;
  color: #ffffff;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
  z-index: 10;
  background: none;
  border: none;
}
.close-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
  color: #ff6fda;
  transform: rotate(90deg);
}

.window-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #5b1fa8;
}

h2 {
  color: #ffffff;
  margin: 0;
  font-size: 1.8rem;
}

h3, h4 {
  color: #ffffff;
  margin: 0 0 1rem 0;
}

.window-body {
  flex: 1;
  overflow-y: auto;
  padding: 0 10px;
}

.functions-container {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.function-section {
  flex: 1;
  padding: 20px;
  border: 1px solid #5b1fa8;
  border-radius: 12px;
  background: rgba(47, 16, 92, 0.3);
}

.function-controls {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.function-controls button {
  padding: 10px 15px;
  background-color: #5b1fa8;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.25s ease;
  font-weight: 500;
}

.function-controls button:nth-child(3) {
  background-color: #9c27b0;
}
.function-controls button:nth-child(4) {
  background-color: #607d8b;
}

.function-controls button:hover {
  background-color: #7b1fa8;
}
.function-controls button:nth-child(3):hover {
  background-color: #7b1fa2;
}
.function-controls button:nth-child(4):hover {
  background-color: #546e7a;
}

.function-details {
  background: rgba(91, 31, 168, 0.2);
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #5b1fa8;
  margin-top: 10px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-weight: 600;
  color: #cccccc;
  font-size: 0.9rem;
}

.info-value {
  color: #ffffff;
  font-weight: 500;
}

.error-message {
  color: #ff4fc4;
  font-size: 0.9em;
  margin: 5px 0;
}

.function-table,
.result-table {
  margin-top: 15px;
  border: 1px solid #5b1fa8;
  border-radius: 8px;
  overflow: hidden;
  background: rgba(47, 16, 92, 0.3);
}

.function-table h4,
.result-table h4 {
  margin: 0;
  padding: 12px;
  background: rgba(91, 31, 168, 0.4);
  border-bottom: 1px solid #5b1fa8;
  color: #ffffff;
}

.empty-table {
  text-align: center;
  padding: 20px;
  color: #cccccc;
}

.point-y-input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #5b1fa8;
  border-radius: 6px;
  font-size: 14px;
  background-color: #2f105c;
  color: #ffffff;
  transition: border-color 0.25s ease;
}

.point-y-input:focus {
  outline: none;
  border-color: #ff4fc4;
  box-shadow: 0 0 0 2px rgba(255, 79, 196, 0.2);
}

/* Убираем стрелки у number input */
.point-y-input[type=number]::-webkit-outer-spin-button,
.point-y-input[type=number]::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.point-y-input[type=number] {
  -moz-appearance: textfield;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 300px;
}

table th,
table td {
  border: 1px solid #5b1fa8;
  padding: 10px;
  text-align: left;
}

table th {
  background-color: #2f105c;
  color: #ffffff;
  font-weight: 600;
}

table td {
  background-color: rgba(35, 9, 66, 0.5);
  color: #ffffff;
}

.clear-button {
  background-color: #e74c3c;
  color: white;
  padding: 8px 15px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.25s ease;
}

.clear-button:hover {
  background-color: #c0392b;
}

.save-button {
  background-color: #4caf50;
  color: white;
}

.save-button:hover:not(:disabled) {
  background-color: #45a049;
}

.save-button:disabled {
  background-color: #888;
  cursor: not-allowed;
}

.export-button {
  background-color: #607d8b;
  color: white;
}

.export-button:hover {
  background-color: #546e7a;
}

.operation-button.derivative {
  background-color: #2196f3;
  font-weight: 600;
}

.operation-button.derivative:hover:not(:disabled) {
  background-color: #1976d2;
}

.operation-button.derivative:disabled {
  background-color: #888;
  cursor: not-allowed;
}

.function-selector-modal {
  background: #1a0a2e;
  border-radius: 16px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.6);
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  border: 1px solid #5b1fa8;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #5b1fa8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(47, 16, 92, 0.3);
}

.modal-header h3 {
  margin: 0;
  color: #ffffff;
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
  border-bottom: 1px solid #5b1fa8;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 8px;
  margin-bottom: 8px;
  background: rgba(47, 16, 92, 0.3);
}

.function-item:hover {
  background-color: rgba(91, 31, 168, 0.4);
  transform: translateX(5px);
}

.function-id {
  color: #cccccc;
  font-size: 0.9rem;
  margin-left: 8px;
}

.function-meta {
  display: flex;
  gap: 15px;
  margin-top: 8px;
  font-size: 0.85rem;
  color: #cccccc;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #5b1fa8;
  text-align: right;
  background: rgba(47, 16, 92, 0.3);
  border-radius: 0 0 16px 16px;
}

.cancel-button {
  padding: 10px 20px;
  background-color: #5b1fa8;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.25s ease;
  color: #ffffff;
}

.cancel-button:hover {
  background-color: #7b1fa8;
}

@media (max-width: 768px) {
  .differentiation-window {
    width: 95%;
    margin: 10px;
    max-height: 95vh;
    padding: 20px 15px;
  }

  .functions-container {
    flex-direction: column;
    gap: 20px;
  }

  .function-section {
    padding: 15px;
  }

  .function-controls {
    flex-direction: column;
  }

  .function-controls button {
    width: 100%;
  }

  table {
    font-size: 0.9em;
  }

  table th, table td {
    padding: 8px;
  }

  .window-body {
    max-height: calc(95vh - 100px);
  }
}

@media (max-width: 480px) {
  .differentiation-window {
    padding: 15px 10px;
  }

  .function-section {
    padding: 12px;
  }

  .modal-body {
    padding: 15px;
  }

  .function-item {
    padding: 12px;
  }
}
</style>