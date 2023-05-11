package org.opengroup.osdu.crs.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.opengroup.osdu.crs.util.Constants;

import java.util.List;

@Data
@AllArgsConstructor
@ApiModel(description = Constants.SWAGGER_TRJ_MINIMUM_DEPTH_INTERVAL_DESCRIPTION)
public class MinimumDepthInterval {
    @ApiModelProperty(
            value = Constants.SWAGGER_MD_I, required = true, dataType = "Double",
            example = Constants.SWAGGER_MD_I_EXAMPLE)
    private List<Double> md_i;
    private Integer md_interval;

}
