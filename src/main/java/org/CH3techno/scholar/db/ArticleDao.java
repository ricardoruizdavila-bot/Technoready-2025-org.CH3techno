package org.CH3techno.scholar.db;

import java.sql.*;

public class ArticleDao {
    private final Connection conn;
    public ArticleDao(Connection conn) { this.conn = conn; }

    public void insertArticle(long researcherId,
                              String title,
                              String authorsCsv,
                              String publicationDate,
                              String abs,
                              String link,
                              String keywordsCsv,
                              Integer citedBy) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO article(" +
                        " researcher_id, title, authors, publication_date, abstract, link, keywords, cited_by" +
                        ") VALUES(?,?,?,?,?,?,?,?)")) {
            ps.setLong(1, researcherId);
            ps.setString(2, title);
            ps.setString(3, authorsCsv);
            ps.setString(4, publicationDate);
            ps.setString(5, abs);
            ps.setString(6, link);
            ps.setString(7, keywordsCsv);
            if (citedBy == null) ps.setNull(8, Types.INTEGER); else ps.setInt(8, citedBy);
            ps.executeUpdate();
        }
    }
}
