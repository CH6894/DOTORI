<script setup lang="ts">
import { ref } from 'vue'
import ModalSellConfirm from '@/components/ModalSellConfirm.vue'
import type { ItemDTO } from '@/types/item'

const showModal = ref(true)

const item: ItemDTO = {
  id: '1',
  itemCode: 'ITEM001',
  name: '(예약분 입고중) 후류 (FURYU) 헌터헌터 누들 스토퍼 피규어 키르아 조르딕&곤 프릭스 (재판) (2종세트)ㅁㄴsadasdㅇㅁㄴㅇ',
  title: '(예약분 입고중) 후류 (FURYU) 헌터헌터 누들 스토퍼 피규어 키르아 조르딕&곤 프릭스 (재판) (2종세트)ㅁㄴsadasdㅇㅁㄴㅇ',
  images: ['/img/placeholder.jpg', '/img/placeholder.jpg'],
  price: 152000,
}

const rows = [
  { option: '미개봉', price: 150000, date: '2025-08-01' },
  { option: '사용감 없음', price: 148000, date: '2025-08-15' },
]

function handleSubmit(payload: any) {
  console.log('SUBMIT:', payload)
  showModal.value = false
}
</script>

<template>
  <ModalSellConfirm
    v-if="showModal"
    :item="item"
    :price="item.price"
    :fee-config="{ inspect:'free', fee:'free', shipping:'seller' }"
    :rows="rows"
    @close="showModal = false"
    @submit="handleSubmit"
  />
</template>
