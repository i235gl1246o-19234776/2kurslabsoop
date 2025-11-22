<!-- src/views/SettingsView.vue -->
<template>
  <SettingsModal :is-open="true" @close="goBack" />
</template>

<script setup>
import SettingsModal from '../components/SettingsModal.vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const goBack = () => {
  router.go(-1); // Возвращаемся на предыдущую страницу
};
</script>