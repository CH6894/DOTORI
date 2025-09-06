// src/types/price.ts
// PriceDTO 타입 정의(api 원본 타입)
export interface PriceDTO {
    itemId: number
    price: number
    payTime: string
}
// 시세 그래프에 보여주기 위한 데이터 형태
export type PricePoint = {
    time: string | number | Date
    price: number
}

// 서버가 period별로 나눠 내려주지 않는다면 단일 배열 반환 가정
export type PriceHistoryResponse = PriceDTO[];

// 서버가 preset별로 내려주는 경우(예: { "1M": [...], "3M": [...] } )
export type PriceHistoryPresetResponse = Record<"1M" | "3M" | "6M" | "1Y", PricePoint[]>;
