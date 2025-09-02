<!-- src/components/HeaderWithQuickbar.vue -->
<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
// import { useAuth } from '@/stores/useAuth' // 실제 스토어 사용 시

// const auth = useAuth()
// const isLoggedIn = computed(() => auth.isLoggedIn)
const isLoggedIn = computed(() => false) // 임시: 나중에 실제 값으로 교체

function getKV(): Storage {
  // 로그인: localStorage(항상 유지), 비로그인: sessionStorage(탭/브라우저 닫히면 삭제)
  return isLoggedIn.value ? localStorage : sessionStorage
}

/* ===== 헤더/퀵바 상태 ===== */
const router = useRouter()
const quickOpen = ref(false)
const headerEl = ref<HTMLElement | null>(null)
const quickTop = ref<number>(64)

// 햄버거/퀵바 ref (바깥 클릭 닫기용)
const hamburgerBtn = ref<HTMLElement | null>(null)
const quickBarEl = ref<HTMLElement | null>(null)

function toggleQuick() { quickOpen.value = !quickOpen.value }
function measureQuickTop() {
  const el = headerEl.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  const bottom = Math.max(0, Math.round(rect.bottom))
  quickTop.value = bottom
  document.documentElement.style.setProperty('--quickbar-top', `${bottom}px`)
}

/* 문서 아무 곳이나 클릭 시 퀵바 닫기 (햄버거/퀵바 내부는 제외) */
function onDocClickCloseQuick(ev: MouseEvent) {
  if (!quickOpen.value) return
  const target = ev.target as Node
  if (hamburgerBtn.value?.contains(target)) return
  if (quickBarEl.value?.contains(target)) return
  quickOpen.value = false
}

/* 퀵바 카테고리 → /search?top=키 */
const quickCats = [
  { label: 'Anime', key: 'Anime' },
  { label: 'KPOP', key: 'Kpop' },
  { label: 'Game', key: 'Game' },
  { label: 'Sports', key: 'Sports' },
  { label: 'Webtoon', key: 'Webtoon' },
  { label: 'Creator', key: 'Creator' },
]

/* ===== 검색 패널 상태 ===== */
const searchWrapEl = ref<HTMLElement | null>(null)
const searchOpen = ref(false)
const query = ref('')

type PopularItem = { term: string; rank: number; prevRank?: number }
type RecentItem = { term: string; ts: number }

/* 데모 인기 검색어 */
const popular = ref<PopularItem[]>([
  { term: '포토카드', rank: 1, prevRank: 3 },
  { term: '슬리브', rank: 2, prevRank: 1 },
  { term: '굿즈 캘린더', rank: 3, prevRank: 5 },
  { term: '한정판', rank: 4, prevRank: 4 },
  { term: '피규어', rank: 5, prevRank: 2 },
  { term: '재입고', rank: 6, prevRank: 6 },
  { term: '티켓', rank: 7, prevRank: 10 },
  { term: '웰컴키트', rank: 8, prevRank: 8 },
  { term: '특전', rank: 9, prevRank: 7 },
  { term: '랜덤가챠', rank: 10, prevRank: 11 },
])

const RECENT_KEY = 'recent-searches'
const recent = ref<RecentItem[]>([])

function loadRecent() {
  try {
    const raw = getKV().getItem(RECENT_KEY)
    recent.value = raw ? JSON.parse(raw) : []
  } catch {
    recent.value = []
  }
}
function saveRecent() {
  const trimmed = recent.value.slice(0, 10)
  recent.value = trimmed
  getKV().setItem(RECENT_KEY, JSON.stringify(trimmed))
}
function addRecent(term: string) {
  const t = term.trim()
  if (!t) return
  recent.value = [{ term: t, ts: Date.now() }, ...recent.value.filter(r => r.term !== t)]
  saveRecent()
}
function removeRecent(term: string) {
  recent.value = recent.value.filter(r => r.term !== term)
  saveRecent()
}
function clearRecent() {
  recent.value = []
  getKV().removeItem(RECENT_KEY)
}

/* 등락 표기 */
function delta(item: PopularItem) {
  if (item.prevRank == null) return 0
  return item.prevRank - item.rank
}
function deltaClass(item: PopularItem) {
  const d = delta(item)
  return d > 0 ? 'up' : d < 0 ? 'down' : 'same'
}
function deltaLabel(item: PopularItem) {
  const d = delta(item)
  return d > 0 ? `▲${d}` : d < 0 ? `▼${Math.abs(d)}` : '—'
}

/* ===== 검색 실행/제출 ===== */
function goSearch(term: string) {
  const q = term.trim()
  if (!q) return
  addRecent(q)
  searchOpen.value = false
  router.push({ name: 'search', query: { q } })
}
function onSearchSubmit(e?: Event) {
  e?.preventDefault()
  goSearch(query.value)
}
function selectTerm(term: string) {
  query.value = term
  goSearch(term)
}

/* 포커스/열고닫기 */
function openPanel() { searchOpen.value = true }

function onDocClick(ev: MouseEvent) {
  const root = searchWrapEl.value
  if (root && !root.contains(ev.target as Node)) searchOpen.value = false
}

function onEscKey(ev: KeyboardEvent) {
  if (ev.key === 'Escape') {
    searchOpen.value = false
    quickOpen.value = false
  }
}

onMounted(async () => {
  await nextTick()
  measureQuickTop()
  loadRecent()

  window.addEventListener('resize', measureQuickTop, { passive: true })
  window.addEventListener('scroll', measureQuickTop, { passive: true })
    ; (document as any).fonts?.ready?.then?.(measureQuickTop)

  document.addEventListener('click', onDocClick)
  document.addEventListener('click', onDocClickCloseQuick) // 👈 문서 아무곳 클릭 시 퀵바 닫기
  document.addEventListener('keydown', onEscKey)

  // 비로그인 시 세션 저장소 정리(선택)
  window.addEventListener('beforeunload', () => {
    if (!isLoggedIn.value) {
      try { sessionStorage.removeItem(RECENT_KEY) } catch { }
    }
  })

  // 로그인 상태에서는 탭 간 동기화(선택)
  window.addEventListener('storage', (e) => {
    if (!isLoggedIn.value) return
    if (e.key === RECENT_KEY) {
      loadRecent()
    }
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', measureQuickTop)
  window.removeEventListener('scroll', measureQuickTop)
  document.removeEventListener('click', onDocClick)
  document.removeEventListener('click', onDocClickCloseQuick)
  document.removeEventListener('keydown', onEscKey)
  // 익명 핸들러 해제는 생략 가능
})
</script>

<template>
  <div class="header-root">
    <header class="header" ref="headerEl">
      <div class="container header__inner">
        <!-- 로고 -->
        <div class="header__logo">
          <RouterLink class="logo__mark" to="/" aria-label="DOTORI 홈으로">
            <img src="/img/Logo_NonBG.svg" alt="DOTORI 로고" />
          </RouterLink>
        </div>

        <div class="header__bottom">
          <!-- 햄버거 버튼 -->
          <button id="hamburgerBtn" ref="hamburgerBtn" class="icon-btn header__hamburger" :aria-expanded="quickOpen"
            aria-controls="quickBar" aria-label="메뉴 열기" @click="toggleQuick">
            <svg width="22" height="18" viewBox="0 0 22 18" aria-hidden="true">
              <rect width="22" height="2" y="0" rx="1" />
              <rect width="22" height="2" y="8" rx="1" />
              <rect width="22" height="2" y="16" rx="1" />
            </svg>
          </button>

          <!-- 상단 메뉴(예시) -->
          <nav class="header__nav">
            <RouterLink to="/dex">도감</RouterLink>
            <RouterLink to="/calendar">캘린더</RouterLink>
            <RouterLink to="/inspection">검수기준</RouterLink>
          </nav>

          <!-- ===== 검색 (확장) ===== -->
          <div class="search-wrap" ref="searchWrapEl">
            <form class="search" role="search" aria-label="검색" @submit.prevent="onSearchSubmit">
              <input v-model="query" type="search" placeholder="검색어를 입력하세요" aria-label="검색어" @focus="openPanel" />
              <button type="submit" class="search__btn" aria-label="검색">
                <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true">
                  <circle cx="11" cy="11" r="7" />
                  <path d="M16.5 16.5L22 22" />
                </svg>
              </button>
            </form>

            <!-- 검색 제안 패널 -->
            <div id="searchPanel" class="search-panel" v-show="searchOpen" role="region" aria-label="검색 제안">
              <div class="panel-grid">
                <!-- 인기 검색어 -->
                <section class="panel-section">
                  <div class="panel-title">인기 검색어</div>
                  <ol class="pop-list">
                    <li v-for="item in popular.slice(0, 10)" :key="item.term" class="pop-item">
                      <button type="button" class="pop-link" @click="selectTerm(item.term)">
                        <span class="rank">{{ item.rank }}</span>
                        <span class="term">{{ item.term }}</span>
                        <span class="delta" :class="deltaClass(item)">{{ deltaLabel(item) }}</span>
                      </button>
                    </li>
                  </ol>
                </section>

                <!-- 최근 검색어 -->
                <section class="panel-section">
                  <div class="panel-title row">
                    <span>최근 검색어</span>
                    <button v-if="recent.length" type="button" class="clear-all" @click="clearRecent"
                      aria-label="최근 검색어 전체 삭제" title="전체 삭제">
                      전체삭제
                    </button>
                  </div>
                  <ul class="recent-list">
                    <li v-if="recent.length === 0" class="recent-empty">최근 검색어가 없습니다.</li>
                    <li v-for="r in recent.slice(0, 10)" :key="r.term" class="recent-item">
                      <button type="button" class="recent-link" @click="selectTerm(r.term)">
                        {{ r.term }}
                      </button>
                      <button type="button" class="recent-del" @click.stop="removeRecent(r.term)" aria-label="삭제"
                        title="삭제">
                        x
                      </button>
                    </li>
                  </ul>
                </section>
              </div>
            </div>
          </div>
          <!-- ===== /검색 ===== -->

          <!-- 우측 아이콘 -->
          <div class="header__icons">
            <RouterLink class="icon-btn" :to="{ name: 'mypage-wish' }" aria-label="관심 상품">
              <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M12 21s-8-5.33-8-11a5 5 0 0 1 9-3 5 5 0 0 1 9 3c0 5.67-8 11-8 11z" fill="currentColor"
                  stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </RouterLink>
            <RouterLink class="icon-btn" :to="{ name: 'mypage' }" aria-label="마이페이지">
              <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="12" cy="8" r="4" fill="currentColor" stroke="currentColor" stroke-width="1.6" />
                <path d="M4 20a8 8 0 0 1 16 0" fill="currentColor" stroke="currentColor" stroke-width="1.6"
                  stroke-linecap="round" />
              </svg>
            </RouterLink>
            <RouterLink class="icon-btn" :to="{ name: 'cart' }" aria-label="장바구니">
              <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M6 6h15l-1.5 9h-12z" fill="currentColor" stroke="currentColor" stroke-width="1.6"
                  stroke-linejoin="round" />
                <path d="M6 6L5 3H2" fill="currentColor" stroke="currentColor" stroke-width="1.6"
                  stroke-linecap="round" />
                <circle cx="9" cy="21" r="1.5" fill="currentColor" />
                <circle cx="18" cy="21" r="1.5" fill="currentColor" />
              </svg>
            </RouterLink>
          </div>
        </div>
      </div>
    </header>

    <!-- ===== 오버레이: 퀵바 열렸을 때 아무곳이나 클릭 시 닫힘 ===== -->
    <div v-show="quickOpen" class="quickbar-overlay" aria-hidden="true" @click="quickOpen = false" />

    <!-- ===== 퀵바 ===== -->
    <div id="quickBar" ref="quickBarEl" class="quick-bar" :class="{ 'is-open': quickOpen }"
      :style="{ top: `${quickTop}px` }" role="region" aria-label="빠른 작업 바">
      <nav class="quick-bar__inner container">
        <RouterLink v-for="c in quickCats" :key="c.key" class="quick-link"
          :to="{ name: 'search', query: { top: c.key } }" @click="quickOpen = false">
          {{ c.label }}
        </RouterLink>
      </nav>
    </div>
  </div>
</template>

<style scoped>
/* 헤더: 맨 위 레이어 */
.header {
  position: sticky;
  top: 0;
  z-index: 50;
  width: 100%;
  background: linear-gradient(180deg, rgba(247, 242, 230, 0.85), rgba(247, 242, 230, 0.85));
  backdrop-filter: blur(0.375rem);
  border-bottom: 0.0625rem solid #f4f3e6;
}

/* 헤더 하단 커튼 */
.header::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: -0.0625rem;
  height: 0.75rem;
  background: linear-gradient(to bottom, #f7f2e6 70%, rgba(247, 242, 230, 0));
  pointer-events: none;
}

/* 퀵바 */
.quick-bar {
  position: fixed;
  left: 0;
  right: 0;
  top: var(--quickbar-top, 0);
  height: var(--quickbar-height);
  background: rgba(45, 37, 28, 0.96);
  color: #fff;
  transform: translateY(-100%);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transition: transform .28s cubic-bezier(0.2, 0.8, 0.2, 1), opacity .28s, visibility 0s .28s;
  z-index: 49;
  box-shadow: 0 0.5rem 1.25rem rgba(0, 0, 0, .18);
  backdrop-filter: saturate(160%) blur(0.5rem);
  overflow: hidden;
}

.quick-bar.is-open {
  transform: translateY(0);
  opacity: 1;
  visibility: visible;
  pointer-events: auto;
  transition: transform .28s cubic-bezier(0.2, 0.8, 0.2, 1), opacity .28s;
}

.quick-bar__inner {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 2.25rem;
  padding: 0 1rem;
  white-space: nowrap;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
}

/* 클릭 오버레이 (시각 영향 없이 클릭만 받음) */
.quickbar-overlay {
  position: fixed;
  inset: 0;
  z-index: 48;
  /* .quick-bar(49) 바로 아래 */
  background: transparent;
}

/* 아이콘 버튼 */
.icon-btn {
  display: inline-grid;
  place-items: center;
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 0.75rem;
  border: 0.0625rem solid #e7dfcd;
  background: #fff7ea;
  box-shadow: 0 0.125rem 0 rgba(0, 0, 0, .02) inset;
  cursor: pointer;
  color: var(--ink);
}

.icon-btn svg {
  width: 1.375rem;
  height: 1.375rem;
  display: block;
}

/* 22px */

/* 헤더 아이콘 영역 */
.header__icons {
  display: flex;
  gap: 0.625rem;
  margin-left: 2.5rem;
  fill: #505050;
}

/* 네비게이션 링크 */
.nav-link {
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  font-weight: 700;
  padding: 0.375rem 0.5rem;
  border-radius: 0.625rem;
  font-size: 1.25rem;
}

.nav-link:hover {
  background: #fff0df;
  text-decoration: none;
}

/* 로고 */
.logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-left: 0.5rem;
}

.logo__mark {
  display: inline-block;
  width: 12.5rem;
}

.logo__mark img {
  width: 100%;
  height: auto;
}

.logo.-small .logo__mark {
  font-size: 1.375rem;
}

.logo.-small .logo__text {
  font-weight: 800;
  font-size: 0.875rem;
}

/* 헤더 레이아웃 */
.header__inner {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-width: 80rem;
  /* 1280px */
  margin: 0 auto;
  padding: 0.75rem 0;
}

.header__logo {
  display: flex;
  justify-content: center;
}

.header__bottom {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 1.25rem;
  margin-left: 0.75rem;
  margin-top: 0.75rem;
  background: transparent !important;
}

.header__nav {
  margin-bottom: 0.5rem;
  display: flex;
  gap: 6.25rem;
  /* 100px */
  white-space: nowrap;
  font-weight: 700;
  font-size: 1.125rem;
  /* 18px */
}

/* 검색 래퍼(패널 기준) */
.search-wrap {
  position: relative;
}

/* 검색 폼 */
.search {
  position: relative;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 0.5rem;
  align-items: center;
  width: 25rem;
  /* 400px */
  margin-left: 7.5rem;
  /* 120px */
  margin-right: 1.875rem;
  /* 30px */
}

/* 검색 input */
.search input,
.search input[type="search"] {
  width: 100%;
  height: 2.5rem;
  padding: 0 0.875rem;
  border-radius: 999px;
  border: 0.0625rem solid #eadfc9;
}

/* 검색 버튼 */
.search__btn {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 999px;
  border: 0.0625rem solid #eadfc9;
  background: #fff;
  cursor: pointer;
}

.search__btn svg {
  stroke: var(--ink);
}

.search__btn svg * {
  fill: none !important;
  stroke: currentColor !important;
  stroke-width: 2 !important;
}

/* 검색 패널 */
.search-panel {
  position: absolute;
  top: calc(100% + 0.5rem);
  left: 0;
  right: 0;
  background: #fff;
  border: 0.0625rem solid #eee;
  border-radius: 0.75rem;
  box-shadow: 0 0.75rem 1.75rem rgba(0, 0, 0, .12);
  padding: 0.75rem;
  z-index: 2000;
}

/* 검색 패널 레이아웃 */
.panel-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.panel-section {
  min-width: 0;
}

.panel-title {
  font: 700 0.8125rem/1 "Pretendard", system-ui, -apple-system, Segoe UI, Roboto, sans-serif;
  color: #333;
  margin: 0.375rem 0 0.625rem;
}

.panel-title.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 인기 검색어 */
.pop-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.pop-item+.pop-item {
  margin-top: 0.375rem;
}

.pop-link {
  width: 100%;
  display: grid;
  grid-template-columns: 1.5rem 1fr auto;
  /* 24px */
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.625rem;
  border-radius: 0.5rem;
  border: 0.0625rem solid transparent;
  background: #fafafa;
  color: inherit;
  cursor: pointer;
  text-align: left;
}

.pop-link:hover {
  background: #f5f5f5;
}

.rank {
  font-weight: 700;
  color: #666;
  text-align: right;
}

.term {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.delta {
  font-size: 0.75rem;
  color: #888;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.delta.up {
  color: #0ea35a;
}

.delta.down {
  color: #e54848;
}

.delta.same {
  color: #9aa0a6;
}

/* 최근 검색어 */
.recent-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.recent-empty {
  padding: 0.5rem 0.625rem;
  color: #9aa0a6;
  background: #fafafa;
  border-radius: 0.5rem;
}

.recent-item {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 0.5rem;
  align-items: center;
  padding: 0.375rem 0;
}

.recent-link {
  max-width: 100%;
  padding: 0.5rem 0.625rem;
  border-radius: 0.5rem;
  background: #fafafa;
  border: 0.0625rem solid transparent;
  text-align: left;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.recent-link:hover {
  background: #f5f5f5;
}

.recent-del {
  width: 1.75rem;
  height: 1.75rem;
  border: 0.0625rem solid #eee;
  border-radius: 0.375rem;
  background: #fff;
  color: #666;
  cursor: pointer;
}

.recent-del:hover {
  background: #f8f8f8;
}

.clear-all {
  border: none;
  background: transparent;
  cursor: pointer;
  color: #888;
  font-size: 0.75rem;
}

.clear-all:hover {
  color: #555;
}

/* 작은 화면에서 1열 */
@media (max-width: 48rem) {
  .panel-grid {
    grid-template-columns: 1fr;
  }

  .search {
    width: min(92vw, 25rem);
    margin-left: 0.75rem;
    margin-right: 0.75rem;
  }

  .header__icons {
    margin-left: 0;
  }

  .header__nav {
    gap: 2rem;
    font-size: 1rem;
  }
}

/* 요구사항: :visited 스타일 */
a:visited {
  text-decoration: none;
  color: inherit;
}
</style>
