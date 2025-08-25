<!-- src/components/filters/SortBar.vue -->
<script setup lang="ts">
const props = defineProps<{
  modelValue: 'priceAsc' | 'priceDesc' | 'popularity' | 'sales'
}>()
const emit = defineEmits<{ (e:'update:modelValue', v:'priceAsc'|'priceDesc'|'popularity'|'sales'): void }>()
const set = (v: 'priceAsc'|'priceDesc'|'popularity'|'sales') => emit('update:modelValue', v)
</script>

<template>
  <div class="sortbar" role="radiogroup" aria-label="정렬">
    <button
      class="sortbtn" role="radio" :aria-checked="modelValue==='priceDesc'"
      @click="set('priceDesc')">가격높은순</button>
    <button
      class="sortbtn" role="radio" :aria-checked="modelValue==='priceAsc'"
      @click="set('priceAsc')">가격낮은순</button>
    <span class="sep" aria-hidden="true">|</span>
    <button
      class="sortbtn" role="radio" :aria-checked="modelValue==='popularity'"
      @click="set('popularity')">인기순</button>
    <button
      class="sortbtn" role="radio" :aria-checked="modelValue==='sales'"
      @click="set('sales')">판매순</button>
  </div>
</template>

<style scoped>
.sortbar{
  display:flex; align-items:center; gap:10px;
  padding:8px 10px;
  background:#EFECC6;          /* 미들탭 박스와 자연스럽게 연결 */
  border:1px solid #eadfc9; border-top:none; border-radius:0 6px 6px 6px;
  margin-top:-1px;             /* 미세 접합 */
}
.sortbtn{
  appearance:none; border:1px solid #eadfc9; background:#fff7ea;
  padding:6px 10px; border-radius:999px; cursor:pointer; font-weight:700; color:#6e6253;
}
.sortbtn[aria-checked="true"]{ background:#ffeccc; color:#670600; }
.sortbtn:focus-visible{ outline:2px dashed #a83b32; outline-offset:2px; }
.sep{ opacity:.4; }
</style>
