package com.jianjin.assistant.domain.document;

public class WriteResult {
    public final Document document;
    public final DocumentVersion version;
    public final boolean created;

    public WriteResult(Document document, DocumentVersion version, boolean created) {
        this.document = document;
        this.version = version;
        this.created = created;
    }
}
