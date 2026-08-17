package com.jianjin.assistant.interfaces.rageval;

import com.jianjin.assistant.domain.rageval.EvaluationDataset;
import com.jianjin.assistant.domain.rageval.EvaluationRunReport;
import com.jianjin.assistant.domain.rageval.EvaluationRunRequest;
import com.jianjin.assistant.service.rag.RagService;
import com.jianjin.assistant.service.rageval.RagEvaluationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** REST entry point for offline RAG golden-set management and benchmark runs. */
@RestController
@RequestMapping("/api/rag/evaluations")
public class RagEvaluationController {
    private final RagEvaluationService evaluation;
    private final RagService rag;

    public RagEvaluationController(RagEvaluationService evaluation, RagService rag) {
        this.evaluation = evaluation;
        this.rag = rag;
    }

    @GetMapping("/contexts")
    public Map<String, Object> contexts() {
        return Map.of("ok", true, "contexts", rag.getEvaluationContexts());
    }

    @PostMapping("/datasets")
    public Map<String, Object> saveDataset(@RequestBody EvaluationDataset dataset) {
        try {
            boolean persisted = evaluation.saveDataset(dataset);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("ok", true);
            out.put("name", dataset.name);
            out.put("version", dataset.version);
            out.put("caseCount", dataset.cases == null ? 0 : dataset.cases.size());
            out.put("persisted", persisted);
            if (!persisted) out.put("warning", "PostgreSQL 不可用；数据集仅保存于当前进程内存。");
            return out;
        } catch (IllegalArgumentException e) {
            return Map.of("ok", false, "error", e.getMessage());
        }
    }

    @PostMapping("/runs")
    public Map<String, Object> run(@RequestBody EvaluationRunRequest request) {
        try {
            EvaluationRunReport report = evaluation.run(request);
            return Map.of("ok", true, "report", report);
        } catch (IllegalArgumentException e) {
            return Map.of("ok", false, "error", e.getMessage());
        }
    }

    @GetMapping("/runs/{runId}")
    public Map<String, Object> getRun(@PathVariable String runId) {
        EvaluationRunReport report = evaluation.findRun(runId);
        return report == null ? Map.of("ok", false, "error", "未找到评测运行记录") : Map.of("ok", true, "report", report);
    }

    @GetMapping("/runs")
    public Map<String, Object> listRuns(
            @RequestParam(value = "datasetName", required = false) String datasetName,
            @RequestParam(value = "datasetVersion", required = false) String datasetVersion) {
        return Map.of("ok", true, "runs", evaluation.listRuns(datasetName, datasetVersion));
    }
}
