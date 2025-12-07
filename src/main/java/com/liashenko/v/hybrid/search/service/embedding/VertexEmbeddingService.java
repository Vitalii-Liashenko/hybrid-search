package com.liashenko.v.hybrid.search.service.embedding;

import com.google.cloud.aiplatform.v1.PredictRequest;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.liashenko.v.hybrid.search.model.Conference;
import com.liashenko.v.hybrid.search.service.EmbeddingService;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.liashenko.v.hybrid.search.service.ConferenceStringifier.getInput;

@AllArgsConstructor
public class VertexEmbeddingService implements EmbeddingService {

    private final PredictionServiceClient client;
    private final String endpoint;

    @Override
    public List<Conference> embed(List<Conference> conferences) {
        List<Conference> conferencesWithEmbeddings = new ArrayList<>();
        for (Conference conf : conferences) {
            String input = getInput(conf);
            List<Float> embedding = embed(input);
            Conference conference = conf.withEmbedding(embedding);
            conferencesWithEmbeddings.add(conference);
        }
        return conferencesWithEmbeddings;
    }

    @Override
    public List<Float> embed(String text) {
        // parameters
        Struct.Builder params = Struct.newBuilder();
        params.putFields("outputDimensionality", num(768));
        params.putFields("autoTruncate", bool(true));

        List<Value> instances = new ArrayList<>();
        instances.add(Value.newBuilder()
                .setStructValue(Struct.newBuilder()
                        .putFields("content", str(text))
                        .putFields("task_type", str("RETRIEVAL_DOCUMENT"))
                        .build())
                .build());
        PredictRequest req = PredictRequest.newBuilder()
                .setEndpoint(endpoint)
                .addAllInstances(instances)
                .setParameters(Value.newBuilder().setStructValue(params.build()).build())
                .build();

        PredictResponse resp = client.predict(req);

        List<List<Float>> out = new ArrayList<>();
        for (Value p : resp.getPredictionsList()) {
            // prediction.embeddings.values -> List<double>
            Value emb = p.getStructValue().getFieldsOrThrow("embeddings");
            List<Float> vec = emb.getStructValue()
                    .getFieldsOrThrow("values")
                    .getListValue().getValuesList()
                    .stream().map(Value::getNumberValue).map(Double::floatValue)
                    .toList();
            out.add(vec);
        }
        return out.getFirst();
    }

    // ---- helpers ----
    private Value str(String s) {
        return Value.newBuilder().setStringValue(s).build();
    }

    private Value num(int n) {
        return Value.newBuilder().setNumberValue(n).build();
    }

    private Value bool(boolean v) {
        return Value.newBuilder().setBoolValue(v).build();
    }
}
