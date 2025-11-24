<template>
  <div class="function-creator">
    <h2>Создание табулированной функции</h2>
    <div class="creation-type">
      <button
        :class="{ active: creationType === 'xy' }"
        @click="creationType = 'xy'"
      >
        Из массивов X/Y
      </button>
      <button
        :class="{ active: creationType === 'math' }"
        @click="creationType = 'math'"
      >
        Из математической функции
      </button>
    </div>

    <div v-if="creationType === 'xy'" class="xy-creator">
      <div class="point-count">
        <label for="pointCount">Количество точек:</label>
        <input
          type="number"
          id="pointCount"
          v-model.number="pointCount"
          min="2"
          max="100"
        />
      </div>

      <div v-if="points.length > 0" class="points-table">
        <h3>Точки функции</h3>
        <table>
          <thead>
            <tr>
              <th>№</th>
              <th>X</th>
              <th>Y</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(point, index) in points" :key="index">
              <td>{{ index + 1 }}</td>
              <td>
                <input
                  type="number"
                  v-model.number="point.x"
                  @input="validatePoints"
                  class="point-input"
                />
              </td>
              <td>
                <input
                  type="number"
                  v-model.number="point.y"
                  @input="validatePoints"
                  class="point-input"
                />
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="pointError" class="error-message">
        {{ pointError }}
      </div>
    </div>

    <div v-if="creationType === 'math'" class="math-creator">
      <div class="math-function">
        <label for="mathFunction">Выберите функцию:</label>
        <select id="mathFunction" v-model="selectedMathFunction">
          <option value="">-- Выберите функцию --</option>
          <option
            v-for="func in mathFunctions"
            :key="func.name"
            :value="func.name"
          >
            {{ func.displayName }}
          </option>
        </select>
      </div>

      <div class="interval">
        <div class="interval-input">
          <label for="fromX">От X:</label>
          <input
            type="number"
            id="fromX"
            v-model.number="fromX"
          />
        </div>
        <div class="interval-input">
          <label for="toX">До X:</label>
          <input
            type="number"
            id="toX"
            v-model.number="toX"
          />
        </div>
      </div>

      <div class="point-count">
        <label for="mathPointCount">Количество точек:</label>
        <input
          type="number"
          id="mathPointCount"
          v-model.number="mathPointCount"
          min="2"
          max="100"
        />
      </div>
    </div>

    <div class="function-name">
      <label for="functionName">Название функции:</label>
      <input
        type="text"
        id="functionName"
        v-model="functionName"
        placeholder="Введите название функции"
      />
    </div>

    <div v-if="creationError" class="error-message">
      {{ creationError }}
    </div>

    <div class="actions">
      <button @click="createFunction" :disabled="isCreating" class="create-btn">
        <span v-if="isCreating">
          <i class="fas fa-spinner fa-spin"></i> Создание...
        </span>
        <span v-else>
          <i class="fas fa-check"></i> Создать функцию
        </span>
      </button>
      <button @click="$emit('close')" class="cancel-btn">
        <i class="fas fa-times"></i> Отмена
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import * as api from '@/api.js';

const emit = defineEmits(['close', 'function-created', 'error']);

// Тип создания функции
const creationType = ref('xy');

// Для создания из X/Y
const pointCount = ref(5);
const points = ref([]);
const pointError = ref('');

// Для создания из математической функции
const mathFunctions = ref([]);
const selectedMathFunction = ref('');
const fromX = ref(0);
const toX = ref(10);
const mathPointCount = ref(10);

// Общие параметры
const functionName = ref('');
const creationError = ref('');
const isCreating = ref(false);

// Загрузка доступных математических функций
const loadMathFunctions = async () => {
  try {
    const functions = await api.getAvailableMathFunctions();
    mathFunctions.value = functions.map(func => ({
      name: func.name,
      displayName: func.displayName || func.name
    }));
  } catch (error) {
    console.error('Ошибка при загрузке математических функций:', error);
    emit('error', `Ошибка при загрузке математических функций: ${error.message}`);
  }
};

// Генерация точек при изменении количества
const generatePoints = () => {
  if (pointCount.value < 2) {
    pointCount.value = 2;
  }
  if (pointCount.value > 100) {
    pointCount.value = 100;
  }
  const newPoints = [];
  for (let i = 0; i < pointCount.value; i++) {
    // Если есть существующие точки, используем их значения
    if (i < points.value.length) {
      newPoints.push({ ...points.value[i] });
    } else {
      // Иначе создаем новые точки с начальными значениями
      const x = i * 1.0;
      newPoints.push({ x, y: Math.sin(x) });
    }
  }
  points.value = newPoints;
  validatePoints();
};

// Валидация точек
const validatePoints = () => {
  pointError.value = '';
  // Проверка на дубликаты X
  const xValues = points.value.map(p => p.x);
  const uniqueXValues = new Set(xValues);
  if (uniqueXValues.size !== xValues.length) {
    pointError.value = 'Обнаружены дубликаты значений X. Все значения X должны быть уникальными.';
    return;
  }
  // Проверка на сортировку по X
  for (let i = 1; i < points.value.length; i++) {
    if (points.value[i].x < points.value[i-1].x) {
      pointError.value = 'Значения X должны быть отсортированы по возрастанию.';
      return;
    }
  }
};

// Создание функции
const createFunction = async () => {
  if (pointError.value) {
    creationError.value = 'Пожалуйста, исправьте ошибки в точках перед созданием функции.';
    return;
  }
  if (!functionName.value.trim()) {
    creationError.value = 'Пожалуйста, введите название функции.';
    return;
  }

  isCreating.value = true;
  creationError.value = '';

  try {
    let result;
    if (creationType.value === 'xy') {
      // Создание из массивов X/Y
      const functionData = {
        functionName: functionName.value.trim(),
        typeFunction: 'tabular',
      };

      // Создание функции
      result = await api.createFunction(functionData);

      // Добавление точек
      await Promise.all(points.value.map(point =>
        api.addTabulatedPoint(result.id, point)
      ));
    } else if (creationType.value === 'math') {
      // Создание из математической функции
      if (!selectedMathFunction.value) {
        throw new Error('Пожалуйста, выберите математическую функцию.');
      }
      if (fromX.value >= toX.value) {
        throw new Error('Начальное значение X должно быть меньше конечного.');
      }
      if (mathPointCount.value < 2) {
        throw new Error('Количество точек должно быть не менее 2.');
      }

      const functionData = {
        functionName: functionName.value.trim(),
        typeFunction: 'tabular',
        mathFunctionName: selectedMathFunction.value,
        fromX: parseFloat(fromX.value),
        toX: parseFloat(toX.value),
        pointCount: parseInt(mathPointCount.value)
      };

      // Создание функции с точками
      result = await api.createFunctionFromMath(functionData);
    }

    // Эмит результата создания
    emit('function-created', {
      functionId: result.id,
      functionName: result.functionName,
      points: creationType.value === 'xy' ? points.value : null
    });

    // Закрытие окна
    emit('close');
  } catch (error) {
    console.error('Ошибка при создании функции:', error);
    creationError.value = `Ошибка при создании функции: ${error.message}`;
    emit('error', creationError.value);
  } finally {
    isCreating.value = false;
  }
};

// Наблюдение за количеством точек
watch(pointCount, generatePoints);

// Инициализация
onMounted(() => {
  generatePoints();
  loadMathFunctions();
});
</script>

<style scoped>
.function-creator {
  padding: 20px;
}
.creation-type {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  border-bottom: 1px solid #ddd;
  padding-bottom: 15px;
}
.creation-type button {
  padding: 8px 16px;
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.creation-type button.active {
  background-color: #2196f3;
  color: white;
  border-color: #2196f3;
}
.point-count, .math-function, .interval, .function-name {
  margin-bottom: 15px;
}
.point-count label, .math-function label, .interval label, .function-name label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}
input[type="number"], input[type="text"], select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}
.points-table {
  margin: 20px 0;
}
table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}
table th, table td {
  padding: 8px;
  text-align: left;
  border: 1px solid #ddd;
}
table th {
  background-color: #f8f9fa;
  font-weight: bold;
}
.point-input {
  width: 100%;
  padding: 4px;
  border: 1px solid #ccc;
  border-radius: 3px;
}
.interval {
  display: flex;
  gap: 15px;
}
.interval-input {
  flex: 1;
}
.error-message {
  color: #d32f2f;
  background-color: #ffebee;
  padding: 10px;
  border-radius: 4px;
  margin: 10px 0;
  border-left: 3px solid #d32f2f;
}
.actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  justify-content: flex-end;
}
.create-btn, .cancel-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}
.create-btn {
  background-color: #4CAF50;
  color: white;
}
.create-btn:hover {
  background-color: #45a049;
}
.create-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}
.cancel-btn {
  background-color: #f44336;
  color: white;
}
.cancel-btn:hover {
  background-color: #da190b;
}
</style>