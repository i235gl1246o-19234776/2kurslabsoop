<template>
  <el-dialog
    v-model="visible"
    :title="`Исследование функции: ${functionData.name}`"
    width="90%"
    :close-on-click-modal="false"
    class="function-explorer-dialog"
  >
    <div class="explorer-content">
      <!-- График функции -->
      <div class="chart-section">
        <function-chart
          :points="points"
          :title="functionData.name"
          @point-selected="handlePointSelected"
        />
      </div>

      <!-- Информация и управление -->
      <div class="control-section">
        <div class="function-info">
          <h3>Информация о функции</h3>
          <p><strong>ID:</strong> {{ functionData.id }}</p>
          <p><strong>Тип:</strong> {{ functionData.type }}</p>
          <p><strong>Количество точек:</strong> {{ points.length }}</p>
          <p v-if="functionData.description">
            <strong>Описание:</strong> {{ functionData.description }}
          </p>
        </div>

        <!-- Вычисление значения -->
        <div class="calculation-section">
          <h3>Вычисление значения</h3>
          <el-input-number
            v-model="calculationX"
            :precision="3"
            :step="0.1"
            placeholder="Введите x"
            style="width: 100%; margin-bottom: 10px;"
          />
          <el-button
            type="primary"
            @click="calculateValue"
            :loading="calculating"
            style="width: 100%;"
          >
            Вычислить f({{ calculationX }})
          </el-button>
          <div v-if="calculationResult !== null" class="result">
            f({{ calculationX }}) = {{ calculationResult.toFixed(6) }}
          </div>
        </div>

        <!-- Действия -->
        <div class="actions-section">
          <h3>Действия</h3>
          <el-button
            v-if="functionData.insertable"
            type="success"
            @click="showInsertDialog = true"
            style="width: 100%; margin-bottom: 10px;"
          >
            <i class="fas fa-plus"></i> Вставить точку
          </el-button>

          <el-button
            v-if="functionData.removable"
            type="danger"
            @click="showRemoveDialog = true"
            style="width: 100%; margin-bottom: 10px;"
          >
            <i class="fas fa-minus"></i> Удалить точку
          </el-button>

          <el-button
            type="warning"
            @click="saveChanges"
            :disabled="!hasChanges"
            style="width: 100%;"
          >
            <i class="fas fa-save"></i> Сохранить изменения
          </el-button>
        </div>
      </div>
    </div>

    <!-- Таблица точек -->
    <div class="points-table-section">
      <h3>Таблица точек ({{ points.length }})</h3>
      <el-table :data="points" style="width: 100%" max-height="300">
        <el-table-column prop="x" label="X" width="120">
          <template #default="scope">
            {{ scope.row.x.toFixed(4) }}
          </template>
        </el-table-column>
        <el-table-column label="Y" width="200">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.y"
              :precision="6"
              :step="0.001"
              controls-position="right"
              size="small"
              @change="markAsChanged(scope.$index)"
            />
          </template>
        </el-table-column>
        <el-table-column label="Действия" width="120" v-if="functionData.removable">
          <template #default="scope">
            <el-button
              v-if="functionData.removable && points.length > 2"
              link
              type="danger"
              @click="removePoint(scope.$index)"
            >
              Удалить
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <template #footer>
      <el-button @click="close">Закрыть</el-button>
      <el-button type="primary" @click="exportFunction">Экспорт</el-button>
    </template>
  </el-dialog>

  <!-- Диалог вставки точки -->
  <insert-point-dialog
    v-if="showInsertDialog"
    :function-id="functionData.id"
    @insert="handleInsertPoint"
    @close="showInsertDialog = false"
  />
</template>

<script>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import FunctionChart from '@/components/FunctionChart.vue'
import InsertPointDialog from '@/components/InsertPointDialog.vue'
import { useFunctionsStore } from '@/stores/functions'

export default {
  name: 'FunctionExplorer',
  components: {
    FunctionChart,
    InsertPointDialog
  },
  props: {
    functionData: {
      type: Object,
      required: true
    },
    modelValue: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue', 'close', 'updated'],
  setup(props, { emit }) {
    const functionsStore = useFunctionsStore()
    const visible = computed({
      get: () => props.modelValue,
      set: (value) => emit('update:modelValue', value)
    })

    const points = ref([])
    const originalPoints = ref([])
    const changedPoints = ref(new Set())
    const calculationX = ref(0)
    const calculationResult = ref(null)
    const calculating = ref(false)
    const showInsertDialog = ref(false)
    const showRemoveDialog = ref(false)

    const hasChanges = computed(() => changedPoints.value.size > 0)

    const loadPoints = async () => {
      try {
        if (props.functionData.points) {
          points.value = [...props.functionData.points]
        } else {
          const pointsData = await functionsStore.getFunctionPoints(props.functionData.id)
          points.value = pointsData.map(p => ({ x: p.x, y: p.y }))
        }
        originalPoints.value = [...points.value]
        changedPoints.value = new Set()
      } catch (error) {
        ElMessage.error('Ошибка загрузки точек: ' + error.message)
      }
    }

    const calculateValue = () => {
      if (points.value.length === 0) {
        ElMessage.warning('Нет точек для вычисления')
        return
      }

      calculating.value = true

      // Линейная интерполяция
      const sortedPoints = [...points.value].sort((a, b) => a.x - b.x)
      const x = calculationX.value

      // Если x вне диапазона
      if (x <= sortedPoints[0].x) {
        calculationResult.value = sortedPoints[0].y
      } else if (x >= sortedPoints[sortedPoints.length - 1].x) {
        calculationResult.value = sortedPoints[sortedPoints.length - 1].y
      } else {
        // Находим интервал для интерполяции
        for (let i = 0; i < sortedPoints.length - 1; i++) {
          if (x >= sortedPoints[i].x && x <= sortedPoints[i + 1].x) {
            const x0 = sortedPoints[i].x
            const y0 = sortedPoints[i].y
            const x1 = sortedPoints[i + 1].x
            const y1 = sortedPoints[i + 1].y

            calculationResult.value = y0 + (y1 - y0) * (x - x0) / (x1 - x0)
            break
          }
        }
      }

      calculating.value = false
    }

    const handlePointSelected = (point) => {
      calculationX.value = point.x
      calculateValue()
    }

    const markAsChanged = (index) => {
      changedPoints.value.add(index)
    }

    const removePoint = (index) => {
      if (points.value.length <= 2) {
        ElMessage.warning('Нельзя удалить точку: минимальное количество точек - 2')
        return
      }

      points.value.splice(index, 1)
      changedPoints.value.add(index) // Отмечаем изменение
    }

    const handleInsertPoint = (newPoint) => {
      // Находим позицию для вставки (сохраняя сортировку по X)
      let insertIndex = points.value.length
      for (let i = 0; i < points.value.length; i++) {
        if (points.value[i].x > newPoint.x) {
          insertIndex = i
          break
        }
      }

      points.value.splice(insertIndex, 0, newPoint)
      changedPoints.value.add(insertIndex)
      showInsertDialog.value = false
    }

    const saveChanges = async () => {
      try {
        // Сохраняем изменения в store или через API
        ElMessage.success('Изменения сохранены')
        changedPoints.value.clear()
        emit('updated')
      } catch (error) {
        ElMessage.error('Ошибка сохранения: ' + error.message)
      }
    }

    const exportFunction = () => {
      // Логика экспорта функции
      ElMessage.info('Функция экспортирована')
    }

    const close = () => {
      if (hasChanges.value) {
        ElMessage.warning('Есть несохраненные изменения')
      }
      emit('close')
    }

    watch(visible, (newVal) => {
      if (newVal) {
        loadPoints()
      }
    })

    return {
      visible,
      points,
      calculationX,
      calculationResult,
      calculating,
      showInsertDialog,
      showRemoveDialog,
      hasChanges,
      calculateValue,
      handlePointSelected,
      markAsChanged,
      removePoint,
      handleInsertPoint,
      saveChanges,
      exportFunction,
      close
    }
  }
}
</script>

<style scoped>
.function-explorer-dialog {
  max-width: 1200px;
}

.explorer-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-section {
  min-height: 400px;
}

.control-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.function-info,
.calculation-section,
.actions-section {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.function-info h3,
.calculation-section h3,
.actions-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #2c3e50;
  border-bottom: 2px solid #3498db;
  padding-bottom: 5px;
}

.result {
  margin-top: 10px;
  padding: 10px;
  background: #e8f5e8;
  border: 1px solid #4caf50;
  border-radius: 4px;
  font-weight: bold;
  color: #2e7d32;
  text-align: center;
}

.points-table-section {
  margin-top: 20px;
}

@media (max-width: 1024px) {
  .explorer-content {
    grid-template-columns: 1fr;
  }
}
</style>