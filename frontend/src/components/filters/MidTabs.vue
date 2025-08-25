<!-- src/components/filters/MidTabs.vue -->
<script setup lang="ts">
const props = defineProps<{ items: string[]; modelValue: string; visible: boolean }>()
const emit  = defineEmits<{ (e:'update:modelValue', v:string): void }>()
</script>

<template>
  <div v-if="visible" class="mid_category_list">
    <ul role="tablist" aria-label="중간 카테고리" class="flex flex-wrap gap-1 list-none p-0 m-0 bg-[#EFECC6]">
      <li>
        <button
          role="tab"
          class="mid_category_link"
          :class="{ selected: modelValue === '' }"
          :aria-selected="modelValue === ''"
          :tabindex="modelValue === '' ? 0 : -1"
          type="button"
          @click="emit('update:modelValue', '')"
        >
          <strong><span>전체</span></strong>
        </button>
      </li>

      <li v-for="mid in items" :key="mid">
        <button
          role="tab"
          class="mid_category_link"
          :class="{ selected: modelValue === mid }"
          :aria-selected="modelValue === mid"
          :tabindex="modelValue === mid ? 0 : -1"
          type="button"
          @click="emit('update:modelValue', mid)"
        >
          <strong><span>{{ mid }}</span></strong>
        </button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.mid_category_link{
  border: 0; background: none; color: #333;
  cursor: pointer; padding: 10px 20px;
}
.mid_category_link.selected{ color:#670600; }
</style>
