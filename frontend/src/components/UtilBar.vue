<!-- src/components/UtilBar.vue -->
<script setup lang="ts">
import { ref } from "vue"
import { RouterLink, useRouter } from "vue-router"

const router = useRouter()

// 나중에 실제 auth store나 API 연결 시 교체
const isLoggedIn = ref(false)

function handleAuth() {
  if (isLoggedIn.value) {
    // 로그아웃 로직 (ex. 토큰 삭제, 세션 해제 등)
    isLoggedIn.value = false
    alert("로그아웃 되었습니다.")
    router.push({ name: "main" }) // 홈으로 보내기
  } else {
    router.push({ name: "login" }) // 로그인 페이지 이동
  }
}
</script>

<template>
  <div class="utilbar">
    <div class="container utilbar__inner">
      <div class="utilbar__spacer"></div>

      <nav class="utilbar__links">
        <!-- ✅ 상태에 따라 버튼 표시 변경 -->
        <button @click="handleAuth" class="auth-btn">
          {{ isLoggedIn ? "로그아웃" : "로그인" }}
        </button>
        <span aria-hidden="true">/</span>
        <RouterLink to="#">고객센터</RouterLink>
      </nav>
    </div>
  </div>
</template>

<style scoped>
/* ---------- Util bar ---------- */
.utilbar {
  background: #f5efdf;
  border-bottom: 1px solid #ece4d1;
  font-size: 12px;
  color: #847768;
}
.utilbar__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 36px;
}
.utilbar__links a,
.utilbar__links .auth-btn {
  padding: 0 8px;
  background: none;
  border: none;
  cursor: pointer;
  font: inherit;
  color: inherit;
}
.utilbar__links span {
  color: #c4b9a9;
}
.utilbar__links .auth-btn:hover {
  text-decoration: underline;
}
</style>
