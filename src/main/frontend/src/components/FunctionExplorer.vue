<!-- src/components/FunctionExplorer.vue -->
<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal-content explorer-modal">
      <div class="modal-header">
        <h2>{{ functionName ? `Функция: ${functionName}` : 'Новая функция' }}</h2>
        <button class="close-btn" @click="close">&times;</button>
      </div>
      <div class="modal-body">
        <div v-if="isLoading" class="loading">
          <p>Загрузка данных...</p>
        </div>
        <div v-else>
          <div class="scale-controls">
            <label>
              Масштаб оси X:
              <input
                type="range"
                v-model.number="xAxisScale"
                min="0.1"
                max="10"
                step="0.1"
              />
              <span>{{ xAxisScale.toFixed(1) }}×</span>
            </label>
          </div>
          <div class="chart-section">
            <FunctionChart
              :points="points"
              :show-slider="true"
              :xAxisScale="xAxisScale"
              @point-selected="handlePointSelected"
              @range-changed="handleRangeChanged"
            />
          </div>
          <div class="points-table">
            <div class="table-header">
              <h3>Точки функции ({{ visiblePoints.length }} из {{ points.length }})</h3>
            </div>
            <table v-if="visiblePoints.length > 0">
              <thead>
                <tr>
                  <th>#</th>
                  <th>x</th>
                  <th>y</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(point, index) in visiblePoints"
                  :key="index"
                  :class="{ 'highlighted-row': highlightedIndex === index + visibleRange.start }"
                >
                  <td>{{ index + visibleRange.start + 1 }}</td>
                  <td>{{ formatNumber(point.x) }}</td>
                  <td>{{ formatNumber(point.y) }}</td>
                </tr>
              </tbody>
            </table>
            <p v-else class="empty-table">Нет точек для отображения</p>
            <div v-if="points.length > maxVisiblePoints" class="pagination-controls">
              <div v-if="points.length > 0" class="slider-container">
                <h4>Навигация по точкам:</h4>
                <input
                  type="range"
                  v-model.number="sliderStart"
                  :min="0"
                  :max="points.length - 1"
                  :step="1"
                  @input="syncSliderEnd"
                  class="slider"
                />
                <input
                  type="range"
                  v-model.number="sliderEnd"
                  :min="0"
                  :max="points.length - 1"
                  :step="1"
                  @input="syncSliderStart"
                  class="slider"
                />
                <div class="slider-labels">
                  <span>От: {{ sliderStart }}</span>
                  <span>До: {{ sliderEnd }}</span>
                </div>
              </div>
              <button @click="decreasePage" :disabled="currentPage === 1">
                <i class="fas fa-arrow-left"></i> Предыдущие
              </button>
              <span>Страница {{ currentPage }} из {{ totalPages }}</span>
              <button @click="increasePage" :disabled="currentPage === totalPages">
                Следующие <i class="fas fa-arrow-right"></i>
              </button>
            </div>
          </div>
          <div class="apply-section">
            <h3>Вычисление значения функции</h3>
            <div class="apply-input">
              <label>
                x =
                <input type="number" v-model="applyX" step="any" @keyup.enter="applyFunction">
              </label>
              <button @click="applyFunction" :disabled="!points.length">
                Вычислить f(x)
              </button>
            </div>
            <div v-if="applyResult !== null" class="apply-result">
              <p>f({{ formatNumber(applyX) }}) = {{ formatNumber(applyResult) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import FunctionChart from './FunctionChart.vue'
import { api } from '../api.js'

const props = defineProps({
  functionId: {
    type: Number,
    required: true
  },
  functionName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['close'])

const points = ref([])
const isLoading = ref(false)
const applyX = ref(0)
const applyResult = ref(null)
const highlightedIndex = ref(-1)
const visibleRange = ref({ start: 0, end: 0 })
const currentPage = ref(1)
const maxVisiblePoints = ref(50)
const xAxisScale = ref(1.0) // Масштаб оси X
const sliderStart = ref(0)
const sliderEnd = ref(0)

const syncSliderEnd = () => {
  if (sliderEnd.value < sliderStart.value) sliderEnd.value = sliderStart.value
}

const syncSliderStart = () => {
  if (sliderStart.value > sliderEnd.value) sliderStart.value = sliderEnd.value
}

watch(() => points.value, (newPoints) => {
  if (newPoints.length > 0) {
    sliderStart.value = 0
    sliderEnd.value = newPoints.length - 1
  }
}, { immediate: true })

const formatNumber = (num) => {
  if (typeof num !== 'number' || isNaN(num)) return '0.00'
  if (Math.abs(num) < 0.001 || Math.abs(num) > 1000) {
    return num.toExponential(2)
  }
  return num.toFixed(2)
}

const totalPages = computed(() => {
  return Math.ceil(points.value.length / maxVisiblePoints.value)
})

const visiblePoints = computed(() => {
  if (!points.value.length) return []
  if (sliderEnd.value > sliderStart.value) {
    return points.value.slice(sliderStart.value, sliderEnd.value + 1)
  }
  if (points.value.length > maxVisiblePoints.value) {
    const start = (currentPage.value - 1) * maxVisiblePoints.value
    const end = Math.min(start + maxVisiblePoints.value, points.value.length)
    return points.value.slice(start, end)
  }
  return points.value
})

onMounted(async () => {
  isLoading.value = true
  try {
    const loadedPoints = await api.getTabulatedPointsByFunctionId(props.functionId)
    points.value = loadedPoints.map(p => ({ x: parseFloat(p.xval), y: parseFloat(p.yval) }))
      .sort((a, b) => a.x - b.x)
  } catch (error) {
    console.error('Ошибка загрузки точек:', error)
    alert('Ошибка загрузки точек функции: ' + (error.message || 'неизвестная ошибка'))
  } finally {
    isLoading.value = false
  }
})

const close = () => {
  emit('close')
}

const applyFunction = () => {
  if (!points.value.length) {
    alert('Нет точек для вычисления')
    return
  }
  const x = parseFloat(applyX.value)
  if (isNaN(x)) {
    alert('Введите корректное значение x')
    return
  }
  applyResult.value = interpolate(points.value, x)
}

const interpolate = (points, x) => {
  const sortedPoints = [...points].sort((a, b) => a.x - b.x)
  if (x <= sortedPoints[0].x) return sortedPoints[0].y
  if (x >= sortedPoints[sortedPoints.length - 1].x) return sortedPoints[sortedPoints.length - 1].y
  for (let i = 0; i < sortedPoints.length - 1; i++) {
    if (x >= sortedPoints[i].x && x <= sortedPoints[i + 1].x) {
      const x0 = sortedPoints[i].x
      const y0 = sortedPoints[i].y
      const x1 = sortedPoints[i + 1].x
      const y1 = sortedPoints[i + 1].y
      return y0 + (y1 - y0) * (x - x0) / (x1 - x0)
    }
  }
  return null
}

const handlePointSelected = (pointData) => {
  highlightedIndex.value = pointData.index + (sliderEnd.value > sliderStart.value ? sliderStart.value : (currentPage.value - 1) * maxVisiblePoints.value)
  applyX.value = pointData.x
  setTimeout(() => {
    highlightedIndex.value = -1
  }, 2000)
}

const handleRangeChanged = (range) => {
  visibleRange.value = range
  currentPage.value = 1
}

const decreasePage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const increasePage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}
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
.explorer-modal {
  background: white;
  border-radius: 10px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.3);
  width: 95%;
  max-width: 1000px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
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
.scale-controls {
  padding: 0 15px 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}
.scale-controls label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
  color: #2c3e50;
}
.scale-controls input[type="range"] {
  width: 200px;
}
.scale-controls span {
  min-width: 40px;
  text-align: center;
  font-weight: bold;
  color: #e74c3c;
}
.chart-section {
  padding: 15px;
  flex: 1;
  min-height: 400px;
}
.points-table {
  padding: 0 15px 15px;
  overflow-x: auto;
}
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
  gap: 10px;
}
table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}
table th, table td {
  padding: 10px;
  text-align: left;
  border-bottom: 1px solid #eee;
}
table th {
  background: #f8f9fa;
  font-weight: 600;
}
.empty-table {
  text-align: center;
  padding: 20px;
  color: #7f8c8d;
  font-style: italic;
}
.apply-section {
  padding: 15px;
  border-top: 1px solid #eee;
  background: #f8f9fa;
}
.apply-input {
  display: flex;
  gap: 10px;
  align-items: center;
  margin: 10px 0;
  flex-wrap: wrap;
}
.apply-input input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 120px;
}
.apply-input button {
  padding: 8px 15px;
  background: #9b59b6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.apply-input button:hover {
  background: #8e44ad;
}
.apply-input button:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}
.apply-result {
  margin-top: 15px;
  padding: 10px;
  background: #e8f4f8;
  border-radius: 4px;
  font-weight: 500;
  color: #2980b9;
}
.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  font-size: 1.2rem;
  color: #7f8c8d;
}
.slider-container {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #eee;
}
.slider-container h4 {
  margin-bottom: 10px;
  color: #333;
}
.slider {
  width: 100%;
  margin: 5px 0;
  height: 8px;
  appearance: none;
  background: #ddd;
  outline: none;
  border-radius: 4px;
}
.slider::-webkit-slider-thumb {
  appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #2196f3;
  cursor: pointer;
  border: 2px solid white;
}
.slider::-moz-range-thumb {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #2196f3;
  cursor: pointer;
  border: 2px solid white;
}
.slider-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 5px;
  font-size: 0.9em;
  color: #666;
}
/* Кастомный скроллбар */
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