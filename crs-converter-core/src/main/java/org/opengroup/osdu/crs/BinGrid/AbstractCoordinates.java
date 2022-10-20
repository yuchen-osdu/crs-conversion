package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AbstractCoordinates {
	
	@JsonProperty("X")
	private Double x;
	
	@JsonProperty("Y")
	private Double y;
	
	@Override
	public String toString() {
		return "AbstractCoordinates [x=" + x + ", y=" + y + "]";
	}
	
}
