package nl.metafactory.agentruntime.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

final class UnsupportedEmbeddingModel implements EmbeddingModel {

    static final String MESSAGE = "The mock profile provides no embeddings; activate a real provider profile";

    private final String message;

    UnsupportedEmbeddingModel() {
        this(MESSAGE);
    }

    UnsupportedEmbeddingModel(String message) {
        this.message = message;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        throw new UnsupportedOperationException(message);
    }

    @Override
    public float[] embed(Document document) {
        throw new UnsupportedOperationException(message);
    }
}
