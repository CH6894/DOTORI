// src/api/tradeHistory.ts
// 거래내역 관련 API 호출 함수들
import api from './axios'

// 거래내역 타입 정의
export interface TradeHistoryItem {
  no: string              // 거래번호
  kind: 'buy' | 'sell'    // 거래유형
  item: string            // 상품명
  price: number           // 가격
  state: {
    type: string          // 상태 타입
    text: string          // 상태 텍스트
  }
  date: string            // 거래일시 (ISO string)
  image: string           // 상품 이미지 URL
}

// 거래내역 조회
export const fetchTradeHistory = () =>
  api.get<TradeHistoryItem[]>('/orders/trade-history').then(r => r.data)
