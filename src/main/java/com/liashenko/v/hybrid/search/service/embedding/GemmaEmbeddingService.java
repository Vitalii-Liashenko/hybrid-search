package com.liashenko.v.hybrid.search.service.embedding;

import com.google.common.base.Stopwatch;
import com.liashenko.v.hybrid.search.model.Conference;
import com.liashenko.v.hybrid.search.service.EmbeddingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.liashenko.v.hybrid.search.service.ConferenceStringifier.getInput;
import static com.liashenko.v.hybrid.search.service.ConferenceStringifier.getShortInput;

/**
 * Service for generating text embeddings using embeddinggemma-300m via Spring's RestClient.
 * This is an alternative implementation using Spring's modern HTTP client instead of the direct HttpClient approach.
 */
@Slf4j
@AllArgsConstructor
public class GemmaEmbeddingService implements EmbeddingService {
    static final int MAX_TOKENS = 400;
    static final int CHARS_PER_TOKEN = 4;
    static final int MAX_CHARS = MAX_TOKENS * CHARS_PER_TOKEN - 1;

    private final RestClient embeddingRestClient;

    /**
     * Generates embeddings for a list of conferences using Spring RestClient.
     *
     * @param conferences list of conferences to embed
     * @return list of conferences with embeddings populated
     */
    public List<Conference> embed(List<Conference> conferences) {
        List<Conference> conferencesWithEmbeddings = new ArrayList<>();
        for (Conference conf : conferences) {
            String input = getInput(conf);
            if (input.length() >= MAX_CHARS) {
                input = getShortInput(conf);
                if (input.length() >= MAX_CHARS) {
                    input = input.substring(0, MAX_CHARS);
                }
            }
            List<Float> embedding = embed(input);
            Conference conference = conf.withEmbedding(embedding);
            conferencesWithEmbeddings.add(conference);
        }
        return conferencesWithEmbeddings;
    }

    public List<Float> embed(String text) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<List<Float>> response = embeddingRestClient.post()
                .uri("/embed")
                .body(Map.of("inputs", text))
                .retrieve()
                .body(new ParameterizedTypeReference<List<List<Float>>>() {
                });

        log.info("Embedded query text in {} using Spring RestClient", stopwatch);
        return response.getFirst();
    }

}
