<template>
  <div class="connection-test">
    <h2>백엔드 연결 테스트</h2>
    
    <div class="test-section">
      <h3>서버 상태 확인</h3>
      <button @click="testServerConnection" :disabled="loading.server">
        {{ loading.server ? '확인 중...' : '서버 연결 테스트' }}
      </button>
      <div v-if="results.server" class="result">
        <p v-if="results.server.success" class="success">✅ 서버 연결 성공</p>
        <p v-else class="error">❌ 서버 연결 실패: {{ results.server.error }}</p>
        <pre>{{ JSON.stringify(results.server, null, 2) }}</pre>
      </div>
    </div>

    <div class="test-section">
      <h3>챗봇 API 테스트</h3>
      <input v-model="testMessage" placeholder="테스트 메시지 입력" />
      <button @click="testChatbotAPI" :disabled="loading.chatbot">
        {{ loading.chatbot ? '전송 중...' : '챗봇 테스트' }}
      </button>
      <div v-if="results.chatbot" class="result">
        <p v-if="results.chatbot.success" class="success">✅ 챗봇 응답 성공</p>
        <p v-else class="error">❌ 챗봇 응답 실패: {{ results.chatbot.error }}</p>
        <pre>{{ JSON.stringify(results.chatbot, null, 2) }}</pre>
      </div>
    </div>

    <div class="test-section">
      <h3>API 설정 정보</h3>
        <div class="api-info">
          <p><strong>백엔드 URL:</strong> http://49.50.135.201</p>
          <p><strong>챗봇 엔드포인트:</strong> /api/chat</p>
          <p><strong>전체 URL:</strong> http://49.50.135.201/api/chat</p>              
       </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { chatAPI } from '@/services/api'

const loading = reactive({
  server: false,
  chatbot: false
})

const results = reactive({
  server: null as any,
  chatbot: null as any
})

const testMessage = ref('안녕하세요')

// 서버 연결 테스트
const testServerConnection = async () => {
  loading.server = true
  try {
    const response = await chatAPI.healthCheck()
    results.server = {
      success: true,
      data: response
    }
  } catch (error: any) {
    results.server = {
      success: false,
      error: error.message,
      status: error.response?.status,
      data: error.response?.data
    }
  } finally {
    loading.server = false
  }
}

// 챗봇 API 테스트
const testChatbotAPI = async () => {
  loading.chatbot = true
  try {
    const response = await chatAPI.sendMessage(testMessage.value)
    results.chatbot = {
      success: true,
      data: response
    }
  } catch (error: any) {
    results.chatbot = {
      success: false,
      error: error.message,
      status: error.response?.status,
      data: error.response?.data
    }
  } finally {
    loading.chatbot = false
  }
}
</script>

<style scoped>
.connection-test {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.test-section {
  margin: 20px 0;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.test-section h3 {
  margin-top: 0;
  color: #333;
}

button {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  margin: 5px;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

button:hover:not(:disabled) {
  background-color: #0056b3;
}

input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin: 5px;
  width: 200px;
}

.result {
  margin-top: 10px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.success {
  color: #28a745;
  font-weight: bold;
}

.error {
  color: #dc3545;
  font-weight: bold;
}

pre {
  background-color: #f1f1f1;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
}

.api-info {
  background-color: #e9ecef;
  padding: 15px;
  border-radius: 4px;
}

.api-info p {
  margin: 5px 0;
}
</style>
