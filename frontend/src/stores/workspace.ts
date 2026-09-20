import { defineStore } from 'pinia'

type Theme = 'light' | 'dark'

const readTheme = (): Theme => {
  const stored = localStorage.getItem('dreamloop-theme')
  return stored === 'dark' ? 'dark' : 'light'
}

export const useWorkspaceStore = defineStore('workspace', {
  state: () => ({
    sidebarCollapsed: false,
    contextOpen: true,
    theme: readTheme() as Theme,
    knowledgeEnabled: true,
  }),
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    toggleContext() {
      this.contextOpen = !this.contextOpen
    },
    toggleKnowledge() {
      this.knowledgeEnabled = !this.knowledgeEnabled
    },
    toggleTheme() {
      this.theme = this.theme === 'light' ? 'dark' : 'light'
      localStorage.setItem('dreamloop-theme', this.theme)
      this.applyTheme()
    },
    applyTheme() {
      document.documentElement.dataset.theme = this.theme
    },
  },
})
