// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    // 프로젝트 루트 기준 절대 경로 별칭
    alias: { '@': '/src' },
  },
  build: {
    outDir: "../backend/src/main/resources/static"
  },
  server: {
    proxy: {
      '/api': { target: 'http://localhost:8081', changeOrigin: true }
    },
  },
})
