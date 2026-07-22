import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/ws': {
        target: 'http://localhost:8080',
        ws: true,
        changeOrigin: true
      },
      
      '/admin/chat': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/admin\/chat/, '/api/chat')
      },
      '/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/emp': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/login': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/register': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/send-code': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/logout': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/upload-avatar': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/user-info': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})