import api from './api'

export function getTasksByProject(projectId) {
  return api.get('/tasks', { params: { projectId } })
}

export function getTasks(filters = {}) {
  return api.get('/tasks', { params: filters })
}

export function getTask(taskId) {
  return api.get(`/tasks/${taskId}`)
}

export function createTask(payload) {
  return api.post('/tasks', payload)
}

export function updateTask(taskId, payload) {
  return api.put(`/tasks/${taskId}`, payload)
}

export function updateAssignee(taskId, assignedUserId) {
  return api.patch(`/tasks/${taskId}/assignee`, { assignedUserId })
}

export function updateStatus(taskId, status) {
  return api.patch(`/tasks/${taskId}/status`, { status })
}

export function getComments(taskId) {
  return api.get(`/tasks/${taskId}/comments`)
}

export function addComment(taskId, comment) {
  return api.post(`/tasks/${taskId}/comments`, { comment })
}

export function getHistory(taskId) {
  return api.get(`/tasks/${taskId}/history`)
}
