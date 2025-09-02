// src/stores/dexKey.ts
export type DexKey = string

export const PREFIX = {
  pokemon:  'game:pokemon:',
  kimetsu:  'animation:kimetsu:',
  onepiece: 'animation:onepiece:',
} as const

export const pad3 = (n: number | string) => String(n).padStart(3, '0')
export const keyOf = (prefix: string, n: number) => `${prefix}${pad3(n)}` as DexKey

// 시리즈별(로컬) 최대 허용 개수
export const MAX_LOCAL = {
  pokemon:  Infinity, // 필요 시 숫자로 제한
  kimetsu:  11,
  onepiece: Infinity,
} as const
