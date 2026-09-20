<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { useWorkspaceStore } from '@/stores/workspace'

const workspace = useWorkspaceStore()

onMounted(() => workspace.applyTheme())
</script>

<template>
  <RouterView />
</template>
