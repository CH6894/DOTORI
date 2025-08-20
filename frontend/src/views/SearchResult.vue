<template>
  <div class="product_list_wrap">
    <div class="product_list_area">
      <!-- Top 카테고리: TopCategoryData 만큼 표시 -->
      <div class="top_category_list">
        <ul>
          <li
            v-for="cat in topCategories"
            :key="cat"
            class="top_category_item"
          >
            <button
              class="top_category_link"
              :class="{ selected: activeTop === cat }"
              type="button"
              @click="selectTop(cat)"
            >
              <strong><span>{{ cat }}</span></strong>
            </button>
          </li>
        </ul>
      </div>

      <!-- Mid 카테고리: Animation 선택 시에만 MidCategoryData 노출 -->
      <div class="mid_category_list" v-if="activeTop === 'Animation'">
        <ul>
          <!-- 전체(초기화) -->
          <li class="mid_category_item">
            <button
              class="mid_category_link"
              :class="{ selected: activeMid === '' }"
              type="button"
              @click="selectMid('')"
            >
              <strong><span>전체</span></strong>
            </button>
          </li>

          <li
            v-for="mid in midCategories"
            :key="mid"
            class="mid_category_item"
          >
            <button
              class="mid_category_link"
              :class="{ selected: activeMid === mid }"
              type="button"
              @click="selectMid(mid)"
            >
              <strong><span>{{ mid }}</span></strong>
            </button>
          </li>
        </ul>
      </div>

      <!-- 상품 리스트: ItemData(현재 Animation만)에서 필터링 -->
      <div class="product_list">
        <div
          v-for="item in filteredItems"
          :key="item.id"
          class="product_item"
        >
          <div class="product_item_img">
            <router-link
              class="product_item_link"
              :to="{ name: 'Product', params: { id: String(item.id) } }"
            >
              <img src="../assets/1.jpg" :alt="item.name + ' 이미지'" />
            </router-link>
          </div>
          <div class="product_item_info">
            <router-link
              class="product_item_title"
              :to="{ name: 'Product', params: { id: String(item.id) } }"
            >
              <span>{{ item.name }}</span>
            </router-link>
            <ul class="price">
              <li class="price"><span>{{ item.price }} 원</span></li>
            </ul>
          </div>
        </div>

        <div v-if="!filteredItems.length" class="empty">검색 결과가 없습니다.</div>
      </div>
    </div>
  </div>
</template>

<script>
import TopCategoryData from "../assets/TopCategoryData.js";
import MidCategoryData from "../assets/MidCategoryData.js";
import ItemData from "../assets/ItemData.js";

export default {
  name: "CategoryTabComponent",
  data() {
    return {
      activeTop: "Animation", // 기본 선택
      activeMid: "",          // "" = 전체(서브필터 해제)
      items: ItemData,        // 전체 아이템(현재 Animation만 포함)
    };
  },
  computed: {
    topCategories() {
      return TopCategoryData; // ["Animation","Creator","Game","Kpop","Sports","Webtoon"]
    },
    midCategories() {
      // 요구사항: Animation의 Mid만 존재/표시
      return this.activeTop === "Animation" ? MidCategoryData : [];
    },
    filteredItems() {
      // Top → Mid 순서로 필터링
      let list = this.items.filter(i => i.top_category === this.activeTop);
      if (this.activeMid) {
        list = list.filter(i => i.mid_category === this.activeMid);
      }
      return list;
    },
  },
  methods: {
    selectTop(cat) {
      if (this.activeTop === cat) return;
      this.activeTop = cat;
      this.activeMid = ""; // Top 바뀌면 Mid 초기화
    },
    selectMid(mid) {
      this.activeMid = mid; // ''이면 전체
    },
  },
};

</script>

<style>
  div.product_list_wrap {
    width: 100%;
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }
  div.top_category_list ul {
    display: flex;
    list-style: none;
    flex-wrap: wrap;
    width: 1280px;
    padding-left: 0px;
    margin: 0 auto;
    padding-top: 20px;
    gap: 4px;
  }

  button.top_category_link{
    border: none;
    background: none;
    color: #670600;
    cursor: pointer;
    padding: 10px 20px;
    border-top-left-radius: 10px;
    border-top-right-radius: 10px;
    background: #F4F3E6;
  }

  button.top_category_link.selected {
    border-top-left-radius: 10px;
    border-top-right-radius: 10px;
    background: #EFECC6;
  }

  div.mid_category_list ul {
    display: flex;
    list-style: none;
    flex-wrap: wrap;
    padding-left: 0px;
    margin: 0 auto;
    background: #EFECC6;
  }

  button.mid_category_link{
    border: none;
    background: none;
    color: #333333;
    cursor: pointer;
    padding: 10px 20px;
  }

  button.mid_category_link.selected{
    border: none;
    background: none;
    color: #670600;
    cursor: pointer;
    padding: 10px 20px;
  }

  div.product_list {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    justify-items: center;
  }

  div.product_item {
    margin: 10px 10px 10px 0;

  }
  a {
    text-decoration: none;
    color: inherit;
  }
  img {
    width: 200px;
    height: auto;
    object-fit: cover; /* 이미지 비율 유지 */
  }
  div.product_item_info {
    margin-top: 2px;
    height: 60px;
  }

  ul.price {
    padding: 0;
    margin: 0;
  }
  ul.price li.price {
    list-style: none;
    font-size: 14px;
    color: #333333;
    margin-top: 5px;
  }

</style>