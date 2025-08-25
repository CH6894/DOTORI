<script setup lang="ts">
const cats = [
  { key: "Animation", img: "/img/category/Anime.jpg" },
  { key: "Webtoon",   img: "/img/category/Webtoon.jpg" },
  { key: "Game",      img: "/img/category/Game.webp" },
  { key: "Sports",    img: "/img/category/Sports.webp" },
  { key: "Creator",   img: "/img/category/Creator.jpg" },
  { key: "Kpop",      img: "/img/category/KPOP.webp" }, // 파일명 대소문자 확인!
]
</script>

<template>
  <div class="category-grid">
    <RouterLink
      v-for="c in cats"
      :key="c.key"
      class="cat-card"
      :to="{ name: 'search', query: { top: c.key } }"
      :aria-label="c.key"
    >
      <img class="cat-card__img" :src="c.img" :alt="c.key" />
      <span class="cat-card__label">{{ c.key }}</span>
    </RouterLink>
  </div>
</template>

<style scoped>
/* ---------- Sections ---------- */
.category-grid {
  display: grid;
  gap: 8px;
  width: 1248px;
  margin: 0 auto;
  grid-template-columns: 306px 306px 306px 306px;
  grid-template-areas:
    "anime anime webtoon game"
    "sports creator kpop kpop";
}

/* 순서 매핑 */
.cat-card:nth-of-type(1) {
  grid-area: anime;
}
.cat-card:nth-of-type(2) {
  grid-area: webtoon;
}
.cat-card:nth-of-type(3) {
  grid-area: game;
}
.cat-card:nth-of-type(4) {
  grid-area: sports;
}
.cat-card:nth-of-type(5) {
  grid-area: creator;
}
.cat-card:nth-of-type(6) {
  grid-area: kpop;
}

/* 카드 = 전체가 버튼 + 이미지 */
.cat-card {
  position: relative;
  display: block;
  min-height: 250px; /* 카드 높이 */
  border-radius: var(--radius);
  overflow: hidden;
  background: #ddd; /* 이미지 로딩 전 배경 */
  border: 1px solid #eadfc9;
  box-shadow: var(--shadow);
  text-decoration: none;
  /* 호버 효과 그대로 */
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.cat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.1);
  text-decoration: none;
}

/* 이미지: 카드 꽉 채우기 */
.cat-card__img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover; /* 버튼 사이즈(카드 크기)에 딱 맞춤 */
}
/* 라벨 박스 */
.cat-card__label {
  position: absolute;
  right: 16px;
  bottom: 14px;
  z-index: 2;
  padding: 4px 10px;
  border-radius: 6px;
  background: transparent;

  color: #fff; /* 원색 글자 (shadow 없음) */
  font-weight: 800;
  font-size: 24px;
  line-height: 1;
}

/* 가독성용 오버레이(선택) */
.cat-card::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.65),
    rgba(0, 0, 0, 0) 60%
  );
  pointer-events: none;
  z-index: 1;
}

/* 반응형 */
@media (max-width: 1280px) {
  .category-grid {
    width: 100%;
    grid-template-columns: 1fr;
  }
  .cat-card {
    min-height: 200px;
  } /* 모바일에서 조금 낮춰도 됨 */
}

/* 링크 visited 스타일(요청 사양): 색상/밑줄 유지 */
.category-grid a:visited {
  text-decoration: none;
  color: inherit;
}
.divider {
  border: 0;
  height: 1px;
  background-color: #e5e5e5; /* 연한 회색 */
  margin: 40px 0; /* 위아래 간격 */
}
</style>