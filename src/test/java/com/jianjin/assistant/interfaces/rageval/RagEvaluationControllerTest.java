package com.jianjin.assistant.interfaces.rageval;

import com.jianjin.assistant.domain.rageval.EvaluationDataset;
import com.jianjin.assistant.domain.rageval.EvaluationRunReport;
import com.jianjin.assistant.domain.rageval.EvaluationRunRequest;
import com.jianjin.assistant.service.rag.RagService;
import com.jianjin.assistant.service.rageval.RagEvaluationService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RagEvaluationControllerTest {
    @Test
    void exposesDatasetRunContextAndHistoryEndpoints() {
        RagEvaluationService evaluation = mock(RagEvaluationService.class);
        RagService rag = mock(RagService.class);
        RagEvaluationController controller = new RagEvaluationController(evaluation, rag);
        EvaluationDataset dataset = new EvaluationDataset();
        dataset.name = "office";
        dataset.version = "v1";
        when(evaluation.saveDataset(any())).thenReturn(true);
        assertEquals(true, controller.saveDataset(dataset).get("ok"));

        EvaluationRunReport report = new EvaluationRunReport();
        report.runId = "run-1";
        when(evaluation.run(any())).thenReturn(report);
        assertEquals(report, controller.run(new EvaluationRunRequest()).get("report"));

        when(rag.getEvaluationContexts()).thenReturn(List.of(new RagService.ContextCatalogItem(1L, "doc", 0, "content")));
        Map<String, Object> contexts = controller.contexts();
        assertEquals(1, ((List<?>) contexts.get("contexts")).size());

        when(evaluation.listRuns("office", "v1")).thenReturn(List.of(report));
        assertEquals(1, ((List<?>) controller.listRuns("office", "v1").get("runs")).size());
    }
}
