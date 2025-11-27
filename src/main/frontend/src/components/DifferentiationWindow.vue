<template>
  <div v-if="show" class="differentiation-window">
    <div class="window-header">
      <h2>Дифференцирование функции</h2>
      <button class="close-button" @click="$emit('close')">&times;</button>
    </div>
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
          <p><strong>Имя:</strong> {{ sourceFunction.functionName || 'Ручная функция' }}</p>
          <p><strong>ID:</strong> {{ sourceFunction.id || 'Не сохранено' }}</p>
          <p><strong>Точек:</strong> {{ sourcePoints.length }}</p>
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
              :key="func.id"
              @click="selectFunction(func)"
              class="function-item"
            >
              <div>
                <strong>{{ func.functionName }}</strong>
                <span class="function-id">(ID: {{ func.id }})</span>
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
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { api } from '../api.js';

const props = defineProps({
  show: Boolean
});

const emit = defineEmits(['close']);

// Состояние
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
const isDifferentiating = ref(false);

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

const handleYInput = (target, index) => {
  return (value) => {
    tempYValues.value[index] = value;
  };
};

const setYValue = (target, index) => {
  return (newValue) => {
    if (isNaN(newValue)) return;
    if (sourcePoints.value[index] && typeof sourcePoints.value[index].setY === 'function') {
      sourcePoints.value[index].setY(newValue);
    }
  };
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
    const response = await fetch(`/api/functions?userId=${userId}`, {
      headers: {
        'Authorization': `Basic ${api.getStoredCredentials()}`
      }
    });

    if (!response.ok) {
      throw new Error(`Ошибка загрузки функций: ${response.status}`);
    }

    const functions = await response.json();
    const func = functions.find(f => f.id === functionId);

    if (!func) throw new Error('Функция не найдена');

    const pointsResponse = await fetch(`/api/tabulated-points/function/${functionId}`, {
      headers: {
        'Authorization': `Basic ${api.getStoredCredentials()}`
      }
    });

    if (!pointsResponse.ok) {
      throw new Error(`Ошибка загрузки точек: ${pointsResponse.status}`);
    }

    const pointsData = await pointsResponse.json();
    const points = pointsData.map(p => createPointObject(
      parseFloat(p.xVal),
      parseFloat(p.yVal)
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

    const response = await fetch(`/api/functions?userId=${userId}`, {
      headers: {
        'Authorization': `Basic ${api.getStoredCredentials()}`
      }
    });

    if (!response.ok) {
      throw new Error(`Ошибка загрузки функций: ${response.status}`);
    }

    const functions = await response.json();
    const functionsWithPoints = await Promise.all(functions.map(async (func) => {
      try {
        const pointsResponse = await fetch(`/api/tabulated-points/function/${func.id}`, {
          headers: {
            'Authorization': `Basic ${api.getStoredCredentials()}`
          }
        });

        if (!pointsResponse.ok) {
          throw new Error(`Ошибка загрузки точек для функции ${func.id}: ${pointsResponse.status}`);
        }

        const pointsData = await pointsResponse.json();
        return { ...func, pointCount: pointsData.length };
      } catch (e) {
        console.error('Ошибка загрузки точек для функции:', e);
        return { ...func, pointCount: 0 };
      }
    }));

    availableFunctions.value = functionsWithPoints.filter(f => f.typeFunction === 'tabular');

  } catch (e) {
    console.error('Ошибка загрузки функций:', e);
    availableFunctions.value = [];
    alert(e.message || 'Не удалось загрузить функции');
  } finally {
    loadingFunctions.value = false;
  }
};

const selectFunction = (func) => {
  loadFunctionPoints(func.id, 'source');
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
    sourceFunction.value = {
      functionName: 'Новая функция (локальная)',
      id: null,
      typeFunction: 'tabular'
    };
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
    const userId = api.getStoredUserId();
    const functionName = sourceFunction.value.functionName || `Функция_${Date.now()}`;
    const functionData = {
      userId: userId,
      functionName: functionName,
      functionExpression: 'manual',
      typeFunction: 'tabular'
    };

    const response = await fetch('/api/functions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${api.getStoredCredentials()}`
      },
      body: JSON.stringify(functionData)
    });

    if (!response.ok) {
      throw new Error(`Ошибка создания функции: ${response.status}`);
    }

    const funcMeta = await response.json();
    const functionId = funcMeta.id;

    // Сохраняем все точки функции
    for (let i = 0; i < sourcePoints.value.length; i++) {
      const yVal = tempYValues.value[i] !== undefined
        ? parseFloat(tempYValues.value[i])
        : getYValue(sourcePoints.value[i], i);

      const pointData = {
        functionId: functionId,
        xVal: getXValue(sourcePoints.value[i], i),
        yVal: yVal
      };

      const pointResponse = await fetch('/api/tabulated-points', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Basic ${api.getStoredCredentials()}`
        },
        body: JSON.stringify(pointData)
      });

      if (!pointResponse.ok) {
        throw new Error(`Ошибка сохранения точки ${i}: ${pointResponse.status}`);
      }
    }

    sourceFunction.value.id = functionId;
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
          id: null,
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

  isDifferentiating.value = true;
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

    // Проверяем, есть ли ID функции и есть ли несохраненные изменения
    if (!sourceFunction.value?.id || hasUnsavedChanges.value) {
      // Выполняем локальное дифференцирование
      resultPts = performLocalDifferentiation(pointsForDiff);
    } else {
      // Выполняем дифференцирование на сервере
      const factoryType = localStorage.getItem('tabulatedFunctionFactory') || 'array';

      const response = await fetch('/api/operations/differentiate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Basic ${api.getStoredCredentials()}`
        },
        body: JSON.stringify({
          functionId: sourceFunction.value.id,
          factoryType: factoryType
        })
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || `Ошибка сервера: ${response.status}`);
      }

      const serverResponse = await response.json();
      resultPts = serverResponse.points.map(p => ({
        x: parseFloat(p.x !== undefined ? p.x : p.xVal),
        y: parseFloat(p.y !== undefined ? p.y : p.yVal)
      }));
    }

    resultPoints.value = resultPts;

  } catch (e) {
    console.error('Ошибка дифференцирования:', e);
    alert(`Ошибка: ${e.message}`);
  } finally {
    isDifferentiating.value = false;
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
    const userId = api.getStoredUserId();
    const functionName = `Производная от ${sourceFunction.value?.functionName || 'неизвестной функции'}`;

    const functionData = {
      userId: userId,
      functionName: functionName,
      functionExpression: 'Производная',
      typeFunction: 'tabular'
    };

    const functionResponse = await fetch('/api/functions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${api.getStoredCredentials()}`
      },
      body: JSON.stringify(functionData)
    });

    if (!functionResponse.ok) {
      throw new Error(`Ошибка создания функции: ${functionResponse.status}`);
    }

    const funcMeta = await functionResponse.json();
    const functionId = funcMeta.id;

    // Сохраняем точки производной
    for (const p of resultPoints.value) {
      const pointData = {
        functionId: functionId,
        xVal: p.x,
        yVal: p.y
      };

      const pointResponse = await fetch('/api/tabulated-points', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Basic ${api.getStoredCredentials()}`
        },
        body: JSON.stringify(pointData)
      });

      if (!pointResponse.ok) {
        throw new Error(`Ошибка сохранения точки: ${pointResponse.status}`);
      }
    }

    alert(`Производная сохранена с ID: ${functionId}`);

  } catch (e) {
    console.error('Ошибка сохранения результата:', e);
    alert(`Ошибка: ${e.message}`);
  }
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
.differentiation-window {
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

.function-table,
.result-table {
  margin-top: 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  overflow: hidden;
}

.function-table h4,
.result-table h4 {
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

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 300px;
}

table th,
table td {
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

.clear-button {
  background-color: #f44336;
  color: white;
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.clear-button:hover {
  background-color: #e53935;
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

.export-button {
  background-color: #607d8b;
  color: white;
}

.export-button:hover {
  background-color: #546e7a;
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
}

.modal-header {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8f9fa;
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

@media (max-width: 768px) {
  .functions-container {
    flex-direction: column;
  }
}
</style>