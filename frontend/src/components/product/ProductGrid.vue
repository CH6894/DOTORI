<!-- src/components/product/ProductGrid.vue -->
<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import ProductCard from '@/components/product/ProductCard.vue'
import type { CatalogItem } from '@/composables/useCatalog'

const props = defineProps<{
  items: CatalogItem[]
  pageSize?: number      // 한 번에 추가로 그릴 개수(기본 24)
  rootMargin?: string    // 미리 당겨서 로드 (기본 '400px')
}>()

const CHUNK = props.pageSize ?? 24
const margin = props.rootMargin ?? '400px'

const page = ref(1)
const visible = computed(() => props.items.slice(0, page.value * CHUNK))
const hasMore = computed(() => visible.value.length < props.items.length)
const isFetching = ref(false)

const sentinel = ref<HTMLElement|null>(null)
let observer: IntersectionObserver | null = null

function onIntersect(entries: IntersectionObserverEntry[]) {
  const entry = entries[0]
  if (!entry?.isIntersecting) return
  if (!hasMore.value || isFetching.value) return
  isFetching.value = true
  // 살짝 지연을 둬서 스크롤 점프 방지 (실제 API 로딩 타임 대체)
  requestAnimationFrame(() => {
    page.value++
    isFetching.value = false
  })
}

async function setupObserver() {
  await nextTick()
  if (!sentinel.value) return
  cleanupObserver()
  observer = new IntersectionObserver(onIntersect, {
    root: null,
    rootMargin: margin,
    threshold: 0,
  })
  observer.observe(sentinel.value)
}

function cleanupObserver() {
  if (observer) { observer.disconnect(); observer = null }
}

onMounted(setupObserver)
onBeforeUnmount(cleanupObserver)

// 아이템 변경 시 리스트 리셋 + 옵저버 재설치
watch(() => props.items, async () => {
  page.value = 1
  await setupObserver()
})
</script>

<template>
  <div>
    <div class="product-grid" role="list">
      <ProductCard
        v-for="it in visible"
        :key="it.id"
        :item="it"
        role="listitem"
      />

      <div v-if="visible.length === 0" class="empty">상품이 없습니다.</div>
    </div>

    <!-- 무한스크롤 센티넬 (화면 하단 근처에 오면 다음 페이지 로딩) -->
    <div
      ref="sentinel"
      class="infinite-sentinel"
      aria-hidden="true"
    />

    <!-- 로딩 인디케이터 (옵션) -->
    <div v-if="isFetching && hasMore" class="center">
      <div class="spinner" aria-label="로딩 중" />
    </div>
  </div>
</template>

<style scoped>
.product-grid {
  margin-top: 36px;
  column-gap: 16px;   /* 기존 가로 갭 유지 */
  row-gap: 48px;      /* ✅ 세로 갭 넉넉히 */
  display: grid;
  grid-template-columns: repeat(6, minmax(0,1fr));
}

.empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 36px 0;
  color: #9a9a9a;
  border: 1px dashed #e5e5e5;
  border-radius: 12px;
}

.center { display: flex; justify-content: center; margin-top: 16px; }

/* 보이지 않는 센티넬 – 공간은 약간 확보해 스크롤 말미가 너무 붙지 않게 함 */
.infinite-sentinel {
  height: 1px;
}

/* 심플 스피너 */
.spinner {
  width: 28px;
  height: 28px;
  border: 2px solid #eadfc9;
  border-top-color: #9a8d7c;
  border-radius: 50%;
  animation: spin 0.9s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 1280px) {
  .product-grid { grid-template-columns: repeat(4, 1fr); }
}
</style>