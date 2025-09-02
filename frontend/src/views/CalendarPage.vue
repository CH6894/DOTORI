<template>
  <div class="page calendar-scope">
    <h2 class="calendar-title">{{ pageDate.getFullYear() }}년 {{ pageDate.getMonth() + 1 }}월</h2>
    <div class="calendar-wrap">
      <div id="calendar" ref="calendarEl"></div>
    </div>
    <section class="agenda">
      <h3 class="agenda__title">상세일정</h3>
      <div class="agenda__body" id="agenda-body" v-html="agendaHtml"></div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import axios from 'axios'

/* ===== FullCalendar ===== */
import { Calendar } from '@fullcalendar/core'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import listPlugin from '@fullcalendar/list'
import bootstrap5Plugin from '@fullcalendar/bootstrap5'

/* ===== API (상대경로) =====
   vite.config.js 예시:
   export default {
     server: {
       port: 5173,
       proxy: { '/api': { target: 'http://localhost:8081', changeOrigin: true } }
     }
   }
*/
const PUB_URL = '/api/public/calendars'

/* ===== 상태 ===== */
const calendarEl = ref(null)
let calendar
const agendaHtml = ref('')
const pageDate = ref(new Date())

/* ===== 유틸 ===== */
const pad = (n) => String(n).padStart(2, '0')
const isoLocal = (d) => {
  const x = new Date(d)
  return `${x.getFullYear()}-${pad(x.getMonth()+1)}-${pad(x.getDate())}T${pad(x.getHours())}:${pad(x.getMinutes())}:${pad(x.getSeconds())}`
}
const escapeHtml = (s) => String(s).replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]))
const fmtYmd = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
const timeRange = (s, e) => {
  const f = (x) => `${pad(x.getHours())}:${pad(x.getMinutes())}`
  if (!e || e.getTime() === s.getTime()) return f(s)
  return `${f(s)} ~ ${f(e)}`
}
// 에러 메시지 가독성
const formatAxiosError = (e) => (
  e?.response
    ? `HTTP ${e.response.status} ${e.response.statusText}\n${JSON.stringify(e.response.data)}`
    : (e?.request ? '응답 없음(네트워크/CORS 가능성)' : `설정 오류: ${e?.message}`)
)

/* ===== 서버에서 현재 뷰 범위 로드 ===== */
async function refetchFromServer() {
  const view = calendar.view
  const start = isoLocal(view.currentStart).slice(0,19) // "YYYY-MM-DDTHH:mm:ss"
  const end   = isoLocal(view.currentEnd).slice(0,19)

  try {
    const { data } = await axios.get(PUB_URL, { params: { start, end } })
    const events = (data || []).map(ev => ({
      id: String(ev.id),
      title: ev.title ?? ev.scheduleName ?? '(제목 없음)',
      start: ev.start ?? ev.scheduleDate,   // 백엔드 필드 호환
      end: ev.end ?? null,
      allDay: false,
      backgroundColor: '#7c3aed',
      borderColor: '#7c3aed',
      extendedProps: { color: '#7c3aed', info: ev.info ?? ev.scheduleInfo ?? '' }
    }))
    calendar.removeAllEvents()
    events.forEach(e => calendar.addEvent(e))
    renderAgenda()
  } catch (e) {
    console.error(e)
    alert('일정 불러오기 실패: ' + formatAxiosError(e))
    calendar.removeAllEvents()
    agendaHtml.value = `<div class="agenda__empty">일정 로드 실패</div>`
  }
}

/* ===== 간단 Agenda 표시 ===== */
function renderAgenda() {
  const container = document.getElementById('agenda-body')
  if (!container) return

  const rs = calendar.view.currentStart
  const re = calendar.view.currentEnd
  const events = calendar.getEvents().filter(ev => (ev.end || ev.start) > rs && ev.start < re)
  if (!events.length) {
    agendaHtml.value = `<div class="agenda__empty">표시할 일정이 없습니다.</div>`
    return
  }

  const byDay = {}
  for (const ev of events) {
    const key = fmtYmd(ev.start)
    byDay[key] ||= []
    byDay[key].push(ev)
  }

  const html = Object.keys(byDay).sort().map(k => {
    const dateObj = new Date(k + 'T00:00:00')
    const dateLabel = `${dateObj.getFullYear()}년 ${dateObj.getMonth()+1}월 ${dateObj.getDate()}일`
    const items = byDay[k]
      .sort((a,b)=>(a.start - b.start))
      .map(ev => {
        const color = ev.extendedProps?.color || ev.backgroundColor || '#7c3aed'
        const time = ev.allDay ? '종일' : timeRange(ev.start, ev.end || ev.start)
        const title = escapeHtml(ev.title || '(제목 없음)')
        return `<li class="agenda__item" style="--dot-color:${color}">
                  <span class="agenda__time">${time}</span>
                  <span class="agenda__title">${title}</span>
                </li>`
      }).join('')
    return `<section class="agenda__section">
              <h4 class="agenda__date">${dateLabel}</h4>
              <ul class="agenda__list">${items}</ul>
            </section>`
  }).join('')

  agendaHtml.value = html
}

onMounted(async () => {
  calendar = new Calendar(calendarEl.value, {
    themeSystem: 'bootstrap5',
    locale: 'ko',
    timeZone: 'Asia/Seoul',
    plugins: [dayGridPlugin, interactionPlugin, listPlugin, bootstrap5Plugin],
    initialView: 'dayGridMonth',
    headerToolbar: {
      left: 'prev today next',
      center: '',
      right: 'dayGridMonth,dayGridWeek,dayGridDay,listMonth'
    },
    dayCellContent: (arg) => ({ html: arg.dayNumberText.replace('일', '') }),
    selectable: false,
    selectMirror: false,
    unselectAuto: false,
    eventClick: () => {},

    datesSet: async () => {
      pageDate.value = calendar.getDate()
      await refetchFromServer()      // ✅ 공개는 읽기만
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
/* ===== Agenda ===== */
.agenda {
  width: var(--cal-fixed-width);
  max-width: 100%;
  margin: 10px auto 0;
}
.agenda__title {
  margin: 16px 0 10px;
  font-size: 16px;
  font-weight: 700;
  color: #111;
}
.agenda__body {
  background: #fff;
  border: 1px solid #e9e9e9;
  border-radius: 8px;
  padding: 12px;
}
.agenda__empty {
  padding: 14px;
  color: #666;
  font-size: 14px;
  text-align: center;
}
</style>
