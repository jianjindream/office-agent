<script setup lang="ts">
import {
  CheckCircle2,
  Clock3,
  Database,
  FileText,
  Filter,
  MoreHorizontal,
  Plus,
  Search,
  UploadCloud,
} from 'lucide-vue-next'

const documents = [
  { name: '产品需求说明书.md', type: 'Markdown', chunks: 38, size: '186 KB', time: '12 分钟前', status: 'ready' },
  { name: '2026 年行业研究报告.pdf', type: 'PDF', chunks: 72, size: '6.4 MB', time: '昨天', status: 'ready' },
  { name: '用户访谈记录合集.txt', type: 'Text', chunks: 26, size: '328 KB', time: '9月18日', status: 'ready' },
  { name: '企业知识管理方案.pdf', type: 'PDF', chunks: 0, size: '4.1 MB', time: '正在解析', status: 'processing' },
]
</script>

<template>
  <div class="page-shell knowledge-page">
    <header class="page-heading">
      <div>
        <h1>工作区知识库</h1>
        <p>集中管理 AI 可以检索和引用的企业资料。</p>
      </div>
      <button class="primary-button" type="button"><Plus :size="17" /> 添加资料</button>
    </header>

    <section class="stats-grid">
      <article class="surface-card stat-card">
        <span class="stat-icon violet"><Database :size="19" /></span>
        <div><small>知识片段</small><strong>236</strong><em>本月新增 42 个</em></div>
      </article>
      <article class="surface-card stat-card">
        <span class="stat-icon blue"><FileText :size="19" /></span>
        <div><small>资料总数</small><strong>12</strong><em>占用 18.6 MB</em></div>
      </article>
      <article class="surface-card stat-card">
        <span class="stat-icon green"><CheckCircle2 :size="19" /></span>
        <div><small>索引状态</small><strong>正常</strong><em>最近更新 12 分钟前</em></div>
      </article>
    </section>

    <section class="upload-card">
      <span class="upload-icon"><UploadCloud :size="24" /></span>
      <div>
        <strong>拖拽文件到这里，或点击选择文件</strong>
        <p>支持 PDF、Markdown、TXT，单个文件最大 30 MB</p>
      </div>
      <button class="secondary-button" type="button">选择文件</button>
    </section>

    <section class="surface-card document-panel">
      <div class="panel-heading">
        <div>
          <h2>全部资料</h2>
          <span>12 个文件</span>
        </div>
        <div class="panel-tools">
          <label class="table-search">
            <Search :size="15" />
            <input type="search" placeholder="搜索资料" />
          </label>
          <button class="icon-button" type="button" aria-label="筛选"><Filter :size="16" /></button>
        </div>
      </div>
      <div class="documents-table">
        <div class="table-row table-header">
          <span>资料名称</span><span>类型</span><span>知识片段</span><span>大小</span><span>更新时间</span><span></span>
        </div>
        <div v-for="doc in documents" :key="doc.name" class="table-row">
          <span class="doc-name"><i><FileText :size="16" /></i><strong>{{ doc.name }}</strong></span>
          <span><b class="type-pill">{{ doc.type }}</b></span>
          <span>{{ doc.status === 'ready' ? doc.chunks : '—' }}</span>
          <span>{{ doc.size }}</span>
          <span class="doc-time">
            <Clock3 v-if="doc.status === 'processing'" :size="13" />
            <i v-else></i>{{ doc.time }}
          </span>
          <span><button class="icon-button" type="button" aria-label="更多操作"><MoreHorizontal :size="16" /></button></span>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.stat-card { display: flex; align-items: center; gap: 14px; padding: 18px; }
.stat-icon { display: inline-flex; align-items: center; justify-content: center; width: 42px; height: 42px; flex: 0 0 auto; border-radius: 13px; }
.stat-icon.violet { background: var(--brand-soft); color: var(--brand); }
.stat-icon.blue { background: #eaf3ff; color: #3978bd; }
.stat-icon.green { background: var(--success-soft); color: var(--success); }
:root[data-theme='dark'] .stat-icon.blue { background: #1d3146; color: #83b8ef; }
.stat-card div { display: grid; grid-template-columns: auto 1fr; align-items: baseline; flex: 1; }
.stat-card small { grid-column: 1 / -1; color: var(--text-muted); font-size: 10px; }
.stat-card strong { margin-top: 5px; color: var(--text-strong); font-size: 21px; font-weight: 690; letter-spacing: -0.03em; }
.stat-card em { justify-self: end; color: var(--text-faint); font-size: 8px; font-style: normal; }
.upload-card { display: flex; align-items: center; gap: 14px; margin-top: 14px; padding: 17px 19px; border: 1px dashed color-mix(in srgb, var(--brand) 35%, var(--border)); border-radius: 15px; background: var(--brand-softer); }
.upload-icon { display: inline-flex; align-items: center; justify-content: center; width: 43px; height: 43px; flex: 0 0 auto; border-radius: 13px; background: var(--bg-elevated); color: var(--brand); box-shadow: var(--shadow-sm); }
.upload-card > div { flex: 1; }
.upload-card strong { color: var(--text-strong); font-size: 11px; font-weight: 630; }
.upload-card p { margin: 4px 0 0; color: var(--text-faint); font-size: 9px; }
.document-panel { margin-top: 20px; overflow: hidden; }
.panel-heading { display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 18px 20px; border-bottom: 1px solid var(--border); }
.panel-heading > div:first-child { display: flex; align-items: baseline; gap: 9px; }
.panel-heading h2 { margin: 0; color: var(--text-strong); font-size: 14px; font-weight: 650; }
.panel-heading span { color: var(--text-faint); font-size: 9px; }
.panel-tools { display: flex; align-items: center; gap: 6px; }
.table-search { display: flex; align-items: center; gap: 7px; height: 34px; padding: 0 10px; border: 1px solid var(--border); border-radius: 9px; background: var(--bg-subtle); color: var(--text-faint); }
.table-search input { width: 130px; border: 0; outline: 0; background: transparent; color: var(--text); font-size: 10px; }
.table-row { display: grid; grid-template-columns: minmax(230px, 2fr) .75fr .7fr .6fr .9fr 40px; min-width: 780px; align-items: center; padding: 0 14px 0 20px; border-bottom: 1px solid var(--border); color: var(--text-muted); font-size: 10px; }
.table-row:last-child { border: 0; }
.table-row:not(.table-header) { min-height: 62px; }
.table-row:not(.table-header):hover { background: var(--bg-subtle); }
.table-header { min-height: 36px; background: var(--bg-subtle); color: var(--text-faint); font-size: 8px; font-weight: 650; letter-spacing: .04em; text-transform: uppercase; }
.documents-table { overflow-x: auto; }
.doc-name { display: flex; min-width: 0; align-items: center; gap: 10px; }
.doc-name i { display: inline-flex; align-items: center; justify-content: center; width: 31px; height: 31px; flex: 0 0 auto; border-radius: 9px; background: var(--brand-softer); color: var(--brand); }
.doc-name strong { overflow: hidden; color: var(--text); font-size: 10px; font-weight: 590; text-overflow: ellipsis; white-space: nowrap; }
.type-pill { padding: 4px 7px; border-radius: 6px; background: var(--bg-hover); color: var(--text-muted); font-size: 8px; font-weight: 600; }
.doc-time { display: inline-flex; align-items: center; gap: 6px; }
.doc-time i { width: 5px; height: 5px; border-radius: 50%; background: var(--success); }
.doc-time svg { color: var(--warning); animation: spin 1.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
@media (max-width: 820px) { .stats-grid { grid-template-columns: 1fr; } }
@media (max-width: 620px) { .upload-card { align-items: flex-start; flex-wrap: wrap; } .upload-card .secondary-button { margin-left: 57px; } .panel-heading { align-items: flex-start; flex-direction: column; } }
</style>
