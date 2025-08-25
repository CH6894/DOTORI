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
    <RouterLink
      class="product-card__thumb"
      :to="to"
      :aria-label="`${item.name} 상세로 이동`"
    >
      <picture>
        <source :srcset="item.thumbWebp || '/img/placeholder.jpg'" type="image/webp" />
        <img
          :src="item.thumbJpg || '/img/placeholder.jpg'"
          :alt="`${item.name} 이미지`"
          loading="lazy"
          width="300"
          height="300"
        />
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
/* 카드 전체 */
.product-card {
  display: grid;
  grid-template-rows: 1fr auto;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 2px;
  overflow: hidden;
  color: inherit;
  text-decoration: none;
  transition: transform .12s ease, box-shadow .12s ease;
}
.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0,0,0,.06);
  text-decoration: none;
}

/* 썸네일 */
.product-card__thumb {
  display: block;
  aspect-ratio: 1 / 1;
  background: #f6f6f6;
  overflow: hidden;
}
.product-card__thumb picture,
.product-card__thumb img {
  width: 100%;
  height: 100%;
  display: block;
}
.product-card__thumb img { object-fit: cover; }

/* 메타 */
.product-card__meta {
  padding: 10px 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.product-card__name {
  font-size: 14px;
  line-height: 1.35;
  font-weight: 700;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.product-card__price {
  font-weight: 700;
  font-size: 14px;
  color: #333;
}

/* 링크 규칙(요구사항) */
a { color: inherit; text-decoration: none; }
a:visited { color: inherit; text-decoration: none; }
a:hover { text-decoration: none; }
</style>
