import api from './api'

export async function getUsers() {
  const { data } = await api.get('/users', { params: { size: 1000 } })
  return { data: data.content }
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

export function exportUsersCsv() {
  return api.get('/users/export', { responseType: 'blob' })
}
