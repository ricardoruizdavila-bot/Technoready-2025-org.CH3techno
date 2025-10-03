package org.CH3techno.scholar.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Autor compatible con:
 *  - Semantic Scholar (authorId, name, url, affiliations[list], citationCount, hIndex, paperCount)
 *  - Proveedores previos (author_id, link, cited_by, affiliations[string])
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Author {

    @JsonAlias({"authorId", "id", "author_id"})
    private String authorId;

    @JsonAlias({"name", "title"})
    private String name;

    // En S2: List<String>, en otros: String. Usamos Object y normalizamos en getAffiliation().
    @JsonAlias("affiliations")
    private Object affiliations;

    @JsonAlias({"citationCount", "cited_by"})
    private Integer citedBy;

    @JsonAlias({"url", "link", "profile_url"})
    private String link;

    @JsonAlias({"hIndex", "h_index"})
    private Integer hIndex;

    @JsonAlias({"paperCount", "papers_count"})
    private Integer paperCount;

    // No viene en el search de S2; lo mantenemos para compatibilidad con la vista.
    private List<Publication> publications;

    // ===== Getters / Setters (lo que usa la vista) =====
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** Devuelve una afiliación legible (string o primer elemento si es lista). */
    public String getAffiliation() {
        if (affiliations == null) return null;
        if (affiliations instanceof String s) return s;
        if (affiliations instanceof List<?> l && !l.isEmpty()) return String.valueOf(l.get(0));
        return String.valueOf(affiliations);
    }
    public Object getAffiliationsRaw() { return affiliations; }
    public void setAffiliations(Object affiliations) { this.affiliations = affiliations; }

    public Integer getCitedBy() { return citedBy; }
    public void setCitedBy(Integer citedBy) { this.citedBy = citedBy; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public Integer gethIndex() { return hIndex; }
    public void sethIndex(Integer hIndex) { this.hIndex = hIndex; }

    public Integer getPaperCount() { return paperCount; }
    public void setPaperCount(Integer paperCount) { this.paperCount = paperCount; }

    public List<Publication> getPublications() { return publications; }
    public void setPublications(List<Publication> publications) { this.publications = publications; }
}
