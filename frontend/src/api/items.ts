// src/api/items.js
// rest api에서 물건 리스트 불러오기
import { http as openApi } from '@/lib/http'

export const fetchItems = (params) =>
    openApi.get('/open/items', { params }).then(r => r.data)
    // axios 응답 객체의 구조 : { data, status, headers, ..}
    // r.data => axios 응답 객체의 data를 받아오겠다는 의미

export const fetchItemById = (id) =>
    openApi.get(`/open/items/${id}`).then(r => r.data)
