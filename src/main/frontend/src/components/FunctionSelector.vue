<template>
  <div class="function-selector">
    <div class="selector-header">
      <h3>{{ title }}</h3>
      <div class="search-filter">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Поиск функций..."
          class="search-input"
        />
        <select v-model="filterType" class="filter-select">
          <option value="all">Все типы</option>
          <option value="tabular">Табличные</option>
          <option value="analytic">Аналитические</option>
        </select>
      </div>
    </div>

    <div class="functions-container" ref="functionsContainer">
      <div v-if="isLoading" class="loading-state">
        <div class="loader"></div>
        <p>Загрузка функций...</p>
      </div>

      <div v-else-if="filteredFunctions.length === 0" class="empty-state">
        <i class="fas fa-folder-open empty-icon"></i>
        <p>Нет доступных функций</p>
        <button @click="refreshFunctions" class="refresh-btn">
          <i class="fas fa-sync"></i> Обновить
        </button>
      </div>

      <div v-else class="functions-grid">
        <div
          v-for="func in paginatedFunctions"
          :key="func.functionId"
          class="function-card"
          :class="{ selected: selectedFunction?.functionId === func.functionId }"
          @click="selectFunction(func)"
        >
          <div class="function-header">
            <span class="function-name">{{ func.functionName }}</span>
            <span class="function-id">#{{ func.functionId }}</span>
          </div>

          <div class="function-meta">
            <span class="function-type" :class="func.typeFunction">
              {{ getFunctionTypeLabel(func.typeFunction) }}
            </span>
            <span class="point-count">
              <i class="fas fa-chart-line"></i> {{ func.pointsCount || 0 }} точек
            </span>
          </div>

          <div class="function-preview">
            <div v-if="func.functionExpression" class="function-expression">
              {{ truncateExpression(func.functionExpression) }}
            </div>
            <div v-else class="placeholder-expression">
              Нет выражения
            </div>
          </div>

          <div class="function-footer">
            <button
              @click.stop="previewFunction(func)"
              class="preview-btn"
              title="Предпросмотр"
            >
              <i class="fas fa-eye"></i>
            </button>
            <button
              @click.stop="toggleFavorite(func)"
              class="favorite-btn"
              :class="{ favorited: isFavorite(func.functionId) }"
              title="Добавить в избранное"
            >
              <i class="fas" :class="isFavorite(func.functionId) ? 'fa-star' : 'fa-star-o'"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="totalPages > 1" class="pagination">
      <button
        @click="currentPage = Math.max(1, currentPage - 1)"
        :disabled="currentPage === 1"
        class="pagination-btn"
      >
        <i class="fas fa-chevron-left"></i> Предыдущая
      </button>

      <div class="page-info">
        Страница {{ currentPage }} из {{ totalPages }}
        <select v-model.number="currentPage" class="page-select">
          <option v-for="page in totalPages" :key="page" :value="page">
            Страница {{ page }}
          </option>
        </select>
      </div>

      <button
        @click="currentPage = Math.min(totalPages, currentPage + 1)"
        :disabled="currentPage === totalPages"
        class="pagination-btn"
      >
        Следующая <i class="fas fa-chevron-right"></i>
      </button>
    </div>

    <!-- Модальное окно для предпросмотра -->
    <div v-if="showPreview" class="modal-overlay" @click="closePreview">
      <div class="preview-modal" @click.stop>
        <div class="modal-header">
          <h3>Предпросмотр функции: {{ previewFunctionData?.functionName }}</h3>
          <button class="close-btn" @click="closePreview">&times;</button>
        </div>

        <div class="modal-body">
          <div v-if="previewLoading" class="loading-state">
            <div class="loader"></div>
            <p>Загрузка данных функции...</p>
          </div>

          <div v-else-if="previewError" class="error-state">
            <i class="fas fa-exclamation-triangle error-icon"></i>
            <p>{{ previewError }}</p>
          </div>

          <div v-else class="preview-content">
            <div class="function-details">
              <div class="detail-row">
                <span class="detail-label">ID функции:</span>
                <span class="detail-value">{{ previewFunctionData?.functionId }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Тип функции:</span>
                <span class="detail-value">{{ getFunctionTypeLabel(previewFunctionData?.typeFunction) }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Выражение:</span>
                <span class="detail-value">{{ previewFunctionData?.functionExpression || 'Не задано' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Количество точек:</span>
                <span class="detail-value">{{ previewPoints.length }}</span>
              </div>
            </div>

            <div class="chart-container">
              <FunctionChart
                :points="previewPoints"
                :chart-title="previewFunctionData?.functionName"
                :show-slider="true"
              />
            </div>

            <div class="points-summary">
              <h4>Характеристики функции</h4>
              <div class="stats-grid">
                <div class="stat-card">
                  <span class="stat-label">Минимальное X</span>
                  <span class="stat-value">{{ formatNumber(previewStats.minX) }}</span>
                </div>
                <div class="stat-card">
                  <span class="stat-label">Максимальное X</span>
                  <span class="stat-value">{{ formatNumber(previewStats.maxX) }}</span>
                </div>
                <div class="stat-card">
                  <span class="stat-label">Минимальное Y</span>
                  <span class="stat-value">{{ formatNumber(previewStats.minY) }}</span>
                </div>
                <div class="stat-card">
                  <span class="stat-label">Максимальное Y</span>
                  <span class="stat-value">{{ formatNumber(previewStats.maxY) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closePreview" class="cancel-btn">Закрыть</button>
          <button @click="usePreviewedFunction" class="select-btn">
            <i class="fas fa-check"></i> Выбрать эту функцию
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import FunctionChart from './FunctionChart.vue';
import { formatNumber } from '../utils/mathFunctions.js';
import { api } from '../api.js';

const props = defineProps({
  functions: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: 'Выберите функцию'
  },
  userId: {
    type: Number,
    required: true
  }
});

const emit = defineEmits(['function-selected', 'refresh']);

// Состояние
const searchQuery = ref('');
const filterType = ref('all');
const selectedFunction = ref(null);
const currentPage = ref(1);
const itemsPerPage = ref(12);
const showPreview = ref(false);
const previewFunctionData = ref(null);
const previewPoints = ref([]);
const previewLoading = ref(false);
const previewError = ref(null);

// Вычисляемые свойства
const isLoading = computed(() => props.loading || previewLoading.value);
const filteredFunctions = computed(() => {
  return props.functions
    .filter(func => {
      // Фильтр по типу
      if (filterType.value !== 'all' && func.typeFunction !== filterType.value) {
        return false;
      }

      // Фильтр по поиску
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase();
        return (
          func.functionName.toLowerCase().includes(query) ||
          func.functionExpression?.toLowerCase().includes(query) ||
          func.functionId.toString().includes(query)
        );
      }

      return true;
    })
    .sort((a, b) => b.functionId - a.functionId); // Сортируем по ID (новые первыми)
});

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredFunctions.value.length / itemsPerPage.value))
);

const paginatedFunctions = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredFunctions.value.slice(start, end);
});

const favorites = computed(() => {
  const saved = localStorage.getItem('functionSelectorFavorites');
  return saved ? JSON.parse(saved) : [];
});

const previewStats = computed(() => {
  if (previewPoints.value.length === 0) return {
    minX: 0,
    maxX: 0,
    minY: 0,
    maxY: 0
  };

  const xValues = previewPoints.value.map(p => p.x);
  const yValues = previewPoints.value.map(p => p.y);

  return {
    minX: Math.min(...xValues),
    maxX: Math.max(...xValues),
    minY: Math.min(...yValues),
    maxY: Math.max(...yValues)
  };
});

// Методы
const getFunctionTypeLabel = (type) => {
  const labels = {
    'tabular': 'Табличная',
    'analytic': 'Аналитическая',
    'composite': 'Составная'
  };
  return labels[type] || type;
};

const selectFunction = (func) => {
  selectedFunction.value = func;
  emit('function-selected', func);
};

const refreshFunctions = () => {
  emit('refresh');
};

const previewFunction = async (func) => {
  showPreview.value = true;
  previewFunctionData.value = func;
  previewPoints.value = [];
  previewError.value = null;
  previewLoading.value = true;

  try {
    const points = await api.getTabulatedPointsByFunctionId(func.functionId);
    previewPoints.value = points.map(p => ({
      x: parseFloat(p.xval),
      y: parseFloat(p.yval)
    })).sort((a, b) => a.x - b.x);
  } catch (error) {
    previewError.value = `Ошибка загрузки точек: ${error.message}`;
    console.error('Error loading function points:', error);
  } finally {
    previewLoading.value = false;
  }
};

const closePreview = () => {
  showPreview.value = false;
  previewFunctionData.value = null;
  previewPoints.value = [];
  previewError.value = null;
};

const usePreviewedFunction = () => {
  if (previewFunctionData.value) {
    selectFunction(previewFunctionData.value);
  }
  closePreview();
};

const toggleFavorite = (func) => {
  let currentFavorites = [...favorites.value];

  if (isFavorite(func.functionId)) {
    currentFavorites = currentFavorites.filter(id => id !== func.functionId);
  } else {
    currentFavorites.push(func.functionId);
  }

  localStorage.setItem('functionSelectorFavorites', JSON.stringify(currentFavorites));
};

const isFavorite = (functionId) => {
  return favorites.value.includes(functionId);
};

const truncateExpression = (expression, maxLength = 30) => {
  if (!expression || expression.length <= maxLength) return expression;
  return expression.substring(0, maxLength) + '...';
};

// Наблюдатели
watch(() => props.functions, (newFunctions) => {
  if (newFunctions.length > 0) {
    currentPage.value = 1;
  }
});

watch(currentPage, () => {
  // Прокручиваем к верху контейнера при изменении страницы
  setTimeout(() => {
    const container = document.querySelector('.functions-container');
    if (container) {
      container.scrollTop = 0;
    }
  }, 50);
});

// Инициализация
onMounted(() => {
  // Загружаем сохраненные настройки
  const savedPerPage = localStorage.getItem('functionSelectorItemsPerPage');
  if (savedPerPage) {
    itemsPerPage.value = parseInt(savedPerPage);
  }
});

// Сохраняем настройки
watch(itemsPerPage, (newVal) => {
  localStorage.setItem('functionSelectorItemsPerPage', newVal.toString());
});
</script>

<style scoped>
.function-selector {
  width: 100%;
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.selector-header {
  padding: 15px 20px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.search-filter {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  flex: 1;
  max-width: 500px;
}

.search-input {
  flex: 1;
  min-width: 200px;
  padding: 8px 15px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
}

.filter-select {
  padding: 8px 15px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
}

.functions-container {
  min-height: 300px;
  max-height: 600px;
  overflow-y: auto;
  padding: 15px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--text-secondary);
}

.loader {
  width: 30px;
  height: 30px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: var(--button-primary);
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 48px;
  color: var(--border-color);
  margin-bottom: 15px;
}

.refresh-btn {
  margin-top: 15px;
  padding: 8px 15px;
  background-color: var(--button-secondary);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.refresh-btn:hover {
  background-color: var(--button-secondary-hover);
}

.functions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  padding: 5px;
}

.function-card {
  background-color: var(--bg-secondary);
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;
}

.function-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  border-color: var(--button-secondary);
}

.function-card.selected {
  border-color: var(--button-primary);
  box-shadow: 0 0 0 2px var(--button-primary);
}

.function-card.selected::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background-color: var(--button-primary);
}

.function-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.function-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 16px;
  word-break: break-word;
}

.function-id {
  color: var(--text-secondary);
  font-size: 12px;
  background-color: var(--bg-tertiary);
  padding: 2px 6px;
  border-radius: 4px;
}

.function-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.function-type {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.function-type.tabular {
  background-color: #e3f2fd;
  color: #1976d2;
}

.function-type.analytic {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.function-type.composite {
  background-color: #fff8e1;
  color: #5d4037;
}

.point-count {
  color: var(--text-secondary);
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.function-preview {
  margin: 10px 0;
  min-height: 40px;
}

.function-expression {
  font-family: monospace;
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.placeholder-expression {
  font-style: italic;
  color: var(--text-secondary);
  font-size: 13px;
}

.function-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--border-color);
}

.preview-btn, .favorite-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background-color: var(--bg-tertiary);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.preview-btn:hover {
  background-color: #2196f3;
  color: white;
}

.favorite-btn {
  transition: all 0.2s;
}

.favorite-btn:hover, .favorite-btn.favorited {
  background-color: #ff9800;
  color: white;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-top: 1px solid var(--border-color);
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-btn {
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  background-color: var(--button-secondary);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  background-color: var(--button-secondary-hover);
  transform: translateY(-1px);
}

.pagination-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.page-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
}

.page-select {
  padding: 6px 8px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
  min-width: 120px;
}

/* Модальное окно для предпросмотра */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.preview-modal {
  background-color: var(--bg-modal);
  border-radius: 8px;
  width: 90%;
  max-width: 1000px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid var(--border-color);
  background-color: var(--bg-tertiary);
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex-grow: 1;
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--error-text);
}

.error-icon {
  font-size: 48px;
  margin-bottom: 15px;
  color: var(--error-text);
}

.function-details {
  margin-bottom: 20px;
  padding: 15px;
  background-color: var(--bg-secondary);
  border-radius: 8px;
}

.detail-row {
  display: flex;
  margin-bottom: 8px;
}

.detail-label {
  flex: 1;
  font-weight: 500;
  color: var(--text-secondary);
}

.detail-value {
  flex: 2;
  color: var(--text-primary);
}

.chart-container {
  margin: 20px 0;
  height: 350px;
}

.points-summary {
  margin-top: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 15px;
  margin-top: 10px;
}

.stat-card {
  background-color: var(--bg-tertiary);
  padding: 12px;
  border-radius: 6px;
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 5px;
}

.stat-value {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 16px;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.cancel-btn, .select-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.cancel-btn {
  background-color: #e0e0e0;
  color: #333;
}

.cancel-btn:hover {
  background-color: #d5d5d5;
}

.select-btn {
  background-color: var(--button-primary);
  color: white;
  display: flex;
  align-items: center;
  gap: 5px;
}

.select-btn:hover {
  background-color: var(--button-primary-hover);
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-secondary);
  cursor: pointer;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background-color: var(--border-color);
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .selector-header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-filter {
    width: 100%;
    flex-direction: column;
    gap: 8px;
  }

  .functions-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }

  .preview-modal {
    width: 95%;
    margin: 10px;
  }

  .modal-body {
    padding: 15px;
  }

  .chart-container {
    height: 300px;
  }
}

@media (max-width: 480px) {
  .functions-grid {
    grid-template-columns: 1fr;
  }

  .function-card {
    min-width: 100%;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>