<script setup lang="ts">
import { onMounted, onBeforeUnmount } from 'vue'

onMounted(() => document.body.classList.add('home'))
onBeforeUnmount(() => document.body.classList.remove('home'))
import HeroSlider from '../components/HeroSlider.vue'
import CategoryGrid from '../components/CategoryGrid.vue'
import ProductGrid from '../components/ProductGrid.vue'
import TopButton from '../components/TopButton.vue'
import ChatbotFab from '../components/ChatbotFab.vue'

</script>


<template>
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

<style>
/* 1) 본문 기본 스타일 */
body {
  min-height: 100%;
  position: relative;
  isolation: isolate; /* 네 코드 그대로 유지 */
  margin: 0;
  font-family: "Pretendard", sans-serif;
  color: #2d251c;
  letter-spacing: -0.1px;
  background-color: #ffffff;

  /* 포인트: 가상요소를 뒤로 보낼 안전한 스택 컨텍스트 생성 */
  position: relative;
  isolation: isolate; /* 중요 */
}

/* 2) 공통: 문서의 최상단/최하단에 “배경처럼” 깔릴 레이어 */
body.home::before,
body.home::after {
  content: "";
  position: absolute; /* 문서(body.home) 기준 배치 — 레이아웃 공간 차지 X */
  left: 0;
  right: 0;
  height: 220px; /* 필요 시 180~380px 등으로 조절 */
  pointer-events: none; /* 클릭 방해 X */
  z-index: -1; /* 본문 내용 뒤로 */
  background-size: auto 100%; /* 높이를 먼저 맞추고, 가로는 비율대로 */
  background-repeat: repeat-x;
}

/* 3) 상단 배경: 문서 맨 위에만 */
body.home::before {
  top: 0;
  background-image: url("./img/Up_BG.svg");
  background-position: center top;
}

/* 4) 하단 배경: 문서 맨 아래에만 */
body.home::after {
  bottom: 0;
  background-image: url("./img/Down_BG.svg");
  background-position: center bottom;
}
/* ---------- Filters ---------- */
.filters {
  text-align: center;
  margin: 4px 0 18px;
  color: var(--accent);
  font-weight: 700;
}
.filters a {
  padding: 0 4px;
}
.filters a:hover {
  text-decoration: none;
  color: var(--accent-2);
}
/* (선택) 반응형 높이: 화면 넓으면 조금 더 크게 */
@media (min-width: 960px) {
  body.home::before,
  body.home::after {
    height: clamp(200px, 22vw, 360px);
  }
}
</style>
