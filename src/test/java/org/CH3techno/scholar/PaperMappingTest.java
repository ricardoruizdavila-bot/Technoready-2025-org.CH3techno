package org.CH3techno.scholar;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.CH3techno.scholar.model.Paper;
import org.CH3techno.scholar.model.PaperSearchResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaperMappingTest {
    @Test
    void parseSerpApiGoogleScholarOrganicResults() throws Exception {
        String json = """
      { "organic_results": [
          { "title": "Cell biology",
            "link": "https://example.com/paper",
            "snippet": "A short abstract...",
            "publication_info": { "summary": "TD Pollard - proteins, 2002" },
            "inline_links": { "cited_by": { "total": 1097 } }
          }
        ]
      }
    """;
        ObjectMapper m = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        PaperSearchResponse r = m.readValue(json, PaperSearchResponse.class);
        assertNotNull(r.getResults());
        assertEquals(1, r.getResults().size());
        Paper p = r.getResults().get(0);
        assertEquals("Cell biology", p.getTitle());
        assertEquals("https://example.com/paper", p.getLink());
        assertEquals(Integer.valueOf(1097), p.getCitationsTotal());
        assertEquals("TD Pollard - proteins, 2002", p.getPublicationSummary());
    }
}
