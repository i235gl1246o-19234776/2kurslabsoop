<template>
  <div>
    <h1>Добро пожаловать в приложение функций</h1>
    <!-- Кнопки для открытия диалогов -->
    <div class="main-buttons">
      <button @click="openCreateDialog">Создать табулированную функцию (X/Y)</button>
      <button @click="openCreateFromMathDialog">Создать табулированную функцию (Math)</button>
      <button @click="openOperationsDialog">Операции</button>
      <button @click="openDifferentiationDialog">Дифференцирование</button>
      <button @click="openIntegrationDialog">Интегрирование</button>
      <button @click="openGraphDialog">График функции</button>
      <button @click="openCompositeFunctionDialog">Создать сложную функцию</button>
      <button @click="openSettingsDialog">Настройки</button>
    </div>
    <!-- Существующие диалоги -->
    <CreateTabulatedFunctionDialog
      :is-open="isCreateDialogOpen"
      :current-user-id="currentUserId"
      @close="closeCreateDialog"
      @function-created="onFunctionCreatedFromXY"
      @error="onError"
    />
    <CreateTabulatedFunctionFromMathDialog
      :is-open="isCreateFromMathDialogOpen"
      :current-user-id="currentUserId"
      @close="closeCreateFromMathDialog"
      @function-created="onFunctionCreatedFromMath"
      @error="onError"
    />
    <OperationsWindow
      :is-open="isOperationsDialogOpen"
      :current-user-id="currentUserId"
      @close="closeOperationsDialog"
      @error="onError"
    />
    <DifferentiationWindow
      :is-open="isDifferentiationDialogOpen"
      :current-user-id="currentUserId"
      @close="closeDifferentiationDialog"
      @error="onError"
    />
    <!-- Новые диалоги -->
    <IntegrationWindow
      :is-open="isIntegrationDialogOpen"
      :current-user-id="currentUserId"
      @close="closeIntegrationDialog"
      @error="onError"
    />
    <FunctionGraphDialog
      :is-open="isGraphDialogOpen"
      :current-user-id="currentUserId"
      @close="closeGraphDialog"
      @error="onError"
    />
    <CreateCompositeFunctionDialog
      :is-open="isCompositeFunctionDialogOpen"
      :current-user-id="currentUserId"
      @close="closeCompositeFunctionDialog"
      @composite-function-created="onCompositeFunctionCreated"
      @error="onError"
    />
    <SettingsDialog
      :is-open="isSettingsDialogOpen"
      @close="closeSettingsDialog"
      @error="onError"
    />
  </div>
</template>
<script>
import CreateTabulatedFunctionDialog from './CreateTabulatedFunctionDialog.vue';
import CreateTabulatedFunctionFromMathDialog from './CreateTabulatedFunctionFromMathDialog.vue';
import OperationsWindow from './OperationsWindow.vue';
import DifferentiationWindow from './DifferentiationWindow.vue';
import IntegrationWindow from './IntegrationWindow.vue';
import FunctionGraphDialog from './FunctionGraphDialog.vue';
import CreateCompositeFunctionDialog from './CreateCompositeFunctionDialog.vue';
import SettingsDialog from './SettingsDialog.vue';
import * as api from '@/api.js';

export default {
  name: 'HomeView',
  components: {
    CreateTabulatedFunctionDialog,
    CreateTabulatedFunctionFromMathDialog,
    OperationsWindow,
    DifferentiationWindow,
    IntegrationWindow,
    FunctionGraphDialog,
    CreateCompositeFunctionDialog,
    SettingsDialog
  },
  props: {
    currentUserId: {
      type: Number,
      required: true,
    }
  },
  data() {
    return {
      isCreateDialogOpen: false,
      isCreateFromMathDialogOpen: false,
      isOperationsDialogOpen: false,
      isDifferentiationDialogOpen: false,
      isIntegrationDialogOpen: false,
      isGraphDialogOpen: false,
      isCompositeFunctionDialogOpen: false,
      isSettingsDialogOpen: false
    };
  },
  methods: {
    // Методы для диалога создания через X/Y
    openCreateDialog() {
      this.isCreateDialogOpen = true;
    },
    closeCreateDialog() {
      this.isCreateDialogOpen = false;
    },
    onFunctionCreatedFromXY(creationData) {
      console.log('HomeView: onFunctionCreatedFromXY вызван с данными:', creationData);
      this.closeCreateDialog();
      this.$emit('function-created', creationData);
    },

    // Методы для диалога создания через Math
    openCreateFromMathDialog() {
      this.isCreateFromMathDialogOpen = true;
    },
    closeCreateFromMathDialog() {
      this.isCreateFromMathDialogOpen = false;
    },
    onFunctionCreatedFromMath(creationData) {
      console.log('HomeView: onFunctionCreatedFromMath вызван с данными:', creationData);
      this.closeCreateFromMathDialog();
      this.$emit('function-created', creationData);
    },

    // Методы для окна операций
    openOperationsDialog() {
      this.isOperationsDialogOpen = true;
    },
    closeOperationsDialog() {
      this.isOperationsDialogOpen = false;
    },

    // Методы для окна дифференцирования
    openDifferentiationDialog() {
      this.isDifferentiationDialogOpen = true;
    },
    closeDifferentiationDialog() {
      this.isDifferentiationDialogOpen = false;
    },

    // Методы для окна интегрирования
    openIntegrationDialog() {
      this.isIntegrationDialogOpen = true;
    },
    closeIntegrationDialog() {
      this.isIntegrationDialogOpen = false;
    },

    // Методы для окна графика
    openGraphDialog() {
      this.isGraphDialogOpen = true;
    },
    closeGraphDialog() {
      this.isGraphDialogOpen = false;
    },

    // Методы для создания сложной функции
    openCompositeFunctionDialog() {
      this.isCompositeFunctionDialogOpen = true;
    },
    closeCompositeFunctionDialog() {
      this.isCompositeFunctionDialogOpen = false;
    },
    onCompositeFunctionCreated(createdFunction) {
      console.log('HomeView: onCompositeFunctionCreated вызван с данными:', createdFunction);
      this.closeCompositeFunctionDialog();
      try {
        // Здесь можно обновить список доступных функций
        alert(`Сложная функция "${createdFunction.displayName}" готова к использованию при создании табулированных функций!`);
      } catch (error) {
        console.error('Ошибка при обработке созданной сложной функции:', error);
        this.onError(`Ошибка при обработке сложной функции: ${error.message}`);
      }
    },

    // Методы для окна настроек
    openSettingsDialog() {
      this.isSettingsDialogOpen = true;
    },
    closeSettingsDialog() {
      this.isSettingsDialogOpen = false;
    },

    // Общий обработчик ошибок
    onError(errorMessage) {
      console.error('HomeView error:', errorMessage);
      alert(`Ошибка: ${errorMessage}`);
    }
  },
  mounted() {
    // Устанавливаем фабрику по умолчанию при загрузке компонента
    if (!localStorage.getItem('selectedTabulatedFunctionFactory')) {
      localStorage.setItem('selectedTabulatedFunctionFactory', 'array');
      console.log('Установлена фабрика по умолчанию: array');
    }
  }
};
</script>
<style scoped>
.main-buttons {
  margin: 20px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}
.main-buttons button {
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  white-space: nowrap;
}
.main-buttons button:hover {
  background-color: #0056b3;
}
.main-buttons button:active {
  transform: translateY(1px);
}
</style>