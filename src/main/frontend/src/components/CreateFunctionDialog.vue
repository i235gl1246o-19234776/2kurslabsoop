<template>
  <el-dialog
    v-model="visible"
    title="Создание функции из точек"
    width="800px"
    :close-on-click-modal="false"
  >
    <el-form :model="form" :rules="rules" ref="formRef">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="Название функции" prop="functionName">
            <el-input v-model="form.functionName" placeholder="Введите название" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="Тип функции" prop="typeFunction">
            <el-select v-model="form.typeFunction" placeholder="Выберите тип">
              <el-option label="Табличная" value="tabular" />
              <el-option label="Аналитическая" value="analytic" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item v-if="form.typeFunction === 'analytic'" label="Выражение функции" prop="functionExpression">
        <el-input v-model="form.functionExpression" placeholder="Например: x^2 + 2*x + 1" />
      </el-form-item>

      <div v-if="form.typeFunction === 'tabular'">
        <el-form-item label="Количество точек">
          <el-input-number
            v-model="pointCount"
            :min="2"
            :max="1000"
            @change="generatePoints"
          />
        </el-form-item>

        <el-table :data="points" style="width: 100%" max-height="300">
          <el-table-column prop="index" label="#" width="60">
            <template #default="scope">
              {{ scope.$index + 1 }}
            </template>
          </el-table-column>

          <el-table-column label="X">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.xVal"
                :step="0.1"
                controls-position="right"
                size="small"
              />
            </template>
          </el-table-column>

          <el-table-column label="Y">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.yVal"
                :step="0.1"
                controls-position="right"
                size="small"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
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
  functionName: '',
  typeFunction: 'tabular',
  functionExpression: '',
  userId: authStore.user?.id
})

const pointCount = ref(5)
const points = ref([])

const rules = {
  functionName: [
    { required: true, message: 'Введите название функции', trigger: 'blur' }
  ],
  typeFunction: [
    { required: true, message: 'Выберите тип функции', trigger: 'change' }
  ],
  functionExpression: [
    {
      required: true,
      message: 'Введите выражение функции',
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (form.value.typeFunction === 'analytic' && !value) {
          callback(new Error('Введите выражение функции'))
        } else {
          callback()
        }
      }
    }
  ]
}

const generatePoints = () => {
  points.value = Array.from({ length: pointCount.value }, (_, i) => ({
    xVal: i,
    yVal: 0
  }))
}

watch(() => form.value.typeFunction, (newVal) => {
  if (newVal === 'tabular') {
    generatePoints()
  }
})

watch(visible, (newVal) => {
  if (newVal) {
    form.value.userId = authStore.user?.id
    if (form.value.typeFunction === 'tabular') {
      generatePoints()
    }
  }
})

const handleCreate = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const functionData = {
          ...form.value,
          userId: authStore.user.id
        }

        const createdFunction = await functionsStore.createFunction(functionData)

        // Для табличной функции создаем точки
        if (form.value.typeFunction === 'tabular' && points.value.length > 0) {
          for (const point of points.value) {
            await api.post('/tabulated-points', {
              functionId: createdFunction.id,
              xVal: point.xVal,
              yVal: point.yVal
            })
          }
        }

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
    functionName: '',
    typeFunction: 'tabular',
    functionExpression: '',
    userId: authStore.user?.id
  }
  pointCount.value = 5
  points.value = []
}
</script>