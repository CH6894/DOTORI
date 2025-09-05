<!-- src/views/checkout/CheckoutPage.vue -->
<template>
  <div class="checkout">
    <h1 class="title">배송 / 결제</h1>

    <!-- 배송 주소 -->
    <section class="card address">
      <div class="row">
        <div class="left">
          <div class="label">배송 주소</div>
          <div class="addr-lines">
            <div class="addr-line"><span class="addr-label">받는 분</span><span class="addr-value">{{ address.receiver }}</span></div>
            <div class="addr-line"><span class="addr-label">연락처</span><span class="addr-value">{{ address.phone }}</span></div>
            <div class="addr-line"><span class="addr-label">주소</span>
              <span class="addr-value">[{{ address.postcode }}] {{ address.addr1 }}<br/>{{ address.addr2 }}</span>
            </div>
          </div>
        </div>
        <button class="btn-outline" @click="addrModalOpen = true">주소변경</button>
      </div>

      <!-- 주소 변경 모달 -->
      <AddressEdit
        v-model="addrModalOpen"
        :value="address"
        @save="updateAddress"
      />

      <!-- 배송 요청사항 -->
      <button class="btn-primary note" @click="isNoteOpen = !isNoteOpen" :class="{ open: isNoteOpen }">
        {{ displayNote }}
        <svg class="chev" viewBox="0 0 24 24" aria-hidden="true">
          <polyline points="6 9 12 15 18 9"></polyline>
        </svg>
      </button>

      <transition name="collapse">
        <div v-show="isNoteOpen" class="note-panel" role="region" aria-label="배송 요청사항 입력">
          <textarea
            v-model="tempNote"
            class="note-textarea"
            rows="3"
            placeholder="예) 경비실에 맡겨주세요 / 부재 시 문앞에 두세요"
          ></textarea>
          <div class="note-actions">
            <button class="btn-subtle" @click="onClearNote">지우기</button>
            <div class="note-actions-right">
              <button class="btn-ghost" @click="onCancelNote">취소</button>
              <button class="btn-save" @click="onSaveNote">저장</button>
            </div>
          </div>
        </div>
      </transition>
    </section>

    <!-- 주문서 -->
    <section class="card order">
      <h2 class="section-title">주문서</h2>

      <div v-for="it in items" :key="it.cartId" class="order-items">
        <div class="thumb"><img :src="it.mainImageUrl || '/default-product.jpg'" alt="" /></div>
        <div class="info">
          <div class="name">{{ it.itemName }}</div>
          <div class="meta">수량: {{ it.quantity }}개</div>
        </div>
        <div class="price">{{ formatCurrency(it.price * it.quantity) }} 원</div>
      </div>

      <p v-if="items.length === 0" class="muted" style="margin-top:12px;">주문할 상품이 없습니다.</p>

      <div class="amount" v-if="items.length">
        <div class="row"><span>상품 금액</span><b>{{ formatCurrency(subtotal) }}원</b></div>
        <div class="row"><span>배송비</span><b>{{ shippingFee === 0 ? '무료' : formatCurrency(shippingFee) + '원' }}</b></div>
        <div class="row total"><span>총 주문 금액</span><b>{{ formatCurrency(total) }}원</b></div>
      </div>

      <!-- 결제 수단 -->
      <div class="pay-methods" v-if="items.length">
        <h3>결제 수단</h3>
        <label class="radio"><input type="radio" value="bank" v-model="payMethod" /><span>무통장 결제</span></label>

        <transition name="collapse">
          <div v-if="payMethod === 'bank'" class="bank-box" role="region" aria-label="무통장 결제 정보">
            <div class="field">
              <label>입금은행 <span class="req">*</span></label>
              <select v-model="selectedBank" class="select">
                <option v-for="b in banks" :key="b.code" :value="b">{{ b.label }}</option>
              </select>
              <div class="hint">계좌번호: <b>{{ selectedBank.account }}</b> / 예금주: {{ selectedBank.holder }}</div>
            </div>
            <div class="field">
              <label>입금자명 <span class="req">*</span></label>
              <input v-model="depositerName" type="text" class="input" placeholder="주문자와 동일 시 생략 가능" />
            </div>
            <div class="notice">
              주문은 <b>{{ depositDeadlineHours }}시간</b> 이내 입금 완료 시 확정됩니다. 기한 내 미입금 시 <b>자동 주문취소</b>됩니다.
            </div>
          </div>
        </transition>

        <label class="radio"><input type="radio" value="easy" v-model="payMethod" /> 간편 결제</label>
        <label class="radio"><input type="radio" value="card" v-model="payMethod" /> 카드 결제</label>
        <label class="radio"><input type="radio" value="mobile" v-model="payMethod" /> 휴대폰 결제</label>
      </div>
    </section>
  </div>

  <!-- 하단 결제바 -->
  <div class="paybar">
    <div class="agree">약관 및 주문 내용을 확인하였으며, 정보 제공 등에 동의합니다.</div>

    <button v-if="canPay" class="btn-pay" @click="submitOrder">
      <b>{{ formatCurrency(total) }}</b>원 결제하기
    </button>

    <button v-else class="btn-pay" disabled>
      <b>{{ formatCurrency(total) }}</b>원 결제하기
    </button>
  </div>
</template>

<script setup>
import api from '@/api/axios'
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AddressEdit from '@/components/AddressEdit.vue'

const route = useRoute()
const router = useRouter()

/* 주소 */
const addrModalOpen = ref(false)
const address = reactive({ receiver: '', phone: '', postcode: '', addr1: '', addr2: '', mainAddress: '' })

const updateAddress = (updatedAddr) => {
  Object.assign(address, updatedAddr)
  saveAddressToServer(updatedAddr)
}
const saveAddressToServer = async (addressData) => {
  try {
    await api.post('/address', { postcode: addressData.postcode, addr1: addressData.addr1, addr2: addressData.addr2 })
  } catch (e) {
    console.error('주소 저장 실패:', e)
    alert('주소 저장에 실패했습니다.')
  }
}
const loadAddress = async () => {
  try {
    const res = await api.get('/address')
    let addressData = null
    if (Array.isArray(res.data) && res.data.length > 0) {
      addressData = res.data[0]
    } else if (res.data && !Array.isArray(res.data)) {
      addressData = res.data
    }
    if (addressData) {
      address.receiver = addressData.receiver || ''
      address.phone = addressData.phone || ''
      address.postcode = addressData.postcode || ''
      address.addr1 = addressData.addr1 || ''
      address.addr2 = addressData.addr2 || ''
      address.mainAddress = addressData.mainAddress || ''
    }
  } catch (e) {
    console.error('주소 로드 실패:', e)
  }
}

/* 🚩 배송 요청사항 (note → deliveryNote 로 명확히 변경) */
const isNoteOpen = ref(false)
const deliveryNote = ref('')     // ✅ 최종 저장된 요청사항
const tempNote = ref('')
const displayNote = computed(() => deliveryNote.value ? `요청사항: ${deliveryNote.value}` : '배송 요청사항 입력')
const onSaveNote = () => { deliveryNote.value = tempNote.value; isNoteOpen.value = false }
const onCancelNote = () => { tempNote.value = deliveryNote.value; isNoteOpen.value = false }
const onClearNote = () => { tempNote.value = ''; deliveryNote.value = '' }

/* 장바구니 */
const items = ref([])
const loadCart = async () => {
  try {
    const idsParam = String(route.query.cartIds || '')
    const idSet = new Set(idsParam.split(',').map(n => parseInt(n)).filter(Number.isFinite))
    const res = await api.get('/cart/me')
    items.value = idSet.size > 0 ? res.data.filter(it => idSet.has(it.cartId)) : res.data
  } catch (e) {
    console.error('장바구니 로드 실패:', e)
    items.value = []
  }
}

/* 금액 */
const subtotal = computed(() => items.value.reduce((s, it) => s + (it.price * it.quantity), 0))
const shippingFee = 0
const total = computed(() => subtotal.value + shippingFee)
const formatCurrency = (n) => n.toLocaleString('ko-KR')

/* 결제 */
const banks = [
  { code: 'KB', label: '국민은행', account: '123456-01-456789', holder: '도토리' },
  { code: 'SHIN', label: '신한은행', account: '110-123-456789', holder: '도토리' },
  { code: 'WOORI', label: '우리은행', account: '1002-123-456789', holder: '도토리' }
]
const payMethod = ref('bank')
const selectedBank = ref(banks[0])
const depositerName = ref('')
const depositDeadlineHours = 24

/* 결제 가능 조건 */
const canPay = computed(() =>
  payMethod.value === 'bank'
    ? (!!selectedBank.value?.account && !!depositerName.value.trim() && items.value.length > 0)
    : false
)

/* 주문 */
const submitOrder = async () => {
  try {
    if (!canPay.value) return alert('입금자명을 입력해주세요.')
    const cartIds = items.value.map(it => it.cartId)
    if (!cartIds.length) return alert('주문할 상품이 없습니다.')
    if (!confirm(`총 ${formatCurrency(total.value)}원을 주문하시겠습니까?`)) return

    const payload = { 
      cartIds, 
      depositerName: depositerName.value,
      payMethod: payMethod.value,
      payMessage: deliveryNote.value || ''   // ✅ 수정됨
    }

    const res = await api.post('/orders', payload)

    const payTime = res.data.payTime   // ✅ 주문 그룹키 대신 payTime 사용
    alert('주문이 완료되었습니다!')

    try { await api.delete('/cart/me') } catch (e) { console.warn('장바구니 초기화 실패:', e) }
    router.push({ name: 'OrderComplete', query: { payTime } })
  } catch (err) {
    console.error('주문 실패:', err)
    alert('주문 처리 중 오류가 발생했습니다.')
  }
}

onMounted(() => {
  loadAddress()
  loadCart()
})
</script>


<style scoped>
.checkout {
  max-width: 720px;
  margin: 24px auto;
  padding: 0 16px;
}

.title { text-align: center; font-size: 22px; margin: 8px 0 16px; }

.card {
  background: #fff; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,.04);
  padding: 16px; margin-bottom: 16px; border: 1px solid #eee;
  width: 140%;
  position: relative; left: 50%; transform: translateX(-50%);
}

.row { display: flex; align-items: center; justify-content: space-between; }
.left { min-width: 0; }
.label { font-weight: 600; margin-bottom: 6px; }

.addr-line { display: flex; margin-top: 6px; }
.addr-label { width: 70px; font-weight: 600; color: #555; flex-shrink: 0; text-align: left; margin-right: 12px; }
.addr-value { flex: 1; line-height: 1.5; word-break: keep-all; overflow-wrap: anywhere; }
.addr-lines>div { line-height: 1.7; }

.muted { color: #8a8a8a; font-weight: 400; }

.btn-outline {
  border: 1px solid #ff7a2e; color: #ff7a2e; background: #fff;
  border-radius: 999px; padding: 6px 12px; font-weight: 600; cursor: pointer; margin-top: -70px;
}
.btn-outline:hover { background: #fff6f0; }

.btn-primary.note {
  width: 100%; margin-top: 12px; background: #ff7a2e; color: #fff; border: none; border-radius: 10px;
  padding: 12px 20px; font-weight: 700; display: flex; align-items: center; justify-content: space-between;
}
.note .chev { width: 18px; height: 18px; stroke: #fff; stroke-width: 2.5; fill: none; display: block; line-height: 0;
  transform-box: fill-box; transform-origin: 40% 50%; transition: transform .2s ease; }
.note.open .chev { transform: rotate(180deg); }

.note-panel { margin-top: 10px; background: #fff; border: 1px solid #eee; border-radius: 10px; padding: 12px; }
.note-textarea { width: 100%; resize: vertical; border: 1px solid #e6e6e6; border-radius: 8px; padding: 10px 12px; box-sizing: border-box; font-size: 14px; }
.note-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.note-actions-right { display: flex; gap: 8px; }
.btn-subtle { background: #f6f6f6; border: 1px solid #e6e6e6; border-radius: 8px; padding: 8px 12px; cursor: pointer; }
.btn-save { background: #ff7a2e; color: #fff; border: none; border-radius: 8px; padding: 8px 14px; font-weight: 700; cursor: pointer; }

.collapse-enter-active, .collapse-leave-active { transition: all .18s ease; }
.collapse-enter-from, .collapse-leave-to { opacity: 0; transform: translateY(-4px); }

.section-title { font-size: 16px; margin: 0 0 12px; }

.order-items { display: grid; grid-template-columns: 72px 1fr auto; gap: 12px; align-items: center; }
.thumb img { width: 72px; height: 72px; object-fit: cover; border-radius: 8px; border: 1px solid #eee; }
.info .name { font-weight: 700; }
.info .meta { color: #8a8a8a; font-size: 13px; margin-top: 4px; }
.price { font-weight: 700; }

.amount { margin-top: 8px; }
.amount .row { padding: 10px 0; }
.amount .row + .row { border-top: 1px solid #eee; }
.amount .row.total { border-top: 2px solid #eee; font-size: 16px; }

.pay-methods { margin-top: 8px; }
.pay-methods h3 { font-size: 15px; margin: 12px 0; }
.radio { display: flex; align-items: center; gap: 8px; padding: 8px 0; }

.bank-box { margin-top: 10px; background: #fff; border: 1px solid #eee; border-radius: 10px; padding: 12px; display: grid; gap: 12px; }
.field { display: grid; gap: 6px; }
.field>label { font-size: 13px; color: #555; }
.req { color: #ff4d4f; }
.select, .input { width: 100%; padding: 10px 12px; border: 1px solid #ddd; border-radius: 8px; box-sizing: border-box; background: #fff; }
.select {
  appearance: none; -webkit-appearance: none; -moz-appearance: none;
  background: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='black'><path d='M7 10l5 5 5-5z'/></svg>") no-repeat right 12px center;
  background-size: 30px; padding-right: 32px;
}
.hint { font-size: 13px; color: #666; }
.notice { background: #fff7f1; border: 1px solid #ffd7bf; color: #000; padding: 10px 12px; border-radius: 8px; line-height: 1.5; font-size: 14px; }

/* 결제바 */
.paybar {
  position: fixed; left: 0; right: 0; bottom: 0;
  background: #ff7a2e; border-top: 1px solid #ffd7bf;
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 100px; gap: 12px;
  z-index: 50;
}

.agree { color: #fff; margin-left: 250px; white-space: nowrap; }
.btn-pay {
  white-space: nowrap; background: #fff; color: #000; border: none; border-radius: 10px; 
  padding: 12px 25px; font-weight: 700; cursor: pointer; margin-right: 250px; text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 45px;
  min-width: 220px;
  line-height: 1; 
  font-size: 15px;
  font-variant-numeric: tabular-nums; 
}
.btn-pay:hover { filter: brightness(0.98); }
.btn-pay:disabled { opacity: .5; cursor: not-allowed; pointer-events: none; filter:none;}

/* 모바일 */
@media (max-width: 480px) {
  .order-items { grid-template-columns: 56px 1fr auto; }
  .thumb img { width: 56px; height: 56px; }
}

:global(body.has-checkout-paybar) {
  padding-bottom: var(--paybar-h, 80px);}
  
</style>