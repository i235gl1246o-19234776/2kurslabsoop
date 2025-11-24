<!-- src/components/CompositeFunctionCreator.vue -->
<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal-content composite-modal">
      <div class="modal-header">
        <h2>Создание составной функции</h2>
        <button class="close-btn" @click="close">&times;</button>
      </div>

      <div v-if="isLoading" class="loading">
        <p>Загрузка данных...</p>
      </div>

      <div v-else class="composite-container">
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
              <p class="hint">Например: f(x) = sin(x) + cos(x) * x²</p>
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
                    <option v-for="func in availableFunctions" :key="func.functionId" :value="func.functionId">
                      {{ func.functionName }} ({{ func.typeFunction === 'tabular' ? 'табличная' : 'математическая' }})
                    </option>
                  </select>

                  <div v-if="block.functionId" class="function-details">
                    <p><strong>Тип:</strong> {{ getFunctionType(block.functionId) }}</p>
                    <p v-if="getFunctionDomain(block.functionId)">
                      <strong>Область определения:</strong> {{ getFunctionDomain(block.functionId) }}
                    </p>
                  </div>
                </div>

                <div v-else-if="block.type === 'operation'" class="block-content">
                  <select v-model="block.operation" class="operation-select">
                    <option value="add">Сложение (+)</option>
                    <option value="subtract">Вычитание (-)</option>
                    <option value="multiply">Умножение (×)</option>
                    <option value="divide">Деление (÷)</option>
                    <option value="compose">Композиция (f(g(x)))</option>
                  </select>

                  <div v-if="block.operation === 'compose'" class="compose-warning">
                    <p><i class="fas fa-exclamation-triangle"></i> Внимание: композиция функций может быть вычислительно сложной</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="function-preview">
          <h3>Предварительный просмотр</h3>

          <div class="preview-expression">
            <p><strong>Текущая формула:</strong></p>
            <div class="formula-display">
              <span v-html="formulaPreview"></span>
            </div>
          </div>

          <div class="function-settings">
            <div class="setting-group">
              <label>
                <span>Локализованное название:</span>
                <input
                  type="text"
                  v-model="localizedFunctionName"
                  placeholder="Введите название функции на русском языке"
                  required
                >
              </label>
            </div>

            <div class="setting-group">
              <label>
                <span>Техническое имя (англ.):</span>
                <input
                  type="text"
                  v-model="technicalFunctionName"
                  placeholder="Введите техническое имя функции"
                  required
                >
              </label>
            </div>

            <div class="setting-group">
              <label>
                <span>Описание функции:</span>
                <textarea
                  v-model="functionDescription"
                  placeholder="Опишите, что делает эта функция"
                  rows="3"
                ></textarea>
              </label>
            </div>
          </div>
        </div>

        <div class="actions">
          <button @click="validateAndCreate" class="create-btn" :disabled="!canCreate">
            <i class="fas fa-save"></i> Создать составную функцию
          </button>
        </div>

        <div v-if="creationResult" class="result-section">
          <h3>Результат создания</h3>
          <div class="result-details">
            <p class="success-message">
              <i class="fas fa-check-circle"></i> Составная функция успешно создана!
            </p>
            <p><strong>ID функции:</strong> {{ creationResult.functionId }}</p>
            <p><strong>Название:</strong> {{ creationResult.functionName }}</p>
            <p><strong>Техническое имя:</strong> {{ creationResult.technicalName }}</p>
            <p><strong>Тип:</strong> Составная функция</p>

            <div class="next-actions">
              <button @click="openForTabulation" class="action-btn">
                <i class="fas fa-table"></i> Создать табулированную функцию
              </button>
              <button @click="closeAndRefresh" class="action-btn">
                <i class="fas fa-sync"></i> Обновить список функций
              </button>
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

const isLoading = ref(false)
const availableFunctions = ref([])
const functionBlocks = ref([])
const localizedFunctionName = ref('')
const technicalFunctionName = ref('')
const functionDescription = ref('')
const creationResult = ref(null)

// Загрузка доступных функций
const loadAvailableFunctions = async () => {
  isLoading.value = true
  try {
    const userId = api.getStoredUserId()
    const functions = await api.getFunctionsByUserId(userId)

    // Фильтруем функции, которые можно использовать в составных
    availableFunctions.value = functions.filter(f =>
      f.typeFunction === 'math' ||
      f.typeFunction === 'tabular' ||
      f.typeFunction === 'composite'
    )
  } catch (error) {
    console.error('Ошибка загрузки функций:', error)
    alert('Ошибка загрузки списка функций: ' + error.message)
  } finally {
    isLoading.value = false
  }
}

// Добавление блока функции
const addFunctionBlock = () => {
  functionBlocks.value.push({
    type: 'function',
    functionId: null,
    name: ''
  })
}

// Добавление блока операции
const addOperationBlock = () => {
  if (functionBlocks.value.length < 2) {
    alert('Для добавления операции необходимо минимум две функции')
    return
  }

  functionBlocks.value.push({
    type: 'operation',
    operation: 'add'
  })
}

// Удаление блока
const removeBlock = (index) => {
  if (confirm('Вы уверены, что хотите удалить этот блок?')) {
    functionBlocks.value.splice(index, 1)
  }
}

// Обновление выбранной функции в блоке
const updateBlockFunction = (index) => {
  const block = functionBlocks.value[index]
  if (block.type === 'function' && block.functionId) {
    const func = availableFunctions.value.find(f => f.functionId === block.functionId)
    if (func) {
      block.name = func.functionName
    }
  }
}

// Получение типа функции
const getFunctionType = (functionId) => {
  const func = availableFunctions.value.find(f => f.functionId === functionId)
  if (!func) return 'неизвестно'

  switch (func.typeFunction) {
    case 'math': return 'Математическая'
    case 'tabular': return 'Табличная'
    case 'composite': return 'Составная'
    default: return func.typeFunction
  }
}

// Получение области определения функции
const getFunctionDomain = (functionId) => {
  const func = availableFunctions.value.find(f => f.functionId === functionId)
  if (!func) return null

  // Для математических функций - стандартная область
  if (func.typeFunction === 'math') {
    return '(-∞, +∞)'
  }

  // Для табличных функций - по точкам
  if (func.typeFunction === 'tabular') {
    return 'требуется загрузка точек'
  }

  return null
}

// Сброс конструктора
const resetBuilder = () => {
  if (functionBlocks.value.length > 0 && !confirm('Сбросить конструктор? Все текущие настройки будут потеряны.')) {
    return
  }

  functionBlocks.value = []
  localizedFunctionName.value = ''
  technicalFunctionName.value = ''
  functionDescription.value = ''
  creationResult.value = null
}

// Предпросмотр формулы
const formulaPreview = computed(() => {
  if (functionBlocks.value.length === 0) return 'f(x) = '

  let formula = 'f(x) = '
  const blocks = [...functionBlocks.value]

  // Обрабатываем блоки
  for (let i = 0; i < blocks.length; i++) {
    const block = blocks[i]

    if (block.type === 'function' && block.functionId) {
      const func = availableFunctions.value.find(f => f.functionId === block.functionId)
      if (func) {
        if (i > 0 && blocks[i-1]?.type === 'operation') {
          // Операция уже добавлена
        } else if (i > 0) {
          formula += ' + '
        }
        formula += `<span class="function-name">${func.functionName}(x)</span>`
      }
    } else if (block.type === 'operation' && i > 0 && blocks[i-1]?.type === 'function') {
      const prevBlock = blocks[i-1]
      const nextBlock = i < blocks.length - 1 ? blocks[i+1] : null

      if (nextBlock && nextBlock.type === 'function') {
        const prevFunc = availableFunctions.value.find(f => f.functionId === prevBlock.functionId)
        const nextFunc = availableFunctions.value.find(f => f.functionId === nextBlock.functionId)

        if (prevFunc && nextFunc) {
          // Удаляем последнюю функцию из формулы
          formula = formula.substring(0, formula.lastIndexOf('<span class="function-name">'))

          let operationSymbol = '+'
          let operationText = ' + '

          switch (block.operation) {
            case 'add':
              operationSymbol = '+'
              operationText = ' + '
              break
            case 'subtract':
              operationSymbol = '-'
              operationText = ' - '
              break
            case 'multiply':
              operationSymbol = '×'
              operationText = ' × '
              break
            case 'divide':
              operationSymbol = '÷'
              operationText = ' ÷ '
              break
            case 'compose':
              operationSymbol = '∘'
              operationText = ' ∘ '
              break
          }

          if (block.operation === 'compose') {
            formula += `<span class="function-name">${prevFunc.functionName}(${nextFunc.functionName}(x))</span>`
          } else {
            formula += `<span class="function-name">${prevFunc.functionName}(x)</span> <span class="operation">${operationSymbol}</span> <span class="function-name">${nextFunc.functionName}(x)</span>`
          }

          // Пропускаем следующий блок (функцию)
          i++
        }
      }
    }
  }

  return formula
})

// Проверка возможности создания
const canCreate = computed(() => {
  return functionBlocks.value.length >= 3 && // Минимум: функция + операция + функция
         localizedFunctionName.value.trim().length >= 3 &&
         technicalFunctionName.value.trim().length >= 3 &&
         !creationResult.value // Если уже создано, блокируем повторное создание
})

// Валидация и создание составной функции
const validateAndCreate = async () => {
  if (!canCreate.value) return

  // Валидация блоков
  const validationErrors = validateBlocks()
  if (validationErrors.length > 0) {
    alert('Ошибки в конфигурации:\n' + validationErrors.join('\n'))
    return
  }

  try {
    isLoading.value = true

    // Формируем структуру составной функции
    const compositeStructure = buildCompositeStructure()

    // Создаем функцию
    const functionData = {
      functionName: localizedFunctionName.value.trim(),
      technicalName: technicalFunctionName.value.trim(),
      functionExpression: JSON.stringify(compositeStructure),
      description: functionDescription.value.trim(),
      typeFunction: 'composite',
      userId: api.getStoredUserId()
    }

    const result = await api.createCompositeFunction(functionData)

    creationResult.value = result
    alert('Составная функция успешно создана!')

  } catch (error) {
    console.error('Ошибка создания составной функции:', error)
    alert('Ошибка создания составной функции: ' + (error.message || 'неизвестная ошибка'))
  } finally {
    isLoading.value = false
  }
}

// Валидация блоков
const validateBlocks = () => {
  const errors = []

  // Проверка последовательности блоков
  for (let i = 0; i < functionBlocks.value.length; i++) {
    const block = functionBlocks.value[i]

    if (block.type === 'function' && !block.functionId) {
      errors.push(`Блок ${i + 1}: Не выбрана функция`)
    }

    if (i === 0 && block.type === 'operation') {
      errors.push('Первый блок не может быть операцией')
    }

    if (i === functionBlocks.value.length - 1 && block.type === 'operation') {
      errors.push('Последний блок не может быть операцией')
    }

    if (block.type === 'operation' && i > 0) {
      const prevBlock = functionBlocks.value[i-1]
      if (prevBlock.type !== 'function') {
        errors.push(`Блок ${i + 1}: Операция должна следовать после функции`)
      }

      if (i < functionBlocks.value.length - 1) {
        const nextBlock = functionBlocks.value[i+1]
        if (nextBlock.type !== 'function') {
          errors.push(`Блок ${i + 1}: После операции должна идти функция`)
        }
      } else {
        errors.push(`Блок ${i + 1}: После операции должна идти функция`)
      }
    }
  }

  return errors
}

// Построение структуры составной функции
const buildCompositeStructure = () => {
  const structure = {
    type: 'composite',
    name: technicalFunctionName.value.trim(),
    displayName: localizedFunctionName.value.trim(),
    description: functionDescription.value.trim(),
    blocks: []
  }

  // Конвертируем блоки в структуру
  functionBlocks.value.forEach(block => {
    if (block.type === 'function') {
      const func = availableFunctions.value.find(f => f.functionId === block.functionId)
      if (func) {
        structure.blocks.push({
          type: 'function',
          functionId: block.functionId,
          name: func.functionName,
          technicalName: func.technicalName || func.functionName
        })
      }
    } else if (block.type === 'operation') {
      structure.blocks.push({
        type: 'operation',
        operation: block.operation
      })
    }
  })

  return structure
}

// Открытие для табуляции
const openForTabulation = () => {
  if (!creationResult.value) return

  // Эмитим событие для создания табулированной функции
  emit('function-created', {
    functionId: creationResult.value.functionId,
    functionName: creationResult.value.functionName,
    isComposite: true
  })

  close()
}

// Закрытие и обновление
const closeAndRefresh = () => {
  emit('close')
  // Здесь можно эмитить событие для обновления списка функций
}

const close = () => {
  emit('close')
}

// Инициализация
onMounted(() => {
  loadAvailableFunctions()

  // Добавляем начальные блоки для примера
  setTimeout(() => {
    if (availableFunctions.value.length >= 2 && functionBlocks.value.length === 0) {
      functionBlocks.value = [
        { type: 'function', functionId: availableFunctions.value[0].functionId, name: availableFunctions.value[0].functionName },
        { type: 'operation', operation: 'add' },
        { type: 'function', functionId: availableFunctions.value[1].functionId, name: availableFunctions.value[1].functionName }
      ]

      localizedFunctionName.value = 'Сумма двух функций'
      technicalFunctionName.value = 'sumFunction'
      functionDescription.value = 'Составная функция, представляющая собой сумму двух базовых функций'
    }
  }, 500)
})
</script>

<style scoped>
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
  max-width: 1000px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #2c3e50;
  color: white;
  border-bottom: 2px solid #34495e;
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
  background: rgba(255, 255, 255, 0.2);
}

.composite-container {
  padding: 20px;
  overflow-y: auto;
  max-height: 80vh;
}

.function-builder {
  margin-bottom: 25px;
}

.builder-controls {
  display: flex;
  gap: 10px;
  margin: 15px 0;
  flex-wrap: wrap;
}

.add-btn, .reset-btn {
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.add-btn {
  background: #3498db;
  color: white;
}

.add-btn:hover {
  background: #2980b9;
}

.reset-btn {
  background: #e74c3c;
  color: white;
}

.reset-btn:hover {
  background: #c0392b;
}

.builder-preview {
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #eee;
}

.empty-builder {
  text-align: center;
  padding: 30px 20px;
  color: #7f8c8d;
}

.empty-builder p {
  margin: 8px 0;
}

.hint {
  font-style: italic;
  color: #95a5a6;
  font-size: 0.9rem;
}

.builder-tree {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.builder-block {
  background: white;
  border: 2px solid #3498db;
  border-radius: 8px;
  padding: 15px;
  position: relative;
}

.block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.block-index {
  background: #3498db;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.9rem;
}

.remove-block {
  background: #e74c3c;
  color: white;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-block:hover {
  background: #c0392b;
}

.block-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.block-content select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 100%;
}

.function-details {
  padding: 10px;
  background: #e8f4f8;
  border-radius: 6px;
  font-size: 0.95rem;
}

.operation-select {
  background: #f1c40f;
  color: #2c3e50;
}

.compose-warning {
  padding: 8px 12px;
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 4px;
  color: #856404;
  font-size: 0.9rem;
}

.function-preview {
  margin: 25px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #eee;
}

.preview-expression {
  margin-bottom: 20px;
}

.formula-display {
  padding: 15px;
  background: white;
  border-radius: 6px;
  border: 1px solid #ddd;
  min-height: 60px;
  font-size: 1.2rem;
  font-family: 'Courier New', monospace;
}

.function-name {
  color: #2980b9;
  font-weight: bold;
}

.operation {
  color: #e74c3c;
  font-weight: bold;
  margin: 0 5px;
}

.function-settings {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.setting-group label {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.setting-group input,
.setting-group textarea {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 100%;
  font-size: 1rem;
}

.setting-group textarea {
  resize: vertical;
  min-height: 80px;
}

.actions {
  display: flex;
  justify-content: center;
  margin: 20px 0;
}

.create-btn {
  padding: 12px 30px;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.create-btn:hover:not(:disabled) {
  background: #219653;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.create-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.result-section {
  margin-top: 25px;
  padding: 20px;
  background: #e8f4e9;
  border-radius: 8px;
  border: 1px solid #27ae60;
}

.result-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.success-message {
  color: #27ae60;
  font-weight: bold;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.next-actions {
  display: flex;
  gap: 15px;
  margin-top: 15px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 10px 20px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #2980b9;
  transform: translateY(-2px);
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  font-size: 1.2rem;
  color: #7f8c8d;
}

@media (max-width: 768px) {
  .composite-modal {
    width: 98%;
    margin: 10px;
  }

  .builder-controls,
  .next-actions {
    flex-direction: column;
  }

  .formula-display {
    font-size: 1rem;
  }
}
</style>