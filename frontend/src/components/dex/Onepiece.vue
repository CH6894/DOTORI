<!-- src/components/dex/Kimetsu.vue -->
<template>
  <div class="figure-grid-container">
    <!-- 헤더: 로고 + 통계 + 인증 입력 -->
    <div class="figure-header">
      <h2 class="dictionary-title">
        <img :src="onepiece" alt="원피스" class="title-img" />
      </h2>
      <div class="collection-stats">
        <span>총 수집품: {{ verifiedCount }}/{{ total }}</span>
        <span>•</span>
        <span>완성도: {{ completionRate }}%</span>
      </div>
    </div>

    <!-- 그리드 -->
    <div class="figure-grid">
      <div
        v-for="f in figures"
        :key="makeKeySafe(f.id)"
        class="figure-card"
        @click="onCardClick(f)"
      >
        <div
          class="figure-image-container"
          :class="{ uncollected: !isActivated(f.id) }"
        >
          <img
            :src="f.image"
            :alt="f.name"
            class="figure-image"
            @error="handleImageError"
            loading="lazy"
          />
          <!-- ✅ 인증 배지 통일 -->
          <VerifiedBadge v-if="isVerified(f.id)" class="verified-badge" aria-label="인증됨" />
        </div>

        <div class="figure-info">
          <h4 class="figure-name">{{ f.name }}</h4>
          <p class="figure-title">시리즈: {{ f.series }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// @ts-expect-error: JS 모듈에 d.ts 없음
import rawOnepieceFigures from '@/data/onepiece.js'
import onepiece from '@/assets/onepiece/logo/onepiece.webp'
import VerifiedBadge from '@/components/dex/VerifiedBadge.vue'
import { computed, onMounted, ref } from 'vue'
import { useDex } from '@/stores/useDex'
import type { DexKey } from '@/stores/dexKey'

/** 원본 데이터 타입 */
type RawFigure = {
  id: number | string | undefined
  name: string
  character: string
  series: string
  image: string
  verified?: boolean
}

/** 정규화된 타입 */
type Figure = {
  id: number
  name: string
  character: string
  series: string
  image: string
  verified?: boolean
}

const dex = useDex()
const onepieceLogo = onepiece


/** 1) 데이터 정규화 (id undefined 방지) */
const normalizeFigures = (input: RawFigure[]): Figure[] => {
  const tmp = input.map((f, i) => {
    const n = Number(f.id)
    const id = Number.isFinite(n) ? n : i + 1
    return { ...f, id } as Figure
  })
  const seen = new Set<number>()
  let bump = tmp.length + 1
  for (const f of tmp) {
    if (seen.has(f.id)) f.id = bump++
    seen.add(f.id)
  }
  return tmp
}
const figures = normalizeFigures(rawOnepieceFigures as RawFigure[])
const total = figures.length

/** 2) 안전 키 생성기 */
function makeKeySafe(id: number | string | undefined): DexKey {
  const n = Number(id)
  if (!Number.isFinite(n)) return 'animation:onepiece:INVALID' as DexKey
  return `animation:onepiece:${String(n).padStart(3, '0')}` as DexKey
}

/** 3) 상태 판정(Set 캐싱) */
const activatedSet = computed(() => new Set<DexKey>(
  dex.items.filter(i => i.activated).map(i => i.itemKey)
))
const verifiedSet = computed(() => new Set<DexKey>(
  dex.items.filter(i => i.verified).map(i => i.itemKey)
))
const isActivated = (id: number | string | undefined) =>
  activatedSet.value.has(makeKeySafe(id))
const isVerified = (id: number | string | undefined) =>
  verifiedSet.value.has(makeKeySafe(id))

/** 통계 */
const itemKeys = computed<DexKey[]>(() => figures.map(f => makeKeySafe(f.id)))
const verifiedCount = computed(() => itemKeys.value.filter(k => dex.isVerified(k)).length)
const completionRate = computed(() => dex.completionByKeys(itemKeys.value))

/** 카드 클릭 → 해당 카드만 토글 */
function onCardClick(f: Figure) {
  if (isVerified(f.id)) return   // 인증된 항목은 클릭 무시
  dex.toggleActivate(makeKeySafe(f.id))
}

/** ✅ 코드 입력 → 포켓몬과 같은 verifyByCode 경로 */
const code = ref('')
async function onSubmitCode() {
  const raw = code.value.trim()
  if (!raw) {
    alert('인증코드를 입력하세요.')
    return
  }
  try {
    const k = await dex.verifyByCode(raw)
    alert(`인증 완료: ${k}`)
  } catch (e: any) {
    const msg =
      e?.message === 'INVALID_RANGE' ? '인증 가능 범위를 벗어났어요.' :
      e?.message === 'INVALID_CODE'  ? '코드 형식이 올바르지 않아요.' :
                                        '인증에 실패했어요. 잠시 후 다시 시도해주세요.'
    alert(msg)
  } finally {
    code.value = ''
  }
}

/** 이미지 실패 핸들러 */
function handleImageError(e: Event) {
  const t = e.target as HTMLImageElement
  console.warn('Image failed to load:', t?.src)
}

/** 4) 마운트 시 레거시/잘못된 키 정리 */
onMounted(() => {
  const valid = new Set(itemKeys.value)
  const before = dex.items.length
  dex.items = dex.items.filter(it => {
    if (!it.itemKey.startsWith('animation:onepiece:')) return true
    return valid.has(it.itemKey)
  })
  const diff = before - dex.items.length
  if (diff > 0) console.info(`[Onepiece] cleaned ${diff} invalid items from store`)

  const dups = itemKeys.value.filter((k, i, arr) => arr.indexOf(k) !== i)
  if (dups.length) console.warn('[Onepiece] duplicate keys detected:', dups)
})
</script>

<style scoped>
@import '@/assets/styles/collection.css';
.figure-header {
  text-align: center;
  margin-bottom: 2.5rem;
  padding: 2rem;
  background: linear-gradient(135deg, #addeff, #3d6285, #3123f8);
  border-radius: 20px;
  color: #fdfdfd;
  box-shadow: inset 0 3px 10px rgba(255,255,255,0.1),
              0 8px 20px rgba(0,0,0,0.4);
}

/* 헤더 로고 중앙 정렬 */
.title-img {
  height: 100px;
  width: auto;
  margin: 0 auto 1rem;
  object-fit: contain;
  display: block;
}

/* 인증 입력 UI */
.verify-form {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  justify-content: center;
}
.verify-input {
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #ddd;
  min-width: 220px;
}

/* 그리드 보강 */
.figure-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 2rem 1.5rem;
  min-height: 400px;
  justify-items: center;
}
.figure-card { width: 100%; }

/* 배지 위치 (필요 시) */
.verified-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 5;
}

/* 반응형 */
@media (max-width: 1280px) { .figure-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 1024px) { .figure-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px)  { .figure-grid { grid-template-columns: repeat(2, 1fr); gap: 1.5rem 1rem; } }
@media (max-width: 480px)  { .figure-grid { grid-template-columns: repeat(2, 1fr); gap: 1rem; } }
</style>
