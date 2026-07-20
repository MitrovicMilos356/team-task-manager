import api from './api'

export function getMembers(projectId) {
  return api.get(`/projects/${projectId}/members`)
}

export function addMember(projectId, userId) {
  return api.post(`/projects/${projectId}/members`, { userId })
}

export function removeMember(projectId, userId) {
  return api.delete(`/projects/${projectId}/members/${userId}`)
}
