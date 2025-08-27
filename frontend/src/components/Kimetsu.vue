<template>
  <div class="figure-grid-container">
    <div class="figure-header">
      <h2 class="dictionary-title">
        <img :src="kimetsuLogo" alt="귀멸의 칼날" class="title-img" />
      </h2>
      <div class="collection-stats">
        <span>총 수집품: {{ collected }}/{{ total }}</span>
        <span>•</span>
        <span>완성도: {{ completionRate }}%</span>
      </div>
    </div>

    <div class="figure-grid">
      <div v-for="figure in kimetsuFigures" :key="figure.name" class="figure-card">
        <div class="figure-image-container" :class="{ 'uncollected': !figure.verified }">
          <img :src="figure.image" :alt="figure.name" class="figure-image" @error="handleImageError" />
          <div v-if="figure.verified" class="verified-badge">✓</div>
        </div>

        <div class="figure-info">
          <h4 class="figure-name">{{ figure.name }}</h4>
          <P class="figure-title">시리즈: {{ figure.series }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import kimetsuFigures from '@/data/kimetsuFigures.js' // 확장자 명시 권장
import logoKimetsu from '@/assets/ani/kimetsu.webp'   // 변수명 살짝 바꿔 혼동 방지

export default {
  name: 'Kimetsu',
  data() {
    return {
      kimetsuFigures,
      kimetsuLogo: logoKimetsu,  // ← 템플릿에서 사용할 수 있게 노출
    }
  },
  computed: {
    total() {
      return this.kimetsuFigures.length
    },
    collected() {
      return this.kimetsuFigures.filter(f => f.verified).length
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
/* ========== Kimetsu.vue 전용 스타일 ========== */

/* 귀멸의 칼날 컬렉션 그리드 */
.figure-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 2rem 1.5rem;
  min-height: 400px;
  justify-items: center;
}

/* 귀멸의 칼날 미수집 아이템 스타일 */
.figure-image-container.uncollected {
  filter: grayscale(100%) blur(1px);
  opacity: 0.6;
  position: relative;
}

.figure-image-container.uncollected::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.3) 0%, rgba(0, 0, 0, 0.6) 100%);
  z-index: 5;
}

.figure-image-container.uncollected::after {
  content: '🔒';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: linear-gradient(135deg, rgba(103, 6, 0, 0.9), rgba(139, 69, 19, 0.9));
  color: white;
  padding: 0.8rem 1.2rem;
  border-radius: 25px;
  font-size: 1.2rem;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  box-shadow: 0 4px 12px rgba(103, 6, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(239, 236, 198, 0.3);
  backdrop-filter: blur(8px);
  z-index: 10;
  animation: lockPulse 2s ease-in-out infinite;
  letter-spacing: 0.5px;
}

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
  text-shadow: 0 1px 2px rgba(103, 6, 0, 0.05);
}

.figure-title {
  color: #670600;
  font-weight: 600;
  font-size: 0.95rem;
  margin: 0 0 0.8rem 0;
}

.figure-details {
  font-size: 0.85rem;
  color: #666666;
  line-height: 1.5;
  padding: 0.8rem;
  background: #ffffff;
  border-radius: 8px;
}

.detail-item {
  margin-bottom: 0.4rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.detail-label {
  font-weight: 600;
  color: #670600;
  min-width: 60px;
}

.detail-value {
  color: #333333;
  font-weight: 500;
  flex: 1;
}

.dictionary-title {
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 가운데 정렬 */
  min-height: 48px;
  /* 기존 h2 높이감 있으면 유지 */
}

.title-img {
  height: 100px;
  /* 원하는 크기로 조절 */
  width: auto;
  margin-bottom: 1rem;
  object-fit: contain;
  display: block;
  /* 이미지 하단 여백 제거 */
}


/* 반응형 */
@media (max-width: 1280px) {
  .figure-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1024px) {
  .figure-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .figure-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1.5rem 1rem;
  }
}

@media (max-width: 480px) {
  .figure-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }
}
</style>