package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.model.Point;
import org.opengroup.osdu.crs.util.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = Constants.SWAGGER_TRJ_STN_OUT_DESCRIPTION)
public class TrajectoryStationOut {
    @ApiModelProperty(
            value = Constants.SWAGGER_MD, required = true, dataType = "Double",
            example = Constants.SWAGGER_MD_EXAMPLE)
    private Double md;

    @ApiModelProperty(
            value = Constants.SWAGGER_INC, required = true, dataType = "Double",
            example = Constants.SWAGGER_INC_EXAMPLE)
    private Double inclination;

    @ApiModelProperty(
            value = Constants.SWAGGER_AZI_TN, required = true, dataType = "Double",
            example = Constants.SWAGGER_AZI_TN_EXAMPLE)
    private Double azimuthTN;

    @ApiModelProperty(
            value = Constants.SWAGGER_AZI_GN, required = true, dataType = "Double",
            example = Constants.SWAGGER_AZI_GN_EXAMPLE)
    private Double azimuthGN;

    @ApiModelProperty(
            value = Constants.SWAGGER_DX_TN, required = true, dataType = "Double",
            example = Constants.SWAGGER_DX_TN_EXAMPLE)
    private Double dxTN;

    @ApiModelProperty(
            value = Constants.SWAGGER_DY_TN, required = true, dataType = "Double",
            example = Constants.SWAGGER_DY_TN_EXAMPLE)
    private Double dyTN;

    @ApiModelProperty(
            value = Constants.SWAGGER_DZ, required = true, dataType = "Double",
            example = Constants.SWAGGER_DZ_EXAMPLE)
    private Double dZ;

    @ApiModelProperty(
            value = Constants.SWAGGER_TRJ_POINT, required = true)
    private Point point;

    @ApiModelProperty(
            value = Constants.SWAGGER_TRJ_WGS84_LONGITUDE, dataType = "Double")
    double wgs84Longitude;

    @ApiModelProperty(
            value = Constants.SWAGGER_TRJ_WGS84_LATITUDE, dataType = "Double")
    double wgs84Latitude;

    @ApiModelProperty(
            value = Constants.SWAGGER_TRJ_RSP_DLS, dataType = "Double")
    double dls;

    @ApiModelProperty(
            value = Constants.SWAGGER_TRJ_RSP_ORIGINAL, dataType = "Boolean"
    )
    private boolean original;
}
