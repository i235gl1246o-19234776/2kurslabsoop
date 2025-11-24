<template>
  <el-dialog
    v-model="visible"
    title="Создание функции из Math функции"
    width="700px"
    :close-on-click-modal="false"
  >
    <el-form :model="form" :rules="rules" ref="formRef">
      <el-form-item label="Math функция" prop="mathFunctionName">
        <el-select
          v-model="form.mathFunctionName"
          placeholder="Выберите математическую функцию"
          style="width: 100%"
          filterable
        >
          <el-option-group label="Основные функции">
            <el-option
              v-for="func in basicFunctions"
              :key="func.name"
              :label="func.displayName"
              :value="func.name"
            >
              <div class="function-option">
                <div class="function-name">{{ func.displayName }}</div>
                <div class="function-description">{{ func.description }}</div>
              </div>
            </el-option>
          </el-option-group>

          <el-option-group label="Тригонометрические функции">
            <el-option
              v-for="func in trigonometricFunctions"
              :key="func.name"
              :label="func.displayName"
              :value="func.name"
            >
              <div class="function-option">
                <div class="function-name">{{ func.displayName }}</div>
                <div class="function-description">{{ func.description }}</div>
              </div>
            </el-option>
          </el-option-group>

          <el-option-group label="Логарифмические и экспоненциальные">
            <el-option
              v-for="func in logExpFunctions"
              :key="func.name"
              :label="func.displayName"
              :value="func.name"
            >
              <div class="function-option">
                <div class="function-name">{{ func.displayName }}</div>
                <div class="function-description">{{ func.description }}</div>
              </div>
            </el-option>
          </el-option-group>
        </el-select>

        <!-- Предпросмотр выбранной функции -->
        <div v-if="selectedFunction" class="function-preview">
          <h4>Предпросмотр: {{ selectedFunction.displayName }}</h4>
          <p class="function-expression">{{ selectedFunction.description }}</p>
          <div class="preview-values">
            <span>f(0) = {{ calculatePreview(0) }}</span>
            <span>f(1) = {{ calculatePreview(1) }}</span>
            <span>f(2) = {{ calculatePreview(2) }}</span>
          </div>
        </div>
      </el-form-item>

      <!-- Остальные поля формы остаются без изменений -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="Начало интервала" prop="xFrom">
            <el-input-number
              v-model="form.xFrom"
              :step="0.1"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Конец интервала" prop="xTo">
            <el-input-number
              v-model="form.xTo"
              :step="0.1"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="Количество точек" prop="count">
        <el-input-number
          v-model="form.count"
          :min="2"
          :max="1000"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="Тип фабрики">
        <el-radio-group v-model="form.factoryType">
          <el-radio label="array">Массив</el-radio>
          <el-radio label="linkedlist">Связный список</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">Отмена</el-button>
      <el-button type="primary" :loading="loading" @click="handleCreate">
        Создать
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useFunctionsStore } from '@/stores/functions'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'created'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const formRef = ref()
const functionsStore = useFunctionsStore()
const authStore = useAuthStore()
const loading = ref(false)

const form = ref({
  mathFunctionName: '',
  xFrom: 0,
  xTo: 10,
  count: 100,
  factoryType: 'array',
  userId: authStore.user?.id
})

// Группировка функций по категориям
const basicFunctions = computed(() =>
  functionsStore.mathFunctions.filter(func =>
    ['SqrFunction', 'IdentityFunction', 'ZeroFunction', 'ConstantFunction',
     'AbsFunction', 'CubeFunction', 'UnitFunction', 'ReciprocalFunction'].includes(func.name)
  )
)

const trigonometricFunctions = computed(() =>
  functionsStore.mathFunctions.filter(func =>
    ['SinFunction', 'CosFunction', 'TanFunction', 'SigmoidFunction'].includes(func.name)
  )
)

const logExpFunctions = computed(() =>
  functionsStore.mathFunctions.filter(func =>
    ['ExpFunction', 'LogFunction', 'Log10Function', 'SqrtFunction'].includes(func.name)
  )
)

const selectedFunction = computed(() =>
  functionsStore.mathFunctions.find(f => f.name === form.value.mathFunctionName)
)

// Функция для предпросмотра значений
const calculatePreview = (x) => {
  if (!selectedFunction.value) return 'N/A'

  const funcName = selectedFunction.value.name
  const result = functionsStore.calculateMathFunctionValue(funcName, x)
  return isNaN(result) || !isFinite(result) ? 'N/A' : result.toFixed(3)
}

const rules = {
  mathFunctionName: [
    { required: true, message: 'Выберите Math функцию', trigger: 'change' }
  ],
  xFrom: [
    { required: true, message: 'Введите начало интервала', trigger: 'blur' }
  ],
  xTo: [
    { required: true, message: 'Введите конец интервала', trigger: 'blur' }
  ],
  count: [
    { required: true, message: 'Введите количество точек', trigger: 'blur' }
  ]
}

const handleCreate = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const createdFunction = await functionsStore.createFromMath({
          ...form.value,
          userId: authStore.user.id
        })

        ElMessage.success('Функция создана успешно')
        emit('created', createdFunction)
        visible.value = false
        resetForm()
      } catch (error) {
        ElMessage.error('Ошибка при создании функции: ' + error.message)
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  form.value = {
    mathFunctionName: '',
    xFrom: 0,
    xTo: 10,
    count: 100,
    factoryType: 'array',
    userId: authStore.user?.id
  }
}

// Загружаем математические функции при открытии диалога
watch(visible, (newVal) => {
  if (newVal) {
    functionsStore.loadMathFunctions()
  }
})
</script>

<style scoped>
.function-option {
  padding: 8px 0;
}

.function-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.function-description {
  font-size: 12px;
  color: #666;
}

.function-preview {
  margin-top: 15px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #409eff;
}

.function-expression {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #1890ff;
  margin: 10px 0;
}

.preview-values {
  display: flex;
  gap: 20px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}
</style>