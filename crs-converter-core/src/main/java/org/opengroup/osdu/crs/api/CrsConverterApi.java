package org.opengroup.osdu.crs.api;

import io.swagger.annotations.*;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.IPointConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.model.*;
import org.opengroup.osdu.crs.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(value = Constants.SWAGGER_TAG_CRS_CONVERSION)
@CrossOrigin
@RestController
@RequestMapping("/v2")
public class CrsConverterApi {

	@Autowired
	private JaxRsDpsLog logger;

	private final ICRSConverter crsConverter;
	private final ITrajectoryConverter crsTrajectoryConverter;
	private final IPointConverter pointConverter;

	public CrsConverterApi(@NonNull ICRSConverter crsConverter,
				  @NonNull ITrajectoryConverter crsTrajectoryConverter,
				  @NonNull IPointConverter pointConverter) {
		this.crsConverter = crsConverter;
		this.crsTrajectoryConverter = crsTrajectoryConverter;
		this.pointConverter = pointConverter;
	}

	@PostMapping("/convert")
	@ApiOperation(value = Constants.SWAGGER_CONVERT_TITLE, notes = Constants.SWAGGER_CONVERT_NOTES, tags = {Constants.SWAGGER_TAG_CRS_CONVERSION})
	@ApiResponses({
			@ApiResponse(code = 200, message = Constants.SWAGGER_CONVERT_SUCCESS_RESPONSE, response = ConvertPointsResponse.class),
			@ApiResponse(code = 400, message = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH, response = ErrorResponse.class),
			@ApiResponse(code = 500, message = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR, response = ErrorResponse.class),
			@ApiResponse(code = 503, message = Constants.SWAGGER_CONVERT_OVERLOAD, response = ErrorResponse.class)})
	public ConvertPointsResponse convertPoint(@NonNull @Valid @RequestBody ConvertPointsRequest request) {
		double[] xyCoordinates = this.pointConverter.mergeXYCoordinates(request.getPoints());
		double[] zCoordinates = this.pointConverter.mergeZCoordinates(request.getPoints());
		ConvertPointsResponse response = this.crsConverter.convertPoint(request.getFromCRS(), request.getToCRS(), xyCoordinates,
				zCoordinates);
		response.setPoints(this.pointConverter.convertValuesToPoints(xyCoordinates, zCoordinates));
		return response;
	}

	@PostMapping("/convertGeoJson")
	@ApiOperation(value = Constants.SWAGGER_GEO_JSON_CONVERT_TITLE, notes = Constants.SWAGGER_GEO_JSON_CONVERT_NOTES, tags = {Constants.SWAGGER_TAG_CRS_CONVERSION})
	@ApiResponses({
			@ApiResponse(code = 200, message = Constants.SWAGGER_CONVERT_SUCCESS_RESPONSE, response = ConvertGeoJsonResponse.class),
			@ApiResponse(code = 400, message = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH, response = ErrorResponse.class),
			@ApiResponse(code = 500, message = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR, response = ErrorResponse.class),
			@ApiResponse(code = 503, message = Constants.SWAGGER_CONVERT_OVERLOAD, response = ErrorResponse.class)})
	public ConvertGeoJsonResponse convertGeoJson(@NonNull @Valid @RequestBody ConvertGeoJsonRequest request) {
		ConvertGeoJsonResponse response = this.crsConverter.convertGeoJson(
				request.getFeatureCollection(),
				request.getToCRS(),
				request.getToUnitZ());
		return response;
	}

	@PostMapping("/convertTrajectory")
	@ApiOperation(value = Constants.SWAGGER_TRJ_CONVERT_TITLE, notes = Constants.SWAGGER_TRJ_CONVERT_NOTES, tags = {Constants.SWAGGER_TAG_TRJ_CONVERSION})
	@ApiResponses({
			@ApiResponse(code = 200, message = Constants.SWAGGER_TRJ_CONVERT_SUCCESS_RESPONSE, response = ConvertTrajectoryResponse.class),
			@ApiResponse(code = 400, message = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH, response = ErrorResponse.class),
			@ApiResponse(code = 500, message = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR, response = ErrorResponse.class),
			@ApiResponse(code = 503, message = Constants.SWAGGER_CONVERT_OVERLOAD, response = ErrorResponse.class)})
	public ConvertTrajectoryResponse convertTrajectory(@ApiParam(hidden = true) @RequestHeader MultiValueMap<String, String> headers,
													   @NonNull @Valid @RequestBody ConvertTrajectoryRequest request) {
		String message = String.format("Using trajectory: %s", "no");
		logger.info(message);
		DpsHeaders dpsHeaders = DpsHeaders.createFromEntrySet(headers.entrySet());
		ConvertTrajectoryResponse response = this.crsTrajectoryConverter.convertTrajectory(dpsHeaders, request);
		return response;
	}
}
