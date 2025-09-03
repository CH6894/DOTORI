<!-- src/components/product/ProductCard.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

/** Item 타입은 프로젝트의 useCatalog.ts와 동일 구조를 기대합니다. */
type Item = {
  id: number | string
  name: string
  price: number
  top_category?: string
  mid_category?: string
  thumbWebp?: string
  thumbJpg?: string
}

const props = defineProps<{ item: Item }>()
const to = computed(() => ({ name: 'product', params: { id: String(props.item.id) } }))

const priceText = computed(() =>
  Number.isFinite(props.item.price)
    ? props.item.price.toLocaleString('ko-KR') + ' 원'
    : String(props.item.price)
)
</script>

<template>
  <article class="product-card" role="article">
    <RouterLink class="product-card__thumb" :to="to" :aria-label="`${item.name} 상세로 이동`">
      <picture>
        <source :srcset="item.thumbWebp || '/img/placeholder.jpg'" type="image/webp" />
        <img :src="item.thumbJpg || '/img/placeholder.jpg'" :alt="`${item.name} 이미지`" loading="lazy" width="300"
          height="300" />
      </picture>
    </RouterLink>

    <div class="product-card__meta">
      <RouterLink class="product-card__name" :to="to">
        {{ item.name }}
      </RouterLink>
      <div class="product-card__price">{{ priceText }}</div>
    </div>
  </article>
</template>

<style scoped>
/* 그리드 컨테이너: 192px 고정 칼럼을 가로에 꽉 차게 배치 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(192px, 192px));
  justify-content: center;
  gap: 16px;
  align-items: start;
}

/* 기본 수축 허용(말줄임 등 동작) */
.products-grid>* {
  min-width: 0;
  min-height: 0;
}

/* ===== 카드: 폭을 192로 고정 + 첫 행(썸네일) 192로 고정 ===== */
.product-card {
  width: 192px;
  display: grid;
  grid-template-rows: 192px auto;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  color: inherit;
  text-decoration: none;
  transition: transform .12s ease, box-shadow .12s ease, border-color .12s ease;
  outline: none;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, .06);
  text-decoration: none;
  border-color: #e6e6e6;
}

.product-card:focus-visible {
  box-shadow: 0 0 0 3px #1a73e8;
  transform: translateY(-1px);
}

/* 링크 :visited 요구사항 */
a {
  text-decoration: none;
  color: inherit;
}

a:visited {
  text-decoration: none;
  color: inherit;
}

/* ===== 썸네일: 정확히 192×192, 이미지 채우기 ===== */
.product-card__thumb {
  width: 192px;
  height: 192px;
  background: #f6f6f6;
  overflow: hidden;
}

.product-card__thumb picture,
.product-card__thumb img {
  width: 100%;
  height: 100%;
  display: block;
}

.product-card__thumb img {
  object-fit: cover;
}

/* 잘리는 걸 허용하고 꽉 채움 */

/* ===== 메타: 이름 2줄 + 가격 1줄로 고정감을 줌 ===== */
.product-card__meta {
  padding: 10px 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.product-card__name {
  font-size: 14px;
  line-height: 1.35;
  font-weight: 700;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  white-space: normal;
  min-height: calc(1.35 * 14px * 2);

  .product-card__price {
    font-weight: 700;
    font-size: 14px;
    color: #333;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>