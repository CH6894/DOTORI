<template>
  <div class="page calendar-scope cute">
    <!-- 카테고리 라벨: 좌측 상단 2줄 구성 -->
    <div class="category-container">
      <strong class="category-title">카테고리</strong>

      <div class="category-labels">
        <!-- 첫 줄 (3개) -->
        <div class="category-row">
          <div
            class="cat-label"
            v-for="cat in categoryList.slice(0, 3)"
            :key="cat.name"
          >
            <span class="color-dot" :style="{ backgroundColor: cat.color }"></span>
            {{ cat.name }}
          </div>
        </div>

        <!-- 둘째 줄 (4개) -->
        <div class="category-row">
          <div
            class="cat-label"
            v-for="cat in categoryList.slice(3)"
            :key="cat.name"
          >
            <span class="color-dot" :style="{ backgroundColor: cat.color }"></span>
            {{ cat.name }}
          </div>
        </div>
      </div>
    </div>


    <h2 class="calendar-title">
      {{ pageDate.getFullYear() }}년 {{ pageDate.getMonth() + 1 }}월
      <span class="title-badge"></span>
    </h2>


    <div class="calendar-wrap" :class="{ 'is-month': isMonthView }">
      <div ref="calendarEl" id="calendar"></div>
    </div>


    <!-- 상세일정(종일/시간 분리, 읽기 전용) -->
    <section class="agenda" v-show="showAgenda" ref="agendaSection" id="agenda-top">
      <h3 class="agenda__title">
      상세일정
      <button v-if="selectedDate" class="btn btn--tiny" @click="clearSelected">닫기</button>
      </h3>
      <div class="agenda__body" v-html="agendaHtml"></div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import axios from 'axios'
import { Calendar } from '@fullcalendar/core'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import listPlugin from '@fullcalendar/list'
import bootstrap5Plugin from '@fullcalendar/bootstrap5'

const PUB_URL = '/api/public/calendars'

const categoryList = [
  { name:'Anime',  color:'#FF4C4C' },
  { name:'Webtoon',color:'#4CAF50' },
  { name:'Game',   color:'#FFD93B' },
  { name:'Sports', color:'#4285F4' },
  { name:'Creator',color:'#FF6FB5' },
  { name:'Kpop',   color:'#9B59B6' },
  { name:'기타',    color:'#FF9800' },
]

/* ===== 유틸 ===== */
const pad=(n)=>String(n).padStart(2,'0')
const startOfDay=(d)=>new Date(d.getFullYear(),d.getMonth(),d.getDate())
const addDays=(d,n)=>new Date(d.getFullYear(),d.getMonth(),d.getDate()+n)
const isoLocal=(d)=>{const x=new Date(d);return `${x.getFullYear()}-${pad(x.getMonth()+1)}-${pad(x.getDate())}T${pad(x.getHours())}:${pad(x.getMinutes())}:${pad(x.getSeconds())}`}
const escapeHtml=(s)=>String(s).replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]))
const hhmm=(d)=>`${pad(d.getHours())}:${pad(d.getMinutes())}`
const timeRange=(s,e)=>(!e||e.getTime()===s.getTime())?hhmm(s):`${hhmm(s)} ~ ${hhmm(e)}`
function unpackInfo(info){ try{return info?JSON.parse(info):{}}catch{return{}} }

/* 상태 */
const calendarEl=ref(null); let calendar
const pageDate=ref(new Date())
const isMonthView = ref(false)

/* 상세일정 상태 */
const showAgenda=ref(false)
const selectedDate=ref(null)
const agendaHtml=ref('')
const agendaSection=ref(null)

/* 스무스 스크롤 */
function scrollToAgenda(offsetY = 72){
  nextTick(()=>{
    const el = agendaSection.value || document.getElementById('agenda-top')
    if(!el) return
    const y = el.getBoundingClientRect().top + window.pageYOffset - offsetY
    window.scrollTo({ top: y, behavior: 'smooth' })
  })
}

/* 아젠다 렌더 */
function renderAgenda(){
  if(!calendar || !selectedDate.value){ agendaHtml.value=''; return }
  const d=selectedDate.value
  const s0=new Date(d.getFullYear(),d.getMonth(),d.getDate(),0,0,0,0)
  const e0=new Date(d.getFullYear(),d.getMonth(),d.getDate(),23,59,59,999)
  const items=calendar.getEvents()
    .filter(ev=>(ev.end||ev.start)>s0 && ev.start<e0)
    .sort((a,b)=>(a.start-b.start))

  if(!items.length){ agendaHtml.value=`<div class="agenda__empty">표시할 일정이 없음이다.</div>`; return }

  const allDayItems = items.filter(ev=>ev.allDay)
  const timedItems  = items.filter(ev=>!ev.allDay)

  const y=d.getFullYear(), m=String(d.getMonth()+1).padStart(2,'0'), day=String(d.getDate()).padStart(2,'0')
  const dateStr=`${y}.${m}.${day}`

  const liTimed = timedItems.map(ev=>{
    const color = ev.extendedProps?.color || ev.backgroundColor || '#ff8ab3'
    const time  = timeRange(ev.start, ev.end || ev.start)
    const title = escapeHtml(ev.title || '(제목 없음)')
    return `<li class="agenda__item" style="--dot-color:${color}">
              <span class="agenda__time">${time}</span>
              <span class="agenda__title">${title}</span>
            </li>`
  }).join('')

  const liAllDay = allDayItems.map(ev=>{
    const color = ev.extendedProps?.color || ev.backgroundColor || '#ff8ab3'
    const title = escapeHtml(ev.title || '(제목 없음)')
    return `<li class="agenda__item agenda__item--allday" style="--dot-color:${color}">
              <span class="agenda__date">${dateStr}</span>
              <span class="agenda__title">${title}</span>
            </li>`
  }).join('')

  const allDaySection = allDayItems.length
    ? `<h4 class="agenda__section-title">종일</h4>
      <ul class="agenda__list agenda__list--allday">${liAllDay}</ul>`
    : ''

  const timedSection = timedItems.length
    ? `<h4 class="agenda__section-title">시간</h4>
      <ul class="agenda__list agenda__list--timed">${liTimed}</ul>`
    : ''

  agendaHtml.value = `${allDaySection}${timedSection}`
}

function clearSelected(){ selectedDate.value=null; showAgenda.value=false; agendaHtml.value='' }

/* ===== 캘린더 초기화 ===== */
onMounted(()=>{
  calendar=new Calendar(calendarEl.value,{
    themeSystem:'bootstrap5',
    locale:'ko',
    timeZone:'Asia/Seoul',
    firstDay:0,
    dayHeaderFormat:{ weekday:'short' },
    plugins:[dayGridPlugin,timeGridPlugin,interactionPlugin,listPlugin,bootstrap5Plugin],

    stickyHeaderDates:true,
    initialView:'dayGridMonth',
    fixedWeekCount: false,
    headerToolbar:{ left:'prev today next', center:'', right:'dayGridMonth,listMonth' },

    height:'auto',
    expandRows:true,

    slotMinTime:'00:00:00',
    slotMaxTime:'24:00:00',
    slotDuration:'00:30:00',
    nowIndicator:false,         // 현재시간 라인 비활성화
    scrollTime:'06:00:00',
    scrollTimeReset:false,

    eventOverlap:false,
    eventMaxStack:3,

    dayMaxEvents:true,
    displayEventTime:true,

    dayCellContent:(arg)=>({ html: arg.dayNumberText.replace('일','') }),
    views:{
      dayGridMonth:{ buttonText:'Month', dayMaxEvents:true, eventDisplay:'block' }
    },

    selectable:true, selectMirror:true, unselectAuto:true, selectLongPressDelay:180,

    eventClick: (arg) => {
      const d = new Date(arg.event.start)
      selectedDate.value = d
      showAgenda.value = true
      renderAgenda()
      if (arg.jsEvent) arg.jsEvent.preventDefault()
      scrollToAgenda()
    },

    events: async (info, success, failure)=>{
      try{
        const s = new Date(info.start)
        const eInc = new Date(info.end.getTime()-1)
        const start = isoLocal(new Date(s.getFullYear(),s.getMonth(),s.getDate(),0,0,0)).slice(0,19)
        const end   = isoLocal(new Date(eInc.getFullYear(),eInc.getMonth(),eInc.getDate(),23,59,59)).slice(0,19)

        const { data }=await axios.get(PUB_URL,{ params:{ start, end } })
        const events=(data||[]).map(ev=>{
          const meta=unpackInfo(ev.scheduleInfo||ev.info)
          const color=meta.color||'#ff8ab3'
          const inc=meta.endInclusive?new Date(meta.endInclusive):null
          const allDay=meta.allDay??!!inc
          const endExclusive= allDay ? (inc?addDays(startOfDay(inc),1):null) : (inc||null)
          return {
            id:String(ev.id),
            title:ev.title??ev.scheduleName??'(제목 없음)',
            start:ev.start??ev.scheduleDate,
            end:endExclusive,
            allDay,
            backgroundColor:color,
            borderColor:color,
            extendedProps:{ color }
          }
        })
        success(events)
      }catch(e){ failure(e) }
    },

    dateClick:(info)=>{
      const d=new Date(info.date); selectedDate.value=d
      if(hasEventsOn(d)){ showAgenda.value=true; renderAgenda() } else { showAgenda.value=false; agendaHtml.value='' }
      scrollToAgenda()
    },

    datesSet: ()=>{
      pageDate.value=calendar.getDate()
      const t = calendar.view.type
      isMonthView.value = (t === 'dayGridMonth')
      calendar.setOption('height','auto')
      calendar.setOption('expandRows',true)
    }
  })
  calendar.render()
  pageDate.value=calendar.getDate()
  setTimeout(()=>calendar.updateSize(),0)
})
onBeforeUnmount(()=>{ if(calendar) calendar.destroy() })

function hasEventsOn(date){
  if(!calendar) return false
  const s0=new Date(date.getFullYear(),date.getMonth(),date.getDate(),0,0,0,0)
  const e0=new Date(date.getFullYear(),date.getMonth(),date.getDate(),23,59,59,999)
  return calendar.getEvents().some(ev=>(ev.end||ev.start)>s0 && ev.start<e0)
}
</script>

<style src="../assets/calendar.css"></style>
<style scoped>
/* ===== 래퍼 ===== */
.calendar-wrap{
  position:relative; z-index:1;
  width: var(--cal-fixed-width, 1280px);
  max-width:100%;
  height:auto;
  margin:0 auto 48px;
}
.calendar-wrap.is-month{ height:auto; overflow:visible; }
.calendar-wrap.is-month :deep(.fc .fc-scroller){ overflow:visible !important; }

/* 툴바(버튼) */
:deep(.fc .fc-toolbar){ position:relative; z-index:5; gap:.5rem; }
:deep(.fc .fc-toolbar .fc-button){
  display:inline-flex; align-items:center; justify-content:center;
  padding:.4rem .65rem; line-height:1.2; font-size:.875rem; font-weight:700;
  border-radius:12px;
}
:deep(.fc .fc-button-group){ display:inline-flex; gap:.25rem; }

/* ===== 귀여운 테마 ===== */
.cute :deep(.fc){ --cute-pink:#ffeed8; --cute-accent:#ffd08a; --cute-mint:#c7f5e9; --cute-ink:#574d68; }
.calendar-title{ text-align:center; font-weight:800; color:#574d68; margin:16px 0 8px; }
.title-badge{ display:inline-block; margin-left:.5rem; padding:.2rem .5rem; border-radius:999px; background:var(--cute-pink); border:1px dashed var(--cute-accent); font-size:.9rem; color:#574d68; }

/* 버튼 테마 */
:deep(.fc .fc-button){ background:#ffffff; color:#574d68; border:1px solid #ffedd3; }
:deep(.fc .fc-button:hover){ background:#fffaf5; border-color:#ffe9b3; }

/* 이벤트 카드 */
:deep(.fc .fc-timegrid-event),
:deep(.fc .fc-daygrid-event){
  border-radius:12px; border:1px solid rgba(0,0,0,.05);
  box-shadow:0 2px 8px rgba(255,138,179,.15);
  padding:2px 6px; font-weight:700;
}
:deep(.fc .fc-event){ cursor:pointer; }

/* ===== 아젠다 ===== */
.agenda{ width: var(--cal-fixed-width, 1280px); max-width:100%; margin:16px auto 0; }
.agenda__title{ display:flex; align-items:center; gap:8px; color:#574d68; font-weight:800; }
.agenda__body{ background:#fff; border:1px solid #ffe4ee; border-radius:14px; padding:12px; }
.agenda__section-title{ margin:8px 0 6px; font-size:14px; font-weight:900; color:#7b718f; }
.agenda__list{ list-style:none; margin:0; padding:0; display:grid; gap:8px; }
.agenda__list--timed .agenda__item,
.agenda__list--allday .agenda__item{
  display:grid; grid-template-columns:140px 1fr; align-items:center;
  gap:10px; padding:8px 10px; border:1px dashed #ffd3e1; border-radius:12px;
}
.agenda__item::before{ content:''; width:8px; height:8px; border-radius:3px; background:var(--dot-color,#ff8ab3); display:inline-block; margin-right:8px; }
.agenda__date,.agenda__time{ font-weight:800; color:#7b718f; }
.agenda__title{ color:#2f2a3b; }

/* =========================
  timeGrid(week/day) : 도트 가로선 제거 + 빈칸 제거
  ========================= */

/* 내부 스크롤 제거 */
:deep(.fc-timeGridWeek-view .fc-scroller),
:deep(.fc-timeGridDay-view  .fc-scroller){
  height:auto !important;
  overflow:visible !important;
}

/* 본문 높이 자동화 */
:deep(.fc-timeGridWeek-view .fc-timegrid-body),
:deep(.fc-timeGridDay-view  .fc-timegrid-body),
:deep(.fc-timeGridWeek-view .fc-timegrid-body > table),
:deep(.fc-timeGridDay-view  .fc-timegrid-body > table){
  height:auto !important;
  min-height:0 !important;
  max-height:none !important;
}

/* 도트 가로선 완전 제거 */
:deep(.fc-timegrid-slot),
:deep(.fc-timegrid-slot-lane),
:deep(.fc-timegrid-divider){
  border:0 !important;
  border-bottom:0 !important;
  background:none !important;
  box-shadow:none !important;
}
:deep(.fc-theme-standard td),
:deep(.fc-theme-standard th){
  border-style: solid !important;
  border-bottom-color: transparent !important;
  background-image:none !important;
}

/* 시간축 제거 */
:deep(.fc-timegrid .fc-timegrid-axis),
:deep(.fc-timegrid .fc-timegrid-axis-frame),
:deep(.fc-timegrid .fc-timegrid-axis-cushion){
  display:none !important;
}

/* 7등분 */
:deep(.fc-timegrid .fc-col-header),
:deep(.fc-timegrid .fc-timegrid-body .fc-scrollgrid-sync-table){
  table-layout:fixed !important; width:100% !important;
}
:deep(.fc-timegrid .fc-col-header colgroup col),
:deep(.fc-timegrid .fc-timegrid-body colgroup col){
  width:calc(100%/7) !important;
}
/* ===== 상세일정 항목: 날짜/시간 ↔ 제목 두 칸 반듯하게 ===== */
.agenda__list--allday .agenda__item,
.agenda__list--timed .agenda__item {
  display: grid;
  grid-template-columns: 140px 1fr; /* 왼쪽 고정, 오른쪽 유동 */
  align-items: center;
  gap: 12px;
  padding: 8px 10px;
  border: 1px dashed #ffd3e1;
  border-radius: 12px;
}

/* 왼쪽 칸: 날짜나 시간 */
.agenda__date,
.agenda__time {
  text-align: right;   /* 숫자가 깔끔히 붙도록 오른쪽 정렬 */
  font-weight: 700;
  color: #7b718f;
  white-space: nowrap; /* 줄바꿈 방지 */
}

/* 오른쪽 칸: 제목 */
.agenda__title {
  font-size: 14px;
  color: #2f2a3b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis; /* 길면 … 처리 */
}
/* ===== 카테고리 라벨 ===== */
.category-container {
  width: var(--cal-fixed-width, 1280px); /* 캘린더 너비와 정확히 맞춤 */
  max-width: 100%;
  margin: 32px auto 1px; /* 위쪽 여백 + 가운데 정렬 */
}

.category-title {
  font-weight: bold;
  font-size: 14px;
  color: #2f2a3b;
  margin-bottom: 4px;
}

.category-labels {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.category-row {
  display: flex;
  gap: 10px;
}

.cat-label {
  display: inline-flex;
  align-items: center;
  font-size: 13px;
  font-weight: 600;
  color: #574d68;
  background: #fffaf5;
  padding: 6px 12px;
  border-radius: 20px;
  border: 1px dashed #ffd3a5;
}

.color-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}
</style>