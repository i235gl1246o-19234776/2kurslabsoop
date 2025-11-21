<!-- src/views/HomeView.vue -->
<template>
  <div>
    <h1>Добро пожаловать в приложение функций</h1>
    <!-- Другие элементы интерфейса -->
    <button @click="openCreateDialog">Создать табулированную функцию</button>

    <!-- Подключаем компонент диалога -->
    <CreateTabulatedFunctionDialog
      :is-open="isCreateDialogOpen"
      @close="closeCreateDialog"
      @function-created="onFunctionCreated"
    />
  </div>
</template>

<script>
import CreateTabulatedFunctionDialog from '@/components/ui/CreateTabulatedFunctionDialog.vue';

export default {
  name: 'HomeView',
  components: {
    CreateTabulatedFunctionDialog
  },
  data() {
    return {
      isCreateDialogOpen: false
    };
  },
  methods: {
    openCreateDialog() {
      this.isCreateDialogOpen = true;
    },
    closeCreateDialog() {
      this.isCreateDialogOpen = false;
    },
    onFunctionCreated(data) {
      console.log("Получены данные для создания функции из диалога:", data);
      // Здесь можно вызвать логику для создания функции с помощью фабрики
      // и, например, добавить её в список функций приложения
    }
  }
};
</script>