package com.jianjin.assistant.infrastructure.persistence;

import com.jianjin.assistant.infrastructure.platform.PostgresConnector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LongTermRepository {

    private static final Logger log = LoggerFactory.getLogger(LongTermRepository.class);
    private static final ObjectMapper M = new ObjectMapper().registerModule(new JavaTimeModule());

    private final PostgresConnector pg;

    public LongTermRepository(PostgresConnector pg) {
        this.pg = pg;
    }

    public static class Row {
        public int id;
        public String content;
        public double importance;
        public List<Double> embedding;
        public Timestamp createdAt;
        public Timestamp lastAccessed;
        public String category;
        public List<String> tags;
        public String slotHint;
    }

    public synchronized int save(String userId, String content, double importance, String embeddingJson) {
        return saveClassified(userId, content, importance, embeddingJson, "general", null, null);
    }

    public synchronized int saveClassified(String userId, String content, double importance, String embeddingJson,
                              String category, String tagsJson, String slotHint) {
        Connection c = pg.connection();
        if (c == null) return -1;
        String cat = (category == null || category.isEmpty()) ? "general" : category;
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO long_term_memory (user_id, content, importance, embedding, category, tags, slot_hint) " +
                        "VALUES (?, ?, ?, ?::jsonb, ?, ?::jsonb, ?) RETURNING id")) {
            ps.setString(1, userId); ps.setString(2, content);
            ps.setDouble(3, importance); ps.setString(4, embeddingJson);
            ps.setString(5, cat); ps.setString(6, tagsJson); ps.setString(7, slotHint);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.warn("长期记忆保存失败: {}", e.getMessage());
        }
        return -1;
    }

    public synchronized List<Row> loadAll(String userId) {
        List<Row> items = new ArrayList<>();
        Connection c = pg.connection();
        if (c == null) return items;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, content, importance, embedding, " +
                        "COALESCE(created_at, NOW()), COALESCE(last_accessed, NOW()), " +
                        "COALESCE(category, 'general'), tags, slot_hint " +
                        "FROM long_term_memory WHERE user_id = ? ORDER BY id")) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Row row = new Row();
                row.id = rs.getInt(1); row.content = rs.getString(2); row.importance = rs.getDouble(3);
                String embJson = rs.getString(4);
                if (embJson != null && !embJson.isEmpty()) {
                    try {
                        row.embedding = M.readValue(embJson,
                                M.getTypeFactory().constructCollectionType(List.class, Double.class));
                    } catch (Exception ignored) {}
                }
                row.createdAt = rs.getTimestamp(5);
                row.lastAccessed = rs.getTimestamp(6);
                row.category = rs.getString(7);
                String tagsJson = rs.getString(8);
                if (tagsJson != null && !tagsJson.isEmpty()) {
                    try {
                        row.tags = M.readValue(tagsJson,
                                M.getTypeFactory().constructCollectionType(List.class, String.class));
                    } catch (Exception ignored) {}
                }
                row.slotHint = rs.getString(9);
                items.add(row);
            }
            }
        } catch (SQLException e) {
            log.warn("加载长期记忆失败: {}", e.getMessage());
        }
        return items;
    }

    public synchronized void update(String userId, int id, String content, double importance, String embeddingJson) {
        Connection c = pg.connection();
        if (c == null) return;
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE long_term_memory SET content = ?, importance = ?, embedding = ?::jsonb, " +
                        "last_accessed = NOW() WHERE id = ? AND user_id = ?")) {
            ps.setString(1, content); ps.setDouble(2, importance);
            ps.setString(3, embeddingJson); ps.setInt(4, id); ps.setString(5, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warn("长期记忆更新失败 (id={}): {}", id, e.getMessage());
        }
    }

    public synchronized void deleteAll(String userId, List<Integer> ids) {
        Connection c = pg.connection();
        if (c == null || ids == null || ids.isEmpty()) return;
        String placeholders = String.join(",", ids.stream().map(i -> "?").toArray(String[]::new));
        try (PreparedStatement ps = c.prepareStatement(
                "DELETE FROM long_term_memory WHERE user_id = ? AND id IN (" + placeholders + ")")) {
            ps.setString(1, userId);
            for (int i = 0; i < ids.size(); i++) ps.setInt(i + 2, ids.get(i));
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warn("长期记忆批量删除失败: {}", e.getMessage());
        }
    }
}
