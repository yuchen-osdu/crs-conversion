package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.util.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = Constants.SWAGGER_CONVERSION_RESPONSE)
public class ConvertPointsResponse {

	@ApiModelProperty(value = Constants.SWAGGER_NOF_POINTS_CONVERTED, dataType = "Integer")
	private Integer successCount;

	@ApiModelProperty(value = Constants.SWAGGER_CONVERTED_POINTS)
	private List<Point> points;

	@ApiModelProperty(value = Constants.SWAGGER_CONVERT_AUDIT)
	private List<String> operationsApplied;
}
