import { createApp } from "vue";
import { createPinia } from 'pinia'
import App from "./App.vue";
import router from "./router";
import axios from 'axios';
import { API_BASE } from './config.js';


// 1. Vue 애플리케이션 인스턴스를 생성하고 'app' 변수에 할당합니다.
const app = createApp(App);

// 2. 'app' 변수를 사용하여 필요한 설정을 순서대로 추가합니다.
// Vue 라우터를 앱에 연결합니다.
app.use(createPinia()).use(router);

// Axios의 기본 URL을 설정하고 Vue의 전역 속성으로 등록합니다.
axios.defaults.baseURL = API_BASE;
app.config.globalProperties.$axios = axios;

// 3. 마지막으로 애플리케이션을 '#app' 엘리먼트에 마운트합니다.
app.mount("#app");