import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '@/services/api'
import { ElMessage } from 'element-plus'

export const useFunctionsStore = defineStore('functions', () => {
  // Состояние
  const functions = ref([])
  const mathFunctions = ref([])
  const loading = ref(false)
  const mathFunctionsLoading = ref(false)
  // Геттеры
  const tabularFunctions = computed(() =>
    functions.value.filter(f => f.typeFunction === 'tabular')
  )
  const analyticFunctions = computed(() =>
    functions.value.filter(f => f.typeFunction === 'analytic')
  )
  const compositeFunctions = computed(() =>
    functions.value.filter(f => f.typeFunction === 'composite')
  )

  // Действия
  async function loadFunctions() {
    loading.value = true
    try {
      const response = await api.get('/functions')
      functions.value = response.data
      console.log('✅ Функции загружены:', functions.value.length)
    } catch (error) {
      console.error('❌ Ошибка при загрузке функций:', error)
      ElMessage.error('Ошибка при загрузке функций: ' + error.message)
      throw error
    } finally {
      loading.value = false
    }
  }

  // Загрузка математических функций
  async function loadMathFunctions() {
    try {
      mathFunctionsLoading.value = true
      const mathFunctionsData = await getMathFunctions()
      // Добавляем фильтрацию и безопасную сортировку
      const filteredFunctions = mathFunctionsData.filter(f => f && f.name)
      filteredFunctions.sort((a, b) => {
        const nameA = a.name || ''
        const nameB = b.name || ''
        return nameA.localeCompare(nameB)
      })
      mathFunctions.value = filteredFunctions
    } catch (error) {
      console.error('❌ Ошибка при загрузке Math функций:', error)
      mathFunctions.value = [] // Устанавливаем пустой массив при ошибке
    } finally {
      mathFunctionsLoading.value = false
    }
  }


 async function createFunction(functionData) {
   try {
       // Сначала создаем функцию
       const functionResponse = await api.post('/functions', {
         userId: functionData.userId,
         typeFunction: functionData.typeFunction || 'tabular',
         functionName: functionData.name,
         functionExpression: functionData.functionExpression || '',
         tabulatedPointIds: [],
         operationIds: []
       });

       // Затем добавляем точки
       if (functionData.points && functionData.points.length > 0) {
         await api.post('/tabulated-points/', functionData.points.map(point => ({
           functionId: functionResponse.data.id,
           xVal: point.x,
           yVal: point.y
         })));
       }

       // Обновляем функцию с точками
       const updatedFunction = await api.get(`/functions/${functionResponse.data.id}`);
       functions.value.push(updatedFunction.data);

       return updatedFunction.data;
   } catch (error) {
     console.error('❌ Ошибка при создании функции:', error)

     if (error.response) {
       console.error('Статус ошибки:', error.response.status)
       console.error('Данные ошибки:', error.response.data)

       let errorMessage = 'Неизвестная ошибка сервера'
       if (error.response.data) {
         if (typeof error.response.data === 'string') {
           errorMessage = error.response.data
         } else if (error.response.data.message) {
           errorMessage = error.response.data.message
         }
       }

       ElMessage.error(`Ошибка при создании функции: ${errorMessage}`)
     } else {
       ElMessage.error('Ошибка при создании функции: ' + error.message)
     }

     throw error
   }
 }

  // stores/functions.js - ИСПРАВЛЕННАЯ ФУНКЦИЯ createFromMath
  // stores/functions.js - ИСПРАВЛЕННАЯ ФУНКЦИЯ createFromMath
  async function createFromMath(mathFunctionData) {
    try {
      console.log('🔄 Создание функции из Math:', JSON.stringify(mathFunctionData, null, 2))

      // ПРОВЕРКА ДАННЫХ
      if (!mathFunctionData.mathFunctionName) {
        throw new Error('Не выбрана математическая функция')
      }

      if (mathFunctionData.xFrom >= mathFunctionData.xTo) {
        throw new Error('Начало интервала должно быть меньше конца')
      }

      if (mathFunctionData.count < 2 || mathFunctionData.count > 1000) {
        throw new Error('Количество точек должно быть от 2 до 1000')
      }

      // ПОДГОТОВКА ДАННЫХ В ФОРМАТЕ POSTMAN
      const dataToSend = {
        userId: functionData.userId,
        typeFunction: functionData.typeFunction || 'tabular',
        name: functionData.name, // Используем name вместо functionName
        functionExpression: functionData.functionExpression || '',
        tabulatedPointIds: [],
        operationIds: []
      }

      console.log('📤 Отправка данных для создания функции из Math:', JSON.stringify(dataToSend, null, 2))

      // РЕАЛЬНЫЙ ВЫЗОВ API
      const response = await api.post('/functions/from-math', dataToSend, {
        headers: {
          'Content-Type': 'application/json'
        }
      })

      console.log('✅ Функция из Math создана:', response.data)
      functions.value.push(response.data)
      ElMessage.success('Функция из Math функции создана успешно')
      return response.data

    } catch (error) {
      console.error('❌ Ошибка при создании функции из Math:', error)

      if (error.response) {
        console.error('Данные ошибки от сервера:', error.response.data)
        console.error('Статус ошибки:', error.response.status)

        let errorMessage = error.response.data?.message || `Ошибка сервера: ${error.response.status}`
        if (error.response.data) {
          if (typeof error.response.data === 'string') {
            errorMessage = error.response.data
          } else if (error.response.data.message) {
            errorMessage = error.response.data.message
          }
        }

        throw new Error(errorMessage)
      } else if (error.request) {
        throw new Error('Не удалось подключиться к серверу')
      } else {
        throw new Error(error.message || 'Неизвестная ошибка')
      }
    }
  }

  // stores/functions.js - ИСПРАВЛЕННАЯ ФУНКЦИЯ createComposite
  // stores/functions.js - ИСПРАВЛЕННАЯ ФУНКЦИЯ createComposite
  // В stores/functions.js
  async function createComposite(compositeData) {
    try {
      console.log('🔄 Создание композитной функции:', compositeData);
      const response = await api.post('/functions/composite', compositeData);
      console.log('✅ Композитная функция создана:', response.data);
      functions.value.push(response.data);
      ElMessage.success('Композитная функция создана успешно');
      return response.data;
    } catch (error) {
      console.error('❌ Ошибка при создании композитной функции:', error);
      ElMessage.error('Ошибка при создании композитной функции: ' + error.message);
      throw error;
    }
  }

  async function deleteFunction(id) {
    try {
      console.log('🔄 Удаление функции ID:', id)
      await api.delete(`/functions/${id}`)
      functions.value = functions.value.filter(f => f.id !== id)
      ElMessage.success('Функция удалена успешно')
    } catch (error) {
      console.error('❌ Ошибка при удалении функции:', error)
      ElMessage.error('Ошибка при удалении функции: ' + error.message)
      throw error
    }
  }

  async function getFunctionPoints(functionId) {
    try {
      console.log('🔄 Загрузка точек функции ID:', functionId)
      const response = await api.get(`/tabulated-points/function/${functionId}`)
      console.log('✅ Точки функции загружены:', response.data)
      return response.data
    } catch (error) {
      console.error('❌ Ошибка при загрузке точек функции:', error)
      throw error
    }
  }

  // Вспомогательные функции для работы с математическими функциями
  function getMathFunctionDisplayName(funcName) {
    const names = {
      'SqrFunction': 'Квадратичная функция (x²)',
      'IdentityFunction': 'Тождественная функция (x)',
      'ZeroFunction': 'Нулевая функция (0)',
      'ConstantFunction': 'Постоянная функция (C)',
      'SinFunction': 'Синус (sin x)',
      'CosFunction': 'Косинус (cos x)',
      'ExpFunction': 'Экспонента (e^x)',
      'LogFunction': 'Натуральный логарифм (ln x)',
      'Квадратичная функция': 'Квадратичная функция (x²)',
      'Тождественная функция': 'Тождественная функция (x)',
      'Нулевая функция': 'Нулевая функция (0)',
      'Постоянная функция': 'Постоянная функция (C)',
      'Синус': 'Синус (sin x)',
      'Косинус': 'Косинус (cos x)',
      'Экспонента': 'Экспонента (e^x)',
      'Натуральный логарифм': 'Натуральный логарифм (ln x)'
    }
    return names[funcName] || funcName
  }

  function getMathFunctionExpression(funcName) {
    const expressions = {
      'SqrFunction': 'x²',
      'IdentityFunction': 'x',
      'ZeroFunction': '0',
      'ConstantFunction': 'C',
      'SinFunction': 'sin(x)',
      'CosFunction': 'cos(x)',
      'ExpFunction': 'e^x',
      'LogFunction': 'ln(x)',
      'Квадратичная функция': 'x²',
      'Тождественная функция': 'x',
      'Нулевая функция': '0',
      'Постоянная функция': 'C',
      'Синус': 'sin(x)',
      'Косинус': 'cos(x)',
      'Экспонента': 'e^x',
      'Натуральный логарифм': 'ln(x)'
    }
    return expressions[funcName] || 'f(x)'
  }

  function calculateMathFunctionValue(funcName, x) {
    const functions = {
      'SqrFunction': (x) => x * x,
      'IdentityFunction': (x) => x,
      'ZeroFunction': (x) => 0,
      'ConstantFunction': (x) => 1,
      'SinFunction': (x) => Math.sin(x),
      'CosFunction': (x) => Math.cos(x),
      'TanFunction': (x) => Math.tan(x),
      'ExpFunction': (x) => Math.exp(x),
      'LogFunction': (x) => x > 0 ? Math.log(x) : NaN,
      'Log10Function': (x) => x > 0 ? Math.log10(x) : NaN,
      'SqrtFunction': (x) => x >= 0 ? Math.sqrt(x) : NaN,
      'AbsFunction': (x) => Math.abs(x),
      'CubeFunction': (x) => x * x * x,
      'ReciprocalFunction': (x) => x !== 0 ? 1 / x : NaN,
      'SigmoidFunction': (x) => 1 / (1 + Math.exp(-x)),
      'UnitFunction': (x) => 1
    }
    const func = functions[funcName]
    if (!func) return NaN
    try {
      return func(x)
    } catch (error) {
      return NaN
    }
  }

  function validateMathFunctionData(data) {
    const errors = []
    if (!data.mathFunctionName) {
      errors.push('Не выбрана математическая функция')
    }
    if (data.xFrom >= data.xTo) {
      errors.push('Начало интервала должно быть меньше конца')
    }
    if (data.count < 2 || data.count > 1000) {
      errors.push('Количество точек должно быть от 2 до 1000')
    }
    return errors
  }

  return {
    // Состояние
    functions,
    mathFunctions,
    loading,
    mathFunctionsLoading,
    // Геттеры
    tabularFunctions,
    analyticFunctions,
    compositeFunctions,
    // Действия
    loadFunctions,
    loadMathFunctions,
    createFunction,
    createFromMath,
    createComposite,
    deleteFunction,
    getFunctionPoints,
    // Вспомогательные функции
    getMathFunctionDisplayName,
    getMathFunctionExpression,
    calculateMathFunctionValue,
    validateMathFunctionData
  }
})