package org.opengroup.osdu.crs.api;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.crs.BinGrid.AbstractBinGrid;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.IPointConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.model.ConvertBinGridRequest;
import org.opengroup.osdu.crs.model.ConvertBinGridResponse;
import org.opengroup.osdu.crs.model.ConvertGeoJsonRequest;
import org.opengroup.osdu.crs.model.ConvertGeoJsonResponse;
import org.opengroup.osdu.crs.model.ConvertPointsRequest;
import org.opengroup.osdu.crs.model.ConvertPointsResponse;
import org.opengroup.osdu.crs.model.ConvertTrajectoryRequest;
import org.opengroup.osdu.crs.model.ConvertTrajectoryResponse;
import org.opengroup.osdu.crs.model.ErrorResponse;
import org.opengroup.osdu.crs.model.Point;
import org.opengroup.osdu.crs.osducoreserviceclient.storage.IStorageClient;
import org.opengroup.osdu.crs.util.Constants;
import org.opengroup.osdu.crs.util.RecordCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
@Api(value = Constants.SWAGGER_TAG_CRS_CONVERSION)
@CrossOrigin
@RestController
@RequestMapping("/v3")
public class CrsConverterApiV3 {

	@Autowired
	private JaxRsDpsLog logger;
    
	@Autowired
    private IStorageClient StorageClient;

	private final ICRSConverter crsConverter;
	private final ITrajectoryConverter crsTrajectoryConverter;
	private final IPointConverter pointConverter;
    private RecordCache recordCache;
 
	public CrsConverterApiV3(@NonNull ICRSConverter crsConverter,
				  @NonNull ITrajectoryConverter crsTrajectoryConverter,
				  @NonNull IPointConverter pointConverter) {
		this.crsConverter = crsConverter;
		this.crsTrajectoryConverter = crsTrajectoryConverter;
		this.pointConverter = pointConverter;
		this.recordCache = new RecordCache();
	}
	// parameter str could be a persistableReference string or recordID. mustID means parameter str must be a recordID
	private String getPersistableReferenceFromID(String str, boolean mustID){
		String temp;
		try {
			temp = URLDecoder.decode(str, "UTF-8"); 
		} catch (Exception e) {
			return str; // try our best to return user input
		}
		// persistableReference string starts with {....}
	    if (temp.startsWith("{") && mustID == false){
			return str;
		}
	
		// set temp as str as we don't want to decode. for example UnitOfMeasure:ft%5BUS%5D in record id
		temp = str;	
		// check the cache for recordID with its persistableReference string
        String pr = recordCache.get(temp);
        if (pr != null){
			return pr; 
		}
		// temp should have record:version format. change last : to be / for storage API call
		temp = temp.substring(0, temp.lastIndexOf(":")) + "/" + temp.substring(temp.lastIndexOf(":") + 1);
		Record record =  StorageClient.getRecord(temp);
		if (record == null)
			throw new ValidationException(String.join(" ", "record not found:", temp));
        pr = record.getData().get("PersistableReference").toString();
		if(pr != null){
			recordCache.put(temp, pr);
			return pr;
		} else {
			throw new ValidationException(String.join(" ", "record does not have PersistableReference:", temp));
		}
    }

	@PostMapping("/convert")
	@ApiOperation(value = Constants.SWAGGER_CONVERT_TITLE, notes = Constants.SWAGGER_CONVERT_NOTES, tags = {Constants.SWAGGER_TAG_CRS_CONVERSION})
	@ApiResponses({
			@ApiResponse(code = 200, message = Constants.SWAGGER_CONVERT_SUCCESS_RESPONSE, response = ConvertPointsResponse.class),
			@ApiResponse(code = 400, message = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH, response = ErrorResponse.class),
			@ApiResponse(code = 500, message = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR, response = ErrorResponse.class),
			@ApiResponse(code = 503, message = Constants.SWAGGER_CONVERT_OVERLOAD, response = ErrorResponse.class)})
	public ConvertPointsResponse convertPoint(@NonNull @Valid @RequestBody ConvertPointsRequest request) {
		String fromCrs = getPersistableReferenceFromID(request.getFromCRS(), false);
		String toCrs = getPersistableReferenceFromID(request.getToCRS(), false);
		double[] xyCoordinates = this.pointConverter.mergeXYCoordinates(request.getPoints());
		double[] zCoordinates = this.pointConverter.mergeZCoordinates(request.getPoints());
		ConvertPointsResponse response = this.crsConverter.convertPoint(fromCrs, toCrs, xyCoordinates,
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
		GeoJsonFeatureCollection features = request.getFeatureCollection();
		String toCrs = getPersistableReferenceFromID(request.getToCRS(), false);
		String toUnitZ = getPersistableReferenceFromID(request.getToUnitZ(), false);

		// The CRS reference as persistableReference string. If populated, the CoordinateReferenceSystemID takes precedence
		if( features.getCoordinateReferenceSystemID() != null){
			String temp = getPersistableReferenceFromID(features.getCoordinateReferenceSystemID(), true);
			if(temp != null)
				features.setPersistableReferenceCrs(temp);
			features.setCoordinateReferenceSystemID(null);
		}
		// The VerticalUnitID definition overrides any self-contained definition in persistableReferenceUnitZ.
		if( features.getVerticalUnitID() != null){
			String temp = getPersistableReferenceFromID(features.getVerticalUnitID(), true);
			if(temp != null)
				features.setPersistableReferenceUnitZ(temp);
			features.setVerticalUnitID(null);
		}

		ConvertGeoJsonResponse response = this.crsConverter.convertGeoJson(
				features,
				toCrs,
				toUnitZ);
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
		request.setTrajectoryCRS(getPersistableReferenceFromID(request.getTrajectoryCRS(), false));
		request.setUnitXY(getPersistableReferenceFromID(request.getUnitXY(), false));
		request.setUnitZ(getPersistableReferenceFromID(request.getUnitZ(), false));
		ConvertTrajectoryResponse response = this.crsTrajectoryConverter.convertTrajectory(dpsHeaders, request);
		return response;
	}
	
	@PostMapping("/convertBinGrid")
	@ApiOperation(value = Constants.SWAGGER_BIN_GRID_CONVERT_TITLE, notes = Constants.SWAGGER_BIN_GRID_CONVERT_NOTES, tags = {
			Constants.SWAGGER_TAG_CRS_CONVERSION })
	@ApiResponses({
			@ApiResponse(code = 200, message = Constants.SWAGGER_BIN_GRID_CONVERSION_RESPONSE, response = ConvertTrajectoryResponse.class),
			@ApiResponse(code = 400, message = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH, response = ErrorResponse.class),
			@ApiResponse(code = 500, message = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR, response = ErrorResponse.class),
			@ApiResponse(code = 503, message = Constants.SWAGGER_CONVERT_OVERLOAD, response = ErrorResponse.class) })
	public ConvertBinGridResponse convertBinGrid(
			@ApiParam(hidden = true) @NonNull @Valid @RequestBody ConvertBinGridRequest request) {

		ConvertBinGridResponse convertBinGridResponse = new ConvertBinGridResponse();
		ConvertPointsRequest convertPointsRequest = new ConvertPointsRequest();		
		try {		
			AbstractBinGrid inBinGrid = request.getInBinGrid();
			List<String> operationsApplied = new ArrayList<>();
			convertPointsRequest.setToCRS(getPersistableReferenceFromID(request.getToCRS(), true));
			convertPointsRequest.setFromCRS(getPersistableReferenceFromID(inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
					.getCoordinateReferenceSystemID(),true));
			if (!StringUtils.isEmpty(request.getToCRS())) {
				request.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().stream()
						.forEach(feature -> {
							Point point = new Point();
							point.setX(feature.getGeometry().getCoordinates().get(0));
							point.setY(feature.getGeometry().getCoordinates().get(1));
							point.setZ(0.0);
							convertPointsRequest.setPoints(Arrays.asList(point));
							ConvertPointsResponse response = convertPoint(convertPointsRequest);
							/*
							 * String str_response = osduRestHelper.testAPI(convertPointsRequest);
							 * ConvertPointsResponse response = new Gson().fromJson(str_response,
							 * ConvertPointsResponse.class);
							 */							
							feature.getGeometry().setCoordinates(Arrays.asList(response.getPoints().get(0).getX(),
									response.getPoints().get(0).getY()));
							operationsApplied.addAll(response.getOperationsApplied());
						});
				convertBinGridResponse.setAppliedOperations(Arrays.asList(operationsApplied.get(0)));
			}

			if (StringUtils.isNotEmpty(inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
					.getCoordinateReferenceSystemID())) {
				String fromCRS = getPersistableReferenceFromID(inBinGrid.getABCDBinGridSpatialLocation()
						.getAsIngestedcoordinates().getCoordinateReferenceSystemID(), true);
				if (fromCRS != null)
					inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
							.setPersistableReferenceCrs(fromCRS);
				inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
						.setCoordinateReferenceSystemID(null);
			}
			String toCrs = getPersistableReferenceFromID(request.getToCRS(), true);
			convertBinGridResponse = this.crsConverter.convertBinGrid(toCrs, inBinGrid, convertBinGridResponse);
			return convertBinGridResponse;
		} catch (IllegalArgumentException illegalException) {
			logger.error("Exception from the convert call " + illegalException.getMessage());
			throw new ValidationException(illegalException.getMessage());
		}
	}
}
