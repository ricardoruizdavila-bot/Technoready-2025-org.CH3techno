package org.CH3techno.scholar.service;

import org.CH3techno.scholar.db.ArticleDao;
import org.CH3techno.scholar.db.DbManager;
import org.CH3techno.scholar.db.ResearcherDao;
import org.CH3techno.scholar.model.Author;
import org.CH3techno.scholar.model.Publication;

import java.sql.Connection;
import java.util.List;

public class DbSeedService {
    private final ScholarApiClient api;
    public DbSeedService(ScholarApiClient api) { this.api = api; }

    /** Guarda 2 investigadores y 3 artículos por cada uno. */
    public void seedTwoResearchers(String r1, String r2) throws Exception {
        if (r1 == null || r1.isBlank() || r2 == null || r2.isBlank())
            throw new IllegalArgumentException("Se requieren dos nombres de investigadores.");

        try (DbManager db = new DbManager()) {
            db.init();
            Connection c = db.getConnection();
            ResearcherDao rdao = new ResearcherDao(c);
            ArticleDao adao = new ArticleDao(c);

            seedOne(r1, rdao, adao);
            seedOne(r2, rdao, adao);

            c.commit();
        }
    }

    private void seedOne(String name, ResearcherDao rdao, ArticleDao adao) throws Exception {
        String authorId = api.resolveScholarAuthorIdByName(name);
        if (authorId == null || authorId.isBlank())
            throw new IllegalStateException("No se pudo resolver author_id de Google Scholar para: " + name);

        var authorResp = api.searchAuthorById(authorId);
        List<Author> authors = authorResp.getResults();
        if (authors == null || authors.isEmpty())
            throw new IllegalStateException("No se encontró el perfil para: " + name);

        Author a = authors.get(0);
        long rid = rdao.getOrCreate(a.getName(), a.getAuthorId(), a.getLink());

        List<Publication> pubs = a.getPublications();
        if (pubs != null) {
            int n = Math.min(3, pubs.size());
            for (int i = 0; i < n; i++) {
                Publication p = pubs.get(i);
                String title = nz(p.getTitle());
                String authorsCsv = a.getName();     // simple: autor principal; puedes ampliar
                String pubDate = nz(p.getYear());    // guardamos año
                String abs = null;                   // normalmente no viene en este endpoint
                String link = p.getLink();
                String keywords = null;              // opcional
                Integer citedBy = p.getCitedBy();

                adao.insertArticle(rid, title, authorsCsv, pubDate, abs, link, keywords, citedBy);
            }
        }
    }

    private static String nz(String s) { return (s == null ? "" : s); }
}
