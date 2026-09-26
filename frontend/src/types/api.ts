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

export interface ReActStep {
  type: string
  content: string
  tool?: string
  params?: Record<string, string>
}

export interface ToolCallResult {
  toolName?: string
  tool_name?: string
  params?: Record<string, unknown>
  toolResult?: string
  tool_result?: string
}

export interface RagChunk {
  id?: string
  content?: string
  docHash?: string
  doc_hash?: string
  metadata?: Record<string, unknown>
}

export interface SearchResult {
  chunk: RagChunk
  similarity: number
}

export interface ChatResponse {
  query?: string
  answer?: string
  mode?: string
  steps?: ReActStep[]
  tool_call?: ToolCallResult
  search_results?: SearchResult[]
  extracted_info?: string
  interrupted?: boolean
}

export type StreamEventType =
  | 'start'
  | 'mode'
  | 'step'
  | 'tool_call'
  | 'observation'
  | 'rag_result'
  | 'token'
  | 'done'
  | 'error'
  | 'graph_ready'
  | 'node_start'
  | 'node_done'
  | 'race_won'

export interface ChatStreamEvent {
  type: StreamEventType | string
  data: Record<string, unknown> | ChatResponse
}
