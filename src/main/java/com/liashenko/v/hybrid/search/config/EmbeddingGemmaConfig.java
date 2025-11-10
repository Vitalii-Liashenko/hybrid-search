package com.liashenko.v.hybrid.search.config;

import com.liashenko.v.hybrid.search.service.EmbeddingGemmaService;
import com.liashenko.v.hybrid.search.service.EmbeddingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties
public class EmbeddingGemmaConfig {

    @Bean
    public RestClient embeddingRestClient(@Value("${embedding-gemma.endpoint}") String endpoint) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(endpoint)
                .defaultHeader("Content-Type", "application/json");
        return builder.build();
    }

    @Bean
    EmbeddingService embeddingService(RestClient embeddingRestClient) {
        return new EmbeddingGemmaService(embeddingRestClient);
    }
}
