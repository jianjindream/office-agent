<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import AppSidebar from '@/components/app/AppSidebar.vue'
import AppTopbar from '@/components/app/AppTopbar.vue'
import ContextPanel from '@/components/app/ContextPanel.vue'
import { useWorkspaceStore } from '@/stores/workspace'

const route = useRoute()
const workspace = useWorkspaceStore()
const showContext = computed(() => Boolean(route.meta.showContext) && workspace.contextOpen)
</script>

<template>
  <div
    class="workspace-layout"
    :class="{
      'sidebar-collapsed': workspace.sidebarCollapsed,
      'context-visible': showContext,
    }"
  >
    <AppSidebar />
    <section class="workspace-main">
      <AppTopbar />
      <main class="page-stage">
        <RouterView />
      </main>
    </section>
    <ContextPanel v-if="showContext" />
  </div>
</template>

<style scoped>
.workspace-layout {
  display: grid;
  grid-template-columns: var(--sidebar-width) minmax(0, 1fr);
  width: 100%;
  height: 100%;
  background:
    radial-gradient(circle at 72% 0%, rgba(111, 101, 235, 0.055), transparent 28%),
    var(--bg-app);
  transition: grid-template-columns 180ms ease;
}

.workspace-layout.sidebar-collapsed {
  grid-template-columns: var(--sidebar-collapsed) minmax(0, 1fr);
}

.workspace-layout.context-visible {
  grid-template-columns: var(--sidebar-width) minmax(0, 1fr) 320px;
}

.workspace-layout.sidebar-collapsed.context-visible {
  grid-template-columns: var(--sidebar-collapsed) minmax(0, 1fr) 320px;
}

.workspace-main {
  display: grid;
  grid-template-rows: var(--topbar-height) minmax(0, 1fr);
  min-width: 0;
  min-height: 0;
}

.page-stage {
  min-width: 0;
  min-height: 0;
  overflow: auto;
}

@media (max-width: 1180px) {
  .workspace-layout.context-visible,
  .workspace-layout.sidebar-collapsed.context-visible {
    grid-template-columns: var(--sidebar-width) minmax(0, 1fr);
  }

  .workspace-layout.sidebar-collapsed.context-visible {
    grid-template-columns: var(--sidebar-collapsed) minmax(0, 1fr);
  }
}

@media (max-width: 860px) {
  .workspace-layout,
  .workspace-layout.context-visible {
    grid-template-columns: var(--sidebar-collapsed) minmax(0, 1fr);
  }
}

@media (max-width: 600px) {
  .workspace-layout,
  .workspace-layout.context-visible,
  .workspace-layout.sidebar-collapsed,
  .workspace-layout.sidebar-collapsed.context-visible {
    grid-template-columns: 1fr;
  }
}
</style>
