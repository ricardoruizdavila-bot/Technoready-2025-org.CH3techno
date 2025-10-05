package org.CH3techno.scholar.db;

import java.sql.*;

public class ResearcherDao {
    private final Connection conn;

    public ResearcherDao(Connection conn) { this.conn = conn; }

    /** Devuelve id en BD; crea si no existe (prioriza gs_author_id, si no, name). */
    public long getOrCreate(String name, String gsAuthorId, String profileLink) throws SQLException {
        Long existing = findIdByGsId(gsAuthorId);
        if (existing != null) return existing;

        existing = findIdByName(name);
        if (existing != null) return existing;

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO researcher(name, gs_author_id, profile_link) VALUES(?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, gsAuthorId);
            ps.setString(3, profileLink);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("No se pudo insertar el investigador.");
    }

    private Long findIdByGsId(String gsAuthorId) throws SQLException {
        if (gsAuthorId == null || gsAuthorId.isBlank()) return null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM researcher WHERE gs_author_id = ?")) {
            ps.setString(1, gsAuthorId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getLong(1); }
        }
        return null;
    }

    private Long findIdByName(String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM researcher WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getLong(1); }
        }
        return null;
    }
}
