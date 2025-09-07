import axios from 'axios'

// 백엔드 API 기본 URL
const API_BASE_URL = 'http://49.50.135.201'

// axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30초 타임아웃
})

// 타입 정의 (백엔드와 일치)
export interface ChatRequest {
  query: string
  user_id?: string
}

export interface ChatResponse {
  answer: string
}

// API 함수들
export const chatAPI = {
  // PDF 기반 질답 (RAG)
  sendMessage(query: string, user_id: string = "default"): Promise<ChatResponse> {
    const request: ChatRequest = {
      query,
      user_id
    }
    return apiClient.post('/api/chat', request).then(response => response.data)
  },
  
  // 서비스 상태 확인
  healthCheck() {
    return apiClient.get('/').then(response => response.data)
  }
}