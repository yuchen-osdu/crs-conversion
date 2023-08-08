package org.opengroup.osdu.crs.api;

import com.google.common.base.Strings;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.model.ConvertPointsResponse;
import org.opengroup.osdu.crs.model.ErrorResponse;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryRequestV4;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryResponseV4;
import org.opengroup.osdu.crs.model.v4.MinimumDepthInterval;
import org.opengroup.osdu.crs.osducoreserviceclient.storage.IStorageClient;
import org.opengroup.osdu.crs.util.Constants;
import org.opengroup.osdu.crs.util.RecordCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Api(value = Constants.SWAGGER_TAG_CRS_CONVERSION)
@CrossOrigin
@RestController
@RequestMapping("/v4")
public class CrsConverterApiV4 {
    @Autowired
    private JaxRsDpsLog logger;
    @Autowired
    private IStorageClient storageClient;

    private final ITrajectoryConverter crsTrajectoryConverter;

    private RecordCache recordCache;
    private static final String BOUND_PROJECTED = "BoundProjected";
    private static final String PROJECTED = "Projected";

    private static final String UTF_8 ="UTF-8";
    private  static final String RECORD_NOT_FOUND= "record not found:";
    public CrsConverterApiV4(@NonNull ITrajectoryConverter crsTrajectoryConverter) {
        this.crsTrajectoryConverter = crsTrajectoryConverter;
        this.recordCache = new RecordCache();
    }
    // parameter str could be a persistableReference string or recordID. mustID means parameter str must be a recordID
    private String getPersistableReferenceFromID(String str, boolean mustID){
        String temp;
        try {
            temp = URLDecoder.decode(str, UTF_8);
        } catch (Exception e) {
            return str; // try our best to return user input
        }
        // persistableReference string starts with "{..}"
        if (temp.startsWith("{") && mustID){
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
        Record dataRecord =  storageClient.getRecord(temp);
        if (dataRecord == null)
            throw new ValidationException(String.join(" ", RECORD_NOT_FOUND, temp));
        pr = dataRecord.getData().get("PersistableReference").toString();
        if(pr != null){
            recordCache.put(temp, pr);
            return pr;
        } else {
            throw new ValidationException(String.join(" ", "record does not have PersistableReference:", temp));
        }
    }

    private String getUnitFromTrajectoryCRS(String trajectoryCRS)  {
        String temp;
        try {
            temp = URLDecoder.decode(trajectoryCRS, UTF_8);
        } catch (Exception e) {
            return trajectoryCRS; // try our best to return user input
        }
        temp = temp.substring(0, temp.lastIndexOf(":")) + "/" + temp.substring(temp.lastIndexOf(":") + 1);
        Record dataRecord =  storageClient.getRecord(temp);
        if (dataRecord == null)
            throw new ValidationException(String.join(" ", RECORD_NOT_FOUND, temp));
        Map<String,Object> data = dataRecord.getData();
        Map<String,Object> coordinateSystem = (Map<String, Object>) data.get("CoordinateSystem");
        return (String) coordinateSystem.get("HorizontalAxisUnitID");
    }

    private boolean checkCRSType(String trajectoryCRS) {
        boolean flag = false;
        String temp;
        try {
            temp = URLDecoder.decode(trajectoryCRS, UTF_8);
        } catch (Exception e) {
            return false; // try our best to return user input
        }
        temp = temp.substring(0, temp.lastIndexOf(":")) + "/" + temp.substring(temp.lastIndexOf(":") + 1);
        Record dataRecord = storageClient.getRecord(temp);
        if (dataRecord == null)
            throw new ValidationException(String.join(" ", RECORD_NOT_FOUND, temp));
        Map<String, Object> data = dataRecord.getData();
        if (data!=null && (data.get("Kind").toString().equalsIgnoreCase(BOUND_PROJECTED) || data.get("Kind").toString().equalsIgnoreCase(PROJECTED))) {
            flag = true;
        }
        return flag;
    }

    @PostMapping(value = "/convertTrajectory", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "${CrsConverterApiV4.convertPoint.summary}", description = "${CrsConverterApiV4.convertPoint.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"crs-converter-api-v4"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SWAGGER_TRJ_CONVERT_SUCCESS_RESPONSE, content = { @Content(schema = @Schema(implementation = ConvertTrajectoryResponseV4.class)) }),
            @ApiResponse(responseCode = "400", description = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = Constants.SWAGGER_CONVERT_OVERLOAD,  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    public ConvertTrajectoryResponseV4 convertTrajectory(@ApiParam(hidden = true) @RequestHeader MultiValueMap<String, String> headers,
                                                       @NonNull @Valid @RequestBody ConvertTrajectoryRequestV4 request) {
        String message = String.format("Using trajectory: %s", "no");
        logger.info(message);
        DpsHeaders dpsHeaders = DpsHeaders.createFromEntrySet(headers.entrySet());
        if(request.getTrajectoryCRS().contains(BOUND_PROJECTED) || request.getTrajectoryCRS().contains(PROJECTED)){
            if(!Strings.isNullOrEmpty(request.getUnitXY())) {
                throw new ValidationException("unitXY should not be provided for BoundProjected and Projected CRS.");
            }
            String unit = getUnitFromTrajectoryCRS(request.getTrajectoryCRS());
            request.setUnitXY(getPersistableReferenceFromID(unit, false));
        }else
            request.setUnitXY(getPersistableReferenceFromID(request.getUnitXY(), false));
        Boolean checkCRSType = checkCRSType(request.getTrajectoryCRS());
        request.setTrajectoryCRS(getPersistableReferenceFromID(request.getTrajectoryCRS(), false));
        request.setUnitZ(getPersistableReferenceFromID(request.getUnitZ(), false));

        MinimumDepthInterval minimumDepthInterval = request.getMD_i();
        if (minimumDepthInterval != null) {
            if (minimumDepthInterval.getMd_interval() != null && minimumDepthInterval.getMd_interval() > 0 && minimumDepthInterval.getMd_i() != null && !minimumDepthInterval.getMd_i().isEmpty()) {
            throw new ValidationException("Both md_i array and md_interval values are provided in the input.");
            } else if (minimumDepthInterval.getMd_interval() != null && minimumDepthInterval.getMd_interval() > 0) {
            List<Double> mdiList = computeMinimumDepthPointsUsingInterval(request.getInputStations().get(0).getMd(),
                    request.getInputStations().get(request.getInputStations().size()-1).getMd(),minimumDepthInterval.getMd_interval());
            mdiList.add(request.getInputStations().get(request.getInputStations().size()-1).getMd());
            minimumDepthInterval.setMd_i(mdiList);
            } else if (minimumDepthInterval.getMd_i() != null && !minimumDepthInterval.getMd_i().isEmpty() && checkMdiListForRange(request.getInputStations().get(0).getMd(), request.getInputStations().get(request.getInputStations().size() - 1).getMd(),
                    minimumDepthInterval.getMd_i())){
                throw new ValidationException("md_i array values provided are not in range of MD stations.");
            }
        }
        return this.crsTrajectoryConverter.convertTrajectoryV4(dpsHeaders, request,checkCRSType,true);
    }

    private boolean checkMdiListForRange(Double firstMd,Double lastMd,List<Double> mdiList){
        boolean checkRange = false;
        for(int count=0;count<mdiList.size();count++){
            if(mdiList.get(count)<firstMd || mdiList.get(count)>lastMd){
                checkRange = true;
                break;
            }
        }
        return checkRange;
    }

    private List<Double> computeMinimumDepthPointsUsingInterval(Double firstMd,Double lastMd,Double mdInterval){
            List<Double> mdiList = new ArrayList<>();
            while(lastMd > firstMd && lastMd > mdInterval){
                mdiList.add(firstMd);
                firstMd+=mdInterval;
            }
         return mdiList;
    }

}
