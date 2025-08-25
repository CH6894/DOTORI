import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 8081,
    strictPort: true,
    // (옵션) HMR가 끊기면 아래 두 줄도 추가
    hmr: { host: '192.168.198.1', port: 8081 },
  },
})
