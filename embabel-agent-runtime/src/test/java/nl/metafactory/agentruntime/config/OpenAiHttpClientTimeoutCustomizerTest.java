package nl.metafactory.agentruntime.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.openai.http.okhttp.OpenAiHttpClientBuilderCustomizer;
import org.springframework.ai.openai.http.okhttp.SpringAiOpenAiHttpClient;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiHttpClientTimeoutCustomizerTest {

    @Test
    void appliesConfiguredConnectAndReadTimeoutsToTheRealHttpClient() {
        OpenAiModelConfig config = new OpenAiModelConfig();
        OpenAiHttpClientBuilderCustomizer customizer = config.openAiHttpClientBuilderCustomizer(45, 600);

        SpringAiOpenAiHttpClient.Builder builder = SpringAiOpenAiHttpClient.builder();
        customizer.customize(builder);
        SpringAiOpenAiHttpClient httpClient = builder.build();

        assertThat(httpClient.getOkHttpClient().connectTimeoutMillis())
                .as("connect timeout must be 45 seconds")
                .isEqualTo(45_000);
        assertThat(httpClient.getOkHttpClient().readTimeoutMillis())
                .as("read timeout must be 600 seconds")
                .isEqualTo(600_000);
        assertThat(httpClient.getOkHttpClient().callTimeoutMillis())
                .as("overall request/call timeout must be 600 seconds")
                .isEqualTo(600_000);
    }

    @Test
    void appliesDifferentConfiguredValuesCorrectly() {
        OpenAiModelConfig config = new OpenAiModelConfig();
        OpenAiHttpClientBuilderCustomizer customizer = config.openAiHttpClientBuilderCustomizer(10, 120);

        SpringAiOpenAiHttpClient.Builder builder = SpringAiOpenAiHttpClient.builder();
        customizer.customize(builder);
        SpringAiOpenAiHttpClient httpClient = builder.build();

        assertThat(httpClient.getOkHttpClient().connectTimeoutMillis()).isEqualTo(10_000);
        assertThat(httpClient.getOkHttpClient().readTimeoutMillis()).isEqualTo(120_000);
        assertThat(httpClient.getOkHttpClient().callTimeoutMillis()).isEqualTo(120_000);
    }
}
