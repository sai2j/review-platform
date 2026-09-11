package com.nit.discovery;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DiscoveryService {

    private final RestTemplate restTemplate;

    @Value("${tinyfish.api-key:TEST_KEY}")
    private String apiKey;

    public DiscoveryService() {

        this.restTemplate = new RestTemplate();
    }


    // =========================================================
    // SEARCH WEBSITE
    // =========================================================

    public List<WebsiteSearchResult> searchWebsites(
            String keyword) {

        List<WebsiteSearchResult> results =
                new ArrayList<>();

        if (keyword == null ||
            keyword.trim().isEmpty()) {

            return results;
        }

        String url = UriComponentsBuilder
                .fromUriString(
                        "https://api.search.tinyfish.ai"
                )
                .queryParam(
                        "query",
                        keyword
                )
                .toUriString();


        // =====================================================
        // HEADERS
        // =====================================================

        HttpHeaders headers =
                new HttpHeaders();

        headers.set(
                "Accept",
                "application/json"
        );

        headers.set(
                "X-API-Key",
                apiKey
        );


        HttpEntity<String> entity =
                new HttpEntity<>(headers);


        // =====================================================
        // CALL TINYFISH
        // =====================================================

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        Map.class
                );


        Map body =
                response.getBody();


        if (body == null) {

            return results;
        }


        List<Map<String, Object>> searchResults =
                (List<Map<String, Object>>)
                        body.get("results");


        if (searchResults == null ||
            searchResults.isEmpty()) {

            return results;
        }


        String searchKeyword =
                keyword
                        .toLowerCase()
                        .trim()
                        .replaceAll("\\s+", "");


        WebsiteSearchResult bestResult =
                null;

        int bestScore = 0;


        // =====================================================
        // CHECK ALL SEARCH RESULTS
        // =====================================================

        for (Map<String, Object> item :
                searchResults) {

            String title =
                    item.get("title") != null
                    ? item.get("title").toString()
                    : "";

            String resultUrl =
                    item.get("url") != null
                    ? item.get("url").toString()
                    : "";

            String description =
                    item.get("snippet") != null
                    ? item.get("snippet").toString()
                    : "";


            if (resultUrl.isBlank()) {

                continue;
            }


            // =================================================
            // ONLY REAL WEBSITE
            // =================================================

            if (!isActualWebsite(resultUrl)) {

                continue;
            }


            String domain =
                    getDomain(resultUrl);


            if (domain == null) {

                continue;
            }


            String cleanDomain =
                    domain
                            .replace(".", "")
                            .replace("-", "")
                            .toLowerCase();


            String cleanTitle =
                    title
                            .toLowerCase()
                            .replaceAll("\\s+", "");


            // =================================================
            // CALCULATE SIMPLE SCORE
            // =================================================

            int score = 0;


            // Exact domain match
            if (cleanDomain.equals(searchKeyword)) {

                score += 100;
            }


            // Domain contains keyword
            else if (cleanDomain.contains(searchKeyword)) {

                score += 50;
            }


            // Exact title match
            if (cleanTitle.equals(searchKeyword)) {

                score += 40;
            }


            // Title contains keyword
            else if (cleanTitle.contains(searchKeyword)) {

                score += 20;
            }


            // URL contains keyword
            if (resultUrl
                    .toLowerCase()
                    .contains(
                            keyword.toLowerCase()
                    )) {

                score += 10;
            }


            /*
             * Agar keyword ka relation hi nahi hai
             * to result ignore karo.
             */

            if (score == 0) {

                continue;
            }


            // =================================================
            // BEST RESULT SAVE KARO
            // =================================================

            if (bestResult == null ||
                score > bestScore) {

                bestScore = score;

                bestResult =
                        new WebsiteSearchResult(
                                title,
                                resultUrl,
                                description
                        );
            }
        }


        // =====================================================
        // ONLY ONE RESULT RETURN
        // =====================================================

        if (bestResult != null) {

            results.add(bestResult);
        }


        return results;
    }


    // =========================================================
    // CHECK ACTUAL WEBSITE
    // =========================================================

    private boolean isActualWebsite(
            String url) {

        try {

            URI uri =
                    URI.create(url);


            String host =
                    uri.getHost();


            if (host == null) {

                return false;
            }


            String lowerHost =
                    host.toLowerCase();


            String lowerUrl =
                    url.toLowerCase();


            // =================================================
            // UNWANTED DOMAINS
            // =================================================

            String[] unwantedDomains = {

                    "wikipedia.org",

                    "youtube.com",

                    "facebook.com",

                    "instagram.com",

                    "linkedin.com",

                    "reddit.com",

                    "quora.com",

                    "twitter.com",

                    "x.com",

                    "pinterest.com",

                    "apps.apple.com",

                    "play.google.com",

                    "apps.microsoft.com",

                    "microsoft.com/store"

            };


            for (String unwanted :
                    unwantedDomains) {

                if (lowerHost.equals(unwanted) ||
                    lowerHost.endsWith("." + unwanted) ||
                    lowerUrl.contains(unwanted)) {

                    return false;
                }
            }


            // =================================================
            // UNWANTED PAGE TYPES
            // =================================================

            String path =
                    uri.getPath();


            if (path != null &&
                !path.equals("/") &&
                !path.isEmpty()) {

                String lowerPath =
                        path.toLowerCase();


                String[] unwantedPaths = {

                        "/wiki/",
                        "/article/",
                        "/articles/",
                        "/news/",
                        "/blog/",
                        "/blogs/",
                        "/search",
                        "/tag/",
                        "/category/",
                        "/topics/",
                        "/stories/",
                        "/post/"

                };


                for (String unwantedPath :
                        unwantedPaths) {

                    if (lowerPath.contains(
                            unwantedPath)) {

                        return false;
                    }
                }
            }


            /*
             * App Store / Play Store ko extra check
             */

            if (lowerHost.contains(
                    "apps.apple.com")) {

                return false;
            }


            if (lowerHost.contains(
                    "play.google.com")) {

                return false;
            }


            return true;

        } catch (Exception e) {

            return false;
        }
    }


    // =========================================================
    // GET DOMAIN
    // =========================================================

    private String getDomain(
            String url) {

        try {

            URI uri =
                    URI.create(url);


            String host =
                    uri.getHost();


            if (host == null) {

                return null;
            }


            host =
                    host.toLowerCase();


            if (host.startsWith("www.")) {

                host =
                        host.substring(4);
            }


            return host;

        } catch (Exception e) {

            return null;
        }
    }
}