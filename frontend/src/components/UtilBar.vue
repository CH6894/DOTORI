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