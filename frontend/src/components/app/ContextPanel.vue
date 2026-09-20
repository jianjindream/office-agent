<script setup lang="ts">
import {
  ChevronRight,
  CircleCheck,
  FileText,
  Globe2,
  LibraryBig,
  MoreHorizontal,
  Paperclip,
  Settings2,
} from 'lucide-vue-next'
import { useWorkspaceStore } from '@/stores/workspace'

const workspace = useWorkspaceStore()

const resources = [
  { title: '产品需求说明书.md', meta: '2.8 MB · 38 个片段', icon: FileText },
  { title: '用户研究报告.pdf', meta: '6.4 MB · 72 个片段', icon: Paperclip },
  { title: '行业洞察资料库', meta: '知识库 · 126 个片段', icon: LibraryBig },
]
</script>

<template>
  <aside class="context-panel">
    <div class="context-heading">
      <div>
        <span>CONTEXT</span>
        <h2>对话上下文</h2>
      </div>
      <button class="icon-button" type="button" aria-label="上下文设置">
        <Settings2 :size="17" />
      </button>
    </div>

    <section class="context-section">
      <div class="section-title">
        <span>正在使用</span>
        <button type="button" aria-label="更多"><MoreHorizontal :size="16" /></button>
      </div>
      <button
        class="context-toggle"
        :class="{ enabled: workspace.knowledgeEnabled }"
        type="button"
        @click="workspace.toggleKnowledge"
      >
        <span class="resource-icon knowledge"><LibraryBig :size="16" /></span>
        <span class="toggle-copy">
          <strong>工作区知识库</strong>
          <small>{{ workspace.knowledgeEnabled ? '已启用语义检索' : '已暂停使用' }}</small>
        </span>
        <span class="switch"><i></i></span>
      </button>
      <button class="context-toggle enabled" type="button">
        <span class="resource-icon web"><Globe2 :size="16" /></span>
        <span class="toggle-copy">
          <strong>联网搜索</strong>
          <small>需要时自动调用</small>
        </span>
        <span class="switch"><i></i></span>
      </button>
    </section>

    <section class="context-section">
      <div class="section-title">
        <span>已选择的资料</span>
        <button type="button">管理</button>
      </div>
      <button v-for="item in resources" :key="item.title" class="resource-item" type="button">
        <span class="resource-icon"><component :is="item.icon" :size="15" /></span>
        <span class="resource-copy">
          <strong>{{ item.title }}</strong>
          <small>{{ item.meta }}</small>
        </span>
        <ChevronRight :size="14" />
      </button>
    </section>

    <section class="context-section activity-section">
      <div class="section-title"><span>本次会话</span></div>
      <div class="activity-card">
        <div><CircleCheck :size="15" /> 上下文已就绪</div>
        <dl>
          <div><dt>知识片段</dt><dd>236</dd></div>
          <div><dt>可用工具</dt><dd>6</dd></div>
          <div><dt>上下文窗口</dt><dd>32K</dd></div>
        </dl>
      </div>
    </section>

    <div class="context-tip">
      <span>提示</span>
      <p>选择更准确的资料，可以获得更可靠的回答和引用来源。</p>
    </div>
  </aside>
</template>

<style scoped>
.context-panel {
  display: flex;
  min-width: 0;
  height: 100%;
  padding: 0 18px 18px;
  flex-direction: column;
  overflow: auto;
  border-left: 1px solid var(--border);
  background: var(--bg-panel);
  backdrop-filter: blur(18px);
}

.context-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: var(--topbar-height);
  border-bottom: 1px solid var(--border);
}

.context-heading span {
  color: var(--text-faint);
  font-size: 8px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.context-heading h2 {
  margin: 3px 0 0;
  color: var(--text-strong);
  font-size: 14px;
  font-weight: 650;
}

.context-section {
  padding: 20px 0;
  border-bottom: 1px solid var(--border);
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  color: var(--text-faint);
  font-size: 9px;
  font-weight: 680;
  letter-spacing: 0.07em;
  text-transform: uppercase;
}

.section-title button {
  display: inline-flex;
  align-items: center;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--brand);
  font-size: 9px;
  cursor: pointer;
}

.context-toggle,
.resource-item {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 9px;
  padding: 9px 8px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: var(--text-muted);
  text-align: left;
  cursor: pointer;
}

.context-toggle:hover,
.resource-item:hover {
  background: var(--bg-hover);
}

.resource-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  flex: 0 0 auto;
  border: 1px solid var(--border);
  border-radius: 9px;
  background: var(--bg-elevated);
  color: var(--text-muted);
}

.resource-icon.knowledge {
  border-color: transparent;
  background: var(--brand-soft);
  color: var(--brand);
}

.resource-icon.web {
  border-color: transparent;
  background: var(--success-soft);
  color: var(--success);
}

.toggle-copy,
.resource-copy {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
}

.toggle-copy strong,
.resource-copy strong {
  overflow: hidden;
  color: var(--text);
  font-size: 10px;
  font-weight: 610;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toggle-copy small,
.resource-copy small {
  margin-top: 3px;
  overflow: hidden;
  color: var(--text-faint);
  font-size: 8px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.switch {
  position: relative;
  width: 26px;
  height: 16px;
  flex: 0 0 auto;
  border-radius: 999px;
  background: var(--border-strong);
  transition: 150ms ease;
}

.switch i {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  transition: 150ms ease;
}

.enabled .switch {
  background: var(--brand);
}

.enabled .switch i {
  transform: translateX(10px);
}

.activity-card {
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--bg-subtle);
}

.activity-card > div {
  display: flex;
  align-items: center;
  gap: 7px;
  color: var(--success);
  font-size: 10px;
  font-weight: 620;
}

.activity-card dl {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin: 13px 0 0;
}

.activity-card dl div {
  display: flex;
  flex-direction: column;
}

.activity-card dt {
  color: var(--text-faint);
  font-size: 8px;
}

.activity-card dd {
  margin: 3px 0 0;
  color: var(--text-strong);
  font-size: 12px;
  font-weight: 680;
}

.context-tip {
  margin-top: auto;
  padding: 13px;
  border-radius: 12px;
  background: var(--brand-softer);
}

.context-tip span {
  color: var(--brand);
  font-size: 9px;
  font-weight: 700;
}

.context-tip p {
  margin: 5px 0 0;
  color: var(--text-muted);
  font-size: 9px;
  line-height: 1.65;
}

@media (max-width: 1180px) {
  .context-panel {
    display: none;
  }
}
</style>
