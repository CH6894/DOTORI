// src/stores/wishlist.ts
import { defineStore } from 'pinia'
import { WishAPI } from '@/api/wishlist'
import { useAuthStore } from '@/stores/auth'

export type WishId = string | number
export type MaybeId = WishId | null | undefined

export type WishItem = {
  wishListId: number
  itemId: number
  name: string
  itemCode: string
  title: string
  price: number
  image: string
  unpacked: boolean
  status: boolean
  [key: string]: any
}

function isValidId(id: any): id is WishId {
  return id !== null && id !== undefined && (typeof id === 'string' || typeof id === 'number')
}

function normId(id: MaybeId) {
  return id == null ? '' : String(id)
}

export const useWishlistStore = defineStore('wishlist', {
  state: () => ({
    items: [] as WishItem[],
    _loaded: false,
  }),

  getters: {
    count: (s) => s.items.length,
    has: (s) => (itemId: MaybeId) =>
      itemId != null && s.items.some(i => i.itemId === Number(itemId)),
    byItemId: (s) => (itemId: MaybeId) =>
      itemId != null ? s.items.find(i => i.itemId === Number(itemId)) : undefined,
  },

  actions: {
    async load() {
      if (this._loaded) return
      const auth = useAuthStore()
      if (!auth.isAuthed) {
        console.log('로그인하지 않음 - 위시리스트 로드 건너뜀')
        this.items = []
        this._loaded = true
        return
      }
      try {
        const response = await WishAPI.list()
        this.items = response.data || []
        console.log('위시리스트 로드:', this.items.length, '개 아이템')
      } catch (error) {
        console.error('위시리스트 로드 실패:', error)
        this.items = []
      } finally {
        this._loaded = true
      }
    },

    async add(itemId: number) {
      const auth = useAuthStore()
      if (!auth.isAuthed) {
        console.log('로그인하지 않음 - 위시리스트 추가 불가')
        throw new Error('로그인이 필요합니다')
      }
      try {
        await WishAPI.add(itemId)
        // 서버에서 성공하면 위시리스트를 다시 로드하여 최신 데이터 가져오기
        this._loaded = false
        await this.load()
        console.log('위시리스트 추가:', itemId, '현재 개수:', this.items.length)
      } catch (error) {
        console.error('위시리스트 추가 실패:', error)
        throw error
      }
    },

    async remove(itemId: number) {
      const auth = useAuthStore()
      if (!auth.isAuthed) {
        console.log('로그인하지 않음 - 위시리스트 제거 불가')
        throw new Error('로그인이 필요합니다')
      }
      try {
        await WishAPI.remove(itemId)
        // 서버에서 성공하면 위시리스트를 다시 로드하여 최신 데이터 가져오기
        this._loaded = false
        await this.load()
        console.log('위시리스트 제거:', itemId, '현재 개수:', this.items.length)
      } catch (error) {
        console.error('위시리스트 제거 실패:', error)
        throw error
      }
    },

    async toggle(itemId: number) {
      if (!isValidId(itemId)) return
      try {
        if (this.has(itemId)) {
          await this.remove(itemId)
        } else {
          await this.add(itemId)
        }
      } catch (error) {
        console.error('위시리스트 토글 실패:', error)
        throw error
      }
    },

    replace(list: any[]) {
      this.items = Array.isArray(list) ? list : []
    },

    clear() {
      this.items = []
    }
  },
})