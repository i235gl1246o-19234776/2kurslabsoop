<!-- src/components/IntegrationWindow.vue -->
<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal-content integration-modal">
      <div class="modal-header">
        <h2>Вычисление определенного интеграла</h2>
        <button class="close-btn" @click="close">&times;</button>
      </div>

      <div class="modal-body" ref="modalBody">
        <div v-if="isLoading" class="loading">
          <p>Загрузка данных...</p>
        </div>

        <div v-else>
          <div class="function-selection">
            <h3>Выберите функцию для интегрирования</h3>
            <div class="function-controls">
              <select v-model="selectedFunctionId" @change="loadFunctionPoints">
                <option value="">Выберите функцию</option>
                <option v-for="func in availableFunctions" :key="func.functionId" :value="func.functionId">
                  {{ func.functionName }} (ID: {{ func.functionId }})
                </option>
              </select>
              <button @click="openFunctionCreator" class="create-btn">
                <i class="fas fa-plus"></i> Создать новую
              </button>
            </div>

            <div v-if="selectedFunction" class="function-info">
              <p><strong>Имя:</strong> {{ selectedFunction.functionName }}</p>
              <p><strong>Тип:</strong> {{ selectedFunction.typeFunction === 'tabular' ? 'Табличная' : 'Математическая' }}</p>
              <p><strong>Область определения:</strong> от {{ domainStart }} до {{ domainEnd }}</p>
              <p><strong>Количество точек:</strong> {{ functionPoints.length }}</p>
            </div>
          </div>

          <div v-if="functionPoints.length > 0" class="integration-settings">
            <h3>Параметры вычисления</h3>

            <div class="settings-row">
              <label>
                <span>Начало интервала (a):</span>
                <input type="number" v-model="integrationStart" step="any" :min="domainStart" :max="domainEnd">
              </label>
              <label>
                <span>Конец интервала (b):</span>
                <input type="number" v-model="integrationEnd" step="any" :min="domainStart" :max="domainEnd">
              </label>
            </div>

            <div class="settings-row">
              <label>
                <span>Количество разбиений (n):</span>
                <input type="number" v-model="integrationSteps" min="10" max="1000000" step="1000">
                <span class="hint">(рекомендуется от 1000 до 100000)</span>
              </label>
              <label>
                <span>Количество потоков:</span>
                <input type="number" v-model="threadCount" min="1" :max="maxThreads" step="1">
                <span class="hint">(максимум {{ maxThreads }})</span>
              </label>
            </div>

            <div class="performance-info">
              <p>Доступных процессорных ядер: {{ availableCores }}</p>
              <p>Рекомендуемое количество потоков: {{ recommendedThreads }}</p>
            </div>
          </div>

          <div v-if="functionPoints.length > 0" class="integration-chart">
            <FunctionChart
              :points="chartPoints"
              :show-slider="false"
              chart-title="Область интегрирования"
            />
          </div>

          <div class="integration-actions">
            <button
              @click="calculateIntegral"
              :disabled="!canCalculate || isCalculating"
              class="calculate-btn"
            >
              <span v-if="isCalculating">
                <i class="fas fa-spinner fa-spin"></i> Вычисление...
              </span>
              <span v-else>
                <i class="fas fa-calculator"></i> Вычислить интеграл
              </span>
            </button>

            <button @click="reset" class="reset-btn">
              <i class="fas fa-redo"></i> Сбросить
            </button>

            <!-- Кнопки прокрутки -->
            <div class="scroll-controls">
              <button @click="scrollToTop" class="scroll-btn" title="В начало">
                <i class="fas fa-arrow-up"></i>
              </button>
              <button @click="scrollToBottom" class="scroll-btn" title="В конец">
                <i class="fas fa-arrow-down"></i>
              </button>
            </div>
          </div>

          <div v-if="calculationResult" class="result-section">
            <h3>Результат вычисления</h3>
            <div class="result-details">
              <p><strong>Значение интеграла:</strong> {{ formattedResult }}</p>
              <p><strong>Время выполнения:</strong> {{ executionTime }} мс</p>
              <p><strong>Количество потоков:</strong> {{ threadCount }}</p>
              <p><strong>Метод:</strong> Параллельный метод трапеций</p>
            </div>

            <div class="performance-chart" v-if="performanceData.length > 0">
              <h4>Сравнение производительности</h4>
              <div class="chart-container">
                <canvas ref="performanceChart"></canvas>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Индикатор прокрутки -->
      <div class="scroll-indicator" v-if="showScrollIndicator">
        <div class="scroll-progress" :style="{ width: scrollProgress + '%' }"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import FunctionChart from './FunctionChart.vue'
import { api } from '../api.js'
import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)

const emit = defineEmits(['close'])

const isLoading = ref(false)
const isCalculating = ref(false)
const availableFunctions = ref([])
const selectedFunctionId = ref(null)
const selectedFunction = ref(null)
const functionPoints = ref([])
const calculationResult = ref(null)
const executionTime = ref(0)
const integrationStart = ref(0)
const integrationEnd = ref(0)
const integrationSteps = ref(10000)
const threadCount = ref(4)
const maxThreads = ref(16)
const availableCores = ref(navigator.hardwareConcurrency || 8)
const performanceData = ref([])
const modalBody = ref(null)
const showScrollIndicator = ref(false)
const scrollProgress = ref(0)

// Инициализация максимального количества потоков
maxThreads.value = Math.min(16, availableCores.value * 2)

const recommendedThreads = computed(() => {
  return Math.min(8, availableCores.value)
})

const domainStart = computed(() => {
  if (functionPoints.value.length === 0) return 0
  return Math.min(...functionPoints.value.map(p => p.x))
})

const domainEnd = computed(() => {
  if (functionPoints.value.length === 0) return 0
  return Math.max(...functionPoints.value.map(p => p.x))
})

const chartPoints = computed(() => {
  if (!selectedFunctionId.value || functionPoints.value.length === 0) return []

  // Фильтруем точки для отображения только в пределах выбранного интервала
  return functionPoints.value.filter(p =>
    p.x >= integrationStart.value && p.x <= integrationEnd.value
  )
})

const formattedResult = computed(() => {
  if (!calculationResult.value) return '0.0000'
  const absValue = Math.abs(calculationResult.value)

  if (absValue < 0.001 || absValue > 10000) {
    return calculationResult.value.toExponential(6)
  }
  return calculationResult.value.toFixed(6)
})

const canCalculate = computed(() => {
  return selectedFunctionId.value &&
         functionPoints.value.length > 0 &&
         integrationStart.value < integrationEnd.value &&
         integrationSteps.value >= 10
})

const performanceChart = ref(null)
let integrationChartInstance = null

// Функции прокрутки
const scrollToTop = () => {
  if (modalBody.value) {
    modalBody.value.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  }
}

const scrollToBottom = () => {
  if (modalBody.value) {
    modalBody.value.scrollTo({
      top: modalBody.value.scrollHeight,
      behavior: 'smooth'
    })
  }
}

// Обработчик прокрутки
const handleScroll = () => {
  if (!modalBody.value) return

  const scrollTop = modalBody.value.scrollTop
  const scrollHeight = modalBody.value.scrollHeight
  const clientHeight = modalBody.value.clientHeight

  // Показываем индикатор только если контент превышает высоту контейнера
  showScrollIndicator.value = scrollHeight > clientHeight

  // Вычисляем прогресс прокрутки
  if (scrollHeight > clientHeight) {
    scrollProgress.value = (scrollTop / (scrollHeight - clientHeight)) * 100
  } else {
    scrollProgress.value = 0
  }
}

// Загрузка доступных функций
const loadAvailableFunctions = async () => {
  isLoading.value = true
  try {
    const userId = api.getStoredUserId()
    const functions = await api.getFunctionsByUserId(userId)
    availableFunctions.value = functions.filter(f =>
      f.typeFunction === 'tabular' || f.implementsMathFunction
    )

    // Автоматически выбираем первую функцию, если есть
    if (availableFunctions.value.length > 0 && !selectedFunctionId.value) {
      selectedFunctionId.value = availableFunctions.value[0].functionId
      await loadFunctionPoints()
    }
  } catch (error) {
    console.error('Ошибка загрузки функций:', error)
    alert('Ошибка загрузки списка функций: ' + error.message)
  } finally {
    isLoading.value = false
  }
}

// Загрузка точек выбранной функции
const loadFunctionPoints = async () => {
  if (!selectedFunctionId.value) return

  isLoading.value = true
  try {
    // Получаем информацию о функции
    const userId = api.getStoredUserId()
    const allFunctions = await api.getFunctionsByUserId(userId)
    selectedFunction.value = allFunctions.find(f => f.functionId === selectedFunctionId.value)

    if (!selectedFunction.value) {
      throw new Error('Функция не найдена')
    }

    // Получаем точки функции
    const points = await api.getTabulatedPointsByFunctionId(selectedFunctionId.value)
    functionPoints.value = points.map(p => ({
      x: parseFloat(p.xval),
      y: parseFloat(p.yval)
    })).sort((a, b) => a.x - b.x)

    // Устанавливаем границы по умолчанию
    integrationStart.value = domainStart.value
    integrationEnd.value = domainEnd.value

    // Устанавливаем рекомендуемое количество разбиений
    integrationSteps.value = Math.min(100000, Math.max(1000, functionPoints.value.length * 10))

    threadCount.value = recommendedThreads.value
  } catch (error) {
    console.error('Ошибка загрузки точек функции:', error)
    alert('Ошибка загрузки точек функции: ' + error.message)
  } finally {
    isLoading.value = false
  }
}

// Вычисление интеграла
// В методе calculateIntegral в IntegrationWindow.vue
// В методе calculateIntegral
const calculateIntegral = async () => {
  if (!canCalculate.value) return

  isCalculating.value = true
  calculationResult.value = null

  try {
    const startTime = performance.now()

    const result = await api.calculateIntegral(
      selectedFunctionId.value,
      parseFloat(integrationStart.value),
      parseFloat(integrationEnd.value),
      parseInt(integrationSteps.value),
      parseInt(threadCount.value)
    )

    const endTime = performance.now()

    calculationResult.value = result.value
    executionTime.value = (endTime - startTime).toFixed(2)

    // Сохраняем данные для сравнения производительности
    performanceData.value.push({
      threads: threadCount.value,
      time: parseFloat(executionTime.value),
      result: calculationResult.value
    })

    renderPerformanceChart()

    console.log('Результат интегрирования (метод трапеций):', result)

    setTimeout(() => {
      scrollToBottom()
    }, 100)
  } catch (error) {
    console.error('Ошибка вычисления интеграла:', error)
    alert('Ошибка вычисления интеграла: ' + (error.message || 'неизвестная ошибка'))
  } finally {
    isCalculating.value = false
  }
}

// Отрисовка графика производительности
const renderPerformanceChart = () => {
  if (!performanceChart.value || performanceData.value.length === 0) return

  const ctx = performanceChart.value.getContext('2d')

  // Если уже есть график, уничтожаем его
  if (integrationChartInstance) {
    integrationChartInstance.destroy()
  }

  // Подготавливаем данные
  const threadCounts = performanceData.value.map(d => d.threads)
  const executionTimes = performanceData.value.map(d => d.time)

  integrationChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: threadCounts.map(t => `${t} потоков`),
      datasets: [{
        label: 'Время выполнения (мс)',
        data: executionTimes,
        backgroundColor: 'rgba(54, 162, 235, 0.5)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1
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
          }
        }
      },
      plugins: {
        title: {
          display: true,
          text: 'Зависимость времени выполнения от количества потоков'
        }
      }
    }
  })
}

// Открытие окна создания функции
const openFunctionCreator = () => {
  window.dispatchEvent(new CustomEvent('open-create-function', {
    detail: { operand: 'integration' }
  }))
}

// Сброс результатов
const reset = () => {
  calculationResult.value = null
  executionTime.value = 0
  performanceData.value = []

  if (integrationChartInstance) {
    integrationChartInstance.destroy()
    integrationChartInstance = null
  }

  // Прокручиваем к началу при сбросе
  scrollToTop()
}

const close = () => {
  emit('close')
}

// Обработчик создания новой функции
const handleFunctionCreated = async (event) => {
  const { functionId, functionName } = event.detail

  // Обновляем список доступных функций
  await loadAvailableFunctions()

  // Выбираем созданную функцию
  selectedFunctionId.value = functionId
  await loadFunctionPoints()
}

// Инициализация
onMounted(async () => {
  threadCount.value = recommendedThreads.value
  await loadAvailableFunctions()

  // Подписка на событие создания функции
  window.addEventListener('function-created', handleFunctionCreated)

  // Добавляем обработчик прокрутки
  if (modalBody.value) {
    modalBody.value.addEventListener('scroll', handleScroll)
  }

  // Инициализируем состояние прокрутки
  setTimeout(handleScroll, 100)
})

// Очистка при размонтировании
onUnmounted(() => {
  window.removeEventListener('function-created', handleFunctionCreated)

  if (integrationChartInstance) {
    integrationChartInstance.destroy()
    integrationChartInstance = null
  }

  // Удаляем обработчик прокрутки
  if (modalBody.value) {
    modalBody.value.removeEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.integration-modal {
  background: white;
  border-radius: 10px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.3);
  width: 95%;
  max-width: 1000px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #2c3e50;
  color: white;
  border-bottom: 2px solid #34495e;
  flex-shrink: 0;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  max-height: calc(90vh - 70px);
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.function-selection, .integration-settings, .result-section {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.function-controls {
  display: flex;
  gap: 10px;
  margin: 10px 0;
  flex-wrap: wrap;
}

.function-controls select {
  flex: 1;
  min-width: 200px;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.create-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.function-info {
  margin-top: 15px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 3px solid #3498db;
}

.settings-row {
  display: flex;
  gap: 20px;
  margin: 15px 0;
  flex-wrap: wrap;
}

.settings-row label {
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex: 1;
  min-width: 250px;
}

.settings-row input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 100%;
}

.hint {
  font-size: 0.85rem;
  color: #7f8c8d;
  font-style: italic;
}

.performance-info {
  margin-top: 15px;
  padding: 12px;
  background: #e8f4f8;
  border-radius: 6px;
  border-left: 3px solid #2980b9;
}

.integration-chart {
  padding: 15px;
  min-height: 350px;
}

.integration-actions {
  display: flex;
  gap: 15px;
  padding: 15px;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  position: relative;
}

.calculate-btn, .reset-btn {
  padding: 12px 25px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.calculate-btn {
  background: #9b59b6;
  color: white;
}

.calculate-btn:hover:not(:disabled) {
  background: #8e44ad;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.calculate-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.reset-btn {
  background: #e74c3c;
  color: white;
}

.reset-btn:hover {
  background: #c0392b;
}

/* Стили для кнопок прокрутки */
.scroll-controls {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.scroll-btn {
  background: #95a5a6;
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.scroll-btn:hover {
  background: #7f8c8d;
  transform: scale(1.1);
}

/* Индикатор прокрутки */
.scroll-indicator {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.scroll-progress {
  height: 100%;
  background: linear-gradient(90deg, #3498db, #9b59b6);
  transition: width 0.1s ease;
  border-radius: 0 2px 2px 0;
}

.result-section {
  background: #f8f9fa;
}

.result-details {
  margin: 15px 0;
  padding: 15px;
  background: white;
  border-radius: 8px;
  border: 1px solid #eee;
  font-size: 1.1rem;
}

.result-details p {
  margin: 8px 0;
}

.performance-chart {
  margin-top: 20px;
}

.chart-container {
  height: 250px;
  position: relative;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  font-size: 1.2rem;
  color: #7f8c8d;
}

@media (max-width: 768px) {
  .settings-row {
    flex-direction: column;
  }

  .integration-modal {
    width: 98%;
    margin: 10px;
  }

  .function-controls {
    flex-direction: column;
  }

  .integration-actions {
    flex-direction: column;
  }

  .scroll-controls {
    margin-left: 0;
    margin-top: 10px;
    order: 3;
  }

  .modal-body {
    max-height: calc(90vh - 120px);
  }
}

@media (max-width: 480px) {
  .integration-actions {
    gap: 10px;
  }

  .calculate-btn, .reset-btn {
    padding: 10px 20px;
    font-size: 0.9rem;
  }

  .scroll-btn {
    width: 35px;
    height: 35px;
  }
}

/* Кастомный скроллбар для модального окна */
.modal-body::-webkit-scrollbar {
  width: 8px;
}

.modal-body::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>