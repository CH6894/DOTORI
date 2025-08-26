<template>
  <div class="page">
    <!-- 화면 중앙 고정 타이틀 -->
    <h2 class="calendar-title">{{ pageDate.getFullYear() }}년 {{ pageDate.getMonth() + 1 }}월</h2>

    <div class="calendar-wrap">
      <div ref="calendarEl" id="calendar"></div>
    </div>

    <!-- 상세일정(Agenda) -->
    <section class="agenda">
      <h3 class="agenda__title">상세 일정</h3>
      <div class="agenda__body" id="agenda-body" v-html="agendaHtml"></div>
    </section>

    <!-- 일정 등록/수정 모달 (조건부 렌더링) -->
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
              <label><input v-model="form.allDay" type="checkbox" /> 종일</label>
            </div>

            <div class="form-row color-row">
              <label for="event-color">색상</label>
              <input id="event-color" v-model="form.color" type="color" />
            </div>
          </form>
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

/* ===== FullCalendar v6 ===== */
import { Calendar } from '@fullcalendar/core'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import listPlugin from '@fullcalendar/list'
import bootstrap5Plugin from '@fullcalendar/bootstrap5'

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

/* ===== 상태 ===== */
const calendarEl = ref(null)
let calendar

const isModalOpen = ref(false)
const modalTitle = ref('일정')
const agendaHtml = ref('')
const pageDate = ref(new Date())

/* ✅ reactive: 객체 자체를 교체하지 말고 속성만 갱신 */
const form = reactive({
  id: '',
  title: '',
  start: '',
  end: '',
  allDay: false,
  color: '#7c3aed'
})

/* ===== 모달 ===== */
function openModal(mode, ev) {
  isModalOpen.value = true
  if (mode === 'create') {
    modalTitle.value = '일정 등록'
    const now = new Date()
    Object.assign(form, {
      id: '',
      title: '',
      start: toLocalInputValue(now),
      end: toLocalInputValue(new Date(now.getTime() + 60 * 60 * 1000)),
      allDay: false,
      color: '#7c3aed'
    })
  } else {
    modalTitle.value = '일정 수정'
    const isAll = ev?.allDay === true
    const startVal = ev?.start ? (isAll ? toLocalInputValue(startOfDay(ev.start)) : toLocalInputValue(ev.start)) : ''
    let endVal = ''
    if (ev?.end) {
      endVal = isAll
        ? toLocalInputValue(endOfDayInclusive(addDays(ev.end, -1)))
        : toLocalInputValue(ev.end)
    }
    Object.assign(form, {
      id: ev?.id ?? '',
      title: ev?.title ?? '',
      start: startVal,
      end: endVal,
      allDay: !!isAll,
      color: ev?.extendedProps?.color || ev?.backgroundColor || '#7c3aed'
    })
  }
}
function closeModal() { isModalOpen.value = false }

/* ===== 저장/삭제 ===== */
function onSave() {
  if (!form.title) { alert('제목을 입력하세요.'); return }

  let s = fromLocalInputValue(form.start)
  let e = form.end ? fromLocalInputValue(form.end) : null
  const isAll = !!form.allDay

  if (isAll) {
    const sDay = startOfDay(s)
    const eDay = e ? startOfDay(e) : sDay
    const startStr = fmtYmd(sDay)
    const endStr = fmtYmd(addDays(eDay, 1))  // exclusive

    if (!form.id) {
      calendar.addEvent({
        id: String(Date.now()),
        title: form.title,
        start: startStr,
        end: endStr,
        allDay: true,
        backgroundColor: form.color,
        borderColor: form.color,
        extendedProps: { color: form.color }
      })
    } else {
      const ev = calendar.getEventById(form.id)
      if (ev) {
        ev.setProp('title', form.title)
        ev.setDates(startStr, endStr, { allDay: true })
        ev.setProp('backgroundColor', form.color)
        ev.setProp('borderColor', form.color)
        ev.setExtendedProp('color', form.color)
      }
    }
  } else {
    if (!e || e.getTime() === s.getTime()) e = new Date(s.getTime() + 60 * 60 * 1000)
    const isMidnight = e.getHours() === 0 && e.getMinutes() === 0 && e.getSeconds() === 0 && e.getMilliseconds() === 0
    const diffDate = e.getFullYear() !== s.getFullYear() || e.getMonth() !== s.getMonth() || e.getDate() !== s.getDate()
    if (isMidnight && diffDate) e = endOfDayInclusive(e)

    if (!form.id) {
      calendar.addEvent({
        id: String(Date.now()),
        title: form.title,
        start: s,
        end: e,
        allDay: false,
        backgroundColor: form.color,
        borderColor: form.color,
        extendedProps: { color: form.color }
      })
    } else {
      const ev = calendar.getEventById(form.id)
      if (ev) {
        ev.setProp('title', form.title)
        ev.setDates(s, e, { allDay: false })
        ev.setProp('backgroundColor', form.color)
        ev.setProp('borderColor', form.color)
        ev.setExtendedProp('color', form.color)
      }
    }
  }

  closeModal()
  renderAgenda()
}
function onDelete() {
  if (form.id) {
    const ev = calendar.getEventById(form.id)
    if (ev) ev.remove()
  }
  closeModal()
  renderAgenda()
}

/* ===== Agenda 생성 ===== */
function splitByDate(start, end, allDay) {
  const days = []
  const s = new Date(start)
  let e = end ? new Date(end) : new Date(start)
  if (allDay && end) e = addDays(e, -1)
  let cur = new Date(s.getFullYear(), s.getMonth(), s.getDate())
  const last = new Date(e.getFullYear(), e.getMonth(), e.getDate())
  while (cur <= last) {
    days.push(fmtYmd(cur))
    cur = new Date(cur.getFullYear(), cur.getMonth(), cur.getDate() + 1)
  }
  return days
}
function renderAgenda() {
  const container = document.getElementById('agenda-body')
  if (!container) return
  const view = calendar.view
  const rs = view.currentStart
  const re = view.currentEnd
  const events = calendar.getEvents().filter(ev => {
    const s = ev.start
    const e = ev.end || ev.start
    return (e > rs && s < re)
  })

  const map = new Map()
  for (const ev of events) {
    for (const day of splitByDate(ev.start, ev.end, ev.allDay)) {
      if (!map.has(day)) map.set(day, [])
      map.get(day).push(ev)
    }
  }

  const keys = Array.from(map.keys()).sort()
  for (const k of keys) {
    map.get(k).sort((a, b) => {
      if (a.allDay !== b.allDay) return a.allDay ? -1 : 1
      return (a.start?.getTime() || 0) - (b.start?.getTime() || 0)
    })
  }

  if (!keys.length) {
    agendaHtml.value = `<div class="agenda__empty">표시할 일정이 없습니다.</div>`
    return
  }

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

/* ===== 캘린더 초기화 ===== */
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
      form.end = toLocalInputValue(d1)
    },

    select: (arg) => {
      openModal('create')
      const isAll = arg.allDay === true
      form.allDay = isAll
      form.start = toLocalInputValue(arg.start)

      if (arg.end) {
        if (isAll) {
          const lastDayInclusive = endOfDayInclusive(addDays(arg.end, -1))
          form.end = toLocalInputValue(lastDayInclusive)
        } else {
          let fixedEnd = arg.end
          if (fixedEnd.getTime() === arg.start.getTime()) {
            fixedEnd = new Date(arg.start.getTime() + 60 * 60 * 1000) // 1시간 보정
          }
          const isMidnight =
            fixedEnd.getHours() === 0 && fixedEnd.getMinutes() === 0 &&
            fixedEnd.getSeconds() === 0 && fixedEnd.getMilliseconds() === 0
          const diffDate =
            fixedEnd.getFullYear() !== arg.start.getFullYear() ||
            fixedEnd.getMonth() !== arg.start.getMonth() ||
            fixedEnd.getDate() !== arg.start.getDate()
          if (isMidnight && diffDate) fixedEnd = endOfDayInclusive(fixedEnd)
          form.end = toLocalInputValue(fixedEnd)
        }
      } else {
        form.end = isAll ? toLocalInputValue(endOfDayInclusive(arg.start)) : ''
      }
      calendar.unselect()
    },

    eventClick: (arg) => openModal('edit', arg.event),

    datesSet: () => {
      renderAgenda()
      pageDate.value = calendar.getDate()
    },
    eventAdd: renderAgenda,
    eventChange: renderAgenda,
    eventRemove: renderAgenda
  })

  calendar.render()
  pageDate.value = calendar.getDate()
  setTimeout(() => calendar.updateSize(), 0)
})

onBeforeUnmount(() => { if (calendar) calendar.destroy() })
</script>
<style src="../assets/calendar.css"></style>
