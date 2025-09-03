<template>
  <div class="page calendar-scope cute">
    <h2 class="calendar-title">
      {{ pageDate.getFullYear() }}년 {{ pageDate.getMonth() + 1 }}월
      <span class="title-badge"></span>
    </h2>

    <div class="calendar-wrap" :class="{ 'is-month': isMonthView }">
      <div ref="calendarEl" id="calendar"></div>
    </div>

    <!-- 상세일정(종일/시간 분리, 읽기 전용) -->
    <section class="agenda" v-show="showAgenda">
      <h3 class="agenda__title">
        상세일정
        <button v-if="selectedDate" class="btn btn--tiny" @click="clearSelected">닫기</button>
      </h3>
      <div class="agenda__body" v-html="agendaHtml"></div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import axios from 'axios'

import { Calendar } from '@fullcalendar/core'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import listPlugin from '@fullcalendar/list'
import bootstrap5Plugin from '@fullcalendar/bootstrap5'
import 'bootstrap/dist/css/bootstrap.min.css'

/* API */
const PUB_URL = '/api/public/calendars'

/* ===== 유틸 ===== */
const pad=(n)=>String(n).padStart(2,'0')
const fmtYmd=(d)=>`${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
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
const showAgenda=ref(false); const selectedDate=ref(null); const agendaHtml=ref('')

/* 아젠다 렌더(✅ 종일에도 날짜 표시) */
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

  // 선택 날짜 문자열
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

  // ✅ 종일: 날짜 + 제목 2열
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

/* 캘린더 초기화(조회 전용) */
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
    headerToolbar:{
      left:'prev today next',
      center:'',
      right:'dayGridMonth,timeGridWeek,timeGridDay,listMonth'
    },

    height:'100%',
    expandRows:true,

    slotMinTime:'06:00:00',
    slotMaxTime:'24:00:00',
    slotDuration:'00:30:00',
    nowIndicator:true,
    scrollTime:'09:00:00',
    scrollTimeReset:false,

    eventOverlap:false,
    eventMaxStack:3,
    dayMaxEvents:true,
    displayEventTime:true,

    dayCellContent:(arg)=>({ html: arg.dayNumberText.replace('일','') }),

    /* 조회용 세팅 */
    selectable:false,
    editable:false,
    eventClick: (arg) => {
      // Day/Week/Month 어디서든 이벤트 클릭 시 '편집' 모달 열기
      // (CalendarManager에서 쓰던 openModal 을 그대로 사용)
      try {
      openModal('edit', arg.event);
      } catch (e) {
        // 혹시 현재 파일에 openModal 이 없다면 CalendarManager 라우팅으로 대체
        // router.push({ name: 'CalendarManager', query: { editId: arg.event.id } })
      }

      // 클릭한 일정 날짜로 아젠다도 동기화(하단 상세 보기 켜기)
      const d = new Date(arg.event.start);
      selectedDate.value = d;
      showAgenda.value = true;
      renderAgenda();

      // a[href]로 등록된 이벤트라도 페이지 이동 막기
      if (arg.jsEvent) arg.jsEvent.preventDefault();
    },


    /* 이벤트 소스(Manager 동일 매핑) */
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
      }catch(e){
        console.error('일정 로드 실패', e)
        failure(e)
      }
    },

    /* 날짜 클릭 시 아젠다 표시만 */
    dateClick:(info)=>{
      const d=new Date(info.date); selectedDate.value=d
      if(hasEventsOn(d)){ showAgenda.value=true; renderAgenda() } else { showAgenda.value=false; agendaHtml.value='' }
    },

    datesSet: ()=>{
      pageDate.value=calendar.getDate()
      const t = calendar.view.type
      isMonthView.value = (t === 'dayGridMonth')
    }
  })
  calendar.render(); pageDate.value=calendar.getDate(); setTimeout(()=>calendar.updateSize(),0)
})
onBeforeUnmount(()=>{ if(calendar) calendar.destroy() })

/* 보조: 해당 날짜에 이벤트 존재 여부 */
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
  position:relative; z-index:1; overflow:visible;
  width: var(--cal-fixed-width, 1280px);
  max-width:100%;
  height: var(--cal-fixed-height, 720px);
  margin:0 auto 60px;
}
.calendar-wrap.is-month{ height:auto; overflow:visible; }
.calendar-wrap.is-month :deep(.fc .fc-scroller){ overflow:visible !important; }

/* 툴바 */
:deep(.fc .fc-toolbar){ position:relative; z-index:5; gap:.5rem; }

/* ===== 귀여운 테마 ===== */
.cute :deep(.fc){
  --cute-pink:#ffeed8; --cute-accent:#ffd08a; --cute-mint:#c7f5e9; --cute-ink:#574d68;
}
.calendar-title{ text-align:center; font-weight:800; color:#574d68; margin:16px 0 8px; }
.title-badge{
  display:inline-block; margin-left:.5rem; padding:.2rem .5rem; border-radius:999px;
  background:var(--cute-pink); border:1px dashed var(--cute-accent); font-size:.9rem; color:#574d68;
}

/* 버튼 */
:deep(.fc .fc-button){
  background:#ffffff; color:#574d68; border:1px solid #ffedd3; border-radius:12px; padding:.4rem .65rem; font-weight:700;
}
:deep(.fc .fc-button:hover){ background:#fffaf5; border-color:#ffe9b3; }

/* Day/Week 라인 선명도 & 세로선 복구 */
:deep(.fc-theme-standard .fc-scrollgrid){ border:1px solid #f1dfe8; }
:deep(.fc-theme-standard td),
:deep(.fc-theme-standard th){ border:1px solid #f1dfe8; }
:deep(.fc-timegrid .fc-timegrid-col){ border-left:1px solid #f1dfe8 !important; }
:deep(.fc-timegrid .fc-timegrid-axis){ border-right:1px solid #f1dfe8 !important; }
:deep(.fc-timegrid .fc-timegrid-slot){ border-bottom:1px solid #f1dfe8 !important; }
:deep(.fc-timegrid .fc-timegrid-slots table){ border-collapse:separate !important; border-spacing:0 !important; }

/* 이벤트 카드 */
:deep(.fc .fc-timegrid-event),
:deep(.fc .fc-daygrid-event){
  border-radius:12px; border:1px solid rgba(0,0,0,.05);
  box-shadow:0 2px 8px rgba(255,138,179,.15);
  padding:2px 6px; font-weight:700;
}
:deep(.fc .fc-event){ cursor: pointer; }

/* ===== 아젠다 ===== */
.agenda{ width: var(--cal-fixed-width, 1280px); max-width:100%; margin:16px auto 0; }
.agenda__title{ display:flex; align-items:center; gap:8px; color:#574d68; font-weight:800; }
.agenda__body{ background:#fff; border:1px solid #ffe4ee; border-radius:14px; padding:12px; }

.agenda__section-title{ margin:8px 0 6px; font-size:14px; font-weight:900; color:#7b718f; }
.agenda__list{ list-style:none; margin:0; padding:0; display:grid; gap:8px; }

/* 시간 섹션: 시간+제목 2열 */
.agenda__list--timed .agenda__item{
  display:grid; grid-template-columns:140px 1fr; align-items:center;
  gap:10px; padding:8px 10px; border:1px dashed #ffd3e1; border-radius:12px;
}

/* ✅ 종일 섹션: 날짜+제목 2열 */
.agenda__list--allday .agenda__item{
  display:grid; grid-template-columns:140px 1fr; align-items:center;
  gap:10px; padding:8px 10px; border:1px dashed #ffd3e1; border-radius:12px;
}

/* 컬러 점 */
.agenda__item::before{
  content:''; width:8px; height:8px; border-radius:3px;
  background:var(--dot-color,#ff8ab3);
  display:inline-block; margin-right:8px;
}

.agenda__date{ font-weight:800; color:#7b718f; }
.agenda__time{ font-weight:800; color:#7b718f; }
.agenda__title{ display:inline-block; color:#2f2a3b; }
/* === [timeGrid 세로선 끊김 고정 패치] ========================= */

/* 1) 캘린더 영역과 내부 스크롤 표면을 전부 흰 배경으로 고정(배경 비침 차단) */
.calendar-wrap{
  background:#fff;         /* ✅ 뒤 배경(물결 등) 비치지 않게 */
  overflow:hidden;         /* 내부 스크롤러 경계 밖으로 튀어나오는 것 차단 */
}
:deep(.fc),
:deep(.fc .fc-scrollgrid),
:deep(.fc .fc-scroller),
:deep(.fc .fc-scroller-harness),
:deep(.fc .fc-scroller-liquid-absolute),
:deep(.fc .fc-timegrid),
:deep(.fc .fc-timegrid-body),
:deep(.fc .fc-timegrid-slots){
  background:#fff !important;
}

/* 2) 세로/가로 경계선을 명시적으로 그려, 중간 끊김 방지 */
:deep(.fc-theme-standard .fc-scrollgrid){ border:1px solid #e9e0da; }
:deep(.fc-theme-standard td),
:deep(.fc-theme-standard th){ border:1px solid #e9e0da; }

:deep(.fc-timegrid .fc-timegrid-col){ border-left:1px solid #e9e0da !important; }
:deep(.fc-timegrid .fc-timegrid-axis){ border-right:1px solid #e9e0da !important; }
:deep(.fc-timegrid .fc-timegrid-slot){ border-bottom:1px solid #e9e0da !important; }

/* 3) 테이블 경계의 서브픽셀 갭 제거(브라우저별 1px 틈 방지) */
:deep(.fc-timegrid .fc-timegrid-slots table){
  border-collapse: separate !important;
  border-spacing: 0 !important;
}

/* 4) 드물게 오버레이가 라인을 덮는 경우 대비(하이라이트/배경 이벤트 투명도 낮춤) */
:deep(.fc .fc-bg-event){ opacity: .9; }       /* 필요 시 조정 */
:deep(.fc .fc-highlight){ opacity: .3; }      /* 드래그/선택 하이라이트 */

/* 5) 아주 고집센 환경에서의 마지막 수단: outline(보더 덮임 방지) */
/* 필요 시 주석 해제해서 사용
:deep(.fc-timegrid .fc-timegrid-col){ outline:1px solid #e9e0da; outline-offset:-1px; }
*/

</style>
