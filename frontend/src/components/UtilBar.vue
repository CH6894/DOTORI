<!-- src/components/UtilBar.vue -->
<template>
  <div class="utilbar">
    <div class="container utilbar__inner">
      <div class="utilbar__spacer"></div>
      <nav class="utilbar__links">
        <a v-if="!isAuthed" href="javascript:;" @click="loginNaver">로그인</a>
        <a v-else href="javascript:;" @click="logout">로그아웃</a>
        <span aria-hidden="true">/</span>
        <a href="#">고객센터</a>
      </nav>
   </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue"

const isAuthed = ref(false)
const API_BASE = 'http://localhost:8081'

// 현재 로그인 상태 반영
function syncAuth() {
  isAuthed.value = !!localStorage.getItem("accessToken")
}

async function logout() {
  try {
    await fetch(`${API_BASE}/logout`, {
      method: "POST",
      credentials: "include",
    })
  } catch (_) {
    // JWT만 쓰면 실패 무시
  } finally {
    localStorage.removeItem("accessToken")
    syncAuth()
    window.location.href = "/"
  }
}

function loginNaver() {
  window.location.href = "/login"
}

onMounted(() => {
  syncAuth()

  const params = new URLSearchParams(window.location.search)
  const token = params.get("token")
  if (token) {
    localStorage.setItem("accessToken", token)
    syncAuth()
    window.history.replaceState({}, document.title, window.location.pathname)
  }

  window.addEventListener("storage", (e) => {
    if (e.key === "accessToken") syncAuth()
  })
})
</script>

<style scoped>
.utilbar {
  background: #f5efdf;
  border-bottom: 1px solid #ece4d1;
  font-size: 12px;
  color: #847768;
  display: flex;
  justify-content: center;
  width: 100%;
}
.utilbar__inner {
  display:flex;
  align-items:center;
  justify-content:flex-end;
  min-height: 36px;
  width: 1280px;
}
.utilbar__links a { 
    padding: 0 8px;
    color:#000;
    text-decoration:none;
}
.utilbar__links span {
  color:#c4b9a9;
}
</style>
