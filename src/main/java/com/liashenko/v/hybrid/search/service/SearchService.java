package com.liashenko.v.hybrid.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.liashenko.v.hybrid.search.service.model.Conference;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.liashenko.v.hybrid.search.service.model.Conference.ATTENDEES_CONCAT_STRING_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.ATTENDEES_COUNT_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.COMPANY_ATTENDEES_COUNT_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.COUNTRY_DESCRIPTION_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.DESCRIPTION_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.EMBEDDING_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.END_DATE_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.FORMATTED_LOCATION_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.GROUP_ID_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.ID_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.INDUSTRY_CODES_CONCAT_STRING_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.INDUSTRY_GROUPS_CONCAT_STRING_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.INDUSTRY_SECTORS_CONCAT_STRING_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.INVESTOR_ATTENDEES_COUNT_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.NAME_FIELD;
import static com.liashenko.v.hybrid.search.service.model.Conference.START_DATE_FIELD;
import static org.apache.commons.lang3.StringUtils.isBlank;

@AllArgsConstructor
@Slf4j
public class SearchService {

    private final ElasticsearchClient client;
    private final String indexName;
    private final EmbeddingService embeddingService;
    private final ResourceLoader resourceLoader;
    private final String indexConfigPath;
    private int indexBatchSize;
    private final Executor taskExecutor;

    private static final List<String> ALL_FIELDS = List.of(
            ID_FIELD,
            NAME_FIELD,
            GROUP_ID_FIELD,
            DESCRIPTION_FIELD,
            START_DATE_FIELD,
            END_DATE_FIELD,
            FORMATTED_LOCATION_FIELD,
            COUNTRY_DESCRIPTION_FIELD,
            ATTENDEES_COUNT_FIELD,
            COMPANY_ATTENDEES_COUNT_FIELD,
            INVESTOR_ATTENDEES_COUNT_FIELD,
            ATTENDEES_CONCAT_STRING_FIELD,
            INDUSTRY_SECTORS_CONCAT_STRING_FIELD,
            INDUSTRY_GROUPS_CONCAT_STRING_FIELD,
            INDUSTRY_CODES_CONCAT_STRING_FIELD
    );

    public void index(List<Conference> docs) {
        if (docs.isEmpty()) {
            return;
        }
        Stopwatch stopwatch = Stopwatch.createStarted();

        List<CompletableFuture<Void>> futures = Lists.partition(docs, indexBatchSize)
                .stream()
                .map(batchOfConferences -> CompletableFuture.runAsync(() -> indexTask(batchOfConferences), taskExecutor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("Indexed {} documents in {}", docs.size(), stopwatch);
    }

    private void indexTask(List<Conference> batchOfConferences) {
        List<Conference> conferences = embeddingService.embed(batchOfConferences);
        Stopwatch localStopwatch = Stopwatch.createStarted();
        perform(toBulkOperations(conferences));
        log.info("Indexed {} conferences in {}", conferences.size(), localStopwatch);
    }

    public List<Conference> search(String queryText, String type, int offset, int limit) throws IOException, InterruptedException {
        if (isBlank(queryText)) {
            return List.of();
        }

        Stopwatch buildQueryStopwatch = Stopwatch.createStarted();
        SearchRequest request = switch (type) {
            case "VECTOR" -> buildVectorSearchRequest(queryText, offset, limit);
            case "KEYWORD" -> buildKeywordSearchRequest(queryText, offset, limit);
            case "HYBRID" -> buildHybridSearchRequest(queryText, offset, limit);
            default -> throw new IllegalArgumentException("Unknown search type: " + type);
        };
        log.info("Built '{}' search request in {}", type, buildQueryStopwatch);

        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Conference> conferences = client.search(request, Conference.class)
                .hits()
                .hits()
                .stream()
                .map(hit -> {
                    Conference conference = hit.source();
                    Optional.ofNullable(hit.score())
                            .ifPresent(conference::setScore);
                    return conference;
                })
                .toList();
        log.info("Search '{}' with type '{}' returned {} results in {}", queryText, type, conferences.size(), stopwatch);
        return conferences;
    }

    private SearchRequest buildVectorSearchRequest(String queryText, int offset, int limit) throws IOException, InterruptedException {
        List<Float> vectorizedQuery = embeddingService.embed(queryText);

        return SearchRequest.of(s -> s
                .index(indexName)
                .from(offset)
                .size(limit)
                .source(sourceBuilder -> sourceBuilder
                        .filter(f -> f.includes(ALL_FIELDS)))
                .knn(k -> k
                        .field(EMBEDDING_FIELD)
                        .queryVector(vectorizedQuery)
                        .k(5)
                        .numCandidates(100)
                ));
    }

    private SearchRequest buildKeywordSearchRequest(String queryText, int offset, int limit) {
        Query multiFieldTextSearchQuery = Query.of(q -> q
                .multiMatch(multiMatchQuery -> multiMatchQuery
                        .fields(getTextFieldsForSearch())
                        .query(queryText)
                        .type(TextQueryType.BestFields)
                )
        );

        return SearchRequest.of(s -> s
                .index(indexName)
                .from(offset)
                .size(limit)
                .source(sourceBuilder -> sourceBuilder
                        .filter(f -> f.includes(ALL_FIELDS)))
                .query(Query.of(queryBuilder -> queryBuilder
                        .bool(boolQueryBuilder ->
                                boolQueryBuilder
                                        .must(multiFieldTextSearchQuery)))));
    }

    private static List<String> getTextFieldsForSearch() {
        return List.of(
                NAME_FIELD + ".text^3",
                DESCRIPTION_FIELD + ".text^2",
                FORMATTED_LOCATION_FIELD + ".text^3",
                COUNTRY_DESCRIPTION_FIELD + ".text^1",
                ATTENDEES_CONCAT_STRING_FIELD + ".text^1",
                INDUSTRY_SECTORS_CONCAT_STRING_FIELD + ".text^2",
                INDUSTRY_GROUPS_CONCAT_STRING_FIELD + ".text^2",
                INDUSTRY_CODES_CONCAT_STRING_FIELD + ".text^2"
        );
    }

    private SearchRequest buildHybridSearchRequest(String queryText, int offset, int limit) throws IOException, InterruptedException {
        List<Float> vectorizedQuery = embeddingService.embed(queryText);

        Query multiFieldTextSearchQuery = Query.of(q -> q
                .multiMatch(multiMatchQuery -> multiMatchQuery
                        .fields(getTextFieldsForSearch())
                        .query(queryText)
                        .type(TextQueryType.BestFields)
                )
        );

        return SearchRequest.of(s -> s
                .index(indexName)
                .from(offset)
                .size(limit)
                .source(sourceBuilder -> sourceBuilder
                        .filter(f -> f.includes(ALL_FIELDS)))
                .query(Query.of(queryBuilder -> queryBuilder
                        .bool(boolQueryBuilder ->
                                boolQueryBuilder
                                        .must(multiFieldTextSearchQuery))))
                .knn(k -> k
                        .field(EMBEDDING_FIELD)
                        .queryVector(vectorizedQuery)
                        .k(5)
                        .numCandidates(100)
                ));
    }

    private void perform(List<BulkOperation> operations) {
        try {
            BulkRequest request = new BulkRequest.Builder().index(indexName)
                    .operations(operations)
                    .build();

            BulkResponse bulkResponse = client.bulk(request);
            if (bulkResponse.errors()) {
                throw new RuntimeException(String.format("Couldn't update beans in '%s'", indexName));
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Couldn't update beans in '%s'", indexName), e);
        }
    }

    private List<BulkOperation> toBulkOperations(List<Conference> beans) {
        return beans.stream()
                .map(this::toBulkOperation)
                .toList();
    }

    private BulkOperation toBulkOperation(Conference bean) {
        return new BulkOperation.Builder().index(operation -> new IndexOperation.Builder<>()
                        .document(bean)
                        .id(bean.getId()))
                .build();
    }

    public void deleteData() {
        if (!indexExists()) {
            return;
        }

        try {
            DeleteIndexResponse response = client.indices().delete(DeleteIndexRequest.of(b -> b.index(indexName)));
            if (!response.acknowledged()) {
                throw new RuntimeException("Index deletion not acknowledged");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete index: '%s'".formatted(indexName), e);
        }
    }

    public void createIndexIfNeeded() {
        if (indexExists()) {
            return;
        }

        CreateIndexResponse response;
        try {
            InputStream indexJsonConfigInputStream = getIndexConfig();
            CreateIndexRequest request = CreateIndexRequest.of(requestBuilder -> requestBuilder
                    .index(indexName)
                    .withJson(indexJsonConfigInputStream)
            );

            response = client.indices()
                    .create(request);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create index: '%s'".formatted(indexName), ex);
        }

        if (!response.acknowledged()) {
            throw new RuntimeException("Failed to create index: '%s'".formatted(indexName));
        }

        log.info("Index '{}' has been created", indexName);
    }

    private boolean indexExists() {
        try {
            BooleanResponse exists = client.indices().exists(ExistsRequest.of(b -> b.index(indexName)));
            return exists.value();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to check if index exists: '%s'".formatted(indexName), ex);
        }
    }

    private InputStream getIndexConfig() throws IOException {
        String indexJsonConfig = new String(resourceLoader.getResource(indexConfigPath)
                .getInputStream()
                .readAllBytes(), StandardCharsets.UTF_8);

        return new ByteArrayInputStream(indexJsonConfig.getBytes(StandardCharsets.UTF_8));
    }
}
