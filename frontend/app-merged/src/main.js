import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

createApp(App)
  .use(router)  // 라우터 사용
  .mount('#app')