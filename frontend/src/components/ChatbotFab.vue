<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { chatAPI } from '@/services/api'

const open = ref(false)
const toggle = () => (open.value = !open.value)
const close = () => (open.value = false)

// ESC로 닫기
const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') close() }
onMounted(() => {
  window.addEventListener('keydown', onKey)
  document.addEventListener('click', closeFAQOnOutsideClick)
})
onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKey)
  document.removeEventListener('click', closeFAQOnOutsideClick)
})

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
const inputRef = ref<HTMLInputElement>()
const showFAQ = ref(false)
const isRotating = ref(false)

// 제안 버튼들
const suggestions = [
  '뉴진스 응원봉 찾아줘',
  '피규어 검수 기준이 뭐야?',
  '귀칼 주인공 피규어 있어?',
  '키링 S급 기준 알려줘',
  '의류 B급은 어느 정도야?',
  'BTS 앨범 중고로 있어?'
]

// 자주 묻는 질문들
const faqQuestions = [
  '중고 상품 등급은 어떻게 나누나요?',
  'B등급 상품도 사용에 문제없나요?',
  'A등급과 B등급의 실질적 차이는?',
  '등급별 할인율은 어떻게 되나요?',
  '새 제품 대비 얼마나 저렴한가요?',
  '배송은 얼마나 걸리나요?',
  '받아보고 마음에 안 들면 교환/환불 가능한가요?',
  '피규어 관절이 헐거우면 사용할 수 없나요?',
  '부속품이나 소품이 빠진 경우도 있나요?',
  '의류나 잡화의 사용감은 어느 정도인가요?',
  '재고 수량이 실시간으로 업데이트되나요?',
  '특정 등급의 상품만 보고 싶어요',
  '검수는 누가 하나요?'
]

// FAQ 토글 함수
const toggleFAQ = () => {
  // 회전 애니메이션 시작
  isRotating.value = true
  setTimeout(() => {
    isRotating.value = false
  }, 300) // 0.3초 후 회전 상태 해제
  
  // FAQ 드롭다운을 조금 늦게 표시
  setTimeout(() => {
    showFAQ.value = !showFAQ.value
  }, 150) // 0.15초 후 FAQ 표시
}

// 바깥 영역 클릭으로 FAQ 닫기
const closeFAQOnOutsideClick = (event: Event) => {
  if (showFAQ.value && !(event.target as Element).closest('.faq-dropdown') && !(event.target as Element).closest('.btn-attach')) {
    showFAQ.value = false
  }
}

// FAQ 질문 클릭 함수
const handleFAQClick = (question: string) => {
  sendMessage(question)
  showFAQ.value = false
}

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
// 전처리 함수 추가
const cleanSearchQuery = (text: string) => {
  return text.replace(/(은|는|이|가|을|를|도|만|에서|으로|로)(\s|$)/g, '$2').trim()
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
    // 조사 전처리 후 API 호출
    const cleanedMessage = cleanSearchQuery(messageText)
    const response = await sendMessageToAPI(cleanedMessage)
    
    // 답변 메시지 추가
    addMessage(response.answer, false)
  } catch (error) {
    console.error('메시지 전송 오류:', error)
    addMessage('죄송해요, 오류가 발생했어요.', false)
  } finally {
    isLoading.value = false
    // 전송 완료 후 입력창에 포커스
    nextTick(() => {
      if (inputRef.value) {
        inputRef.value.focus()
      }
    })
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
            <button class="btn-attach" :class="{ 'rotating': isRotating }" title="자주 묻는 질문" aria-label="자주 묻는 질문" @click="toggleFAQ">?</button>
            <input 
              ref="inputRef"
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
              :disabled="isLoading"
            >
              {{ isLoading ? '전송중...' : '전송' }}
            </button>
            
            <!-- FAQ 드롭다운 - input 위에 -->
            <div v-if="showFAQ" class="faq-dropdown">
              <div class="faq-header">
                <h4>자주 묻는 질문</h4>
                <button class="faq-close" @click="showFAQ = false">×</button>
              </div>
              <div class="faq-list">
                <button 
                  v-for="(question, index) in faqQuestions" 
                  :key="index"
                  class="faq-item"
                  @click="handleFAQClick(question)"
                >
                  {{ question }}
                </button>
              </div>
            </div>
          </footer>
        </section>
      </div>
    </transition>
  </div>
</template>

<style scoped>
/* v-html로 생성된 링크는 scoped가 적용되지 않으므로 별도 처리 */
</style>

<style>
.chatbot .ai-message .message-bubble div a.product-link,
.chatbot .user-message .message-bubble div a.product-link {
  background: #B35428 !important;       
  color: #fff !important;               
  border-color: #B35428 !important;     
  text-decoration: none !important;
  font-weight: 600 !important;
  font-size: 0.9em !important;
  padding: 8px 16px !important;
  border-radius: 10px !important;
  display: inline-block !important;
  margin-top: 12px !important;
  transition: all 0.25s ease !important;
  cursor: pointer !important;
}


.chatbot .ai-message .message-bubble div a.product-link:hover,
.chatbot .user-message .message-bubble div a.product-link:hover {
  background: #B35428 !important;       
  color: #fff !important;               
  border-color: #B35428 !important;
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 12px rgba(107, 62, 46, 0.25) !important; /* 은은한 그림자 */
}

</style>

<style scoped>
/* 반응형 챗봇 크기 조정 */
.chatbot{
  /* 화면 크기에 따른 FAB 크기 조정 */
  --fab-size: clamp(4rem, 8vw, 8rem);                 /* 64px ~ 128px */
  --fab-right: clamp(3rem, 5vw, 4rem);                /* 48px ~ 64px - 왼쪽으로 이동 */
  --fab-bottom: clamp(5rem, 8vh, 7rem);               /* 80px ~ 112px - 위로 올림 */
  
  /* 화면 크기에 따른 패널 크기 조정 */
  --panel-w: clamp(20rem, 85vw, 26.25rem);            /* 320px ~ 420px */
  --panel-h: clamp(60vh, 75vh, 78vh);                 /* 60vh ~ 78vh */
  --panel-gap: clamp(0.5rem, 2vw, 1rem);              /* 8px ~ 16px */
  --panel-scale: clamp(0.7, 0.8, 0.9);                /* 0.7 ~ 0.9 */
  
  /* 색상 변수 */
  --brand: #c6742e;
  --brand-dark: #b36222;
  --cream: #f4f2e5;
  --accent: #ff7a3a;
  --shadow: 0 1rem 2.5rem rgba(0,0,0,.18);
  
  /* 패널 위치 조정 */
  --panel-shift-x: clamp(2rem, 4vw, 3rem);            /* 32px ~ 48px */
  --panel-shift-y: clamp(3rem, 5vw, 4.5rem);          /* 48px ~ 72px */
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
  box-shadow: var(--shadow); 
  background: var(--brand);
  position: relative;
}

.squirrel-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('/img/chatbot/background.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  opacity: 0.1;
  z-index: 0;
}

/* 머리 */
.head{ position: relative; background: var(--brand); z-index: 1; }
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
  position: relative; z-index: 1;
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
/* 칩 버튼 컨테이너 */
.chips {
  display: flex;
  flex-wrap: wrap;          /* 줄바꿈 허용 */
  gap: 0.75rem 0.5rem;      /* 가로·세로 간격 */
  justify-content: flex-start; /* 왼쪽 정렬 (원하면 center도 가능) */
  margin-top: 0.75rem;
  margin-bottom: 0.5rem;
}

/* 칩 버튼 스타일 */
.chip {
  border: 0;
  padding: 0.4rem 0.9rem;         /* 패딩 살짝 줄이기 */
  border-radius: 999px;
  background: #9b9b9b;
  color: #fff;
  font-weight: 500;               /* 굵기 ↓ (600 → 500) */
  font-size: 0.78rem;             /* 크기 ↓ (12.5px 정도) */
  line-height: 1.3;               /* 글자 간격 여유 */
  cursor: pointer;

  display: inline-block;
  white-space: nowrap;
  text-align: center;

  transition: transform 0.15s ease, box-shadow 0.2s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.12); /* 그림자도 약하게 */
}

.chip:hover {
  background: #777;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0,0,0,0.18);
}



/* 입력부 */
.foot{
  background: var(--brand); display: grid;
  grid-template-columns: 2.75rem 1fr auto;              /* 44px */
  gap: 0.625rem;                                        /* 10px */
  align-items: center;
  padding: 0.625rem 0.625rem 0.75rem;                   /* 10px 10px 12px */
  border-top: 0.375rem solid var(--brand-dark);         /* 6px */
  position: relative; z-index: 1;
}
.btn-attach{
  width: 2.5rem; height: 2.5rem;                        /* 40px */
  border-radius: 50%; border: 0;
  background: #fff; color: var(--accent); font-size: 1.375rem; /* 22px */
  cursor: pointer; display: grid; place-items: center;
  transition: transform 0.3s ease;
}

.btn-attach.rotating {
  transform: rotate(50deg);
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

/* FAQ 드롭다운 스타일 */
.faq-dropdown {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #d8d3c7;
  border-radius: 0.75rem 0.75rem 0 0;
  box-shadow: 0 -4px 12px rgba(0,0,0,0.1);
  z-index: 1000;
  max-height: 250px;
  overflow-y: auto;
  margin-bottom: 0.625rem;
 
}

.faq-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: var(--brand);
  color: #fff;
  border-radius: 0.75rem 0.75rem 0 0;
}

.faq-header h4 {
  margin: 0;
  font-size: 0.875rem;
  font-weight: 600;
}

.faq-close {
  background: none;
  border: none;
  color: #fff;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0;
  width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.faq-list {
  padding: 0.5rem;
}

.faq-item {
  display: block;
  width: 100%;
  padding: 0.5rem 0.75rem;
  margin-bottom: 0.25rem;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 0.5rem;
  color: #333;
  font-size: 0.8rem;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
}

.faq-item:hover {
  background: var(--brand);
  color: #fff;
  border-color: var(--brand);
}

.faq-item:last-child {
  margin-bottom: 0;
}

/* 메시지 안의 링크 스타일 */
.message-bubble a {
  color: inherit;
  text-decoration: underline;
}
.user-bubble a {
  color: #fff;
}

/* 상품 링크 스타일 - 실제 HTML 구조에 맞는 선택자 */
.chatbot .ai-message .message-bubble div a.product-link,
.chatbot .user-message .message-bubble div a.product-link {
  color: var(--brand) !important;
  text-decoration: underline !important;
  font-weight: 900 !important;
  font-size: 1.1em !important;
  transition: all 0.2s ease !important;
  display: inline !important;
}

.chatbot .ai-message .message-bubble div a.product-link:hover,
.chatbot .user-message .message-bubble div a.product-link:hover {
  color: var(--brand-dark) !important;
  text-decoration: underline !important;
  font-size: 1.15em !important;
  font-weight: 900 !important;
}

/* 메시지 내 강조 텍스트 */
.message-bubble strong {
  color: var(--brand);
  font-weight: 700;
}

/* 메시지 버블 내 텍스트 줄바꿈 개선 - 긴 상품명 가독성 향상 */
.message-bubble div {
  word-break: keep-all;
  overflow-wrap: break-word;
  line-height: 1.4;
  white-space: pre-wrap;
  hyphens: auto;
  -webkit-hyphens: auto;
  -ms-hyphens: auto;
  font-weight: 500;
}

/* 스크롤바 */
.body::-webkit-scrollbar{ width: 0.5rem; }              /* 8px */
.body::-webkit-scrollbar-thumb{ background: #d0c8b2; border-radius: 0.625rem; } /* 10px */

/* 반응형 미디어 쿼리 */
@media (max-width: 768px) {
  .chatbot {
    /* 모바일에서 더 작은 크기 */
    --fab-size: clamp(3.5rem, 10vw, 5rem);             /* 56px ~ 80px */
    --panel-w: clamp(18rem, 90vw, 22rem);              /* 288px ~ 352px */
    --panel-h: clamp(50vh, 70vh, 75vh);                /* 50vh ~ 75vh */
    --panel-scale: clamp(0.6, 0.75, 0.8);              /* 0.6 ~ 0.8 */
    --panel-shift-x: clamp(1rem, 3vw, 2rem);           /* 16px ~ 32px */
    --panel-shift-y: clamp(2rem, 4vw, 3rem);           /* 32px ~ 48px */
  }
  
  /* 모바일에서 패널 위치 조정 */
  .panel-wrap {
    right: clamp(0.5rem, 2vw, 1rem) !important;
    bottom: clamp(0.5rem, 2vw, 1rem) !important;
    transform: scale(var(--panel-scale)) !important;
  }
  
  /* 모바일에서 FAB 위치 조정 */
  .fab-wrap {
    right: clamp(2rem, 4vw, 2.5rem) !important;       /* 왼쪽으로 이동 */
    bottom: clamp(4rem, 6vh, 5.5rem) !important;       /* 위로 올림 */
  }
}

@media (max-width: 480px) {
  .chatbot {
    /* 매우 작은 화면에서 최소 크기 */
    --fab-size: clamp(3rem, 12vw, 4rem);               /* 48px ~ 64px */
    --panel-w: clamp(16rem, 95vw, 20rem);              /* 256px ~ 320px */
    --panel-h: clamp(45vh, 65vh, 70vh);                /* 45vh ~ 70vh */
    --panel-scale: clamp(0.5, 0.7, 0.75);              /* 0.5 ~ 0.75 */
  }
  
  /* 매우 작은 화면에서 FAB 위치 조정 */
  .fab-wrap {
    right: clamp(1.5rem, 3vw, 2rem) !important;       /* 왼쪽으로 이동 */
    bottom: clamp(3.5rem, 5vh, 4.5rem) !important;     /* 위로 올림 */
  }
  
  /* 작은 화면에서 텍스트 크기 조정 */
  .chip {
    font-size: 0.7rem !important;                      /* 11.2px */
    padding: 0.3rem 0.7rem !important;                 /* 4.8px 11.2px */
  }
  
  .message-bubble div {
    font-size: 0.85rem !important;                     /* 13.6px */
  }
  
  .input {
    font-size: 0.8rem !important;                      /* 12.8px */
  }
  
  .btn-send {
    font-size: 0.8rem !important;                      /* 12.8px */
    padding: 0 0.5rem !important;                      /* 0 8px */
  }
}
</style>
