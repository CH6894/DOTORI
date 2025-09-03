import { http } from '@/lib/http'

export const WishAPI = {
  add: (itemId: number) => http.post('/api/wish', { itemId }),
  remove: (itemId: number) => http.delete('/api/wish', { data: { itemId } }),
  list: () => http.get('/api/wish')
}
