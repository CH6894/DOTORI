<!-- src/App.vue (merged) -->
<script setup lang="ts">
import { computed } from "vue"
import { useRoute } from "vue-router"

import UtilityBar from "@/components/UtilBar.vue"
import MainHeader from "@/components/Header.vue"       
import AppHeader  from "@/components/AppHeader.vue"    
import AppFooter  from "@/components/AppFooter.vue"
import ChatbotFab from "@/components/ChatbotFab.vue"
import TopButton  from "@/components/TopButton.vue"

type HeaderKind = "main" | "sub" | "none"

const route = useRoute()

// 라우트 메타 기반 레이아웃 제어
const headerKind  = computed<HeaderKind>(() => (route.meta.header as HeaderKind) ?? "main")
const showFooter  = computed<boolean>(() => (route.meta.footer  as boolean) ?? true)
const showUtil    = computed<boolean>(() => (route.meta.utilbar as boolean) ?? true)
const showChatbot = computed<boolean>(() => route.meta.chatbot !== false) 
const showTop     = computed<boolean>(() => route.meta.topbtn  !== false) 

// 헤더 선택
const currentHeader = computed(() => {
  if (headerKind.value === "main") return MainHeader
  if (headerKind.value === "sub")  return AppHeader
  return null // "none"
})
</script>

<template>
  <div id="app">
    <!-- 상단 유틸바 -->
    <UtilityBar v-if="showUtil" />

    <!-- 동적 헤더 -->
    <div v-if="currentHeader" class="app-header">
      <!-- 🔧 Transition 경고 방지: 요소(root)로 한 번 감싸서 key 부여 -->
      <Transition mode="out-in">
        <div :key="headerKind">
          <component :is="currentHeader" />
        </div>
      </Transition>
    </div>

    <!-- 페이지 본문 -->
    <main class="main-content">
      <RouterView />
    </main>

    <!-- 공통 푸터 -->
    <AppFooter v-if="showFooter" />

    <!-- 플로팅 UI -->
    <ChatbotFab v-if="showChatbot" class="floating-chatbot" />
    <TopButton  v-if="showTop"     class="floating-topbtn" />
  </div>
</template>

<style>
:root { --utilbar-h: 36px; }

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  position: sticky;   /* 정책에 따라 fixed로 바꿔도 됨 */
  top: 0;             /* 유틸바 아래로 내리고 싶으면 top: var(--utilbar-h); */
  z-index: 100;       /* 모달/드롭다운 z-index 설계에 맞춰 조정 */
}

/* 본문은 남는 공간을 차지하여 footer를 아래로 밀어냄 */
.main-content {
  flex: 1;
  position: relative;
  z-index: 1;
}

/* 요구사항: 링크 방문 스타일 공통 적용 필요 시 전역 CSS에서 관리 권장
a { text-decoration: none; color: inherit; }
a:visited { text-decoration: none; color: inherit; } */
</style>
