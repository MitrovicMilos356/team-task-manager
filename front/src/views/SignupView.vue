<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()

const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  password: ''
})

const errors = reactive({
  firstName: '',
  lastName: '',
  email: '',
  password: ''
})

const serverError = ref('')
const submitting = ref(false)

function validate() {
  errors.firstName = form.firstName ? '' : 'First name is required.'
  errors.lastName = form.lastName ? '' : 'Last name is required.'

  if (!form.email) {
    errors.email = 'Email is required.'
  } else if (!/^\S+@\S+\.\S+$/.test(form.email)) {
    errors.email = 'Enter a valid email address.'
  } else {
    errors.email = ''
  }

  errors.password = form.password.length >= 8 ? '' : 'Password must be at least 8 characters.'

  return !errors.firstName && !errors.lastName && !errors.email && !errors.password
}

async function onSubmit() {
  serverError.value = ''
  if (!validate()) return

  submitting.value = true
  try {
    await auth.register({
      firstName: form.firstName,
      lastName: form.lastName,
      email: form.email,
      password: form.password
    })
    router.push({ name: 'login' })
  } catch (err) {
    serverError.value = err.response?.data?.message || 'Registration failed. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>Create account</h1>
      <p class="subtitle">Join Team Task Manager</p>

      <div v-if="serverError" class="alert-error">{{ serverError }}</div>

      <form @submit.prevent="onSubmit" novalidate>
        <div class="two-col">
          <div class="field">
            <label for="firstName">First name</label>
            <input id="firstName" v-model.trim="form.firstName" type="text" autocomplete="given-name" />
            <div v-if="errors.firstName" class="field-error">{{ errors.firstName }}</div>
          </div>

          <div class="field">
            <label for="lastName">Last name</label>
            <input id="lastName" v-model.trim="form.lastName" type="text" autocomplete="family-name" />
            <div v-if="errors.lastName" class="field-error">{{ errors.lastName }}</div>
          </div>
        </div>

        <div class="field">
          <label for="email">Email</label>
          <input id="email" v-model.trim="form.email" type="email" autocomplete="email" />
          <div v-if="errors.email" class="field-error">{{ errors.email }}</div>
        </div>

        <div class="field">
          <label for="password">Password</label>
          <input id="password" v-model="form.password" type="password" autocomplete="new-password" />
          <div v-if="errors.password" class="field-error">{{ errors.password }}</div>
        </div>

        <button class="btn-primary" type="submit" :disabled="submitting">
          {{ submitting ? 'Creating account…' : 'Sign up' }}
        </button>
      </form>

      <p class="switch-link">
        Already have an account? <router-link to="/login">Sign in</router-link>
      </p>
    </div>
  </div>
</template>
