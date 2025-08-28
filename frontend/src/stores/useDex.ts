// src/stores/useDex.ts
import { defineStore } from 'pinia'
import { http as axios } from '@/lib/http'

/** 공통 키 포맷: category:sub:id (예: game:pokemon:001) */
export type DexKey = string

export type UserItemState = {
  itemKey: DexKey
  activated: boolean
  verified: boolean
}

type DexState = {
  userId: string
  items: UserItemState[]
  loading: boolean
}

/** ★ 개발용: 백엔드 없이 동작 (네트워크 호출 X) */
const __FORCE_LOCAL_DUMMY__ = true

/** 더미 인증 코드 매핑(고정 키) */
const DUMMY_MAP: Record<string, DexKey> = {
  TEST1: 'game:pokemon:001',
  TEST2: 'game:pokemon:002',
  TEST3: 'game:pokemon:003',
  TEST4: 'game:pokemon:004',
  TEST5: 'game:pokemon:005',
  TEST6: 'game:pokemon:006',
}

/** 귀멸 인증 허용 개수(로컬 한정) */
const KIMETSU_MAX = 11

/** 로컬 스토리지 키 */
const LS_KEY = 'dex_state_v1'

function loadFromLS(): Partial<DexState> | null {
  try {
    const raw = localStorage.getItem(LS_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}
function saveToLS(state: DexState) {
  try {
    localStorage.setItem(
      LS_KEY,
      JSON.stringify({ userId: state.userId, items: state.items })
    )
  } catch {}
}
const pad3 = (n: number | string) => String(n).padStart(3, '0')

export const useDex = defineStore('dex', {
  state: (): DexState => ({
    userId: '',
    items: [],
    loading: false,
  }),

  getters: {
    /** 개별 키 활성여부 */
    isActivated: (s) => (key: DexKey): boolean =>
      !!s.items.find((i) => i.itemKey === key)?.activated,

    /** 개별 키 인증여부 */
    isVerified: (s) => (key: DexKey): boolean =>
      !!s.items.find((i) => i.itemKey === key)?.verified,

    /** 주어진 키 배열 기준 완성도(%) */
    completionByKeys: (s) => (keys: DexKey[]): number => {
      const total = keys.length || 1
      const verifiedSet = new Set(
        s.items.filter((i) => i.verified).map((i) => i.itemKey)
      )
      let verified = 0
      for (const k of keys) if (verifiedSet.has(k)) verified++
      return Math.round((verified / total) * 100)
    },
  },

  actions: {
    /** 내부 upsert + 저장 */
    _upsert(key: DexKey, patch: Partial<UserItemState>) {
      const cur = this.items.find((i) => i.itemKey === key)
      if (cur) {
        Object.assign(cur, patch)
        /** ★ 불변식: 인증된 항목은 항상 활성 상태 */
        if (patch.verified || cur.verified) {
          cur.verified = true
          cur.activated = true
        }
      } else {
        const verified = !!patch.verified
        this.items.push({
          itemKey: key,
          verified,
          /** ★ verified면 activated도 true 로 고정 */
          activated: verified ? true : !!patch.activated,
        })
      }
      if (__FORCE_LOCAL_DUMMY__) saveToLS(this.$state)
    },

    /** 상태 초기화/로드 */
    async fetchState(): Promise<void> {
      try {
        this.loading = true

        if (__FORCE_LOCAL_DUMMY__) {
          const saved = loadFromLS()
          this.userId = saved?.userId ?? 'dev-user'
          this.items = Array.isArray(saved?.items) ? saved!.items! : []
          return
        }

        // 실제 서버 사용 시
        const { data } = await axios.get<{
          userId: string
          items: UserItemState[]
        }>('/api/dex/state')
        this.userId = data?.userId ?? ''
        this.items = Array.isArray(data?.items) ? data.items : []
      } catch (e) {
        console.error('[dex] fetchState failed', e)
        if (!__FORCE_LOCAL_DUMMY__) {
          this.userId = ''
          this.items = []
        }
      } finally {
        this.loading = false
      }
    },

    /** 단방향 활성화(기존 유지) */
    async activate(itemKey: DexKey): Promise<void> {
      try {
        this._upsert(itemKey, { activated: true })
        if (__FORCE_LOCAL_DUMMY__) return
        await axios.post('/api/dex/activate', { itemKey })
      } catch (e) {
        console.error('[dex] activate failed', e)
      }
    },

    /** ✅ 토글 활성화(해당 카드만). 인증된 항목은 꺼지지 않음 */
    async toggleActivate(itemKey: DexKey): Promise<void> {
      const cur = this.items.find((i) => i.itemKey === itemKey)
      const isVerified = !!cur?.verified
      const next = !cur?.activated

      /** ★ 인증된 항목은 비활성화 불가 → 조용히 무시(필요시 UI 토스트) */
      if (isVerified && !next) return

      this._upsert(itemKey, { activated: next })
      if (__FORCE_LOCAL_DUMMY__) return
      await axios.post('/api/dex/activate', { itemKey, activated: next })
    },

    /** ✅ (버튼용) 귀멸 로컬 인증: 1..KIMETSU_MAX 범위에서만 성공 */
    async verifyKimetsuLocal(id: number, max = KIMETSU_MAX): Promise<DexKey> {
      const n = Number(id)
      if (!Number.isFinite(n) || n < 1 || n > max) {
        throw new Error('INVALID_RANGE')
      }
      const key = `animation:kimetsu:${pad3(n)}` as DexKey
      /** ★ 인증과 동시에 활성화 */
      this._upsert(key, { verified: true, activated: true })
      if (!__FORCE_LOCAL_DUMMY__) {
        await axios.post('/api/dex/verify-kimetsu', { itemKey: key })
      }
      return key
    },

    /** 인증코드 검증 → 해당 itemKey 인증 (로컬도 범위 체크) */
    async verifyByCode(code: string): Promise<DexKey> {
      try {
        this.loading = true
        const normalized = code.trim().toUpperCase()

        if (__FORCE_LOCAL_DUMMY__) {
          // 1) 고정 더미 맵
          if (DUMMY_MAP[normalized]) {
            const key = DUMMY_MAP[normalized]
            /** ★ 인증과 동시에 활성화 */
            this._upsert(key, { verified: true, activated: true })
            return key
          }

          // 2) 귀멸 패턴: KIMETSU### (1..KIMETSU_MAX만 허용)
          const mK = normalized.match(/^KIMETSU[-_]?(\d{1,3})$/)
          if (mK) {
            const n = Number(mK[1])
            if (Number.isFinite(n) && n >= 1 && n <= KIMETSU_MAX) {
              const key = `animation:kimetsu:${pad3(n)}` as DexKey
              /** ★ 인증과 동시에 활성화 */
              this._upsert(key, { verified: true, activated: true })
              return key
            }
            throw new Error('INVALID_RANGE')
          }

          // (포켓몬 패턴 필요시 여기에 추가)
          // const mP = normalized.match(/^POKEMON[-_]?(\d{1,3})$/)
          // if (mP) { ... }

          // 매칭 실패
          throw new Error('INVALID_CODE')
        }

        // 실제 서버 사용 시
        const { data } = await axios.post<{ ok: boolean; itemKey: DexKey }>(
          '/api/dex/verify',
          { code }
        )
        if (!data?.ok || !data?.itemKey) throw new Error('INVALID_CODE')

        /** ★ 인증과 동시에 활성화 */
        const key = data.itemKey
        this._upsert(key, { verified: true, activated: true })
        return key
      } catch (e) {
        console.error('[dex] verifyByCode failed', e)
        throw e
      } finally {
        this.loading = false
      }
    },

    /** (옵션) 현재 뷰에서 유효한 키 목록만 남기고 정리 */
    purgeInvalid(validKeys: DexKey[]) {
      const valid = new Set(validKeys)
      const before = this.items.length
      this.items = this.items.filter((it) =>
        valid.has(it.itemKey) ? true : !it.itemKey.startsWith('animation:kimetsu:')
      )
      const diff = before - this.items.length
      if (diff > 0) console.info(`[dex] purged ${diff} invalid items`)
      if (__FORCE_LOCAL_DUMMY__) saveToLS(this.$state)
    },
  },
})
