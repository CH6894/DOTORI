<!-- src/App.vue (merged) -->
<script setup lang="ts">
import { computed } from "vue"
import { useRoute, RouterView } from "vue-router"

import UtilityBar from "@/components/UtilBar.vue"
import MainHeader from "@/components/Header.vue"        // 메인(메인 랜딩 등)
import AppHeader  from "@/components/AppHeader.vue"     // 서브(일반 내부 페이지)
import AppFooter  from "@/components/AppFooter.vue"
import ChatbotFab from "@/components/ChatbotFab.vue"
import TopButton  from "@/components/TopButton.vue"

/** 라우트 메타로 레이아웃 제어 */
type HeaderKind = "main" | "sub" | "none"

const route = useRoute()

const headerKind  = computed<HeaderKind>(() => (route.meta.header as HeaderKind) ?? "sub")
const showFooter  = computed<boolean>(() => (route.meta.footer  as boolean) ?? true)
const showUtil    = computed<boolean>(() => (route.meta.utilbar as boolean) ?? true)
const showChatbot = computed<boolean>(() => route.meta.chatbot !== false) // 기본: 보임
const showTop     = computed<boolean>(() => route.meta.topbtn  !== false) // 기본: 보임

/** 헤더 선택 (팀원 App.vue와의 합본: 기본 AppHeader 사용) */
const currentHeader = computed(() => {
  if (headerKind.value === "main") return MainHeader
  if (headerKind.value === "sub")  return AppHeader
  return null                      // "none"이면 헤더 숨김
})

/* 필요 시 모달 오픈 시 헤더/푸터 숨기고 싶다면 주석 해제
const hideForModal = computed(() => route.query.sell === '1')
const showFooterFinal  = computed(() => !hideForModal.value && showFooter.value)
const showUtilFinal    = computed(() => !hideForModal.value && showUtil.value)
const showChatbotFinal = computed(() => !hideForModal.value && showChatbot.value)
const showTopFinal     = computed(() => !hideForModal.value && showTop.value)
*/
</script>

<template>
  <div id="app">
    <!-- 상단 유틸바 -->
    <UtilityBar v-if="showUtil" />

    <!-- 공통/동적 헤더 -->
    <component :is="currentHeader" v-if="currentHeader" class="app-header" />

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
  position: sticky; /* 또는 fixed; 프로젝트 정책에 맞게 */
  top: 0;
  z-index: 100;     /* 모달 오버레이가 위로 오도록 100 이하 유지 권장 */
}

.main-content {
  flex: 1;
  position: relative;
  z-index: 1;
}

/* 플로팅 컴포넌트 위치는 각 컴포넌트에서 스타일링하는 걸 권장 */
</style>
