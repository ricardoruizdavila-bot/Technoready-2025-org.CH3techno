package org.CH3techno.scholar.view;

import org.CH3techno.scholar.model.Paper;

import java.util.List;

public class ConsolePaperView {

    public void showPapers(List<Paper> papers) {
        if (papers == null || papers.isEmpty()) {
            System.out.println("No se encontraron artículos.");
            return;
        }
        System.out.println("=== Resultados de artículos ===");
        for (Paper p : papers) {
            System.out.printf("- %s%n", safe(p.getTitle()));
            String sum = p.getPublicationSummary();
            if (sum != null && !sum.isBlank()) {
                System.out.println("  " + sum);
            }
            Integer cites = p.getCitationsTotal();
            System.out.printf("  Citas: %s%n", cites != null ? cites : "—");
            if (p.getLink() != null && !p.getLink().isBlank()) {
                System.out.println("  Link: " + p.getLink());
            }
            String sn = p.getSnippet();
            if (sn != null && !sn.isBlank()) {
                System.out.println("  Snippet: " + sn);
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
