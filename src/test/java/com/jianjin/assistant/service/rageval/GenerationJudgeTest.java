package com.jianjin.assistant.service.rageval;

import com.jianjin.assistant.domain.rageval.GenerationMetrics;
import com.jianjin.assistant.service.llm.LlmService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GenerationJudgeTest {
    @Test
    void acceptsStrictJudgeJson() {
        LlmService llm = mock(LlmService.class);
        when(llm.chat(anyString(), anyList())).thenReturn("{\"faithfulness\":0.9,\"answerRelevance\":0.8,\"reason\":\"有依据\"}");

        GenerationMetrics result = new GenerationJudge(llm).judge("q", "ref", List.of(), "answer");

        assertNotNull(result);
        assertEquals(0.9, result.faithfulness, 1e-9);
        assertEquals(0.8, result.answerRelevance, 1e-9);
    }

    @Test
    void rejectsMockOrMalformedJudgeOutput() {
        LlmService llm = mock(LlmService.class);
        when(llm.chat(anyString(), anyList())).thenReturn("not-json");
        assertNull(new GenerationJudge(llm).judge("q", "ref", List.of(), "answer"));
    }
}
