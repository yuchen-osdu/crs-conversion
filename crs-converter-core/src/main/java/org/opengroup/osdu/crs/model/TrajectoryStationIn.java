package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.util.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@ApiModel(description = Constants.SWAGGER_TRJ_STN_IN_DESCRIPTION)
public class TrajectoryStationIn {
	public TrajectoryStationIn(){
		// set the optional values to NaN
		dx = Double.NaN;
		dy = Double.NaN;
		dz = Double.NaN;
	}

	@NotNull
	@ApiModelProperty(
			value = Constants.SWAGGER_MD, required = true, dataType = "Double",
			example = Constants.SWAGGER_MD_EXAMPLE)
	private Double md;

	@NotNull
	@ApiModelProperty(
			value = Constants.SWAGGER_INC, required = true, dataType = "Double",
			example = Constants.SWAGGER_INC_EXAMPLE)
	private Double inclination;

	@NotNull
	@ApiModelProperty(
			value = Constants.SWAGGER_AZI, required = true, dataType = "Double",
			example = Constants.SWAGGER_AZI_EXAMPLE)
	private Double azimuth;

	@ApiModelProperty(
			value = Constants.SWAGGER_DX, dataType = "Double",
			example = Constants.SWAGGER_DX_EXAMPLE)
	private Double dx;

	@ApiModelProperty(
			value = Constants.SWAGGER_DY, dataType = "Double",
			example = Constants.SWAGGER_DY_EXAMPLE)
	private Double dy;

	@ApiModelProperty(
			value = Constants.SWAGGER_DZ, dataType = "Double",
			example = Constants.SWAGGER_DZ_EXAMPLE)
	private Double dz;
}
