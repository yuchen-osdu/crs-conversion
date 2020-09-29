package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.util.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel(description = Constants.SWAGGER_GEO_JSON_CONVERT_REQUEST_DESCR)
public class ConvertGeoJsonRequest {
	@ApiModelProperty(value = Constants.SWAGGER_GEO_JSON_FEATURE_COLLECTION, required = true)
	private GeoJsonFeatureCollection featureCollection;

	@NotEmpty
	@ApiModelProperty(value = Constants.SWAGGER_TARGET_CRS, required = true, dataType = "String",
			example = Constants.SWAGGER_TARGET_CRS_GEO_EXAMPLE)
	private String toCRS;

	@ApiModelProperty(value = Constants.SWAGGER_TARGET_Z_UNIT, required = true, dataType = "String",
			example = Constants.SWAGGER_TARGET_Z_UNIT_EXAMPLE)
	private String toUnitZ;
}
