import api from '@/api/axios'

export const WishAPI = {
  add: (itemId: number) => api.post('/wish', { itemId }),
  remove: (itemId: number) => api.delete('/wish', { data: { itemId } }),
  list: () => api.get('/wish')
}
