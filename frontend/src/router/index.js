// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

// 페이지 컴포넌트 임포트
import LoginView from '../views/LoginView.vue'
import CheckoutPage from '../views/checkout/CheckoutPage.vue'
import OrderComplete from '../views/checkout/OrderComplete.vue'
import VerifyUploadPage from '../views/VerifyUploadPage.vue'
import AdminPage from '../views/AdminPage.vue'
import OAuthCallback from '../pages/OAuthCallback.vue'

// (선택) 성공/실패 더미 페이지가 없으면 간단한 컴포넌트로 대체해도 됩니다.
const LoginSuccess = { template: '<div style="padding:24px">로그인 성공! 🎉</div>' }
const LoginFailed  = { template: '<div style="padding:24px">로그인 실패 😢</div>' }

// ✅ 먼저 routes 배열을 선언
const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/login-success', component: LoginSuccess },
  { path: '/login-failed',  component: LoginFailed },
  { path: '/checkout', name: 'checkout', component: CheckoutPage, meta: { requiresAuth: true } },
  { path: '/ordercomplete', name: 'ordercomplete', component: OrderComplete, meta: { requiresAuth: true }},
  { path: '/verify-upload', component: VerifyUploadPage },
  { path: '/admin', component:AdminPage},
  { path: '/oauth2/callback', component: OAuthCallback },
]

// 그 다음 라우터 생성 시 routes를 넣어줌

export default createRouter({
  history: createWebHistory(),
  routes,
})

