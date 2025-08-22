<template>
  <main class="mypage">
    <section class="container">
      <h1 class="section__title section__title--center">보관중인 상품</h1>

      <div class="panel panel--center">
        <template v-if="items.length">
          <ul class="storage">
            <li v-for="it in items" :key="it.id" class="storage__row">
              <img :src="it.image" :alt="it.title" class="storage__thumb" />
              <div class="storage__meta">
                <p class="storage__title">{{ it.title }}</p>
                <p class="storage__sub">{{ it.price.toLocaleString() }}원</p>
              </div>
              <div class="storage__actions">
                <button class="btn btn--ghost btn--sm">상세</button>
                <button class="btn btn--sm" @click="remove(it.id)">해제</button>
              </div>
            </li>
          </ul>
        </template>
        <template v-else>
          <p class="muted center">보관함이 비어 있습니다.</p>
        </template>
      </div>
    </section>
  </main>
</template>

<script>
// 실제 이미지 import
import imgRengokuKeyring from '@/assets/list/렌고쿠 코쥬로 키링.svg'
import imgAkazaDoll from '@/assets/list/아카자 인형.svg'

export default {
  name: 'MyPageStorage',
  data() {
    return {
      items: [
        { 
          id: 'st1', 
          title: '렌고쿠 코쥬로 키링', 
          image: imgRengokuKeyring, 
          price: 10000 
        },
        { 
          id: 'st2', 
          title: '아카자 인형', 
          image: imgAkazaDoll, 
          price: 48000 
        },
      ],
    };
  },
  methods:{
    remove(id){ 
      this.items = this.items.filter(v => v.id !== id); 
    }
  }
};
</script>

<style scoped>
.container{ width:min(1160px,92%); margin:0 auto; padding-bottom:120px; }
.section__title{ margin:22px 4px 12px; font-size:22px; font-weight:800; color:#2d251c; }
.section__title--center{ text-align:center; }
.panel{ background:#fff; border:1.5px solid #e9e4db; border-radius:12px; padding:16px 18px; }
.panel--center{ max-width:980px; margin:0 auto 18px; }

.muted{ color:#7b6d5d; }
.center{ text-align:center; }

.storage{ list-style:none; margin:0; padding:0; display:flex; flex-direction:column; gap:10px; }
.storage__row{ display:grid; grid-template-columns:64px 1fr auto; gap:10px; align-items:center; padding:6px 4px; border-bottom:1px solid #eee6d7; }
.storage__thumb{ width:64px; height:64px; object-fit:cover; border-radius:8px; background:#f4f3e6; border:1px solid #e8e1d4; }
.storage__title{ font-weight:800; }
.storage__sub{ margin-top:2px; font-size:12px; color:#7b6d5d; }
.storage__actions{ display:flex; gap:8px; }

.btn{ appearance:none; border:0; background:#f4f4f4; color:black; padding:10px 14px; border-radius:8px; font-weight:700; cursor:pointer; }
.btn--ghost{ background:#f4f3e6; color:#5f5346; border:1px solid #d9d9d9; }
.btn--wide{ width:100%; padding:12px 16px; }
.btn--sm{ padding:6px 10px; border-radius:8px; background:#fff7ea; color:#332b22; border:1px solid #e3d7c2; }
</style>