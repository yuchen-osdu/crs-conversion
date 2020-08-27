package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.util.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = Constants.SWAGGER_GEO_JSON_CONVERSION_RESPONSE)
public class ConvertGeoJsonResponse {

	@ApiModelProperty(value = Constants.SWAGGER_GEO_JSON_SUCCESS_COUNT, dataType = "Integer")
	private Integer successCount;

	@ApiModelProperty(value = Constants.SWAGGER_GEO_JSON_COORDINATE_COUNT, dataType = "Integer")
	private Integer totalCount;

	@ApiModelProperty(value = Constants.SWAGGER_GEO_JSON_CONVERTED)
	private GeoJsonFeatureCollection featureCollection;

	@ApiModelProperty(value = Constants.SWAGGER_CONVERT_AUDIT)
	private List<String> operationsApplied;
}