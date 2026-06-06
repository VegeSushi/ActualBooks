package online.sushiware.actualBooks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class WikipediaFetcher {
    private final HttpClient client = HttpClient.newHttpClient();
    private static final String USER_AGENT = "ActualBooksPlugin/1.0 (Contact: your-email@example.com)";
    private final Logger logger;

    public WikipediaFetcher(Logger logger) { this.logger = logger; }

    public CompletableFuture<WikipediaPage> fetchRandomSummary() {
        String url = "https://en.wikipedia.org/w/api.php?action=query&format=json&list=random&rnnamespace=0&rnlimit=1";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", USER_AGENT).GET().build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> JsonParser.parseString(body).getAsJsonObject().getAsJsonObject("query")
                        .getAsJsonArray("random").get(0).getAsJsonObject().get("title").getAsString())
                .thenCompose(this::fetchSummary);
    }

    private CompletableFuture<WikipediaPage> fetchSummary(String title) {
        String encoded = URLEncoder.encode(title, StandardCharsets.UTF_8);
        // Removed 'exintro' to get fuller page content
        String url = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&explaintext&redirects=1&titles=" + encoded;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", USER_AGENT).GET().build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    JsonObject pages = JsonParser.parseString(body).getAsJsonObject().getAsJsonObject("query").getAsJsonObject("pages");
                    String pageId = pages.keySet().iterator().next();
                    String extract = pages.getAsJsonObject(pageId).has("extract") ? pages.getAsJsonObject(pageId).get("extract").getAsString() : "No content.";
                    String clean = extract.replaceAll("\\n+", "\n").replaceAll("(?m)^[ \t]*\r?\n", "").trim();
                    logger.info("Wikipedia fetch successful: " + title);
                    return new WikipediaPage(title, clean);
                });
    }
}