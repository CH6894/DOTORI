<template>
  <div class="inspect-page" :class="{ 'no-emoji': !SHOW_EMOJI }">
    <!-- 제목 -->
    <div class="title">
      <h1>검수 기준</h1>
    </div>

    <!-- 카테고리 -->
    <section class="ig-panel cat-panel">
      <div class="cat" role="grid" aria-label="inspection categories quick select">
        <button
          v-for="c in catItems"
          :key="c.key"
          class="chip"
          role="gridcell"
          :class="{ active: activeKey === c.key }"
          :aria-label="`${c.label} 바로가기`"
          @click="activeKey = c.key"
        >
          <span class="chip-label">{{ c.label }}</span>
        </button>
      </div>
    </section>

    <!-- 카테고리별 기준표 -->
    <section>
      <div class="ig-panel">
        <div class="ig-panel-title">설명란</div>
        <div class="ig-desc">

          <!-- 유의사항(빨강) -->
          <div class="notice-text">
            <h3>※ 유의사항</h3>
            <p>본 검수 기준은 거래 당사자 간 원활한 거래와 보편적 기준 확립을 위한 안내입니다. 제조사별 보증은 포함하지 않습니다.</p>
            <p>제품 기능 및 기타 제품 관련 문의는 제조업체로 문의하시기 바랍니다. 제조업체의 A/S 여부는 보장되지 않을 수 있습니다.</p>
            <p>＊다음 사항들은 제조/유통 과정의 특성으로 인해 하자로 판단하지 않습니다.＊</p>
            <ul>
              -제조공정·유통과정·소재 특성상 발생 가능한 사항<br>
              -미세한 도장/마감 편차 및 생산 시점별 차이<br>
              -제품 본연의 기능과 무관한 경미 흠(스크래치 등)<br>
              -운송 과정에서 포장재에 발생할 수 있는 눌림/주름<br>
              -상품택/구성품의 경미한 눌림·이염·누락 등
            </ul>
          </div>

          <!-- 실무 가이드 -->
          <div class="guide-text">
            <h3>실무 가이드</h3>
            <p>· 점상 손상은 <strong>가장 긴 부분</strong> 기준</p>
            <p>· 면상 손상은 <strong>최대 지름</strong> 기준</p>
            <p>· <strong>5mm 이내</strong>에 모여 있는 손상은 <strong>1개</strong>로 간주</p>
          </div>

          <!-- 기존 할인율/등급 요약 -->
          <p class="ig-discount">
            <strong>할인율</strong>:
            최상 {{ current.discount.s }}%, 상 {{ current.discount.a }}%, 중 {{ current.discount.b }}%, 하 {{ current.discount.c }}%
          </p>
          <ul class="ig-grades">
            <li><span class="dot s"></span><strong>S급</strong>: {{ current.brief.s }}</li>
            <li><span class="dot a"></span><strong>A급</strong>: {{ current.brief.a }}</li>
            <li><span class="dot b"></span><strong>B급</strong>: {{ current.brief.b }}</li>
            <li><span class="dot c"></span><strong>C급</strong>: {{ current.brief.c }}</li>
            <li><span class="dot f"></span><strong>F급</strong>: {{ current.brief.f }}</li>
          </ul>
        </div>
      </div>

      <div class="ig-panel">
        <div class="ig-panel-title">세부 기준표</div>
        <div class="ig-table-wrap">
          <table class="ig-table">
            <thead>
              <tr><th style="width:180px;">구분</th><th>세부 기준</th></tr>
            </thead>
            <tbody>
              <tr v-for="(row, idx) in current.table" :key="idx">
                <th class="ig-row-head">{{ row.section }}</th>
                <td class="ig-row-body">
                  <ul class="ig-bullets">
                    <li v-if="row.s"><span class="badge s">S</span>{{ row.s }}</li>
                    <li v-if="row.a"><span class="badge a">A</span>{{ row.a }}</li>
                    <li v-if="row.b"><span class="badge b">B</span>{{ row.b }}</li>
                    <li v-if="row.c"><span class="badge c">C</span>{{ row.c }}</li>
                    <li v-if="row.f"><span class="badge f">F</span>{{ row.f }}</li>
                  </ul>
                </td>
              </tr>
            </tbody>
          </table>
          <p class="hint">안전/재생불가 이슈는 즉시 F, 전체 등급은 항목 중 최저 평가를 따릅니다.</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import '../assets/inspection.css'

/** 이모지 표시 토글 */
const SHOW_EMOJI = false

/* 카테고리 칩 데이터 */
const catItems = [
  { label: '피규어',     key: 'figure' },
  { label: '키링',       key: 'keyring' },
  { label: '잡화',       key: 'acc' },
  { label: '문구류',     key: 'stationery' },
  { label: '의류/패션',  key: 'apparel' },
  { label: '생활용품',   key: 'living' },
]
const activeKey = ref('figure')

/* ==== 데이터: 새로운 카테고리 기준 ==== */
const DATA = reactive({
  figure: {
    discount: { s: 5, a: 20, b: 40, c: 65 },
    brief: {
      s: '거의 신품급, 사용감 거의 없음, 모든 기능 완벽',
      a: '가벼운 사용감, 기능 정상',
      b: '뚜렷한 사용감, 기능 정상',
      c: '심한 사용감, 기능 일부 제한',
      f: '사용 불가능 상태',
    },
    table: [
      { section: '도색 상태',
        s: '크랙/벗겨짐/번짐/컬러 체인지 0',
        a: '크랙 ≤3mm 1–2개, 벗겨짐 ≤2mm 1–2개, 페이딩 거의 없음',
        b: '크랙 ≤8mm 3–5개, 벗겨짐 ≤5mm 3–5개, 부분 페이딩',
        c: '크랙 ≤1.5cm 6–10개, 벗겨짐 ≤1cm 6–10개, 페이딩 뚜렷',
        f: '도색 디스토션 등 복구 불가' },
      { section: '헤드/페이스',
        s: '아이/립/헤어/데칼 완벽',
        a: '아이 95%↑, 립 미세 번짐 1–2곳, 데칼 들뜸 ≤2mm 1–2곳',
        b: '아이 80%↑, 립 번짐 ≤5mm 2–3곳, 데칼 들뜸 ≤5mm 3–5곳',
        c: '아이 60%↑ 인식, 데칼 완전 들뜸 2–3곳',
        f: '헤드 크랙 등 주요 손상' },
      { section: '관절/가동',
        s: '헐거움 0, 가동 100%, 포징 완전',
        a: '헐거움 1–2부위, 가동 90% 이상',
        b: '헐거움 3–5부위, 가동 70% 이상',
        c: '조인트 크랙 1–2개, 가동 50% 이상',
        f: '조인트 브레이크 등 복구 불가' },
      { section: '바디/의상',
        s: '화이트닝/풀림/부속 로스 0',
        a: '실밥 ≤3mm 1–2개, 옐로잉 거의 없음',
        b: '옐로잉 ≤1cm 3–5곳, 풀림 ≤8mm 2–3곳, 소품 1–2개 로스',
        c: '광범위 옐로잉, 의상 찢어짐 ≤5mm 1–2곳' },
    ],
  },

  keyring: {
    discount: { s: 5, a: 15, b: 35, c: 60 },
    brief: {
      s: '신품급(프린팅/도색/메탈 파츠)',
      a: '경미한 벗겨짐/헤어라인',
      b: '페이딩/블리딩 뚜렷',
      c: '광범위 손상/러스트 가능',
      f: '프린팅 50%↑ 로스·체인/링 파손',
    },
    table: [
      { section: '프린팅/도색',
        s: '벗겨짐/크랙/블리딩 0, 페이딩 0%',
        a: '벗겨짐 ≤2mm 1–2개, 페이딩 ≤5%, 엣지 크랙 ≤1mm 1–2개',
        b: '벗겨짐 ≤5mm 3–5개, 페이딩 ≤20%, 블리딩 ≤3mm 2–3곳',
        c: '벗겨짐 ≤1cm 6–10개, 페이딩 ≤40%, 셰이프 로스 부분',
        f: '프린팅 완전 로스 ≥50%' },
      { section: '메탈 파츠',
        s: '도금 벗겨짐/오염/변형 0, 링 밀착',
        a: '도금 벗겨짐 ≤2mm 1–2개, 미세 스크래치, 링 약간 헐거움',
        b: '도금 벗겨짐 ≤5mm 3–5개, 체인 킹크 1–2곳',
        c: '광범위 벗겨짐, 킹크 다수·러스트',
        f: '체인/링 브레이크(복구 불가)' },
      { section: '소재별',
        s: '러버/PVC 균열 0, 경화 0%, 아크릴 스크래치 0, 투명도 100%',
        a: '러버/PVC 미세 균열 ≤1mm 1–2개, 아크릴 헤어라인 2–3개',
        b: '러버/PVC 균열 ≤3mm 2–3개·경화 ≤20%, 아크릴 딥 스크래치 ≤3mm 2–3개',
        c: '러버/PVC 딥 크랙 ≤5mm·경화 ≤50%, 아크릴 크랙 라인 다수',
        f: '소재 디스토션으로 형태 인식 불가' },
    ],
  },

  acc: {
    discount: { s: 10, a: 25, b: 45, c: 70 },
    brief: {
      s: '외관/기능/내부 최상',
      a: '경미한 기스·느슨함',
      b: '갈라짐/풀림/마모 뚜렷',
      c: '녹/막힘 등 사용성 저하',
      f: '구조적 손상/부품 분실',
    },
    table: [
      { section: '뱃지/핀버튼/스티커',
        s: '프린팅 손상 0, 접착력/고정력 100%, 구김 0',
        a: '프린팅 미세 흐림, 기스 ≤2mm 1–2개, 모서리 약간 말림',
        b: '프린팅 부분 바램, 기스 ≤5mm 3–5개, 구김 ≤3mm 2–3곳',
        c: '프린팅 심한 바램, 기스 ≤1cm 6–10개, 구김/찢어짐 ≤2mm 1–2곳',
        f: '프린팅 50%↑ 손상, 핀 파손' },
      { section: '지갑/파우치/가방',
        s: '지퍼/자석/접착 100%, 박음질 완벽, 얼룩 0',
        a: '지퍼 약간 뻑뻑, 박음질 느슨함 미세, 작은 얼룩 ≤3mm 1–2개',
        b: '지퍼 걸림 가끔, 박음질 느슨함 ≤5mm 2–3곳, 얼룩 ≤1cm 3–5개',
        c: '지퍼 자주 막힘, 박음질 끊어짐 ≤1cm 2–3곳, 광범위 얼룩',
        f: '지퍼 파손/구조적 손상' },
      { section: '거울/그립톡',
        s: '표면 기스 0, 기능 100%',
        a: '헤어라인 기스 1–2개, 기능 95%↑',
        b: '딥 스크래치 ≤2mm 2–3개, 기능 80%↑',
        c: '스크래치 ≤5mm 다수, 기능 저하 뚜렷',
        f: '균열/기능 불가' },
      { section: '깃발/응원봉커버',
        s: '원단/프린팅 손상 0, 고정력 100%',
        a: '미세 주름, 프린팅 95%↑ 선명',
        b: '주름 ≤5mm 3–5개, 프린팅 흐림 부분',
        c: '찢어짐 ≤3mm 1–2개, 프린팅 바램 뚜렷',
        f: '큰 찢어짐/프린팅 50%↑ 손상' },
      { section: '우산',
        s: '살대/천 손상 0, 기능 100%',
        a: '살대 약간 휨 1–2개, 천 미세 얼룩 ≤3mm 1–2개',
        b: '살대 휨 3–5개, 천 얼룩 ≤1cm 3–5개',
        c: '살대 부러짐 1–2개, 천 찢어짐 ≤5mm 1–2개',
        f: '살대 다수 파손/천 큰 찢어짐' },
    ],
  },

  stationery: {
    discount: { s: 5, a: 20, b: 40, c: 65 },
    brief: {
      s: '신품에 준함',
      a: '경미한 기스/주름',
      b: '찌그러짐/흔적 부분',
      c: '찢어짐/기능 저하',
      f: '주요 기능 불가/제본 파손',
    },
    table: [
      { section: '달력/앨범/포토카드',
        s: '모서리 손상/인쇄 흐림/구김 0, 제본 완벽',
        a: '모서리 미세 마모, 인쇄 95%↑ 선명, 제본 양호',
        b: '모서리 마모 ≤2mm, 구김 ≤3mm 1–2곳, 제본 약간 느슨함',
        c: '모서리 찍힘 ≤5mm 2–3곳, 구김 다수, 제본 일부 끊어짐',
        f: '심한 찢어짐/제본 파손' },
      { section: '마그넷/키캡',
        s: '프린팅 손상 0, 자력/장착감 100%, 표면 기스 0',
        a: '프린팅 95%↑ 선명, 자력/장착감 90%↑, 헤어라인 기스 1–2개',
        b: '프린팅 흐림 부분, 자력/장착감 70%↑, 기스 ≤3mm 2–3개',
        c: '프린팅 바램 뚜렷, 자력/장착감 50%↑, 기스 ≤5mm 다수',
        f: '프린팅 50%↑ 손상, 자력/장착 불가' },
      { section: '아크릴블록/스탠드',
        s: '스크래치/균열/변색 0, 투명도 100%',
        a: '헤어라인 스크래치 1–2개, 투명도 95%↑',
        b: '딥 스크래치 ≤2mm 2–3개, 미세 균열 ≤1mm 1개',
        c: '스크래치 ≤5mm 다수, 균열 ≤3mm 1–2개',
        f: '관통 균열/형태 변형' },
      { section: '마우스패드/집게',
        s: '표면 기스/얼룩 0, 미끄럼방지/집는힘 100%',
        a: '기스 ≤2mm 1–2개, 모서리 약간 말림, 기능 90%↑',
        b: '기스 ≤5mm 3–5개, 얼룩 ≤1cm 2–3개, 기능 70%↑',
        c: '기스 ≤1cm 다수, 광범위 얼룩, 기능 50%↑',
        f: '찢어짐/기능 상실' },
      { section: '볼펜/스탬프',
        s: '기능 100%, 몸통 기스 0, 클립/손잡이 완벽',
        a: '기능 90%↑, 기스 ≤2mm 1–2개, 클립 약간 헐거움',
        b: '기능 70%↑, 찌그러짐 ≤3mm 2–4개, 클립 약간 휨',
        c: '기능 50%↑, 갈라짐 ≤5mm 1–2개',
        f: '주요 기능 불가' },
      { section: '퍼즐/와펜/마스킹테이프',
        s: '조각/자수/프린팅 손상 0, 접착력 100%',
        a: '조각 모서리 미세 마모 1–2개, 자수 미세 풀림 1–2곳, 접착력 90%↑',
        b: '조각 찍힘 ≤2mm 2–3개, 자수 풀림 ≤3mm 2–3곳, 접착력 70%↑',
        c: '조각 손상 ≤3mm 3–5개, 자수 풀림 ≤5mm 다수, 접착력 50%↑',
        f: '조각 분실/자수 50%↑ 손상/접착 불가' },
    ],
  },

  apparel: {
    discount: { s: 10, a: 25, b: 50, c: 75 },
    brief: {
      s: '원단/프린팅/마감 신품급',
      a: '보풀·미세 갈라짐 경미',
      b: '보풀/갈라짐/들뜸 뚜렷',
      c: '광범위 손상/마감 파손',
      f: '큰 구멍·그래픽 절반 이상 손상',
    },
    table: [
      { section: '원단 상태',
        s: '보풀/실밥/바램/변형 0',
        a: '보풀 ≤2mm 3–5개, 구멍 ≤1mm 1개, 색 변화 ≤5%',
        b: '보풀 ≤5mm 6–10개, 구멍 ≤3mm 1–2개, 바램 ≤20%',
        c: '보풀 ≤1cm 다수, 구멍 ≤8mm 2–3개, 바램 ≤40%' },
      { section: '프린팅/자수',
        s: '갈라짐/벗겨짐/들뜸/자수 풀림 0',
        a: '미세 갈라짐 ≤2mm 1–2개, 그래픽 바램 ≤5%, 들뜸 ≤2mm 1–2곳',
        b: '갈라짐 ≤5mm 3–5개, 부분적 벗겨짐, 들뜸 ≤5mm 3–5곳',
        c: '프린트 떨어짐 ≤1cm 6–10곳, 심한 바램, 광범위 들뜸',
        f: '그래픽 절반 이상 손상' },
      { section: '부속품/마감',
        s: '단추/지퍼/리벳/와펜 모두 양호',
        a: '단추 약간 헐거움 1–2개, 지퍼 약간 뻑뻑',
        b: '단추 빠짐 1–2개, 지퍼 걸림 가끔, 박음질 느슨함 ≤8mm 2–3곳',
        c: '단추 빠짐 3개 이상, 지퍼 자주 걸림, 박음질 끊어짐 ≤1cm 3–5곳',
        f: '큰 구멍 ≥1cm, 구조적 손상' },
    ],
  },

  living: {
    discount: { s: 10, a: 25, b: 45, c: 70 },
    brief: {
      s: '표면/인쇄/기능 완벽',
      a: '가벼운 기스·흠 있으나 기능 90%↑',
      b: '눈에 띄는 손상, 기능 70%↑',
      c: '광범위 손상 및 기능 저하',
      f: '누수·큰 갈라짐·기능 불가',
    },
    table: [
      { section: '컵/텀블러',
        s: '표면 기스 0, 프린트 닳음 0%, 밀폐 완벽',
        a: '기스 ≤2mm 1–2개, 작은 흠 ≤1mm 1개',
        b: '기스 ≤5mm 3–5개, 찌그러짐 ≤3mm 2–3개',
        c: '깊은 기스 ≤1cm 6–10개, 찌그러짐 다수',
        f: '누수/큰 갈라짐' },
      { section: '쿠션/인형/바디필로우',
        s: '겉 얼룩 0, 속 빠짐 0%, 모양 100%, 박음질 완벽',
        a: '작은 얼룩 ≤3mm 1–2개, 속 빠짐 ≤5%',
        b: '얼룩 ≤1cm 3–5개, 속 빠짐 ≤20%, 모양 부분 변형',
        c: '광범위 얼룩, 속 빠짐 ≤40%, 박음질 느슨함 ≤1cm 3–5곳' },
      { section: '응원봉',
        s: '발광 100%, 연결 완벽, 외관 손상 0',
        a: '발광 95%↑, 연결 안정, 기스 ≤2mm 1–2개',
        b: '발광 80%↑, 연결 간헐적 문제, 기스 ≤5mm 3–5개',
        c: '발광 60%↑, 연결 불안정, 갈라짐 ≤3mm 1–2개',
        f: '발광 불가/연결 불가/구조적 손상' },
    ],
  },
})

const current = computed(() => DATA[activeKey.value])
</script>