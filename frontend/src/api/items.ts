// src/api/items.ts
// rest api에서 물건 리스트 불러오기
import openApi from './axiosPublic'
import type { Page, PageParams } from '@/types/common'
import type { ItemDTO } from '@/types/item'
import type { PriceDTO } from '@/types/price'

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

// 검색어로 아이템 찾기
export const searchItems = (query: string, params?: PageParams) =>
    openApi.get<Page<ItemDTO>>('/open/items/search', { 
        params: { q: query, ...params } 
    }).then(r => r.data)

// ItemDetails의 모든 이미지 조회 (관리자 이미지 포함)
export const fetchAllImagesByItemDetailsId = (itemDetailsId: number) =>
    openApi.get<string[]>(`/open/items/item-details/${itemDetailsId}/images`).then(r => r.data)

// ItemDetails의 관리자 이미지만 조회
export const fetchAdminImagesByItemDetailsId = (itemDetailsId: number) =>
    openApi.get<string[]>(`/open/items/item-details/${itemDetailsId}/admin-images`).then(r => r.data)

// ItemDetails의 판매자 이미지만 조회
export const fetchSellerImagesByItemDetailsId = (itemDetailsId: number) =>
    openApi.get<string[]>(`/open/items/item-details/${itemDetailsId}/seller-images`).then(r => r.data)

// 이미지 정보 타입 정의
export interface ImageInfo {
    url: string
    type: string // "admin" 또는 "seller"
    typeLabel: string // "관리자" 또는 "판매자"
}

// 모든 이미지 정보 조회 (관리자 이미지 우선, 판매자 이미지 후순위)
export const fetchAllImageInfosByItemDetailsId = (itemDetailsId: number) =>
    openApi.get<ImageInfo[]>(`/open/items/item-details/${itemDetailsId}/image-infos`).then(r => r.data)

export const fetchPriceHistory = (itemDetailsId: number, from?: string, to?: string) =>
    openApi.get<PriceDTO[]>(`/open/items/${itemDetailsId}/price`, {params: {from, to}}).then(r => r.data)

export const fetchPriceHistoryByPeriod = (itemCode: string, period: string) =>
    openApi.get<PriceDTO[]>(`/open/items/${itemCode}/price-history/${period}`)
    .then(r => r.data)
