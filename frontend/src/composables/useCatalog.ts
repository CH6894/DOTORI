// src/composables/useCatalog.ts
import { computed, type Ref } from 'vue'

export type CatalogItem = {
  id: number | string
  name: string
  price: number
  top_category: string
  mid_category?: string
  thumbWebp?: string
  thumbJpg?: string
}

export function useCatalog(all: CatalogItem[], top: Ref<string>, mid: Ref<string>) {
  const filtered = computed(() => {
    const src = Array.isArray(all) ? all : []
    let list = src.filter(i => i?.top_category === top.value)
    if (mid.value) list = list.filter(i => i?.mid_category === mid.value)
    return list
  })
  return { filtered }
}

