package org.opengroup.osdu.crs.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Response resulting of a failed CRS conversion operation")
public class ErrorResponse {

	@ApiModelProperty(value = "Error message", dataType = "String")
	private String error;
}
