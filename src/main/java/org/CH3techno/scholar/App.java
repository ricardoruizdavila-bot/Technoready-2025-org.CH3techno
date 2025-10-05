package org.CH3techno.scholar;

import org.CH3techno.scholar.controller.AuthorController;
import org.CH3techno.scholar.controller.PaperController;
import org.CH3techno.scholar.db.DbManager;
import org.CH3techno.scholar.service.DbSeedService;
import org.CH3techno.scholar.service.ScholarApiClient;
import org.CH3techno.scholar.view.ConsoleAuthorView;
import org.CH3techno.scholar.view.ConsolePaperView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

public class App {
    public static void main(String[] args) throws Exception {
        // Base URL por defecto (autores por nombre con Semantic Scholar).
        // Para SerpAPI (Google Scholar), exporta SCHOLAR_API_BASE_URL="https://serpapi.com/search.json"
        String baseUrl = System.getenv().getOrDefault(
                "SCHOLAR_API_BASE_URL",
                "https://api.semanticscholar.org/graph/v1/author/search"
        );

        String apiKey    = System.getenv("SCHOLAR_API_KEY");
        String keyParam  = System.getenv("SCHOLAR_KEY_PARAM");   // ej. "api_key" (SerpAPI) o "" (S2)
        String keyHeader = System.getenv("SCHOLAR_KEY_HEADER");  // ej. "X-API-KEY" o ""

        boolean keyNeeded =
                (keyParam != null && !keyParam.isBlank()) ||
                        (keyHeader != null && !keyHeader.isBlank());

        if (keyNeeded && (apiKey == null || apiKey.isBlank())) {
            System.err.println("Define SCHOLAR_API_KEY en tus variables de entorno.");
            System.exit(1);
        }

        // ----- MODO: SEED DB (Sprint 3) -----
        if (args.length > 0 && "--seed-db".equalsIgnoreCase(args[0])) {
            String r1 = (args.length > 1) ? args[1] : null;
            String r2 = (args.length > 2) ? args[2] : null;
            if (r1 == null || r2 == null) {
                System.err.println("Uso: --seed-db \"Investigador Uno\" \"Investigador Dos\"");
                System.exit(2);
            }

            ScholarApiClient client = new ScholarApiClient(baseUrl, apiKey);
            try {
                DbSeedService seeder = new DbSeedService(client);
                seeder.seedTwoResearchers(r1, r2);
                System.out.println("BD cargada correctamente: 2 investigadores × 3 artículos cada uno.");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("[ERROR] Falló la carga: " + e.getMessage());
                System.exit(3);
            } finally {
                client.close();
            }
            return;
        }

        // ----- MODO: LIST DB (verificar BD) -----
        if (args.length > 0 && "--list-db".equalsIgnoreCase(args[0])) {
            try (DbManager db = new DbManager()) {
                db.init();
                Connection c = db.getConnection();
                try (Statement st = c.createStatement()) {
                    String sql = """
                        SELECT r.name AS researcher, a.title, COALESCE(a.cited_by, 0) AS cited_by
                        FROM article a
                        JOIN researcher r ON r.id = a.researcher_id
                        ORDER BY r.name, a.id;
                        """;
                    try (ResultSet rs = st.executeQuery(sql)) {
                        System.out.println("=== Contenido de la BD ===");
                        while (rs.next()) {
                            String researcher = rs.getString("researcher");
                            String title = rs.getString("title");
                            int cited = rs.getInt("cited_by");
                            System.out.printf("- %s | %s (Citas: %d)%n", researcher, title, cited);
                        }
                    }
                }
                c.commit();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("[ERROR] No se pudo listar la BD: " + e.getMessage());
                System.exit(4);
            }
            return;
        }

        // ----- MODO: PAPERS o AUTHORS (como ya tenías) -----
        String modeEnv = System.getenv().getOrDefault("SCHOLAR_MODE", "authors");
        boolean modePapers = modeEnv.equalsIgnoreCase("papers")
                || (args.length > 0 && "--papers".equalsIgnoreCase(args[0]));

        String query;
        if (modePapers && args.length > 0 && "--papers".equalsIgnoreCase(args[0])) {
            query = (args.length > 1)
                    ? String.join(" ", Arrays.copyOfRange(args, 1, args.length))
                    : "biology";
        } else {
            query = (args.length > 0) ? String.join(" ", args) : "Albert Einstein";
        }

        ScholarApiClient client = new ScholarApiClient(baseUrl, apiKey);
        try {
            if (modePapers) {
                ConsolePaperView view = new ConsolePaperView();
                PaperController controller = new PaperController(client, view);
                controller.onSearchPapers(query);
            } else {
                ConsoleAuthorView view = new ConsoleAuthorView();
                AuthorController controller = new AuthorController(client, view);
                controller.onSearchAuthors(query);
            }
        } finally {
            client.close();
        }
    }
}
