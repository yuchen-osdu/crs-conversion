package org.opengroup.osdu.crs.api;

import com.google.common.base.Strings;
import io.swagger.annotations.*;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.IPointConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.model.ConvertTrajectoryResponse;
import org.opengroup.osdu.crs.model.ErrorResponse;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryRequestV4;
import org.opengroup.osdu.crs.model.v4.MinimumDepthInterval;
import org.opengroup.osdu.crs.osducoreserviceclient.storage.IStorageClient;
import org.opengroup.osdu.crs.util.Constants;
import org.opengroup.osdu.crs.util.RecordCache;
import org.springframework.beans.factory.annotation.Autowired;
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
    private IStorageClient StorageClient;
    private final ICRSConverter crsConverter;
    private final ITrajectoryConverter crsTrajectoryConverter;
    private final IPointConverter pointConverter;
    private RecordCache recordCache;
    private static final String BOUND_PROJECTED = "BoundProjected";
    private static final String PROJECTED = "Projected";

    public CrsConverterApiV4(@NonNull ICRSConverter crsConverter,
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

    private String getUnitFromTrajectoryCRS(String trajectoryCRS)  {
        String temp;
        try {
            temp = URLDecoder.decode(trajectoryCRS, "UTF-8");
        } catch (Exception e) {
            return trajectoryCRS; // try our best to return user input
        }
        temp = temp.substring(0, temp.lastIndexOf(":")) + "/" + temp.substring(temp.lastIndexOf(":") + 1);
        Record record =  StorageClient.getRecord(temp);
        if (record == null)
            throw new ValidationException(String.join(" ", "record not found:", temp));
        Map<String,Object> data = record.getData();
        Map<String,Object> coordinateSystem = (Map<String, Object>) data.get("CoordinateSystem");
        String horizontalAxisUnitID = (String) coordinateSystem.get("HorizontalAxisUnitID");
        return horizontalAxisUnitID;
    }

    private boolean checkCRSType(String trajectoryCRS) {
        boolean flag = false;
        String temp;
        try {
            temp = URLDecoder.decode(trajectoryCRS, "UTF-8");
        } catch (Exception e) {
            return false; // try our best to return user input
        }
        temp = temp.substring(0, temp.lastIndexOf(":")) + "/" + temp.substring(temp.lastIndexOf(":") + 1);
        Record record = StorageClient.getRecord(temp);
        if (record == null)
            throw new ValidationException(String.join(" ", "record not found:", temp));
        Map<String, Object> data = record.getData();
        if (data!=null && (data.get("Kind").toString().equalsIgnoreCase(BOUND_PROJECTED) || data.get("Kind").toString().equalsIgnoreCase(PROJECTED))) {
            flag = true;
        }
        return flag;
    }

    @PostMapping("/convertTrajectory")
    @ApiOperation(value = Constants.SWAGGER_TRJ_CONVERT_TITLE, notes = Constants.SWAGGER_TRJ_CONVERT_NOTES, tags = {Constants.SWAGGER_TAG_TRJ_CONVERSION})
    @ApiResponses({
            @ApiResponse(code = 200, message = Constants.SWAGGER_TRJ_CONVERT_SUCCESS_RESPONSE, response = ConvertTrajectoryResponse.class),
            @ApiResponse(code = 400, message = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH, response = ErrorResponse.class),
            @ApiResponse(code = 500, message = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 503, message = Constants.SWAGGER_CONVERT_OVERLOAD, response = ErrorResponse.class)})
    public ConvertTrajectoryResponse convertTrajectory(@ApiParam(hidden = true) @RequestHeader MultiValueMap<String, String> headers,
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

        if(minimumDepthInterval!=null && minimumDepthInterval.getMd_interval()!=null && minimumDepthInterval.getMd_interval()>0 && minimumDepthInterval.getMd_i()!=null && minimumDepthInterval.getMd_i().size()>0 ){
            throw new ValidationException("Both md_i array and md_interval values are provided in the input.");
        }else if(minimumDepthInterval!=null && minimumDepthInterval.getMd_interval()!=null && minimumDepthInterval.getMd_interval()>0){
            List<Double> mdiList = computeMinimumDepthPointsUsingInterval(request.getInputStations().get(0).getMd(),
                    request.getInputStations().get(request.getInputStations().size()-1).getMd(),minimumDepthInterval.getMd_interval());
            mdiList.add(request.getInputStations().get(request.getInputStations().size()-1).getMd());
            minimumDepthInterval.setMd_i(mdiList);
        }
        ConvertTrajectoryResponse response = this.crsTrajectoryConverter.convertTrajectoryV4(dpsHeaders, request,checkCRSType);
        return response;
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
