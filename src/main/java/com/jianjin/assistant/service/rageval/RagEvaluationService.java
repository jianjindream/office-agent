package com.jianjin.assistant.service.rageval;

import com.jianjin.assistant.config.AppConfig;
import com.jianjin.assistant.domain.rageval.ContextMetrics;
import com.jianjin.assistant.domain.rageval.EvaluationCase;
import com.jianjin.assistant.domain.rageval.EvaluationCaseResult;
import com.jianjin.assistant.domain.rageval.EvaluationDataset;
import com.jianjin.assistant.domain.rageval.EvaluationRunReport;
import com.jianjin.assistant.domain.rageval.EvaluationRunRequest;
import com.jianjin.assistant.domain.rageval.GenerationMetrics;
import com.jianjin.assistant.domain.rageval.RankedContext;
import com.jianjin.assistant.domain.rageval.StageMetrics;
import com.jianjin.assistant.infrastructure.persistence.RagEvaluationRepository;
import com.jianjin.assistant.service.rag.RagService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Coordinates validation, production-pipeline tracing, scoring, judging and report persistence. */
@Service
public class RagEvaluationService {
    private final RagService rag;
    private final AppConfig cfg;
    private final GenerationJudge judge;
    private final RagEvaluationRepository repository;
    /** Retains submitted golden sets when optional PostgreSQL is unavailable. */
    private final Map<String, EvaluationDataset> volatileDatasets = new ConcurrentHashMap<>();

    public RagEvaluationService(RagService rag, AppConfig cfg, GenerationJudge judge,
                                RagEvaluationRepository repository) {
        this.rag = rag;
        this.cfg = cfg;
        this.judge = judge;
        this.repository = repository;
    }

    public boolean saveDataset(EvaluationDataset dataset) {
        validateDataset(dataset);
        volatileDatasets.put(key(dataset.name, dataset.version), dataset);
        return repository.saveDataset(dataset);
    }

    public EvaluationRunReport run(EvaluationRunRequest request) {
        if (request == null || blank(request.datasetName) || blank(request.datasetVersion)) {
            throw new IllegalArgumentException("datasetName 和 datasetVersion 为必填项");
        }
        EvaluationDataset dataset = repository.findDataset(request.datasetName, request.datasetVersion);
        if (dataset == null) dataset = volatileDatasets.get(key(request.datasetName, request.datasetVersion));
        if (dataset == null) throw new IllegalArgumentException("未找到指定的黄金数据集版本");
        validateDataset(dataset);
        if (dataset.cases.size() > cfg.getEvaluation().getMaxCases()) {
            throw new IllegalArgumentException("单次评测最多支持 " + cfg.getEvaluation().getMaxCases() + " 条样例");
        }

        List<Integer> topKs = normalizeTopKs(request.topKs);
        boolean generation = request.generationEvaluation == null || request.generationEvaluation;
        if (generation) {
            for (EvaluationCase item : dataset.cases) {
                if (blank(item.referenceAnswer)) {
                    throw new IllegalArgumentException("启用生成评测时每条样例都必须提供 referenceAnswer（caseId=" + item.caseId + "）");
                }
            }
        }

        EvaluationRunReport report = new EvaluationRunReport();
        report.runId = UUID.randomUUID().toString();
        report.datasetName = dataset.name;
        report.datasetVersion = dataset.version;
        report.startedAt = Instant.now().toString();
        report.status = "running";
        report.topKs = topKs;
        report.configurationSnapshot = snapshot(generation);
        if (generation && !cfg.isRealLLM()) {
            report.warnings.add("未配置真实 LLM，已跳过 Faithfulness 与 Answer Relevance 评测。");
        }

        List<StageMetrics> retrieval = new ArrayList<>();
        List<StageMetrics> rerank = new ArrayList<>();
        List<ContextMetrics> contexts = new ArrayList<>();
        List<GenerationMetrics> generations = new ArrayList<>();
        for (EvaluationCase item : dataset.cases) {
            EvaluationCaseResult result = runCase(item, topKs, generation, report.warnings);
            report.caseResults.add(result);
            retrieval.add(result.retrievalMetrics);
            rerank.add(result.rerankMetrics);
            contexts.add(result.contextMetrics);
            if (result.generationMetrics != null && result.generationMetrics.faithfulness != null) {
                generations.add(result.generationMetrics);
            }
        }
        report.retrievalMetrics = RagMetricCalculator.averageStages(retrieval, topKs);
        report.rerankMetrics = RagMetricCalculator.averageStages(rerank, topKs);
        report.contextMetrics = RagMetricCalculator.averageContexts(contexts);
        report.generationMetrics = averageGeneration(generations);
        report.completedAt = Instant.now().toString();
        report.status = report.warnings.isEmpty() ? "completed" : "completed_with_warnings";
        // Persist the same state a later GET will return; downgrade it only if storage fails.
        report.persisted = true;
        report.persisted = repository.saveRun(report);
        if (!report.persisted) report.warnings.add("PostgreSQL 不可用或保存失败；本次报告仅在本次响应中返回。");
        if (!report.warnings.isEmpty()) report.status = "completed_with_warnings";
        return report;
    }

    public EvaluationRunReport findRun(String runId) { return repository.findRun(runId); }
    public List<EvaluationRunReport> listRuns(String datasetName, String datasetVersion) {
        return repository.listRuns(datasetName, datasetVersion);
    }

    private EvaluationCaseResult runCase(EvaluationCase item, List<Integer> topKs,
                                         boolean generation, List<String> globalWarnings) {
        RagService.QueryTrace trace = rag.traceQuery(item.question);
        EvaluationCaseResult out = new EvaluationCaseResult();
        out.caseId = item.caseId;
        out.question = item.question;
        out.category = item.category;
        out.difficulty = item.difficulty;
        out.primaryQuery = trace.primaryQuery;
        out.expandedQueries = new ArrayList<>(trace.expandedQueries);
        out.relevantContextIds = distinct(item.relevantContextIds);
        out.primaryRetrievalContextIds = ids(trace.primaryRetrievalCandidates);
        out.retrievalContextIds = ids(trace.retrievalCandidates);
        out.rerankedContextIds = ids(trace.rerankedContexts);
        out.finalContextIds = ids(trace.finalContexts);
        out.retrievalRanking = ranking(trace.retrievalCandidates);
        out.primaryRetrievalRanking = ranking(trace.primaryRetrievalCandidates);
        out.rerankRanking = ranking(trace.rerankedContexts);
        out.finalContextRanking = ranking(trace.finalContexts);
        out.retrievalMetrics = RagMetricCalculator.evaluate(out.relevantContextIds, trace.retrievalCandidates, topKs);
        out.rerankMetrics = RagMetricCalculator.evaluate(out.relevantContextIds, trace.rerankedContexts, topKs);
        out.contextMetrics = RagMetricCalculator.context(out.relevantContextIds, trace.finalContexts);
        out.missedContextIds = missed(out.relevantContextIds, trace.retrievalCandidates);
        out.answer = trace.answer;
        out.warning = trace.warning;
        if (generation && cfg.isRealLLM()) {
            out.generationMetrics = judge.judge(item.question, item.referenceAnswer, trace.finalContexts, trace.answer);
            if (out.generationMetrics == null) {
                out.warning = joinWarning(out.warning, "generation_judge_unavailable");
                addOnce(globalWarnings, "部分生成评测未能解析 Judge 输出，相关分数已置空。");
            }
        }
        return out;
    }

    private Map<String, Object> snapshot(boolean generation) {
        Map<String, Object> out = new LinkedHashMap<>();
        Map<String, Object> ragConfig = new LinkedHashMap<>();
        ragConfig.put("chunkSize", cfg.getRag().getChunkSize());
        ragConfig.put("chunkOverlap", cfg.getRag().getChunkOverlap());
        ragConfig.put("parentChunkSize", cfg.getRag().getParentChunkSize());
        ragConfig.put("parentChunkOverlap", cfg.getRag().getParentChunkOverlap());
        ragConfig.put("topK", cfg.getRag().getTopK());
        ragConfig.put("rrfConstantK", cfg.getRag().getRrfConstantK());
        ragConfig.put("semanticWeight", cfg.getRag().getSemanticWeight());
        ragConfig.put("keywordWeight", cfg.getRag().getKeywordWeight());
        ragConfig.put("rewriteEnabled", cfg.getRag().getRewrite().isEnabled());
        ragConfig.put("rerankEnabled", cfg.getRag().getRerank().isEnabled());
        out.put("rag", ragConfig);
        out.put("retrievalMode", rag.getMode());
        out.put("embeddingModel", cfg.getEmbedding().getModel());
        out.put("llmModel", cfg.getLlm().getModel());
        out.put("generationEvaluation", generation);
        return out;
    }

    private static GenerationMetrics averageGeneration(List<GenerationMetrics> all) {
        GenerationMetrics out = new GenerationMetrics();
        if (all == null || all.isEmpty()) return out;
        double faithfulness = 0;
        double relevance = 0;
        for (GenerationMetrics item : all) {
            faithfulness += item.faithfulness;
            relevance += item.answerRelevance;
        }
        out.faithfulness = faithfulness / all.size();
        out.answerRelevance = relevance / all.size();
        return out;
    }

    private static List<Long> ids(List<RagService.ScoredChunk> chunks) {
        List<Long> out = new ArrayList<>();
        for (RagService.ScoredChunk item : chunks) if (item != null && item.contextId >= 0) out.add(item.contextId);
        return out;
    }

    private static List<RankedContext> ranking(List<RagService.ScoredChunk> chunks) {
        List<RankedContext> out = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            RagService.ScoredChunk item = chunks.get(i);
            if (item == null) continue;
            RankedContext row = new RankedContext();
            row.contextId = item.contextId;
            row.rank = i + 1;
            row.score = item.score;
            row.source = item.source;
            row.sourceSupportCount = item.sourceSupportCount;
            row.fallback = item.fallback;
            out.add(row);
        }
        return out;
    }

    private static List<Long> missed(Collection<Long> relevant, List<RagService.ScoredChunk> retrieved) {
        Set<Long> actual = new LinkedHashSet<>(ids(retrieved));
        List<Long> out = new ArrayList<>();
        for (Long id : distinct(relevant)) if (!actual.contains(id)) out.add(id);
        return out;
    }

    private static List<Long> distinct(Collection<Long> values) {
        LinkedHashSet<Long> set = new LinkedHashSet<>();
        if (values != null) for (Long value : values) if (value != null && value >= 0) set.add(value);
        return new ArrayList<>(set);
    }

    private static List<Integer> normalizeTopKs(List<Integer> topKs) {
        LinkedHashSet<Integer> cleaned = new LinkedHashSet<>();
        if (topKs != null) for (Integer k : topKs) if (k != null && k > 0) cleaned.add(k);
        if (cleaned.isEmpty()) cleaned.addAll(List.of(1, 3, 5, 10));
        List<Integer> out = new ArrayList<>(cleaned);
        out.sort(Comparator.naturalOrder());
        return out;
    }

    private static void validateDataset(EvaluationDataset dataset) {
        if (dataset == null || blank(dataset.name) || blank(dataset.version)) {
            throw new IllegalArgumentException("数据集 name 和 version 为必填项");
        }
        if (dataset.cases == null || dataset.cases.isEmpty()) {
            throw new IllegalArgumentException("数据集至少需要一条黄金样例");
        }
        Set<String> ids = new LinkedHashSet<>();
        for (EvaluationCase item : dataset.cases) {
            if (item == null || blank(item.caseId) || blank(item.question)) {
                throw new IllegalArgumentException("每条样例必须包含 caseId 和 question");
            }
            if (!ids.add(item.caseId)) throw new IllegalArgumentException("caseId 不可重复: " + item.caseId);
            if (item.relevantContextIds == null || item.relevantContextIds.isEmpty()) {
                throw new IllegalArgumentException("每条样例至少需要一个 relevantContextIds（caseId=" + item.caseId + "）");
            }
        }
    }

    private static boolean blank(String text) { return text == null || text.isBlank(); }
    private static String key(String name, String version) { return name + "\u0000" + version; }
    private static String joinWarning(String left, String right) { return blank(left) ? right : left + "; " + right; }
    private static void addOnce(List<String> warnings, String message) { if (!warnings.contains(message)) warnings.add(message); }
}
