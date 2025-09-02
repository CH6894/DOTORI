<!-- src/components/AppFooter.vue -->
<template>
  <footer class="footer">
    <div class="footer-wave" aria-hidden="true"></div>
    <div class="container footer__inner">
      <div class="footer__brand">
        <RouterLink class="logo__mark -small" :to="{ name: 'main' }" aria-label="DOTORI 홈으로">
          <img src="../assets/Logo_NonBG.svg" alt="DOTORI 로고">
        </RouterLink>
      </div>

      <div class="footer__info">
        <!-- <span class="footer__notice">본 사이트는 현재 개발 단계의 테스트 페이지입니다.</span> -->
        <span class="footer__sep" aria-hidden="true">·</span>
        <span class="footer__copy">© DOTORI. All rights reserved.</span>
      </div>
      <!-- 필요시 노출하는 링크들 -->
      <nav class="footer__nav">
        <RouterLink to="/about">회사소개</RouterLink>
        <RouterLink to="/notices">공지사항</RouterLink>
        <RouterLink to="/terms">이용약관</RouterLink>
        <RouterLink to="/privacy">개인정보처리방침</RouterLink>
        <RouterLink to="/support">고객센터</RouterLink>
      </nav>
    </div>
  </footer>

</template>


<script>
export default {
  name: 'FooterComponent'
}
</script>

<style>
.footer {
  position: relative;
  margin-top: 3.75rem;            /* 60px */
  background: #f4f3e6;
  border-top: 1px solid #e5dcc9;
  padding: 1.25rem 0;             /* 20px */
  font-size: 0.875rem;            /* 14px */
  color: #555;
  overflow: visible;
}

.footer-wave {
  position: absolute;
  top: -9.375rem;                 /* -150px */
  left: 0;
  right: 0;
  width: 100%;
  height: 9.375rem;               /* 150px */
  background-image: url('@/assets/Down_BG.svg');
  background-repeat: no-repeat;
  background-position: left top;
  background-size: 100% auto;
  pointer-events: none;
}

.footer__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.25rem;                   /* 20px */
  padding: 1.5rem 0;              /* 24px */
  flex-wrap: wrap;
}

.footer__inner.center {
  text-align: center;
}

.footer__nav {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.875rem 1.25rem;          /* 14px 20px */
}

.footer__nav a {
  color: #6e6253;
  font-weight: 600;
  text-decoration: none;
}

.footer__nav a:visited {
  text-decoration: none;
  color: inherit;
}

.footer .logo img {
  width: 10rem;                   /* 160px */
  height: auto;
}

.logo__mark.-small {
  width: 7.5rem;                  /* 120px */
  display: inline-block;
}

.logo__mark.-small img {
  width: 100%;
  height: auto;
}

.footer__info {
  display: flex;
  align-items: center;
  gap: 0.625rem;                  /* 10px */
  color: #7b6d5d;
  font-size: 0.75rem;             /* 12px */
  line-height: 1.4;
  flex: 1 1 auto;
  min-width: 15rem;               /* 240px */
}

.footer__notice {
  display: block;
  font-size: 0.75rem;             /* 12px */
  color: #888;
  margin-top: 0.313rem;           /* 5px */
  opacity: 0.95;
}

.footer__copy {
  margin: 0.5rem 0 0;             /* 8px */
  font-size: 0.75rem;             /* 12px */
  color: #9a8d7c;
  line-height: 1.6;
}

.footer__sep {
  opacity: 0.4;
}

@media (max-width: 48rem) {       /* 768px */
  .footer__info {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;                 /* 4px */
  }

  .footer__sep {
    display: none;
  }
}
</style>
