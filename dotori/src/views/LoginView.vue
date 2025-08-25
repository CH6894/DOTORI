<template>
  <div class="page">
    <main class="auth">
      <section class="auth-card" role="region" aria-labelledby="authTitle">
        <h1 id="authTitle" class="auth-title">간편 로그인</h1>

        <div class="btn-col">
          <button class="btn btn-naver" @click="loginWithNaver" aria-label="네이버로 계속하기">
            <span class="btn__icon" aria-hidden="true">
              <svg viewBox="0 0 24 24"><path fill="#fff" d="M6 4h5.3l6.7 9.1V4H24v16h-5.3L12 10.9V20H6z"/></svg>
            </span>
            <span class="btn__text">네이버로 계속하기</span>
          </button>

          <button class="btn btn-kakao" @click="handleSocialLogin('kakao')" aria-label="카카오로 계속하기">
            <span class="btn__icon" aria-hidden="true">
              <svg viewBox="0 0 24 24">
                <path fill="#391B1B" d="M12 3C6.5 3 2 6.5 2 10.7c0 2.5 1.7 4.7 4.3 6.1L5.5 21l4-2.1c.8.1 1.6.2 2.5.2 5.5 0 10-3.5 10-7.8S17.5 3 12 3z"/>
              </svg>
            </span>
            <span class="btn__text">카카오로 계속하기</span>
          </button>

          <button class="btn btn-google" @click="handleSocialLogin('google')" aria-label="구글로 계속하기">
            <span class="btn__icon" aria-hidden="true">
              <svg viewBox="0 0 24 24">
                <path fill="#EA4335" d="M12 10.2v3.7h5.2c-.2 1.3-1.6 3.7-5.2 3.7A6 6 0 1 1 12 6c1.6 0 2.6.6 3.2 1.2l2.2-2.2C16.2 3.8 14.3 3 12 3a9 9 0 1 0 0 18c5.2 0 8.7-3.7 8.7-8.9 0-.6-.1-1.2-.2-1.7z"/>
                <path fill="#4285F4" d="M22.7 12.1c0-.6-.1-1.2-.2-1.7H12v3.7h5.2c-.2 1.3-1.6 3.7-5.2 3.7v3.6c5.2 0 8.7-3.7 8.7-8.9z"/>
                <path fill="#FBBC05" d="M6.5 14.2a5.5 5.5 0 0 1 0-4.4V6.2H2.7a9 9 0 0 0 0 11.6z"/>
                <path fill="#34A853" d="M12 21c2.6 0 4.8-.9 6.4-2.5l-3.1-2.4c-.9.6-2 1-3.3 1a6 6 0 0 1-5.6-3.9H2.7v3.1A9 9 0 0 0 12 21z"/>
              </svg>
            </span>
            <span class="btn__text">구글로 계속하기</span>
          </button>
        </div>


      </section>
    </main>
  </div>
</template>

<script setup>
import { OAUTH } from '@/config'

function generateRandomString(length) {
  let result = '';
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  const charactersLength = characters.length;
  for (let i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() * charactersLength));
  }
  return result;
}

function loginWithNaver() {
  const state = generateRandomString(10);
  const naverLoginUrl = OAUTH.naver.replace('YOUR_STATE', state);
  window.location.href = naverLoginUrl;
}

function handleSocialLogin(provider) {
  alert(`${provider}는 백엔드 연결 후 활성화됩니다.`)
}
</script>

<style scoped>
/* 페이지 래퍼 */
.page{
  margin-top:-100px;
  min-height: 75vh;
  display: flex; flex-direction: column;
}

/* 가운데 정렬 */
.auth{
  flex: 1;
  display: flex; align-items: center; justify-content: center;
  padding: 60px 16px;
  position: relative;
}

/* 카드 */
.auth-card{
  width: 100%;
  max-width: 440px;
  background: #fff;
  border: 1px solid #EAEAEA;
  border-radius: 24px;
  box-shadow: 2px 1px 10px rgba(0,0,0,0.12);
  padding: 20px 40px 20px;
  text-align: center;
}

.auth-title{
  font-size: 20px;
  font-weight: 800;
  color: #333;
  margin: 6px 0 24px;
}

/* 버튼 컬럼 */
.btn-col{ display: grid; gap: 14px; margin-bottom: 18px; }

/* 공통 버튼 */
.btn{
  display: grid; grid-template-columns: 40px 1fr;
  align-items: center; gap: 8px;
  width: 100%; height: 56px;
  border-radius: 14px; border: 1px solid #C4C4C4;
  font-size: 15px; font-weight: 800;
  cursor: pointer; background: #fff;
}
.btn__icon{
  display: grid; place-items: center;
  width: 40px; height: 40px; margin-left: 135px;
}
.btn__icon svg{ width: 26px; height: 26px; display: block; }
.btn__text{ justify-self: center; }

/* 개별 브랜드 색 */
.btn-naver{ background:#03C75A; color:#fff; border-color:#03C75A; }
.btn-kakao{ background:#FEE500; color:#392020; border-color:#F0D800; }
.btn-google{ background:#fff; color:#333; }

/* 호버/포커스 */
.btn:hover{ filter: brightness(.98); }
.btn:focus{ outline: 3px solid rgba(46,170,220,.35); outline-offset: 2px; }



/* 반응형 */
@media (max-width: 480px){
  .auth-card{ padding: 24px 16px; border-radius: 18px; }
  .btn{ height: 52px; font-size: 16px; }
}
</style>