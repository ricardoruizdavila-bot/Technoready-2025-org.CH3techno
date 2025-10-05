package org.CH3techno.scholar.db;

import java.sql.*;

public class DbManager implements AutoCloseable {
    private final String url;
    private Connection conn;

    public DbManager() {
        // Override opcional: DB_URL=jdbc:sqlite:/ruta/absoluta/scholar.db
        this.url = System.getenv().getOrDefault("DB_URL", "jdbc:sqlite:scholar.db");
    }

    public void init() throws SQLException {
        this.conn = DriverManager.getConnection(url);
        this.conn.setAutoCommit(false);
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS researcher(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  name TEXT NOT NULL,
                  gs_author_id TEXT,
                  profile_link TEXT
                );
            """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS article(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  researcher_id INTEGER,
                  title TEXT NOT NULL,
                  authors TEXT,
                  publication_date TEXT,
                  abstract TEXT,
                  link TEXT,
                  keywords TEXT,
                  cited_by INTEGER,
                  UNIQUE(researcher_id, title),
                  FOREIGN KEY(researcher_id) REFERENCES researcher(id)
                );
            """);
            st.execute("CREATE INDEX IF NOT EXISTS idx_article_researcher ON article(researcher_id);");
        }
        conn.commit();
    }

    public Connection getConnection() { return conn; }

    @Override public void close() {
        try { if (conn != null) conn.close(); } catch (Exception ignore) {}
    }
}
