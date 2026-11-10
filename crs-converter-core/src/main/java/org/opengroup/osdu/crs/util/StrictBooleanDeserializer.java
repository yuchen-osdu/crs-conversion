package org.opengroup.osdu.crs.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/** Preserves the JSON type required by the request contract. */
public class StrictBooleanDeserializer extends JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.hasToken(JsonToken.VALUE_TRUE) || parser.hasToken(JsonToken.VALUE_FALSE)) {
            return parser.getBooleanValue();
        }
        return (Boolean) context.handleUnexpectedToken(Boolean.class, parser);
    }
}
