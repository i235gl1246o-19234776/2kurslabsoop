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
          <div class="file-controls">
            <button @click="createNewFunction" class="primary-button">
              <i class="fas fa-plus"></i> Новая функция
            </button>
            <button @click="loadFromFile" class="secondary-button">
              <i class="fas fa-upload"></i> Загрузить
            </button>
            <button @click="saveToFile" class="secondary-button" :disabled="!points.length">
              <i class="fas fa-download"></i> Сохранить
            </button>
          </div>

          <!-- Масштаб оси X -->
          <div class="scale-controls">
            <label>
              Масштаб оси X:
              <input type="range" v-model.number="xAxisScale" min="0.1" max="10" step="0.1" />
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
              <div v-if="isFunctionInsertable" class="insert-control">
                <input type="number" v-model="newPoint.x" step="any" placeholder="x" />
                <input type="number" v-model="newPoint.y" step="any" placeholder="y" />
                <button @click="insertPoint" class="insert-btn">
                  <i class="fas fa-plus"></i> Вставить точку
                </button>
              </div>
            </div>

            <table v-if="visiblePoints.length > 0">
              <thead>
                <tr>
                  <th>#</th>
                  <th>x</th>
                  <th>y</th>
                  <th v-if="isFunctionRemovable">Действия</th>
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
                  <td v-if="isFunctionRemovable">
                    <button @click="removePoint(index + visibleRange.start)" class="remove-btn">
                      <i class="fas fa-trash"></i> Удалить
                    </button>
                  </td>
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
  functionId: { type: Number, default: null },
  functionName: { type: String, default: '' },
  initialPoints: { type: Array, default: () => [] },
  insertable: { type: Boolean, default: false },
  removable: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'function-created', 'function-updated', 'create-new-function'])

const points = ref([...props.initialPoints])
const isLoading = ref(false)
const newPoint = ref({ x: 0, y: 0 })
const applyX = ref(0)
const applyResult = ref(null)
const isFunctionInsertable = computed(() => props.insertable)
const isFunctionRemovable = computed(() => props.removable)
const highlightedIndex = ref(-1)
const visibleRange = ref({ start: 0, end: 0 })
const currentPage = ref(1)
const maxVisiblePoints = ref(50)
const xAxisScale = ref(1.0)

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
  if (Math.abs(num) < 0.001 || Math.abs(num) > 1000) return num.toExponential(2)
  return num.toFixed(2)
}

const totalPages = computed(() => Math.ceil(points.value.length / maxVisiblePoints.value))

const visiblePoints = computed(() => {
  if (!points.value.length) return []
  if (sliderEnd.value > sliderStart.value) return points.value.slice(sliderStart.value, sliderEnd.value + 1)
  if (points.value.length > maxVisiblePoints.value) {
    const start = (currentPage.value - 1) * maxVisiblePoints.value
    const end = Math.min(start + maxVisiblePoints.value, points.value.length)
    return points.value.slice(start, end)
  }
  return points.value
})

onMounted(async () => {
  if (props.functionId) {
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
  }
})

watch(() => props.initialPoints, (newPoints) => {
  points.value = [...newPoints].sort((a, b) => a.x - b.x)
})

const close = () => emit('close')
const createNewFunction = () => emit('create-new-function')

const loadFromFile = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json'
  input.onchange = e => {
    const file = e.target.files[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = event => {
      try {
        const data = JSON.parse(event.target.result)
        if (data.points && Array.isArray(data.points)) {
          points.value = data.points.map(p => ({ x: parseFloat(p.x), y: parseFloat(p.y) })).sort((a,b)=>a.x-b.x)
          applyResult.value = null
          currentPage.value = 1
        } else throw new Error('Неверный формат файла')
      } catch (err) {
        console.error(err)
        alert('Ошибка при загрузке файла. Проверьте формат данных.')
      }
    }
    reader.readAsText(file)
  }
  input.click()
}

const saveToFile = () => {
  if (!points.value.length) return
  const data = { functionName: props.functionName || 'Безымянная функция', points: points.value, createdAt: new Date().toISOString() }
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${props.functionName || 'function'}_${Date.now()}.json`
  document.body.appendChild(a)
  a.click()
  setTimeout(() => { document.body.removeChild(a); URL.revokeObjectURL(url) }, 0)
}

const insertPoint = () => {
  let x = parseFloat(newPoint.value.x.toString().trim().replace(',', '.'))
  let y = parseFloat(newPoint.value.y.toString().trim().replace(',', '.'))
  if (isNaN(x) || isNaN(y)) { alert('Введите корректные числовые значения для x и y'); return }
  const xNorm = Math.round(x * 1e10) / 1e10
  let found = false
  for (let i = 0; i < points.value.length; i++) {
    if (Math.round(points.value[i].x * 1e10) / 1e10 === xNorm) { points.value[i].y = y; found = true; break }
  }
  if (!found) points.value.push({ x, y })
  points.value.sort((a, b) => a.x - b.x)
  newPoint.value = { x: '', y: '' }
  applyResult.value = null
  currentPage.value = 1
}

const removePoint = (index) => {
  if (confirm('Вы уверены, что хотите удалить эту точку?')) {
    points.value.splice(index, 1)
    applyResult.value = null
    if (points.value.length > 0) {
      const maxPage = Math.ceil(points.value.length / maxVisiblePoints.value)
      if (currentPage.value > maxPage) currentPage.value = maxPage
    }
  }
}

const applyFunction = () => {
  if (!points.value.length) { alert('Нет точек для вычисления'); return }
  const x = parseFloat(applyX.value)
  if (isNaN(x)) { alert('Введите корректное значение x'); return }
  applyResult.value = interpolate(points.value, x)
}

const interpolate = (points, x) => {
  const sorted = [...points].sort((a,b)=>a.x-b.x)
  if (x <= sorted[0].x) return sorted[0].y
  if (x >= sorted[sorted.length-1].x) return sorted[sorted.length-1].y
  for (let i = 0; i < sorted.length-1; i++) {
    if (x >= sorted[i].x && x <= sorted[i+1].x) {
      const {x:x0, y:y0} = sorted[i], {x:x1, y:y1} = sorted[i+1]
      return y0 + (y1-y0)*(x-x0)/(x1-x0)
    }
  }
  return null
}

const handlePointSelected = (pointData) => {
  highlightedIndex.value = pointData.index + (sliderEnd.value>sliderStart.value ? sliderStart.value : (currentPage.value-1)*maxVisiblePoints.value)
  applyX.value = pointData.x
  setTimeout(() => { highlightedIndex.value = -1 }, 2000)
}

const handleRangeChanged = (range) => {
  visibleRange.value = range
  currentPage.value = 1
}

const decreasePage = () => { if(currentPage.value>1) currentPage.value-- }
const increasePage = () => { if(currentPage.value<totalPages.value) currentPage.value++ }
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(5px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.explorer-modal {
  background: #230942;
  border-radius: 20px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.6);
  border: 2px solid rgba(255, 255, 255, 0.2);
  width: 95vw;
  height: 95vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2rem;
  background: linear-gradient(135deg, #2f105c 0%, #5b1fa8 100%);
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
}

.modal-header h2 {
  color: #ffffff;
  margin: 0;
  font-size: 2.2rem;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  color: #ffffff;
  font-size: 3rem;
  cursor: pointer;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.modal-body {
  padding: 2rem;
  overflow-y: auto;
  background: #230942;
  flex: 1;
  font-size: 1.1rem;
}

.file-controls {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 30px;
}

.primary-button, .secondary-button {
  padding: 16px 24px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.2rem;
  min-height: 60px;
}

.primary-button {
  background: linear-gradient(135deg, #ff4fc4 0%, #9b59b6 100%);
  color: #ffffff;
}

.primary-button:hover {
  background: linear-gradient(135deg, #ff6fda 0%, #a96bc1 100%);
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(255, 79, 196, 0.4);
}

.secondary-button {
  background: #2f105c;
  color: #ffffff;
  border: 2px solid #5b1fa8;
}

.secondary-button:hover:not(:disabled) {
  background: #5b1fa8;
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(91, 31, 168, 0.4);
}

.scale-controls {
  margin-bottom: 30px;
  padding: 20px;
  background: rgba(47, 16, 92, 0.6);
  border-radius: 12px;
  border: 2px solid #5b1fa8;
}

.scale-controls label {
  display: flex;
  align-items: center;
  gap: 15px;
  color: #ffffff;
  font-weight: 600;
  font-size: 1.3rem;
}

.scale-controls input[type="range"] {
  flex: 1;
  height: 12px;
  border-radius: 6px;
  background: #2f105c;
  outline: none;
  -webkit-appearance: none;
}

.scale-controls input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #ff4fc4;
  cursor: pointer;
  border: 3px solid #ffffff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.scale-value {
  font-weight: bold;
  color: #ff4fc4;
  min-width: 60px;
  font-size: 1.4rem;
}

.chart-section {
  margin-bottom: 30px;
  background: rgba(47, 16, 92, 0.4);
  border-radius: 12px;
  padding: 20px;
  border: 2px solid #5b1fa8;
  min-height: 300px;
}

.points-table {
  margin-bottom: 30px;
  background: rgba(47, 16, 92, 0.4);
  border-radius: 12px;
  padding: 20px;
  border: 2px solid #5b1fa8;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.table-header h3 {
  color: #ffffff;
  margin: 0;
  font-size: 1.6rem;
  font-weight: 600;
}

.insert-control {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.large-input {
  padding: 12px 16px;
  border: 2px solid #5b1fa8;
  border-radius: 8px;
  background: #2f105c;
  color: #ffffff;
  width: 120px;
  font-size: 1.2rem;
}

.large-input::placeholder {
  color: #cccccc;
  font-size: 1.1rem;
}

.large-button {
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1.2rem;
  font-weight: 600;
  transition: all 0.2s;
}

.insert-btn {
  background: #ff4fc4;
  color: #ffffff;
  display: flex;
  align-items: center;
  gap: 8px;
}

.insert-btn:hover {
  background: #ff6fda;
  transform: translateY(-2px);
}

.large-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
  font-size: 1.2rem;
}

.large-table th, .large-table td {
  padding: 16px 20px;
  text-align: left;
  border-bottom: 2px solid #5b1fa8;
}

.large-table th {
  background: #2f105c;
  color: #ffffff;
  font-weight: 700;
  font-size: 1.3rem;
}

.large-cell {
  background: rgba(35, 9, 66, 0.7);
  color: #ffffff;
  font-size: 1.2rem;
}

.highlighted-row {
  background: rgba(255, 79, 196, 0.3) !important;
  animation: highlight 2s ease;
}

@keyframes highlight {
  0% { background: rgba(255, 79, 196, 0.5); }
  100% { background: rgba(255, 79, 196, 0.3); }
}

.remove-btn {
  background: #e74c3c;
  color: #ffffff;
  display: flex;
  align-items: center;
  gap: 8px;
}

.remove-btn:hover {
  background: #c0392b;
  transform: translateY(-2px);
}

.empty-table {
  text-align: center;
  color: #cccccc;
  font-style: italic;
  padding: 40px;
  font-size: 1.4rem;
}

.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
  padding: 15px;
  background: rgba(47, 16, 92, 0.6);
  border-radius: 8px;
}

.pagination-controls button {
  padding: 12px 20px;
  background: #5b1fa8;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.2rem;
  font-weight: 600;
}

.pagination-controls button:hover:not(:disabled) {
  background: #ff4fc4;
  transform: translateY(-2px);
}

.pagination-controls button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.page-info {
  color: #ffffff;
  font-weight: 600;
  font-size: 1.3rem;
}

.slider-container {
  width: 100%;
  margin-bottom: 15px;
}

.slider-container h4 {
  color: #ffffff;
  margin-bottom: 12px;
  font-size: 1.4rem;
}

.large-slider {
  width: 100%;
  margin: 8px 0;
  height: 12px;
}

.large-slider::-webkit-slider-thumb {
  width: 28px;
  height: 28px;
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  color: #cccccc;
  font-size: 1.2rem;
  margin-top: 8px;
}

.apply-section {
  background: rgba(47, 16, 92, 0.4);
  border-radius: 12px;
  padding: 20px;
  border: 2px solid #5b1fa8;
}

.apply-section h3 {
  color: #ffffff;
  margin-bottom: 20px;
  font-size: 1.6rem;
  font-weight: 600;
}

.apply-input {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.large-label {
  color: #ffffff;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.3rem;
  font-weight: 600;
}

.apply-input button {
  padding: 12px 24px;
  background: #ff4fc4;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1.2rem;
  font-weight: 600;
}

.apply-input button:hover:not(:disabled) {
  background: #ff6fda;
  transform: translateY(-2px);
}

.apply-input button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.apply-result {
  padding: 15px;
  background: rgba(255, 79, 196, 0.2);
  border-radius: 8px;
  border: 2px solid #ff4fc4;
}

.large-result {
  color: #ffffff;
  margin: 0;
  font-weight: 600;
  font-size: 1.4rem;
}

.loading {
  text-align: center;
  padding: 60px;
  color: #ffffff;
  font-size: 1.4rem;
}

/* Увеличиваем размер иконок */
.fas {
  font-size: 1.3em;
}

@media (max-width: 768px) {
  .explorer-modal {
    width: 98vw;
    height: 98vh;
  }

  .modal-body {
    padding: 1.5rem;
  }

  .modal-header h2 {
    font-size: 1.8rem;
  }

  .file-controls {
    flex-direction: column;
  }

  .table-header {
    flex-direction: column;
    align-items: stretch;
  }

  .insert-control {
    justify-content: center;
  }

  .pagination-controls {
    flex-direction: column;
  }

  .large-table {
    font-size: 1.1rem;
  }

  .large-table th, .large-table td {
    padding: 12px 15px;
  }
}
</style>