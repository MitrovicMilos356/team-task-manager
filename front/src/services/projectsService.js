import api from './api'

export function getProjects() {
  return api.get('/projects')
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
