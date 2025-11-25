<!-- src/components/CompositeFunctionCreator.vue -->
<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal-content composite-modal">
      <div class="modal-header">
        <h2>Создание составной функции</h2>
        <button class="close-btn" @click="close">&times;</button>
      </div>

      <div class="composite-container">
        <!-- Состояние загрузки -->
        <div v-if="isLoading" class="loading">
          <p>Загрузка данных...</p>
        </div>

        <!-- Основной контент -->
        <div v-else>
          <!-- Ошибки -->
          <div v-if="errorMessage" class="error-section">
            <h3>Ошибка</h3>
            <div class="error-details">
              <p class="error-message">
                <i class="fas fa-exclamation-circle"></i> {{ errorMessage }}
              </p>
            </div>
          </div>

          <!-- Конструктор -->
          <div class="function-builder">
            <h3>Конструктор составной функции</h3>

            <div class="builder-controls">
              <button @click="addFunctionBlock" class="add-btn">
                <i class="fas fa-plus"></i> Добавить функцию
              </button>
              <button @click="addOperationBlock" class="add-btn">
                <i class="fas fa-plus"></i> Добавить операцию
              </button>
              <button @click="resetBuilder" class="reset-btn">
                <i class="fas fa-redo"></i> Сбросить
              </button>
            </div>

            <div class="builder-preview">
              <div v-if="functionBlocks.length === 0" class="empty-builder">
                <p>Добавьте функции и операции для создания составной функции</p>
                <p class="hint">Например: f(x) = sin(x) + cos(x)</p>
              </div>

              <div v-else class="builder-tree">
                <div v-for="(block, index) in functionBlocks" :key="index" class="builder-block">
                  <div class="block-header">
                    <span class="block-index">{{ index + 1 }}</span>
                    <button @click="removeBlock(index)" class="remove-block">
                      <i class="fas fa-times"></i>
                    </button>
                  </div>

                  <div v-if="block.type === 'function'" class="block-content">
                    <select v-model="block.functionId" @change="updateBlockFunction(index)">
                      <option value="">Выберите функцию</option>
                      <option
                        v-for="func in availableFunctions"
                        :key="func.id"
                        :value="func.id"
                      >
                        {{ func.functionName }} ({{ getFunctionTypeDisplay(func.typeFunction) }})
                      </option>
                    </select>

                    <div v-if="block.functionId" class="function-details">
                      <p><strong>Выражение:</strong> {{ getFunctionExpression(block.functionId) }}</p>
                    </div>
                  </div>

                  <div v-else-if="block.type === 'operation'" class="block-content">
                    <select v-model="block.operation" class="operation-select">
                      <option value="add">Сложение (+)</option>
                      <option value="subtract">Вычитание (-)</option>
                      <option value="multiply">Умножение (×)</option>
                      <option value="divide">Деление (÷)</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Настройки функции -->
          <div class="function-settings">
            <h3>Настройки функции</h3>

            <div class="setting-group">
              <label>
                <span>Название функции:</span>
                <input
                  type="text"
                  v-model="localizedFunctionName"
                  placeholder="Введите название функции"
                  required
                >
              </label>
            </div>

            <div class="setting-group">
              <label>
                <span>Техническое имя:</span>
                <input
                  type="text"
                  v-model="technicalFunctionName"
                  placeholder="Введите техническое имя"
                  required
                >
              </label>
            </div>

            <div class="setting-group">
              <label>
                <span>Описание:</span>
                <textarea
                  v-model="functionDescription"
                  placeholder="Опишите функцию"
                  rows="3"
                ></textarea>
              </label>
            </div>
          </div>

          <!-- Предпросмотр -->
          <div class="function-preview">
            <h3>Предварительный просмотр</h3>
            <div class="preview-expression">
              <p><strong>Формула:</strong></p>
              <div class="formula-display">
                {{ formulaPreview }}
              </div>
            </div>
          </div>

          <!-- Действия -->
          <div class="actions">
            <button
              @click="validateAndCreate"
              class="create-btn"
              :disabled="!canCreate || isCreating"
            >
              <i class="fas fa-save"></i>
              {{ isCreating ? 'Создание...' : 'Создать составную функцию' }}
            </button>
          </div>

          <!-- Результат -->
          <div v-if="creationResult" class="result-section">
            <h3>Функция создана!</h3>
            <div class="result-details">
              <p><strong>ID:</strong> {{ creationResult.functionId }}</p>
              <p><strong>Название:</strong> {{ creationResult.functionName }}</p>
              <p><strong>Техническое имя:</strong> {{ creationResult.technicalName }}</p>

              <div class="next-actions">
                <button @click="testFunction" class="action-btn">
                  <i class="fas fa-calculator"></i> Протестировать функцию
                </button>
                <button @click="close" class="action-btn">
                  <i class="fas fa-check"></i> Готово
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../api.js'

const emit = defineEmits(['close', 'function-created'])

// Состояние
const isLoading = ref(true)
const isCreating = ref(false)
const availableFunctions = ref([])
const functionBlocks = ref([])
const localizedFunctionName = ref('')
const technicalFunctionName = ref('')
const functionDescription = ref('')
const creationResult = ref(null)
const errorMessage = ref('')

// Загрузка доступных функций
const loadAvailableFunctions = async () => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const userId = api.getStoredUserId()
    console.log('👤 Loading functions for user:', userId)

    const functions = await api.getFunctionsByUserId(userId)
    console.log('📊 Loaded functions:', functions)

    // Фильтруем функции, которые можно использовать
    availableFunctions.value = functions.filter(f =>
      f.typeFunction === 'math' || f.functionExpression?.includes('COMPOSITE')
    )

    console.log('✅ Available functions:', availableFunctions.value)

    if (availableFunctions.value.length === 0) {
      errorMessage.value = 'Нет доступных функций для создания составной функции. Сначала создайте математические функции.'
    }

  } catch (error) {
    console.error('❌ Error loading functions:', error)
    errorMessage.value = 'Ошибка загрузки функций: ' + error.message
  } finally {
    isLoading.value = false
  }
}

// Добавление блоков
const addFunctionBlock = () => {
  functionBlocks.value.push({
    type: 'function',
    functionId: '',
    name: ''
  })
}

const addOperationBlock = () => {
  if (functionBlocks.value.length === 0) {
    errorMessage.value = 'Сначала добавьте функцию'
    return
  }

  functionBlocks.value.push({
    type: 'operation',
    operation: 'add'
  })
}

const removeBlock = (index) => {
  functionBlocks.value.splice(index, 1)
}

const updateBlockFunction = (index) => {
  const block = functionBlocks.value[index]
  if (block.type === 'function' && block.functionId) {
    const func = availableFunctions.value.find(f => f.id === block.functionId)
    if (func) {
      block.name = func.functionName
    }
  }
}

// Вспомогательные функции
const getFunctionTypeDisplay = (type) => {
  return type === 'math' ? 'математическая' : 'составная'
}

const getFunctionExpression = (functionId) => {
  const func = availableFunctions.value.find(f => f.id === functionId)
  return func ? (func.functionExpression || 'нет выражения') : 'не найдено'
}

// Предпросмотр формулы
const formulaPreview = computed(() => {
  if (functionBlocks.value.length === 0) return 'f(x) = '

  let formula = 'f(x) = '

  for (let i = 0; i < functionBlocks.value.length; i++) {
    const block = functionBlocks.value[i]

    if (block.type === 'function' && block.functionId) {
      const func = availableFunctions.value.find(f => f.id === block.functionId)
      if (func) {
        if (i > 0) {
          const prevBlock = functionBlocks.value[i-1]
          if (prevBlock.type === 'operation') {
            formula += ` ${getOperationSymbol(prevBlock.operation)} `
          }
        }
        formula += `${func.functionName}(x)`
      }
    }
  }

  return formula
})

const getOperationSymbol = (operation) => {
  switch (operation) {
    case 'add': return '+'
    case 'subtract': return '-'
    case 'multiply': return '×'
    case 'divide': return '÷'
    default: return '?'
  }
}

// Валидация
const canCreate = computed(() => {
  const hasValidBlocks = functionBlocks.value.length >= 1 &&
    functionBlocks.value.every(block =>
      block.type === 'function' ? block.functionId : true
    )

  const hasValidName = localizedFunctionName.value.trim().length >= 2
  const hasValidTechnicalName = technicalFunctionName.value.trim().length >= 2

  return hasValidBlocks && hasValidName && hasValidTechnicalName && !creationResult.value
})

const validateBlocks = () => {
  const errors = []

  if (functionBlocks.value.length === 0) {
    errors.push('Добавьте хотя бы одну функцию')
  }

  for (let i = 0; i < functionBlocks.value.length; i++) {
    const block = functionBlocks.value[i]

    if (block.type === 'function' && !block.functionId) {
      errors.push(`Блок ${i + 1}: не выбрана функция`)
    }

    if (block.type === 'operation' && i === 0) {
      errors.push('Первый блок не может быть операцией')
    }

    if (block.type === 'operation' && i === functionBlocks.value.length - 1) {
      errors.push('Последний блок не может быть операцией')
    }
  }

  return errors
}

// Создание функции
const validateAndCreate = async () => {
  if (!canCreate.value) return

  const validationErrors = validateBlocks()
  if (validationErrors.length > 0) {
    errorMessage.value = 'Ошибки в конфигурации:\n' + validationErrors.join('\n')
    return
  }

  try {
    isCreating.value = true
    errorMessage.value = ''

    // Формируем структуру
    const compositeStructure = {
      type: 'composite',
      name: technicalFunctionName.value.trim(),
      displayName: localizedFunctionName.value.trim(),
      description: functionDescription.value.trim(),
      blocks: functionBlocks.value.map(block => {
        if (block.type === 'function') {
          const func = availableFunctions.value.find(f => f.id === block.functionId)
          return {
            type: 'function',
            functionId: block.functionId.toString(),
            name: func.functionName,
            technicalName: func.technicalName || func.functionName
          }
        } else {
          return {
            type: 'operation',
            operation: block.operation
          }
        }
      })
    }

    const functionData = {
      functionName: localizedFunctionName.value.trim(),
      technicalName: technicalFunctionName.value.trim(),
      description: functionDescription.value.trim(),
      functionExpression: JSON.stringify(compositeStructure),
      userId: api.getStoredUserId()
    }

    console.log('📤 Sending function data:', functionData)

    const result = await api.createCompositeFunction(functionData)

    creationResult.value = result
    errorMessage.value = ''

    console.log('✅ Function created successfully:', result)

  } catch (error) {
    console.error('❌ Error creating function:', error)
    errorMessage.value = 'Ошибка создания функции: ' + error.message
  } finally {
    isCreating.value = false
  }
}

// Тестирование функции
const testFunction = () => {
  if (creationResult.value) {
    emit('function-created', creationResult.value)
    close()
  }
}

// Сброс
const resetBuilder = () => {
  if (confirm('Сбросить конструктор?')) {
    functionBlocks.value = []
    localizedFunctionName.value = ''
    technicalFunctionName.value = ''
    functionDescription.value = ''
    creationResult.value = null
    errorMessage.value = ''
  }
}

const close = () => {
  emit('close')
}

// Инициализация
onMounted(() => {
  loadAvailableFunctions()
})
</script>

<style scoped>
/* Стили остаются такими же как в предыдущей версии */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.composite-modal {
  background: white;
  border-radius: 10px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.3);
  width: 95%;
  max-width: 800px;
  max-height: 90vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #2c3e50;
  color: white;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
}

.composite-container {
  padding: 20px;
  overflow-y: auto;
  max-height: 70vh;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.error-section {
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 5px;
  padding: 15px;
  margin-bottom: 20px;
}

.error-message {
  color: #c00;
  margin: 0;
}

.function-builder,
.function-settings,
.function-preview {
  margin-bottom: 25px;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.builder-controls {
  display: flex;
  gap: 10px;
  margin: 15px 0;
}

.add-btn, .reset-btn {
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn {
  background: #4CAF50;
  color: white;
}

.reset-btn {
  background: #f44336;
  color: white;
}

.builder-tree {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.builder-block {
  border: 1px solid #ccc;
  border-radius: 5px;
  padding: 10px;
}

.block-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.block-content select {
  width: 100%;
  padding: 5px;
}

.setting-group {
  margin-bottom: 15px;
}

.setting-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.setting-group input,
.setting-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.formula-display {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 5px;
  font-family: monospace;
  font-size: 1.1em;
}

.actions {
  text-align: center;
  margin: 20px 0;
}

.create-btn {
  background: #2196F3;
  color: white;
  border: none;
  padding: 12px 25px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1.1em;
}

.create-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.result-section {
  background: #e8f5e8;
  border: 1px solid #4CAF50;
  border-radius: 5px;
  padding: 15px;
}

.next-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.action-btn {
  background: #666;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
}
</style>