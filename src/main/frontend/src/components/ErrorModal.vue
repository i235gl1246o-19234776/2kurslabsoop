<!-- src/main/frontend/src/components/ui/ErrorModal.vue -->
<template>
  <div v-if="isVisible" class="modal-overlay">
    <div class="modal-content">
      <h3>Ошибка</h3>
      <p>{{ message }}</p>
      <div class="button-group">
        <button @click="closeModal" class="ok-btn">OK</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ErrorModal',
  // Получает видимость и сообщение из родительского компонента
  props: {
    isVisible: {
      type: Boolean,
      required: true
    },
    message: {
      type: String,
      required: true
    }
  },
  emits: ['close'], // Событие для закрытия
  methods: {
    closeModal() {
      this.$emit('close');
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
  z-index: 2000; /* Установим высокий z-index, чтобы быть поверх других модальных окон */
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  min-width: 300px;
  max-width: 500px;
  text-align: center;
}

.button-group {
  margin-top: 15px;
  display: flex;
  justify-content: center;
}

.ok-btn {
  background-color: #f44336;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.ok-btn:hover {
  background-color: #da190b;
}
</style>