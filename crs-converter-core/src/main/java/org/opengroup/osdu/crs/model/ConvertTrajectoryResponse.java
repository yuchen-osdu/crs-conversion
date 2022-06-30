package org.opengroup.osdu.crs.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.opengroup.osdu.crs.util.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = Constants.SWAGGER_TRJ_RSP_DESCRIPTION)
public class ConvertTrajectoryResponse {
    @NotEmpty
    @ApiModelProperty(
            value = Constants.SWAGGER_TRJ_REQ_CRS, required = true, dataType = "String",
            example = Constants.SWAGGER_TARGET_CRS_EXAMPLE)
    private String trajectoryCRS;

    @NotEmpty
    @ApiModelProperty(
            value = Constants.SWAGGER_TRJ_RSP_UNIT_XY, dataType = "String",
            example = Constants.SWAGGER_TRJ_REQ_UNIT_EXAMPLE)
    private String unitXY;

    @NotEmpty
    @ApiModelProperty(
            value = Constants.SWAGGER_TRJ_RSP_UNIT_Z, required = true, dataType = "String",
            example = Constants.SWAGGER_TRJ_REQ_UNIT_EXAMPLE)
    private String unitZ;

    @NotEmpty
    @ApiModelProperty(
            value = Constants.SWAGGER_TRJ_RSP_UNIT_DLS, required = true, dataType = "String",
            example = Constants.SWAGGER_TRJ_RSP_UNIT_DLS_EXAMPLE)
    private String unitDls;

    @Valid
    @NotEmpty
    @ApiModelProperty(value = Constants.SWAGGER_TRJ_RSP_LIST_OF_STATIONS, required = true)
    private List<TrajectoryStationOut> stations;

    @Valid
    @NotEmpty
    @ApiModelProperty(value = Constants.SWAGGER_TRJ_RSP_LOCAL_CRS, required = true, dataType = "String",
            example = Constants.SWAGGER_TARGET_CRS_EXAMPLE)
    private String localCRS;

    @NotEmpty
    @ApiModelProperty(
            value = Constants.SWAGGER_TRJ_RSP_METHOD, required = true, dataType = "String",
            example = Constants.SWAGGER_TRJ_REQ_METHOD_EXAMPLE)
    private String method;

    @JsonProperty("inputKind")
    @ApiModelProperty(
            value = Constants.SWAGGER_TRJ_REQ_INPUT_KIND, dataType = "String",
            example = Constants.SWAGGER_TRJ_REQ_INPUT_KIND_EXAMPLE)
    private String inputKind;

    @ApiModelProperty(value = Constants.SWAGGER_CONVERT_AUDIT)
    private List<String> operationsApplied;
}
