<!-- src/views/ItemList.vue -->
<template>
  <div class="product_list_wrap">
    <h2>Items</h2>

    <div v-if="loading">로딩 중...</div>
    <div v-else-if="error" style="color:red">{{ error }}</div>
    <div v-else>
      <ul v-if="items.length">
        <li v-for="it in items" :key="it.id" class="product_item">
          <strong>{{ it.name }}</strong>
          <span v-if="it.price"> — {{ it.price.toLocaleString() }}원</span>
          <p v-if="it.description">{{ it.description }}</p>
        </li>
      </ul>
      <p v-else>표시할 상품이 없습니다.</p>

      <div class="pager">
        <button :disabled="page===0" @click="prev">이전</button>
        <span> {{ page+1 }} / {{ totalPages }} </span>
        <button :disabled="page>=totalPages-1" @click="next">다음</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios'
import { ref, onMounted } from 'vue'

const items = ref([])
const page = ref(0)
const size = ref(10)
const totalPages = ref(1)
const loading = ref(false)
const error = ref('')

const fetchItems = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await axios.get(`/api/items`, {
      params: { page: page.value, size: size.value },
    })
    // Spring Data Page 응답 형태: content, totalPages, number 등
    items.value = data.content ?? []
    totalPages.value = data.totalPages ?? 1
  } catch (e) {
    error.value = '목록을 불러오는 중 오류가 발생했습니다.'
    console.error(e)
  } finally {
    loading.value = false
  }
}

const next = () => { if (page.value < totalPages.value - 1) { page.value++; fetchItems() } }
const prev = () => { if (page.value > 0) { page.value--; fetchItems() } }

onMounted(fetchItems)
</script>

<style scoped>
.product_item { margin: 8px 0; }
.pager { display:flex; gap:12px; align-items:center; margin-top:12px; }
</style>
