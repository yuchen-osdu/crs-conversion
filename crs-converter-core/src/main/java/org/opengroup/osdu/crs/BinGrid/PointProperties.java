package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PointProperties {
	
	@JsonProperty("Label")
	private String label;
	@JsonProperty("InlineNo")
	private Integer inlineNo;
	@JsonProperty("CrosslineNo")
	private Integer crosslineNo;	

}
