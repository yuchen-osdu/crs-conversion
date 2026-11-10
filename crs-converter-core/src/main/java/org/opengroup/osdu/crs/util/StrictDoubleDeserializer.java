package org.opengroup.osdu.crs.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/** Preserves the JSON number type required by the request contract. */
public class StrictDoubleDeserializer extends JsonDeserializer<Double> {
    @Override
    public Double deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT) || parser.hasToken(JsonToken.VALUE_NUMBER_FLOAT)) {
            return parser.getDoubleValue();
        }
        return (Double) context.handleUnexpectedToken(Double.class, parser);
    }
}
