<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps<{ content: string; streaming?: boolean }>()
const html = ref('')
const root = ref<HTMLElement | null>(null)
let renderVersion = 0

watch(
  () => props.content,
  async (content) => {
    const version = ++renderVersion
    const rendered = await renderMarkdown(content)
    if (version !== renderVersion) return
    html.value = rendered
    await nextTick()
  },
  { immediate: true },
)

const handleClick = async (event: MouseEvent) => {
  const target = event.target as HTMLElement
  const button = target.closest<HTMLButtonElement>('.code-copy')
  if (!button) return
  const code = button.parentElement?.querySelector('code')?.textContent ?? ''
  await navigator.clipboard.writeText(code)
  button.textContent = '已复制'
  window.setTimeout(() => (button.textContent = '复制'), 1200)
}
</script>

<template>
  <div ref="root" class="markdown-content" :class="{ streaming }" @click="handleClick" v-html="html"></div>
</template>

<style scoped>
.markdown-content { color: var(--text); font-size: 13px; line-height: 1.82; word-break: break-word; }
.markdown-content.streaming::after { display: inline-block; width: 6px; height: 15px; margin-left: 3px; border-radius: 2px; background: var(--brand); vertical-align: -2px; animation: cursor-blink .9s ease-in-out infinite; content: ''; }
.markdown-content :deep(p) { margin: 0 0 12px; }.markdown-content :deep(p:last-child) { margin-bottom: 0; }
.markdown-content :deep(h1),.markdown-content :deep(h2),.markdown-content :deep(h3) { margin: 22px 0 10px; color: var(--text-strong); font-weight: 680; letter-spacing: -.02em; line-height: 1.4; }
.markdown-content :deep(h1) { font-size: 20px; }.markdown-content :deep(h2) { font-size: 17px; }.markdown-content :deep(h3) { font-size: 15px; }
.markdown-content :deep(ul),.markdown-content :deep(ol) { margin: 9px 0 13px; padding-left: 22px; }.markdown-content :deep(li) { margin: 4px 0; }
.markdown-content :deep(a) { color: var(--brand); text-decoration: underline; text-decoration-color: color-mix(in srgb,var(--brand) 35%,transparent); text-underline-offset: 3px; }
.markdown-content :deep(blockquote) { margin: 14px 0; padding: 9px 13px; border-left: 3px solid var(--brand); border-radius: 0 8px 8px 0; background: var(--brand-softer); color: var(--text-muted); }
.markdown-content :deep(code:not(pre code)) { padding: 2px 5px; border: 1px solid var(--border); border-radius: 5px; background: var(--bg-subtle); color: var(--brand); font-family: "SFMono-Regular",Consolas,monospace; font-size: .9em; }
.markdown-content :deep(.code-block) { position: relative; margin: 15px 0; overflow: hidden; border: 1px solid var(--border); border-radius: 12px; background: var(--bg-subtle); }
.markdown-content :deep(.code-copy) { position: absolute; z-index: 2; top: 8px; right: 8px; padding: 4px 8px; border: 1px solid rgba(128,128,128,.2); border-radius: 6px; background: rgba(255,255,255,.8); color: #62646e; font-size: 9px; cursor: pointer; backdrop-filter: blur(8px); }
.markdown-content :deep(.shiki) { margin: 0; padding: 18px; overflow-x: auto; background: transparent !important; font-family: "SFMono-Regular",Consolas,monospace; font-size: 11px; line-height: 1.65; }
:root[data-theme='dark'] .markdown-content :deep(.shiki),:root[data-theme='dark'] .markdown-content :deep(.shiki span) { color: var(--shiki-dark) !important; background-color: var(--shiki-dark-bg) !important; }
.markdown-content :deep(table) { width: 100%; margin: 14px 0; overflow: hidden; border: 1px solid var(--border); border-collapse: separate; border-spacing: 0; border-radius: 10px; font-size: 11px; }
.markdown-content :deep(th),.markdown-content :deep(td) { padding: 9px 11px; border-right: 1px solid var(--border); border-bottom: 1px solid var(--border); text-align: left; }.markdown-content :deep(th) { background: var(--bg-subtle); color: var(--text-strong); font-weight: 650; }.markdown-content :deep(tr:last-child td) { border-bottom: 0; }.markdown-content :deep(th:last-child),.markdown-content :deep(td:last-child) { border-right: 0; }
@keyframes cursor-blink { 0%,100%{opacity:.25} 50%{opacity:1} }
</style>
