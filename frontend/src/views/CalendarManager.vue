<template>
  <div class="page calendar-scope cute">
    <h2 class="calendar-title">
      {{ pageDate.getFullYear() }}년 {{ pageDate.getMonth() + 1 }}월
      <span class="title-badge"></span>
    </h2>

    <div class="calendar-wrap" :class="{ 'is-month': isMonthView }">
      <div ref="calendarEl" id="calendar"></div>
    </div>

    <!-- 요약 -->
    <section class="summary">
      <h3 class="summary__title">요약</h3>
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
      <div v-else class="summary__empty">현재 화면 범위에 일정이 없음이다.</div>
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
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import listPlugin from '@fullcalendar/list'
import bootstrap5Plugin from '@fullcalendar/bootstrap5'
import 'bootstrap/dist/css/bootstrap.min.css'

/* API */
const PUB_URL = '/api/public/calendars'
const ADM_URL = '/api/admin/calendars'

/* ===== 유틸 ===== */
const pad=(n)=>String(n).padStart(2,'0')
const toLocalInputValue=(dLike)=>{const d=new Date(dLike);return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`}
const fromLocalInputValue=(v)=>v?new Date(v):null
const fmtYmd=(d)=>`${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
const startOfDay=(d)=>new Date(d.getFullYear(),d.getMonth(),d.getDate())
const endOfDayInclusive=(d)=>new Date(d.getFullYear(),d.getMonth(),d.getDate(),23,59,59,999)
const addDays=(d,n)=>new Date(d.getFullYear(),d.getMonth(),d.getDate()+n)
const isoLocal=(d)=>{const x=new Date(d);return `${x.getFullYear()}-${pad(x.getMonth()+1)}-${pad(x.getDate())}T${pad(x.getHours())}:${pad(x.getMinutes())}:${pad(x.getSeconds())}`}
/* Axios 에러 포맷 */
const formatAxiosError=(e)=> e?.response?`HTTP ${e.response.status} ${e.response.statusText}\n${JSON.stringify(e.response.data)}`:(e?.request?'응답 없음(네트워크/CORS 가능성)':`설정 오류: ${e?.message}`)

/* 요약 표기 */
const hhmm=(d)=>`${pad(d.getHours())}:${pad(d.getMinutes())}`
const timeRange=(s,e)=>(!e||e.getTime()===s.getTime())?hhmm(s):`${hhmm(s)} ~ ${hhmm(e)}`
const dateLabelKR=(d)=>`${d.getMonth()+1}월 ${d.getDate()}일`
const dayOnlyKR=(d)=>`${d.getDate()}일`
function rangeLabelKR(start,endExclusive,allDay){
  if(!allDay) return null
  if(!endExclusive) return '종일'
  const endInc=new Date(endExclusive.getTime()-1)
  if(start.toDateString()===endInc.toDateString()) return '종일'
  const sameMonth=(start.getFullYear()===endInc.getFullYear())&&(start.getMonth()===endInc.getMonth())
  return sameMonth?`${dayOnlyKR(start)}부터 ${dayOnlyKR(endInc)}까지`:`${dateLabelKR(start)}부터 ${dateLabelKR(endInc)}까지`
}

/* datetime-local */
const ymd=(d)=>`${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
const toLocalDateInput00=(d)=>`${ymd(new Date(d))}T00:00`
const toLocalDateInput2359=(d)=>`${ymd(new Date(d))}T23:59`

/* ✅ 종일 입력값 정규화 */
function normalizeAllDayRange(startVal, endVal){
  const s = new Date(startVal)
  const e = new Date(endVal || startVal)
  const start00 = new Date(s.getFullYear(), s.getMonth(), s.getDate(), 0, 0, 0, 0)
  const end2359 = new Date(e.getFullYear(), e.getMonth(), e.getDate(), 23, 59, 59, 999)
  return { start00, end2359 }
}

/* scheduleInfo JSON */
function packInfo(obj){ try{return JSON.stringify(obj)}catch{return null} }
function unpackInfo(info){ try{return info?JSON.parse(info):{}}catch{return{}} }

/* 상태 */
const calendarEl=ref(null); let calendar
const isModalOpen=ref(false); const modalTitle=ref('일정')
const pageDate=ref(new Date()); const summary=ref([])
const ADMIN_USER_ID=1
const form=reactive({ id:'', title:'', start:'', end:'', allDay:false, color:'#ff8ab3' })

/* 상세일정 상태 */
const showAgenda=ref(false); const selectedDate=ref(null); const agendaHtml=ref(''); const pendingSelect=ref(null)
const isMonthView = ref(false)

/* 종일 토글 */
watch(()=>form.allDay,(v)=>{
  if(v){
    const s=fromLocalInputValue(form.start)||new Date()
    const e=form.end?fromLocalInputValue(form.end):s
    form.start=toLocalDateInput00(s); form.end=toLocalDateInput2359(e)
  }
})

/* ===== 모달/폼 헬퍼 ===== */
function resetForm(){
  form.id=''; form.title=''; form.start=''; form.end=''; form.allDay=false; form.color='#ff8ab3'
}
function fillFormFromEvent(ev){
  const meta = ev.extendedProps || {}
  const allDay = !!ev.allDay
  form.id = String(ev.id)
  form.title = ev.title || ''
  form.allDay = allDay
  form.color = meta.color || ev.backgroundColor || '#ff8ab3'
  if(allDay){
    const s0 = new Date(ev.start.getFullYear(), ev.start.getMonth(), ev.start.getDate())
    const eInc = new Date((ev.end || ev.start).getTime() - 1)
    form.start = toLocalDateInput00(s0)
    form.end   = toLocalDateInput2359(eInc)
  }else{
    const eInc = new Date((ev.end || ev.start).getTime() - 1)
    form.start = toLocalInputValue(ev.start)
    form.end   = toLocalInputValue(eInc)
  }
}
function openModal(mode='create', evObj){
  isModalOpen.value = true
  if(mode==='create'){
    modalTitle.value = '일정 등록'
    resetForm()
    // dateClick/select에서 미리 세팅한 값 유지하도록 둠이다.
  }else{
    modalTitle.value = '일정 수정'
    if(evObj) fillFormFromEvent(evObj)
  }
}
function closeModal(){
  isModalOpen.value = false
  pendingSelect.value = null
}

/* ===== 서버 CRUD ===== */
async function createEventToServer({ title, start, info }){
  const body={ scheduleName:title, scheduleDate:start, scheduleInfo:info??null, userId:ADMIN_USER_ID }
  const { data }=await axios.post(ADM_URL, body, { headers:{'Content-Type':'application/json'} })
  return data
}
async function updateEventToServer(id,{ title, start, info }){
  const body={ scheduleName:title, scheduleDate:start, scheduleInfo:info??null, userId:ADMIN_USER_ID }
  return axios.put(`${ADM_URL}/${encodeURIComponent(id)}`, body, { headers:{'Content-Type':'application/json'} })
}
async function deleteEventFromServer(id){ await axios.delete(`${ADM_URL}/${encodeURIComponent(id)}`) }

function patchCalendarEventLocal(id,{ title,color,allDay,start,endExclusive }){
  const ev=calendar.getEventById(id); if(!ev) return
  ev.setProp('title',title); ev.setProp('backgroundColor',color); ev.setProp('borderColor',color)
  ev.setExtendedProp('color',color); ev.setAllDay(!!allDay)
  if(start) ev.setStart(start); if(endExclusive) ev.setEnd(endExclusive)
}

/* ===== 저장/삭제 버튼 ===== */
async function onSave(){
  try{
    if(!form.title){ alert('제목을 입력하세요.'); return }
    let startDate, endInclusive
    if(form.allDay){
      if(pendingSelect.value?.allDay){
        const DAY=24*60*60*1000
        const sSel0=new Date(pendingSelect.value.start.getFullYear(),pendingSelect.value.start.getMonth(),pendingSelect.value.start.getDate())
        const rawEnd=pendingSelect.value.end?new Date(pendingSelect.value.end):new Date(pendingSelect.value.start)
        const dayCount=Math.max(1,Math.round((rawEnd-sSel0)/DAY))
        startDate=sSel0
        endInclusive=endOfDayInclusive(new Date(sSel0.getFullYear(),sSel0.getMonth(),sSel0.getDate()+(dayCount-1)))
      }else{
        const { start00,end2359 }=normalizeAllDayRange(form.start, form.end||form.start)
        startDate=start00; endInclusive=end2359
      }
    }else{
      const s=fromLocalInputValue(form.start)
      const e=form.end?fromLocalInputValue(form.end):s
      startDate=s
      endInclusive=(e.getFullYear()===s.getFullYear()&&e.getMonth()===s.getMonth()&&e.getDate()===s.getDate())?e:endOfDayInclusive(s)
    }
    const startIso=isoLocal(startDate).slice(0,19)
    const info=packInfo({ color:form.color, allDay:!!form.allDay, endInclusive:endInclusive?isoLocal(endInclusive).slice(0,19):null })
    if(form.id){
      try{
        await updateEventToServer(form.id,{ title:form.title,start:startIso,info })
        const endExclusive=form.allDay?addDays(startOfDay(endInclusive),1):endInclusive
        patchCalendarEventLocal(form.id,{ title:form.title,color:form.color,allDay:!!form.allDay,start:new Date(startIso),endExclusive })
      }catch{
        try{ await deleteEventFromServer(form.id) }catch{}
        await createEventToServer({ title:form.title,start:startIso,info })
      }
    }else{
      await createEventToServer({ title:form.title,start:startIso,info })
    }
    calendar.refetchEvents(); pendingSelect.value=null; closeModal(); alert('저장 완료이다.')
  }catch(e){ alert('저장 실패: '+(e.message||e)) }
}
async function onDelete(){
  try{
    if(!form.id) return
    await deleteEventFromServer(form.id)
    calendar.refetchEvents(); closeModal(); alert('삭제 완료이다.')
  }catch(e){ alert('삭제 실패: '+(e.message||e)) }
}

/* ===== 요약/아젠다 ===== */
function refreshSummary(){
  if(!calendar) return
  const view=calendar.view
  const rs=view.currentStart, re=view.currentEnd
  const list=calendar.getEvents()
    .filter(ev => (ev.end||ev.start)>rs && ev.start<re)
    .map(ev=>{
      const color=ev.extendedProps?.color||ev.backgroundColor||'#ff8ab3'
      const whenLabel= ev.allDay ? rangeLabelKR(ev.start,ev.end,true) : timeRange(ev.start, ev.end||ev.start)
      return { id:ev.id, title:ev.title||'(제목 없음)', start:ev.start, end:ev.end||ev.start, allDay:ev.allDay, color, ymd:fmtYmd(ev.start), whenLabel }
    })
    .sort((a,b)=>(a.start?.getTime()||0)-(b.start?.getTime()||0))
  summary.value=list
}
function hasEventsOn(date){
  if(!calendar) return false
  const s0=new Date(date.getFullYear(),date.getMonth(),date.getDate(),0,0,0,0)
  const e0=new Date(date.getFullYear(),date.getMonth(),date.getDate(),23,59,59,999)
  return calendar.getEvents().some(ev=>(ev.end||ev.start)>s0 && ev.start<e0)
}
function renderAgenda(){
  if(!calendar || !selectedDate.value){ agendaHtml.value=''; return }
  const d=selectedDate.value
  const s0=new Date(d.getFullYear(),d.getMonth(),d.getDate(),0,0,0,0)
  const e0=new Date(d.getFullYear(),d.getMonth(),d.getDate(),23,59,59,999)
  const items=calendar.getEvents().filter(ev=>(ev.end||ev.start)>s0 && ev.start<e0).sort((a,b)=>(a.start-b.start))
  if(!items.length){ agendaHtml.value=`<div class="agenda__empty">표시할 일정이 없음이다.</div>`; return }
  const html=items.map(ev=>{
    const color=ev.extendedProps?.color||ev.backgroundColor||'#ff8ab3'
    const time= ev.allDay ? rangeLabelKR(ev.start,ev.end,true) : timeRange(ev.start, ev.end||ev.start)
    const safe=(ev.title||'(제목 없음)').replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]))
    return `<li class="agenda__item" style="--dot-color:${color}"><span class="agenda__date">${fmtYmd(ev.start)}</span><span class="agenda__time">${time}</span><span class="agenda__title">${safe}</span></li>`
  }).join('')
  agendaHtml.value=`<ul class="agenda__list">${html}</ul>`
}
function clearSelected(){ selectedDate.value=null; showAgenda.value=false; agendaHtml.value='' }

/* 요약에서 수정/삭제 */
function editFromSummary(id){
  if(!calendar) return
  const ev=calendar.getEventById(String(id))
  if(!ev){ alert('이벤트를 찾을 수 없음이다.'); return }
  openModal('edit', ev)
}
async function deleteFromSummary(id){
  try{
    const ev=calendar.getEventById(String(id))
    if(!ev){ alert('이벤트를 찾을 수 없음이다.'); return }
    await deleteEventFromServer(ev.id); calendar.refetchEvents()
  }catch(e){ alert('삭제 실패: '+(e.message||e)) }
}

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
    headerToolbar:{
      left:'prev today next',
      center:'',
      right:'dayGridMonth,timeGridWeek,timeGridDay,listMonth,addEventButton'
    },
    customButtons:{ addEventButton:{ text:' 일정 등록', click:()=>openModal('create') } },

    height:'100%',
    expandRows:true,

    slotMinTime:'00:00:00',
    slotMaxTime:'24:00:00',
    slotDuration:'00:30:00',
    nowIndicator:true,
    scrollTime:'06:00:00',
    scrollTimeReset:false,

    eventOverlap:false,
    eventMaxStack:3,

    dayMaxEvents:true,
    displayEventTime:true,

    dayCellContent:(arg)=>({ html: arg.dayNumberText.replace('일','') }),
    views:{
      fiveWeek:{ type:'dayGrid', duration:{ weeks:5 }, dateAlignment:'month', dateIncrement:{ months:1 }, buttonText:'Month(5주)' },
      dayGridMonth:{ buttonText:'Month', dayMaxEvents:true, eventDisplay:'block' },
      timeGridDay:{ dayHeaderFormat:{ weekday:'short', month:'numeric', day:'numeric' } },
      timeGridWeek:{ dayHeaderFormat:{ weekday:'short', month:'numeric', day:'numeric' } },
      dayGridWeek:{ dayHeaderFormat:{ weekday:'short', month:'numeric', day:'numeric' } }
    },

    selectable:true, selectMirror:true, unselectAuto:true, selectLongPressDelay:180,

    /* ✅ 드래그/리사이즈 활성화 */
    editable:true,
    eventStartEditable:true,
    eventDurationEditable:true,

    /* ✅ 이벤트 로드 */
    events: async (info, success, failure)=>{
      try{
        // 뷰 범위를 로컬 00:00 ~ 23:59:59로 보냄이다.
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
        alert('일정 불러오기 실패: '+formatAxiosError(e))
        failure(e)
      }
    },

    /* ✅ 이벤트 세트 변경 후 동기화 */
    eventsSet: ()=>{
      refreshSummary()
      if(selectedDate.value && hasEventsOn(selectedDate.value)){ showAgenda.value=true; renderAgenda() }
      else{ showAgenda.value=false; agendaHtml.value='' }
    },

    /* ✅ 클릭/선택으로 등록 */
    dateClick:(info)=>{
      pendingSelect.value=null
      const d=new Date(info.date); selectedDate.value=d
      if(hasEventsOn(d)){ showAgenda.value=true; renderAgenda() } else { showAgenda.value=false; agendaHtml.value='' }
      openModal('create')
      const vt = calendar.view.type
      if (vt.startsWith('dayGrid')) {
        form.allDay = true
        form.start = toLocalDateInput00(info.date)
        form.end   = toLocalDateInput2359(info.date)
      } else {
        form.allDay = false
        form.start = toLocalInputValue(info.date)
        form.end   = toLocalInputValue(info.date)
      }
    },
    select:(arg)=>{
      const d=new Date(arg.start); selectedDate.value=d
      if(hasEventsOn(d)){ showAgenda.value=true; renderAgenda() } else { showAgenda.value=false; agendaHtml.value='' }
      openModal('create')
      pendingSelect.value={ start:new Date(arg.start), end:new Date(arg.end||arg.start), allDay:arg.allDay===true }
      form.allDay = arg.allDay===true
      if(form.allDay){
        const DAY=24*60*60*1000
        const s0=new Date(arg.start.getFullYear(),arg.start.getMonth(),arg.start.getDate())
        const rawEnd=arg.end?new Date(arg.end):new Date(arg.start)
        const dayCount=Math.max(1,Math.round((rawEnd-s0)/DAY))
        const lastDay=new Date(s0.getFullYear(),s0.getMonth(),s0.getDate()+(dayCount-1))
        form.start=toLocalDateInput00(s0); form.end=toLocalDateInput2359(lastDay)
      }else{
        const eInc=new Date((arg.end||arg.start).getTime()-1)
        form.start=toLocalInputValue(arg.start); form.end=toLocalInputValue(eInc)
      }
      calendar.unselect()
    },

    /* ✅ 이벤트 클릭 시 편집 */
    eventClick: (arg) => {
      try { openModal('edit', arg.event) } catch {}
      const d = new Date(arg.event.start)
      selectedDate.value = d
      showAgenda.value = true
      renderAgenda()
      if (arg.jsEvent) arg.jsEvent.preventDefault()
    },

    /* ✅ 드래그 이동 서버 반영 */
    eventDrop: async (info) => {
      try{
        const ev = info.event
        const allDay = !!ev.allDay
        const startIso = isoLocal(ev.start).slice(0,19)
        const e = ev.end || ev.start
        const endInclusive = allDay
          ? isoLocal(new Date(e.getTime() - 1)).slice(0,19)
          : isoLocal(e).slice(0,19)
        const color = ev.extendedProps?.color || ev.backgroundColor || '#ff8ab3'
        const infoJson = packInfo({ color, allDay, endInclusive })
        await updateEventToServer(ev.id, { title: ev.title, start: startIso, info: infoJson })
        refreshSummary(); renderAgenda()
      }catch(e){
        info.revert()
        alert('이동 저장 실패: ' + formatAxiosError(e))
      }
    },

    /* ✅ 리사이즈 서버 반영 */
    eventResize: async (info) => {
      try{
        const ev = info.event
        const allDay = !!ev.allDay
        const startIso = isoLocal(ev.start).slice(0,19)
        const e = ev.end || ev.start
        const endInclusive = allDay
          ? isoLocal(new Date(e.getTime() - 1)).slice(0,19)
          : isoLocal(e).slice(0,19)
        const color = ev.extendedProps?.color || ev.backgroundColor || '#ff8ab3'
        const infoJson = packInfo({ color, allDay, endInclusive })
        await updateEventToServer(ev.id, { title: ev.title, start: startIso, info: infoJson })
        refreshSummary(); renderAgenda()
      }catch(e){
        info.revert()
        alert('리사이즈 저장 실패: ' + formatAxiosError(e))
      }
    },

    datesSet: ()=>{
      pageDate.value=calendar.getDate()
      const t = calendar.view.type
      isMonthView.value = (t === 'fiveWeek' || t === 'dayGridMonth')
    }
  })
  calendar.render(); pageDate.value=calendar.getDate(); setTimeout(()=>calendar.updateSize(),0)
})
onBeforeUnmount(()=>{ if(calendar) calendar.destroy() })
</script>

<style src="../assets/calendar.css"></style>
<style scoped>
/* ===== 래퍼 ===== */
.calendar-wrap{
  position:relative; z-index:1; overflow:visible;
  width: var(--cal-fixed-width, 1280px);
  max-width:100%;
  height: var(--cal-fixed-height, 720px);
  margin:0 auto;
}
/* Month(5주/기본): 자동 높이, 내부 스크롤 해제 */
.calendar-wrap.is-month{ height:auto; overflow:visible; }
.calendar-wrap.is-month :deep(.fc .fc-scroller){ overflow:visible !important; }

/* Month 하단 1줄 제거 */
.calendar-wrap.is-month :deep(.fc-daygrid-body tr:nth-child(n+6)){ display:none !important; }
.calendar-wrap.is-month :deep(.fc-scrollgrid-section-footer){ display:none !important; }
.calendar-wrap.is-month :deep(.fc-scrollgrid-sync-table){ height:auto !important; }

/* 툴바 클릭 레이어 보장 */
:deep(.fc .fc-toolbar){ position:relative; z-index:5; }

/* ===== 귀여운 테마 ===== */
.cute :deep(.fc){
  --cute-pink:#ffeed8; --cute-accent:#ffd08a; --cute-mint:#c7f5e9; --cute-ink:#574d68;
}
.calendar-title{
  text-align:center; font-weight:800; color:#574d68; margin:16px 0 8px;
}
.title-badge{
  display:inline-block; margin-left:.5rem; padding:.2rem .5rem; border-radius:999px;
  background:var(--cute-pink); border:1px dashed var(--cute-accent); font-size:.9rem; color:#574d68;
}

/* FC 버튼 */
:deep(.fc .fc-toolbar){ gap:.5rem; }
:deep(.fc .fc-button){
  background:#ffffff; color:#574d68; border:1px solid #ffedd3; border-radius:12px; padding:.4rem .65rem; font-weight:700;
}
:deep(.fc .fc-button:hover){ background:#fffaf5; border-color:#ffe9b3; }
:deep(.fc .fc-button-primary:disabled){ opacity:.6; }

/* Day 시간열/현재시간 표시 */
:deep(.fc-timegrid-axis-cushion){ color:#7b718f; font-weight:700; }
:deep(.fc .fc-timegrid-now-indicator-arrow){ border-top-color:var(--cute-accent); }
:deep(.fc .fc-timegrid-now-indicator-line){ border-color:var(--cute-accent); }

/* 이벤트 모양 */
:deep(.fc .fc-timegrid-event),
:deep(.fc .fc-daygrid-event){
  border-radius:12px; border:1px solid rgba(0,0,0,.05);
  box-shadow:0 2px 8px rgba(255,138,179,.15);
  padding:2px 6px; font-weight:700;
}
:deep(.fc .fc-event-time){ font-weight:800; }
:deep(.fc .fc-event){ cursor: pointer; }

/* 요약/아젠다 */
.summary, .agenda{ width: var(--cal-fixed-width, 1280px); max-width:100%; margin:16px auto 0; }
.summary__title{ margin:12px 0 10px; font-size:16px; font-weight:800; color:#574d68; }
.summary__list{ display:grid; gap:8px; padding:0; margin:0; list-style:none; }
.summary__item{
  display:grid; grid-template-columns:10px 110px 140px 1fr auto; gap:10px; align-items:center;
  background:#fff; border:1px solid #ffe4ee; border-radius:14px; padding:8px 10px;
}
.summary__dot{ width:8px; height:8px; border-radius:3px; background:var(--dot-color,#ff8ab3); }
.summary__date,.summary__when{ font-size:13px; color:#7b718f; }
.summary__text{ font-size:14px; color:#2f2a3b; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.summary__actions .btn--tiny{ border-color:#ffd3e1; }
.summary__actions .btn--tiny:hover{ background:#fff5f9; }
.btn--danger{ color:#dc2626; border-color:#fecaca; }

.agenda__title{ display:flex; align-items:center; gap:8px; color:#574d68; font-weight:800; }
.agenda__body{ background:#fff; border:1px solid #ffe4ee; border-radius:14px; padding:12px; }
.agenda__item{ display:grid; grid-template-columns:110px 140px 1fr; gap:10px; padding:8px 10px; border:1px dashed #ffd3e1; border-radius:12px; }

/* 캘린더와 요약/상세일정 간격 확보 */
.calendar-wrap { margin-bottom: 60px; }
.summary, .agenda {
  clear: both;
  position: relative;
  z-index: 10;
  margin-top: 0;
  background: #fff;
  padding: 8px 0;
}

/* === timeGrid 세로선 끊김 고정 패치 === */
.calendar-wrap{ background:#fff; overflow:hidden; }
:deep(.fc),
:deep(.fc .fc-scrollgrid),
:deep(.fc .fc-scroller),
:deep(.fc .fc-scroller-harness),
:deep(.fc .fc-scroller-liquid-absolute),
:deep(.fc .fc-timegrid),
:deep(.fc .fc-timegrid-body),
:deep(.fc .fc-timegrid-slots){ background:#fff !important; }
:deep(.fc-theme-standard .fc-scrollgrid){ border:1px solid #e9e0da; }
:deep(.fc-theme-standard td),
:deep(.fc-theme-standard th){ border:1px solid #e9e0da; }
:deep(.fc-timegrid .fc-timegrid-col){ border-left:1px solid #e9e0da !important; }
:deep(.fc-timegrid .fc-timegrid-axis){ border-right:1px solid #e9e0da !important; }
:deep(.fc-timegrid .fc-timegrid-slot){ border-bottom:1px solid #e9e0da !important; }
:deep(.fc-timegrid .fc-timegrid-slots table){ border-collapse: separate !important; border-spacing: 0 !important; }

/* 모달 오버레이 */
.modal{
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.modal__dialog{ background:#fff; border:2px solid #ffe4ee; border-radius:16px; box-shadow:0 10px 30px rgba(0,0,0,.08); max-width:520px; width:min(92vw,520px); }
.modal__header{ padding:12px 16px; border-bottom:1px dashed #ffd3e1; display:flex; justify-content:space-between; align-items:center; }
.modal__close{ background:#fff; border:1px solid #ffd3e1; border-radius:8px; width:28px; height:28px; }
.modal__body{ padding:12px 16px; }
.form-row{ display:grid; gap:6px; margin-bottom:10px; }
.form-row input[type="text"], .form-row input[type="datetime-local"], .form-row input[type="color"]{
  border:1px solid #ffd3e1; border-radius:10px; padding:8px;
}
.modal__footer{ display:flex; justify-content:space-between; gap:12px; padding:12px 16px; border-top:1px dashed #ffd3e1; }
</style>
