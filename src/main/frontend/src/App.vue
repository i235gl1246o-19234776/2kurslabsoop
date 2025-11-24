<template>
  <el-container class="app-container">
    <el-header v-if="isAuthenticated">
      <NavigationBar />
    </el-header>

    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script>
import { computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import NavigationBar from '@/components/NavigationBar.vue'

export default {
  name: 'App',
  components: {
    NavigationBar
  },
  setup() {
    const authStore = useAuthStore()
    const isAuthenticated = computed(() => authStore.isAuthenticated)

    // Инициализируем аутентификацию при загрузке приложения
    onMounted(() => {
      authStore.initializeFromStorage()
    })

    return {
      isAuthenticated
    }
  }
}
</script>

<style>
.app-container {
  min-height: 100vh;
}

.el-header {
  padding: 0;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.el-main {
  padding: 20px;
  background: #f5f5f5;
}
</style>