<!-- src/views/checkout/OrderComplete.vue -->
<template>
  <div class="order-complete">
    <!-- 상단 아이콘 + 완료 문구 -->
    <div class="hero">
      <img class="hero__icon" :src="iconSrc" alt="주문 완료 아이콘" />
      <h1 class="hero__title">주문이 완료되었습니다.</h1>
      <p class="hero__meta">
        주문번호 : <span class="order-no">{{ orderData.orderNo }}</span>
      </p>
      <p class="hero__date">
        주문일시 : {{ formatDate(orderData.orderDate) }}
      </p>
    </div>

    <!-- 주문 상품 표 -->
    <section class="card wide">
      <h2 class="section-title">주문 상품</h2>
      <div class="thead row">
        <div>상품 이미지</div>
        <div>상품 정보</div>
        <div>수량</div>
        <div>할인 금액</div>
        <div>결제 금액</div>
      </div>
      <div class="tbody">
        <div class="row" v-for="it in orderData.items" :key="it.id">
          <div class="thumb">
            <img :src="it.thumb" alt="" />
          </div>
          <div class="info">
            <div class="name">{{ it.name }}</div>
            <div class="meta">{{ it.desc || it.option || '' }}</div>
          </div>
          <div>{{ it.qty }}</div>
          <div>{{ fmt(it.discount || 0) }}원</div>
          <div class="right"><b>{{ fmt(it.price * it.qty - (it.discount || 0)) }}원</b></div>
        </div>
      </div>
    </section>

    <!-- 배송/결제 정보 -->
    <section class="card wide">
      <div class="info-grid">
        <div class="panel">
          <h3 class="section-title">배송지 정보</h3>
          <div class="dl">
            <div class="dt">이름</div>
            <div class="dd">{{ orderData.address.receiver }}</div>
            <div class="dt">휴대폰 번호</div>
            <div class="dd">{{ orderData.address.phone }}</div>
            <div class="dt">주소</div>
            <div class="dd">[{{ orderData.address.postcode }}] {{ orderData.address.addr1 }}<br />{{
              orderData.address.addr2 }}</div>
            <div v-if="orderData.note" class="dt">배송 요청사항</div>
            <div v-if="orderData.note" class="dd">{{ orderData.note }}</div>
          </div>
        </div>

        <div class="panel">
          <h3 class="section-title">결제 정보</h3>
          <div class="dl">
            <div class="dt">결제 수단</div>
            <div class="dd">{{ getPaymentMethodText(orderData.payMethod) }}</div>
            <div v-if="orderData.payMethod === 'bank'" class="dt">입금자명</div>
            <div v-if="orderData.payMethod === 'bank'" class="dd">{{ orderData.depositorName }}</div>
            <div class="dt">상품 금액</div>
            <div class="dd">{{ fmt(orderData.subtotal) }}원</div>
            <div class="dt">할인 금액</div>
            <div class="dd">{{ fmt(orderData.discount) }}원</div>
            <div class="dt">배송비</div>
            <div class="dd">{{ orderData.shippingFee === 0 ? '무료' : fmt(orderData.shippingFee) + '원' }}</div>
            <div class="dt total">총 결제 금액</div>
            <div class="dd total"><b>{{ fmt(orderData.total) }}원</b></div>
          </div>
        </div>
      </div>
    </section>

    <!-- 하단 액션 -->
    <div class="actions">
      <router-link class="btn outline" :to="{ name: 'mypage' }">주문 상세보기</router-link>
      <router-link class="btn primary" :to="{ name: 'main' }">쇼핑 계속하기</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
/* 아이콘 이미지는 프로젝트에 있는 걸로 교체해도 됨 */
import icon from '@/assets/cart.svg' // 없으면 다른 이미지 경로로 바꿔도 OK

const router = useRouter()

// ====== 주문 데이터 불러오기 ======
const orderData = ref({
  items: [],
  address: {
    receiver: '',
    phone: '',
    postcode: '',
    addr1: '',
    addr2: ''
  },
  note: '',
  payMethod: '',
  selectedBank: null,
  depositorName: '',
  subtotal: 0,
  discount: 0,
  shippingFee: 0,
  total: 0,
  orderDate: new Date().toISOString(),
  orderNo: ''
})

onMounted(() => {
  const savedOrderData = localStorage.getItem('dotori_order_data')
  if (savedOrderData) {
    try {
      orderData.value = JSON.parse(savedOrderData)
    } catch (e) {
      console.error('주문 데이터 파싱 오류:', e)
      // 기본값으로 폴백
      orderData.value = {
        items: [],
        address: { receiver: '', phone: '', postcode: '', addr1: '', addr2: '' },
        note: '',
        payMethod: '',
        selectedBank: null,
        depositorName: '',
        subtotal: 0,
        discount: 0,
        shippingFee: 0,
        total: 0,
        orderDate: new Date().toISOString(),
        orderNo: '주문 데이터 없음'
      }
    }
  } else {
    // 주문 데이터가 없으면 장바구니로 리다이렉트
    router.push({ name: 'cart' })
    return
  }
})

const iconSrc = icon

// 결제 수단 텍스트 변환
const getPaymentMethodText = (method) => {
  const methodMap = {
    'bank': '무통장 입금',
    'easy': '간편 결제',
    'card': '카드 결제',
    'mobile': '휴대폰 결제'
  }
  return methodMap[method] || method
}

// 날짜 포맷팅
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const fmt = (n) => n.toLocaleString('ko-KR')
</script>

<style scoped>
/* Checkout 페이지와 톤/폭을 맞춤 */
.order-complete {
  max-width: 720px;
  margin: 24px auto 120px;
  /* 하단 여백: 고정 결제바와 동일 높이 */
  padding: 0 16px;
}

/* 상단 히어로 */
.hero {
  text-align: center;
  margin: 8px 0 8px;
}

.hero__icon {
  width: 85px;
  height: 85px;
  object-fit: contain;
  display: block;
  margin: 16px auto 10px;
}

.hero__title {
  font-size: 18px;
  margin: 0;
}

.hero__meta {
  margin-top: 6px;
  color: #ff7a2e;
  font-size: 13px;
}

.hero__date {
  margin-top: 4px;
  color: #666;
  font-size: 12px;
}

.order-no {
  font-weight: 700;
}

/* 카드 공통 (Checkout과 동일한 중앙정렬/폭) */
.card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, .04);
  padding: 16px;
  margin-bottom: 16px;
  width: 100%;
}

.card.wide {
  width: 140%;
  position: relative;
  left: 50%;
  transform: translateX(-50%);
}

.section-title {
  font-size: 16px;
  margin: 0 0 12px;
}

/* 표 레이아웃 */
.thead,
.row {
  display: grid;
  grid-template-columns: 100px 1fr 80px 120px 140px;
  align-items: center;
  gap: 12px;
}

.thead {
  font-weight: 700;
  color: #666;
  padding: 6px 2px;
  border-bottom: 1px solid #eee;
}

.tbody .row {
  padding: 10px 2px;
  border-bottom: 1px solid #f2f2f2;
}

.thumb img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
}

.info .name {
  font-weight: 700;
  line-height: 1.4;
}

.info .meta {
  color: #8a8a8a;
  font-size: 13px;
  margin-top: 2px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.right {
  text-align: right;
}

/* 2단 그리드 */
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 16px;
}

.dl {
  display: grid;
  grid-template-columns: 110px 1fr;
  column-gap: 12px;
  row-gap: 10px;
  align-items: baseline;
}

.dt {
  color: #333;
  font-weight: 700;
}

.dd {
  color: #222;
  line-height: 1.6;
}

.total {
  border-top: 1px solid #eee;
  padding-top: 8px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  width: 122%;
}

.btn {
  border-radius: 10px;
  padding: 10px 14px;
  cursor: pointer;
  border: 1px solid #ddd;
}

.btn.primary {
  background: #ff7a2e;
  color: #fff;
  border-color: #ff7a2e;
  text-decoration: none;
}

.btn.outline {
  background: #fff;
  color: #000;
  text-decoration: none;
}

/* 반응형 */
@media (max-width: 720px) {

  .thead,
  .row {
    grid-template-columns: 80px 1fr 60px 100px 1fr;
  }

  .grid {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
