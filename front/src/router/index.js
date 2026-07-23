import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'
import LoginView from '../views/LoginView.vue'
import SignupView from '../views/SignupView.vue'
import DashboardView from '../views/DashboardView.vue'
import AdminUsersView from '../views/admin/AdminUsersView.vue'
import AdminProjectsView from '../views/admin/AdminProjectsView.vue'
import AdminProjectDetailView from '../views/admin/AdminProjectDetailView.vue'
import AdminAuditLogView from '../views/admin/AdminAuditLogView.vue'
import ProjectsView from '../views/ProjectsView.vue'
import ProjectDetailView from '../views/ProjectDetailView.vue'
import TasksView from '../views/TasksView.vue'
import TaskDetailView from '../views/TaskDetailView.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'login', component: LoginView, meta: { guestOnly: true } },
  { path: '/signup', name: 'signup', component: SignupView, meta: { guestOnly: true } },
  { path: '/dashboard', name: 'dashboard', component: DashboardView, meta: { requiresAuth: true } },
  { path: '/projects', name: 'projects', component: ProjectsView, meta: { requiresAuth: true } },
  { path: '/projects/:id', name: 'project-detail', component: ProjectDetailView, meta: { requiresAuth: true } },
  { path: '/tasks', name: 'tasks', component: TasksView, meta: { requiresAuth: true } },
  { path: '/tasks/:id', name: 'task-detail', component: TaskDetailView, meta: { requiresAuth: true } },
  {
    path: '/admin',
    redirect: '/admin/users',
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: 'users', name: 'admin-users', component: AdminUsersView, meta: { requiresAuth: true, requiresAdmin: true } },
      { path: 'projects', name: 'admin-projects', component: AdminProjectsView, meta: { requiresAuth: true, requiresAdmin: true } },
      { path: 'projects/:id', name: 'admin-project-detail', component: AdminProjectDetailView, meta: { requiresAuth: true, requiresAdmin: true } },
      { path: 'audit-log', name: 'admin-audit-log', component: AdminAuditLogView, meta: { requiresAuth: true, requiresAdmin: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login' }
  }

  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return { name: 'dashboard' }
  }

  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }
})

export default router
