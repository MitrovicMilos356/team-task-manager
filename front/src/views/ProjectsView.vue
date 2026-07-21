<script setup>
import { onMounted, ref } from 'vue'
import AppHeader from '../components/AppHeader.vue'
import * as projectsService from '../services/projectsService'

const projects = ref([])
const loading = ref(true)
const loadError = ref('')

async function load() {
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

onMounted(load)
</script>

<template>
  <div class="app-page">
    <AppHeader />
    <main class="app-content">
      <div class="panel">
        <div class="panel-header">
          <h1>Projects</h1>
        </div>

        <div v-if="loading" class="empty-state">Loading projects…</div>
        <div v-else-if="loadError" class="alert-error">{{ loadError }}</div>
        <div v-else-if="!projects.length" class="empty-state">You are not a member of any project yet.</div>
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
                <router-link class="btn-link" :to="{ name: 'project-detail', params: { id: project.id } }">
                  View
                </router-link>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>
  </div>
</template>
