package com.liashenko.v.hybrid.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.liashenko.v.hybrid.search.config.property.DataLoadingProperties;
import com.liashenko.v.hybrid.search.service.SearchService;
import com.liashenko.v.hybrid.search.service.embedding.GemmaEmbeddingService;
import com.liashenko.v.hybrid.search.service.EmbeddingService;
import com.liashenko.v.hybrid.search.service.search.DefaultSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.client.RestClient;

import java.util.concurrent.Executor;

@Profile("gemma")
@Configuration
public class GemmaConfig {

    @Bean
    public RestClient embeddingRestClient(@Value("${embedding-gemma.endpoint}") String endpoint) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(endpoint)
                .defaultHeader("Content-Type", "application/json");
        return builder.build();
    }

    @Bean
    EmbeddingService embeddingService(RestClient embeddingRestClient) {
        return new GemmaEmbeddingService(embeddingRestClient);
    }

    @Bean
    SearchService searchService(ElasticsearchClient elasticsearchClient,
                                @Value("${elasticsearch.index}") String index,
                                EmbeddingService embeddingService,
                                ResourceLoader resourceLoader,
                                DataLoadingProperties dataLoadingProperties,
                                Executor indexingTaskExecutor) {

        return new DefaultSearchService(
                elasticsearchClient,
                index,
                embeddingService,
                resourceLoader,
                dataLoadingProperties.getIndexConfigFilePath(),
                dataLoadingProperties.getBatchSize(),
                indexingTaskExecutor);
    }
}
