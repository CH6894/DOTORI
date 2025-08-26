// src/api/items.js
import api from './axios'

// 목록(페이징)
export async function fetchItems({ page = 0, size = 10, sort } = {}) {
    const { data } = await api.get('/api/items', {
        params: { page, size, ...(sort ? { sort } : {}) },
    })
    return data                // { content, totalPages, ... } (Spring Data Page)
}

// 단건 조회
export async function fetchItem(id) {
    const { data } = await api.get(`/api/items/${id}`)
    return data
}

// (선택) 생성/수정/삭제가 필요해지면 아래처럼 추가
// export async function createItem(payload) {
//   const { data } = await api.post('/api/items', payload)
//   return data
// }
// export async function updateItem(id, payload) {
//   const { data } = await api.put(`/api/items/${id}`, payload)
//   return data
// }
// export async function deleteItem(id) {
//   await api.delete(`/api/items/${id}`)
// }
