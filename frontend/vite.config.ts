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
          target: 'http://49.50.135.201:8081',
          changeOrigin: true,
          secure: false,
        },
        '/open': {
          target: 'http://49.50.135.201:8081',
          changeOrigin: true,
        },
        '/static': 'http://49.50.135.201:8081',
      }
    }
})