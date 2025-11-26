<!-- src/components/FunctionChart.vue -->
<template>
  <div class="chart-container">
    <canvas ref="chartCanvas"></canvas>
    <div v-if="showSlider" class="slider-controls">
      <button @click="zoomIn">+</button>
      <button @click="zoomOut">-</button>
      <button @click="resetZoom">⟲</button>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, watch } from 'vue';
import {
  Chart,
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Tooltip,
  Legend,
} from 'chart.js';
import zoomPlugin from 'chartjs-plugin-zoom'; // ← Убедитесь, что установлен!
// Регистрация компонентов
Chart.register(
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Tooltip,
  Legend,
  zoomPlugin
);
const props = defineProps({
  points: {
    type: Array,
    required: true
  },
  showSlider: {
    type: Boolean,
    default: false
  },
  chartTitle: {
    type: String,
    default: ''
  },
  xAxisScale: { // ← НОВЫЙ ПРОПС
    type: Number,
    default: 1.0
  }
})
const emit = defineEmits(['point-selected', 'range-changed']);
const chartCanvas = ref(null);
let myChart = null;
const createChart = () => {
  if (!chartCanvas.value || !props.points.length) return;
  const ctx = chartCanvas.value.getContext('2d');
  if (myChart) {
    myChart.destroy();
  }
  myChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: props.points.map(p => p.x.toFixed(3)),
      datasets: [{
        label: 'Табулированная функция',
        data: props.points.map(p => p.y),
        borderColor: '#4CAF50',
        backgroundColor: 'rgba(76, 175, 80, 0.1)',
        pointBackgroundColor: '#E91E63',
        pointBorderColor: '#FFFFFF',
        pointRadius: 5,
        pointHoverRadius: 8,
        tension: 0.1
      }]
    },
    options: {
      responsive: true,
      plugins: {
        legend: {
          display: true,
          position: 'top'
        },
        tooltip: {
          mode: 'index',
          intersect: false,
          callbacks: {
            title: function(context) {
              return `x = ${context[0].label}`;
            },
            label: function(context) {
              return `y = ${context.raw.toFixed(3)}`;
            }
          }
        },
        zoom: {
          zoom: {
            wheel: {
              enabled: true,
            },
            pinch: {
              enabled: true
            },
            mode: 'xy',
            onZoom: ({ chart }) => {
              // При зуме — можно отправлять событие с диапазоном
              const minIndex = Math.floor(chart.scales.x.min / chart.scales.x.max * props.points.length);
              const maxIndex = Math.ceil(chart.scales.x.max / chart.scales.x.max * props.points.length);
              emit('range-changed', { start: minIndex, end: maxIndex });
            }
          },
          pan: {
            enabled: true,
            mode: 'xy',
            onPan: ({ chart }) => {
              const minIndex = Math.floor(chart.scales.x.min / chart.scales.x.max * props.points.length);
              const maxIndex = Math.ceil(chart.scales.x.max / chart.scales.x.max * props.points.length);
              emit('range-changed', { start: minIndex, end: maxIndex });
            }
          }
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: 'x'
          },
          min: Math.min(...props.points.map(p => p.x)),
          max: Math.max(...props.points.map(p => p.x))
        },
        y: {
          title: {
            display: true,
            text: 'y'
          },
          beginAtZero: false
        }
      },
      onClick: (event, elements) => {
        if (elements.length > 0) {
          const index = elements[0].index;
          const point = props.points[index];
          emit('point-selected', { index, x: point.x, y: point.y });
        }
      }
    }
  });
  // Сброс зума при изменении данных
  myChart.resetZoom();
};
const zoomIn = () => {
  if (myChart) {
    myChart.zoom(1.1); // увеличиваем на 10%
  }
};
const zoomOut = () => {
  if (myChart) {
    myChart.zoom(0.9); // уменьшаем на 10%
  }
};
const resetZoom = () => {
  if (myChart) {
    myChart.resetZoom();
  }
};
onMounted(createChart);
watch(() => props.points, createChart, { deep: true });
// Эмит события при выборе точки (если нужно)
</script>
<style scoped>
.chart-container {
  position: relative;
}
.slider-controls {
  display: flex;
  gap: 5px;
  margin-top: 10px;
  justify-content: center;
}
.slider-controls button {
  padding: 5px 10px;
  background: #f0f0f0;
  border: 1px solid #ddd;
  cursor: pointer;
  font-size: 1.2rem;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.slider-controls button:hover {
  background: #e0e0e0;
}
</style>