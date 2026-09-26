import { fetchEventSource, type EventSourceMessage } from '@microsoft/fetch-event-source'
import { API_BASE_URL, ApiError, request } from './http'
import type {
  ChatRequest,
  ChatStreamEvent,
  DocumentSummary,
  SystemStatus,
  ToolSummary,
} from '@/types/api'

interface StreamOptions {
  signal: AbortSignal
  onEvent: (event: ChatStreamEvent) => void
}

const parseEvent = (message: EventSourceMessage): ChatStreamEvent => {
  let data: Record<string, unknown> = {}
  if (message.data) {
    try {
      data = JSON.parse(message.data) as Record<string, unknown>
    } catch {
      data = { message: message.data }
    }
  }
  return { type: message.event || 'message', data }
}

export const agentApi = {
  status: () => request<SystemStatus>('/api/status'),
  tools: () => request<ToolSummary[]>('/api/tools'),
  documents: () => request<DocumentSummary[]>('/api/documents'),
  chat: (payload: ChatRequest) =>
    request<Record<string, unknown>>('/api/chat', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  streamChat: (payload: ChatRequest, options: StreamOptions) =>
    fetchEventSource(`${API_BASE_URL}/api/chat/stream`, {
      method: 'POST',
      headers: {
        Accept: 'text/event-stream',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
      signal: options.signal,
      openWhenHidden: true,
      async onopen(response) {
        if (!response.ok) {
          const payload = await response.json().catch(() => null)
          const message =
            typeof payload === 'object' && payload && 'error' in payload
              ? String(payload.error)
              : `流式请求失败（${response.status}）`
          throw new ApiError(message, response.status, payload)
        }
        const contentType = response.headers.get('content-type') ?? ''
        if (!contentType.includes('text/event-stream')) {
          throw new ApiError('服务没有返回 SSE 数据流', response.status)
        }
      },
      onmessage(message) {
        options.onEvent(parseEvent(message))
      },
      onerror(error) {
        throw error
      },
    }),
  uploadFile: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return request<Record<string, unknown>>('/api/upload/file', { method: 'POST', body: form })
  },
  cancel: () => request<{ ok: boolean; message: string }>('/api/chat/cancel', { method: 'POST' }),
}
