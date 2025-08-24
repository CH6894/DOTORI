<script setup lang="ts">
import UtilBar from '../components/UtilBar.vue'
import Header from '../components/Header.vue'
import HeroSlider from '../components/HeroSlider.vue'
import CategoryGrid from '../components/CategoryGrid.vue'
import ProductGrid from '../components/ProductGrid.vue'
import TopButton from '../components/TopButton.vue'
import ChatbotFab from '../components/ChatbotFab.vue'
import "@/assets/styles/main.css";

</script>


<template>
<!-- 상단 얇은 유틸 바 -->
<UtilBar />


<!-- 공용 헤더 -->
<Header />


<main>
<div class="container">
<!-- 히어로 배너: 기존 hero.js를 Vue 컴포넌트로 치환 -->
<HeroSlider />


<!-- 카테고리 -->
<section class="section">
<h2 class="section__title">카테고리</h2>
<CategoryGrid />
</section>

<hr class="divider" />


<!-- 베스트셀러 -->
<section class="section">
<h2 class="section__title center">Best Seller</h2>
<p class="filters">
</p>
<ProductGrid />
</section>
</div>
</main>


<TopButton />
<ChatbotFab />
</template>