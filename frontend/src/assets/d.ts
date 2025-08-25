// src/types/assets.d.ts
declare module '@/assets/TopCategoryData.js' {
  export const TopCategoryData: string[]
  export default TopCategoryData
}

declare module '@/assets/MidCategoryData.js' {
  export const MidCategoryData: string[]
  export default MidCategoryData
}

declare module '@/assets/ItemData.js' {
  export type Item = {
    id: number | string
    name: string
    price: number
    top_category: string
    mid_category?: string
    thumbWebp?: string
    thumbJpg?: string
  }
  export const ItemData: Item[]
  export default ItemData
}
