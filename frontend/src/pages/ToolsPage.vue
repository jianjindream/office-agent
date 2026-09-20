<script setup lang="ts">
import { CalendarDays, Check, Clock3, CloudSun, Code2, ExternalLink, Globe2, MoreHorizontal, Plus, Search, Wrench } from 'lucide-vue-next'

const tools = [
  { icon: Globe2, name: '联网搜索', desc: '搜索实时网页内容并整理可信来源', category: '内置工具', color: 'blue', enabled: true },
  { icon: CloudSun, name: '天气查询', desc: '获取城市当前天气和未来预报', category: '内置工具', color: 'amber', enabled: true },
  { icon: Clock3, name: '时间助手', desc: '处理时间、时区和日期相关任务', category: '内置工具', color: 'violet', enabled: true },
  { icon: Code2, name: '安全代码执行', desc: '在隔离环境中执行受控命令', category: '内置工具', color: 'green', enabled: false },
  { icon: CalendarDays, name: '团队日历', desc: '读取并整理团队日程安排', category: 'MCP', color: 'rose', enabled: true },
]
</script>

<template>
  <div class="page-shell tools-page">
    <header class="page-heading">
      <div><h1>工具中心</h1><p>连接外部能力，让 AI 不止回答问题，还能执行工作。</p></div>
      <button class="primary-button" type="button"><Plus :size="17" /> 接入 MCP 工具</button>
    </header>

    <section class="tools-overview">
      <div><span class="overview-icon"><Wrench :size="21" /></span><div><small>可用工具</small><strong>6</strong></div></div>
      <i></i>
      <div><span class="status-dot"></span><div><small>运行状态</small><strong>5 个正常</strong></div></div>
      <i></i>
      <div><span class="calls-icon"><ExternalLink :size="18" /></span><div><small>今日调用</small><strong>28 次</strong></div></div>
    </section>

    <div class="tools-filter">
      <div><button class="active" type="button">全部</button><button type="button">内置工具</button><button type="button">MCP</button></div>
      <label><Search :size="15" /><input placeholder="搜索工具" /></label>
    </div>

    <section class="tool-grid">
      <article v-for="tool in tools" :key="tool.name" class="surface-card tool-card">
        <div class="tool-card-top">
          <span class="tool-icon" :class="tool.color"><component :is="tool.icon" :size="20" /></span>
          <span class="tool-category">{{ tool.category }}</span>
          <button class="icon-button" type="button" aria-label="更多操作"><MoreHorizontal :size="17" /></button>
        </div>
        <h2>{{ tool.name }}</h2><p>{{ tool.desc }}</p>
        <div class="tool-card-footer">
          <span :class="{ offline: !tool.enabled }"><i></i>{{ tool.enabled ? '运行正常' : '已暂停' }}</span>
          <button class="switch" :class="{ enabled: tool.enabled }" type="button" :aria-label="`${tool.name}开关`"><i></i></button>
        </div>
      </article>

      <button class="add-tool-card" type="button"><span><Plus :size="20" /></span><strong>添加新工具</strong><small>通过 HTTP 端点连接 MCP 服务</small></button>
    </section>

    <section class="surface-card recent-calls">
      <div class="section-heading"><div><h2>最近调用</h2><span>实时查看工具执行状态</span></div><button type="button">查看全部</button></div>
      <div class="call-row"><span class="call-status"><Check :size="13" /></span><div><strong>联网搜索</strong><small>搜索“AI 办公产品趋势”</small></div><span>1.8 秒</span><time>3 分钟前</time></div>
      <div class="call-row"><span class="call-status"><Check :size="13" /></span><div><strong>工作区知识库</strong><small>检索 3 个相关文档片段</small></div><span>0.6 秒</span><time>12 分钟前</time></div>
    </section>
  </div>
</template>

<style scoped>
.tools-overview { display: flex; align-items: center; gap: 28px; padding: 17px 21px; border: 1px solid var(--border); border-radius: 15px; background: linear-gradient(105deg, var(--bg-elevated), var(--brand-softer)); box-shadow: var(--shadow-sm); }
.tools-overview > div { display: flex; align-items: center; gap: 11px; }
.tools-overview > i { width: 1px; height: 32px; background: var(--border); }
.tools-overview span { display: inline-flex; align-items: center; justify-content: center; }
.overview-icon, .calls-icon { width: 37px; height: 37px; border-radius: 11px; background: var(--brand-soft); color: var(--brand); }
.calls-icon { background: var(--success-soft); color: var(--success); }
.status-dot { width: 10px; height: 10px; margin: 0 13px; border-radius: 50%; background: var(--success); box-shadow: 0 0 0 6px var(--success-soft); }
.tools-overview div div { display: flex; flex-direction: column; }
.tools-overview small { color: var(--text-faint); font-size: 8px; }
.tools-overview strong { margin-top: 3px; color: var(--text-strong); font-size: 13px; font-weight: 660; }
.tools-filter { display: flex; align-items: center; justify-content: space-between; margin: 24px 0 13px; }
.tools-filter > div { display: flex; gap: 4px; }
.tools-filter button { padding: 8px 12px; border: 0; border-radius: 8px; background: transparent; color: var(--text-muted); font-size: 10px; cursor: pointer; }
.tools-filter button.active { background: var(--bg-elevated); color: var(--text-strong); box-shadow: var(--shadow-sm); font-weight: 620; }
.tools-filter label { display: flex; align-items: center; gap: 7px; height: 34px; padding: 0 10px; border: 1px solid var(--border); border-radius: 9px; background: var(--bg-elevated); color: var(--text-faint); }
.tools-filter input { width: 130px; border: 0; outline: 0; background: transparent; color: var(--text); font-size: 10px; }
.tool-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
.tool-card { padding: 16px; transition: 160ms ease; }
.tool-card:hover { border-color: var(--border-strong); box-shadow: var(--shadow-md); transform: translateY(-2px); }
.tool-card-top { display: flex; align-items: center; }
.tool-icon { display: inline-flex; align-items: center; justify-content: center; width: 39px; height: 39px; border-radius: 12px; }
.tool-icon.blue { background: #eaf3ff; color: #3978bd; }.tool-icon.amber { background: var(--warning-soft); color: var(--warning); }.tool-icon.violet { background: var(--brand-soft); color: var(--brand); }.tool-icon.green { background: var(--success-soft); color: var(--success); }.tool-icon.rose { background: #fff0f3; color: #bd5c70; }
:root[data-theme='dark'] .tool-icon.blue { background: #1d3146; color: #83b8ef; }:root[data-theme='dark'] .tool-icon.rose { background: #3d242d; color: #ef9bae; }
.tool-category { margin-left: 9px; padding: 4px 7px; border-radius: 6px; background: var(--bg-hover); color: var(--text-faint); font-size: 8px; font-weight: 600; }
.tool-card-top .icon-button { width: 29px; height: 29px; margin-left: auto; }
.tool-card h2 { margin: 15px 0 0; color: var(--text-strong); font-size: 13px; font-weight: 650; }
.tool-card p { min-height: 34px; margin: 6px 0 16px; color: var(--text-muted); font-size: 9px; line-height: 1.65; }
.tool-card-footer { display: flex; align-items: center; justify-content: space-between; padding-top: 12px; border-top: 1px solid var(--border); }
.tool-card-footer > span { display: inline-flex; align-items: center; gap: 6px; color: var(--success); font-size: 8px; }
.tool-card-footer > span i { width: 5px; height: 5px; border-radius: 50%; background: currentColor; }
.tool-card-footer > span.offline { color: var(--text-faint); }
.switch { position: relative; width: 28px; height: 17px; padding: 0; border: 0; border-radius: 99px; background: var(--border-strong); cursor: pointer; }
.switch i { position: absolute; top: 3px; left: 3px; width: 11px; height: 11px; border-radius: 50%; background: white; box-shadow: 0 1px 2px rgba(0,0,0,.18); transition: 150ms; }
.switch.enabled { background: var(--brand); }.switch.enabled i { transform: translateX(11px); }
.add-tool-card { display: flex; align-items: center; justify-content: center; min-height: 202px; flex-direction: column; border: 1px dashed var(--border-strong); border-radius: 16px; background: transparent; cursor: pointer; }
.add-tool-card:hover { border-color: var(--brand); background: var(--brand-softer); }
.add-tool-card span { display: inline-flex; align-items: center; justify-content: center; width: 40px; height: 40px; border-radius: 12px; background: var(--bg-elevated); color: var(--brand); box-shadow: var(--shadow-sm); }
.add-tool-card strong { margin-top: 11px; color: var(--text-strong); font-size: 11px; }.add-tool-card small { margin-top: 5px; color: var(--text-faint); font-size: 8px; }
.recent-calls { margin-top: 20px; overflow: hidden; }
.section-heading { display: flex; align-items: center; justify-content: space-between; padding: 15px 18px; border-bottom: 1px solid var(--border); }
.section-heading h2 { display: inline; margin: 0; color: var(--text-strong); font-size: 12px; font-weight: 650; }.section-heading span { margin-left: 9px; color: var(--text-faint); font-size: 8px; }.section-heading button { border: 0; background: transparent; color: var(--brand); font-size: 9px; cursor: pointer; }
.call-row { display: grid; grid-template-columns: 28px 1fr 70px 80px; align-items: center; gap: 10px; min-height: 52px; padding: 0 18px; border-bottom: 1px solid var(--border); color: var(--text-muted); font-size: 9px; }.call-row:last-child { border: 0; }
.call-status { display: inline-flex; align-items: center; justify-content: center; width: 23px; height: 23px; border-radius: 8px; background: var(--success-soft); color: var(--success); }.call-row div { display: flex; flex-direction: column; }.call-row strong { color: var(--text); font-size: 9px; font-weight: 600; }.call-row small { margin-top: 3px; color: var(--text-faint); font-size: 8px; }.call-row time { color: var(--text-faint); text-align: right; }
@media (max-width: 900px) { .tool-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 620px) { .tools-overview { align-items: flex-start; flex-direction: column; gap: 14px; }.tools-overview > i { width: 100%; height: 1px; }.tools-filter { align-items: flex-start; flex-direction: column; gap: 10px; }.tool-grid { grid-template-columns: 1fr; } }
</style>
