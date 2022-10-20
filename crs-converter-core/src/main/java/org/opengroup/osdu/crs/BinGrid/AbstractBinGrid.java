package org.opengroup.osdu.crs.BinGrid;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AbstractBinGrid {

	@JsonProperty("BinGridName")
	private String binGridName;

	@JsonProperty("BinGridTypeID")
	private String binGridTypeID;

	@JsonProperty("SourceBinGridID")
	private String sourceBinGridID;

	@JsonProperty("SourceBinGridAppID")
	private String sourceBinGridAppID;

	@JsonProperty("CoveragePercent")
	private String coveragePercent;

	@JsonProperty("BinGridDefinitionMethodTypeID")
	private String binGridDefinitionMethodTypeID;

	@JsonProperty("P6TransformationMethod")
	private Integer p6TransformationMethod;

	@JsonProperty("P6BinGridOriginI")
	private Double p6BinGridOriginI;

	@JsonProperty("P6BinGridOriginJ")
	private Double p6BinGridOriginJ;

	@JsonProperty("P6BinGridOriginEasting")
	private Double p6BinGridOriginEasting;

	@JsonProperty("P6BinGridOriginNorthing")
	private Double p6BinGridOriginNorthing;

	@JsonProperty("P6ScaleFactorOfBinGrid")
	private Double p6ScaleFactorOfBinGrid;

	@JsonProperty("P6BinWidthOnIaxis")
	private Double p6BinWidthOnIaxis;

	@JsonProperty("P6BinWidthOnJaxis")
	private Double p6BinWidthOnJaxis;

	@JsonProperty("P6MapGridBearingOfBinGridJaxis")
	private Double p6MapGridBearingOfBinGridJaxis;

	@JsonProperty("P6BinNodeIncrementOnIaxis")
	private Double p6BinNodeIncrementOnIaxis;

	@JsonProperty("P6BinNodeIncrementOnJaxis")
	private Double p6BinNodeIncrementOnJaxis;

	@JsonProperty("ABCDBinGridLocalCoordinates")
	private AbstractCoordinates[] aBCDBinGridLocalCoordinates;
	 
	@JsonProperty("ABCDBinGridSpatialLocation")
	private AbstractSpatialLocation aBCDBinGridSpatialLocation;

	

	@Override
	public String toString() {
		return "AbstractBinGrid [binGridName=" + binGridName + ", binGridTypeID=" + binGridTypeID + ", sourceBinGridID="
				+ sourceBinGridID + ", sourceBinGridAppID=" + sourceBinGridAppID + ", soveragePercent="
				+ coveragePercent + ", binGridDefinitionMethodTypeID=" + binGridDefinitionMethodTypeID
				+ ", p6TransformationMethod=" + p6TransformationMethod + ", p6BinGridOriginI=" + p6BinGridOriginI
				+ ", p6BinGridOriginJ=" + p6BinGridOriginJ + ", p6BinGridOriginEasting=" + p6BinGridOriginEasting
				+ ", p6BinGridOriginNorthing=" + p6BinGridOriginNorthing + ", p6ScaleFactorOfBinGrid="
				+ p6ScaleFactorOfBinGrid + ", p6BinWidthOnIaxis=" + p6BinWidthOnIaxis + ", p6BinWidthOnJaxis="
				+ p6BinWidthOnJaxis + ", p6MapGridBearingOfBinGridJaxis=" + p6MapGridBearingOfBinGridJaxis
				+ ", p6BinNodeIncrementOnIaxis=" + p6BinNodeIncrementOnIaxis + ", p6BinNodeIncrementOnJaxis="
				+ p6BinNodeIncrementOnJaxis + ", aBCDBinGridLocalCoordinates="
				+ Arrays.toString(aBCDBinGridLocalCoordinates) + ", aBCDBinGridSpatialLocation="
				+ aBCDBinGridSpatialLocation + "]";
	}

}
