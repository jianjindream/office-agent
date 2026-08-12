package com.jianjin.assistant.domain.document;

public class ParseResult {
    public String filename;
    public String contentType;
    public String parser;
    public String content;
    public int pages;
    public int textChars;
    public boolean needsOCR;

    public ParseResult() {}

    public ParseResult(String filename, String contentType, String parser,
                       String content, int pages, int textChars, boolean needsOCR) {
        this.filename = filename;
        this.contentType = contentType;
        this.parser = parser;
        this.content = content;
        this.pages = pages;
        this.textChars = textChars;
        this.needsOCR = needsOCR;
    }
}
