<!-- src/components/IntegrationWindow.vue -->
<template>
  <div class="modal-overlay" @click.self="close">
    <div class="integration-window">
      <div class="window-header">
        <h2>Вычисление определенного интеграла</h2>
        <button class="close-button" @click="close">&times;</button>
      </div>

      <div class="window-body" ref="windowBody">
        <div v-if="isLoading" class="loading">
          <div class="spinner"></div>
          <p>Загрузка данных...</p>
        </div>

        <div v-else>
          <div class="section">
            <h3>Выберите функцию для интегрирования</h3>
            <div class="function-controls">
              <select v-model="selectedFunctionId" @change="loadFunctionPoints" class="function-select">
                <option value="">Выберите функцию</option>
                <option v-for="func in availableFunctions" :key="func.functionId" :value="func.functionId">
                  {{ func.functionName }} (ID: {{ func.functionId }})
                </option>
              </select>
            </div>

            <div v-if="selectedFunction" class="function-info">
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">Имя:</span>
                  <span class="info-value">{{ selectedFunction.functionName }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Тип:</span>
                  <span class="info-value">{{ selectedFunction.typeFunction === 'tabular' ? 'Табличная' : 'Математическая' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Область определения:</span>
                  <span class="info-value">от {{ domainStart.toFixed(2) }} до {{ domainEnd.toFixed(2) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Точек:</span>
                  <span class="info-value">{{ functionPoints.length }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="functionPoints.length > 0" class="section">
            <h3>Параметры вычисления</h3>

            <div class="settings-grid">
              <div class="setting-group">
                <label class="setting-label">
                  <span class="label-text">Начало интервала (a):</span>
                  <input
                    type="number"
                    v-model="integrationStart"
                    step="any"
                    :min="domainStart"
                    :max="domainEnd"
                    class="setting-input"
                  >
                </label>
              </div>

              <div class="setting-group">
                <label class="setting-label">
                  <span class="label-text">Конец интервала (b):</span>
                  <input
                    type="number"
                    v-model="integrationEnd"
                    step="any"
                    :min="domainStart"
                    :max="domainEnd"
                    class="setting-input"
                  >
                </label>
              </div>

              <div class="setting-group">
                <label class="setting-label">
                  <span class="label-text">Количество разбиений (n):</span>
                  <input
                    type="number"
                    v-model="integrationSteps"
                    min="10"
                    max="1000000"
                    step="1"
                    class="setting-input"
                  >
                  <span class="setting-hint">(рекомендуется от 1000 до 100000)</span>
                </label>
              </div>

              <div class="setting-group">
                <label class="setting-label">
                  <span class="label-text">Количество потоков:</span>
                  <input
                    type="number"
                    v-model="threadCount"
                    min="1"
                    :max="maxThreads"
                    step="1"
                    class="setting-input"
                  >
                  <span class="setting-hint">(максимум {{ maxThreads }})</span>
                </label>
              </div>
            </div>

            <div class="performance-info">
              <div class="performance-stats">
                <div class="stat-item">
                  <span class="stat-label">Доступных ядер:</span>
                  <span class="stat-value">{{ availableCores }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">Рекомендуется потоков:</span>
                  <span class="stat-value">{{ recommendedThreads }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="functionPoints.length > 0" class="section">
            <div class="chart-header">
              <h3>Область интегрирования</h3>
              <div class="chart-legend">
                <div class="legend-item">
                  <span class="legend-color integration-area"></span>
                  <span class="legend-text">Площадь под кривой</span>
                </div>
              </div>
            </div>
            <FunctionChart
              :points="chartPoints"
              :show-slider="false"
              :integration-range="{ start: integrationStart, end: integrationEnd }"
            />
          </div>

          <div class="actions-section">
            <button
              @click="calculateIntegral"
              :disabled="!canCalculate || isCalculating"
              class="calculate-button"
            >
              <span class="btn-content">
                <span v-if="isCalculating" class="btn-spinner"></span>
                <span class="btn-icon">∫</span>
                <span class="btn-text">
                  {{ isCalculating ? 'Вычисление...' : 'Вычислить интеграл' }}
                </span>
              </span>
            </button>

            <button @click="reset" class="reset-button">
              <span class="btn-content">
                <span class="btn-icon">↺</span>
                <span class="btn-text">Сбросить</span>
              </span>
            </button>

            <div class="scroll-controls">
              <button @click="scrollToTop" class="scroll-button" title="В начало">
                <span class="scroll-icon">↑</span>
              </button>
              <button @click="scrollToBottom" class="scroll-button" title="В конец">
                <span class="scroll-icon">↓</span>
              </button>
            </div>
          </div>

          <div v-if="errorMessage" class="error-section">
            <p class="error-message">
              <i class="fas fa-exclamation-circle"></i> {{ errorMessage }}
            </p>
          </div>

          <div v-if="calculationResult !== null" class="section result-section">
            <div class="result-header">
              <h3>Результат вычисления</h3>
              <div class="result-badge">
                ∫<sub>{{ integrationStart.toFixed(2) }}</sub><sup>{{ integrationEnd.toFixed(2) }}</sup> f(x) dx
              </div>
            </div>

            <div class="result-content">
              <div class="result-main">
                <div class="result-value">
                  {{ formattedResult }}
                </div>
                <div class="result-label">Значение интеграла</div>
              </div>

              <div class="result-details">
                <div class="detail-item">
                  <span class="detail-label">Время выполнения:</span>
                  <span class="detail-value">{{ executionTime }} мс</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">Количество потоков:</span>
                  <span class="detail-value">{{ threadCount }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">Метод:</span>
                  <span class="detail-value">Параллельный метод трапеций</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">Разбиений:</span>
                  <span class="detail-value">{{ integrationSteps.toLocaleString() }}</span>
                </div>
              </div>
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
const errorMessage = ref('')
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
const windowBody = ref(null)
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
  return functionPoints.value.filter(p =>
    p.x >= integrationStart.value && p.x <= integrationEnd.value
  )
})

// Исправленный метод форматирования результата
const formattedResult = computed(() => {
  if (calculationResult.value === null) return '0.000000'

  const value = Number(calculationResult.value)
  if (isNaN(value)) return '0.000000'

  const absValue = Math.abs(value)
  if (absValue < 0.001 || absValue > 10000) {
    return value.toExponential(6)
  }
  return value.toFixed(6)
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
  if (windowBody.value) {
    windowBody.value.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  }
}

const scrollToBottom = () => {
  if (windowBody.value) {
    windowBody.value.scrollTo({
      top: windowBody.value.scrollHeight,
      behavior: 'smooth'
    })
  }
}

// Обработчик прокрутки
const handleScroll = () => {
  if (!windowBody.value) return

  const scrollTop = windowBody.value.scrollTop
  const scrollHeight = windowBody.value.scrollHeight
  const clientHeight = windowBody.value.clientHeight

  showScrollIndicator.value = scrollHeight > clientHeight

  if (scrollHeight > clientHeight) {
    scrollProgress.value = (scrollTop / (scrollHeight - clientHeight)) * 100
  } else {
    scrollProgress.value = 0
  }
}

// Загрузка доступных функций
const loadAvailableFunctions = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const userId = api.getStoredUserId()
    const functions = await api.getFunctionsByUserId(userId)
    availableFunctions.value = functions.filter(f =>
      f.typeFunction === 'tabular' || f.implementsMathFunction
    )

    if (availableFunctions.value.length > 0 && !selectedFunctionId.value) {
      selectedFunctionId.value = availableFunctions.value[0].functionId
      await loadFunctionPoints()
    }
  } catch (error) {
    console.error('Ошибка загрузки функций:', error)
    errorMessage.value = 'Ошибка загрузки списка функций: ' + (error.message || 'неизвестная ошибка')
  } finally {
    isLoading.value = false
  }
}

// Загрузка точек выбранной функции
const loadFunctionPoints = async () => {
  if (!selectedFunctionId.value) return

  isLoading.value = true
  errorMessage.value = ''
  try {
    const userId = api.getStoredUserId()
    const allFunctions = await api.getFunctionsByUserId(userId)
    selectedFunction.value = allFunctions.find(f => f.functionId === selectedFunctionId.value)

    if (!selectedFunction.value) {
      throw new Error('Функция не найдена')
    }

    const points = await api.getTabulatedPointsByFunctionId(selectedFunctionId.value)
    functionPoints.value = points.map(p => ({
      x: parseFloat(p.xval),
      y: parseFloat(p.yval)
    })).sort((a, b) => a.x - b.x)

    // Проверяем, что есть точки и они различны
    if (functionPoints.value.length === 0) {
      throw new Error('Функция не содержит точек для интегрирования')
    }

    integrationStart.value = domainStart.value
    integrationEnd.value = domainEnd.value

    // Увеличиваем количество шагов для более точного вычисления
    integrationSteps.value = Math.min(1000000, Math.max(1000, functionPoints.value.length * 100))
    threadCount.value = recommendedThreads.value
  } catch (error) {
    console.error('Ошибка загрузки точек функции:', error)
    errorMessage.value = 'Ошибка загрузки точек функции: ' + (error.message || 'неизвестная ошибка')
  } finally {
    isLoading.value = false
  }
}

// Вычисление интеграла
const calculateIntegral = async () => {
  if (!canCalculate.value) {
    const missingItems = []
    if (!selectedFunctionId.value) missingItems.push('функция')
    if (functionPoints.value.length === 0) missingItems.push('точки функции')
    if (integrationStart.value >= integrationEnd.value) missingItems.push('корректный интервал')
    if (integrationSteps.value < 10) missingItems.push('достаточное количество разбиений')

    errorMessage.value = `Невозможно вычислить интеграл. Не хватает: ${missingItems.join(', ')}.`
    return
  }

  isCalculating.value = true
  calculationResult.value = null
  errorMessage.value = ''

  try {
    const startTime = performance.now()

    const result = await api.calculateIntegral(
      selectedFunctionId.value,
      parseFloat(integrationStart.value),
      parseFloat(integrationEnd.value),
      parseInt(integrationSteps.value),
      parseInt(threadCount.value)
    )

    // Универсальная обработка разных форматов ответа
    let integralValue
    if (typeof result === 'number') {
      integralValue = result
    } else if (result && typeof result.result === 'number') {
      integralValue = result.result
    } else if (result && typeof result.value === 'number') {
      integralValue = result.value
    } else if (result && result.data && typeof result.data.value === 'number') {
      integralValue = result.data.value
    } else if (result && result.data && typeof result.data === 'number') {
      integralValue = result.data
    } else {
      throw new Error('Некорректный формат ответа от сервера')
    }

    const endTime = performance.now()

    calculationResult.value = integralValue
    executionTime.value = (endTime - startTime).toFixed(2)

    performanceData.value.push({
      threads: threadCount.value,
      time: parseFloat(executionTime.value),
      result: integralValue
    })

    renderPerformanceChart()

    setTimeout(() => {
      scrollToBottom()
    }, 100)
  } catch (error) {
    console.error('Ошибка вычисления интеграла:', error)
    errorMessage.value = 'Ошибка вычисления интеграла: ' + (error.message || 'неизвестная ошибка')

    // Сбрасываем результат в случае ошибки
    calculationResult.value = null
  } finally {
    isCalculating.value = false
  }
}

// Отрисовка графика производительности
const renderPerformanceChart = () => {
  if (!performanceChart.value || performanceData.value.length === 0) return

  const ctx = performanceChart.value.getContext('2d')

  if (integrationChartInstance) {
    integrationChartInstance.destroy()
  }

  const threadCounts = performanceData.value.map(d => d.threads)
  const executionTimes = performanceData.value.map(d => d.time)

  integrationChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: threadCounts.map(t => `${t} потоков`),
      datasets: [{
        label: 'Время выполнения (мс)',
        data: executionTimes,
        backgroundColor: '#ff4fc4',
        borderColor: '#ff6fda',
        borderWidth: 2,
        borderRadius: 6
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
            text: 'Время (мс)',
            color: '#ffffff'
          },
          grid: {
            color: 'rgba(255, 255, 255, 0.1)'
          },
          ticks: {
            color: '#ffffff'
          }
        },
        x: {
          title: {
            display: true,
            text: 'Количество потоков',
            color: '#ffffff'
          },
          grid: {
            display: false
          },
          ticks: {
            color: '#ffffff'
          }
        }
      },
      plugins: {
        legend: {
          display: false
        },
        title: {
          display: true,
          text: 'Зависимость времени выполнения от количества потоков',
          color: '#ffffff',
          font: {
            size: 14
          }
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
  errorMessage.value = ''

  if (integrationChartInstance) {
    integrationChartInstance.destroy()
    integrationChartInstance = null
  }

  scrollToTop()
}

const close = () => {
  emit('close')
}

// Обработчик создания новой функции
const handleFunctionCreated = async (event) => {
  const { functionId, functionName } = event.detail
  await loadAvailableFunctions()
  selectedFunctionId.value = functionId
  await loadFunctionPoints()
}

// Инициализация
onMounted(async () => {
  threadCount.value = recommendedThreads.value
  await loadAvailableFunctions()

  window.addEventListener('function-created', handleFunctionCreated)

  if (windowBody.value) {
    windowBody.value.addEventListener('scroll', handleScroll)
  }

  setTimeout(handleScroll, 100)
})

// Очистка при размонтировании
onUnmounted(() => {
  window.removeEventListener('function-created', handleFunctionCreated)

  if (integrationChartInstance) {
    integrationChartInstance.destroy()
    integrationChartInstance = null
  }

  if (windowBody.value) {
    windowBody.value.removeEventListener('scroll', handleScroll)
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
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.integration-window {
  position: relative;
  padding: 25px 20px;
  border-radius: 16px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.6);
  max-width: 900px;
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
  margin-bottom: 1.8rem;
  text-align: center;
  padding: 0 20px;
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
  max-height: calc(90vh - 120px);
}

.section {
  margin-bottom: 25px;
  padding: 20px;
  background: rgba(47, 16, 92, 0.3);
  border-radius: 12px;
  border: 1px solid #5b1fa8;
}

.loading {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 200px;
  gap: 15px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #5b1fa8;
  border-top: 4px solid #ff4fc4;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.function-controls {
  display: flex;
  gap: 12px;
  margin: 15px 0;
  flex-wrap: wrap;
  align-items: center;
}

.function-select {
  flex: 1;
  min-width: 250px;
  padding: 10px;
  border: 1px solid #5b1fa8;
  border-radius: 8px;
  background-color: #2f105c;
  color: #ffffff;
  font-size: 1rem;
}

.function-select:focus {
  outline: none;
  border-color: #ff4fc4;
}

.create-button {
  background-color: #ff4fc4;
  color: #ffffff;
  border: none;
  padding: 10px 18px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  transition: background-color 0.25s ease;
}

.create-button:hover {
  background-color: #ff6fda;
}

.btn-icon {
  font-weight: bold;
}

.function-info {
  margin-top: 15px;
  padding: 15px;
  background: rgba(91, 31, 168, 0.2);
  border-radius: 8px;
  border-left: 3px solid #ff4fc4;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
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

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
  margin: 15px 0;
}

.setting-group {
  display: flex;
  flex-direction: column;
}

.setting-label {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label-text {
  font-weight: 600;
  color: #ffffff;
  font-size: 0.95rem;
}

.setting-input {
  padding: 10px;
  border: 1px solid #5b1fa8;
  border-radius: 8px;
  background-color: #2f105c;
  color: #ffffff;
  font-size: 1rem;
  transition: border-color 0.25s ease;
}

.setting-input:focus {
  outline: none;
  border-color: #ff4fc4;
}

/* Убираем стрелки у number input */
.setting-input[type=number]::-webkit-outer-spin-button,
.setting-input[type=number]::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.setting-input[type=number] {
  -moz-appearance: textfield;
}

.setting-hint {
  font-size: 0.85rem;
  color: #cccccc;
  font-style: italic;
  margin-top: 4px;
}

.performance-info {
  margin-top: 15px;
  padding: 15px;
  background: rgba(33, 150, 243, 0.2);
  border-radius: 8px;
  border-left: 3px solid #2196f3;
}

.performance-stats {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-weight: 600;
  color: #90caf9;
  font-size: 0.9rem;
}

.stat-value {
  color: #ffffff;
  font-weight: 700;
  font-size: 1.1rem;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}

.chart-legend {
  display: flex;
  gap: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-color {
  width: 14px;
  height: 14px;
  border-radius: 3px;
}

.integration-area {
  background: #ff4fc4;
}

.legend-text {
  font-size: 0.9rem;
  color: #cccccc;
}

.actions-section {
  display: flex;
  gap: 12px;
  padding: 20px;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  position: relative;
  background: rgba(47, 16, 92, 0.3);
  border-radius: 12px;
  margin: 20px 0;
}

.calculate-button, .reset-button {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: background-color 0.25s ease;
  min-width: 180px;
  justify-content: center;
}

.calculate-button {
  background-color: #2196f3;
  color: #ffffff;
}

.calculate-button:hover:not(:disabled) {
  background-color: #1976d2;
}

.calculate-button:disabled {
  background-color: #888;
  cursor: not-allowed;
}

.reset-button {
  background-color: #e74c3c;
  color: #ffffff;
}

.reset-button:hover {
  background-color: #c0392b;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid transparent;
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.scroll-controls {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.scroll-button {
  background-color: #5b1fa8;
  color: white;
  border: none;
  width: 35px;
  height: 35px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.25s ease;
  font-size: 0.9rem;
}

.scroll-button:hover {
  background-color: #7b1fa8;
}

.scroll-indicator {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: rgba(91, 31, 168, 0.3);
  z-index: 10;
}

.scroll-progress {
  height: 100%;
  background: linear-gradient(90deg, #5b1fa8, #ff4fc4);
  transition: width 0.1s ease;
  border-radius: 0 2px 2px 0;
}

.result-section {
  background: rgba(155, 89, 182, 0.2);
  border-color: #9b59b6;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.result-badge {
  background: linear-gradient(135deg, #5b1fa8, #ff4fc4);
  color: white;
  padding: 6px 12px;
  border-radius: 16px;
  font-weight: 600;
  font-size: 1rem;
}

.result-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin: 20px 0;
}

.result-main {
  text-align: center;
  padding: 20px;
  background: rgba(47, 16, 92, 0.5);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border: 1px solid #5b1fa8;
}

.result-value {
  font-size: 2rem;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 8px;
  font-family: 'Courier New', monospace;
}

.result-label {
  color: #cccccc;
  font-size: 1rem;
  font-weight: 600;
}

.result-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  background: rgba(47, 16, 92, 0.5);
  border-radius: 12px;
  border: 1px solid #5b1fa8;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid rgba(91, 31, 168, 0.3);
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  font-weight: 600;
  color: #cccccc;
}

.detail-value {
  font-weight: 500;
  color: #ffffff;
}

.performance-chart {
  margin-top: 20px;
}

.performance-chart h4 {
  margin: 0 0 15px 0;
  color: #ffffff;
}

.chart-container {
  height: 250px;
  position: relative;
  background: rgba(47, 16, 92, 0.5);
  border-radius: 8px;
  padding: 15px;
  border: 1px solid #5b1fa8;
}

.error-section {
  background-color: rgba(231, 76, 60, 0.15);
  border: 1px solid #e74c3c;
  border-radius: 8px;
  padding: 15px;
  margin: 15px 0;
  color: #ff4fc4;
  font-size: 0.95rem;
}

.error-message {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.fas {
  color: #e74c3c;
}

@media (max-width: 768px) {
  .integration-window {
    width: 95%;
    margin: 10px;
    max-height: 95vh;
    padding: 20px 15px;
  }

  .window-header {
    padding: 0 10px;
  }

  h2 {
    font-size: 1.5rem;
  }

  .section {
    padding: 15px;
    margin-bottom: 20px;
  }

  .function-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .function-select {
    min-width: auto;
  }

  .settings-grid {
    grid-template-columns: 1fr;
  }

  .actions-section {
    flex-direction: column;
  }

  .scroll-controls {
    margin-left: 0;
    margin-top: 12px;
    order: 3;
  }

  .result-content {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .result-value {
    font-size: 1.8rem;
  }

  .window-body {
    max-height: calc(95vh - 100px);
  }
}

@media (max-width: 480px) {
  .integration-window {
    padding: 15px 10px;
  }

  .section {
    padding: 12px;
    margin-bottom: 15px;
  }

  .calculate-button, .reset-button {
    min-width: auto;
    width: 100%;
    padding: 10px 20px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .performance-stats {
    flex-direction: column;
    gap: 12px;
  }

  .chart-header {
    flex-direction: column;
    align-items: flex-start;
  }
}

/* Кастомный скроллбар */
.window-body::-webkit-scrollbar {
  width: 6px;
}

.window-body::-webkit-scrollbar-track {
  background: #2f105c;
  border-radius: 3px;
}

.window-body::-webkit-scrollbar-thumb {
  background: #5b1fa8;
  border-radius: 3px;
}

.window-body::-webkit-scrollbar-thumb:hover {
  background: #ff4fc4;
}
</style>