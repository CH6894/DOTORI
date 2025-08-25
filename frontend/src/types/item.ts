import { NumberLiteralType } from "typescript"

export interface Item{
    ean: string
    name: string
    title: string
    manufacturer: string | null
    texture: string | null
    size: string
    imgUrl: string | null
    storageFees: number
    genre: string
}

export interface PageResponse<T> {
    content: T[]
    page: number
    size: number
    totalElements: number
    totalPages: number
    last: boolean
}