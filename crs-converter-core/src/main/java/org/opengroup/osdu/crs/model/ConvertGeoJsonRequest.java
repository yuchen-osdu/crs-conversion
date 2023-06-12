package org.opengroup.osdu.crs.model;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.util.Constants;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Schema(description = Constants.SWAGGER_GEO_JSON_CONVERT_REQUEST_DESCR)
public class ConvertGeoJsonRequest {
	@Schema(description = Constants.SWAGGER_GEO_JSON_FEATURE_COLLECTION)
	@Parameter(required = true)
	private GeoJsonFeatureCollection featureCollection;

	@NotEmpty
	@Schema(description = Constants.SWAGGER_TARGET_CRS, type = "string",example = Constants.SWAGGER_TARGET_CRS_GEO_EXAMPLE)
	@Parameter(required = true)
	private String toCRS;

	@Schema(description = Constants.SWAGGER_TARGET_Z_UNIT, type = "string",example = Constants.SWAGGER_TARGET_Z_UNIT_EXAMPLE)
	@Parameter(required = true)
	private String toUnitZ;
}
