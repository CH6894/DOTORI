<!-- src/components/ModalSellConfirm.vue -->
<template>
  <teleport to="body">
    <!-- 0-1: 오버레이 클릭으로 닫히지 않음 (onClose 제거) -->
    <div class="modal-overlay" role="dialog" aria-modal="true">
      <div class="modal-sheet" role="document">
        <!-- Header -->
        <header class="sheet-header">
          <!-- 0-2: 2,3단계에만 뒤로가기 아이콘 노출 -->
          <button v-if="step === 2 || step === 3" class="icon-btn back" aria-label="뒤로가기" @click="goBack">
            ←
          </button>

          <h2 class="sheet-title">
            <template v-if="step < 4">판매 등록 확인</template>
            <template v-else>판매 신청 완료</template>
          </h2>

          <button class="icon-btn" @click="onClose" aria-label="닫기">×</button>
        </header>

        <!-- STEP 1: 요약/가격 -->
        <section class="sheet-body" v-if="step === 1">
          <!-- 좌 -->
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

          <!-- 우 -->
          <aside class="right">
            <div class="summary">
              <div class="muted">판매 등록가</div>
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

            <!-- 1-1: 그래프 영역(메모 위) -->
            <div class="card">
              <div class="card-title">시세</div>
              <div class="chart">
                <svg :viewBox="`0 0 ${chartW} ${chartH}`" width="100%" height="140" role="img" aria-label="최근 시세 추이">
                  <polyline :points="sparkPoints" fill="none" stroke="currentColor" stroke-width="2"
                    vector-effect="non-scaling-stroke" />
                </svg>
              </div>
            </div>

            <!-- 메모 -->
            <div class="memo-box">
              <div class="memo-title">판매자 코멘트</div>
              <textarea v-model="memo" rows="3" maxlength="50" @input="enforceLimit" placeholder="구성품/특이사항 메모 (최대 50자)"
                aria-label="판매자 코멘트"></textarea>
              <div class="memo-count">{{ memo.length }}/50</div>
            </div>

            <label class="confirm">
              <input type="checkbox" v-model="confirmedStep1" />
              판매하려는 상품이 맞습니다.
            </label>

            <!-- 1-2: 칩 + 가격>0 + 체크 3요건 -->
            <button class="cta align-right" :disabled="!canGoStep2" @click="goStep2">
              계속하기
            </button>
          </aside>
        </section>

        <!-- STEP 2: 사진 업로드·검수 요청 -->
        <section class="sheet-body single" v-else-if="step === 2">
          <div class="uploader">
            <div class="uploader__intro">
              <h3 class="uploader__title">상품 사진 업로드 · 검수 요청</h3>
              <p class="uploader__desc">
                정면/후면/라벨/스크래치 등 <strong>여러 각도</strong>로 촬영한 이미지를 올려주세요.
                최소 {{ MIN_FILES }}장, 최대 {{ MAX_FILES }}장 · 파일당 최대 {{ MAX_MB }}MB.
              </p>
            </div>

            <!-- 2-1: 게이지/텍스트 분리(겹침 방지) -->
            <div v-if="uploading" class="uploader__progress-wrap">
              <div class="uploader__progress" role="progressbar" :aria-valuenow="progress" aria-valuemin="0"
                aria-valuemax="100">
                <div class="uploader__progress-bar" :style="{ width: progress + '%' }"></div>
              </div>
              <div class="uploader__progress-text" aria-live="polite">
                검수 요청 준비 중… {{ progress }}%
              </div>
            </div>

            <section class="uploader__dropzone" :class="{ 'uploader__dropzone--drag': isDragOver }">
              <input ref="fileInput" class="uploader__input" type="file" multiple accept="image/*,.jpg,.jpeg,.png,.webp"
                :disabled="uploading" @change="onFilePick" />
              <div class="uploader__content" @click="openPicker" @dragover.prevent="onDragOver"
                @dragleave.prevent="onDragLeave" @drop.prevent="onDrop">
                <div class="uploader__icon" aria-hidden="true">📸</div>
                <p class="uploader__hint">이미지를 끌어오거나 클릭해서 선택</p>
                <p class="uploader__sub">대표 이미지는 <b>첫 번째</b>로 지정됩니다.</p>
              </div>
            </section>

            <ul v-if="errors.length" class="uploader__errors" role="alert">
              <li v-for="(e, i) in errors" :key="i">{{ e }}</li>
            </ul>

            <section v-if="items.length" class="uploader__grid">
              <article v-for="(it, idx2) in items" :key="it.id" class="uploader__card">
                <img :src="it.preview" :alt="`미리보기 ${idx2 + 1}`" class="uploader__img" />
                <div class="uploader__meta">
                  <span class="uploader__badge">{{ idx2 === 0 ? '대표' : '보조' }}</span>
                  <span class="uploader__size">{{ formatBytes(it.file.size) }}</span>
                </div>
                <div class="uploader__actions">
                  <button class="btn btn--ghost" @click="move(idx2, -1)" :disabled="idx2 === 0 || uploading"
                    aria-label="왼쪽으로">←</button>
                  <button class="btn btn--ghost" @click="move(idx2, 1)"
                    :disabled="idx2 === items.length - 1 || uploading" aria-label="오른쪽으로">→</button>
                  <button class="btn btn--ghost danger" @click="remove(idx2)" :disabled="uploading">삭제</button>
                </div>
                <label class="uploader__caption">
                  <span class="uploader__caption-label">설명(선택)</span>
                  <input class="uploader__caption-input" type="text" maxlength="60" placeholder="예) 상자 모서리 스크래치 있음"
                    v-model="it.caption" :disabled="uploading" />
                </label>
              </article>
            </section>

            <div class="uploader__footer">
              <label class="confirm">
                <input type="checkbox" v-model="confirmedUpload" :disabled="uploading" />
                <span>해당 이미지가 내가 판매할 실제 상품임을 확인합니다.</span>
              </label>
              <div class="spacer" />
              <button class="btn btn--primary" :disabled="!canUploadSubmit || uploading" @click="submitUploadThenNext">
                검수 요청하기
              </button>
            </div>

            <p class="uploader__fine">
              ※ 업로드 시 메타데이터(EXIF)는 보안상 제거될 수 있으며, 부적절한 이미지(저작권/개인정보/광고)는 반려됩니다.
            </p>
          </div>
        </section>

        <!-- STEP 3: 약관 동의 -->
        <section class="sheet-body" v-else-if="step === 3">
          <!-- 좌: 대표/썸네일 -->
          <div class="left">
            <!-- 3-1: 대표 600x600 정방형 -->
            <div class="rep">
              <img :src="repSrc" alt="대표 이미지" />
            </div>
            <!-- 3-1: 보조 100x100 정방형 썸네일 그리드 -->
            <div class="thumbs" v-if="otherThumbs.length">
              <button v-for="(src, i) in otherThumbs" :key="src + i" class="thumbs__item" @click="makeRep(i + 1)"
                :aria-label="`대표로 변경 ${i + 2}번째 이미지`" title="대표로 변경">
                <img :src="src" alt="보조 이미지 썸네일" />
              </button>
            </div>

            <div class="summary compact">
              <div class="muted">판매 등록가</div>
              <div class="total">
                <span>합계</span>
                <span class="total-amt">{{ format(total) }}원</span>
              </div>
            </div>
          </div>

          <!-- 우: 약관 -->
          <aside class="right">
            <div class="terms">
              <div class="terms-title">약관 및 주의사항 동의</div>

              <div class="terms-subtitle">약관 동의</div>
              <label v-for="item in termItems" :key="item.key" class="term">
                <input type="checkbox" v-model="terms[item.key]" />
                <span v-html="item.text"></span>
              </label>

              <div class="terms-subtitle">주의사항 동의</div>
              <label v-for="item in noticeItems" :key="item.key" class="term">
                <input type="checkbox" v-model="notices[item.key]" />
                <span v-html="item.text"></span>
              </label>

              <label class="term all">
                <input type="checkbox" :checked="allAgreed" @change="toggleAll" />
                전체 동의
              </label>

              <div class="actions two">
                <!-- 0-2: 본문 뒤로가기 제거(헤더에서 처리) -->
                <span aria-hidden="true"></span>
                <button type="button" class="cta" :disabled="!allAgreed" @click="submitAll">제출</button>
              </div>
            </div>
          </aside>
        </section>

        <!-- STEP 4: 완료 -->
        <section class="sheet-body single" v-else-if="step === 4">
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
                택배 파손 방지를 위해 완충재를 충분히 사용해 주세요.
              </div>
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
import { createInspection } from '@/api/inspection' // 경로는 프로젝트에 맞게

type Condition = 'excellent' | 'good' | 'fair' | 'poor'
type Item = { id: string | number; title: string; images?: string[]; condition?: Condition; price?: number }
type FeeConfig = { inspect: 'free' | number; fee: 'free' | number; shipping: 'seller' | 'buyer' | number }

const MAX_PRICE = 1_000_000_000 - 1
const STAGES = ['신청 확인 중', '입고 확인', '검수 중', '등록 대기중', '등록'] as const

/* 업로드 제약 */
const MIN_FILES = 3
const MAX_FILES = 8
const MAX_MB = 10

/* 0-2: 헤더 뒤로가기 */
function goBack() {
  if (step.value === 2) step.value = 1
  else if (step.value === 3) step.value = 2
}

const props = defineProps<{ item: Item; price?: number; feeConfig?: FeeConfig }>()
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'submit', payload: { item: Item; price: number; memo: string; total: number; selectedChip: string | null }): void
  (e: 'submitted', payload: { item: Item; price: number; memo: string; total: number; selectedChip: string | null; files: Array<{ file: File; caption?: string; isCover: boolean }> }): void
}>()

/* 갤러리(좌측) */
const idx = ref(0)
const images = computed<string[]>(() => props.item.images?.length ? props.item.images : ['/img/placeholder.jpg'])
const currentImage = computed(() => images.value[idx.value])
function setIdx(i: number) { if (i >= 0 && i < images.value.length) idx.value = i }
function prev() { if (idx.value > 0) idx.value-- }
function next() { if (idx.value < images.value.length - 1) idx.value++ }

/* 칩 */
const chips = ['개봉', '미개봉'] as const
const selectedChip = ref<string | null>(null)
function toggleChip(chip: string) {
  selectedChip.value = selectedChip.value === chip ? null : chip
}

/* 가격 입력 (1-2: NaN 방지, 빈 값→0) */
const safeNum = (n: any) => Number.isFinite(Number(n)) ? Number(n) : 0
const initialPrice = safeNum(props.price ?? props.item.price ?? 0)
const localPrice = ref<number>(Math.min(initialPrice, MAX_PRICE))
const priceText = ref<string>(format(localPrice.value))
function digitsOnly(s: string) { return s.replace(/[^\d]/g, '') }
function handlePriceInput(e: Event) {
  const input = e.target as HTMLInputElement
  let digits = digitsOnly(input.value)
  if (digits === '') digits = '0'
  digits = String(Number(digits))
  let num = safeNum(digits)
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

/* 수수료/배송 */
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
const feeValue = (v: 'free' | number) => v === 'free' ? 0 : v
const shipValue = () => typeof feeCfg.value.shipping === 'number' ? feeCfg.value.shipping : 0
const total = computed<number>(() => Math.max(0, price.value - feeValue(feeCfg.value.fee) - feeValue(feeCfg.value.inspect) - (feeCfg.value.shipping === 'seller' ? shipValue() : 0)))

/* 1단계 확인/메모 */
const confirmedStep1 = ref(false)
const memo = ref('')
function enforceLimit(e: Event) {
  const t = e.target as HTMLTextAreaElement
  if (t.value.length > 50) t.value = t.value.slice(0, 50)
  memo.value = t.value
}

/* 1-2 다음 단계 가능 여부 */
const canGoStep2 = computed(() => !!selectedChip.value && price.value > 0 && confirmedStep1.value)
function goStep2() { if (canGoStep2.value) step.value = 2 }

/* 1-1: 스파클라인 데이터 */
const chartW = 300, chartH = 100
const series = ref<number[]>([14, 16, 13, 18, 22, 21, 25, 24, 26, 23, 27, 30])
const sparkPoints = computed(() => {
  if (!series.value.length) return ''
  const xs = series.value
  const min = Math.min(...xs), max = Math.max(...xs)
  const n = xs.length - 1 || 1
  return xs.map((v, i) => {
    const x = (i / n) * chartW
    const y = chartH - (max === min ? chartH / 2 : ((v - min) / (max - min)) * chartH)
    return `${x},${y}`
  }).join(' ')
})

/* 업로더(2단계) */
const fileInput = ref<HTMLInputElement | null>(null)
const isDragOver = ref(false)
const items = ref<{ id: string; file: File; preview: string; caption: string }[]>([])
const errors = ref<string[]>([])
const uploading = ref(false)
const progress = ref(0)
const confirmedUpload = ref(false)

const canUploadSubmit = computed(() => items.value.length >= MIN_FILES && items.value.length <= MAX_FILES && confirmedUpload.value)
const openPicker = () => fileInput.value?.click()
function onDragOver() { isDragOver.value = true }
function onDragLeave() { isDragOver.value = false }
function onDrop(e: DragEvent) {
  isDragOver.value = false
  if (!e.dataTransfer?.files) return
  addFiles(e.dataTransfer.files)
}
function onFilePick(e: Event) {
  const input = e.target as HTMLInputElement
  if (!input.files) return
  addFiles(input.files)
  input.value = ''
}
function addFiles(fileList: FileList) {
  errors.value = []
  const queue = Array.from(fileList)
  if (items.value.length + queue.length > MAX_FILES) errors.value.push(`최대 ${MAX_FILES}장까지 업로드할 수 있어요.`)
  for (const f of queue) {
    if (!/^image\/(jpeg|png|webp|jpg)/i.test(f.type)) { errors.value.push(`${f.name} · 지원하지 않는 형식`); continue }
    if (f.size > MAX_MB * 1024 * 1024) { errors.value.push(`${f.name} · ${MAX_MB}MB 초과`); continue }
    if (items.value.length >= MAX_FILES) break
    const id = crypto.randomUUID()
    const preview = URL.createObjectURL(f)
    items.value.push({ id, file: f, preview, caption: '' })
  }
}
function remove(index: number) {
  const [removed] = items.value.splice(index, 1)
  if (removed) URL.revokeObjectURL(removed.preview)
}
function move(index: number, dir: -1 | 1) {
  const t = index + dir
  if (t < 0 || t >= items.value.length) return
  const tmp = items.value[index]
  items.value[index] = items.value[t]
  items.value[t] = tmp
}
function formatBytes(bytes: number) {
  if (bytes < 1024) return `${bytes} B`
  const kb = bytes / 1024
  if (kb < 1024) return `${kb.toFixed(1)} KB`
  const mb = kb / 1024
  return `${mb.toFixed(2)} MB`
}

async function submitUploadThenNext() {
  if (!canUploadSubmit.value) return
  uploading.value = true
  progress.value = 0
  await new Promise<void>((resolve) => {
    const timer = setInterval(() => {
      progress.value += Math.max(1, Math.round(Math.random() * 8))
      if (progress.value >= 100) { progress.value = 100; clearInterval(timer); resolve() }
    }, 120)
  })
  uploading.value = false
  step.value = 3
}

/* 3단계 약관 */
type TermKeys = 'agree1' | 'agree2' | 'agree3' | 'agree4'
type NoticeKeys = 'notice1' | 'notice2' | 'notice3' | 'notice4'
const terms = reactive<Record<TermKeys, boolean>>({ agree1: false, agree2: false, agree3: false, agree4: false })
const notices = reactive<Record<NoticeKeys, boolean>>({ notice1: false, notice2: false, notice3: false, notice4: false })

const termItems: ReadonlyArray<{ key: TermKeys; text: string }> = [
  { key: 'agree1', text: '<b>[필수]</b> 판매 물품은 정품이며 허위·과장 정보가 없음을 확인합니다.' },
  { key: 'agree2', text: '<b>[필수]</b> 거래 취소/반품 정책을 확인하였으며 위반 시 제재에 동의합니다.' },
  { key: 'agree3', text: '<b>[필수]</b> 개인정보 처리방침 및 결제 대행 이용에 동의합니다.' },
  { key: 'agree4', text: '<b>[필수]</b> 분쟁 발생 시 플랫폼의 중재 절차에 협조합니다.' },
] as const
const noticeItems: ReadonlyArray<{ key: NoticeKeys; text: string }> = [
  { key: 'notice1', text: '<b>[필수]</b> 위조·모조·불법 복제품이 아닌 정품만 등록·발송합니다.' },
  { key: 'notice2', text: '<b>[필수]</b> 상품 설명과 실제 상태/구성품이 일치하며, 하자·사용 흔적은 고지했습니다.' },
  { key: 'notice3', text: '<b>[필수]</b> 파손·오염 방지를 위해 안전 포장하고, 추적 가능한 배송수단으로 발송합니다.' },
  { key: 'notice4', text: '<b>[필수]</b> 검수 불합격 시 거래 취소 및 왕복 배송비 등 비용은 판매자 부담에 동의합니다.' },
] as const

const allAgreed = computed(() => (Object.values(terms) as boolean[]).every(Boolean) && (Object.values(notices) as boolean[]).every(Boolean))
function toggleAll(e: Event) {
  const v = (e.target as HTMLInputElement).checked
    ; (Object.keys(terms) as TermKeys[]).forEach(k => terms[k] = v)
    ; (Object.keys(notices) as NoticeKeys[]).forEach(k => notices[k] = v)
}

/* 3-1 대표/썸네일 렌더 */
const repSrc = computed(() => items.value.length ? items.value[0].preview : '/img/placeholder.jpg')
const otherThumbs = computed(() => items.value.slice(1).map(i => i.preview))
function makeRep(otherIndex1Based: number) {
  const i = otherIndex1Based
  if (i < 1 || i >= items.value.length) return
  const [picked] = items.value.splice(i, 1)
  items.value.unshift(picked)
}

/* 완료 단계 진행바 표기 */
const currentStage = ref(0)
function stepClass(i: number) { return { complete: i < currentStage.value, active: i === currentStage.value, pending: i > currentStage.value } }

/* 공통 유틸 */
const copied = ref(false)
async function copyAddress() {
  const text = `광주광역시 동구 중앙로 196, 302호(광주빌딩) `
  try { await navigator.clipboard.writeText(text); copied.value = true; setTimeout(() => copied.value = false, 1500) } catch { }
}
function format(n: number) { return n.toLocaleString('ko-KR') }
function onClose() { emit('close') }

/* 최종 제출 → 4단계 */
const isSubmitting = ref(false)
const step = ref<1 | 2 | 3 | 4>(1)
const userId = 1

const normalizedPrice = price.toString().replace(/,/g, "")

async function submitAll() {
  if (!allAgreed.value || isSubmitting.value) {
    if (!allAgreed.value) alert('필수 항목에 모두 동의해주세요.');
    return isSubmitting.value = true
  }
  try {
    const fd = new FormData()
    fd.append('userId', String(userId))
    fd.append('productTitle', props.item.title)
    fd.append("price", String(price.value ?? 0))
    fd.append('unpacked', selectedChip.value === '미개봉' ? '0' : '1')
    fd.append('memo', memo.value ?? '')
    
    items.value.forEach(i => fd.append('images', i.file))

    for (const [key, value] of fd.entries()) {
      console.log("FormData:", key, value)
    }
    items.value.forEach(i => fd.append('images', i.file))

    const res = await createInspection(fd)
    console.log('created:', res) // { inspectionId, itemId, status }

    step.value = 4 // 완료 페이지로 이동
    await nextTick()
  } catch (e) {
    console.error(e)
    alert('판매 신청 중 오류가 발생했습니다.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
/* Overlay & Sheet */
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
  width: min(1200px, 95vw);
  height: min(850px, 50vw);
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
  font-size: 24px;
  color: #666;
  border-radius: 50%;
  cursor: pointer
}

.icon-btn:hover {
  background: #f5f5f5;
  color: #222
}

.icon-btn.back {
  margin-right: 8px
}

/* Layout */
.sheet-body {
  display: grid;
  grid-template-columns: 1fr 0.8fr;
  gap: 24px;
  padding: 12px 36px;
}

.sheet-body.single {
  display: block;
  margin-top: 60px;
  padding: 24px 36px;
}

.left {
  min-height: 560px;
  max-width: 550px;
}

.item-title {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  overflow: hidden;
  font-size: 24px;
  font-weight: 800;
  margin: 10px 0 12px
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

/* Image viewer */
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

/* Right summary */
.right {
  display: flex;
  flex-direction: column;
  gap: 12px
}

.summary {
  border-radius: 12px;
  border: 1px solid #eee;
  padding: 16px;
}

.summary.compact {
  padding: 12px;
  width: 500px;
}

.price-input {
  width: 90%;
  padding: 10px 12px;
  font-size: 20px;
  font-weight: 800;
  border: 1px solid #ddd;
  border-radius: 8px;
  margin: 6px 0 12px;
}

.price-input:focus {
  outline: none;
  border-color: #FC703C;
  box-shadow: 0 0 0 3px rgba(252, 112, 60, .15);
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
  font-weight: 800;
  margin-bottom: 8px;
  font-size: 18px;
}

/* 1-1: 그래프 */
.chart {
  height: 140px;
  display: block;
  color: #6366f1;
}

/* 메모 */
.memo-box {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 12px;
  background: #fafafa;
}

.memo-title {
  color: #555;
  font-weight: 800;
  margin-bottom: 6px
}

textarea {
  resize: none;
  width: 100%;
  min-height: 84px;
  border: 1px solid #e1e1e1;
  border-radius: 10px;
  padding: 10px 12px;
  font: inherit;
  background: #fff
}

textarea:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, .15);
}

.memo-count {
  text-align: right;
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px
}

.confirm {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  color: #333;
  margin-top: 6px
}

/* Buttons */
.actions {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.actions.end {
  justify-content: flex-end;
}

button.ghost {
  width: 100px;
  height: 48px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 10px;
  font-weight: 800;
  cursor: pointer
}

button.ghost:hover {
  background: #f6f6f6
}

.cta {
  width: 120px;
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

.align-right {
  align-self: flex-end
}

/* Uploader (Step2) */
.uploader {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 12px
}

.uploader__intro {
  margin-bottom: 4px
}

.uploader__title {
  font-size: 20px;
  font-weight: 800;
  margin: 0 0 4px
}

.uploader__desc {
  color: #555
}

/* 2-1: 게이지/텍스트 분리 */
.uploader__progress-wrap {
  margin-top: 6px
}

.uploader__progress {
  height: 10px;
  background: #f3f4f6;
  border-radius: 10px;
  overflow: hidden
}

.uploader__progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #FC703C, #f5f2c6);
  transition: width .2s
}

.uploader__progress-text {
  margin-top: 6px;
  font-size: 12px;
  color: #6b7280
}

.uploader__dropzone {
  border: 2px dashed #d1d5db;
  border-radius: 14px;
  padding: 18px;
  background: #fafafa;
}

.uploader__dropzone--drag {
  background: #eef2ff;
  border-color: #6366f1;
}

.uploader__input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.uploader__content {
  text-align: center;
  cursor: pointer;
}

.uploader__icon {
  font-size: 32px;
  margin-bottom: 8px
}

.uploader__hint {
  font-weight: 700
}

.uploader__sub {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px
}

.uploader__errors {
  margin: 8px 0 0;
  padding: 10px 12px;
  background: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
  border-radius: 10px;
  font-size: 13px;
}

.uploader__errors li+li {
  margin-top: 4px
}

.uploader__grid {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
}

.uploader__card {
  position: relative;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.uploader__img {
  width: 100%;
  aspect-ratio: 1/1;
  object-fit: cover;
  background: #f3f4f6;
}

.uploader__meta {
  position: absolute;
  top: 8px;
  left: 8px;
  display: flex;
  gap: 6px;
  align-items: center;
}

.uploader__badge {
  background: #111827;
  color: #fff;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 999px;
}

.uploader__size {
  background: #ffffffd9;
  border: 1px solid #e5e7eb;
  backdrop-filter: blur(2px);
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 999px;
}

.uploader__actions {
  display: flex;
  gap: 6px;
  padding: 8px;
  justify-content: space-between;
}

.uploader__caption {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 0 10px 10px;
}

.uploader__caption-label {
  font-size: 12px;
  color: #6b7280
}

.uploader__caption-input {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 8px 10px;
  font-size: 14px
}

.uploader__footer {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 6px;
}

.btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 8px 14px;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer
}

.btn:disabled {
  opacity: .6;
  cursor: not-allowed
}

.btn--ghost {
  background: #fff
}

.btn--primary {
  background: #111827;
  color: #fff;
  border-color: #111827
}

.btn--ghost.danger {
  color: #dc2626;
  border-color: #fecaca
}

.spacer {
  flex: 1
}

.uploader__fine {
  margin: 6px 2px 2px;
  color: #6b7280;
  font-size: 12px
}

/* Step3: 대표/썸네일 고정 크기(3-1) */
.rep {
  width: 500px;
  max-width: 100%;
  height: 500px;
  border-radius: 12px;
  overflow: hidden;
  background: #f6f7f9;
  display: grid;
  place-items: center;
  margin-bottom: 10px;
}

.rep img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbs {
  display: grid;
  grid-template-columns: repeat(5, 100px);
  gap: 8px;
  margin-bottom: 12px;
}

.thumbs__item {
  width: 100px;
  height: 100px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0;
  background: #fff;
  cursor: pointer;
  overflow: hidden;
}

.thumbs__item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 3-1: 약관 CSS 강화 */
.terms {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #fff;
}

.terms-title {
  font-weight: 900;
  font-size: 18px;
}

.terms-subtitle {
  margin: 12px 0 6px;
  font-weight: 700;
  font-size: 14px;
  color: #444;
}

.term {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  line-height: 1.55;
  padding: 6px 0;
}

.term input {
  margin-top: 3px
}

.term.all {
  border-top: 1px dashed #eee;
  padding-top: 10px;
  margin-top: 4px;
}

/* Step4 */
.completion {
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px
}

.success-title {
  font-size: 24px;
  font-weight: 900;
  text-align: center;
  margin-bottom: 16px;
}

.success-sub {
  color: #666;
  text-align: center;
}

.progress {
  position: relative;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  list-style: none;
  padding: 20px 6px;
  margin: 8px 0 16px;
  border: 1px solid #eee;
  border-radius: 12px;
}

.progress::before {
  content: '';
  position: absolute;
  left: 24px;
  right: 24px;
  top: 48px;
  height: 2px;
  background: #eee;
}

.progress li {
  position: relative;
  text-align: center;
  flex: 1
}

.progress .dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #ddd;
  display: inline-block;
  position: relative;
  z-index: 1;
}

.progress .label {
  display: block;
  margin-top: 8px;
  font-weight: 700;
  font-size: 13px;
  color: #777;
  padding-top: 8px;
}

.progress li.active .dot {
  background: #FC703C;
  box-shadow: 0 0 0 6px rgba(252, 112, 60, .15)
}

.progress li.active .label {
  color: #FC703C
}

.progress li.complete .dot {
  background: #27ae60
}

.progress li.complete .label {
  color: #27ae60
}

.address-card {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 16px;
}

.addr-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 16px;
  margin-bottom: 16px;
}

.addr-grid .row {
  grid-column: 1 / -1
}

.addr-grid .k {
  display: inline-block;
  min-width: 76px;
  font-weight: 800;
  color: #555;
  margin-right: 6px
}

.addr-grid .v {
  color: #222;
  font-weight: 700
}

.addr-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px
}

.copied {
  color: #27ae60;
  font-weight: 800;
  font-size: 12px
}

.addr-note {
  margin-top: 16px;
  color: #777;
  font-size: 16px;
}

/* Responsive */
@media (max-width: 980px) {
  .sheet-body {
    grid-template-columns: 1fr
  }

  .addr-grid {
    grid-template-columns: 1fr
  }

  .progress::before {
    left: 12px;
    right: 12px;
    top: 48px
  }
}
</style>
