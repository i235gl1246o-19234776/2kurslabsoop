<template>
  <div class="composite-function-view">
    <h1>Создание составных функций</h1>
    <CompositeFunctionCreator
      :show="true"
      @close="handleClose"
      @function-created="handleFunctionCreated"
    />
  </div>
</template>

<script>
import { ref } from 'vue'
import CompositeFunctionCreator from '@/components/CompositeFunctionCreator.vue'

export default {
  name: 'CompositeFunctionView',
  components: {
    CompositeFunctionCreator
  },
  setup() {
    const handleClose = () => {
      // Обработка закрытия
      console.log('Composite function creator closed')
    }

    const handleFunctionCreated = (functionData) => {
      // Обработка создания функции
      console.log('Function created:', functionData)
    }

    return {
      handleClose,
      handleFunctionCreated
    }
  }
}
</script>

<style scoped>
.composite-function-view {
  padding: 20px;
}
</style>