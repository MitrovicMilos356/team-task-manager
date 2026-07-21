<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()

function onLogout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <header class="app-header">
    <div class="app-header-brand">Team Task Manager</div>
    <nav class="app-header-nav">
      <router-link to="/dashboard">Dashboard</router-link>
      <router-link to="/projects">Projects</router-link>
      <router-link to="/tasks">Tasks</router-link>
      <router-link v-if="auth.isAdmin" to="/admin/users">Manage users</router-link>
      <router-link v-if="auth.isAdmin" to="/admin/projects">Manage projects</router-link>
    </nav>
    <div class="app-header-user">
      <span>{{ auth.user?.email }}</span>
      <button class="btn-link" type="button" @click="onLogout">Log out</button>
    </div>
  </header>
</template>
