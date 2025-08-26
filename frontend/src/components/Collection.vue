<!-- src/views/Collection.vue -->
<template>
  <div class="pokemon-collection">
    <!-- 등록 섹션 -->
    <div class="register-section">
      <div class="register-container">
        <div class="register-bar">
          <div class="search-input-wrapper">
            <input type="text" class="register-input" placeholder="영수증 번호 또는 인증코드를 입력하세요" v-model="registerCode"
              @keypress.enter="registerCard" />
            <button v-if="registerCode" class="clear-btn" @click="clearSearch" title="전체 삭제">
              ✕
            </button>
          </div>
          <button class="register-btn" @click="registerCard">등록하기</button>
        </div>
      </div>
    </div>

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

      <!-- 포켓몬 카드 그리드 -->
      <div class="card-grid-container" v-if="showPokemonGrid">
        <div class="card-grid">
          <div v-for="card in sortedPokemonCards" :key="card.id" class="pokemon-card"
            @click.prevent.stop="card.special && showCardDetail(card)"
            :style="{ cursor: card.special ? 'pointer' : 'default' }">
            <div class="card-image-container">
              <img :src="card.image" :alt="card.name" class="pokemon-card-image" @error="handleImageError"
                loading="lazy" />
              <div v-if="card.verified" class="verified-badge">✓</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 다른 카테고리들 (빈 상태) -->
      <div class="empty-state" v-else>
        <div class="empty-icon">📦</div>
        <h3>{{ currentCategoryName }} 준비 중입니다</h3>
        <p>해당 카테고리의 콘텐츠는 곧 업데이트될 예정입니다.</p>
      </div>
    </div>

    <!-- 게임 스타일 모달: Teleport로 body에 렌더 -->
    <teleport to="body">
      <div v-if="selectedCard" class="game-modal-overlay" role="dialog" aria-modal="true"
        aria-labelledby="dex-modal-title" @click.self="closeModal">
        <div class="game-modal" @click.stop ref="modalPanel" tabindex="-1">
          <div class="modal-header">
            <div class="modal-title" id="dex-modal-title">포켓몬 도감</div>
            <button class="modal-close" @click="closeModal" aria-label="닫기">✕</button>
          </div>

          <div class="modal-body">
            <div class="card-display">
              <img :src="selectedCard.image" :alt="selectedCard.name" class="modal-card-image" />
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
                      :style="{ width: `${Math.min(100, (selectedCard.hp / 120) * 100)}%` }" />
                  </div>
                  <span class="stat-value">{{ selectedCard.hp }}</span>
                </div>

                <div class="stat-item">
                  <span class="stat-label">공격력</span>
                  <div class="stat-bar">
                    <div class="stat-fill attack-bar"
                      :style="{ width: `${Math.min(100, (selectedCard.attack / 100) * 100)}%` }" />
                  </div>
                  <span class="stat-value">{{ selectedCard.attack }}</span>
                </div>
              </div>

              <div class="mt-4" style="text-align:right">
                <button class="confirm-btn" @click="closeModal">확인</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script>
// 이미지 import
import pokecardback from '@/assets/pokemon/pokecardback.svg'
import pokecardf from '@/assets/pokemon/pokecardf.svg'
import pokecardp from '@/assets/pokemon/pokecardp.svg'
import pokecardm from '@/assets/pokemon/pokecardm.svg'

export default {
  name: 'PokemonCollection',
  data() {
    return {
      registerCode: '',
      currentCategory: 'game',
      currentSubCategory: 'pokemon',
      currentSort: 'latest',
      selectedCard: null,

      categories: [
        { id: 'animation', name: 'Animation' },
        { id: 'creater', name: 'Creater' },
        { id: 'game', name: 'Game' },
        { id: 'kpop', name: 'Kpop' },
        { id: 'sports', name: 'Sports' },
        { id: 'webtoon', name: 'Webtoon' }
      ],

      subCategories: {
        animation: [
          { id: 'onepiece', name: '귀멸의 칼날' },
          { id: 'naruto', name: '나루토' }
        ],
        creater: [
          { id: 'artist1', name: '침착맨' },
          { id: 'artist2', name: '펭수' }
        ],
        game: [
          { id: 'pokemon', name: '포켓몬스터' },
          { id: 'zelda', name: '젤다의 전설' }
        ],
        kpop: [
          { id: 'bts', name: 'BTS' },
          { id: 'blackpink', name: 'BLACKPINK' },
          { id: 'newjeans', name: 'NewJeans' }
        ],
        sports: [
          { id: 'baseball', name: '야구' },
          { id: 'soccer', name: '축구' }
        ],
        webtoon: [
          { id: 'tower', name: '신의 탑' },
          { id: 'noblesse', name: '마루는 강쥐' }
        ]
      },

      totalCards: 63,
      currentCards: 3,

      pokemonCards: [
        { id: 1, name: "파이리", type: "fire", number: "001", hp: 50, attack: 60, emoji: "🔥", rarity: "common", special: true, image: pokecardf },
        { id: 2, name: "포켓몬카드", type: "normal", number: "002", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 3, name: "포켓몬카드", type: "normal", number: "003", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 4, name: "포켓몬카드", type: "normal", number: "004", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 5, name: "포켓몬카드", type: "normal", number: "005", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 6, name: "포켓몬카드", type: "normal", number: "006", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 7, name: "포켓몬카드", type: "normal", number: "007", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 8, name: "포켓몬카드", type: "normal", number: "008", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 9, name: "포켓몬카드", type: "normal", number: "009", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 25, name: "피카츄", type: "electric", number: "025", hp: 60, attack: 55, emoji: "⚡", rarity: "rare", special: true, image: pokecardp },
        { id: 10, name: "모나피", type: "grass", number: "010", hp: 70, attack: 50, emoji: "🌊", rarity: "rare", special: true, image: pokecardm },
        { id: 11, name: "포켓몬카드", type: "normal", number: "011", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 12, name: "포켓몬카드", type: "normal", number: "012", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 13, name: "포켓몬카드", type: "normal", number: "013", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 14, name: "포켓몬카드", type: "normal", number: "014", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 15, name: "포켓몬카드", type: "normal", number: "015", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 16, name: "포켓몬카드", type: "normal", number: "016", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 17, name: "포켓몬카드", type: "normal", number: "017", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 18, name: "포켓몬카드", type: "normal", number: "018", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 19, name: "포켓몬카드", type: "normal", number: "019", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 20, name: "포켓몬카드", type: "normal", number: "020", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 21, name: "포켓몬카드", type: "normal", number: "021", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 22, name: "포켓몬카드", type: "normal", number: "022", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 23, name: "포켓몬카드", type: "normal", number: "023", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 24, name: "포켓몬카드", type: "normal", number: "024", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 26, name: "포켓몬카드", type: "normal", number: "026", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 27, name: "포켓몬카드", type: "normal", number: "027", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 28, name: "포켓몬카드", type: "normal", number: "028", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 29, name: "포켓몬카드", type: "normal", number: "029", hp: 40, attack: 50, special: false, image: pokecardback },
        { id: 30, name: "포켓몬카드", type: "normal", number: "030", hp: 40, attack: 50, special: false, image: pokecardback }
      ]
    }
  },

  computed: {
    currentSubCategories() {
      return this.subCategories[this.currentCategory] || []
    },
    currentCategoryName() {
      const category = this.categories.find(cat => cat.id === this.currentCategory)
      return category ? category.name : ''
    },
    showPokemonGrid() {
      return this.currentCategory === 'game' && this.currentSubCategory === 'pokemon'
    },
    resultText() {
      return this.showPokemonGrid ? `(${this.currentCards}/${this.totalCards})` : '(0/0)'
    },
    sortedPokemonCards() {
      const cards = [...this.pokemonCards]
      if (this.currentSort === 'number') cards.sort((a, b) => parseInt(a.number) - parseInt(b.number))
      else if (this.currentSort === 'latest') cards.sort((a, b) => b.id - a.id)
      return cards
    }
  },

  watch: {
    currentCategory() {
      if (this.currentSubCategories.length > 0) {
        this.currentSubCategory = this.currentSubCategories[0].id
      }
    }
  },

  mounted() {
    window.addEventListener('keydown', this.onKeydown)
  },
  beforeUnmount() {
    window.removeEventListener('keydown', this.onKeydown)
    this.unlockScroll() // 안전장치
  },

  methods: {
    setCategory(categoryId) {
      this.currentCategory = categoryId
      const list = this.subCategories[categoryId]
      this.currentSubCategory = list && list.length ? list[0].id : ''
    },
    setSubCategory(subCategoryId) { this.currentSubCategory = subCategoryId },
    setSort(sortId) { this.currentSort = sortId },
    toggleSort() { this.currentSort = this.currentSort === 'number' ? 'latest' : 'number' },

    registerCard() {
      if (!this.registerCode.trim()) {
        alert('영수증 번호 또는 인증코드를 입력해주세요.')
        return
      }
      const validCodes = {
        'PIKACHU001': { id: 25, name: '피카츄' },
        'CHARIZARD001': { id: 1, name: '파이리' },
        'MANAPHY001': { id: 10, name: '모나피' },
        'RECEIPT123456': { id: 26, name: '라이츄' },
        'PURCHASE789': { id: 150, name: '뮤츠' }
      }
      const code = this.registerCode.toUpperCase()
      if (validCodes[code]) {
        const cardInfo = validCodes[code]
        const idx = this.pokemonCards.findIndex(c => c.id === cardInfo.id)
        if (idx !== -1) {
          this.pokemonCards[idx].special = true
          this.pokemonCards[idx].verified = true
        }
        alert(`✅ ${cardInfo.name} 카드가 성공적으로 등록되었습니다! 인증 마크가 부여되었습니다.`)
        this.registerCode = ''
      } else {
        alert('❌ 올바르지 않은 인증코드입니다. 영수증을 확인해주세요.')
      }
    },

    clearSearch() { this.registerCode = '' },

    getTypeKorean(type) {
      const typeMap = { fire: '불꽃', water: '물', grass: '풀', electric: '전기', psychic: '에스퍼', normal: '노말' }
      return typeMap[type] || type
    },

    showCardDetail(card) {
      this.selectedCard = card
      this.lockScroll()
      this.$nextTick(() => {
        const el = this.$refs.modalPanel
        if (el && typeof el.focus === 'function') el.focus()
      })
    },

    closeModal() {
      this.selectedCard = null
      this.unlockScroll()
    },

    onKeydown(e) {
      if (e.key === 'Escape' && this.selectedCard) this.closeModal()
    },

    lockScroll() { document.documentElement.classList.add('scroll-lock') },
    unlockScroll() { document.documentElement.classList.remove('scroll-lock') },

    handleImageError(event) {
      console.log('Image failed to load:', event.target?.src)
    }
  }
}
</script>

<style scoped>
/* ====== 페이지/그리드/버튼 등 로컬 스타일 (scoped) ====== */
.pokemon-collection {
  background: #f8f9fa;
  min-height: calc(100vh - 200px);
}

/* 등록 섹션 */
.register-section {
  background: #f8f9fa;
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
  font-size: 1rem;
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
  background: linear-gradient(45deg, #670600, #8B4513);
  color: #fff;
  border: none;
  padding: 1rem 2rem;
  border-radius: 15px;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  transition: all .3s;
  box-shadow: 0 4px 12px rgba(103, 6, 0, .2);
  white-space: nowrap;
}

.register-btn:hover {
  background: linear-gradient(45deg, #5a0500, #7a3a0f);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(103, 6, 0, .3);
}

/* 메인/헤더/정렬 */
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
  max-width: 1200px;
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

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #666;
  grid-column: 1 / -1;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: .3;
}

.empty-state h3 {
  font-size: 1.5rem;
  margin-bottom: .5rem;
  color: #333;
}

.empty-state p {
  font-size: 1rem;
  line-height: 1.5;
  max-width: 400px;
  margin: 0 auto;
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

  .register-btn {
    align-self: stretch;
    padding: 1rem;
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

  .sort-options {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 480px) {
  .card-grid-container {
    width: 95%;
    padding: 1rem;
    border-radius: 12px;
  }

  .card-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }
}
</style>

<!-- ====== 전역 스타일(UNSCOPED) : Teleport 모달/링크/스크롤락 ====== -->
<style>
/* 모달: Teleport된 요소에 반드시 unscoped로 적용되어야 함 */
.game-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .8);
  backdrop-filter: blur(5px);
  z-index: 1000;
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
  outline: none;
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
  animation: cardFloat 2s ease-in-out infinite;
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
  animation: statFill .4s ease-out;
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

@keyframes modalFadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes modalSlideIn {
  from {
    transform: scale(.9) translateY(-20px);
    opacity: 0;
  }

  to {
    transform: scale(1) translateY(0);
    opacity: 1;
  }
}

@keyframes cardFloat {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-5px);
  }
}

@keyframes statFill {
  from {
    width: 0;
  }
}

/* 전역 링크 요구사항 */
a {
  text-decoration: none;
  color: inherit;
}

a:visited {
  text-decoration: none;
  color: inherit;
}

a:hover {
  text-decoration: underline;
}

/* 스크롤 락 */
html.scroll-lock,
html.scroll-lock body {
  overflow: hidden;
}
</style>
