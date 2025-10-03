package org.CH3techno.scholar;

import org.CH3techno.scholar.controller.AuthorController;
import org.CH3techno.scholar.controller.PaperController;
import org.CH3techno.scholar.service.ScholarApiClient;
import org.CH3techno.scholar.view.ConsoleAuthorView;
import org.CH3techno.scholar.view.ConsolePaperView;

public class App {
    public static void main(String[] args) throws Exception {
        // Fallback sano: búsqueda de autores por nombre (Semantic Scholar)
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

        // Modo: "authors" (default) o "papers"
        String modeEnv = System.getenv().getOrDefault("SCHOLAR_MODE", "authors");
        boolean modePapers = modeEnv.equalsIgnoreCase("papers")
                || (args.length > 0 && "--papers".equalsIgnoreCase(args[0]));

        // Query según el modo (si se usa --papers, la query va después del flag)
        String query;
        if (modePapers && args.length > 0 && "--papers".equalsIgnoreCase(args[0])) {
            query = (args.length > 1)
                    ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))
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
