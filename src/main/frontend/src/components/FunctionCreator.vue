<template>
  <div class="creator">
    <!-- Крестик для закрытия окна -->
    <div class="close-button" @click="$emit('close')">&times;</div>
    <h2>Создать функцию</h2>
    <div class="tabs">
      <button
        @click="activeTab = 'fromArrays'"
        :class="{ active: activeTab === 'fromArrays' }"
      >
        Из массивов X и Y
      </button>
      <button
        @click="activeTab = 'fromFunction'"
        :class="{ active: activeTab === 'fromFunction' }"
      >
        Из функции MathFunction
      </button>
    </div>
    <div v-if="activeTab === 'fromArrays'">
      <h3>Создание из массивов</h3>
      <!-- Добавляем валидацию для pointCount -->
      <input
        v-model.number="pointCount"
        type="number"
        placeholder="Количество точек"
        min="1"
        @blur="validatePointCount"
        :class="{ 'error-input': pointCountError }"
      />
      <span v-if="pointCountError" class="error-message">{{ pointCountError }}</span>
      <button @click="generateTable" class="generate-button">Сгенерировать таблицу</button>
      <table v-if="points.length > 0">
        <thead>
          <tr>
            <th>X</th>
            <th>Y</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(point, index) in points" :key="index">
            <td>
              <input
                v-model.number="point.x"
                type="number"
                @blur="validatePointValue(point, 'x', index)"
                :class="{ 'error-input': pointErrors[index] && pointErrors[index].x }"
              />
              <span v-if="pointErrors[index] && pointErrors[index].x" class="error-message">{{ pointErrors[index].x }}</span>
            </td>
            <td>
              <input
                v-model.number="point.y"
                type="number"
                @blur="validatePointValue(point, 'y', index)"
                :class="{ 'error-input': pointErrors[index] && pointErrors[index].y }"
              />
              <span v-if="pointErrors[index] && pointErrors[index].y" class="error-message">{{ pointErrors[index].y }}</span>
            </td>
          </tr>
        </tbody>
      </table>
      <!-- Валидация для functionName -->
      <input
        v-model="functionName"
        type="text"
        placeholder="Название функции"
        @blur="validateFunctionName"
        :class="{ 'error-input': functionNameError }"
      />
      <span v-if="functionNameError" class="error-message">{{ functionNameError }}</span>
      <input v-model="functionExpression" type="text" placeholder="Выражение функции (опционально)" />
      <!-- Выбор типа функции только если НЕ создаем для операций -->
      <select v-if="!isForOperation" v-model="typeFunction">
        <option value="tabular">Табулированная</option>
        <option value="analytic">Аналитическая</option>
      </select>
      <!-- Кнопки JSON -->
      <div class="json-controls">
        <button @click="loadFromJson" class="json-button secondary">
          <i class="fas fa-upload"></i> Загрузить из JSON
        </button>
        <button @click="saveAsJson" class="json-button secondary" :disabled="!canExport">
          <i class="fas fa-download"></i> Сохранить как JSON
        </button>
      </div>
      <button @click="createFunctionFromArrays" class="create-button">Создать</button>
    </div>
    <div v-if="activeTab === 'fromFunction'">
      <h3>Создание из функции</h3>
      <select v-model="selectedFunctionName">
        <option disabled value="">Выберите функцию</option>
        <option v-for="name in sortedFunctionNames" :key="name" :value="name">{{ name }}</option>
      </select>
      <!-- Валидация для pointCount в этой вкладке тоже -->
      <input
        v-model.number="pointCountFromFunction"
        type="number"
        placeholder="Количество точек"
        min="1"
        @blur="validatePointCountFromFunction"
        :class="{ 'error-input': pointCountFromFunctionError }"
      />
      <span v-if="pointCountFromFunctionError" class="error-message">{{ pointCountFromFunctionError }}</span>
      <input v-model.number="startXFromFunction" type="number" placeholder="Начало интервала X" />
      <input v-model.number="endXFromFunction" type="number" placeholder="Конец интервала X" />
      <!-- Валидация для functionName в этой вкладке -->
      <input
        v-model="functionNameFromFunction"
        type="text"
        placeholder="Название функции"
        @blur="validateFunctionNameFromFunction"
        :class="{ 'error-input': functionNameFromFunctionError }"
      />
      <span v-if="functionNameFromFunctionError" class="error-message">{{ functionNameFromFunctionError }}</span>
      <input v-model="functionExpressionFromFunction" type="text" placeholder="Выражение функции (опционально)" />
      <!-- Выбор типа функции только если НЕ создаем для операций -->
      <select v-if="!isForOperation" v-model="typeFunctionFromFunction">
        <option value="tabular">Табулированная</option>
        <option value="analytic">Аналитическая</option>
      </select>
      <!-- Кнопки JSON -->
      <div class="json-controls">
        <button @click="loadFromJson" class="json-button secondary">
          <i class="fas fa-upload"></i> Загрузить из JSON
        </button>
        <button @click="saveAsJson" class="json-button secondary" :disabled="!canExport">
          <i class="fas fa-download"></i> Сохранить как JSON
        </button>
      </div>
      <button @click="createFunctionFromMathFunction" class="create-button">Создать</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, inject, watch } from 'vue';
import { api } from '../api.js';

// Объявляем события, которые компонент может эмитить
const emit = defineEmits(['close', 'function-created']);

// Добавляем пропс для определения контекста создания
const props = defineProps({
  isForOperation: {
    type: Boolean,
    default: false
  }
});

// --- ИНЪЕКТИРУЕМ ФУНКЦИЮ showError ИЗ App.vue ---
const showError = inject('showError');
if (!showError) {
  console.error("FunctionCreator: 'showError' function not provided by parent component.");
}
// --- КОНЕЦ ИНЪЕКЦИИ ---

const activeTab = ref('fromArrays');
const pointCount = ref(0);
const points = ref([]);
const functionName = ref('');
const functionExpression = ref('');
const typeFunction = ref('tabular');
const selectedFunctionName = ref('');

// --- НОВЫЕ ПЕРЕМЕННЫЕ ДЛЯ ВАЛИДАЦИИ И ВКЛАДКИ 'fromFunction' ---
const pointCountFromFunction = ref(0);
const startXFromFunction = ref(0);
const endXFromFunction = ref(1);
const functionNameFromFunction = ref('');
const functionExpressionFromFunction = ref('');
const typeFunctionFromFunction = ref('tabular');

// --- КОНЕЦ НОВЫХ ПЕРЕМЕННЫХ ---

// --- СОСТОЯНИЯ ДЛЯ ОШИБОК ВАЛИДАЦИИ ---
const pointCountError = ref('');
const functionNameError = ref('');
const pointCountFromFunctionError = ref('');
const functionNameFromFunctionError = ref('');

// --- КОНЕЦ СОСТОЯНИЙ ОШИБОК ---

// --- СОСТОЯНИЕ ДЛЯ ОШИБОК ТОЧЕК ---
const pointErrors = ref({});

// --- КОНЕЦ СОСТОЯНИЯ ---

// Маппинг названий функций
const functionMap = {
  "Квадратичная функция": "SqrFunction",
  "Тождественная функция": "IdentityFunction",
};

// Сортированный список названий функций
const sortedFunctionNames = computed(() => Object.keys(functionMap));

// Вычисляемое свойство: можно ли экспортировать
const canExport = computed(() => {
  return (
    (activeTab.value === 'fromArrays' && points.value.length > 0 && functionName.value) ||
    (activeTab.value === 'fromFunction' && functionNameFromFunction.value)
  );
});

// Устанавливаем тип функции как табулированную, если создаем для операций
watch(() => props.isForOperation, (isForOperation) => {
  if (isForOperation) {
    typeFunction.value = 'tabular';
    typeFunctionFromFunction.value = 'tabular';
  }
}, { immediate: true });

// --- ФУНКЦИИ ВАЛИДАЦИИ ---
const validatePointCount = () => {
  if (pointCount.value === '' || pointCount.value === null || pointCount.value === undefined) {
    pointCountError.value = 'Количество точек обязательно.';
    return false;
  }
  if (isNaN(pointCount.value) || pointCount.value < 1) {
    pointCountError.value = 'Количество точек должно быть положительным числом.';
    return false;
  }
  pointCountError.value = '';
  return true;
};

const validatePointValue = (point, coord, index) => {
  if (!pointErrors.value[index]) pointErrors.value[index] = {};
  if (point[coord] === '' || point[coord] === null || point[coord] === undefined) {
    pointErrors.value[index][coord] = `Значение ${coord.toUpperCase()} обязательно.`;
    return false;
  }
  if (isNaN(point[coord])) {
    pointErrors.value[index][coord] = `Значение ${coord.toUpperCase()} должно быть числом.`;
    return false;
  }
  pointErrors.value[index][coord] = '';
  return true;
};

const validateFunctionName = () => {
  if (!functionName.value.trim()) {
    functionNameError.value = 'Название функции обязательно.';
    return false;
  }
  if (functionName.value.trim().length > 255) {
    functionNameError.value = 'Название функции слишком длинное.';
    return false;
  }
  functionNameError.value = '';
  return true;
};

const validatePointCountFromFunction = () => {
  if (pointCountFromFunction.value === '' || pointCountFromFunction.value === null || pointCountFromFunction.value === undefined) {
    pointCountFromFunctionError.value = 'Количество точек обязательно.';
    return false;
  }
  if (isNaN(pointCountFromFunction.value) || pointCountFromFunction.value < 1) {
    pointCountFromFunctionError.value = 'Количество точек должно быть положительным числом.';
    return false;
  }
  pointCountFromFunctionError.value = '';
  return true;
};

const validateFunctionNameFromFunction = () => {
  if (!functionNameFromFunction.value.trim()) {
    functionNameFromFunctionError.value = 'Название функции обязательно.';
    return false;
  }
  if (functionNameFromFunction.value.trim().length > 255) {
    functionNameFromFunctionError.value = 'Название функции слишком длинное.';
    return false;
  }
  functionNameFromFunctionError.value = '';
  return true;
};

// --- КОНЕЦ ФУНКЦИЙ ВАЛИДАЦИИ ---

const generateTable = () => {
  if (!validatePointCount()) return;
  if (pointCount.value > 100) {
    if (!confirm(`Вы ввели ${pointCount.value} точек. Это может быть неудобно. Продолжить?`)) {
      return;
    }
  }
  points.value = Array.from({ length: pointCount.value }, () => ({ x: 0, y: 0 }));
  pointErrors.value = {};
};

// --- ФУНКЦИЯ СОЗДАНИЯ ФУНКЦИИ ИЗ МАССИВОВ ---
const createFunctionFromArrays = async () => {
  const isPointCountValid = validatePointCount();
  const isFunctionNameValid = validateFunctionName();
  let allPointsValid = true;
  points.value.forEach((point, index) => {
    const xValid = validatePointValue(point, 'x', index);
    const yValid = validatePointValue(point, 'y', index);
    if (!xValid || !yValid) allPointsValid = false;
  });

  if (!isPointCountValid || !isFunctionNameValid || !allPointsValid) {
    showError('Пожалуйста, исправьте ошибки в форме перед отправкой.');
    return;
  }

  const currentFunctionName = functionName.value;
  const actualType = props.isForOperation ? 'tabular' : typeFunction.value;

  try {
    const functionData = {
      functionName: currentFunctionName,
      functionExpression: functionExpression.value,
      typeFunction: actualType,
    };

    const functionResponse = await api.createFunction(functionData);
    console.log("Запрос на создание функции отправлен.");

    const functionId = functionResponse.functionId;
    console.log("Найден functionId:", functionId);

    if (typeof functionId !== 'number' || isNaN(functionId)) {
        throw new Error("Полученный ID функции некорректен.");
    }

    console.log("Отправляем точки для functionId:", functionId);
    for (const point of points.value) {
        await api.createTabulatedPoint(functionId, point.x, point.y);
    }

    emit('function-created', {
      points: points.value,
      functionId: functionId,
      functionName: currentFunctionName
    });

    alert("Функция и точки успешно созданы!");
    points.value = [];
    pointCount.value = 0;
    functionName.value = "";
    functionExpression.value = "";
    typeFunction.value = "tabular";
    pointErrors.value = {};
    emit('close');
  } catch (e) {
    console.error("Create from arrays error:", e);
    showError(e.message || 'Ошибка при создании функции');
  }
};

// --- ФУНКЦИЯ СОЗДАНИЯ ФУНКЦИИ ИЗ MATH FUNCTION ---
const createFunctionFromMathFunction = async () => {
  const isPointCountValid = validatePointCountFromFunction();
  const isFunctionNameValid = validateFunctionNameFromFunction();
  const isIntervalValid = startXFromFunction.value < endXFromFunction.value;

  if (!isPointCountValid || !isFunctionNameValid) {
    showError('Пожалуйста, исправьте ошибки в форме перед отправкой.');
    return;
  }

  if (!isIntervalValid) {
    showError("Начало интервала должно быть меньше конца.");
    return;
  }

  if (!selectedFunctionName.value) {
    showError("Пожалуйста, выберите функцию из списка.");
    return;
  }

  const currentFunctionName = functionNameFromFunction.value;
  const actualType = props.isForOperation ? 'tabular' : typeFunctionFromFunction.value;
  const mathFunctionName = functionMap[selectedFunctionName.value];

  try {
    const functionData = {
      functionName: currentFunctionName,
      functionExpression: functionExpressionFromFunction.value,
      typeFunction: actualType,
    };

    const functionResponse = await api.createFunction(functionData);
    const functionId = functionResponse.functionId;

    if (typeof functionId !== 'number' || isNaN(functionId)) {
      throw new Error("Получен некорректный ID функции.");
    }

    const factoryType = localStorage.getItem('tabulatedFunctionFactory') || 'array';
    await api.calculateAndSaveTabulatedPoints(
      functionId,
      mathFunctionName,
      startXFromFunction.value,
      endXFromFunction.value,
      pointCountFromFunction.value,
      factoryType
    );

    emit('function-created', {
      points: [],
      functionId: functionId,
      functionName: currentFunctionName
    });

    alert("Функция и точки успешно созданы из MathFunction!");
    functionNameFromFunction.value = "";
    functionExpressionFromFunction.value = "";
    typeFunctionFromFunction.value = "tabular";
    selectedFunctionName.value = "";
    pointCountFromFunction.value = 0;
    startXFromFunction.value = 0;
    endXFromFunction.value = 1;
    emit('close');
  } catch (e) {
    console.error("Create from function error:", e);
    showError(e.message || 'Неизвестная ошибка при создании функции.');
  }
};

// --- JSON IMPORT / EXPORT ---
const loadFromJson = () => {
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
        if (!data.functionName) {
          throw new Error('Отсутствует название функции');
        }
        activeTab.value = 'fromArrays';
        functionName.value = data.functionName;
        functionExpression.value = data.functionExpression || '';
        typeFunction.value = data.typeFunction || 'tabular';
        if (Array.isArray(data.points) && data.points.length > 0) {
          points.value = data.points
            .map(p => ({ x: parseFloat(p.x), y: parseFloat(p.y) }))
            .filter(p => !isNaN(p.x) && !isNaN(p.y));
          pointCount.value = points.value.length;
        } else {
          points.value = [];
          pointCount.value = 0;
        }
        pointErrors.value = {};
        pointCountError.value = '';
        functionNameError.value = '';
        alert('Данные успешно загружены из JSON!');
      } catch (error) {
        console.error('Ошибка загрузки JSON:', error);
        showError('Ошибка при загрузке JSON: ' + (error.message || 'некорректный формат'));
      }
    };
    reader.readAsText(file);
  };
  input.click();
};

const saveAsJson = () => {
  let data;
  if (activeTab.value === 'fromArrays') {
    if (!functionName.value || points.value.length === 0) {
      showError('Недостаточно данных для экспорта');
      return;
    }
    data = {
      functionName: functionName.value,
      functionExpression: functionExpression.value,
      typeFunction: typeFunction.value,
      points: points.value.map(p => ({ x: p.x, y: p.y }))
    };
  } else {
    if (!functionNameFromFunction.value) {
      showError('Укажите название функции');
      return;
    }
    data = {
      functionName: functionNameFromFunction.value,
      functionExpression: functionExpressionFromFunction.value,
      typeFunction: typeFunctionFromFunction.value,
      mathFunctionName: selectedFunctionName.value,
      pointCount: pointCountFromFunction.value,
      startX: startXFromFunction.value,
      endX: endXFromFunction.value
    };
  }

  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${data.functionName || 'function'}.json`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
};
</script>

<style scoped>
.creator {
  position: relative;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  max-width: 800px;
  margin: 0 auto;
}

/* Стили для крестика */
.close-button {
  position: absolute;
  top: 10px;
  right: 10px;
  cursor: pointer;
  font-size: 24px;
  color: #666;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
  z-index: 10;
}

.close-button:hover {
  background-color: #f0f0f0;
  color: #d32f2f;
  transform: rotate(90deg);
}

h2 {
  color: #333;
  margin-bottom: 1.5rem;
  text-align: center;
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  justify-content: center;
}

.tabs button {
  padding: 8px 15px;
  background-color: #e9ecef;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.tabs button.active {
  background-color: #2196f3;
  color: white;
}

.tabs button:hover:not(.active) {
  background-color: #dee2e6;
}

input, select, button {
  padding: 8px;
  margin: 0.25rem 0;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 100%;
  box-sizing: border-box;
}

button {
  background-color: #42b983;
  color: white;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

button:hover {
  background-color: #359c6d;
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.generate-button {
  background-color: #2196f3;
}

.generate-button:hover {
  background-color: #1976d2;
}

.create-button {
  background-color: #4caf50;
  font-weight: bold;
  padding: 10px;
  margin-top: 1rem;
  font-size: 16px;
}

.create-button:hover {
  background-color: #43a047;
}

table {
  width: 100%;
  margin-top: 1rem;
  border-collapse: collapse;
}

table th, table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

table th {
  background-color: #f5f5f5;
}

/* Стили для валидации */
.error-input {
  border: 2px solid #d32f2f !important;
}

.error-message {
  color: #d32f2f;
  font-size: 0.85em;
  display: block;
  margin-top: 0.25rem;
  min-height: 1.2em;
}

/* Стили для JSON кнопок */
.json-controls {
  display: flex;
  gap: 10px;
  margin: 15px 0;
  justify-content: center;
  flex-wrap: wrap;
}

.json-button {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 15px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-weight: 500;
  background: #ecf0f1;
  color: #2c3e50;
  transition: all 0.2s;
}

.json-button:hover {
  background: #bdc3c7;
}

.json-button:disabled {
  background: #ddd;
  cursor: not-allowed;
}

@media (max-width: 600px) {
  .creator {
    padding: 15px;
    margin: 10px;
  }
  table {
    font-size: 0.9em;
  }
  table th, table td {
    padding: 6px;
  }
}
</style>