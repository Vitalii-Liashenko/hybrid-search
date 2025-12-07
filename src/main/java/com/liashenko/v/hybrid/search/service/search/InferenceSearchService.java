package com.liashenko.v.hybrid.search.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.google.common.base.Stopwatch;
import com.liashenko.v.hybrid.search.model.Conference;
import com.liashenko.v.hybrid.search.service.EmbeddingService;
import com.liashenko.v.hybrid.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

import static com.liashenko.v.hybrid.search.model.Conference.EMBEDDING_FIELD;

@Slf4j
public class InferenceSearchService extends DefaultSearchService implements SearchService {

    public InferenceSearchService(ElasticsearchClient client, String indexName, EmbeddingService embeddingService, ResourceLoader resourceLoader, String indexConfigPath, int indexBatchSize, Executor taskExecutor) {
        super(client, indexName, embeddingService, resourceLoader, indexConfigPath, indexBatchSize, taskExecutor);
    }

    protected SearchRequest buildVectorSearchRequest(String queryText, int offset, int limit) throws IOException, InterruptedException {
//        List<Float> vectorizedQuery = embeddingService.embed(queryText); //no need to call embedding service when use inference in ES

        return SearchRequest.of(s -> s
                .index(indexName)
                .from(offset)
                .size(limit)
                .explain(true)
                .source(sourceBuilder -> sourceBuilder
                        .filter(f -> f.includes(ALL_FIELDS)))
                .knn(k -> k
                                .field(EMBEDDING_FIELD)
//                        .queryVector(vectorizedQuery) //no need to call embedding service when use inference in ES
                                .queryVectorBuilder(builder -> builder
                                        .textEmbedding(textEmbeddingBuilder -> textEmbeddingBuilder
                                                .modelId("embeddinggemma") //name of created inference_id in ES
                                                .modelText(queryText)))
                                .k(10)
                                .numCandidates(100)
                ));
    }

    protected SearchRequest buildHybridSearchRequest(String queryText, int offset, int limit) throws IOException, InterruptedException {
//        List<Float> vectorizedQuery = embeddingService.embed(queryText); //no need to call embedding service when use inference in ES

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
                .explain(true)
                .source(sourceBuilder -> sourceBuilder
                        .filter(f -> f.includes(ALL_FIELDS)))
                .query(Query.of(queryBuilder -> queryBuilder
                        .bool(boolQueryBuilder ->
                                boolQueryBuilder
                                        .must(multiFieldTextSearchQuery))))
                .knn(k -> k
                                .field(EMBEDDING_FIELD)
//                              .queryVector(vectorizedQuery)
                                .queryVectorBuilder(builder -> builder
                                        .textEmbedding(textEmbeddingBuilder -> textEmbeddingBuilder
                                                .modelId("hugging_face_embeddings")
                                                .modelText(queryText)))
                                .k(5)
                                .numCandidates(100)
                ));
    }
}
