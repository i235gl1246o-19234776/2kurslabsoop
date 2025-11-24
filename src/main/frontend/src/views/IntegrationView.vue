<template>
  <div class="integration-view">
    <el-card>
      <template #header>
        <h2>Вычисление интегралов</h2>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <function-operand
            title="Функция для интегрирования"
            :function-id="selectedFunctionId"
            @function-change="handleFunctionChange"
            @function-created="handleFunctionCreated"
          />
        </el-col>

        <el-col :span="12">
          <el-form :model="form" :rules="rules" ref="formRef" label-width="140px">
            <el-form-item label="Нижний предел" prop="fromX">
              <el-input-number
                v-model="form.fromX"
                :step="0.1"
                :precision="3"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="Верхний предел" prop="toX">
              <el-input-number
                v-model="form.toX"
                :step="0.1"
                :precision="3"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="Количество потоков" prop="threadCount">
              <el-slider
                v-model="form.threadCount"
                :min="1"
                :max="16"
                :step="1"
                show-stops
                show-input
              />
            </el-form-item>

            <el-form-item label="Тип фабрики">
              <el-radio-group v-model="form.factoryType">
                <el-radio label="array">Массив</el-radio>
                <el-radio label="linkedlist">Связный список</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                :disabled="!selectedFunctionId"
                @click="handleIntegrate"
                style="width: 100%"
                size="large"
              >
                <el-icon><CPU /></el-icon>
                Вычислить интеграл
              </el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>

      <el-card v-if="result" style="margin-top: 20px" class="result-card">
        <template #header>
          <div class="result-header">
            <h3>Результат интегрирования</h3>
            <el-tag type="success">Успешно</el-tag>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="Значение интеграла">
            <span class="result-value">{{ result.value.toFixed(6) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="Время вычисления">
            <span class="result-duration">{{ result.duration }} мс</span>
          </el-descriptions-item>
          <el-descriptions-item label="Интервал">
            [{{ form.fromX.toFixed(2) }}, {{ form.toX.toFixed(2) }}]
          </el-descriptions-item>
          <el-descriptions-item label="Потоков использовано">
            {{ form.threadCount }}
          </el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 15px">
          <el-button type="success" @click="saveResult">
            Сохранить результат
          </el-button>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useFunctionsStore } from '@/stores/functions'
import { useAuthStore } from '@/stores/auth'
import { api } from '@/services/api'
import { ElMessage } from 'element-plus'
import { CPU } from '@element-plus/icons-vue'
import FunctionOperand from '@/components/FunctionOperand.vue'

const functionsStore = useFunctionsStore()
const authStore = useAuthStore()

const selectedFunctionId = ref(null)
const loading = ref(false)
const result = ref(null)
const formRef = ref()

const form = ref({
  fromX: 0,
  toX: 10,
  threadCount: 4,
  factoryType: 'array'
})

const rules = {
  fromX: [
    { required: true, message: 'Введите нижний предел', trigger: 'blur' }
  ],
  toX: [
    { required: true, message: 'Введите верхний предел', trigger: 'blur' }
  ],
  threadCount: [
    { required: true, message: 'Выберите количество потоков', trigger: 'change' }
  ]
}

const canIntegrate = computed(() =>
  selectedFunctionId.value && form.value.fromX !== null && form.value.toX !== null
)

const handleFunctionChange = (functionId) => {
  selectedFunctionId.value = functionId
}

const handleFunctionCreated = () => {
  functionsStore.loadFunctions()
}

const handleIntegrate = async () => {
  if (!formRef.value || !canIntegrate.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await api.post('/operations/integrate', {
          functionId: selectedFunctionId.value,
          ...form.value
        })

        result.value = response.data
        ElMessage.success('Интеграл вычислен успешно')
      } catch (error) {
        ElMessage.error('Ошибка при вычислении интеграла: ' + error.message)
      } finally {
        loading.value = false
      }
    }
  })
}

const saveResult = async () => {
  if (!result.value) return

  try {
    const functionData = {
      typeFunction: 'analytic',
      functionName: `Интеграл_${Date.now()}`,
      functionExpression: `∫f(x)dx ≈ ${result.value.value.toFixed(6)}`,
      userId: authStore.user.id
    }

    await functionsStore.createFunction(functionData)
    ElMessage.success('Результат сохранен как аналитическая функция')
  } catch (error) {
    ElMessage.error('Ошибка при сохранении результата')
  }
}
</script>

<style scoped>
.integration-view {
  padding: 20px;
}

.result-card {
  border: 1px solid #e1f5fe;
  background: #f8fdff;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-value {
  font-size: 18px;
  font-weight: bold;
  color: #1890ff;
}

.result-duration {
  font-weight: bold;
  color: #52c41a;
}
</style>