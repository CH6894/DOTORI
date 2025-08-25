<template>
  <div id="app">
    <AppHeader />   <!-- 공통 헤더 -->
    <main class="main-content">
      <RouterView />  <!-- 페이지별 본문 -->
    </main>
    <AppFooter />   <!-- 공통 푸터 -->
  </div>
</template>

<script>
import AppHeader from "@/components/AppHeader.vue"
import AppFooter from "@/components/AppFooter.vue"

export default {
  components: { AppHeader, AppFooter }   // ← 등록 필수
}
</script>

<style scoped>
#app {
  min-height: 100vh; /* 화면 전체 높이 */
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1; /* 남는 공간을 차지해서 footer를 아래로 밀어냄 */
  position: relative;
  z-index: 2;  
}

</style>
