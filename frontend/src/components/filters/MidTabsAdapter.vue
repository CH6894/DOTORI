<!-- src/components/filters/MidTabsAdapter.vue -->
<script setup lang="ts">
import { ref, watch, computed, withDefaults } from 'vue'
import MidTabs from './MidTabs.vue'

type Item = { id: string; name: string }

const props = withDefaults(defineProps<{
  /** 표시할 탭 목록: [{ id, name }] */
  items: Item[]
  /** 현재 선택된 탭 id (v-model) */
  modelValue: string
  /** 네비게이션 접근성 라벨 */
  ariaLabel?: string
  /** 보이기/숨기기 */
  visible?: boolean

  /** '전체' 탭 포함 여부 (기본: true) */
  includeAll?: boolean
  /** '전체' 탭의 id (기본: 'all') */
  allId?: string
  /** '전체' 탭의 라벨 (기본: '전체') */
  allName?: string
}>(), {
  visible: true,
  includeAll: true,
  allId: 'all',
  allName: '전체',
})

const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>()

/** includeAll=false면 '전체'를 제거한 목록을 사용 (공백/대소문자까지 방어) */
const filteredItems = computed<Item[]>(() => {
  const list = props.items ?? []
  if (props.includeAll) return list
  const norm = (v: string) => (v ?? '').toString().trim().toLowerCase()
  const allId   = norm(props.allId || 'all')
  const allName = norm(props.allName || '전체')
  return list.filter(i => norm(i.id) !== allId && norm(i.name) !== allName)
})

/** 이름/ID 양방향 매핑 (필터된 목록 기준) */
const nameById = computed(() => new Map(filteredItems.value.map(i => [i.id, i.name])))
const idByName = computed(() => new Map(filteredItems.value.map(i => [i.name, i.id])))

/** 하위 MidTabs는 name을 v-model로 받으므로, 내부 선택 값은 name으로 관리 */
const selectedName = ref<string>('')

/** props 변화 시 내부/외부 선택 동기화 */
watch(
  () => [props.items, props.modelValue, props.includeAll, props.allId, props.allName] as const,
  () => {
    const items = filteredItems.value
    const first = items[0]

    // modelValue가 유효한 경우 → 해당 name으로 설정
    const fromModel = props.modelValue ? nameById.value.get(props.modelValue) : undefined
    if (fromModel) {
      selectedName.value = fromModel
      return
    }

    // includeAll=false인데 modelValue가 '전체'를 가리키면 첫번째 유효 id로 보정
    if (!props.includeAll && props.modelValue === props.allId && first?.id) {
      emit('update:modelValue', first.id)
      selectedName.value = first.name
      return
    }

    // 모델이 비어있거나 유효하지 않으면 첫 항목으로 초기화
    if (first) {
      selectedName.value = first.name
      if (props.modelValue !== first.id) emit('update:modelValue', first.id)
    } else {
      // 항목이 비어있는 경우 방어
      selectedName.value = ''
    }
  },
  { immediate: true }
)

/** 하위(MidTabs)에서 이름이 바뀌면 id로 역매핑해서 부모로 올림 */
watch(selectedName, (next) => {
  const id = idByName.value.get(next)
  if (id && id !== props.modelValue) {
    emit('update:modelValue', id)
  } else if (!id && filteredItems.value[0]) {
    // name→id 역매핑 실패 시 첫 항목으로 보정
    emit('update:modelValue', filteredItems.value[0].id)
    selectedName.value = filteredItems.value[0].name
  }
})
</script>

<template>
  <MidTabs
    v-if="visible"
    :items="filteredItems.map(i => i.name)"
    v-model="selectedName"
    :aria-label="ariaLabel || '중위 카테고리'"
    :visible="visible"
  />
</template>
