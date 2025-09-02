// src/stores/auth.ts
import { defineStore } from 'pinia'

interface AuthState {
  token: string | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('accessToken') || null,
  }),
  
  getters: {
    isAuthed: (state): boolean => !!state.token,
  },
  
  actions: {
    setToken(token: string | null): void {
      this.token = token
      if (token) {
        localStorage.setItem('accessToken', token)
      } else {
        localStorage.removeItem('accessToken')
      }
    },
    
    logout(): void {
      this.setToken(null)
    },
  },
})

