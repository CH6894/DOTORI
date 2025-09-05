<!-- src/components/AppHeader.vue -->
<template>
  <link rel="stylesheet" href="../assets/global.css">
<div class="utilbar">
    <div class="container utilbar__inner">
      <div class="utilbar__spacer"></div>
      <nav class="utilbar__links">
        <a v-if="!isAuthed" href="javascript:;" @click="loginNaver">로그인</a>
        <a v-else href="javascript:;" @click="logout">로그아웃</a>
        <span aria-hidden="true">/</span>
        <a href="#">고객센터</a>
      </nav>
   </div>
</div>
  <!-- 헤더 -->
  <div class="logo-box">
      <img src="../assets/dotori_logo.svg" alt="DOTORI 로고" class="logo-img" />
  </div>
  <header class="header">
  <div class="header__bottom">
    <div class="container">
    <!-- 햄버거 -->
      <button class="icon-btn" type="button" aria-label="메뉴 열기">
      <svg width="22" height="18" viewBox="0 0 22 18" aria-hidden="true">
        <rect width="22" height="2" y="0" rx="1" />
        <rect width="22" height="2" y="8" rx="1" />
        <rect width="22" height="2" y="16" rx="1" />
      </svg>
    </button>

    <!-- 네비게이션 -->
    <nav class="header__nav" aria-label="주요 메뉴">
      <a class="nav-link" href="#">도감</a>
      <a class="nav-link" href="#">캘린더</a>
      <a class="nav-link" href="#">검수 기준</a>
    </nav>

    <!-- 검색 -->
    <SearchComponent />

    <!-- 아이콘 -->
        <div class="header__icons">
          <RouterLink class="icon-btn" :to="{ name: 'mypage-wish' }" aria-label="관심 상품">
            <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M12 21s-8-5.33-8-11a5 5 0 0 1 9-3 5 5 0 0 1 9 3c0 5.67-8 11-8 11z" fill="currentColor"
                stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </RouterLink>
          <RouterLink class="icon-btn" :to="{ name: 'mypage' }" aria-label="마이페이지">
            <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="12" cy="8" r="4" fill="currentColor" stroke="currentColor" stroke-width="1.6" />
              <path d="M4 20a8 8 0 0 1 16 0" fill="currentColor" stroke="currentColor" stroke-width="1.6"
                stroke-linecap="round" />
            </svg>
          </RouterLink>
          <RouterLink class="icon-btn" :to="{ name: 'cart' }" aria-label="장바구니">
            <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M6 6h15l-1.5 9h-12z" fill="currentColor" stroke="currentColor" stroke-width="1.6"
                stroke-linejoin="round" />
              <path d="M6 6L5 3H2" fill="currentColor" stroke="currentColor" stroke-width="1.6"
                stroke-linecap="round" />
              <circle cx="9" cy="21" r="1.5" fill="currentColor" />
              <circle cx="18" cy="21" r="1.5" fill="currentColor" />
            </svg>
          </RouterLink>
        </div>
  </div>
  </div>
  
</header>

</template>


<script setup>
import { ref, onMounted } from 'vue'
import SearchComponent from './Search.vue'

const isAuthed = ref(false)
const API_BASE = 'http://192.168.198.1:8085'

// 현재 로그인 상태 반영
function syncAuth() {
  isAuthed.value = !!localStorage.getItem('accessToken')
}

async function logout() {
  try {
    // 세션/쿠키 로그아웃(있다면)
    await fetch(`${API_BASE}/logout`, {
      method: 'POST',
      credentials: 'include', // JSESSIONID 보내기
    })
  } catch (_) {
    // JWT만 쓰면 실패 무시해도 됨
  } finally {
    // 클라이언트 토큰 제거
    localStorage.removeItem('accessToken')
    syncAuth()
    // 원하는 페이지로
    window.location.href = '/login'
  }
}

function loginNaver() {
  window.location.assign(`${API_BASE}/oauth2/authorization/naver`)
}

onMounted(() => {
  // 기존 syncAuth
  syncAuth()

  // ✅ URL에 토큰이 있으면 localStorage에 저장
  const params = new URLSearchParams(window.location.search)
  const token = params.get("token")
  if (token) {
    localStorage.setItem("accessToken", token)
    syncAuth()

    // 토큰 저장 후 URL 깔끔하게 정리
    window.history.replaceState({}, document.title, window.location.pathname)
  }

  // 다른 탭과 동기화
  window.addEventListener('storage', (e) => {
    if (e.key === 'accessToken') syncAuth()
  })
})
</script>   

<style>
/* ---------- Util bar ---------- */
.utilbar {
  background: #fff;
  border-bottom: 1px solid #ece4d1;
  font-size: 12px;
  color: #847768;
  display: flex;
  justify-content: center;
  width: 100%;
}
.utilbar__inner {
  display:flex;
  align-items:center;
  justify-content:flex-end;
  min-height: 36px;
  width: 1280px;
}
.utilbar__links a { 
    padding: 0 8px;
    color:#000;
    text-decoration:none;
}
.utilbar__links span {
  color:#c4b9a9;
}

.logo-box {
  display: flex;
  justify-content: center;
  margin-bottom: 15px; /* 로고와 메뉴 간격 */
}

.logo-img {
  margin-top:10px;
  height: 85px; /* 원하는 크기로 조절 */
  width: auto;
}

.header { 
  position: sticky; 
  top: 0; 
  z-index: 50;
  width: 100%;  /* 화면 전체 폭 기준 */
  margin: 0; padding: 0;
  backdrop-filter: blur(6px);
  border-bottom: 1px solid #ede6d8;
  background: #fff;
}

.icon-btn {
  display:inline-grid; 
  place-items:center;
  width:36px; height:36px; 
  border-radius: 12px;
  border:1px solid #e7dfcd; 
  background:#fff7ea;
  box-shadow: 0 2px 0 rgba(0,0,0,.02) inset;
  cursor:pointer;
}

.icon-btn {
  color: var(--ink);
}

.icon-btn svg {
  width: 22px; 
  height: 22px; 
  display: block; 
}

.header__icons {
  fill:#505050;
}


.nav-link {
  display: inline-flex;
  font-weight: 700;
  padding: 6px 8px;
  font-size: 17px;
  color:#000;
  text-decoration:none;
}
.nav-link:hover { 
  background: #fff0df;
  text-decoration:none;
  color:#000;
}

.logo { display:flex; 
  align-items:center; 
  gap:8px; 
  margin-left: 8px; 
}
.logo__mark {
  display:inline-block; 
  width: 200px; 
}
.logo__mark img {
  width:100%;
  height:auto; 
}
.logo.-small .logo__mark {
  font-size: 22px; 
}
.logo.-small .logo__text { 
  font-weight:800; 
  font-size:14px; 
}




.header__inner {
  max-width: 1500px;            /* 컨텐츠 폭 제한 */
  margin: 0 auto;               /* 가운데 정렬 */
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px 0;
}
.header__logo {
  display:flex;
  justify-content:center; 
}

.header__bottom {
  width: 100%;   
  height: 50px;              /* 부모 100% 대신 화면 전체 */
  display: flex;
  justify-content: flex-start;
  gap: 0px;
  background: #F4F3E6;             /* 좌우 살짝 여백만 */
  box-sizing: border-box;
}
/* 추가 1 */
.header__bottom > * {
  width: 1280px;
  margin: 0 auto;
}

.header__bottom > .container{
  display: flex;
  align-items: center;
}
.header__nav {
  display: flex;
  gap: 80px;
  margin-left: 80px;
}

.header__icons {
  display:flex;
  gap:10px;
  margin-left: auto;
}

.search {
  display:grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  align-items:center;
  margin-left: 200px;
}
.search__btn svg * {
  fill: none !important;
  stroke: currentColor !important;
  stroke-width: 2 !important;
}
.search input {
  width:400px;
  height: 40px;
  padding: 0 14px;
  border-radius: 999px; 
  border:1px solid #eadfc9;
}
.search__btn {
  height:40px;
  width:40px; 
  border-radius:999px;
  border:1px solid #eadfc9;
  background:#fff;
  cursor:pointer;
}
.search__btn svg {
  stroke: var(--ink); 
}

</style>