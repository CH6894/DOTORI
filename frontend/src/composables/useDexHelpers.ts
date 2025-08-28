// 컬렉션별 itemKey를 "도메인 표준"으로 생성/파싱하는 헬퍼
// 포맷: `${category}:${sub}:${id}`  예) 'game:pokemon:001', 'kpop:blackpink:JISOO-01'

export type DexKey = `${string}:${string}:${string}`

export function makeKey(category: string, sub: string, id: string | number): DexKey {
  const normId = typeof id === 'number' ? String(id).padStart(3, '0') : String(id)
  return `${category}:${sub}:${normId}` as DexKey
}

export function parseKey(key: DexKey) {
  const [category, sub, id] = key.split(':')
  return { category, sub, id }
}
