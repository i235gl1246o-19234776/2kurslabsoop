<template>
  <div class="function-chart-simple">
    <div class="chart-header">
      <h3>{{ title }}</h3>
    </div>
    <div class="chart-container">
      <canvas ref="chartCanvas"></canvas>
    </div>
    <div class="chart-info">
      <p>Точек на графике: {{ points.length }}</p>
      <p v-if="points.length > 0">
        Диапазон X: [{{ minX.toFixed(2) }}, {{ maxX.toFixed(2) }}]
        Y: [{{ minY.toFixed(2) }}, {{ maxY.toFixed(2) }}]
      </p>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

export default {
  name: 'FunctionChartSimple',
  props: {
    points: {
      type: Array,
      default: () => []
    },
    title: {
      type: String,
      default: 'График функции'
    }
  },
  setup(props) {
    const chartCanvas = ref(null)
    let chartInstance = null

    const minX = computed(() => {
      if (!props.points.length) return 0
      return Math.min(...props.points.map(p => p.x))
    })

    const maxX = computed(() => {
      if (!props.points.length) return 0
      return Math.max(...props.points.map(p => p.x))
    })

    const minY = computed(() => {
      if (!props.points.length) return 0
      return Math.min(...props.points.map(p => p.y))
    })

    const maxY = computed(() => {
      if (!props.points.length) return 0
      return Math.max(...props.points.map(p => p.y))
    })

    const createChart = () => {
      if (!chartCanvas.value) {
        console.log('❌ canvas element not found')
        return
      }

      if (!props.points || props.points.length === 0) {
        console.log('❌ no points data')
        return
      }

      console.log('🔄 Creating chart with points:', props.points)

      // Уничтожаем старый график
      if (chartInstance) {
        chartInstance.destroy()
      }

      const ctx = chartCanvas.value.getContext('2d')

      // Сортируем точки по X
      const sortedPoints = [...props.points].sort((a, b) => a.x - b.x)

      chartInstance = new Chart(ctx, {
        type: 'line',
        data: {
          labels: sortedPoints.map(p => p.x.toFixed(2)),
          datasets: [{
            label: props.title,
            data: sortedPoints.map(point => point.y),
            borderColor: '#3498db',
            backgroundColor: 'rgba(52, 152, 219, 0.1)',
            borderWidth: 2,
            pointRadius: 4,
            pointBackgroundColor: '#3498db',
            fill: true,
            tension: 0.4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            x: {
              title: {
                display: true,
                text: 'X'
              }
            },
            y: {
              title: {
                display: true,
                text: 'Y'
              }
            }
          },
          plugins: {
            title: {
              display: true,
              text: props.title
            },
            legend: {
              display: false
            }
          }
        }
      })

      console.log('✅ Chart created successfully')
    }

    // Отслеживание изменений в точках
    watch(() => props.points, (newPoints) => {
      console.log('📈 Points changed:', newPoints)
      if (newPoints && newPoints.length > 0) {
        createChart()
      }
    }, { deep: true })

    // Инициализация при монтировании
    onMounted(() => {
      console.log('🎯 Chart component mounted')
      createChart()
    })

    // Очистка при размонтировании
    onUnmounted(() => {
      if (chartInstance) {
        chartInstance.destroy()
        chartInstance = null
      }
    })

    return {
      chartCanvas,
      minX,
      maxX,
      minY,
      maxY
    }
  }
}
</script>

<style scoped>
.function-chart-simple {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-header {
  padding: 10px;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
}

.chart-header h3 {
  margin: 0;
  color: #2c3e50;
}

.chart-container {
  flex: 1;
  position: relative;
  min-height: 300px;
}

.chart-info {
  padding: 10px;
  background: #f8f9fa;
  border-top: 1px solid #e0e0e0;
  font-size: 12px;
  color: #666;
}

.chart-info p {
  margin: 2px 0;
}
</style>