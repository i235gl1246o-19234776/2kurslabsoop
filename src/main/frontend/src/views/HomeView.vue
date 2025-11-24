<template>
  <div class="home-view">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-statistic title="Всего функций" :value="functionsStore.functions.length" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="Табличные" :value="functionsStore.tabularFunctions.length" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="Аналитические" :value="functionsStore.analyticFunctions.length" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="Math функции" :value="functionsStore.mathFunctions.length" />
      </el-col>
    </el-row>

    <el-divider />

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="quick-actions">
          <template #header>
            <h3>Быстрые действия</h3>
          </template>

          <el-space direction="vertical" style="width: 100%">
            <el-button
              type="primary"
              @click="$router.push('/functions/create')"
              style="width: 100%"
            >
              Создать функцию из точек
            </el-button>

            <el-button
              type="success"
              @click="$router.push('/functions/create-from-math')"
              style="width: 100%"
            >
              Создать из Math функции
            </el-button>

            <el-button
              type="warning"
              @click="$router.push('/functions/composite')"
              style="width: 100%"
            >
              Создать сложную функцию
            </el-button>

            <el-button
              type="info"
              @click="$router.push('/operations')"
              style="width: 100%"
            >
              Операции с функциями
            </el-button>

            <el-button
              type="danger"
              @click="$router.push('/integration')"
              style="width: 100%"
            >
              Вычисление интегралов
            </el-button>
          </el-space>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <h3>Последние функции</h3>
          </template>

          <el-table
            :data="recentFunctions"
            height="300"
            v-loading="functionsStore.loading"
          >
            <el-table-column prop="functionName" label="Название" />
            <el-table-column prop="typeFunction" label="Тип" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.typeFunction === 'tabular' ? 'primary' : 'success'">
                  {{ scope.row.typeFunction === 'tabular' ? 'Табличная' : 'Аналитическая' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="functionExpression" label="Выражение" />
            <el-table-column label="Действия" width="80">
              <template #default="scope">
                <el-button
                  link
                  type="primary"
                  @click="$router.push(`/functions?id=${scope.row.id}`)"
                >
                  Открыть
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useFunctionsStore } from '@/stores/functions'

const functionsStore = useFunctionsStore()

const recentFunctions = computed(() =>
  functionsStore.functions.slice(0, 5)
)

onMounted(async () => {
  await functionsStore.loadFunctions()
  await functionsStore.loadMathFunctions()
})
</script>

<style scoped>
.home-view {
  padding: 20px;
}

.quick-actions .el-button {
  justify-content: flex-start;
  height: 50px;
  font-size: 16px;
}
</style>