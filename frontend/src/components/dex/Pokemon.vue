<!-- src/components/Pokemon.vue -->
<template>
  <!-- 카드 그리드 -->
  <div class="card-grid-container">
    <!-- 헤더(로고 + 통계) -->
    <div class="figure-header">
      <h2 class="dictionary-title">
        <img :src="pokemonLogo" alt="POKÉMON" class="title-img" />
      </h2>
      <div class="collection-stats">
        <span>수집 카드: {{itemKeys.filter(k => dex.isVerified(k)).length}}/{{ itemKeys.length }}</span>
        <span>•</span>
        <span>완성도: {{ completionRate }}%</span>
      </div>
    </div>

    <!-- 상단 우측 정렬 컨트롤 -->
    <div class="content-header">
      <div class="header-right">
        <div class="sort-options">
          <button :class="['sort-btn', { active: currentSort === 'number' }]" @click="toggleSort" title="번호순 정렬">
            ⇅ 번호순
          </button>
        </div>
      </div>
    </div>
    <div class="card-grid">
      <div v-for="card in visiblePokemonCards" :key="card.id" class="pokemon-card" style="cursor: pointer"
        @click.prevent.stop="onCardClick(card)">
        <div class="card-image-container">
          <img :src="dex.isVerified(makeKey('game', 'pokemon', card.number))
            ? getFrontImageById(card.id)
            : (isFlipped(card.id) ? getFrontImageById(card.id) : cardBackSrc)" :alt="card.name"
            class="pokemon-card-image" />
          <VerifiedBadge v-if="dex.isVerified(makeKey('game', 'pokemon', card.number))" class="verified-badge" />
        </div>
      </div>
    </div>

    <!-- 로딩 인디케이터 / 무한 스크롤 -->
    <div v-if="isLoading" class="loading">
      <div class="loader" aria-label="로딩 중"></div>
    </div>
    <div ref="sentinel" class="infinite-sentinel" aria-hidden="true"></div>
  </div>

  <!-- 모달 -->
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
                  <div class="stat-fill hp-bar" :style="{ width: `${Math.min(100, (selectedCard.hp / 120) * 100)}%` }">
                  </div>
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
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import cardBackSrc from '@/assets/pokemon/pokecardback.svg'
import { useDex } from '@/stores/useDex'
import { makeKey, type DexKey } from '@/composables/useDexHelpers'
import VerifiedBadge from '@/components/dex/VerifiedBadge.vue'

/* 이미지 로딩 */
const pokemonPngMap = import.meta.glob<string>('/src/assets/pokemon/SVG_*.png', { eager: true, import: 'default' })
const imageList = Object.keys(pokemonPngMap)
  .sort((a, b) => a.localeCompare(b, undefined, { numeric: true }))
  .map(k => pokemonPngMap[k] as string)

/* 로고 */
const logoMap = import.meta.glob<string>('/src/assets/pokemon/pokemon.webp', { eager: true, import: 'default' })
const pokemonLogo = (Object.values(logoMap)[0] as string | undefined) ?? cardBackSrc

/* 타입 */
type PokemonCard = {
  id: number
  name: string
  type:
  | 'normal' | 'fighting' | 'flying' | 'poison' | 'ground' | 'bug' | 'rock' | 'steel' | 'ghost'
  | 'fire' | 'water' | 'grass' | 'electric' | 'psychic' | 'ice' | 'dragon' | 'dark' | 'fairy'
  number: string
  hp: number
  attack: number
  image: string
}

/* 데이터 */
const registerCode = ref('')
const currentSort = ref<'latest' | 'number'>('number')
const selectedCard = ref<PokemonCard | null>(null)
const flipped = ref<Set<number>>(new Set())
const pokemonCards = ref<PokemonCard[]>(makeCards())

function makeCards(): PokemonCard[] {
  const total = imageList.length
  return Array.from({ length: total }, (_, i) => {
    const id = i + 1
    return {
      id,
      name: '포켓몬카드',
      type: 'normal',
      number: String(id).padStart(3, '0'),
      hp: 40 + (id % 17) * 5,
      attack: 40 + (id % 13) * 5,
      image: imageList[i],
    }
  })
}

/* 정렬 & 페이징 */
const sortedPokemonCards = computed<PokemonCard[]>(() => {
  const list = [...pokemonCards.value]
  if (currentSort.value === 'number') list.sort((a, b) => parseInt(a.number) - parseInt(b.number))
  else list.sort((a, b) => b.id - a.id)
  return list
})

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
onBeforeUnmount(() => { io?.disconnect(); io = null; unlockScroll() })

/* 정렬 토글 */
function toggleSort() {
  currentSort.value = currentSort.value === 'number' ? 'latest' : 'number'
  loadedCount.value = PAGE_SIZE
}

/* 등록 로직 (부모에서 관리) */
function registerCard(code: string) {
  if (!code.trim()) {
    alert('영수증 번호 또는 인증코드를 입력해주세요.')
    return
  }
  alert(`현재는 인증/등록 기능이 비활성화되어 있습니다.\n입력: ${code}`)
  registerCode.value = ''
}
function clearSearch() { registerCode.value = '' }

/* 타입 한글 표기 */
function getTypeKorean(type: PokemonCard['type']) {
  const map: Record<PokemonCard['type'], string> = {
    normal: '노말', fighting: '격투', flying: '비행', poison: '독', ground: '땅', bug: '벌레',
    rock: '바위', steel: '강철', ghost: '고스트', fire: '불꽃', water: '물', grass: '풀',
    electric: '전기', psychic: '에스퍼', ice: '얼음', dragon: '드래곤', dark: '악', fairy: '페어리',
  }
  return map[type] ?? type
}

/* 뒤집기/모달 */
function isFlipped(id: number) { return flipped.value.has(id) }
function getFrontImageById(id: number) { return imageList[id - 1] ?? cardBackSrc }
function onCardClick(card: PokemonCard) {
  const key = makeKey('game', 'pokemon', card.number)

  // 인증된 카드는 이미 앞면 고정 → 클릭해도 뒤집기 안 함
  if (dex.isVerified(key)) {
    selectedCard.value = { ...card, image: getFrontImageById(card.id) }
    lockScroll()
    return
  }

  // 기존 활성화/뒤집기 로직
  dex.activate(key)
  if (!isFlipped(card.id)) {
    const next = new Set(flipped.value)
    next.add(card.id)
    flipped.value = next
    return
  }
  selectedCard.value = { ...card, image: getFrontImageById(card.id) }
  lockScroll()
}


const dex = useDex()

const itemKeys = computed<DexKey[]>(() =>
  pokemonCards.value.map(c => makeKey('game', 'pokemon', c.number))
)

const completionRate = computed(() => dex.completionByKeys(itemKeys.value))


/* 스크롤 잠금/해제 & 모달 */
function lockScroll() {
  const doc = document.documentElement
  const body = document.body
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
function closeModal() { selectedCard.value = null; unlockScroll() }
function onKey(e: KeyboardEvent) { if (e.key === 'Escape' && selectedCard.value) closeModal() }
onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))

/* 통계 */
const totalCards = computed(() => pokemonCards.value.length)
const pokemonCollected = computed(() => flipped.value.size)
const pokemonCompletionRate = computed(() =>
  totalCards.value ? Math.round((flipped.value.size / totalCards.value) * 100) : 0
)

/* 이미지 실패 */
function handleImageError(e: Event) {
  const t = e.target as HTMLImageElement
  console.log('Image failed to load:', t?.src)
}
</script>

<style scoped>
.pokemon-collection {
  min-height: calc(100vh - 200px);
}

.dictionary-title {
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-img {
  height: 100px;
  width: auto;
  display: block;
  margin: 0 auto 1rem;
}

.collection-stats {
  font-size: .8rem;
  background: rgba(239, 236, 198, .2);
  backdrop-filter: blur(10px);
  padding: .6rem 1rem;
  border-radius: 50px;
  display: inline-block;
  border: 1px solid rgba(239, 236, 198, .3);
  position: relative;
  z-index: 1;
  color: #fff;
}

.collection-stats span {
  margin: 0 .3rem;
  font-weight: 600;
}

/* 상단 정렬 영역 */
.content-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
  width: 1280px;
  max-width: 98%;
  margin-left: auto;
  margin-right: auto;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-right: 24px;
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

.verified-badge {
  z-index: 6;
  position: absolute;
  top: 1rem;
  left: 1rem;
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

/* 모달 */
.game-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .8);
  backdrop-filter: blur(5px);
  z-index: 99999;
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

/* 확인 버튼 */
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

.figure-header {
  text-align: center;
  margin-bottom: 2.5rem;
  padding: 2rem;
  width: 98%;
  max-width: 1240px;
  margin: 0 auto 1.25rem;
  padding: 1.5rem;
  text-align: center;
  background: linear-gradient(135deg, #1b2a4a, #2d6cdf, #47a3ff);
  border-radius: 16px;
  color: #fdfdfd;
  box-shadow: inset 0 3px 10px rgba(255, 255, 255, .08), 0 8px 20px rgba(0, 0, 0, .18);
  border-radius: 20px;
  color: white;
  position: relative;
  overflow: hidden;
  border: 2px solid #EFECC6;
}

.figure-header::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="20" cy="20" r="2" fill="rgba(239,236,198,0.3)"/><circle cx="80" cy="40" r="1.5" fill="rgba(239,236,198,0.2)"/><circle cx="40" cy="80" r="1" fill="rgba(239,236,198,0.2)"/><circle cx="90" cy="90" r="1" fill="rgba(239,236,198,0.2)"/></svg>');
  animation: floatStars 20s linear infinite;
}

@keyframes floatStars {
  0% {
    transform: translateY(0) rotate(0deg);
  }

  100% {
    transform: translateY(-10px) rotate(360deg);
  }
}
</style>
