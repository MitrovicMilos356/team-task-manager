import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('ttm_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let refreshPromise = null

async function clearSessionAndRedirect() {
  localStorage.removeItem('ttm_token')
  localStorage.removeItem('ttm_refresh_token')
  localStorage.removeItem('ttm_user')
  try {
    const { useAuthStore } = await import('../store/auth')
    useAuthStore()._clearSession()
  } catch {
    // Pinia may not be active yet (e.g. during app bootstrap); localStorage clear above is enough
  }
  if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    const status = error.response?.status
    const url = originalRequest?.url || ''
    const isAuthEndpoint = url.includes('/auth/login') || url.includes('/auth/refresh') || url.includes('/auth/register')

    if (status !== 401 || isAuthEndpoint || !originalRequest || originalRequest._retry) {
      return Promise.reject(error)
    }

    const refreshToken = localStorage.getItem('ttm_refresh_token')
    if (!refreshToken) {
      await clearSessionAndRedirect()
      return Promise.reject(error)
    }

    originalRequest._retry = true

    try {
      if (!refreshPromise) {
        refreshPromise = api
          .post('/auth/refresh', { refreshToken })
          .then((res) => res.data)
          .finally(() => {
            refreshPromise = null
          })
      }
      const data = await refreshPromise
      localStorage.setItem('ttm_token', data.token)
      localStorage.setItem('ttm_refresh_token', data.refreshToken)
      try {
        const { useAuthStore } = await import('../store/auth')
        useAuthStore()._setSession(data.token, data.refreshToken, data.user)
      } catch {
        // best-effort store sync; localStorage above already keeps requests working
      }
      originalRequest.headers.Authorization = `Bearer ${data.token}`
      return api(originalRequest)
    } catch (refreshError) {
      await clearSessionAndRedirect()
      return Promise.reject(refreshError)
    }
  }
)

export default api
