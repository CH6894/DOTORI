<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'

/* ===== 기존 헤더 상태 ===== */
const quickOpen = ref(false)
const headerEl = ref<HTMLElement | null>(null)
const quickTop = ref<number>(64)

function toggleQuick() { quickOpen.value = !quickOpen.value }
function measureQuickTop() {
  const el = headerEl.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  const bottom = Math.max(0, Math.round(rect.bottom))
  quickTop.value = bottom
  document.documentElement.style.setProperty('--quickbar-top', `${bottom}px`)
}

/* ===== 검색 패널 상태/로직 추가 ===== */
const searchWrapEl = ref<HTMLElement | null>(null)
const searchOpen = ref(false)
const query = ref('')

type PopularItem = { term: string; rank: number; prevRank?: number }
type RecentItem = { term: string; ts: number }

/* 데모 인기 검색어(랭크는 1이 최고) */
const popular = ref<PopularItem[]>([
  { term: '포토카드', rank: 1, prevRank: 3 },
  { term: '슬리브',   rank: 2, prevRank: 1 },
  { term: '굿즈 캘린더',     rank: 3, prevRank: 5 },
  { term: '한정판',     rank: 4, prevRank: 4 },
  { term: '피규어',     rank: 5, prevRank: 2 },
  { term: '재입고',     rank: 6, prevRank: 6 },
  { term: '티켓',     rank: 7, prevRank: 10 },
  { term: '웰컴키트',        rank: 8, prevRank: 8 },
  { term: '특전',       rank: 9, prevRank: 7 },
  { term: '랜덤가챠',        rank: 10, prevRank: 11 },
])

/* 최근 검색어: localStorage에 저장/불러오기 */
const RECENT_KEY = 'recent-searches'
const recent = ref<RecentItem[]>([])

function loadRecent() {
  try {
    const raw = localStorage.getItem(RECENT_KEY)
    recent.value = raw ? JSON.parse(raw) : []
  } catch { recent.value = [] }
}
function saveRecent() {
  localStorage.setItem(RECENT_KEY, JSON.stringify(recent.value.slice(0, 10)))
}
function addRecent(term: string) {
  const t = term.trim()
  if (!t) return
  recent.value = [{ term: t, ts: Date.now() }, ...recent.value.filter(r => r.term !== t)]
  if (recent.value.length > 10) recent.value.length = 10
  saveRecent()
}
function removeRecent(term: string) {
  recent.value = recent.value.filter(r => r.term !== term)
  saveRecent()
}
function clearRecent() {
  recent.value = []
  saveRecent()
}

/* 등락 계산: prevRank - rank > 0 이면 상승(▲), <0 하락(▼), =0 보합(—/삼각 회색) */
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

/* 제출/선택 */
function onSearchSubmit(e?: Event) {
  e?.preventDefault()
  addRecent(query.value)
  // TODO: 라우터 연결 시 교체
  // router.push({ name:'Search', query:{ q: query.value } })
  // 현재는 주소 이동 없이 패널만 닫음
  searchOpen.value = false
}
function selectTerm(term: string) {
  query.value = term
  onSearchSubmit()
}

/* 포커스/열고닫기 */
function openPanel() { searchOpen.value = true }
function onDocClick(ev: MouseEvent) {
  const root = searchWrapEl.value
  if (root && !root.contains(ev.target as Node)) searchOpen.value = false
}
function onEscKey(ev: KeyboardEvent) {
  if (ev.key === 'Escape') searchOpen.value = false
}

onMounted(async () => {
  await nextTick()
  measureQuickTop()
  loadRecent()
  window.addEventListener('resize', measureQuickTop, { passive: true })
  window.addEventListener('scroll', measureQuickTop, { passive: true })
  ;(document as any).fonts?.ready?.then?.(measureQuickTop)

  document.addEventListener('click', onDocClick)
  document.addEventListener('keydown', onEscKey)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', measureQuickTop)
  window.removeEventListener('scroll', measureQuickTop)
  document.removeEventListener('click', onDocClick)
  document.removeEventListener('keydown', onEscKey)
})
</script>

<template>
  <header class="header" ref="headerEl">
    <div class="container header__inner">
      <!-- 로고 -->
      <div class="header__logo">
        <a class="logo__mark" href="/" aria-label="DOTORI 홈으로">
          <img src="/img/Logo_NonBG.svg" alt="DOTORI 로고" />
        </a>
      </div>

      <!-- 햄버거 + 메뉴 + 검색 + 아이콘 -->
      <div class="header__bottom">
        <!-- 햄버거 버튼 -->
        <button id="hamburgerBtn" class="icon-btn header__hamburger" :aria-expanded="quickOpen"
          aria-controls="quickBar" aria-label="메뉴 열기" @click="toggleQuick">
          <svg width="22" height="18" viewBox="0 0 22 18" aria-hidden="true">
            <rect width="22" height="2" y="0" rx="1" />
            <rect width="22" height="2" y="8" rx="1" />
            <rect width="22" height="2" y="16" rx="1" />
          </svg>
        </button>

        <!-- 메뉴 -->
        <nav class="header__nav">
          <a href="#">도감</a>
          <a href="#">캘린더</a>
          <a href="#">검수기준</a>
        </nav>

        <!-- ===== 검색 (확장) ===== -->
        <div class="search-wrap" ref="searchWrapEl">
          <form class="search" role="search" aria-label="검색" @submit.prevent="onSearchSubmit">
            <input
              v-model="query"
              type="search"
              placeholder="검색어를 입력하세요"
              aria-label="검색어"
              @focus="openPanel"
            />
            <button type="submit" class="search__btn" aria-label="검색">
              <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="11" cy="11" r="7" />
                <path d="M16.5 16.5L22 22" />
              </svg>
            </button>
          </form>

          <!-- 패널 -->
          <div
            id="searchPanel"
            class="search-panel"
            v-show="searchOpen"
            role="region"
            aria-label="검색 제안"
          >
            <div class="panel-grid">
              <!-- 인기 검색어 -->
              <section class="panel-section">
                <div class="panel-title">인기 검색어</div>
                <ol class="pop-list">
                  <li v-for="item in popular.slice(0,10)" :key="item.term" class="pop-item">
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
                  <button
                    v-if="recent.length"
                    type="button"
                    class="clear-all"
                    @click="clearRecent"
                    aria-label="최근 검색어 전체 삭제"
                    title="전체 삭제"
                  >
                    전체삭제
                  </button>
                </div>
                <ul class="recent-list">
                  <li v-if="recent.length === 0" class="recent-empty">최근 검색어가 없습니다.</li>
                  <li v-for="r in recent.slice(0,10)" :key="r.term" class="recent-item">
                    <button type="button" class="recent-link" @click="selectTerm(r.term)">
                      {{ r.term }}
                    </button>
                    <button
                      type="button"
                      class="recent-del"
                      @click.stop="removeRecent(r.term)"
                      aria-label="삭제"
                      title="삭제"
                    >
                      x
                    </button>
                  </li>
                </ul>
              </section>
            </div>
          </div>
        </div>
        <!-- ===== /검색 ===== -->

        <!-- 아이콘 -->
        <div class="header__icons">
          <a class="icon-btn" href="#" aria-label="관심 상품">
            <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M12 21s-8-5.33-8-11a5 5 0 0 1 9-3 5 5 0 0 1 9 3c0 5.67-8 11-8 11z" fill="currentColor"
                stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </a>
          <a class="icon-btn" href="#" aria-label="마이페이지">
            <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="12" cy="8" r="4" fill="currentColor" stroke="currentColor" stroke-width="1.6" />
              <path d="M4 20a8 8 0 0 1 16 0" fill="currentColor" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
            </svg>
          </a>
          <a class="icon-btn" href="#" aria-label="장바구니">
            <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M6 6h15l-1.5 9h-12z" fill="currentColor" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round" />
              <path d="M6 6L5 3H2" fill="currentColor" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
              <circle cx="9" cy="21" r="1.5" fill="currentColor" />
              <circle cx="18" cy="21" r="1.5" fill="currentColor" />
            </svg>
          </a>
        </div>
      </div>
    </div>
  </header>

  <!-- 퀵바 -->
  <div id="quickBar" class="quick-bar" :class="{ 'is-open': quickOpen }" :style="{ top: `${quickTop}px` }"
    role="region" aria-label="빠른 작업 바">
    <nav class="quick-bar__inner container">
      <a href="#">Anime</a>
      <a href="#">KPOP</a>
      <a href="#">Game</a>
      <a href="#">Sports</a>
      <a href="#">Webtoon</a>
      <a href="#">Creator</a>
    </nav>
  </div>
</template>

<style scoped>
/* 검색 래퍼: 패널 절대배치 기준 */
.search-wrap { position: relative; }

/* 패널 박스 */
.search-panel{
  position: absolute;
  top: calc(100% + 8px);
  left: 0; right: 0;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 12px;
  box-shadow: 0 12px 28px rgba(0,0,0,.12);
  padding: 12px;
  z-index: 2000;
}

/* 패널 레이아웃: 2열 */
.panel-grid{
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.panel-section { min-width: 0; }
.panel-title{
  font: 700 13px/1 "Pretendard", system-ui, -apple-system, Segoe UI, Roboto, sans-serif;
  color: #333;
  margin: 6px 0 10px;
}
.panel-title.row{
  display: flex; align-items: center; justify-content: space-between;
}

/* 인기 검색어 */
.pop-list{ list-style: none; margin: 0; padding: 0; }
.pop-item + .pop-item { margin-top: 6px; }
.pop-link{
  width: 100%;
  display: grid;
  grid-template-columns: 24px 1fr auto; /* rank, term, delta */
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid transparent;
  background: #fafafa;
  color: inherit;
  cursor: pointer;
  text-align: left;
}
.pop-link:hover{ background: #f5f5f5; }
.rank{ font-weight: 700; color:#666; text-align: right; }
.term{
  overflow: hidden; white-space: nowrap; text-overflow: ellipsis;
}
.delta{
  font-size: 12px; color:#888; display:inline-flex; align-items:center; gap:4px;
}
.delta.up{ color:#0ea35a; }     /* ▲ 상승 - 초록 */
.delta.down{ color:#e54848; }   /* ▼ 하락 - 빨강 */
.delta.same{ color:#9aa0a6; }   /* — 보합 - 회색 */

/* 최근 검색어 */
.recent-list{ list-style:none; margin:0; padding:0; }
.recent-empty{
  padding: 8px 10px; color:#9aa0a6; background:#fafafa; border-radius:8px;
}
.recent-item{
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  align-items: center;
  padding: 6px 0;
}
.recent-link{
  max-width: 100%;
  padding: 8px 10px;
  border-radius: 8px;
  background:#fafafa;
  border: 1px solid transparent;
  text-align: left;
  overflow: hidden; white-space: nowrap; text-overflow: ellipsis;
}
.recent-link:hover{ background:#f5f5f5; }
.recent-del{
  width: 28px; height: 28px;
  border: 1px solid #eee; border-radius: 6px; background:#fff; color:#666;
  cursor: pointer;
}
.recent-del:hover{ background:#f8f8f8; }
.clear-all{
  border: none; background: transparent; cursor: pointer;
  color:#888; font-size:12px;
}
.clear-all:hover{ color:#555; }

/* 입력창 자체는 기존 스타일 유지 가정, 필요한 경우 보강 */
.search { position: relative; } /* 패널 기준이 form이면 이걸로도 OK */
.search input[type="search"]{ width: 100%; }

/* 작은 화면에서 1열로 */
@media (max-width: 768px){
  .panel-grid{ grid-template-columns: 1fr; }
}
.search {
    width : 400px;
}
</style>
