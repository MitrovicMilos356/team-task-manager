<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import AppHeader from '../components/AppHeader.vue'
import * as projectsService from '../services/projectsService'
import * as tasksService from '../services/tasksService'
import { downloadBlob } from '../utils/download'

const STATUSES = ['todo', 'in_progress', 'in_review', 'done', 'canceled']
const PRIORITIES = ['low', 'medium', 'high', 'urgent']

const PAGE_SIZE = 10

const projects = ref([])
const tasks = ref([])
const loading = ref(true)
const loadError = ref('')
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

const filters = reactive({
  projectId: '',
  status: '',
  priority: '',
  assigneeId: '',
  dueBefore: '',
  dueAfter: '',
  q: ''
})

const assigneeOptions = computed(() => {
  const seen = new Map()
  for (const task of tasks.value) {
    if (task.assignedUser) seen.set(task.assignedUser.id, task.assignedUser)
  }
  return [...seen.values()]
})

const exporting = ref(false)
const exportError = ref('')

function buildFilterParams() {
  const params = {}
  if (filters.projectId) params.projectId = filters.projectId
  if (filters.status) params.status = filters.status
  if (filters.priority) params.priority = filters.priority
  if (filters.assigneeId) params.assignedUserId = filters.assigneeId
  if (filters.dueBefore) params.dueBefore = filters.dueBefore
  if (filters.dueAfter) params.dueAfter = filters.dueAfter
  if (filters.q) params.q = filters.q
  return params
}

async function loadProjects() {
  try {
    const { data } = await projectsService.getProjects()
    projects.value = data
  } catch {
    // project filter is optional; leave list empty on failure
  }
}

async function loadTasks() {
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await tasksService.getTasks(buildFilterParams(), page.value, PAGE_SIZE)
    tasks.value = data.content
    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } catch (err) {
    loadError.value = err.response?.data?.message || 'Failed to load tasks.'
  } finally {
    loading.value = false
  }
}

async function exportCsv() {
  exportError.value = ''
  exporting.value = true
  try {
    const { data } = await tasksService.exportTasksCsv(buildFilterParams())
    downloadBlob(data, 'tasks.csv')
  } catch (err) {
    exportError.value = err.response?.data?.message || 'Could not export tasks.'
  } finally {
    exporting.value = false
  }
}

function applyFilters() {
  page.value = 0
  loadTasks()
}

function clearFilters() {
  filters.projectId = ''
  filters.status = ''
  filters.priority = ''
  filters.assigneeId = ''
  filters.dueBefore = ''
  filters.dueAfter = ''
  filters.q = ''
  page.value = 0
  loadTasks()
}

function goToPage(target) {
  if (target < 0 || target >= totalPages.value) return
  page.value = target
  loadTasks()
}

onMounted(() => {
  loadProjects()
  loadTasks()
})
</script>

<template>
  <div class="app-page">
    <AppHeader />
    <main class="app-content">
      <div class="panel">
        <div class="panel-header">
          <h1>Tasks</h1>
          <button class="btn-secondary" type="button" :disabled="exporting" @click="exportCsv">
            {{ exporting ? 'Exporting…' : 'Export CSV' }}
          </button>
        </div>
        <div v-if="exportError" class="alert-error">{{ exportError }}</div>

        <div class="filter-bar">
          <div class="field">
            <label for="filter-project">Project</label>
            <select id="filter-project" v-model="filters.projectId" @change="applyFilters">
              <option value="">All projects</option>
              <option v-for="project in projects" :key="project.id" :value="project.id">{{ project.name }}</option>
            </select>
          </div>
          <div class="field">
            <label for="filter-status">Status</label>
            <select id="filter-status" v-model="filters.status" @change="applyFilters">
              <option value="">Any status</option>
              <option v-for="status in STATUSES" :key="status" :value="status">{{ status }}</option>
            </select>
          </div>
          <div class="field">
            <label for="filter-priority">Priority</label>
            <select id="filter-priority" v-model="filters.priority" @change="applyFilters">
              <option value="">Any priority</option>
              <option v-for="priority in PRIORITIES" :key="priority" :value="priority">{{ priority }}</option>
            </select>
          </div>
          <div class="field">
            <label for="filter-assignee">Assignee</label>
            <select id="filter-assignee" v-model="filters.assigneeId" @change="applyFilters">
              <option value="">Anyone</option>
              <option v-for="user in assigneeOptions" :key="user.id" :value="user.id">
                {{ user.firstName }} {{ user.lastName }}
              </option>
            </select>
          </div>
          <div class="field">
            <label for="filter-due-after">Due after</label>
            <input id="filter-due-after" v-model="filters.dueAfter" type="date" @change="applyFilters" />
          </div>
          <div class="field">
            <label for="filter-due-before">Due before</label>
            <input id="filter-due-before" v-model="filters.dueBefore" type="date" @change="applyFilters" />
          </div>
          <div class="field">
            <label for="filter-q">Search</label>
            <input id="filter-q" v-model="filters.q" type="text" placeholder="Title or description" @keyup.enter="applyFilters" />
          </div>
          <button class="btn-secondary" type="button" @click="applyFilters">Apply</button>
          <button class="btn-link" type="button" @click="clearFilters">Clear</button>
        </div>

        <div v-if="loading" class="empty-state">Loading tasks…</div>
        <div v-else-if="loadError" class="alert-error">{{ loadError }}</div>
        <div v-else-if="!tasks.length" class="empty-state">No tasks match these filters.</div>
        <template v-else>
          <table class="table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Project</th>
                <th>Assignee</th>
                <th>Status</th>
                <th>Priority</th>
                <th>Due date</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="task in tasks" :key="task.id">
                <td>
                  <router-link class="btn-link" :to="{ name: 'task-detail', params: { id: task.id } }">
                    {{ task.title }}
                  </router-link>
                </td>
                <td>{{ projects.find((p) => p.id === task.projectId)?.name || task.projectId }}</td>
                <td>{{ task.assignedUser ? `${task.assignedUser.firstName} ${task.assignedUser.lastName}` : 'Unassigned' }}</td>
                <td>{{ task.status }}</td>
                <td>{{ task.priority }}</td>
                <td>{{ task.dueDate || '—' }}</td>
              </tr>
            </tbody>
          </table>
          <div v-if="totalPages > 1" class="pager">
            <button class="btn-secondary" type="button" :disabled="page === 0" @click="goToPage(page - 1)">Previous</button>
            <span>Page {{ page + 1 }} of {{ totalPages }} ({{ totalElements }} tasks)</span>
            <button class="btn-secondary" type="button" :disabled="page >= totalPages - 1" @click="goToPage(page + 1)">Next</button>
          </div>
        </template>
      </div>
    </main>
  </div>
</template>
