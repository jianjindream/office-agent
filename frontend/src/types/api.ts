export interface ToolSummary {
  name: string
  description: string
  is_mcp?: boolean
  params?: Array<Record<string, unknown>>
}

export interface DocumentSummary {
  id: string
  title: string
  docType?: string
  source?: string
  createdAt?: string
  updatedAt?: string
}

export interface SystemStatus {
  rag_loaded: boolean
  rag_mode: string
  tools_count: number
  llm_model: string
  embedding_model: string
  is_mock: boolean
  infrastructure: Record<string, unknown>
}

export interface ChatRequest {
  message: string
  use_rag: boolean
  selected_tools: string[]
  explicit: boolean
  user_id: string
  session_id: string
}
