package org.CH3techno.scholar.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modelo de Autor compatible con:
 *  - Semantic Scholar (authorId, name, url, affiliations[list], citationCount, hIndex, paperCount)
 *  - SerpAPI Google Scholar (author_id, link, cited_by, affiliations[string])
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Author {

    @JsonAlias({"authorId", "id", "author_id"})
    private String authorId;

    @JsonAlias({"name", "title"})
    private String name;

    /**
     * En algunas APIs viene como String; en otras, como List<String>.
     * Guardamos el valor crudo y normalizamos en los getters.
     */
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

    /** Publicaciones del autor (si el endpoint las entrega). */
    private List<Publication> publications;

    // ===== Getters / Setters =====
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** Devuelve una afiliación legible (string o el primer elemento si es lista). */
    public String getAffiliation() {
        if (affiliations == null) return null;
        if (affiliations instanceof String s) return s;
        if (affiliations instanceof List<?> l && !l.isEmpty()) return String.valueOf(l.get(0));
        return String.valueOf(affiliations);
    }

    /** Devuelve la(s) afiliación(es) como lista de strings (nunca null). */
    public List<String> getAffiliationsAsList() {
        if (affiliations == null) return Collections.emptyList();
        if (affiliations instanceof String s) return Collections.singletonList(s);
        if (affiliations instanceof List<?> l) {
            List<String> out = new ArrayList<>(l.size());
            for (Object o : l) if (o != null) out.add(String.valueOf(o));
            return out;
        }
        return Collections.singletonList(String.valueOf(affiliations));
    }

    /** Setter genérico (para Jackson y mapeos manuales). */
    public void setAffiliations(Object affiliations) { this.affiliations = affiliations; }

    /** Setter de conveniencia por si el mapper envía un String. */
    public void setAffiliation(String affiliation) { this.affiliations = affiliation; }

    public Integer getCitedBy() { return citedBy; }
    public void setCitedBy(Integer citedBy) { this.citedBy = citedBy; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    // Conservamos la firma usada previamente en tu proyecto.
    public Integer gethIndex() { return hIndex; }
    public void sethIndex(Integer hIndex) { this.hIndex = hIndex; }

    public Integer getPaperCount() { return paperCount; }
    public void setPaperCount(Integer paperCount) { this.paperCount = paperCount; }

    public List<Publication> getPublications() { return publications; }
    public void setPublications(List<Publication> publications) { this.publications = publications; }

    // (Opcional) para depuración
    @Override public String toString() {
        return "Author{" +
                "authorId='" + authorId + '\'' +
                ", name='" + name + '\'' +
                ", affiliation='" + getAffiliation() + '\'' +
                ", citedBy=" + citedBy +
                ", link='" + link + '\'' +
                ", hIndex=" + hIndex +
                ", paperCount=" + paperCount +
                ", publications=" + (publications == null ? 0 : publications.size()) +
                '}';
    }
}
