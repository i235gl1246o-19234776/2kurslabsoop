<!-- src/components/InsertPointDialog.vue -->
<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <div class="modal-header">
        <h3>Вставить новую точку</h3>
        <button class="close-btn" @click="close">&times;</button>
      </div>

      <div class="dialog-content">
        <p>Введите координаты новой точки для вставки в функцию</p>

        <div class="input-group">
          <label for="insertX">Координата X:</label>
          <input
            type="number"
            id="insertX"
            v-model="newPoint.x"
            step="any"
            placeholder="Введите значение X"
            @keyup.enter="insertPoint"
          />
          <span v-if="xError" class="error-message">{{ xError }}</span>
        </div>

        <div class="input-group">
          <label for="insertY">Координата Y:</label>
          <input
            type="number"
            id="insertY"
            v-model="newPoint.y"
            step="any"
            placeholder="Введите значение Y"
            @keyup.enter="insertPoint"
          />
          <span v-if="yError" class="error-message">{{ yError }}</span>
        </div>

        <div v-if="insertError" class="error-message dialog-error">
          {{ insertError }}
        </div>
      </div>

      <div class="dialog-footer">
        <button class="cancel-btn" @click="close">Отмена</button>
        <button class="confirm-btn" @click="insertPoint" :disabled="isInserting">
          <span v-if="isInserting">
            <i class="fas fa-spinner fa-spin"></i> Вставка...
          </span>
          <span v-else>
            Вставить точку
          </span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  functionId: Number
});

const emit = defineEmits(['insert', 'close']);

// Состояние
const newPoint = ref({ x: 0, y: 0 });
const xError = ref('');
const yError = ref('');
const insertError = ref('');
const isInserting = ref(false);

// Валидация X
const validateX = () => {
  xError.value = '';

  if (isNaN(newPoint.value.x) || !isFinite(newPoint.value.x)) {
    xError.value = 'Координата X должна быть числом';
    return false;
  }

  return true;
};

// Валидация Y
const validateY = () => {
  yError.value = '';

  if (isNaN(newPoint.value.y) || !isFinite(newPoint.value.y)) {
    yError.value = 'Координата Y должна быть числом';
    return false;
  }

  return true;
};

// Вставка точки
const insertPoint = () => {
  insertError.value = '';

  if (!validateX() || !validateY()) {
    return;
  }

  emit('insert', { ...newPoint.value });
  close();
};

// Закрытие диалога
const close = () => {
  emit('close');
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
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 500px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #2c3e50;
  color: white;
  border-bottom: 2px solid #3498db;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.close-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.dialog-content {
  padding: 20px;
}

.input-group {
  margin-bottom: 15px;
}

.input-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #333;
}

.input-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.error-message {
  color: #e74c3c;
  font-size: 0.85rem;
  margin-top: 5px;
  display: block;
}

.dialog-error {
  margin-top: 15px;
  padding: 10px;
  background-color: #ffebee;
  border-radius: 4px;
  border-left: 3px solid #e74c3c;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 15px 20px;
  border-top: 1px solid #eee;
  background-color: #f8f9fa;
}

.cancel-btn, .confirm-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}

.cancel-btn {
  background-color: #6c757d;
  color: white;
}

.cancel-btn:hover {
  background-color: #5a6268;
}

.confirm-btn {
  background-color: #27ae60;
  color: white;
}

.confirm-btn:hover {
  background-color: #219653;
}

.confirm-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}
</style>