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
</script>

<template>
  <section class="best">
    <!-- 상품 그리드 -->
    <div class="product-grid">
      <RouterLink v-for="p in props.items" :key="p.id" class="product-card"
        :to="{ name: 'product', params: { id: String(p.id) } }" aria-label="상품 상세로 이동">
        <div class="product-card__thumb">
          <img :src="p.thumbJpg" :alt="p.name" loading="lazy" />
        </div>
        <div class="product-card__meta">
          <div class="product-card__name">{{ p.name }}</div>
          <div class="product-card__price">{{ p.price }}</div>
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
  border-radius: 0.125rem;                           /* 2px */
  overflow: hidden;
  transition: transform .12s ease, box-shadow .12s ease;
}
.product-card:hover {
  transform: translateY(-0.125rem);                  /* -2px */
  box-shadow: 0 0.375rem 1.125rem rgba(0,0,0,.06);   /* 6px 18px */
  text-decoration: none;
}
/* 하이퍼링크 visited 사양 유지 */
.product-card:visited {
  color: inherit;
  text-decoration: none;
}

.product-card__thumb {
  aspect-ratio: 1 / 1;
  background: #f6f6f6;
  overflow: hidden;
}
.product-card__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.product-card__meta {
  padding: 0.625rem 0.75rem 0.75rem;                 /* 10px 12px 12px */
  display: flex;
  align-items: flex-start;
  flex-direction: column;
  gap: 0.25rem;                                      /* 4px */
}

.product-card__name {
  font-size: 0.875rem;                               /* 14px */
  line-height: 1.35;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 700;
  color: #333333;
}

.product-card__price {
  font-weight: 700;
  font-size: 0.875rem;                               /* 14px */
  color: #333;
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
}

/* ≤1024px → 3열 */
@media (max-width: 64rem) {                           /* 1024px */
  .product-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .tabs { gap: 1.5rem; }                              /* 간격 완화 */
}

/* ≤768px → 2열 */
@media (max-width: 48rem) {                           /* 768px */
  .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .tab { font-size: 1rem; }                           /* 16px */
}

/* ≤480px → 1열 */
@media (max-width: 30rem) {                           /* 480px */
  .product-grid { grid-template-columns: 1fr; }
  .product-card__name, .product-card__price { font-size: 0.8125rem; } /* 13px */
  .tabs { gap: 1rem; padding: 0.5rem 0 0.75rem; }
}
</style>
