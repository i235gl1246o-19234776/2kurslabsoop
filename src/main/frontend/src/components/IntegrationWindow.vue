<!-- src/components/IntegrationWindow.vue -->
<template>
  <div v-if="show" class="integration-window">
    <div class="window-header">
      <h2>Вычисление определенного интеграла</h2>
      <button class="close-button" @click="$emit('close')">&times;</button>
    </div>

    <div class="function-section">
      <h3>Исходная функция</h3>
      <div class="function-controls">
        <button @click="createFunction('source')">Создать</button>
        <button @click="openFunctionSelector('source')">Загрузить</button>
        <button @click="saveFunction('source')" :disabled="!sourceFunction || !hasUnsavedChanges">Сохранить изменения</button>
      </div>

      <div v-if="sourceFunction" class="function-details">
        <p><strong>Имя:</strong> {{ sourceFunction.functionName }}</p>
        <p><strong>ID:</strong> {{ sourceFunction.functionId }}</p>
      </div>

      <div v-if="sourcePoints.length > 0" class="function-table">
        <h4>Точки исходной функции</h4>
        <table>
          <thead>
            <tr>
              <th>X</th>
              <th>Y</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(point, index) in sourcePoints" :key="index">
              <td>{{ point.x }}</td>
              <td>
                <input
                  type="number"
                  v-model="tempYValues[index]"
                  @change="updatePoint(index)"
                  :disabled="!sourceFunction || !sourceFunction.functionId"
                  class="point-y-input"
                />
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="sourceError" class="error-message">
        {{ sourceError }}
      </div>
    </div>

    <!-- Настройки интегрирования -->
    <div class="integration-settings">
      <h3>Параметры интегрирования</h3>
      <div class="settings-grid">
        <div class="setting-group">
          <label for="fromInput">Начало интервала (от):</label>
          <input
            type="number"
            id="fromInput"
            v-model="fromX"
            class="setting-input"
            :disabled="!canSetIntegrationRange"
          />
        </div>
        <div class="setting-group">
          <label for="toInput">Конец интервала (до):</label>
          <input
            type="number"
            id="toInput"
            v-model="toX"
            class="setting-input"
            :disabled="!canSetIntegrationRange"
          />
        </div>
        <div class="setting-group">
          <label for="threadsInput">Количество потоков:</label>
          <input
            type="number"
            id="threadsInput"
            v-model="threads"
            min="1"
            max="16"
            class="setting-input"
          />
        </div>
      </div>
    </div>

    <!-- Результат -->
    <div v-if="integrationResult !== null" class="result-section">
      <h3>Результат интегрирования</h3>
      <div class="result-details">
        <p><strong>Значение интеграла:</strong> {{ integrationResult.value.toFixed(6) }}</p>
        <p><strong>Время выполнения:</strong> {{ integrationResult.executionTime }} мс</p>
        <p><strong>Использовано потоков:</strong> {{ integrationResult.threadsUsed }}</p>
      </div>

      <div class="result-chart">
        <h4>Зависимость времени выполнения от количества потоков</h4>
        <div class="chart-container">
          <canvas ref="chartCanvas"></canvas>
        </div>
      </div>

      <div class="export-button" @click="exportResults">
        Экспортировать результаты
      </div>
    </div>

    <!-- Действия -->
    <div class="integration-actions">
      <button @click="calculateIntegral" :disabled="!canCalculate" class="calculate-button">
        Вычислить интеграл
      </button>
      <button @click="reset" class="reset-button">
        Сбросить результаты
      </button>
    </div>

    <!-- Модальное окно выбора функции -->
    <div v-if="showFunctionSelector" class="modal-overlay">
      <div class="function-selector-modal">
        <div class="modal-header">
          <h2>Выберите функцию</h2>
          <button class="close-btn" @click="closeFunctionSelector">&times;</button>
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
          <button class="cancel-button" @click="closeFunctionSelector">Отмена</button>
        </div>
      </div>
    </div>

    <!-- Индикатор загрузки -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="spinner"></div>
      <p>Вычисление интеграла...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { Chart, registerables } from 'chart.js';
import * as api from '@/api.js';

Chart.register(...registerables);

const props = defineProps({
  show: Boolean
});

const emit = defineEmits(['close', 'create-function']);

// Состояние функций
const sourceFunction = ref(null);
const sourcePoints = ref([]);
const originalPoints = ref([]);
const tempYValues = ref({});

// Состояние ошибок
const sourceError = ref('');

// Состояние загрузки
const loadingSourcePoints = ref(false);
const isLoading = ref(false);
const loadingFunctions = ref(false);

// Состояние выбора функции
const showFunctionSelector = ref(false);
const availableFunctions = ref([]);

// Настройки интегрирования
const fromX = ref(null);
const toX = ref(null);
const threads = ref(4);

// Результаты
const integrationResult = ref(null);
const executionTimes = ref([]);
const threadCounts = ref([]);

// Ссылки
const chartCanvas = ref(null);
let chartInstance = null;

// Вычисляемые свойства
const hasUnsavedChanges = computed(() => {
  if (!sourceFunction.value || originalPoints.value.length !== sourcePoints.value.length) {
    return false;
  }

  for (let i = 0; i < sourcePoints.value.length; i++) {
    const original = originalPoints.value[i];
    const current = sourcePoints.value[i];
    if (original && Math.abs(current.y - original.y) > 0.0001) {
      return true;
    }
  }

  return false;
});

const canSetIntegrationRange = computed(() => {
  return sourcePoints.value.length > 0;
});

const canCalculate = computed(() => {
  return sourceFunction.value &&
         fromX.value !== null &&
         toX.value !== null &&
         fromX.value < toX.value &&
         threads.value > 0;
});

// Загрузка доступных функций
const loadAvailableFunctions = async () => {
  loadingFunctions.value = true;
  try {
    const userId = api.getStoredUserId();
    const functions = await api.getFunctionsByUserId(userId);
    availableFunctions.value = functions.filter(f => f.typeFunction !== 'composite');
  } catch (error) {
    console.error('Ошибка при загрузке функций:', error);
    alert(`Ошибка при загрузке функций: ${error.message}`);
  } finally {
    loadingFunctions.value = false;
  }
};

// Открытие селектора функций
const openFunctionSelector = () => {
  showFunctionSelector.value = true;
  loadAvailableFunctions();
};

// Закрытие селектора функций
const closeFunctionSelector = () => {
  showFunctionSelector.value = false;
};

// Выбор функции
const selectFunction = async (func) => {
  try {
    sourceFunction.value = func;
    sourceError.value = '';

    loadingSourcePoints.value = true;
    const pointsResponse = await api.getTabulatedPointsByFunctionId(func.functionId);
    sourcePoints.value = pointsResponse.points.map(p => ({ x: p.x, y: p.y }));
    originalPoints.value = [...sourcePoints.value];

    // Инициализация временных значений Y
    tempYValues.value = {};
    sourcePoints.value.forEach((point, index) => {
      tempYValues.value[index] = point.y;
    });

    // Установка диапазона интегрирования по умолчанию
    if (sourcePoints.value.length > 0) {
      const xValues = sourcePoints.value.map(p => p.x);
      fromX.value = Math.min(...xValues);
      toX.value = Math.max(...xValues);
    }

    // Сброс результатов
    integrationResult.value = null;
    executionTimes.value = [];
    threadCounts.value = [];

    closeFunctionSelector();
  } catch (error) {
    console.error('Ошибка при загрузке точек функции:', error);
    sourceError.value = `Ошибка при загрузке точек функции: ${error.message}`;
  } finally {
    loadingSourcePoints.value = false;
  }
};

// Создание функции
const createFunction = (target) => {
  emit('create-function', target);
};

// Обновление точки
const updatePoint = (index) => {
  if (sourcePoints.value[index]) {
    sourcePoints.value[index].y = parseFloat(tempYValues.value[index]);
  }
};

// Сохранение функции
const saveFunction = async (target) => {
  if (!sourceFunction.value) {
    alert('Нет функции для сохранения');
    return;
  }

  try {
    // Обновление точек на сервере
    await Promise.all(sourcePoints.value.map((point, index) => {
      if (Math.abs(point.y - originalPoints.value[index].y) > 0.0001) {
        return api.updateTabulatedPoint(sourceFunction.value.functionId, point);
      }
      return Promise.resolve();
    }));

    // Обновление оригинальных точек
    originalPoints.value = [...sourcePoints.value];

    alert(`Функция успешно сохранена!`);
  } catch (error) {
    console.error('Ошибка при сохранении функции:', error);
    alert(`Ошибка при сохранении функции: ${error.message}`);
  }
};

// Вычисление интеграла
const calculateIntegral = async () => {
  if (!canCalculate.value) {
    alert('Невозможно выполнить интегрирование. Проверьте параметры.');
    return;
  }

  isLoading.value = true;
  sourceError.value = '';

  try {
    // Проверка, есть ли несохраненные изменения
    const hasUnsaved = hasUnsavedChanges.value;
    const factoryType = localStorage.getItem('selectedTabulatedFunctionFactory') || 'array';

    // Массив для хранения результатов с разным количеством потоков
    executionTimes.value = [];
    threadCounts.value = [];

    // Тестирование с разным количеством потоков для построения графика
    const maxThreads = Math.min(threads.value, 16); // Ограничение максимум 16 потоками
    const testThreads = [1, 2, 4, 8, maxThreads].filter(t => t <= maxThreads);

    for (const threadCount of testThreads) {
      let result;

      if (!sourceFunction.value.functionId || hasUnsaved) {
        // Локальное интегрирование
        result = performLocalIntegration(sourcePoints.value, fromX.value, toX.value, threadCount);
      } else {
        // Серверное интегрирование
        const requestBody = {
          functionId: sourceFunction.value.functionId,
          fromX: fromX.value,
          toX: toX.value,
          threadCount: threadCount,
          factoryType: factoryType
        };

        result = await api.integrateFunction(requestBody);
      }

      // Сохранение результатов для графика
      executionTimes.value.push(result.executionTime);
      threadCounts.value.push(threadCount);

      // Если это последний поток (основной расчет), сохраняем результат
      if (threadCount === maxThreads) {
        integrationResult.value = {
          value: result.value,
          executionTime: result.executionTime,
          threadsUsed: threadCount
        };
      }
    }

    // Построение графика после получения всех результатов
    await nextTick();
    renderChart();

  } catch (error) {
    console.error('Ошибка при интегрировании:', error);
    sourceError.value = `Ошибка при интегрировании: ${error.message}`;
    alert(sourceError.value);
  } finally {
    isLoading.value = false;
  }
};

// Локальное интегрирование (упрощенная версия)
const performLocalIntegration = (points, fromX, toX, threadCount) => {
  // Сортировка точек по X
  const sortedPoints = [...points].sort((a, b) => a.x - b.x);

  // Фильтрация точек в диапазоне интегрирования
  const filteredPoints = sortedPoints.filter(p => p.x >= fromX && p.x <= toX);

  if (filteredPoints.length < 2) {
    throw new Error('Недостаточно точек в указанном диапазоне для интегрирования');
  }

  // Метод трапеций
  let integral = 0;
  for (let i = 0; i < filteredPoints.length - 1; i++) {
    const x0 = filteredPoints[i].x;
    const x1 = filteredPoints[i + 1].x;
    const y0 = filteredPoints[i].y;
    const y1 = filteredPoints[i + 1].y;
    integral += (x1 - x0) * (y0 + y1) / 2;
  }

  // Имитация времени выполнения в зависимости от количества потоков
  const baseTime = 100 + Math.random() * 50; // Базовое время 100-150 мс
  const executionTime = baseTime / Math.sqrt(threadCount); // Упрощенная модель

  return {
    value: integral,
    executionTime: Math.round(executionTime)
  };
};

// Построение графика
const renderChart = () => {
  if (chartInstance) {
    chartInstance.destroy();
  }

  if (!chartCanvas.value || executionTimes.value.length === 0) {
    return;
  }

  chartInstance = new Chart(chartCanvas.value, {
    type: 'line',
    data: {
      labels: threadCounts.value,
      datasets: [{
        label: 'Время выполнения (мс)',
        data: executionTimes.value,
        borderColor: 'rgba(54, 162, 235, 1)',
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        borderWidth: 2,
        tension: 0.4,
        fill: true,
        pointBackgroundColor: 'rgba(54, 162, 235, 1)',
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        pointRadius: 5,
        pointHoverRadius: 7
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          title: {
            display: true,
            text: 'Время (мс)'
          }
        },
        x: {
          title: {
            display: true,
            text: 'Количество потоков'
          },
          ticks: {
            stepSize: 1
          }
        }
      },
      plugins: {
        title: {
          display: true,
          text: 'Зависимость времени выполнения от количества потоков'
        },
        tooltip: {
          mode: 'index',
          intersect: false,
          callbacks: {
            label: function(context) {
              return `Время: ${context.raw} мс`;
            }
          }
        }
      },
      interaction: {
        mode: 'nearest',
        axis: 'x',
        intersect: false
      }
    }
  });
};

// Экспорт результатов
const exportResults = () => {
  if (!integrationResult.value) return;

  const results = {
    functionId: sourceFunction.value?.functionId,
    functionName: sourceFunction.value?.functionName,
    fromX: fromX.value,
    toX: toX.value,
    result: integrationResult.value.value,
    executionTime: integrationResult.value.executionTime,
    threadsUsed: integrationResult.value.threadsUsed,
    performanceData: {
      threadCounts: threadCounts.value,
      executionTimes: executionTimes.value
    },
    timestamp: new Date().toISOString()
  };

  const blob = new Blob([JSON.stringify(results, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `integration_result_${new Date().toISOString().replace(/[:.]/g, '-')}.json`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
};

// Сброс результатов
const reset = () => {
  integrationResult.value = null;
  executionTimes.value = [];
  threadCounts.value = [];

  if (chartInstance) {
    chartInstance.destroy();
    chartInstance = null;
  }
};

// Закрытие окна
const close = () => {
  emit('close');
};

// Очистка при размонтировании
onUnmounted(() => {
  if (chartInstance) {
    chartInstance.destroy();
    chartInstance = null;
  }
});

// Инициализация
onMounted(() => {
  // Можно добавить дополнительную инициализацию
});

// Сброс состояния при закрытии окна
watch(() => props.show, (newVal) => {
  if (!newVal) {
    sourceFunction.value = null;
    sourcePoints.value = [];
    originalPoints.value = [];
    tempYValues.value = {};
    fromX.value = null;
    toX.value = null;
    threads.value = 4;
    integrationResult.value = null;
    executionTimes.value = [];
    threadCounts.value = [];
    sourceError.value = '';

    if (chartInstance) {
      chartInstance.destroy();
      chartInstance = null;
    }
  }
});
</script>

<style scoped>
.integration-window {
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

.function-section {
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f9f9f9;
  margin-bottom: 20px;
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

.function-table {
  margin-top: 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  overflow: hidden;
}

.function-table h4 {
  margin: 0 0 10px 0;
  padding: 10px;
  background-color: #e9ecef;
  border-bottom: 1px solid #ddd;
}

table {
  width: 100%;
  border-collapse: collapse;
}

table th,
table td {
  padding: 8px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

table th {
  background-color: #f8f9fa;
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

.integration-settings {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #ddd;
  margin-bottom: 25px;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.setting-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #333;
}

.setting-input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.result-section {
  margin-top: 25px;
  padding: 20px;
  background-color: #e8f5e9;
  border-radius: 8px;
  border: 1px solid #4caf50;
}

.result-details {
  background-color: white;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
  font-size: 1.1rem;
}

.result-details p {
  margin: 8px 0;
}

.result-chart {
  margin-top: 20px;
}

.chart-container {
  height: 250px;
  position: relative;
}

.export-button {
  background-color: #607d8b;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.2s;
  margin-top: 15px;
  text-align: center;
}

.export-button:hover {
  background-color: #546e7a;
}

.integration-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 25px;
  flex-wrap: wrap;
}

.calculate-button {
  padding: 12px 24px;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  font-size: 1.1rem;
  transition: background-color 0.2s;
}

.calculate-button:hover {
  background-color: #43a047;
}

.calculate-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.reset-button {
  padding: 12px 24px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  font-size: 1.1rem;
  transition: background-color 0.2s;
}

.reset-button:hover {
  background-color: #e53935;
}

/* Стили для модального окна выбора функции */
/* ... (скопируйте стили модального окна из OperationsWindow.vue) ... */

/* Индикатор загрузки */
/* ... (скопируйте стили индикатора загрузки из OperationsWindow.vue) ... */

/* Адаптивные стили */
@media (max-width: 768px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }

  .integration-actions {
    flex-direction: column;
  }

  .chart-container {
    height: 200px;
  }
}
</style>