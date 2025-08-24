import { createRouter, createWebHistory } from 'vue-router';

import SearchResult from '../views/SearchResult.vue';
import ProductInfo from '../views/ProductInfo.vue';
import MainViews from '../views/MainViews.vue';
import SellPage from '../views/SellPage.vue';

const routes = [
  { path: "/",
    name: "Main", 
    component: MainViews },
  { 
    path: "/sellpage",
    name: "Sellpage",
    component: SellPage },

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