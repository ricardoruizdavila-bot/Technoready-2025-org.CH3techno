package org.CH3techno.scholar;

import org.CH3techno.scholar.controller.PaperController;
import org.CH3techno.scholar.service.ScholarApiClient;
import org.CH3techno.scholar.view.ConsolePaperView;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PaperControllerTest {
    @Test
    void emptyQueryIsHandled() {
        ScholarApiClient fake = new ScholarApiClient("http://localhost", "") {
            @Override public org.CH3techno.scholar.model.PaperSearchResponse searchPapers(String q) {
                throw new RuntimeException("No debe llamarse si la query está vacía");
            }
        };
        var view = new ConsolePaperView();
        var c = new PaperController(fake, view);
        assertDoesNotThrow(() -> c.onSearchPapers("   "));
    }
}
