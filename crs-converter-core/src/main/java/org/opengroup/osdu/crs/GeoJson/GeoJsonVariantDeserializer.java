package org.opengroup.osdu.crs.GeoJson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.opengroup.osdu.crs.GeoJson.GeoJsonBase.GeoJsonVariant;

import java.io.IOException;

/** Accepts only the string enum values published for geoJsonVariantInternal. */
public class GeoJsonVariantDeserializer extends JsonDeserializer<GeoJsonVariant> {
    @Override
    public GeoJsonVariant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (!parser.hasToken(JsonToken.VALUE_STRING)) {
            return (GeoJsonVariant) context.handleUnexpectedToken(GeoJsonVariant.class, parser);
        }
        String text = parser.getText();
        try {
            return GeoJsonVariant.valueOf(text);
        } catch (IllegalArgumentException ex) {
            return (GeoJsonVariant) context.handleWeirdStringValue(GeoJsonVariant.class, text,
                    "not one of the values accepted for geoJsonVariantInternal");
        }
    }

    @Override
    public GeoJsonVariant getNullValue(DeserializationContext context) throws JsonMappingException {
        return context.reportInputMismatch(GeoJsonVariant.class, "geoJsonVariantInternal cannot be null");
    }
}
