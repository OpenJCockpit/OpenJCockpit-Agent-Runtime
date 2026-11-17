package nl.metafactory.agentruntime.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnsupportedEmbeddingModelTest {

    private final UnsupportedEmbeddingModel model = new UnsupportedEmbeddingModel();

    @Test
    void embeddingRequestsFailWithAnActionableMessage() {
        assertThatThrownBy(() -> model.call(new EmbeddingRequest(List.of("text"), null)))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage(UnsupportedEmbeddingModel.MESSAGE);
        assertThatThrownBy(() -> model.embed(new Document("text")))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage(UnsupportedEmbeddingModel.MESSAGE);
        assertThatThrownBy(() -> model.embed("text"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage(UnsupportedEmbeddingModel.MESSAGE);
    }

    @Test
    void theMessageCanNameWhyThereAreNoEmbeddings() {
        assertThatThrownBy(() -> new UnsupportedEmbeddingModel("no embeddings here").embed("text"))
                .hasMessage("no embeddings here");
    }
}
