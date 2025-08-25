import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  // 개발 서버 설정
  server: {
    port: 5173, // 원하는 포트 번호로 설정
  }
});