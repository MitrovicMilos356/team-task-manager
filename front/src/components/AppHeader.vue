<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { isDark, toggleTheme } from '../theme'

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
      <router-link v-if="auth.isAdmin" to="/admin/audit-log">Audit log</router-link>
    </nav>
    <div class="app-header-user">
      <button
        class="theme-toggle"
        type="button"
        :aria-label="isDark ? 'Switch to light mode' : 'Switch to dark mode'"
        :title="isDark ? 'Switch to light mode' : 'Switch to dark mode'"
        @click="toggleTheme"
      >
        {{ isDark ? '☀️' : '🌙' }}
      </button>
      <span>{{ auth.user?.email }}</span>
      <button class="btn-link" type="button" @click="onLogout">Log out</button>
    </div>
  </header>
</template>
