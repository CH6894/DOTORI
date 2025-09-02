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
            <div class="addr-line">
              <span class="addr-label">받는 분</span>
              <span class="addr-value">{{ address.receiver }}</span>
            </div>

            <div class="addr-line">
              <span class="addr-label">연락처</span>
              <span class="addr-value">{{ address.phone }}</span>
            </div>

            <div class="addr-line">
              <span class="addr-label">주소</span>
              <span class="addr-value">
                [{{ address.postcode }}] {{ address.addr1 }}<br />
                {{ address.addr2 }}
              </span>
            </div>
          </div>
        </div>
        <button class="btn-outline" @click="addrModalOpen = true">주소변경</button>
      </div>

      <AddressEdit
        v-model="addrModalOpen"
        :value="address"
        @save="updateAddress"
        @search-postcode="openPostcode"
      />

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

      <div v-for="it in items" :key="it.id" class="order-items">
        <div class="thumb"><img :src="it.thumb" alt="" /></div>
        <div class="info">
          <div class="name">{{ it.name }}</div>
          <div class="meta">
            {{ it.desc }}<br />
            {{ it.option }} <span v-if="it.option && it.qty">/</span> {{ it.qty }}개
          </div>
        </div>
        <div class="price">{{ formatCurrency(it.price * it.qty) }} 원</div>
      </div>

      <p v-if="items.length === 0" class="muted" style="margin-top:12px;">주문할 상품이 없습니다.</p>

      <hr v-if="items.length" />

      <!-- 결제 금액 -->
      <div class="amount" v-if="items.length">
        <div class="row"><span>상품 금액</span><b>{{ formatCurrency(subtotal) }}원</b></div>
        <div class="row"><span>할인 금액</span><b>{{ formatCurrency(discount) }}원</b></div>
        <div class="row">
          <span>배송비</span>
          <b>{{ shippingFee === 0 ? '무료' : formatCurrency(shippingFee) + '원' }}</b>
        </div>
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
              <div class="inline">
                <select v-model="selectedBank" class="select">
                  <option v-for="b in banks" :key="b.code" :value="b">{{ b.label }}</option>
                </select>
              </div>
              <div class="hint">계좌번호: <b>{{ selectedBank.account }}</b><span v-if="selectedBank.holder"> / 예금주: {{ selectedBank.holder }}</span></div>
            </div>

            <div class="field">
              <label>입금자명 <span class="req">*</span></label>
              <input v-model="depositorName" type="text" class="input" placeholder="주문자와 동일 시 생략 가능" @input="onInputDepositor" />
            </div>

            <div class="notice">
              주문은 <b>{{ depositDeadlineHours }}시간</b> 이내 입금 완료 시 확정됩니다.
              기한 내 미입금 시 <b>자동 주문취소</b>됩니다.
            </div>
          </div>
        </transition>

        <label class="radio"><input type="radio" value="easy" v-model="payMethod" /><span>간편 결제</span></label>
        <label class="radio"><input type="radio" value="card" v-model="payMethod" /><span>카드 결제</span></label>
        <label class="radio"><input type="radio" value="mobile" v-model="payMethod" /><span>휴대폰</span></label>
      </div>
    </section>
  </div>

<!-- 하단 고정 결제 바 -->
<div class="paybar">
  <div class="agree">
    약관 및 주문 내용을 확인하였으며, 정보 제공 등에 동의합니다.
  </div>

  <!-- ✅ 무통장 + 유효할 때만 '이동 가능한' 링크 -->
  <router-link
    v-if="payMethod === 'bank' && isBankFormValid"
    :to="{ name: 'ordercomplete' }"
    class="btn-pay"
  >
    <b>{{ formatCurrency(total) }}</b>원 결제하기
  </router-link>

  <!-- ❌ 그 외에는 항상 비활성 버튼만 -->
  <button v-else class="btn-pay" disabled @click.prevent>
    <b>{{ formatCurrency(total) }}</b>원 결제하기
  </button>
</div>

</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import AddressEdit from '@/components/AddressEdit.vue'

/* ───────── 결제바 높이만큼 body 하단 패딩 부여(이 페이지에서만 적용) ───────── */
const paybarEl = ref<HTMLElement | null>(null)
let roPaybar: ResizeObserver | null = null
function applyBodyPadding(h: number) {
  document.body.style.setProperty('--paybar-h', `${h}px`)
  document.body.classList.add('has-checkout-paybar') // 페이지 떠나면 제거
}
onMounted(() => {
  const el = paybarEl.value
  const update = () => {
    const h = el ? Math.round(el.getBoundingClientRect().height) : 80
    applyBodyPadding(h)
  }
  roPaybar = new ResizeObserver(update)
  if (el) roPaybar.observe(el)
  update()
})
onBeforeUnmount(() => {
  roPaybar?.disconnect(); roPaybar = null
  document.body.style.removeProperty('--paybar-h')
  document.body.classList.remove('has-checkout-paybar')
})

/* ───────── 기본 상수/상태 ───────── */
const LS_KEY = 'dotori_cart_v1'

const addrModalOpen = ref(false)
const address = reactive({
  receiver: '구창회',
  phone: '010-0000-0000',
  postcode: '06236',
  addr1: '서울 강남구 테헤란로 000',
  addr2: 'OOO빌딩 10층',
})
const updateAddress = (next: any) => { Object.assign(address, next) }
const openPostcode = () => { alert('우편번호 검색 띄우기') }

/* 요청사항 */
const isNoteOpen = ref(false)
const note = ref('')
const tempNote = ref('')
const displayNote = computed(() => {
  const txt = note.value.trim()
  if (!txt) return '요청 사항 없음'
  return txt.length > 24 ? txt.slice(0, 24) + '…' : txt
})
const onSaveNote = () => { note.value = (tempNote.value || '').trim(); isNoteOpen.value = false }
const onCancelNote = () => { tempNote.value = note.value; isNoteOpen.value = false }
const onClearNote = () => { tempNote.value = ''; note.value = ''; isNoteOpen.value = false }

/* 장바구니 → 체크아웃 데이터 */
const route = useRoute()
type RawItem = {
  id: number | string
  title?: string
  name?: string
  desc?: string
  option?: string
  qty: number
  price: number
  shipping?: number
  thumb?: string
  image?: string
}
const allItems = ref<RawItem[]>([])
const items = ref<RawItem[]>([])

onMounted(() => {
  const raw = localStorage.getItem(LS_KEY)
  const parsed: RawItem[] = raw ? JSON.parse(raw) : []

  const mode = String(route.query.mode ?? 'all')
  const idsParam = String(route.query.ids ?? '')
  const idSet = new Set(idsParam.split(',').map(s => parseInt(s.trim(), 10)).filter(n => Number.isFinite(n)))

  allItems.value = parsed.map(it => ({
    ...it,
    name: it.name ?? it.title ?? '(무제)',
    thumb: it.thumb ?? it.image ?? 'https://via.placeholder.com/72x72?text=item',
    qty: Math.max(1, Number(it.qty) || 1),
    price: Math.max(0, Number(it.price) || 0),
    shipping: Math.max(0, Number(it.shipping) || 0),
  }))

  items.value = mode === 'selected'
    ? allItems.value.filter(it => idSet.has(Number(it.id)))
    : allItems.value
})

/* 금액 계산 */
const discount = computed(() => items.value.reduce((s, it) => s + (Number((it as any).discount) || 0), 0))
const subtotal  = computed(() => items.value.reduce((sum, it) => sum + it.price * it.qty, 0))
const shippingFee = computed(() => items.value.reduce((sum, it) => sum + (it.shipping || 0), 0))
const total = computed(() => subtotal.value - discount.value + shippingFee.value)
const formatCurrency = (n: number) => n.toLocaleString('ko-KR', { maximumFractionDigits: 0 })

/* 결제 수단/검증 */
const banks = [
  { code: 'KB', label: '국민은행', account: '123456-01-456789', holder: '도토리' },
  { code: 'SHIN', label: '신한은행', account: '110-123-456789', holder: '도토리' },
  { code: 'WOORI', label: '우리은행', account: '1002-123-456789', holder: '도토리' },
]
const payMethod = ref<'bank' | 'easy' | 'card' | 'mobile'>('easy')
const selectedBank = ref(banks[0])
const depositorName = ref('')
const depositDeadlineHours = ref(3)

const onInputDepositor = (e: Event) => {
  depositorName.value = (e.target as HTMLInputElement).value
}

const isBankFormValid = computed(() => {
  if (payMethod.value !== 'bank') return false
  const hasBank = !!selectedBank.value?.account
  const hasName = !!depositorName.value?.trim()
  return hasBank && hasName
})

const isPayEnabled = computed(() => {
  if (!items.value.length) return false
  return payMethod.value === 'bank' ? isBankFormValid.value : true
})

const onPay = () => {
  if (!isPayEnabled.value) {
    if (payMethod.value === 'bank') alert('무통장 결제 정보를 완료해 주세요.')
    else alert('결제할 상품이 없습니다.')
    return
  }
  alert(`${payMethod.value} 방식으로 ${total.value.toLocaleString()}원 결제합니다.`)
}
</script>

<style scoped>
.checkout {
  max-width: 720px;
  margin: 24px auto; /* 결제바 때문에 여백은 body 패딩으로 처리 */
  padding: 0 16px;
}

.title { text-align: center; font-size: 22px; margin: 8px 0 16px; }

.card {
  background: #fff; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,.04);
  padding: 16px; margin-bottom: 16px; border: 1px solid #eee;

  /* 가운데 넓게 쓰려면 유지 */
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
.inline { display: flex; gap: 20px; align-items: center; }
.select, .input { width: 100%; padding: 10px 12px; border: 1px solid #ddd; border-radius: 8px; box-sizing: border-box; background: #fff; }
.select {
  appearance: none; -webkit-appearance: none; -moz-appearance: none;
  background: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='black'><path d='M7 10l5 5 5-5z'/></svg>") no-repeat right 12px center;
  background-size: 30px; padding-right: 32px;
}
.btn-ghost.sm { padding: 8px 10px; border: 1px solid #ddd; border-radius: 8px; background: #fff; cursor: pointer; }
.hint { font-size: 13px; color: #666; }
.notice { background: #fff7f1; border: 1px solid #ffd7bf; color: #000; padding: 10px 12px; border-radius: 8px; line-height: 1.5; font-size: 14px; }

/* ===== 결제바 (항상 화면 하단) ===== */
.paybar {
  position: fixed; left: 0; right: 0; bottom: 0;
  background: #ff7a2e; border-top: 1px solid #ffd7bf;
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 100px; gap: 12px;
  z-index: 50;
}

/* 텍스트/버튼 */
.agree { color: #fff; margin-left: 250px; white-space: nowrap; }
.btn-pay {
  white-space: nowrap; background: #fff; color: #000; border: none; border-radius: 10px; 
  padding: 12px 25px; font-weight: 700; cursor: pointer; margin-right: 250px; text-decoration: none;
    /* 추가 */
  display: inline-flex;      /* 내부 텍스트 크기 변화에도 고정 */
  align-items: center;
  justify-content: center;
  height: 45px;              /* 원하는 높이 고정 */
  min-width: 220px;          /* 원하는 최소 너비 고정 */
  line-height: 1; 
  font-size: 15px;
  font-variant-numeric: tabular-nums; 
}
.btn-pay .amount{
  font-variant-numeric: tabular-nums;
}
.btn-pay:hover { filter: brightness(0.98); }
.btn-pay:disabled { opacity: .5; cursor: not-allowed; pointer-events: none; filter:none;}

/* 모바일 */
@media (max-width: 480px) {
  .order-items { grid-template-columns: 56px 1fr auto; }
  .thumb img { width: 56px; height: 56px; }
}

/* ───── 전역 바디 패딩을 '이 페이지에서만' 적용 ─────
   - 결제바 높이만큼 body 하단에 여백을 확보해서
     맨 아래에서 '푸터 전체' + '아래 결제바'가 동시에 보이도록 함 */
:global(body.has-checkout-paybar) {
  padding-bottom: var(--paybar-h, 80px);
}
</style>
