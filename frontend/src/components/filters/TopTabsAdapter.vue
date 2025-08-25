<!-- src/components/filters/TopTabsAdapter.vue -->
<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import TopTabs from './TopTabs.vue'

type Item = { id: string; name: string }

const props = defineProps<{
  items: Item[]              // {id,name}[]
  modelValue: string         // 선택된 'id'
  ariaLabel?: string
}>()

const emit = defineEmits<{ (e:'update:modelValue', v:string): void }>()

// name <-> id 매핑
const nameById = computed(() => new Map(props.items.map(i => [i.id, i.name])))
const idByName = computed(() => new Map(props.items.map(i => [i.name, i.id])))

// TopTabs는 '이름'을 v-model로 받으므로 내부에 선택된 name 상태를 둔다
const selectedName = ref<string>('')

// 부모의 modelValue(id)나 items가 바뀌면 name 동기화
watch(
  () => [props.items, props.modelValue] as const,
  () => {
    selectedName.value = nameById.value.get(props.modelValue) ?? props.items[0]?.name ?? ''
  },
  { immediate: true }
)

// TopTabs에서 name이 바뀌면 id로 변환해서 부모에 올려보낸다
watch(selectedName, (next) => {
  const id = idByName.value.get(next)
  if (id && id !== props.modelValue) emit('update:modelValue', id)
})
</script>

<template>
  <!-- 공용 TopTabs는 string[]를 받으므로 name 배열로 변환 -->
  <TopTabs
    :items="props.items.map(i => i.name)"
    v-model="selectedName"
    :aria-label="props.ariaLabel"
  />
</template>
