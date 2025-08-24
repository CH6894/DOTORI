<!-- src/components/ModalSellConfirm.vue -->
<template>
    <teleport to="body">

  <div class="modal-overlay" @click.self="onClose" role="dialog" aria-modal="true">
    <div class="modal-sheet">
      <!-- 헤더 -->
      <header class="sheet-header">
        <h2 class="sheet-title">
          <template v-if="step !== 3">판매 등록 확인</template>
          <template v-else>판매 신청 완료</template>
        </h2>
        <button class="icon-btn" @click="onClose" aria-label="닫기">×</button>
      </header>

      <!-- STEP 1 & 2 -->
      <section class="sheet-body" v-if="step !== 3">
        <!-- 좌측 -->
        <div class="left">
          <div class="main-image-container">
            <img :src="currentImage" :alt="item.title" class="main-image" />
            <button v-if="images.length > 1" class="nav-btn prev-btn" :disabled="idx === 0" @click="prev" aria-label="이전 이미지">‹</button>
            <button v-if="images.length > 1" class="nav-btn next-btn" :disabled="idx === images.length - 1" @click="next" aria-label="다음 이미지">›</button>
            <div v-if="images.length > 1" class="image-indicators">
              <span v-for="(img, i) in images" :key="img + '-' + i" class="indicator" :class="{ active: i === idx }" @click="setIdx(i)"></span>
            </div>
          </div>

          <h3 class="item-title">{{ item.title }}</h3>

          <div class="badge-row">
            <button
              v-for="chip in chips"
              :key="chip"
              type="button"
              class="chip"
              :class="{ active: selectedChip === chip, disabled: step === 2 }"
              :disabled="step === 2"
              @click="toggleChip(chip)">
              {{ chip }}
            </button>
          </div>
        </div>

        <!-- 우측 -->
        <aside class="right">
          <!-- STEP 1 -->
          <template v-if="step === 1">
            <div class="summary">
              <div class="muted">판매 등록가</div>
              <input
                type="text"
                inputmode="numeric"
                pattern="[0-9]*"
                :value="priceText"
                @input="handlePriceInput"
                @keydown="onPriceKeydown"
                placeholder="가격 입력"
                class="price-input"
                aria-label="판매 등록가"
              />

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
              <div class="memo-title">판매자 코멘트</div>
              <textarea
                v-model="memo"
                rows="2"
                maxlength="50"
                @input="enforceLimit"
                placeholder="구성품/특이사항 메모 (최대 50자)"></textarea>
            </div>

            <label class="confirm">
              <input type="checkbox" v-model="confirmed" />
              판매하려는 상품이 맞습니다.
            </label>

            <button class="cta align-right"  :disabled="!confirmed" @click="goStep2">
              계속하기
            </button>
          </template>

          <!-- STEP 2 -->
          <template v-else>
            <div class="terms">
              <div class="terms-title">약관 및 주의사항 동의</div>

              <!-- 약관 동의: v-for 형식 -->
              <div class="terms-subtitle">약관 동의</div>
              <label
                v-for="item in termItems"
                :key="item.key"
                class="term"
              >
                <input type="checkbox" v-model="terms[item.key]" />
                <span v-html="item.text"></span>
              </label>

              <!-- 주의사항 동의: v-for 형식 -->
              <div class="terms-subtitle">주의사항 동의</div>
              <label
                v-for="item in noticeItems"
                :key="item.key"
                class="term"
              >
                <input type="checkbox" v-model="notices[item.key]" />
                <span v-html="item.text"></span>
              </label>

              <!-- 전체 동의 -->
              <label class="term all">
                <input type="checkbox" :checked="allAgreed" @change="toggleAll" />
                전체 동의
              </label>

              <div class="actions two">
                <button type="button" class="ghost" @click="step = 1">뒤로가기</button>
                <button type="button" class="cta" :disabled="!allAgreed" @click="submit">제출</button>
              </div>
            </div>
          </template>
        </aside>
      </section>

      <!-- STEP 3: 완료 -->
      <section v-else class="sheet-body single">
        <div class="completion">
          <div class="success-title">판매 신청이 완료되었습니다 🎉</div>
          <div class="success-sub">아래 안내에 따라 물품을 보내주세요. 접수 후 상태가 단계별로 업데이트됩니다.</div>

          <ol class="progress">
            <li :class="stepClass(0)"><span class="dot"></span><span class="label">신청 확인 중</span></li>
            <li :class="stepClass(1)"><span class="dot"></span><span class="label">입고 확인</span></li>
            <li :class="stepClass(2)"><span class="dot"></span><span class="label">검수 중</span></li>
            <li :class="stepClass(3)"><span class="dot"></span><span class="label">등록 대기중</span></li>
            <li :class="stepClass(4)"><span class="dot"></span><span class="label">등록</span></li>
          </ol>

          <div class="address-card">
            <div class="card-title">입고 주소</div>
            <div class="addr-grid">
              <div><span class="k">받는 사람</span><span class="v">Dotori</span></div>
              <div><span class="k">연락처</span><span class="v">02-1234-5678</span></div>
              <div class="row">
                <span class="k">주소</span>
                <span class="v">광주광역시 동구 중앙로 196, 3층(광주빌딩)</span>
              </div>
            </div>
            <div class="addr-actions">
              <button type="button" class="ghost sm" @click="copyAddress">주소 복사</button>
              <span v-if="copied" class="copied">복사됨!</span>
            </div>
            <div class="addr-note">
              배송 후 마이페이지에서 배송상태를 갱신해주세요.<br> 
              우리 사이트에 등록된 성함으로 보내주세요.<br>
              택배 파손 방지를 위해 완충재를 충분히 사용해 주세요.</div>
          </div>

          <div class="actions end">
            <button type="button" class="ghost" @click="onClose">닫기</button>
          </div>
        </div>
      </section>
    </div>
  </div>
    </teleport>

</template>

<script setup lang="ts">
import { computed, nextTick, ref, reactive } from 'vue'

/** 타입 */
type Condition = 'excellent' | 'good' | 'fair' | 'poor'
type Item = { id: string | number; title: string; images?: string[]; condition?: Condition; price?: number }
type FeeConfig = { inspect: 'free' | number; fee: 'free' | number; shipping: 'seller' | 'buyer' | number }

/** 상수 */
const MAX_PRICE = 1_000_000_000 - 1 // 10억 미만
const STAGES = ['신청 확인 중', '입고 확인', '검수 중', '등록 대기중', '등록'] as const

/** 단계 */
const step = ref<1 | 2 | 3>(1)
function goStep2() { step.value = 2 }

/** props / emits */
const props = defineProps<{
  item: Item
  price?: number
  feeConfig?: FeeConfig
}>()
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'submit', payload: { item: Item; price: number; memo: string; total: number; selectedChip: string | null }): void
  (e: 'submitted', payload: any): void
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
  if (step.value === 2) return
  selectedChip.value = selectedChip.value === chip ? null : chip
}

/** 가격 입력 */
const localPrice = ref<number>(Math.min(props.price ?? props.item.price ?? 0, MAX_PRICE))
const priceText = ref<string>(format(localPrice.value))
function digitsOnly(s: string) { return s.replace(/[^\d]/g, '') }
function handlePriceInput(e: Event) {
  const input = e.target as HTMLInputElement
  let digits = digitsOnly(input.value)
  if (digits === '') digits = '0'
  digits = String(Number(digits))
  let num = Number(digits)
  if (num > MAX_PRICE) num = MAX_PRICE
  localPrice.value = num
  const formatted = format(num)
  priceText.value = formatted
  nextTick(() => input.setSelectionRange(formatted.length, formatted.length))
}
function onPriceKeydown(e: KeyboardEvent) {
  const allow = ['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Home', 'End', 'Tab']
  if (allow.includes(e.key)) return
  if (/^\d$/.test(e.key)) return
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
  if (typeof s === 'number') return format(s) + '원 (정액)'
  return ''
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

/** 메모 */
const confirmed = ref(false)
const memo = ref('')
function enforceLimit(e: Event) {
  const t = e.target as HTMLTextAreaElement
  if (t.value.length > 50) t.value = t.value.slice(0, 50)
  memo.value = t.value
}

/** 약관/주의사항 상태 */
type TermKeys = 'agree1' | 'agree2' | 'agree3' | 'agree4'
const terms = reactive<Record<TermKeys, boolean>>({
  agree1: false,
  agree2: false,
  agree3: false,
  agree4: false,
})

type NoticeKeys = 'notice1' | 'notice2' | 'notice3' | 'notice4'
const notices = reactive<Record<NoticeKeys, boolean>>({
  notice1: false,
  notice2: false,
  notice3: false,
  notice4: false,
})

/** 약관 동의 항목(4) */
const termItems: ReadonlyArray<{ key: TermKeys; text: string }> = [
  { key: 'agree1', text: '<b>[필수]</b> 판매 물품은 정품이며 허위·과장 정보가 없음을 확인합니다.' },
  { key: 'agree2', text: '<b>[필수]</b> 거래 취소/반품 정책을 확인하였으며 위반 시 제재에 동의합니다.' },
  { key: 'agree3', text: '<b>[필수]</b> 개인정보 처리방침 및 결제 대행 이용에 동의합니다.' },
  { key: 'agree4', text: '<b>[필수]</b> 분쟁 발생 시 플랫폼의 중재 절차에 협조합니다.' },
] as const

/** 주의사항 동의 항목(4) */
const noticeItems: ReadonlyArray<{ key: NoticeKeys; text: string }> = [
  { key: 'notice1', text: '<b>[필수]</b> 위조·모조·불법 복제품이 아닌 정품만 등록·발송합니다.' },
  { key: 'notice2', text: '<b>[필수]</b> 상품 설명과 실제 상태/구성품이 일치하며, 하자·사용 흔적은 고지했습니다.' },
  { key: 'notice3', text: '<b>[필수]</b> 파손·오염 방지를 위해 안전 포장하고, 추적 가능한 배송수단으로 발송합니다.' },
  { key: 'notice4', text: '<b>[필수]</b> 검수 불합격 시 거래 취소 및 왕복 배송비 등 비용은 판매자 부담에 동의합니다.' },
] as const

/** 전체 동의 */
const allAgreed = computed(() =>
  (Object.values(terms) as boolean[]).every(Boolean) &&
  (Object.values(notices) as boolean[]).every(Boolean)
)

function toggleAll(e: Event) {
  const v = (e.target as HTMLInputElement).checked
  ;(Object.keys(terms) as TermKeys[]).forEach(k => (terms[k] = v))
  ;(Object.keys(notices) as NoticeKeys[]).forEach(k => (notices[k] = v))
}

/** 진행 상태 표시 */
const currentStage = ref(0)
function stepClass(i: number) {
  return {
    complete: i < currentStage.value,
    active: i === currentStage.value,
    pending: i > currentStage.value,
  }
}

/** 유틸 */
const copied = ref(false)
async function copyAddress() {
  const text = `광주광역시 동구 중앙로 196, 302호(광주빌딩) `
  try {
    await navigator.clipboard.writeText(text)
    copied.value = true
    setTimeout(() => (copied.value = false), 1500)
  } catch {}
}

function format(n: number) { return n.toLocaleString('ko-KR') }
function onClose() { emit('close') }

/** 제출 */
function submit() {
  if (!allAgreed.value) {
    alert('필수 항목에 모두 동의해주세요.')
    return
  }
  step.value = 3
  emit('submitted', {
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
.modal-overlay{ position:fixed; inset:0; background:rgba(0,0,0,.6); display:flex; align-items:center; justify-content:center; z-index:1001; }
.modal-sheet{ width:min(1200px,95vw); height:min(850px,50vw); background:#fff; border-radius:16px; box-shadow:0 24px 80px rgba(0,0,0,.35); overflow:hidden; animation:pop .2s ease; }
@keyframes pop{ from{ transform:translateY(10px) scale(.98); opacity:.7 } to{ transform:none; opacity:1 } }

.sheet-header{ display:flex; align-items:center; justify-content:space-between; padding:16px 20px; border-bottom:1px solid #eee }
.sheet-title{ font-size:18px; font-weight:800; margin:0 }
.icon-btn{ width:36px; height:36px; border:none; background:transparent; font-size:28px; color:#666; border-radius:50%; cursor:pointer }
.icon-btn:hover{ background:#f5f5f5; color:#222 }

/* STEP1/2 레이아웃 */
.sheet-body{ display:grid; grid-template-columns: 1fr 0.8fr; gap:24px; padding-left:36px; padding-right:36px; padding-top:12px; }
.left{ height:600px; }
.left .item-title{ font-size:24px; font-weight:800; margin:10px 0 12px }
.item-title{ display:-webkit-box; -webkit-box-orient:vertical; -webkit-line-clamp:2; line-clamp:2; overflow:hidden; }
.badge-row{ display:flex; flex-wrap:wrap; gap:10px; margin-bottom:12px }
.chip{ border:1px solid #ddd; background:#fff; border-radius:999px; padding:8px 24px; cursor:pointer; font-size:16px; font-weight:700; user-select:none }
.chip.active{ border-color:#FC703C; color:#FC703C }
.chip.disabled{ opacity:.6; cursor:not-allowed }
.main-image-container{ position:relative; width:100%; aspect-ratio:1; border-radius:12px; overflow:hidden; background:#f8f9fa; margin-bottom:12px; box-shadow:0 4px 20px rgba(0,0,0,.1) }
.main-image{ width:100%; height:100%; object-fit:cover }
.nav-btn{ position:absolute; top:50%; transform:translateY(-50%); width:40px; height:40px; border-radius:50%; border:none; background:#fff; font-weight:700; font-size:18px; color:#333; cursor:pointer }
.nav-btn:disabled{ opacity:.35; cursor:not-allowed }
.prev-btn{ left:10px } .next-btn{ right:10px }
.image-indicators{ position:absolute; bottom:10px; left:50%; transform:translateX(-50%); display:flex; gap:6px }
.indicator{ width:8px; height:8px; border-radius:50%; background:rgba(255,255,255,.6); cursor:pointer }
.indicator.active{ background:#fff; transform:scale(1.2) }

/* 약관/주의사항 */
.terms{ border:1px solid #eee; border-radius:12px; padding:16px; display:flex; flex-direction:column; gap:10px }
.terms-title{ font-weight:900; }
.terms-subtitle{ margin:16px 0 8px; font-weight:700; font-size:14px; color:#444; }
.term{ display:flex; gap:8px; align-items:flex-start; line-height:1.5 }
.term.all{ border-top:1px dashed #eee; padding-top:10px; margin-top:4px; }

.right{ display:flex; flex-direction:column; gap:12px }
.summary{ border-radius:12px; border:1px solid #eee; padding:16px }
.price-input{ width:90%; padding:8px 12px; font-size:20px; font-weight:800; border:1px solid #ddd; border-radius:8px; margin:6px 0 12px; }
.price-input:focus{ outline:none; border-color:#FC703C }
input[type="number"]{ appearance:textfield; -moz-appearance:textfield; -webkit-appearance:none; }
.muted{ color:#333; font-weight:900; letter-spacing:.02em }
.fees{ display:grid; gap:8px; margin:8px 0 12px }
.fee{ display:flex; justify-content:space-between; color:#6b6b6b; font-weight:600 }
.total{ display:flex; justify-content:space-between; border-top:1px dashed #eee; padding-top:12px; align-items:center }
.total-amt{ font-size:22px; font-weight:900; color:#111 }
.card{ border:1px solid #eee; border-radius:12px; padding:12px; margin-top:12px }
.card-title{ font-weight:800; margin-bottom:8px; font-size: 18px;}
.chart-placeholder{ height:140px; background:#f6f7f9; border-radius:10px; display:grid; place-items:center; color:#999 }
.memo-box{ border:1px solid #eee; border-radius:12px; padding:12px }
.memo-title{ color:#999; font-weight:800; margin-bottom:6px }
textarea{ resize:none; width:95%; border:1px solid #e6e6e6; border-radius:10px; padding:10px; font:inherit }

/* STEP3 */
.sheet-body.single{ display:block; padding:24px 36px; }
.completion{ max-width:720px; margin:0 auto; display:flex; flex-direction:column; gap:16px }
.success-title{ font-size:24px; font-weight:900; text-align:center; margin-bottom:16px; }
.success-sub{ color:#666; text-align:center; }

.progress{
  position:relative; display:flex; justify-content:space-between; gap:12px;
  list-style:none; padding:20px 6px; margin:8px 0 16px; border:1px solid #eee; border-radius:12px;
}
.progress::before{ content:''; position:absolute; left:24px; right:24px; top:48px; height:2px; background:#eee; }
.progress li{ position:relative; text-align:center; flex:1 }
.progress .dot{ width:14px; height:14px; border-radius:50%; background:#ddd; display:inline-block; position:relative; z-index:1; }
.progress .label{ display:block; margin-top:8px; font-weight:700; font-size:13px; color:#777; padding-top:8px; }
.progress li.active .dot{ background:#FC703C; box-shadow:0 0 0 6px rgba(252,112,60,.15) }
.progress li.active .label{ color:#FC703C }
.progress li.complete .dot{ background:#27ae60 }
.progress li.complete .label{ color:#27ae60 }

.address-card{ border:1px solid #eee; border-radius:12px; padding:16px; }
.addr-grid{ display:grid; grid-template-columns:1fr 1fr; gap:16px; margin-top:16px; margin-bottom:16px; }
.addr-grid .row{ grid-column:1 / -1 }
.addr-grid .k{ display:inline-block; min-width:76px; font-weight:800; color:#555; margin-right:6px }
.addr-grid .v{ color:#222; font-weight:700 }
.addr-grid .code{ font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace; letter-spacing:.03em }
.addr-actions{ display:flex; align-items:center; gap:8px; margin-top:10px }
.copied{ color:#27ae60; font-weight:800; font-size:12px }
.addr-note{ margin-top:16px; color:#777; font-size:16px;}

.actions{ display:flex; justify-content:flex-end; gap:8px; }
.actions.two{ justify-content:space-between; }
.actions.end{ justify-content:flex-end; }

button.ghost{ width:100px; height:48px; border:1px solid #ddd; background:#fff; border-radius:10px; font-weight:800; cursor:pointer }
button.ghost:hover{ background:#f6f6f6 }
.cta{ width:100px; height:48px; border:none; border-radius:10px; background:#FC703C; color:#fff; font-weight:800; cursor:pointer }
.cta:disabled{ opacity:.4; cursor:not-allowed }
.cta:not(:disabled):hover{ background:#f04005 }
.align-right { align-self: flex-end; }

@media (max-width: 980px){
  .sheet-body{ grid-template-columns:1fr }
  .addr-grid{ grid-template-columns:1fr }
  .progress::before{ left:12px; right:12px; top:48px }
}

</style>

