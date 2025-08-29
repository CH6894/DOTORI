<!-- src/views/Dex.vue -->
<template>
  <div class="dex-view">
    <!-- ✅ 전 카테고리 공용 등록 바 -->
    <RegisterBar
      v-model:code="registerCode"
      @register="handleRegister"
      @clear="handleClear"
    />

    <!-- 카테고리 탭 -->
    <section class="product_list_wrap">
      <div class="product_list_area" role="region" aria-label="도감 카테고리">
        <div class="container-1280">
          <TopTabsAdapter v-model="currentCategory" :items="categories" aria-label="상위 카테고리" />
        </div>
        <div class="container-1280">
          <MidTabsAdapter
            v-model="currentSubCategory"
            :items="currentSubCategories"
            aria-label="중위 카테고리"
            :visible="currentSubCategories.length > 0"
          />
        </div>
      </div>
    </section>

    <!-- 메인 컨텐츠: 동적 컴포넌트 삽입 -->
    <div class="main-content">
      <div v-if="activeViewComponent" class="view-container">
        <component :is="activeViewComponent" />
      </div>

      <div class="empty-state" v-else>
        <div class="empty-icon">📦</div>
        <h3>{{ currentCategoryName }} 준비 중입니다</h3>
        <p>해당 카테고리의 콘텐츠는 곧 업데이트될 예정입니다.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import TopTabsAdapter from '@/components/filters/TopTabsAdapter.vue'
import MidTabsAdapter from '@/components/filters/MidTabsAdapter.vue'
import RegisterBar from '@/components/dex/RegisterBar.vue'
import { onMounted } from 'vue'
import { useDex } from '@/stores/useDex'


/* ✅ 동적 컴포넌트 */
import PokemonCollection from '@/components/dex/Pokemon.vue'
import BlackPink from '@/components/dex/BlackPink.vue'
import ChimCollection from '@/components/dex/ChimCollection.vue'
import KiaCollection from '@/components/dex/KiaCollection.vue'
import Kimetsu from '@/components/dex/Kimetsu.vue'

type Category = { id: string; name: string }
type SubCategory = { id: string; name: string }

const dex = useDex()
onMounted(() => { dex.fetchState() })

/* 등록 상태/핸들러 (전역 적용) */
const registerCode = ref('')

async function handleRegister(code: string) {
  const input = code.trim()
  if (!input) {
    alert('영수증 번호 또는 인증코드를 입력해주세요.')
    return
  }
  try {
    const itemKey = await dex.verifyByCode(input)   // ✅ 스토어로 인증 (더미/실제 모두 커버)
    alert(`인증 완료! 아이템: ${itemKey}`)
  } catch (e) {
    alert('인증 실패 또는 만료된 코드입니다.')
    console.error(e)
  } finally {
    registerCode.value = ''
  }
}
function handleClear() { registerCode.value = '' }


/* 탭 목록 */
const categories = ref<Category[]>([
  { id: 'animation', name: 'Animation' },
  { id: 'creater',  name: 'Creater'  },
  { id: 'game',     name: 'Game'     },
  { id: 'kpop',     name: 'Kpop'     },
  { id: 'sports',   name: 'Sports'   },
  { id: 'webtoon',  name: 'Webtoon'  },
])

const subCategories = ref<Record<string, SubCategory[]>>({
  animation: [{ id: 'kimetsu', name: '귀멸의 칼날' }],
  creater:   [{ id: 'chim',    name: '침착맨' }],
  game:      [{ id: 'pokemon', name: '포켓몬스터' }, ],
  kpop:      [{ id: 'blackpink', name: 'BLACKPINK' }],
  sports:    [{ id: 'kia', name: 'KIA' }],
  webtoon:   [{ id: 'tower', name: '신의 탑' }, { id: 'noblesse', name: '마루는 강쥐' }],
})

const currentCategory = ref<string>('game')
const currentSubCategory = ref<string>('pokemon')

const currentSubCategories = computed(() => subCategories.value[currentCategory.value] || [])
const currentCategoryName = computed(() => categories.value.find(c => c.id === currentCategory.value)?.name || '')

const registry: Record<string, any> = {
  'game:pokemon':       PokemonCollection,
  'kpop:blackpink':     BlackPink,
  'creater:chim':       ChimCollection,
  'sports:kia':         KiaCollection,
  'animation:kimetsu':  Kimetsu,
}

const activeViewComponent = computed(() => {
  const key = `${currentCategory.value}:${currentSubCategory.value}`
  return registry[key] ?? null
})

watch(currentCategory, () => {
  const list = currentSubCategories.value
  if (!list.find(s => s.id === currentSubCategory.value)) {
    currentSubCategory.value = list[0]?.id ?? ''
  }
})
</script>

<style scoped>
.product_list_wrap { width: 100%; display: flex; justify-content: center; }
.product_list_area { width: 100%; }
.container-1280 { width: 100%; max-width: 1280px; margin: 0 auto; padding-left: 16px; padding-right: 16px; }

.main-content {
  width: 100%;
  margin: 0 auto;
  padding: 2rem 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.view-container { width: 100%; }

.empty-state { text-align: center; padding: 4rem 2rem; color: #666; }
.empty-icon { font-size: 4rem; margin-bottom: 1rem; opacity: .3; }
</style>

<style>
a { text-decoration: none; color: inherit; }
a:visited { text-decoration: none; color: inherit; }

/* 모달 스크롤 락 (하위 컴포넌트에서 사용) */
html.scroll-lock,
html.scroll-lock body {
  overflow: hidden;
}
</style>
