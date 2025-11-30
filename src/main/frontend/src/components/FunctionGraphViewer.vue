<!-- src/components/FunctionGraphViewer.vue -->
<template>
  <div class="graph-container">
    <Line
      :chart-options="chartOptions"
      :chart-data="chartData"
      :height="400"
    />
  </div>
</template>

<script setup>
import { Line } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, LineElement, LinearScale, CategoryScale, PointElement } from 'chart.js'
import { computed } from 'vue'

ChartJS.register(Title, Tooltip, Legend, LineElement, LinearScale, CategoryScale, PointElement)

const props = defineProps({
  points: {
    type: Array,
    default: () => []
  }
})

const chartData = computed(() => {
  // Всегда возвращаем полную структуру с labels и datasets
  if (!props.points.length) {
    return {
      labels: [],
      datasets: []
    }
  }

  // Сортируем точки по X для корректного отображения линии
  const sortedPoints = [...props.points].sort((a, b) => a.x - b.x)

  return {
    labels: sortedPoints.map(p => p.x.toFixed(2)),
    datasets: [
      {
        label: 'Функция',
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        borderColor: '#42b983',
        borderWidth: 2,
        pointBackgroundColor: '#42b983',
        pointBorderColor: '#fff',
        pointRadius: 4,
        pointHoverRadius: 6,
        data: sortedPoints.map(p => ({ x: p.x, y: p.y })),
        fill: false,
        tension: 0.1
      }
    ]
  }
})

const chartOptions = computed(() => {
  return {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        title: {
          display: true,
          text: 'X'
        },
        grid: {
          color: 'rgba(0, 0, 0, 0.1)'
        }
      },
      y: {
        title: {
          display: true,
          text: 'Y'
        },
        grid: {
          color: 'rgba(0, 0, 0, 0.1)'
        },
        type: 'linear'
      }
    },
    plugins: {
      legend: {
        position: 'top',
      },
      title: {
        display: true,
        text: 'График функции'
      },
      tooltip: {
        callbacks: {
          label: function(context) {
            return `f(${context.parsed.x.toFixed(3)}) = ${context.parsed.y.toFixed(3)}`
          }
        }
      }
    },
    interaction: {
      intersect: false,
      mode: 'index'
    }
  }
})
</script>

<style scoped>
.graph-container {
  width: 100%;
  height: 400px;
  margin: 20px 0;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 15px;
}
</style>