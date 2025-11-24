<template>
  <div class="operations-with-functions">
    <h1>Операции с функциями</h1>
    <el-card class="operations-card">
      <!-- Выбор функций -->
      <div class="function-selection">
        <el-form :model="form" label-width="120px">
          <el-form-item label="Первая функция">
            <el-select v-model="form.function1" placeholder="Выберите функцию" style="width: 100%">
              <el-option
                v-for="func in functions"
                :key="func.id"
                :label="func.name"
                :value="func.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="Вторая функция" v-if="needsSecondFunction">
            <el-select v-model="form.function2" placeholder="Выберите функцию" style="width: 100%">
              <el-option
                v-for="func in functions"
                :key="func.id"
                :label="func.name"
                :value="func.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="Операция">
            <el-select v-model="form.operation" @change="updateOperation" placeholder="Выберите операцию" style="width: 100%">
              <el-option label="Сложение (f + g)" value="add" />
              <el-option label="Вычитание (f - g)" value="subtract" />
              <el-option label="Умножение (f * g)" value="multiply" />
              <el-option label="Деление (f / g)" value="divide" />
              <el-option label="Композиция f(g(x))" value="compose" />
              <el-option label="Дифференцирование f'(x)" value="differentiate" />
            </el-select>
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
              @click="executeOperation"
              :disabled="!canExecute"
              :loading="loading"
            >
              Выполнить операцию
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      <!-- Результат -->
      <div v-if="result" class="result-section">
        <h3>Результат операции:</h3>
        <div class="result-content">
          <p><strong>Тип:</strong> {{ result.type }}</p>
          <p><strong>Название:</strong> {{ result.name }}</p>
          <p><strong>Описание:</strong> {{ result.description }}</p>
          <el-button type="success" @click="saveResult" v-if="result.id">
            Сохранить результат
          </el-button>
        </div>
      </div>

      <!-- Предпросмотр графика -->
      <div v-if="resultPoints && resultPoints.length > 0" class="chart-preview">
        <h3>График результата:</h3>
        <FunctionChart :points="resultPoints" :showSlider="true" />
      </div>
    </el-card>
  </div>
</template>
<script>
import { ref, computed, onMounted } from 'vue';
import { useFunctionsStore } from '@/stores/functions';
import { api } from '@/services/api';
import { ElMessage } from 'element-plus';
import FunctionChart from '@/components/FunctionChart.vue';

export default {
  name: 'OperationsWithFunctions',
  components: {
    FunctionChart
  },
  setup() {
    const functionsStore = useFunctionsStore();
    const loading = ref(false);
    const result = ref(null);
    const resultPoints = ref([]);

    const form = ref({
      function1: '',
      function2: '',
      operation: '',
      factoryType: 'array'
    });

    const functions = computed(() => functionsStore.functions);
    const needsSecondFunction = computed(() => {
      return ['add', 'subtract', 'multiply', 'divide', 'compose'].includes(form.value.operation);
    });

    const canExecute = computed(() => {
      if (!form.value.function1 || !form.value.operation) return false;
      if (needsSecondFunction.value && !form.value.function2) return false;
      return true;
    });

    const updateOperation = () => {
      if (!needsSecondFunction.value) {
        form.value.function2 = '';
      }
    };

    const executeOperation = async () => {
      try {
        loading.value = true;
        result.value = null;
        resultPoints.value = [];

        let operationData = {
          operation: form.value.operation,
          factoryType: form.value.factoryType
        };

        if (needsSecondFunction.value) {
          operationData = {
            ...operationData,
            operand1Id: form.value.function1,
            operand2Id: form.value.function2
          };

          console.log('📤 Выполнение бинарной операции:', operationData);
          const response = await api.post(`/operations/${form.value.operation}`, operationData);
          resultPoints.value = response.data.map(point => ({ x: point.xVal, y: point.yVal }));
        } else {
          operationData = {
            ...operationData,
            operand1Id: form.value.function1
          };

          console.log('📤 Выполнение унарной операции:', operationData);
          const response = await api.post('/operations/differentiate', operationData);
          resultPoints.value = response.data.map(point => ({ x: point.xVal, y: point.yVal }));
        }

        // Создаем описание результата
        const func1 = functions.value.find(f => f.id == form.value.function1);
        const func2 = functions.value.find(f => f.id == form.value.function2);

        let description = '';
        let typeName = '';

        switch (form.value.operation) {
          case 'add':
            description = `Сумма функций ${func1.name} и ${func2.name}`;
            typeName = 'analytic';
            break;
          case 'subtract':
            description = `Разность функций ${func1.name} и ${func2.name}`;
            typeName = 'analytic';
            break;
          case 'multiply':
            description = `Произведение функций ${func1.name} и ${func2.name}`;
            typeName = 'analytic';
            break;
          case 'divide':
            description = `Частное функций ${func1.name} и ${func2.name}`;
            typeName = 'analytic';
            break;
          case 'compose':
            description = `Композиция ${func1.name}(${func2.name}(x))`;
            typeName = 'composite';
            break;
          case 'differentiate':
            description = `Производная функции ${func1.name}`;
            typeName = 'analytic';
            break;
          default:
            description = 'Результат операции';
            typeName = 'analytic';
        }

        result.value = {
          type: typeName,
          name: `Результат ${form.value.operation}`,
          description: description
        };

        ElMessage.success('Операция выполнена успешно!');
      } catch (error) {
        console.error('❌ Ошибка выполнения операции:', error);
        ElMessage.error('Ошибка при выполнении операции: ' + (error.message || 'Неизвестная ошибка'));
      } finally {
        loading.value = false;
      }
    };

    const saveResult = async () => {
      try {
        // Сохранение результата как новой функции
        const functionData = {
          name: result.value.name,
          description: result.value.description,
          typeFunction: result.value.type,
          userId: parseInt(localStorage.getItem('userId') || '1')
        };

        const createdFunction = await functionsStore.createFunction(functionData);

        // Если это табулированная функция, добавляем точки
        if (resultPoints.value.length > 0) {
          for (const point of resultPoints.value) {
            await api.post('/tabulated-points', {
              functionId: createdFunction.id,
              xVal: point.x,
              yVal: point.y
            });
          }
        }

        ElMessage.success('Результат сохранен как новая функция!');
        result.value = null;
        form.value = { function1: '', function2: '', operation: '', factoryType: 'array' };
      } catch (error) {
        console.error('❌ Ошибка сохранения:', error);
        ElMessage.error('Ошибка при сохранении результата: ' + error.message);
      }
    };

    onMounted(async () => {
      await functionsStore.loadFunctions();
      await functionsStore.loadMathFunctions();
    });

    return {
      form,
      loading,
      result,
      resultPoints,
      functions,
      needsSecondFunction,
      canExecute,
      updateOperation,
      executeOperation,
      saveResult
    };
  }
};
</script>
<style scoped>
.operations-with-functions {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.operations-card {
  margin-bottom: 20px;
}
.function-selection {
  margin-bottom: 30px;
}
.result-section {
  margin-top: 30px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
}
.result-section h3 {
  margin-top: 0;
  color: #333;
}
.result-content {
  margin-top: 15px;
}
.result-content p {
  margin: 8px 0;
}
.chart-preview {
  margin-top: 30px;
}
</style>