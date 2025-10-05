package org.CH3techno.scholar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Publication {
    private String title;
    private String year;     // guarda solo el año si es lo único disponible
    private String venue;    // revista/congreso
    private String link;     // URL del artículo
    private int citedBy;     // total de citas (0 si no viene)

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public int getCitedBy() { return citedBy; }
    public void setCitedBy(int citedBy) { this.citedBy = citedBy; }
}
