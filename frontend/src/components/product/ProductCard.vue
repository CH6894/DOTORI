<!-- src/components/product/ProductCard.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import type { Item } from '@/composables/useCatalog'
import { RouterLink } from 'vue-router'

const props = defineProps<{ item: Item }>()
const to = computed(() => ({ name: 'product', params: { id: String(props.item.id) } }))
</script>

<template>
  <div class="product_item">
    <RouterLink class="product_item_link" :to="to" :aria-label="`${item.name} 상세`">
      <picture>
        <source :srcset="item.thumbWebp || '/img/placeholder.webp'" type="image/webp" />
        <img
          :src="item.thumbJpg || '/img/placeholder.jpg'"
          :alt="`${item.name} 이미지`"
          width="200" height="200"
          loading="lazy"
          style="object-fit:cover"
        />
      </picture>
    </RouterLink>
    <div class="product_item_info">
      <RouterLink class="product_item_title" :to="to">
        <span>{{ item.name }}</span>
      </RouterLink>
      <ul class="price">
        <li class="price"><span>{{ item.price.toLocaleString() }} 원</span></li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.product_item { margin:10px 10px 10px 0; }
.product_item_info{ margin-top:2px; height:60px; }
.price { padding:0; margin:0; }
.price li.price { list-style:none; font-size:14px; color:#333; margin-top:5px; }
a { text-decoration:none; color:inherit; }         /* 요구사항 */
a:visited { text-decoration:none; color:inherit; } /* 요구사항 */
</style>
