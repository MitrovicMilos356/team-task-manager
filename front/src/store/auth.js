import { defineStore } from 'pinia'
import * as authService from '../services/authService'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('ttm_token') || null,
    user: JSON.parse(localStorage.getItem('ttm_user') || 'null')
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === 'admin'
  },

  actions: {
    async login(credentials) {
      const { data } = await authService.login(credentials)
      this._setSession(data.token, data.user)
      return data
    },

    async register(payload) {
      const { data } = await authService.register(payload)
      return data
    },

    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('ttm_token')
      localStorage.removeItem('ttm_user')
    },

    _setSession(token, user) {
      this.token = token
      this.user = user
      localStorage.setItem('ttm_token', token)
      localStorage.setItem('ttm_user', JSON.stringify(user))
    }
  }
})
