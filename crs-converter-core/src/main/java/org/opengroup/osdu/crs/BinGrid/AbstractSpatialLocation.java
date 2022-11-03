package org.opengroup.osdu.crs.BinGrid;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AbstractSpatialLocation {

	@JsonProperty("SpatialLocationCoordinatesDate")
	private Date spatialLocationCoordinatesDate;

	@JsonProperty("QuantitativeAccuracyBandID")
	private String quantitativeAccuracyBandID;

	@JsonProperty("QualitativeSpatialAccuracyTypeID")
	private String qualitativeSpatialAccuracyTypeID;

	@JsonProperty("CoordinateQualityCheckPerformedBy")
	private String coordinateQualityCheckPerformedBy;

	@JsonProperty("CoordinateQualityCheckDateTime")
	private Date coordinateQualityCheckDateTime;

	@JsonProperty("CoordinateQualityCheckRemarks")
	private String[] coordinateQualityCheckRemarks;

	@JsonProperty("AppliedOperations")
	private String[] appliedOperations;

	@JsonProperty("SpatialParameterTypeID")
	private String spatialParameterTypeID;

	@JsonProperty("SpatialGeometryTypeID")
	private String spatialGeometryTypeID;

	@JsonProperty("AsIngestedCoordinates")
	private AbstractAnyCrsFeatureCollection asIngestedcoordinates;

	@JsonProperty("Wgs84Coordinates")
	private AbstractFeatureCollection wgs84Coordinates;

}
