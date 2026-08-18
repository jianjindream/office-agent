package com.jianjin.assistant.infrastructure.persistence;

import com.jianjin.assistant.infrastructure.platform.PostgresConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class ChatHistoryRepository {

    private static final Logger log = LoggerFactory.getLogger(ChatHistoryRepository.class);

    private final PostgresConnector pg;

    public ChatHistoryRepository(PostgresConnector pg) {
        this.pg = pg;
    }

    public static class Row {
        public long id;
        public String role;
        public String content;
        public String createdAt;
    }

    public synchronized long save(String userId, String sessionId, String role, String content) {
        Connection c = pg.connection();
        if (c == null) return 0;
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO chat_history (user_id, session_id, role, content) VALUES (?, ?, ?, ?) RETURNING id")) {
            ps.setString(1, userId); ps.setString(2, sessionId); ps.setString(3, role); ps.setString(4, content);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong(1) : 0; }
        } catch (SQLException e) {
            log.warn("聊天记录保存失败: {}", e.getMessage());
        }
        return 0;
    }

    public synchronized List<Row> load(String userId, String sessionId, long afterId, int limit) {
        List<Row> rows = new ArrayList<>();
        Connection c = pg.connection();
        if (c == null) return rows;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, role, content, TO_CHAR(created_at, 'HH24:MI:SS') FROM chat_history " +
                        "WHERE user_id = ? AND session_id = ? AND id > ? ORDER BY id DESC LIMIT ?")) {
            ps.setString(1, userId); ps.setString(2, sessionId); ps.setLong(3, afterId); ps.setInt(4, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Row r = new Row();
                r.id = rs.getLong(1); r.role = rs.getString(2);
                r.content = rs.getString(3); r.createdAt = rs.getString(4);
                    rows.add(r);
                }
            }
        } catch (SQLException e) {
            log.warn("加载聊天记录失败: {}", e.getMessage());
        }
        Collections.reverse(rows);
        return rows;
    }

    public synchronized void deleteThrough(String userId, String sessionId, long throughId) {
        if (throughId <= 0) return;
        Connection c = pg.connection();
        if (c == null) return;
        try (PreparedStatement ps = c.prepareStatement(
                "DELETE FROM chat_history WHERE user_id = ? AND session_id = ? AND id <= ?")) {
            ps.setString(1, userId); ps.setString(2, sessionId); ps.setLong(3, throughId); ps.executeUpdate();
        } catch (SQLException e) { log.warn("聊天历史清理失败: {}", e.getMessage()); }
    }
}
