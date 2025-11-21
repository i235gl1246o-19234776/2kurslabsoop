<!-- src/main/frontend/src/App.vue -->
<template>
  <div id="app">
    <h1>Приложение табулированных функций</h1>
    <p v-if="isAuthenticated">Текущий пользователь ID: {{ currentUserId }}</p>
    <p v-else>Пользователь не аутентифицирован</p>

    <div class="main-buttons">
      <button @click="showCreateDialog = true">Создать табулированную функцию (из X/Y)</button>
      <button @click="showCreateFromMathDialog = true">Создать табулированную функцию (из MathFunction)</button>
      <button @click="showSettingsDialog = true">Настройки</button>
    </div>

    <!-- Диалог создания из X/Y -->
    <CreateTabulatedFunctionDialog
      v-if="showCreateDialog"
      :is-open="showCreateDialog"
      @close="showCreateDialog = false"
      @function-created="onFunctionCreatedFromXY"
    ></CreateTabulatedFunctionDialog> <!-- Явно добавлен закрывающий тег -->

    <!-- Диалог создания из MathFunction -->
    <CreateTabulatedFunctionFromMathDialog
      v-if="showCreateFromMathDialog"
      :is-open="showCreateFromMathDialog"
      :current-user-id="currentUserId"
      @close="showCreateFromMathDialog = false"
      @function-created="onFunctionCreatedFromMath"
      @error="showError"
    ></CreateTabulatedFunctionFromMathDialog> <!-- Явно добавлен закрывающий тег -->

    <!-- Диалог настроек -->
    <SettingsDialog
      v-if="showSettingsDialog"
      :is-open="showSettingsDialog"
      @close="showSettingsDialog = false"
    ></SettingsDialog> <!-- Явно добавлен закрывающий тег -->

    <!-- Общий модальный компонент для ошибок -->
    <ErrorModal
      :is-visible="showErrorModal"
      :message="errorMessage"
      @close="closeErrorModal"
    ></ErrorModal> <!-- Явно добавлен закрывающий тег -->
  </div>
</template>

<script>
import CreateTabulatedFunctionDialog from './components/CreateTabulatedFunctionDialog.vue';
import CreateTabulatedFunctionFromMathDialog from './components/CreateTabulatedFunctionFromMathDialog.vue';
import SettingsDialog from './components/SettingsDialog.vue';
import ErrorModal from './components/ErrorModal.vue';
import * as api from './api.js';
import { setCredentials, isAuthenticated } from './auth.js';

export default {
  name: 'App',
  components: {
    CreateTabulatedFunctionDialog,
    CreateTabulatedFunctionFromMathDialog,
    SettingsDialog,
    ErrorModal
  },
  data() {
    return {
      showCreateDialog: false,
      showCreateFromMathDialog: false,
      showSettingsDialog: false,
      currentUserId: 1,
      createdFunctions: [],
      showErrorModal: false,
      errorMessage: ''
    };
  },
  computed: {
    isAuthenticated() {
      return isAuthenticated();
    }
  },
  created() {
    setCredentials('admin', 'admin123');
    this.currentUserId = 4;
  },
  methods: {
    showError(message) {
      this.errorMessage = message;
      this.showErrorModal = true;
    },
    closeErrorModal() {
      this.showErrorModal = false;
      this.errorMessage = '';
    },
    async onFunctionCreatedFromXY(data) {
      if (!this.isAuthenticated) {
         this.showError("Пожалуйста, сначала войдите в систему.");
         return;
      }

      try {
        const functionName = `Function_${Date.now()}`;
        const functionType = 'tabular';
        const functionExpression = null;

        const newFunction = await api.createFunction(
          functionName,
          functionType,
          functionExpression,
          this.currentUserId
        );

        console.log('Функция создана на бэкенде:', newFunction);

        await api.createTabulatedPoints(newFunction.id, data.xValues, data.yValues);

        this.createdFunctions.push(newFunction);

        alert(`Функция "${functionName}" (ID: ${newFunction.id}) успешно создана на сервере!`);

      } catch (error) {
        console.error('Ошибка при создании функции из X/Y:', error);
        this.showError(`Ошибка при создании функции: ${error.message}`);
      }
    },
    onFunctionCreatedFromMath(createdFunction) {
      console.log('Функция из MathFunction создана на бэкенде:', createdFunction);
      this.createdFunctions.push(createdFunction);
    }
  }
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}

.main-buttons {
  margin: 20px 0;
}

.main-buttons button {
  margin: 0 10px;
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.main-buttons button:hover {
  background-color: #0056b3;
}
</style>