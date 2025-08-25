<!-- src/views/checkout/Checkout.vue -->
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

      <AddressEdit v-model="addrModalOpen" :value="address" @save="updateAddress" @search-postcode="openPostcode" />

      <button class="btn-primary note" @click="isNoteOpen = !isNoteOpen" :class="{ open: isNoteOpen }">
        {{ displayNote }}
        <svg class="chev" viewBox="0 0 24 24" aria-hidden="true">
          <polyline points="6 9 12 15 18 9"></polyline>
        </svg>
      </button>

      <!-- 토글 패널 -->
      <transition name="collapse">
        <div v-show="isNoteOpen" class="note-panel" role="region" aria-label="배송 요청사항 입력">
          <textarea v-model="tempNote" class="note-textarea" rows="3" placeholder="예) 경비실에 맡겨주세요 / 부재 시 문앞에 두세요" />
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

      <!-- 주문 상품 -->
      <div class="order-items" v-for="it in items" :key="it.id">
        <div class="thumb">
          <img :src="it.thumb" alt="" />
        </div>
        <div class="info">
          <div class="name">{{ it.name }}</div>
          <div class="meta">
            {{ it.desc }}<br />
            {{ it.option }} / {{ it.qty }}개
          </div>
        </div>
        <div class="price">{{ formatCurrency(it.price * it.qty) }} 원</div>
      </div>

      <hr />

      <!-- 결제 금액 -->
      <div class="amount">
        <div class="row">
          <span>상품 금액</span>
          <b>{{ formatCurrency(subtotal) }}원</b>
        </div>
        <div class="row">
          <span>할인 금액</span>
          <b>{{ formatCurrency(discount) }}원</b>
        </div>
        <div class="row">
          <span>배송비</span>
          <b>{{ shippingFee === 0 ? '무료' : formatCurrency(shippingFee) + '원' }}</b>
        </div>
        <div class="row total">
          <span>총 주문 금액</span>
          <b>{{ formatCurrency(total) }}원</b>
        </div>
      </div>

      <!-- 결제 수단 -->
      <div class="pay-methods">
        <h3>결제 수단</h3>

        <label class="radio">
          <input type="radio" value="bank" v-model="payMethod" />
          <span>무통장 결제</span>
        </label>

        <transition name="collapse">
          <div v-if="payMethod === 'bank'" class="bank-box" role="region" aria-label="무통장 결제 정보">
            <div class="field">
              <label>입금은행 <span class="req">*</span></label>
              <div class="inline">
                <select v-model="selectedBank" class="select">
                  <option v-for="b in banks" :key="b.code" :value="b">
                    {{ b.label }}
                  </option>
                </select>
              </div>
              <div class="hint">
                계좌번호: <b>{{ selectedBank.account }}</b>
                <span v-if="selectedBank.holder"> / 예금주: {{ selectedBank.holder }}</span>
              </div>
            </div>

            <div class="field">
              <label>입금자명 <span class="req">*</span></label>
              <input v-model="depositorName" type="text" class="input" placeholder="주문자와 동일 시 생략 가능" />
            </div>

            <div class="notice">
              주문은 <b>{{ depositDeadlineHours }}시간</b> 이내 입금 완료 시 확정됩니다.
              기한 내 미입금 시 <b>자동 주문취소</b>됩니다.
            </div>
          </div>
        </transition>

        <label class="radio">
          <input type="radio" value="easy" v-model="payMethod" />
          <span>간편 결제</span>
        </label>

        <label class="radio">
          <input type="radio" value="card" v-model="payMethod" />
          <span>카드 결제</span>
        </label>

        <label class="radio">
          <input type="radio" value="mobile" v-model="payMethod" />
          <span>휴대폰</span>
        </label>
      </div>
    </section>

    <!-- 하단 고정 결제 바 -->
    <div class="paybar">
      <div class="agree">약관 및 주문 내용을 확인하였으며, 정보 제공 등에 동의합니다.</div>
      <!-- <div class="sum"><b>{{ formatCurrency(total) }}</b>원 결제하기</div> -->
      <!-- <button class="btn-pay" :disbled="payMethod !== 'bank' || !isBankFormValid" @click="onPay"><b>{{
        formatCurrency(total) }}</b>원 결제하기</button> -->
      <router-link v-if="isBankFormValid" :to="{ name: 'ordercomplete' }" class="btn-pay">
        <b>{{ formatCurrency(total) }}</b>원 결제하기
      </router-link>
      <button v-else class="btn-pay" :disabled="payMethod !== 'bank' || !isBankFormValid" @click="onPay">
        <b>{{ formatCurrency(total) }}</b>원 결제하기
      </button>
    </div>
  </div>
</template>

<script setup>

// 요청사항 토글/값
const isNoteOpen = ref(false)
const note = ref('')       // 최종 저장된 값
const tempNote = ref('')   // 편집 중 임시 값


const displayNote = computed(() => {
  const txt = note.value.trim()
  if (!txt) return '요청 사항 없음'
  return txt.length > 24 ? txt.slice(0, 24) + '…' : txt
})

const onSaveNote = () => {
  note.value = (tempNote.value || '').trim()
  isNoteOpen.value = false
}

const onCancelNote = () => {
  tempNote.value = note.value  // 원래값 복구
  isNoteOpen.value = false
}

const onClearNote = () => {
  tempNote.value = ''
  note.value = ''
  isNoteOpen.value = false
}

// (선택) 결제 시 요청사항 함께 전달
// onPay() 내부에서 body에 note.value 포함해 전송하면 됨


import { computed, reactive, ref } from 'vue'
// import { useRouter } from 'vue-router'
// const router = useRouter()
import AddressEdit from '../../components/AddressEdit.vue'

const addrModalOpen = ref(false)

/** —— MOCK: 실제 데이터 연동 시 교체 ——————————————————————— */
const address = reactive({
  receiver: '구창회',
  phone: '010-0000-0000',
  postcode: '06236',
  addr1: '서울 강남구 테헤란로 000',
  addr2: 'OOO빌딩 10층',
})

const items = ref([
  {
    id: 'sku-1',
    name: '상품이름 A',
    desc: '상품 상세 설명',
    option: '옵션1',
    qty: 1,
    price: 90000,
    thumb: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTz2BqmHnvVTX6iSjug5pt1h4ehgaX_GHat-Q&s',
  },
  {
    id: 'sku-2',
    name: '상품이름 B',
    desc: '다른 설명',
    option: '옵션2',
    qty: 3,
    price: 45000,
    thumb: 'https://images.unsplash.com/photo-1549068106-b024baf5062d?w=400&q=80&auto=format&fit=crop',
  },
])

const discount = computed(() =>
  items.value.reduce((s, it) => s + (it.discount || 0), 0)
)
const shippingFee = ref(0)
const payMethod = ref('easy')


// 합계는 배열 기준으로
const subtotal = computed(() =>
  items.value.reduce((sum, it) => sum + it.price * it.qty, 0)
)
const total = computed(() => subtotal.value - discount.value + shippingFee.value)
/** ———————————————————————————————————————————————— */

const formatCurrency = (n) =>
  n.toLocaleString('ko-KR', { maximumFractionDigits: 0 })


const updateAddress = (next) => {
  Object.assign(address, next)       // 화면 갱신
  // TODO: 서버 저장(API) 호출 지점
}

const openPostcode = () => {
  // TODO: 다음 주소 API 등 열기 → 선택 시 form.postcode/addr1 채우기
  alert('우편번호 검색 띄우기')
}

const isBankFormValid = computed(() => {
  if (payMethod.value !== 'bank') return false
  const hasBank = !!selectedBank.value?.account
  const hasName = !!depositorName.value?.trim()
  return hasBank && hasName

})

const onPay = () => {
  alert(`${payMethod.value} 방식으로 ${total.value}원 결제합니다.`)
  if (payMethod.value !== 'bank' || !isBankFormValid.value) return
  
}

/* 무통장 결제용 상태 */
const banks = [
  { code: 'KB', label: '국민은행', account: '123456-01-456789', holder: '도토리' },
  { code: 'SHIN', label: '신한은행', account: '110-123-456789', holder: '도토리' },
  { code: 'WOORI', label: '우리은행', account: '1002-123-456789', holder: '도토리' },
]
const selectedBank = ref(banks[0])
const depositorName = ref('')
const depositDeadlineHours = ref(3)

</script>

<style scoped>
.checkout {
  max-width: 720px;
  margin: 24px auto 120px;
  /* 하단 고정바 공간 확보 */
  padding: 0 16px;
}

.title {
  text-align: center;
  font-size: 22px;
  margin: 8px 0 16px;
}

.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, .04);
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid #eee;
  width: 140%;
  position: relative;
  left: 50%;
  transform: translateX(-50%);
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.left {
  min-width: 0;
}

.label {
  font-weight: 600;
  margin-bottom: 6px;
}

.addr-line {
  display: flex;
  margin-top: 6px;
}

.addr-label {
  width: 70px;
  font-weight: 600;
  color: #555;
  flex-shrink: 0;
  text-align: left;
  margin-right: 12px;
}

.addr-value {
  flex: 1;
  line-height: 1.5;
  word-break: keep-all;
  overflow-wrap: anywhere;
}

.addr-lines>div {
  line-height: 1.7;
  /* margin-left: 15px; */
}

.muted {
  color: #8a8a8a;
  font-weight: 400;
}

.btn-outline {
  border: 1px solid #ff7a2e;
  color: #ff7a2e;
  background: #fff;
  border-radius: 999px;
  padding: 6px 12px;
  font-weight: 600;
  cursor: pointer;
  margin-top: -70px;
}

.btn-outline:hover {
  background: #fff6f0;
}

.btn-primary.note {
  width: 100%;
  margin-top: 12px;
  background: #ff7a2e;
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 12px 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
}

.note .chev {
  width: 18px;
  height: 18px;
  stroke: #fff;
  stroke-width: 2.5;
  fill: none;
  display: block;
  /* ← inline(=baseline) 대신 block */
  line-height: 0;
  /* ← 텍스트 라인박스 영향 제거 */
  transform-box: fill-box;
  /* ← SVG의 경계 기준 */
  transform-origin: 40% 50%;
  /* ← 중앙 기준으로 회전 */
  transition: transform .2s ease;
}

.note.open .chev {
  transform: rotate(180deg);
}

.note-panel {
  margin-top: 10px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 12px;
}

.note-textarea {
  width: 100%;
  resize: vertical;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  padding: 10px 12px;
  box-sizing: border-box;
  font-size: 14px;
}

.note-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.note-actions-right {
  display: flex;
  gap: 8px;
}

/* 버튼들 */
.btn-subtle {
  background: #f6f6f6;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
}

.btn-save {
  background: #ff7a2e;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 8px 14px;
  font-weight: 700;
  cursor: pointer;
}

/* 부드러운 열고닫기 */
.collapse-enter-active,
.collapse-leave-active {
  transition: all .18s ease;
}

.collapse-enter-from,
.collapse-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.section-title {
  font-size: 16px;
  margin: 0 0 12px;
}

.order-items {
  display: grid;
  grid-template-columns: 72px 1fr auto;
  gap: 12px;
  align-items: center;
}

.thumb img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
}

.info .name {
  font-weight: 700;
}

.info .meta {
  color: #8a8a8a;
  font-size: 13px;
  margin-top: 4px;
}

.price {
  font-weight: 700;
}

.amount {
  margin-top: 8px;
}

.amount .row {
  padding: 10px 0;
}

.amount .row+.row {
  border-top: 1px solid #eee;
}

.amount .row.total {
  border-top: 2px solid #eee;
  font-size: 16px;
}

.pay-methods {
  margin-top: 8px;
}

.pay-methods h3 {
  font-size: 15px;
  margin: 12px 0;
}

.radio {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}

.bank-box {
  margin-top: 10px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 12px;
  display: grid;
  gap: 12px;
}

.field {
  display: grid;
  gap: 6px;
}

.field>label {
  font-size: 13px;
  color: #555;
}

.req {
  color: #ff4d4f;
}

.inline {
  display: flex;
  gap: 20px;
  align-items: center;
}

.select,
.input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-sizing: border-box;
  background: #fff;
}

.select {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;

  background: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='black'><path d='M7 10l5 5 5-5z'/></svg>") no-repeat right 12px center;
  background-size: 30px;

  padding-right: 32px;
}

.btn-ghost.sm {
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
}

.hint {
  font-size: 13px;
  color: #666;
}

.notice {
  background: #fff7f1;
  border: 1px solid #ffd7bf;
  color: #000000;
  padding: 10px 12px;
  border-radius: 8px;
  line-height: 1.5;
  font-size: 14px;
}

/* 부드러운 토글 애니메이션 */
.collapse-enter-active,
.collapse-leave-active {
  transition: all .18s ease;
}

.collapse-enter-from,
.collapse-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}


.agree {
  color: #fff;
  margin-left: 250px;
  white-space: nowrap;
}

.paybar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #ff7a2e;
  border-top: 1px solid #ffd7bf;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 100px;
  gap: 12px;
}

.sum {
  color: #2b2b2b;
}

.btn-pay {
  white-space: nowrap;
  background: #fff;
  color: #000;
  border: none;
  border-radius: 10px;
  padding: 12px 25px;
  font-weight: 700;
  cursor: pointer;
  margin-right: 250px;
  text-decoration:none;
}

.btn-pay:hover {
  filter: brightness(0.98);
}

.btn-pay:disabled {
  opacity: .5;
  cursor: not-allowed;
  pointer-events: none;
}

/* 모바일 최적화 */
@media (max-width: 480px) {
  .order-items {
    grid-template-columns: 56px 1fr auto;
  }

  .thumb img {
    width: 56px;
    height: 56px;
  }
}
</style>
