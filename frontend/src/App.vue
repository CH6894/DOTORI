<template>
  <div class="app">
    <Header />
    <main class="main-content">
      <router-view></router-view>
    </main>
    <Footer />
  </div>
</template>

<script>

import Header from "./components/AppHeader.vue";
import Footer from "./components/AppFooter.vue";


export default {
  name: "app",
  components: {
    Header:Header,
    Footer:Footer,
  },
}
</script>

<style>

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