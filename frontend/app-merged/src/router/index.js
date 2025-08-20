import { createRouter, createWebHistory } from 'vue-router';

import SearchResult from '../views/SearchResult.vue';
import ProductInfo from '../views/ProductInfo.vue';

const routes = [
  {
    path: '/',
    name: 'SearchResult',
    redirect: '/search',
  },
  {
    path: '/search',
    name: 'Search',
    component: SearchResult
  },
  {
    path: '/product/:id',
    name: 'Product',
    component: ProductInfo,
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;