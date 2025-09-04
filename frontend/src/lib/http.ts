// src/lib/http.ts
import axios from 'axios'

export const http = axios.create()

/**
 * 개발 모드에서는 /api/dex/* 를 전부 목킹해서
 * 절대 네트워크를 타지 않게 한다.
 */
if (import.meta.env.DEV) {
  const DUMMY_MAP: Record<string, string> = {
    TEST123: 'game:pokemon:001',
    TEST456: 'game:pokemon:002',
    TEST789: 'game:pokemon:003',
  }

  // 요청 인터셉트: verify/state/activate 엔드포인트를 가로채서
  // "에러 대신" 우리가 만든 data를 반환하게 만든다.
  http.interceptors.request.use((config) => {
    const u = (config.url || '').toString()
    const m = (config.method || 'get').toLowerCase()

    // verify
    if (m === 'post' && u === '/api/dex/verify') {
      const raw = (config.data as any) || {}
      const normalized = String(raw.code ?? '').trim().toUpperCase()
      const itemKey = DUMMY_MAP[normalized] ?? 'game:pokemon:001'
      // 응답 인터셉터에서 분기하도록 특수 플래그 첨부
      ;(config as any).__MOCK__ = { data: { ok: true, itemKey } }
    }

    // state
    if (m === 'get' && u === '/api/dex/state') {
      const saved = JSON.parse(localStorage.getItem('dex_state_v1') || '{}')
      const data = {
        userId: saved?.userId ?? 'dev-user',
        items: Array.isArray(saved?.items) ? saved.items : [],
      }
      ;(config as any).__MOCK__ = { data }
    }

    // activate
    if (m === 'post' && u === '/api/dex/activate') {
      ;(config as any).__MOCK__ = { data: { ok: true } }
    }

    return config
  })

  // 응답 인터셉트: __MOCK__이 있으면 실제 네트워크를 호출하지 않고 우리가 만든 응답을 반환
  http.interceptors.response.use(
    (resp) => {
      const mock = (resp.config as any).__MOCK__
      return mock ? { ...resp, data: mock.data } : resp
    },
    (err) => {
      const mock = err?.config && (err.config as any).__MOCK__
      if (mock) return { data: mock.data }
      return Promise.reject(err)
    }
  )
}
