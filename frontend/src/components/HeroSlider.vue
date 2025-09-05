<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { RouterLink } from 'vue-router'

/** 슬라이드 데이터 */
const slides = [
  {
    href: '/dex',
    image: '/img/hero/Banner.webp'
  },
  {
    href: '/category',
    image: '/img/hero/Banner3.webp'
  },
  {
    href: '/inspection',
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
        <!-- ✅ RouterLink로 변경해서 SPA 네비게이션 -->
        <!-- 원본 슬라이드들 -->
        <RouterLink
          v-for="(s, i) in slides"
          :key="i"
          class="hero hero-slide"
          :to="s.href"
        >
          <div class="hero__art" aria-hidden="true">
            <img :src="s.image" alt="" loading="lazy" decoding="async" />
          </div>
        </RouterLink>
      </div>
    </div>

    <!-- 점 네비 -->
    <div class="hero-dots" role="tablist" aria-label="배너 선택">
      <button
        v-for="(s, i) in slides"
        :key="i"
        class="dot"
        :class="{ active: i === index }"
        :aria-selected="i === index"
        @click="go(i)"
        :aria-label="`${i + 1}번째 배너로 이동`"
      ></button>
    </div>
  </section>
</template>

<style scoped>
/* ---------- Hero Slider ---------- */
.hero-slider {
  position: relative;
  padding-top: 0.75rem; /* 12px */
}

.hero-viewport {
  overflow: hidden;
  border-radius: 1.5rem; /* 24px */
}

.hero-track {
  display: flex;
  will-change: transform;
  transition: transform 0.6s ease-in-out;
}

/* 각 슬라이드 */
.hero-slide {
  flex: 0 0 100%;
  min-width: 100%;
  position: relative;
  display: block;
  height: 22.5rem; /* 360px */
  overflow: hidden;
  text-decoration: none;
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
  border-radius: 1.125rem; /* 18px */
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
  padding: 2.5rem 6vw; /* 40px → rem, 가로는 vw 유지 */
  text-shadow: 0 0.125rem 0.5rem rgba(0, 0, 0, .35); /* 2px 8px */
  align-self: center;
}

.hero h1 {
  font-size: 2.5rem; /* 40px */
  line-height: 1.15;
  margin: 0 0 0.625rem; /* 10px */
  letter-spacing: -0.031rem; /* -0.5px */
}

.hero__sub {
  margin: 0;
  color: #d6cde8;
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
  width: 2.75rem;  /* 44px */
  height: 2.75rem; /* 44px */
  border-radius: 50%;
  cursor: pointer;
}

.hero-btn.prev {
  left: 0.625rem; /* 10px */
}

.hero-btn.next {
  right: 0.625rem; /* 10px */
}

.hero-btn:hover {
  filter: brightness(1.03);
}


/* 인디케이터 (dots) */
.hero-dots {
  position: absolute;
  left: 50%;
  bottom: 0.625rem; /* 10px */
  transform: translateX(-50%);
  display: flex;
  gap: 0.5rem; /* 8px */
  padding: 1rem; /* 16px */
  z-index: 5;
}

.hero-dots .dot{
  width: 0.625rem;  /* 10px */
  height: 0.625rem; /* 10px */
  border-radius: 50%;
  border: 0;
  background: #d1d1d1;           
  cursor: pointer;
  transition: transform .2s ease, background-color .2s ease;
}

.hero-dots .dot:hover{ transform: scale(1.1) }

.hero-dots .dot.active,
.hero-dots .dot[aria-selected="true"]{
  background: #3b3b3b;          
}

/* 접근성: 키보드 포커스 링 */
.hero-dots .dot:focus-visible{
  outline: 0.125rem solid #0f0f0f; /* 2px */
  outline-offset: 0.125rem;        /* 2px */
}

/* 모션 줄이기 선호 시 */
@media (prefers-reduced-motion: reduce) {
  .hero-track {
    transition: none;
  }
}
</style>
