<template>
  <main class="mypage">
    <section class="container">
      <!-- 목록 화면 -->
      <div v-if="!showDetail">
        <h1 class="section__title section__title--center">판매 내역</h1>

        <!-- 요약 정보 -->
        <div class="panel panel--center">
          <ul class="sales-summary">
            <li class="sales-summary__item">
              <span class="sales-summary__label">전체</span>
              <span class="sales-summary__num">{{ summary.total }}</span>
            </li>
            <li class="sales-summary__item">
              <span class="sales-summary__label">판매중</span>
              <span class="sales-summary__num">{{ summary.selling }}</span>
            </li>
            <li class="sales-summary__item">
              <span class="sales-summary__label">거래완료</span>
              <span class="sales-summary__num">{{ summary.completed }}</span>
            </li>
            <li class="sales-summary__item">
              <span class="sales-summary__label">판매취소</span>
              <span class="sales-summary__num">{{ summary.cancelled }}</span>
            </li>
          </ul>
        </div>

        <!-- 필터 및 검색 -->
        <div class="panel panel--center">
          <div class="filter-section">
            <div class="filter-group">
              <label class="filter-label">상태</label>
              <select v-model="selectedStatus" class="filter-select">
                <option value="all">전체</option>
                <option value="ing">판매중</option>
                <option value="done">거래완료</option>
                <option value="cancelled">판매취소</option>
              </select>
            </div>
            <div class="filter-group">
              <input 
                v-model="searchKeyword" 
                type="text" 
                placeholder="판매번호 또는 상품명 검색"
                class="search-input"
              />
            </div>
          </div>
        </div>

        <!-- 판매 내역 테이블 -->
        <div class="panel panel--center" v-if="filteredSales.length">
          <table class="tbl">
            <thead>
              <tr>
                <th>판매번호</th>
                <th>상품</th>
                <th>금액</th>
                <th>상태</th>
                <th>등록일</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="sale in filteredSales" :key="sale.no">
                <td>{{ sale.no }}</td>
                <td>{{ sale.item }}</td>
                <td>{{ sale.price.toLocaleString() }}원</td>
                <td><span class="badge" :data-type="sale.state.type">{{ sale.state.text }}</span></td>
                <td>{{ sale.date }}</td>
                <td>
                  <button class="btn btn--sm" @click="viewSaleDetail(sale)">보기</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 빈 상태 -->
        <div v-else class="panel panel--center">
          <div class="empty-state">
            <p class="empty-message">판매 내역이 없습니다.</p>
            <button class="btn btn--primary" @click="goToSell">상품 판매하기</button>
          </div>
        </div>
      </div>

      <!-- 상세 화면 -->
      <div v-else class="detail-view">
        <div class="header">
          <div class="spacer"></div>
          <h1 class="title">판매 신청 내역</h1>
          <button class="btn-close" @click="goBackToList">
            <span>×</span>
          </button>
        </div>

        <!-- 완료 메시지 -->
        <div class="success-message">
          <h2 class="success-title">판매 신청이 완료되었습니다</h2>
          <p class="success-desc">아래 안내에 따라 물품을 보내주세요. 접수 후 상태가 단계별로 업데이트됩니다.</p>
        </div>

        <!-- 진행 단계 -->
        <div class="panel">
          <div class="progress-steps">
            <div 
              v-for="(step, index) in steps" 
              :key="step.key"
              class="step"
              :class="{ 'is-active': index === currentStep, 'is-completed': index < currentStep }"
            >
              <div class="step-circle">
                <span v-if="index < currentStep">✓</span>
                <span v-else>{{ index + 1 }}</span>
              </div>
              <p class="step-label">{{ step.label }}</p>
            </div>
          </div>
        </div>

        <!-- 입고 주소 -->
        <div class="panel">
          <h3 class="section-title">입고 주소</h3>
          <div class="address-info">
            <div class="info-row">
              <span class="label">받는 사람</span>
              <span class="value">{{ pickupInfo.receiver }}</span>
            </div>
            <div class="info-row">
              <span class="label">연락처</span>
              <span class="value">{{ pickupInfo.phone }}</span>
            </div>
            <div class="info-row">
              <span class="label">주소</span>
              <span class="value">{{ pickupInfo.address }}</span>
            </div>
            <button class="btn btn-copy" @click="copyAddress">주소 복사</button>
          </div>
        </div>

        <!-- 주의사항 -->
        <div class="panel">
          <div class="notice">
            <p>배송 후 <strong>마이페이지에서 배송상태를 갱신</strong>주세요.</p>
            <p>우리 사이트에 등록된 정보로 보내주세요.</p>
            <p>택배 파손 방지를 위해 완충재를 충분히 사용해 주세요.</p>
          </div>
        </div>

        <!-- 판매 상품 정보 -->
        <div class="panel">
          <h3 class="section-title">판매 상품 정보</h3>
          <div class="product-info">
            <img :src="selectedSale.image" :alt="selectedSale.item" class="product-image" />
            <div class="product-details">
              <h4 class="product-name">{{ selectedSale.item }}</h4>
              <p class="product-price">{{ selectedSale.price.toLocaleString() }}원</p>
              <div class="status-badge" :class="`status-${selectedSale.state.type}`">
                {{ selectedSale.state.text }}
              </div>
            </div>
          </div>
        </div>

        <!-- 하단 버튼 -->
        <div class="bottom-actions">
          <button class="btn btn-primary" @click="goBackToList">목록으로</button>
        </div>
      </div>

    </section>
  </main>
</template>

<script>
export default {
  name: 'MyPageSales',
  data() {
    return {
      showDetail: false,
      selectedSale: null,
      currentStep: 0,
      
      summary: { 
        total: 5, 
        selling: 2, 
        completed: 2, 
        cancelled: 1 
      },
      
      selectedStatus: 'all',
      searchKeyword: '',
      
      steps: [
        { key: 'apply', label: '신청 완료' },
        { key: 'confirm', label: '입고 확인' },
        { key: 'inspect', label: '검수 중' },
        { key: 'wait', label: '등록 대기중' },
        { key: 'register', label: '등록' }
      ],
      
      pickupInfo: {
        receiver: 'Dotori',
        phone: '02-1234-5678',
        address: '광주광역시 동구 조선대학로 196, 3층(광주빌딩)'
      },
      
      sales: [
        {
          no: 'S2025081201',
          item: '루피 피규어',
          price: 53000,
          state: { type: 'ing', text: '판매중' },
          date: '2025.08.12',
          image: 'https://via.placeholder.com/120x120'
        },
        {
          no: 'S2025080303',
          item: '미쿠 스페셜',
          price: 67000,
          state: { type: 'done', text: '거래 완료' },
          date: '2025.08.03',
          image: 'https://via.placeholder.com/120x120'
        },
        {
          no: 'S2025080201',
          item: '나루토 피규어',
          price: 45000,
          state: { type: 'ing', text: '판매중' },
          date: '2025.08.02',
          image: 'https://via.placeholder.com/120x120'
        },
        {
          no: 'S2025073105',
          item: '포켓몬 카드',
          price: 32000,
          state: { type: 'done', text: '거래 완료' },
          date: '2025.07.31',
          image: 'https://via.placeholder.com/120x120'
        },
        {
          no: 'S2025072801',
          item: '건담 모형',
          price: 89000,
          state: { type: 'cancelled', text: '판매 취소' },
          date: '2025.07.28',
          image: 'https://via.placeholder.com/120x120'
        }
      ]
    };
  },
  
  watch: {
    showDetail: {
      handler(newVal) {
        if (newVal) {
          // 상세 화면으로 전환될 때
          this.$nextTick(() => {
            window.scrollTo(0, 0);
            document.documentElement.scrollTop = 0;
            document.body.scrollTop = 0;
          });
        } else {
          // 목록 화면으로 전환될 때
          this.$nextTick(() => {
            window.scrollTo(0, 0);
            document.documentElement.scrollTop = 0;
            document.body.scrollTop = 0;
          });
        }
      },
      immediate: false
    }
  },
  
  computed: {
    filteredSales() {
      let filtered = this.sales;
      
      // 상태 필터링
      if (this.selectedStatus !== 'all') {
        filtered = filtered.filter(sale => sale.state.type === this.selectedStatus);
      }
      
      // 검색 키워드 필터링
      if (this.searchKeyword.trim()) {
        const keyword = this.searchKeyword.toLowerCase();
        filtered = filtered.filter(sale => 
          sale.no.toLowerCase().includes(keyword) ||
          sale.item.toLowerCase().includes(keyword)
        );
      }
      
      return filtered;
    }
  },
  
  methods: {
    viewSaleDetail(sale) {
      this.selectedSale = sale;
      
      // 판매 상태에 따라 currentStep 설정
      switch (sale.state.type) {
        case 'ing':
          this.currentStep = 0;
          break;
        case 'confirmed':
          this.currentStep = 1;
          break;
        case 'inspecting':
          this.currentStep = 2;
          break;
        case 'waiting':
          this.currentStep = 3;
          break;
        case 'done':
          this.currentStep = 4;
          break;
        default:
          this.currentStep = 0;
      }
      
      this.showDetail = true; // 이때 watch가 실행됨
    },
    
    goBackToList() {
      this.showDetail = false; // 이때 watch가 실행됨
      this.selectedSale = null;
    },
    
    copyAddress() {
      const fullAddress = `${this.pickupInfo.receiver}\n${this.pickupInfo.phone}\n${this.pickupInfo.address}`;
      navigator.clipboard.writeText(fullAddress).then(() => {
        alert('주소가 복사되었습니다.');
      });
    },
    
    goToSell() {
      // 판매 등록 페이지로 이동
      this.$router.push('/sell');
    }
  }
};
</script>

<style scoped>
.container { width: min(1160px, 92%); margin: 0 auto; padding-bottom: 120px; }

.section__title { margin: 22px 4px 12px; font-size: 22px; font-weight: 800; color: #2d251c; }
.section__title--center { text-align: center; }

.panel {
  background: #fff; 
  border: 1.5px solid #e9e4db; 
  border-radius: 12px; 
  padding: 20px;
  margin-bottom: 20px;
}
.panel--center { max-width: 980px; margin: 0 auto 20px; }

/* 요약 정보 */
.sales-summary {
  margin: 0; 
  padding: 20px 10px; 
  border: 1px solid #e9e4db;
  border-radius: 8px; 
  list-style: none; 
  display: grid; 
  grid-template-columns: repeat(4, 1fr); 
  background: #fff;
}
.sales-summary__item { text-align: center; padding: 10px 0; }
.sales-summary__label { 
  display: block; 
  font-weight: 800; 
  color: #2d251c; 
  margin-bottom: 8px; 
  font-size: 14px; 
}
.sales-summary__num { 
  display: inline-block; 
  font-weight: 800; 
  font-size: 18px; 
  color: #000; 
}

/* 필터 섹션 */
.filter-section {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 120px;
}
.filter-label {
  font-size: 13px;
  font-weight: 600;
  color: #666;
  white-space: nowrap;
}
.filter-select {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  min-height: 38px;
}
.search-input {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  height: 22px;
  width: 200px;
}

/* 테이블 */
.tbl { width: 100%; border-collapse: collapse; }
.tbl thead th {
  background: #fafafa; 
  padding: 14px 10px; 
  border-bottom: 1px solid #eee; 
  font-weight: 700; 
  font-size: 14px;
  text-align: center;
}
.tbl td { 
  border-bottom: 1px solid #eee6d7; 
  padding: 12px 10px; 
  text-align: center;
}

/* 상태 뱃지 */
.badge { 
  padding: 6px 12px; 
  border-radius: 20px; 
  font-size: 12px; 
  font-weight: 600;
  border: 1px solid #e4d8c3; 
}
.badge[data-type="ing"] { background: #fff3cd; border-color: #ffeaa7; color: #856404; }
.badge[data-type="done"] { background: #d4edda; border-color: #c3e6cb; color: #155724; }
.badge[data-type="cancelled"] { background: #f8d7da; border-color: #f5c6cb; color: #721c24; }

/* 버튼 */
.btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 13px;
  white-space: nowrap;
}
.btn--sm {
  padding: 6px 12px;
  font-size: 12px;
}
.btn--primary {
  background: #fc703c;
  color: #fff;
  border-color: #fc703c;
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
}
.empty-message {
  margin: 0 0 20px 0;
  color: #666;
  font-size: 16px;
}

/* 모바일 대응 */
@media (max-width: 640px) {
  .sales-summary { grid-template-columns: repeat(2, 1fr); row-gap: 8px; }
  .filter-section { flex-direction: column; align-items: stretch; }
  .search-input { min-width: auto; }
  .panel--center { max-width: 100%; }
}

/* 상세보기 화면 스타일 */
.detail-view {
  max-width: 600px;
  margin: 0 auto;
}

/* 헤더 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 30px 0;
  padding: 0 10px;
}

.spacer {
  width: 40px;
}

.btn-close {
  width: 30px;
  height: 30px;
  margin-bottom: 10px;
  border: none;
  background: #f4f3e6;
  border: 1px solid #e5dcc9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 20px;
  color: #7b6d5d;
  font-weight: 600;
}

.btn-close:hover {
  background: #e9e4db;
}

.title {
  font-size: 20px;
  font-weight: 800;
  margin: 0;
  color: #2d251c;
}

/* 성공 메시지 */
.success-message {
  text-align: center;
  margin-bottom: 30px;
}

.success-title {
  font-size: 20px;
  font-weight: 800;
  color: #2d251c;
  margin: 0 0 12px 0;
}

.success-desc {
  color: #7b6d5d;
  margin: 0;
  line-height: 1.5;
  font-size: 14px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 16px 0;
  color: #333;
}

/* 진행 단계 */
.progress-steps {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.progress-steps::before {
  content: '';
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  height: 2px;
  background: #e9e4db;
  z-index: 1;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  position: relative;
  z-index: 2;
}

.step-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e9e4db;
  color: #7b6d5d;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.step.is-active .step-circle {
  background: #fc703c;
  color: white;
}

.step.is-completed .step-circle {
  background: #38b36f;
  color: white;
}

.step-label {
  font-size: 11px;
  color: #7b6d5d;
  text-align: center;
  margin: 0;
  white-space: nowrap;
}

.step.is-active .step-label {
  color: #fc703c;
  font-weight: 600;
}

/* 주소 정보 */
.address-info {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.info-row:last-of-type {
  margin-bottom: 16px;
}

.label {
  font-weight: 600;
  color: #333;
  min-width: 60px;
}

.value {
  color: #666;
  flex: 1;
  text-align: right;
}

.btn-copy {
  width: 100%;
  padding: 12px;
  background: #ffff;
  color: black;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  font-size: 14px;
}

/* 주의사항 */
.notice {
  background: #fff3cd;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #ffc107;
}

.notice p {
  margin: 0 0 8px 0;
  color: #856404;
  font-size: 14px;
  line-height: 1.4;
}

.notice p:last-child {
  margin-bottom: 0;
}

/* 상품 정보 */
.product-info {
  display: flex;
  gap: 16px;
  align-items: center;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.product-details {
  flex: 1;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #333;
}

.product-price {
  font-size: 18px;
  font-weight: 700;
  color: #ff6b35;
  margin: 0 0 8px 0;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
}

.status-ing {
  background: #fff3cd;
  color: #856404;
}

.status-done {
  background: #d4edda;
  color: #155724;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
}

/* 하단 버튼 */
.bottom-actions {
  margin-top: 30px;
}

.btn-primary {
  background: #fc703c;
  color: white;
  width: 100%;
  border: none;
  border-radius: 8px;
  padding: 14px 28px;
  font-weight: 700;
  cursor: pointer;
  font-size: 16px;
}

.btn-primary:hover {
  background: #e5633a;
}
</style>