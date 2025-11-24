<template>
  <el-menu
    mode="horizontal"
    :default-active="activeIndex"
    router
    class="navigation-menu"
  >
    <el-menu-item index="/">
      <i class="fas fa-home"></i>
      Главная
    </el-menu-item>

    <el-menu-item index="/functions">
      <i class="fas fa-list"></i>
      Функции
    </el-menu-item>

    <!-- НОВЫЙ ПУНКТ - ГРАФИКИ -->
    <el-menu-item index="/graphs">
      <i class="fas fa-chart-line"></i>
      Графики
    </el-menu-item>

    <el-menu-item index="/operations">
      <i class="fas fa-calculator"></i>
      Операции
    </el-menu-item>

    <el-menu-item index="/integration">
      <i class="fas fa-integral"></i>
      Интегралы
    </el-menu-item>

    <el-menu-item index="/composite">
      <i class="fas fa-sitemap"></i>
      Сложные функции
    </el-menu-item>

    <el-menu-item index="/settings" style="margin-left: auto">
      <i class="fas fa-cog"></i>
      Настройки
    </el-menu-item>

    <el-menu-item @click="handleLogout">
      <i class="fas fa-sign-out-alt"></i>
      Выход
    </el-menu-item>
  </el-menu>
</template>

<script>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'NavigationBar',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const authStore = useAuthStore()

    const activeIndex = computed(() => route.path)

    const handleLogout = () => {
      authStore.logout()
      router.push('/login')
    }

    return {
      activeIndex,
      handleLogout
    }
  }
}
</script>

<style scoped>
.navigation-menu {
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.navigation-menu .el-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.navigation-menu .el-menu-item i {
  width: 16px;
  text-align: center;
}
</style>git