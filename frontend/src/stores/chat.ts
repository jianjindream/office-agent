import { defineStore } from 'pinia'
import type { SearchResult } from '@/types/api'

export type MessageRole = 'user' | 'assistant'
export type MessageStatus = 'complete' | 'streaming' | 'error' | 'stopped'
export type ExecutionStatus = 'pending' | 'running' | 'success' | 'error'

export interface ExecutionEvent {
  id: string
  type: 'mode' | 'step' | 'tool' | 'observation' | 'graph' | 'race'
  title: string
  detail?: string
  tool?: string
  status: ExecutionStatus
  startedAt: number
}

export interface ChatMessage {
  id: string
  role: MessageRole
  content: string
  createdAt: number
  status: MessageStatus
  mode?: string
  execution: ExecutionEvent[]
  sources: SearchResult[]
  extractedInfo?: string
  error?: string
}

export interface ChatSession {
  id: string
  title: string
  createdAt: number
  updatedAt: number
  messages: ChatMessage[]
}

const STORAGE_KEY = 'dreamloop-chat-sessions-v2'
const CURRENT_KEY = 'dreamloop-current-session-v2'
const USER_KEY = 'dreamloop-user-id'

const uid = (prefix: string) =>
  `${prefix}-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 9)}`

const loadSessions = (): ChatSession[] => {
  try {
    const parsed = JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '[]') as ChatSession[]
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

const getUserId = () => {
  const existing = localStorage.getItem(USER_KEY)
  if (existing) return existing
  const created = crypto.randomUUID?.() ?? uid('user')
  localStorage.setItem(USER_KEY, created)
  return created
}

export const useChatStore = defineStore('chat', {
  state: () => ({
    sessions: loadSessions() as ChatSession[],
    currentSessionId: localStorage.getItem(CURRENT_KEY) ?? '',
    userId: getUserId(),
  }),
  getters: {
    currentSession(state): ChatSession | undefined {
      return state.sessions.find((session) => session.id === state.currentSessionId)
    },
    sortedSessions(state): ChatSession[] {
      return [...state.sessions].sort((a, b) => b.updatedAt - a.updatedAt)
    },
  },
  actions: {
    persist() {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.sessions.slice(0, 20)))
      if (this.currentSessionId) localStorage.setItem(CURRENT_KEY, this.currentSessionId)
    },
    ensureSession() {
      if (this.currentSession) return this.currentSession
      return this.createSession()
    },
    createSession() {
      const now = Date.now()
      const session: ChatSession = {
        id: uid('session'),
        title: '新对话',
        createdAt: now,
        updatedAt: now,
        messages: [],
      }
      this.sessions.unshift(session)
      this.currentSessionId = session.id
      this.persist()
      return session
    },
    selectSession(sessionId: string) {
      if (!this.sessions.some((session) => session.id === sessionId)) return
      this.currentSessionId = sessionId
      this.persist()
    },
    addMessage(message: Omit<ChatMessage, 'id' | 'createdAt'>) {
      const session = this.ensureSession()
      const created: ChatMessage = {
        ...message,
        id: uid('message'),
        createdAt: Date.now(),
      }
      session.messages.push(created)
      session.updatedAt = Date.now()
      if (message.role === 'user' && session.title === '新对话') {
        session.title = message.content.replace(/\s+/g, ' ').trim().slice(0, 24) || '新对话'
      }
      this.persist()
      return created
    },
    updateMessage(messageId: string, patch: Partial<ChatMessage>) {
      const session = this.currentSession
      const message = session?.messages.find((item) => item.id === messageId)
      if (!session || !message) return
      Object.assign(message, patch)
      session.updatedAt = Date.now()
      this.persist()
    },
    appendContent(messageId: string, chunk: string) {
      const message = this.currentSession?.messages.find((item) => item.id === messageId)
      if (!message) return
      message.content += chunk
    },
    addExecution(messageId: string, event: Omit<ExecutionEvent, 'id' | 'startedAt'>) {
      const message = this.currentSession?.messages.find((item) => item.id === messageId)
      if (!message) return
      message.execution.push({ ...event, id: uid('event'), startedAt: Date.now() })
    },
    completeLatestTool(messageId: string, tool: string, detail: string, failed = false) {
      const message = this.currentSession?.messages.find((item) => item.id === messageId)
      if (!message) return
      const item = [...message.execution].reverse().find((event) => event.type === 'tool' && event.tool === tool)
      if (item) {
        item.status = failed ? 'error' : 'success'
        item.detail = detail
      } else {
        this.addExecution(messageId, {
          type: 'observation',
          title: `${tool} 已完成`,
          detail,
          tool,
          status: failed ? 'error' : 'success',
        })
      }
    },
    removeMessage(messageId: string) {
      const session = this.currentSession
      if (!session) return
      session.messages = session.messages.filter((message) => message.id !== messageId)
      session.updatedAt = Date.now()
      this.persist()
    },
    clearCurrentSession() {
      if (!this.currentSession) return
      this.currentSession.messages = []
      this.currentSession.title = '新对话'
      this.currentSession.updatedAt = Date.now()
      this.persist()
    },
  },
})
