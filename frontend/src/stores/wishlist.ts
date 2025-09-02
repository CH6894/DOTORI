// src/stores/wishlist.ts
import { defineStore } from 'pinia'

export type WishId = string | number
export type MaybeId = WishId | null | undefined

export type WishItem = {
  id: WishId
  title: string
  price: number
  image?: string
  // 필요한 메타가 있으면 자유롭게 추가 (e.g. sellerId, sku 등)
  [key: string]: any
}

const LS_WISHLIST_KEY = 'dotori_wishlist_v1'

function isValidId(id: any): id is WishId {
  return id !== null && id !== undefined && (typeof id === 'string' || typeof id === 'number')
}

function normId(id: MaybeId) {
  return id == null ? '' : String(id)
}

function normalize(item: any): WishItem | null {
  // id가 없으면 스킵(런타임 에러 방지)
  if (!isValidId(item?.id)) return null
  return {
    id: item.id as WishId,
    title: item.title ?? item.name ?? '',
    price: Number(item.price ?? 0),
    image: item.image ?? item.thumb ?? item.images?.[0],
    ...item,
  }
}

export const useWishlistStore = defineStore('wishlist', {
  state: () => ({
    items: [] as WishItem[],
    _loaded: false,
  }),

  getters: {
    count: (s) => s.items.length,
    // ✅ undefined/null도 안전하게 받을 수 있게 완화
    has: (s) => (id: MaybeId) =>
      id != null && s.items.some(i => normId(i.id) === normId(id)),
    byId: (s) => (id: MaybeId) =>
      id != null ? s.items.find(i => normId(i.id) === normId(id)) : undefined,
  },

  actions: {
    load() {
      if (this._loaded) return
      try {
        const raw = localStorage.getItem(LS_WISHLIST_KEY)
        const arr: any[] = raw ? JSON.parse(raw) : []
        this.items = arr
          .map(normalize)
          .filter(Boolean) as WishItem[]
      } catch {
        this.items = []
      } finally {
        this._loaded = true
      }
    },

    persist() {
      try {
        localStorage.setItem(LS_WISHLIST_KEY, JSON.stringify(this.items))
      } catch {
        /* noop */
      }
    },

    add(item: any) {
      const w = normalize(item)
      if (!w) return
      if (this.has(w.id)) return
      this.items.push(w)
      this.persist()
    },

    remove(id: MaybeId) {
      if (id == null) return
      this.items = this.items.filter(i => normId(i.id) !== normId(id))
      this.persist()
    },

    toggle(item: any) {
      const id = item?.id as MaybeId
      if (!isValidId(id)) return
      this.has(id) ? this.remove(id) : this.add(item)
    },

    replace(list: any[]) {
      this.items = (list || [])
        .map(normalize)
        .filter(Boolean) as WishItem[]
      this.persist()
    },

    clear() {
      this.items = []
      this.persist()
    },

    // 서버에서 위시리스트 동기화
    async syncFromServer() {
      try {
        const token = localStorage.getItem('auth_token')
        if (!token) return

        const response = await fetch('/api/wishlist', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })

        if (response.ok) {
          const serverWishlist = await response.json()
          // 서버 데이터를 로컬에 동기화
          this.replace(serverWishlist)
        }
      } catch (error) {
        console.error('서버 동기화 실패:', error)
      }
    },

    // (선택) 서버 동기화 예시
    // async syncAddToServer(postFn: (item:any) => Promise<void>, item:any) {
    //   await postFn(item)
    //   this.add(item)
    // },
    // async syncRemoveFromServer(delFn: (id:WishId) => Promise<void>, id:WishId) {
    //   await delFn(id)
    //   this.remove(id)
    // },
  },
})
