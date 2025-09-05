<script setup lang="ts">
import { onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

onMounted(() => {
  // URL에서 token과 redirect URL 추출
  const token = route.query.token as string | null;
  const redirectUrl = route.query.redirect_uri as string | null;

  if (token) {
    try {
      // 로컬스토리지에 저장 (accessToken으로 통일)
      localStorage.setItem("accessToken", token);
      console.log("[OAuth2Callback] 토큰 저장 성공:", token);

      // ✅ 필요하다면 axios 디폴트 헤더에도 즉시 반영
      // import axios from "axios"; (위에 추가)
      // axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;

      // 리다이렉트 URL이 있으면 해당 페이지로, 없으면 메인화면으로
      const targetUrl = redirectUrl || "/";
      console.log("[OAuth2Callback] 리다이렉트:", targetUrl);
      router.replace(targetUrl);
    } catch (e) {
      console.error("[OAuth2Callback] 토큰 저장 중 오류:", e);
      router.replace("/login");
    }
  } else {
    console.error("[OAuth2Callback] 토큰이 없음 → 로그인 페이지로 이동");
    router.replace("/login");
  }
});
</script>

<template>
  <div style="text-align:center; padding:40px;">
    <h2>로그인 처리 중입니다...</h2>
    <p>잠시만 기다려 주세요 ⏳</p>
  </div>
</template>
