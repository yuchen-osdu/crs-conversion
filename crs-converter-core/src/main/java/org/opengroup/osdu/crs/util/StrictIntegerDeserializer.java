package org.opengroup.osdu.crs.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/** Preserves the JSON integer type required by the request contract. */
public class StrictIntegerDeserializer extends JsonDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT) || parser.hasToken(JsonToken.VALUE_NUMBER_FLOAT)) {
            try {
                return parser.getDecimalValue().intValueExact();
            } catch (ArithmeticException ex) {
                return (Integer) context.handleUnexpectedToken(Integer.class, parser);
            }
        }
        return (Integer) context.handleUnexpectedToken(Integer.class, parser);
    }
}
