<template>
  <div class="settings-view">
    <el-card>
      <template #header>
        <h2>Настройки приложения</h2>
      </template>

      <el-tabs v-model="activeTab">
        <!-- Настройки фабрики -->
        <el-tab-pane label="Фабрика функций" name="factory">
          <el-form :model="settings" label-width="200px">
            <el-form-item label="Тип фабрики по умолчанию">
              <el-radio-group v-model="settings.defaultFactoryType">
                <el-radio label="array">Массив (ArrayTabulatedFunction)</el-radio>
                <el-radio label="linkedlist">Связный список (LinkedListTabulatedFunction)</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="Автоматическое создание">
              <el-switch v-model="settings.autoCreateFunctions" />
              <div class="setting-description">
                Автоматически создавать функции после операций
              </div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Настройки вычислений -->
        <el-tab-pane label="Вычисления" name="calculations">
          <el-form :model="settings" label-width="200px">
            <el-form-item label="Количество потоков по умолчанию">
              <el-input-number
                v-model="settings.defaultThreadCount"
                :min="1"
                :max="16"
              />
              <div class="setting-description">
                Количество потоков для параллельных вычислений
              </div>
            </el-form-item>

            <el-form-item label="Количество точек по умолчанию">
              <el-input-number
                v-model="settings.defaultPointCount"
                :min="10"
                :max="1000"
              />
              <div class="setting-description">
                Количество точек при создании функций из Math функций
              </div>
            </el-form-item>

            <el-form-item label="Точность вычислений">
              <el-input-number
                v-model="settings.calculationPrecision"
                :min="1"
                :max="10"
              />
              <div class="setting-description">
                Количество знаков после запятой при отображении результатов
              </div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Настройки интерфейса -->
        <el-tab-pane label="Интерфейс" name="interface">
          <el-form :model="settings" label-width="200px">
            <el-form-item label="Тема оформления">
              <el-radio-group v-model="settings.theme">
                <el-radio label="light">Светлая</el-radio>
                <el-radio label="dark">Темная</el-radio>
                <el-radio label="auto">Системная</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="Язык интерфейса">
              <el-select v-model="settings.language">
                <el-option label="Русский" value="ru" />
                <el-option label="English" value="en" />
              </el-select>
            </el-form-item>

            <el-form-item label="Показывать подсказки">
              <el-switch v-model="settings.showTooltips" />
            </el-form-item>

            <el-form-item label="Автосохранение">
              <el-switch v-model="settings.autoSave" />
              <div class="setting-description">
                Автоматически сохранять изменения в функциях
              </div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Информация о системе -->
        <el-tab-pane label="Система" name="system">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="Всего функций">
              <el-tag type="primary">{{ functionsStore.functions.length }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Табличных функций">
              <el-tag>{{ functionsStore.tabularFunctions.length }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Аналитических функций">
              <el-tag type="success">{{ functionsStore.analyticFunctions.length }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Math функций">
              <el-tag type="warning">{{ functionsStore.mathFunctions.length }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Версия приложения">
              <el-tag>1.0.0</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Последнее обновление">
              <el-tag>{{ new Date().toLocaleDateString() }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <div style="margin-top: 20px">
            <el-button type="primary" @click="clearCache">
              Очистить кэш
            </el-button>
            <el-button @click="exportData">
              Экспорт данных
            </el-button>
            <el-button type="danger" @click="resetSettings">
              Сбросить настройки
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>

      <div class="settings-actions">
        <el-button type="primary" @click="saveSettings">
          Сохранить настройки
        </el-button>
        <el-button @click="resetForm">
          Отменить изменения
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useFunctionsStore } from '@/stores/functions'
import { ElMessage, ElMessageBox } from 'element-plus'

const functionsStore = useFunctionsStore()

const activeTab = ref('factory')
const settings = ref({
  // Настройки фабрики
  defaultFactoryType: 'array',
  autoCreateFunctions: true,

  // Настройки вычислений
  defaultThreadCount: 4,
  defaultPointCount: 100,
  calculationPrecision: 4,

  // Настройки интерфейса
  theme: 'light',
  language: 'ru',
  showTooltips: true,
  autoSave: false
})

const defaultSettings = { ...settings.value }

// Загрузка настроек
const loadSettings = () => {
  const saved = localStorage.getItem('appSettings')
  if (saved) {
    try {
      const parsed = JSON.parse(saved)
      settings.value = { ...settings.value, ...parsed }
    } catch (error) {
      console.error('Ошибка загрузки настроек:', error)
    }
  }
}

// Сохранение настроек
const saveSettings = () => {
  try {
    localStorage.setItem('appSettings', JSON.stringify(settings.value))
    ElMessage.success('Настройки сохранены')

    // Применяем настройки темы
    applyTheme(settings.value.theme)
  } catch (error) {
    ElMessage.error('Ошибка сохранения настроек')
  }
}

// Применение темы
const applyTheme = (theme) => {
  const html = document.documentElement
  html.classList.remove('light-theme', 'dark-theme')

  if (theme === 'dark') {
    html.classList.add('dark-theme')
  } else if (theme === 'light') {
    html.classList.add('light-theme')
  } else {
    // Автоматическая тема
    if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
      html.classList.add('dark-theme')
    } else {
      html.classList.add('light-theme')
    }
  }
}

// Сброс формы
const resetForm = () => {
  loadSettings()
  ElMessage.info('Изменения отменены')
}

// Сброс настроек
const resetSettings = async () => {
  try {
    await ElMessageBox.confirm(
      'Вы уверены, что хотите сбросить все настройки?',
      'Подтверждение сброса',
      { type: 'warning' }
    )

    settings.value = { ...defaultSettings }
    localStorage.removeItem('appSettings')
    applyTheme(settings.value.theme)
    ElMessage.success('Настройки сброшены')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Ошибка при сбросе настроек')
    }
  }
}

// Очистка кэша
const clearCache = async () => {
  try {
    await ElMessageBox.confirm(
      'Очистить кэш приложения?',
      'Подтверждение очистки',
      { type: 'warning' }
    )

    localStorage.removeItem('appCache')
    ElMessage.success('Кэш очищен')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Ошибка при очистке кэша')
    }
  }
}

// Экспорт данных
const exportData = () => {
  const data = {
    settings: settings.value,
    functions: functionsStore.functions,
    exportDate: new Date().toISOString()
  }

  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `function-manager-backup-${new Date().toISOString().split('T')[0]}.json`
  link.click()
  URL.revokeObjectURL(url)

  ElMessage.success('Данные экспортированы')
}

// Наблюдатель для автоматического сохранения
watch(settings, (newSettings) => {
  if (newSettings.autoSave) {
    saveSettings()
  }
}, { deep: true })

// Инициализация
onMounted(async () => {
  await functionsStore.loadFunctions()
  loadSettings()
  applyTheme(settings.value.theme)
})
</script>

<style scoped>
.settings-view {
  padding: 20px;
}

.setting-description {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.settings-actions {
  margin-top: 20px;
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #e8e8e8;
}

:deep(.el-tabs__content) {
  padding: 20px 0;
}

:deep(.el-descriptions) {
  margin-top: 10px;
}
</style>

<style>
/* Глобальные стили для тем */
.light-theme {
  --el-color-primary: #409eff;
  --el-bg-color: #ffffff;
}

.dark-theme {
  --el-color-primary: #409eff;
  --el-bg-color: #141414;
  background-color: #141414;
  color: #ffffff;
}

.dark-theme .el-card {
  background-color: #1f1f1f;
  border-color: #434343;
}

.dark-theme .el-card__header {
  border-bottom-color: #434343;
}
</style>