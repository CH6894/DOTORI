<!-- src/App.vue (patched) -->
<script setup lang="ts">
import { computed } from "vue"
import { useRoute, RouterView } from "vue-router"

import UtilityBar from "@/components/UtilBar.vue"
import MainHeader from "@/components/Header.vue"        // 메인 헤더
import AppHeader  from "@/components/AppHeader.vue"     // 서브 헤더
import AppFooter  from "@/components/AppFooter.vue"
import ChatbotFab from "@/components/ChatbotFab.vue"
import TopButton  from "@/components/TopButton.vue"

type HeaderKind = "main" | "sub" | "none"

const route = useRoute()

/** 라우트 메타로 레이아웃 제어 */
const headerKind  = computed<HeaderKind>(() => (route.meta.header as HeaderKind) ?? "main")
const showFooter  = computed<boolean>(() => (route.meta.footer  as boolean) ?? true)
const showUtil    = computed<boolean>(() => (route.meta.utilbar as boolean) ?? true)
const showChatbot = computed<boolean>(() => route.meta.chatbot !== false) // 기본: 보임
const showTop     = computed<boolean>(() => route.meta.topbtn  !== false) // 기본: 보임

/** 헤더 선택 */
const currentHeader = computed(() => {
  if (headerKind.value === "main") return MainHeader
  if (headerKind.value === "sub")  return AppHeader
  return null // "none"이면 헤더 숨김
})
</script>

<template>
  <div id="app">
    <!-- 상단 유틸바 -->
    <UtilityBar v-if="showUtil" />

    <!-- 동적 헤더 (래퍼로 감싸 class 부여 + out-in 교체) -->
    <div v-if="currentHeader" class="app-header">
      <Transition mode="out-in">
        <component :is="currentHeader" :key="headerKind" />
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

/* 헤더가 fragment를 렌더하더라도 래퍼에 class가 안전하게 적용됨 */
.app-header {
  position: sticky; /* 또는 fixed; 정책에 맞게 */
  top: 0;
  z-index: 100;     /* 모달 오버레이가 위로 오도록 100 이하 권장 */
}

.main-content {
  flex: 1;
  position: relative;
  z-index: 1;
}
</style>
