package org.opengroup.osdu.crs.model.v4;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.crs.model.ConvertTrajectoryResponse;
import org.opengroup.osdu.crs.model.ScaleConvergence;
import org.opengroup.osdu.crs.model.TrajectoryStationOut;
import org.opengroup.osdu.crs.util.Constants;

import javax.validation.Valid;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = Constants.SWAGGER_TRJ_RSP_DESCRIPTION)
public class ConvertTrajectoryResponseV4 extends ConvertTrajectoryResponse {

    @Valid
    @ApiModelProperty(value = Constants.SWAGGER_TRJ_RSP_LIST_OF_STATIONS_I, required = true)
    private List<TrajectoryStationOut> stations_i;

    @Valid
    @ApiModelProperty(value = Constants.SWAGGER_TRJ_SCALE_CONVERGENCE, required = true)
    private List<ScaleConvergence> scaleConvergenceList;

    @Valid
    @ApiModelProperty(value = Constants.SWAGGER_TRJ_UNIT_MD)
    private String unitMD;
}
