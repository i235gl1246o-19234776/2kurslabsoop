<template>
  <div class="register-container">
    <RegisterForm @register-success="handleRegisterSuccess" />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import RegisterForm from '../components/RegisterForm.vue';

const router = useRouter();

const handleRegisterSuccess = () => {
  router.push('/login');
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: var(--bg-primary);
}
</style>