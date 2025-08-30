// Spring Data Page<T> 응답 형식에 맞춘 제네릭 타입
// 상품 리스트, 유저 리스트, 주문 리스트 등을 쓸 때 이 객체를 부르기
// import type {Page} from '@/types/common'으로 부르기.
// 사용 방법 : openApi.get<Page<불러온 데이터>>('/주소/주소') 이런 식
export type Page<T> = {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;   // 현재 페이지(0-based)
    size: number;     // 요청한 페이지 크기
    first: boolean;
    last: boolean;
};

export type PageParams = {
    page?: number; 
    size?: number; 
    sort?: string 
};

