import { defineStore } from 'pinia'
import * as authService from '../services/authService'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('ttm_token') || null,
    refreshToken: localStorage.getItem('ttm_refresh_token') || null,
    user: JSON.parse(localStorage.getItem('ttm_user') || 'null')
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === 'admin'
  },

  actions: {
    async login(credentials) {
      const { data } = await authService.login(credentials)
      this._setSession(data.token, data.refreshToken, data.user)
      return data
    },

    async register(payload) {
      const { data } = await authService.register(payload)
      return data
    },

    async logout() {
      const refreshToken = this.refreshToken
      this._clearSession()
      if (refreshToken) {
        try {
          await authService.logout(refreshToken)
        } catch {
          // best-effort server-side revocation; local session is already cleared
        }
      }
    },

    _setSession(token, refreshToken, user) {
      this.token = token
      this.refreshToken = refreshToken
      this.user = user
      localStorage.setItem('ttm_token', token)
      localStorage.setItem('ttm_refresh_token', refreshToken)
      localStorage.setItem('ttm_user', JSON.stringify(user))
    },

    _clearSession() {
      this.token = null
      this.refreshToken = null
      this.user = null
      localStorage.removeItem('ttm_token')
      localStorage.removeItem('ttm_refresh_token')
      localStorage.removeItem('ttm_user')
    }
  }
})
