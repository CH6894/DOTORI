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
