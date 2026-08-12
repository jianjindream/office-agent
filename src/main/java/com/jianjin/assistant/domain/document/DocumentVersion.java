package com.jianjin.assistant.domain.document;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class DocumentVersion {

    private String id;
    private String documentId;
    private int version;
    private String contentMd;
    private String summary = "";
    private Map<String, Object> metadata = new LinkedHashMap<>();
    private Instant createdAt;

    public DocumentVersion() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public String getContentMd() { return contentMd; }
    public void setContentMd(String contentMd) { this.contentMd = contentMd; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary == null ? "" : summary; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata == null ? new LinkedHashMap<>() : metadata;
    }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
