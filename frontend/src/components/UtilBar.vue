<template>
  <div class="utilbar">
    <div class="container utilbar__inner">
      <div class="utilbar__spacer"></div>
      <nav class="utilbar__links">
        <RouterLink :to="{ name: 'login' }">로그인</RouterLink>
        <span aria-hidden="true">/</span>
        <RouterLink to="#">고객센터</RouterLink>
      </nav>
    </div>
  </div>
</template>
<style scoped>
/* ---------- Util bar ---------- */
.utilbar {
  background: #f5efdf;
  border-bottom: 1px solid #ece4d1;
  font-size: 12px;
  color: #847768;
}
.utilbar__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 36px;
}
.utilbar__links a {
  padding: 0 8px;
}
.utilbar__links span {
  color: #c4b9a9;
}

</style>