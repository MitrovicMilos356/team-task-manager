<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import AppHeader from '../components/AppHeader.vue'
import * as projectsService from '../services/projectsService'
import * as tasksService from '../services/tasksService'

const STATUSES = ['todo', 'in_progress', 'in_review', 'done', 'canceled']
const PRIORITIES = ['low', 'medium', 'high', 'urgent']

const projects = ref([])
const tasks = ref([])
const loading = ref(true)
const loadError = ref('')

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

const filteredTasks = computed(() => {
  if (!filters.assigneeId) return tasks.value
  return tasks.value.filter((t) => t.assignedUser?.id === Number(filters.assigneeId))
})

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
    const params = {}
    if (filters.projectId) params.projectId = filters.projectId
    if (filters.status) params.status = filters.status
    if (filters.priority) params.priority = filters.priority
    if (filters.dueBefore) params.dueBefore = filters.dueBefore
    if (filters.dueAfter) params.dueAfter = filters.dueAfter
    if (filters.q) params.q = filters.q
    const { data } = await tasksService.getTasks(params)
    tasks.value = data
  } catch (err) {
    loadError.value = err.response?.data?.message || 'Failed to load tasks.'
  } finally {
    loading.value = false
  }
}

function clearFilters() {
  filters.projectId = ''
  filters.status = ''
  filters.priority = ''
  filters.assigneeId = ''
  filters.dueBefore = ''
  filters.dueAfter = ''
  filters.q = ''
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
        </div>

        <div class="filter-bar">
          <div class="field">
            <label for="filter-project">Project</label>
            <select id="filter-project" v-model="filters.projectId" @change="loadTasks">
              <option value="">All projects</option>
              <option v-for="project in projects" :key="project.id" :value="project.id">{{ project.name }}</option>
            </select>
          </div>
          <div class="field">
            <label for="filter-status">Status</label>
            <select id="filter-status" v-model="filters.status" @change="loadTasks">
              <option value="">Any status</option>
              <option v-for="status in STATUSES" :key="status" :value="status">{{ status }}</option>
            </select>
          </div>
          <div class="field">
            <label for="filter-priority">Priority</label>
            <select id="filter-priority" v-model="filters.priority" @change="loadTasks">
              <option value="">Any priority</option>
              <option v-for="priority in PRIORITIES" :key="priority" :value="priority">{{ priority }}</option>
            </select>
          </div>
          <div class="field">
            <label for="filter-assignee">Assignee</label>
            <select id="filter-assignee" v-model="filters.assigneeId">
              <option value="">Anyone</option>
              <option v-for="user in assigneeOptions" :key="user.id" :value="user.id">
                {{ user.firstName }} {{ user.lastName }}
              </option>
            </select>
          </div>
          <div class="field">
            <label for="filter-due-after">Due after</label>
            <input id="filter-due-after" v-model="filters.dueAfter" type="date" @change="loadTasks" />
          </div>
          <div class="field">
            <label for="filter-due-before">Due before</label>
            <input id="filter-due-before" v-model="filters.dueBefore" type="date" @change="loadTasks" />
          </div>
          <div class="field">
            <label for="filter-q">Search</label>
            <input id="filter-q" v-model="filters.q" type="text" placeholder="Title or description" @keyup.enter="loadTasks" />
          </div>
          <button class="btn-secondary" type="button" @click="loadTasks">Apply</button>
          <button class="btn-link" type="button" @click="clearFilters">Clear</button>
        </div>

        <div v-if="loading" class="empty-state">Loading tasks…</div>
        <div v-else-if="loadError" class="alert-error">{{ loadError }}</div>
        <div v-else-if="!filteredTasks.length" class="empty-state">No tasks match these filters.</div>
        <table v-else class="table">
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
            <tr v-for="task in filteredTasks" :key="task.id">
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
      </div>
    </main>
  </div>
</template>
