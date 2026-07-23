import api from './api'

export async function getProjects() {
  const { data } = await api.get('/projects', { params: { size: 1000 } })
  return { data: data.content }
}

export function getProject(id) {
  return api.get(`/projects/${id}`)
}

export function createProject(payload) {
  return api.post('/projects', payload)
}

export function updateProject(id, payload) {
  return api.put(`/projects/${id}`, payload)
}

export function deactivateProject(id) {
  return api.delete(`/projects/${id}`)
}

export function exportProjectsCsv() {
  return api.get('/projects/export', { responseType: 'blob' })
}
