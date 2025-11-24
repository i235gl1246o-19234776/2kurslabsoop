<template>
  <div class="function-table">
    <el-table
      :data="points"
      :loading="loading"
      max-height="300"
      style="width: 100%"
    >
      <el-table-column prop="xVal" label="X" width="120">
        <template #default="scope">
          {{ scope.row.xVal.toFixed(4) }}
        </template>
      </el-table-column>

      <el-table-column prop="yVal" label="Y" width="120">
        <template #default="scope">
          {{ scope.row.yVal.toFixed(4) }}
        </template>
      </el-table-column>

      <el-table-column v-if="editable" label="Действия" width="100">
        <template #default="scope">
          <el-button link type="primary" @click="editPoint(scope.row)">
            Изменить
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="points.length === 0 && !loading" class="empty-table">
      <el-empty description="Нет данных функции" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { api } from '@/services/api'

const props = defineProps({
  functionId: Number,
  points: Array,
  editable: {
    type: Boolean,
    default: false
  }
})

const points = ref(props.points || [])
const loading = ref(false)

const loadPoints = async () => {
  if (!props.functionId) return

  loading.value = true
  try {
    const response = await api.get(`/tabulated-points/function/${props.functionId}`)
    points.value = response.data
  } catch (error) {
    console.error('Ошибка загрузки точек:', error)
    points.value = []
  } finally {
    loading.value = false
  }
}

const editPoint = (point) => {
  // Реализация редактирования точки
  console.log('Edit point:', point)
}

onMounted(() => {
  if (props.functionId) {
    loadPoints()
  }
})

watch(() => props.functionId, (newVal) => {
  if (newVal) {
    loadPoints()
  } else {
    points.value = []
  }
})

watch(() => props.points, (newVal) => {
  if (newVal) {
    points.value = newVal
  }
})
</script>

<style scoped>
.empty-table {
  padding: 40px 0;
  text-align: center;
  color: #909399;
}
</style>