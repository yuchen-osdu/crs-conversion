package org.opengroup.osdu.crs.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.opengroup.osdu.crs.util.Constants;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@ApiModel(description = Constants.SWAGGER_TRJ_MINIMUM_DEPTH_INTERVAL_DESCRIPTION)
public class MinimumDepthInterval {

    @NotNull
    @ApiModelProperty(
            value = Constants.SWAGGER_MD_I, required = true, dataType = "Double",
            example = Constants.SWAGGER_MD_I_EXAMPLE)
    private double md_i;

    @NotNull
    @ApiModelProperty(
            value = Constants.SWAGGER_MD_INTERVAL, required = true, dataType = "Double",
            example = Constants.SWAGGER_MD_INTERVAL_EXAMPLE)
    private double md_interval;

}
