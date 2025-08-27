import axios from 'axios'

// 백엔드 API 기본 URL
const API_BASE_URL = 'http://127.0.0.1:8000'

// axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30초 타임아웃
})

// 타입 정의 (백엔드와 일치)
export interface ChatMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
}

export interface ChatRequest {
  message: string
  session_id?: string
  conversation_history?: ChatMessage[]
}

export interface ChatResponse {
  response: string
  session_id: string
  timestamp: string
  tokens_used?: number
  model_used?: string
}

// API 함수들
export const chatAPI = {
  // PDF 기반 질답 (RAG)
  async sendMessage(request: ChatRequest): Promise<ChatResponse> {
    try {
      const response = await apiClient.post('/api/chat', request)
      return response.data
    } catch (error) {
      console.error('챗봇 API 오류:', error)
      throw new Error('챗봇 서비스에 연결할 수 없습니다.')
    }
  },
  // 서비스 상태 확인
  async healthCheck() {
    try {
      const response = await apiClient.get('/health')
      return response.data
    } catch (error) {
      console.error('헬스체크 실패:', error)
      return { status: 'unhealthy' }
    }
  }
}