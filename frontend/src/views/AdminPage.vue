<template>
  <div class="admin-page">
    <!-- 상단 헤더(페이지 타이틀/필터) -->
    <header class="page-header">
      <h1 class="title">검수 관리</h1>
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
            <option value="REVIEWING">검토중</option>
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
              <th style="width: 170px">검수 ID</th>
              <th style="width: 170px">상품명</th>
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
              <td>{{ ins.photos.length }}</td>
              <td>
                <span :class="['badge', `badge--${ins.status.toLowerCase()}`]">{{ toKrStatus(ins.status) }}</span>
              </td>
              <!-- <td>
                <button class="btn btn--small" @click.stop="openReview(ins)">검토</button>
              </td> -->
              <td>
                <span v-if="ins.grade" :class="['badge-grade', `badge-grade--${ins.grade.toLowerCase()}`]">
                  {{ ins.grade }}
                </span>
                <span v-else class="badge-grade badge-grade--none">-</span>
              </td>
            </tr>
            <tr v-if="!paged.length">
              <td colspan="8" class="empty">검색 조건에 맞는 항목이 없습니다.</td>
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
                  <span :class="['badge', `badge--${current.status.toLowerCase()}`]">{{ toKrStatus(current.status)
                  }}</span>
                </div>
              </div>

              <!-- 내부 전용 정보 -->
              <div class="internal">
                <span class="lock" aria-hidden="true">🔒</span>
                <div class="internal__content">
                  <div><strong>촬영시각:</strong> <span>{{ current.capturedAtInternal ? fmt(current.capturedAtInternal)
                    : '없음' }}</span></div>
                  <div v-if="current.warnings?.length"><strong>자동 경고:</strong>
                    <ul class="warnings">
                      <li v-for="(w, i) in current.warnings" :key="i">⚠ {{ w }}</li>
                    </ul>
                  </div>
                </div>
              </div>

              <!-- 이미지 미리보기 그리드 -->
              <h3 class="section-title">이미지 ({{ current.photos.length }})</h3>
              <div class="grid">
                <figure v-for="p in current.photos" :key="p.id" class="pic" @click="openViewer(p)">
                  <img :src="p.url" :alt="`photo ${p.id}`" />
                  <figcaption>
                    <span v-if="p.isCover" class="chip">대표</span>
                    <span class="meta">{{ p.width }}×{{ p.height }}</span>
                  </figcaption>
                </figure>
              </div>

              <!-- 의사결정 영역 -->
              <h3 class="section-title">검수 결정</h3>
              <div class="decision">
                <div class="decision__left">
                  <div class="reasons" v-if="decision === 'REJECTED'">
                    <span class="label">반려 사유</span>
                    <div class="checks">
                      <label v-for="r in defaultReasons" :key="r" class="check"><input type="checkbox" :value="r"
                          v-model="rejectReasons" /> {{ r }}</label>
                    </div>
                    <textarea v-model="rejectNote" class="note" placeholder="추가 메모(선택)"></textarea>
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
                  </div>

                </div>
                <div class="decision__right">
                  <div class="buttons">
                    <button class="btn btn--ghost danger"  :class="{ active: decision==='REJECTED' }"@click="setReject">반려</button>
                    <button class="btn btn--primary":class="{ active: decision==='APPROVED' }"@click="setApprove">승인</button>
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
import { computed, onMounted, ref } from "vue"
import {
  fetchInspections,
  type Inspection,
  type Photo,
  type Status,
} from "@/api/inspection"
import { toDegrees } from "chart.js/helpers"

// ---------------------
// 상태
// ---------------------
const list = ref<Inspection[]>([])
const q = ref("")
const status = ref<"" | Status>("")
const dateFrom = ref("")
const dateTo = ref("")

const page = ref(1)
const pageSize = ref(8)

const panelOpen = ref(false)
const current = ref<Inspection | null>(null)

const decision = ref<Status | null>(null)
const rejectReasons = ref<string[]>([])
const rejectNote = ref("")
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

// ---------------------
// 파생 값 & 페이지네이션
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
    .sort((a, b) => Number(b.id.replace("ins_", "")) - Number(a.id.replace("ins_", "")))
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
    return rejectReasons.value.length > 0 || !!rejectNote.value.trim()
  if (decision.value === "APPROVED") return true
  return false
})

// ---------------------
// 유틸/포맷터
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
  return s === "PENDING"
    ? "대기"
    : s === "REVIEWING"
      ? "검토중"
      : s === "APPROVED"
        ? "승인"
        : "반려"
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

function openReview(ins: Inspection) {
  current.value = {
    ...ins,
    status: ins.status === "PENDING" ? "REVIEWING" : ins.status,
  }
  panelOpen.value = true
  decision.value = null
  rejectReasons.value = []
  rejectNote.value = ""
}
function closePanel() {
  panelOpen.value = false
  current.value = null
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
  alert(
    `결정 저장: ${toKrStatus(decision.value)}\n등급: ${grade.value || "없음"}\n사유: ${rejectReasons.value.join(", ")
    }\n메모: ${rejectNote.value}`
  )
  const idx = list.value.findIndex((x) => x.id === current.value!.id)
  if (idx >= 0) {
    list.value[idx].status = decision.value
    if (decision.value === "APPROVED") {
      list.value[idx].grade = grade.value || undefined
    }
  }
  closePanel()
}

// ---------------------
// 데이터 로딩
// ---------------------
onMounted(async () => {
  list.value = await fetchInspections({ status: "", q: "" })
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
  margin-right: auto;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
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

.badge--reviewing {
  background: #eef2ff;
  color: #4338ca;
  border: 1px solid #c7d2fe;
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
.badge-grade--s { background: linear-gradient(45deg, #9333ea, #f43f5e); } 
.badge-grade--a { background: #2563eb; } 
.badge-grade--b { background: #16a34a; } 
.badge-grade--c { background: #f59e0b; }
.badge-grade--none { background: #9ca3af; } 


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
  appearance: none;            /* 기본 브라우저 스타일 제거 */
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

.btn--ghost {
  background: #fff;
  color:#0f172a;
  border-color: #111827;
}

.btn--small {
  padding: 6px 10px;
  font-size: 12px;
}

.btn--primary {
  background: #fff;
  color:#0f172a;
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

@media (max-width: 640px) {
  .filters {
    gap: 8px;
  }

  .field {
    width: calc(50% - 8px);
  }
}
</style>
