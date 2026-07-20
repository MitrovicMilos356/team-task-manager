import api from './api'

export function getTasksByProject(projectId) {
  return api.get('/tasks', { params: { projectId } })
}

export function createTask(payload) {
  return api.post('/tasks', payload)
}

export function updateAssignee(taskId, assignedUserId) {
  return api.patch(`/tasks/${taskId}/assignee`, { assignedUserId })
}
