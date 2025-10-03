package org.CH3techno.scholar;

import org.junit.jupiter.api.Test;
import org.CH3techno.scholar.controller.AuthorController;
import org.CH3techno.scholar.service.ScholarApiClient;
import org.CH3techno.scholar.view.ConsoleAuthorView;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class AuthorControllerTest {

    @Test
    void onSearchAuthors_handlesEmptyQuery() {
        ScholarApiClient dummyApi = new ScholarApiClient("http://localhost", "x");
        ConsoleAuthorView view = new ConsoleAuthorView();
        AuthorController c = new AuthorController(dummyApi, view);
        assertDoesNotThrow(() -> c.onSearchAuthors("   "));
    }
}
