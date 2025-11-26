import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@api': path.resolve(__dirname, './src/api.js') // Исправлен путь к api.js
    }
  },
  // Добавляем настройки прокси для API запросов
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // ЗАМЕНИТЕ НА АДРЕС ВАШЕГО БЭКЕНДА
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
        secure: false, // Отключаем проверку SSL сертификатов для разработки
        // Дополнительные настройки для отладки:
        configure: (proxy, options) => {
          // Отладка прокси в консоли
          proxy.on('error', (err, req, res) => {
            console.error('[PROXY ERROR]:', err.message)
          })
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('[PROXY REQUEST]:', req.method, req.url)
          })
        }
      }
    }
  }
})