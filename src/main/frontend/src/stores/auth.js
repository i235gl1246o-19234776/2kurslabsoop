// stores/auth.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api, setCredentials, setUserId, clearAuth, getStoredUser, getStoredUserId } from '@/services/api'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('auth') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const isAuthenticated = ref(!!token.value)

  const initializeFromStorage = () => {
    try {
      const storedToken = localStorage.getItem('auth')
      const storedUser = getStoredUser()
      const storedUserId = getStoredUserId()

      if (storedToken) {
        token.value = storedToken
        user.value = storedUser
        isAuthenticated.value = true

        console.log('✅ Аутентификация восстановлена из localStorage')
      } else {
        console.log('ℹ️ В localStorage нет данных аутентификации')
      }
    } catch (error) {
      console.error('❌ Ошибка при восстановлении аутентификации:', error)
      clearAuth()
    }
  }

  const login = async (username, password) => {
    try {
      // Создаем token для базовой аутентификации
      const authToken = btoa(`${username}:${password}`)

      // Пробуем разные endpoints для проверки аутентификации
      let userData = null

      try {
        // Попытка 1: endpoint для получения информации о пользователе
        const response = await api.get('/user', {
          headers: {
            Authorization: `Basic ${authToken}`
          }
        })
        userData = response.data
      } catch (userError) {
        console.log('❌ Endpoint /user не доступен, пробуем другой...')

        // Попытка 2: endpoint для получения функций (как проверка аутентификации)
        const functionsResponse = await api.get('/functions', {
          headers: {
            Authorization: `Basic ${authToken}`
          }
        })

        // Если запрос прошел успешно, создаем объект пользователя
        userData = {
          id: 1,
          username: username,
          name: 'Администратор'
        }
      }

      // Сохраняем данные пользователя
      user.value = userData
      token.value = authToken
      isAuthenticated.value = true

      // Сохраняем в localStorage
      localStorage.setItem('auth', token.value)
      localStorage.setItem('user', JSON.stringify(user.value))
      if (user.value?.id) {
        setUserId(user.value.id.toString())
      }

      // Устанавливаем credentials для будущих запросов
      setCredentials(username, password)

      ElMessage.success('Вход выполнен успешно!')
      return userData

    } catch (error) {
      clearAuth()
      console.error('❌ Ошибка входа:', error)

      if (error.response?.status === 401) {
        throw new Error('Неверное имя пользователя или пароль')
      } else if (error.response?.status === 404) {
        throw new Error('Сервер недоступен или endpoint не найден')
      } else {
        throw new Error('Ошибка подключения к серверу')
      }
    }
  }

  const logout = () => {
    clearAuth()
    token.value = ''
    user.value = null
    isAuthenticated.value = false
    ElMessage.success('Выход выполнен успешно')
  }

  return {
    token,
    user,
    isAuthenticated,
    initializeFromStorage,
    login,
    logout
  }
})