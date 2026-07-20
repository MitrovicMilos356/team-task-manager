import api from './api'

export function getUsers() {
  return api.get('/users')
}

export function createUser(payload) {
  return api.post('/users', payload)
}

export function updateUser(id, payload) {
  return api.put(`/users/${id}`, payload)
}

export function updateUserStatus(id, active) {
  return api.patch(`/users/${id}/status`, { active })
}
