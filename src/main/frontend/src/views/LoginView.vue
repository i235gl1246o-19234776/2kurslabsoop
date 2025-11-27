<template>
  <div class="login-container">
    <LoginForm @login-success="handleLoginSuccess" />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import LoginForm from '../components/LoginForm.vue';

const router = useRouter();

const handleLoginSuccess = () => {
  router.push('/'); // После успешного входа перенаправляем на главную
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--bg-primary);
}
</style>