package org.CH3techno.scholar.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Mapea la respuesta de:
 *   - Semantic Scholar: { "data": [ ... ], "total": n, "next": m }
 *   - (Opcional) otros proveedores: "profiles"
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorSearchResponse {

    // S2 usa "data"; dejamos "profiles" por compatibilidad opcional.
    @JsonAlias({"data", "profiles"})
    private List<Author> results;

    // Campos útiles de S2 (opcionales)
    private Integer total;

    // S2 devuelve "next" para paginación (offset del siguiente batch)
    @JsonAlias("next")
    private Integer next;

    public List<Author> getResults() {
        return results;
    }
    public void setResults(List<Author> results) {
        this.results = results;
    }

    public Integer getTotal() {
        return total;
    }
    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getNext() {
        return next;
    }
    public void setNext(Integer next) {
        this.next = next;
    }
}
