<!-- src/components/AppFooter.vue -->
<template>
  <footer class="footer">
  <div class="footer-wave" aria-hidden="true"></div>
  <div class="container footer__inner">
    <div class="footer__brand">
      <a class="logo__mark -small" href="/Main.html" aria-label="DOTORI 홈으로">
        <img src="../assets/Logo_NonBG.svg" alt="DOTORI 로고">
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


<script>
    export default{
        name:'FooterComponent'
    }
</script>   

<style>

/* ---------- Footer ---------- */

.footer {
  position: relative;            /* 파형 절대배치 기준 */
  background:#F4F3E6;
  border-top:1px solid #e5dcc9;
  padding: 20px 0;
  color:#555; font-size:14px;
  overflow: visible;
}
.footer-wave{
  position:absolute; top:-150px; left:0; right:0;
  width:100%; height:150px;
  background-image:url('@/assets/Down_BG.svg');
  background-repeat:no-repeat;
  background-position:left top;   /* center → left top */
  background-size:100% auto;      /* cover → 가로 100% */
  pointer-events:none;         /* 클릭 방해 X */
  
}
.footer__inner.center {
  text-align: center; /* 중앙 정렬 */
}

.footer__notice {
  display: block;
  font-size: 12px;
  color: #888;
  margin-top: 5px;
}

.footer__inner{
  display:flex; align-items:center; gap:20px; justify-content:center; padding:20px 0; flex-wrap:wrap;
}
.footer__nav { display:flex; justify-content:center; gap:14px 15px; padding-bottom: 30px;}
.footer__nav a { color:#6e6253; font-weight:600; }
.footer__copy { margin:8px 0 0; font-size:12px; color:#9a8d7c; line-height: 1.6; padding-bottom: 40px;}
.footer .logo img {
  width: 160px;   /* 원하면 100~160px 사이로 조절 */
  height: auto;
}

.logo__mark.-small{ width:120px; display:inline-block; padding-bottom: 30px;}
.logo__mark.-small img{ width:100%; height:auto; }

.footer__info{
  display:flex; align-items:center; gap:10px; color:#7b6d5d; font-size:12px;
  line-height:1.4; flex: 0 0 auto; min-width:unset;
}
.footer__notice{ opacity:.95; }
.footer__copy{ color:#9a8d7c; }
.footer__sep{ opacity:.4; }

/* 화면이 좁아지면 세로로 자동 전환 */
@media (max-width: 768px){
  .footer__info{
    flex-direction: column; align-items:flex-start; gap:4px;
  }
  .footer__sep{ display:none; }
}


</style>