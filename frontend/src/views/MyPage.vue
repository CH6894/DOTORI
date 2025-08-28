<!-- src/views/MyPage.vue -->
<template>
  <main class="mypage">
    <!-- 헤더: 제목 + 탭 -->
    <section class="mypage__head container">
      <h1 class="mypage__title">마이페이지</h1>

      <nav class="tabs" role="tablist" aria-label="마이페이지 탭">
        <button v-for="t in tabs" :key="t.key" class="tab" :class="{ 'is-active': currentTab === t.key }" role="tab"
          :aria-selected="currentTab === t.key" @click="setTab(t.key)">
          {{ t.label }}
        </button>
      </nav>
    </section>

    <!-- ✅ 자식 라우트가 들어올 자리 (예: /mypage/orders 등) -->
    <section class="container" v-if="isChildRoute">
      <router-view />
    </section>

    <!-- 본문 (자식 라우트가 아닐 때만 보임) -->
    <section class="container" v-else>
      <!-- 내 정보 탭 -->
      <div v-show="currentTab === 'profile'">
        <!-- 프로필 카드 -->
        <div class="panel profile-card">
          <div class="card__tools">
            <button v-if="!isEditing" class="link-edit" @click="startEdit">회원정보 수정</button>
            <div v-else class="edit-actions">
              <button class="btn btn--ghost btn--sm" @click="cancelEdit">취소</button>
              <button class="btn btn--sm" @click="saveProfile">저장</button>
            </div>
          </div>

          <!-- 프로필 본문 -->
          <div class="profile">
            <div class="profile__avatar">
              <img :src="preview || user.photo" alt="프로필" />
            </div>

            <!-- 보기 / 편집 전환 -->
            <div v-if="!isEditing" class="profile__info">
              <p>
                <span class="profile__label">닉네임</span>
                <span class="profile__nickname">{{ user.nickname }}</span>
              </p>
              <p>
                <span class="profile__label">아이디</span>
                <span class="profile__userid">{{ user.userId }}</span>
              </p>
            </div>

            <form v-else class="profile__form" @submit.prevent="saveProfile">
              <label class="profile__row">
                <span class="profile__label">닉네임</span>
                <input v-model.trim="form.nickname" placeholder="닉네임" />
              </label>
              <label class="profile__row">
                <span class="profile__label">아이디</span>
                <input v-model.trim="form.userId" placeholder="아이디" />
              </label>
            </form>
          </div>

          <!-- 사진 변경 버튼 -->
          <div class="profile__photoRow">
            <input ref="file" type="file" accept="image/*" class="hidden" @change="onPick" />
            <button class="btn btn--ghost" @click="triggerPick">프로필사진 변경</button>
          </div>
        </div>

        <!-- 주문 배송 현황 -->
        <h2 class="section__title link" @click="go('ShipPage')">주문 배송 현황</h2>
        <div class="panel">
          <ol class="steps steps--flat">
            <li v-for="(s, i) in orderSteps" :key="s.key" class="step" :class="{ 'is-done': i <= orderProgressIndex }">
              <img :src="s.icon" :alt="s.label" class="step__icon" />
              <div v-if="i < orderSteps.length - 1" class="step__arrow" :class="{ 'is-on': i < orderProgressIndex }"
                aria-hidden="true">›</div>
              <p class="step__label">{{ s.label }}</p>
            </li>
          </ol>
        </div>

        <!-- 주문 내역 -->
        <h2 class="section__title link" @click="go('OrdersPage')">주문 내역</h2>
        <div class="panel">
          <ul class="order-summary">
            <li class="order-summary__item">
              <span class="order-summary__label">전체</span>
              <span class="order-summary__num">{{ orderSummary.total }}</span>
            </li>
            <li class="order-summary__item">
              <span class="order-summary__label">입금중</span>
              <span class="order-summary__num">{{ orderSummary.deposit }}</span>
            </li>
            <li class="order-summary__item">
              <span class="order-summary__label">진행중</span>
              <span class="order-summary__num">{{ orderSummary.progress }}</span>
            </li>
            <li class="order-summary__item">
              <span class="order-summary__label">종료</span>
              <span class="order-summary__num">{{ orderSummary.done }}</span>
            </li>
          </ul>
        </div>

        <!-- 판매 내역 -->
        <h2 class="section__title link" @click="go('SalesPage')">판매 내역</h2>
        <div class="card">
          <template v-if="sales && sales.length">
            <table class="tbl">
              <thead>
                <tr>
                  <th>판매번호</th>
                  <th>상품</th>
                  <th>금액</th>
                  <th>상태</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="s in sales" :key="s.no">
                  <td>{{ s.no }}</td>
                  <td>{{ s.item }}</td>
                  <td>{{ s.price.toLocaleString() }}원</td>
                  <td><span class="badge" :data-type="s.state.type">{{ s.state.text }}</span></td>
                </tr>
              </tbody>
            </table>
          </template>
          <template v-else>
            <p class="muted center">등록된 판매내역이 없습니다.</p>
            <button class="btn btn--wide" @click="go('SalesPage')">판매하기</button>
          </template>
        </div>

        <!-- 보관중인 상품 -->
        <h2 class="section__title link" @click="go('StoragePage')">보관중인 상품</h2>
        <div class="card">
          <template v-if="storageItems && storageItems.length">
            <ul class="storage">
              <li v-for="it in storageItems" :key="it.id" class="storage__row">
                <img :src="it.image" :alt="it.title" class="storage__thumb" />
                <div class="storage__meta">
                  <p class="storage__title">{{ it.title }}</p>
                  <p class="storage__sub">{{ it.price.toLocaleString() }}원</p>
                </div>
                <div class="storage__actions">
                  <button class="btn btn--ghost btn--sm" @click="go('StoragePage')">상세</button>
                  <button class="btn btn--sm" @click="removeStorage(it.id)">해제</button>
                </div>
              </li>
            </ul>
          </template>
          <template v-else>
            <p class="muted center">보관함이 비어 있습니다.</p>
            <button class="btn btn--wide" @click="go('StoragePage')">보관함 열기</button>
          </template>
        </div>

        <!-- 위시리스트 -->
        <h2 class="section__title link" @click="go('WishPage')">위시리스트</h2>
        <div class="panel">
          <ul class="wish">
            <li v-for="w in wishlist" :key="w.id" class="wish__item">
              <figure class="wish__fig">
                <img :src="w.image" :alt="w.title" />
              </figure>
              <div class="wish__meta">
                <p class="wish__title">{{ w.title }}</p>
                <p class="wish__sub">{{ w.price.toLocaleString() }}원</p>
              </div>
              <div class="wish__actions">
                <button class="btn btn--ghost btn--thin" @click="go('WishPage')">상세</button>
                <button class="btn btn--thin" @click="addToCart(w.id)">장바구니</button>
              </div>
            </li>
          </ul>
        </div>
      </div>
      <!-- 문의 탭 -->
      <div v-show="currentTab === 'qna'" class="card">
        <h2 class="card__title">문의 내역</h2>
        <p class="muted">문의 리스트/상세가 이 영역에 표시됩니다.</p>
      </div>
    </section>

    <!-- 토스트 메시지 (항상 최상위에서 노출) -->
    <div v-if="toast.open" class="toast">
      장바구니에 담겼습니다
    </div>
  </main>
</template>

<script>
import { useOrderStore } from '@/stores/orders'

import defaultPhoto from '@/assets/profile_phto.svg'
import iconOrder from '@/assets/주문.png'
import iconPay from '@/assets/결제.png'
import iconPrep from '@/assets/준비.png'
import iconShip from '@/assets/배송.png'
import iconDone from '@/assets/완료.png'

/* 위시리스트 이미지들 */
import imgRengokuKeyring from '@/assets/list/렌고쿠 코쥬로 키링.svg'
import imgAkazaDoll from '@/assets/list/아카자 인형.svg'
import imgKozumeFigure from '@/assets/list/코즈메 켄마 피규어.svg'
import imgMikuFigure from '@/assets/list/하츠네 미쿠 피규어.svg'
import imgHinataSoyoFigure from '@/assets/list/히나타 소요 피규어.svg'

/* ✅ 라우터 이름 매핑 */
const ROUTE_NAME_MAP = {
  ShipPage: 'mypage-ship',
  OrdersPage: 'mypage-orders',
  SalesPage: 'mypage-sales',
  StoragePage: 'mypage-storage',
  WishPage: 'mypage-wish',
}

export default {
  name: 'MyPage',

  data() {
    return {
      /* 탭(토글): URL과 무관한 로컬 상태 */
      currentTab: 'profile',
      tabs: [
        { key: 'profile', label: '내 정보' },
        { key: 'qna', label: '문의내역' },
      ],

      /* 프로필 */
      isEditing: false,
      user: { photo: defaultPhoto, nickname: '다람이', userId: 'user01' },
      form: { nickname: '다람이', userId: 'user01' },
      preview: '',

      /* 주문 단계 */
      orderSteps: [
        { key: 'order', label: '주문접수', icon: iconOrder },
        { key: 'payment', label: '결제 완료', icon: iconPay },
        { key: 'prep', label: '상품 준비', icon: iconPrep },
        { key: 'ship', label: '배송중', icon: iconShip },
        { key: 'done', label: '배송 완료', icon: iconDone },
      ],
      orderProgressIndex: 2,

      /* 판매 내역 (더미) */
      sales: [
        { no: 'S2025081201', item: '루피 피규어', price: 53000, state: { type: 'ing', text: '판매중' } },
        { no: 'S2025080303', item: '미쿠 스페셜', price: 67000, state: { type: 'done', text: '거래 완료' } },
      ],

      /* 보관함 (더미) */
      storageItems: [
        { id: 'st1', title: '렌고쿠 코쥬로 키링', image: imgRengokuKeyring, price: 10000 },
        { id: 'st2', title: '아카자 인형', image: imgAkazaDoll, price: 48000 },
      ],

      /* 위시리스트 (더미) */
      wishlist: [
        { id: 'w1', title: '렌고쿠 코쥬로 키링', image: imgRengokuKeyring, price: 10000 },
        { id: 'w2', title: '아카자 인형', image: imgAkazaDoll, price: 48000 },
        { id: 'w3', title: '코즈메 켄마 피규어', image: imgKozumeFigure, price: 59000 },
        { id: 'w4', title: '하츠네 미쿠 피규어', image: imgMikuFigure, price: 53000 },
        { id: 'w5', title: '히나타 소요 피규어', image: imgHinataSoyoFigure, price: 67000 },
      ],

      toast: { open: false, _t: null },

      /* Pinia store */
      store: useOrderStore(),
    }
  },

  computed: {
    /* 자식 라우트 판별: /mypage의 children이면 true */
    isChildRoute() {
      const n = this.$route.name || ''
      return n.startsWith('mypage-') && n !== 'mypage'
    },
    /* 주문 요약(스토어) */
    orderSummary() {
      return this.store?.orderSummary ?? { total: 0, deposit: 0, progress: 0, done: 0 }
    },
  },

  methods: {
    /* 탭 변경: URL 건드리지 않음 */
    setTab(key) {
      if (this.currentTab === key) return
      this.currentTab = key
    },

    // 프로필 관련
    triggerPick() { this.$refs.file?.click() },
    onPick(e) {
      const f = e.target.files?.[0]
      if (!f) return
      const reader = new FileReader()
      reader.onload = () => (this.preview = reader.result)
      reader.readAsDataURL(f)
    },
    startEdit() {
      this.form.nickname = this.user.nickname
      this.form.userId = this.user.userId
      this.isEditing = true
    },
    cancelEdit() {
      this.isEditing = false
      this.preview = ''
      if (this.$refs.file) this.$refs.file.value = ''
    },
    saveProfile() {
      this.user.nickname = this.form.nickname || this.user.nickname
      this.user.userId = this.form.userId || this.user.userId
      if (this.preview) this.user.photo = this.preview
      this.isEditing = false
      this.preview = ''
      if (this.$refs.file) this.$refs.file.value = ''
      alert('저장되었습니다!')
    },

    /* ✅ 섹션 타이틀 클릭 시 자식 라우트로 이동 (기존 유지) */
    go(name) {
      const real = ROUTE_NAME_MAP[name]
      if (real) {
        this.$router.push({ name: real })
        return
      }
      console.warn(`[MyPage] 알 수 없는 이동 키: ${name}. /mypage로 이동합니다.`)
      this.$router.push('/mypage')
    },

    // 보관함
    removeStorage(id) {
      this.storageItems = this.storageItems.filter(it => it.id !== id)
    },

    // 장바구니 관련
    currency(n) {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
        maximumFractionDigits: 0
      }).format(n)
    },
    getCart() {
      try {
        const raw = localStorage.getItem('dotori_cart_v1')
        return raw ? JSON.parse(raw) : []
      } catch (e) { return [] }
    },
    saveCart(list) { localStorage.setItem('dotori_cart_v1', JSON.stringify(list)) },
    upsert(cart, item) {
      const i = cart.findIndex(x => x.id === item.id)
      if (i >= 0) cart[i].qty += item.qty
      else cart.push(item)
      return cart
    },
    showToast() {
      this.toast.open = true
      clearTimeout(this.toast._t)
      this.toast._t = setTimeout(() => { this.toast.open = false }, 2000)
    },
    addToCart(id) {
      const w = this.wishlist.find(x => x.id === id)
      if (!w) { this.showToast(); return }
      const item = { id: w.id, title: w.title, price: w.price, qty: 1, shipping: 0, thumb: w.image }
      const next = this.upsert(this.getCart(), item)
      this.saveCart(next)
      this.showToast()
    },
  },
}
</script>


<style scoped>
/* ---- (기존 스타일 그대로) ---- */
.container {
  width: min(1160px, 92%);
  margin: 0 auto;
}

.mypage {
  color: #2d251c;
  letter-spacing: -0.1px;
}

.mypage__head {
  padding-top: 18px;
  text-align: center;
}

.mypage__title {
  margin: 16px 0 12px;
  font-size: 28px;
  font-weight: 800;
}

/* 탭 */
.tabs {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  width: min(420px, 100%);
  margin: 0 auto;
  border-bottom: 2px solid #e5dcc9;
  gap: 0; /* 필요시 간격 조절 */
}
.tab {
  appearance: none;
  background: transparent;
  border: 0;
  padding: 12px 8px;
  font-weight: 700;
  color: #7b6d5d;
  position: relative;
  cursor: pointer;
  text-align: center;
}

.tab.is-active {
  color: #2d251c;
}

.tab.is-active::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: -2px;
  height: 2px;
  background: #2d251c;
}

/* 패널 및 카드 */
.card {
  margin-top: 1.5rem;
  padding: 22px;
  background: #fff;
  border: 1px solid #e8e1d4;
  border-radius: 10px;
}

.card__title {
  margin: 0 0 14px;
  font-size: 16px;
  font-weight: 800;
}

.panel {
  background: #fff;
  border: 1.5px solid #e9e4db;
  border-radius: 12px;
  padding: 16px 18px;
}

.section__title {
  margin: 22px 4px 10px;
  font-size: 16px;
  font-weight: 800;
  color: #2d251c;
}

.section__title.link {
  cursor: pointer;
}

.section__title.link:hover {
  text-decoration: underline;
}

.muted {
  color: #7b6d5d;
}

.center {
  text-align: center;
}

/* 프로필 카드 */
.profile-card {
  margin-top: 1.5rem;
  position: relative;
}

.card__tools {
  position: absolute;
  top: 10px;
  right: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.link-edit {
  background: transparent;
  border: 0;
  font-size: 12px;
  color: #7b6d5d;
  cursor: pointer;
}

.edit-actions .btn--sm {
  padding: 6px 10px;
}

.profile {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile__avatar {
  width: 80px;
  height: 80px;
  border: 1px solid #e5dcc9;
  border-radius: 50%;
  overflow: hidden;
  display: grid;
  place-items: center;
  background: #fff;
}

.profile__avatar img {
  width: 80%;
  height: 80%;
  object-fit: cover;
}

.profile__label {
  font-size: 14px;
  color: #8f8577;
  margin-right: 6px;
}

.profile__nickname {
  font-size: 18px;
  font-weight: 800;
  color: #2d251c;
}

.profile__userid {
  font-size: 14px;
  font-weight: 600;
  color: #5c5346;
}

.profile__form {
  max-width: 360px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.profile__row {
  display: flex;
  flex-direction: column;
}

.profile__form .profile__label {
  font-size: 12px;
  color: #8f8577;
  margin-bottom: 4px;
}

.profile__form input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e5dcc9;
  border-radius: 10px;
}

.profile__photoRow {
  position: absolute;
  right: 16px;
  bottom: 16px;
}

.hidden {
  display: none;
}

/* 버튼 */
.btn {
  appearance: none;
  border: 0;
  background: #f4f4f4;
  color: black;
  padding: 10px 14px;
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
}

.btn--ghost {
  background: #f4f3e6;
  color: #5f5346;
  border: 1px solid #d9d9d9;
}

.btn--wide {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d9d9d9;
}

.btn--sm {
  padding: 6px 10px;
  border-radius: 8px;
  background: #fff7ea;
  color: #332b22;
  border: 1px solid #e3d7c2;
  margin-left: 5px;
}

.btn--thin {
  padding: 6px 10px;
  border-radius: 7px;
  border: 1px solid #d9d9d9;
}

/* 주문 진행 단계 */
:root {
  --line: #e9e4db;
  --green: #f4f4f4;
}

.steps--flat {
  margin: 8px 0 0;
  padding: 12px 10px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  align-items: center;
}

.step {
  position: relative;
  display: grid;
  grid-template-rows: 28px auto;
  justify-items: center;
  padding: 6px 0 2px;
}

.step__arrow {
  position: absolute;
  top: 3px;
  right: -2px;
  color: #d9d4cc;
  font-size: 22px;
  line-height: 22px;
  transition: color .2s;
}

.step__arrow.is-on {
  color: var(--green);
}

.step__label {
  margin-top: 1rem;
  font-size: 12px;
  color: #8f8577;
}

.step.is-done .step__label {
  color: #5c5246;
  font-weight: 700;
}

.step__icon {
  width: 40px;
  height: 40px;
  object-fit: contain;
  filter: grayscale(100%) opacity(0.6);
  transition: filter .2s ease;
}

.step.is-done .step__icon {
  filter: none;
}

/* 주문 요약 */
.order-summary {
  margin: 8px 0 0;
  padding: 14px 10px;
  border: 1px solid var(--line);
  border-radius: 8px;
  list-style: none;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: #fff;
}

.order-summary__item {
  text-align: center;
  padding: 10px 0;
}

.order-summary__label {
  display: block;
  font-weight: 800;
  color: #2d251c;
  margin-bottom: 6px;
  font-size: 14px;
}

.order-summary__num {
  display: inline-block;
  font-weight: 800;
  font-size: 16px;
  color: #000;
}

/* 테이블 */
.tbl {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  border-bottom: 1px solid #eee6d7;
  padding: 10px 8px;
  text-align: left;
}

.badge {
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid #e4d8c3;
}

.badge[data-type="ing"] {
  background: #fff7ea;
}

.badge[data-type="done"] {
  background: #e9f7ef;
  border-color: #cfe9d9;
}

/* 보관함 */
.storage {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.storage__row {
  display: grid;
  grid-template-columns: 64px 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 6px 4px;
  border-bottom: 1px solid #eee6d7;
}

.storage__thumb {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 8px;
  background: #f4f3e6;
  border: 1px solid #e8e1d4;
}

.storage__title {
  font-weight: 800;
}

.storage__sub {
  margin-top: 2px;
  font-size: 12px;
  color: #7b6d5d;
}

.storage__actions {
  display: flex;
  gap: 8px;
}

/* 위시리스트 */
.wish {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
  list-style: none;
  padding: 0;
  margin: 0;
}

.wish__item {
  background: #fff;
  border: 1px solid #e8e1d4;
  border-radius: 10px;
  overflow: hidden;
  display: grid;
  grid-template-rows: 200px auto auto;
}

.wish__fig {
  background: #f4f3e6;
  display: grid;
  place-items: center;
  width: 185px;
  height: 200px;
  margin: 0 auto;
}

.wish__fig img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.wish__meta {
  padding: 20px 12px 5px;
  margin-top: 0;
}

.wish__title {
  font-weight: 800;
  margin-bottom: 4px;
}

.wish__sub {
  font-size: 12px;
  color: #7b6d5d;
}

.wish__actions {
  display: flex;
  gap: 6px;
  padding: 8px 8px 8px;
}

/* 토스트 */
.toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #f4f4f4;
  color: #7b6d5d;
  padding: 14px 20px;
  border-radius: 12px;
  font-weight: 600;
  text-align: center;
  z-index: 9999;
  animation: fadeInOut 2.2s ease forwards;
}

@keyframes fadeInOut {
  0% {
    opacity: 0;
    transform: translate(-50%, -60%);
  }

  15% {
    opacity: 1;
    transform: translate(-50%, -50%);
  }

  85% {
    opacity: 1;
    transform: translate(-50%, -50%);
  }

  100% {
    opacity: 0;
    transform: translate(-50%, -40%);
  }
}

/* 반응형 */
@media (max-width:1024px) {
  .wish {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width:640px) {
  .tabs {
    width: 100%;
  }

  .profile {
    flex-direction: column;
    align-items: flex-start;
  }

  .wish {
    grid-template-columns: repeat(2, 1fr);
  }

  .steps--flat {
    grid-template-columns: repeat(5, minmax(44px, 1fr));
  }

  .order-summary {
    grid-template-columns: repeat(2, 1fr);
    row-gap: 8px;
  }
}
</style>
