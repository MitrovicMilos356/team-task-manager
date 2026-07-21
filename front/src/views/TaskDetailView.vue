<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '../components/AppHeader.vue'
import { useAuthStore } from '../store/auth'
import * as projectsService from '../services/projectsService'
import * as tasksService from '../services/tasksService'

const STATUSES = ['todo', 'in_progress', 'in_review', 'done', 'canceled']

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const taskId = Number(route.params.id)

const task = ref(null)
const project = ref(null)
const comments = ref([])
const history = ref([])
const loading = ref(true)
const loadError = ref('')

const statusValue = ref('')
const statusSubmitting = ref(false)
const statusError = ref('')

const commentText = ref('')
const commentSubmitting = ref(false)
const commentError = ref('')

const canChangeStatus = computed(() => {
  if (!task.value) return false
  return auth.isAdmin || task.value.assignedUser?.id === auth.user?.id
})

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    const { data: taskData } = await tasksService.getTask(taskId)
    task.value = taskData
    statusValue.value = taskData.status

    const [projectRes, commentsRes, historyRes] = await Promise.all([
      projectsService.getProject(taskData.projectId),
      tasksService.getComments(taskId),
      tasksService.getHistory(taskId)
    ])
    project.value = projectRes.data
    comments.value = commentsRes.data
    history.value = historyRes.data
  } catch (err) {
    loadError.value = err.response?.data?.message || 'Failed to load task.'
  } finally {
    loading.value = false
  }
}

async function onUpdateStatus() {
  statusError.value = ''
  statusSubmitting.value = true
  try {
    const { data } = await tasksService.updateStatus(taskId, statusValue.value)
    task.value = data
    const { data: historyData } = await tasksService.getHistory(taskId)
    history.value = historyData
  } catch (err) {
    statusError.value = err.response?.data?.message || 'Could not update status.'
  } finally {
    statusSubmitting.value = false
  }
}

async function onAddComment() {
  commentError.value = ''
  if (!commentText.value.trim()) {
    commentError.value = 'Comment cannot be empty.'
    return
  }
  commentSubmitting.value = true
  try {
    await tasksService.addComment(taskId, commentText.value.trim())
    commentText.value = ''
    const { data } = await tasksService.getComments(taskId)
    comments.value = data
  } catch (err) {
    commentError.value = err.response?.data?.message || 'Could not post comment.'
  } finally {
    commentSubmitting.value = false
  }
}

function backToProject() {
  if (project.value) router.push({ name: 'project-detail', params: { id: project.value.id } })
  else router.push({ name: 'tasks' })
}

onMounted(load)
</script>

<template>
  <div class="app-page">
    <AppHeader />
    <main class="app-content">
      <div v-if="loading" class="empty-state">Loading task…</div>
      <div v-else-if="loadError" class="alert-error">{{ loadError }}</div>
      <template v-else-if="task">
        <div class="panel" style="margin-bottom: 24px">
          <div class="panel-header">
            <div>
              <h1>{{ task.title }}</h1>
              <p class="subtitle" style="margin:4px 0 0">
                {{ project?.name }} · Priority: {{ task.priority }} · Due: {{ task.dueDate || '—' }}
              </p>
            </div>
            <button class="btn-secondary" type="button" @click="backToProject">Back to project</button>
          </div>
          <p>{{ task.description || 'No description.' }}</p>
          <p class="subtitle">
            Assignee: {{ task.assignedUser ? `${task.assignedUser.firstName} ${task.assignedUser.lastName}` : 'Unassigned' }}
          </p>

          <div v-if="statusError" class="alert-error">{{ statusError }}</div>
          <div class="two-col" style="align-items: end; max-width: 420px">
            <div class="field" style="margin-bottom: 0">
              <label for="status-select">Status</label>
              <select id="status-select" v-model="statusValue" :disabled="!canChangeStatus">
                <option v-for="status in STATUSES" :key="status" :value="status">{{ status }}</option>
              </select>
            </div>
            <button
              class="btn-primary"
              type="button"
              style="width:auto"
              :disabled="!canChangeStatus || statusSubmitting"
              @click="onUpdateStatus"
            >
              {{ statusSubmitting ? 'Updating…' : 'Update status' }}
            </button>
          </div>
          <p v-if="!canChangeStatus" class="subtitle" style="margin-top:8px">
            Only an admin or the assignee can change this task's status.
          </p>
        </div>

        <div class="panel" style="margin-bottom: 24px">
          <div class="panel-header">
            <h1>Comments</h1>
          </div>

          <div v-if="!comments.length" class="empty-state">No comments yet.</div>
          <div v-else class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-meta">
                {{ comment.author?.firstName }} {{ comment.author?.lastName }} ·
                {{ comment.createdAt ? comment.createdAt.slice(0, 16).replace('T', ' ') : '' }}
              </div>
              <div>{{ comment.comment }}</div>
            </div>
          </div>

          <div v-if="commentError" class="alert-error">{{ commentError }}</div>
          <div class="field">
            <label for="new-comment">Add a comment</label>
            <textarea id="new-comment" v-model="commentText" rows="3"></textarea>
          </div>
          <button class="btn-primary" type="button" style="width:auto" :disabled="commentSubmitting" @click="onAddComment">
            {{ commentSubmitting ? 'Posting…' : 'Post comment' }}
          </button>
        </div>

        <div class="panel">
          <div class="panel-header">
            <h1>Change history</h1>
          </div>
          <div v-if="!history.length" class="empty-state">No changes recorded yet.</div>
          <ul v-else class="history-list">
            <li v-for="entry in history" :key="entry.id" class="history-item">
              {{ entry.changedBy?.firstName }} {{ entry.changedBy?.lastName }} changed
              <strong>{{ entry.fieldName }}</strong> from
              <em>{{ entry.oldValue ?? '—' }}</em> to <em>{{ entry.newValue ?? '—' }}</em>
              · {{ entry.createdAt ? entry.createdAt.slice(0, 16).replace('T', ' ') : '' }}
            </li>
          </ul>
        </div>
      </template>
    </main>
  </div>
</template>
