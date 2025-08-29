<template>
  <span
    class="badge"
    role="img"
    :aria-label="ariaLabel"
    :style="{
      top: typeof offsetTop === 'number' ? offsetTop + 'px' : offsetTop,
      left: typeof offsetLeft === 'number' ? offsetLeft + 'px' : offsetLeft,
      zIndex: String(zIndex),
    }"
  >
    <svg
      :width="size"
      :height="size"
      viewBox="0 0 24 24"
      aria-hidden="true"
      focusable="false"
    >
      <!-- 파란 원 (완전 불투명) -->
      <circle cx="12" cy="12" r="10" :fill="color" />
      <!-- 체크 -->
      <path
        d="M7 12.5l3 3.5 7-8"
        :stroke="checkColor"
        stroke-width="2.5"
        stroke-linecap="round"
        stroke-linejoin="round"
        fill="none"
      />
    </svg>
  </span>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  size?: number | string
  color?: string
  checkColor?: string
  ariaLabel?: string
  offsetTop?: number | string
  offsetLeft?: number | string
  zIndex?: number
}>(), {
  size: 24,
  color: '#0A1FE4',     // 파란 원 (불투명)
  checkColor: '#FFFFFF',// 흰 체크
  ariaLabel: '인증됨',
  offsetTop: 8,         // 부모 좌측 상단에서 8px
  offsetLeft: 8,
  zIndex: 20,           // 이미지 위로 보장
})
</script>

<style scoped>
.badge{
  position: absolute;           /* 부모 기준 절대 배치 */
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  box-shadow: 0 4px 10px rgba(0,0,0,.15);
  pointer-events: none;         /* 카드 클릭 방해 금지 */
  isolation: isolate;   
          /* 예기치 않은 혼합 효과 방지 */
}
.badge svg{ display: block }    /* 안티앨리어싱 여백 방지 */
</style>
