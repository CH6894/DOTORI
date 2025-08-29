<template>
  <main class="cart container">
    <h1 class="title">장바구니</h1>

    <div class="table-wrap">
      <table class="cart-table">
        <colgroup>
          <col style="width:50px" />
          <col style="width:auto" />
          <col style="width:140px" />
          <col style="width:120px" />
          <col style="width:100px" />
          <col style="width:100px" />
          <col style="width:80px" />
        </colgroup>

        <thead>
          <tr>
            <th class="w-40"><input type="checkbox" :checked="allChecked" @change="toggleAll($event.target.checked)" /></th>
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
        <span>총 <b>{{ totalQty }}</b> 개의 상품 금액
          <b>{{ currency(subtotal) }}</b> + 배송비
          <b>{{ currency(shippingTotal) }}</b> =
          <b class="sum">{{ currency(grandTotal) }}</b>
        </span>
      </div>
    </div>

    <!-- 주문 버튼들을 toolbar 아래에 -->
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

const cartItems = ref([])
const router = useRouter()

// items 변수 정의
const items = cartItems

// 선택 상태
const checked = reactive({})
const allChecked = computed(() => items.value.length && items.value.every(it => checked[it.cartId] !== false))
const toggleAll = (v) => { items.value.forEach(it => { checked[it.cartId] = v }) }
const selectedIds = computed(() => items.value.filter(it => checked[it.cartId] !== false).map(it => it.cartId))

// 합계
const subtotal = computed(() => 
  items.value.reduce((s, it) => s + (checked[it.cartId] !== false ? it.price * it.quantity : 0), 0))
const shippingTotal = computed(() => 0) // 배송비 로직 필요시 수정
const grandTotal = computed(() => subtotal.value + shippingTotal.value)
const totalQty = computed(() => 
  items.value.reduce((s, it) => s + (checked[it.cartId] !== false ? it.quantity : 0), 0))

// 금액 포맷
const currency = (n) => new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW', maximumFractionDigits: 0 }).format(n)

// API 호출
const loadCart = async () => {
  try {
    console.log('장바구니 로딩 시작...')
    const res = await api.get('/cart/me')
    console.log('장바구니 응답:', res.data)
    items.value = res.data
    res.data.forEach(it => { checked[it.cartId] = true }) // 기본 선택
  } catch (error) {
    console.error('장바구니 로딩 실패:', error)
    if (error.response?.status === 401) {
      console.log('인증 실패 - 로그인 필요')
    }
  }
}

const applyQty = async ({ id, qty }) => {
  try {
    await api.put(`/cart/${id}`, null, { params: { quantity: qty } })
    await loadCart()
  } catch (error) {
    console.error('수량 변경 실패:', error)
  }
}

const removeOne = async (id) => {
  try {
    await api.delete(`/cart/${id}`)
    await loadCart()
  } catch (error) {
    console.error('상품 삭제 실패:', error)
  }
}

const removeSelected = async () => {
  try {
    for (const id of selectedIds.value) {
      await api.delete(`/cart/${id}`)
    }
    await loadCart()
  } catch (error) {
    console.error('선택 상품 삭제 실패:', error)
  }
}

// 주문하기
const orderSelected = () => {
  if (!selectedIds.value.length) return
  router.push({ name: 'CheckoutPage', query: { cartIds: selectedIds.value.join(',') } })
}

const orderAll = () => {
  if (!items.value.length) return
  const allIds = items.value.map(it => it.cartId)
  router.push({ name: 'CheckoutPage', query: { cartIds: allIds.join(',') } })
}

// 마운트 시 로드
onMounted(() => {
  console.log('ShoppingCart 마운트됨')
  loadCart()
})
</script>


<style scoped>
.container { max-width:1280px; margin:0 auto; padding:20px; }
.title { font-size:28px; font-weight:800; text-align:center; margin:16px 0 24px; }
.table-wrap { border:1px solid #eee; border-radius:14px; overflow:hidden; background:#fff; }

/* 열 정렬 고정 */
.cart-table { width:100%; border-collapse:collapse; table-layout:fixed; }
.cart-table thead th {
  background:#fafafa; padding:14px 10px; border-bottom:1px solid #eee; font-weight:700; font-size:14px;
}
/* 숫자/관리 헤더 가운데 정렬 */
.cart-table thead th:nth-child(3),
.cart-table thead th:nth-child(4),
.cart-table thead th:nth-child(5),
.cart-table thead th:nth-child(6),
.cart-table thead th:nth-child(7) { text-align:center; }

.cart-table th, .cart-table td { padding:14px 10px; font-size:14px; vertical-align:middle; overflow:hidden; }
.w-40 { width:40px; }
.empty { text-align:center; padding:60px 0; color:#777; }

/* 기존 toolbar 디자인 그대로 */
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

.left { display: flex; gap: 8px; }
.total { display: flex; align-items: center; gap: 16px; }
.total .sum { font-size: 20px; color: #111; }

/* 주문 버튼들을 toolbar 바로 아래에 */
.order-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: 10px 18px;
  margin-top: 8px;
}

.btn { 
  padding: 10px 14px; 
  border: 1px solid #ddd; 
  background: #fff; 
  border-radius: 12px; 
  cursor: pointer;
  font-weight: 600;
  white-space: nowrap;
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

.btn.ghost { 
  background: #ffff 
}
</style>