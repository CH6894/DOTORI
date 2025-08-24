<template>
  <footer class="footer">
  <div class="footer-wave" aria-hidden="true"></div>
  <div class="container footer__inner">
    <div class="footer__brand">
      <a class="logo__mark -small" href="/Main.html" aria-label="DOTORI 홈으로">
        <img src="/img/Logo_NonBG.svg" alt="DOTORI 로고">
      </a>
    </div>

    <div class="footer__info">
      <!-- <span class="footer__notice">본 사이트는 현재 개발 단계의 테스트 페이지입니다.</span> -->
      <span class="footer__sep" aria-hidden="true">·</span>
      <span class="footer__copy">© DOTORI. All rights reserved.</span>
    </div>
    <!-- 필요시 노출하는 링크들 -->
    <nav class="footer__nav">
      <a href="/about.html">회사소개</a>
      <a href="/notices.html">공지사항</a>
      <a href="/terms.html">이용약관</a>
      <a href="/privacy.html">개인정보처리방침</a>
      <a href="/support.html">고객센터</a>
    </nav>
  </div>
</footer>
</template>


<style scoped>
.footer { padding: 32px 0; background: #F4F3E6; color:#2d251c; }
.footer__inner { display:flex; gap: 12px; justify-content: space-between; align-items: center; }
.footer a { text-decoration: none; }
</style>