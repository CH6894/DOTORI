<template>
  <div class="figure-grid-container">
    <div class="figure-header">
      <img :src="chimLogo" alt="침착맨" class="title-img" />
      <div class="collection-stats">
        <span>수집한 굿즈: {{ collected }}/{{ total }} • 완성도: {{ completionRate }}%</span>
      </div>
    </div>

    <div class="figure-grid">
      <div 
        v-for="item in chimCollection" 
        :key="item.id" 
        class="figure-card"
      >
        <div
          class="chim-image-container"
          :class="{ uncollected: !item.verified }"
        >
          <img
            :src="item.image"
            :alt="item.name"
            class="chim-image"
            @error="handleImageError"
          />
          <div v-if="item.verified" class="verified-badge">✓</div>
        </div>

        <div class="figure-info">
          <h4 class="figure-name">{{ item.name }}</h4>
          <p class="figure-title">{{ item.desc }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import chimCollection from '@/data/chimCollection.js'   
import chimLogoFile from '@/assets/Chim/ccm.jpg'      
export default {
  name: 'Chim',
  data() {
    return {
      chimCollection,
      chimLogo: chimLogoFile,  // ✅ 템플릿에서 사용 가능
    }
  },
  computed: {
    total() {
      return this.chimCollection.length
    },
    collected() {
      return this.chimCollection.filter(p => p.verified).length
    },
    completionRate() {
      return Math.round((this.collected / this.total) * 100)
    }
  },
  methods: {
    handleImageError(event) {
      console.warn('Image failed to load:', event.target.src)
    }
  }
}
</script>

<style scoped>
@import '@/assets/styles/collection.css';
/* ========== ChimCollection.vue 전용 스타일 ========== */

/* 헤더 */
.figure-header {
  text-align: center;
  margin-bottom: 2.5rem;
  padding: 2rem;
  background: linear-gradient(135deg, rgba(0,0,0,0.3) 0%, rgba(0,0,0,0.6) 100%);
  border-radius: 20px;
  color: #fdfdfd;
  box-shadow: inset 0 3px 10px rgba(255,255,255,0.1),
              0 8px 20px rgba(0,0,0,0.4);
}
.title-img{
  height: 100px;      /* 필요에 맞게 조절 */
  width: auto;
  display: block;
  margin: 0 auto 1rem;
}

/* 그리드 */
.figure-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 2rem 1.5rem;
  min-height: 400px;
  justify-items: center;
}
.chim-image{
  width: 100%;
  height: auto;
  object-fit: contain;
  display: block;
}

/* ✅ 미수집(잠금) 오버레이: 컨테이너 전체를 정확히 덮도록 inset:0 추가 */
.chim-image-container.uncollected {
  filter: grayscale(100%) blur(1px);
  opacity: 0.6;
  position: relative;
}
.chim-image-container.uncollected::before {
  content: '';
  position: absolute;
  inset: 0;  /* ← 전체 덮기 */
  background: linear-gradient(135deg, rgba(0,0,0,0.3) 0%, rgba(0,0,0,0.6) 100%);
  z-index: 5;
}
.chim-image-container.uncollected::after {
  content: '🔒';
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  background: linear-gradient(135deg, rgba(103, 6, 0, 0.9), rgba(139, 69, 19, 0.9));
  color: white;
  padding: 0.8rem 1.2rem;
  border-radius: 25px;
  font-size: 1.2rem;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0,0,0,0.5);
  box-shadow: 0 4px 12px rgba(103, 6, 0, 0.4), inset 0 1px 0 rgba(255,255,255,0.2);
  border: 2px solid rgba(239, 236, 198, 0.3);
  backdrop-filter: blur(8px);
  z-index: 10;
  animation: lockPulse 2s ease-in-out infinite;
  letter-spacing: 0.5px;
}
@keyframes lockPulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.9; }
  50%      { transform: translate(-50%, -50%) scale(1.05); opacity: 1; }
}

/* 미수집 카드 호버 무력화 (브라우저가 :has 지원 시) */
.figure-card:has(.chim-image-container.uncollected):hover {
  transform: none;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  border-color: #f0f0f0;
}

/* 카드 정보 */
.figure-info {
  padding: 1.5rem;
  background: linear-gradient(to bottom, #ffffff, #fafbfc);
  position: relative;
}
.figure-name {
  font-size: 1.2rem;
  font-weight: 700;
  color: #333333;
  margin: 0 0 0.5rem 0;
  line-height: 1.3;
  text-shadow: 0 1px 2px rgba(103,6,0,0.05);
}
.figure-title {
  color: #670600;
  font-weight: 600;
  font-size: 0.95rem;
  margin: 0 0 0.8rem 0;
}

/* 반응형 */
@media (max-width: 1280px) {
  .figure-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 1024px) {
  .figure-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .figure-grid { grid-template-columns: repeat(2, 1fr); gap: 1.5rem 1rem; }
}
@media (max-width: 480px) {
  .figure-grid { grid-template-columns: repeat(2, 1fr); gap: 1rem; }
}
</style>
