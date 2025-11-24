<template>
  <div class="composite-function-creator">
    <h1>Создание композитной функции</h1>
    <p class="subtitle">Композитная функция: f(g(x)) - одна функция внутри другой</p>

    <el-card class="form-card">
      <el-form :model="form" label-width="200px">
        <el-form-item label="Название функции" prop="name">
          <el-input
            v-model="form.name"
            placeholder="Введите название композитной функции"
          />
        </el-form-item>

        <el-form-item label="Описание">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="Введите описание композитной функции"
            :rows="3"
          />
        </el-form-item>

        <!-- Выбор внешней функции -->
        <el-form-item label="Внешняя функция (f)">
          <el-select
            v-model="form.outerFunctionId"
            placeholder="Выберите внешнюю функцию"
            style="width: 100%"
            @change="updatePreview"
          >
            <el-option
              v-for="func in availableFunctions"
              :key="func.id"
              :label="func.name"
              :value="func.id"
            />
          </el-select>
        </el-form-item>

        <!-- Выбор внутренней функции -->
        <el-form-item label="Внутренняя функция (g)">
          <el-select
            v-model="form.innerFunctionId"
            placeholder="Выберите внутреннюю функцию"
            style="width: 100%"
            @change="updatePreview"
          >
            <el-option
              v-for="func in availableFunctions"
              :key="func.id"
              :label="func.name"
              :value="func.id"
            />
          </el-select>
        </el-form-item>

        <!-- Предпросмотр -->
        <div class="preview-section">
          <h4>Предпросмотр композитной функции:</h4>
          <div v-if="form.outerFunctionId && form.innerFunctionId" class="preview-content">
            <p class="function-formula">
              <span class="outer-func">{{ getFunctionName(form.outerFunctionId) }}</span>
              (<span class="inner-func">{{ getFunctionName(form.innerFunctionId) }}</span>(x))
            </p>
            <p class="preview-text">f(g(x)) = {{ getFunctionName(form.outerFunctionId) }}({{ getFunctionName(form.innerFunctionId) }}(x))</p>
          </div>
          <div v-else class="preview-placeholder">
            Выберите внешнюю и внутреннюю функции для предпросмотра
          </div>
        </div>

        <!-- Техническое имя -->
        <el-form-item label="Техническое имя">
          <el-input
            v-model="form.technicalName"
            placeholder="Введите техническое имя (только латинские буквы, цифры и _)"
          />
          <div class="hint">Используется для внутренних вычислений</div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="createCompositeFunction"
            :loading="loading"
            :disabled="!canCreate"
          >
            Создать композитную функцию
          </el-button>
          <el-button @click="resetForm">Сбросить</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useFunctionsStore } from '@/stores/functions';
import { ElMessage } from 'element-plus';

export default {
  name: 'CompositeFunctionCreator',
  setup() {
    const functionsStore = useFunctionsStore();
    const loading = ref(false);
    const availableFunctions = ref([]);

    const form = ref({
      name: '',
      technicalName: '',
      description: '',
      outerFunctionId: '',
      innerFunctionId: ''
    });

    // Проверка возможности создания
    const canCreate = computed(() => {
      return form.value.name &&
             form.value.outerFunctionId &&
             form.value.innerFunctionId &&
             form.value.outerFunctionId !== form.value.innerFunctionId;
    });

    const loadAvailableFunctions = async () => {
      try {
        await functionsStore.loadFunctions();
        availableFunctions.value = functionsStore.functions || [];
        console.log('✅ Функции загружены для композиции:', availableFunctions.value.length);
      } catch (error) {
        console.error('❌ Ошибка загрузки функций:', error);
        ElMessage.error('Ошибка загрузки функций');
      }
    };

    const getFunctionName = (functionId) => {
      const func = availableFunctions.value.find(f => f.id === functionId);
      return func ? func.name : '?';
    };

    const updatePreview = () => {
      // Логика обновления предпросмотра
      if (form.value.outerFunctionId && form.value.innerFunctionId) {
        console.log('Предпросмотр:', {
          outer: getFunctionName(form.value.outerFunctionId),
          inner: getFunctionName(form.value.innerFunctionId)
        });
      }
    };

    const generateTechnicalName = () => {
      const outer = getFunctionName(form.value.outerFunctionId).replace(/[^a-zA-Z0-9]/g, '_');
      const inner = getFunctionName(form.value.innerFunctionId).replace(/[^a-zA-Z0-9]/g, '_');
      return `composite_${outer}_${inner}`.toLowerCase();
    };

    const createCompositeFunction = async () => {
      try {
        loading.value = true;

        // Валидация
        if (!form.value.name) {
          ElMessage.error('Введите название функции');
          return;
        }

        if (!form.value.outerFunctionId || !form.value.innerFunctionId) {
          ElMessage.error('Выберите внешнюю и внутреннюю функции');
          return;
        }

        if (form.value.outerFunctionId === form.value.innerFunctionId) {
          ElMessage.error('Внешняя и внутренняя функции должны быть разными');
          return;
        }

        // Генерируем техническое имя, если не задано
        if (!form.value.technicalName) {
          form.value.technicalName = generateTechnicalName();
        }

        // Проверяем техническое имя
        if (!/^[a-zA-Z0-9_]+$/.test(form.value.technicalName)) {
          ElMessage.error('Техническое имя может содержать только латинские буквы, цифры и подчеркивания');
          return;
        }

        // Получаем userId
        const userId = localStorage.getItem('userId') || 34;

        // Подготавливаем данные для композитной функции
        const compositeData = {
          functionName: form.value.name,
          technicalName: form.value.technicalName,
          typeFunction: 'composite',
          userId: parseInt(userId),
          outerFunction: form.value.outerFunctionId,
          innerFunction: form.value.innerFunctionId,
          functionExpression: `${getFunctionName(form.value.outerFunctionId)}(${getFunctionName(form.value.innerFunctionId)}(x))`
        };

        if (form.value.description) {
          compositeData.description = form.value.description;
        }

        console.log('📤 Создание композитной функции:', compositeData);

        // Используем метод создания композитной функции из store
        await functionsStore.createComposite(compositeData);

        ElMessage.success('Композитная функция создана успешно!');
        resetForm();

      } catch (error) {
        console.error('❌ Ошибка создания композитной функции:', error);
        ElMessage.error('Ошибка при создании композитной функции: ' + (error.message || 'Неизвестная ошибка'));
      } finally {
        loading.value = false;
      }
    };

    const resetForm = () => {
      form.value = {
        name: '',
        technicalName: '',
        description: '',
        outerFunctionId: '',
        innerFunctionId: ''
      };
    };

    onMounted(() => {
      loadAvailableFunctions();
    });

    return {
      form,
      loading,
      availableFunctions,
      canCreate,
      getFunctionName,
      updatePreview,
      createCompositeFunction,
      resetForm
    };
  }
};
</script>

<style scoped>
.composite-function-creator {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.subtitle {
  color: #666;
  text-align: center;
  margin-bottom: 30px;
  font-style: italic;
}

.form-card {
  margin-bottom: 20px;
}

.preview-section {
  margin: 20px 0;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #1890ff;
}

.preview-content {
  margin-top: 10px;
}

.function-formula {
  font-family: 'Courier New', monospace;
  font-size: 18px;
  font-weight: bold;
  color: #1890ff;
  text-align: center;
  margin: 10px 0;
}

.outer-func {
  color: #e91e63;
  font-weight: bold;
}

.inner-func {
  color: #2196f3;
  font-weight: bold;
}

.preview-text {
  text-align: center;
  color: #666;
  font-style: italic;
  margin-top: 10px;
}

.preview-placeholder {
  color: #999;
  font-style: italic;
  text-align: center;
}

.hint {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

/* Анимация для предпросмотра */
.preview-content {
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>