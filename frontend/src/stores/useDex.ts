// src/stores/useDex.ts
import { defineStore } from 'pinia'
import { http as axios } from '@/lib/http'
import { PREFIX, MAX_LOCAL, pad3, keyOf, type DexKey } from './dexKey'
import type { DexStateResponse, CollectionVerifyResponse } from '@/types/api'

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
const __FORCE_LOCAL_DUMMY__ = import.meta.env.VITE_USE_DUMMY_API === 'true'

/** 더미 인증 코드 매핑(고정 키) */
const DUMMY_MAP: Record<string, DexKey> = {
  // 포켓몬
  TEST1: keyOf(PREFIX.pokemon, 1),
  TEST2: keyOf(PREFIX.pokemon, 2),
  TEST3: keyOf(PREFIX.pokemon, 3),
  TEST4: keyOf(PREFIX.pokemon, 4),
  TEST5: keyOf(PREFIX.pokemon, 5),
  TEST6: keyOf(PREFIX.pokemon, 6),

  // 원피스(고정 샘플)
  OP001: keyOf(PREFIX.onepiece, 1),
  OP002: keyOf(PREFIX.onepiece, 2),
  OP003: keyOf(PREFIX.onepiece, 3),
}

/** (하위 호환) 귀멸 인증 허용 개수(로컬 한정) */
const KIMETSU_MAX = MAX_LOCAL.kimetsu

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
    localStorage.setItem(LS_KEY, JSON.stringify({ userId: state.userId, items: state.items }))
  } catch {}
}

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
      const verifiedSet = new Set(s.items.filter((i) => i.verified).map((i) => i.itemKey))
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
        const { data } = await axios.get<DexStateResponse>('/api/collection/dex-state')
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
        await axios.post('/api/collection/activate', { itemKey })
      } catch (e) {
        console.error('[dex] activate failed', e)
      }
    },

    /** ✅ 토글 활성화(해당 카드만). 인증된 항목은 꺼지지 않음 */
    async toggleActivate(itemKey: DexKey): Promise<void> {
      const cur = this.items.find((i) => i.itemKey === itemKey)
      const isVerified = !!cur?.verified
      const next = !cur?.activated
      if (isVerified && !next) return // 인증된 항목은 비활성화 불가

      this._upsert(itemKey, { activated: next })
      if (__FORCE_LOCAL_DUMMY__) return
      await axios.post('/api/collection/activate', { itemKey, activated: next })
    },

    /** ✅ (버튼용) 귀멸 로컬 인증: 1..KIMETSU_MAX 범위에서만 성공 */
    async verifyKimetsuLocal(id: number, max = KIMETSU_MAX): Promise<DexKey> {
      const n = Number(id)
      if (!Number.isFinite(n) || n < 1 || n > max) throw new Error('INVALID_RANGE')
      
      // 귀멸의 칼날 테스트 코드 형식으로 변환하여 인증
      const code = `KIMETSU${n}`
      return await this.verifyByCode(code)
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
            this._upsert(key, { verified: true, activated: true })
            return key
          }

          // 2) 공통 패턴 (POKEMON/PKM/KIMETSU/KIM/ONEPIECE/OP)
          //    예) POKEMON-7, PKM007, KIM_11, OP12, ONEPIECE001
          const m = normalized.match(/^(POKEMON|PKM|KIMETSU|KIM|ONEPIECE|OP)[-_]?(\d{1,3})$/)
          if (m) {
            const tag = m[1]
            const n = Number(m[2])
            if (!Number.isFinite(n) || n < 1) throw new Error('INVALID_RANGE')

            let prefix = ''
            let max = Infinity
            if (tag === 'POKEMON' || tag === 'PKM')     { prefix = PREFIX.pokemon;  max = MAX_LOCAL.pokemon }
            if (tag === 'KIMETSU' || tag === 'KIM')     { prefix = PREFIX.kimetsu;  max = MAX_LOCAL.kimetsu }
            if (tag === 'ONEPIECE' || tag === 'OP')     { prefix = PREFIX.onepiece; max = MAX_LOCAL.onepiece }

            if (n > max) throw new Error('INVALID_RANGE')
            const key = keyOf(prefix, n)
            this._upsert(key, { verified: true, activated: true })
            return key
          }

          // 매칭 실패
          throw new Error('INVALID_CODE')
        }

        // 실제 서버 사용 시
        const { data } = await axios.post<CollectionVerifyResponse>(
          '/api/collection/verify-code',
          { code }
        )
        if (!data?.ok || !data?.itemKey) throw new Error('INVALID_CODE')

        const key = data.itemKey as DexKey
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
      this.items = this.items.filter((it) => valid.has(it.itemKey))
      const diff = before - this.items.length
      if (diff > 0) console.info(`[dex] purged ${diff} invalid items`)
      if (__FORCE_LOCAL_DUMMY__) saveToLS(this.$state)
    },
  },
})
