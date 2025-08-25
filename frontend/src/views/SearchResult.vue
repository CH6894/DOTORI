<!-- src/views/SearchResult.vue -->
<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TopTabs from '@/components/filters/TopTabs.vue'
import MidTabs from '@/components/filters/MidTabs.vue'
import ProductGrid from '@/components/product/ProductGrid.vue'
import { useCatalog, type Item } from '@/composables/useCatalog'

// 기존 JS 데이터 import 호환
import * as TopCat from '@/assets/TopCategoryData.js'
import * as MidCat from '@/assets/MidCategoryData.js'
import * as Items  from '@/assets/ItemData.js'

// default or named 대응
const TopCategoryData: string[] = (TopCat as any).default ?? TopCat.TopCategoryData ?? []
const MidCategoryData: string[] = (MidCat as any).default ?? MidCat.MidCategoryData ?? []
const ItemData: Item[]          = (Items  as any).default ?? Items.ItemData ?? []

const route = useRoute()
const router = useRouter()

const top = ref<string>((route.query.top as string) || 'Animation')
const mid = ref<string>((route.query.mid as string) || '')

const { filtered } = useCatalog(ItemData, top, mid)

const showMid = computed(() => top.value === 'Animation')

const loading = ref(false)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    loading.value = true
    // 필요 시 API 호출 자리(지금은 로컬 데이터 사용)
  } catch (e: any) {
    error.value = e?.message ?? '목록을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
})

// URL <-> 상태 동기화
watch([top, mid], ([t, m]) => {
  router.replace({ query: { ...route.query, top: t, mid: m } })
})

watch(() => route.query, (q) => {
  if (typeof q.top === 'string' && q.top !== top.value) top.value = q.top
  if (typeof q.mid === 'string' && q.mid !== mid.value) mid.value = q.mid
})
</script>

<template>
  <div class="container mx-auto w-[1280px] px-0">
    <!-- Top Tabs -->
    <TopTabs
      :items="TopCategoryData"
      v-model="top"
      class="mt-5"
    />

    <!-- Mid Tabs -->
    <MidTabs
      :items="MidCategoryData"
      v-model="mid"
      :visible="showMid"
      class="mb-3"
    />

    <!-- 상태 -->
    <div v-if="loading" class="py-12 text-center text-gray-500">불러오는 중…</div>
    <div v-else-if="error" class="py-12 text-center text-red-600">{{ error }}</div>

    <!-- 목록 -->
    <ProductGrid v-else :items="filtered" />
  </div>
</template>

<style scoped>
.container { /* 1280 고정 컨테이너 */
  max-width: 1280px;
}
</style>
