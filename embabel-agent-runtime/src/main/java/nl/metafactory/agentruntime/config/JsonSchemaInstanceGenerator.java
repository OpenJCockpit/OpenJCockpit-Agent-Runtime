package nl.metafactory.agentruntime.config;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

final class JsonSchemaInstanceGenerator {

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;
    private static final String ROOT_NAME = "value";

    JsonNode instanceOf(JsonNode schema) {
        return valueFor(schema, schema, ROOT_NAME);
    }

    private JsonNode valueFor(JsonNode root, JsonNode schema, String name) {
        if (schema.has("$ref")) {
            return valueFor(root, resolve(root, schema.get("$ref").asString()), name);
        }
        if (schema.has("const")) {
            return schema.get("const");
        }
        if (schema.has("enum")) {
            return schema.get("enum").get(0);
        }
        return switch (typeOf(schema)) {
            case "object" -> objectFor(root, schema);
            case "array" -> arrayFor(root, schema, name);
            case "string" -> NODES.stringNode("mock-" + name);
            case "integer", "number" -> NODES.numberNode(0);
            case "boolean" -> NODES.booleanNode(false);
            default -> throw unsupported(schema);
        };
    }

    private ObjectNode objectFor(JsonNode root, JsonNode schema) {
        ObjectNode object = NODES.objectNode();
        schema.path("properties").properties()
                .forEach(property -> object.set(property.getKey(), valueFor(root, property.getValue(), property.getKey())));
        return object;
    }

    private ArrayNode arrayFor(JsonNode root, JsonNode schema, String name) {
        ArrayNode array = NODES.arrayNode();
        if (schema.has("items")) {
            array.add(valueFor(root, schema.get("items"), name));
        }
        return array;
    }

    private static String typeOf(JsonNode schema) {
        JsonNode type = schema.path("type");
        if (type.isArray()) {
            for (JsonNode candidate : type) {
                if (!"null".equals(candidate.asString())) {
                    return candidate.asString();
                }
            }
        }
        return type.asString("");
    }

    private static JsonNode resolve(JsonNode root, String reference) {
        if (!reference.startsWith("#/")) {
            throw new UnsupportedOperationException("Mock LLM supports only local JSON schema references, not " + reference);
        }
        JsonNode target = root.at(reference.substring(1));
        if (target.isMissingNode()) {
            throw new UnsupportedOperationException("Mock LLM cannot resolve JSON schema reference " + reference);
        }
        return target;
    }

    private static UnsupportedOperationException unsupported(JsonNode schema) {
        return new UnsupportedOperationException("Mock LLM cannot generate a value for JSON schema " + schema
                + "; activate a real provider profile for this request");
    }
}
