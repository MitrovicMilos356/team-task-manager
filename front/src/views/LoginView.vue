<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()

const form = reactive({
  email: '',
  password: ''
})

const errors = reactive({ email: '', password: '' })
const serverError = ref('')
const submitting = ref(false)

function validate() {
  errors.email = ''
  errors.password = ''

  if (!form.email) {
    errors.email = 'Email is required.'
  } else if (!/^\S+@\S+\.\S+$/.test(form.email)) {
    errors.email = 'Enter a valid email address.'
  }

  if (!form.password) {
    errors.password = 'Password is required.'
  }

  return !errors.email && !errors.password
}

async function onSubmit() {
  serverError.value = ''
  if (!validate()) return

  submitting.value = true
  try {
    await auth.login({ email: form.email, password: form.password })
    router.push({ name: 'dashboard' })
  } catch (err) {
    serverError.value = err.response?.data?.message || 'Login failed. Check your credentials.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>Welcome back</h1>
      <p class="subtitle">Sign in to Team Task Manager</p>

      <div v-if="serverError" class="alert-error">{{ serverError }}</div>

      <form @submit.prevent="onSubmit" novalidate>
        <div class="field">
          <label for="email">Email</label>
          <input id="email" v-model.trim="form.email" type="email" autocomplete="email" />
          <div v-if="errors.email" class="field-error">{{ errors.email }}</div>
        </div>

        <div class="field">
          <label for="password">Password</label>
          <input id="password" v-model="form.password" type="password" autocomplete="current-password" />
          <div v-if="errors.password" class="field-error">{{ errors.password }}</div>
        </div>

        <button class="btn-primary" type="submit" :disabled="submitting">
          {{ submitting ? 'Signing in…' : 'Sign in' }}
        </button>
      </form>

      <p class="switch-link">
        Don't have an account? <router-link to="/signup">Sign up</router-link>
      </p>
    </div>
  </div>
</template>
