import api from './api'

export function login(credentials) {
  return api.post('/auth/login', credentials)
}

export function register(payload) {
  return api.post('/auth/register', payload)
}

export function fetchCurrentUser() {
  return api.get('/users/me')
}

export function refresh(refreshToken) {
  return api.post('/auth/refresh', { refreshToken })
}

export function logout(refreshToken) {
  return api.post('/auth/logout', { refreshToken })
}
