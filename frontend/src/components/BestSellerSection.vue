<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import ProductGrid from '@/components/ProductGrid.vue'
import type { ItemDTO } from '@/types/item'
import type { CatalogItem } from '@/composables/useCatalog'
import { fetchGenre } from '@/api/items'

// ✅ 부모에서 장르 배열을 내려줄 수 있게(없으면 기본값 사용)
const props = defineProps<{
    genres?: string[]
    pageSize?: number
}>()

const GENRES = props.genres?.length
    ? props.genres
    : ['Anime', 'KPOP', 'Sports', 'Webtoon', 'Game', 'Creator']

const PAGE_SIZE = props.pageSize ?? 6

// 탭/인디케이터 상태
const currentTab = ref(GENRES[0])
const tabsWrap = ref<HTMLElement | null>(null)
const tabBtns = ref<HTMLButtonElement[]>([])
const indicator = ref({ left: 0, width: 0 })

// 데이터 상태
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

async function loadByGenre(genre: string) {
    try {
        loading.value = true
        error.value = null
        const res = await fetchGenre(genre, { page: 0, size: PAGE_SIZE })
        items.value = (res.content ?? []).map(adapt)
    } catch (e: any) {
        error.value = e?.message ?? '베스트셀러를 불러오지 못했습니다.'
    } finally {
        loading.value = false
    }
}

function setTab(genre: string) {
    if (genre === currentTab.value) return
    currentTab.value = genre
    loadByGenre(genre)
}

function updateIndicator() {
    const wrap = tabsWrap.value
    if (!wrap || !tabBtns.value?.length) return
    const idx = GENRES.indexOf(currentTab.value)
    const el = tabBtns.value[idx]
    if (!el) return

    // 가로 스크롤 보정: wrap 내에서의 visible left를 기준
    const wrapRect = wrap.getBoundingClientRect()
    const elRect = el.getBoundingClientRect()
    const left = elRect.left - wrapRect.left + wrap.scrollLeft

    indicator.value = {
        left,               // translateX에 넣을 값 (scrollLeft를 더했으니 transform만 사용)
        width: el.offsetWidth,
    }
}

let ro: ResizeObserver | null = null
function onScroll() {
    // 스크롤해도 밑줄이 버튼 아래를 따라오게
    updateIndicator()
}
function onResize() {
    // 리사이즈 시 위치/폭 재계산
    updateIndicator()
}

onMounted(async () => {
    await loadByGenre(currentTab.value)
    await nextTick()
    updateIndicator()

    // 리사이즈·스크롤 대응
    window.addEventListener('resize', onResize, { passive: true })
    tabsWrap.value?.addEventListener('scroll', onScroll, { passive: true })

    // 폰트 로딩이나 레이아웃 변화 대응
    ro = new ResizeObserver(() => updateIndicator())
    if (tabsWrap.value) ro.observe(tabsWrap.value)
    // 각 버튼 폭 변화도 감지
    tabBtns.value.forEach(btn => ro?.observe(btn))
})

onBeforeUnmount(() => {
    window.removeEventListener('resize', onResize)
    tabsWrap.value?.removeEventListener('scroll', onScroll)
    ro?.disconnect()
})

watch(currentTab, async () => {
    await nextTick()
    updateIndicator()
})
</script>

<template>
    <section class="section">
        <h2 class="section__title center">Best Seller</h2>

        <!-- 🔹 장르 탭 (마이페이지 인디케이터 패턴 복제) -->
        <div class="tabs-container">
            <nav class="tabs" role="tablist" aria-label="베스트셀러 장르" ref="tabsWrap">
                <span class="tabs__indicator"
                    :style="{ width: indicator.width + 'px', transform: `translateX(${indicator.left}px)` }" />
                <button v-for="(g, i) in GENRES" :key="g" ref="tabBtns" class="tab"
                    :class="{ 'is-active': currentTab === g }" role="tab" :aria-selected="currentTab === g"
                    @click="setTab(g)">
                    {{ g }}
                </button>
            </nav>
        </div>

        <!-- 🔹 콘텐츠 -->
        <div v-if="error" class="error">{{ error }}</div>
        <div v-else>
            <div v-if="loading" class="skeleton-grid">
                <div v-for="n in PAGE_SIZE" :key="n" class="skeleton-card" />
            </div>
            <ProductGrid v-else :items="items" :maxItems="PAGE_SIZE" />
        </div>
    </section>
</template>

<style scoped>
/* 탭 컨테이너 */
.tabs-container {
    display: flex;
    justify-content: center;
    margin-bottom: 16px;
}

/* 탭 바 */
.tabs {
    position: relative;
    display: inline-flex;
    justify-content: center;
    gap: 8px;
    padding: 8px;
    border-bottom: 1px solid rgba(45, 37, 28, 0.08);
    /* 장르 많을 때 가로 스크롤 */
    scrollbar-width: thin;
}

/* 밑줄 인디케이터 */
.tabs__indicator {
    position: absolute;
    left: 0;
    bottom: -1px;
    height: 2px;
    background: var(--accent, #6b4eff);
    transition: transform 200ms ease, width 200ms ease;
    will-change: transform, width;
    pointer-events: none;
}

/* 버튼 */
.tab {
    position: relative;
    padding: 10px 12px;
    border: 0;
    background: transparent;
    font-weight: 600;
    color: #6b6b6b;
    white-space: nowrap;
    cursor: pointer;
}

.tab.is-active {
    color: #2d251c;
}

/* 로딩 스켈레톤 */
.skeleton-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-top: 16px;
}

@media (min-width: 768px) {
    .skeleton-grid {
        grid-template-columns: repeat(4, 1fr);
    }
}

.skeleton-card {
    height: 280px;                                    /* ProductGrid와 동일한 높이 (200px 이미지 + 80px 메타) */
    border-radius: 12px;
    background: linear-gradient(90deg, #f2f2f2, #eaeaea, #f2f2f2);
    background-size: 200% 100%;
    animation: shimmer 1.2s infinite;
    box-shadow: 0 0.125rem 0.5rem rgba(0,0,0,.08);  /* ProductGrid와 동일한 그림자 */
}

@keyframes shimmer {
    0% {
        background-position: 200% 0;
    }

    100% {
        background-position: -200% 0;
    }
}

.error {
    color: #c0362c;
    font-weight: 600;
    margin-top: 12px;
}
</style>
