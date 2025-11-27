<template>
  <div class="form-container">
    <h2>Вход</h2>
    <form @submit.prevent="login">
      <input v-model="username" type="text" placeholder="Имя пользователя" required />
      <input v-model="password" type="password" placeholder="Пароль" required />
      <button type="submit" :disabled="isLoading">Войти</button>
      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
    </form>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue';
import { useRouter } from 'vue-router';

const username = ref('');
const password = ref('');
const isLoading = ref(false);
const errorMessage = ref('');
const router = useRouter();
const showError = inject('showError');

const login = async () => {
  try {
    isLoading.value = true;
    errorMessage.value = '';

    console.log("Попытка входа:", username.value);

    // Используем правильный эндпоинт для Spring Boot
    const response = await fetch('/api/users/authenticate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        name: username.value,
        password: password.value
      })
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || `Ошибка аутентификации: ${response.status}`);
    }

    const userData = await response.json();
    console.log("Успешный вход, данные:", userData);

    // Сохраняем учетные данные для Basic Auth
    const credentials = btoa(`${username.value}:${password.value}`);
    localStorage.setItem('authCredentials', credentials);
    localStorage.setItem('userId', userData.id);

    // Перенаправляем на главную
    router.push('/');

  } catch (e) {
    console.error("Ошибка входа:", e);
    errorMessage.value = e.message || 'Произошла внутренняя ошибка сервера';
    // Для ошибки 500 показываем дополнительную информацию
    if (e.message.includes('500') || e.message.includes('Internal Server Error')) {
      showError('Ошибка 500: Сервер не отвечает или возвращает ошибку. Проверьте консоль сервера на наличие ошибок.');
    } else {
      showError(errorMessage.value);
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.form-container {
  background: white;
  padding: 2rem;
  border-radius: 10px;
  box-shadow: 0 4px 25px rgba(0, 0, 0, 0.1);
  max-width: 400px;
  width: 90%;
  margin: 0 auto;
}
.form-container h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: var(--header-bg);
}
.form-container input {
  width: 100%;
  padding: 0.8rem;
  margin: 0.5rem 0;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 1rem;
}
.form-container button {
  width: 100%;
  padding: 0.9rem;
  background: var(--button-primary);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
  margin-top: 1rem;
  transition: background 0.3s;
}
.form-container button:hover:not(:disabled) {
  background: var(--button-primary-hover);
}
.form-container button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.error-message {
  color: var(--error-text);
  background: var(--error-bg);
  border: 1px solid var(--error-border);
  padding: 0.75rem;
  border-radius: 4px;
  margin-top: 0.5rem;
  font-size: 0.9rem;
}
</style>