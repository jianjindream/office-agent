<script setup lang="ts">
import { Clock3, FilePlus2, FileText, LayoutGrid, List, MoreHorizontal, Plus, Search, Sparkles } from 'lucide-vue-next'

const documents = [
  { title: '第三季度经营分析', type: '经营报告', updated: '刚刚编辑', words: '2,840 字', color: 'violet', progress: 86 },
  { title: '智能办公产品需求文档', type: '产品文档', updated: '昨天编辑', words: '6,120 字', color: 'blue', progress: 64 },
  { title: '用户访谈洞察总结', type: '研究总结', updated: '9月18日', words: '1,960 字', color: 'green', progress: 92 },
  { title: 'AI 知识库实施方案', type: '项目方案', updated: '9月16日', words: '4,360 字', color: 'amber', progress: 48 },
]
</script>

<template>
  <div class="page-shell documents-page">
    <header class="page-heading">
      <div><h1>AI 文档</h1><p>将对话结果沉淀为可继续编辑和复用的工作成果。</p></div>
      <button class="primary-button" type="button"><Plus :size="17" /> 新建文档</button>
    </header>

    <section class="template-banner">
      <div class="template-art"><Sparkles :size="24" /></div>
      <div><span>AI 快速起草</span><h2>从一个想法开始，生成完整文档</h2><p>选择模板或描述目标，DreamLoop 会帮你完成结构、内容与润色。</p></div>
      <button class="secondary-button" type="button">浏览模板</button>
    </section>

    <div class="documents-toolbar">
      <div><button class="active" type="button">全部文档</button><button type="button">我的文档</button><button type="button">与我共享</button></div>
      <div class="toolbar-right">
        <label><Search :size="15" /><input placeholder="搜索文档" /></label>
        <button class="view-button active" type="button" aria-label="网格视图"><LayoutGrid :size="16" /></button>
        <button class="view-button" type="button" aria-label="列表视图"><List :size="16" /></button>
      </div>
    </div>

    <section class="document-grid">
      <button class="new-document-card" type="button">
        <span><FilePlus2 :size="22" /></span><strong>创建空白文档</strong><small>或使用 AI 从模板开始</small>
      </button>
      <article v-for="doc in documents" :key="doc.title" class="document-card surface-card">
        <div class="doc-preview" :class="doc.color">
          <div class="preview-lines"><i></i><i></i><i></i><i></i></div>
          <span><FileText :size="20" /></span>
        </div>
        <div class="document-info">
          <div class="document-title"><div><span>{{ doc.type }}</span><h3>{{ doc.title }}</h3></div><button class="icon-button" type="button"><MoreHorizontal :size="16" /></button></div>
          <div class="document-meta"><span><Clock3 :size="12" />{{ doc.updated }}</span><span>{{ doc.words }}</span></div>
          <div class="completion"><span><i :style="{ width: `${doc.progress}%` }"></i></span><small>{{ doc.progress }}% 完成</small></div>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.template-banner { position: relative; display: flex; align-items: center; gap: 18px; padding: 22px 24px; overflow: hidden; border: 1px solid color-mix(in srgb, var(--brand) 15%, var(--border)); border-radius: 17px; background: linear-gradient(115deg, var(--brand-softer), var(--bg-elevated)); }
.template-banner::after { position: absolute; top: -70px; right: 120px; width: 190px; height: 190px; border-radius: 50%; background: color-mix(in srgb, var(--brand) 8%, transparent); content: ''; }
.template-art { display: inline-flex; align-items: center; justify-content: center; width: 52px; height: 52px; flex: 0 0 auto; border-radius: 16px; background: var(--brand); color: white; box-shadow: 0 12px 26px rgba(97,87,230,.22); }
.template-banner > div:nth-child(2) { position: relative; z-index: 1; flex: 1; }
.template-banner span { color: var(--brand); font-size: 9px; font-weight: 700; }
.template-banner h2 { margin: 5px 0 0; color: var(--text-strong); font-size: 16px; font-weight: 650; }
.template-banner p { margin: 5px 0 0; color: var(--text-muted); font-size: 10px; }
.template-banner button { position: relative; z-index: 1; }
.documents-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 18px; margin: 25px 0 13px; }
.documents-toolbar > div:first-child { display: flex; gap: 4px; }
.documents-toolbar > div:first-child button { padding: 8px 11px; border: 0; border-radius: 8px; background: transparent; color: var(--text-muted); font-size: 10px; cursor: pointer; }
.documents-toolbar > div:first-child button.active { background: var(--bg-elevated); color: var(--text-strong); box-shadow: var(--shadow-sm); font-weight: 620; }
.toolbar-right { display: flex; align-items: center; gap: 5px; }
.toolbar-right label { display: flex; align-items: center; gap: 7px; height: 34px; padding: 0 10px; border: 1px solid var(--border); border-radius: 9px; background: var(--bg-elevated); color: var(--text-faint); }
.toolbar-right input { width: 120px; border: 0; outline: 0; background: transparent; color: var(--text); font-size: 10px; }
.view-button { display: inline-flex; align-items: center; justify-content: center; width: 34px; height: 34px; padding: 0; border: 1px solid transparent; border-radius: 9px; background: transparent; color: var(--text-faint); cursor: pointer; }
.view-button.active { border-color: var(--border); background: var(--bg-elevated); color: var(--brand); }
.document-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 13px; }
.new-document-card, .document-card { min-height: 246px; overflow: hidden; }
.new-document-card { display: flex; align-items: center; justify-content: center; flex-direction: column; border: 1px dashed var(--border-strong); border-radius: 16px; background: color-mix(in srgb, var(--bg-elevated) 55%, transparent); color: var(--text-muted); cursor: pointer; transition: 160ms ease; }
.new-document-card:hover { border-color: var(--brand); background: var(--brand-softer); color: var(--brand); }
.new-document-card span { display: inline-flex; align-items: center; justify-content: center; width: 45px; height: 45px; border-radius: 14px; background: var(--bg-elevated); box-shadow: var(--shadow-sm); }
.new-document-card strong { margin-top: 13px; color: var(--text-strong); font-size: 11px; font-weight: 620; }
.new-document-card small { margin-top: 5px; color: var(--text-faint); font-size: 9px; }
.document-card { transition: 170ms ease; }
.document-card:hover { border-color: var(--border-strong); box-shadow: var(--shadow-md); transform: translateY(-2px); }
.doc-preview { position: relative; display: flex; align-items: center; justify-content: center; height: 128px; overflow: hidden; }
.doc-preview.violet { background: linear-gradient(145deg, #efedff, #dad6ff); color: #6358d5; }
.doc-preview.blue { background: linear-gradient(145deg, #eaf5ff, #d5eaff); color: #3f79ad; }
.doc-preview.green { background: linear-gradient(145deg, #ebf8f2, #d4f0e3); color: #338966; }
.doc-preview.amber { background: linear-gradient(145deg, #fff8e8, #faeac5); color: #b07825; }
.doc-preview > span { position: absolute; top: 12px; right: 12px; display: inline-flex; padding: 7px; border-radius: 9px; background: rgba(255,255,255,.7); backdrop-filter: blur(8px); }
.preview-lines { width: 54%; padding: 14px; border-radius: 7px; background: rgba(255,255,255,.78); box-shadow: 0 9px 25px rgba(49,50,70,.09); transform: rotate(-2deg); }
.preview-lines i { display: block; width: 100%; height: 4px; margin: 6px 0; border-radius: 9px; background: currentColor; opacity: .16; }
.preview-lines i:nth-child(2) { width: 73%; } .preview-lines i:nth-child(4) { width: 48%; }
.document-info { padding: 14px; }
.document-title { display: flex; align-items: flex-start; justify-content: space-between; gap: 8px; }
.document-title span { color: var(--brand); font-size: 8px; font-weight: 650; }
.document-title h3 { margin: 4px 0 0; color: var(--text-strong); font-size: 11px; font-weight: 630; }
.document-title .icon-button { width: 28px; height: 28px; }
.document-meta { display: flex; justify-content: space-between; margin-top: 13px; color: var(--text-faint); font-size: 8px; }
.document-meta span { display: inline-flex; align-items: center; gap: 4px; }
.completion { display: flex; align-items: center; gap: 8px; margin-top: 12px; }
.completion > span { height: 3px; flex: 1; overflow: hidden; border-radius: 99px; background: var(--bg-hover); }
.completion i { display: block; height: 100%; border-radius: inherit; background: var(--brand); }
.completion small { color: var(--text-faint); font-size: 7px; }
@media (max-width: 940px) { .document-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 650px) { .template-banner { align-items: flex-start; flex-wrap: wrap; } .template-banner button { margin-left: 70px; } .documents-toolbar { align-items: flex-start; flex-direction: column; } .document-grid { grid-template-columns: 1fr; } }
</style>
