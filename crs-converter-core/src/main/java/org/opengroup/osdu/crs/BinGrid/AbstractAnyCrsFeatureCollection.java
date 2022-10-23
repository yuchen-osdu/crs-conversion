package org.opengroup.osdu.crs.BinGrid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AbstractAnyCrsFeatureCollection {

	@JsonProperty("features")
	private List<AbstractFeature> features;

	@JsonProperty("persistableReferenceCrs")
	private String persistableReferenceCrs;

	@JsonProperty("CoordinateReferenceSystemID")
	private String CoordinateReferenceSystemID;

}
