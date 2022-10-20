package org.opengroup.osdu.crs.BinGrid;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AbstractFeatureCollection {
	
	@JsonProperty("features")
    private AbstractFeature[] features;

	@Override
	public String toString() {
		return "AbstractFeatureCollection [features=" + Arrays.toString(features) + "]";
	}
	





}
