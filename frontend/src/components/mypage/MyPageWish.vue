<template>
  <main class="mypage">
    <section class="container">
      <h1 class="section__title section__title--center">위시리스트</h1>

      <div class="panel panel--center">
        <ul class="wish">
          <li v-for="w in wish" :key="w.id" class="wish__item" @click="console.log('상세페이지:', w.id)">
            <figure class="wish__fig">
              <img :src="w.image" :alt="w.title" />
              <button class="heart-btn" @click.stop="toggleWish(w.id)">♥</button>
            </figure>
            <div class="wish__meta">
              <p class="wish__title">{{ w.title }}</p>
              <p class="wish__sub">{{ w.price.toLocaleString() }}원</p>
            </div>
            <div class="wish__actions">
              <button class="btn btn--thin" @click.stop="addToCart(w)">장바구니</button>
            </div>
          </li>
        </ul>
      </div>
    </section>
    <div v-if="toast.open" class="toast">장바구니에 담겼습니다</div>
  </main>
</template>

<script>
import api from '@/api/axios.js'

export default {
  name: 'MyPageWish',
  data() {
    return {
      wish: [],
      toast: { open: false, _t: null },
    }
  },
  
  async mounted() {
    await this.loadWishlist()
  },

  methods: {
    async loadWishlist() {
      try {
        const response = await api.post('/api/wishlist')
        this.wish = response.data.map(w => ({
          id: w.itemDetails.id,
          title: w.itemDetails.item.name,
          price: w.itemDetails.cost,
          image: w.itemDetails.images?.[0]?.url || 'https://picsum.photos/400/400'
        }))
      } catch (error) {
        console.error('위시리스트 조회 실패:', error)
      }
    },

    async toggleWish(itemId) {
      try {
        await api.post('/api/wishlist/toggle', { itemId })
        await this.loadWishlist()
      } catch (error) {
        console.error('위시리스트 토글 실패:', error)
      }
    },

    currency(n){
      return new Intl.NumberFormat('ko-KR',{style:'currency',currency:'KRW',maximumFractionDigits:0}).format(n)
    },

    getCart(){
      try{ const raw = localStorage.getItem('dotori_cart_v1'); return raw ? JSON.parse(raw) : [] }
      catch(e){ return [] }
    },
    saveCart(list){ localStorage.setItem('dotori_cart_v1', JSON.stringify(list)) },
    upsert(cart, item){
      const i = cart.findIndex(x=>x.id===item.id)
      if(i>=0) cart[i].qty += item.qty
      else cart.push(item)
      return cart
    },

    asCartItem(w){ return { id:w.id, title:w.title, price:w.price, qty:1, shipping:0, thumb:w.image } },

    showToast(){
      this.toast.open = true
      clearTimeout(this.toast._t)
      this.toast._t = setTimeout(()=>{ this.toast.open = false }, 2000)
    },

    addToCart(w){
      const item = this.asCartItem(w)
      this.saveCart(this.upsert(this.getCart(), item))
      this.showToast()
    },

    goCart(){ this.$router.push('/cart') },
  }
}
</script>

<style scoped>
.container{ width:min(1160px,92%); margin:0 auto; padding-bottom:120px; }
.section__title{ margin:22px 4px 12px; font-size:22px; font-weight:800; color:#2d251c; }
.section__title--center{ text-align:center; }
.panel{ background:#fff; border:1.5px solid #e9e4db; border-radius:12px; padding:16px 18px; }
.panel--center{ max-width:980px; margin:0 auto 18px; }

.wish{ display:grid; grid-template-columns:repeat(5,1fr); gap:10px; list-style:none; padding:0; margin:0; }
.wish__item{ background:#fff; border:1px solid #e8e1d4; border-radius:10px; overflow:hidden; display:grid; grid-template-rows:120px auto auto; cursor: pointer; }
.wish__fig{ background:#f4f3e6; display:grid; place-items:center; position: relative; }
.wish__fig img{ width:100%; height:100%; object-fit:cover; }
.wish__meta{ padding:10px 12px 5px; margin-top:10px; }
.wish__title{ font-weight:800; }
.wish__sub{ font-size:12px; color:#7b6d5d; }
.wish__actions{ display:flex; gap:6px; padding:8px 8px 8px; }

.btn{ appearance:none; border:0; background:#f4f4f4; color:black; padding:10px 14px; border-radius:8px; font-weight:700; cursor:pointer; }
.btn--ghost{ background:#f4f3e6; color:#5f5346; border:1px solid #d9d9d9; }
.btn--thin{ padding:6px 10px; border-radius:7px; border:1px solid #d9d9d9;}

.heart-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  color: #ff4757;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.heart-btn:hover {
  background: white;
  transform: scale(1.1);
}

@media (max-width:1024px){ .wish{ grid-template-columns:repeat(3,1fr); } }
@media (max-width:640px){ .wish{ grid-template-columns:repeat(2,1fr); } }

.toast{
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background:#f4f4f4;
  color:#7b6d5d;
  padding:14px 20px;
  border-radius:12px;
  font-weight:600;
  text-align:center;
  z-index:9999;
  animation: fadeInOut 2.2s ease forwards;
}
@keyframes fadeInOut{
  0%{ opacity:0; transform:translate(-50%,-60%) }
  15%{ opacity:1; transform:translate(-50%,-50%) }
  85%{ opacity:1; transform:translate(-50%,-50%) }
  100%{ opacity:0; transform:translate(-50%,-40%) }
}
</style>