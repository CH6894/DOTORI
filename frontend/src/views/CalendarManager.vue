<template>
  <div class="page calendar-scope">
    <h2 class="calendar-title">{{ pageDate.getFullYear() }}년 {{ pageDate.getMonth() + 1 }}월</h2>

    <router-link to="/admin" class="back-btn" role="button">검수관리</router-link>

    <div class="calendar-wrap">
      <div ref="calendarEl" id="calendar"></div>
    </div>

    <!-- 요약 -->
    <section class="summary">
      <h3 class="summary__title">일정 요약</h3>
      <ul v-if="summary.length" class="summary__list">
        <li v-for="ev in summary" :key="ev.id" class="summary__item">
          <span class="summary__dot" :style="{ '--dot-color': ev.color }"></span>
          <span class="summary__date">{{ ev.ymd }}</span>
          <span class="summary__when">{{ ev.whenLabel }}</span>
          <span class="summary__text">{{ ev.title }}</span>
          <div class="summary__actions">
            <button class="btn btn--tiny" @click="editFromSummary(ev.id)">수정</button>
            <button class="btn btn--tiny btn--danger" @click="deleteFromSummary(ev.id)">삭제</button>
          </div>
        </li>
      </ul>
      <div v-else class="summary__empty">현재 화면 범위에 일정이 없습니다.</div>
    </section>

    <!-- 상세일정 -->
    <section class="agenda" v-show="showAgenda">
      <h3 class="agenda__title">
        상세일정
        <small v-if="selectedDate">— {{ selectedDate.toLocaleDateString('ko-KR') }}</small>
        <button v-if="selectedDate" class="btn btn--tiny" @click="clearSelected">닫기</button>
      </h3>
      <div class="agenda__body" v-html="agendaHtml"></div>
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
              <label><input v-model="form.allDay" type="checkbox" /> 종일(기간 선택 시 사용)</label>
            </div>
            <div class="form-row color-row">
              <label for="event-color">색상</label>
              <input id="event-color" v-model="form.color" type="color" />
            </div>
          </form>
        </div>

        <div class="modal__footer">
          <div class="left">
            <button v-if="form.id" class="btn btn-danger" @click="onDelete" type="button">삭제</button>
          </div>
          <div class="right">
            <button class="btn btn-secondary" @click="closeModal" type="button">취소</button>
            <button class="btn btn-primary" @click="onSave" type="button">
              {{ form.id ? '수정' : '저장' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
import axios from 'axios'
import { Calendar } from '@fullcalendar/core'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'          // ✅ Day/Week 시간격자
import interactionPlugin from '@fullcalendar/interaction'
import listPlugin from '@fullcalendar/list'
import bootstrap5Plugin from '@fullcalendar/bootstrap5'

/* API */
const PUB_URL = '/api/public/calendars'
const ADM_URL = '/api/admin/calendars'

/* ===== 유틸 ===== */
const pad = (n) => String(n).padStart(2, '0')
const toLocalInputValue = (dateLike) => {
  const d = new Date(dateLike)
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}
const fromLocalInputValue = (v) => (v ? new Date(v) : null)
const fmtYmd = (d) => `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
const startOfDay = (d)=>new Date(d.getFullYear(), d.getMonth(), d.getDate())
const endOfDayInclusive=(d)=>new Date(d.getFullYear(), d.getMonth(), d.getDate(), 23, 59, 59, 999)
const addDays=(d,n)=>new Date(d.getFullYear(), d.getMonth(), d.getDate()+n)
const isoLocal=(dateObj)=>{const d=new Date(dateObj);return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`}
const formatAxiosError=(e)=> e?.response?`HTTP ${e.response.status} ${e.response.statusText}\n${JSON.stringify(e.response.data)}`:(e?.request?'응답 없음(네트워크/CORS 가능성)':`설정 오류: ${e?.message}`)

/* 요약 표기 */
const hhmm = (d)=>`${pad(d.getHours())}:${pad(d.getMinutes())}`
const timeRange = (s,e)=> (!e||e.getTime()===s.getTime()) ? hhmm(s) : `${hhmm(s)} ~ ${hhmm(e)}`
const dateLabelKR = (d) => `${d.getMonth()+1}월 ${d.getDate()}일`
const dayOnlyKR   = (d) => `${d.getDate()}일`
function rangeLabelKR(start, endExclusive, allDay){
  if(!allDay) return null
  if(!endExclusive) return '종일'
  const endInc = new Date(endExclusive.getTime() - 1)
  const sameDay = start.toDateString() === endInc.toDateString()
  if(sameDay) return '종일'
  const sameMonth = (start.getFullYear()===endInc.getFullYear()) && (start.getMonth()===endInc.getMonth())
  return sameMonth
    ? `${dayOnlyKR(start)}부터 ${dayOnlyKR(endInc)}까지`
    : `${dateLabelKR(start)}부터 ${dateLabelKR(endInc)}까지`
}

/* datetime-local */
const ymd = (d) => `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
const toLocalDateInput00 = (dateObj) => `${ymd(new Date(dateObj))}T00:00`
const toLocalDateInput2359 = (dateObj) => `${ymd(new Date(dateObj))}T23:59`

/* 종일 기간 정규화 */
function normalizeAllDayRange(startInputStr, endInputStr) {
  const s = new Date(startInputStr)
  const e = endInputStr ? new Date(endInputStr) : new Date(startInputStr)
  const start00 = new Date(s.getFullYear(), s.getMonth(), s.getDate(), 0, 0, 0, 0)
  const end2359 = new Date(e.getFullYear(), e.getMonth(), e.getDate(), 23, 59, 59, 999)
  if (end2359 < start00) {
    return { start00, end2359: new Date(start00.getFullYear(), start00.getMonth(), start00.getDate(), 23, 59, 59, 999) }
  }
  return { start00, end2359 }
}

/* scheduleInfo JSON */
function packInfo(obj){ try{return JSON.stringify(obj)}catch{return null} }
function unpackInfo(info){ try{return info?JSON.parse(info):{}}catch{return{}} }

/* ===== 날짜 헤더 포맷터 ===== */
const koDayHeader = (date) => {
  const yo = '일월화수목금토'[date.getDay()]
  return `${date.getMonth() + 1}월 ${date.getDate()}일 (${yo})`
}

/* 상태 */
const calendarEl = ref(null)
let calendar
const isModalOpen = ref(false)
const modalTitle = ref('일정')
const pageDate = ref(new Date())
const summary = ref([])
const ADMIN_USER_ID = 1
const form = reactive({ id:'', title:'', start:'', end:'', allDay:false, color:'#7c3aed' })

/* 상세일정 상태 */
const showAgenda = ref(false)
const selectedDate = ref(null)
const agendaHtml   = ref('')
const pendingSelect = ref(null)

/* 종일 토글 스냅 */
watch(() => form.allDay, (v) => {
  if (v) {
    const s = fromLocalInputValue(form.start) || new Date()
    const e = form.end ? fromLocalInputValue(form.end) : s
    form.start = toLocalDateInput00(s)
    form.end   = toLocalDateInput2359(e)
  }
})

/* 서버 연동 */
async function createEventToServer({ title, start, info }) {
  const body = { scheduleName: title, scheduleDate: start, scheduleInfo: info ?? null, userId: ADMIN_USER_ID }
  const { data } = await axios.post(ADM_URL, body, { headers: { 'Content-Type': 'application/json' } })
  return data
}
async function updateEventToServer(id, { title, start, info }) {
  const body = { scheduleName: title, scheduleDate: start, scheduleInfo: info ?? null, userId: ADMIN_USER_ID }
  return axios.put(`${ADM_URL}/${encodeURIComponent(id)}`, body, { headers: { 'Content-Type': 'application/json' } })
}
async function deleteEventFromServer(id){ await axios.delete(`${ADM_URL}/${encodeURIComponent(id)}`) }

/* 조회 변환 */
async function fetchEventsForRange(startDate, endDate){
  const start = isoLocal(startDate).slice(0,19)
  const end   = isoLocal(endDate).slice(0,19)
  try{
    const { data } = await axios.get(PUB_URL, { params: { start, end } })
    return (data||[]).map(ev=>{
      const meta = unpackInfo(ev.scheduleInfo || ev.info)
      const color = meta.color || '#7c3aed'
      const inc = meta.endInclusive ? new Date(meta.endInclusive) : null
      let endExclusive = null
      const allDay = meta.allDay ?? !!inc
      if(allDay){
        endExclusive = inc ? addDays(startOfDay(inc), 1) : null
      }else{
        endExclusive = inc || null
      }
      return {
        id: String(ev.id),
        title: ev.title ?? ev.scheduleName ?? '(제목 없음)',
        start: ev.start ?? ev.scheduleDate,
        end: endExclusive,
        allDay,
        backgroundColor: color,
        borderColor: color,
        extendedProps: { color }
      }
    })
  }catch(e){
    alert('일정 불러오기 실패: ' + formatAxiosError(e))
    return []
  }
}

/* 캘린더 즉시 반영 */
function patchCalendarEventLocal(id, { title, color, allDay, start, endExclusive }) {
  const ev = calendar.getEventById(id)
  if (!ev) return
  ev.setProp('title', title)
  ev.setProp('backgroundColor', color)
  ev.setProp('borderColor', color)
  ev.setExtendedProp('color', color)
  ev.setAllDay(!!allDay)
  if (start) ev.setStart(start)
  if (endExclusive) ev.setEnd(endExclusive)
}

/* 저장 */
async function onSave(){
  try{
    if(!form.title){ alert('제목을 입력하세요.'); return }

    let startDate, endInclusive

    if (form.allDay) {
      if (pendingSelect.value?.allDay) {
        const DAY   = 24*60*60*1000
        const sSel0 = new Date(
          pendingSelect.value.start.getFullYear(),
          pendingSelect.value.start.getMonth(),
          pendingSelect.value.start.getDate()
        )
        const rawEnd   = pendingSelect.value.end ? new Date(pendingSelect.value.end) : new Date(pendingSelect.value.start)
        const dayCount = Math.max(1, Math.round((rawEnd - sSel0) / DAY))
        startDate      = sSel0
        endInclusive   = endOfDayInclusive(new Date(sSel0.getFullYear(), sSel0.getMonth(), sSel0.getDate() + (dayCount - 1)))
      } else {
        const { start00, end2359 } = normalizeAllDayRange(form.start, form.end || form.start)
        startDate    = start00
        endInclusive = end2359
      }
    } else {
      const s = fromLocalInputValue(form.start)
      const e = form.end ? fromLocalInputValue(form.end) : s
      startDate    = s
      endInclusive = (e.getFullYear()===s.getFullYear() && e.getMonth()===s.getMonth() && e.getDate()===s.getDate())
        ? e : endOfDayInclusive(s)
    }

    const startIso = isoLocal(startDate).slice(0, 19)
    const info = packInfo({
      color: form.color,
      allDay: !!form.allDay,
      endInclusive: endInclusive ? isoLocal(endInclusive).slice(0, 19) : null
    })

    if (form.id) {
      try {
        await updateEventToServer(form.id, { title: form.title, start: startIso, info })
        const endExclusive = form.allDay ? addDays(startOfDay(endInclusive), 1) : endInclusive
        patchCalendarEventLocal(form.id, {
          title: form.title, color: form.color, allDay: !!form.allDay,
          start: new Date(startIso), endExclusive
        })
      } catch {
        try { await deleteEventFromServer(form.id) } catch {}
        await createEventToServer({ title: form.title, start: startIso, info })
      }
    } else {
      await createEventToServer({ title: form.title, start: startIso, info })
    }

    await refetchFromServer()
    pendingSelect.value = null
    closeModal()
    alert('저장 완료.')
  }catch(e){
    alert('저장 실패: ' + (e.message || e))
  }
}

async function onDelete(){
  try{
    if(!form.id) return
    await deleteEventFromServer(form.id)
    await refetchFromServer()
    closeModal()
    alert('삭제 완료.')
  }catch(e){
    alert('삭제 실패: ' + (e.message || e))
  }
}

/* 요약 리스트 */
function refreshSummary(){
  if(!calendar) return
  const view = calendar.view
  const rs = view.currentStart, re = view.currentEnd
  const list = calendar.getEvents()
    .filter(ev => (ev.end || ev.start) > rs && ev.start < re)
    .map(ev=>{
      const color = ev.extendedProps?.color || ev.backgroundColor || '#7c3aed'
      const whenLabel = ev.allDay ? rangeLabelKR(ev.start, ev.end, true) : timeRange(ev.start, ev.end || ev.start)
      return {
        id: ev.id, title: ev.title || '(제목 없음)',
        start: ev.start, end: ev.end || ev.start, allDay: ev.allDay, color,
        ymd: fmtYmd(ev.start), whenLabel,
      }
    })
    .sort((a,b)=>(a.start?.getTime()||0)-(b.start?.getTime()||0))
  summary.value = list
}

/* ===== 상세일정 보임 규칙 ===== */
function hasEventsOn(date){
  if(!calendar) return false
  const s0 = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0,0,0,0)
  const e0 = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 23,59,59,999)
  return calendar.getEvents().some(ev => (ev.end || ev.start) > s0 && ev.start < e0)
}

/* 상세일정 렌더: 선택된 하루만 */
function renderAgenda(){
  if(!calendar || !selectedDate.value){ agendaHtml.value = ''; return }
  const d = selectedDate.value
  const s0 = new Date(d.getFullYear(), d.getMonth(), d.getDate(), 0,0,0,0)
  const e0 = new Date(d.getFullYear(), d.getMonth(), d.getDate(), 23,59,59,999)
  const items = calendar.getEvents()
    .filter(ev => (ev.end || ev.start) > s0 && ev.start < e0)
    .sort((a,b)=>(a.start - b.start))

  if(!items.length){ agendaHtml.value = `<div class="agenda__empty">표시할 일정이 없습니다.</div>`; return }

  const html = items.map(ev=>{
    const color = ev.extendedProps?.color || ev.backgroundColor || '#7c3aed'
    const time  = ev.allDay ? rangeLabelKR(ev.start, ev.end, true) : timeRange(ev.start, ev.end || ev.start)
    const safe  = (ev.title || '(제목 없음)').replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]))
    return `<li class="agenda__item" style="--dot-color:${color}">
              <span class="agenda__date">${fmtYmd(ev.start)}</span>
              <span class="agenda__time">${time}</span>
              <span class="agenda__title">${safe}</span>
            </li>`
  }).join('')
  agendaHtml.value = `<ul class="agenda__list">${html}</ul>`
}
function clearSelected(){
  selectedDate.value = null
  showAgenda.value   = false
  agendaHtml.value   = ''
}

/* ===== 요약에서 수정/삭제 ===== */
function editFromSummary(id){
  if(!calendar) return
  const ev = calendar.getEventById(String(id))
  if(!ev){ alert('이벤트를 찾을 수 없습니다.'); return }
  openModal('edit', ev)
}
async function deleteFromSummary(id){
  try{
    const ev = calendar.getEventById(String(id))
    if(!ev){ alert('이벤트를 찾을 수 없습니다.'); return }
    await deleteEventFromServer(ev.id)
    await refetchFromServer()
  }catch(e){
    alert('삭제 실패: ' + (e.message || e))
  }
}

/* 렌더링 */
async function refetchFromServer(){
  const view = calendar.view
  const events = await fetchEventsForRange(view.currentStart, view.currentEnd)
  calendar.removeAllEvents()
  events.forEach(e=>calendar.addEvent(e))
  refreshSummary()

  if (selectedDate.value && hasEventsOn(selectedDate.value)) {
    showAgenda.value = true
    renderAgenda()
  } else {
    showAgenda.value = false
    agendaHtml.value = ''
  }
}

/* 모달 열고 닫기 */
function openModal(mode, ev){
  isModalOpen.value = true
  if(mode==='create'){
    modalTitle.value='일정 등록'
    const today = selectedDate.value || new Date()
    Object.assign(form, {
      id:'', title:'', color:'#7c3aed', allDay:true,
      start: toLocalDateInput00(today),
      end:   toLocalDateInput2359(today)
    })
  }else{
    modalTitle.value='일정 수정'
    if (ev?.allDay) {
      const endShown = ev.end ? new Date(ev.end.getTime() - 1) : ev.start
      Object.assign(form, {
        id: ev?.id ?? '',
        title: ev?.title ?? '',
        allDay: true,
        color: ev?.extendedProps?.color || ev?.backgroundColor || '#7c3aed',
        start: toLocalDateInput00(ev.start),
        end:   toLocalDateInput2359(endShown),
      })
    } else {
      const shownEnd = ev?.end ? new Date(ev.end.getTime() - 1) : ev?.start
      Object.assign(form, {
        id: ev?.id ?? '',
        title: ev?.title ?? '',
        allDay: false,
        color: ev?.extendedProps?.color || ev?.backgroundColor || '#7c3aed',
        start: ev?.start ? toLocalInputValue(ev.start) : '',
        end:   shownEnd ? toLocalInputValue(shownEnd) : '',
      })
    }
  }
}
function closeModal(){ isModalOpen.value=false; pendingSelect.value = null }

/* 캘린더 초기화 */
onMounted(() => {
  calendar = new Calendar(calendarEl.value, {
    themeSystem: 'bootstrap5',
    locale: 'ko',
    timeZone: 'Asia/Seoul',
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin, listPlugin, bootstrap5Plugin], // ✅ Day/Week 플러그인 포함
    initialView: 'dayGridMonth',

    // ===== Month 뷰 줄수 자동(빈줄 제거) & 비현재월 숨김 / 높이 =====
    fixedWeekCount: false,        // ✅ 6줄 강제 해제
    showNonCurrentDates: false,   // ◇ 이전/다음달 날짜칸 숨김
    expandRows: true,
    height: 'auto',

    headerToolbar: {
      left: 'prev today next',
      center: '',
      right: 'dayGridMonth,dayGridWeek,timeGridDay,listMonth,addEventButton' // ✅ Day 버튼을 timeGridDay로
    },
    customButtons: {
      addEventButton: { text: '일정 등록', click: () => openModal('create') }
    },

    // 월 그리드 날짜 숫자만
    dayCellContent: (arg) => ({ html: arg.dayNumberText.replace('일', '') }),

    // 주/일 헤더 포맷
    views: {
      dayGridWeek:  { dayHeaderContent: (arg) => ({ html: koDayHeader(arg.date) }) },
      dayGridDay:   { dayHeaderContent: (arg) => ({ html: koDayHeader(arg.date) }) },
      timeGridWeek: { dayHeaderContent: (arg) => ({ html: koDayHeader(arg.date) }) }, // ✅
      timeGridDay:  { dayHeaderContent: (arg) => ({ html: koDayHeader(arg.date) }) }  // ✅
    },

    // ✅ Day/Week 시간 슬롯 표기
    slotMinTime: '07:00:00',
    slotMaxTime: '22:00:00',
    eventTimeFormat: { hour: '2-digit', minute: '2-digit', meridiem: false },

    selectable: true,
    selectMirror: true,
    unselectAuto: true,
    selectLongPressDelay: 200,

    // ===== 클릭: 셀 전체 하이라이트 + 기존 로직 유지 =====
    dateClick: (info) => {
      // 기존 선택 제거
      calendarEl.value
        .querySelectorAll('.fc .fc-daygrid-day.is-picked')
        .forEach(el => el.classList.remove('is-picked'))
      // 방금 클릭한 셀 표시
      info.dayEl.classList.add('is-picked')

      // ⬇️ 기존 동작
      pendingSelect.value = null
      const d = new Date(info.date)
      selectedDate.value = d
      if (hasEventsOn(d)) { showAgenda.value = true; renderAgenda() }
      else { showAgenda.value = false; agendaHtml.value = '' }

      openModal('create')
      form.allDay = true
      form.start = toLocalDateInput00(info.date)
      form.end   = toLocalDateInput2359(info.date)
    },

    // ===== 드래그 선택: 시작 셀 하이라이트 + 기존 로직 유지 =====
    select: (arg) => {
      // 기존 선택 제거
      calendarEl.value
        .querySelectorAll('.fc .fc-daygrid-day.is-picked')
        .forEach(el => el.classList.remove('is-picked'))
      // 드래그 시작 셀 표시
      const startCell = calendarEl.value.querySelector(`[data-date="${arg.startStr}"]`)
      if (startCell) startCell.classList.add('is-picked')

      // ⬇️ 기존 동작
      const d = new Date(arg.start)
      selectedDate.value = d
      if (hasEventsOn(d)) { showAgenda.value = true; renderAgenda() }
      else { showAgenda.value = false; agendaHtml.value = '' }

      openModal('create')
      pendingSelect.value = {
        start: new Date(arg.start),
        end: new Date(arg.end || arg.start),
        allDay: arg.allDay === true
      }

      form.allDay = arg.allDay === true
      if (form.allDay) {
        const DAY = 24 * 60 * 60 * 1000
        const start0 = new Date(arg.start.getFullYear(), arg.start.getMonth(), arg.start.getDate())
        const rawEnd = arg.end ? new Date(arg.end) : new Date(arg.start)
        const dayCount = Math.max(1, Math.round((rawEnd - start0) / DAY))
        const lastDay = new Date(start0.getFullYear(), start0.getMonth(), start0.getDate() + (dayCount - 1))
        form.start = toLocalDateInput00(start0)
        form.end   = toLocalDateInput2359(lastDay)
      } else {
        const eInc = new Date((arg.end || arg.start).getTime() - 1)
        form.start = toLocalInputValue(arg.start)
        form.end   = toLocalInputValue(eInc)
      }
      calendar.unselect()
    },

    eventClick: (arg) => openModal('edit', arg.event),

    datesSet: async () => {
      // 뷰 이동 시 하이라이트 초기화
      calendarEl.value
        .querySelectorAll('.fc .fc-daygrid-day.is-picked')
        .forEach(el => el.classList.remove('is-picked'))

      pageDate.value = calendar.getDate()
      await refetchFromServer()
    }
  })

  calendar.render()
  pageDate.value = calendar.getDate()
  setTimeout(() => calendar.updateSize(), 0)
})

onBeforeUnmount(()=>{ if(calendar) calendar.destroy() })
</script>

<style src="../assets/calendar.css"></style>
<style scoped>
/* 모달 footer */
.modal__footer{display:flex;justify-content:space-between;align-items:center;gap:12px;padding:12px 16px;border-top:1px solid #eee;}
.modal__footer .left{display:flex;align-items:center;gap:8px;}
.modal__footer .right{display:flex;align-items:center;gap:8px;}

/* 요약 */
.summary{ width: var(--cal-fixed-width, 1280px); max-width:100%; margin:16px auto 0; }
.summary__title{ margin:12px 0 10px; font-size:16px; font-weight:800; color:#111; }
.summary__list{ list-style:none; padding:0; margin:0; display:grid; gap:8px; }
.summary__item{ display:grid; grid-template-columns:10px 110px 140px 1fr auto; align-items:center; gap:10px; padding:8px 10px; border:1px solid #e9e9e9; border-radius:10px; background:#fff; }
.summary__dot{ width:8px; height:8px; border-radius:2px; background:var(--dot-color,#999); }
.summary__date,.summary__when{ font-size:13px; color:#444; }
.summary__text{ font-size:14px; color:#111; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.summary__actions{ display:flex; gap:6px; }
.btn.btn--tiny{ padding:4px 8px; border-radius:8px; border:1px solid #ddd; background:#fff; font-size:12px; cursor:pointer; }
.btn.btn--tiny:hover{ background:#f5f5f5; }
.btn--danger{ color:#dc2626; border-color:#fecaca; }

/* back button */
.back-btn{ margin-left:7rem; margin-top:-5rem; appearance:none; display:inline-flex; align-items:center; gap:.5rem; padding:.625rem 1rem; border:1px solid #e5dcc9; border-radius:.5rem; background:#fff; color:#5f5346; font-weight:700; font-size:.9375rem; line-height:1; cursor:pointer; transition:background .2s, border-color .2s, transform .02s; }
.back-btn:hover{ background:#f9f6ef; border-color:#d8cdb3; }
.back-btn:active{ transform:translateY(1px); }
.back-btn:focus-visible{ outline:2px solid transparent; box-shadow:0 0 0 3px rgba(252,112,60,.25); border-color:#fc703c; }
.back-btn:disabled{ opacity:.55; cursor:not-allowed; }

/* 상세일정 */
.agenda{ width: var(--cal-fixed-width, 1280px); max-width:100%; margin:16px auto 0; }
.agenda__title{ margin:12px 0 10px; font-size:16px; font-weight:800; color:#111; display:flex; align-items:center; gap:8px; }
.agenda__body{ background:#fff; border:1px solid #e9e9e9; border-radius:8px; padding:12px; }
.agenda__empty{ padding:14px; color:#666; font-size:14px; text-align:center; }
.agenda__list{ list-style:none; margin:0; padding:0; display:grid; gap:8px; }
.agenda__item{ display:grid; grid-template-columns:110px 140px 1fr; align-items:center; gap:10px; padding:8px 10px; border:1px solid #eee; border-radius:10px; }
</style>
