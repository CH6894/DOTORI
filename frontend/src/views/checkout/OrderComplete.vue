<!-- src/views/checkout/OrderComplete.vue -->
<template>
  <div class="order-complete">
    <!-- 상단 아이콘 + 완료 문구 -->
    <div class="hero">
      <img class="hero__icon" :src="iconSrc" alt="주문 완료 아이콘" />
      <h1 class="hero__title">주문이 완료되었습니다.</h1>
      <p class="hero__meta">
        주문번호 : <span class="order-no">{{ orderNo }}</span>
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
        <div class="row" v-for="it in items" :key="it.id">
          <div class="thumb">
            <img :src="it.thumb" alt="" />
          </div>
          <div class="info">
            <div class="name">{{ it.name }}</div>
            <div class="meta">{{ it.desc }}</div>
          </div>
          <div>{{ it.qty }}</div>
          <div>{{ fmt(it.discount) }}원</div>
          <div class="right"><b>{{ fmt(it.price * it.qty - it.discount) }}원</b></div>
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
            <div class="dd">{{ ship.receiver }}</div>
            <div class="dt">휴대폰 번호</div>
            <div class="dd">{{ ship.phone }}</div>
            <div class="dt">주소</div>
            <div class="dd">[{{ ship.postcode }}] {{ ship.addr1 }}<br />{{ ship.addr2 }}</div>
          </div>
        </div>

        <div class="panel">
          <h3 class="section-title">결제 정보</h3>
          <div class="dl">
            <div class="dt">상품 금액</div>
            <div class="dd">{{ fmt(subtotal) }}원</div>
            <div class="dt">할인 금액</div>
            <div class="dd">{{ fmt(discount) }}원</div>
            <div class="dt">배송비</div>
            <div class="dd">{{ shippingFee === 0 ? '무료' : fmt(shippingFee) + '원' }}</div>
            <div class="dt total">총 결제 금액</div>
            <div class="dd total"><b>{{ fmt(total) }}원</b></div>
          </div>
        </div>
      </div>
    </section>

    <!-- 하단 액션 -->
    <div class="actions">
      <router-link class="btn outline" :to="{ name: 'login' }">주문 상세보기</router-link>
      <router-link class="btn primary" :to="{ name: 'checkout' }">쇼핑 계속하기</router-link>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
/* 아이콘 이미지는 프로젝트에 있는 걸로 교체해도 됨 */
import icon from '../../assets/cart.svg' // 없으면 다른 이미지 경로로 바꿔도 OK

// ====== 목업 데이터 (페이지 먼저 확인용) ======
const iconSrc = icon
const orderNo = '1234567890'
const items = reactive([
  { id: 'sku-1', name: '상품 A', price: 90000, qty: 1, discount: 0, thumb: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTz2BqmHnvVTX6iSjug5pt1h4ehgaX_GHat-Q&s' },
  { id: 'sku-2', name: '상품 B', price: 45000, qty: 3, discount: 0, thumb: 'https://images.unsplash.com/photo-1549068106-b024baf5062d?w=400&q=80&auto=format&fit=crop' }
])
const ship = {
  receiver: '구창회',
  phone: '010-0000-0000',
  postcode: '06236',
  addr1: '서울 강남구 테헤란로 000',
  addr2: 'OOO빌딩 10층'
}
const shippingFee = ref(0)

const discount = computed(() => items.reduce((s, it) => s + it.discount, 0))
const subtotal = computed(() => items.reduce((s, it) => s + it.price * it.qty, 0))
const total = computed(() => subtotal.value - discount.value + shippingFee.value)

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
  justify-content:flex-end;
  gap: 10px;
  margin-top: 20px;
  width:122%;
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

  .info-grid{ grid-template-columns: 1fr; }
}
</style>
