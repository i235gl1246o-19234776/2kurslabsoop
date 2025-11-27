import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    // Автоматический импорт ref, reactive, computed и других Vue функций
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router'],
      dts: 'src/auto-imports.d.ts',
    }),
    // Автоматический импорт компонентов
    Components({
      resolvers: [ElementPlusResolver()],
      dirs: ['src/components'],
      extensions: ['vue'],
      dts: 'src/components.d.ts',
    }),
  ],
  resolve: {
    alias: {
      // Стандартный алиас для src директории
      '@': path.resolve(__dirname, 'src'),
    },
    extensions: ['.js', '.vue', '.json']
  },
  // Настройки прокси для API запросов
  server: {
    proxy: {
      // Проксируем все запросы начинающиеся с /api
      '/api': {
        target: 'http://localhost:8080', // Замените на адрес вашего бэкенда
        changeOrigin: true,
        // Сохраняем префикс /api при запросе к бэкенду
        rewrite: (path) => path,
        secure: false,
        // Логирование для отладки
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.error('[PROXY ERROR]:', err.message)
          })
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('[PROXY REQUEST]:', req.method, req.url)
          })
          proxy.on('proxyRes', (proxyRes, req, res) => {
            console.log('[PROXY RESPONSE]:', proxyRes.statusCode, req.url)
          })
        }
      }
    }
  },
  build: {
    // Оптимизация сборки
    target: 'esnext',
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    }
  }
})