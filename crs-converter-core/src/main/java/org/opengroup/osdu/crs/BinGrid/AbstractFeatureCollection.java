package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AbstractFeatureCollection {
	
	@JsonProperty("features")
    private AbstractFeature[] features;

}
