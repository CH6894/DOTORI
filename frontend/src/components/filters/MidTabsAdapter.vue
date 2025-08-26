<!-- src/components/filters/MidTabsAdapter.vue -->
<script setup lang="ts">
import { ref, watch, computed, withDefaults } from 'vue'
import MidTabs from './MidTabs.vue'

type Item = { id: string; name: string }

// visible 기본값 true로 두되, 필요하면 상위에서 제어 가능
const props = withDefaults(defineProps<{
  items: Item[]              // {id,name}[]
  modelValue: string         // 선택된 'id'
  ariaLabel?: string
  visible?: boolean          // 🔸 MidTabs가 필수로 요구하는 prop
}>(), {
  visible: true,
})

const emit = defineEmits<{ (e:'update:modelValue', v:string): void }>()

const nameById = computed(() => new Map(props.items.map(i => [i.id, i.name])))
const idByName = computed(() => new Map(props.items.map(i => [i.name, i.id])))

const selectedName = ref<string>('')

watch(
  () => [props.items, props.modelValue] as const,
  () => {
    selectedName.value =
      nameById.value.get(props.modelValue) ?? props.items[0]?.name ?? ''
  },
  { immediate: true }
)

watch(selectedName, (next) => {
  const id = idByName.value.get(next)
  if (id && id !== props.modelValue) emit('update:modelValue', id)
})
</script>

<template>
  <MidTabs
    :items="props.items.map(i => i.name)"
    v-model="selectedName"
    :aria-label="props.ariaLabel"
    :visible="props.visible" 
  />
</template>
