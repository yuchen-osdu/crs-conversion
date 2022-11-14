package org.opengroup.osdu.crs.BinGrid;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.opengroup.osdu.crs.model.ConvertBinGridRequest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestBinGrid {

	private static final String Input_Request_Test_1 = "{\r\n" + "    \"inBinGrid\": {\r\n"
			+ "    \"BinGridDefinitionMethodTypeID\": \"4Corner\",\r\n" + "    \"ABCDBinGridSpatialLocation\": {\r\n"
			+ "        \"AsIngestedCoordinates\": {\r\n" + "            \"type\": \"AnyCrsFeatureCollection\",\r\n"
			+ "            \"CoordinateReferenceSystemID\": \"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\r\n"
			+ "            \"features\": [\r\n" + "                {\r\n"
			+ "                    \"type\": \"AnyCrsFeature\",\r\n" + "                    \"properties\": {\r\n"
			+ "                        \"Kind\": \"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\r\n"
			+ "                        \"PointProperties\": [\r\n" + "                            {\r\n"
			+ "                                \"Label\": \"A\",\r\n"
			+ "                                \"Inline\": 1,\r\n"
			+ "                                \"Crossline\": 1000\r\n" + "                            }\r\n"
			+ "                        ]\r\n" + "                    },\r\n" + "                    \"geometry\": {\r\n"
			+ "                        \"type\": \"AnyCrsPoint\",\r\n"
			+ "                        \"coordinates\": [\r\n" + "                            500000.00,\r\n"
			+ "                            3000000.00\r\n" + "                        ]\r\n"
			+ "                    }\r\n" + "                },\r\n" + "                {\r\n"
			+ "                    \"type\": \"AnyCrsFeature\",\r\n" + "                    \"properties\": {\r\n"
			+ "                        \"Kind\": \"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\r\n"
			+ "                        \"PointProperties\": [\r\n" + "                            {\r\n"
			+ "                                \"Label\": \"B\",\r\n"
			+ "                                \"Inline\": 1,\r\n"
			+ "                                \"Crossline\": 2000\r\n" + "                            }\r\n"
			+ "                        ]\r\n" + "                    },\r\n" + "                    \"geometry\": {\r\n"
			+ "                        \"type\": \"AnyCrsPoint\",\r\n"
			+ "                        \"coordinates\": [\r\n" + "                            500000.00,\r\n"
			+ "                            3100000.00\r\n" + "                        ]\r\n"
			+ "                    }\r\n" + "                },\r\n" + "                {\r\n"
			+ "                    \"type\": \"AnyCrsFeature\",\r\n" + "                    \"properties\": {\r\n"
			+ "                        \"Kind\": \"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\r\n"
			+ "                        \"PointProperties\": [\r\n" + "                            {\r\n"
			+ "                                \"Label\": \"C\",\r\n"
			+ "                                \"Inline\": 101,\r\n"
			+ "                                \"Crossline\": 1000\r\n" + "                            }\r\n"
			+ "                        ]\r\n" + "                    },\r\n" + "                    \"geometry\": {\r\n"
			+ "                        \"type\": \"AnyCrsPoint\",\r\n"
			+ "                        \"coordinates\": [\r\n" + "                            600000.00,\r\n"
			+ "                            3000000.00\r\n" + "                        ]\r\n"
			+ "                    }\r\n" + "                },\r\n" + "                {\r\n"
			+ "                    \"type\": \"AnyCrsFeature\",\r\n" + "                    \"properties\": {\r\n"
			+ "                        \"Kind\": \"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\r\n"
			+ "                        \"PointProperties\": [\r\n" + "                            {\r\n"
			+ "                                \"Label\": \"D\",\r\n"
			+ "                                \"Inline\": 101,\r\n"
			+ "                                \"Crossline\": 2000\r\n" + "                            }\r\n"
			+ "                        ]\r\n" + "                    },\r\n" + "                    \"geometry\": {\r\n"
			+ "                        \"type\": \"AnyCrsPoint\",\r\n"
			+ "                        \"coordinates\": [\r\n" + "                            600000.00,\r\n"
			+ "                            3100000.00\r\n" + "                        ]\r\n"
			+ "                    }\r\n" + "                }\r\n" + "            ]\r\n" + "        }\r\n"
			+ "    },\r\n" + "    \"P6ScaleFactorOfBinGrid\": 1,\r\n" + "    \"P6BinNodeIncrementOnIaxis\": 1,\r\n"
			+ "    \"P6BinNodeIncrementOnJaxis\": 1\r\n" + "}\r\n" + "}";

	@Test
	public void testBinGridInputRequest() {
		ConvertBinGridRequest inBinGridRequest;
		inBinGridRequest = TestBinGrid.createInstance(Input_Request_Test_1);
		assertNotNull(inBinGridRequest);
		assertEquals("4Corner", inBinGridRequest.getInBinGrid().getBinGridDefinitionMethodTypeID());
		assertEquals(1, inBinGridRequest.getInBinGrid().getP6ScaleFactorOfBinGrid());
		assertEquals(1, inBinGridRequest.getInBinGrid().getP6BinNodeIncrementOnIaxis());
		assertEquals(1, inBinGridRequest.getInBinGrid().getP6BinNodeIncrementOnJaxis());
		assertEquals(4, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().size());
		assertEquals(1, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(0).getProperties().getPointPropertiesList().get(0).getInline());
		assertEquals(1000, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(0).getProperties().getPointPropertiesList().get(0).getCrossline());
		
		assertEquals(1, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(1).getProperties().getPointPropertiesList().get(0).getInline());
		assertEquals(2000, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(1).getProperties().getPointPropertiesList().get(0).getCrossline());
		
		assertEquals(101, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(2).getProperties().getPointPropertiesList().get(0).getInline());
		assertEquals(1000, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(2).getProperties().getPointPropertiesList().get(0).getCrossline());
		
		assertEquals(101, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(3).getProperties().getPointPropertiesList().get(0).getInline());
		assertEquals(2000, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(3).getProperties().getPointPropertiesList().get(0).getCrossline());
		
		assertEquals(500000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(0).getGeometry().getCoordinates().get(0));
		assertEquals(3000000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(0).getGeometry().getCoordinates().get(1));
		
		assertEquals(500000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(1).getGeometry().getCoordinates().get(0));
		assertEquals(3100000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(1).getGeometry().getCoordinates().get(1));
		
		assertEquals(600000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(2).getGeometry().getCoordinates().get(0));
		assertEquals(3000000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(2).getGeometry().getCoordinates().get(1));
		
		assertEquals(600000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(3).getGeometry().getCoordinates().get(0));
		assertEquals(3100000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(3).getGeometry().getCoordinates().get(1));
	}

	public static ConvertBinGridRequest createInstance(String json) {
		ConvertBinGridRequest result;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonNode node = mapper.readTree(json);
			result = mapper.treeToValue(node, ConvertBinGridRequest.class);
		} catch (IOException e) {
			return null;
		}
		return result;
	}

}
