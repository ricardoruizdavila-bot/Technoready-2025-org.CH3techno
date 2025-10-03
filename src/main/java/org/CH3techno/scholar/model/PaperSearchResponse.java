package org.CH3techno.scholar.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** SerpAPI google_scholar devuelve la lista en "organic_results". */
public class PaperSearchResponse {
    @JsonProperty("organic_results")
    private List<Paper> results;

    public List<Paper> getResults() { return results; }
    public void setResults(List<Paper> results) { this.results = results; }
}
