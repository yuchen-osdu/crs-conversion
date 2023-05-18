package org.opengroup.osdu.crs.interfaces;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.crs.model.ConvertTrajectoryRequest;
import org.opengroup.osdu.crs.model.ConvertTrajectoryResponse;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryRequestV4;

public interface ITrajectoryConverter {
	ConvertTrajectoryResponse convertTrajectory(DpsHeaders headers, ConvertTrajectoryRequest request);
	ConvertTrajectoryResponse convertTrajectoryV4(DpsHeaders headers, ConvertTrajectoryRequestV4 request, Boolean flag);

}
