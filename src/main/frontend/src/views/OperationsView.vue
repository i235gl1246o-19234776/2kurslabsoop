<template>
  <div class="operations-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>Операции с функциями</h2>
          <el-radio-group v-model="activeOperation" size="large">
            <el-radio-button label="binary">Бинарные операции</el-radio-button>
            <el-radio-button label="differentiate">Дифференцирование</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- Бинарные операции -->
      <div v-if="activeOperation === 'binary'">
        <el-row :gutter="20">
          <el-col :span="8">
            <function-operand
              title="Функция 1"
              :function-id="operand1Id"
              @function-change="handleOperand1Change"
              @function-created="handleFunctionCreated"
            />
          </el-col>

          <el-col :span="8">
            <div class="operation-center">
              <div class="operation-buttons">
                <el-button
                  type="primary"
                  :disabled="!canPerformOperation"
                  @click="performOperation('add')"
                >
                  <el-icon><Plus /></el-icon>
                  Сложение
                </el-button>

                <el-button
                  type="success"
                  :disabled="!canPerformOperation"
                  @click="performOperation('subtract')"
                >
                  <el-icon><Minus /></el-icon>
                  Вычитание
                </el-button>

                <el-button
                  type="warning"
                  :disabled="!canPerformOperation"
                  @click="performOperation('multiply')"
                >
                  <el-icon><Close /></el-icon>
                  Умножение
                </el-button>

                <el-button
                  type="danger"
                  :disabled="!canPerformOperation"
                  @click="performOperation('divide')"
                >
                  <el-icon><Divide /></el-icon>
                  Деление
                </el-button>
              </div>

              <el-form :model="operationSettings" label-width="120px">
                <el-form-item label="Тип фабрики">
                  <el-radio-group v-model="operationSettings.factoryType">
                    <el-radio label="array">Массив</el-radio>
                    <el-radio label="linkedlist">Связный список</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-form>
            </div>
          </el-col>

          <el-col :span="8">
            <function-operand
              title="Функция 2"
              :function-id="operand2Id"
              @function-change="handleOperand2Change"
              @function-created="handleFunctionCreated"
            />
          </el-col>
        </el-row>

        <!-- Результат -->
        <el-card v-if="operationResult" style="margin-top: 20px">
          <template #header>
            <div class="result-header">
              <h3>Результат операции</h3>
              <el-button type="success" @click="saveResult">
                Сохранить результат
              </el-button>
            </div>
          </template>

          <function-table :points="operationResult" />
        </el-card>
      </div>

      <!-- Дифференцирование -->
      <div v-if="activeOperation === 'differentiate'">
        <el-row :gutter="20">
          <el-col :span="12">
            <function-operand
              title="Исходная функция"
              :function-id="differentiateFunctionId"
              @function-change="handleDifferentiateFunctionChange"
              @function-created="handleFunctionCreated"
            />
          </el-col>

          <el-col :span="12">
            <div class="differentiate-controls">
              <el-form :model="differentiateSettings" label-width="120px">
                <el-form-item label="Тип фабрики">
                  <el-radio-group v-model="differentiateSettings.factoryType">
                    <el-radio label="array">Массив</el-radio>
                    <el-radio label="linkedlist">Связный список</el-radio>
                  </el-radio-group>
                </el-form-item>

                <el-form-item>
                  <el-button
                    type="primary"
                    :disabled="!differentiateFunctionId"
                    @click="performDifferentiation"
                    :loading="differentiateLoading"
                  >
                    Выполнить дифференцирование
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-col>
        </el-row>

        <!-- Результат дифференцирования -->
        <el-card v-if="differentiateResult" style="margin-top: 20px">
          <template #header>
            <div class="result-header">
              <h3>Производная функции</h3>
              <el-button type="success" @click="saveDifferentiateResult">
                Сохранить результат
              </el-button>
            </div>
          </template>

          <function-table :points="differentiateResult" />
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useFunctionsStore } from '@/stores/functions'
import { api } from '@/services/api'
import { ElMessage } from 'element-plus'
import { Plus, Minus, Close, Divide } from '@element-plus/icons-vue'
import FunctionOperand from '@/components/FunctionOperand.vue'
import FunctionTable from '@/components/FunctionTable.vue'

const functionsStore = useFunctionsStore()

const activeOperation = ref('binary')

// Бинарные операции
const operand1Id = ref(null)
const operand2Id = ref(null)
const operationResult = ref(null)
const operationSettings = ref({
  factoryType: 'array'
})

// Дифференцирование
const differentiateFunctionId = ref(null)
const differentiateResult = ref(null)
const differentiateLoading = ref(false)
const differentiateSettings = ref({
  factoryType: 'array'
})

const canPerformOperation = computed(() =>
  operand1Id.value && operand2Id.value
)

const handleOperand1Change = (functionId) => {
  operand1Id.value = functionId
}

const handleOperand2Change = (functionId) => {
  operand2Id.value = functionId
}

const handleDifferentiateFunctionChange = (functionId) => {
  differentiateFunctionId.value = functionId
}

const handleFunctionCreated = () => {
  functionsStore.loadFunctions()
}

const performOperation = async (operation) => {
  try {
    const response = await api.post(`/operations/${operation}`, {
      operand1Id: operand1Id.value,
      operand2Id: operand2Id.value,
      factoryType: operationSettings.value.factoryType
    })

    operationResult.value = response.data
    ElMessage.success(`Операция ${operation} выполнена успешно`)
  } catch (error) {
    ElMessage.error(`Ошибка при выполнении операции: ${error.message}`)
  }
}

const performDifferentiation = async () => {
  differentiateLoading.value = true
  try {
    const response = await api.post('/operations/differentiate', {
      operand1Id: differentiateFunctionId.value,
      factoryType: differentiateSettings.value.factoryType
    })

    differentiateResult.value = response.data
    ElMessage.success('Дифференцирование выполнено успешно')
  } catch (error) {
    ElMessage.error(`Ошибка при дифференцировании: ${error.message}`)
  } finally {
    differentiateLoading.value = false
  }
}

const saveResult = async () => {
  if (!operationResult.value) return

  try {
    // Создаем новую функцию из результата
    const functionData = {
      typeFunction: 'tabular',
      functionName: `Результат_операции_${Date.now()}`,
      userId: functionsStore.functions[0]?.userId // Берем ID текущего пользователя
    }

    await functionsStore.createFunction(functionData)
    ElMessage.success('Результат сохранен как новая функция')
  } catch (error) {
    ElMessage.error('Ошибка при сохранении результата')
  }
}

const saveDifferentiateResult = async () => {
  if (!differentiateResult.value) return

  try {
    const functionData = {
      typeFunction: 'tabular',
      functionName: `Производная_${Date.now()}`,
      userId: functionsStore.functions[0]?.userId
    }

    await functionsStore.createFunction(functionData)
    ElMessage.success('Производная сохранена как новая функция')
  } catch (error) {
    ElMessage.error('Ошибка при сохранении производной')
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.operation-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 20px;
}

.operation-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.operation-buttons .el-button {
  width: 200px;
}

.differentiate-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>