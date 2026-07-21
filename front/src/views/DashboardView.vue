<script setup>
import { onMounted, ref } from 'vue'
import { useAuthStore } from '../store/auth'
import AppHeader from '../components/AppHeader.vue'
import * as dashboardService from '../services/dashboardService'

const STATUS_LABELS = {
  todo: 'To do',
  in_progress: 'In progress',
  in_review: 'In review',
  done: 'Done',
  canceled: 'Canceled'
}

const auth = useAuthStore()
const stats = ref(null)
const loading = ref(true)
const loadError = ref('')

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await dashboardService.getStatistics()
    stats.value = data
  } catch (err) {
    loadError.value = err.response?.data?.message || 'Failed to load dashboard statistics.'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="app-page">
    <AppHeader />
    <main class="app-content">
      <div class="panel" style="margin-bottom: 24px">
        <h1 style="margin:0">Welcome{{ auth.user ? `, ${auth.user.firstName}` : '' }}</h1>
        <p class="subtitle" style="margin:6px 0 0">
          Signed in as {{ auth.user?.email }} ({{ auth.user?.role }})
        </p>
      </div>

      <div v-if="loading" class="empty-state">Loading dashboard…</div>
      <div v-else-if="loadError" class="alert-error">{{ loadError }}</div>
      <template v-else-if="stats">
        <div class="stat-grid">
          <div class="stat-tile">
            <div class="stat-value">{{ stats.totalProjects }}</div>
            <div class="stat-label">Projects</div>
          </div>
          <div class="stat-tile">
            <div class="stat-value">{{ stats.totalOpenTasks }}</div>
            <div class="stat-label">Open tasks</div>
          </div>
          <div class="stat-tile" :class="{ 'stat-tile-warn': stats.overdueTasks > 0 }">
            <div class="stat-value">{{ stats.overdueTasks }}</div>
            <div class="stat-label">Overdue tasks</div>
          </div>
          <div class="stat-tile">
            <div class="stat-value">{{ stats.myAssignedOpenTasks }}</div>
            <div class="stat-label">Assigned to me</div>
          </div>
        </div>

        <div class="panel" style="margin-top: 24px">
          <div class="panel-header">
            <h1>Tasks by status</h1>
          </div>
          <table class="table">
            <thead>
              <tr>
                <th>Status</th>
                <th>Count</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(count, status) in stats.tasksByStatus" :key="status">
                <td>{{ STATUS_LABELS[status] || status }}</td>
                <td>{{ count }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </main>
  </div>
</template>
