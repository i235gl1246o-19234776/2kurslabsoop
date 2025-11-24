<!-- src/main/frontend/src/components/ui/InsertPointDialog.vue -->
<template>
  <div v-if="isOpen" class="modal-overlay" @click="closeDialog">
    <div class="modal-content" @click.stop>
      <h2>Вставка новой точки</h2>
      <div class="input-section">
        <label for="xInput">Значение X:</label>
        <input
          id="xInput"
          v-model.number="xValue"
          type="number"
          step="any"
          placeholder="Введите X"
        />
        <label for="yInput">Значение Y:</label>
        <input
          id="yInput"
          v-model.number="yValue"
          type="number"
          step="any"
          placeholder="Введите Y"
        />
      </div>
      <div class="button-group">
        <button @click="insertPoint" class="create-btn">Вставить точку</button>
        <button @click="closeDialog" class="cancel-btn">Отмена</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'InsertPointDialog',
  props: {
    isOpen: {
      type: Boolean,
      required: true
    }
  },
  emits: ['close', 'point-inserted'],
  data() {
    return {
      xValue: 0,
      yValue: 0
    };
  },
  methods: {
    insertPoint() {
      if (typeof this.xValue !== 'number' || typeof this.yValue !== 'number' ||
          isNaN(this.xValue) || isNaN(this.yValue) ||
          !isFinite(this.xValue) || !isFinite(this.yValue)) {
        alert('Пожалуйста, введите корректные числовые значения для X и Y.');
        return;
      }

      this.$emit('point-inserted', {
        x: this.xValue,
        y: this.yValue
      });
      this.closeDialog();
    },
    closeDialog() {
      this.xValue = 0;
      this.yValue = 0;
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
  z-index: 1000;
}
.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  min-width: 400px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}
.input-section {
  margin-bottom: 20px;
}
.input-section label {
  display: block;
  margin-bottom: 5px;
}
.input-section input {
  width: 100%;
  padding: 8px;
  margin-bottom: 10px;
  box-sizing: border-box;
}
.button-group {
  display: flex;
  justify-content: space-between;
}
.create-btn {
  background-color: #2196F3;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.create-btn:hover {
  background-color: #0b7dda;
}
.cancel-btn {
  background-color: #f44336;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.cancel-btn:hover {
  background-color: #da190b;
}
</style>