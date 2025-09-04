<!-- src/components/ProductGrid.vue -->
<script setup lang="ts">
import { RouterLink } from 'vue-router'

type Item = {
  id: number | string
  name: string
  price: number
  top_category: string
  mid_category?: string
  thumbWebp?: string
  thumbJpg?: string
}

const props = defineProps<{ items: Item[] }>()

const formatPrice = (n: number) => {
  try { return n?.toLocaleString?.() ?? String(n) }
  catch { return String(n) }
}

// 이미지 로딩 에러 핸들링
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  if (img.src !== '/img/placeholder.jpg') {
    img.src = '/img/placeholder.jpg'
  }
}

// 이미지 로딩 완료 핸들링
const handleImageLoad = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.opacity = '1'
}
</script>

<template>
  <section class="best">
    <!-- 상품 그리드 -->
    <div class="product-grid">
      <RouterLink v-for="p in props.items" :key="p.id" class="product-card"
        :to="{ name: 'product', params: { id: String(p.id) } }" aria-label="상품 상세로 이동">
        <div class="product-card__thumb">
          <img 
            :src="p.thumbJpg || '/img/placeholder.jpg'" 
            :alt="p.name" 
            loading="lazy" 
            @error="handleImageError"
            @load="handleImageLoad"
          />
        </div>
        <div class="product-card__meta">
          <div class="product-card__name">{{ p.name }}</div>
          <div class="product-card__price">{{ formatPrice(p.price) }}원</div>
        </div>
      </RouterLink>

      <div v-if="props.items.length === 0" class="empty">해당 태그의 상품이 없어요.</div>
    </div>

  </section>
</template>

<style scoped>
.product-grid {
  margin-top: 1.25rem;                               /* 20px */
  display: grid;
  gap: 1rem;                                         /* 16px */
  grid-template-columns: repeat(6, minmax(0, 1fr));  /* ≥1280px 기본 6열 */
}

.product-card {
  display: grid;
  grid-template-rows: 1fr auto;
  color: inherit;
  text-decoration: none;
  background: #ffffff;
  border: 0.0625rem solid #eee;                      /* 1px */
  border-radius: 0.75rem;                            /* 12px - 카테고리와 동일 */
  overflow: hidden;
  transition: transform .2s ease, box-shadow .2s ease;
  box-shadow: 0 0.125rem 0.5rem rgba(0,0,0,.08);    /* 카테고리와 동일한 그림자 */
}
.product-card:hover {
  transform: translateY(-0.1875rem);                 /* -3px - 카테고리와 동일 */
  box-shadow: 0 0.625rem 1.625rem rgba(0,0,0,.10);  /* 카테고리와 동일한 호버 그림자 */
  text-decoration: none;
}
/* 하이퍼링크 visited 사양 유지 */
.product-card:visited {
  color: inherit;
  text-decoration: none;
}

.product-card__thumb {
  position: relative;
  background: #f6f6f6;
  overflow: hidden;
  height: 12.5rem;                                   /* 200px - 고정 높이 설정 */
  width: 100%;                                       /* 전체 너비 사용 */
}
.product-card__thumb img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card__meta {
  padding: 0.75rem 1rem 1rem;                        /* 12px 16px 16px - 카테고리와 유사 */
  display: flex;
  align-items: flex-start;
  flex-direction: column;
  gap: 0.375rem;                                     /* 6px */
  background: #ffffff;
}

.product-card__name {
  font-size: 0.875rem;                               /* 14px */
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;                             /* 2줄로 제한 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 600;
  color: #2d251c;                                    /* 카테고리와 동일한 색상 */
  width: 100%;
  min-height: 2.8em;                                 /* 2줄 높이 보장 */
}

.product-card__price {
  font-weight: 700;
  font-size: 0.9375rem;                              /* 15px - 약간 크게 */
  color: #FC703C;                                    /* 요청된 색상 */
}

.empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 2.25rem 0;                                /* 36px */
  color: #9a9a9a;
  border: 0.0625rem dashed #e5e5e5;                  /* 1px */
  border-radius: 0.75rem;                            /* 12px */
}

.center {
  display: flex;
  justify-content: center;
  margin-top: 1rem;                                   /* 16px */
}

.best { width: 100%; }

/* --------- Tabs (중앙 정렬) --------- */
.tabs {
  position: relative;
  display: flex;
  justify-content: center;
  gap: 2.25rem;                                      /* 36px */
  padding: 0.75rem 0 1.125rem;                       /* 12px 0 18px */
  align-items: flex-end;
  width: 100%;
}

.tab {
  background: none;
  border: 0;
  padding: 0;
  font: 700 1.125rem/1.2 "Pretendard", system-ui, -apple-system, "Segoe UI", Roboto, sans-serif; /* 18px */
  color: #b8b8b8;
  cursor: pointer;
}
.tab[aria-selected="true"] { color: #222; }

.tabs__track {
  position: absolute;
  bottom: 0;
  height: 0.1875rem;                                 /* 3px */
  background: #e5e5e5;
  border-radius: 999px;
  transition: left .2s ease, width .2s ease;
}
.tabs__indicator {
  position: absolute;
  bottom: 0;
  height: 0.1875rem;                                 /* 3px */
  background: #333;
  border-radius: 999px;
  transition: left .25s ease, width .25s ease;
}

/* ---------- Buttons ---------- */
.btn-more {
  display: inline-block;
  margin-top: 1.125rem;                              /* 18px */
  padding: 0.625rem 1.75rem;                         /* 10px 28px */
  border-radius: 999px;
  border: 0.0625rem solid #e3d7c2;                   /* 1px */
  background: #fff7ea;
  font-weight: 700;
  cursor: pointer;
}
.btn-more:hover { background: #ffefdb; }

/* ===== Breakpoints ===== */
/* ≤1280px → 4열 */
@media (max-width: 80rem) {                           /* 1280px */
  .product-grid { grid-template-columns: repeat(4, minmax(0, 1fr)); }
  .product-card__thumb { height: 11.25rem; }          /* 180px */
}

/* ≤1024px → 3열 */
@media (max-width: 64rem) {                           /* 1024px */
  .product-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .product-card__thumb { height: 10rem; }             /* 160px */
  .tabs { gap: 1.5rem; }                              /* 간격 완화 */
}

/* ≤768px → 2열 */
@media (max-width: 48rem) {                           /* 768px */
  .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .product-card__thumb { height: 9.375rem; }          /* 150px */
  .tab { font-size: 1rem; }                           /* 16px */
}

/* ≤480px → 1열 */
@media (max-width: 30rem) {                           /* 480px */
  .product-grid { grid-template-columns: 1fr; }
  .product-card__thumb { height: 8.75rem; }           /* 140px */
  .product-card__name, .product-card__price { font-size: 0.8125rem; } /* 13px */
  .tabs { gap: 1rem; padding: 0.5rem 0 0.75rem; }
}
</style>
