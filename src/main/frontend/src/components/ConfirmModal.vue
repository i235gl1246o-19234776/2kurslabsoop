<!-- src/components/ConfirmModal.vue -->
<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-content">
      <div class="modal-header">
        <h3>⚠️ Подтвердите действие</h3>
      </div>
      <div class="modal-body">
        <p>{{ message }}</p>
      </div>
      <div class="modal-footer">
        <button @click="confirm" class="btn-confirm">ОК</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  show: Boolean,
  message: {
    type: String,
    default: 'Подтвердите действие'
  }
});

const emit = defineEmits(['confirm', 'close']);

const confirm = () => {
  emit('confirm');
  emit('close');
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-content {
  background: linear-gradient(
    160deg,
    #2a0c55 0%,
    #551e90 45%,
    #ff4fc494 100%
  );
  color: #ffffff;
  border-radius: 14px;
  padding: 25px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.15);
  text-align: center;
}

.modal-header h3 {
  margin: 0 0 1rem 0;
  font-size: 1.3rem;
  font-weight: 600;
}

.modal-body p {
  margin: 0 0 1.5rem 0;
  font-size: 1.1rem;
  line-height: 1.5;
}

.modal-footer {
  display: flex;
  justify-content: center;
}

.btn-confirm {
  padding: 0.7rem 1.5rem;
  font-size: 1rem;
  font-weight: 600;
  background: linear-gradient(135deg, #2f105c 0%, #7b1fa8 40%, #ff4fc4 100%);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.btn-confirm:hover {
  transform: translateY(-2px);
  background: linear-gradient(135deg, #3d1474 0%, #9e27c8 40%, #ff6fda 100%);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.4);
}

.btn-confirm:active {
  transform: scale(0.98);
}
</style>