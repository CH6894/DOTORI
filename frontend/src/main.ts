// src/main.ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import axios from 'axios';
import { API_BASE } from './config';

// 글로벌 스타일 (당신 코드에 있었던 부분 유지)
import '@/assets/styles/base.css'
import '@/assets/styles/tokens.css'
import '@/assets/styles/utilities.css'
import '@/assets/styles/links.css'

const app = createApp(App)


// 팀원 코드 스타일을 따라 바로 등록
app.use(createPinia())
app.use(router)

app.mount('#app')
    