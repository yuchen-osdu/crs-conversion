package org.opengroup.osdu.crs.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryResponseV4;

import java.io.IOException;

public class TrajectoryConverterV4Tests {

    private static final String INPUT_REQ_AZI_PROJ_CRS_GN = "{\r\n    \"azimuthReference\": \"GN\",\r\n    \"interpolate\": false,\r\n    \"referencePoint\": {\r\n        \"y\": 6500000,\r\n        \"x\": 400000,\r\n        \"z\": 0\r\n    },\r\n    \"unitZ\": \"osdu:reference-data--UnitOfMeasure:m:\",\r\n    \"inputStations\": [\r\n        {\r\n            \"md\": 0,\r\n            \"azimuth\": 90,\r\n            \"inclination\": 90\r\n        },\r\n        {\r\n            \"md\": 2000,\r\n            \"azimuth\": 90,\r\n            \"inclination\": 90\r\n        },\r\n        {\r\n            \"md\": 4000,\r\n            \"azimuth\": 90,\r\n            \"inclination\": 90\r\n        },\r\n        {\r\n            \"md\": 6000,\r\n            \"azimuth\": 90,\r\n            \"inclination\": 90\r\n        },\r\n        {\r\n            \"md\": 8000,\r\n            \"azimuth\": 90,\r\n            \"inclination\": 90\r\n        },\r\n        {\r\n            \"md\": 10000,\r\n            \"azimuth\": 90,\r\n            \"inclination\": 90\r\n        }\r\n    ],\r\n    \"trajectoryCRS\": \"osdu:reference-data--CoordinateReferenceSystem:Projected:EPSG::32631:\",\r\n    \"inputKind\": \"MD_Incl_Azim\",\r\n    \"method\": \"AzimuthalEquidistant\"\r\n}";

    @Test
    public void convertTrajectoryForAzimuthalEquidistantProjectedCRS_GN_WithSuccess() {

    }

    public void convertTrajectoryForLMPProjectedCRS_GN_WithSuccess() {

    }

    public void convertTrajectoryForLMPGeographicCRS_TN_WithSuccess() {

    }

    public void convertTrajectoryForGNLProjectedCRS_GN_WithSuccess() {

    }

    public void convertTrajectoryForAzimuthalEquidistantProjectedCRS_GN_Mdi_WithSuccess() {

    }

    public void convertTrajectoryForAzimuthalEquidistantProjectedCRS_GN_Md_interval_WithSuccess() {

    }

    public static ConvertTrajectoryResponseV4 createResponse(String json) {
        ConvertTrajectoryResponseV4 result;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonNode node = mapper.readTree(json);
            result = mapper.treeToValue(node, ConvertTrajectoryResponseV4.class);
        } catch (IOException e) {
            return null;
        }
        return result;
    }

    public static String createPersistenceReferenceUnitXY(String json) {
        String result;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonNode node = mapper.readTree(json);
            result = mapper.treeToValue(node, String.class);
        } catch (IOException e) {
            return null;
        }
        return result;
    }

}
