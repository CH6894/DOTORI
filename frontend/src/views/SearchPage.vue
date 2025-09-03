<!-- src/views/SearchPage.vue -->
<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ProductGrid from '@/components/product/ProductGrid.vue'
import type { CatalogItem } from '@/composables/useCatalog'
import { searchItems } from '@/api/items'
import type { ItemDTO } from '@/types/item'
import type { Page } from '@/types/common'

const route = useRoute()
const router = useRouter()

const searchQuery = ref<string>((route.query.q as string) || '')
const items = ref<CatalogItem[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

function adapt(dto: ItemDTO): CatalogItem {
  const base = import.meta.env.VITE_ASSET_BASE
  const img = dto.itemCode ? `${base}${dto.itemCode}.jpg` : undefined
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

// 검색어로 아이템 찾기
async function loadBySearch(query: string) {
  try {
    loading.value = true
    error.value = null
    const res: Page<ItemDTO> = await searchItems(query, { page: 0, size: 60 })
    items.value = (res.content ?? []).map(adapt)
  } catch (e: any) {
    error.value = e?.message ?? '검색 결과를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

// 초기 로드
onMounted(() => {
  if (searchQuery.value) {
    loadBySearch(searchQuery.value)
  }
})

// 검색어 변경 감지
watch(searchQuery, (q) => {
  if (q) {
    loadBySearch(q)
  } else {
    items.value = []
  }
})

// URL 동기화
watch(searchQuery, (sq) => {
  const q: Record<string, any> = { ...route.query }
  
  if (sq) {
    q.q = sq
  } else {
    delete q.q
  }
  
  router.replace({ query: q })
})

watch(() => route.query, (q) => {
  if (typeof q.q === 'string' && q.q !== searchQuery.value) {
    searchQuery.value = q.q
  }
})
</script>

<template>
  <div class="container mx-auto w-[1280px] px-0">
    <!-- 검색 결과 헤더 -->
    <div v-if="searchQuery" class="search-header">
      <h1 class="search-title">"{{ searchQuery }}" 검색 결과</h1>
      <p class="search-count">{{ items.length }}개의 상품을 찾았습니다.</p>
    </div>

    <!-- 검색어가 없을 때 -->
    <div v-else class="empty-state">
      <h1 class="empty-title">검색어를 입력해주세요</h1>
      <p class="empty-description">상단의 검색창에서 원하는 상품을 검색해보세요.</p>
    </div>

    <!-- 상태 표시 -->
    <div v-if="loading" class="py-12 text-center text-gray-500">
      <div class="loading-spinner"></div>
      <p>검색 중...</p>
    </div>
    
    <div v-else-if="error" class="py-12 text-center text-red-600">
      <p>{{ error }}</p>
    </div>

    <!-- 검색 결과가 없을 때 -->
    <div v-else-if="searchQuery && items.length === 0" class="py-12 text-center text-gray-500">
      <h2 class="text-xl font-semibold mb-2">검색 결과가 없습니다</h2>
      <p>"{{ searchQuery }}"에 대한 검색 결과를 찾을 수 없습니다.</p>
      <p class="mt-2">다른 검색어로 시도해보세요.</p>
    </div>

    <!-- 상품 목록 -->
    <ProductGrid v-else-if="items.length > 0" :items="items" />
  </div>
</template>

<style scoped>
/* 1280 컨테이너 */
.container { 
  max-width: 1280px; 
}

/* 검색 결과 헤더 */
.search-header {
  margin-top: 36px;
  margin-bottom: 24px;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.search-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.search-count {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 빈 상태 */
.empty-state {
  margin-top: 80px;
  text-align: center;
}

.empty-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.empty-description {
  font-size: 16px;
  color: #666;
  margin: 0;
}

/* 로딩 스피너 */
.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #670600;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 그리드 세로 간격 */
:deep(.product-grid) {
  margin-top: 24px;
  column-gap: 16px;
  row-gap: 28px;
}
</style>
