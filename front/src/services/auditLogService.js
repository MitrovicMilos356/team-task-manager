import api from './api'

export function getAuditLog(page = 0, size = 20) {
  return api.get('/audit-log', { params: { page, size } })
}
