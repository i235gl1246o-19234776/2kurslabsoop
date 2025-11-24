<template>
  <div class="integral-calculator">
    <h1>Вычисление интегралов</h1>

    <el-card class="calculator-card">
      <el-form :model="form" label-width="200px">
        <el-form-item label="Выберите функцию">
          <el-select v-model="form.functionId" placeholder="Выберите функцию" style="width: 100%">
            <el-option
              v-for="func in functions"
              :key="func.id"
              :label="func.name"
              :value="func.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Интервал интегрирования">
          <div class="interval-inputs">
            <el-input-number
              v-model="form.from"
              :precision="3"
              :step="0.1"
              placeholder="0.0"
            />
            <span class="interval-separator">до</span>
            <el-input-number
              v-model="form.to"
              :precision="3"
              :step="0.1"
              placeholder="1.0"
            />
          </div>
        </el-form-item>

        <el-form-item label="Метод интегрирования">
          <el-select v-model="form.method" placeholder="Выберите метод" style="width: 100%">
            <el-option label="Метод Симпсона" value="simpson" />
            <el-option label="Метод трапеций" value="trapezoidal" />
            <el-option label="Метод прямоугольников" value="rectangle" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="calculateIntegral"
            :disabled="!canCalculate"
            :loading="loading"
          >
            Вычислить интеграл
          </el-button>
        </el-form-item>
      </el-form>

      <!-- Результат -->
      <div v-if="result !== null" class="result-section">
        <h3>Результат вычисления:</h3>
        <div class="result-content">
          <p class="integral-formula">
            ∫<sub>{{ form.from }}</sub><sup>{{ form.to }}</sup> f(x) dx
          </p>
          <p class="result-value">= {{ result }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useFunctionsStore } from '@/stores/functions';
import { ElMessage } from 'element-plus';

export default {
  name: 'IntegralCalculator',
  setup() {
    const functionsStore = useFunctionsStore();
    const loading = ref(false);
    const result = ref(null);

    const form = ref({
      functionId: '',
      from: 0,
      to: 1,
      method: 'simpson'
    });

    const functions = computed(() => functionsStore.functions);

    const canCalculate = computed(() => {
      return form.value.functionId &&
             form.value.from !== null &&
             form.value.to !== null &&
             form.value.from < form.value.to;
    });

    const calculateIntegral = async () => {
      try {
        loading.value = true;
        result.value = null;

        const integralData = {
          functionId: form.value.functionId,
          from: form.value.from,
          to: form.value.to,
          method: form.value.method
        };

        console.log('📤 Вычисление интеграла:', integralData);

        // Здесь должен быть вызов API для вычисления интеграла
        // Пока используем мок-данные
        const mockResult = Math.random() * 10;
        result.value = mockResult.toFixed(6);

        ElMessage.success('Интеграл вычислен успешно!');

      } catch (error) {
        console.error('❌ Ошибка вычисления интеграла:', error);
        ElMessage.error('Ошибка при вычислении интеграла: ' + (error.message || 'Неизвестная ошибка'));
      } finally {
        loading.value = false;
      }
    };

    onMounted(async () => {
      await functionsStore.loadFunctions();
    });

    return {
      form,
      loading,
      result,
      functions,
      canCalculate,
      calculateIntegral
    };
  }
};
</script>

<style scoped>
.integral-calculator {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.calculator-card {
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

.result-section {
  margin-top: 30px;
  padding: 20px;
  background: #f0f9ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
}

.result-section h3 {
  margin-top: 0;
  color: #1890ff;
}

.integral-formula {
  font-size: 18px;
  font-weight: bold;
  margin: 10px 0;
}

.result-value {
  font-size: 24px;
  font-weight: bold;
  color: #52c41a;
  margin: 10px 0;
}
</style>