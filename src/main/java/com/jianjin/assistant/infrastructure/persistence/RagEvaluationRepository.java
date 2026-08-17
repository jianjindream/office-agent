package com.jianjin.assistant.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianjin.assistant.domain.rageval.EvaluationCase;
import com.jianjin.assistant.domain.rageval.EvaluationDataset;
import com.jianjin.assistant.domain.rageval.EvaluationRunReport;
import com.jianjin.assistant.infrastructure.platform.PostgresConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** PostgreSQL storage for versioned golden sets and immutable evaluation reports. */
@Repository
public class RagEvaluationRepository {
    private static final Logger log = LoggerFactory.getLogger(RagEvaluationRepository.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final PostgresConnector pg;

    public RagEvaluationRepository(PostgresConnector pg) { this.pg = pg; }

    public boolean saveDataset(EvaluationDataset dataset) {
        Connection connection = pg.connection();
        if (connection == null) return false;
        try {
            long id;
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO rag_eval_datasets (name, version, description, updated_at) VALUES (?, ?, ?, NOW()) " +
                            "ON CONFLICT (name, version) DO UPDATE SET description = EXCLUDED.description, updated_at = NOW() " +
                            "RETURNING id")) {
                statement.setString(1, dataset.name);
                statement.setString(2, dataset.version);
                statement.setString(3, dataset.description);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) return false;
                    id = result.getLong(1);
                }
            }
            try (PreparedStatement delete = connection.prepareStatement("DELETE FROM rag_eval_cases WHERE dataset_id = ?")) {
                delete.setLong(1, id);
                delete.executeUpdate();
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO rag_eval_cases (dataset_id, case_id, case_json) VALUES (?, ?, ?::jsonb)")) {
                for (EvaluationCase item : dataset.cases) {
                    insert.setLong(1, id);
                    insert.setString(2, item.caseId);
                    insert.setString(3, MAPPER.writeValueAsString(item));
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            return true;
        } catch (Exception e) {
            log.warn("RAG evaluation dataset save failed: {}", e.getMessage());
            return false;
        }
    }

    public EvaluationDataset findDataset(String name, String version) {
        Connection connection = pg.connection();
        if (connection == null) return null;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, name, version, description FROM rag_eval_datasets WHERE name = ? AND version = ?")) {
            statement.setString(1, name);
            statement.setString(2, version);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return null;
                long id = result.getLong("id");
                EvaluationDataset dataset = new EvaluationDataset();
                dataset.name = result.getString("name");
                dataset.version = result.getString("version");
                dataset.description = result.getString("description");
                dataset.cases = loadCases(connection, id);
                return dataset;
            }
        } catch (Exception e) {
            log.warn("RAG evaluation dataset load failed: {}", e.getMessage());
            return null;
        }
    }

    public boolean saveRun(EvaluationRunReport report) {
        Connection connection = pg.connection();
        if (connection == null) return false;
        try {
            Long datasetId = datasetId(connection, report.datasetName, report.datasetVersion);
            if (datasetId == null) return false;
            String config = MAPPER.writeValueAsString(report.configurationSnapshot);
            String complete = MAPPER.writeValueAsString(report);
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO rag_eval_runs (id, dataset_id, status, config_snapshot, report_json, completed_at) " +
                            "VALUES (?::uuid, ?, ?, ?::jsonb, ?::jsonb, NOW())")) {
                statement.setString(1, report.runId);
                statement.setLong(2, datasetId);
                statement.setString(3, report.status);
                statement.setString(4, config);
                statement.setString(5, complete);
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO rag_eval_case_results (run_id, case_id, result_json) VALUES (?::uuid, ?, ?::jsonb)")) {
                for (var item : report.caseResults) {
                    statement.setString(1, report.runId);
                    statement.setString(2, item.caseId);
                    statement.setString(3, MAPPER.writeValueAsString(item));
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            return true;
        } catch (Exception e) {
            log.warn("RAG evaluation run save failed: {}", e.getMessage());
            return false;
        }
    }

    public EvaluationRunReport findRun(String runId) {
        Connection connection = pg.connection();
        if (connection == null) return null;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT report_json::text FROM rag_eval_runs WHERE id = ?::uuid")) {
            statement.setString(1, runId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? MAPPER.readValue(result.getString(1), EvaluationRunReport.class) : null;
            }
        } catch (Exception e) {
            log.warn("RAG evaluation run load failed: {}", e.getMessage());
            return null;
        }
    }

    public List<EvaluationRunReport> listRuns(String datasetName, String datasetVersion) {
        List<EvaluationRunReport> out = new ArrayList<>();
        Connection connection = pg.connection();
        if (connection == null) return out;
        StringBuilder sql = new StringBuilder("SELECT r.report_json::text FROM rag_eval_runs r " +
                "JOIN rag_eval_datasets d ON d.id = r.dataset_id WHERE 1 = 1");
        List<String> values = new ArrayList<>();
        String normalizedName = blankToNull(datasetName);
        String normalizedVersion = blankToNull(datasetVersion);
        if (normalizedName != null) {
            sql.append(" AND d.name = ?");
            values.add(normalizedName);
        }
        if (normalizedVersion != null) {
            sql.append(" AND d.version = ?");
            values.add(normalizedVersion);
        }
        sql.append(" ORDER BY r.completed_at DESC");
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.size(); i++) statement.setString(i + 1, values.get(i));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    EvaluationRunReport report = MAPPER.readValue(result.getString(1), EvaluationRunReport.class);
                    report.caseResults = new ArrayList<>();
                    out.add(report);
                }
            }
        } catch (Exception e) {
            log.warn("RAG evaluation run list failed: {}", e.getMessage());
        }
        return out;
    }

    private static List<EvaluationCase> loadCases(Connection connection, long datasetId) throws Exception {
        List<EvaluationCase> cases = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT case_json::text FROM rag_eval_cases WHERE dataset_id = ? ORDER BY case_id")) {
            statement.setLong(1, datasetId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) cases.add(MAPPER.readValue(result.getString(1), EvaluationCase.class));
            }
        }
        return cases;
    }

    private static Long datasetId(Connection connection, String name, String version) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM rag_eval_datasets WHERE name = ? AND version = ?")) {
            statement.setString(1, name);
            statement.setString(2, version);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : null;
            }
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
