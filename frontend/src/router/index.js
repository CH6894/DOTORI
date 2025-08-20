import { createRouter, createWebHistory } from 'vue-router';

import SearchResult from '../views/SearchResult.vue';


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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;