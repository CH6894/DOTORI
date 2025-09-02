<template>
  <div class="page calendar-scope">
    <h2 class="calendar-title">
      {{ pageDate.getFullYear() }}년 {{ pageDate.getMonth() + 1 }}월
    </h2>

    <router-link to="/admin" class="back-btn" role="button">검수관리</router-link>

    <div class="calendar-wrap">
      <div ref="calendarEl" id="calendar"></div>
    </div>

    <!-- ✅ 일정 요약 -->
    <section class="summary">
      <h3 class="summary__title">일정 요약</h3>
      <ul v-if="summary.length" class="summary__list">
        <li v-for="ev in summary" :key="ev.id" class="summary__item">
          <span class="summary__dot" :style="{ '--dot-color': ev.color }"></span>
          <span class="summary__date">{{ ev.ymd }}</span>
          <span class="summary__when">{{ ev.allDay ? '종일' : ev.time }}</span>
          <span class="summary__text">{{ ev.title }}</span>
          <div class="summary__actions">
            <button class="btn btn--tiny" @click="editFromSummary(ev.id)">수정</button>
            <button class="btn btn--tiny btn--danger" @click="deleteFromSummary(ev.id)">삭제</button>
          </div>
        </li>
      </ul>
      <div v-else class="summary__empty">현재 화면 범위에 일정이 없습니다.</div>
    </section>

    <!-- 모달 -->
    <div v-if="isModalOpen" class="modal" @click.self="closeModal" aria-hidden="false">
      <div class="modal__dialog" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <div class="modal__header">
          <h2 id="modal-title">{{ modalTitle }}</h2>
          <button class="modal__close" aria-label="닫기" @click="closeModal">×</button>
        </div>

        <div class="modal__body">
          <form @submit.prevent>
            <input type="hidden" v-model="form.id" />
            <div class="form-row">
              <label for="event-title">제목</label>
              <input id="event-title" v-model="form.title" type="text" required />
            </div>
            <div class="form-row">
              <label for="event-start">시작</label>
              <input id="event-start" v-model="form.start" type="datetime-local" required />
            </div>
            <div class="form-row">
              <label for="event-end">종료</label>
              <input id="event-end" v-model="form.end" type="datetime-local" />
            </div>
            <div class="form-row">
              <label><input v-model="form.allDay" type="checkbox" /> 종일(기간 선택 시 권장)</label>
            </div>
            <div class="form-row color-row">
              <label for="event-color">색상</label>
              <input id="event-color" v-model="form.color" type="color" />
            </div>
          </form>

          <hr style="margin:16px 0; border:none; border-top:1px solid #eee;" />

          <section class="agenda">
            <h3 class="agenda__title">상세 일정</h3>
            <div class="agenda__body" id="agenda-body" v-html="agendaHtml"></div>
          </section>
        </div>

        <div class="modal__footer">
          <div class="left">
            <button v-if="form.id" @click="onDelete" class="btn btn-danger" type="button">삭제</button>
          </div>
          <div class="right">
            <button class="btn btn-secondary" @click="closeModal" type="button">취소</button>
            <button class="btn btn-primary" @click="onSave" type="button">저장</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import axios from 'axios'
import { Calendar } from '@fullcalendar/core'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import listPlugin from '@fullcalendar/list'
import bootstrap5Plugin from '@fullcalendar/bootstrap5'

/* ===== API (상대경로) ===== */
const PUB_URL = '/api/public/calendars'
const ADM_URL = '/api/admin/calendars'

/* ===== 유틸 ===== */
const pad = (n) => String(n).padStart(2, '0')
const toLocalInputValue = (dateLike) => {
  const d = new Date(dateLike)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}
const fromLocalInputValue = (v) => (v ? new Date(v) : null)
const escapeHtml = (s) => String(s).replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]))
const fmtYmd = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
const timeRange = (s, e) => {
  const f = (x) => `${pad(x.getHours())}:${pad(x.getMinutes())}`
  if (!e || e.getTime() === s.getTime()) return f(s)
  return `${f(s)} ~ ${f(e)}`
}
const startOfDay = (d) => new Date(d.getFullYear(), d.getMonth(), d.getDate())
const addDays = (d, n) => new Date(d.getFullYear(), d.getMonth(), d.getDate() + n)
const endOfDayInclusive = (d) => new Date(d.getFullYear(), d.getMonth(), d.getDate(), 23, 59, 59, 999)
const isoLocal = (dateObj) => {
  const d = new Date(dateObj)
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
const formatAxiosError = (e) => (
  e?.response
    ? `HTTP ${e.response.status} ${e.response.statusText}\n${JSON.stringify(e.response.data)}`
    : (e?.request ? '응답 없음(네트워크/CORS 가능성)' : `설정 오류: ${e?.message}`)
)

/* scheduleInfo JSON pack/unpack */
function packInfo({ color, groupId, allDay }) {
  try { return JSON.stringify({ color, groupId, allDay: !!allDay }); }
  catch { return null; }
}
function unpackInfo(info) {
  try { return info ? JSON.parse(info) : {}; }
  catch { return {}; }
}

/* ===== 상태 ===== */
const calendarEl = ref(null)
let calendar
const isModalOpen = ref(false)
const modalTitle = ref('일정')
const agendaHtml = ref('')
const pageDate = ref(new Date())
const summary = ref([])

const ADMIN_USER_ID = 1
const form = reactive({ id:'', title:'', start:'', end:'', allDay:false, color:'#7c3aed' })

/* ===== 백엔드 연동 ===== */
async function fetchEventsForRange(startDate, endDate) {
  const start = isoLocal(startDate).slice(0, 19)
  const end   = isoLocal(endDate).slice(0, 19)
  try {
    const { data } = await axios.get(PUB_URL, { params: { start, end } })
    return (data || []).map(ev => {
      const meta = unpackInfo(ev.scheduleInfo || ev.info)
      const color = meta.color || '#7c3aed'
      const allDay = !!meta.allDay
      return {
        id: String(ev.id),
        title: ev.title ?? ev.scheduleName ?? '(제목 없음)',
        start: ev.start ?? ev.scheduleDate,
        end: null, // 엔드 없음(스키마 제한)
        allDay,
        backgroundColor: color,
        borderColor: color,
        extendedProps: { color, groupId: meta.groupId || null, info: ev.info ?? ev.scheduleInfo ?? '' }
      }
    })
  } catch (e) {
    console.error(e)
    alert('일정 불러오기 실패: ' + formatAxiosError(e))
    return []
  }
}

async function createEventToServer({ title, start, info }) {
  const body = {
    scheduleName: title,
    scheduleDate: start,
    scheduleInfo: info ?? null,
    userId: ADMIN_USER_ID
  }
  try {
    await axios.post(ADM_URL, body, { headers: { 'Content-Type': 'application/json' } })
  } catch (e) {
    console.error(e)
    throw new Error(formatAxiosError(e))
  }
}
async function deleteEventFromServer(id) {
  try {
    await axios.delete(`${ADM_URL}/${encodeURIComponent(id)}`)
  } catch (e) {
    console.error(e)
    throw new Error(formatAxiosError(e))
  }
}

/* ===== 저장/삭제 ===== */
async function onSave() {
  try {
    if (!form.title) { alert('제목을 입력하세요.'); return }

    const s = fromLocalInputValue(form.start)
    const e = form.end ? fromLocalInputValue(form.end) : null
    const endEx = e || new Date(s.getTime() + 60 * 60 * 1000)

    // 단일 수정 모드면 기존 1건 삭제 (그룹 전체 수정은 별도 구현 권장)
    if (form.id) {
      try { await deleteEventFromServer(form.id) } catch (_) {}
    }

    // 같은 기간 묶음을 식별할 groupId
    const groupId = (window.crypto?.randomUUID?.() || `${Date.now()}-${Math.random().toString(16).slice(2)}`)

    if (form.allDay && endEx > s) {
      // 종일 + 기간: 하루씩 쪼개서 여러 건 생성
      const dayMs = 24*60*60*1000
      for (let t = new Date(s.getFullYear(), s.getMonth(), s.getDate()); t < endEx; t = new Date(t.getTime() + dayMs)) {
        const dayStart = new Date(t.getFullYear(), t.getMonth(), t.getDate(), 0, 0, 0)
        const scheduleDate = isoLocal(dayStart).slice(0,19)
        const scheduleInfo = packInfo({ color: form.color, groupId, allDay: true })
        await createEventToServer({ title: form.title, start: scheduleDate, info: scheduleInfo })
      }
    } else {
      // 단일 이벤트(엔드 없이 포인트성)
      const startIso = isoLocal(s).slice(0, 19)
      const scheduleInfo = packInfo({ color: form.color, groupId, allDay: !!form.allDay })
      await createEventToServer({ title: form.title, start: startIso, info: scheduleInfo })
    }

    await refetchFromServer()
    closeModal()
    alert('저장 완료.')
  } catch (e) {
    console.error(e)
    alert('저장 실패: ' + (e.message || e))
  }
}

async function onDelete() {
  try {
    if (!form.id) return
    await deleteEventFromServer(form.id)
    await refetchFromServer()
    closeModal()
    alert('삭제 완료.')
  } catch (e) {
    console.error(e)
    alert('삭제 실패: ' + (e.message || e))
  }
}

/* ===== 요약/Agenda ===== */
function splitByDate(start, end, allDay) {
  const days = []
  const s = new Date(start)
  let e = end ? new Date(end) : new Date(start)
  if (allDay && end) e = addDays(e, -1)
  let cur = new Date(s.getFullYear(), s.getMonth(), s.getDate())
  const last = new Date(e.getFullYear(), e.getMonth(), e.getDate())
  while (cur <= last) { days.push(fmtYmd(cur)); cur = new Date(cur.getFullYear(), cur.getMonth(), cur.getDate() + 1) }
  return days
}
function renderAgenda() {
  const container = document.getElementById('agenda-body')
  if (!container) return
  const view = calendar.view
  const rs = view.currentStart
  const re = view.currentEnd
  const events = calendar.getEvents().filter(ev => (ev.end || ev.start) > rs && ev.start < re)
  const map = new Map()
  for (const ev of events) {
    for (const day of splitByDate(ev.start, ev.end, ev.allDay)) {
      if (!map.has(day)) map.set(day, [])
      map.get(day).push(ev)
    }
  }
  const keys = Array.from(map.keys()).sort()
  for (const k of keys) {
    map.get(k).sort((a, b) => (a.allDay !== b.allDay) ? (a.allDay ? -1 : 1) : ((a.start?.getTime()||0)-(b.start?.getTime()||0)))
  }
  if (!keys.length) { agendaHtml.value = `<div class="agenda__empty">표시할 일정이 없습니다.</div>`; return }
  const html = keys.map(k => {
    const dateObj = new Date(k + 'T00:00:00')
    const dateLabel = `${dateObj.getFullYear()}년 ${dateObj.getMonth() + 1}월 ${dateObj.getDate()}일`
    const items = map.get(k).map(ev => {
      const color = ev.extendedProps?.color || ev.backgroundColor || '#7c3aed'
      const time = ev.allDay ? '종일' : timeRange(ev.start, ev.end)
      const title = escapeHtml(ev.title || '(제목 없음)')
      return `
        <li class="agenda__item" style="--dot-color:${color}">
          <span class="agenda__time">${time}</span>
          <span class="agenda__title">${title}</span>
          <button class="agenda__edit" data-id="${ev.id}">수정</button>
        </li>`
    }).join('')
    return `<section class="agenda__section"><h4 class="agenda__date">${dateLabel}</h4><ul class="agenda__list">${items}</ul></section>`
  }).join('')
  agendaHtml.value = html
  setTimeout(() => {
    container.querySelectorAll('.agenda__edit').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-id')
        const ev = calendar.getEventById(id)
        if (ev) openModal('edit', ev)
      })
    })
  })
}

function refreshSummary() {
  if (!calendar) return
  const view = calendar.view
  const rs = view.currentStart
  const re = view.currentEnd
  const list = calendar.getEvents()
    .filter(ev => (ev.end || ev.start) > rs && ev.start < re)
    .map(ev => {
      const color = ev.extendedProps?.color || ev.backgroundColor || '#7c3aed'
      return {
        id: ev.id,
        title: ev.title || '(제목 없음)',
        start: ev.start,
        end: ev.end || ev.start,
        allDay: ev.allDay,
        color,
        time: ev.allDay ? '종일' : timeRange(ev.start, ev.end || ev.start),
        ymd: fmtYmd(ev.start),
      }
    })
    .sort((a, b) => (a.start?.getTime() || 0) - (b.start?.getTime() || 0))
  summary.value = list
}

/* 그룹 삭제 지원(같은 groupId 전부 삭제) */
async function deleteFromSummary(id) {
  try {
    const ev = calendar.getEventById(id)
    const gid = ev?.extendedProps?.groupId
    if (gid) {
      const same = calendar.getEvents().filter(e => e.extendedProps?.groupId === gid)
      for (const e of same) await deleteEventFromServer(e.id)
    } else {
      await deleteEventFromServer(id)
    }
    await refetchFromServer()
  } catch (e) {
    alert('삭제 실패: ' + (e.message || e))
  }
}
function editFromSummary(id) {
  const ev = calendar.getEventById(id)
  if (ev) openModal('edit', ev)
}

/* ===== 캘린더 ===== */
async function refetchFromServer() {
  const view = calendar.view
  const start = view.currentStart
  const end   = view.currentEnd
  try {
    const events = await fetchEventsForRange(start, end)
    calendar.removeAllEvents()
    events.forEach(e => calendar.addEvent(e))
    renderAgenda()
    refreshSummary()
  } catch (e) {
    console.error(e)
    alert('이벤트 갱신 실패: ' + (e.message || e))
  }
}

function openModal(mode, ev) {
  isModalOpen.value = true
  if (mode === 'create') {
    modalTitle.value = '일정 등록'
    const now = new Date()
    Object.assign(form, {
      id: '', title: '', allDay: false, color: '#7c3aed',
      start: toLocalInputValue(now),
      end: toLocalInputValue(new Date(now.getTime() + 60 * 60 * 1000))
    })
  } else {
    modalTitle.value = '일정 수정'
    Object.assign(form, {
      id: ev?.id ?? '',
      title: ev?.title ?? '',
      start: ev?.start ? toLocalInputValue(ev.start) : '',
      end: ev?.end ? toLocalInputValue(ev.end) : '',
      allDay: !!ev?.allDay,
      color: ev?.extendedProps?.color || ev?.backgroundColor || '#7c3aed'
    })
  }
}

function closeModal() { isModalOpen.value = false }

onMounted(() => {
  calendar = new Calendar(calendarEl.value, {
    themeSystem: 'bootstrap5',
    locale: 'ko',
    timeZone: 'Asia/Seoul',
    plugins: [dayGridPlugin, interactionPlugin, listPlugin, bootstrap5Plugin],
    initialView: 'dayGridMonth',
    headerToolbar: {
      left: 'prev today next',
      center: '',
      right: 'dayGridMonth,dayGridWeek,dayGridDay,listMonth addEventButton'
    },
    customButtons: {
      addEventButton: { text: '일정 등록', click: () => openModal('create') }
    },
    dayCellContent: (arg) => ({ html: arg.dayNumberText.replace('일', '') }),
    selectable: true,
    selectMirror: true,
    unselectAuto: true,
    selectLongPressDelay: 200,

    dateClick: (info) => {
      openModal('create')
      form.allDay = true
      const d0 = startOfDay(info.date)
      const d1 = endOfDayInclusive(info.date)
      form.start = toLocalInputValue(d0)
      form.end = toLocalInputValue(new Date(d1.getTime() + 1)) // 배타 end 흉내 필요 없음(우회 방식)
    },

    // ⬇️ 드래그한 기간 그대로 폼에 반영
    select: (arg) => {
      openModal('create')
      form.allDay = arg.allDay === true
      form.start = toLocalInputValue(arg.start)
      form.end   = arg.end ? toLocalInputValue(arg.end) : ''
      calendar.unselect()
    },

    eventClick: (arg) => openModal('edit', arg.event),

    datesSet: async () => {
      pageDate.value = calendar.getDate()
      await refetchFromServer()
    }
  })

  calendar.render()
  pageDate.value = calendar.getDate()
  setTimeout(() => calendar.updateSize(), 0)
})

onBeforeUnmount(() => { if (calendar) calendar.destroy() })
</script>

<style src="../assets/calendar.css"></style>
<style scoped>
/* 모달 본문이 길어질 경우 스크롤 */
.modal__body { max-height: 70vh; overflow: auto; }

/* ===== Summary ===== */
.summary {
  width: var(--cal-fixed-width, 1280px);
  max-width: 100%;
  margin: 16px auto 0;
}
.summary__title {
  margin: 12px 0 10px;
  font-size: 16px;
  font-weight: 800;
  color: #111;
}
.summary__list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 8px;
}
.summary__item {
  display: grid;
  grid-template-columns: 10px 110px 96px 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border: 1px solid #e9e9e9;
  border-radius: 10px;
  background: #fff;
}
.summary__dot { width: 8px; height: 8px; border-radius: 2px; background: var(--dot-color, #999); }
.summary__date, .summary__when { font-size: 13px; color: #444; }
.summary__text { font-size: 14px; color: #111; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.summary__actions { display: flex; gap: 6px; }
.btn.btn--tiny { padding: 4px 8px; border-radius: 8px; border: 1px solid #ddd; background: #fff; font-size: 12px; cursor: pointer; }
.btn.btn--tiny:hover { background: #f5f5f5; }
.btn--danger { color: #dc2626; border-color: #fecaca; }
.summary__empty { padding: 14px; text-align: center; color: #666; border: 1px dashed #e5e7eb; border-radius: 10px; background: #fff; }
@media (max-width: 640px) {
  .summary__item { grid-template-columns: 10px 92px 72px 1fr auto; gap: 8px; }
  .summary__date, .summary__when { font-size: 12px; }
  .summary__text { font-size: 13px; }
}

/* back button (원본 유지) */
.back-btn{ margin-left:7rem; margin-top:-5rem; appearance:none; display:inline-flex; align-items:center; gap:.5rem; padding:.625rem 1rem; border:1px solid #e5dcc9; border-radius:.5rem; background:#fff; color:#5f5346; font-weight:700; font-size:.9375rem; line-height:1; cursor:pointer; transition:background .2s ease, border-color .2s ease, transform .02s ease; }
.back-btn:hover{ background:#f9f6ef; border-color:#d8cdb3; }
.back-btn:active{ transform:translateY(1px); }
.back-btn:focus-visible{ outline:2px solid transparent; box-shadow:0 0 0 3px rgba(252,112,60,.25); border-color:#fc703c; }
.back-btn:disabled{ opacity:.55; cursor:not-allowed; }
</style>
