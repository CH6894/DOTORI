<template>
  <div class="admin-page">
    <!-- 상단 헤더(페이지 타이틀/필터) -->
    <header class="page-header">
      <h1 class="title">검수 관리</h1>
      <router-link class="btn btn--cal" :to="{ name: 'CalendarManager' }">
        일정 관리
      </router-link>
      <div class="filters">
        <label class="field">
          <span>검색</span>
          <input v-model.trim="q" type="text" placeholder="상품명/판매자/ID 검색" />
        </label>
        <label class="field">
          <span>상태</span>
          <select v-model="status">
            <option value="">전체</option>
            <option value="PENDING">대기</option>
            <option value="APPROVED">승인</option>
            <option value="REJECTED">반려</option>
          </select>
        </label>
        <label class="field">
          <span>등록일(시작)</span>
          <input v-model="dateFrom" type="date" />
        </label>
        <label class="field">
          <span>등록일(끝)</span>
          <input v-model="dateTo" type="date" />
        </label>
        <button class="btn" @click="applyFilters">필터 적용</button>
        <button class="btn btn--ghost" @click="resetFilters">초기화</button>
      </div>
    </header>

    <!-- 목록 테이블 -->
    <section class="card">
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 150px">검수 ID</th>
              <th style="width: 190px">상품명</th>
              <th style="width: 170px">판매자</th>
              <th style="width: 170px">판매 등록가</th>
              <th style="width: 170px">개봉 여부</th>
              <th style="width: 170px">등록일</th>
              <th style="width: 170px">촬영시각</th>
              <th style="width: 140px">이미지</th>
              <th style="width: 140px">상태</th>
              <th style="width: 140px">등급</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="ins in paged" :key="ins.id" @click="openReview(ins)" class="row">
              <td><code>{{ ins.id }}</code></td>
              <td>
                <div class="cell-title">{{ ins.listingTitle }}</div>
                <div class="cell-sub">#{{ ins.listingId }}</div>
              </td>
              <td>{{ ins.sellerName }}</td>
              <td>{{ ins.sellPrice.toLocaleString() }}원</td>
              <td>{{ ins.isOpened }}</td>
              <td>{{ fmt(ins.submittedAt) }}</td>
              <td>
                <span class="chip chip--internal" v-if="ins.capturedAtInternal">{{ fmt(ins.capturedAtInternal) }}</span>
                <span class="chip chip--muted" v-else>없음</span>
              </td>
              <td>{{ ins.photos?.length || 0 }}</td>
              <td>
                <span :class="['badge', `badge--${ins.status.toLowerCase()}`]">{{ toKrStatus(ins.status) }}</span>
              </td>
              <td>
                <span v-if="ins.grade" :class="['badge-grade', `badge-grade--${ins.grade.toLowerCase()}`]">
                  {{ ins.grade }}
                </span>
                <span v-else class="badge-grade badge-grade--none">-</span>
              </td>
            </tr>
            <tr v-if="!paged.length">
              <td colspan="10" class="empty">검색 조건에 맞는 항목이 없습니다.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 페이지네이션 -->
      <div class="pagination" v-if="totalPages > 1">
        <button class="btn btn--ghost" :disabled="page === 1" @click="page--">이전</button>
        <span class="page-indicator">{{ page }} / {{ totalPages }}</span>
        <button class="btn btn--ghost" :disabled="page === totalPages" @click="page++">다음</button>
      </div>
    </section>

    <!-- 사이드 드로어 (검토 패널) -->
    <teleport to="body">
      <transition name="slide">
        <div v-if="panelOpen" class="drawer-overlay" @click.self="closePanel">
          <aside class="drawer" role="dialog" aria-modal="true" aria-labelledby="drawerTitle">
            <header class="drawer__header">
              <h2 id="drawerTitle" class="drawer__title">검수 상세</h2>
              <button class="icon-btn" aria-label="닫기" @click="closePanel">✕</button>
            </header>

            <section v-if="current" class="drawer__body">
              <!-- 상단 메타 -->
              <div class="meta">
                <div class="meta__left">
                  <div class="meta__title">{{ current.listingTitle }}</div>
                  <div class="meta__sub">판매자: {{ current.sellerName }} · 등록일: {{ fmt(current.submittedAt) }}</div>
                </div>
                <div class="meta__right">
                  <span :class="['badge', `badge--${current.status.toLowerCase()}`]">{{ toKrStatus(current.status) }}</span>
                </div>
              </div>

              <!-- 내부 전용 정보 -->
              <div class="internal">
                <span class="lock" aria-hidden="true">🔒</span>
                <div class="internal__content">
                  <div><strong>촬영시각:</strong> <span>{{ current.capturedAtInternal ? fmt(current.capturedAtInternal) : '없음' }}</span></div>
                </div>
              </div>

              <!-- 이미지 미리보기 그리드 (판매자 이미지만) -->
              <h3 class="section-title">이미지 ({{ Math.min(sellerPhotos?.length || 0, 5) }})</h3>
              <div class="thumb-row">
                <figure
                  v-for="p in (sellerPhotos?.slice(0, 5) || [])"
                  :key="p.id"
                  class="thumb"
                  @click="openViewer(p)"
                >
                  <img :src="p.url" :alt="`photo ${p.id}`" />
                  <figcaption>
                    <span v-if="p.isCover" class="chip">대표</span>
                    <span class="meta">{{ p.width }}×{{ p.height }}</span>
                  </figcaption>
                </figure>
              </div>

              <!-- 판매자 메모 표시 -->
              <h3 class="section-title">판매자 메모</h3>
              <div v-if="current?.memo && current.memo.trim()" class="memo-view">
                <p class="memo-text">{{ current.memo }}</p>
              </div>
              <div v-else class="memo-empty">메모 없음</div>

              <!-- 관리자 이미지 섹션 -->
              <h3 class="section-title">관리자 검수 이미지</h3>
              <div v-if="adminImages.length" class="admin-images-section">
                <div class="admin-images-grid">
                  <div v-for="(img, idx) in adminImages" :key="idx" class="admin-image-card">
                    <img :src="img.preview" :alt="`관리자 이미지 ${idx + 1}`" class="admin-image" />
                    <div class="admin-image-actions">
                      <button class="btn btn--ghost danger" @click="removeAdminImage(idx)">삭제</button>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="admin-images-empty">
                <p>관리자 이미지가 없습니다.</p>
              </div>

              <!-- 의사결정 영역 -->
              <h3 class="section-title">검수 결정</h3>
              <div class="decision">
                <div class="decision__left">
                  <div class="reasons" v-if="decision === 'REJECTED'">
                    <span class="label">반려 사유</span>
                    <div class="checks">
                      <label v-for="r in defaultReasons" :key="r" class="check">
                        <input type="checkbox" :value="r" v-model="rejectReasons" /> {{ r }}
                      </label>
                    </div>
                  </div>

                  <div v-if="decision === 'APPROVED'" class="grade-select">
                    <label class="label">등급 선택</label>
                    <select v-model="grade">
                      <option value="">선택 없음</option>
                      <option value="S">S</option>
                      <option value="A">A</option>
                      <option value="B">B</option>
                      <option value="C">C</option>
                    </select>
                    <textarea v-model="approveNote" class="note" placeholder="추가 메모(선택)"></textarea>
                    
                    <!-- 관리자 이미지 업로드 섹션 -->
                    <div class="admin-image-upload">
                      <label class="label">관리자 이미지 업로드 </label>
                      <div class="dropzone" :class="{ 'dropzone--drag': isDragOver }" 
                           @dragover.prevent="onDragOver" @dragleave.prevent="onDragLeave" @drop.prevent="onDrop">
                        <input ref="adminFileInput" class="dropzone__input" type="file" multiple accept="image/*" 
                               @change="onAdminFilePick" style="display: none;" />
                        <div class="dropzone__content" @click.stop="openAdminPicker" 
                             @dragover.prevent="onDragOver" @dragleave.prevent="onDragLeave" @drop.prevent="onDrop">
                          <div class="dropzone__icon" aria-hidden="true">📸</div>
                          <p class="dropzone__title">이미지를 끌어오거나 클릭해서 선택</p>
                          <p class="dropzone__hint">최대 5장 · 파일당 최대 10MB</p>
                          <p class="dropzone__sub">상품 상태 확인용 <strong>추가 이미지</strong>를 업로드하세요.</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="decision__right">
                  <div class="buttons">
                    <button class="btn btn--ghost danger" :class="{ active: decision === 'REJECTED' }" @click="setReject">반려</button>
                    <button class="btn btn--primary" :class="{ active: decision === 'APPROVED' }" @click="setApprove">승인</button>
                  </div>
                </div>
              </div>
              
            </section>

            <!-- 하단 액션바 -->
            <footer class="drawer__footer">
              <button class="btn btn--ghost" @click="closePanel">닫기</button>
              <button class="btn btn--primary" :disabled="!canSubmitDecision" @click="submitDecision">결정 저장</button>
            </footer>
          </aside>
        </div>
      </transition>

      <!-- 전체 이미지 뷰어 -->
      <transition name="fade">
        <div v-if="viewerOpen" class="viewer" @click.self="viewerOpen = false">
          <img :src="viewerSrc" alt="preview" />
          <button class="icon-btn viewer__close" aria-label="닫기" @click="viewerOpen = false">✕</button>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue"
import {
  fetchInspectionsFromAdmin,
  approveInspection,
  rejectInspection,
  uploadAdminImages,
  getInspectionDecision,
  getAdminImages,
  type Inspection,
  type Photo,
  type Status,
} from "@/api/inspection"
import { fetchAdminImagesByItemDetailsId } from "@/api/items"
import axios from "axios"

const API_BASE = "http://localhost:8081/api/inspections"

type InspectionEx = Inspection & { memo?: string }

const list = ref<Inspection[]>([])
const q = ref("")
const status = ref<"" | Status>("")
const dateFrom = ref("")
const dateTo = ref("")

const page = ref(1)
const pageSize = ref(12)

const panelOpen = ref(false)
const current = ref<InspectionEx | null>(null)

const decision = ref<Status | null>(null)
const rejectReasons = ref<string[]>([])
const approveNote = ref("")
const defaultReasons = [
  "촬영 각도/장면 부족",
  "해상도/초점 문제",
  "라벨/시리얼 확인 불가",
  "상품 상태 설명 불충분",
  "광고/워터마크 포함",
]
const grade = ref<Inspection["grade"] | "">("")

const viewerOpen = ref(false)
const viewerSrc = ref("")

// 관리자 이미지 업로드 관련
const adminFileInput = ref<HTMLInputElement | null>(null)
const adminImages = ref<Array<{ file: File; preview: string }>>([])
const isDragOver = ref(false)

// ---------------------
// 판매자 이미지만 필터링
const sellerPhotos = computed(() => {
  if (!current.value?.photos) return []
  
  // 관리자 이미지 URL 패턴을 제외 (관리자 이미지는 /uploads/admin/ 경로를 사용)
  return current.value.photos.filter(photo => 
    !photo.url.includes('/uploads/admin/') && 
    !photo.url.includes('admin')
  )
})

// 필터 + 페이지네이션
// ---------------------
const filtered = computed(() => {
  const qv = q.value.toLowerCase()
  const from = dateFrom.value ? new Date(dateFrom.value) : null
  const to = dateTo.value ? new Date(dateTo.value + "T23:59:59") : null

  return list.value.filter((it) => {
    const hitQ =
      !qv ||
      it.id.toLowerCase().includes(qv) ||
      it.listingTitle.toLowerCase().includes(qv) ||
      it.sellerName.toLowerCase().includes(qv)

    const hitStatus = !status.value || it.status === status.value
    const created = new Date(it.submittedAt)
    const hitFrom = !from || created >= from
    const hitTo = !to || created <= to

    return hitQ && hitStatus && hitFrom && hitTo
  })
})


const totalPages = computed(() =>
  Math.max(1, Math.ceil(filtered.value.length / pageSize.value))
)
const paged = computed(() => {
  if (page.value > totalPages.value) page.value = totalPages.value
  const start = (page.value - 1) * pageSize.value
  return filtered.value.slice(start, start + pageSize.value)
})

const canSubmitDecision = computed(() => {
  if (!current.value || !decision.value) return false
  if (decision.value === "REJECTED")
    return rejectReasons.value.length > 0 || !!approveNote.value.trim()
  if (decision.value === "APPROVED") return true
  return false
})

// ---------------------
// 유틸
// ---------------------
function fmt(iso?: string) {
  if (!iso) return ""
  const d = new Date(iso)
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, "0")
  const dd = String(d.getDate()).padStart(2, "0")
  const hh = String(d.getHours()).padStart(2, "0")
  const mi = String(d.getMinutes()).padStart(2, "0")
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
}
function toKrStatus(s: Status) {
  return s === "PENDING" ? "대기" : s === "APPROVED" ? "승인" : "반려"
}

// ---------------------
// 동작
// ---------------------
function applyFilters() {
  page.value = 1
}
function resetFilters() {
  q.value = ""
  status.value = ""
  dateFrom.value = ""
  dateTo.value = ""
  page.value = 1
}
async function openReview(ins: Inspection) {
  current.value = { ...ins }
  panelOpen.value = true
  
  // 먼저 기본값으로 초기화 (각 상품별로 독립적인 데이터 관리)
  decision.value = null
  rejectReasons.value = []
  approveNote.value = ""
  grade.value = ""
  
  // 백엔드에서 검수 결정 정보 조회
  try {
    const decisionInfo = await getInspectionDecision(ins.id)
    console.log('백엔드에서 가져온 검수 결정 정보:', decisionInfo)
    
    if (decisionInfo) {
      // 등급 설정
      if (decisionInfo.quality) {
        grade.value = getGradeText(decisionInfo.quality)
      }
      
      // 관리자 메모 설정
      if (decisionInfo.adminNote) {
        approveNote.value = decisionInfo.adminNote
      }
      
      // 승인/반려 상태 설정 (admissionState: 0=대기, 1=승인, 2=반려)
      if (decisionInfo.admissionState === 1) {
        decision.value = "APPROVED"
      } else if (decisionInfo.admissionState === 2) {
        decision.value = "REJECTED"
        // 반려 사유는 rejectionReason에서 가져올 수 있음
      }
    }
  } catch (error) {
    console.error('검수 결정 정보 조회 실패:', error)
    // 실패 시 기본값 설정
    decision.value = null
    rejectReasons.value = []
    approveNote.value = ""
    grade.value = ""
  }
  
  // 관리자 이미지 로드
  await loadAdminImages(ins.id)
}

// 등급 숫자를 문자로 변환하는 헬퍼 함수
function getGradeText(quality: number): string {
  switch (quality) {
    case 1: return "S"
    case 2: return "A"
    case 3: return "B"
    case 4: return "C"
    default: return ""
  }
}

// 관리자 이미지 로드 함수
async function loadAdminImages(inspectionId: string) {
  try {
    console.log('관리자 이미지 로드 시작:', inspectionId)
    console.log('현재 검수 정보:', current.value)
    
    // 새로운 API를 사용하여 관리자 이미지 조회
    const images = await getAdminImages(inspectionId)
    console.log('로드된 관리자 이미지들:', images)
    
    // 기존 관리자 이미지 정리
    adminImages.value.forEach(img => {
      if (img.file && img.file.name !== 'admin-image') {
        URL.revokeObjectURL(img.preview)
      }
    })
    adminImages.value = []
    
    // 새로운 관리자 이미지 추가
    images.forEach((imageUrl: string) => {
      adminImages.value.push({
        file: new File([], 'admin-image'), // 더미 파일 객체
        preview: imageUrl
      })
    })
    
    console.log('관리자 이미지 로드 완료, 총 개수:', adminImages.value.length)
  } catch (error) {
    console.error('관리자 이미지 로드 실패:', error)
  }
}
function closePanel() {
  panelOpen.value = false
  current.value = null
  
  // 검수 데이터 초기화 (다음 상품을 위해)
  decision.value = null
  rejectReasons.value = []
  approveNote.value = ""
  grade.value = ""
  
  // 관리자 이미지는 초기화하지 않음 (업로드 후에도 유지)
  isDragOver.value = false
}
function openViewer(p: Photo) {
  viewerSrc.value = p.url
  viewerOpen.value = true
}
function setApprove() {
  decision.value = "APPROVED"
}
function setReject() {
  decision.value = "REJECTED"
}
async function submitDecision() {
  if (!current.value || !decision.value) return

  try {

    if (decision.value === "APPROVED") {
      // 승인 시: 등급과 추가메모 설정
      const gradeNumber = grade.value ? getGradeNumber(grade.value) : undefined
      const note = approveNote.value.trim() || undefined
      await approveInspection(current.value.id, gradeNumber, note)
      
      // 관리자 이미지가 있으면 업로드
      if (adminImages.value.length > 0) {
        const imageFiles = adminImages.value
          .filter(img => img.file && img.file.name !== 'admin-image')
          .map(img => img.file!)
        if (imageFiles.length > 0) {
          await uploadAdminImages(current.value.id, imageFiles)
          console.log("관리자 이미지 업로드 완료:", imageFiles.length, "장")
        }
      }
    } else if (decision.value === "REJECTED") {
      // 반려 시: 등급은 null, 반려사유만 설정
      const reason = rejectReasons.value.join(", ") + (approveNote.value ? ` - ${approveNote.value}` : "")
      await rejectInspection(current.value.id, undefined, reason)
    }

    const { items } = await fetchInspectionsFromAdmin({
      page: 0,
      size: 50,
    })
    list.value = items

    // 성공적으로 저장됨
    
    // 관리자 이미지 새로고침
    await loadAdminImages(current.value.id)
    
    closePanel()
  } catch (error) {
    console.error("결정 저장 실패:", error)
    alert("저장 실패! 콘솔 확인하세요.")
  }
}

// 관리자 이미지 업로드 관련 함수들
function openAdminPicker() {
  console.log('파일 선택기 열기 시도')
  console.log('adminFileInput.value:', adminFileInput.value)
  adminFileInput.value?.click()
}

function onAdminFilePick(event: Event) {
  console.log('파일 선택 이벤트 발생')
  const target = event.target as HTMLInputElement
  const files = target.files
  console.log('선택된 파일들:', files)
  if (files && files.length > 0) {
    console.log('파일 개수:', files.length)
    handleAdminFiles(Array.from(files))
  } else {
    console.log('선택된 파일이 없습니다.')
  }
}

function onDragOver(event: DragEvent) {
  event.preventDefault()
  isDragOver.value = true
}

function onDragLeave(event: DragEvent) {
  event.preventDefault()
  isDragOver.value = false
}

function onDrop(event: DragEvent) {
  event.preventDefault()
  isDragOver.value = false
  
  const files = event.dataTransfer?.files
  if (files) {
    handleAdminFiles(Array.from(files))
  }
}

function handleAdminFiles(files: File[]) {
  console.log('handleAdminFiles 호출됨, 파일 개수:', files.length)
  const maxFiles = 5
  const maxSize = 10 * 1024 * 1024 // 10MB
  
  // 파일 개수 제한
  if (adminImages.value.length + files.length > maxFiles) {
    alert(`최대 ${maxFiles}장까지만 업로드할 수 있습니다.`)
    return
  }
  
  files.forEach((file, index) => {
    console.log(`파일 ${index + 1} 처리 중:`, file.name, file.type, file.size)
    
    // 파일 크기 체크
    if (file.size > maxSize) {
      alert(`${file.name}은(는) 10MB를 초과합니다.`)
      return
    }
    
    // 이미지 파일 체크
    if (!file.type.startsWith('image/')) {
      alert(`${file.name}은(는) 이미지 파일이 아닙니다.`)
      return
    }
    
    // 미리보기 URL 생성
    const preview = URL.createObjectURL(file)
    console.log('미리보기 URL 생성:', preview)
    adminImages.value.push({ file, preview })
    console.log('현재 adminImages 개수:', adminImages.value.length)
  })
}

function removeAdminImage(index: number) {
  const removed = adminImages.value.splice(index, 1)[0]
  if (removed && removed.file) {
    // 실제 파일이 있는 경우에만 URL 해제
    URL.revokeObjectURL(removed.preview)
  }
}

// 등급 문자를 숫자로 변환하는 헬퍼 함수
function getGradeNumber(grade: string): number {
  switch (grade) {
    case "S": return 1
    case "A": return 2
    case "B": return 3
    case "C": return 4
    default: return 1
  }
}

// 검수 결정은 백엔드에서 관리하므로 localStorage 사용하지 않음

// ---------------------
// ✅ DB에서 불러오기
// ---------------------
onMounted(async () => {
  const { items } = await fetchInspectionsFromAdmin({
    state: undefined,
    from: undefined,
    to: undefined,
    page: 0,
    size: 50,
  })
  list.value = items
})
</script>

<style scoped>
.admin-page {
  padding: 16px;
}

.page-header {
  display: flex;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.title {
  font-size: 22px;
  font-weight: 800;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-left: auto;
  align-items: flex-end;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
}

.field input,
.field select {
  height: 36px;
  padding: 0 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 0;
}

.table-wrap {
  width: 100%;
  overflow: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  padding: 12px;
  border-bottom: 1px solid #f1f5f9;
  text-align: left;
  font-size: 14px;
}

.table thead th {
  position: sticky;
  top: 0;
  background: #fafafa;
  z-index: 1;
}

.row {
  cursor: pointer;
}

.row:hover {
  background: #fafafa;
}

.cell-title {
  font-weight: 700;
}

.cell-sub {
  color: #6b7280;
  font-size: 12px;
}

.empty {
  text-align: center;
  padding: 24px;
  color: #6b7280;
}

.badge {
  display: inline-block;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
}

.badge--pending {
  background: #fff7ed;
  color: #c2410c;
  border: 1px solid #fed7aa;
}

.badge--approved {
  background: #ecfdf5;
  color: #047857;
  border: 1px solid #a7f3d0;
}

.badge--rejected {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}

/* 등급 뱃지 */
.badge-grade {
  display: inline-block;
  min-width: 36px;
  text-align: center;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 700;
  padding: 4px 8px;
  color: #fff;
}

/* ✅ 등급별 색상 */
.badge-grade--s {
  background: linear-gradient(45deg, #9333ea, #f43f5e);
}

.badge-grade--a {
  background: #2563eb;
}

.badge-grade--b {
  background: #16a34a;
}

.badge-grade--c {
  background: #f59e0b;
}

.badge-grade--none {
  background: #9ca3af;
}


.chip {
  background: #111827;
  color: #fff;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 11px;
}

.chip--internal {
  background: #0f172a;
  color: #fff;
}

.chip--muted {
  background: #e5e7eb;
  color: #374151;
}

.grade-select {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 12px;
}

.grade-select .label {
  font-weight: 700;
  font-size: 14px;
  color: #374151;
}

.grade-select select {
  appearance: none;
  /* 기본 브라우저 스타일 제거 */
  -webkit-appearance: none;
  -moz-appearance: none;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

/* 호버 / 포커스 */
.grade-select select:hover {
  border-color: #9ca3af;
  background: #f9fafb;
}

.grade-select select:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 2px #bfdbfe;
}

.btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 8px 14px;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}

.btn--cal {
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
}

.btn--ghost {
  background: #fff;
  color: #0f172a;
  border-color: #111827;
}

.btn--small {
  padding: 6px 10px;
  font-size: 12px;
}

.btn--primary {
  background: #fff;
  color: #0f172a;
  border-color: #111827;
}

.btn.danger,
.btn.btn--ghost.danger {
  color: #dc2626;
  border-color: #fecaca;
  margin-right: 10px;
}

/* 반려 선택됨 */
.btn.danger.active {
  background: #dc2626;
  color: #fff;
  border-color: #dc2626;
}

/* 승인 선택됨 */
.btn--primary.active {
  background: #111827;
  color: #fff;
  border-color: #111827;
}


.pagination {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: center;
  padding: 12px;
}

.page-indicator {
  font-size: 13px;
  color: #6b7280;
}

/* Drawer */
.drawer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .45);
  z-index: 6000;
  display: flex;
  justify-content: flex-end;
}

.drawer {
  width: min(840px, 100%);
  height: 100%;
  background: #fff;
  box-shadow: -12px 0 40px rgba(0, 0, 0, .18);
  display: flex;
  flex-direction: column;
}

.drawer__header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #eef2f7;
}

.drawer__title {
  font-size: 23px;
  font-weight: 800;
}

.icon-btn {
  margin-left: auto;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 18px;
}

.drawer__body {
  padding: 16px;
  overflow: auto;
}

.drawer__footer {
  margin-top: auto;
  padding: 12px 16px;
  border-top: 1px solid #eef2f7;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.meta__title {
  font-size: 18px;
  font-weight: 800;
}

.meta__sub {
  color: #6b7280;
  font-size: 12px;
  margin-top: 4px;
}

.internal {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
  padding: 10px 12px;
  margin: 12px 0;
}

.internal .lock {
  font-size: 18px;
}

.internal__content {
  font-size: 14px;
  margin-top: 6px;
}

.warnings {
  margin-top: 6px;
}

.warnings li {
  font-size: 13px;
  color: #b45309;
}

.section-title {
  font-size: 18px;
  font-weight: 800;
  margin: 16px 0 8px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 10px;
}

.pic {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
  background: #fafafa;
  cursor: zoom-in;
}

.pic img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
}

.pic figcaption {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px;
  font-size: 11px;
  color: #6b7280;
}

.decision {
  display: flex;
  gap: 16px;
}

.decision__left {
  flex: 1;
}

.decision__right {
  display: flex;
  align-items: flex-start;
}

.reasons .label {
  display: block;
  font-weight: 700;
  margin-bottom: 8px;
  margin: 10px 1px 7px;
}

.checks {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
}

.check {
  font-size: 14px;
}

.note {
  width: 100%;
  min-height: 84px;
  margin-top: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 8px 10px;
  font-size: 14px;
}

/* Viewer */
.viewer {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .75);
  z-index: 7000;
  display: grid;
  place-items: center;
}

.viewer img {
  max-width: 92vw;
  max-height: 92vh;
  border-radius: 12px;
  background: #111;
}

.viewer__close {
  position: fixed;
  top: 12px;
  right: 12px;
  color: #fff;
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity .18s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: transform .22s ease;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
}
/* === 한 줄 썸네일 행 === */
.thumb-row {
  display: flex;
  overflow-x: auto;
  font-size: 0;
  padding-bottom: 6px;
}
.thumb {
  margin: 0.6rem;  
  flex: 0 0 140px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
  background: #fafafa;
  cursor: zoom-in;
}
.thumb img {
  width: 100%;
  height: 140px;
  object-fit: cover;
  display: block;
}
.thumb figcaption {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px;
  font-size: 11px;
  color: #6b7280;
}
/* === 판매자 메모 표시 === */
.memo-view {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
}
.memo-text {
  white-space: pre-wrap;   /* 줄바꿈 유지 */
  line-height: 1.5;
  color: #111827;
}
.memo-empty {
  color: #9ca3af;
  font-size: 14px;
  padding: 8px 0;
}

/* 관리자 이미지 섹션 */
.admin-images-section {
  margin: 16px 0;
}

.admin-images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.admin-image-card {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #e5e7eb;
}

.admin-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
}

.admin-image-actions {
  position: absolute;
  top: 4px;
  right: 4px;
  display: flex;
  gap: 4px;
}

.admin-image-actions .btn {
  padding: 4px 8px;
  font-size: 12px;
  min-height: auto;
}

.admin-images-empty {
  color: #9ca3af;
  font-size: 14px;
  padding: 16px;
  text-align: center;
  border: 1px dashed #e5e7eb;
  border-radius: 8px;
  background: #f9fafb;
}



/* === 이미지 뷰어 모달 === */
.image-viewer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.9);
  z-index: 8000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-viewer {
  position: relative;
  max-width: 95vw;
  max-height: 95vh;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
}

.image-viewer__close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 40px;
  height: 40px;
  border: none;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  font-size: 24px;
  border-radius: 50%;
  cursor: pointer;
  z-index: 10;
}

.image-viewer__close:hover {
  background: rgba(0, 0, 0, 0.9);
}

.image-viewer__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.image-viewer__main {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 8px;
}

.image-viewer__controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.image-viewer__counter {
  color: #fff;
  font-weight: 600;
  min-width: 60px;
  text-align: center;
}

.image-viewer__thumbs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 8px 0;
}

.image-viewer__thumb {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.image-viewer__thumb:hover {
  opacity: 0.8;
}

.image-viewer__thumb.active {
  opacity: 1;
  border: 2px solid #fff;
}

/* 관리자 이미지 업로드 스타일 (UploadVerifyModal.vue와 동일) */
.admin-image-upload {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.dropzone {
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  background: #f9fafb;
  transition: all 0.2s ease;
  cursor: pointer;
  position: relative;
}

.dropzone:hover {
  border-color: #9ca3af;
  background: #f3f4f6;
}

.dropzone--drag {
  border-color: #3b82f6;
  background: #eff6ff;
}

.dropzone__input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.dropzone__content {
  pointer-events: auto;
}

.dropzone__icon {
  font-size: 48px;
  margin-bottom: 12px;
  display: block;
}

.dropzone__title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.dropzone__hint {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 8px 0;
}

.dropzone__sub {
  font-size: 13px;
  color: #9ca3af;
  margin: 0;
}

.admin-preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.admin-preview-card {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #e5e7eb;
}

.admin-preview-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
}

.admin-preview-actions {
  position: absolute;
  top: 4px;
  right: 4px;
  display: flex;
  gap: 4px;
}

.admin-preview-actions .btn {
  padding: 4px 8px;
  font-size: 12px;
  min-height: auto;
}

.test-upload-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 10px;
  font-size: 14px;
}

.test-upload-btn:hover {
  background: #2563eb;
}

@media (max-width: 640px) {
  .filters {
    gap: 8px;
  }

  .field {
    width: calc(50% - 8px);
  }
}
</style>

