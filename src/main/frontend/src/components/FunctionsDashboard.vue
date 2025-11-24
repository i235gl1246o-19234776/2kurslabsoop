<template>
  <div class="functions-dashboard">
    <div class="header">
      <h1>Лабораторная работа: Табулированные функции</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateFunctionDialog = true">
          Создать функцию
        </el-button>
        <el-button @click="showSettingsDialog = true">
          Настройки
        </el-button>
      </div>
    </div>

    <div class="dashboard-content">
      <!-- Список функций -->
      <div class="functions-list">
        <h2>Доступные функции</h2>
        <div v-if="functions.length === 0" class="no-functions">
          <p>Нет созданных функций</p>
        </div>
        <el-table v-else :data="functions" style="width: 100%" @row-click="selectFunction">
          <el-table-column prop="name" label="Название" />
          <el-table-column prop="type" label="Тип" />
          <el-table-column prop="pointsCount" label="Точек" />
          <el-table-column label="Действия">
            <template #default="scope">
              <el-button size="small" @click.stop="exploreFunction(scope.row)">
                Исследовать
              </el-button>
              <el-button size="small" type="danger" @click.stop="deleteFunction(scope.row)">
                Удалить
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- График выбранной функции -->
      <div class="function-chart">
        <h2>График функции</h2>
        <div class="chart-container">
          <div v-if="selectedFunction && selectedFunction.points && selectedFunction.points.length > 0" class="chart-wrapper">
            <function-chart
              :points="selectedFunction.points"
              :show-controls="true"
              :title="selectedFunction.name"
            />
          </div>
          <div v-else class="no-data">
            <p v-if="!selectedFunction">Выберите функцию для отображения графика</p>
            <p v-else-if="!selectedFunction.points || selectedFunction.points.length === 0">
              У выбранной функции нет точек данных
            </p>
          </div>
        </div>

        <!-- Информация о выбранной функции -->
        <div v-if="selectedFunction" class="selected-function-info">
          <h3>Информация о функции</h3>
          <p><strong>Название:</strong> {{ selectedFunction.name }}</p>
          <p><strong>Тип:</strong> {{ selectedFunction.typeFunction || 'Не указан' }}</p>
          <p><strong>Количество точек:</strong> {{ selectedFunction.points ? selectedFunction.points.length : 0 }}</p>
        </div>
      </div>
    </div>

    <!-- Отладочная информация (временно) -->
    <div class="debug-info" style="background: #f0f0f0; padding: 10px; margin-top: 20px;">
      <h4>Отладочная информация:</h4>
      <p>Всего функций: {{ functions.length }}</p>
      <p>Выбранная функция: {{ selectedFunction ? selectedFunction.name : 'нет' }}</p>
      <p v-if="selectedFunction">Точек у выбранной функции: {{ selectedFunction.points ? selectedFunction.points.length : 0 }}</p>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FunctionChart from '@/components/FunctionChart.vue'
import { useFunctionsStore } from '@/stores/functions'

export default {
  name: 'FunctionsDashboard',
  components: {
    FunctionChart
  },
  setup() {
    const functionsStore = useFunctionsStore()
    const functions = ref([])
    const selectedFunction = ref(null)
    const showCreateFunctionDialog = ref(false)
    const showSettingsDialog = ref(false)

    const loadFunctions = async () => {
      try {
        console.log('🔄 Загрузка функций...')
        await functionsStore.loadFunctions()

        // Преобразуем данные для отображения
        functions.value = functionsStore.functions.map(func => ({
          ...func,
          type: func.typeFunction || 'tabular',
          pointsCount: func.points ? func.points.length : 0
        }))

        console.log('✅ Функции загружены:', functions.value)

        // Если есть функции, выбираем первую
        if (functions.value.length > 0 && !selectedFunction.value) {
          selectFunction(functions.value[0])
        }
      } catch (error) {
        console.error('❌ Ошибка загрузки функций:', error)
        ElMessage.error('Ошибка загрузки функций: ' + error.message)
      }
    }

    const selectFunction = (func) => {
      console.log('🎯 Выбрана функция:', func)
      selectedFunction.value = func

      // Если у функции нет точек, пытаемся загрузить их
      if (!func.points || func.points.length === 0) {
        loadFunctionPoints(func.id)
      }
    }

    const loadFunctionPoints = async (functionId) => {
      try {
        console.log('🔄 Загрузка точек для функции ID:', functionId)
        const points = await functionsStore.getFunctionPoints(functionId)

        // Обновляем точки у выбранной функции
        if (selectedFunction.value && selectedFunction.value.id === functionId) {
          selectedFunction.value.points = points
          console.log('✅ Точки загружены:', points)
        }

        // Также обновляем в общем списке
        const funcIndex = functions.value.findIndex(f => f.id === functionId)
        if (funcIndex !== -1) {
          functions.value[funcIndex].points = points
        }
      } catch (error) {
        console.error('❌ Ошибка загрузки точек:', error)
        ElMessage.error('Ошибка загрузки точек функции: ' + error.message)
      }
    }

    const exploreFunction = (func) => {
      selectFunction(func)
      // Здесь можно открыть отдельное окно исследования
      ElMessage.info(`Исследование функции: ${func.name}`)
    }

    const deleteFunction = async (func) => {
      try {
        await ElMessageBox.confirm(
          `Вы уверены, что хотите удалить функцию "${func.name}"?`,
          'Подтверждение удаления',
          { type: 'warning' }
        )

        await functionsStore.deleteFunction(func.id)
        ElMessage.success('Функция удалена успешно')

        // Перезагружаем список
        await loadFunctions()

        // Сбрасываем выбор если удалили выбранную функцию
        if (selectedFunction.value && selectedFunction.value.id === func.id) {
          selectedFunction.value = null
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('Ошибка удаления функции: ' + error.message)
        }
      }
    }

    onMounted(() => {
      console.log('🚀 FunctionsDashboard mounted')
      loadFunctions()
    })

    return {
      functions,
      selectedFunction,
      showCreateFunctionDialog,
      showSettingsDialog,
      selectFunction,
      exploreFunction,
      deleteFunction,
      loadFunctionPoints
    }
  }
}
</script>

<style scoped>
.functions-dashboard {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
}

.header h1 {
  margin: 0;
  color: #2c3e50;
}

.dashboard-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  min-height: 600px;
}

.functions-list {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.function-chart {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.chart-container {
  flex: 1;
  position: relative;
  min-height: 400px;
}

.chart-wrapper {
  height: 100%;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  color: #999;
  font-style: italic;
  text-align: center;
}

.no-functions {
  text-align: center;
  padding: 40px;
  color: #999;
}

.selected-function-info {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #3498db;
}

.selected-function-info h3 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #2c3e50;
}

@media (max-width: 1024px) {
  .dashboard-content {
    grid-template-columns: 1fr;
  }
}
</style>