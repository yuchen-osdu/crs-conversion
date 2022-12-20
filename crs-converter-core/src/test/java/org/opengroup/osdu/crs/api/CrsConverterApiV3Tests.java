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

	private static final String Input_Response_Test_1a = "{\"maxMisLocation\":{\"dI\":0.0,\"dJ\":0.0},\"outBinGrid\":{\"BinGridName\":\"TestBinGridName\",\"BinGridTypeID\":\"TestBinGridTypeID\",\"SourceBinGridID\":123,\"SourceBinGridAppID\":\"TestSourceBinGridAppID\",\"CoveragePercent\":1.0,\"BinGridDefinitionMethodTypeID\":\"opendes:reference-data--BinGridDefinitionMethodType:4Corner:\",\"P6TransformationMethod\":9666,\"P6BinGridOriginI\":1.0,\"P6BinGridOriginJ\":1000.0,\"P6BinGridOriginEasting\":500000.0,\"P6BinGridOriginNorthing\":3000000.0,\"P6ScaleFactorOfBinGrid\":1.0,\"P6BinWidthOnIaxis\":1000.0,\"P6BinWidthOnJaxis\":100.0,\"P6MapGridBearingOfBinGridJaxis\":0.0,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1,\"ABCDBinGridSpatialLocation\":{\"SpatialLocationCoordinatesDate\":\"2022-12-20T07:28:33.091Z\",\"CoordinateQualityCheckPerformedBy\":\"CRSConvertservice,POSTconvertBinGrid\",\"CoordinateQualityCheckDateTime\":\"2022-12-20T07:28:33.091Z\",\"CoordinateQualityCheckRemarks\":[\"Max.squaringerror:dI=0.0,dJ=0.0bin\"],\"AsIngestedCoordinates\":{\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.0,3000000.0]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.0,3100000.0]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.0,3000000.0]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.0,3100000.0]}}]},\"Wgs84Coordinates\":{\"features\":[{\"type\":\"Feature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-93.0,27.12246964]}},{\"type\":\"Feature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-93.0,28.02525499]}},{\"type\":\"Feature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-91.99105764,27.11884583]}},{\"type\":\"Feature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-91.98276722,28.02148995]}}],\"type\":\"FeatureCollection\"}}},\"AppliedOperations\":[\"Squaredupthebingrid.dI=0.0,dJ=0.0bin\"]}";
		
	private static final String PersistenceReferenceCRS_1a = "\"{\\\"authCode\\\":{\\\"auth\\\":\\\"EPSG\\\",\\\"code\\\":\\\"32615\\\"},\\\"name\\\":\\\"WGS_1984_UTM_Zone_15N\\\",\\\"type\\\":\\\"LBC\\\",\\\"ver\\\":\\\"PE_10_9_1\\\",\\\"wkt\\\":\\\"PROJCS[\\\\\\\"WGS_1984_UTM_Zone_15N\\\\\\\",GEOGCS[\\\\\\\"GCS_WGS_1984\\\\\\\",DATUM[\\\\\\\"D_WGS_1984\\\\\\\",SPHEROID[\\\\\\\"WGS_1984\\\\\\\",6378137.0,298.257223563]],PRIMEM[\\\\\\\"Greenwich\\\\\\\",0.0],UNIT[\\\\\\\"Degree\\\\\\\",0.0174532925199433]],PROJECTION[\\\\\\\"Transverse_Mercator\\\\\\\"],PARAMETER[\\\\\\\"False_Easting\\\\\\\",500000.0],PARAMETER[\\\\\\\"False_Northing\\\\\\\",0.0],PARAMETER[\\\\\\\"Central_Meridian\\\\\\\",-93.0],PARAMETER[\\\\\\\"Scale_Factor\\\\\\\",0.9996],PARAMETER[\\\\\\\"Latitude_Of_Origin\\\\\\\",0.0],UNIT[\\\\\\\"Meter\\\\\\\",1.0],AUTHORITY[\\\\\\\"EPSG\\\\\\\",32615]]\\\"}\"";
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
		response = CrsConverterApiV3Tests.createResponse(Input_Response_Test_1a);
		String persistableReferenceCrs = CrsConverterApiV3Tests.createPersistenceReferenceCRS(PersistenceReferenceCRS_1a);
		response.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().setPersistableReferenceCrs(persistableReferenceCrs);	
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
	
	public static ConvertBinGridResponse createResponse(String json) {
		ConvertBinGridResponse result;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonNode node = mapper.readTree(json);
			result = mapper.treeToValue(node, ConvertBinGridResponse.class);
		} catch (IOException e) {
			return null;
		}
		return result;
	}
	
	public static String createPersistenceReferenceCRS(String json) {
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
