<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
// App.vue는 라우터의 뼈대 역할만 수행
</script>

<style>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
}

#app {
  min-height: 100vh;
}
</style>