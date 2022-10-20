package org.opengroup.osdu.crs.BinGrid;

import java.util.Arrays;
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
	private AbstractAnyCrsFeatureCollection asingestedcoordinates;

	@JsonProperty("Wgs84Coordinates")
	private AbstractFeatureCollection wgs84Coordinates;

	@Override
	public String toString() {
		return "AbstractSpatialLocation [spatialLocationCoordinatesDate=" + spatialLocationCoordinatesDate
				+ ", quantitativeAccuracyBandID=" + quantitativeAccuracyBandID + ", qualitativeSpatialAccuracyTypeID="
				+ qualitativeSpatialAccuracyTypeID + ", coordinateQualityCheckPerformedBy="
				+ coordinateQualityCheckPerformedBy + ", coordinateQualityCheckDateTime="
				+ coordinateQualityCheckDateTime + ", coordinateQualityCheckRemarks="
				+ Arrays.toString(coordinateQualityCheckRemarks) + ", appliedOperations="
				+ Arrays.toString(appliedOperations) + ", spatialParameterTypeID=" + spatialParameterTypeID
				+ ", spatialGeometryTypeID=" + spatialGeometryTypeID + ", wgs84Coordinates=" + wgs84Coordinates + "]";
	}

}
