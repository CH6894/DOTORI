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
.hero-slider {
  position: relative;
}

.hero-viewport {
  overflow: hidden;
}

.hero-track {
  display: flex;
  transition: transform .5s ease;
}

.hero-slide {
  min-width: 100%;
  position: relative;
  display: block;
  height: 360px;
  overflow: hidden;
}

/* 이미지 영역 */
.hero__art {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.hero__art img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
}

/* 텍스트 */
.hero__content {
  position: relative;
  z-index: 1;
  color: #fff;
  padding: 40px 6vw;
  text-shadow: 0 2px 8px rgba(0, 0, 0, .35);
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
  left: 8px;
}

.hero-btn.next {
  right: 8px;
}

/* 점 네비 */
.hero-dots {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-top: 10px;
  position: relative;
  z-index: 2;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 0;
  background: #cfcfcf;
  cursor: pointer;
}

.dot.active {
  background: #333;
}
</style>
