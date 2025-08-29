<!-- src/components/filters/MidTabsAdapter.vue -->
<script setup lang="ts">
import { ref, watch, computed, withDefaults } from 'vue'
import MidTabs from './MidTabs.vue'

type Item = { id: string; name: string }

const props = withDefaults(defineProps<{
  items: Item[]
  modelValue: string
  ariaLabel?: string
  visible?: boolean
  includeAll?: boolean     // ← 추가: 기본 true
  allId?: string           // ← 추가: ‘전체’의 id
  allName?: string         // ← 추가: ‘전체’의 라벨
}>(), {
  visible: true,
  includeAll: true,
  allId: 'all',
  allName: '전체',
})

const emit = defineEmits<{ (e:'update:modelValue', v:string): void }>()

/** ‘전체’ 숨김 여부에 따라 항목 필터링 */
const filteredItems = computed<Item[]>(() => {
  if (props.includeAll) return props.items
  const nameLower = props.allName.toLowerCase()
  return props.items.filter(i =>
    i.id !== props.allId &&
    i.name !== props.allName &&
    i.name.toLowerCase() !== nameLower
  )
})

/** 이름/ID 매핑은 필터된 목록 기준으로 생성 */
const nameById = computed(() => new Map(filteredItems.value.map(i => [i.id, i.name])))
const idByName = computed(() => new Map(filteredItems.value.map(i => [i.name, i.id])))

const selectedName = ref<string>('')

/** props 변화에 맞춰 초기 선택값 세팅 */
watch(
  () => [props.items, props.modelValue, props.includeAll] as const,
  () => {
    // modelValue가 유효하면 그 이름으로, 아니면 첫 번째 탭으로
    const fromModel = nameById.value.get(props.modelValue)
    if (fromModel) {
      selectedName.value = fromModel
    } else {
      selectedName.value = filteredItems.value[0]?.name ?? ''
      // includeAll=false인데 modelValue가 ‘전체’를 가리키고 있으면 첫 탭 id로 교정
      if (!props.includeAll && (props.modelValue === props.allId)) {
        const firstId = filteredItems.value[0]?.id
        if (firstId) emit('update:modelValue', firstId)
      }
    }
  },
  { immediate: true }
)

/** 하위 MidTabs에서 이름이 바뀌면 id로 역매핑해 상위로 올림 */
watch(selectedName, (next) => {
  const id = idByName.value.get(next)
  if (id && id !== props.modelValue) emit('update:modelValue', id)
})
</script>

<template>
  <MidTabs
    :items="filteredItems.map(i => i.name)"
    v-model="selectedName"
    :aria-label="props.ariaLabel"
    :visible="props.visible"
  />
</template>
