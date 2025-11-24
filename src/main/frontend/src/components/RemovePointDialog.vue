<!-- src/components/RemovePointDialog.vue -->
<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <div class="modal-header">
        <h3>Удалить точку</h3>
        <button class="close-btn" @click="close">&times;</button>
      </div>

      <div class="dialog-content">
        <p>Выберите точку для удаления из функции</p>

        <div class="point-selector">
          <label for="pointSelect">Выберите точку:</label>
          <select
            id="pointSelect"
            v-model="selectedPointIndex"
            class="point-select"
          >
            <option value="-1" disabled>-- Выберите точку --</option>
            <option
              v-for="(point, index) in points"
              :key="index"
              :value="index"
            >
              Точка #{{ index + 1 }} (X: {{ point.x }}, Y: {{ point.y }})
            </option>
          </select>
        </div>

        <div v-if="selectedPointIndex >= 0" class="preview-section">
          <h4>Предпросмотр удаления:</h4>
          <div class="point-details">
            <p><strong>Координаты точки:</strong> X = {{ points[selectedPointIndex].x }}, Y = {{ points[selectedPointIndex].y }}</p>
            <p class="warning"><i class="fas fa-exclamation-triangle"></i> После удаления точка будет безвозвратно удалена из функции</p>
          </div>

          <div class="chart-preview">
            <canvas ref="previewChart"></canvas>
          </div>
        </div>

        <div v-if="removeError" class="error-message dialog-error">
          {{ removeError }}
        </div>
      </div>

      <div class="dialog-footer">
        <button class="cancel-btn" @click="close">Отмена</button>
        <button class="confirm-btn" @click="removePoint" :disabled="selectedPointIndex < 0 || isRemoving">
          <span v-if="isRemoving">
            <i class="fas fa-spinner fa-spin"></i> Удаление...
          </span>
          <span v-else>
            Удалить точку
          </span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

const props = defineProps({
  functionId: Number,
  points: Array
});

const emit = defineEmits(['remove', 'close']);

// Состояние
const selectedPointIndex = ref(-1);
const removeError = ref('');
const isRemoving = ref(false);
const previewChart = ref(null);
let chartInstance = null;

// Предпросмотр графика с выделенной точкой
const renderPreviewChart = () => {
  if (!previewChart.value || selectedPointIndex.value < 0 || selectedPointIndex.value >= props.points.length) {
    if (chartInstance) {
      chartInstance.destroy();
      chartInstance = null;
    }
    return;
  }

  const ctx = previewChart.value.getContext('2d');

  if (chartInstance) {
    chartInstance.destroy();
  }

  // Сортируем точки по X
  const sortedPoints = [...props.points].sort((a, b) => a.x - b.x);
  const xValues = sortedPoints.map(p => p.x);
  const yValues = sortedPoints.map(p => p.y);

  // Создаем цвета для точек
  const pointColors = sortedPoints.map((_, index) =>
    index === selectedPointIndex.value ? '#e74c3c' : '#3498db'
  );

  chartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels: xValues,
      datasets: [{
        label: 'Функция',
        data: sortedPoints.map((point, index) => ({ x: xValues[index], y: yValues[index] })),
        borderColor: '#3498db',
        backgroundColor: 'rgba(52, 152, 219, 0.1)',
        borderWidth: 2,
        pointRadius: sortedPoints.map((_, index) => index === selectedPointIndex.value ? 6 : 4),
        pointBackgroundColor: pointColors,
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        fill: false,
        tension: 0.3
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
        legend: {
          display: false
        },
        tooltip: {
          mode: 'index',
          intersect: false
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

// Удаление точки
const removePoint = () => {
  if (selectedPointIndex.value < 0) {
    removeError.value = 'Пожалуйста, выберите точку для удаления';
    return;
  }

  removeError.value = '';

  try {
    const pointToRemove = props.points[selectedPointIndex.value];
    emit('remove', pointToRemove.x);
    close();
  } catch (error) {
    removeError.value = `Ошибка при удалении точки: ${error.message}`;
  }
};

// Закрытие диалога
const close = () => {
  emit('close');
};

// Отслеживание изменений в выбранной точке
watch(selectedPointIndex, () => {
  renderPreviewChart();
});

// Очистка при размонтировании
onUnmounted(() => {
  if (chartInstance) {
    chartInstance.destroy();
    chartInstance = null;
  }
});
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 600px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #2c3e50;
  color: white;
  border-bottom: 2px solid #e74c3c;
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
  background-color: rgba(255, 255, 255, 0.2);
}

.dialog-content {
  padding: 20px;
}

.point-selector {
  margin-bottom: 20px;
}

.point-selector label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #333;
}

.point-select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  background-color: white;
}

.preview-section {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.point-details {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 15px;
}

.warning {
  color: #e74c3c;
  margin-top: 8px;
  font-style: italic;
}

.chart-preview {
  height: 200px;
  position: relative;
}

.error-message {
  color: #e74c3c;
  font-size: 0.85rem;
  margin-top: 5px;
  display: block;
}

.dialog-error {
  margin-top: 15px;
  padding: 10px;
  background-color: #ffebee;
  border-radius: 4px;
  border-left: 3px solid #e74c3c;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 15px 20px;
  border-top: 1px solid #eee;
  background-color: #f8f9fa;
}

.cancel-btn, .confirm-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}

.cancel-btn {
  background-color: #6c757d;
  color: white;
}

.cancel-btn:hover {
  background-color: #5a6268;
}

.confirm-btn {
  background-color: #e74c3c;
  color: white;
}

.confirm-btn:hover {
  background-color: #c0392b;
}

.confirm-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}
</style>