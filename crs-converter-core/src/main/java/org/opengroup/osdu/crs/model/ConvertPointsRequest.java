package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.util.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.Valid;
import java.util.List;

@Data
@ApiModel(description = Constants.SWAGGER_CONVERT_REQUEST_DESCR)
public class ConvertPointsRequest {
	@NotEmpty
	@ApiModelProperty(value = Constants.SWAGGER_SOURCE_CRS, required = true, dataType = "String",
					  example = Constants.SWAGGER_SOURCE_CRS_EXAMPLE)
	private String fromCRS;

	@NotEmpty
	@ApiModelProperty(value = Constants.SWAGGER_TARGET_CRS, required = true, dataType = "String",
					  example = Constants.SWAGGER_CONVERT_TARGET_CRS_EXAMPLE)
	private String toCRS;

	@Valid
	@NotEmpty
	@ApiModelProperty(value = Constants.SWAGGER_LIST_OF_POINTS_TO_BE_CONVERTED, required = true,
					  example = Constants.SWAGGER_LIST_OF_POINTS_TO_BE_CONVERTED_EXAMPLE)
	private List<Point> points;
}
