// src/api/items.ts
// rest api에서 물건 리스트 불러오기
import openApi from './axiosPublic'
import type { Page, PageParams } from '@/types/common'
import type { Item as ItemDTO } from '@/types/item'

// 탑 카테고리로 아이템 찾기
export const fetchGenre = (genre : string, params?: PageParams) =>
    openApi.get<Page<ItemDTO>>(`/open/items/genre/${genre}`).then(r => r.data)
// axios 응답 객체의 구조 : { data, status, headers, ..}
// r.data => axios 응답 객체의 data를 받아오겠다는 의미

// 타이틀로 아이템 찾기
export const fetchTitle = (genre : string, title: string, params?: PageParams) =>
    openApi.get<Page<ItemDTO>>(`/open/items/genre/${genre}/title/${title}`).then(r => r.data)

// 아이템 상세 페이지
export const fetchItemById = (id : string) =>
    openApi.get<ItemDTO>(`/open/items/${id}`).then(r => r.data)