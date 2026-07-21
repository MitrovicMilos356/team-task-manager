import api from './api'

export function getStatistics() {
  return api.get('/dashboard/statistics')
}
