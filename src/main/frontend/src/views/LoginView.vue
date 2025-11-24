<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>Вход в систему</h2>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="Имя пользователя" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="Введите имя пользователя"
          />
        </el-form-item>

        <el-form-item label="Пароль" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="Введите пароль"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
          >
            Войти
          </el-button>
        </el-form-item>
      </el-form>

      <div class="debug-info" v-if="debugMode">
        <h4>Отладочная информация:</h4>
        <p><strong>BaseURL:</strong> http://localhost:8080/api</p>
        <p><strong>Token:</strong> {{ computedToken }}</p>
        <p><strong>Status:</strong> {{ debugStatus }}</p>
      </div>

      <div class="demo-credentials">
        <h4>Демо доступ:</h4>
        <p><strong>Имя пользователя:</strong> admin</p>
        <p><strong>Пароль:</strong> admin123</p>
        <el-button
          size="small"
          type="text"
          @click="fillDemoCredentials"
        >
          Заполнить демо данные
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

export default {
  name: 'LoginView',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()

    const loginFormRef = ref()
    const loading = ref(false)
    const debugMode = ref(true) // Включите для отладки
    const debugStatus = ref('')

    const loginForm = ref({
      username: '',
      password: ''
    })

    const computedToken = computed(() => {
      if (loginForm.value.username && loginForm.value.password) {
        return btoa(`${loginForm.value.username}:${loginForm.value.password}`)
      }
      return ''
    })

    const loginRules = {
      username: [
        { required: true, message: 'Пожалуйста, введите имя пользователя', trigger: 'blur' }
      ],
      password: [
        { required: true, message: 'Пожалуйста, введите пароль', trigger: 'blur' }
      ]
    }

    const fillDemoCredentials = () => {
      loginForm.value.username = 'admin'
      loginForm.value.password = 'admin123'
    }

    const handleLogin = async () => {
      if (!loginFormRef.value) return

      try {
        const valid = await loginFormRef.value.validate()
        if (!valid) return

        loading.value = true
        debugStatus.value = 'Выполняется вход...'

        await authStore.login(loginForm.value.username, loginForm.value.password)

        debugStatus.value = 'Успешно! Перенаправление...'
        router.push('/')

      } catch (error) {
        console.error('Ошибка входа:', error)
        debugStatus.value = `Ошибка: ${error.message}`
        ElMessage.error(error.message || 'Ошибка входа. Проверьте логин и пароль.')
      } finally {
        loading.value = false
      }
    }

    return {
      loginFormRef,
      loginForm,
      loginRules,
      loading,
      debugMode,
      debugStatus,
      computedToken,
      fillDemoCredentials,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f5f5;
}

.login-card {
  width: 450px;
  padding: 20px;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.demo-credentials {
  margin-top: 20px;
  padding: 15px;
  background: #f0f9ff;
  border: 1px solid #bae7ff;
  border-radius: 4px;
}

.demo-credentials h4 {
  margin: 0 0 10px 0;
  color: #1890ff;
}

.demo-credentials p {
  margin: 5px 0;
  font-size: 14px;
}

.debug-info {
  margin-top: 15px;
  padding: 10px;
  background: #fff2e8;
  border: 1px solid #ffbb96;
  border-radius: 4px;
  font-size: 12px;
}

.debug-info h4 {
  margin: 0 0 8px 0;
  color: #d46b08;
}

.debug-info p {
  margin: 2px 0;
  word-break: break-all;
}
</style>