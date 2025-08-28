<!-- src/components/ComingSoon.vue -->
<template>
  <main class="comingsoon">
    <section class="container">
      <h1 class="title">🚧 준비 중입니다</h1>
      <p class="desc">
        해당 페이지는 현재 준비 중입니다.<br />
        빠른 시일 내에 찾아뵐게요!
      </p>
      <RouterLink to="/" class="back-home">홈으로 가기</RouterLink>
    </section>
  </main>
</template>

<script setup lang="ts">
// 특별한 로직 필요 없음
</script>

<style scoped>
.comingsoon {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.container {
  max-width: 640px;
  padding: 40px 20px;
}

.title {
  font-size: 28px;
  font-weight: 800;
  color: #2d251c;
  margin-bottom: 16px;
}

.desc {
  font-size: 16px;
  color: #666;
  margin-bottom: 24px;
  line-height: 1.6;
}

.back-home {
  display: inline-block;
  padding: 10px 20px;
  border-radius: 8px;
  background: #670600;
  color: #fff;
  font-weight: 700;
  text-decoration: none;
}
.back-home:hover {
  background: #8c0a0a;
}
</style>
