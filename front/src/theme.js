import { ref } from 'vue'

const STORAGE_KEY = 'ttm_theme'

export const isDark = ref(false)

function apply(dark) {
  document.documentElement.classList.toggle('dark', dark)
}

export function initTheme() {
  const stored = localStorage.getItem(STORAGE_KEY)
  const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)').matches
  isDark.value = stored ? stored === 'dark' : Boolean(prefersDark)
  apply(isDark.value)
}

export function toggleTheme() {
  isDark.value = !isDark.value
  apply(isDark.value)
  localStorage.setItem(STORAGE_KEY, isDark.value ? 'dark' : 'light')
}
