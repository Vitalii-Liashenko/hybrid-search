package com.liashenko.v.hybrid.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.liashenko.v.hybrid.search.config.property.DataLoadingProperties;
import com.liashenko.v.hybrid.search.controller.DefaultSearchMapper;
import com.liashenko.v.hybrid.search.controller.SearchMapper;
import com.liashenko.v.hybrid.search.service.ConferenceCsvParser;
import com.liashenko.v.hybrid.search.service.CsvDataLoaderService;
import com.liashenko.v.hybrid.search.service.EmbeddingService;
import com.liashenko.v.hybrid.search.service.SearchService;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.text.SimpleDateFormat;
import java.util.concurrent.Executor;

@Configuration
public class AppConfig {

    @Bean(name = "indexingTaskExecutor")
    public Executor indexingTaskExecutor(DataLoadingProperties dataLoadingProperties) {
        DataLoadingProperties.ThreadPoolProperties threadPool = dataLoadingProperties.getThreadPool();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPool.getCorePoolSize());
        executor.setMaxPoolSize(threadPool.getMaxPoolSize());
        executor.setQueueCapacity(threadPool.getQueueCapacity());
        executor.setThreadNamePrefix(threadPool.getThreadNamePrefix());
        executor.initialize();
        return executor;
    }

    @Bean
    RestClient elasticRestClient(RestClientBuilder clientBuilder) {
        return clientBuilder.build();
    }

    @Bean
    ElasticsearchClient elasticClient(
            RestClient elasticRestClient
    ) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        JsonpMapper mapper = new JacksonJsonpMapper(objectMapper);
        ElasticsearchTransport transport = new RestClientTransport(elasticRestClient, mapper);
        return new ElasticsearchClient(transport);
    }

    @Bean
    SearchService esRepository(ElasticsearchClient elasticsearchClient,
                               @Value("${elasticsearch.index}") String index,
                               EmbeddingService embeddingService,
                               ResourceLoader resourceLoader,
                               DataLoadingProperties dataLoadingProperties,
                               Executor indexingTaskExecutor) {

        return new SearchService(
                elasticsearchClient,
                index,
                embeddingService,
                resourceLoader,
                dataLoadingProperties.getIndexConfigFilePath(),
                dataLoadingProperties.getBatchSize(),
                indexingTaskExecutor);
    }

    @Bean
    CsvDataLoaderService csvDataLoaderService(
            DataLoadingProperties properties,
            ResourceLoader resourceLoader,
            SearchService searchService
    ) {
        return new CsvDataLoaderService(properties,
                resourceLoader,
                new ConferenceCsvParser(),
                searchService);
    }

    @Bean
    SearchMapper searchMapper() {
        return new DefaultSearchMapper();
    }
}