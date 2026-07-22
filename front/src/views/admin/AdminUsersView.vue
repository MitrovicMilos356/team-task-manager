<script setup>
import { onMounted, reactive, ref } from 'vue'
import AppHeader from '../../components/AppHeader.vue'
import * as usersAdminService from '../../services/usersAdminService'

const ROLES = ['admin', 'member']

const users = ref([])
const loading = ref(true)
const loadError = ref('')

const modalOpen = ref(false)
const modalMode = ref('create')
const editingId = ref(null)
const submitting = ref(false)
const serverError = ref('')

const form = reactive({ firstName: '', lastName: '', email: '', password: '', role: 'member' })
const errors = reactive({ firstName: '', lastName: '', email: '', password: '', role: '' })

async function loadUsers() {
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await usersAdminService.getUsers()
    users.value = data
  } catch (err) {
    loadError.value = err.response?.data?.message || 'Failed to load users.'
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.firstName = ''
  form.lastName = ''
  form.email = ''
  form.password = ''
  form.role = 'member'
  errors.firstName = ''
  errors.lastName = ''
  errors.email = ''
  errors.password = ''
  errors.role = ''
  serverError.value = ''
}

function openCreateModal() {
  resetForm()
  modalMode.value = 'create'
  editingId.value = null
  modalOpen.value = true
}

function openEditModal(user) {
  resetForm()
  modalMode.value = 'edit'
  editingId.value = user.id
  form.firstName = user.firstName
  form.lastName = user.lastName
  form.email = user.email
  form.role = user.role
  modalOpen.value = true
}

function closeModal() {
  modalOpen.value = false
}

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

  if (modalMode.value === 'create') {
    errors.password = form.password && form.password.length >= 8
      ? ''
      : 'Password must be at least 8 characters.'
  }

  errors.role = form.role ? '' : 'Role is required.'

  return !errors.firstName && !errors.lastName && !errors.email && !errors.password && !errors.role
}

async function onSubmit() {
  serverError.value = ''
  if (!validate()) return

  submitting.value = true
  try {
    if (modalMode.value === 'create') {
      await usersAdminService.createUser({
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password,
        role: form.role
      })
    } else {
      await usersAdminService.updateUser(editingId.value, {
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        role: form.role
      })
    }
    modalOpen.value = false
    await loadUsers()
  } catch (err) {
    serverError.value = err.response?.data?.message || 'Could not save user.'
  } finally {
    submitting.value = false
  }
}

async function toggleActive(user) {
  try {
    await usersAdminService.updateUserStatus(user.id, !user.active)
    await loadUsers()
  } catch (err) {
    loadError.value = err.response?.data?.message || 'Could not update user status.'
  }
}

onMounted(loadUsers)
</script>

<template>
  <div class="app-page">
    <AppHeader />
    <main class="app-content">
      <div class="panel">
        <div class="panel-header">
          <h1>User administration</h1>
          <button class="btn-primary" type="button" style="width:auto" @click="openCreateModal">
            Add user
          </button>
        </div>

        <div v-if="loading" class="empty-state">Loading users…</div>
        <div v-else-if="loadError" class="alert-error">{{ loadError }}</div>
        <div v-else-if="!users.length" class="empty-state">No users yet. Add the first one.</div>
        <table v-else class="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.firstName }} {{ user.lastName }}</td>
              <td>{{ user.email }}</td>
              <td>{{ user.role }}</td>
              <td>
                <span class="badge" :class="user.active ? 'badge-success' : 'badge-error'">
                  {{ user.active ? 'Active' : 'Inactive' }}
                </span>
              </td>
              <td>
                <div class="table-actions">
                  <button class="btn-link" type="button" @click="openEditModal(user)">Edit</button>
                  <button class="btn-link" type="button" @click="toggleActive(user)">
                    {{ user.active ? 'Deactivate' : 'Activate' }}
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
        <h2>{{ modalMode === 'create' ? 'Add user' : 'Edit user' }}</h2>

        <div v-if="serverError" class="alert-error">{{ serverError }}</div>

        <form @submit.prevent="onSubmit" novalidate>
          <div class="two-col">
            <div class="field">
              <label for="firstName">First name</label>
              <input id="firstName" v-model.trim="form.firstName" type="text" />
              <div v-if="errors.firstName" class="field-error">{{ errors.firstName }}</div>
            </div>
            <div class="field">
              <label for="lastName">Last name</label>
              <input id="lastName" v-model.trim="form.lastName" type="text" />
              <div v-if="errors.lastName" class="field-error">{{ errors.lastName }}</div>
            </div>
          </div>

          <div class="field">
            <label for="email">Email</label>
            <input id="email" v-model.trim="form.email" type="email" />
            <div v-if="errors.email" class="field-error">{{ errors.email }}</div>
          </div>

          <div v-if="modalMode === 'create'" class="field">
            <label for="password">Password</label>
            <input id="password" v-model="form.password" type="password" />
            <div v-if="errors.password" class="field-error">{{ errors.password }}</div>
          </div>

          <div class="field">
            <label for="role">Role</label>
            <select id="role" v-model="form.role">
              <option v-for="role in ROLES" :key="role" :value="role">{{ role }}</option>
            </select>
            <div v-if="errors.role" class="field-error">{{ errors.role }}</div>
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
