<script setup>
import { onMounted, reactive, ref } from 'vue'
import AppHeader from '../../components/AppHeader.vue'
import * as projectsService from '../../services/projectsService'

const projects = ref([])
const loading = ref(true)
const loadError = ref('')

const modalOpen = ref(false)
const modalMode = ref('create')
const editingId = ref(null)
const submitting = ref(false)
const serverError = ref('')

const form = reactive({ name: '', description: '' })
const errors = reactive({ name: '' })

async function loadProjects() {
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await projectsService.getProjects()
    projects.value = data
  } catch (err) {
    loadError.value = err.response?.data?.message || 'Failed to load projects.'
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.name = ''
  form.description = ''
  errors.name = ''
  serverError.value = ''
}

function openCreateModal() {
  resetForm()
  modalMode.value = 'create'
  editingId.value = null
  modalOpen.value = true
}

function openEditModal(project) {
  resetForm()
  modalMode.value = 'edit'
  editingId.value = project.id
  form.name = project.name
  form.description = project.description || ''
  modalOpen.value = true
}

function closeModal() {
  modalOpen.value = false
}

function validate() {
  errors.name = form.name ? '' : 'Name is required.'
  return !errors.name
}

async function onSubmit() {
  serverError.value = ''
  if (!validate()) return

  submitting.value = true
  try {
    const payload = { name: form.name, description: form.description }
    if (modalMode.value === 'create') {
      await projectsService.createProject(payload)
    } else {
      await projectsService.updateProject(editingId.value, payload)
    }
    modalOpen.value = false
    await loadProjects()
  } catch (err) {
    serverError.value = err.response?.data?.message || 'Could not save project.'
  } finally {
    submitting.value = false
  }
}

async function onDeactivate(project) {
  try {
    await projectsService.deactivateProject(project.id)
    await loadProjects()
  } catch (err) {
    loadError.value = err.response?.data?.message || 'Could not deactivate project.'
  }
}

onMounted(loadProjects)
</script>

<template>
  <div class="app-page">
    <AppHeader />
    <main class="app-content">
      <div class="panel">
        <div class="panel-header">
          <h1>Projects</h1>
          <button class="btn-primary" type="button" style="width:auto" @click="openCreateModal">
            Add project
          </button>
        </div>

        <div v-if="loading" class="empty-state">Loading projects…</div>
        <div v-else-if="loadError" class="alert-error">{{ loadError }}</div>
        <div v-else-if="!projects.length" class="empty-state">No projects yet. Add the first one.</div>
        <table v-else class="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Description</th>
              <th>Members</th>
              <th>Open tasks</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="project in projects" :key="project.id">
              <td>{{ project.name }}</td>
              <td>{{ project.description }}</td>
              <td>{{ project.memberCount }}</td>
              <td>{{ project.openTaskCount }}</td>
              <td>
                <span class="badge" :class="project.active ? 'badge-success' : 'badge-error'">
                  {{ project.active ? 'Active' : 'Inactive' }}
                </span>
              </td>
              <td>
                <div class="table-actions">
                  <router-link class="btn-link" :to="{ name: 'admin-project-detail', params: { id: project.id } }">
                    Manage
                  </router-link>
                  <button class="btn-link" type="button" @click="openEditModal(project)">Edit</button>
                  <button v-if="project.active" class="btn-link" type="button" @click="onDeactivate(project)">
                    Deactivate
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>

    <div v-if="modalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card">
        <h2>{{ modalMode === 'create' ? 'Add project' : 'Edit project' }}</h2>

        <div v-if="serverError" class="alert-error">{{ serverError }}</div>

        <form @submit.prevent="onSubmit" novalidate>
          <div class="field">
            <label for="name">Name</label>
            <input id="name" v-model.trim="form.name" type="text" />
            <div v-if="errors.name" class="field-error">{{ errors.name }}</div>
          </div>

          <div class="field">
            <label for="description">Description</label>
            <textarea id="description" v-model.trim="form.description" rows="3"></textarea>
          </div>

          <div class="modal-actions">
            <button class="btn-secondary" type="button" @click="closeModal">Cancel</button>
            <button class="btn-primary" type="submit" style="width:auto" :disabled="submitting">
              {{ submitting ? 'Saving…' : 'Save' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
