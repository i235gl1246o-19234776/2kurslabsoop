<template>
  <Teleport to="body">
    <div v-if="isOpen" class="modal-overlay" @click="closeDialog">
      <div class="modal-content" @click.stop style="width: 80%; max-width: 1000px; height: 80vh; max-height: 800px; display: flex; flex-direction: column;">
        <h2>Вычисление определенного интеграла</h2>
        <div class="function-section">
          <div class="function-info">
            <span v-if="sourceFunction && sourceFunction.name">{{ sourceFunction.name }} (ID: {{ sourceFunction.id }})</span>
            <span v-else>Функция не загружена</span>
          </div>
          <div class="data-table-container">
            <table class="points-table" v-if="sourceFunction && sourceFunction.points">
              <thead>
                <tr>
                  <th>Индекс</th>
                  <th>X</th>
                  <th>Y</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(point, index) in sourceFunction.points" :key="index">
                  <td>{{ index }}</td>
                  <td>{{ point.x.toFixed(4) }}</td>
                  <td>{{ point.y.toFixed(4) }}</td>
                </tr>
              </tbody>
            </table>
            <div v-else class="no-data">Нет данных для отображения</div>
          </div>
        </div>
        <div class="integration-settings">
          <div class="input-group">
            <label for="fromInput">От X:</label>
            <input
              id="fromInput"
              v-model.number="fromX"
              type="number"
              step="any"
              placeholder="Начало интервала"
              :disabled="!sourceFunction"
            />
          </div>
          <div class="input-group">
            <label for="toInput">До X:</label>
            <input
              id="toInput"
              v-model.number="toX"
              type="number"
              step="any"
              placeholder="Конец интервала"
              :disabled="!sourceFunction"
            />
          </div>
          <div class="input-group">
            <label for="threadsInput">Количество потоков (1-16):</label>
            <input
              id="threadsInput"
              v-model.number="threadCount"
              type="number"
              min="1"
              max="16"
              placeholder="Количество потоков"
              :disabled="!sourceFunction"
            />
          </div>
        </div>
        <div class="result-section" v-if="integrationResult !== null">
          <h3>Результат интегрирования</h3>
          <p>Значение интеграла на интервале [{{ fromX }}, {{ toX }}]: <strong>{{ integrationResult.toFixed(6) }}</strong></p>
          <p>Время вычисления: {{ integrationDuration }} мс</p>
        </div>
        <div class="button-group">
          <button
            @click="computeIntegral"
            :disabled="!canIntegrate || isComputing"
            class="action-btn"
          >
            <span v-if="isComputing">Вычисление...</span>
            <span v-else>Вычислить интеграл</span>
          </button>
          <button @click="closeDialog" class="cancel-btn">Закрыть</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
<script>
import { Teleport } from 'vue';
import * as api from '@/api.js';
export default {
  name: 'IntegrationWindow',
  components: {
    Teleport,
  },
  props: {
    isOpen: {
      type: Boolean,
      required: true,
    },
    currentUserId: {
      type: Number,
      required: true,
    }
  },
  emits: ['close', 'error'],
  data() {
    return {
      sourceFunction: null,
      fromX: null,
      toX: null,
      threadCount: 4,
      integrationResult: null,
      integrationDuration: 0,
      isComputing: false
    };
  },
  computed: {
    canIntegrate() {
      return this.sourceFunction &&
             this.sourceFunction.id &&
             typeof this.fromX === 'number' &&
             typeof this.toX === 'number' &&
             typeof this.threadCount === 'number' &&
             this.threadCount > 0 && this.threadCount <= 16 &&
             this.fromX < this.toX;
    }
  },
  methods: {
    closeDialog() {
      this.sourceFunction = null;
      this.fromX = null;
      this.toX = null;
      this.threadCount = 4;
      this.integrationResult = null;
      this.integrationDuration = 0;
      this.$emit('close');
    },
    handleSourceFunctionCleared() {
      this.sourceFunction = null;
      this.integrationResult = null;
    },
    handleSourceFunctionLoaded(fullFunctionData) {
      this.sourceFunction = fullFunctionData;
      this.integrationResult = null;
      // Устанавливаем интервал по умолчанию
      if (fullFunctionData.points && fullFunctionData.points.length >= 2) {
        this.fromX = fullFunctionData.points[0].x;
        this.toX = fullFunctionData.points[fullFunctionData.points.length - 1].x;
      }
    },
    async computeIntegral() {
      if (!this.canIntegrate || this.isComputing) return;
      this.isComputing = true;
      this.integrationResult = null;
      try {
        const factoryType = localStorage.getItem('selectedTabulatedFunctionFactory') || 'array';
        console.log('Отправка запроса на вычисление интеграла:', {
          functionId: this.sourceFunction.id,
          fromX: this.fromX,
          toX: this.toX,
          threadCount: this.threadCount,
          factoryType
        });
        // Выполняем интегрирование
        const integrationResult = await api.performIntegration(
          this.sourceFunction.id,
          this.fromX,
          this.toX,
          this.threadCount,
          factoryType
        );
        console.log('Результат интегрирования:', integrationResult);
        this.integrationResult = integrationResult.value;
        this.integrationDuration = integrationResult.duration;
        alert(`Интеграл успешно вычислен: ${this.integrationResult.toFixed(6)} (время: ${this.integrationDuration} мс)`);
      } catch (err) {
        console.error('Ошибка при вычислении интеграла:', err);
        let errorMessage = 'Произошла внутренняя ошибка при вычислении интеграла.';
        if (err.response) {
          // Ошибка ответа сервера
          errorMessage = `Ошибка сервера (${err.response.status}): ${err.response.data?.message || 'Нет деталей'}`;
        } else if (err.message) {
          // Ошибка запроса
          errorMessage = err.message;
        }
        this.handleError(
          `Ошибка при вычислении интеграла: ${errorMessage}.
` +
          `Проверьте корректность интервала и наличие точек функции.`
        );
      } finally {
        this.isComputing = false;
      }
    },
    handleError(message) {
      console.error('IntegrationWindow error:', message);
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
.function-section {
  flex: 1;
  min-height: 300px;
  margin-bottom: 20px;
}
.function-info {
  margin-bottom: 10px;
  font-weight: bold;
  color: #333;
}
.data-table-container {
  flex: 1;
  overflow-y: auto;
  min-height: 200px;
}
.points-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}
.points-table, .points-table th, .points-table td {
  border: 1px solid #ccc;
}
.points-table th, .points-table td {
  padding: 8px;
  text-align: center;
}
.no-data {
  text-align: center;
  padding: 20px;
  color: #666;
  font-style: italic;
}
.integration-settings {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}
.input-group {
  flex: 1;
}
.input-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #333;
}
.input-group input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.result-section {
  margin: 20px 0;
  padding: 15px;
  background-color: #e8f5e9;
  border-radius: 8px;
  border: 1px solid #4caf50;
}
.result-section h3 {
  margin-top: 0;
  color: #2e7d32;
}
.button-group {
  display: flex;
  gap: 10px;
  justify-content: center;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
.action-btn, .cancel-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}
.action-btn {
  background-color: #2196F3;
  color: white;
}
.action-btn:disabled {
  background-color: #bbdefb;
  cursor: not-allowed;
}
.cancel-btn {
  background-color: #f44336;
  color: white;
}
.action-btn:hover:not(:disabled),
.cancel-btn:hover {
  opacity: 0.9;
}
</style>