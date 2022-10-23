package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AbstractFeature extends BinGridJsonBase{
	
	AbstractFeature(String type) {
		super(type);		
	}

	@JsonProperty("properties")
    private Object properties;	
	
	@JsonProperty("geometry")
    private BinGridJsonBase geometry;
	

	
	@Override
	public String toString() {
		return "AbstractFeature [properties=" + properties + ", geometry=" + geometry + "]";
	}

}
