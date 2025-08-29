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

/* ===== FullCalendar v6 ===== */
import { Calendar } from '@fullcalendar/core'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import listPlugin from '@fullcalendar/list'
import bootstrap5Plugin from '@fullcalendar/bootstrap5'

/* ===== 상태 ===== */
const calendarEl = ref(null)
let calendar

const isModalOpen = ref(false)
const modalTitle = ref('일정')
const agendaHtml = ref('')

/* 중앙 타이틀 날짜(달 변경 시 자동 갱신) */
const pageDate = ref(new Date())

/* ===== 모달 ===== */
function openModal(mode) {
  isModalOpen.value = true
  modalTitle.value = mode === 'create' ? '일정 등록' : '일정 수정'
}

/* ===== Agenda (간단 표시만 유지) ===== */
function renderAgenda() {
  const container = document.getElementById('agenda-body')
  if (!container) return
  agendaHtml.value = `<div class="agenda__empty">표시할 일정이 없습니다.</div>`
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
      center: '', // 기본 타이틀 숨김 (우리가 직접 타이틀 렌더링)
      right: 'dayGridMonth,dayGridWeek,dayGridDay,listMonth'
    },
    dayCellContent: (arg) => ({ html: arg.dayNumberText.replace('일', '') }),

    // 일정 등록 기능 비활성화
    selectable: false,
    selectMirror: false,
    unselectAuto: false,

    // 기존 이벤트 클릭만 가능 (편집 모드 열기)
    eventClick: () => openModal('edit'),

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