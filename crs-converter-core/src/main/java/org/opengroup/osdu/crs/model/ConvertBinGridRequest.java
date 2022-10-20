package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.BinGrid.AbstractBinGrid;

import lombok.Data;

@Data
public class ConvertBinGridRequest {
	
	private String toCRS;
	
	private AbstractBinGrid inBinGrid;	

}
