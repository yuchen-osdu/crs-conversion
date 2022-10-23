package org.opengroup.osdu.crs.model;

import java.util.List;

import org.opengroup.osdu.crs.BinGrid.AbstractBinGrid;
import org.opengroup.osdu.crs.BinGrid.MaxMinLocation;

import lombok.Data;

@Data
public class ConvertBinGridResponse {

	private MaxMinLocation maxMinLocation;

	private AbstractBinGrid outBinGrid;
	private List<String> operationsApplied;

}
