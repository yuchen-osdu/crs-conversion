package org.opengroup.osdu.crs.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.crs.util.Constants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = Constants.SWAGGER_TRJ_SCALE_CONVERGENCE)
public class ScaleConvergence {

    @ApiModelProperty(
            value = Constants.SWAGGER_TRJ_SCALE_FACTOR, required = true, dataType = "Double",
            example = Constants.SWAGGER_TRJ_SCALE_FACTOR)
    private double scalefactor;
    @ApiModelProperty(
            value = Constants.SWAGGER_TRJ_CONVERGENCE, required = true, dataType = "Double",
            example = Constants.SWAGGER_TRJ_CONVERGENCE)
    private double convergence;
    @ApiModelProperty(
            value = Constants.SWAGGER_TRJ_POINT, required = true, dataType = "Double",
            example = Constants.SWAGGER_TRJ_POINT)
    private Point point;

}
