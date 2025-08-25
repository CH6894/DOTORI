<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

/** 슬라이드 데이터 */
const slides = [
  {
    href: '/promo/summer',
    image: '/img/hero/Banner.webp'
  },
  {
    href: '/promo/new',
    image: '/img/hero/Banner3.webp'
  },
  {
    href: '/promo/sale',
    image: '/img/hero/Banner2.webp'
  }
]

/** 상태 */
const index = ref(0)
let timer: ReturnType<typeof setInterval> | null = null
const intervalMs = 4000 // 자동 슬라이드 주기(ms)

/** 동작 */
function go(n: number) {
  index.value = (n + slides.length) % slides.length
}
function next() { go(index.value + 1) }
function prev() { go(index.value - 1) }

function startAuto() {
  stopAuto()
  timer = setInterval(next, intervalMs)
}
function stopAuto() {
  if (timer) { clearInterval(timer); timer = null }
}

onMounted(startAuto)
onBeforeUnmount(stopAuto)
</script>

<template>
  <section class="hero-slider" aria-label="프로모션 배너 목록" @mouseenter="stopAuto" @mouseleave="startAuto">
    <!-- 좌우 버튼 -->
    <button class="hero-btn prev" type="button" aria-label="이전 배너" @click="prev">&#10094;</button>
    <button class="hero-btn next" type="button" aria-label="다음 배너" @click="next">&#10095;</button>

    <!-- 슬라이드 -->
    <div class="hero-viewport">
      <div class="hero-track" :style="{ transform: `translateX(-${index * 100}%)` }">
        <a v-for="(s, i) in slides" :key="i" class="hero hero-slide" :href="s.href">
          <!-- 배경 이미지 -->
          <div class="hero__art" aria-hidden="true">
            <img :src="s.image" alt="" loading="lazy" decoding="async" />
          </div>
        </a>
      </div>
    </div>

    <!-- 점 네비 -->
    <div class="hero-dots" role="tablist" aria-label="배너 선택">
      <button v-for="(s, i) in slides" :key="i" class="dot" :class="{ active: i === index }" @click="go(i)"
        :aria-label="`${i + 1}번째 배너로 이동`"></button>
    </div>
  </section>
</template>

<style scoped>
/* ---------- Hero Slider ---------- */
.hero-slider {
  position: relative;
  padding-top: 12px;
}

.hero-viewport {
  overflow: hidden;
  border-radius: 24px;
}

.hero-track {
  display: flex;
  will-change: transform;
  transition: transform 0.45s cubic-bezier(0.2, 0.8, 0.2, 1);
}

/* 각 슬라이드 */
.hero-slide {
  flex: 0 0 100%;
  min-width: 100%;
  position: relative;
  display: block;
  height: 360px;
  overflow: hidden;
  text-decoration: none;
  /* 링크 밑줄 제거 */
}

.hero-slide:visited {
  color: inherit;
  text-decoration: none;
}

/* 이미지 */
.hero__art {
  position: absolute;
  inset: 0;
  z-index: 1;
  /* 통일 */
  border-radius: 18px;
  background: radial-gradient(80px 80px at 20% 80%, #ffcfed33 0, transparent 60%),
    radial-gradient(80px 80px at 70% 20%, #8ad3ff33 0, transparent 60%),
    linear-gradient(135deg, #2b262d, #1f1a23);
}

.hero__art img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
}

.hero__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* 텍스트 */
.hero__content {
  position: relative;
  z-index: 1;
  color: #fff;
  padding: 40px 6vw;
  text-shadow: 0 2px 8px rgba(0, 0, 0, .35);
  align-self: center;
}

.hero h1 {
  font-size: 40px;
  line-height: 1.15;
  margin: 0 0 10px;
  letter-spacing: -0.5px;
}

.hero__sub {
  margin: 0;
  color: #d6cde8;
}

/* Hero 장식 */
.hero::before {
  content: "";
  position: absolute;
  inset: 0;
  background:
    radial-gradient(2px 2px at 10% 20%, #fff8 50%, transparent 51%) repeat,
    radial-gradient(1.5px 1.5px at 80% 30%, #fff6 50%, transparent 51%) repeat,
    radial-gradient(1.5px 1.5px at 45% 70%, #fff7 50%, transparent 51%) repeat;
  background-size: 180px 140px, 220px 160px, 200px 140px;
  pointer-events: none;
}

/* 좌우 버튼 */
.hero-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  border: 0;
  background: rgba(0, 0, 0, .3);
  color: #fff;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  cursor: pointer;
}

.hero-btn.prev {
  left: 10px;
}

.hero-btn.next {
  right: 10px;
}

.hero-btn:hover {
  filter: brightness(1.03);
}

/* 인디케이터 (dots) */
.hero-dots {
  position: absolute;
  left: 50%;
  bottom: 10px;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  padding: 16px;
  z-index: 5;
}

.hero-dots button {
  width: 10px;
  height: 10px;
  border-radius: 50%;      
  border: 0;
  background: #d7cbbb;    
  cursor: pointer;
  transition: all 0.25s ease;
}

/* 모션 줄이기 선호 시 */
@media (prefers-reduced-motion: reduce) {
  .hero-track {
    transition: none;
  }
}
</style>
