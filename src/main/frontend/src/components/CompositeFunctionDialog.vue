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
            <el-input
              v-model="form.functionName"
              placeholder="Введите название функции"
              :maxlength="100"
            />
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

      <!-- Остальной код без изменений -->
    </el-form>
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
  functionName: '', // Исправлено с customName на functionName
  typeFunction: 'tabular',
  functionExpression: '',
  userId: authStore.user?.id
})

const pointCount = ref(5)
const points = ref([])

const rules = {
  functionName: [
    { required: true, message: 'Введите название функции', trigger: 'blur' },
    { min: 1, max: 100, message: 'Название должно быть от 1 до 100 символов', trigger: 'blur' }
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


const handleCreate = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const createdFunction = await functionsStore.createComposite({
          ...form.value,
          userId: authStore.user.id
        })

        ElMessage.success('Сложная функция создана успешно')
        emit('created', createdFunction)
        visible.value = false
        resetForm()
      } catch (error) {
        ElMessage.error('Ошибка при создании сложной функции: ' + error.message)
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  form.value = {
    baseFunctionName: '',
    outerFunctionName: '',
    customName: '',
    userId: authStore.user?.id
  }
}
</script>