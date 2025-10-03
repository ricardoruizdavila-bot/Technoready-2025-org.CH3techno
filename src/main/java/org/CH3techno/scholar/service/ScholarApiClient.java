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

    public void close() throws IOException {
        http.close();
    }
}
