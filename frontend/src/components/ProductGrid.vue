<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'

/** 1) 탭 라벨 & 키(데이터용) 분리 */
const tabs = ['Anime', 'KPOP', 'Sports', 'Webtoon', 'Game', 'Creator']
const tabKeys = ['anime', 'kpop', 'sports', 'webtoon', 'game', 'creator'] as const
const active = ref(0)
const activeKey = computed(() => tabKeys[active.value])

/** 2) 데모 상품 (thumb: 이미지 경로 추가) */
type Product = {
  id: number
  name: string
  price: string
  tags: string[]      // 예: ['anime', 'kpop']
  thumb: string       // 예: '/assets/products/1.webp'
}

const products = ref<Product[]>(
  Array.from({ length: 300 }, (_, i) => {
    const group = i % tabKeys.length
    return {
      id: i + 1,
      name: `제품 ${i + 1}`,
      price: `${((i % 20) + 1) * 1000}원`,
      tags: [tabKeys[group]],                           // 각 탭 키 하나씩 배분
      thumb: new URL(`../assets/products/${(i % 12) + 1}.webp`, import.meta.url).href
    }
  })
)

/** 3) 탭별 필터링 */
const filtered = computed(() =>
  products.value.filter(p => p.tags.includes(activeKey.value))
)

/** 4) More 버튼 (페이지네이션: 6개씩 추가) */
const pageSize = 6
const visibleCount = ref(pageSize)
const visibleProducts = computed(() => filtered.value.slice(0, visibleCount.value))
const hasMore = computed(() => visibleCount.value < filtered.value.length)
const loadMore = () => {
  visibleCount.value = Math.min(visibleCount.value + pageSize, filtered.value.length)
}

/** 탭 변경 시 첫 페이지로 리셋 */
watch(active, () => { visibleCount.value = pageSize })

/** 5) 인디케이터/트랙 계산 (중앙 정렬 대응) */
const tabRefs = ref<HTMLElement[]>([])
const setTabRef = (el: HTMLElement | null, idx: number) => { if (el) tabRefs.value[idx] = el }

const indicator = ref<{ left: string; width: string }>({ left: '0px', width: '0px' })
const track = ref<{ left: string; width: string }>({ left: '0px', width: '0px' })

const updateBars = () => {
  const list = tabRefs.value.filter(Boolean)
  const current = tabRefs.value[active.value]
  if (!current || list.length === 0) return

  // 활성 밑줄
  indicator.value = {
    left: `${current.offsetLeft}px`,
    width: `${current.offsetWidth}px`,
  }

  // 회색 트랙: 탭 묶음 전체 길이만
  const first = list[0]
  const last = list[list.length - 1]
  const left = first.offsetLeft
  const width = last.offsetLeft + last.offsetWidth - left
  track.value = { left: `${left}px`, width: `${width}px` }
}

onMounted(async () => { await nextTick(); updateBars(); window.addEventListener('resize', updateBars) })
watch(active, async () => { await nextTick(); updateBars() })
</script>

<template>
  <section class="best">
    <!-- 탭 -->
    <div class="tabs" role="tablist" aria-label="상품 태그">
      <button
        v-for="(t, i) in tabs"
        :key="t"
        class="tab"
        role="tab"
        :aria-selected="active === i"
        @click="active = i"
        :ref="el => setTabRef(el as HTMLElement, i)"
      >
        {{ t }}
      </button>

      <!-- 짧은 회색 트랙 -->
      <div class="tabs__track" :style="track" aria-hidden="true"></div>
      <!-- 활성 탭 밑줄 -->
      <div class="tabs__indicator" :style="indicator" aria-hidden="true"></div>
    </div>

    <!-- 상품 그리드 -->
    <div class="product-grid">
      <a v-for="p in visibleProducts" :key="p.id" class="product-card" href="#">
        <div class="product-card__thumb">
          <!-- 접근성 위해 img 사용 (레이아웃은 CSS로 관리) -->
          <img :src="p.thumb" :alt="p.name" loading="lazy" />
        </div>
        <div class="product-card__meta">
          <div class="product-card__name">{{ p.name }}</div>
          <div class="product-card__price">{{ p.price }}</div>
        </div>
      </a>

      <div v-if="visibleProducts.length === 0" class="empty">해당 태그의 상품이 없어요.</div>
    </div>

    <!-- More 버튼 (남은 상품 있을 때만) -->
    <div class="center" v-if="hasMore">
      <button class="btn-more" type="button" @click="loadMore">More</button>
    </div>
  </section>
</template>

<style scoped>
.best { width: 100%; }

/* --------- Tabs (중앙 정렬) --------- */
.tabs {
  position: relative;
  display: flex;
  justify-content: center;   
  gap: 36px;
  padding: 12px 0 18px;
  align-items: flex-end;
  width: 100%;
}
.tab {
  background: none; border: 0; padding: 0;
  font: 700 18px/1.2 "Pretendard", system-ui, -apple-system, "Segoe UI", Roboto, sans-serif;
  color: #b8b8b8; cursor: pointer;
}
.tab[aria-selected="true"] { color: #222; }

/* 트랙/인디케이터 */
.tabs__track {
  position: absolute; bottom: 0; height: 3px;
  background: #e5e5e5; border-radius: 999px;
  transition: left .2s ease, width .2s ease;
}
.tabs__indicator {
  position: absolute; bottom: 0; height: 3px;
  background: #333; border-radius: 999px;
  transition: left .25s ease, width .25s ease;
}

/* --------- Product grid --------- */
.product-grid {
  margin-top: 20px;
  display: grid; gap: 16px;
  grid-template-columns: repeat(6, minmax(0, 1fr));
}
.product-card {
  display: grid; grid-template-rows: 1fr auto;
  color: inherit; text-decoration: none; background: #ffffff; 
  border: 1px solid #eee; border-radius: 2px; overflow: hidden;
  transition: transform .12s ease, box-shadow .12s ease;
}
.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0,0,0,.06);
}
.product-card__thumb {
  aspect-ratio: 1 / 1;
  background: #f6f6f6;
  overflow: hidden;
}
.product-card__thumb img {
  width: 100%; height: 100%;
  object-fit: cover; display: block;
}
.product-card__meta {
  padding: 10px 12px 12px;
  display: flex;
  align-items: flex-start;
  flex-direction: column;  
  gap: 4px;   
}
.product-card__name {
  font-size: 14px;
  line-height: 1.35;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.product-card__price {
  font-weight: 700;
  font-size: 14px;
  color: #333;
}.empty {
  grid-column: 1 / -1; text-align: center; padding: 36px 0;
  color: #9a9a9a; border: 1px dashed #e5e5e5; border-radius: 12px;
}
.center { display: flex; justify-content: center; margin-top: 16px; }

@media (max-width: 1280px) { .product-grid { grid-template-columns: repeat(4, 1fr); } }
</style>
