// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  build: {
    outDir: '../webapp/dist',
    emptyOutDir: true,
  },
  server: {
    port: 3000, // Порт для разработки
    proxy: {
      // <-- Вот этот блок важен
      '/api': {
        target: 'http://localhost:8080/yourapp', // Адрес вашего Java-сервера
        changeOrigin: true, // Важно для правильной работы с заголовками Host
        secure: false,      // Установите в true, если Java-сервер использует HTTPS
        // Добавьте это, чтобы видеть логи прокси в консоли Vite
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.error('Proxy error:', err);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('Sending proxy request:', req.method, req.url);
          });
          proxy.on('proxyRes', (proxyRes, req, res) => {
            console.log('Received proxy response:', proxyRes.statusCode, req.url);
          });
        },
      }
    }
  }
})