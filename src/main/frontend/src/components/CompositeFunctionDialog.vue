<template>
  <el-dialog
    v-model="visible"
    title="Создание сложной функции"
    width="600px"
    :close-on-click-modal="false"
  >
    <el-form :model="form" :rules="rules" ref="formRef">
      <el-form-item label="Базовая функция" prop="baseFunctionName">
        <el-select v-model="form.baseFunctionName" placeholder="Выберите базовую функцию" style="width: 100%">
          <el-option
            v-for="name in functionsStore.mathFunctions"
            :key="'base_' + name"
            :label="name"
            :value="name"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="Внешняя функция" prop="outerFunctionName">
        <el-select v-model="form.outerFunctionName" placeholder="Выберите внешнюю функцию" style="width: 100%">
          <el-option
            v-for="name in functionsStore.mathFunctions"
            :key="'outer_' + name"
            :label="name"
            :value="name"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="Пользовательское имя" prop="customName">
        <el-input v-model="form.customName" placeholder="Введите имя для сложной функции" />
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
import { ref, computed } from 'vue'
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
  baseFunctionName: '',
  outerFunctionName: '',
  customName: '',
  userId: authStore.user?.id
})

const rules = {
  baseFunctionName: [
    { required: true, message: 'Выберите базовую функцию', trigger: 'change' }
  ],
  outerFunctionName: [
    { required: true, message: 'Выберите внешнюю функцию', trigger: 'change' }
  ],
  customName: [
    { required: true, message: 'Введите имя для сложной функции', trigger: 'blur' }
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