// src/types/api.ts

// Collection API 응답 타입
export interface CollectionVerifyResponse {
  ok: boolean
  itemKey?: string
  message?: string
}

export interface DexStateResponse {
  userId: string
  items: UserItemState[]
}

export interface UserItemState {
  itemKey: string
  activated: boolean
  verified: boolean
}

// HTTP 클라이언트 확장 타입
export interface HttpConfig {
  __DUMMY__?: {
    data: any
  }
}

