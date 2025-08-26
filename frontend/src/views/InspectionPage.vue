<template>
  <div class="cream">
    <!-- 헤더 -->
    <header class="c-head">
      <div class="c-container">
        <h1 class="c-title">검수 기준</h1>
        <p class="c-sub">구분별 세부 기준 · 등급/할인율 · 패키지/구성품 판정 · 유의사항</p>
      </div>
    </header>

    <div class="c-container c-body">
      <!-- 상단 개요 표 -->
      <section class="c-section">
        <div class="c-table-wrap">
          <table class="c-outline">
            <thead>
              <tr>
                <th style="width:160px">선별</th>
                <th>이슈와 · 상태 · etc</th>
                <th>구성 · 사이즈 · 색감 · 패브릭 검사</th>
                <th>포장/패키지 사례</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <th>체크</th>
                <td>불량 · 형태변형 · 얼룩/스크래치 · 사용감</td>
                <td>누락/불일치 · 규격 차이 · 색감/원단/프린트</td>
                <td>박스/블리스터 · 비닐/라벨 · 동봉물</td>
              </tr>
              <tr>
                <th>스티커/가이드</th>
                <td colspan="3">통과 상품에 인증 스티커(홀로그램/QR) 부착 · 발송 시 제거 금지</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- 공통 안내 -->
      <section class="c-section">
        <h2 class="c-h2">공통 기준</h2>
        <ul class="c-bullets">
          <li>점상 손상: <b>가장 긴 부분</b> 기준</li>
          <li>면상 손상: <b>최대 지름</b> 기준</li>
          <li><b>5mm 이내</b> 밀집은 <b>1개</b>로 계산</li>
          <li>최종 등급은 항목 중 <b>최저 평가</b>를 따름 · 안전/작동 불가 이슈는 즉시 <b>F</b></li>
          <li class="c-dim"><code>최종가 = 정가 × (100 − 할인율) ÷ 100</code> (예) 50,000원 · A(20%) → <b>40,000원</b></li>
        </ul>
      </section>

      <!-- 카테고리 탭 -->
      <section class="c-section">
        <div class="c-tabs" role="tablist" aria-label="inspection categories">
          <button
            v-for="t in tabs"
            :key="t.key"
            class="c-tab"
            :class="{ active: active === t.key }"
            role="tab"
            :aria-selected="active === t.key"
            @click="active = t.key"
          >
            {{ t.label }}
          </button>
        </div>

        <!-- 할인율/요약 -->
        <div class="c-panel">
          <div class="c-panel-hd">{{ current.label }} · 할인율</div>
          <div class="c-panel-bd">
            <p class="c-note">
              S {{ current.discount.s }}% · A {{ current.discount.a }}% ·
              B {{ current.discount.b }}% · C {{ current.discount.c }}%
            </p>
            <ul class="c-legend">
              <li><span class="dot s"></span><b>S</b> {{ current.brief.s }}</li>
              <li><span class="dot a"></span><b>A</b> {{ current.brief.a }}</li>
              <li><span class="dot b"></span><b>B</b> {{ current.brief.b }}</li>
              <li><span class="dot c"></span><b>C</b> {{ current.brief.c }}</li>
              <li><span class="dot f"></span><b>F</b> {{ current.brief.f }}</li>
            </ul>
          </div>
        </div>

        <!-- KREAM 스타일: 항목 × S/A/B/C/F 매트릭스 -->
        <div class="c-table-wrap">
          <table class="c-matrix">
            <thead>
              <tr>
                <th class="sticky-left" style="width:200px">항목</th>
                <th class="g g-s">S</th>
                <th class="g g-a">A</th>
                <th class="g g-b">B</th>
                <th class="g g-c">C</th>
                <th class="g g-f">F</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(r, i) in current.table" :key="i">
                <th class="sticky-left">{{ r.section }}</th>
                <td><span v-if="r.s">{{ r.s }}</span></td>
                <td><span v-if="r.a">{{ r.a }}</span></td>
                <td><span v-if="r.b">{{ r.b }}</span></td>
                <td><span v-if="r.c">{{ r.c }}</span></td>
                <td><span v-if="r.f" class="bad">{{ r.f }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- 패키지/구성품 표 -->
      <section class="c-section">
        <h2 class="c-h2">불량/패키지 판정 사항</h2>
        <div class="c-table-wrap">
          <table class="c-judge">
            <thead>
              <tr>
                <th style="width:200px">항목</th>
                <th class="g g-s">S</th>
                <th class="g g-a">A</th>
                <th class="g g-b">B</th>
                <th class="g g-c">C</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, i) in pkgTable" :key="i">
                <th>{{ row.item }}</th>
                <td class="center">{{ row.s }}</td>
                <td class="center">{{ row.a }}</td>
                <td class="center">{{ row.b }}</td>
                <td class="center">{{ row.c }}</td>
              </tr>
            </tbody>
          </table>
          <p class="c-hint">※ 원문 기준을 요약 반영. 안전/재생불가 사유는 별도 F 판정.</p>
        </div>
      </section>

      <!-- 유의사항/페널티 -->
      <section class="c-section">
        <h2 class="c-h2">검수 유의사항/페널티</h2>
        <div class="c-table-wrap">
          <table class="c-judge">
            <thead>
              <tr>
                <th style="width:180px">구분</th>
                <th style="width:140px">처리</th>
                <th style="width:90px">페널티</th>
                <th>비고</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(p, i) in penalties" :key="i">
                <td class="bold">{{ p.type }}</td>
                <td>{{ p.action }}</td>
                <td>{{ p.penalty }}</td>
                <td>{{ p.note }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <details class="c-details" open>
          <summary>검수 기준 적용 제외</summary>
          <ul class="c-bullets">
            <li><b>제조 공정</b> : 몰드 라인/미세 어긋남, 도금·연결부 편차, 프린팅 위치 1–2mm 차이</li>
            <li><b>유통/포장</b> : 박스 모서리 ≤5mm 눌림, 블리스터 주름, 포장재 칼자국</li>
            <li><b>검수/배송</b> : 완충재 손상, 택 고리 이탈, 스티커 흔적</li>
            <li><b>제품 특성</b> : 시리얼 차이, 시기별 색감/패키징, 인증스티커 위치/크기 차이</li>
          </ul>
        </details>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import '@/assets/inspection.css'
import { computed, reactive, ref } from 'vue'

type Row = { section:string; s?:string; a?:string; b?:string; c?:string; f?:string }
type Brief = { s:string;a:string;b:string;c:string;f:string }
type Discount = { s:number;a:number;b:number;c:number }
type Cat = { key:string; label:string; discount:Discount; brief:Brief; table:Row[] }

const tabs = [
  { key:'figure', label:'피규어' },
  { key:'keyring', label:'키링' },
  { key:'acc', label:'잡화' },
  { key:'stationery', label:'문구' },
  { key:'apparel', label:'의류' },
  { key:'living', label:'생활' },
] as const

/* === 카테고리 데이터 (F 조건 포함) === */
const DATA = reactive<Record<typeof tabs[number]['key'], Cat>>({
  figure: {
    key:'figure', label:'피규어',
    discount:{ s:5, a:20, b:40, c:65 },
    brief:{ s:'거의 신품급', a:'가벼운 사용감', b:'뚜렷한 사용감', c:'심한 사용감/기능 일부 제한', f:'사용 불가' },
    table:[
      { section:'도색', s:'크랙/벗겨짐/번짐 0 · 컬러 체인지 0',
        a:'크랙 ≤3mm 1–2 · 벗겨짐 ≤2mm 1–2 · 페이딩 경미',
        b:'크랙 ≤8mm 3–5 · 벗겨짐 ≤5mm 3–5 · 부분 페이딩',
        c:'크랙 ≤1.5cm 6–10 · 벗겨짐 ≤1cm 6–10 · 페이딩 뚜렷',
        f:'도색 디스토션 등 복구 불가' },
      { section:'헤드/페이스', s:'아이/립/헤어/데칼 완벽',
        a:'아이 95%↑ · 립 미세 번짐 1–2 · 데칼 들뜸 ≤2mm 1–2',
        b:'아이 80%↑ · 립 번짐 ≤5mm 2–3 · 데칼 들뜸 ≤5mm 3–5',
        c:'아이 60%↑ 인식 · 데칼 완전 들뜸 2–3',
        f:'헤드 크랙 등 주요 손상' },
      { section:'관절/가동', s:'헐거움 0 · 100% 가동',
        a:'헐거움 1–2부위 · 90%↑',
        b:'헐거움 3–5부위 · 70%↑',
        c:'조인트 크랙 1–2 · 50%↑',
        f:'조인트 브레이크(복구 불가)' },
      { section:'바디/의상', s:'화이트닝/풀림/부속 로스 0',
        a:'실밥 ≤3mm 1–2 · 옐로잉 경미',
        b:'옐로잉 ≤1cm 3–5 · 풀림 ≤8mm 2–3 · 소품 로스 1–2',
        c:'광범위 옐로잉 · 찢어짐 ≤5mm 1–2',
        f:'부속품 로스 ≥70% 또는 구조적 손상' }
    ]
  },
  keyring: {
    key:'keyring', label:'키링',
    discount:{ s:5, a:15, b:35, c:60 },
    brief:{ s:'신품급(프린팅/메탈)', a:'경미 벗겨짐/헤어라인', b:'페이딩/블리딩', c:'광범위 손상/러스트 가능', f:'프린팅 50%↑ 로스·체인/링 파손' },
    table:[
      { section:'프린팅/도색', s:'벗겨짐/크랙/블리딩 0 · 페이딩 0%',
        a:'벗겨짐 ≤2mm 1–2 · 페이딩 ≤5% · 엣지 크랙 ≤1mm 1–2',
        b:'벗겨짐 ≤5mm 3–5 · 페이딩 ≤20% · 블리딩 ≤3mm 2–3',
        c:'벗겨짐 ≤1cm 6–10 · 페이딩 ≤40% · 셰이프 로스',
        f:'프린팅 로스 ≥50%' },
      { section:'메탈 파츠', s:'도금 벗겨짐/오염/변형 0 · 링 밀착',
        a:'도금 벗겨짐 ≤2mm 1–2 · 미세 스크래치 · 링 약간 헐거움',
        b:'도금 벗겨짐 ≤5mm 3–5 · 체인 킹크 1–2',
        c:'광범위 벗겨짐/러스트 · 킹크 다수',
        f:'체인/링 브레이크' },
      { section:'소재', s:'균열 0 · 경화 0% · 스크래치 0 · 투명도 100%',
        a:'미세 균열 ≤1mm 1–2 · 아크릴 헤어라인 2–3',
        b:'균열 ≤3mm 2–3 · 경화 ≤20% · 딥 스크래치 ≤3mm 2–3',
        c:'딥 크랙 ≤5mm/경화 ≤50% · 크랙 라인 다수',
        f:'디스토션으로 형태 인식 불가' }
    ]
  },
  acc: {
    key:'acc', label:'잡화',
    discount:{ s:10, a:25, b:45, c:70 },
    brief:{ s:'외관/기능/내부 최상', a:'경미한 기스·느슨함', b:'갈라짐/풀림/마모 뚜렷', c:'녹/막힘 등 사용성 저하', f:'구조적 손상/부품 분실' },
    table:[
      { section:'외관', s:'가죽 갈라짐/박음질 풀림/금속 기스 0',
        a:'가죽 기스 ≤3mm 1–2 · 박음질 미세 느슨함',
        b:'갈라짐 ≤8mm 3–5 · 풀림 ≤5mm 2–3 · 마모 뚜렷',
        c:'갈라짐 ≤1.5cm 6–10 · 박음질 끊어짐 ≤1cm 2–3',
        f:'박음질 파손 등 구조적 손상' },
      { section:'기능성', s:'지퍼 부드러움 · 단추 밀착 · 찍찍이/자석 100%',
        a:'지퍼 약간 뻑뻑 · 단추 1–2 헐거움',
        b:'지퍼 가끔 걸림 · 단추 약함 3–5 · 찍찍이 ≥70%',
        c:'지퍼 자주 막힘 · 단추 파손 1–2 · 찍찍이 ≥50%',
        f:'지퍼 완전 고장 또는 주요 부품 분실' },
      { section:'내부', s:'안감 얼룩/늘어남/마모 0',
        a:'자국 ≤3mm 1–2 · 늘어남 ≤10%',
        b:'얼룩 ≤1cm 3–5 · 늘어남 ≤30%',
        c:'광범위 얼룩/변형',
        f:'구조적 손상' }
    ]
  },
  stationery: {
    key:'stationery', label:'문구',
    discount:{ s:5, a:20, b:40, c:65 },
    brief:{ s:'신품 준함', a:'경미한 기스/주름', b:'찌그러짐/흔적 부분', c:'찢어짐/기능 저하', f:'주요 기능 불가/제본 파손' },
    table:[
      { section:'필기구', s:'몸통 기스 0 · 클립 100% · 잉크 정상',
        a:'기스 ≤2mm 1–2 · 클립 약간 헐거움 · 잉크 ≥80%',
        b:'찌그러짐 ≤3mm 2–4 · 클립 약간 휨 · 잉크 ≥50%',
        c:'갈라짐 ≤5mm 1–2 · 잉크 ≥25%',
        f:'주요 기능 불가' },
      { section:'노트/다이어리', s:'표지 구김/제본 느슨함/필기/말림 0',
        a:'표지 주름 ≤5mm 1–2 · 필기 ≤5%',
        b:'표지 주름 ≤1cm 3–5 · 제본 약함 · 필기 ≤15%',
        c:'표지 찢어짐 ≤5mm 1–2 · 제본 일부 끊어짐 · 필기 ≤30%',
        f:'제본 파손' },
      { section:'기타 문구', s:'지우개 굳음 0% · 변색/눈금 흐림 0',
        a:'지우개 굳음 ≤10% · 눈금 흐림 경미',
        b:'굳음 ≤30% · 변색 부분',
        c:'지우개 갈라짐 ≤3mm 2–3 · 기능 저하',
        f:'재질 변형으로 사용 불가' }
    ]
  },
  apparel: {
    key:'apparel', label:'의류',
    discount:{ s:10, a:25, b:50, c:75 },
    brief:{ s:'원단/프린팅/마감 신품급', a:'보풀·미세 갈라짐 경미', b:'보풀/갈라짐/들뜸 뚜렷', c:'광범위 손상/마감 파손', f:'큰 구멍·그래픽 절반 이상 손상' },
    table:[
      { section:'원단', s:'보풀/실밥/바램/변형 0',
        a:'보풀 ≤2mm 3–5 · 구멍 ≤1mm 1',
        b:'보풀 ≤5mm 6–10 · 구멍 ≤3mm 1–2',
        c:'보풀 ≤1cm 다수 · 구멍 ≤8mm 2–3',
        f:'큰 구멍 ≥1cm' },
      { section:'프린팅/자수', s:'갈라짐/벗겨짐/들뜸/자수 풀림 0',
        a:'미세 갈라짐 ≤2mm 1–2 · 바램 ≤5%',
        b:'갈라짐 ≤5mm 3–5 · 부분적 벗겨짐',
        c:'프린트 떨어짐 ≤1cm 6–10 · 심한 바램',
        f:'그래픽 절반 이상 손상' },
      { section:'부속/마감', s:'단추/지퍼/리벳/와펜 양호',
        a:'단추 약간 헐거움 1–2 · 지퍼 약간 뻑뻑',
        b:'단추 빠짐 1–2 · 지퍼 가끔 걸림',
        c:'단추 빠짐 3+ · 지퍼 자주 걸림',
        f:'구조 손상' }
    ]
  },
  living: {
    key:'living', label:'생활',
    discount:{ s:10, a:25, b:45, c:70 },
    brief:{ s:'표면/인쇄/기능 완벽', a:'가벼운 기스·흠(기능 90%↑)', b:'눈에 띄는 손상(기능 70%↑)', c:'광범위 손상/기능 저하', f:'누수·큰 갈라짐·기능 불가' },
    table:[
      { section:'컵/텀블러', s:'기스 0 · 닳음 0% · 보온 100%',
        a:'기스 ≤2mm 1–2 · 보온 90%↑',
        b:'기스 ≤5mm 3–5 · 찌그러짐 ≤3mm',
        c:'깊은 기스 ≤1cm 6–10 · 찌그러짐 다수',
        f:'누수 또는 큰 갈라짐' },
      { section:'쿠션/인형', s:'겉 얼룩 0 · 속 빠짐 0% · 모양 100%',
        a:'작은 얼룩 ≤3mm 1–2 · 속 빠짐 ≤5%',
        b:'얼룩 ≤1cm 3–5 · 속 빠짐 ≤20%',
        c:'광범위 얼룩 · 속 빠짐 ≤40%',
        f:'형태 유지 불가 또는 심각한 파열' },
      { section:'플라스틱류', s:'기스/변색/헐거움 0',
        a:'헤어라인 기스 2–3 · 변색 경미',
        b:'깊은 기스 ≤3mm 2–3 · 바램 ≤20%',
        c:'갈라짐 ≤5mm 1–2 · 심한 변색',
        f:'큰 갈라짐 또는 기능 불가' }
    ]
  }
})

const active = ref<typeof tabs[number]['key']>('figure')
const current = computed(() => DATA[active.value])

/* 패키지/구성품 표 */
const pkgTable = [
  { item:'보증/라벨/택',  s:'모두 동일/정상', a:'미세 흔적', b:'부분 마모', c:'훼손/누락' },
  { item:'내부 포장재',   s:'정상', a:'주름 경미', b:'우그러짐', c:'찢김/누락' },
  { item:'설명서/스티커', s:'동봉', a:'모서리 경미', b:'오염/구김', c:'누락' },
  { item:'색상/사이즈',   s:'정상', a:'미세 편차', b:'눈에 띄는 편차', c:'불일치' },
]

/* 페널티 표 */
const penalties = [
  { type:'상품 불일치', action:'즉시 불합격', penalty:'10%', note:'다른 상품 발송' },
  { type:'사이즈 불일치', action:'즉시 불합격', penalty:'10%', note:'표기와 상이' },
  { type:'구성품 누락', action:'구매자 의사 확인', penalty:'10%', note:'박스/설명서/기본 부속' },
]
</script>
