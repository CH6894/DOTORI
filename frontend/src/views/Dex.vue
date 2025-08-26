<!-- src/views/Dex.vue -->
<template>
  <div class="pokemon-collection">
    <!-- 등록 섹션 -->
    <div class="register-section">
      <div class="register-container">
        <div class="register-bar">
          <div class="search-input-wrapper">
            <input type="text" class="register-input" placeholder="영수증 번호 또는 인증코드를 입력하세요" v-model="registerCode"
              @keypress.enter="registerCard" />
            <button v-if="registerCode" class="clear-btn" @click="clearSearch" title="전체 삭제">✕</button>
          </div>
          <button class="register-btn" @click="registerCard">등록하기</button>
        </div>
      </div>
    </div>

    <!-- 카테고리 탭 -->
    <section class="product_list_wrap">
      <div class="product_list_area" role="region" aria-label="도감 카테고리">
        <div class="container-1280">
          <TopTabsAdapter v-model="currentCategory" :items="categories" aria-label="상위 카테고리" />
        </div>
        <div class="container-1280">
          <MidTabsAdapter v-model="currentSubCategory" :items="currentSubCategories" aria-label="중위 카테고리"
            :visible="currentSubCategories.length > 0" />
        </div>
      </div>
    </section>

    <!-- 메인 컨텐츠 -->
    <div class="main-content">
      <div class="content-header">
        <div class="header-right">
          <div class="result-count">{{ resultText }}</div>
          <div class="sort-options">
            <button :class="['sort-btn', { active: currentSort === 'number' }]" @click="toggleSort" title="번호순 정렬">
              ⇅ 번호순
            </button>
          </div>
        </div>
      </div>

      <!-- 카드 그리드 -->
      <div class="card-grid-container" v-if="showPokemonGrid && visiblePokemonCards.length">
        <div class="card-grid">
          <div v-for="card in visiblePokemonCards" :key="card.id" class="pokemon-card"
            :style="{ cursor: card.special ? 'pointer' : 'default' }"
            @click.prevent.stop="card.special && showCardDetail(card)">
            <div class="card-image-container">
              <img :src="card.image" :alt="card.name" class="pokemon-card-image" loading="lazy"
                @error="handleImageError" />
              <div v-if="card.verified" class="verified-badge">✓</div>
            </div>
          </div>
        </div>

        <!-- 로딩 인디케이터 -->
        <div v-if="isLoading" class="loading">
          <div class="loader" aria-label="로딩 중"></div>
        </div>
        <!-- 무한 스크롤 센티넬 -->
        <div ref="sentinel" class="infinite-sentinel" aria-hidden="true"></div>
      </div>

      <!-- 다른 카테고리 빈 상태 -->
      <div class="empty-state" v-else>
        <div class="empty-icon">📦</div>
        <h3>{{ currentCategoryName }} 준비 중입니다</h3>
        <p>해당 카테고리의 콘텐츠는 곧 업데이트될 예정입니다.</p>
      </div>
    </div>

    <!-- 게임 스타일 모달 (Teleport로 문서 끝으로 이동) -->
    <teleport to="body">
      <div v-if="selectedCard" class="game-modal-overlay" @click="closeModal">
        <div class="game-modal" role="dialog" aria-modal="true" aria-labelledby="dex-modal-title" @click.stop>
          <div class="modal-header">
            <div class="modal-title" id="dex-modal-title">포켓몬 도감</div>
            <button class="modal-close" @click="closeModal" aria-label="닫기">✕</button>
          </div>

          <div class="modal-body">
            <div class="card-display">
              <img :src="selectedCard.image" :alt="selectedCard.name" class="modal-card-image" loading="lazy" />
            </div>

            <div class="card-details">
              <div class="pokemon-name-section">
                <h2 class="pokemon-name">{{ selectedCard.name }}</h2>
                <span class="pokemon-number">#{{ selectedCard.number }}</span>
              </div>

              <div class="pokemon-type">
                <span class="type-label">타입:</span>
                <span class="type-value">{{ getTypeKorean(selectedCard.type) }}</span>
              </div>

              <div class="pokemon-stats">
                <div class="stat-item">
                  <span class="stat-label">HP</span>
                  <div class="stat-bar">
                    <div class="stat-fill hp-bar"
                      :style="{ width: `${Math.min(100, (selectedCard.hp / 120) * 100)}%` }"></div>
                  </div>
                  <span class="stat-value">{{ selectedCard.hp }}</span>
                </div>

                <div class="stat-item">
                  <span class="stat-label">공격력</span>
                  <div class="stat-bar">
                    <div class="stat-fill attack-bar"
                      :style="{ width: `${Math.min(100, (selectedCard.attack / 100) * 100)}%` }"></div>
                  </div>
                  <span class="stat-value">{{ selectedCard.attack }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button class="confirm-btn" @click="closeModal">확인</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import TopTabsAdapter from '@/components/filters/TopTabsAdapter.vue'
import MidTabsAdapter from '@/components/filters/MidTabsAdapter.vue'

// 이미지
import pokecardback from '@/assets/pokemon/pokecardback.svg'
import pokecardf from '@/assets/pokemon/pokecardf.svg'
import pokecardp from '@/assets/pokemon/pokecardp.svg'
import pokecardm from '@/assets/pokemon/pokecardm.svg'

type Category = { id: string; name: string }
type SubCategory = { id: string; name: string }
type PokemonCard = {
  id: number
  name: string
  type:
  | 'normal' | 'fighting' | 'flying' | 'poison' | 'ground' | 'bug' | 'rock' | 'steel' | 'ghost'
  | 'fire' | 'water' | 'grass' | 'electric' | 'psychic' | 'ice' | 'dragon' | 'dark' | 'fairy'
  number: string
  hp: number
  attack: number
  emoji?: string
  rarity?: 'common' | 'rare'
  special: boolean
  verified?: boolean
  image: string
}

// 특수 카드 정의
const SPECIALS: Record<number, Partial<PokemonCard>> = {
  1: { name: '파이리', type: 'fire', emoji: '🔥', rarity: 'rare', special: true, image: pokecardf },
  10: { name: '모나피', type: 'grass', emoji: '🌊', rarity: 'rare', special: true, image: pokecardm },
  25: { name: '피카츄', type: 'electric', emoji: '⚡', rarity: 'rare', special: true, image: pokecardp },
}

// 카드 생성기
function makeCards(n = 151): PokemonCard[] {
  return Array.from({ length: n }, (_, i) => {
    const id = i + 1
    const base: PokemonCard = {
      id,
      name: '포켓몬카드',
      type: 'normal',
      number: String(id).padStart(3, '0'),
      hp: 40 + (id % 17) * 5,     // 40~120
      attack: 40 + (id % 13) * 5, // 40~100
      special: false,
      image: pokecardback,
    }
    return { ...base, ...(SPECIALS[id] ?? {}) }
  })
}

// 상태
const registerCode = ref('')
const currentCategory = ref<string>('game')
const currentSubCategory = ref<string>('pokemon')
const currentSort = ref<'latest' | 'number'>('number')
const selectedCard = ref<PokemonCard | null>(null)

const categories = ref<Category[]>([
  { id: 'animation', name: 'Animation' },
  { id: 'creater', name: 'Creater' },
  { id: 'game', name: 'Game' },
  { id: 'kpop', name: 'Kpop' },
  { id: 'sports', name: 'Sports' },
  { id: 'webtoon', name: 'Webtoon' },
])

const subCategories = ref<Record<string, SubCategory[]>>({
  animation: [{ id: 'onepiece', name: '귀멸의 칼날' }, { id: 'naruto', name: '나루토' }],
  creater: [{ id: 'artist1', name: '침착맨' }, { id: 'artist2', name: '펭수' }],
  game: [{ id: 'pokemon', name: '포켓몬스터' }, { id: 'zelda', name: '젤다의 전설' }],
  kpop: [{ id: 'bts', name: 'BTS' }, { id: 'blackpink', name: 'BLACKPINK' }, { id: 'newjeans', name: 'NewJeans' }],
  sports: [{ id: 'baseball', name: '야구' }, { id: 'soccer', name: '축구' }],
  webtoon: [{ id: 'tower', name: '신의 탑' }, { id: 'noblesse', name: '마루는 강쥐' }],
})

const pokemonCards = ref<PokemonCard[]>(makeCards(151))

// 계산값
const currentSubCategories = computed(() => subCategories.value[currentCategory.value] || [])
const currentCategoryName = computed(() => {
  const cat = categories.value.find(c => c.id === currentCategory.value)
  return cat ? cat.name : ''
})
const showPokemonGrid = computed(() => currentCategory.value === 'game' && currentSubCategory.value === 'pokemon')

const sortedPokemonCards = computed<PokemonCard[]>(() => {
  const list = [...pokemonCards.value]
  if (currentSort.value === 'number') list.sort((a, b) => parseInt(a.number) - parseInt(b.number))
  else list.sort((a, b) => b.id - a.id)
  return list
})

// 무한 스크롤
const PAGE_SIZE = 30
const loadedCount = ref(PAGE_SIZE)
const isLoading = ref(false)
const sentinel = ref<HTMLElement | null>(null)
let io: IntersectionObserver | null = null

const visiblePokemonCards = computed(() => sortedPokemonCards.value.slice(0, loadedCount.value))
const hasMore = computed(() => loadedCount.value < sortedPokemonCards.value.length)

function loadMore() {
  if (!hasMore.value || isLoading.value) return
  isLoading.value = true
  setTimeout(() => {
    loadedCount.value = Math.min(loadedCount.value + PAGE_SIZE, sortedPokemonCards.value.length)
    isLoading.value = false
  }, 200)
}

onMounted(async () => {
  await nextTick()
  io = new IntersectionObserver(
    entries => entries.forEach(e => e.isIntersecting && loadMore()),
    { root: null, rootMargin: '200px', threshold: 0 }
  )
  if (sentinel.value) io.observe(sentinel.value)
})

onBeforeUnmount(() => {
  io?.disconnect()
  io = null
  unlockScroll() // 안전 해제
})

// 카테고리/정렬 변경 시 페이지 리셋
watch([currentCategory, currentSubCategory, currentSort], () => {
  loadedCount.value = PAGE_SIZE
})

// 상단 결과 카운트
const totalCards = computed(() => pokemonCards.value.length)
const currentLoaded = computed(() => visiblePokemonCards.value.length)
const resultText = computed(() => (showPokemonGrid.value ? `(${currentLoaded.value}/${totalCards.value})` : '(0/0)'))

// 정렬 토글
function toggleSort() {
  currentSort.value = currentSort.value === 'number' ? 'latest' : 'number'
}

// 등록
function registerCard() {
  if (!registerCode.value.trim()) {
    alert('영수증 번호 또는 인증코드를 입력해주세요.')
    return
  }
  const validCodes: Record<string, { id: number; name: string }> = {
    PIKACHU001: { id: 25, name: '피카츄' },
    CHARIZARD001: { id: 1, name: '파이리' },
    MANAPHY001: { id: 10, name: '모나피' },
    RECEIPT123456: { id: 26, name: '라이츄' },
    PURCHASE789: { id: 150, name: '뮤츠' },
  }
  const info = validCodes[registerCode.value.toUpperCase()]
  if (info) {
    const idx = pokemonCards.value.findIndex(c => c.id === info.id)
    if (idx !== -1) {
      pokemonCards.value[idx].special = true
      pokemonCards.value[idx].verified = true
    }
    alert(`✅ ${info.name} 카드가 성공적으로 등록되었습니다! 인증 마크가 부여되었습니다.`)
    registerCode.value = ''
  } else {
    alert('❌ 올바르지 않은 인증코드입니다. 영수증을 확인해주세요.')
  }
}

function clearSearch() {
  registerCode.value = ''
}

function getTypeKorean(type: PokemonCard['type']) {
  const map: Record<PokemonCard['type'], string> = {
    normal: '노말', fighting: '격투', flying: '비행', poison: '독', ground: '땅', bug: '벌레',
    rock: '바위', steel: '강철', ghost: '고스트', fire: '불꽃', water: '물', grass: '풀',
    electric: '전기', psychic: '에스퍼', ice: '얼음', dragon: '드래곤', dark: '악', fairy: '페어리',
  }
  return map[type] ?? type
}

// ── 스크롤 잠금(레이아웃 흔들림 방지 포함) ──
function lockScroll() {
  const doc = document.documentElement
  const body = document.body
  // 스크롤바 폭 계산
  const sbw = window.innerWidth - doc.clientWidth
  doc.classList.add('scroll-lock')
  if (sbw > 0) body.style.paddingRight = `${sbw}px`
}

function unlockScroll() {
  const doc = document.documentElement
  const body = document.body
  doc.classList.remove('scroll-lock')
  body.style.paddingRight = ''
}

// 모달 열기/닫기
function showCardDetail(card: PokemonCard) {
  selectedCard.value = card
  lockScroll()
}

function closeModal() {
  selectedCard.value = null
  unlockScroll()
}

// ESC 닫기
function onKey(e: KeyboardEvent) {
  if (e.key === 'Escape' && selectedCard.value) closeModal()
}
onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))

// 이미지 실패 핸들러
function handleImageError(e: Event) {
  const t = e.target as HTMLImageElement
  console.log('Image failed to load:', t?.src)
}
</script>

<style scoped>
.pokemon-collection {
  min-height: calc(100vh - 200px);
}

/* 등록 섹션 */
.register-section {
  padding: 2rem 0;
  border-bottom: 1px solid #e9ecef;
}

.register-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 2rem;
}

.register-bar {
  display: flex;
  gap: 1rem;
  align-items: center;
  justify-content: center;
}

.search-input-wrapper {
  position: relative;
  max-width: 400px;
  width: 100%;
  display: flex;
  align-items: center;
}

.register-input {
  width: 100%;
  padding: 1rem 3rem 1rem 1.5rem;
  border: 2px solid #EFECC6;
  border-radius: 15px;
  font-size: 14px;
  outline: none;
  transition: all .3s;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .05);
}

.register-input:focus {
  border-color: #670600;
  box-shadow: 0 4px 12px rgba(103, 6, 0, .1);
}

.register-input::placeholder {
  color: #999;
}

.clear-btn {
  position: absolute;
  right: 12px;
  background: none;
  border: none;
  color: #999;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all .3s;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
}

.clear-btn:hover {
  background: rgba(103, 6, 0, .1);
  color: #670600;
  transform: scale(1.1);
}

.register-btn {
  background: #670600;
  color: #fff;
  border: none;
  padding: 16px 20px;
  border-radius: 15px;
  cursor: pointer;
  font-weight: 500;
  font-size: 12px;
  transition: all .3s;
  box-shadow: 0 4px 12px rgba(103, 6, 0, .2);
  white-space: nowrap;
}

.register-btn:hover {
  background: linear-gradient(45deg, #5a0500, #5a0500);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(103, 6, 0, .3);
}

/* 레이아웃 */
.product_list_wrap {
  width: 100%;
  display: flex;
  justify-content: center;
}

.product_list_area {
  width: 100%;
}

.container-1280 {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding-left: 16px;
  padding-right: 16px;
}

/* 메인 컨텐츠 */
.main-content {
  width: 100%;
  margin: 0 auto;
  padding: 2rem 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.content-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
  width: 1280px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-right: 24px;
}

.result-count {
  font-size: .9rem;
  color: #666;
  font-weight: 500;
}

.sort-options {
  display: flex;
  gap: .5rem;
  align-items: center;
}

.sort-btn {
  background: #fff;
  border: 1px solid #ddd;
  padding: .4rem .8rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: .8rem;
  transition: all .3s;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  white-space: nowrap;
}

.sort-btn.active {
  background: #ff6b35;
  color: #fff;
  border-color: #ff6b35;
}

.sort-btn:hover {
  background: #f8f9fa;
  border-color: #ff6b35;
}

.sort-btn.active:hover {
  background: #e55a2b;
}

/* 카드 그리드 */
.card-grid-container {
  max-width: 1240px;
  width: 90%;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, .1);
  padding: 2rem;
  margin: 0 auto;
  border: 2px solid #EFECC6;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 2rem 1.5rem;
  min-height: 400px;
  justify-items: center;
}

.pokemon-card {
  background: transparent;
  border-radius: 8px;
  overflow: hidden;
  transition: all .3s ease;
  cursor: pointer;
  aspect-ratio: 3/4;
  width: 100%;
}

.pokemon-card:hover {
  transform: translateY(-4px);
}

.card-image-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.pokemon-card-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.verified-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: linear-gradient(45deg, #670600, #8B4513);
  color: #fff;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(103, 6, 0, .3);
  border: 3px solid #EFECC6;
  animation: verifiedGlow 3s ease-in-out infinite;
}

@keyframes verifiedGlow {

  0%,
  100% {
    transform: scale(1);
    box-shadow: 0 4px 12px rgba(103, 6, 0, .3);
  }

  50% {
    transform: scale(1.05);
    box-shadow: 0 6px 16px rgba(103, 6, 0, .4), 0 0 20px rgba(239, 236, 198, .5);
  }
}

/* 무한 스크롤 */
.infinite-sentinel {
  width: 100%;
  height: 1px;
}

.loading {
  text-align: center;
  padding: 12px 0;
}

.loader {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(0, 0, 0, .12);
  border-top-color: #670600;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 모달 (z-index 크게 + Teleport로 헤더보다 위에) */
.game-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .8);
  backdrop-filter: blur(5px);
  z-index: 99999;
  /* 헤더보다 항상 크게 */
  display: flex;
  align-items: center;
  justify-content: center;
  animation: modalFadeIn .15s ease-out;
}

.game-modal {
  background: linear-gradient(145deg, #1a1a2e, #16213e);
  border: 3px solid #0f3460;
  border-radius: 20px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, .7), inset 0 1px 0 rgba(255, 255, 255, .1);
  animation: modalSlideIn .2s ease-out;
  position: relative;
}

.modal-header {
  background: linear-gradient(90deg, #0f3460, #16213e);
  padding: 1rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #0f3460;
}

.modal-title {
  color: #ffd700;
  font-size: 1.5rem;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, .8);
}

.modal-close {
  background: none;
  border: none;
  color: #fff;
  font-size: 1.5rem;
  cursor: pointer;
  padding: .5rem;
  border-radius: 50%;
  transition: all .3s;
}

.modal-close:hover {
  background: rgba(255, 0, 0, .2);
  color: #ff6b6b;
  transform: scale(1.1);
}

.modal-body {
  padding: 2rem;
  display: flex;
  gap: 2rem;
  align-items: center;
}

.card-display {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-card-image {
  width: 180px;
  height: 240px;
  object-fit: contain;
  border-radius: 10px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, .5);
}

.card-details {
  flex: 1;
  color: #fff;
}

.pokemon-name-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.pokemon-name {
  font-size: 2rem;
  font-weight: bold;
  color: #ffd700;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, .8);
  margin: 0;
}

.pokemon-number {
  background: linear-gradient(45deg, #ff6b6b, #ff8e8e);
  color: #fff;
  padding: .3rem .8rem;
  border-radius: 15px;
  font-size: .9rem;
  font-weight: bold;
  box-shadow: 0 4px 8px rgba(0, 0, 0, .3);
}

.pokemon-type {
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.type-label {
  color: #ccc;
  font-size: 1rem;
}

.type-value {
  background: linear-gradient(45deg, #4ecdc4, #44a08d);
  color: #fff;
  padding: .4rem 1rem;
  border-radius: 20px;
  font-weight: bold;
  box-shadow: 0 4px 8px rgba(0, 0, 0, .3);
}

.pokemon-stats {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-label {
  min-width: 60px;
  color: #ccc;
  font-size: .9rem;
  font-weight: bold;
}

.stat-bar {
  flex: 1;
  height: 8px;
  background: rgba(255, 255, 255, .1);
  border-radius: 4px;
  overflow: hidden;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, .3);
}

.stat-fill {
  height: 100%;
  border-radius: 4px;
  transition: width .3s ease-out;
}

.hp-bar {
  background: linear-gradient(90deg, #ff6b6b, #ff8e8e);
}

.attack-bar {
  background: linear-gradient(90deg, #ffd93d, #ffed4e);
}

.stat-value {
  min-width: 40px;
  color: #ffd700;
  font-weight: bold;
  text-align: right;
}

.modal-footer {
  padding: 1.5rem;
  text-align: center;
  border-top: 2px solid #0f3460;
  background: rgba(0, 0, 0, .2);
}

@keyframes modalFadeIn {
  from {
    opacity: 0
  }

  to {
    opacity: 1
  }
}

@keyframes modalSlideIn {
  from {
    transform: scale(.9) translateY(-20px);
    opacity: 0
  }

  to {
    transform: scale(1) translateY(0);
    opacity: 1
  }
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #666;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: .3;
}

/* 반응형 */
@media (max-width: 1280px) {
  .card-grid-container {
    width: 85%;
    padding: 1.5rem;
  }

  .card-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 1024px) {
  .card-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .register-container {
    padding: 0 1rem;
  }

  .register-bar {
    flex-direction: column;
    gap: 1rem;
  }

  .search-input-wrapper {
    max-width: none;
    width: 100%;
  }

  .card-grid-container {
    width: 95%;
    padding: 1rem;
    border-radius: 15px;
  }

  .card-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 1.5rem 1rem;
  }

  .content-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .modal-body {
    flex-direction: column;
    gap: 1rem;
  }

  .modal-card-image {
    width: 150px;
    height: 200px;
  }

  .pokemon-name {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }
}

.confirm-btn {
  background: linear-gradient(45deg, #ff6b6b, #ff8e8e);
  color: #fff;
  border: none;
  padding: .5rem 1rem;
  border-radius: 25px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 6px 12px rgba(0, 0, 0, .3);
  transition: all .3s;
}

.confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, .4);
  background: linear-gradient(45deg, #ff5252, #ff7979);
}
</style>

<!-- 전역(:visited, 스크롤락) -->
<style>
a {
  text-decoration: none;
  color: inherit;
}

a:visited {
  text-decoration: none;
  color: inherit;
}

/* 스크롤 락 */
html.scroll-lock,
html.scroll-lock body {
  overflow: hidden;
}
</style>
