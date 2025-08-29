<!-- src/views/SearchResult.vue -->
<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TopTabs from '@/components/filters/TopTabs.vue'
import MidTabs from '@/components/filters/MidTabs.vue'
import ProductGrid from '@/components/product/ProductGrid.vue'
import { useCatalog, type Item } from '@/composables/useCatalog'

// rest api에서 가져온 데이터 로드
import { fetchItems } from '@/api/items'
import * as Items from '@/assets/ItemData.js'

// 기존 JS 데이터 import 호환
import * as TopCat from '@/assets/TopCategoryData.js'
import { MidCategoryMap } from '@/assets/MidCategoryMap'

const route = useRoute()
const router = useRouter()

const top = ref<string>((route.query.top as string) || 'Animation')
const mid = ref<string>((route.query.mid as string) || '')

// 정렬 상태: 필드 + 방향(같은 아이콘 다시 누르면 asc/desc 토글)
type SortField = 'price' | 'popularity' | 'sales'
type SortDir = 'asc' | 'desc'
const sortField = ref<SortField>((route.query.sf as SortField) || 'price')
const sortDir   = ref<SortDir>((route.query.sd as SortDir) || 'desc')

// 데이터 로드(로컬 데모)
const TopCategoryData: string[] = (TopCat as any).default ?? TopCat.TopCategoryData ?? []
const ItemData: Item[] = (Items as any).default ?? Items.ItemData ?? []
const midOptions = computed<string[]>(() => MidCategoryMap[top.value] ?? [])

// 데이터 불러오기
export type Item = {
  id : number | string
  title?: string
  name?: string
  price?: number
}

// 필터링 → 정렬
const { filtered } = useCatalog(ItemData, top, mid)
const sorted = computed(() => {
  const arr = [...filtered.value]
  const key = sortField.value
  const dir = sortDir.value
  return arr.sort((a: any, b: any) => {
    const av = a?.[key] ?? 0
    const bv = b?.[key] ?? 0
    return dir === 'asc' ? av - bv : bv - av
  })
})

// 아이콘 클릭: 같은 필드면 방향 토글, 다른 필드면 해당 필드로 전환하고 기본 desc
function onClickSort(field: SortField) {
  if (sortField.value === field) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDir.value = 'desc'
  }
}

const loading = ref(false)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    loading.value = true
  } catch (e: any) {
    error.value = e?.message ?? '목록을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
})

// 상위 탭 바뀌면 mid 유효성 보정
watch(top, (t) => {
  const opts = MidCategoryMap[t] ?? []
  if (!opts.includes(mid.value)) mid.value = ''
})

// URL 동기화 (top/mid + 정렬)
watch([top, mid, sortField, sortDir], ([t, m, sf, sd]) => {
  const q: Record<string, any> = { ...route.query, top: t, sf, sd }
  if (t === 'Animation' && m) q.mid = m
  else delete q.mid
  router.replace({ query: q })
})
watch(() => route.query, (q) => {
  if (typeof q.top === 'string' && q.top !== top.value) top.value = q.top
  if (typeof q.mid === 'string' && q.mid !== mid.value) mid.value = q.mid
  if (typeof q.sf  === 'string' && q.sf  !== sortField.value) sortField.value = q.sf as SortField
  if (typeof q.sd  === 'string' && q.sd  !== sortDir.value)   sortDir.value   = q.sd as SortDir
})
</script>

<template>
  <div class="container mx-auto w-[1280px] px-0">
    <div class="category-tabs">
      <!-- Top Tabs -->
      <TopTabs :items="TopCategoryData" v-model="top" />

      <!-- Mid Tabs -->
      <MidTabs :items="midOptions" v-model="mid" :visible="true" />

      <!-- 정렬 아이콘 바 -->
      <div class="sortbar" role="toolbar" aria-label="정렬">
        <!-- 가격 -->
        <button
          type="button"
          class="sorticon"
          :class="{ active: sortField === 'price' }"
          :aria-pressed="sortField === 'price'"
          :aria-label="`가격순 (${sortField==='price' ? (sortDir==='asc'?'오름차순':'내림차순') : '선택안됨'})`"
          title="가격순"
          @click="onClickSort('price')"
        >
          <!-- price tag icon -->
          <svg viewBox="0 0 24 24" aria-hidden="true" class="ic">
            <path d="M21 10.5 13.5 3a2.12 2.12 0 0 0-1.5-.62H5a2 2 0 0 0-2 2v7a2.12 2.12 0 0 0 .62 1.5L11 20.5a2.12 2.12 0 0 0 3 0L21 13.5a2.12 2.12 0 0 0 0-3Z" />
            <circle cx="7.5" cy="7.5" r="1.5" />
          </svg>
          <svg v-if="sortField==='price'" viewBox="0 0 24 24" aria-hidden="true" class="arrow">
            <path v-if="sortDir==='asc'" d="M12 5l-5 6h10l-5-6zM7 15h10v2H7z"/>
            <path v-else             d="M7 7h10v2H7zM12 19l5-6H7l5 6z"/>
          </svg>
          <span class="label">가격</span>
        </button>

        <!-- 인기 -->
        <button
          type="button"
          class="sorticon"
          :class="{ active: sortField === 'popularity' }"
          :aria-pressed="sortField === 'popularity'"
          :aria-label="`인기순 (${sortField==='popularity' ? (sortDir==='asc'?'오름차순':'내림차순') : '선택안됨'})`"
          title="인기순"
          @click="onClickSort('popularity')"
        >
          <!-- star icon -->
          <svg viewBox="0 0 24 24" aria-hidden="true" class="ic">
            <path d="M12 2l2.9 6.26 6.9.6-5.2 4.5 1.6 6.64L12 16.9 5.8 20l1.6-6.64L2.2 8.86l6.9-.6L12 2z"/>
          </svg>
          <svg v-if="sortField==='popularity'" viewBox="0 0 24 24" aria-hidden="true" class="arrow">
            <path v-if="sortDir==='asc'" d="M12 5l-5 6h10l-5-6zM7 15h10v2H7z"/>
            <path v-else             d="M7 7h10v2H7zM12 19l5-6H7l5 6z"/>
          </svg>
          <span class="label">인기</span>
        </button>

        <!-- 판매 -->
        <button
          type="button"
          class="sorticon"
          :class="{ active: sortField === 'sales' }"
          :aria-pressed="sortField === 'sales'"
          :aria-label="`판매순 (${sortField==='sales' ? (sortDir==='asc'?'오름차순':'내림차순') : '선택안됨'})`"
          title="판매순"
          @click="onClickSort('sales')"
        >
          <!-- cart icon -->
          <svg viewBox="0 0 24 24" aria-hidden="true" class="ic">
            <path d="M7 6h13l-1.5 9H8.2L6.5 4H3" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round" stroke-linecap="round"/>
            <circle cx="9" cy="21" r="1.6"/>
            <circle cx="18" cy="21" r="1.6"/>
          </svg>
          <svg v-if="sortField==='sales'" viewBox="0 0 24 24" aria-hidden="true" class="arrow">
            <path v-if="sortDir==='asc'" d="M12 5l-5 6h10l-5-6zM7 15h10v2H7z"/>
            <path v-else             d="M7 7h10v2H7zM12 19l5-6H7l5 6z"/>
          </svg>
          <span class="label">판매</span>
        </button>
      </div>
    </div>

    <!-- 상태 -->
    <div v-if="loading" class="py-12 text-center text-gray-500">불러오는 중…</div>
    <div v-else-if="error" class="py-12 text-center text-red-600">{{ error }}</div>

    <!-- 목록: 정렬된 결과 전달 -->
    <ProductGrid v-else :items="sorted" />
  </div>
</template>

<style scoped>
/* 1280 컨테이너 */
.container { max-width: 1280px; }

/* 탭 영역 간격 */
.category-tabs { margin-top: 36px; margin-bottom: 10px; }

/* 선택된 Top 탭 아래 얇은 이음새(책갈피) */
.category-tabs :deep(.top_category_link.selected){ position: relative; }
.category-tabs :deep(.top_category_link.selected)::after{
  content:""; position:absolute; left:-1px; right:-1px; bottom:-4px; height:4px;
  background:#EFECC6; pointer-events:none;
  border-left:1px solid #eadfc9; border-right:1px solid #eadfc9;
}

/* Mid 박스 접합 */
.category-tabs :deep(.mid_category_list){
  position: relative; z-index: 1;
  background:#EFECC6; border:1px solid #eadfc9; border-top:none;
  border-radius:0 6px 6px 6px; padding:6px 10px; margin-top:-4px;
}

/* ===== 정렬 아이콘 바 ===== */
.sortbar{
  display:flex; align-items:center; gap:12px;
  padding:8px 10px;
  background: #fff;
  border:1px solid #eadfc9; border-top:none;
  border-radius:0 0 6px 6px;
  margin-top:-3px;
}
.sorticon{
  display:inline-flex; align-items:center; gap:6px;
  border:0; background:transparent; cursor:pointer;
  padding:6px 8px; border-radius:10px; color:#6e6253; font-weight:700;
}
.sorticon:hover{ background: rgba(0,0,0,.04); }
.sorticon.active{ color:#670600; background: rgba(103,6,0,.06); }

/* 아이콘 크기 */
.ic{ width:20px; height:20px; display:block; }
.ic path, .ic circle { fill: currentColor; }

/* 방향표시는 활성 항목에만 보임 */
.arrow{ width:16px; height:16px; display:block; color: currentColor; }
.arrow path { fill: currentColor; }

/* 레이블(텍스트) */
.label{ font-size:13px; }

/* 그리드 세로 간격 넉넉히 */
:deep(.product-grid){
  margin-top: 24px;
  column-gap: 16px;
  row-gap: 28px;
}
</style>
