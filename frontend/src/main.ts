// src/main.ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import axios from 'axios';
import { API_BASE } from './config';

import '@/assets/styles/base.css'
import '@/assets/styles/tokens.css'
import '@/assets/styles/utilities.css'

const app = createApp(App)
const pinia = createPinia()

axios.defaults.baseURL = API_BASE;
app.config.globalProperties.$axios = axios;

app.use(pinia)
app.use(router)
app.mount('#app')
