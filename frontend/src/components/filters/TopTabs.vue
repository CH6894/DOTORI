<!-- src/components/filters/TopTabs.vue -->
<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps<{ items: string[]; modelValue: string }>()
const emit  = defineEmits<{ (e:'update:modelValue', v:string): void }>()

const activeIndex = computed(() => props.items.indexOf(props.modelValue))
function select(i: number) { if (i >= 0) emit('update:modelValue', props.items[i]) }

// 키보드(좌우 이동)
function onKey(e: KeyboardEvent) {
  if (props.items.length === 0) return
  const idx = activeIndex.value < 0 ? 0 : activeIndex.value
  if (e.key === 'ArrowRight') { e.preventDefault(); select((idx + 1) % props.items.length) }
  if (e.key === 'ArrowLeft')  { e.preventDefault(); select((idx - 1 + props.items.length) % props.items.length) }
}
onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))
</script>

<template>
  <div class="top_category_list">
    <ul role="tablist" aria-label="상위 카테고리" class="flex flex-wrap gap-1 list-none p-0 m-0">
      <li v-for="(cat, i) in items" :key="cat">
        <button
          role="tab"
          class="top_category_link"
          :class="{ selected: modelValue === cat }"
          :aria-selected="modelValue === cat"
          :tabindex="modelValue === cat ? 0 : -1"
          type="button"
          @click="emit('update:modelValue', cat)"
        >
          <strong><span>{{ cat }}</span></strong>
        </button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.top_category_link{
  border: 0;
  color: #670600;
  cursor: pointer;
  padding: 10px 20px;
  border-top-left-radius: 10px; border-top-right-radius: 10px;
  background: #F4F3E6;
}
.top_category_link.selected{ background:#EFECC6; }
</style>
