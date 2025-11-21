<template>
  <div class="creator">
    <h2>Создать функцию</h2>
    <div>
      <button @click="activeTab = 'fromArrays'">Из массивов X и Y</button>
      <button @click="activeTab = 'fromFunction'">Из функции MathFunction</button>
    </div>

    <div v-if="activeTab === 'fromArrays'">
      <h3>Создание из массивов</h3>
      <input v-model.number="pointCount" type="number" placeholder="Количество точек" min="1" />
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
            <td><input v-model.number="point.x" type="number" /></td>
            <td><input v-model.number="point.y" type="number" /></td>
          </tr>
        </tbody>
      </table>

      <!-- Поля для данных функции -->
      <input v-model="functionName" type="text" placeholder="Название функции" />
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
        <option v-for="name in sortedFunctionNames" :key="name" :value="name">{{ name }}</option>
      </select>
      <input v-model.number="pointCount" type="number" placeholder="Количество точек" min="1" />
      <input v-model.number="startX" type="number" placeholder="Начало интервала X" />
      <input v-model.number="endX" type="number" placeholder="Конец интервала X" />
      <input v-model="functionName" type="text" placeholder="Название функции" />
      <input v-model="functionExpression" type="text" placeholder="Выражение функции (опционально)" />
      <select v-model="typeFunction">
        <option value="tabular">Табулированная</option>
        <option value="analytic">Аналитическая</option>
      </select>
      <button @click="createFunctionFromMathFunction">Создать</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { api } from '../api.js';

const activeTab = ref('fromArrays');
const pointCount = ref(0);
const points = ref([]);
const functionName = ref('');
const functionExpression = ref('');
const typeFunction = ref('tabular');
const selectedFunctionName = ref('');
const startX = ref(0);
const endX = ref(1);

// Пример маппинга названий -> функций
const functionMap = {
  "Квадратичная функция": "SqrFunction",
  "Тождественная функция": "IdentityFunction",
  "Экспоненциальная функция": "ExpFunction",
};

const sortedFunctionNames = computed(() => Object.keys(functionMap).sort());

const generateTable = () => {
  if (pointCount.value <= 0) {
    alert("Количество точек должно быть положительным.");
    return;
  }
  if (pointCount.value > 100) {
    if (!confirm(`Вы ввели ${pointCount.value} точек. Это может быть неудобно. Продолжить?`)) {
      return;
    }
  }
  points.value = Array.from({ length: pointCount.value }, () => ({ x: 0, y: 0 }));
};

// --- ОБНОВЛЁННАЯ ФУНКЦИЯ createFunctionFromArrays ---
const createFunctionFromArrays = async () => {
  if (!functionName.value.trim()) {
    alert("Введите название функции.");
    return;
  }
  if (points.value.some(p => isNaN(p.x) || isNaN(p.y))) {
    alert("Все значения X и Y должны быть числами.");
    return;
  }

  const currentFunctionName = functionName.value; // Сохраним имя для поиска

  try {
    const functionData = {
      functionName: currentFunctionName,
      functionExpression: functionExpression.value,
      typeFunction: typeFunction.value,
      // userId не включаем (должен быть добавлен на сервере из аутентификации)
    };

    console.log("functionData перед отправкой:", functionData);
    console.log("JSON.stringify(functionData):", JSON.stringify(functionData));

    // 1. Создать функцию в таблице functions (ожидаем, что сервер НЕ возвращает id)
    await api.createFunction(functionData); // <-- Вызов без сохранения ответа
    console.log("Запрос на создание функции отправлен.");

    // 2. Получить список функций пользователя, чтобы найти только что созданную
    const userId = api.getStoredUserId(); // Получаем userId из api.js
    console.log("Получаем функции для userId:", userId);
    const allFunctions = await api.getFunctionsByUserId(userId);

    // 3. Найти функцию по имени (предполагаем, что имя уникально или это последняя созданная)
    //    Ищем последнюю функцию с совпадающим именем
    const createdFunction = allFunctions
        .filter(f => f.functionName === currentFunctionName) // Фильтруем по имени
        .sort((a, b) => b.functionId - a.functionId) // <-- ИСПРАВЛЕНО: сортируем по functionId
        [0]; // Берем первый элемент (с самым большим functionId)

    if (!createdFunction) {
        console.error("Не удалось найти только что созданную функцию в списке функций пользователя.");
        alert("Ошибка: не удалось получить ID созданной функции.");
        return;
    }

    const functionId = createdFunction.functionId; // <-- ИСПРАВЛЕНО: используем functionId
    console.log("Найден functionId:", functionId, "Тип:", typeof functionId);

    // Проверим, что это действительно число
    if (typeof functionId !== 'number' || isNaN(functionId)) {
        console.error("Поле 'functionId' найденной функции не является числом:", functionId);
        alert("Ошибка: полученный ID функции некорректен.");
        return;
    }

    // 4. Отправить точки в таблицу tabulated_functions
    console.log("Отправляем точки для functionId:", functionId);
    for (const point of points.value) {
        console.log("Отправляем точку:", { functionId, xval: point.x, yval: point.y });
        await api.createTabulatedPoints(functionId, point.x, point.y);
    }

    alert("Функция и точки успешно созданы!");
    points.value = [];
    pointCount.value = 0;
    functionName.value = "";
    functionExpression.value = "";
    typeFunction.value = "tabular";

  } catch (e) {
    console.error("Create from arrays error:", e);
    alert("Ошибка создания функции: " + e.message);
  }
};
// --- КОНЕЦ ОБНОВЛЁННОЙ ФУНКЦИИ ---

// --- ОБНОВЛЁННАЯ ФУНКЦИЯ createFunctionFromMathFunction ---
const createFunctionFromMathFunction = async () => {
  if (!functionName.value.trim()) {
    alert("Введите название функции.");
    return;
  }
  if (!selectedFunctionName.value) {
    alert("Выберите функцию.");
    return;
  }
  if (pointCount.value <= 0 || startX.value >= endX.value) {
    alert("Некорректные параметры интервала или количества точек.");
    return;
  }

  const currentFunctionName = functionName.value; // Сохраним имя для поиска

  try {
    const functionData = {
      functionName: currentFunctionName,
      functionExpression: functionExpression.value,
      typeFunction: typeFunction.value,
      // userId не включаем (должен быть добавлен на сервере из аутентификации)
    };

    console.log("functionData перед отправкой (from function):", functionData);
    console.log("JSON.stringify(functionData) (from function):", JSON.stringify(functionData));

    // 1. Создать функцию в таблице functions (ожидаем, что сервер НЕ возвращает id)
    await api.createFunction(functionData); // <-- Вызов без сохранения ответа
    console.log("Запрос на создание функции (from function) отправлен.");

    // 2. Получить список функций пользователя, чтобы найти только что созданную
    const userId = api.getStoredUserId(); // Получаем userId из api.js
    console.log("Получаем функции для userId (from function):", userId);
    const allFunctions = await api.getFunctionsByUserId(userId);

    // 3. Найти функцию по имени
    const createdFunction = allFunctions
        .filter(f => f.functionName === currentFunctionName) // Фильтруем по имени
        .sort((a, b) => b.functionId - a.functionId) // <-- ИСПРАВЛЕНО: сортируем по functionId
        [0]; // Берем первый элемент (с самым большим functionId)

    if (!createdFunction) {
        console.error("Не удалось найти только что созданную функцию (from function) в списке функций пользователя.");
        alert("Ошибка: не удалось получить ID созданной функции.");
        return;
    }

    const functionId = createdFunction.functionId; // <-- ИСПРАВЛЕНО: используем functionId
    console.log("Найден functionId (from function):", functionId, "Тип:", typeof functionId);

    if (typeof functionId !== 'number' || isNaN(functionId)) {
        console.error("Поле 'functionId' найденной функции (from function) не является числом:", functionId);
        alert("Ошибка: полученный ID функции некорректен.");
        return;
    }

    // 4. На бэкенде вычислить точки и сохранить в tabulated_functions (пока заглушка)
    // const pointsData = {
    //   function_id: functionId,
    //   math_function_name: functionMap[selectedFunctionName.value],
    //   start: startX.value,
    //   end: endX.value,
    //   count: pointCount.value
    // };
    // await api.createTabulatedPoints(pointsData); // Требует обновления на бэкенде

    alert("Функция успешно создана из другой функции!");
    functionName.value = "";
    functionExpression.value = "";
    typeFunction.value = "tabular";
    selectedFunctionName.value = "";
    pointCount.value = 0;
    startX.value = 0;
    endX.value = 1;

  } catch (e) {
    console.error("Create from function error:", e);
    alert("Ошибка создания функции: " + e.message);
  }
};
// --- КОНЕЦ ОБНОВЛЁННОЙ ФУНКЦИИ ---
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
</style>