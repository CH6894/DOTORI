<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'
import type { CatalogItem } from '@/composables/useCatalog'
import { fetchGenre } from '@/api/items'
import type { ItemDTO } from '@/types/item'

onMounted(() => document.body.classList.add('home'))
onBeforeUnmount(() => document.body.classList.remove('home'))
import HeroSlider from '../components/HeroSlider.vue'
import CategoryGrid from '../components/CategoryGrid.vue'
import ProductGrid from '../components/ProductGrid.vue'
import TopButton from '../components/TopButton.vue'
import ChatbotFab from '../components/ChatbotFab.vue'

// 베스트셀러 데이터
const bestSellerItems = ref<CatalogItem[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

// SearchResult의 adapt 함수를 복사해서 사용
function adapt(dto: ItemDTO): CatalogItem {
  const base = import.meta.env.VITE_ASSET_BASE
  const img = dto.itemCode ? `${base}${dto.itemCode}.jpg` : undefined
  console.log(img)
  return {
    id: dto.itemCode,
    name: dto.name || dto.title,
    price: Number(dto.cost ?? 0),
    top_category: dto.genre || '',
    mid_category: dto.title,
    thumbWebp: img,
    thumbJpg: img,
  }
}

// 베스트셀러 데이터 로딩 함수 (SearchResult 패턴 참고)
const loadBestSellerItems = async () => {
  try {
    loading.value = true
    error.value = null

    // 베스트셀러는 인기 장르나 전체 상품에서 가져올 수 있음
    const res = await fetchGenre('Pokemon', { page: 0, size: 12 }) // 예시: 포켓몬 장르
    bestSellerItems.value = (res.content ?? []).map(adapt)
  } catch (e: any) {
    error.value = e?.message ?? '베스트셀러를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(loadBestSellerItems)

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
        <ProductGrid :items="bestSellerItems" />
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
  isolation: isolate;
  margin: 0;
  font-family: "Pretendard", sans-serif;
  color: #2d251c;
  letter-spacing: -0.1px;
  background-color: #ffffff;

  /* 포인트: 가상요소를 뒤로 보낼 안전한 스택 컨텍스트 생성 */
  position: relative;
  isolation: isolate;
}

/* 2) 공통: 문서의 최상단/최하단에 "배경처럼" 깔릴 레이어 */
body.home::before,
body.home::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  height: 220px;
  pointer-events: none;
  z-index: -1;
  background-size: auto 100%;
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

.divider {
  border: 0;
  height: 1px;
  background-color: rgba(45, 37, 28, 0.08);
  margin-block: clamp(16px, 4vw, 40px);
  margin-inline: calc(50% - 50vw);
}
</style>
