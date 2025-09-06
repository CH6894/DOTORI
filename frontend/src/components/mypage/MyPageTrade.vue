<!-- frontend/src/components/mypage/MyPageTrade.vue -->
<template>
    <main class="mypage">
        <section class="container">
            <div class="trade">
                <!-- 목록 화면 -->
                <div v-if="!showDetail">
                    <div class="section-header">
                        <button @click="goToMyPage" class="btn-back">←돌아가기</button>
                        <h1 class="section__title section__title--center">거래 내역</h1>
                    </div>

                <!-- 요약(전체/판매/구매) -->
                <div class="panel panel--center">
                    <ul class="trade-summary" role="list">
                        <li class="trade-summary__item">
                            <span class="trade-summary__label">전체</span>
                            <span class="trade-summary__num">{{ summary.total }}</span>
                        </li>
                        <li class="trade-summary__item">
                            <span class="trade-summary__label">판매</span>
                            <span class="trade-summary__num">{{ summary.sell }}</span>
                        </li>
                        <li class="trade-summary__item">
                            <span class="trade-summary__label">구매</span>
                            <span class="trade-summary__num">{{ summary.buy }}</span>
                        </li>
                    </ul>
                </div>
                
                <!-- 거래 테이블 -->
                <div class="panel panel--center">
                    <!-- 필터 -->
                    <div class="panel panel--center">
                        <div class="filter-bar" role="tablist" aria-label="거래 유형 필터">
                            <div class="tabs">
                                <button class="tab" :class="{ 'is-active': selectedKind === 'all' }" role="tab"
                                    :aria-selected="selectedKind === 'all'" @click="setKind('all')">
                                    전체 ({{ summary.total }})
                                </button>
                                <button class="tab" :class="{ 'is-active': selectedKind === 'sell' }" role="tab"
                                    :aria-selected="selectedKind === 'sell'" @click="setKind('sell')">
                                    판매 ({{ summary.sell }})
                                </button>
                                <button class="tab" :class="{ 'is-active': selectedKind === 'buy' }" role="tab"
                                    :aria-selected="selectedKind === 'buy'" @click="setKind('buy')">
                                    구매 ({{ summary.buy }})
                                </button>
                            </div>

                            <div class="search-wrap">
                                <input v-model.trim="searchKeyword" class="search-input" type="text"
                                    placeholder="거래번호 또는 상품명 검색" aria-label="검색" />
                            </div>
                        </div>
                    </div>
                    
                    <!-- 검색 결과 표시 영역 -->
                    <div class="table-wrap" v-if="filteredTrades.length > 0">
                        <table class="tbl tbl--fixed">
                            <colgroup>
                                <col class="col-no" />
                                <col class="col-product" />
                                <col class="col-price" />
                                <col class="col-date" />
                                <col class="col-status" />
                            </colgroup>
                            <thead>
                                <tr>
                                    <th>거래번호</th>
                                    <th>상품</th>
                                    <th>금액</th>
                                    <th>거래일자</th>
                                    <th>거래상태</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="t in pagedTrades" :key="t.no">
                                    <td>{{ t.no }}</td>

                                    <!-- 상품 컬럼: 배지 칸(고정) + 제목(유연) -->
                                    <td class="td-product">
                                        <span class="kind-badge" :data-kind="t.kind">
                                            {{ t.kind === 'sell' ? '판매' : '구매' }}
                                        </span>
                                        <span class="product-title" :title="t.item">{{ t.item }}</span>
                                    </td>

                                    <td>{{ t.price.toLocaleString() }}원</td>
                                    <td>{{ formatDate(t.date) }}</td>
                                    <td>
                                        <span class="badge" :data-type="t.state.type">{{ t.state.text }}</span>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    
                    <!-- 검색 결과 없음 메시지 -->
                    <div v-if="filteredTrades.length === 0" class="no-results">
                        <p>검색 결과가 없습니다.</p>
                    </div>

                    <!-- 페이지네이션 -->
                    <nav class="pagination" role="navigation" aria-label="페이지네이션" v-if="pageCount > 1">
                        <button class="page-btn" :disabled="page === 1" @click="goPage(1)" aria-label="첫 페이지">≪</button>
                        <button class="page-btn" :disabled="page === 1" @click="prevPage" aria-label="이전 페이지">‹</button>

                        <button v-for="n in pagesToShow" :key="n" class="page-num" :class="{ 'is-active': page === n }"
                            @click="goPage(n)" :aria-current="page === n ? 'page' : null">
                            {{ n }}
                        </button>

                        <button class="page-btn" :disabled="page === pageCount" @click="nextPage"
                            aria-label="다음 페이지">›</button>
                        <button class="page-btn" :disabled="page === pageCount" @click="goPage(pageCount)"
                            aria-label="마지막 페이지">≫</button>

                        <span class="page-info" aria-hidden="true">페이지 {{ page }} / {{ pageCount }}</span>
                    </nav>
                </div>

            </div>

            <!-- 상세 화면 -->
            <div v-else class="detail-view">
                <div class="header">
                    <div class="spacer"></div>
                    <h1 class="title">거래 상세</h1>
                    <button class="btn-close" @click="goBack"><span>×</span></button>
                </div>

                <div class="panel">
                    <div class="detail-head">
                        <div class="left">
                            <div class="no">{{ tradeSelected.no }}</div>
                            <div class="date">{{ formatDate(tradeSelected.date) }}</div>
                        </div>
                        <div class="right">
                            <span class="kind-badge lg" :data-kind="tradeSelected.kind">
                                {{ tradeSelected.kind === 'sell' ? '판매' : '구매' }}
                            </span>
                            <span class="badge lg" :data-type="tradeSelected.state.type">
                                {{ tradeSelected.state.text }}
                            </span>
                        </div>
                    </div>

                    <div class="product-info">
                        <img :src="tradeSelected.image" :alt="tradeSelected.item" class="product-image" />
                        <div class="product-details">
                            <h4 class="product-name">{{ tradeSelected.item }}</h4>
                            <p class="product-price">{{ tradeSelected.price.toLocaleString() }}원</p>
                        </div>
                    </div>
                </div>

                <div class="bottom-actions">
                    <button class="btn-primary" @click="goBack">목록으로</button>
                </div>
            </div>
            </div>
        </section>
    </main>
</template>

<script>
export default {
    name: 'MyPageTrade',
    data() {
        return {
            showDetail: false,
            tradeSelected: null,

            selectedKind: 'all',   // all | sell | buy
            searchKeyword: '',

            // 페이지네이션
            page: 1,
            pageSize: 20,

            // 데모 데이터 (생략 가능 / 기존 네 데이터 그대로 사용)
            trades: [
                { no: 'O2025082901', kind: 'buy', item: '루피 피규어', price: 53000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-29T10:12:00' },
                { no: 'O2025082801', kind: 'buy', item: '미쿠 스페셜', price: 67000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-28T09:01:00' },
                { no: 'S2025082701', kind: 'sell', item: '아카자 인형', price: 48000, state: { type: 'sell-done', text: '판매 완료' }, date: '2025-08-27T14:30:00' },
                { no: 'S2025082601', kind: 'sell', item: '렌고쿠 키링', price: 10000, state: { type: 'selling', text: '판매중' }, date: '2025-08-26T18:25:00' },
                { no: 'O2025082501', kind: 'buy', item: '원피스 세트', price: 82000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-25T11:05:00' },

                { no: 'O2025082401', kind: 'buy', item: '포켓몬 카드', price: 32000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-24T10:12:00' },
                { no: 'O2025082301', kind: 'buy', item: '건담 모형', price: 89000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-23T09:01:00' },
                { no: 'S2025082201', kind: 'sell', item: '하츠네 미쿠 피규어', price: 53000, state: { type: 'sell-done', text: '판매 완료' }, date: '2025-08-22T14:30:00' },
                { no: 'S2025082101', kind: 'sell', item: '히나타 소요 피규어', price: 67000, state: { type: 'selling', text: '판매중' }, date: '2025-08-21T18:25:00' },
                { no: 'O2025082001', kind: 'buy', item: '나루토 피규어', price: 45000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-20T11:05:00' },

                { no: 'O2025081901', kind: 'buy', item: '조로 피규어', price: 54000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-19T10:12:00' },
                { no: 'O2025081801', kind: 'buy', item: '사보 피규어', price: 52000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-18T09:01:00' },
                { no: 'S2025081701', kind: 'sell', item: '토가 키링', price: 12000, state: { type: 'sell-done', text: '판매 완료' }, date: '2025-08-17T14:30:00' },
                { no: 'S2025081601', kind: 'sell', item: '에렌 피규어', price: 60000, state: { type: 'selling', text: '판매중' }, date: '2025-08-16T18:25:00' },
                { no: 'O2025081501', kind: 'buy', item: '리바이 피규어', price: 62000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-15T11:05:00' },

                { no: 'O2025081401', kind: 'buy', item: '루피 피규어', price: 53000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-14T10:12:00' },
                { no: 'O2025081301', kind: 'buy', item: '미쿠 스페셜', price: 67000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-13T09:01:00' },
                { no: 'S2025081201', kind: 'sell', item: '아카자 인형', price: 48000, state: { type: 'sell-done', text: '판매 완료' }, date: '2025-08-12T14:30:00' },
                { no: 'S2025081101', kind: 'sell', item: '렌고쿠 키링', price: 10000, state: { type: 'selling', text: '판매중' }, date: '2025-08-11T18:25:00' },
                { no: 'O2025081001', kind: 'buy', item: '원피스 세트', price: 82000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-10T11:05:00' },

                { no: 'O2025080901', kind: 'buy', item: '포켓몬 카드', price: 32000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-09T10:12:00' },
                { no: 'O2025080801', kind: 'buy', item: '건담 모형', price: 89000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-08T09:01:00' },
                { no: 'S2025080701', kind: 'sell', item: '하츠네 미쿠 피규어', price: 53000, state: { type: 'sell-done', text: '판매 완료' }, date: '2025-08-07T14:30:00' },
                { no: 'S2025080601', kind: 'sell', item: '히나타 소요 피규어', price: 67000, state: { type: 'selling', text: '판매중' }, date: '2025-08-06T18:25:00' },
                { no: 'O2025080501', kind: 'buy', item: '나루토 피규어', price: 45000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-05T11:05:00' },

                { no: 'O2025080401', kind: 'buy', item: '조로 피규어', price: 54000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-04T10:12:00' },
                { no: 'O2025080301', kind: 'buy', item: '사보 피규어', price: 52000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-03T09:01:00' },
                { no: 'S2025080201', kind: 'sell', item: '토가 키링', price: 12000, state: { type: 'sell-done', text: '판매 완료' }, date: '2025-08-02T14:30:00' },
                { no: 'S2025080101', kind: 'sell', item: '에렌 피규어', price: 60000, state: { type: 'selling', text: '판매중' }, date: '2025-08-01T18:25:00' },
                { no: 'O2025073101', kind: 'buy', item: '리바이 피규어', price: 62000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-07-31T11:05:00' },

                { no: 'O2025073001', kind: 'buy', item: '루피 피규어', price: 53000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-07-30T10:12:00' },
                { no: 'O2025072901', kind: 'buy', item: '미쿠 스페셜', price: 67000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-07-29T09:01:00' },
                { no: 'S2025072801', kind: 'sell', item: '아카자 인형', price: 48000, state: { type: 'sell-done', text: '판매 완료' }, date: '2025-07-28T14:30:00' },
                { no: 'S2025072701', kind: 'sell', item: '렌고쿠 키링', price: 10000, state: { type: 'selling', text: '판매중' }, date: '2025-07-27T18:25:00' },
                { no: 'O2025072601', kind: 'buy', item: '원피스 세트', price: 82000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-07-26T11:05:00' },

                { no: 'O2025072501', kind: 'buy', item: '포켓몬 카드', price: 32000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-07-25T10:12:00' },
                { no: 'O2025072401', kind: 'buy', item: '건담 모형', price: 89000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-07-24T09:01:00' },
                { no: 'S2025072301', kind: 'sell', item: '하츠네 미쿠 피규어', price: 53000, state: { type: 'sell-done', text: '판매 완료' }, date: '2025-07-23T14:30:00' },
                { no: 'S2025072201', kind: 'sell', item: '히나타 소요 피규어', price: 67000, state: { type: 'selling', text: '판매중' }, date: '2025-07-22T18:25:00' },
                { no: 'O2025072101', kind: 'buy', item: '나루토 피규어', price: 45000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-07-21T11:05:00' },

                { no: 'O2025072001', kind: 'buy', item: '조로 피규어', price: 54000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-07-20T10:12:00' },
                { no: 'O2025071901', kind: 'buy', item: '사보 피규어', price: 52000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-07-19T09:01:00' },
                { no: 'S2025071801', kind: 'sell', item: '토가 키링', price: 12000, state: { type: 'sell-done', text: '판매 완료' }, date: '2025-07-18T14:30:00' },
                { no: 'S2025071701', kind: 'sell', item: '에렌 피규어', price: 60000, state: { type: 'selling', text: '판매중' }, date: '2025-07-17T18:25:00' },
                { no: 'O2025071601', kind: 'buy', item: '리바이 피규어', price: 62000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-07-16T11:05:00' },

                { no: 'O2025071501', kind: 'buy', item: '루피 피규어', price: 53000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-07-15T10:12:00' },
                { no: 'O2025071401', kind: 'buy', item: '미쿠 스페셜', price: 67000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-07-14T09:01:00' },
                { no: 'S2025071301', kind: 'sell', item: '아카자 인형', price: 48000, state: { type: 'sell-done', text: '판매 완료' }, date: '2025-07-13T14:30:00' },
                { no: 'S2025071201', kind: 'sell', item: '렌고쿠 키링', price: 10000, state: { type: 'selling', text: '판매중' }, date: '2025-07-12T18:25:00' },
                { no: 'O2025071101', kind: 'buy', item: '원피스 세트', price: 82000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-07-11T11:05:00' },
            ]
            ,
        }
    },

    computed: {
        summary() {
            const total = this.trades.length
            const sell = this.trades.filter(t => t.kind === 'sell').length
            return { total, sell, buy: total - sell }
        },

        sortedTrades() {
            return [...this.trades].sort(
                (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()
            )
        },

        filteredTrades() {
            let rows = this.sortedTrades
            if (this.selectedKind !== 'all') rows = rows.filter(t => t.kind === this.selectedKind)
            if (this.searchKeyword) {
                const q = this.searchKeyword.toLowerCase()
                rows = rows.filter(t => {
                    const no = t.no ? t.no.toLowerCase() : ''
                    const item = t.item ? t.item.toLowerCase() : ''
                    return no.includes(q) || item.includes(q)
                })
            }
            return rows
        },

        pageCount() {
            return Math.max(1, Math.ceil(this.filteredTrades.length / this.pageSize))
        },

        pagedTrades() {
            const start = (this.page - 1) * this.pageSize
            return this.filteredTrades.slice(start, start + this.pageSize)
        },

        // 숫자 버튼: 현재 페이지 기준 최대 5개
        pagesToShow() {
            const max = 5
            const half = Math.floor(max / 2)
            let start = Math.max(1, this.page - half)
            let end = Math.min(this.pageCount, start + max - 1)
            if (end - start + 1 < max) start = Math.max(1, end - max + 1)
            const arr = []
            for (let i = start; i <= end; i++) arr.push(i)
            return arr
        },
    },

    watch: {
        // 필터/검색 변경 시 1페이지로 리셋
        selectedKind() { this.page = 1 },
        searchKeyword() { this.page = 1 },

        // 전체 개수가 줄어  현재 페이지가 넘치면 보정
        filteredTrades() {
            if (this.page > this.pageCount) this.page = this.pageCount
        },
    },

    methods: {
        setKind(kind) { this.selectedKind = kind },

        viewDetail(trade) {
            this.tradeSelected = trade
            this.showDetail = true
            this.$nextTick(() => window.scrollTo({ top: 0 }))
        },
        goBack() {
            this.showDetail = false
            this.tradeSelected = null
        },
        goToMyPage() {
            this.$router.push({ name: 'mypage' })
        },

        goPage(n) {
            if (n < 1 || n > this.pageCount) return
            this.page = n
            this.$nextTick(() => window.scrollTo({ top: 0, behavior: 'smooth' }))
        },
        prevPage() { this.goPage(this.page - 1) },
        nextPage() { this.goPage(this.page + 1) },

        formatDate(iso) {
            if (!iso) return ''
            if (iso.includes('T')) {
                const d = new Date(iso)
                if (isNaN(d)) return iso
                const y = d.getFullYear()
                const m = String(d.getMonth() + 1).padStart(2, '0')
                const day = String(d.getDate()).padStart(2, '0')
                return `${y}.${m}.${day}`
            }
            return iso
        },
    },
}
</script>

<style scoped>
/* ===== 섹션 헤더 ===== */
.section-header {
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 2rem 0 1rem 0;
    position: relative;
    width: 100%;
}

.section-header .section__title {
    text-align: center;
    margin: 0;
    font-size: 22px;
    font-weight: 800;
    color: #2d251c;
    flex: 1;
}

.section-header .btn-back {
    position: absolute;
    left: 0;
    z-index: 1;
}

.btn-back {
    padding: 0.5rem 0.75rem;
    background: #f8f9fa;
    color: #6c757d;
    border: 1px solid #dee2e6;
    border-radius: 4px;
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.15s ease;
    flex-shrink: 0;
}

.btn-back:hover {
    background: #e9ecef;
    color: #495057;
    border-color: #adb5bd;
}

/* ===== Trade page (scoped) ===== */
.trade {
    width: 100%;
    --row-h: 3.5rem;
    --badge-col: clamp(2.1rem, 2vw, 2.4rem);
}

.mypage {
    color: #2d251c;
    letter-spacing: -0.00625rem;
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
}

.container {
    width: clamp(20rem, 96vw, 82.5rem);
    /* 320px ~ 1320px */
    margin: 0 auto;
    padding-bottom: 120px;
}

/* 제목 & 패널 */
.section__title {
    margin: 1.375rem .25rem .75rem;
    font-size: 1.375rem;
    font-weight: 800;
    color: #2d251c;
}

.section__title--center {
    text-align: center;
}

.panel {
    background: #fff;
    border: .09375rem solid #e9e4db;
    border-radius: .75rem;
    padding: 1.25rem;
    margin-bottom: 1.25rem;
}

.panel--center {
    margin: 0 auto 1.25rem;
}

/* 요약 */
.trade-summary {
    margin: 0;
    padding: 1.25rem .625rem;
    list-style: none;
    border: .0625rem solid #e9e4db;
    border-radius: .5rem;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: .25rem;
    background: #fff;
}

.trade-summary__item {
    text-align: center;
    padding: .625rem 0;
}

.trade-summary__label {
    display: block;
    font-weight: 800;
    color: #2d251c;
    margin-bottom: .5rem;
    font-size: .875rem;
}

.trade-summary__num {
    display: inline-block;
    font-weight: 800;
    font-size: 1.125rem;
    color: #000;
}

/* 필터 */
.filter-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: .75rem;
    flex-wrap: wrap;
}

.tabs {
    display: flex;
    gap: .375rem;
}

.tab {
    appearance: none;
    background: #fff;
    border: .0625rem solid #e5dcc9;
    padding: .5rem .75rem;
    border-radius: 999px;
    font-weight: 700;
    color: #7b6d5d;
    cursor: pointer;
}

.tab.is-active {
    color: #2d251c;
    border-color: #2d251c;
}

.search-wrap {
    margin-left: auto;
}

.search-input {
    padding: .625rem .75rem;
    border: .0625rem solid #ddd;
    border-radius: .375rem;
    font-size: .875rem;
    min-height: 2.375rem;
    width: min(40ch, 100%);
}

.no-results {
    text-align: center;
    padding: 3rem 1rem;
    color: #666;
    font-size: 1rem;
}

/* ===== 표: 줄 깨짐 방지 ===== */
.table-wrap {
    overflow-x: auto;
}

.tbl {
    width: 100%;
    border-collapse: collapse;
}

.tbl--fixed {
    table-layout: fixed;
}

/* td/th 공통 */
.tbl--fixed th,
.tbl--fixed td {
    height: var(--row-h);
    padding: 0 .625rem;
    border-bottom: .0625rem solid #eee6d7;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    vertical-align: middle;
    text-align: center;
}

.tbl--fixed tbody tr {
    height: var(--row-h);
}

/* 상품 열 좌측, 상태 열 중앙 */
.tbl--fixed th:nth-child(2),
.tbl--fixed td:nth-child(2) {
    text-align: left;
}

.tbl--fixed th:nth-child(5),
.tbl--fixed td:nth-child(5) {
    text-align: center;
}

/* 유연한 컬럼 폭 */
.tbl--fixed .col-no {
    width: clamp(12ch, 16ch, 18ch);
}

.tbl--fixed .col-product {
    width: auto;
}

.tbl--fixed .col-price {
    width: clamp(9ch, 11ch, 13ch);
}

.tbl--fixed .col-date {
    width: clamp(10ch, 12ch, 14ch);
}

.tbl--fixed .col-status {
    width: clamp(8ch, 10ch, 12ch);
}

/* ===== 상품 셀 내부만 grid 사용 ===== */
.td-product {
    inline-size: 100%;
    min-width: 0;
    height: 100%;
    display: grid;
    grid-template-columns: var(--badge-col) minmax(0, 1fr);
    align-items: center;
    gap: .5rem;
}

/* 구매/판매 배지 폭 고정 + 좌측정렬 */
.kind-badge {
    box-sizing: border-box;
    width: 100%;
    text-align: left;
    padding: .25rem .5rem;
    border-radius: 999px;
    font-size: .75rem;
    font-weight: 600;
    border: .0625rem solid #e4d8c3;
    background: #f7f5ef;
    color: #5c5346;
}

.kind-badge[data-kind="sell"] {
    background: #fff7ea;
    border-color: #f0dfbd;
}

.kind-badge[data-kind="buy"] {
    background: #eef6ff;
    border-color: #cfe1ff;
}

/* 상품명 */
.product-title {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-weight: 600;
}

/* 상태 뱃지 */
.badge {
    display: inline-block;
    padding: .375rem .75rem;
    border-radius: 20rem;
    font-size: .75rem;
    font-weight: 700;
    color: #fff;
    border: .0625rem solid transparent;
}

.badge.lg {
    padding: .5rem .875rem;
}

.badge[data-type="sell-done"] {
    background: #f97316;
}

.badge[data-type="buy-done"] {
    background: #22c55e;
}

.badge[data-type="selling"] {
    background: #3b82f6;
}

/* 페이지네이션 */
.pagination {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: .375rem;
    padding-top: 1rem;
    flex-wrap: wrap;
}

.page-btn,
.page-num {
    min-width: 2.25rem;
    min-height: 2.25rem;
    padding: .25rem .5rem;
    border: .0625rem solid #e0e0e0;
    background: #fff;
    border-radius: .375rem;
    font-weight: 700;
    font-size: .875rem;
    cursor: pointer;
}

.page-num.is-active {
    background: #2d251c;
    color: #fff;
    border-color: #2d251c;
}

.page-btn:disabled {
    opacity: .45;
    cursor: not-allowed;
}

.page-info {
    margin-left: .5rem;
    font-size: .875rem;
    color: #7b6d5d;
}

/* 버튼 */
.btn {
    padding: .5rem 1rem;
    border: .0625rem solid #ddd;
    background: #fff;
    border-radius: .375rem;
    cursor: pointer;
    font-weight: 600;
    font-size: .8125rem;
    white-space: nowrap;
}

.btn--sm {
    padding: .375rem .75rem;
    font-size: .75rem;
}

.btn-primary {
    background: #fc703c;
    color: #fff;
    border: none;
    width: 100%;
    border-radius: .5rem;
    padding: .875rem 1.75rem;
    font-weight: 700;
    cursor: pointer;
    font-size: 1rem;
}

.btn-primary:hover {
    background: #e5633a;
}

/* 빈 상태 */
.empty-state {
    text-align: center;
    padding: 2.5rem 1.25rem;
}

.empty-message {
    margin: 0;
    color: #666;
    font-size: 1rem;
}

/* 상세 */
.detail-view {
    max-width: 45rem;
    margin: 0 auto;
}

.header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 1.875rem 0;
    padding: 0 .625rem;
}

.spacer {
    width: 2.5rem;
}

.btn-close {
    width: 1.875rem;
    height: 1.875rem;
    margin-bottom: .625rem;
    border: none;
    background: #f4f3e6;
    border: .0625rem solid #e5dcc9;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 1.25rem;
    color: #7b6d5d;
    font-weight: 600;
}

.btn-close:hover {
    background: #e9e4db;
}

.title {
    font-size: 1.25rem;
    font-weight: 800;
    margin: 0;
    color: #2d251c;
}

.detail-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: .75rem;
    margin-bottom: .75rem;
}

.detail-head .no {
    font-weight: 800;
    color: #2d251c;
}

.detail-head .date {
    color: #7b6d5d;
    font-size: .8125rem;
}

.detail-head .right {
    display: flex;
    align-items: center;
    gap: .5rem;
}

.product-info {
    display: flex;
    gap: 1rem;
    align-items: center;
}

.product-image {
    width: 5rem;
    height: 5rem;
    object-fit: cover;
    border-radius: .5rem;
    border: .0625rem solid #e0e0e0;
}

.product-details {
    flex: 1;
}

.product-name {
    font-size: 1rem;
    font-weight: 600;
    margin: 0 0 .25rem 0;
    color: #333;
}

.product-price {
    font-size: 1.125rem;
    font-weight: 700;
    color: #ff6b35;
    margin: 0;
}

.bottom-actions {
    margin-top: 1.875rem;
}

/* 반응형 */
@media (max-width:48rem) {
    .search-wrap {
        margin-left: 0;
        width: 100%;
    }

    .search-input {
        width: 100%;
    }

    .detail-view {
        max-width: 100%;
    }
}
</style>
