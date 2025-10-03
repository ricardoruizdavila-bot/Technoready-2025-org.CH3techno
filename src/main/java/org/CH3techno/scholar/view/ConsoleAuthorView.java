package org.CH3techno.scholar.view;

import org.CH3techno.scholar.model.Author;
import org.CH3techno.scholar.model.Publication;

import java.util.List;

public class ConsoleAuthorView {

    public void showAuthors(List<Author> authors) {
        if (authors == null || authors.isEmpty()) {
            System.out.println("No se encontraron autores.");
            return;
        }
        System.out.println("=== Resultados de autores ===");
        for (Author a : authors) {
            String aff = a.getAffiliation();
            Integer cites = a.getCitedBy();
            System.out.printf("- %s (afiliación: %s) | citas: %s | id: %s%n",
                    safe(a.getName()),
                    safe(aff),
                    (cites != null ? cites : "—"),
                    safe(a.getAuthorId()));
            if (a.getPublications() != null && !a.getPublications().isEmpty()) {
                System.out.println("  Publicaciones destacadas:");
                for (Publication p : a.getPublications().stream().limit(3).toList()) {
                    System.out.printf("    · %s (%s) – %s | citas: %d%n",
                            safe(p.getTitle()), safe(p.getYear()), safe(p.getVenue()), p.getCitedBy());
                }
            }
            if (a.getLink() != null && !a.getLink().isBlank()) {
                System.out.println("  Perfil: " + a.getLink());
            }
        }
    }

    public void showError(String message) {
        System.err.println("[ERROR] " + message);
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
