package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class InnerBinGridCoordinates {
	
	@JsonProperty("x")
	private Double x;
	
	@JsonProperty("y")
    private Double y;	
	
	@JsonProperty("z")
	private Double z;
	



	@Override
	public String toString() {
		return "InnerBinGridJsonCoordinates [x=" + x + ", y=" + y + ", z=" + z + "]";
	}

}
