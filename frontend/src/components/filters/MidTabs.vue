<!-- src/components/filters/MidTabs.vue -->
<script setup lang="ts">
const props = defineProps<{ items: string[]; modelValue: string; visible: boolean }>()
const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>()
</script>

<template>
  <div v-if="visible" class="mid_category_list">
    <ul role="tablist" aria-label="중간 카테고리" class="mid_ul">
      <li class="mid_item">
        <button role="tab" class="mid_category_link" :class="{ selected: modelValue === '' }"
          :aria-selected="modelValue === ''" :tabindex="modelValue === '' ? 0 : -1" type="button"
          @click="emit('update:modelValue', '')">
          <strong><span>전체</span></strong>
        </button>
      </li>

      <li v-for="mid in items" :key="mid" class="mid_item">
        <button role="tab" class="mid_category_link" :class="{ selected: modelValue === mid }"
          :aria-selected="modelValue === mid" :tabindex="modelValue === mid ? 0 : -1" type="button"
          @click="emit('update:modelValue', mid)">
          <strong><span>{{ mid }}</span></strong>
        </button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.mid_category_list {
  background: #EFECC6;
  border-radius: 0px 10px 10px 10px;
  padding: 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.mid_ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.mid_item {
  margin: 0;
  padding: 0;
}

.mid_category_link {
  border: 0;
  background: none;
  color: #333;
  cursor: pointer;
  padding: 10px 16px;
  transition: background-color .15s ease, color .15s ease;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;

}

.mid_category_link:hover {
  background: rgba(0, 0, 0, .05);
}

.mid_category_link.selected {
  color: #670600;
  background: rgba(103, 6, 0, .08);
}

.mid_category_link:focus-visible {
  outline: 2px dashed #a83b32;
  outline-offset: 2px;
}
</style>
