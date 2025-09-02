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
  
  // 가격을 숫자로 정규화 (문자열에서 "원" 제거)
  let price = item.price ?? 0
  if (typeof price === 'string') {
    // "원" 단위 제거하고 숫자로 변환
    price = Number(price.replace(/[원,]/g, '')) || 0
  } else {
    price = Number(price) || 0
  }
  
  return {
    id: item.id as WishId,
    title: item.title ?? item.name ?? '',
    price: price,
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
        console.log('위시리스트 로드:', this.items.length, '개 아이템')
      } catch (error) {
        console.error('위시리스트 로드 실패:', error)
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
      console.log('위시리스트 추가:', w, '현재 개수:', this.items.length)
    },

    remove(id: MaybeId) {
      if (id == null) return
      const beforeCount = this.items.length
      this.items = this.items.filter(i => normId(i.id) !== normId(id))
      this.persist()
      console.log('위시리스트 제거:', id, '이전 개수:', beforeCount, '현재 개수:', this.items.length)
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

    // (선택) 서버 동기화 예시
    // async syncFromServer(fetchFn: () => Promise<any[]>) {
    //   const serverList = await fetchFn()
    //   this.replace(serverList)
    // },
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
