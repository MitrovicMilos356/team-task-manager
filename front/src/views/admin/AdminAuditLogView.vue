<script setup>
import { onMounted, ref } from 'vue'
import AppHeader from '../../components/AppHeader.vue'
import * as auditLogService from '../../services/auditLogService'

const PAGE_SIZE = 20

const entries = ref([])
const loading = ref(true)
const loadError = ref('')
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await auditLogService.getAuditLog(page.value, PAGE_SIZE)
    entries.value = data.content
    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } catch (err) {
    loadError.value = err.response?.data?.message || 'Failed to load the audit log.'
  } finally {
    loading.value = false
  }
}

function goToPage(target) {
  if (target < 0 || target >= totalPages.value) return
  page.value = target
  load()
}

function formatEntity(entry) {
  return entry.entityId ? `${entry.entityType} #${entry.entityId}` : entry.entityType
}

onMounted(load)
</script>

<template>
  <div class="app-page">
    <AppHeader />
    <main class="app-content">
      <div class="panel">
        <div class="panel-header">
          <h1>Audit log</h1>
        </div>

        <div v-if="loading" class="empty-state">Loading audit log…</div>
        <div v-else-if="loadError" class="alert-error">{{ loadError }}</div>
        <div v-else-if="!entries.length" class="empty-state">No audit events yet.</div>
        <template v-else>
          <table class="table">
            <thead>
              <tr>
                <th>When</th>
                <th>Actor</th>
                <th>Action</th>
                <th>Entity</th>
                <th>Details</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="entry in entries" :key="entry.id">
                <td>{{ entry.createdAt }}</td>
                <td>{{ entry.actor ? `${entry.actor.firstName} ${entry.actor.lastName}` : 'Unknown' }}</td>
                <td>{{ entry.action }}</td>
                <td>{{ formatEntity(entry) }}</td>
                <td>{{ entry.details || '—' }}</td>
              </tr>
            </tbody>
          </table>
          <div v-if="totalPages > 1" class="pager">
            <button class="btn-secondary" type="button" :disabled="page === 0" @click="goToPage(page - 1)">Previous</button>
            <span>Page {{ page + 1 }} of {{ totalPages }} ({{ totalElements }} events)</span>
            <button class="btn-secondary" type="button" :disabled="page >= totalPages - 1" @click="goToPage(page + 1)">Next</button>
          </div>
        </template>
      </div>
    </main>
  </div>
</template>
