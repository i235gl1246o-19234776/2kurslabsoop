import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { useErrorStore } from '@/stores/error'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// Перехватчик для добавления авторизации
api.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Basic ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Вспомогательные функции для работы с localStorage
export const getStoredUser = () => {
  try {
    const userData = localStorage.getItem('user')
    return userData ? JSON.parse(userData) : null
  } catch (error) {
    console.error('Ошибка при чтении пользователя:', error)
    return null
  }
}

export const getStoredUserId = () => {
  try {
    return localStorage.getItem('userId')
  } catch (error) {
    console.error('Ошибка при чтении userId:', error)
    return null
  }
}

export const clearAuth = () => {
  try {
    localStorage.removeItem('user')
    localStorage.removeItem('userId')
    localStorage.removeItem('auth')
    console.log('Аутентификация очищена')
  } catch (error) {
    console.error('Ошибка при очистке аутентификации:', error)
  }
}

export const setCredentials = (username, password) => {
  // Базовая аутентификация
  const token = btoa(`${username}:${password}`)
  localStorage.setItem('auth', token)
}

export const setUserId = (userId) => {
  localStorage.setItem('userId', userId.toString())
}

// Функции для работы с составными функциями
export const createCompositeFunction = async (functionData) => {
  try {
    console.log('Отправка данных для создания составной функции:', functionData)
    const response = await api.post('/functions/composite', functionData, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    console.log('Ответ от сервера при создании составной функции:', response.data)
    return response.data
  } catch (error) {
    console.error('Ошибка при создании составной функции:', error)
    // Более детальная обработка ошибок
    if (error.response) {
      console.error('Данные ошибки от сервера:', error.response.data)
      console.error('Статус ошибки:', error.response.status)
      throw new Error(error.response.data.message || `Ошибка сервера: ${error.response.status}`)
    } else if (error.request) {
      throw new Error('Не удалось подключиться к серверу')
    } else {
      throw new Error(error.message || 'Неизвестная ошибка')
    }
  }
}

// Получение доступных математических функций для составных функций
export const getAvailableMathFunctions = async () => {
  try {
    const response = await api.get('/math-functions/available')
    return response.data
  } catch (error) {
    console.error('Ошибка при получении математических функций:', error)
    // Возвращаем пустой массив в случае ошибки
    return []
  }
}

// Получение функций пользователя
export const getFunctionsByUserId = async (userId) => {
  try {
    const response = await api.get(`/functions/user/${userId}`)
    return response.data
  } catch (error) {
    console.error('Ошибка при получении функций пользователя:', error)
    throw error
  }
}

// Получение табулированных точек по ID функции
export const getTabulatedPointsByFunctionId = async (functionId) => {
  try {
    const response = await api.get(`/tabulated-points/function/${functionId}`)
    return response.data
  } catch (error) {
    console.error('Ошибка при получении точек функции:', error)
    throw error
  }
}

// Создание табулированной функции
export const createFunction = async (functionData) => {
  try {
    const response = await api.post('/functions', functionData, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    return response.data
  } catch (error) {
    console.error('Ошибка при создании функции:', error)
    throw error
  }
}

// Создание функции из математической функции
export const createFunctionFromMath = async (functionData) => {
  try {
    console.log('Отправка данных для создания функции из Math:', functionData)
    const response = await api.post('/functions/from-math', functionData, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    console.log('Ответ от сервера при создании функции из Math:', response.data)
    return response.data
  } catch (error) {
    console.error('Ошибка при создании функции из Math:', error)
    throw error
  }
}

// Создание точек для табулированной функции
export const createTabulatedPoints = async (functionId, xValues, yValues) => {
  try {
    const points = xValues.map((x, index) => ({
      functionId: functionId,
      xVal: x,
      yVal: yValues[index]
    }))
    const response = await api.post('/tabulated-points/batch', points, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    return response.data
  } catch (error) {
    console.error('Ошибка при создании точек:', error)
    throw error
  }
}

// Обновление точки табулированной функции
export const updateTabulatedPoint = async (functionId, point) => {
  try {
    const response = await api.put(`/tabulated-points/function/${functionId}`, point, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    return response.data
  } catch (error) {
    console.error('Ошибка при обновлении точки:', error)
    throw error
  }
}

// Удаление всех точек функции
export const deleteAllPointsByFunctionId = async (functionId) => {
  try {
    const response = await api.delete(`/tabulated-points/function/${functionId}`)
    return response.data
  } catch (error) {
    console.error('Ошибка при удалении точек:', error)
    throw error
  }
}

export const testCreateFunction = async (testData) => {
  try {
    console.log('🧪 Тестирование создания функции:', testData)
    const response = await api.post('/functions', testData)
    console.log('✅ Тест успешен:', response.data)
    return response.data
  } catch (error) {
    console.error('❌ Тест не пройден:', error.response?.data || error.message)
    throw error
  }
}

export const testAuth = async (username, password) => {
  try {
    const token = btoa(`${username}:${password}`)
    // Пробуем разные endpoints
    const endpoints = ['/user', '/functions', '/auth/user', '/api/user']
    for (const endpoint of endpoints) {
      try {
        const response = await api.get(endpoint, {
          headers: {
            Authorization: `Basic ${token}`
          }
        })
        console.log(`✅ Успешная аутентификация через ${endpoint}`)
        return { success: true, endpoint, data: response.data }
      } catch (error) {
        console.log(`❌ Endpoint ${endpoint} не доступен:`, error.response?.status)
      }
    }
    throw new Error('Не удалось найти рабочий endpoint для аутентификации')
  } catch (error) {
    console.error('❌ Ошибка тестирования аутентификации:', error)
    throw error
  }
}

// Получение всех точек по ID функции
export const getAllPointsByFunctionId = async (functionId) => {
  try {
    const response = await api.get(`/tabulated-points/function/${functionId}/all`)
    return response.data
  } catch (error) {
    console.error('Ошибка при получении всех точек функции:', error)
    throw error
  }
}

// Получение математических функций
export const getMathFunctions = async () => {
  try {
    console.log('🔄 Запрос математических функций с сервера...')
    const response = await api.get('/functions/math-functions')
    console.log('✅ Математические функции получены:', response.data)
    return response.data
  } catch (error) {
    console.error('❌ Ошибка при получении математических функций:', error)
    throw error
  }
}

// Перехватчик для обработки ошибок
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const errorStore = useErrorStore()
    if (error.response?.status === 401) {
      errorStore.showErrorModal(
        'Ошибка авторизации',
        'Пожалуйста, войдите в систему'
      )
      const authStore = useAuthStore()
      authStore.logout()
    } else if (error.response?.status === 403) {
      errorStore.showErrorModal(
        'Доступ запрещен',
        'У вас недостаточно прав для выполнения этого действия'
      )
    } else if (error.response?.data?.message) {
      errorStore.showErrorModal(
        'Ошибка',
        error.response.data.message
      )
    } else {
      errorStore.showErrorModal(
        'Ошибка',
        'Произошла непредвиденная ошибка'
      )
    }
    return Promise.reject(error)
  }
)

// Единый экспорт всех функций
export {
  api
}