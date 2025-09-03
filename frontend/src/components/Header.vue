<!-- src/components/HeaderWithQuickbar.vue -->
<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import SearchComponent from './Search.vue'
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

/* ===== ESC 키 처리 ===== */
function onEscKey(ev: KeyboardEvent) {
  if (ev.key === 'Escape') {
    quickOpen.value = false
  }
}

onMounted(async () => {
  await nextTick()
  measureQuickTop()

  window.addEventListener('resize', measureQuickTop, { passive: true })
  window.addEventListener('scroll', measureQuickTop, { passive: true })
    ; (document as any).fonts?.ready?.then?.(measureQuickTop)

  document.addEventListener('click', onDocClickCloseQuick) // 👈 문서 아무곳 클릭 시 퀵바 닫기
  document.addEventListener('keydown', onEscKey)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', measureQuickTop)
  window.removeEventListener('scroll', measureQuickTop)
  document.removeEventListener('click', onDocClickCloseQuick)
  document.removeEventListener('keydown', onEscKey)
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

          <!-- ===== 검색 (공통 컴포넌트) ===== -->
          <SearchComponent />
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
          :to="{ name: 'category', query: { top: c.key } }" @click="quickOpen = false">
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
