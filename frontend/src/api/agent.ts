import { request } from './http'
import type { ChatRequest, DocumentSummary, SystemStatus, ToolSummary } from '@/types/api'

export const agentApi = {
  status: () => request<SystemStatus>('/api/status'),
  tools: () => request<ToolSummary[]>('/api/tools'),
  documents: () => request<DocumentSummary[]>('/api/documents'),
  chat: (payload: ChatRequest) =>
    request<Record<string, unknown>>('/api/chat', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  uploadFile: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return request<Record<string, unknown>>('/api/upload/file', { method: 'POST', body: form })
  },
  cancel: () => request<{ ok: boolean; message: string }>('/api/chat/cancel', { method: 'POST' }),
}
