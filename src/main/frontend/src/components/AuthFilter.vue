<template>
  <div v-if="!isAuthenticated">
    <div class="auth-message">
      <p>Пожалуйста, войдите в систему для доступа к этому контенту.</p>
      <button @click="showLogin = true">Войти</button>
    </div>
  </div>
  <slot v-else />

  <!-- Модальное окно входа -->
  <div v-if="showLogin" class="modal">
    <LoginForm @login-success="handleLoginSuccess" @close="showLogin = false" />
  </div>
</template>

<script setup>
import { ref, onMounted, inject, computed } from 'vue';
import { api } from '../api.js';
import LoginForm from './LoginForm.vue';

const props = defineProps({
  requiredRole: {
    type: String,
    default: 'user'
  }
});

const emit = defineEmits(['auth-required']);

// Состояние аутентификации
const showLogin = ref(false);
const user = ref(null);

// Инжектируем данные аутентификации из App.vue
const auth = inject('auth');
const showError = inject('showError');

const isAuthenticated = computed(() => {
  if (!auth || !auth.isLoggedIn) return false;

  // Если не требуется роль администратора, достаточно просто быть авторизованным
  if (props.requiredRole === 'user') return auth.isLoggedIn.value;

  // Если требуется роль администратора, проверяем роль
  return auth.isLoggedIn.value && user.value?.role === 'ADMIN';
});

const handleLoginSuccess = (userData) => {
  user.value = userData;
  showLogin.value = false;
};

onMounted(async () => {
  if (api.isAuthenticated()) {
    try {
      const response = await fetch('/api/users/me', {
        headers: {
          'Authorization': `Basic ${api.getStoredCredentials()}`
        }
      });

      if (response.ok) {
        const userData = await response.json();
        user.value = userData;
      } else {
        api.logout();
      }
    } catch (err) {
      api.logout();
      showError('Ошибка проверки аутентификации');
    }
  }
});
</script>

<style scoped>
.auth-message {
  text-align: center;
  padding: 40px;
  background-color: var(--bg-secondary);
  border-radius: 8px;
  margin: 20px 0;
}

.auth-message p {
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.auth-message button {
  background-color: var(--button-primary);
  color: white;
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.auth-message button:hover {
  background-color: var(--button-primary-hover);
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: var(--modal-overlay);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
</style>