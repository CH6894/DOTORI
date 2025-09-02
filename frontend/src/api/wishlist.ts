import api from './axios.js'

export interface WishlistItem {
  wishlistId: number
  itemId: number
  itemImg: string
  name: string
  cost: number
}

export interface WishlistResponse {
  success: boolean
  wishlistId?: number
  isLiked?: boolean
  error?: string
}

/**
 * 위시리스트 목록 조회
 */
export const getWishlist = async (): Promise<WishlistItem[]> => {
  try {
    const response = await api.get('/wishlist')
    return response.data || []
  } catch (error) {
    console.error('위시리스트 조회 실패:', error)
    throw error
  }
}

/**
 * 위시리스트에 상품 추가/제거 (토글)
 */
export const toggleWishlistItem = async (itemId: number): Promise<WishlistResponse> => {
  try {
    const response = await api.post('/wishlist', { itemId })
    return response.data
  } catch (error) {
    console.error('위시리스트 토글 실패:', error)
    throw error
  }
}

/**
 * 위시리스트에서 상품 제거
 */
export const removeWishlistItem = async (wishlistId: number): Promise<WishlistResponse> => {
  try {
    const response = await api.delete(`/wishlist/${wishlistId}`)
    return response.data
  } catch (error) {
    console.error('위시리스트 제거 실패:', error)
    throw error
  }
}

