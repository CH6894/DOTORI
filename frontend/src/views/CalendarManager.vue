<!-- CalendarPage.vue (CalendarPage 스타일 통일 최종본) -->
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

    <!-- 상세일정(종일/시간 분리, 읽기 전용) -->
    <section class="agenda" v-show="showAgenda" ref="agendaSection" id="agenda-top">
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

            <!-- 카테고리(7색 팔레트) -->
            <div class="form-row">
              <label>카테고리(색)</label>
              <div class="cat-grid">
                <button
                  v-for="c in CATEGORY_PALETTE"
                  :key="c.key"
                  type="button"
                  class="cat-chip"
                  :class="{ active: form.category === c.key }"
                  :style="{ '--chip-color': c.color }"
                  @click="selectCategory(c.key)"
                >
                  <span class="chip-dot"></span>
                  {{ c.label }}
                </button>
              </div>
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
import { ref, reactive, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import axios from 'axios'

import { Calendar } from '@fullcalendar/core'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import listPlugin from '@fullcalendar/list'
import bootstrap5Plugin from '@fullcalendar/bootstrap5'

/* API */
const PUB_URL = '/api/public/calendars'
const ADM_URL = '/api/admin/calendars'

/* 카테고리 팔레트 */
const CATEGORY_PALETTE = [
  { key:'anime',   label:'Anime',  color:'#FF4C4C' },
  { key:'webtoon', label:'Webtoon',color:'#4CAF50' },
  { key:'game',    label:'Game',   color:'#FFD93B' },
  { key:'sports',  label:'Sports', color:'#4285F4' },
  { key:'creator', label:'Creator',color:'#FF6FB5' },
  { key:'kpop',    label:'Kpop',   color:'#9B59B6' },
  { key:'etc',     label:'기타',    color:'#FF9800' },
]
const mapKeyToColor = (k) => CATEGORY_PALETTE.find(x=>x.key===k)?.color || CATEGORY_PALETTE.at(-1).color
const inferKeyByColor = (color) => {
  const hit = CATEGORY_PALETTE.find(x => x.color.toLowerCase() === String(color||'').toLowerCase())
  return hit?.key || 'etc'
}

/* 유틸 */
const pad=(n)=>String(n).padStart(2,'0')
const ymd=(d)=>`${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
const toLocalInputValue=(dLike)=>{const d=new Date(dLike);return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`}
const fromLocalInputValue=(v)=>v?new Date(v):null
const fmtYmd=(d)=>`${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
const startOfDay=(d)=>new Date(d.getFullYear(),d.getMonth(),d.getDate())
const endOfDayInclusive=(d)=>new Date(d.getFullYear(),d.getMonth(),d.getDate(),23,59,59,999)
const addDays=(d,n)=>new Date(d.getFullYear(),d.getMonth(),d.getDate()+n)
const isoLocal=(d)=>{const x=new Date(d);return `${x.getFullYear()}-${pad(x.getMonth()+1)}-${pad(x.getDate())}T${pad(x.getHours())}:${pad(x.getMinutes())}:${pad(x.getSeconds())}`}
const hhmm=(d)=>`${pad(d.getHours())}:${pad(d.getMinutes())}`
const timeRange=(s,e)=>(!e||e.getTime()===s.getTime())?hhmm(s):`${hhmm(s)} ~ ${hhmm(e)}`

/* 요약 라벨 */
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

/* datetime-local 보조 */
const toLocalDateInput00=(d)=>`${ymd(new Date(d))}T00:00`
const toLocalDateInput2359=(d)=>`${ymd(new Date(d))}T23:59`
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
const pageDate=ref(new Date()); const isMonthView=ref(false)
const summary=ref([])
const ADMIN_USER_ID=1
const form=reactive({ id:'', title:'', start:'', end:'', allDay:false, category:'etc' })
const isModalOpen=ref(false); const modalTitle=ref('일정')

/* 상세일정 상태 */
const showAgenda=ref(false); const selectedDate=ref(null); const agendaHtml=ref(''); const pendingSelect=ref(null)

/* 스무스 스크롤 */
function scrollToAgenda(offsetY = 72){
  nextTick(()=>{
    const el = document.getElementById('agenda-top')
    if(!el) return
    const y = el.getBoundingClientRect().top + window.pageYOffset - offsetY
    window.scrollTo({ top: y, behavior: 'smooth' })
  })
}

/* 종일 토글 시 입력값 00~23:59 보정 */
watch(()=>form.allDay,(v)=>{
  if(v){
    const s=fromLocalInputValue(form.start)||new Date()
    const e=form.end?fromLocalInputValue(form.end):s
    form.start=toLocalDateInput00(s); form.end=toLocalDateInput2359(e)
  }
})

/* 카테고리 선택 */
function selectCategory(key){ form.category = key }

/* 폼 헬퍼 */
function resetForm(){ form.id=''; form.title=''; form.start=''; form.end=''; form.allDay=false; form.category='etc' }
function fillFormFromEvent(ev){
  const meta = ev.extendedProps || {}
  const allDay = !!ev.allDay
  form.id   = String(ev.id)
  form.title= ev.title || ''
  form.allDay = allDay
  const color = meta.color || ev.backgroundColor
  form.category = meta.category || inferKeyByColor(color)
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
  if(mode==='create'){ modalTitle.value='일정 등록'; resetForm() }
  else { modalTitle.value='일정 수정'; if(evObj) fillFormFromEvent(evObj) }
}
function closeModal(){ isModalOpen.value=false; pendingSelect.value=null }

/* 서버 CRUD */
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

function patchCalendarEventLocal(id,{ title,color,category,allDay,start,endExclusive }){
  const ev=calendar.getEventById(id); if(!ev) return
  ev.setProp('title',title)
  ev.setProp('backgroundColor',color)
  ev.setProp('borderColor',color)
  ev.setExtendedProp('color',color)
  ev.setExtendedProp('category',category)
  ev.setAllDay(!!allDay)
  if(start) ev.setStart(start); if(endExclusive) ev.setEnd(endExclusive)
}

/* 저장/삭제 */
async function onSave(){
  try{
    if(!form.title){ alert('제목을 입력하세요.'); return }
    const color = mapKeyToColor(form.category)
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
    const info=packInfo({ color, category:form.category, allDay:!!form.allDay, endInclusive:endInclusive?isoLocal(endInclusive).slice(0,19):null })

    if(form.id){
      try{
        await updateEventToServer(form.id,{ title:form.title,start:startIso,info })
        const endExclusive=form.allDay?addDays(startOfDay(endInclusive),1):endInclusive
        patchCalendarEventLocal(form.id,{ title:form.title,color,category:form.category,allDay:!!form.allDay,start:new Date(startIso),endExclusive })
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

/* 요약/아젠다 */
function refreshSummary(){
  if(!calendar) return
  const view=calendar.view
  const rs=view.currentStart, re=view.currentEnd
  const list=calendar.getEvents()
    .filter(ev => (ev.end||ev.start)>rs && ev.start<re)
    .map(ev=>{
      const color=ev.extendedProps?.color||ev.backgroundColor||mapKeyToColor('etc')
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
    const title = String(ev.title||'(제목 없음)').replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]))
    return `<li class="agenda__item" style="--dot-color:${color}">
              <span class="agenda__time">${time}</span>
              <span class="agenda__title">${title}</span>
            </li>`
  }).join('')

  const liAllDay = allDayItems.map(ev=>{
    const color = ev.extendedProps?.color || ev.backgroundColor || '#ff8ab3'
    const title = String(ev.title||'(제목 없음)').replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]))
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

/* 캘린더 초기화 */
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
      right:'dayGridMonth,listMonth,addEventButton'
    },
    customButtons:{ addEventButton:{ text:' 일정 등록', click:()=>openModal('create') } },

    height:'auto',
    expandRows:true,

    slotMinTime:'00:00:00',
    slotMaxTime:'24:00:00',
    slotDuration:'00:30:00',
    nowIndicator:false,         // 현재시간 빨간선 제거
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

    /* 드래그/리사이즈 */
    editable:true, eventStartEditable:true, eventDurationEditable:true,

    /* 이벤트 로드 */
    events: async (info, success, failure)=>{
      try{
        const s = new Date(info.start)
        const eInc = new Date(info.end.getTime()-1)
        const start = isoLocal(new Date(s.getFullYear(),s.getMonth(),s.getDate(),0,0,0)).slice(0,19)
        const end   = isoLocal(new Date(eInc.getFullYear(),eInc.getMonth(),eInc.getDate(),23,59,59)).slice(0,19)

        const { data }=await axios.get(PUB_URL,{ params:{ start, end } })
        const events=(data||[]).map(ev=>{
          const meta=unpackInfo(ev.scheduleInfo||ev.info)
          const color = meta.color || mapKeyToColor(meta.category||'etc')
          const category = meta.category || inferKeyByColor(color)
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
            extendedProps:{ color, category }
          }
        })
        success(events)
      }catch(e){ failure(e) }
    },

    /* 렌더 후 요약/아젠다 동기화 */
    eventsSet: ()=>{
      refreshSummary()
      if(selectedDate.value && hasEventsOn(selectedDate.value)){ showAgenda.value=true; renderAgenda() }
      else{ showAgenda.value=false; agendaHtml.value='' }
    },

    /* 클릭/선택 등록 */
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
      form.category = 'etc'
      scrollToAgenda()
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
      form.category = 'etc'
      calendar.unselect()
      scrollToAgenda()
    },

    /* 편집 */
    eventClick: (arg) => {
      try { openModal('edit', arg.event) } catch {}
      const d = new Date(arg.event.start)
      selectedDate.value = d
      showAgenda.value = true
      renderAgenda()
      if (arg.jsEvent) arg.jsEvent.preventDefault()
      scrollToAgenda()
    },

    /* 이동/리사이즈 저장 */
    eventDrop: async (info) => {
      try{
        const ev = info.event
        const allDay = !!ev.allDay
        const startIso = isoLocal(ev.start).slice(0,19)
        const e = ev.end || ev.start
        const endInclusive = allDay
          ? isoLocal(new Date(e.getTime() - 1)).slice(0,19)
          : isoLocal(e).slice(0,19)
        const color = ev.extendedProps?.color || mapKeyToColor('etc')
        const category = ev.extendedProps?.category || inferKeyByColor(color)
        const infoJson = packInfo({ color, category, allDay, endInclusive })
        await updateEventToServer(ev.id, { title: ev.title, start: startIso, info: infoJson })
        refreshSummary(); renderAgenda()
      }catch(e){
        info.revert(); alert('이동 저장 실패이다.')
      }
    },
    eventResize: async (info) => {
      try{
        const ev = info.event
        const allDay = !!ev.allDay
        const startIso = isoLocal(ev.start).slice(0,19)
        const e = ev.end || ev.start
        const endInclusive = allDay
          ? isoLocal(new Date(e.getTime() - 1)).slice(0,19)
          : isoLocal(e).slice(0,19)
        const color = ev.extendedProps?.color || mapKeyToColor('etc')
        const category = ev.extendedProps?.category || inferKeyByColor(color)
        const infoJson = packInfo({ color, category, allDay, endInclusive })
        await updateEventToServer(ev.id, { title: ev.title, start: startIso, info: infoJson })
        refreshSummary(); renderAgenda()
      }catch(e){
        info.revert(); alert('리사이즈 저장 실패이다.')
      }
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
</script>

<style src="../assets/calendar.css"></style>
<style scoped>
/* ===== 래퍼 ===== */
.calendar-wrap{
  position:relative; z-index:1;
  width: var(--cal-fixed-width, 1280px);
  max-width:100%;
  height:auto;
  margin:0 auto 48px; /* 캘린더-요약/상세 간 여백 */
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

/* ===== 귀여운 테마 ===== */
.cute :deep(.fc){ --cute-pink:#ffeed8; --cute-accent:#ffd08a; --cute-ink:#574d68; }
.calendar-title{ text-align:center; font-weight:800; color:#574d68; margin:16px 0 8px; }
.title-badge{ display:inline-block; margin-left:.5rem; padding:.2rem .5rem; border-radius:999px; background:var(--cute-pink); border:1px dashed var(--cute-accent); font-size:.9rem; color:#574d68; }

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

/* ===== 요약/아젠다 ===== */
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

/* === timeGrid week/day: 가로선/라벨/스크롤 제거 + 세로선 유지 === */
:deep(.fc-timeGridWeek-view .fc-timegrid-axis),
:deep(.fc-timeGridDay-view  .fc-timegrid-axis),
:deep(.fc-timeGridWeek-view .fc-timegrid-slot-label),
:deep(.fc-timeGridDay-view  .fc-timegrid-slot-label){ display:none !important; }

:deep(.fc-timeGridWeek-view .fc-timegrid-slot),
:deep(.fc-timeGridDay-view  .fc-timegrid-slot){ border-bottom:0 !important; }

:deep(.fc-timeGridWeek-view .fc-scroller),
:deep(.fc-timeGridDay-view  .fc-scroller){ overflow:hidden !important; }

:deep(.fc-timeGridWeek-view .fc-timegrid-body),
:deep(.fc-timeGridDay-view  .fc-timegrid-body){ height:auto !important; }

/* 세로선 유지(테마 기본 보더 사용) */
:deep(.fc-theme-standard .fc-scrollgrid){ border:1px solid #e9e0da; }
:deep(.fc-theme-standard td),
:deep(.fc-theme-standard th){ border:1px solid #e9e0da; }

/* 현재시간 표시 제거 */
:deep(.fc-timegrid-now-indicator-line),
:deep(.fc-timegrid-now-indicator-arrow){ display:none !important; }

/* 모달 & 카테고리 칩 */
.modal{ position: fixed; inset: 0; background: rgba(0,0,0,.35); display: flex; align-items: center; justify-content: center; z-index: 9999; }
.modal__dialog{ background:#fff; border:2px solid #ffe4ee; border-radius:16px; box-shadow:0 10px 30px rgba(0,0,0,.08); max-width:520px; width:min(92vw,520px); }
.modal__header{ padding:12px 16px; border-bottom:1px dashed #ffd3e1; display:flex; justify-content:space-between; align-items:center; }
.modal__close{ background:#fff; border:1px solid #ffd3e1; border-radius:8px; width:28px; height:28px; }
.modal__body{ padding:12px 16px; }
.form-row{ display:grid; gap:6px; margin-bottom:10px; }
.form-row input[type="text"], .form-row input[type="datetime-local"]{ border:1px solid #ffd3e1; border-radius:10px; padding:8px; }
.modal__footer{ display:flex; justify-content:space-between; gap:12px; padding:12px 16px; border-top:1px dashed #ffd3e1; }

.cat-grid{
  display:grid; grid-template-columns: repeat(3, minmax(0,1fr)); gap:8px;
}
.cat-chip{
  display:flex; align-items:center; gap:8px; padding:8px 10px; border-radius:12px; border:1px solid #e7e7e7; background:#fff; font-weight:700;
}
.cat-chip .chip-dot{
  width:14px; height:14px; border-radius:6px; background:var(--chip-color);
  box-shadow:0 0 0 2px rgba(0,0,0,0.05) inset;
}
.cat-chip.active{
  border-color: var(--chip-color);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--chip-color) 25%, transparent);
}
</style>
