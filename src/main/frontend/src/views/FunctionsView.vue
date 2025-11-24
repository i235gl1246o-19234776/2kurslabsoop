<template>
  <div class="functions-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>Мои функции</h2>
          <div>
            <el-button type="primary" @click="showCreateDialog = true">
              Создать из точек
            </el-button>
            <el-button type="success" @click="showMathDialog = true">
              Создать из Math функции
            </el-button>
            <el-button type="warning" @click="showCompositeDialog = true">
              Создать сложную функцию
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="functionsStore.functions" v-loading="functionsStore.loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="functionName" label="Название" />
        <el-table-column prop="typeFunction" label="Тип">
          <template #default="scope">
            <el-tag :type="scope.row.typeFunction === 'tabular' ? 'primary' : 'success'">
              {{ scope.row.typeFunction === 'tabular' ? 'Табличная' : 'Аналитическая' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="functionExpression" label="Выражение" />
        <el-table-column label="Действия" width="200">
          <template #default="scope">
            <el-button size="small" @click="viewFunction(scope.row)">Просмотр</el-button>
            <el-button size="small" type="danger" @click="deleteFunction(scope.row.id)">
              Удалить
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Диалог создания из точек -->
    <CreateFunctionDialog v-model="showCreateDialog" />

    <!-- Диалог создания из Math функции -->
    <CreateFromMathDialog v-model="showMathDialog" />

    <!-- Диалог создания сложной функции -->
    <CompositeFunctionDialog v-model="showCompositeDialog" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useFunctionsStore } from '@/stores/functions'
import { ElMessage, ElMessageBox } from 'element-plus'
import CreateFunctionDialog from '@/components/CreateFunctionDialog.vue'
import CreateFromMathDialog from '@/components/CreateFromMathDialog.vue'
import CompositeFunctionDialog from '@/components/CompositeFunctionDialog.vue'

const functionsStore = useFunctionsStore()
const showCreateDialog = ref(false)
const showMathDialog = ref(false)
const showCompositeDialog = ref(false)

onMounted(async () => {
  await functionsStore.loadFunctions()
  await functionsStore.loadMathFunctions()
})

const viewFunction = (func) => {
  // Реализация просмотра функции
  ElMessage.info(`Просмотр функции: ${func.functionName}`)
}

const deleteFunction = async (id) => {
  try {
    await ElMessageBox.confirm(
      'Вы уверены, что хотите удалить эту функцию?',
      'Подтверждение удаления',
      { type: 'warning' }
    )

    await functionsStore.deleteFunction(id)
    ElMessage.success('Функция удалена')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Ошибка при удалении функции')
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>