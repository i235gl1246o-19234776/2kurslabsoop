<template>
  <div class="creator">
    <h2>Создать функцию</h2>
    <div>
      <button @click="activeTab = 'fromArrays'">Из массивов X и Y</button>
      <button @click="activeTab = 'fromFunction'">Из функции MathFunction</button>
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
      <button @click="generateTable">Сгенерировать таблицу</button>

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
      <select v-model="typeFunction">
        <option value="tabular">Табулированная</option>
        <option value="analytic">Аналитическая</option>
      </select>

      <button @click="createFunctionFromArrays">Создать</button>
    </div>

    <div v-if="activeTab === 'fromFunction'">
      <h3>Создание из функции</h3>
      <select v-model="selectedFunctionName">
        <option v-for="name in sortedFunctionNames" :key="name" :value="functionMap[name]">{{ name }}</option> <!-- Изменили :value на functionMap[name] -->
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
      <select v-model="typeFunctionFromFunction">
        <option value="tabular">Табулированная</option>
        <option value="analytic">Аналитическая</option>
      </select>
      <button @click="createFunctionFromMathFunction">Создать</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, inject, toRaw } from 'vue';
import { api } from '../api.js';

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
const selectedFunctionName = ref(''); // Должно содержать значение из functionMap (например, "SqrFunction")
const startX = ref(0);
const endX = ref(1);

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

// Пример маппинга названий -> функций
const functionMap = {
  "Квадратичная функция": "SqrFunction",
  "Тождественная функция": "IdentityFunction",
  "Экспоненциальная функция": "ExpFunction",
};

const sortedFunctionNames = computed(() => Object.keys(functionMap).sort());

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
  if (isNaN(point[coord])) {
    pointErrors.value[index][coord] = `Значение ${coord.toUpperCase()} должно быть числом.`;
  } else {
    pointErrors.value[index][coord] = '';
  }
};

const validateFunctionName = () => {
  if (!functionName.value.trim()) {
    functionNameError.value = 'Название функции обязательно.';
    return false;
  }
  if (functionName.value.trim().length > 255) { // Пример ограничения
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
  if (!validatePointCount()) return; // Проверяем перед генерацией

  if (pointCount.value > 100) {
    if (!confirm(`Вы ввели ${pointCount.value} точек. Это может быть неудобно. Продолжить?`)) {
      return;
    }
  }
  points.value = Array.from({ length: pointCount.value }, () => ({ x: 0, y: 0 }));
  pointErrors.value = {}; // Сбрасываем ошибки точек
};

// --- ОБНОВЛЁННАЯ ФУНКЦИЯ createFunctionFromArrays ---
const createFunctionFromArrays = async () => {
  // Проверяем валидацию перед отправкой
  const isPointCountValid = validatePointCount();
  const isFunctionNameValid = validateFunctionName();
  const arePointsValid = points.value.every(p => !isNaN(p.x) && !isNaN(p.y));

  if (!isPointCountValid || !isFunctionNameValid || !arePointsValid) {
    showError('Пожалуйста, исправьте ошибки в форме перед отправкой.');
    return;
  }

  const currentFunctionName = functionName.value;

  try {
    const functionData = {
      functionName: currentFunctionName,
      functionExpression: functionExpression.value,
      typeFunction: typeFunction.value,
    };

    // Очищаем объект перед логированием и отправкой
    const cleanFunctionData = toRaw(functionData);
    console.log("functionData перед отправкой:", cleanFunctionData);
    console.log("JSON.stringify(functionData):", JSON.stringify(cleanFunctionData));

    await api.createFunction(cleanFunctionData);
    console.log("Запрос на создание функции отправлен.");

    const userId = api.getStoredUserId();
    console.log("Получаем функции для userId:", userId);
    const allFunctions = await api.getFunctionsByUserId(userId);

    const createdFunction = allFunctions
        .filter(f => f.functionName === currentFunctionName)
        .sort((a, b) => b.functionId - a.functionId)
        [0];

    if (!createdFunction) {
        throw new Error("Не удалось получить ID созданной функции.");
    }

    const functionId = createdFunction.functionId;
    console.log("Найден functionId:", functionId);

    if (typeof functionId !== 'number' || isNaN(functionId)) {
        throw new Error("Полученный ID функции некорректен.");
    }

    console.log("Отправляем точки для functionId:", functionId);
    for (const point of points.value) {
        console.log("Отправляем точку:", { functionId, xval: point.x, yval: point.y });
        // --- ЛОГИРОВАНИЕ ДЛЯ createTabulatedPoints ---
        const pointDataForLog = {
            functionId: functionId, // <- число
            xval: point.x,         // <- число
            yval: point.y          // <- число
        };
        console.log("  Подготовленные данные для точки (для JSON.stringify):", pointDataForLog);
        console.log("  JSON.stringify(pointData):", JSON.stringify(pointDataForLog));
        // --- КОНЕЦ ЛОГИРОВАНИЯ ---
        await api.createTabulatedPoints(functionId, point.x, point.y);
    }

    alert("Функция и точки успешно созданы!");
    points.value = [];
    pointCount.value = 0;
    functionName.value = "";
    functionExpression.value = "";
    typeFunction.value = "tabular";
    pointErrors.value = {}; // Сбрасываем ошибки точек

  } catch (e) {
    console.error("Create from arrays error:", e);
    // --- ВЫЗОВ ЦЕНТРАЛИЗОВАННОГО ОБРАБОТЧИКА ОШИБОК ---
    showError(e.message);
    // --- КОНЕЦ ВЫЗОВА ---
  }
};
// --- КОНЕЦ ОБНОВЛЁННОЙ ФУНКЦИИ ---

const createFunctionFromMathFunction = async () => {
  // Проверяем валидацию перед отправкой
  const isPointCountValid = validatePointCountFromFunction();
  const isFunctionNameValid = validateFunctionNameFromFunction();
  const isIntervalValid = startXFromFunction.value < endXFromFunction.value;

  if (!isPointCountValid || !isFunctionNameValid || !isIntervalValid) {
    let errorMsg = "Пожалуйста, исправьте ошибки в форме перед отправкой.";
    if (!isIntervalValid) {
      errorMsg += " Начало интервала должно быть меньше конца.";
    }
    showError(errorMsg);
    return;
  }

  // Проверяем, выбрана ли функция
  if (!selectedFunctionName.value) {
    showError("Пожалуйста, выберите функцию из списка.");
    return;
  }

  const currentFunctionName = functionNameFromFunction.value;

  try {
    const functionData = {
      functionName: currentFunctionName,
      functionExpression: functionExpressionFromFunction.value,
      typeFunction: typeFunctionFromFunction.value,
    };

    // Очищаем объект перед логированием и отправкой
    const cleanFunctionData = toRaw(functionData);
    console.log("functionData перед отправкой (from function):", cleanFunctionData);
    console.log("JSON.stringify(functionData) (from function):", JSON.stringify(cleanFunctionData));

    await api.createFunction(cleanFunctionData);
    console.log("Запрос на создание функции (from function) отправлен.");

    const userId = api.getStoredUserId();
    console.log("Получаем функции для userId (from function):", userId);
    const allFunctions = await api.getFunctionsByUserId(userId);

    const createdFunction = allFunctions
        .filter(f => f.functionName === currentFunctionName)
        .sort((a, b) => b.functionId - a.functionId)
        [0];

    if (!createdFunction) {
        throw new Error("Не удалось получить ID созданной функции.");
    }

    const functionId = createdFunction.functionId;
    console.log("Найден functionId (from function):", functionId);

    if (typeof functionId !== 'number' || isNaN(functionId)) {
        throw new Error("Полученный ID функции некорректен.");
    }

    // --- ВЫЗОВ НОВОГО МЕТОДА API ДЛЯ ВЫЧИСЛЕНИЯ И СОХРАНЕНИЯ ТОЧЕК ---
    console.log("Вычисляем и сохраняем точки для functionId:", functionId, "MathFunction:", selectedFunctionName.value, "start:", startXFromFunction.value, "end:", endXFromFunction.value, "count:", pointCountFromFunction.value);

    // --- ЛОГИРОВАНИЕ ДЛЯ calculateAndSaveTabulatedPoints ---
    const calculateDataForLog = {
        functionId: functionId, // <- число
        mathFunctionName: selectedFunctionName.value, // <- строка
        start: startXFromFunction.value, // <- число
        end: endXFromFunction.value,   // <- число
        count: pointCountFromFunction.value // <- число
    };
    console.log("  Подготовленные данные для calculate (для JSON.stringify):", calculateDataForLog);
    console.log("  JSON.stringify(calculateData):", JSON.stringify(calculateDataForLog));
    // --- КОНЕЦ ЛОГИРОВАНИЯ ---

    await api.calculateAndSaveTabulatedPoints(
        functionId,
        selectedFunctionName.value, // <-- Имя функции из functionMap
        startXFromFunction.value,
        endXFromFunction.value,
        pointCountFromFunction.value
    );
    // --- КОНЕЦ ВЫЗОВА ---

    alert("Функция и точки успешно созданы из MathFunction!");
    functionNameFromFunction.value = "";
    functionExpressionFromFunction.value = "";
    typeFunctionFromFunction.value = "tabular";
    selectedFunctionName.value = ""; // Сбросим выбор функции
    pointCountFromFunction.value = 0;
    startXFromFunction.value = 0;
    endXFromFunction.value = 1;

  } catch (e) {
    console.error("Create from function error:", e);
    // --- ВЫЗОВ ЦЕНТРАЛИЗОВАННОГО ОБРАБОТЧИКА ОШИБОК ---
    showError(e.message);
    // --- КОНЕЦ ВЫЗОВА ---
  }
};

</script>

<style scoped>
.creator {
  margin: 1rem;
}
input, select, button {
  margin: 0.25rem;
}
table {
  width: 100%;
  margin-top: 1rem;
}

/* --- СТИЛИ ДЛЯ ВАЛИДАЦИИ --- */
.error-input {
  border: 2px solid #d32f2f; /* Красная рамка */
}
.error-message {
  color: #d32f2f; /* Красный цвет текста */
  font-size: 0.85em;
  display: block;
  margin-top: 0.25rem;
}
/* --- КОНЕЦ СТИЛЕЙ --- */
</style>