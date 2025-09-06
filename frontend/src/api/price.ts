// src/api/price.ts
import openApi from './axiosPublic'
import type { PriceHistoryResponse } from '@/types/price';

export const fetchPriceHistoryPreset = (itemCode: string) => 
    openApi.get<PriceHistoryResponse>(`/open/items/code/${itemCode}/price-history/1M?period=1M`).then(r=>r.data)
