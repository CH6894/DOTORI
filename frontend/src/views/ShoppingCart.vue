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
            :key="it.id"
            :item="it"
            v-model="checked[it.id]"
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

<script>
import { reactive, ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import CartItem from '@/components/shoppingcart/CartItem.vue'

const LS_KEY = 'dotori_cart_v1'

export default {
  name: 'CartView',
  components: { CartItem },
  setup() {
    const router = useRouter()

    const items = ref([])
    onMounted(() => {
      const raw = localStorage.getItem(LS_KEY)
      items.value = raw ? JSON.parse(raw) : []
    })

    // 선택 상태
    const checked = reactive({})
    const allChecked = computed(() => items.value.length && items.value.every(it => checked[it.id] !== false))
    const toggleAll = (v) => { items.value.forEach(it => { checked[it.id] = v }) }
    const selectedIds = computed(() => items.value.filter(it => checked[it.id] !== false).map(it => it.id))

    // 합계
    const subtotal = computed(() =>
      items.value.reduce((s, it) => s + (checked[it.id] !== false ? it.price * it.qty : 0), 0)
    )
    const shippingTotal = computed(() =>
      items.value.reduce((s, it) => s + (checked[it.id] !== false ? (it.shipping || 0) : 0), 0)
    )
    const grandTotal = computed(() => subtotal.value + shippingTotal.value)
    const totalQty = computed(() =>
      items.value.reduce((s, it) => s + (checked[it.id] !== false ? it.qty : 0), 0)
    )

    // 수량 적용/삭제
    const applyQty = ({ id, qty }) => {
      const it = items.value.find(x => x.id === id)
      if (it) it.qty = Math.max(1, parseInt(qty, 10) || 1)
    }
    const removeOne = (id) => { items.value = items.value.filter(x => x.id !== id) }
    const removeSelected = () => { items.value = items.value.filter(x => !selectedIds.value.includes(x.id)) }

    // 주문 → /checkout 이동 (쿼리로 모드/ids 전달)
    const currency = (n) => new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW', maximumFractionDigits: 0 }).format(n)

    const orderSelected = () => {
      if (!selectedIds.value.length) return
      router.push({
        path: '/checkout',
        query: { mode: 'selected', ids: selectedIds.value.join(',') },
      })
    }

    const orderAll = () => {
      if (!items.value.length) return
      router.push({ path: '/checkout', query: { mode: 'all' } })
    }

    // 저장
    watch(items, v => localStorage.setItem(LS_KEY, JSON.stringify(v)), { deep: true })

    return {
      items,
      checked,
      allChecked,
      toggleAll,
      selectedIds,
      subtotal,
      shippingTotal,
      grandTotal,
      totalQty,
      currency,
      applyQty,
      removeOne,
      removeSelected,
      orderSelected,
      orderAll,
    }
  },
}
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