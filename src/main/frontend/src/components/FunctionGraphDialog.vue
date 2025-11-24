<template>
  <Teleport to="body">
    <div v-if="isOpen" class="modal-overlay" @click="closeDialog">
      <div class="modal-content" @click.stop style="width: 90%; max-width: 1200px; height: 85vh; max-height: 900px; display: flex; flex-direction: column;">
        <h2>График функции</h2>
        <FunctionGraphSection
          title="Функция"
          :function-data="functionData"
          :is-result="false"
          :current-user-id="currentUserId"
          @function-loaded="handleFunctionLoaded"
          @function-cleared="handleFunctionCleared"
          @error="handleError"
        />
        <div class="button-group">
          <button @click="closeDialog" class="cancel-btn">Закрыть</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
<script>
import { Teleport } from 'vue';
import FunctionGraphSection from './FunctionGraphSection.vue';

export default {
  name: 'FunctionGraphDialog',
  components: {
    Teleport,
    FunctionGraphSection
  },
  props: {
    isOpen: {
      type: Boolean,
      required: true
    },
    currentUserId: {
      type: Number,
      required: true
    }
  },
  emits: ['close', 'error'],
  data() {
    return {
      functionData: null
    };
  },
  methods: {
    closeDialog() {
      this.functionData = null;
      this.$emit('close');
    },
    handleFunctionLoaded(fullFunctionData) {
      this.functionData = fullFunctionData;
    },
    handleFunctionCleared() {
      this.functionData = null;
    },
    handleError(message) {
      console.error('FunctionGraphDialog error:', message);
      this.$emit('error', message);
    }
  }
};
</script>
<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}
.button-group {
  margin-top: 15px;
  display: flex;
  justify-content: center;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
.cancel-btn {
  padding: 10px 20px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
}
.cancel-btn:hover {
  background-color: #da190b;
}
</style>