<!-- src/components/filters/TopTabs.vue -->
<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{ items: string[]; modelValue: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>()

// 키보드 핸들러를 tablist 컨테이너에만 바인딩
const listRef = ref<HTMLElement | null>(null)
function onKey(e: KeyboardEvent) {
  if (!listRef.value?.contains(e.target as Node)) return
  if (!props.items.length) return
  const idx = Math.max(0, props.items.indexOf(props.modelValue))
  if (e.key === 'ArrowRight') { e.preventDefault(); emit('update:modelValue', props.items[(idx + 1) % props.items.length]) }
  if (e.key === 'ArrowLeft') { e.preventDefault(); emit('update:modelValue', props.items[(idx - 1 + props.items.length) % props.items.length]) }
}
</script>

<template>
  <div class="top_category_list" @keydown="onKey">
    <ul ref="listRef" role="tablist" aria-label="상위 카테고리" class="top_category_ul">
      <li v-for="cat in items" :key="cat" class="top_category_item">
        <button role="tab" class="top_category_link" :class="{ selected: modelValue === cat }"
          :aria-selected="modelValue === cat" :tabindex="modelValue === cat ? 0 : -1" type="button"
          @click="emit('update:modelValue', cat)">
          <strong><span>{{ cat }}</span></strong>
        </button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.top_category_list {
  display: inline-block;
  background: transparent;
}

.top_category_ul {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  list-style: none;
  margin: 0;
  padding: 0px;
}

.top_category_item {
  flex: 0 0 auto;
  margin: 0;
  padding: 0;
}

.top_category_link {
  flex: 0 0 auto;
  white-space: nowrap;
  padding: 8px 16px;
  border: 1px solid #eadfc9;
  border-bottom: none;
  border-radius: 6px 6px 0 0;
  background: #F4F3E6;
  color: #6e6253;
  font-weight: 700;
  position: relative;
  z-index: 0;              
  color: #6e6253;
  padding: 8px 16px;
}

/* 선택 탭 */
.top_category_link.selected {
  padding: 8px 16px;
  background: #EFECC6;
  color: #670600;
  font-weight: 800;
  position: relative;
  z-index: 2;
}
</style>
