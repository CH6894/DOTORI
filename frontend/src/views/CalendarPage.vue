<template>
  <div class="page calendar-scope">
    <h2 class="calendar-title">{{ pageDate.getFullYear() }}년 {{ pageDate.getMonth() + 1 }}월</h2>
    <div class="calendar-wrap">
      <div id="calendar" ref="calendarEl"></div>
    </div>
    <!-- 상세일정: 이벤트가 있는 날짜를 클릭했을 때만 보임 -->
    <section class="agenda" v-show="showAgenda">
      <h3 class="agenda__title">
        상세일정
        <small v-if="selectedDate">— {{ selectedDate.toLocaleDateString('ko-KR') }}</small>
        <button v-if="selectedDate" class="btn btn--tiny"
                @click="selectedDate=null; showAgenda=false; agendaHtml=''">닫기</button>
      </h3>
      <div class="agenda__body" id="agenda-body" v-html="agendaHtml"></div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import axios from 'axios'
import { Calendar } from '@fullcalendar/core'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import listPlugin from '@fullcalendar/list'
import bootstrap5Plugin from '@fullcalendar/bootstrap5'


const PUB_URL = '/api/public/calendars'

const calendarEl = ref(null)
let calendar
const agendaHtml = ref('')
const pageDate = ref(new Date())

/* 상세일정: 처음엔 숨김 */
const selectedDate = ref(null)
const showAgenda   = ref(false)

/* 유틸 */
const pad=(n)=>String(n).padStart(2,'0')
const isoLocal=(d)=>{const x=new Date(d);return `${x.getFullYear()}-${pad(x.getMonth()+1)}-${pad(x.getDate())}T${pad(x.getHours())}:${pad(x.getMinutes())}:${pad(x.getSeconds())}`}
const escapeHtml=(s)=>String(s).replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]))
const fmtYmd=(d)=>`${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
const hhmm=(d)=>`${pad(d.getHours())}:${pad(d.getMinutes())}`
const timeRange=(s,e)=>(!e||e.getTime()===s.getTime())?hhmm(s):`${hhmm(s)} ~ ${hhmm(e)}`
const startOfDay=(d)=>new Date(d.getFullYear(), d.getMonth(), d.getDate())
const addDays=(d,n)=>new Date(d.getFullYear(), d.getMonth(), d.getDate()+n)
function unpackInfo(info){ try{return info?JSON.parse(info):{}}catch{return{}} }
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

/* day/week 헤더 포맷터 */
const koDayHeader = (date) => {
  const yo = '일월화수목금토'[date.getDay()]
  return `${date.getMonth() + 1}월 ${date.getDate()}일 (${yo})`
}

/* 해당 날짜에 이벤트가 있는지 */
function hasEventsOn(date){
  if(!calendar) return false
  const s0 = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0,0,0,0)
  const e0 = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 23,59,59,999)
  return calendar.getEvents().some(ev => (ev.end || ev.start) > s0 && ev.start < e0)
}

/* 조회 */
async function refetchFromServer(){
  const view = calendar.view
  const start = isoLocal(view.currentStart).slice(0,19)
  const end   = isoLocal(view.currentEnd).slice(0,19)

  try{
    const { data } = await axios.get(PUB_URL, { params:{ start, end } })
    const events = (data||[]).map(ev=>{
      const meta = unpackInfo(ev.scheduleInfo || ev.info)
      const color = meta.color || '#7c3aed'
      const inc = meta.endInclusive ? new Date(meta.endInclusive) : null
      const allDay = meta.allDay ?? !!inc
      const endExclusive = allDay ? (inc ? addDays(startOfDay(inc),1) : null) : (inc || null)
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
    calendar.removeAllEvents()
    events.forEach(e=>calendar.addEvent(e))

    // 규칙 유지: 선택일에 이벤트가 있으면 보이기
    if (selectedDate.value && hasEventsOn(selectedDate.value)) {
      showAgenda.value = true
      renderAgenda()
    } else {
      showAgenda.value = false
      agendaHtml.value = ''
    }
  }catch(e){
    console.error(e)
    calendar.removeAllEvents()
    agendaHtml.value = `<div class="agenda__empty">일정 로드 실패</div>`
  }
}

/* Agenda: 선택된 하루만 */
function renderAgenda(){
  const container = document.getElementById('agenda-body')
  if(!container) return
  if(!selectedDate.value){ agendaHtml.value=''; return }

  const d = selectedDate.value
  const s0 = new Date(d.getFullYear(), d.getMonth(), d.getDate(), 0,0,0,0)
  const e0 = new Date(d.getFullYear(), d.getMonth(), d.getDate(), 23,59,59,999)
  const items = calendar.getEvents()
    .filter(ev => (ev.end || ev.start) > s0 && ev.start < e0)
    .sort((a,b)=>(a.start-b.start))

  if(!items.length){ agendaHtml.value=`<div class="agenda__empty">표시할 일정이 없습니다.</div>`; return }

  const html = items.map(ev=>{
    const color = ev.extendedProps?.color || ev.backgroundColor || '#7c3aed'
    const time = ev.allDay ? rangeLabelKR(ev.start, ev.end, true) : timeRange(ev.start, ev.end || ev.start)
    const title = escapeHtml(ev.title || '(제목 없음)')
    return `<li class="agenda__item" style="--dot-color:${color}">
              <span class="agenda__time">${time}</span>
              <span class="agenda__title">${title}</span>
            </li>`
  }).join('')

  agendaHtml.value =
    `<section class="agenda__section">
       <h4 class="agenda__date">${d.getFullYear()}년 ${d.getMonth()+1}월 ${d.getDate()}일</h4>
       <ul class="agenda__list">${html}</ul>
     </section>`
}

onMounted(async()=>{
  calendar = new Calendar(calendarEl.value, {
    themeSystem:'bootstrap5', locale:'ko', timeZone:'Asia/Seoul',
    plugins:[dayGridPlugin, interactionPlugin, listPlugin, bootstrap5Plugin],
    initialView:'dayGridMonth',
    headerToolbar:{ left:'prev today next', center:'', right:'dayGridMonth,dayGridWeek,dayGridDay,listMonth' },

    // 월 그리드 날짜 숫자만
    dayCellContent:(arg)=>({ html: arg.dayNumberText.replace('일','') }),

    // day/week 헤더 'M월 D일 (요일)'
    views:{
      dayGridWeek:{ dayHeaderContent:(arg)=>({ html: koDayHeader(arg.date) }) },
      dayGridDay :{ dayHeaderContent:(arg)=>({ html: koDayHeader(arg.date) }) }
    },

    // 날짜 클릭/드래그 시: 이벤트가 있는 날에만 상세일정 표시
    dateClick:(info)=>{
      const d = new Date(info.date)
      selectedDate.value = d
      if (hasEventsOn(d)) { showAgenda.value = true; renderAgenda() }
      else { showAgenda.value = false; agendaHtml.value = '' }
    },
    select:(arg)=>{
      const d = new Date(arg.start)
      selectedDate.value = d
      if (hasEventsOn(d)) { showAgenda.value = true; renderAgenda() }
      else { showAgenda.value = false; agendaHtml.value = '' }
    },

    selectable:true, selectMirror:false, unselectAuto:false, eventClick:()=>{},
    datesSet: async ()=>{ pageDate.value=calendar.getDate(); await refetchFromServer() }
  })
  calendar.render()
  pageDate.value = calendar.getDate()
  setTimeout(()=>calendar.updateSize(),0)
})
onBeforeUnmount(()=>{ if(calendar) calendar.destroy() })
</script>

<style src="../assets/calendar.css"></style>
<style scoped>
.agenda { width: var(--cal-fixed-width); max-width: 100%; margin: 10px auto 0; }
.agenda__title { margin: 16px 0 10px; font-size: 16px; font-weight: 700; color: #111; display:flex; align-items:center; gap:8px; }
.agenda__body { background:#fff; border:1px solid #e9e9e9; border-radius:8px; padding:12px; }
.agenda__empty { padding:14px; color:#666; font-size:14px; text-align:center; }
.agenda__list { list-style:none; margin:0; padding:0; display:grid; gap:8px; }
.agenda__item { display:grid; grid-template-columns:140px 1fr; align-items:center; gap:10px; padding:8px 10px; border:1px solid #eee; border-radius:10px; }
</style>
