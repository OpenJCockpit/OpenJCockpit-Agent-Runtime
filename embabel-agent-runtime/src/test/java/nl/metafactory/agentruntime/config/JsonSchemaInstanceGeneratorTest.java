package nl.metafactory.agentruntime.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonSchemaInstanceGeneratorTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private final JsonSchemaInstanceGenerator generator = new JsonSchemaInstanceGenerator();

    private JsonNode instanceOf(String schema) {
        return generator.instanceOf(json.readTree(schema));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "{\"type\":\"string\"}                       | \"mock-value\"",
            "{\"type\":\"integer\"}                      | 0",
            "{\"type\":\"number\"}                       | 0",
            "{\"type\":\"boolean\"}                      | false",
            "{\"type\":[\"null\",\"string\"]}            | \"mock-value\"",
            "{\"enum\":[\"LOW\",\"HIGH\"]}               | \"LOW\"",
            "{\"const\":42}                              | 42",
            "{\"type\":\"array\"}                        | []",
            "{\"type\":\"array\",\"items\":{\"type\":\"boolean\"}} | [false]"
    })
    void scalarAndCollectionSchemasGetAFixedValue(String schema, String expected) {
        assertThat(instanceOf(schema)).isEqualTo(json.readTree(expected));
    }

    @Test
    void objectsAreFilledPropertyByPropertyIncludingLocalReferences() {
        JsonNode instance = instanceOf("""
                {"type":"object","properties":{
                  "title":{"type":"string"},
                  "tags":{"type":"array","items":{"type":"string"}},
                  "owner":{"$ref":"#/$defs/Person"}},
                 "$defs":{"Person":{"type":"object","properties":{"name":{"type":"string"},"active":{"type":"boolean"}}}}}""");

        assertThat(instance).isEqualTo(json.readTree("""
                {"title":"mock-title","tags":["mock-tags"],"owner":{"name":"mock-name","active":false}}"""));
    }

    @Test
    void theSameSchemaAlwaysYieldsTheSameInstance() {
        String schema = "{\"type\":\"object\",\"properties\":{\"a\":{\"type\":\"string\"},\"b\":{\"type\":\"integer\"}}}";
        assertThat(instanceOf(schema)).isEqualTo(instanceOf(schema));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"type\":\"null\"}", "{\"type\":[\"null\"]}", "{\"oneOf\":[{\"type\":\"string\"}]}"})
    void schemasWithoutAGeneratableTypeFailExplicitly(String schema) {
        assertThatThrownBy(() -> instanceOf(schema))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("cannot generate a value for JSON schema")
                .hasMessageContaining("activate a real provider profile");
    }

    @Test
    void remoteReferencesAreRejected() {
        assertThatThrownBy(() -> instanceOf("{\"$ref\":\"https://example.org/schema.json\"}"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("only local JSON schema references");
    }

    @Test
    void danglingReferencesAreRejected() {
        assertThatThrownBy(() -> instanceOf("{\"$ref\":\"#/$defs/Missing\"}"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("cannot resolve JSON schema reference #/$defs/Missing");
    }
}
