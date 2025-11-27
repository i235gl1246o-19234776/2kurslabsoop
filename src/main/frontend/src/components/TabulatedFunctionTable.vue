<template>
  <div class="table-container">
    <div class="table-controls">
      <div class="search-container">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Поиск по X или Y..."
          class="search-input"
        />
        <button @click="clearSearch" class="clear-search-btn" v-if="searchQuery">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <div class="actions-container">
        <button @click="exportToCSV" class="export-btn">
          <i class="fas fa-download"></i> Экспорт в CSV
        </button>
        <button @click="exportToJSON" class="export-btn">
          <i class="fas fa-file-export"></i> Экспорт в JSON
        </button>
        <button @click="toggleEditMode" class="toggle-edit-btn">
          <i class="fas" :class="isEditMode ? 'fa-lock' : 'fa-edit'"></i>
          {{ isEditMode ? 'Завершить редактирование' : 'Редактировать' }}
        </button>
      </div>
    </div>

    <div class="table-wrapper" ref="tableWrapper">
      <table class="function-table" ref="functionTable">
        <thead>
          <tr>
            <th
              v-for="(column, index) in columns"
              :key="index"
              @click="sortColumn(column.key)"
              class="sortable-header"
            >
              {{ column.label }}
              <span v-if="sortKey === column.key" class="sort-icon">
                {{ sortDirection === 'asc' ? '↑' : '↓' }}
              </span>
            </th>
            <th v-if="isEditMode && isRemovable" class="action-header">Действия</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(point, index) in paginatedPoints" :key="index" class="point-row">
            <td class="x-column">{{ formatNumber(point.x) }}</td>
            <td class="y-column">
              <span v-if="!isEditMode">{{ formatNumber(point.y) }}</span>
              <input
                v-else
                type="number"
                v-model.number="editedPoints[point.x]"
                class="y-input"
                @blur="saveYValue(point.x)"
                @keydown.enter="saveYValue(point.x)"
                step="any"
              />
            </td>
            <td v-if="isEditMode && isRemovable" class="action-column">
              <button @click="removePoint(point.x)" class="remove-btn">
                <i class="fas fa-trash"></i>
              </button>
            </td>
          </tr>
          <tr v-if="filteredPoints.length === 0">
            <td colspan="3" class="empty-row">
              {{ searchQuery ? 'По вашему запросу ничего не найдено' : 'Нет точек для отображения' }}
            </td>
          </tr>
        </tbody>
      </table>
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

    <div v-if="showInsertRow && isInsertable" class="insert-row">
      <h4>Вставить новую точку</h4>
      <div class="insert-controls">
        <input
          v-model.number="newPoint.x"
          type="number"
          placeholder="X значение"
          class="insert-input"
          step="any"
        />
        <input
          v-model.number="newPoint.y"
          type="number"
          placeholder="Y значение"
          class="insert-input"
          step="any"
        />
        <button @click="insertPoint" class="insert-btn">
          <i class="fas fa-plus"></i> Вставить точку
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { formatNumber } from '../utils/mathFunctions.js';

const props = defineProps({
  points: {
    type: Array,
    required: true,
    default: () => []
  },
  isInsertable: {
    type: Boolean,
    default: false
  },
  isRemovable: {
    type: Boolean,
    default: false
  },
  showInsertRow: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['point-inserted', 'point-removed', 'y-value-updated']);

// Состояние таблицы
const searchQuery = ref('');
const sortKey = ref('x');
const sortDirection = ref('asc');
const currentPage = ref(1);
const itemsPerPage = ref(20);
const isEditMode = ref(false);
const editedPoints = ref({});
const newPoint = ref({ x: 0, y: 0 });

// Вычисляемые свойства
const columns = computed(() => [
  { key: 'x', label: 'X' },
  { key: 'y', label: 'Y' }
]);

const filteredPoints = computed(() => {
  if (!searchQuery.value) return sortedPoints.value;

  const query = searchQuery.value.toLowerCase();
  return sortedPoints.value.filter(point =>
    point.x.toString().includes(query) ||
    point.y.toString().includes(query)
  );
});

const sortedPoints = computed(() => {
  const sorted = [...props.points].sort((a, b) => {
    if (a[sortKey.value] < b[sortKey.value]) return sortDirection.value === 'asc' ? -1 : 1;
    if (a[sortKey.value] > b[sortKey.value]) return sortDirection.value === 'asc' ? 1 : -1;
    return 0;
  });
  return sorted;
});

const totalPages = computed(() =>
  Math.ceil(filteredPoints.value.length / itemsPerPage.value)
);

const paginatedPoints = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredPoints.value.slice(start, end);
});

// Методы
const sortColumn = (key) => {
  if (sortKey.value === key) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortKey.value = key;
    sortDirection.value = 'asc';
  }
};

const clearSearch = () => {
  searchQuery.value = '';
};

const toggleEditMode = () => {
  isEditMode.value = !isEditMode.value;
  if (!isEditMode.value) {
    // Сохраняем все изменения при выходе из режима редактирования
    Object.keys(editedPoints.value).forEach(x => {
      const xNum = parseFloat(x);
      const point = props.points.find(p => p.x === xNum);
      if (point && editedPoints.value[x] !== point.y) {
        emit('y-value-updated', { x: xNum, y: editedPoints.value[x] });
      }
    });
    editedPoints.value = {};
  }
};

const saveYValue = (x) => {
  const xNum = parseFloat(x);
  const point = props.points.find(p => p.x === xNum);
  if (point && editedPoints.value[x] !== point.y) {
    emit('y-value-updated', { x: xNum, y: editedPoints.value[x] });
  }
};

const insertPoint = () => {
  if (isNaN(newPoint.value.x) || isNaN(newPoint.value.y)) {
    alert('Пожалуйста, введите корректные числовые значения для X и Y');
    return;
  }

  emit('point-inserted', { ...newPoint.value });
  newPoint.value = { x: 0, y: 0 };
};

const removePoint = (x) => {
  if (confirm('Вы уверены, что хотите удалить эту точку?')) {
    emit('point-removed', x);
  }
};

const exportToCSV = () => {
  if (props.points.length === 0) return;

  const header = 'X,Y\n';
  const rows = props.points.map(p => `${p.x},${p.y}\n`).join('');
  const csvContent = header + rows;

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.setAttribute('href', url);
  link.setAttribute('download', 'function_points.csv');
  link.style.visibility = 'hidden';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

const exportToJSON = () => {
  if (props.points.length === 0) return;

  const jsonData = {
    points: props.points.map(p => ({ x: p.x, y: p.y })),
    timestamp: new Date().toISOString()
  };

  const jsonContent = JSON.stringify(jsonData, null, 2);
  const blob = new Blob([jsonContent], { type: 'application/json;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.setAttribute('href', url);
  link.setAttribute('download', 'function_points.json');
  link.style.visibility = 'hidden';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

const scrollToPoint = (xValue) => {
  if (!props.points.length) return;

  // Находим индекс точки
  const index = props.points.findIndex(p => p.x === xValue);
  if (index === -1) return;

  // Вычисляем страницу
  const page = Math.floor(index / itemsPerPage.value) + 1;
  currentPage.value = page;

  // Прокручиваем к таблице
  setTimeout(() => {
    const tableWrapper = document.querySelector('.table-wrapper');
    if (tableWrapper) {
      tableWrapper.scrollIntoView({ behavior: 'smooth' });
    }
  }, 100);
};

// Наблюдатели
watch(() => props.points, (newPoints) => {
  // Сбрасываем состояние редактирования при изменении точек
  editedPoints.value = {};
  currentPage.value = 1;
});

watch(currentPage, () => {
  if (props.points.length === 0) return;

  // Прокручиваем к верху таблицы при изменении страницы
  setTimeout(() => {
    const tableWrapper = document.querySelector('.table-wrapper');
    if (tableWrapper) {
      tableWrapper.scrollTop = 0;
    }
  }, 50);
});

// Методы для родительского компонента
defineExpose({
  scrollToPoint
});

// Инициализация
onMounted(() => {
  // Загружаем сохраненное состояние таблицы из localStorage
  const savedPerPage = localStorage.getItem('tabulatedFunctionItemsPerPage');
  if (savedPerPage) {
    itemsPerPage.value = parseInt(savedPerPage);
  }
});

// Сохраняем настройки в localStorage
watch(itemsPerPage, (newVal) => {
  localStorage.setItem('tabulatedFunctionItemsPerPage', newVal.toString());
});
</script>

<style scoped>
.table-container {
  width: 100%;
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-controls {
  display: flex;
  justify-content: space-between;
  padding: 15px;
  border-bottom: 1px solid var(--border-color);
  flex-wrap: wrap;
  gap: 10px;
}

.search-container {
  position: relative;
  flex: 1;
  max-width: 300px;
}

.search-input {
  width: 100%;
  padding: 8px 30px 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
}

.clear-search-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
}

.clear-search-btn:hover {
  color: var(--text-primary);
}

.actions-container {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.export-btn, .toggle-edit-btn {
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  transition: all 0.2s;
}

.export-btn {
  background-color: #3498db;
  color: white;
}

.export-btn:hover {
  background-color: #2980b9;
}

.toggle-edit-btn {
  background-color: #2ecc71;
  color: white;
}

.toggle-edit-btn:hover {
  background-color: #27ae60;
}

.table-wrapper {
  overflow-x: auto;
  max-width: 100%;
  min-height: 300px;
}

.function-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 400px;
}

.function-table th, .function-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}

.function-table th {
  background-color: var(--bg-tertiary);
  font-weight: 600;
  color: var(--text-secondary);
  position: sticky;
  top: 0;
}

.function-table tr:hover {
  background-color: var(--bg-secondary);
}

.sortable-header {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.sort-icon {
  font-size: 12px;
  color: var(--button-primary);
}

.x-column {
  font-weight: 500;
  color: #2980b9;
}

.y-column {
  color: #27ae60;
}

.y-input {
  width: 100%;
  padding: 6px 10px;
  border: 1px solid #3498db;
  border-radius: 4px;
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
}

.y-input:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.3);
}

.action-column {
  width: 60px;
}

.remove-btn {
  background: none;
  border: none;
  color: #e74c3c;
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  transition: color 0.2s;
}

.remove-btn:hover {
  color: #c0392b;
}

.empty-row {
  text-align: center;
  padding: 20px;
  color: var(--text-secondary);
  font-style: italic;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
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
}

.insert-row {
  padding: 15px;
  border-top: 1px solid var(--border-color);
  background-color: var(--bg-tertiary);
}

.insert-row h4 {
  margin-bottom: 10px;
  color: var(--text-primary);
}

.insert-controls {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.insert-input {
  flex: 1;
  min-width: 120px;
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
}

.insert-btn {
  padding: 8px 15px;
  background-color: #2ecc71;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.2s;
}

.insert-btn:hover {
  background-color: #27ae60;
}

@media (max-width: 768px) {
  .table-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .search-container, .actions-container {
    width: 100%;
  }

  .actions-container {
    flex-wrap: wrap;
  }

  .export-btn, .toggle-edit-btn {
    flex: 1;
    min-width: 120px;
    justify-content: center;
  }

  .pagination {
    flex-direction: column;
    align-items: stretch;
  }

  .page-info {
    justify-content: center;
  }

  .function-table {
    font-size: 14px;
  }

  .function-table th, .function-table td {
    padding: 8px 10px;
  }
}
</style>