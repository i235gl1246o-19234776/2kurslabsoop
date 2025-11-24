<template>
  <div class="graphs-view">
    <div class="header">
      <h1>Графики функций</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showAllGraphs = !showAllGraphs">
          {{ showAllGraphs ? 'Показать один график' : 'Показать все графики' }}
        </el-button>
      </div>
    </div>

    <!-- Один выбранный график -->
    <div v-if="!showAllGraphs" class="single-graph-mode">
      <div class="function-selector">
        <el-select v-model="selectedFunctionId" placeholder="Выберите функцию" style="width: 300px">
          <el-option
            v-for="func in functions"
            :key="func.id"
            :label="func.name"
            :value="func.id"
          />
        </el-select>
      </div>

      <div class="main-chart">
        <div v-if="selectedFunction" class="chart-container">
          <function-chart
            :points="selectedFunction.points"
            :title="selectedFunction.name"
            :show-controls="true"
          />
        </div>
        <div v-else class="no-function-selected">
          <el-empty description="Выберите функцию для отображения графика" />
        </div>
      </div>
    </div>

    <!-- Все графики на одной странице -->
    <div v-else class="all-graphs-mode">
      <div class="graphs-grid">
        <div
          v-for="func in functionsWithPoints"
          :key="func.id"
          class="graph-item"
        >
          <div class="graph-card">
            <h3>{{ func.name }}</h3>
            <div class="mini-chart">
              <function-chart
                :points="func.points"
                :title="func.name"
                :show-controls="false"
              />
            </div>
            <div class="graph-info">
              <p>Точек: {{ func.points.length }}</p>
              <el-button size="small" @click="openFunctionInSingleMode(func)">
                Открыть
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="functionsWithPoints.length === 0" class="no-graphs">
        <el-empty description="Нет функций с точками для отображения" />
      </div>
    </div>

    <!-- Информационная панель -->
    <div class="info-panel">
      <el-alert
        title="Управление графиками"
        description="Используйте колесо мыши для масштабирования, перетаскивание для перемещения по графику"
        type="info"
        show-icon
        :closable="false"
      />
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import FunctionChart from '@/components/FunctionChart.vue'
import { useFunctionsStore } from '@/stores/functions'

export default {
  name: 'GraphsView',
  components: {
    FunctionChart
  },
  setup() {
    const functionsStore = useFunctionsStore()
    const functions = ref([])
    const selectedFunctionId = ref(null)
    const showAllGraphs = ref(false)

    // Загружаем функции
    const loadFunctions = async () => {
      try {
        await functionsStore.loadFunctions()
        functions.value = functionsStore.functions

        // Автоматически выбираем первую функцию с точками
        const firstFunctionWithPoints = functions.value.find(func =>
          func.points && func.points.length > 0
        )
        if (firstFunctionWithPoints) {
          selectedFunctionId.value = firstFunctionWithPoints.id
        }
      } catch (error) {
        ElMessage.error('Ошибка загрузки функций: ' + error.message)
      }
    }

    // Выбранная функция
    const selectedFunction = computed(() => {
      if (!selectedFunctionId.value) return null
      return functions.value.find(func => func.id === selectedFunctionId.value)
    })

    // Функции с точками
    const functionsWithPoints = computed(() => {
      return functions.value.filter(func => func.points && func.points.length > 0)
    })

    // Открыть функцию в режиме одного графика
    const openFunctionInSingleMode = (func) => {
      selectedFunctionId.value = func.id
      showAllGraphs.value = false
    }

    onMounted(() => {
      loadFunctions()
    })

    return {
      functions,
      selectedFunctionId,
      selectedFunction,
      showAllGraphs,
      functionsWithPoints,
      openFunctionInSingleMode
    }
  }
}
</script>

<style scoped>
.graphs-view {
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

.single-graph-mode {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.function-selector {
  display: flex;
  justify-content: center;
}

.main-chart {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  min-height: 500px;
}

.chart-container {
  height: 500px;
}

.no-function-selected {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.all-graphs-mode {
  padding: 20px 0;
}

.graphs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.graph-item {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.graph-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.graph-card {
  padding: 15px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.graph-card h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  text-align: center;
  font-size: 16px;
}

.mini-chart {
  flex: 1;
  min-height: 250px;
  position: relative;
}

.graph-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e0e0e0;
}

.graph-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.no-graphs {
  text-align: center;
  padding: 40px;
}

.info-panel {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .graphs-grid {
    grid-template-columns: 1fr;
  }

  .header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }

  .function-selector {
    justify-content: stretch;
  }
}
</style>