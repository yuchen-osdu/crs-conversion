package org.opengroup.osdu.crs.interfaces;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.crs.model.ConvertTrajectoryRequest;
import org.opengroup.osdu.crs.model.ConvertTrajectoryResponse;

public interface ITrajectoryConverter {
	ConvertTrajectoryResponse convertTrajectory(DpsHeaders headers, ConvertTrajectoryRequest request);
}
