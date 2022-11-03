package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class AbstractFeature extends BinGridJsonBase{
	
	AbstractFeature(String type) {
		super(type);		
	}

	@JsonProperty("properties")
    private PropertiesBinGridCorners properties;	
	
	@JsonProperty("geometry")
    private BinGridJsonBase geometry;
	
}
