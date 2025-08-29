// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }, 
  },
  server: {
    host: '0.0.0.0', 
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,        
      },
      '/open': {
        // /api 경로는 로그인과 같은 인증에 사용하니 새로운 접근 주소를 만들어줌.
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/static': 'http://localhost:8081',
    }
  }
})