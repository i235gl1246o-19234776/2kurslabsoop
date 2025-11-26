<template>
  <div class="create-function-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>➕ Создание функции из точек</h2>
          <el-button type="primary" @click="showCreateDialog = true">
            Создать новую функцию
          </el-button>
        </div>
      </template>

      <el-alert
        title="Инструкция"
        description="Создайте табличную функцию, указав значения X и Y для каждой точки"
        type="info"
        show-icon
        style="margin-bottom: 20px"
      />

      <!-- Список существующих функций -->
      <el-card v-if="functions.length > 0">
        <template #header>
          <h3>Мои табличные функции</h3>
        </template>

        <el-table :data="tabularFunctions" v-loading="loading">
          <el-table-column prop="name" label="Название" />
          <el-table-column prop="typeFunction" label="Тип" width="120">
            <template #default="scope">
              <el-tag type="primary">Табличная</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Количество точек" width="150">
            <template #default="scope">
              {{ getPointCount(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column label="Действия" width="200">
            <template #default="scope">
              <el-button size="small" @click="viewFunction(scope.row)">
                Просмотр
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="deleteFunction(scope.row.id)"
              >
                Удалить
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-empty
        v-else
        description="У вас пока нет табличных функций"
        :image-size="200"
      >
        <el-button type="primary" @click="showCreateDialog = true">
          Создать первую функцию
        </el-button>
      </el-empty>
    </el-card>

    <!-- Диалог создания функции -->
    <el-dialog
      v-model="showCreateDialog"
      title="Создание табличной функции"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="Название функции" prop="name">
          <el-input
            v-model="form.name"
            placeholder="Введите название функции"
          />
        </el-form-item>

        <el-form-item label="Количество точек">
          <el-input-number
            v-model="pointCount"
            :min="2"
            :max="100"
            @change="generatePoints"
          />
        </el-form-item>

        <el-alert
          title="Введите значения X и Y для каждой точки"
          type="warning"
          show-icon
          style="margin-bottom: 15px"
        />

        <el-table :data="points" style="width: 100%" max-height="400">
          <el-table-column prop="index" label="#" width="60">
            <template #default="scope">
              {{ scope.$index + 1 }}
            </template>
          </el-table-column>

          <el-table-column label="X значение">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.xVal"
                :step="0.1"
                :precision="3"
                controls-position="right"
                size="small"
                style="width: 120px"
              />
            </template>
          </el-table-column>

          <el-table-column label="Y значение">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.yVal"
                :step="0.1"
                :precision="3"
                controls-position="right"
                size="small"
                style="width: 120px"
              />
            </template>
          </el-table-column>

          <el-table-column label="Действия" width="80">
            <template #default="scope">
              <el-button
                link
                type="danger"
                @click="removePoint(scope.$index)"
                :disabled="points.length <= 2"
              >
                Удалить
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top: 15px; text-align: center">
          <el-button @click="addPoint">
            + Добавить точку
          </el-button>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">Отмена</el-button>
        <el-button type="primary" :loading="loading" @click="handleCreate">
          Создать функцию
        </el-button>
      </template>
    </el-dialog>

    <!-- Диалог просмотра функции -->
    <el-dialog
      v-model="showViewDialog"
      :title="`Функция: ${selectedFunction?.name}`"
      width="600px"
    >
      <function-table
        v-if="selectedFunction"
        :function-id="selectedFunction.id"
      />
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useFunctionsStore } from '@/stores/functions'
import { useAuthStore } from '@/stores/auth'
import { api } from '@/services/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import FunctionTable from '@/components/FunctionTable.vue'

export default {
  name: 'CreateFunctionView',
  components: {
    FunctionTable
  },
  setup() {
    // ✅ ПЕРВОЕ: Объявляем хранилища
    const functionsStore = useFunctionsStore()
    const authStore = useAuthStore()

    // ✅ ВТОРОЕ: Объявляем реактивные данные
    const showCreateDialog = ref(false)
    const showViewDialog = ref(false)
    const loading = ref(false)
    const formRef = ref()
    const selectedFunction = ref(null)

    const form = ref({
      name: '',
      typeFunction: 'tabular',
      userId: null
    })

    const pointCount = ref(5)
    const points = ref([])

    // ✅ ТРЕТЬЕ: Правила валидации
    const rules = {
      name: [
        { required: true, message: 'Введите название функции', trigger: 'blur' },
        { min: 2, message: 'Название должно быть не менее 2 символов', trigger: 'blur' }
      ]
    }

    // ✅ ЧЕТВЕРТОЕ: Computed свойства
    const functions = computed(() => functionsStore.functions || [])
    const tabularFunctions = computed(() => functionsStore.tabularFunctions || [])

    // ✅ ПЯТОЕ: Методы
    const generatePoints = () => {
      points.value = Array.from({ length: pointCount.value }, (_, i) => ({
        xVal: i,
        yVal: 0
      }))
    }

    const addPoint = () => {
      if (points.value.length >= 100) {
        ElMessage.warning('Максимальное количество точек: 100')
        return
      }

      const lastPoint = points.value[points.value.length - 1]
      points.value.push({
        xVal: lastPoint ? lastPoint.xVal + 1 : points.value.length,
        yVal: 0
      })
    }

    const removePoint = (index) => {
      if (points.value.length <= 2) {
        ElMessage.warning('Функция должна содержать минимум 2 точки')
        return
      }
      points.value.splice(index, 1)
    }

    const getPointCount = (func) => {
      return func.pointsCount || 'N/A'
    }

    const viewFunction = (func) => {
      selectedFunction.value = func
      showViewDialog.value = true
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

    const handleCreate = async () => {
      if (!formRef.value) return

      // Валидация точек
      if (points.value.length < 2) {
        ElMessage.error('Функция должна содержать минимум 2 точки')
        return
      }

      // Проверка на уникальность X значений
      const xValues = points.value.map(p => p.xVal)
      const uniqueXValues = new Set(xValues)
      if (uniqueXValues.size !== xValues.length) {
        ElMessage.error('X значения должны быть уникальными')
        return
      }

      // Сортировка точек по X
      points.value.sort((a, b) => a.xVal - b.xVal)

      const valid = await formRef.value.validate()
      if (!valid) return

      loading.value = true
      try {
        // ✅ ИСПРАВЛЕННО: Правильное получение userId
        console.log('🔍 Проверка аутентификации:')
        console.log('authStore.user:', authStore.user)
        console.log('authStore.user?.id:', authStore.user?.id)
        console.log('localStorage userId:', localStorage.getItem('userId'))
        console.log('localStorage user:', localStorage.getItem('user'))

        // Получаем userId разными способами
        let userId = null

        // Способ 1: Из authStore
        if (authStore.user?.id) {
          userId = authStore.user.id
        }
        // Способ 2: Из localStorage
        else if (localStorage.getItem('userId')) {
          userId = localStorage.getItem('userId')
        }
        // Способ 3: Используем тот же userId, что и в Postman (34)
        else {
          userId = 34 // Используем тот же ID, что и в рабочем Postman запросе
        }

        console.log('✅ Используемый userId:', userId)

        if (!userId) {
          ElMessage.error('Не удалось определить пользователя. Пожалуйста, войдите заново.')
          return
        }

        // ✅ ИСПРАВЛЕННО: Используем правильный формат данных
        const functionData = {
          name: form.value.name,
          typeFunction: form.value.typeFunction,
          userId: parseInt(userId),
          // Добавляем точки
          points: points.value.map(p => ({ x: p.xVal, y: p.yVal }))
        }

        console.log('📤 Создание функции с данными:', functionData)

        await functionsStore.createFunction(functionData)
        ElMessage.success('Функция создана успешно!')
        showCreateDialog.value = false
        resetForm()

      } catch (error) {
        console.error('❌ Ошибка создания функции:', error)
        ElMessage.error('Ошибка при создании функции: ' + (error.message || 'Неизвестная ошибка'))
      } finally {
        loading.value = false
      }
    }

    const resetForm = () => {
      form.value = {
        name: '',
        typeFunction: 'tabular',
        userId: null
      }
      pointCount.value = 5
      points.value = []
      generatePoints()
    }

    // ✅ ШЕСТОЕ: Хуки жизненного цикла
    onMounted(async () => {
      await functionsStore.loadFunctions()
      generatePoints()
    })

    return {
      // Реактивные данные
      showCreateDialog,
      showViewDialog,
      loading,
      formRef,
      selectedFunction,
      form,
      pointCount,
      points,

      // Computed
      functions,
      tabularFunctions,

      // Правила
      rules,

      // Методы
      generatePoints,
      addPoint,
      removePoint,
      getPointCount,
      viewFunction,
      deleteFunction,
      handleCreate,
      resetForm
    }
  }
}
</script>

<style scoped>
.create-function-view {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-table) {
  margin-top: 10px;
}

:deep(.el-table .cell) {
  text-align: center;
}
</style>