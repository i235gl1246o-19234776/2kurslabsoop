<template>
  <div class="create-from-math">
    <h1>Создание функции из Math функции</h1>
    <el-card class="form-card">
      <el-form :model="form" label-width="200px">
        <!-- Выбор математической функции -->
        <el-form-item label="Math функция">
          <el-select
            v-model="form.mathFunctionName"
            placeholder="Выберите математическую функцию"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="func in mathFunctions"
              :key="func.name"
              :label="getFunctionDisplayName(func.name)"
              :value="func.name"
            />
          </el-select>
        </el-form-item>
        <!-- Интервал -->
        <el-form-item label="Интервал [xFrom, xTo]">
          <div class="interval-inputs">
            <el-input-number
              v-model="form.xFrom"
              :precision="3"
              :step="0.1"
              placeholder="0.000"
            />
            <span class="interval-separator">до</span>
            <el-input-number
              v-model="form.xTo"
              :precision="3"
              :step="0.1"
              placeholder="10.000"
            />
          </div>
        </el-form-item>
        <!-- Количество точек -->
        <el-form-item label="Количество точек">
          <el-input-number
            v-model="form.count"
            :min="2"
            :max="1000"
            placeholder="100"
          />
        </el-form-item>
        <!-- Тип фабрики -->
        <el-form-item label="Тип фабрики">
          <el-radio-group v-model="form.factoryType">
            <el-radio label="array">Массив</el-radio>
            <el-radio label="linkedlist">Связный список</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- Предпросмотр -->
        <el-form-item label="Предпросмотр">
          <div class="preview">
            <p><strong>Функция:</strong> {{ getFunctionExpression(form.mathFunctionName) }}</p>
            <p><strong>Интервал:</strong> [{{ form.xFrom }}, {{ form.xTo }}]</p>
            <p><strong>Точек:</strong> {{ form.count }}</p>
          </div>
        </el-form-item>
        <!-- Кнопки -->
        <el-form-item>
          <el-button type="primary" @click="createFunction" :loading="loading">
            Создать функцию
          </el-button>
          <el-button @click="resetForm">Сбросить</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <!-- Отладочная информация -->
    <div class="debug-info" v-if="debugMode">
      <h3>Отладочная информация:</h3>
      <p>Загружено math функций: {{ mathFunctions.length }}</p>
      <pre>{{ mathFunctions }}</pre>
    </div>
  </div>
</template>
<script>
import { ref, onMounted, computed } from 'vue'
import { useFunctionsStore } from '@/stores/functions'
import { ElMessage } from 'element-plus'

export default {
  name: 'CreateFromMathView',
  setup() {
    const functionsStore = useFunctionsStore()
    const loading = ref(false)
    const debugMode = ref(true) // Включите для отладки
    const form = ref({
      mathFunctionName: '',
      xFrom: 0,
      xTo: 10,
      count: 100,
      factoryType: 'array'
    })

    const mathFunctions = computed(() => functionsStore.mathFunctions)

    const getFunctionDisplayName = (funcName) => {
      return functionsStore.getMathFunctionDisplayName(funcName)
    }

    const getFunctionExpression = (funcName) => {
      return functionsStore.getMathFunctionExpression(funcName)
    }

    const createFunction = async () => {
      try {
        loading.value = true

        // Валидация
        const errors = functionsStore.validateMathFunctionData(form.value)
        if (errors.length > 0) {
          ElMessage.error(errors.join(', '))
          return
        }

        // Добавляем userId
        const userId = localStorage.getItem('userId') || 1
        const functionData = {
          ...form.value,
          userId: parseInt(userId),
          name: `Функция ${form.value.mathFunctionName}`
        }

        console.log('📤 Отправка данных:', functionData)
        await functionsStore.createFromMath(functionData)
        ElMessage.success('Функция создана успешно!')
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
        mathFunctionName: '',
        xFrom: 0,
        xTo: 10,
        count: 100,
        factoryType: 'array'
      }
    }

    // Загружаем математические функции при монтировании
    onMounted(async () => {
      console.log('🔄 Загрузка математических функций...')
      await functionsStore.loadMathFunctions()
      console.log('✅ Math функции загружены:', functionsStore.mathFunctions)
    })

    return {
      form,
      loading,
      debugMode,
      mathFunctions,
      getFunctionDisplayName,
      getFunctionExpression,
      createFunction,
      resetForm
    }
  }
}
</script>
<style scoped>
.create-from-math {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.form-card {
  margin-bottom: 20px;
}
.interval-inputs {
  display: flex;
  align-items: center;
  gap: 10px;
}
.interval-separator {
  margin: 0 10px;
  color: #666;
}
.preview {
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
}
.preview p {
  margin: 5px 0;
}
.debug-info {
  margin-top: 20px;
  padding: 15px;
  background: #fff2e8;
  border: 1px solid #ffbb96;
  border-radius: 4px;
}
.debug-info h3 {
  margin: 0 0 10px 0;
  color: #d46b08;
}
.debug-info pre {
  background: white;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
}
</style>