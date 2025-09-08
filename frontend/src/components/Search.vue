<!-- src/components/Search.vue -->
<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

/* ===== 검색 상태 ===== */
const query = ref('')
const searchOpen = ref(false)
const searchWrapEl = ref<HTMLElement | null>(null)

/* ===== 최근 검색어 관리 ===== */
const RECENT_KEY = 'search_recent'
const recent = ref<{ term: string; timestamp: number }[]>([])

function loadRecent() {
  try {
    const stored = localStorage.getItem(RECENT_KEY)
    if (stored) {
      recent.value = JSON.parse(stored)
    }
  } catch {
    recent.value = []
  }
}

function addRecent(term: string) {
  const trimmed = term.trim()
  if (!trimmed) return
  
  // 기존 항목 제거
  recent.value = recent.value.filter(r => r.term !== trimmed)
  
  // 새 항목 추가 (최대 20개)
  recent.value.unshift({ term: trimmed, timestamp: Date.now() })
  recent.value = recent.value.slice(0, 20)
  
  // 저장
  try {
    localStorage.setItem(RECENT_KEY, JSON.stringify(recent.value))
  } catch {}
}

function removeRecent(term: string) {
  recent.value = recent.value.filter(r => r.term !== term)
  try {
    localStorage.setItem(RECENT_KEY, JSON.stringify(recent.value))
  } catch {}
}

function clearRecent() {
  recent.value = []
  try {
    localStorage.removeItem(RECENT_KEY)
  } catch {}
}

/* ===== 인기 검색어 (임시 데이터) ===== */
const popular = ref([
  { term: '에스파', rank: 1, delta: 2 },
  { term: '홀로라이브', rank: 2, delta: -1 },
  { term: '원피스', rank: 3, delta: 0 },
  { term: '귀멸의칼날', rank: 4, delta: 3 },
  { term: '화산귀환', rank: 5, delta: 1 },
])

function delta(item: any) {
  return item.delta || 0
}

function deltaClass(item: any) {
  const d = delta(item)
  return d > 0 ? 'up' : d < 0 ? 'down' : 'same'
}

function deltaLabel(item: any) {
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

function handleSearch() {
  goSearch(query.value)
}

function selectTerm(term: string) {
  query.value = term
  goSearch(term)
}

/* ===== 포커스/열고닫기 ===== */
function openPanel() { 
  searchOpen.value = true 
}

function onDocClick(ev: MouseEvent) {
  const root = searchWrapEl.value
  if (root && !root.contains(ev.target as Node)) searchOpen.value = false
}

function onEscKey(ev: KeyboardEvent) {
  if (ev.key === 'Escape') {
    searchOpen.value = false
  }
}

onMounted(() => {
  loadRecent()
  document.addEventListener('click', onDocClick)
  document.addEventListener('keydown', onEscKey)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
  document.removeEventListener('keydown', onEscKey)
})
</script>

<template>
  <div class="search-wrap" ref="searchWrapEl">
    <form class="search" role="search" aria-label="검색" @submit.prevent="handleSearch">
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

    <!-- 검색 제안 패널 -->
    <div id="searchPanel" class="search-panel" v-show="searchOpen" role="region" aria-label="검색 제안">
      <div class="panel-grid">
        <!-- 인기 검색어 -->
        <section class="panel-section">
          <div class="panel-title row">인기 검색어</div>
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
                ×
              </button>
            </li>
          </ul>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.search-wrap {
  position: relative;
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
  right: 5%;
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
</style>