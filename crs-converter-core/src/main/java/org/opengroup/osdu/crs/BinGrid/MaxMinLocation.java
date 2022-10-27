package org.opengroup.osdu.crs.BinGrid;

import lombok.Data;

@Data
public class MaxMinLocation {
		
	private Double dI;
	private Double dJ;
	
	@Override
	public String toString() {
		return "MaxMinLocation [dI=" + dI + ", dJ=" + dJ + "]";
	}
	
}
