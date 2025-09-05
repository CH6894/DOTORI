<template>
  <main class="cart container">
    <h1 class="title">장바구니</h1>

    <div class="table-wrap">
      <table class="cart-table">
        <thead>
          <tr>
            <th><input type="checkbox" :checked="allChecked" @change="toggleAll($event.target.checked)" /></th>
            <th>상품 정보</th>
            <th>수량</th>
            <th>상품 금액</th>
            <th>할인/적립</th>
            <th>배송비</th>
            <th>관리</th>
          </tr>
        </thead>

        <tbody v-if="items.length">
          <CartItem
            v-for="it in items"
            :key="it.cartId"
            :item="it"
            v-model="checked[it.cartId]"
            @apply-qty="applyQty"
            @remove="removeOne"
          />
        </tbody>
        <tbody v-else>
          <tr>
            <td colspan="7" class="empty">장바구니가 비어 있습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 가격 정보 바 -->
    <div class="toolbar" v-if="items.length">
      <div class="left">
        <button class="btn sm" @click="removeSelected" :disabled="selectedIds.length===0">선택 상품 삭제</button>
        <button class="btn sm ghost" @click="router.push('/')">쇼핑 계속 하기</button>
      </div>
      <div class="right total">
        <span>
          총 <b>{{ totalQty }}</b> 개의 상품 금액
          <b>{{ currency(subtotal) }}</b> + 배송비
          <b>{{ currency(shippingTotal) }}</b> =
          <b class="sum">{{ currency(grandTotal) }}</b>
        </span>
      </div>
    </div>

    <!-- 주문 버튼 -->
    <div class="order-buttons" v-if="items.length">
      <button class="btn lg ghost" @click="orderSelected" :disabled="selectedIds.length===0">선택 상품 주문</button>
      <button class="btn lg primary" @click="orderAll" :disabled="items.length===0">전체 상품 주문</button>
    </div>
  </main>
</template>

<script setup>
import api from '@/api/axios'
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import CartItem from '@/components/shoppingcart/CartItem.vue'

const items = ref([])
const checked = reactive({})
const router = useRouter()

// 전체 선택 상태
const allChecked = computed(() => items.value.length && items.value.every(it => checked[it.cartId] !== false))
const toggleAll = (v) => items.value.forEach(it => { checked[it.cartId] = v })

// 선택된 cartId 목록
const selectedIds = computed(() => items.value.filter(it => checked[it.cartId] !== false).map(it => it.cartId))

// 금액 계산
const subtotal = computed(() => items.value.reduce((s, it) => checked[it.cartId] !== false ? s + (it.price * it.quantity) : s, 0))
const shippingTotal = computed(() => 0)
const grandTotal = computed(() => subtotal.value + shippingTotal.value)
const totalQty = computed(() => items.value.reduce((s, it) => checked[it.cartId] !== false ? s + it.quantity : s, 0))

const currency = (n) => new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW', maximumFractionDigits: 0 }).format(n || 0)

// 장바구니 불러오기
const loadCart = async () => {
  try {
    const res = await api.get('/cart/me')
    items.value = res.data
    res.data.forEach(it => { checked[it.cartId] = true })
  } catch (error) {
    console.error('장바구니 로드 실패:', error)
  }
}

// 수량 변경
const applyQty = async ({ id, qty }) => {
  try {
    await api.put(`/cart/${id}`, null, { params: { quantity: qty } })
    await loadCart()
  } catch (error) {
    console.error('수량 변경 실패:', error)
  }
}

// 개별 삭제
const removeOne = async (id) => {
  if (!confirm('삭제하시겠습니까?')) return
  await api.delete(`/cart/${id}`)
  await loadCart()
}

// 선택 삭제
const removeSelected = async () => {
  if (!confirm(`선택한 ${selectedIds.value.length}개 상품을 삭제하시겠습니까?`)) return
  for (const id of selectedIds.value) {
    await api.delete(`/cart/${id}`)
  }
  await loadCart()
}

// 주문 페이지 이동
const orderSelected = () => {
  if (!selectedIds.value.length) return alert('주문할 상품을 선택해주세요.')
  router.push({ name: 'checkout', query: { cartIds: selectedIds.value.join(',') } })
}

const orderAll = () => {
  if (!items.value.length) return alert('장바구니가 비어있습니다.')
  const allIds = items.value.map(it => it.cartId)
  router.push({ name: 'checkout', query: { cartIds: allIds.join(',') } })
}

// mounted
onMounted(loadCart)
</script>


<style scoped>
.container { 
  max-width: 1280px; 
  margin: 0 auto; 
  padding: 20px; 
}

.title { 
  font-size: 28px; 
  font-weight: 800; 
  text-align: center; 
  margin: 16px 0 24px; 
}

/* 디버깅 정보 스타일 */
.debug-info {
  background: #f0f8ff;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  margin: 10px 0;
  font-family: monospace;
}

.debug-item {
  padding: 5px 0;
  border-bottom: 1px solid #eee;
}

.debug-item:last-child {
  border-bottom: none;
}

.table-wrap { 
  border: 1px solid #eee; 
  border-radius: 14px; 
  overflow: hidden; 
  background: #fff; 
}

/* 테이블 스타일 */
.cart-table { 
  width: 100%; 
  border-collapse: collapse; 
  table-layout: fixed; 
}

.cart-table thead th {
  background: #fafafa; 
  padding: 14px 10px; 
  border-bottom: 1px solid #eee; 
  font-weight: 700; 
  font-size: 14px;
}

.cart-table thead th:nth-child(3),
.cart-table thead th:nth-child(4),
.cart-table thead th:nth-child(5),
.cart-table thead th:nth-child(6),
.cart-table thead th:nth-child(7) { 
  text-align: center; 
}

.cart-table th, 
.cart-table td { 
  padding: 14px 10px; 
  font-size: 14px; 
  vertical-align: middle; 
  overflow: hidden; 
}

.w-40 { 
  width: 40px; 
}

.empty { 
  text-align: center; 
  padding: 60px 0; 
  color: #777; 
}

/* Toolbar 스타일 */
.toolbar {
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
  gap: 20px;
  padding: 18px; 
  margin-top: 16px; 
  border-radius: 16px; 
  background: #f6f7f8; 
  flex-wrap: wrap;
}

.left { 
  display: flex; 
  gap: 8px; 
}

.total { 
  display: flex; 
  align-items: center; 
  gap: 16px; 
}

.total .sum { 
  font-size: 20px; 
  color: #111; 
}

/* 주문 버튼들 */
.order-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: 10px 18px;
  margin-top: 8px;
}

/* 버튼 스타일 */
.btn { 
  padding: 10px 14px; 
  border: 1px solid #ddd; 
  background: #fff; 
  border-radius: 12px; 
  cursor: pointer;
  font-weight: 600;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.btn:hover {
  background: #f8f9fa;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn.sm { 
  padding: 8px 12px; 
  font-size: 14px; 
}

.btn.lg { 
  padding: 12px 18px; 
  font-weight: 700; 
  background: #fff;
}

.btn.primary { 
  background: #fc703c; 
  color: #fff; 
  border-color: #fc703c; 
}

.btn.primary:hover {
  background: #e85a2b;
}

.btn.ghost { 
  background: #fff;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .container {
    padding: 10px;
  }
  
  .toolbar {
    flex-direction: column;
    gap: 10px;
  }
  
  .order-buttons {
    flex-direction: column;
  }
  
  .btn.lg {
    width: 100%;
  }
}
</style>