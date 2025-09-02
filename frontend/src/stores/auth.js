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
    },
    logout() { this.setToken(null) },
  },
})
