<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { chatAPI } from '@/services/api'

const open = ref(false)
const toggle = () => (open.value = !open.value)
const close = () => (open.value = false)

// ESC로 닫기
const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') close() }
onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))

// === 채팅 상태 관리 ===
interface Message {
  id: string
  text: string
  isUser: boolean
  timestamp: Date
}

const messages = ref<Message[]>([])
const inputText = ref('')
const isLoading = ref(false)
const userId = ref('default')
const chatBody = ref<HTMLElement>()

// 제안 버튼들
const suggestions = [
  '뉴진스 콜라보 굿즈 있어?',
  '원피스 조로 피규어',
  '블랙핑크 응원봉',
  '검수 등급이 뭐야?',
  'S급이랑 A급 차이는?',
  'c등급 쓰는데 문제 없어?'
]

// === 메시지 관리 함수들 ===
const addMessage = (text: string, isUser: boolean) => {
  const message: Message = {
    id: Date.now().toString() + Math.random(),
    text,
    isUser,
    timestamp: new Date()
  }
  messages.value.push(message)
  
  // 스크롤을 맨 아래로
  nextTick(() => {
    if (chatBody.value) {
      chatBody.value.scrollTop = chatBody.value.scrollHeight
    }
  })
}

// === API 호출 함수 ===
const sendMessageToAPI = async (userMessage: string) => {
  try {
    return await chatAPI.sendMessage(userMessage, userId.value)
  } catch (error) {
    console.error('API 호출 실패:', error)
    return {
      answer: '죄송해요, 일시적인 오류가 발생했어요. 잠시 후 다시 시도해주세요!',
      is_multiple: false,
      messages: []
    }
  }
}

// === 메시지 전송 ===
const sendMessage = async (text?: string) => {
  const messageText = text || inputText.value.trim()
  if (!messageText || isLoading.value) return

  // 사용자 메시지 추가
  addMessage(messageText, true)
  inputText.value = ''
  isLoading.value = true

  try {
    // API 호출
    const response = await sendMessageToAPI(messageText)
    
    // 첫 번째 메시지 추가
    addMessage(response.answer, false)
    
    // 추가 메시지들이 있으면 순차적으로 표시
    if (response.is_multiple && response.messages && Array.isArray(response.messages)) {
      response.messages.forEach((message: string, index: number) => {
        setTimeout(() => {
          addMessage(message, false)
        }, (index + 1) * 1000) // 1초 간격으로 조정
      })
    }
  } catch (error) {
    console.error('메시지 전송 오류:', error)
    addMessage('죄송해요, 오류가 발생했어요.', false)
  } finally {
    isLoading.value = false
  }
}

// === 제안 버튼 클릭 ===
const handleSuggestionClick = (suggestion: string) => {
  sendMessage(suggestion)
}

// === 엔터키로 전송 ===
const handleKeyPress = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

// 텍스트에서 마크다운 링크를 HTML로 변환
const formatMessage = (text: string) => {
  // 마크다운 링크를 HTML로 변환
  let formatted = text.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer" class="product-link">$1</a>')
  
  // 마크다운 볼드 텍스트를 HTML로 변환
  formatted = formatted.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  
  // 줄바꿈을 <br>로 변환
  formatted = formatted.replace(/\n/g, '<br>')
  
  return formatted
}
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
        <img class="fab-icon" src="/img/chatbot/ChatBot.svg" alt="챗봇 열기" />
      </div>
    </transition>

    <!-- 백드롭: 열릴 때만 표시, 클릭 시 닫힘 -->
    <transition name="fade">
      <div v-if="open" class="backdrop" @click="close" aria-hidden="true"></div>
    </transition>

    <!-- 패널 -->
    <transition name="panel">
      <div v-if="open" class="panel-wrap">
        <section class="squirrel-panel" role="dialog" aria-label="다람쥐 챗봇">
          <header class="head">
            <button class="btn-close" @click="close" aria-label="닫기">×</button>
            <i class="eye eye--left"></i>
            <i class="eye eye--right"></i>
            <i class="nose"></i>
          </header>

          <main class="body" ref="chatBody">
            <!-- 초기 인사말 (메시지가 없을 때만 표시) -->
            <div v-if="messages.length === 0" class="welcome">
              <div class="bubble welcome-bubble">
                <p class="greet">
                  <strong>안녕하세요! 저는 쇼핑 도우미 다람이에요</strong>
                </p>
                <p>무엇을 도와드릴까요?</p>
                <div class="chips">
                  <button 
                    v-for="(suggestion, i) in suggestions" 
                    :key="i" 
                    class="chip"
                    @click="handleSuggestionClick(suggestion)"
                  >
                    {{ suggestion }}
                  </button>
                </div>
              </div>
            </div>

            <!-- 채팅 메시지들 -->
            <div class="chat-messages">
              <div
                v-for="message in messages"
                :key="message.id"
                :class="['message', { 
                  'user-message': message.isUser, 
                  'ai-message': !message.isUser 
                }]"
              >
                <div class="message-bubble" :class="{ 'user-bubble': message.isUser }">
                  <div v-html="formatMessage(message.text)"></div>
                  <small class="timestamp">
                    {{ message.timestamp.toLocaleTimeString('ko-KR', { 
                      hour: '2-digit', 
                      minute: '2-digit' 
                    }) }}
                  </small>
                </div>
              </div>

              <!-- 로딩 표시 -->
              <div v-if="isLoading" class="message ai-message">
                <div class="message-bubble loading">
                  <div class="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                </div>
              </div>
            </div>
          </main>

          <footer class="foot">
            <button class="btn-attach" title="첨부" aria-label="첨부">+</button>
            <input 
              class="input" 
              type="text" 
              placeholder="메시지 입력" 
              v-model="inputText"
              @keypress="handleKeyPress"
              :disabled="isLoading"
            />
            <button
              class="btn-send"
              type="button"
              @click="sendMessage"
              :disabled="isLoading || !inputText.trim()"
            >
              {{ isLoading ? '전송중...' : '전송' }}
            </button>
          </footer>
        </section>
      </div>
    </transition>
  </div>
</template>

<style scoped>
/* 기존 스타일은 그대로 유지하되, 단위만 반응형으로 정리 */
.chatbot{
  --fab-size: 8rem;                                   /* 128px */
  --fab-right: calc(1.5rem + 3.75rem);                /* 24px + 60px */
  --fab-bottom: calc(1.5rem + 2rem);                  /* 24px + 32px */
  --panel-w: min(90vw, 26.25rem);                     /* 420px 상한 */
  --panel-h: 78vh;
  --panel-gap: 1rem;                                  /* 16px */
  --panel-scale: .8;
  --brand: #c6742e;
  --brand-dark: #b36222;
  --cream: #f4f2e5;
  --accent: #ff7a3a;
  --shadow: 0 1rem 2.5rem rgba(0,0,0,.18);            /* 0 16px 40px */
  --panel-shift-x: 3rem;                               /* 48px */
  --panel-shift-y: 4.5rem;                             /* 72px */
}

/* 트랜지션 */
.fade-enter-active, .fade-leave-active{ transition: opacity .18s ease }
.fade-enter-from, .fade-leave-to{ opacity: 0 }
.panel-enter-active{ animation: pop-in .18s ease-out both }
.panel-leave-active{ animation: pop-out .14s ease-in both }

@keyframes pop-in {
  from { transform: translateY(0.5rem) scale(var(--panel-scale)); opacity: 0; }   /* 8px */
  to   { transform: translateY(0)       scale(var(--panel-scale)); opacity: 1; }
}
@keyframes pop-out{
  from { transform: translateY(0)       scale(var(--panel-scale)); opacity: 1; }
  to   { transform: translateY(0.5rem)  scale(var(--panel-scale)); opacity: 0; }  /* 8px */
}

/* FAB */
.fab-wrap{
  position: fixed; right: var(--fab-right); bottom: var(--fab-bottom);
  width: var(--fab-size); height: var(--fab-size); z-index: 2147483647;
  display: grid; place-items: center; cursor: pointer;
}
.fab-icon{
  width: 100%; height: 100%; display: block;
  transition: transform .2s ease, filter .2s ease;
}
.fab-wrap:hover .fab-icon{ 
  transform: translateY(-0.125rem);                    /* -2px */
  filter: drop-shadow(0 0.625rem 1.375rem rgba(0,0,0,.2)); /* 0 10px 22px */
}

/* 백드롭 */
.backdrop{
  position: fixed; inset: 0; background: rgba(0,0,0,.25); z-index: 9998;
}

/* 패널 */
.panel-wrap{
  position: fixed;
  right: clamp(0rem, calc(var(--fab-right) - var(--panel-shift-x)), 100vw);
  bottom: max(0rem, calc(var(--fab-bottom) + var(--fab-size) + var(--panel-gap) - var(--panel-shift-y)));
  transform: scale(var(--panel-scale));
  transform-origin: bottom right; z-index: 9999;
}

.squirrel-panel{
  width: var(--panel-w); height: var(--panel-h);
  display: grid; grid-template-rows: 8.75rem 1fr auto;  /* 140px */
  border-radius: 1.25rem;                               /* 20px */
  overflow: hidden;
  box-shadow: var(--shadow); background: var(--brand);
}

/* 머리 */
.head{ position: relative; background: var(--brand); }
.btn-close{
  position: absolute; right: 1rem; top: 1rem;           /* 16px */
  background: #e5e5e5; border: 0; border-radius: 0.5rem;/* 8px */
  padding: 0.5rem 0.625rem;                             /* 8px 10px */
  font-size: 1rem;                                      /* 16px */
  cursor: pointer; color: #333;
}
.eye{
  position: absolute; top: 3.5rem;                      /* 56px */
  width: 1rem; height: 2.25rem;                         /* 16px x 36px */
  background: #2c231c; border-radius: 0.625rem;         /* 10px */
}
.eye--left{ left: calc(50% - 3.75rem); }                /* 60px */
.eye--right{ right: calc(50% - 3.75rem); }              /* 60px */
.nose{
  position: absolute; top: 5.75rem; left: 50%; transform: translateX(-50%); /* 92px */
  width: 2.25rem; height: 1rem;                         /* 36px x 16px */
  background: #f2a4a4; border-radius: 1.125rem;         /* 18px */
}

/* 본문 */
.body{ 
  background: var(--cream); padding: 1rem 0.875rem 0;   /* 16px 14px 0 */
  overflow-y: auto; min-width: 0;
  display: flex; flex-direction: column;
}

/* 웰컴 메시지 */
.welcome { margin-bottom: 1rem; }                       /* 16px */
.welcome-bubble {
  position: relative; max-width: calc(100% - 1.75rem);  /* 28px */
  background: #fff; border: 0.0625rem solid #d8d3c7;    /* 1px */
  border-radius: 0.75rem;                               /* 12px */
  padding: 0.875rem 1rem;                               /* 14px 16px */
  color: #222; box-shadow: 0 0.125rem 0.25rem rgba(0,0,0,.05); /* 2px 4px */
}

/* 채팅 메시지 */
.chat-messages { flex: 1; }
.message { margin-bottom: 0.875rem; display: flex; }    /* 14px */

.user-message { justify-content: flex-end; }
.ai-message { justify-content: flex-start; }

.message-bubble {
  max-width: 80%; padding: 0 0.9375rem;                 /* 0 15px */
  border-radius: 1.125rem;                               /* 18px */
  background: #fff; color: #333; position: relative;
  border-bottom: 0;
}
.message-bubble:not(.loading) div {
  margin: 0.9375rem 0.3125rem 0.125rem 0.3125rem;       /* 15px 5px 2px 5px */
}
.user-bubble {
  background: var(--accent); color: #fff;
  border-bottom-right-radius: 0.375rem;                 /* 6px */
}

.ai-message .message-bubble {
  background: #fff; border: 0.0625rem solid #d8d3c7;    /* 1px */
  border-bottom-left-radius: 0.375rem;                  /* 6px */
}

.timestamp {
  display: block; font-size: 0.6875rem;                 /* 11px */
  opacity: 0.6; margin-bottom: 0.625rem;                /* 10px */
}

.message-bubble.loading{
  width: 3.75rem;                                       /* 60px */
  height: 2.1875rem;                                    /* 35px */
  background: #ffffff !important;
  display: grid;
  place-items: center; 
}

/* 로딩 애니메이션 */
.loading { background: #f0f0f0 !important; }
.typing-indicator { display: flex; gap: 0.1875rem; }    /* 3px */
.typing-indicator span {
  width: 0.375rem; height: 0.375rem;                    /* 6px */
  border-radius: 50%; background: #999;
  animation: typing 1.4s infinite ease-in-out;
}
.typing-indicator span:nth-child(1) { animation-delay: 0s; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.5; }
  30% { transform: translateY(-0.625rem); opacity: 1; }  /* -10px */
}

/* 제안 칩들 */
.greet{ margin: 0.3125rem 0 0.0625rem 0.0625rem; }      /* 5px 0 1px 1px */
.chips{ display: flex; flex-wrap: wrap; gap: 0.5rem; }   /* 8px */
.chip{
  border: 0; padding: 0.375rem 0.75rem;                 /* 6px 12px */
  border-radius: 999px;
  background: #9b9b9b; color: #fff; font-weight: 600; cursor: pointer;
  font-size: 0.75rem;                                    /* 12px */
  transition: background 0.2s;
}
.chip:hover { background: #777; }

/* 입력부 */
.foot{
  background: var(--brand); display: grid;
  grid-template-columns: 2.75rem 1fr auto;              /* 44px */
  gap: 0.625rem;                                        /* 10px */
  align-items: center;
  padding: 0.625rem 0.625rem 0.75rem;                   /* 10px 10px 12px */
  border-top: 0.375rem solid var(--brand-dark);         /* 6px */
}
.btn-attach{
  width: 2.5rem; height: 2.5rem;                        /* 40px */
  border-radius: 50%; border: 0;
  background: #fff; color: var(--accent); font-size: 1.375rem; /* 22px */
  cursor: pointer; display: grid; place-items: center;
}
.input{
  height: 2.5rem; border-radius: 0.625rem;              /* 10px */
  border: 0; outline: none;
  padding: 0 0.75rem;                                   /* 12px */
  background: #f4f2e5; color: #333; font-size: 0.875rem;/* 14px */
}
.input:disabled { opacity: 0.7; cursor: not-allowed; }
.btn-send{
  height: 2.5rem; border-radius: 0.625rem;              /* 10px */
  border: 0;
  background: var(--accent); color: #fff; font-weight: 800;
  padding: 0 0.75rem; cursor: pointer; font-size: 0.875rem; /* 14px */
}
.btn-send:disabled {
  background: #ccc; cursor: not-allowed;
}

/* 메시지 안의 링크 스타일 */
.message-bubble a {
  color: inherit;
  text-decoration: underline;
}
.user-bubble a {
  color: #fff;
}

/* 상품 링크 스타일 */
.product-link {
  display: inline-block;
  background: var(--accent);
  color: #fff !important;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  text-decoration: none !important;
  font-weight: 600;
  margin-top: 0.75rem;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.product-link:hover {
  background: #e66a2a;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

/* 메시지 내 강조 텍스트 */
.message-bubble strong {
  color: var(--brand);
  font-weight: 700;
}

/* 스크롤바 */
.body::-webkit-scrollbar{ width: 0.5rem; }              /* 8px */
.body::-webkit-scrollbar-thumb{ background: #d0c8b2; border-radius: 0.625rem; } /* 10px */
</style>