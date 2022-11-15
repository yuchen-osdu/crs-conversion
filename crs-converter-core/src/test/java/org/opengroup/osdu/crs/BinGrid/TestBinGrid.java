package org.opengroup.osdu.crs.BinGrid;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import javax.validation.ValidationException;

import org.junit.Test;
import org.opengroup.osdu.crs.converter.CRSConverter;
import org.opengroup.osdu.crs.model.ConvertBinGridRequest;
import org.opengroup.osdu.crs.util.ConstantsTests;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestBinGrid {

	private static final String Input_Request_Test_1a = "{\"inBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3100000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3100000.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";

	private static final String Input_Request_Test_1b = "{\"inBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[499850.00,3100000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600100.00,3100000.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";

	private static final String Input_Request_Test_1c = "{\"inBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3100000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[598995.00,3006300.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3100000.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";

	private static final String Input_Request_Test_1d = "{\"toCRS\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32064_EPSG::15851\",\"inBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3593524.86,9888579.93]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3577518.4,10217694.31]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3922906.04,9904378.13]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3906852.11,10233974.62]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";

	private static final String Input_Request_Test_1e = "{\"toCRS\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32064_EPSG::15851\",\"inBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[-312617.66,9888576.39]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[-296599.22,10217690.07]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[16280.67,9872829.65]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[32255.91,10201463.52]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";

	private static final String Input_Request_Test_2a = "{\\\"inBinGrid\\\":{\\\"BinGridDefinitionMethodTypeID\\\":\\\"4Corner\\\",\\\"ABCDBinGridSpatialLocation\\\":{\\\"AsIngestedCoordinates\\\":{\\\"type\\\":\\\"AnyCrsFeatureCollection\\\",\\\"CoordinateReferenceSystemID\\\":\\\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::26715_EPSG::15851\\\",\\\"features\\\":[{\\\"type\\\":\\\"AnyCrsFeature\\\",\\\"properties\\\":{\\\"Kind\\\":\\\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\\\",\\\"PointProperties\\\":[{\\\"Label\\\":\\\"A\\\",\\\"Inline\\\":14100,\\\"Crossline\\\":5161}]},\\\"geometry\\\":{\\\"type\\\":\\\"AnyCrsPoint\\\",\\\"coordinates\\\":[423082.00,3227690.00]}},{\\\"type\\\":\\\"AnyCrsFeature\\\",\\\"properties\\\":{\\\"Kind\\\":\\\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\\\",\\\"PointProperties\\\":[{\\\"Label\\\":\\\"B\\\",\\\"Inline\\\":14100,\\\"Crossline\\\":9409}]},\\\"geometry\\\":{\\\"type\\\":\\\"AnyCrsPoint\\\",\\\"coordinates\\\":[491346.00,3309044.00]}},{\\\"type\\\":\\\"AnyCrsFeature\\\",\\\"properties\\\":{\\\"Kind\\\":\\\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\\\",\\\"PointProperties\\\":[{\\\"Label\\\":\\\"C\\\",\\\"Inline\\\":17700,\\\"Crossline\\\":5161}]},\\\"geometry\\\":{\\\"type\\\":\\\"AnyCrsPoint\\\",\\\"coordinates\\\":[395504.00,3250830.00]}},{\\\"type\\\":\\\"AnyCrsFeature\\\",\\\"properties\\\":{\\\"Kind\\\":\\\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\\\",\\\"PointProperties\\\":[{\\\"Label\\\":\\\"D\\\",\\\"Inline\\\":17700,\\\"Crossline\\\":9409}]},\\\"geometry\\\":{\\\"type\\\":\\\"AnyCrsPoint\\\",\\\"coordinates\\\":[463768.00,3332184.00]}}]}},\\\"P6ScaleFactorOfBinGrid\\\":1,\\\"P6BinNodeIncrementOnIaxis\\\":3,\\\"P6BinNodeIncrementOnJaxis\\\":2}}";

	private static final String Input_Request_Test_2b = "{\"inBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::26715_EPSG::15851\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":14100,\"Crossline\":5161}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[395504.00,3250830.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":14100,\"Crossline\":9409}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[463768.00,3332184.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":17700,\"Crossline\":5161}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[423082.00,3227690.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":17700,\"Crossline\":9409}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[491346.00,3309044.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":3,\"P6BinNodeIncrementOnJaxis\":2}}";

	private static final String Input_Request_Test_2c = "{\\\"inBinGrid\\\":{\\\"BinGridDefinitionMethodTypeID\\\":\\\"4Corner\\\",\\\"ABCDBinGridSpatialLocation\\\":{\\\"AsIngestedCoordinates\\\":{\\\"type\\\":\\\"AnyCrsFeatureCollection\\\",\\\"CoordinateReferenceSystemID\\\":\\\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::26715_EPSG::15851\\\",\\\"features\\\":[{\\\"type\\\":\\\"AnyCrsFeature\\\",\\\"properties\\\":{\\\"Kind\\\":\\\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\\\",\\\"PointProperties\\\":[{\\\"Label\\\":\\\"A\\\",\\\"Inline\\\":14100,\\\"Crossline\\\":5161}]},\\\"geometry\\\":{\\\"type\\\":\\\"AnyCrsPoint\\\",\\\"coordinates\\\":[491346.00,3309044.00]}},{\\\"type\\\":\\\"AnyCrsFeature\\\",\\\"properties\\\":{\\\"Kind\\\":\\\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\\\",\\\"PointProperties\\\":[{\\\"Label\\\":\\\"B\\\",\\\"Inline\\\":14100,\\\"Crossline\\\":9409}]},\\\"geometry\\\":{\\\"type\\\":\\\"AnyCrsPoint\\\",\\\"coordinates\\\":[423082.00,3227690.00]}},{\\\"type\\\":\\\"AnyCrsFeature\\\",\\\"properties\\\":{\\\"Kind\\\":\\\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\\\",\\\"PointProperties\\\":[{\\\"Label\\\":\\\"C\\\",\\\"Inline\\\":17700,\\\"Crossline\\\":5161}]},\\\"geometry\\\":{\\\"type\\\":\\\"AnyCrsPoint\\\",\\\"coordinates\\\":[463768.00,3332184.00]}},{\\\"type\\\":\\\"AnyCrsFeature\\\",\\\"properties\\\":{\\\"Kind\\\":\\\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\\\",\\\"PointProperties\\\":[{\\\"Label\\\":\\\"D\\\",\\\"Inline\\\":17700,\\\"Crossline\\\":9409}]},\\\"geometry\\\":{\\\"type\\\":\\\"AnyCrsPoint\\\",\\\"coordinates\\\":[395504.00,3250830.00]}}]}},\\\"P6ScaleFactorOfBinGrid\\\":1,\\\"P6BinNodeIncrementOnIaxis\\\":3,\\\"P6BinNodeIncrementOnJaxis\\\":2}}";

	private static final String Input_Request_Test_2d = "{\"inBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::26715_EPSG::15851\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":14100,\"Crossline\":5161}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[491346.00,3309044.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":14100,\"Crossline\":9409}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[423082.00,3227690.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":17700,\"Crossline\":5161}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[463768.00,3332184.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":17700,\"Crossline\":9409}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[395504.00,3250830.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":3,\"P6BinNodeIncrementOnJaxis\":2}}";

	private static final String Input_Request_Test_2e = "{\"inBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::26715_EPSG::15851\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":14100,\"Crossline\":5161}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[423082.00,3227690.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":14100,\"Crossline\":9409}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[395504.00,3250830.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":17700,\"Crossline\":5161}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[491346.00,3309044.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":17700,\"Crossline\":9409}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[463768.00,3332184.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":3,\"P6BinNodeIncrementOnJaxis\":2}}";

	private static final String Input_Request_with_3_point = "{\"inBinGrid\":{\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3100000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3000000.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";

	@Test
	public void testBinGridInputRequest() {
		ConvertBinGridRequest inBinGridRequest;
		inBinGridRequest = TestBinGrid.createInstance(Input_Request_Test_1a);
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

	@Test(expected = ValidationException.class)
	public void testBinGridInvalidRequest() {
		ConvertBinGridRequest inBinGridRequest;
		inBinGridRequest = TestBinGrid.createInstance(Input_Request_with_3_point);
		assertNotNull(inBinGridRequest);
		new CRSConverter().convertBinGrid(ConstantsTests.EB_NAD83_UTM11N_1702[ConstantsTests.V2],
				inBinGridRequest.getInBinGrid());
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
