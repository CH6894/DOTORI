<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
const router = useRouter()
const auth = useAuthStore()

onMounted(() => {
  const token = new URLSearchParams(location.search).get('token')
  if (token) {
    localStorage.setItem('accessToken', token)
    router.replace('/')
  } else {
    alert('로그인 실패'); router.replace('/login')
  }
})
</script>
<template><div>로그인 처리 중...</div></template>
