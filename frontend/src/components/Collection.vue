<template>
  <div class="pokemon-collection">
    <!-- 등록 섹션 -->
    <div class="register-section">
      <div class="register-container">
        <div class="register-bar">
          <div class="search-input-wrapper">
            <input type="text" class="register-input" placeholder="영수증 번호 또는 인증코드를 입력하세요" v-model="registerCode"
              @keypress.enter="registerCard">
            <button v-if="registerCode" class="clear-btn" @click="clearSearch" title="전체 삭제">
              ✕
            </button>
          </div>
          <button class="register-btn" @click="registerCard">등록하기</button>
        </div>
      </div>
    </div>

      <TopTabs :items="TopCategoryData" v-model="top" />

      <!-- Mid Tabs -->
      <MidTabs :items="midOptions" v-model="mid" :visible="true" />

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
            @click="card.special ? showCardDetail(card) : null"
            :style="{ cursor: card.special ? 'pointer' : 'default' }">
            <div class="card-image-container">
              <img :src="card.image" :alt="card.name" class="pokemon-card-image" @error="handleImageError" />
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

    <!-- 게임 스타일 모달 -->
    <div v-if="selectedCard" class="game-modal-overlay" @click="closeModal">
      <div class="game-modal" @click.stop>
        <div class="modal-header">
          <div class="modal-title">포켓몬 도감</div>
          <button class="modal-close" @click="closeModal">✕</button>
        </div>

        <div class="modal-body">
          <div class="card-display">
            <img :src="selectedCard.image" :alt="selectedCard.name" class="modal-card-image">
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
      return this.subCategories[this.currentCategory] || [];
    },

    currentCategoryName() {
      const category = this.categories.find(cat => cat.id === this.currentCategory);
      return category ? category.name : '';
    },

    showPokemonGrid() {
      return this.currentCategory === 'game' && this.currentSubCategory === 'pokemon';
    },

    resultText() {
      if (this.showPokemonGrid) {
        return `(${this.currentCards}/${this.totalCards})`;
      }
      return '(0/0)';
    },

    sortedPokemonCards() {
      let cards = [...this.pokemonCards];

      if (this.currentSort === 'number') {
        cards.sort((a, b) => parseInt(a.number) - parseInt(b.number));
      } else if (this.currentSort === 'latest') {
        cards.sort((a, b) => b.id - a.id);
      }

      return cards;
    }
  },

  watch: {
    currentCategory() {
      if (this.currentSubCategories.length > 0) {
        this.currentSubCategory = this.currentSubCategories[0].id;
      }
    }
  },

  methods: {
    setCategory(categoryId) {
      this.currentCategory = categoryId;
      // 카테고리 변경 시 첫 번째 서브카테고리 자동 선택
      if (this.subCategories[categoryId] && this.subCategories[categoryId].length > 0) {
        this.currentSubCategory = this.subCategories[categoryId][0].id;
      } else {
        this.currentSubCategory = '';
      }
    },

    setSubCategory(subCategoryId) {
      this.currentSubCategory = subCategoryId;
    },

    setSort(sortId) {
      this.currentSort = sortId;
    },

    toggleSort() {
      this.currentSort = this.currentSort === 'number' ? 'latest' : 'number';
    },

    registerCard() {
      if (!this.registerCode.trim()) {
        alert('영수증 번호 또는 인증코드를 입력해주세요.');
        return;
      }

      // 인증 코드 예시들
      const validCodes = {
        'PIKACHU001': { id: 25, name: '피카츄' },
        'CHARIZARD001': { id: 1, name: '파이리' },
        'MANAPHY001': { id: 10, name: '모나피' },
        'RECEIPT123456': { id: 26, name: '라이츄' },
        'PURCHASE789': { id: 150, name: '뮤츠' }
      };

      const code = this.registerCode.toUpperCase();

      if (validCodes[code]) {
        const cardInfo = validCodes[code];

        // 해당 카드를 special로 변경
        const cardIndex = this.pokemonCards.findIndex(card => card.id === cardInfo.id);
        if (cardIndex !== -1) {
          this.pokemonCards[cardIndex].special = true;
          this.pokemonCards[cardIndex].verified = true; // 인증 마크
        }

        alert(`✅ ${cardInfo.name} 카드가 성공적으로 등록되었습니다! 인증 마크가 부여되었습니다.`);
        this.registerCode = '';
      } else {
        alert('❌ 올바르지 않은 인증코드입니다. 영수증을 확인해주세요.');
      }
    },

    clearSearch() {
      this.registerCode = '';
    },

    getTypeKorean(type) {
      const typeMap = {
        fire: '불꽃',
        water: '물',
        grass: '풀',
        electric: '전기',
        psychic: '에스퍼',
        normal: '노말'
      };
      return typeMap[type] || type;
    },

    showCardDetail(card) {
      this.selectedCard = card;
      document.body.style.overflow = 'hidden';
      // 헤더 숨기기
      const header = document.querySelector('header');
      const nav = document.querySelector('nav');
      if (header) header.style.display = 'none';
      if (nav) nav.style.display = 'none';
    },

    closeModal() {
      this.selectedCard = null;
      document.body.style.overflow = 'auto';
      // 헤더 다시 보이기
      const header = document.querySelector('header');
      const nav = document.querySelector('nav');
      if (header) header.style.display = 'block';
      if (nav) nav.style.display = 'block';
    },

    handleImageError(event) {
      console.log('Image failed to load:', event.target.src);
    }
  }
}
</script>

<style scoped>
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
  transition: all 0.3s;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.register-input:focus {
  border-color: #670600;
  box-shadow: 0 4px 12px rgba(103, 6, 0, 0.1);
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
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
}

.clear-btn:hover {
  background: rgba(103, 6, 0, 0.1);
  color: #670600;
  transform: scale(1.1);
}

.register-btn {
  background: linear-gradient(45deg, #670600, #8B4513);
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 15px;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(103, 6, 0, 0.2);
  white-space: nowrap;
}

.register-btn:hover {
  background: linear-gradient(45deg, #5a0500, #7a3a0f);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(103, 6, 0, 0.3);
}

/* 카테고리 탭 스타일 */
.product_list_wrap {
  width: 100%;
  display: flex;
  justify-content: center;
}

.top_category_list ul {
  display: flex;
  list-style: none;
  flex-wrap: wrap;
  width: 1280px;
  padding-left: 0px;
  margin: 0 auto;
  padding-top: 20px;
  gap: 4px;
}

.top_category_link {
  border: none;
  background: none;
  color: #670600;
  cursor: pointer;
  padding: 15px 30px;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  background: #F4F3E6;
  transition: all 0.3s;
  font-size: 1.2rem;
  font-weight: 600;
}

.top_category_link.selected {
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  background: #EFECC6;
  font-size: 1.2rem;
  font-weight: 600;
}

.top_category_link:hover {
  background: #EFECC6;
}

.mid_category_list ul {
  display: flex;
  list-style: none;
  flex-wrap: wrap;
  padding-left: 0px;
  margin: 0 auto;
  background: #EFECC6;
  width: 1280px;
  padding: 10px 0;
  min-height: 50px;
  align-items: center;
}

.mid_category_link {
  border: none;
  background: transparent;
  color: #333333;
  cursor: pointer;
  padding: 10px 20px;
  transition: all 0.3s;
  font-size: 1rem;
  font-weight: 500;
  width: auto;
  min-width: fit-content;
  line-height: 1.5;
  height: 40px;
  display: flex;
  align-items: center;
}

.mid_category_link.selected {
  border: none;
  background: transparent;
  color: #670600;
  cursor: pointer;
  padding: 10px 20px;
  font-size: 1rem;
  font-weight: 500;
  line-height: 1.5;
  height: 40px;
  display: flex;
  align-items: center;
}

.mid_category_link:hover {
  color: #670600;
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
}

.result-count {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

.sort-options {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.sort-btn {
  background: white;
  border: 1px solid #ddd;
  padding: 0.4rem 0.8rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  white-space: nowrap;
}

.sort-btn.active {
  background: #ff6b35;
  color: white;
  border-color: #ff6b35;
}

.sort-btn:hover {
  background: #f8f9fa;
  border-color: #ff6b35;
}

.sort-btn.active:hover {
  background: #e55a2b;
}

/* 카드 그리드 컨테이너 */
.card-grid-container {
  max-width: 1200px;
  width: 90%;
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  padding: 2rem;
  margin: 0 auto;
  border: 2px solid #EFECC6;
}

/* 카드 그리드 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 2rem 1.5rem;
  min-height: 400px;
  justify-items: center;
}

/* 포켓몬 카드 */
.pokemon-card {
  background: transparent;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
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
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(103, 6, 0, 0.3);
  border: 3px solid #EFECC6;
  animation: verifiedGlow 3s ease-in-out infinite;
}

@keyframes verifiedGlow {

  0%,
  100% {
    transform: scale(1);
    box-shadow: 0 4px 12px rgba(103, 6, 0, 0.3);
  }

  50% {
    transform: scale(1.05);
    box-shadow: 0 6px 16px rgba(103, 6, 0, 0.4), 0 0 20px rgba(239, 236, 198, 0.5);
  }
}

/* 게임 스타일 모달 */
.game-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(5px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: modalFadeIn 0.15s ease-out;
}

.game-modal {
  background: linear-gradient(145deg, #1a1a2e, #16213e);
  border: 3px solid #0f3460;
  border-radius: 20px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.7),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  animation: modalSlideIn 0.2s ease-out;
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
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
}

.modal-close {
  background: none;
  border: none;
  color: #fff;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.3s;
}

.modal-close:hover {
  background: rgba(255, 0, 0, 0.2);
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
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.5);
  animation: cardFloat 2s ease-in-out infinite;
}

.card-details {
  flex: 1;
  color: white;
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
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
  margin: 0;
}

.pokemon-number {
  background: linear-gradient(45deg, #ff6b6b, #ff8e8e);
  color: white;
  padding: 0.3rem 0.8rem;
  border-radius: 15px;
  font-size: 0.9rem;
  font-weight: bold;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
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
  color: white;
  padding: 0.4rem 1rem;
  border-radius: 20px;
  font-weight: bold;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
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
  font-size: 0.9rem;
  font-weight: bold;
}

.stat-bar {
  flex: 1;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.3);
}

.stat-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease-out;
  animation: statFill 0.4s ease-out;
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
  background: rgba(0, 0, 0, 0.2);
}

.confirm-btn {
  background: linear-gradient(45deg, #ff6b6b, #ff8e8e);
  color: white;
  border: none;
  padding: 0.8rem 2rem;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.3s;
}

.confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.4);
  background: linear-gradient(45deg, #ff5252, #ff7979);
}

.confirm-btn:active {
  transform: translateY(0);
}

/* 애니메이션 */
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
    transform: scale(0.9) translateY(-20px);
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

/* 모바일 반응형 */
@media (max-width: 768px) {
  .game-modal {
    width: 95%;
    margin: 1rem;
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

  .pokemon-name-section {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
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
  opacity: 0.3;
}

.empty-state h3 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
  color: #333;
}

.empty-state p {
  font-size: 1rem;
  line-height: 1.5;
  max-width: 400px;
  margin: 0 auto;
}

/* 반응형 디자인 */
@media (max-width: 1280px) {
  .card-grid-container {
    width: 85%;
    margin: 0 auto;
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

  .search-container,
  .tabs-container,
  .main-content {
    padding: 0 1rem;
  }

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

  .register-input {
    max-width: none;
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

  .tab-list {
    overflow-x: auto;
    scrollbar-width: none;
  }

  .tab-list::-webkit-scrollbar {
    display: none;
  }

  .sub-tabs {
    overflow-x: auto;
    scrollbar-width: none;
  }

  .sub-tabs::-webkit-scrollbar {
    display: none;
  }

  .content-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .sort-options {
    width: 100%;
    justify-content: flex-start;
  }

  .category-tabs {
    height: auto;
    min-height: 99px;
  }

  .tab-list {
    height: auto;
    min-height: 60px;
    padding: 0.5rem 0;
  }

  .sub-tabs {
    height: auto;
    min-height: 39px;
    padding: 0.5rem 0;
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

  .tab-btn {
    font-size: 0.9rem;
    padding: 0 1rem;
  }

  .sub-tab {
    font-size: 0.8rem;
    padding: 0 1rem;
  }

  .empty-state {
    padding: 3rem 1rem;
  }

  .empty-icon {
    font-size: 3rem;
  }

  .empty-state h3 {
    font-size: 1.3rem;
  }

  .empty-state p {
    font-size: 0.9rem;
  }
}
</style>