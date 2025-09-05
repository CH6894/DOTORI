<template>
  <div class="not-found">
    <div class="not-found__container">
      <div class="not-found__content">
        <div class="not-found__icon">
          <svg width="120" height="120" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" fill="#c6742e"/>
          </svg>
        </div>
        
        <h1 class="not-found__title">404</h1>
        <h2 class="not-found__subtitle">페이지를 찾을 수 없습니다</h2>
        <p class="not-found__description">
          요청하신 페이지가 존재하지 않거나 이동되었을 수 있습니다.<br>
          URL을 다시 확인해주시거나 홈으로 돌아가세요.
        </p>
        
        <div class="not-found__actions">
          <RouterLink to="/" class="btn btn--primary">
            홈으로 돌아가기
          </RouterLink>
          <button @click="goBack" class="btn btn--secondary">
            이전 페이지
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goBack() {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  background: transparent;
  padding: 2rem;
  padding-top: 4rem;
}

.not-found__container {
  max-width: 600px;
  width: 100%;
  text-align: center;
}

.not-found__content {
  background: transparent;
  border-radius: 1.5rem;
  padding: 3rem 2rem;
}

.not-found__icon {
  margin-bottom: 2rem;
  opacity: 0.7;
}

.not-found__title {
  font-size: 4rem;
  font-weight: 700;
  color: #c6742e;
  margin: 0 0 1rem 0;
  line-height: 1;
}

.not-found__subtitle {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 1rem 0;
}

.not-found__description {
  font-size: 1rem;
  color: #666;
  line-height: 1.6;
  margin: 0 0 2.5rem 0;
}

.not-found__actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s ease;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  min-width: 140px;
}

.btn--primary {
  background: #c6742e;
  color: white;
}

.btn--primary:hover {
  background: #b36222;
  transform: translateY(-1px);
}

.btn--secondary {
  background: transparent;
  color: #c6742e;
  border: 2px solid #c6742e;
}

.btn--secondary:hover {
  background: #c6742e;
  color: white;
  transform: translateY(-1px);
}

/* 반응형 */
@media (max-width: 768px) {
  .not-found {
    padding: 1rem;
    padding-top: 3rem;
  }
  
  .not-found__content {
    padding: 2rem 1.5rem;
  }
  
  .not-found__title {
    font-size: 3rem;
  }
  
  .not-found__subtitle {
    font-size: 1.25rem;
  }
  
  .not-found__actions {
    flex-direction: column;
    align-items: center;
  }
  
  .btn {
    width: 100%;
    max-width: 200px;
  }
}
</style>
