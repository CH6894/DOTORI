<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

onMounted(() => {
  const token = new URLSearchParams(location.search).get('token')
  
  if (token) {
    auth.setToken(token)
    router.replace('/')
  } else {
    alert('로그인 실패')
    router.replace('/login')
  }
})
</script>

<template>
  <div class="oauth-callback">
    <div class="loading-spinner"></div>
    <p>로그인 처리 중...</p>
  </div>
</template>

<style scoped>
.oauth-callback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  gap: 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
