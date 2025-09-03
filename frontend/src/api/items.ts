// src/api/items.ts
// rest api에서 물건 리스트 불러오기
import openApi from './axiosPublic'
import type { Page, PageParams } from '@/types/common'
import type { ItemDTO } from '@/types/item'

// 전체 아이템 목록 조회
export const fetchItems = (params?: PageParams) =>
    openApi.get<Page<ItemDTO>>('/open/items', { params }).then(r => r.data)

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

// 승인된 미개봉 상품의 ItemDetails 조회
export const fetchApprovedUnpackedItemDetails = (itemCode: string) =>
    openApi.get(`/open/items/${itemCode}/approved-unpacked-details`).then(r => r.data)

// 승인된 개봉 상품의 ItemDetails 조회
export const fetchApprovedOpenedItemDetails = (itemCode: string) =>
    openApi.get(`/open/items/${itemCode}/approved-opened-details`).then(r => r.data)
