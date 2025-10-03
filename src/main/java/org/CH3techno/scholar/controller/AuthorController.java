package org.CH3techno.scholar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.CH3techno.scholar.model.AuthorSearchResponse;
import org.CH3techno.scholar.service.ScholarApiClient;
import org.CH3techno.scholar.view.ConsoleAuthorView;

public class AuthorController {
    private static final Logger log = LoggerFactory.getLogger(AuthorController.class);

    private final ScholarApiClient api;
    private final ConsoleAuthorView view;

    public AuthorController(ScholarApiClient api, ConsoleAuthorView view) {
        this.api = api;
        this.view = view;
    }

    public void onSearchAuthors(String query) {
        try {
            if (query == null || query.isBlank()) {
                view.showError("La búsqueda no puede estar vacía.");
                return;
            }
            AuthorSearchResponse resp = api.searchAuthors(query);
            view.showAuthors(resp.getResults());
        } catch (Exception e) {
            log.error("Fallo en búsqueda de autores", e);
            view.showError("No se pudo completar la búsqueda: " + e.getMessage());
        }
    }
}
