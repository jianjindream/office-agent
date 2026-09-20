<script setup lang="ts">
import { ref } from 'vue'
import {
  ArrowUp,
  AtSign,
  BarChart3,
  ChevronDown,
  FileText,
  Globe2,
  Lightbulb,
  Paperclip,
  PenLine,
  ShieldCheck,
  Sparkles,
} from 'lucide-vue-next'

const prompt = ref('')

const suggestions = [
  {
    icon: FileText,
    title: '总结一份文档',
    description: '提炼重点、结论与后续行动',
    prompt: '请帮我总结这份文档，并列出关键结论和下一步行动。',
    tone: 'violet',
  },
  {
    icon: BarChart3,
    title: '撰写工作汇报',
    description: '把工作进展整理为专业汇报',
    prompt: '请根据我的工作内容，生成一份结构清晰的本周工作汇报。',
    tone: 'blue',
  },
  {
    icon: Lightbulb,
    title: '生成项目方案',
    description: '从目标到执行计划一次梳理',
    prompt: '请帮我制定一份项目实施方案，包含目标、里程碑、风险和人员分工。',
    tone: 'amber',
  },
  {
    icon: PenLine,
    title: '润色现有内容',
    description: '改善表达、结构和专业度',
    prompt: '请帮我润色以下内容，使表达更专业、简洁、自然。',
    tone: 'green',
  },
]

const useSuggestion = (value: string) => {
  prompt.value = value
}
</script>

<template>
  <div class="chat-page">
    <div class="chat-canvas">
      <section class="welcome-block">
        <div class="ai-orb" aria-hidden="true">
          <span class="orb-glow"></span>
          <Sparkles :size="25" stroke-width="1.7" />
        </div>
        <span class="welcome-kicker">你的智能办公搭档</span>
        <h2>下午好，准备处理什么工作？</h2>
        <p>我可以结合工作区资料，帮你检索、分析、写作和执行任务。</p>
      </section>

      <section class="suggestion-grid" aria-label="快捷任务">
        <button
          v-for="item in suggestions"
          :key="item.title"
          class="suggestion-card"
          type="button"
          @click="useSuggestion(item.prompt)"
        >
          <span class="suggestion-icon" :class="item.tone">
            <component :is="item.icon" :size="18" stroke-width="1.8" />
          </span>
          <span class="suggestion-copy">
            <strong>{{ item.title }}</strong>
            <small>{{ item.description }}</small>
          </span>
          <ArrowUp class="suggestion-arrow" :size="15" />
        </button>
      </section>

      <section class="composer-wrap">
        <div class="composer-focus-line"></div>
        <textarea
          v-model="prompt"
          rows="3"
          aria-label="向 DreamLoop 提问"
          placeholder="描述你想完成的工作，或粘贴需要处理的内容……"
        ></textarea>
        <div class="composer-toolbar">
          <div class="composer-tools">
            <button type="button" aria-label="添加附件" title="添加附件">
              <Paperclip :size="17" />
            </button>
            <button type="button" aria-label="引用内容" title="引用内容">
              <AtSign :size="17" />
            </button>
            <span class="toolbar-divider"></span>
            <button class="context-chip active" type="button">
              <ShieldCheck :size="14" />
              工作区知识库
              <ChevronDown :size="13" />
            </button>
            <button class="context-chip" type="button">
              <Globe2 :size="14" />
              自动选择工具
              <ChevronDown :size="13" />
            </button>
          </div>
          <button class="send-button" :disabled="!prompt.trim()" type="button" aria-label="发送消息">
            <ArrowUp :size="18" stroke-width="2.2" />
          </button>
        </div>
      </section>

      <div class="composer-note">
        <span><i></i> 企业数据仅在当前工作区内使用</span>
        <span>Enter 发送 · Shift + Enter 换行</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-page {
  display: flex;
  min-height: 100%;
  padding: 46px 30px 38px;
  align-items: center;
  justify-content: center;
}

.chat-canvas {
  width: min(760px, 100%);
  margin-top: -20px;
}

.welcome-block {
  text-align: center;
}

.ai-orb {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  margin-bottom: 15px;
  border: 1px solid color-mix(in srgb, var(--brand) 20%, var(--border));
  border-radius: 18px;
  background: linear-gradient(145deg, var(--bg-elevated), var(--brand-softer));
  color: var(--brand);
  box-shadow: 0 14px 34px rgba(88, 77, 210, 0.12);
}

.orb-glow {
  position: absolute;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: color-mix(in srgb, var(--brand) 18%, transparent);
  filter: blur(12px);
}

.ai-orb svg {
  position: relative;
}

.welcome-kicker {
  display: block;
  color: var(--brand);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.welcome-block h2 {
  margin: 8px 0 0;
  color: var(--text-strong);
  font-size: clamp(25px, 3vw, 32px);
  font-weight: 680;
  letter-spacing: -0.045em;
}

.welcome-block p {
  margin: 10px 0 0;
  color: var(--text-muted);
  font-size: 13px;
}

.suggestion-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 32px;
}

.suggestion-card {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 11px;
  padding: 13px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: color-mix(in srgb, var(--bg-elevated) 90%, transparent);
  box-shadow: var(--shadow-sm);
  text-align: left;
  cursor: pointer;
  transition: 170ms ease;
}

.suggestion-card:hover {
  border-color: color-mix(in srgb, var(--brand) 22%, var(--border));
  background: var(--bg-elevated);
  box-shadow: 0 10px 28px rgba(29, 31, 48, 0.07);
  transform: translateY(-2px);
}

.suggestion-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  flex: 0 0 auto;
  border-radius: 11px;
}

.suggestion-icon.violet { background: var(--brand-soft); color: var(--brand); }
.suggestion-icon.blue { background: #eaf3ff; color: #3978bd; }
.suggestion-icon.amber { background: var(--warning-soft); color: var(--warning); }
.suggestion-icon.green { background: var(--success-soft); color: var(--success); }

:root[data-theme='dark'] .suggestion-icon.blue { background: #1d3146; color: #83b8ef; }

.suggestion-copy {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
}

.suggestion-copy strong {
  color: var(--text-strong);
  font-size: 12px;
  font-weight: 630;
}

.suggestion-copy small {
  margin-top: 4px;
  overflow: hidden;
  color: var(--text-faint);
  font-size: 9px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.suggestion-arrow {
  flex: 0 0 auto;
  color: var(--text-faint);
  opacity: 0;
  transform: rotate(45deg) translate(3px, 3px);
  transition: 160ms ease;
}

.suggestion-card:hover .suggestion-arrow {
  opacity: 1;
  transform: rotate(45deg) translate(0, 0);
}

.composer-wrap {
  position: relative;
  margin-top: 16px;
  overflow: hidden;
  border: 1px solid var(--border-strong);
  border-radius: 17px;
  background: var(--bg-elevated);
  box-shadow: 0 16px 44px rgba(32, 34, 48, 0.09);
  transition: 170ms ease;
}

.composer-wrap:focus-within {
  border-color: color-mix(in srgb, var(--brand) 50%, var(--border));
  box-shadow: 0 18px 48px rgba(75, 65, 187, 0.12);
}

.composer-focus-line {
  position: absolute;
  top: 0;
  right: 24%;
  left: 24%;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--brand), transparent);
  opacity: 0;
  transition: 170ms ease;
}

.composer-wrap:focus-within .composer-focus-line {
  right: 8%;
  left: 8%;
  opacity: 0.7;
}

.composer-wrap textarea {
  width: 100%;
  min-height: 100px;
  padding: 18px 18px 8px;
  resize: none;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--text-strong);
  font-size: 13px;
  line-height: 1.7;
}

.composer-wrap textarea::placeholder {
  color: var(--text-faint);
}

.composer-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 9px 10px 10px 12px;
}

.composer-tools {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 5px;
}

.composer-tools > button:not(.context-chip) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  padding: 0;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
}

.composer-tools > button:hover {
  background: var(--bg-hover);
  color: var(--text-strong);
}

.toolbar-divider {
  width: 1px;
  height: 17px;
  margin: 0 3px;
  background: var(--border);
}

.context-chip {
  display: inline-flex;
  align-items: center;
  min-height: 29px;
  gap: 5px;
  padding: 0 9px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg-subtle);
  color: var(--text-muted);
  font-size: 9px;
  font-weight: 570;
  cursor: pointer;
}

.context-chip.active {
  border-color: color-mix(in srgb, var(--brand) 20%, var(--border));
  background: var(--brand-softer);
  color: var(--brand);
}

.send-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  flex: 0 0 auto;
  border: 0;
  border-radius: 11px;
  background: var(--brand);
  color: white;
  box-shadow: 0 7px 16px rgba(97, 87, 230, 0.24);
  cursor: pointer;
  transition: 150ms ease;
}

.send-button:hover:not(:disabled) {
  background: var(--brand-hover);
  transform: translateY(-1px);
}

.send-button:disabled {
  background: var(--border-strong);
  box-shadow: none;
  cursor: default;
}

.composer-note {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 9px 4px 0;
  color: var(--text-faint);
  font-size: 8px;
}

.composer-note span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.composer-note i {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--success);
}

@media (max-width: 720px) {
  .chat-page {
    min-height: calc(100% - 70px);
    padding: 26px 16px 100px;
    align-items: flex-start;
  }

  .chat-canvas {
    margin-top: 0;
  }

  .suggestion-grid {
    grid-template-columns: 1fr;
    margin-top: 24px;
  }

  .context-chip {
    display: none;
  }

  .composer-note span:last-child {
    display: none;
  }
}
</style>
