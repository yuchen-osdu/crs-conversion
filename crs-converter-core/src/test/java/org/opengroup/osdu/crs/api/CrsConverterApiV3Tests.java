package org.opengroup.osdu.crs.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.validation.ValidationException;

import org.junit.Test;
import org.mockito.Mock;
import org.opengroup.osdu.crs.converter.CRSConverter;
import org.opengroup.osdu.crs.model.ConvertBinGridRequest;
import org.opengroup.osdu.crs.model.ConvertBinGridResponse;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CrsConverterApiV3Tests {

	private static final String Input_Request_Test_1a = "{\"inBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3100000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3100000.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";

	private static final String Input_Response_Test_1a = "{\"MaxMisLocation\":{\"dI\":0,\"dJ\":0},\"outBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"P6TransformationMethod\":9666,\"P6BinGridOriginI\":51,\"P6BinGridOriginJ\":1500,\"P6BinGridOriginEasting\":550000,\"P6BinGridOriginNorthing\":3050000,\"P6ScaleFactorOfBinGrid\":1,\"P6BinWidthOnIaxis\":1000,\"P6BinWidthOnJaxis\":100,\"P6MapGridBearingOfBinGridJaxis\":0,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1,\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"type\":\"AnyCrsFeatureCollection\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000,3000000]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000,3100000]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000,3000000]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000,3100000]}}]},\"Wgs84Coordinates\":{\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000,3000000]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000,3100000]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000,3000000]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000,3100000]}}]}}}}";

	private static final String Input_Request_Test_1d = "{\"toCRS\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32064_EPSG::15851\",\"inBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3593524.86,9888579.93]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3577518.4,10217694.31]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3922906.04,9904378.13]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3906852.11,10233974.62]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";

	private static final String Input_Response_Test_1d = "{\"MaxMisLocation\":{\"dI\":0,\"dJ\":0.38},\"outBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"P6TransformationMethod\":9666,\"P6BinGridOriginI\":51,\"P6BinGridOriginJ\":1500,\"P6BinGridOriginEasting\":3750200.3525,\"P6BinGridOriginNorthing\":10061156.7475,\"P6ScaleFactorOfBinGrid\":1,\"P6BinWidthOnIaxis\":3297.4784730940096,\"P6BinWidthOnJaxis\":329.74531059515755,\"P6MapGridBearingOfBinGridJaxis\":357.21353378192293,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1,\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"type\":\"AnyCrsFeatureCollection\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3593536.46093266,9888463.875]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3577506.27470713,10217819.311]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3922894.43029287,9904494.184]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3906864.24406734,10233849.62]}}]},\"Wgs84Coordinates\":{\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3593536.46093266,9888463.875]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3577506.27470713,10217819.311]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3922894.43029287,9904494.184]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3906864.24406734,10233849.62]}}]}}}}";

	private static final String Input_Request_with_3_point = "{\"inBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3100000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3000000.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";

	@Mock
	CRSConverter crsConverter;

	@MockBean
	private CrsConverterApiV3 crsConverterApi;

	private ConvertBinGridResponse makeBinGridResponse() {
		ConvertBinGridResponse response = new ConvertBinGridResponse();
		response = CrsConverterApiV3Tests.createResponseInstance(Input_Response_Test_1a);
		return response;
	}

	private ConvertBinGridResponse makeBinGridResponseWithToCRS() {
		ConvertBinGridResponse response = new ConvertBinGridResponse();
		response = CrsConverterApiV3Tests.createResponseInstance(Input_Response_Test_1d);
		return response;
	}

	@Test
	public void convertBinGridWithSuccessfulResponse() {
		ConvertBinGridRequest inBinGrid = new ConvertBinGridRequest();
		ConvertBinGridResponse outBinGrid = new ConvertBinGridResponse();
		inBinGrid = CrsConverterApiV3Tests.createRequestInstance(Input_Request_Test_1a);
		when(crsConverter.squaring("toCrs", inBinGrid.getInBinGrid(), outBinGrid)).thenReturn(makeBinGridResponse());
		ConvertBinGridResponse response = crsConverterApi.convertBinGrid(inBinGrid);
		assertNotNull(response);
		assertEquals(response.getMaxMisLocation().getDI(), 0.0);
		assertEquals(response.getMaxMisLocation().getDJ(), 0.0);
	}

	@Test
	public void convertBinGridWithSuccessfulResponseWithToCRS() {
		ConvertBinGridRequest inBinGrid = new ConvertBinGridRequest();
		ConvertBinGridResponse outBinGrid = new ConvertBinGridResponse();
		inBinGrid = CrsConverterApiV3Tests.createRequestInstance(Input_Request_Test_1d);
		when(crsConverter.squaring(
				"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32064_EPSG::15851",
				inBinGrid.getInBinGrid(), outBinGrid)).thenReturn(makeBinGridResponseWithToCRS());
		ConvertBinGridResponse response = crsConverterApi.convertBinGrid(inBinGrid);
		assertNotNull(response);
		assertEquals(response.getMaxMisLocation().getDI(), 0.0);
		assertEquals(response.getMaxMisLocation().getDJ(), 0.38);
	}

	@Test(expected = ValidationException.class)
	public void convertBinGridSizeErrorTest() {
		ConvertBinGridRequest inBinGrid = new ConvertBinGridRequest();
		ConvertBinGridResponse outBinGrid = new ConvertBinGridResponse();
		inBinGrid = CrsConverterApiV3Tests.createRequestInstance(Input_Request_with_3_point);
		when(crsConverter.squaring("toCrs", inBinGrid.getInBinGrid(), outBinGrid)).thenThrow(ValidationException.class);
	}

	public static ConvertBinGridRequest createRequestInstance(String json) {
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

	public static ConvertBinGridResponse createResponseInstance(String json) {
		ConvertBinGridResponse result;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonNode node = mapper.readTree(json);
			result = mapper.treeToValue(node, ConvertBinGridResponse.class);
		} catch (IOException e) {
			return null;
		}
		return result;
	}

}
