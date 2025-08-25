<template>
  <!-- body 최상단으로 텔레포트하여 헤더보다 위 레이어에서 그리기 -->
  <teleport to="body">
    <transition name="fade">
      <div
        v-if="modelValue"
        class="overlay"
        role="dialog"
        aria-modal="true"
        aria-labelledby="verifyTitle"
        @keydown.esc.prevent="handleClose"
      >
        <!-- Modal -->
        <div class="modal" ref="modalRef" tabindex="0">
          <!-- Header -->
          <div class="modal__header">
            <h2 id="verifyTitle" class="modal__title">상품 사진 업로드 · 검수 요청</h2>
            <button class="icon-btn" aria-label="닫기" @click="handleClose">✕</button>
          </div>

          <!-- Progress (가짜 업로드 시뮬레이션용) -->
          <div v-if="uploading" class="progress">
            <div class="progress__bar" :style="{ width: progress + '%' }" />
            <span class="progress__text">검수 요청 준비 중… {{ progress }}%</span>
          </div>

          <!-- Dropzone -->
          <section class="dropzone" :class="{ 'dropzone--drag': isDragOver }">
            <input
              ref="fileInput"
              class="dropzone__input"
              type="file"
              multiple
              accept="image/*,.jpg,.jpeg,.png,.webp"
              :disabled="uploading"
              @change="onFilePick"
            />

            <div class="dropzone__content" @click="openPicker" @dragover.prevent="onDragOver" @dragleave.prevent="onDragLeave" @drop.prevent="onDrop">
              <div class="dropzone__icon" aria-hidden="true">📸</div>
              <p class="dropzone__title">이미지를 끌어오거나 클릭해서 선택</p>
              <p class="dropzone__hint">최소 {{ MIN_FILES }}장, 최대 {{ MAX_FILES }}장 · 파일당 최대 {{ MAX_MB }}MB</p>
              <p class="dropzone__sub">정면/후면/라벨/상세 스크래치 등 <strong>여러 각도</strong>로 촬영해주세요.</p>
            </div>
          </section>

          <!-- Errors -->
          <ul v-if="errors.length" class="errors" role="alert">
            <li v-for="(e, i) in errors" :key="i">{{ e }}</li>
          </ul>

          <!-- Previews -->
          <section v-if="items.length" class="grid">
            <article v-for="(item, idx) in items" :key="item.id" class="card">
              <img :src="item.preview" :alt="`미리보기 ${idx + 1}`" class="card__img" />
              <div class="card__meta">
                <span class="badge">{{ idx === 0 ? '대표' : '보조' }}</span>
                <span class="size">{{ formatBytes(item.file.size) }}</span>
              </div>
              <div class="card__actions">
                <button class="btn btn--ghost" @click="move(idx, -1)" :disabled="idx === 0 || uploading" aria-label="왼쪽으로">←</button>
                <button class="btn btn--ghost" @click="move(idx, 1)" :disabled="idx === items.length - 1 || uploading" aria-label="오른쪽으로">→</button>
                <button class="btn btn--ghost danger" @click="remove(idx)" :disabled="uploading">삭제</button>
              </div>
              <label class="caption">
                <span class="caption__label">설명(선택)</span>
                <input class="caption__input" type="text" maxlength="60" placeholder="예) 상자 모서리 스크래치 있음" v-model="item.caption" :disabled="uploading" />
              </label>
            </article>
          </section>

          <!-- Footer / Actions -->
          <div class="modal__footer">
            <label class="confirm">
              <input type="checkbox" v-model="confirmed" :disabled="uploading" />
              <span>해당 이미지가 내가 판매할 실제 상품임을 확인합니다.</span>
            </label>

            <div class="spacer" />

            <!-- <button class="btn btn--ghost" @click="handleClose" :disabled="uploading">취소</button> -->
            <button class="btn btn--primary" :disabled="!canSubmit || uploading" @click="submit">검수 요청하기</button>
          </div>

          <p class="fine">※ 업로드 시 메타데이터(EXIF)는 보안상 제거될 수 있으며, 부적절한 이미지(저작권/개인정보/광고)는 반려됩니다.</p>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'

/**********************
 * PROPS & EMITS
 **********************/
interface EmitPayload {
  files: Array<{ file: File; caption?: string; isCover: boolean }>
}

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'submit', payload: EmitPayload): void
  (e: 'close'): void
}>()

/**********************
 * STATE
 **********************/
const MIN_FILES = 3
const MAX_FILES = 8
const MAX_MB = 10

const fileInput = ref<HTMLInputElement | null>(null)
const modalRef = ref<HTMLElement | null>(null)
const isDragOver = ref(false)
const items = ref<{ id: string; file: File; preview: string; caption: string }[]>([])
const errors = ref<string[]>([])
const confirmed = ref(false)

const uploading = ref(false)
const progress = ref(0)

const canSubmit = computed(() => items.value.length >= MIN_FILES && items.value.length <= MAX_FILES && confirmed.value)

/**********************
 * METHODS
 **********************/
const openPicker = () => fileInput.value?.click()

function onFilePick(e: Event) {
  const input = e.target as HTMLInputElement
  if (!input.files) return
  addFiles(input.files)
  input.value = '' // allow re-pick same files
}

function onDragOver() {
  isDragOver.value = true
}
function onDragLeave() {
  isDragOver.value = false
}
function onDrop(e: DragEvent) {
  isDragOver.value = false
  if (!e.dataTransfer?.files) return
  addFiles(e.dataTransfer.files)
}

function addFiles(fileList: FileList) {
  errors.value = []
  const queue = Array.from(fileList)

  // Capacity check
  if (items.value.length + queue.length > MAX_FILES) {
    errors.value.push(`최대 ${MAX_FILES}장까지 업로드할 수 있어요.`)
  }

  for (const f of queue) {
    if (!/^image\/(jpeg|png|webp|jpg)/i.test(f.type)) {
      errors.value.push(`${f.name} · 지원하지 않는 형식`)
      continue
    }
    if (f.size > MAX_MB * 1024 * 1024) {
      errors.value.push(`${f.name} · ${MAX_MB}MB 초과`)
      continue
    }
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
  const target = index + dir
  if (target < 0 || target >= items.value.length) return
  const tmp = items.value[index]
  items.value[index] = items.value[target]
  items.value[target] = tmp
}

function formatBytes(bytes: number) {
  if (bytes < 1024) return `${bytes} B`
  const kb = bytes / 1024
  if (kb < 1024) return `${kb.toFixed(1)} KB`
  const mb = kb / 1024
  return `${mb.toFixed(2)} MB`
}

function reset() {
  errors.value = []
  confirmed.value = false
  items.value.forEach(i => URL.revokeObjectURL(i.preview))
  items.value = []
  progress.value = 0
  uploading.value = false
}

function handleClose() {
  if (uploading.value) return
  emit('update:modelValue', false)
  emit('close')
  // 포커스 반환을 위해 약간의 지연 후 리셋
  nextTick(() => reset())
}

async function submit() {
  if (!canSubmit.value) return
  uploading.value = true
  progress.value = 0

  // 실제 업로드 대신 진행률 시뮬레이션
  await new Promise<void>((resolve) => {
    const timer = setInterval(() => {
      progress.value += Math.max(1, Math.round(Math.random() * 8))
      if (progress.value >= 100) {
        progress.value = 100
        clearInterval(timer)
        resolve()
      }
    }, 120)
  })

  // payload 구성(대표 이미지는 첫 번째)
  const payload = {
    files: items.value.map((i, idx) => ({ file: i.file, caption: i.caption || undefined, isCover: idx === 0 }))
  }

  emit('submit', payload)
  handleClose()
}

/**********************
 * ACCESSIBILITY: autofocus when opened
 **********************/
watch(
  () => props.modelValue,
  async (open) => {
    if (open) {
      await nextTick()
      modalRef.value?.focus()
      // ✅ 모달 열릴 때: 헤더가 뒤로 가도록 전역 클래스 추가 + 스크롤 잠금
      document.body.classList.add('modal-open')
    } else {
      // ✅ 모달 닫힐 때 복구
      document.body.classList.remove('modal-open')
    }
  }
)


onMounted(() => {
  // body 스크롤 잠금/해제 & 헤더 덮기 위한 클래스 토글은 watcher에서 처리
})
</script>

<style scoped>
/* --- Overlay & Transitions --- */
.fade-enter-active, .fade-leave-active { transition: opacity .18s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,.55); /* 약간 더 진하게 */
  display: grid; place-items: center; z-index: 5000; /* ✅ 헤더 위로 확실히 */
  padding: 24px;
}
.modal {
  width:min(1200px,95vw); 
  height:min(850px,50vw);
  overflow: auto;
  background: #fff; border-radius: 16px; box-shadow: 0 12px 40px rgba(0,0,0,.18);
  padding: 20px 20px 12px; outline: none;
}
.modal__header { display:flex; align-items:center; gap:12px; }
.modal__title { font-size: 20px; font-weight: 700; line-height: 1.3; }
.icon-btn { margin-left: auto; border: none; background: transparent; font-size: 18px; cursor: pointer; padding: 6px; }

/* --- Progress --- */
.progress { position: relative; margin: 16px 0 8px; background: #f3f4f6; border-radius: 10px; height: 10px; }
.progress__bar { position:absolute; left:0; top:0; bottom:0; background: linear-gradient(90deg,#4f46e5,#22c55e); border-radius: 10px; transition: width .2s; }
.progress__text { display:block; font-size: 12px; color: #6b7280; margin-top: 6px; }

/* --- Dropzone --- */
.dropzone { border: 2px dashed #d1d5db; border-radius: 14px; padding: 48px; background: #fafafa; margin-top: 12px; }
.dropzone--drag { background: #eef2ff; border-color: #6366f1; }
.dropzone__input { position: absolute; opacity: 0; pointer-events: none; }
.dropzone__content { text-align: center; cursor: pointer; }
.dropzone__icon { font-size: 32px; margin-bottom: 8px; }
.dropzone__title { font-weight: 600; }
.dropzone__hint, .dropzone__sub { font-size: 12px; color: #6b7280; margin-top: 4px; }
.dropzone__sub strong { color:#374151; }

/* --- Errors --- */
.errors { margin: 12px 0 0; padding: 10px 12px; background:#fef2f2; color:#991b1b; border:1px solid #fecaca; border-radius: 10px; font-size: 13px; }
.errors li + li { margin-top: 4px; }

/* --- Grid Preview --- */
.grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
}
.card { position: relative; background:#fff; border:1px solid #e5e7eb; border-radius: 12px; overflow: hidden; display:flex; flex-direction: column; }
.card__img { width: 100%; aspect-ratio: 1/1; object-fit: cover; background:#f3f4f6; }
.card__meta { position: absolute; top: 8px; left: 8px; display:flex; gap:6px; align-items:center; }
.badge { background:#111827; color:#fff; font-size: 11px; padding: 2px 6px; border-radius: 999px; }
.size { background:#ffffffd9; border:1px solid #e5e7eb; backdrop-filter: blur(2px); font-size: 11px; padding:2px 6px; border-radius: 999px; }
.card__actions { display:flex; gap:6px; padding:8px; justify-content: space-between; }
.caption { display:flex; flex-direction: column; gap: 6px; padding: 0 10px 10px; }
.caption__label { font-size: 15px; color:#6b7280; }
.caption__input { border:1px solid #e5e7eb; border-radius: 10px; padding: 8px 10px; font-size: 14px; }

/* --- Footer --- */
.modal__footer { display:flex; align-items:center; gap: 12px; margin-top: 14px; }
.confirm { display:flex; align-items:center; gap: 8px; font-size: 14px; color:#374151; }
.spacer { flex:1; }

.btn { border:1px solid #e5e7eb; background:#fff; padding:8px 14px; border-radius: 10px; font-weight:600; cursor:pointer; }
.btn:disabled { opacity:.6; cursor:not-allowed; }
.btn--ghost { background:#fff; }
.btn--primary { background:#111827; color:#fff; border-color:#111827; }
.btn--ghost.danger { color:#dc2626; border-color:#fecaca; }

.fine { margin: 10px 2px 2px; color:#6b7280; font-size: 15px; }

/* --- Mobile tweaks --- */
@media (max-width: 560px) {
  .modal { padding: 16px 12px 8px; border-radius: 12px; }
  .grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
