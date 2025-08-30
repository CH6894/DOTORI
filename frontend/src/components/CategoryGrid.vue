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
/* ---------- Sections (Responsive) ---------- */
.category-grid{
  display: grid;
  gap: 0.5rem;                       /* 8px */
  max-width: 78rem;                  /* 1248px */
  width: 100%;
  margin: 0 auto;

  /* Desktop: 4 cols, 기존 영역 유지 */
  grid-template-columns: repeat(4, minmax(0, 1fr));
  grid-template-areas:
    "anime  anime  webtoon game"
    "sports creator kpop   kpop";
}

/* 순서 매핑 (DOM 순서 그대로) */
.cat-card:nth-of-type(1){ grid-area: anime; }
.cat-card:nth-of-type(2){ grid-area: webtoon; }
.cat-card:nth-of-type(3){ grid-area: game; }
.cat-card:nth-of-type(4){ grid-area: sports; }
.cat-card:nth-of-type(5){ grid-area: creator; }
.cat-card:nth-of-type(6){ grid-area: kpop; }

/* 카드 = 전체가 버튼 + 이미지 */
.cat-card{
  position: relative;
  display: block;
  min-height: 15.625rem;             /* 250px */
  border-radius: var(--radius);
  overflow: hidden;
  background: #ddd;
  border: 0.0625rem solid #eadfc9;    /* 1px */
  box-shadow: var(--shadow);
  text-decoration: none;
  transition: transform .2s ease, box-shadow .2s ease;
}
.cat-card:hover{
  transform: translateY(-0.1875rem);  /* -3px */
  box-shadow: 0 0.625rem 1.625rem rgba(0,0,0,.10); /* 10px 26px */
  text-decoration: none;
}

/* 이미지: 카드 꽉 채우기 */
.cat-card__img{
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 라벨 */
.cat-card__label{
  position: absolute;
  right: 1rem;                        /* 16px */
  bottom: 0.875rem;                   /* 14px */
  z-index: 2;
  padding: 0.25rem 0.625rem;          /* 4px 10px */
  border-radius: 0.375rem;            /* 6px */
  background: transparent;
  color: #fff;
  font-weight: 800;
  font-size: 1.5rem;                  /* 24px */
  line-height: 1;
}

/* 가독성 오버레이 */
.cat-card::after{
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,.65), rgba(0,0,0,0) 60%);
  pointer-events: none;
  z-index: 1;
}

/* ---------- Breakpoints ---------- */
/* ≤1280px: 2열로 재배치 */
@media (max-width: 80rem){            /* 1280px */
  .category-grid{
    grid-template-columns: repeat(2, minmax(0, 1fr));
    /* 2열에서의 보기 좋은 배치 */
    grid-template-areas:
      "anime  anime"
      "webtoon game"
      "kpop   kpop"
      "sports creator";
  }
  .cat-card{ min-height: 12.5rem; }   /* 200px */
}

/* ≤768px: 1열 스택 */
@media (max-width: 48rem){            /* 768px */
  .category-grid{
    grid-template-columns: 1fr;
    grid-template-areas:
      "anime"
      "webtoon"
      "game"
      "kpop"
      "sports"
      "creator";
  }
  .cat-card{ min-height: 11.25rem; }  /* 180px 정도로 살짝 더 낮춤 */
  .cat-card__label{ font-size: 1.25rem; } /* 20px */
}

/* 링크 visited (요청 사양 유지) */
.category-grid a:visited{
  text-decoration: none;
  color: inherit;
}

/* 구분선 */
.divider{
  border: 0;
  height: 0.0625rem;                 /* 1px */
  background-color: #e5e5e5;
  margin: 2.5rem 0;                  /* 40px */
}
</style>
