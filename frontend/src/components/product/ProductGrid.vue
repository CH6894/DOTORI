<!-- src/components/product/ProductGrid.vue -->
<script setup lang="ts">
import ProductCard from './ProductCard.vue'
import type { Item } from '@/composables/useCatalog'

const props = defineProps<{ items: Item[] }>()
</script>

<template>
  <div class="grid grid-cols-6 gap-4 justify-items-center">
    <ProductCard v-for="it in props.items" :key="it.id" :item="it" />
    <div v-if="!props.items.length" class="empty" role="status">검색 결과가 없습니다.</div>
  </div>
</template>

<style scoped>
.empty {
  grid-column: 1 / -1; text-align: center; padding: 36px 0;
  color: #9a9a9a; border: 1px dashed #e5e5e5; border-radius: 12px;
}
</style>
