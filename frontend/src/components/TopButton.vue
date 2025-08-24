<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
// import { useRouter } from 'vue-router'  // 라우터 쓸 경우 주석 해제

const props = withDefaults(defineProps<{
  threshold?: number
  homeHref?: string
}>(), {
  threshold: 240,
  homeHref: '/',   // 기본 홈 경로
})

const visible = ref(false)
// const router = useRouter()  // 라우터 쓸 경우 주석 해제

const onScroll = () => {
  visible.value = window.scrollY > props.threshold
}

const toTop = () => {
  const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  window.scrollTo({ top: 0, behavior: prefersReduced ? 'auto' : 'smooth' })
}

const toHome = () => {
  // vue-router 사용 시:
  // router.push({ path: '/' })  // 혹은 원하는 라우트
  // 일반 링크 이동:
  window.location.assign(props.homeHref)
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})
onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <!-- 버튼 스택 컨테이너 -->
  <transition name="fade-pop">
    <div v-show="visible" class="fab-stack" aria-hidden="false">
      <!-- 홈 버튼 -->
      <button
        class="fab"
        type="button"
        @click="toHome"
        aria-label="홈으로"
        title="홈으로"
      >
        <!-- 집 아이콘 (stroke 아이콘) -->
        <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
          <!-- 지붕/외곽 -->
          <path d="M4 10l8-6 8 6v8a2 2 0 0 1-2 2h-4a0 0 0 0 1 0 0h-4a0 0 0 0 1 0 0h-4a2 2 0 0 1-2-2z"
                fill="none" stroke="currentColor" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round"/>
          <!-- 현관 (문) -->
          <path d="M10 20v-5h4v5" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>

      <!-- 위로 가기 버튼 (화살표 머리만: chevron-up) -->
      <button
        class="fab"
        type="button"
        @click="toTop"
        aria-label="상단으로"
        title="상단으로"
      >
        <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
          <path d="M6 14l6-6 6 6"
                fill="none" stroke="currentColor" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  </transition>
</template>

<style scoped>
.fab-stack{
  position: fixed;
  right: 24px;
  bottom: calc(24px + env(safe-area-inset-bottom, 0px));
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: 12px; /* 버튼 간격 */
}

/* 공통 FAB 버튼 */
.fab{
  display: inline-flex;
  align-items: center;
  justify-content: center;

  width: 44px; height: 44px;        /* 접근성 권장 최소 크기 */
  border: none; border-radius: 50%;
  background: #fff; color: #222;
  box-shadow: 0 8px 24px rgba(0,0,0,.18);
  cursor: pointer;

  transition: transform .2s ease, box-shadow .2s ease, opacity .2s ease;
}
.fab:hover{ transform: translateY(-2px); box-shadow: 0 12px 32px rgba(0,0,0,.22); }
.fab:active{ transform: translateY(0);   box-shadow: 0 6px 18px rgba(0,0,0,.16); }
.fab:focus-visible{ outline: 2px solid #7aa2ff; outline-offset: 2px; }

.ic{ width: 20px; height: 20px; display: block; }

/* 페이드+팝 애니메이션 */
.fade-pop-enter-from, .fade-pop-leave-to { opacity: 0; transform: translateY(8px); }
.fade-pop-enter-active, .fade-pop-leave-active { transition: opacity .18s ease, transform .18s ease; }

/* 감속 선호 시 애니메이션 제거 */
@media (prefers-reduced-motion: reduce){
  .fab, .fade-pop-enter-active, .fade-pop-leave-active{ transition: none !important; }
}
</style>
