package org.CH3techno.scholar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Paper {
    private String title;
    private String link;
    private String snippet;

    @JsonProperty("publication_info")
    private PublicationInfo publicationInfo;

    @JsonProperty("inline_links")
    private InlineLinks inlineLinks;

    // --- nested DTOs ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublicationInfo {
        // SerpAPI devuelve "summary" con autores/venue/año en una sola cadena.
        private String summary;
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InlineLinks {
        @JsonProperty("cited_by")
        private CitedBy citedBy;
        public CitedBy getCitedBy() { return citedBy; }
        public void setCitedBy(CitedBy citedBy) { this.citedBy = citedBy; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CitedBy {
        private Integer total;
        public Integer getTotal() { return total; }
        public void setTotal(Integer total) { this.total = total; }
    }

    // --- getters usados por la vista ---
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    /** Devuelve resumen de publicación (autores/venue/año) si existe. */
    public String getPublicationSummary() {
        return publicationInfo != null ? publicationInfo.getSummary() : null;
    }

    /** Devuelve total de citas si viene en inline_links.cited_by.total. */
    public Integer getCitationsTotal() {
        return (inlineLinks != null && inlineLinks.getCitedBy() != null)
                ? inlineLinks.getCitedBy().getTotal() : null;
    }
}
