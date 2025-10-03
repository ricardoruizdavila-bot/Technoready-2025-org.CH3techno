package org.CH3techno.scholar.controller;

import org.CH3techno.scholar.model.PaperSearchResponse;
import org.CH3techno.scholar.service.ScholarApiClient;
import org.CH3techno.scholar.view.ConsolePaperView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaperController {
    private static final Logger log = LoggerFactory.getLogger(PaperController.class);

    private final ScholarApiClient api;
    private final ConsolePaperView view;

    public PaperController(ScholarApiClient api, ConsolePaperView view) {
        this.api = api; this.view = view;
    }

    public void onSearchPapers(String term) {
        try {
            String q = term == null ? "" : term.trim();
            if (q.isEmpty()) {
                view.showError("La búsqueda no puede estar vacía.");
                return;
            }
            PaperSearchResponse resp = api.searchPapers(q);
            view.showPapers(resp.getResults());
        } catch (Exception e) {
            log.error("Fallo en búsqueda de artículos", e);
            view.showError("No se pudo completar la búsqueda: " + e.getMessage());
        }
    }
}
