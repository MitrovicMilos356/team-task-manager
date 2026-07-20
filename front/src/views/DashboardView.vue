<script setup>
import { useAuthStore } from '../store/auth'
import AppHeader from '../components/AppHeader.vue'

const auth = useAuthStore()
</script>

<template>
  <div class="app-page">
    <AppHeader />
    <main class="app-content">
      <div class="auth-card">
        <h1>Welcome{{ auth.user ? `, ${auth.user.firstName}` : '' }}</h1>
        <p class="subtitle">You are signed in as {{ auth.user?.email }} ({{ auth.user?.role }})</p>
      </div>
    </main>
  </div>
</template>
