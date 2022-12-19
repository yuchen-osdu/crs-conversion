package org.opengroup.osdu.crs.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.crs.BinGrid.MaxMisLocation;
import org.opengroup.osdu.crs.converter.CRSConverter;
import org.opengroup.osdu.crs.model.ConvertBinGridRequest;
import org.opengroup.osdu.crs.model.ConvertBinGridResponse;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class CrsConverterApiV3Tests {

	private static final String Input_Request_Test_1a = "{\"inBinGrid\":{\"BinGridName\":\"TestBinGridName\",\"BinGridTypeID\":\"TestBinGridTypeID\",\"SourceBinGridID\":123,\"SourceBinGridAppID\":\"TestSourceBinGridAppID\",\"CoveragePercent\":1.0,\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"opendes:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3100000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3100000.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}\"";

	private static final String Input_Response_Test_1a = "{\"MaxMisLocation\":{\"dI\":0,\"dJ\":0},\"outBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"P6TransformationMethod\":9666,\"P6BinGridOriginI\":51,\"P6BinGridOriginJ\":1500,\"P6BinGridOriginEasting\":550000,\"P6BinGridOriginNorthing\":3050000,\"P6ScaleFactorOfBinGrid\":1,\"P6BinWidthOnIaxis\":1000,\"P6BinWidthOnJaxis\":100,\"P6MapGridBearingOfBinGridJaxis\":0,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1,\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"type\":\"AnyCrsFeatureCollection\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000,3000000]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000,3100000]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000,3000000]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000,3100000]}}]},\"Wgs84Coordinates\":{\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000,3000000]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000,3100000]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000,3000000]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000,3100000]}}]}}}}";

	private static final String Input_Request_Test_1d = "{\"toCRS\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32064_EPSG::15851\",\"inBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3593524.86,9888579.93]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3577518.4,10217694.31]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3922906.04,9904378.13]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3906852.11,10233974.62]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";

	private static final String Input_Response_Test_1d = "{\"MaxMisLocation\":{\"dI\":0,\"dJ\":0.38},\"outBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"P6TransformationMethod\":9666,\"P6BinGridOriginI\":51,\"P6BinGridOriginJ\":1500,\"P6BinGridOriginEasting\":3750200.3525,\"P6BinGridOriginNorthing\":10061156.7475,\"P6ScaleFactorOfBinGrid\":1,\"P6BinWidthOnIaxis\":3297.4784730940096,\"P6BinWidthOnJaxis\":329.74531059515755,\"P6MapGridBearingOfBinGridJaxis\":357.21353378192293,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1,\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"type\":\"AnyCrsFeatureCollection\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3593536.46093266,9888463.875]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3577506.27470713,10217819.311]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3922894.43029287,9904494.184]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3906864.24406734,10233849.62]}}]},\"Wgs84Coordinates\":{\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3593536.46093266,9888463.875]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3577506.27470713,10217819.311]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3922894.43029287,9904494.184]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3906864.24406734,10233849.62]}}]}}}}";

	private static final String Input_Request_with_3_point = "{\"inBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3100000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3000000.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";

	@Mock
	private CRSConverter crsConverter;

	@Mock
	private CrsConverterApiV3 crsConverterApi;

	@Test
	public void convertBinGridWithSuccessfulResponse() {
		ConvertBinGridRequest inBinGrid = new ConvertBinGridRequest();
		ConvertBinGridResponse response = new ConvertBinGridResponse();
		MaxMisLocation location = new MaxMisLocation();
		location.setDI(0.0);
		location.setDJ(0.0);
		response.setMaxMisLocation(location);
		inBinGrid = CrsConverterApiV3Tests.createInstance(Input_Request_Test_1a);
		response.setOutBinGrid(inBinGrid.getInBinGrid());
		lenient().when(crsConverter.squaring(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(response);
		assertEquals(new Double(0.0), response.getMaxMisLocation().getDI());
		assertEquals(new Double(0.0),response.getMaxMisLocation().getDJ());	
	}

	public static ConvertBinGridRequest createInstance(String json) {
		ConvertBinGridRequest result;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonNode node = mapper.readTree(json);
			result = mapper.treeToValue(node, ConvertBinGridRequest.class);
		} catch (IOException e) {
			return null;
		}
		return result;
	}

}
