package org.opengroup.osdu.crs.BinGrid;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class BinGridJsonBase {
	
	@JsonProperty("type")
    @NotEmpty
    private String type;

	@JsonProperty("coordinates")
    @NotEmpty
    private BinGridJsonCoordinates coordinates;
        
    BinGridJsonBase(String type) {
        this.type = type;      
    }
   	
}
