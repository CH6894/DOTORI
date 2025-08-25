<template>
  <div class="page">
    <Header />

    <main class="container">
      <h1>굿즈 검수 요청</h1>
      <button class="btn" @click="open = true">검수 요청하기</button>

      <!-- ✅ 모달은 여기서 열고 닫음 -->
      <UploadVerifyModal v-model="open" @submit="handleSubmit" />
    </main>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
// import Header from '../components/AppHeader.vue'
// import Footer from '../components/AppFooter.vue'
import UploadVerifyModal from '../components/UploadVerifyModal.vue'

const open = ref(false)
// 페이지 들어오자마자 띄워보고 싶으면 주석 해제
// onMounted(() => { open.value = true })

function handleSubmit(payload: { files: { file: File; caption?: string; isCover: boolean }[] }) {
  console.log('검수 제출 payload:', payload)
  // TODO: FormData로 변환해서 API 호출
}
</script>
