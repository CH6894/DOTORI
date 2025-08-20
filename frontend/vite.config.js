import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    // ✅ '@/...' 경로 인식을 위해 반드시 추가
    alias: { '@': path.resolve(__dirname, 'src') }
  },
  server: {
    // ✅ fetch('/api/...') 호출을 백엔드로 프록시 (주소는 환경에 맞게 바꾸세요)
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }

})
