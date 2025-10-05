package org.CH3techno.scholar.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.CH3techno.scholar.model.AuthorSearchResponse;
import org.CH3techno.scholar.model.PaperSearchResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

//añadido para sprint 3 apoya a la busqueda de autores por id y su generacion en tablas
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ScholarApiClient {
    private static final Logger log = LoggerFactory.getLogger(ScholarApiClient.class);

    private final String baseUrl;
    private final String apiKey;

    // Parametrización por entorno para adaptarnos a distintos proveedores
    private final String queryParam;   // p.ej., "q" (papers SerpAPI) o "query" (Semantic Scholar)
    private final String keyParam;     // p.ej., "api_key" si la key va en query
    private final String keyHeader;    // p.ej., "X-API-KEY" si la key va por header
    private final String extraQuery;   // p.ej., "engine=google_scholar&hl=es"

    private final CloseableHttpClient http;
    private final ObjectMapper mapper;

    public ScholarApiClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;

        this.queryParam = getenvOr("SCHOLAR_QUERY_PARAM", "query");
        this.keyParam   = getenvOr("SCHOLAR_KEY_PARAM", "api_key");
        this.keyHeader  = getenvOr("SCHOLAR_KEY_HEADER", "");
        this.extraQuery = getenvOr("SCHOLAR_EXTRA_QUERY", "");

        RequestConfig cfg = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(10))
                .setResponseTimeout(Timeout.ofSeconds(20))
                .build();

        this.http = HttpClients.custom()
                .setDefaultRequestConfig(cfg)
                .build();

        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static String getenvOr(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }

    /** BÚSQUEDA DE AUTORES (queda igual, ahora reutiliza doRequest) */
    public AuthorSearchResponse searchAuthors(String query) throws IOException {
        String body = doRequest(query);
        return mapper.readValue(body, AuthorSearchResponse.class);
    }

    /** BÚSQUEDA DE ARTÍCULOS (papers) */
    public PaperSearchResponse searchPapers(String query) throws IOException {
        String body = doRequest(query);
        return mapper.readValue(body, PaperSearchResponse.class);
    }

    /** GET reutilizable: arma URL con variables de entorno y devuelve el body */
    private String doRequest(String query) throws IOException {
        String q = URLEncoder.encode(query, StandardCharsets.UTF_8);

        StringBuilder sb = new StringBuilder("?");
        sb.append(queryParam).append("=").append(q);
        if (!extraQuery.isBlank()) {
            sb.append("&").append(extraQuery);
        }
        if (!keyParam.isBlank()) {
            sb.append("&").append(keyParam).append("=").append(apiKey);
        }
        String url = baseUrl + sb;

        HttpGet get = new HttpGet(url);
        get.addHeader("Accept", "application/json");
        if (!keyHeader.isBlank()) {
            get.addHeader(keyHeader, apiKey);
        }

        try (CloseableHttpResponse res = http.execute(get)) {
            int code = res.getCode();
            HttpEntity entity = res.getEntity();

            String body;
            try {
                body = (entity != null) ? EntityUtils.toString(entity) : "";
            } catch (ParseException pe) {
                throw new IOException("No se pudo leer el cuerpo de la respuesta HTTP.", pe);
            }

            log.debug("HTTP {} body: {}", code, body);

            if (code >= 200 && code < 300) {
                return body;
            } else if (code == 429) {
                throw new IOException("Rate limit excedido (429). Intenta más tarde.");
            } else if (code >= 500) {
                throw new IOException("Error del servidor API (" + code + ").");
            } else {
                throw new IOException("Error de solicitud API (" + code + "): " + body);
            }
        }
    }
    /** Paso 1: resolver author_id de Google Scholar buscando el perfil en Google. */
    public String resolveScholarAuthorIdByName(String authorName) throws IOException {
        // Bias: perfiles de Scholar
        String q = "site:scholar.google.com/citations?user= " + authorName;
        String body = doRequestWith("q", "engine=google&hl=es&num=10", q);

        JsonNode root = mapper.readTree(body);
        JsonNode results = root.path("organic_results");

        java.util.regex.Pattern p = java.util.regex.Pattern.compile("[?&]user=([A-Za-z0-9_-]+)");
        if (results.isArray()) {
            for (JsonNode r : results) {
                // link principal
                String link = r.hasNonNull("link") ? r.get("link").asText() : null;
                if (link != null) {
                    java.util.regex.Matcher m = p.matcher(link);
                    if (m.find()) return m.group(1);
                }
                // sitelinks
                JsonNode sitelinks = r.path("sitelinks");
                if (sitelinks.isArray()) {
                    for (JsonNode s : sitelinks) {
                        String l2 = s.hasNonNull("link") ? s.get("link").asText() : null;
                        if (l2 != null) {
                            java.util.regex.Matcher m2 = p.matcher(l2);
                            if (m2.find()) return m2.group(1);
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Paso 2: traer perfil del autor por author_id. */
    public org.CH3techno.scholar.model.AuthorSearchResponse searchAuthorById(String authorId) throws IOException {
        String body = doRequestWith("author_id", "engine=google_scholar_author&hl=es", authorId);
        org.CH3techno.scholar.model.Author a = fromSerpApiAuthorJson(body);

        org.CH3techno.scholar.model.AuthorSearchResponse resp =
                new org.CH3techno.scholar.model.AuthorSearchResponse();
        java.util.List<org.CH3techno.scholar.model.Author> list = new java.util.ArrayList<>();
        list.add(a);
        resp.setResults(list);
        return resp;
    }

    /** Mapear JSON de SerpAPI (google_scholar_author) a Author + Publications. */
    private org.CH3techno.scholar.model.Author fromSerpApiAuthorJson(String body) throws IOException {
        JsonNode root = mapper.readTree(body);
        JsonNode a = root.path("author");

        org.CH3techno.scholar.model.Author out = new org.CH3techno.scholar.model.Author();
        if (a.isObject()) {
            if (a.hasNonNull("author_id")) out.setAuthorId(a.get("author_id").asText());
            if (a.hasNonNull("name"))      out.setName(a.get("name").asText());
            if (a.hasNonNull("link"))      out.setLink(a.get("link").asText());
            if (a.hasNonNull("cited_by"))  out.setCitedBy(a.get("cited_by").asInt());

            // Ajusta esta línea según tu modelo: setAffiliation(String) o setAffiliations(List<String>)
            if (a.hasNonNull("affiliations")) {
                try { out.setAffiliation(a.get("affiliations").asText()); } catch (Throwable ignore) {}
            }
        }

        java.util.List<org.CH3techno.scholar.model.Publication> pubs = new java.util.ArrayList<>();
        JsonNode arts = root.path("articles");
        if (arts.isArray()) {
            for (JsonNode it : arts) {
                org.CH3techno.scholar.model.Publication p =
                        new org.CH3techno.scholar.model.Publication();
                if (it.hasNonNull("title"))       p.setTitle(it.get("title").asText());
                if (it.hasNonNull("year"))        p.setYear(it.get("year").asText());
                if (it.hasNonNull("publication")) p.setVenue(it.get("publication").asText());
                if (it.hasNonNull("link"))        p.setLink(it.get("link").asText());

                Integer cb = null;
                if (it.has("cited_by")) {
                    JsonNode cbn = it.get("cited_by");
                    if (cbn.isObject() && cbn.hasNonNull("value")) cb = cbn.get("value").asInt();
                    else if (cbn.isInt() || cbn.isLong())         cb = cbn.asInt();
                }
                p.setCitedBy(cb == null ? 0 : cb);
                pubs.add(p);
            }
        }
        out.setPublications(pubs);

        // Fallback: si no vino author_id, tomarlo de search_parameters
        if ((out.getAuthorId() == null || out.getAuthorId().isBlank())
                && root.path("search_parameters").hasNonNull("author_id")) {
            out.setAuthorId(root.path("search_parameters").get("author_id").asText());
        }
        return out;
    }

    /** GET con qParam/extra específicos para cada engine (ignora las env por defecto). */
    private String doRequestWith(String qParam, String extra, String value) throws IOException {
        String q = java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder("?")
                .append(qParam).append("=").append(q);
        if (extra != null && !extra.isBlank()) sb.append("&").append(extra);
        if (this.keyParam != null && !this.keyParam.isBlank())
            sb.append("&").append(this.keyParam).append("=").append(this.apiKey);

        String url = this.baseUrl + sb.toString();

        HttpGet get = new HttpGet(url);
        get.addHeader("Accept", "application/json");
        if (this.keyHeader != null && !this.keyHeader.isBlank())
            get.addHeader(this.keyHeader, this.apiKey);

        try (CloseableHttpResponse res = http.execute(get)) {
            int code = res.getCode();
            HttpEntity entity = res.getEntity();
            String body;
            try { body = (entity != null) ? EntityUtils.toString(entity) : ""; }
            catch (ParseException pe) { throw new IOException("No se pudo leer el cuerpo.", pe); }

            log.debug("HTTP {} body: {}", code, body);

            if (code >= 200 && code < 300) return body;
            if (code == 429) throw new IOException("Rate limit excedido (429). Intenta más tarde.");
            if (code >= 500) throw new IOException("Error del servidor API (" + code + ").");
            throw new IOException("Error de solicitud API (" + code + "): " + body);
        }
    }


    public void close() throws IOException {
        http.close();
    }
}
