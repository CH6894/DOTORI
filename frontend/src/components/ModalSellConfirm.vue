<!-- src/components/ModalSellConfirm.vue -->
<template>
  <div class="modal-overlay" @click.self="onClose" role="dialog" aria-modal="true">
    <div class="modal-sheet">
      <!-- 헤더 -->
      <header class="sheet-header">
        <h2 class="sheet-title">판매 등록 확인</h2>
        <button class="icon-btn" @click="onClose" aria-label="닫기">×</button>
      </header>

      <section class="sheet-body">
        <!-- 좌측 -->
        <div class="left">
          <div class="main-image-container">
            <img :src="currentImage" :alt="item.title" class="main-image" />
            <button v-if="images.length > 1" class="nav-btn prev-btn" :disabled="idx === 0" @click="prev"
              aria-label="이전 이미지">‹</button>
            <button v-if="images.length > 1" class="nav-btn next-btn" :disabled="idx === images.length - 1"
              @click="next" aria-label="다음 이미지">›</button>
            <div v-if="images.length > 1" class="image-indicators">
              <span v-for="(img, i) in images" :key="img + '-' + i" class="indicator" :class="{ active: i === idx }"
                @click="setIdx(i)"></span>
            </div>
          </div>

          <h3 class="item-title">{{ item.title }}</h3>

          <div class="badge-row">
            <button v-for="chip in chips" :key="chip" type="button" class="chip"
              :class="{ active: selectedChip === chip }" @click="toggleChip(chip)">
              {{ chip }}
            </button>
          </div>
        </div>

        <!-- 우측 -->
        <aside class="right">
          <div class="summary">
            <div class="muted">판매 등록가</div>

            <!-- ✅ 천단위 자동포맷 + 숫자만 + 최대 1억 -->
            <input type="text" inputmode="numeric" pattern="[0-9]*" :value="priceText" @input="handlePriceInput"
              @keydown="onPriceKeydown" placeholder="가격 입력" class="price-input" aria-label="판매 등록가" />

            <div class="fees">
              <div class="fee"><span>검수비</span><span>{{ inspectLabel }}</span></div>
              <div class="fee"><span>수수료</span><span>{{ serviceFeeLabel }}</span></div>
              <div class="fee"><span>배송비</span><span>{{ shippingText }}</span></div>
            </div>

            <div class="total">
              <span>합계</span>
              <span class="total-amt">{{ format(total) }}원</span>
            </div>
          </div>

          <div class="card">
            <div class="card-title">시세</div>
            <div class="chart-placeholder">그래프 영역</div>
          </div>

          <div class="memo-box">
            <div class="memo-title">무엇이 적힐 수 있는 공간</div>
            <textarea v-model="memo" rows="2" maxlength="50" @input="enforceLimit"
              placeholder="구성품/특이사항 메모 (최대 50자)"></textarea>
          </div>

          <label class="confirm">
            <input type="checkbox" v-model="confirmed" />
            판매하려는 상품이 맞습니다.
          </label>

          <button class="cta" :disabled="!confirmed" @click="submit">
            판매 계속 하기
          </button>
        </aside>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'

/** 타입 */
type Condition = 'excellent' | 'good' | 'fair' | 'poor'
type Item = {
  id: string | number
  title: string
  images?: string[]
  condition?: Condition
  price?: number
}
type FeeConfig = {
  inspect: 'free' | number
  fee: 'free' | number
  shipping: 'seller' | 'buyer' | number
}

/** 상수: 최대 1억 */
const MAX_PRICE = 1_000_000_000 - 1

/** props / emits */
const props = defineProps<{
  item: Item
  price?: number        // 초기값 용(선택)
  feeConfig?: FeeConfig
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'submit', payload: { item: Item; price: number; memo: string; total: number; selectedChip: string | null }): void
}>()

/** 갤러리 */
const idx = ref(0)
const images = computed<string[]>(() => props.item.images?.length ? props.item.images : ['/img/placeholder.jpg'])
const currentImage = computed(() => images.value[idx.value])
function setIdx(i: number) { if (i >= 0 && i < images.value.length) idx.value = i }
function prev() { if (idx.value > 0) idx.value-- }
function next() { if (idx.value < images.value.length - 1) idx.value++ }

/** 칩 */
const chips = ['개봉', '미개봉'] as const
const selectedChip = ref<string | null>(null)
function toggleChip(chip: string) {
  selectedChip.value = selectedChip.value === chip ? null : chip
}

/** 가격(모달 내 입력) */
const localPrice = ref<number>(Math.min(props.price ?? props.item.price ?? 0, MAX_PRICE))
const priceText = ref<string>(format(localPrice.value))

function digitsOnly(s: string) {
  return s.replace(/[^\d]/g, '')
}
function handlePriceInput(e: Event) {
  const input = e.target as HTMLInputElement
  // 숫자만 추출
  let digits = digitsOnly(input.value)
  if (digits === '') digits = '0'
  // 선행 0 제거
  digits = String(Number(digits))
  // 최대값 클램프
  let num = Number(digits)
  if (num > MAX_PRICE) num = MAX_PRICE

  localPrice.value = num
  const formatted = format(num)
  priceText.value = formatted

  // 커서 위치는 실사용성 위해 맨 끝으로
  nextTick(() => {
    input.setSelectionRange(formatted.length, formatted.length)
  })
}
function onPriceKeydown(e: KeyboardEvent) {
  // 허용 키
  const allow = ['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Home', 'End', 'Tab']
  if (allow.includes(e.key)) return
  // 숫자 허용
  if (/^\d$/.test(e.key)) return
  // 그 외 차단 (복붙은 input 핸들러에서 정제)
  e.preventDefault()
}
const price = computed<number>(() => localPrice.value)

/** 수수료/배송 */
const feeCfg = computed<FeeConfig>(() => props.feeConfig ?? ({ inspect: 'free', fee: 'free', shipping: 'seller' }))
function feeLabel(v: 'free' | number) { return v === 'free' ? '무료' : `${format(v)}원` }
const inspectLabel = computed(() => feeLabel(feeCfg.value.inspect))
const serviceFeeLabel = computed(() => feeLabel(feeCfg.value.fee))
const shippingText = computed(() => {
  const s = feeCfg.value.shipping
  if (s === 'seller') return '선불 / 판매자 부담'
  if (s === 'buyer') return '착불 / 구매자 부담'
  return format(s) + '원 (정액)'
})
const feeValue = (v: 'free' | number) => (v === 'free' ? 0 : v)
const shipValue = () => (typeof feeCfg.value.shipping === 'number' ? feeCfg.value.shipping : 0)

/** 합계 */
const total = computed<number>(() => {
  const svc = feeValue(feeCfg.value.fee)
  const insp = feeValue(feeCfg.value.inspect)
  const ship = feeCfg.value.shipping === 'seller' ? shipValue() : 0
  return Math.max(0, price.value - svc - insp - ship)
})

/** 제출/닫기/유틸 */
const confirmed = ref(false)
const memo = ref('')
function enforceLimit(e: Event) {
  const t = e.target as HTMLTextAreaElement
  if (t.value.length > 50) t.value = t.value.slice(0, 50)
  memo.value = t.value
}
function format(n: number) { return n.toLocaleString('ko-KR') }
function onClose() { emit('close') }
function submit() {
  emit('submit', {
    item: props.item,
    price: localPrice.value,
    memo: memo.value,
    total: total.value,
    selectedChip: selectedChip.value
  })
}
</script>

<style scoped>
/* 오버레이/시트 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
}

.modal-sheet {
  width: min(1100px, 95vw);
  height: min(800px, 50vw);
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, .35);
  overflow: hidden;
  animation: pop .2s ease;
}

@keyframes pop {
  from {
    transform: translateY(10px) scale(.98);
    opacity: .7
  }

  to {
    transform: none;
    opacity: 1
  }
}

.sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #eee
}

.sheet-title {
  font-size: 18px;
  font-weight: 800;
  margin: 0
}

.icon-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  font-size: 28px;
  color: #666;
  border-radius: 50%;
  cursor: pointer
}

.icon-btn:hover {
  background: #f5f5f5;
  color: #222
}

.sheet-body {
  display: grid;
  grid-template-columns: 1fr 0.8fr;
  gap: 24px;
  padding-left: 36px;
  padding-right: 36px;
  padding-top: 12px;
}

/* 좌측 */
.left {
  height: 600px;
}

.left .item-title {
  font-size: 24px;
  font-weight: 800;
  margin: 10px 0 12px
}

.item-title {
  display: -webkit-box;          
  -webkit-box-orient: vertical;  
  -webkit-line-clamp: 2;        
  line-clamp: 2;            
  overflow: hidden;
}


.badge-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px
}

.chip {
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 999px;
  padding: 8px 24px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 700;
  user-select: none
}

.chip.active {
  border-color: #FC703C;
  color: #FC703C
}

/* 갤러리 */
.main-image-container {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  background: #f8f9fa;
  margin-bottom: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, .1)
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: #fff;
  font-weight: 700;
  font-size: 18px;
  color: #333;
  cursor: pointer
}

.nav-btn:disabled {
  opacity: .35;
  cursor: not-allowed
}

.prev-btn {
  left: 10px
}

.next-btn {
  right: 10px
}

.image-indicators {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 6px
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, .6);
  cursor: pointer
}

.indicator.active {
  background: #fff;
  transform: scale(1.2)
}

/* 우측 */
.right {
  display: flex;
  flex-direction: column;
  gap: 12px
}

.summary {
  border-radius: 12px;
  border: 1px solid #eee;
  padding: 16px
}

.price-input {
  width: 100%;
  padding: 8px 12px;
  font-size: 20px;
  font-weight: 800;
  border: 1px solid #ddd;
  border-radius: 8px;
  margin: 6px 0 12px;
}

.price-input:focus {
  outline: none;
  border-color: #FC703C
}

/* (참고) number input 스핀버튼 제거 - 지금은 text라 필요 없지만 혹시 바꿀 때 대비 */
.price-input::-webkit-outer-spin-button,
.price-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

input[type="number"] {
  appearance: textfield;
  -moz-appearance: textfield;
  -webkit-appearance: none;
}

.memo-box {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 12px
}

.memo-title {
  color: #999;
  font-weight: 800;
  margin-bottom: 6px
}

textarea {
  resize: none;
  width: 100%;
  border: 1px solid #e6e6e6;
  border-radius: 10px;
  padding: 10px;
  font: inherit
}

.muted {
  color: #333;
  font-weight: 900;
  letter-spacing: .02em
}

.fees {
  display: grid;
  gap: 8px;
  margin: 8px 0 12px
}

.fee {
  display: flex;
  justify-content: space-between;
  color: #6b6b6b;
  font-weight: 600
}

.total {
  display: flex;
  justify-content: space-between;
  border-top: 1px dashed #eee;
  padding-top: 12px;
  align-items: center
}

.total-amt {
  font-size: 22px;
  font-weight: 900;
  color: #111
}

.card {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 12px;
  margin-top: 12px
}

.card-title {
  font-weight: 700;
  margin-bottom: 8px
}

.chart-placeholder {
  height: 140px;
  background: #f6f7f9;
  border-radius: 10px;
  display: grid;
  place-items: center;
  color: #999
}

.confirm {
  display: flex;
  gap: 8px;
  align-items: center;
  color: #333;
  font-weight: 700
}

.cta {
  height: 48px;
  border: none;
  border-radius: 10px;
  background: #FC703C;
  color: #fff;
  font-weight: 800;
  cursor: pointer
}

.cta:disabled {
  opacity: .4;
  cursor: not-allowed
}

.cta:not(:disabled):hover {
  background: #f04005
}

/* 반응형 */
@media (max-width: 980px) {
  .sheet-body {
    grid-template-columns: 1fr
  }
}
</style>
