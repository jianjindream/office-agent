<script setup lang="ts">
import { ChevronRight, FileText, Quote } from 'lucide-vue-next'
import type { SearchResult } from '@/types/api'

defineProps<{ sources: SearchResult[] }>()

const sourceTitle = (source: SearchResult, index: number) => {
  const metadata = source.chunk?.metadata
  return String(metadata?.title ?? metadata?.filename ?? `知识库片段 ${index + 1}`)
}
</script>

<template>
  <div v-if="sources.length" class="sources">
    <div class="sources-title"><Quote :size="13" /><span>参考了 {{ sources.length }} 个知识片段</span></div>
    <div class="source-grid">
      <button v-for="(source, index) in sources.slice(0, 4)" :key="source.chunk?.id ?? index" type="button">
        <span class="source-index">{{ index + 1 }}</span>
        <span class="source-copy"><strong>{{ sourceTitle(source, index) }}</strong><small>{{ Math.round(source.similarity * 100) }}% 相关 · {{ source.chunk?.content?.slice(0, 55) || '知识库内容' }}</small></span>
        <ChevronRight :size="13" />
      </button>
    </div>
  </div>
</template>

<style scoped>
.sources { margin-top: 16px; padding-top: 13px; border-top: 1px solid var(--border); }.sources-title { display: flex; align-items: center; gap: 6px; color: var(--text-faint); font-size: 8px; font-weight: 620; }.source-grid { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 7px; margin-top: 8px; }.source-grid button { display: flex; min-width: 0; align-items: center; gap: 8px; padding: 9px; border: 1px solid var(--border); border-radius: 9px; background: var(--bg-subtle); text-align: left; cursor: pointer; }.source-grid button:hover { border-color: color-mix(in srgb,var(--brand) 22%,var(--border)); background: var(--brand-softer); }.source-index { display: inline-flex; align-items: center; justify-content: center; width: 20px; height: 20px; flex: 0 0 auto; border-radius: 6px; background: var(--brand-soft); color: var(--brand); font-size: 8px; font-weight: 700; }.source-copy { display: flex; min-width: 0; flex: 1; flex-direction: column; }.source-copy strong,.source-copy small { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.source-copy strong { color: var(--text); font-size: 8px; font-weight: 620; }.source-copy small { margin-top: 3px; color: var(--text-faint); font-size: 7px; }.source-grid svg { color: var(--text-faint); }
@media(max-width:700px){.source-grid{grid-template-columns:1fr}}
</style>
