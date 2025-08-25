import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('accessToken') || null,
  }),
  getters: {
    isAuthed: (s) => !!s.token,
  },
  actions: {
    setToken(t) {
      this.token = t
      if (t) localStorage.setItem('accessToken', t)
      else localStorage.removeItem('accessToken')
      // 전역 이벤트 (optional): 다른 컴포넌트가 즉시 반응하도록
      window.dispatchEvent(new Event('auth-changed'))
    },
    logout() { this.setToken(null) },
  },
})
