package com.liashenko.v.hybrid.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.liashenko.v.hybrid.search.config.property.DataLoadingProperties;
import com.liashenko.v.hybrid.search.service.EmbeddingService;
import com.liashenko.v.hybrid.search.service.SearchService;
import com.liashenko.v.hybrid.search.service.embedding.VertexEmbeddingService;
import com.liashenko.v.hybrid.search.service.search.DefaultSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.concurrent.Executor;

@Profile("vertex")
@Configuration
public class VertexEmbeddingConfig {

    @Bean
    EmbeddingService embeddingService(@Value("${vertex.region}") String region,
                                      @Value("${vertex.model}") String modelName,
                                      @Value("${vertex.project-id}") String projectId) throws IOException {
        String endpointPath = String.format(
                "projects/%s/locations/%s/publishers/google/models/%s",
                projectId, region, modelName
        );

        PredictionServiceSettings settings = PredictionServiceSettings.newBuilder()
                .setEndpoint(region + "-aiplatform.googleapis.com:443")
                .build();
        PredictionServiceClient client = PredictionServiceClient.create(settings);
        return new VertexEmbeddingService(client, endpointPath);
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
