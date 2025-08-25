import api from "@/lib/api"
import type { PageResponse } from "@/types/item"
import type { Item } from "@/types/item"

export async function fetchItems(params:{
    genre: string
    manufacturer: string
    page?: number
    size?: number
}) {
    const {genre, manufacturer = "", page = 0, size = 20 } = params
    const res = await api.get<PageResponse<Item>>("/api/items",{
        params: {genre, manufacturer, page, size}
    })
    return res.data
}