<!-- src/views/Dex.vue -->
<template>
  <div class="pokemon-collection">
    <!-- 등록 섹션 -->
    <div class="register-section">
      <div class="register-container">
        <div class="register-bar">
          <div class="search-input-wrapper">
            <input 
              type="text" 
              class="register-input" 
              placeholder="영수증 번호 또는 인증코드를 입력하세요"
              v-model="registerCode"
              @keypress.enter="registerCard"
            >
            <button 
              v-if="registerCode" 
              class="clear-btn" 
              @click="clearSearch"
              title="전체 삭제"
            >
              ✕
            </button>
          </div>
          <button class="register-btn" @click="registerCard">등록하기</button>
        </div>
      </div>
    </div>

    <!-- ✅ 카테고리 탭: 공용 UI(TopTabs/MidTabs) + 어댑터 -->
    <section class="product_list_wrap">
      <div class="product_list_area" role="region" aria-label="도감 카테고리">
        
    <div class="container-1280">
      <TopTabsAdapter
        v-model="currentCategory"
        :items="categories"
        aria-label="상위 카테고리"
      />
    </div>
    <div class="container-1280">
      <MidTabsAdapter
        v-model="currentSubCategory"
        :items="currentSubCategories"
        aria-label="중위 카테고리"
        :visible="currentSubCategories.length > 0"
      />
    </div>
  </div>
    </section>

    <!-- 메인 컨텐츠 -->
    <div class="main-content">
      <div class="content-header">
        <div class="header-right">
          <div class="result-count">{{ resultText }}</div>
          <div class="sort-options">
            <button 
              :class="['sort-btn', { active: currentSort === 'number' }]"
              @click="toggleSort"
              title="번호순 정렬"
            >
              ⇅ 번호순
            </button>
          </div>
        </div>
      </div>

      <!-- 포켓몬 카드 그리드 -->
      <div class="card-grid-container" v-if="showPokemonGrid">
        <div class="card-grid">
          <div 
            v-for="card in sortedPokemonCards" 
            :key="card.id"
            class="pokemon-card"
            @click="card.special ? showCardDetail(card) : null"
            :style="{ cursor: card.special ? 'pointer' : 'default' }"
          >
            <div class="card-image-container">
              <img 
                :src="card.image" 
                :alt="card.name"
                class="pokemon-card-image"
                loading="lazy"
                @error="handleImageError"
              />
              <div v-if="card.verified" class="verified-badge">✓</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 다른 카테고리 빈 상태 -->
      <div class="empty-state" v-else>
        <div class="empty-icon">📦</div>
        <h3>{{ currentCategoryName }} 준비 중입니다</h3>
        <p>해당 카테고리의 콘텐츠는 곧 업데이트될 예정입니다.</p>
      </div>
    </div>

    <!-- 게임 스타일 모달 -->
    <div v-if="selectedCard" class="game-modal-overlay" @click="closeModal">
      <div class="game-modal" @click.stop>
        <div class="modal-header">
          <div class="modal-title">포켓몬 도감</div>
          <button class="modal-close" @click="closeModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="card-display">
            <img :src="selectedCard.image" :alt="selectedCard.name" class="modal-card-image" loading="lazy">
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
                  <div class="stat-fill hp-bar" :style="{ width: `${(selectedCard.hp / 120) * 100}%` }"></div>
                </div>
                <span class="stat-value">{{ selectedCard.hp }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">공격력</span>
                <div class="stat-bar">
                  <div class="stat-fill attack-bar" :style="{ width: `${(selectedCard.attack / 100) * 100}%` }"></div>
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
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import TopTabsAdapter from '@/components/filters/TopTabsAdapter.vue'
import MidTabsAdapter from '@/components/filters/MidTabsAdapter.vue'

// 이미지들
import pokecardback from '@/assets/pokemon/pokecardback.svg'
import pokecardf    from '@/assets/pokemon/pokecardf.svg'
import pokecardp    from '@/assets/pokemon/pokecardp.svg'
import pokecardm    from '@/assets/pokemon/pokecardm.svg'

type Category = { id: string; name: string }
type SubCategory = { id: string; name: string }
type PokemonCard = {
  id: number
  name: string
  type: 'fire'|'water'|'grass'|'electric'|'psychic'|'normal'
  number: string
  hp: number
  attack: number
  emoji?: string
  rarity?: 'common'|'rare'
  special: boolean
  verified?: boolean
  image: string
}

const registerCode = ref('')
const currentCategory = ref<string>('game')
const currentSubCategory = ref<string>('pokemon')
const currentSort = ref<'latest' | 'number'>('latest')
const selectedCard = ref<PokemonCard | null>(null)

const categories = ref<Category[]>([
  { id: 'animation', name: 'Animation' },
  { id: 'creater',   name: 'Creater' },
  { id: 'game',      name: 'Game' },
  { id: 'kpop',      name: 'Kpop' },
  { id: 'sports',    name: 'Sports' },
  { id: 'webtoon',   name: 'Webtoon' }
])

const subCategories = ref<Record<string, SubCategory[]>>({
  animation: [{ id: 'onepiece', name: '귀멸의 칼날' }, { id: 'naruto', name: '나루토' }],
  creater:   [{ id: 'artist1', name: '침착맨' }, { id: 'artist2', name: '펭수' }],
  game:      [{ id: 'pokemon', name: '포켓몬스터' }, { id: 'zelda', name: '젤다의 전설' }],
  kpop:      [{ id: 'bts', name: 'BTS' }, { id: 'blackpink', name: 'BLACKPINK' }, { id: 'newjeans', name: 'NewJeans' }],
  sports:    [{ id: 'baseball', name: '야구' }, { id: 'soccer', name: '축구' }],
  webtoon:   [{ id: 'tower', name: '신의 탑' }, { id: 'noblesse', name: '마루는 강쥐' }],
})

const totalCards = ref<number>(63)
const currentCards = ref<number>(3)

const pokemonCards = ref<PokemonCard[]>([
  { id: 1,  name: "파이리",     type: "fire",     number: "001", hp: 50, attack: 60, emoji: "🔥", rarity: "rare",  special: true,  image: pokecardf },
  { id: 2,  name: "포켓몬카드", type: "normal",   number: "002", hp: 40, attack: 50, special: false, image: pokecardback },
  { id: 3,  name: "포켓몬카드", type: "normal",   number: "003", hp: 40, attack: 50, special: false, image: pokecardback },
  { id: 4,  name: "포켓몬카드", type: "normal",   number: "004", hp: 40, attack: 50, special: false, image: pokecardback },
  { id: 5,  name: "포켓몬카드", type: "normal",   number: "005", hp: 40, attack: 50, special: false, image: pokecardback },
  { id: 6,  name: "포켓몬카드", type: "normal",   number: "006", hp: 40, attack: 50, special: false, image: pokecardback },
  { id: 7,  name: "포켓몬카드", type: "normal",   number: "007", hp: 40, attack: 50, special: false, image: pokecardback },
  { id: 8,  name: "포켓몬카드", type: "normal",   number: "008", hp: 40, attack: 50, special: false, image: pokecardback },
  { id: 9,  name: "포켓몬카드", type: "normal",   number: "009", hp: 40, attack: 50, special: false, image: pokecardback },
  { id: 25, name: "피카츄",     type: "electric", number: "025", hp: 60, attack: 55, emoji: "⚡", rarity: "rare",  special: true,  image: pokecardp },
  { id: 10, name: "모나피",     type: "grass",    number: "010", hp: 70, attack: 50, emoji: "🌊", rarity: "rare",  special: true,  image: pokecardm },
])

const currentSubCategories = computed<SubCategory[]>(() => subCategories.value[currentCategory.value] || [])

const currentCategoryName = computed<string>(() => {
  const cat = categories.value.find(c => c.id === currentCategory.value)
  return cat ? cat.name : ''
})

const showPokemonGrid = computed<boolean>(() => currentCategory.value === 'game' && currentSubCategory.value === 'pokemon')

const resultText = computed<string>(() => showPokemonGrid.value ? `(${currentCards.value}/${totalCards.value})` : '(0/0)')

const sortedPokemonCards = computed<PokemonCard[]>(() => {
  const list = [...pokemonCards.value]
  if (currentSort.value === 'number') {
    list.sort((a, b) => parseInt(a.number) - parseInt(b.number))
  } else {
    list.sort((a, b) => b.id - a.id)
  }
  return list
})

// 상위 카테고리 변경 시 mid 첫 항목 자동 선택
watch(currentCategory, () => {
  const list = currentSubCategories.value
  currentSubCategory.value = list.length ? list[0].id : ''
})

function toggleSort() {
  currentSort.value = currentSort.value === 'number' ? 'latest' : 'number'
}

function registerCard() {
  if (!registerCode.value.trim()) {
    alert('영수증 번호 또는 인증코드를 입력해주세요.')
    return
  }
  const validCodes: Record<string, { id: number; name: string }> = {
    'PIKACHU001':    { id: 25,  name: '피카츄' },
    'CHARIZARD001':  { id: 1,   name: '파이리' },
    'MANAPHY001':    { id: 10,  name: '모나피' },
    'RECEIPT123456': { id: 26,  name: '라이츄' },
    'PURCHASE789':   { id: 150, name: '뮤츠' },
  }
  const code = registerCode.value.toUpperCase()
  const info = validCodes[code]
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
    fire: '불꽃', water: '물', grass: '풀', electric: '전기', psychic: '에스퍼', normal: '노말'
  }
  return map[type] ?? type
}

function showCardDetail(card: PokemonCard) {
  selectedCard.value = card
  document.body.style.overflow = 'hidden'
  const header = document.querySelector('header')
  const nav = document.querySelector('nav')
  if (header) (header as HTMLElement).style.display = 'none'
  if (nav) (nav as HTMLElement).style.display = 'none'
}

function closeModal() {
  selectedCard.value = null
  document.body.style.overflow = 'auto'
  const header = document.querySelector('header')
  const nav = document.querySelector('nav')
  if (header) (header as HTMLElement).style.display = 'block'
  if (nav) (nav as HTMLElement).style.display = 'block'
}

function handleImageError(e: Event) {
  const t = e.target as HTMLImageElement
  console.log('Image failed to load:', t?.src)
}
</script>

<style scoped>
/* (네가 주었던 스타일 유지 – 탭 전용 스타일은 공용 컴포넌트 내부로 이동 권장) */


/* 등록 섹션 */
.register-section {padding: 2rem 0; border-bottom: 16px }
.register-container { max-width: 1280px; margin: 0 auto; padding: 0 2rem; }
.register-bar { display: flex; gap: 1rem; align-items: center; justify-content: center; }
.search-input-wrapper { position: relative; max-width: 400px; width: 100%; display: flex; align-items: center; }
.register-input { width: 100%; padding: 1rem 3rem 1rem 1.5rem; border: 2px solid #EFECC6; border-radius: 15px; font-size: 1rem; outline: none; transition: all 0.3s; background: white; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.register-input:focus { border-color: #670600; box-shadow: 0 4px 12px rgba(103, 6, 0, 0.1); }
.register-input::placeholder { color: #999; }
.clear-btn { position: absolute; right: 12px; background: none; border: none; color: #999; font-size: 1.2rem; cursor: pointer; padding: 8px; border-radius: 50%; transition: all 0.3s; display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; }
.clear-btn:hover { background: rgba(103, 6, 0, 0.1); color: #670600; transform: scale(1.1); }
.register-btn { background: linear-gradient(45deg, #670600, #8B4513); color: white; border: none; padding: 1rem 2rem; border-radius: 15px; cursor: pointer; font-weight: 600; font-size: 1rem; transition: all 0.3s; box-shadow: 0 4px 12px rgba(103, 6, 0, 0.2); white-space: nowrap; }
.register-btn:hover { background: linear-gradient(45deg, #5a0500, #7a3a0f); transform: translateY(-2px); box-shadow: 0 6px 16px rgba(103, 6, 0, 0.3); }

/* 레이아웃 */
.product_list_wrap { width: 100%; display: flex; justify-content: center; }
.product_list_area { width: 100%; }

/* 메인 컨텐츠 */
.main-content { width: 100%; margin: 0 auto; padding: 1rem 0; display: flex; flex-direction: column; align-items: center; }
.content-header { display: flex; justify-content: flex-end; align-items: center; margin-bottom: 1rem; flex-wrap: wrap; gap: 1rem; width: 1280px; }
.header-right { display: flex; align-items: center; gap: 1rem; margin-right: 20px;}
.result-count { font-size: 0.9rem; color: #666; font-weight: 500; }
.sort-options { display: flex; gap: 0.5rem; align-items: center; }
.sort-btn { background: white; border: 1px solid #ddd; padding: 0.4rem 0.8rem; border-radius: 4px; cursor: pointer; font-size: 0.8rem; transition: all 0.3s; display: flex; align-items: center; justify-content: center; height: 32px; white-space: nowrap; }
.sort-btn.active { background: #ff6b35; color: white; border-color: #ff6b35; }
.sort-btn:hover { background: #f8f9fa; border-color: #ff6b35; }
.sort-btn.active:hover { background: #e55a2b; }

/* 카드 영역 */
.card-grid-container { max-width: 1200px; width: 90%; background: white; border-radius: 20px; box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1); padding: 2rem; margin: 0 auto; border: 2px solid #EFECC6; }
.card-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 2rem 1.5rem; min-height: 400px; justify-items: center; }
.pokemon-card { background: transparent; border-radius: 8px; overflow: hidden; transition: all 0.3s ease; cursor: pointer; aspect-ratio: 3/4; width: 100%; }
.pokemon-card:hover { transform: translateY(-4px); }
.card-image-container { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; position: relative; }
.pokemon-card-image { width: 100%; height: 100%; object-fit: contain; }
.verified-badge { position: absolute; top: 8px; right: 8px; background: linear-gradient(45deg, #670600, #8B4513); color: white; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; font-weight: bold; box-shadow: 0 4px 12px rgba(103, 6, 0, 0.3); border: 3px solid #EFECC6; animation: verifiedGlow 3s ease-in-out infinite; }
.container-1280 {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding-left: 16px;   /* 좌우 여백이 필요하면 */
  padding-right: 16px;  /* 프로젝트 토큰에 맞춰 조정 */
}

@keyframes verifiedGlow { 0%, 100% { transform: scale(1); box-shadow: 0 4px 12px rgba(103, 6, 0, 0.3); } 50% { transform: scale(1.05); box-shadow: 0 6px 16px rgba(103, 6, 0, 0.4), 0 0 20px rgba(239, 236, 198, 0.5); } }

/* 모달 등 나머지 스타일은 생략 없이 유지 (이미 위에서 포함) */
</style>
