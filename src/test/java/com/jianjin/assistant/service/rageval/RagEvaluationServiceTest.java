package com.jianjin.assistant.service.rageval;

import com.jianjin.assistant.config.AppConfig;
import com.jianjin.assistant.domain.rageval.EvaluationCase;
import com.jianjin.assistant.domain.rageval.EvaluationDataset;
import com.jianjin.assistant.domain.rageval.EvaluationRunReport;
import com.jianjin.assistant.domain.rageval.EvaluationRunRequest;
import com.jianjin.assistant.infrastructure.persistence.RagEvaluationRepository;
import com.jianjin.assistant.model.Chunk;
import com.jianjin.assistant.service.rag.RagService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RagEvaluationServiceTest {
    @Test
    void runsDeterministicLayersWhenPostgresAndRealJudgeAreUnavailable() {
        AppConfig cfg = new AppConfig();
        RagService rag = mock(RagService.class);
        when(rag.getMode()).thenReturn("hybrid");
        RagService.QueryTrace trace = new RagService.QueryTrace();
        trace.primaryQuery = "标准问题";
        trace.retrievalCandidates = List.of(chunk(42), chunk(99));
        trace.rerankedContexts = List.of(chunk(42));
        trace.finalContexts = List.of(chunk(42));
        trace.answer = "答案";
        when(rag.traceQuery(anyString())).thenReturn(trace);
        RagEvaluationRepository repository = mock(RagEvaluationRepository.class);
        when(repository.saveDataset(any())).thenReturn(false);
        when(repository.findDataset(anyString(), anyString())).thenReturn(null);
        when(repository.saveRun(any())).thenReturn(false);
        GenerationJudge judge = mock(GenerationJudge.class);
        RagEvaluationService service = new RagEvaluationService(rag, cfg, judge, repository);

        EvaluationDataset dataset = dataset();
        assertFalse(service.saveDataset(dataset));
        EvaluationRunRequest request = new EvaluationRunRequest();
        request.datasetName = "office";
        request.datasetVersion = "v1";
        EvaluationRunReport report = service.run(request);

        assertEquals(1.0, report.retrievalMetrics.recallAtK.get(1), 1e-9);
        assertEquals(1.0, report.contextMetrics.precision, 1e-9);
        assertNull(report.generationMetrics.faithfulness);
        assertFalse(report.persisted);
        assertTrue(report.warnings.stream().anyMatch(w -> w.contains("真实 LLM")));
        verifyNoInteractions(judge);
    }

    @Test
    void rejectsDuplicateCaseIdsAndGenerationRunsWithoutReferenceAnswers() {
        AppConfig cfg = new AppConfig();
        RagEvaluationService service = new RagEvaluationService(mock(RagService.class), cfg,
                mock(GenerationJudge.class), mock(RagEvaluationRepository.class));
        EvaluationDataset invalid = dataset();
        EvaluationCase duplicate = new EvaluationCase();
        duplicate.caseId = "case-1";
        duplicate.question = "另一个问题";
        duplicate.relevantContextIds = List.of(42L);
        invalid.cases.add(duplicate);
        assertThrows(IllegalArgumentException.class, () -> service.saveDataset(invalid));
    }

    private static EvaluationDataset dataset() {
        EvaluationCase item = new EvaluationCase();
        item.caseId = "case-1";
        item.question = "标准问题";
        item.referenceAnswer = "答案";
        item.relevantContextIds = List.of(42L);
        EvaluationDataset dataset = new EvaluationDataset();
        dataset.name = "office";
        dataset.version = "v1";
        dataset.cases.add(item);
        return dataset;
    }

    private static RagService.ScoredChunk chunk(long id) {
        return new RagService.ScoredChunk(id, new Chunk((int) id, "context"), 0.8, "hybrid", 2, false);
    }
}
