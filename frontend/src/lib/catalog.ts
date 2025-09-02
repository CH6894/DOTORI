// src/lib/catalog.ts
import { MidCategoryMap } from '@/assets/MidCategoryMap'
import { fetchTitle } from '@/api/items'
import type { ItemDTO } from '@/types/item'
import type { CatalogItem } from '@/composables/useCatalog'

export function adaptItemDTOToCatalogItem(dto: ItemDTO, fallbackGenre?: string): CatalogItem {
  const base = import.meta.env.VITE_ASSET_BASE
  const img = dto.itemCode ? `${base}${dto.itemCode}.jpg` : undefined
  return {
    id: dto.itemCode,
    name: dto.name || dto.title,
    price: Number(dto.cost ?? 0),
    top_category: dto.genre || fallbackGenre || '',
    mid_category: dto.title,
    thumbWebp: img,
    thumbJpg: img,
  }
}

export async function fetchSixByGenre(genre: string): Promise<CatalogItem[]> {
  const titles = MidCategoryMap[genre] || []
  const NEED = 6
  if (!titles.length) return []

  const pickedTitles = Array.from({ length: NEED }, (_, i) => titles[i % titles.length])

  const results = await Promise.all(pickedTitles.map(async (title) => {
    try {
      const page = await fetchTitle(genre, title)
      const list = page?.content ?? []
      const preferred = list.find((it: ItemDTO) => typeof it.itemCode === 'string' && it.itemCode.endsWith('0001'))
      const chosen = preferred || list[0]
      return chosen ? adaptItemDTOToCatalogItem(chosen, genre) : null
    } catch {
      return null
    }
  }))

  return results.filter((v): v is CatalogItem => !!v)
}


