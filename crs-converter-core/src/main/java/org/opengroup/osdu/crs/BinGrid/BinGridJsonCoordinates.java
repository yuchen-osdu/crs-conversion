package org.opengroup.osdu.crs.BinGrid;

import java.util.List;

import lombok.Data;

@Data
public class BinGridJsonCoordinates {
	
	private List<InnerBinGridCoordinates> abstractCoordinates;	
	
}
