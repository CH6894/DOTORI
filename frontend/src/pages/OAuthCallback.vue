<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// URL에서 token 추출
const token = route.query.token as string | undefined

if (token) {
  // 로컬스토리지에 저장
  localStorage.setItem("AuthToken", token)
  console.log("[OAuth2Callback] 토큰 저장됨:", token)

  // 원하는 페이지로 이동 (예: 메인 화면)
  router.replace("/")
} else {
  console.error("[OAuth2Callback] 토큰이 없음")
  router.replace("/login")
}
</script>

<template>
  <div>로그인 처리 중...</div>
</template>
