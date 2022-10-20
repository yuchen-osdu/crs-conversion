package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AbstractAnyCrsFeatureCollection {

	@JsonProperty("features")
	private AbstractFeature[] features;

	@JsonProperty("persistableReferenceCrs")
	private String persistableReferenceCrs;

	@JsonProperty("CoordinateReferenceSystemID")
	private String CoordinateReferenceSystemID;

}
