import { createApp } from 'vue'

// 간단한 테스트 컴포넌트
const TestApp = {
  template: `
    <div>
      <h1>Vite 테스트 성공!</h1>
      <p>이 화면이 보이면 Vite가 정상 작동하는 거예요</p>
    </div>
  `
}

createApp(TestApp).mount('#app')