<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '../components/AppHeader.vue'
import { useAuthStore } from '../store/auth'
import * as projectsService from '../services/projectsService'
import * as projectMembersService from '../services/projectMembersService'
import * as tasksService from '../services/tasksService'

const PRIORITIES = ['low', 'medium', 'high', 'urgent']

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const projectId = Number(route.params.id)

const project = ref(null)
const members = ref([])
const tasks = ref([])
const loading = ref(true)
const loadError = ref('')

const taskModalOpen = ref(false)
const taskSubmitting = ref(false)
const taskServerError = ref('')
const taskForm = reactive({ title: '', description: '', priority: 'medium', assignedUserId: '', dueDate: '' })
const taskErrors = reactive({ title: '' })

const canCreateTasks = computed(() => {
  if (auth.isAdmin) return true
  return members.value.some((m) => m.id === auth.user?.id)
})

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    const [projectRes, membersRes, tasksRes] = await Promise.all([
      projectsService.getProject(projectId),
      projectMembersService.getMembers(projectId),
      tasksService.getTasksByProject(projectId)
    ])
    project.value = projectRes.data
    members.value = membersRes.data.map((m) => ({ ...m.user, joinedAt: m.joinedAt }))
    tasks.value = tasksRes.data
  } catch (err) {
    loadError.value = err.response?.data?.message || 'Failed to load project.'
  } finally {
    loading.value = false
  }
}

function openTaskModal() {
  taskForm.title = ''
  taskForm.description = ''
  taskForm.priority = 'medium'
  taskForm.assignedUserId = ''
  taskForm.dueDate = ''
  taskErrors.title = ''
  taskServerError.value = ''
  taskModalOpen.value = true
}

function closeTaskModal() {
  taskModalOpen.value = false
}

function validateTask() {
  taskErrors.title = taskForm.title ? '' : 'Title is required.'
  return !taskErrors.title
}

async function onCreateTask() {
  taskServerError.value = ''
  if (!validateTask()) return

  taskSubmitting.value = true
  try {
    await tasksService.createTask({
      projectId,
      title: taskForm.title,
      description: taskForm.description,
      priority: taskForm.priority,
      assignedUserId: taskForm.assignedUserId ? Number(taskForm.assignedUserId) : null,
      dueDate: taskForm.dueDate || null
    })
    taskModalOpen.value = false
    await load()
  } catch (err) {
    taskServerError.value = err.response?.data?.message || 'Could not create task.'
  } finally {
    taskSubmitting.value = false
  }
}

function backToProjects() {
  router.push({ name: 'projects' })
}

onMounted(load)
</script>

<template>
  <div class="app-page">
    <AppHeader />
    <main class="app-content">
      <div v-if="loading" class="empty-state">Loading project…</div>
      <div v-else-if="loadError" class="alert-error">{{ loadError }}</div>
      <template v-else>
        <div class="panel" style="margin-bottom: 24px">
          <div class="panel-header">
            <div>
              <h1>{{ project.name }}</h1>
              <p class="subtitle" style="margin:4px 0 0">{{ project.description }}</p>
            </div>
            <button class="btn-secondary" type="button" @click="backToProjects">Back to projects</button>
          </div>
          <span class="badge" :class="project.active ? 'badge-success' : 'badge-error'">
            {{ project.active ? 'Active' : 'Inactive' }}
          </span>
        </div>

        <div class="panel" style="margin-bottom: 24px">
          <div class="panel-header">
            <h1>Members</h1>
          </div>
          <div v-if="!members.length" class="empty-state">No members yet.</div>
          <table v-else class="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Member since</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="member in members" :key="member.id">
                <td>{{ member.firstName }} {{ member.lastName }}</td>
                <td>{{ member.email }}</td>
                <td>{{ member.role }}</td>
                <td>{{ member.joinedAt ? member.joinedAt.slice(0, 10) : '—' }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="panel">
          <div class="panel-header">
            <h1>Tasks</h1>
            <button
              v-if="canCreateTasks"
              class="btn-primary"
              type="button"
              style="width:auto"
              @click="openTaskModal"
            >
              Add task
            </button>
          </div>

          <div v-if="!tasks.length" class="empty-state">No tasks yet.</div>
          <table v-else class="table">
            <thead>
              <tr>
                <th>Title</th>
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
                <td>{{ task.assignedUser ? `${task.assignedUser.firstName} ${task.assignedUser.lastName}` : 'Unassigned' }}</td>
                <td>{{ task.status }}</td>
                <td>{{ task.priority }}</td>
                <td>{{ task.dueDate || '—' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </main>

    <div v-if="taskModalOpen" class="modal-overlay" @click.self="closeTaskModal">
      <div class="modal-card">
        <h2>Add task</h2>

        <div v-if="taskServerError" class="alert-error">{{ taskServerError }}</div>

        <form @submit.prevent="onCreateTask" novalidate>
          <div class="field">
            <label for="task-title">Title</label>
            <input id="task-title" v-model.trim="taskForm.title" type="text" />
            <div v-if="taskErrors.title" class="field-error">{{ taskErrors.title }}</div>
          </div>

          <div class="field">
            <label for="task-description">Description</label>
            <textarea id="task-description" v-model.trim="taskForm.description" rows="3"></textarea>
          </div>

          <div class="two-col">
            <div class="field">
              <label for="task-priority">Priority</label>
              <select id="task-priority" v-model="taskForm.priority">
                <option v-for="priority in PRIORITIES" :key="priority" :value="priority">{{ priority }}</option>
              </select>
            </div>
            <div class="field">
              <label for="task-due-date">Due date</label>
              <input id="task-due-date" v-model="taskForm.dueDate" type="date" />
            </div>
          </div>

          <div class="field">
            <label for="task-assignee">Assignee</label>
            <select id="task-assignee" v-model="taskForm.assignedUserId">
              <option value="">Unassigned</option>
              <option v-for="member in members" :key="member.id" :value="member.id">
                {{ member.firstName }} {{ member.lastName }}
              </option>
            </select>
          </div>

          <div class="modal-actions">
            <button class="btn-secondary" type="button" @click="closeTaskModal">Cancel</button>
            <button class="btn-primary" type="submit" style="width:auto" :disabled="taskSubmitting">
              {{ taskSubmitting ? 'Saving…' : 'Save' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
