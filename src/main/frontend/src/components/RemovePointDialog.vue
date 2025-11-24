<!-- src/main/frontend/src/components/ui/RemovePointDialog.vue -->
<template>
  <div v-if="isOpen" class="modal-overlay" @click="closeDialog">
    <div class="modal-content" @click.stop>
      <h2>Удаление точки</h2>
      <div class="input-section">
        <label for="pointIndex">Выберите индекс точки для удаления:</label>
        <select
          id="pointIndex"
          v-model.number="selectedIndex"
        >
          <option
            v-for="index in availableIndices"
            :key="index"
            :value="index"
          >
            Точка {{ index }} (X: {{ getPointX(index) }})
          </option>
        </select>
      </div>
      <div class="button-group">
        <button @click="removePoint" class="remove-btn">Удалить точку</button>
        <button @click="closeDialog" class="cancel-btn">Отмена</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RemovePointDialog',
  props: {
    isOpen: {
      type: Boolean,
      required: true
    },
    pointCount: {
      type: Number,
      required: true,
      default: 0
    },
    functionPoints: {
      type: Array,
      default: () => []
    }
  },
  emits: ['close', 'point-removed'],
  data() {
    return {
      selectedIndex: 0
    };
  },
  computed: {
    availableIndices() {
      return Array.from({ length: this.pointCount }, (_, i) => i);
    }
  },
  methods: {
    getPointX(index) {
      if (this.functionPoints && this.functionPoints[index]) {
        return this.functionPoints[index].x.toFixed(4);
      }
      return 'N/A';
    },
    removePoint() {
      if (this.selectedIndex < 0 || this.selectedIndex >= this.pointCount) {
        alert('Неверный индекс точки');
        return;
      }

      this.$emit('point-removed', this.selectedIndex);
      this.closeDialog();
    },
    closeDialog() {
      this.selectedIndex = 0;
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
.input-section select {
  width: 100%;
  padding: 8px;
  margin-bottom: 10px;
  box-sizing: border-box;
}
.button-group {
  display: flex;
  justify-content: space-between;
}
.remove-btn {
  background-color: #f44336;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.remove-btn:hover {
  background-color: #da190b;
}
.cancel-btn {
  background-color: #9e9e9e;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.cancel-btn:hover {
  background-color: #757575;
}
</style>