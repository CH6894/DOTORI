<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

const open = ref(false)
const toggle = () => (open.value = !open.value)
const close  = () => (open.value = false)

// ESC 로 닫기
const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') close() }
onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))

const suggestions = ['도토리 주기','도토리 주기','도토리 주기','도토리 주기','도토리 주기','도토리 주기']
</script>

<template>
  <div class="chatbot">
    <!-- FAB 아이콘: 열리면 사라짐 -->
    <transition name="fade">
      <div
        v-if="!open"
        class="fab-wrap"
        aria-label="챗봇 열기"
        role="button"
        tabindex="0"
        @click="toggle"
        @keydown.enter.prevent="toggle"
        @keydown.space.prevent="toggle"
      >
        <!-- /public/img/ChatBot.svg 기준 -->
        <img class="fab-icon" src="/img/chatbot/ChatBot.svg" alt="챗봇 열기" />
      </div>
    </transition>

    <!-- 백드롭: 열릴 때만 표시, 클릭 시 닫힘 -->
    <transition name="fade">
      <div v-if="open" class="backdrop" @click="close" aria-hidden="true"></div>
    </transition>

    <!-- 패널: 래퍼에 scale(.6) 적용 -->
    <transition name="panel">
      <div v-if="open" class="panel-wrap">
        <section class="squirrel-panel" role="dialog" aria-label="다람쥐 챗봇">
          <header class="head">
            <button class="btn-close" @click="close" aria-label="닫기">X</button>

            <i class="eye eye--left"></i>
            <i class="eye eye--right"></i>
            <i class="nose"></i>
          </header>

          <main class="body">
            <div class="bubble">
              <p class="greet"><strong>안녕하세요! 어쩌고 저쩌고 도와드립니다!</strong></p>
              <div class="chips">
                <button v-for="(t,i) in suggestions" :key="i" class="chip">{{ t }}</button>
              </div>
            </div>
          </main>

          <footer class="foot">
            <button class="btn-attach" title="첨부" aria-label="첨부">+</button>
            <input class="input" type="text" placeholder="메시지 입력" />
            <button class="btn-send" type="button">전송</button>
          </footer>
        </section>
      </div>
    </transition>
  </div>
</template>

<style scoped>
/* ===== 토큰 ===== */
.chatbot{
  --fab-size: 128px;     /* 아이콘 크기 */
  --fab-right: 24px;
  --fab-bottom: 24px;

  --panel-w: 420px;      /* 원본 사이즈 유지 */
  --panel-h: 78vh;
  --panel-gap: 16px;
  --panel-scale: .8;     /* ✅ 60%로 축소(래퍼에 적용) */

  --brand: #c6742e;
  --brand-dark: #b36222;
  --cream: #f4f2e5;
  --accent: #ff7a3a;
  --shadow: 0 16px 40px rgba(0,0,0,.18);
  --fab-right: calc(24px + 60px);   /* = 40px → 왼쪽으로 이동 */
  --fab-bottom: calc(24px + 32px);  /* = 56px → 위로 이동 */
  --panel-shift-x: 48px;
  --panel-shift-y: 72px;
}

/* ===== 트랜지션 ===== */
.fade-enter-active, .fade-leave-active{ transition: opacity .18s ease }
.fade-enter-from, .fade-leave-to{ opacity: 0 }

/* panel 트랜지션은 래퍼(.panel-wrap)에 적용됨 */
.panel-enter-active{ animation: pop-in .18s ease-out both }
.panel-leave-active{ animation: pop-out .14s ease-in both }
@keyframes pop-in {
  from { transform: translateY(8px) scale(var(--panel-scale)); opacity: 0; }
  to   { transform: translateY(0)   scale(var(--panel-scale)); opacity: 1; }
}
@keyframes pop-out{
  from { transform: translateY(0)   scale(var(--panel-scale)); opacity: 1; }
  to   { transform: translateY(8px) scale(var(--panel-scale)); opacity: 0; }
}

/* ===== FAB ===== */
.fab-wrap{
  position: fixed;
  right: var(--fab-right);
  bottom: var(--fab-bottom);
  width: var(--fab-size);
  height: var(--fab-size);
  z-index: 2147483647;
  display: grid; place-items: center;
  cursor: pointer;
}
.fab-icon{
  width: 100%; height: 100%; display: block;
  transition: transform .2s ease, filter .2s ease;
}
.fab-wrap:hover .fab-icon{ transform: translateY(-2px); filter: drop-shadow(0 10px 22px rgba(0,0,0,.2)); }
.fab-wrap:focus-visible{ outline: 3px solid #7aa7ff; outline-offset: 3px; }

/* ===== 백드롭 ===== */
.backdrop{
  position: fixed; inset: 0; background: rgba(0,0,0,.25);
  z-index: 9998;
}

/* ===== 패널 래퍼(여기에 scale 적용) ===== */
.panel-wrap{
  position: fixed;
  /* 기존: right: var(--fab-right); 또는 calc(...) 였다면 교체 */
  right: clamp(0px, calc(var(--fab-right) - var(--panel-shift-x)), 100vw);
  bottom: max(0px,
    calc(var(--fab-bottom) + var(--fab-size) + var(--panel-gap) - var(--panel-shift-y))
  );  transform: scale(var(--panel-scale));
  transform-origin: bottom right;
  z-index: 9999;
  will-change: transform;
}
/* ===== 패널 본체(원본 크기 유지) ===== */
.squirrel-panel{
  width: var(--panel-w);
  height: var(--panel-h);
  display: grid;
  grid-template-rows: 140px 1fr auto;
  border-radius: 20px;
  overflow: hidden;              /* 모서리 라운드 살림 */
  box-shadow: var(--shadow);
  background: var(--brand);
  box-sizing: border-box;
}

/* ===== 머리/귀/얼굴 ===== */
.head{ position: relative; background: var(--brand); }
.btn-close{
  position: absolute; right: 16px; top: 16px;
  background: #e5e5e5; border:0; border-radius: 8px;
  padding: 8px 10px; font-size: 14px; cursor: pointer; color:#333;
}
.eye{
  position: absolute; top: 56px; width: 16px; height: 36px;
  background: #2c231c; border-radius: 10px;
}
.eye--left{ left: calc(50% - 60px); }
.eye--right{ right: calc(50% - 60px); }
.nose{
  position: absolute; top: 92px; left: 50%; transform: translateX(-50%);
  width: 36px; height: 16px; background: #f2a4a4;
  border-radius: 18px; box-shadow: 0 1px 0 rgba(0,0,0,.12) inset;
}

/* ===== 본문 ===== */
.body{ background: var(--cream); padding: 16px 14px 0; overflow: auto; }
.bubble{
  position: relative; max-width: calc(100% - 28px);
  background: #fff; border: 1px solid #d8d3c7; border-radius: 12px;
  padding: 14px 16px; color:#222; box-shadow: 0 2px 4px rgba(0,0,0,.05);
}
.bubble::after{
  content:""; position:absolute; left: 20px; bottom: -14px;
  border-width: 14px 12px 0 0; border-style: solid; display:block;
  border-color: #fff transparent transparent transparent;
  filter: drop-shadow(-1px 1px 0 #d8d3c7);
}
.greet{ margin: 0 0 10px; }

/* 칩 */
.chips{ display:flex; flex-wrap:wrap; gap:10px; }
.chip{
  border:0; padding:8px 12px; border-radius:999px;
  background:#9b9b9b; color:#fff; font-weight:700; cursor:pointer;
}

/* ===== 입력 ===== */
.foot{
  background: var(--brand);
  display: grid;
  grid-template-columns: 44px 1fr auto;   /* ⬅ 버튼은 auto, 인풋은 1fr로 길게 */
  gap: 10px;
  align-items: center;
  padding: 10px 10px 12px;
  border-top: 6px solid var(--brand-dark);
}
.btn-attach{
  width: 40px; height: 40px;              /* ⬅ 작게 통일 */
  border-radius: 50%;
  border: 0; background: #fff; color: var(--accent);
  font-size: 22px; font-weight: 900; line-height: 1;
  cursor: pointer; display: grid; place-items: center;
  box-shadow: 0 6px 16px rgba(0,0,0,.15);
}
.input{
  height: 40px;                            /* ⬅ 인풋 길고 낮게 */
  border-radius: 10px;
  border: 0; outline: none; padding: 0 12px;
  background: #f4f2e5; color: #333; font-size: 14px;
  min-width: 0;                             /* ⬅ overflow 방지 (중요) */
}
.btn-send{
  height: 40px;                             /* ⬅ 버튼 낮게 */
  border-radius: 10px;
  border: 0; background: var(--accent); color:#fff;
  font-weight: 800; font-size: 14px;
  padding: 0 12px;                          /* ⬅ 내용만큼 너비 */
  cursor: pointer; white-space: nowrap;     /* ⬅ “전송” 두 줄 방지 */
  box-shadow: 0 6px 16px rgba(0,0,0,.15);
}
.body{ background: var(--cream); padding: 16px 14px 0; overflow: auto; min-width: 0; }
/* 스크롤바(선택) */
.body::-webkit-scrollbar{ width:10px }
.body::-webkit-scrollbar-thumb{ background:#d0c8b2; border-radius:10px }
</style>
