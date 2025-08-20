import { createRouter, createWebHistory } from 'vue-router'
import ProductInfo from '@/views/ProductInfo.vue'

const routes = [
  {
    path: '/',
    redirect: '/product/1' 
  },
  {
    path: '/product/:id',
    name: 'ProductInfo',
    component: ProductInfo
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router