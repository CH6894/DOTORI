import { NumberLiteralType } from "typescript"

export interface Item{
  itemCode: string 
  title: string
  cost?: number
  genre?: string
  size?: string
  manufacturer?: string
  material?: string
  information?: string
  releaseDate?: string
  ean?: string
}

export interface PageResponse<T> {
    content: T[]
    page: number
    size: number
    totalElements: number
    totalPages: number
    last: boolean
}