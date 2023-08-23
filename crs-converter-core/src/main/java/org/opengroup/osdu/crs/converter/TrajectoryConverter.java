package org.opengroup.osdu.crs.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeodeticCRS;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.crs.api.exception.BadRequestException;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.model.*;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryRequestV4;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryResponseV4;
import org.opengroup.osdu.crs.model.v4.MinimumDepthInterval;
import org.opengroup.osdu.crs.model.v4.TrajectoryStationInV4;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.SisTransformations;
import org.opengroup.osdu.crs.util.Constants;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.opengroup.osdu.crs.model.ReferenceConverter.parseSpatialReference;
import static org.opengroup.osdu.crs.model.ReferenceConverter.parseUnitReference;

@Service
public class TrajectoryConverter implements ITrajectoryConverter {

    private static final String METER = "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}";
    private static final String DEGPFT = "{\"scaleOffset\":{\"scale\":5.72614583987641E-4,\"offset\":0.0},\"symbol\":\"deg/100ft\",\"baseMeasurement\":{\"ancestry\":\"Rotation_Per_Length\",\"type\":\"UM\"},\"type\":\"USO\"}";
    private static final String DEGPM = "{\"scaleOffset\":{\"scale\":5.81776417331443E-4,\"offset\":0.0},\"symbol\":\"deg/30m\",\"baseMeasurement\":{\"ancestry\":\"Rotation_Per_Length\",\"type\":\"UM\"},\"type\":\"USO\"}";
    private static final double EPS = 1.0e-10;
    private static final int ESRI_PRJ_ERROR = 0;
    private static final int ESRI_DATUM_ERROR = 1;
    private static final double DEG2RAD = Math.PI / 180.0;
    private static final double RAD2DEG = 180.0 / Math.PI;

    public TrajectoryConverter() {
    }

    @Override
    public ConvertTrajectoryResponse convertTrajectory(DpsHeaders headers, ConvertTrajectoryRequest request) {
        TrajectoryComputationState state = new TrajectoryComputationState();
        state.setDpsHeaders(headers);
        ConvertTrajectoryResponse response = new ConvertTrajectoryResponse();
        response.setTrajectoryCRS(request.getTrajectoryCRS());
        response.setUnitXY(request.getUnitXY());
        response.setUnitZ(request.getUnitZ());
        response.setMethod(request.getMethod());
        response.setInputKind(request.getInputKind());
        if (isRequestValid(request, state)) {
            double gridConvergence;
            double to_gn;
            double to_tn;
            AzimuthCorrector azimuthCorrector = new AzimuthCorrector();
            response.setStations(populateResponseFromRequest(request));
            ProjectionCorrectionSet correctionSet
                    = azimuthCorrector.createProjectionCorrectionSet(
                            state.getSourceCRSAsPersistableReference(), request.getReferencePoint(), state.getHorizontalUnit());
            gridConvergence = correctionSet.getConvergenceAngleInDeg();
                if (state.getAzimuthReference() == AzimuthReferenceType.GRID_NORTH) { // correct GN to TN first
                    to_tn = gridConvergence;
                    to_gn = 0.0;
                    state.getOperations().add(String.format("derived TN from GN azimuth by grid convergence %f", (to_tn + 360) % 360.0));
                } else {
                    to_tn = 0.0;
                    to_gn = -gridConvergence;
                    state.getOperations().add(String.format("derived GN from TN azimuth by grid convergence %f", (to_gn + 360) % 360.0));
                }

                for (TrajectoryStationOut to : response.getStations()) {
                    double tn = (to.getAzimuthTN() + to_tn + 360) % 360.0;
                    double gn = (to.getAzimuthGN() + to_gn + 360) % 360.0;
                    to.setAzimuthTN(tn);
                    to.setAzimuthGN(gn);
                    to.setDls(Double.NaN);
                }

            ConvertTrajectoryResponse siResponse = normalizeTrajectory(response, state);
            Point referencePoint = new Point(0.0, 0.0, siResponse.getStations().get(0).getPoint().getZ());
            if (callTrajectoryEngineService(siResponse, referencePoint, state)) {
                deNormalizeTrajectory(siResponse, response, state);
                state.getOperations().add(String.format("computation method: %s", state.getMethod().toString()));
                if (state.getMethod() == TrajectoryComputationMethod.LeesModifiedProposal) {
                    // convertPoints(points, correctionSet.getPeAzimuthalEquidistantCRS(), state);
                    convertPointsLmp(response, state);
                } else {
                    // convert from local azimuthal equidistant CRS to requested CRS
                    convertPoints(response, correctionSet.getPeAzimuthalEquidistantCRS(), state);
                }
                response.setLocalCRS(correctionSet.getAzimuthalEquidistantCRS().createPersistableReference());
                convertToWgs84(response, state);
            } else {
                throw new BadRequestException(String.join(" ", state.getErrors()));
            }
        } else {
            throw new BadRequestException(String.join(" ", state.getErrors()));
        }
        response.setOperationsApplied(state.getOperations());
        return response;
    }
    @Override
    public ConvertTrajectoryResponseV4 convertTrajectoryV4(DpsHeaders headers, ConvertTrajectoryRequestV4 request, boolean flag_check_projected, boolean flag_check_scaleFactor, boolean isInclOnly) {
        TrajectoryComputationStateV4 state = new TrajectoryComputationStateV4();
        state.setDpsHeaders(headers);
        ConvertTrajectoryResponseV4 response = new ConvertTrajectoryResponseV4();
        response.setTrajectoryCRS(request.getTrajectoryCRS());
        response.setUnitXY(request.getUnitXY());
        response.setUnitZ(request.getUnitZ());
        response.setMethod(request.getMethod());
        response.setInputKind(request.getInputKind());
        response.setUnitMD(request.getUnitMD());

        //validate request payload and initialize TrajectoryComputationState
        if (isRequestValidV4(request, state)) {
            double gridConvergence;
            double to_gn;
            double to_tn;
            AzimuthCorrector azimuthCorrector = new AzimuthCorrector();
            // populate TrajectoryStationOut from request payload
            response.setStations(populateResponseFromRequestV4(request, isInclOnly));
            ProjectionCorrectionSet correctionSet
                    = azimuthCorrector.createProjectionCorrectionSet(
                            state.getSourceCRSAsPersistableReference(), request.getReferencePoint(), state.getHorizontalUnit());
            gridConvergence = correctionSet.getConvergenceAngleInDeg();
            if(state.getAzimuthReference() != null) {
                if (state.getAzimuthReference() == AzimuthReferenceType.GRID_NORTH) { // correct GN to TN first
                    to_tn = gridConvergence;
                    to_gn = 0.0;
                    state.getOperations().add(String.format("derived TN from GN azimuth by grid convergence %f", (to_tn + 360) % 360.0));
                } else {
                    to_tn = 0.0;
                    to_gn = -gridConvergence;
                    state.getOperations().add(String.format("derived GN from TN azimuth by grid convergence %f", (to_gn + 360) % 360.0));
                }
                for (TrajectoryStationOut to : response.getStations()) {
                    double tn = (to.getAzimuthTN() + to_tn + 360) % 360.0;
                    double gn = (to.getAzimuthGN() + to_gn + 360) % 360.0;
                    to.setAzimuthTN(tn);
                    to.setAzimuthGN(gn);
                    to.setDls(Double.NaN);
                }
            }

            // original request payload units are converted to METER unit
            ConvertTrajectoryResponseV4 siResponse = normalizeTrajectoryV4(response, state);
            Point referencePoint = new Point(0.0, 0.0, siResponse.getStations().get(0).getPoint().getZ());
            // compute minimum curvature
            if (callTrajectoryEngineServiceV4(siResponse, referencePoint, state)) {
                // The converted request payload units are converted back from METER unit to original units passed from payload
                deNormalizeTrajectoryV4(siResponse, response, state);

                state.getOperations().add(String.format("computation method: %s", state.getMethod().toString()));
                if (state.getMethod() == TrajectoryComputationMethod.LeesModifiedProposal) {
                    // This function does LMP computation when method passed from payload is LMP
                    convertPointsLmp(response, state);
                } else {
                    // convert from local azimuthal equidistant CRS to requested CRS
                    convertPoints(response, correctionSet.getPeAzimuthalEquidistantCRS(), state);
                    // add method to compute interpolation based on MD_i input
                    if(request.getMD_i()!=null && !request.getMD_i().getMd_i().isEmpty() && flag_check_scaleFactor) {
                        computeInterpolationForMDiInput(request, response, state,flag_check_projected);
                        state.getOperations().add("Interpolation for MD_i input stations;" +request.getMD_i().getMd_i().size()+ " points interpolated");
                    }
                }
                ConvertTrajectoryRequestV4 dummyRequestForScaleCompute = null;
                List<ScaleConvergence> scaleConvergenceList = new ArrayList<>();
                if (flag_check_projected && flag_check_scaleFactor) {
                    //a dummy (fake/artificial) directional survey at the desired
                    //referencePoint location, creating a "path", going due north (GN) with
                    //a (true) horizontal length of 100m (i.e., set inclination=90 and
                    //azimuth=0).
                    dummyRequestForScaleCompute = prepareDummyPayload(request);
                    // this function uses dummy payload prepared above to compute scale factor & convergence for first station
                    ScaleConvergence scaleConvergenceFirst = computeScaleFactorAndConvergence(headers,dummyRequestForScaleCompute,flag_check_projected,response.getStations().get(0));
                    scaleConvergenceList.add(scaleConvergenceFirst);
                    response.setScaleConvergenceList(scaleConvergenceList);
                }
                if (state.getMethod() == TrajectoryComputationMethod.GridNorthLocal && flag_check_scaleFactor){
                    // calculated when method is GNL
                    // demonstrates how to “back out” the applied point scale factor
                    computeUnscaledValuesForXAndY(response);
                }
                if (flag_check_projected && flag_check_scaleFactor) {
                    // this function uses dummy payload prepared above to compute scale factor & convergence for last station
                    ScaleConvergence scaleConvergenceLast = computeScaleFactorAndConvergence(headers, dummyRequestForScaleCompute, flag_check_projected, response.getStations().get(response.getStations().size() - 1));
                    scaleConvergenceList.add(scaleConvergenceLast);
                }
                response.setLocalCRS(correctionSet.getAzimuthalEquidistantCRS().createPersistableReference());
                // converts original coordinates to WGS84 coordinates
                convertToWgs84(response, state);
            } else {
                throw new BadRequestException(String.join(" ", state.getErrors()));
            }
        } else {
            throw new BadRequestException(String.join(" ", state.getErrors()));
        }
        response.setOperationsApplied(state.getOperations());

        // returning max_horizontal_error & TVD_correction applied for inclination only scenario
        //Inclination-only data, also known as INC-ONLY or TOTCO, are survey stations that do not
        // have azimuth observables.
        if (isInclOnly) {
            DecimalFormat upto1decimal = new DecimalFormat("#.#");
            TrajectoryStationOut lastStationOut = response.getStations().get(response.getStations().size() - 1);
            Integer max_horizontal_error = new Double(lastStationOut.getDyTN()).intValue();
            Double tvd_correction = lastStationOut.getMd() - lastStationOut.getDZ();
            response.getOperationsApplied().add("max_horizontal_error = " + max_horizontal_error + " " + state.getHorizontalUnit().getSymbol());
            response.getOperationsApplied().add("TVD_correction applied = " + Double.parseDouble(upto1decimal.format(tvd_correction)) + " " + state.getVerticalUnit().getSymbol());
            return response;
        }

        return response;
    }

    public ConvertTrajectoryResponseV4 computeInterpolationForMDiInput(ConvertTrajectoryRequestV4 request, ConvertTrajectoryResponseV4 response, TrajectoryComputationStateV4 state,boolean flag_check_projected) {

        List<TrajectoryStationOut> stationsListOuti = new ArrayList<>();
        MinimumDepthInterval minimumDepthInterval = request.getMD_i();
        List<Double> mdiList = minimumDepthInterval.getMd_i();
        List<TrajectoryStationOut> stationsList = response.getStations();
        for (int count = 0; count < mdiList.size(); count++) {
            TrajectoryStationOut stationsListOut = interpolateMdi(mdiList.get(count), stationsList,flag_check_projected);
            stationsListOuti.add(stationsListOut);
        }
        response.setStations_i(stationsListOuti);
        convertToWgs84V4(response, state);

        return response;
    }

    private static Object[] beforeAndAfterMdi(Double mdi, List<TrajectoryStationOut> stationsList) {
        TrajectoryStationOut beforeMdiStation;
        List<TrajectoryStationOut> result = stationsList.stream().filter(stationOut -> stationOut.getMd() <= mdi).collect(Collectors.toList());
        if (result.get(result.size() - 1).getMd().doubleValue() == mdi.doubleValue() && result.size() == 1)
            beforeMdiStation = result.get(0);
        else if (result.get(result.size() - 1).getMd().doubleValue() == mdi.doubleValue())
            beforeMdiStation = result.get(result.size() - 2);
        else
            beforeMdiStation = result.get(result.size() - 1);
        TrajectoryStationOut afterMdiStation = afterMdi(mdi, stationsList);
        return new Object[]{beforeMdiStation, afterMdiStation};
    }

    private static TrajectoryStationOut afterMdi(Double mdi, List<TrajectoryStationOut> stationsList) {
        TrajectoryStationOut afterMdiStation;
        List<TrajectoryStationOut> result = stationsList.stream().filter(stationOut -> stationOut.getMd() >= mdi).collect(Collectors.toList());
        if (mdi.doubleValue() == result.get(0).getMd().doubleValue() && result.size() == 1)
            afterMdiStation = result.get(0);
        else if (mdi.doubleValue() == result.get(0).getMd().doubleValue())
            afterMdiStation = result.get(1);
        else
            afterMdiStation = result.get(0);
        return afterMdiStation;
    }

    private TrajectoryStationOut interpolateMdi(Double mdi, List<TrajectoryStationOut> stationsList, boolean flag_check_projected) {
        TrajectoryStationOut trajectoryStationOuti = new TrajectoryStationOut();
        Double md_i_minus_md_1;
        Double md_2_minus_md_1;
        Object[] station = beforeAndAfterMdi(mdi, stationsList);
        TrajectoryStationOut stationOut1 = (TrajectoryStationOut) station[0];
        TrajectoryStationOut stationOut2 = (TrajectoryStationOut) station[1];

        Double md2 = stationOut2.getMd();
        double inc_2 = stationOut2.getInclination();
        double inc_1 = stationOut1.getInclination();
        double azi_TN2 = stationOut2.getAzimuthTN();
        double azi_TN1 = stationOut1.getAzimuthTN();
        double azi_GN1 = stationOut1.getAzimuthGN();
        md_i_minus_md_1 = mdi - stationOut1.getMd();
        md_2_minus_md_1 = md2 - stationOut1.getMd();
        double dl = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((Math.toRadians(inc_2) - Math.toRadians(inc_1)) / 2), 2) + Math.sin(Math.toRadians(inc_1)) * Math.sin(Math.toRadians(inc_2)) * Math.pow(Math.sin((Math.toRadians(azi_TN2) - Math.toRadians(azi_TN1)) / 2), 2)));
        if (dl < -1.0)
            dl = -1.0;
        else if(dl > 1.0)
            dl = 1.0;
        double dl_i = dl * (md_i_minus_md_1 / md_2_minus_md_1);
        double inc_i;
        double azi_TN_i;
        double rf_i;
        if (dl_i == 0 || Double.isNaN(dl_i)) {
            inc_i = Math.toRadians(inc_1);
            azi_TN_i = Math.toRadians(azi_TN1);
            rf_i = 1;
        } else {
            inc_i = Math.acos((Math.sin(dl - dl_i) / Math.sin(dl)) * Math.cos(Math.toRadians(inc_1)) + (Math.sin(dl_i) / Math.sin(dl)) * Math.cos(Math.toRadians(inc_2)));
            if (inc_i < -1.0)
                inc_i = -1.0;
            else if (inc_i > 1.0)
                inc_i = 1.0;
            azi_TN_i = Math.atan2(Math.sin(Math.toRadians(inc_1)) * Math.sin(Math.toRadians(azi_TN1)) * Math.sin(dl - dl_i) + Math.sin(Math.toRadians(inc_2)) * Math.sin(Math.toRadians(azi_TN2)) * Math.sin(dl_i),
                    Math.sin(Math.toRadians(inc_1)) * Math.cos(Math.toRadians(azi_TN1)) * Math.sin(dl - dl_i) + Math.sin(Math.toRadians(inc_2)) * Math.cos(Math.toRadians(azi_TN2)) * Math.sin(dl_i));
            rf_i = 2 * Math.tan(dl_i / 2) / dl_i;
        }

        double n_i_minus_1 = md_i_minus_md_1 * (rf_i / 2) * (Math.sin(Math.toRadians(inc_1)) * Math.cos(Math.toRadians(azi_TN1)) + Math.sin(inc_i) * Math.cos(azi_TN_i));
        double e_i_minus_1 = md_i_minus_md_1 * (rf_i / 2) * (Math.sin(Math.toRadians(inc_1)) * Math.sin(Math.toRadians(azi_TN1)) + Math.sin(inc_i) * Math.sin(azi_TN_i));
        double d_i_minus_1 = md_i_minus_md_1 * (rf_i / 2) * (Math.cos(Math.toRadians(inc_1)) + Math.cos(inc_i));
        double convergence_deg = 0.0;
        double azi_GN_i = 0.0;
        if (flag_check_projected) {
            // this is close enough for small interpolation to assume as constant.
            convergence_deg = azi_TN1 - azi_GN1;
            azi_GN_i = azi_TN_i - Math.toRadians(convergence_deg);
            if (azi_GN_i < 0) {
                azi_GN_i += 360;
            } else if (azi_GN_i >= 360) {
                azi_GN_i -= 360;
            }
        }
        double convergence = Math.toRadians((convergence_deg));
        double e_i_minus_1_GN = Math.cos(convergence) * e_i_minus_1 - Math.sin(convergence) * n_i_minus_1;
        double n_i_minus_1_GN = Math.sin(convergence) * e_i_minus_1 + Math.cos(convergence) * n_i_minus_1;

        trajectoryStationOuti.setMd(mdi);
        trajectoryStationOuti.setInclination(Math.toDegrees(inc_i));
        trajectoryStationOuti.setAzimuthTN(Math.toDegrees(azi_TN_i));
        trajectoryStationOuti.setAzimuthGN(Math.toDegrees(azi_GN_i));
        trajectoryStationOuti.setDxTN(e_i_minus_1 +stationOut1.getDxTN());
        trajectoryStationOuti.setDyTN(n_i_minus_1+ stationOut1.getDyTN());
        trajectoryStationOuti.setDZ(d_i_minus_1+stationOut1.getDZ());
        trajectoryStationOuti.setDls(Math.toDegrees(dl_i));
        if (flag_check_projected) {
            trajectoryStationOuti.setPoint(new Point(stationOut1.getPoint().getX() + e_i_minus_1_GN, stationOut1.getPoint().getY() + n_i_minus_1_GN, stationOut1.getPoint().getZ() - d_i_minus_1));
            //call LMP method
        }
        return trajectoryStationOuti;
    }

    public void computeUnscaledValuesForXAndY(ConvertTrajectoryResponseV4 response){
        List<TrajectoryStationOut> stationsList = response.getStations();
        double scaleFactor = response.getScaleConvergenceList().get(0).getScalefactor();
        int count=0;
        Point firstStationPoint = stationsList.get(0).getPoint();
        double y0 = firstStationPoint.getY();
        double x0 = firstStationPoint.getX();
        for(count=0;count<stationsList.size();count++) {
            TrajectoryStationOut to = stationsList.get(count);
            double x = x0 + (to.getPoint().getX() - x0) / scaleFactor;
            double y = y0 + (to.getPoint().getY() - y0) / scaleFactor;
            response.getStations().get(count).setPoint(new Point(x, y, to.getPoint().getZ()));
        }
    }

    public ConvertTrajectoryRequestV4 prepareDummyPayload(ConvertTrajectoryRequestV4 request){
        String dummyRequestTemplate = "{ \"azimuthReference\": \"GN\", \"inputStations\": [   {      \"md\": 0,     \"azimuth\": 0,      \"inclination\": 90    },    {      \"md\": 20,        \"azimuth\": 0,      \"inclination\": 90    }  ],  \"inputKind\": \"MD_Incl_Azim\",  \"interpolate\": false,  \"method\": \"AzimuthalEquidistant\"}";
        ObjectMapper mapper = new ObjectMapper();
        ConvertTrajectoryRequestV4  requestV4 = null;
        try {
            requestV4 = mapper.readValue(dummyRequestTemplate, ConvertTrajectoryRequestV4.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        requestV4.setTrajectoryCRS(request.getTrajectoryCRS());
        requestV4.setUnitXY(request.getUnitXY());
        requestV4.setUnitZ("{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}");
        return requestV4;
    }

    public  ScaleConvergence computeScaleFactorAndConvergence(DpsHeaders headers,ConvertTrajectoryRequestV4 dummyRequestForScaleCompute,boolean flag_check_projected,TrajectoryStationOut trajectoryStationOut) {
        Gson gson = new Gson();
        Point deepCopy = gson.fromJson(gson.toJson(trajectoryStationOut.getPoint()), Point.class);
        dummyRequestForScaleCompute.setReferencePoint(deepCopy);
        ConvertTrajectoryResponseV4 dummyResponseFirst = convertTrajectoryV4(headers,dummyRequestForScaleCompute,flag_check_projected,false, false);
        List<TrajectoryStationOut> dummyStationsList = dummyResponseFirst.getStations();
        TrajectoryStationOut dummyFirstStation_pointFirst = dummyStationsList.get(0);
        TrajectoryStationOut dummyLastStation_pointFirst = dummyStationsList.get(dummyStationsList.size()-1);

        double dGN = Math.sqrt(Math.pow(dummyLastStation_pointFirst.getPoint().getY() - dummyFirstStation_pointFirst.getPoint().getY(), 2) + Math.pow(dummyLastStation_pointFirst.getPoint().getX() - dummyFirstStation_pointFirst.getPoint().getX(), 2));
        double dTN = Math.sqrt(Math.pow(dummyLastStation_pointFirst.getDxTN() - dummyFirstStation_pointFirst.getDxTN(), 2) + Math.pow(dummyLastStation_pointFirst.getDyTN() - dummyFirstStation_pointFirst.getDyTN(), 2));
        DecimalFormat upto6decimal = new DecimalFormat("#.######");
        double scaleFactorPointFirst = Double.parseDouble(upto6decimal.format(dGN / dTN));
        DecimalFormat upto5decimal = new DecimalFormat("#.#####");
        double gridConvergencePointFirst = dummyFirstStation_pointFirst.getAzimuthTN() - dummyFirstStation_pointFirst.getAzimuthGN();
        if (gridConvergencePointFirst < -180) {
            gridConvergencePointFirst += 360;
        } else if (gridConvergencePointFirst > 180) {
            gridConvergencePointFirst -= 360;
        }
        ScaleConvergence scaleConvergence = new ScaleConvergence();
        scaleConvergence.setPoint(trajectoryStationOut.getPoint());
        scaleConvergence.setScalefactor(scaleFactorPointFirst);
        scaleConvergence.setConvergence(Double.parseDouble(upto5decimal.format(gridConvergencePointFirst)));
        return scaleConvergence;
    }

    private double[] extractCoordinatesFromResponse(ConvertTrajectoryResponse response) {
        double[] coordinates = new double[2 * response.getStations().size()];
        int i = 0;
        for (TrajectoryStationOut item : response.getStations()) {
            coordinates[i] = item.getPoint().getX();
            coordinates[i + 1] = item.getPoint().getY();
            i += 2;
        }
        return coordinates;
    }

    private double[] extractCoordinatesFromResponseV4(ConvertTrajectoryResponseV4 response) {
        double[] coordinates = new double[2 * response.getStations_i().size()];
        int i = 0;
        for (TrajectoryStationOut item : response.getStations_i()) {
            coordinates[i] = item.getPoint().getX();
            coordinates[i + 1] = item.getPoint().getY();
            i += 2;
        }
        return coordinates;
    }

    private double[] extractCoordinatesFromResponse(ConvertTrajectoryResponseV4 response) {
        double[] coordinates = new double[2 * response.getStations().size()];
        int i = 0;
        for (TrajectoryStationOut item : response.getStations()) {
            coordinates[i] = item.getPoint().getX();
            coordinates[i + 1] = item.getPoint().getY();
            i += 2;
        }
        return coordinates;
    }

    private double[] extractElevationsFromResponse(ConvertTrajectoryResponse response) {
        double[] elevations = new double[response.getStations().size()];
        int i = 0;
        for (TrajectoryStationOut item : response.getStations()) {
            elevations[i] = item.getPoint().getZ();
            i++;
        }
        return elevations;
    }

    private double[] extractElevationsFromResponse(ConvertTrajectoryResponseV4 response) {
        double[] elevations = new double[response.getStations().size()];
        int i = 0;
        for (TrajectoryStationOut item : response.getStations()) {
            elevations[i] = item.getPoint().getZ();
            i++;
        }
        return elevations;
    }

    private double[] extractElevationsFromResponseV4(ConvertTrajectoryResponseV4 response) {
        double[] elevations = new double[response.getStations_i().size()];
        int i = 0;
        for (TrajectoryStationOut item : response.getStations_i()) {
            elevations[i] = item.getPoint().getZ();
            i++;
        }
        return elevations;
    }

    public ConvertTrajectoryResponse normalizeTrajectory(ConvertTrajectoryResponse response, TrajectoryComputationState state) {
        ConvertTrajectoryResponse siResponse = new ConvertTrajectoryResponse();
        double xyFactor = state.getHorizontalUnit().scaleToSI();
        double z_Factor = state.getVerticalUnit().scaleToSI();

        siResponse.setInputKind(response.getInputKind());
        siResponse.setUnitXY(METER);
        siResponse.setUnitZ(METER);
        siResponse.setStations(new ArrayList<>(response.getStations()));
        for (TrajectoryStationOut item : siResponse.getStations()) {
            item.setMd(item.getMd() * z_Factor);
            item.setInclination(item.getInclination() * DEG2RAD);
            item.setAzimuthTN(item.getAzimuthTN() * DEG2RAD);
            item.setAzimuthGN(item.getAzimuthGN() * DEG2RAD);
        }
        Point p0 = siResponse.getStations().get(0).getPoint();
        double x = p0.getX() * xyFactor;
        double y = p0.getY() * xyFactor;
        double z = p0.getZ() * z_Factor;
        p0.setX(x);
        p0.setY(y);
        p0.setZ(z);

        return siResponse;
    }

    public ConvertTrajectoryResponseV4 normalizeTrajectoryV4(ConvertTrajectoryResponseV4 response, TrajectoryComputationStateV4 state) {
        ConvertTrajectoryResponseV4 siResponse = new ConvertTrajectoryResponseV4();
        double xyFactor = state.getHorizontalUnit().scaleToSI();
        double z_Factor = state.getVerticalUnit().scaleToSI();
        double unitMD_Factor = 0.0;
        boolean checkUnitMD = false;

        // unitMD_Factor takes precedence if unitMD passed from payload, else z_Factor
        if(state.getUnitMD()!=null){
             unitMD_Factor = state.getUnitMD().scaleToSI();
             checkUnitMD =  true;
             state.getOperations().add(String.format("unitMD Factor value: %s is used for computation of MD", unitMD_Factor));
        }else{
             state.getOperations().add(String.format("UnitMD set to be equal to unitZ %s:",state.getVerticalUnit().getSymbol()));
        }
        siResponse.setInputKind(response.getInputKind());
        siResponse.setUnitXY(METER);
        siResponse.setUnitZ(METER);
        siResponse.setUnitMD(METER);
        siResponse.setStations(new ArrayList<>(response.getStations()));
        for (TrajectoryStationOut item : siResponse.getStations()) {
            if(checkUnitMD){
                item.setMd(item.getMd() * unitMD_Factor);
            }else{
                item.setMd(item.getMd() * z_Factor);
            }
            item.setInclination(item.getInclination() * DEG2RAD);
            item.setAzimuthTN(item.getAzimuthTN() * DEG2RAD);
            item.setAzimuthGN(item.getAzimuthGN() * DEG2RAD);
        }
        Point p0 = siResponse.getStations().get(0).getPoint();
        double x = p0.getX() * xyFactor;
        double y = p0.getY() * xyFactor;
        double z = p0.getZ() * z_Factor;
        p0.setX(x);
        p0.setY(y);
        p0.setZ(z);

        return siResponse;
    }

    public void deNormalizeTrajectory(ConvertTrajectoryResponse siResponse, ConvertTrajectoryResponse response, TrajectoryComputationState state) {
        double xyFactor = 1.0 / state.getHorizontalUnit().scaleToSI();
        double z_Factor = 1.0 / state.getVerticalUnit().scaleToSI();
        double dlFactor;
        if (z_Factor != 1.0) {
            dlFactor = 30.48; // non-metric: deg/100ft
            response.setUnitDls(DEGPFT);
        } else { //              metric:     deg/30m
            dlFactor = 30;
            response.setUnitDls(DEGPM);
        }
        if (siResponse != response) {
            response.getStations().clear();
            for (int i = 0; i < siResponse.getStations().size(); i++) {
                TrajectoryStationOut si = siResponse.getStations().get(i);
                TrajectoryStationOut dn = new TrajectoryStationOut();
                dn.setMd(si.getMd() * z_Factor);
                dn.setDxTN(si.getDxTN() * xyFactor);
                dn.setDyTN(si.getDyTN() * xyFactor);
                dn.setDZ(si.getDZ() * z_Factor);
                dn.setAzimuthTN(si.getAzimuthTN() * RAD2DEG);
                dn.setAzimuthGN(si.getAzimuthGN() * RAD2DEG);
                dn.setInclination(si.getInclination() * RAD2DEG);
                dn.setDls(si.getDls() * dlFactor * RAD2DEG);
                Point p = si.getPoint();
                double x = p.getX() * xyFactor;
                double y = p.getY() * xyFactor;
                double z = p.getZ() * z_Factor;
                dn.setPoint(new Point(x, y, z));
                dn.setOriginal(si.isOriginal());
                dn.setWgs84Latitude(si.getWgs84Latitude());
                dn.setWgs84Longitude(si.getWgs84Longitude());
                response.getStations().add(dn);
            }
        } else {
            for (TrajectoryStationOut to : response.getStations()) {
                to.setDls(to.getDls() * dlFactor);
            }
        }
    }

    public void deNormalizeTrajectoryV4(ConvertTrajectoryResponseV4 siResponse, ConvertTrajectoryResponseV4 response, TrajectoryComputationStateV4 state) {
        double xyFactor = 1.0 / state.getHorizontalUnit().scaleToSI();
        double z_Factor = 1.0 / state.getVerticalUnit().scaleToSI();
        Double unitMD_Factor = null;
        if(state.getUnitMD()!=null) {
            unitMD_Factor = 1.0 / state.getUnitMD().scaleToSI();
        }
        double dlFactor;
        if (z_Factor != 1.0 || (unitMD_Factor != null && unitMD_Factor != 1.0)) {
            dlFactor = 30.48; // non-metric: deg/100ft
            response.setUnitDls(DEGPFT);
        } else { //              metric:     deg/30m
            dlFactor = 30;
            response.setUnitDls(DEGPM);
        }
        if (siResponse != response) {
            response.getStations().clear();
            for (int i = 0; i < siResponse.getStations().size(); i++) {
                TrajectoryStationOut si = siResponse.getStations().get(i);
                TrajectoryStationOut dn = new TrajectoryStationOut();
                if(unitMD_Factor!= null){
                    dn.setMd(si.getMd() * unitMD_Factor);
                }else{
                    dn.setMd(si.getMd() * z_Factor);
                }
                dn.setDxTN(si.getDxTN() * xyFactor);
                dn.setDyTN(si.getDyTN() * xyFactor);
                dn.setDZ(si.getDZ() * z_Factor);
                dn.setAzimuthTN(si.getAzimuthTN() * RAD2DEG);
                dn.setAzimuthGN(si.getAzimuthGN() * RAD2DEG);
                dn.setInclination(si.getInclination() * RAD2DEG);
                dn.setDls(si.getDls() * dlFactor * RAD2DEG);
                Point p = si.getPoint();
                double x = p.getX() * xyFactor;
                double y = p.getY() * xyFactor;
                double z = p.getZ() * z_Factor;
                dn.setPoint(new Point(x, y, z));
                dn.setOriginal(si.isOriginal());
                dn.setWgs84Latitude(si.getWgs84Latitude());
                dn.setWgs84Longitude(si.getWgs84Longitude());
                response.getStations().add(dn);
            }
        } else {
            for (TrajectoryStationOut to : response.getStations()) {
                to.setDls(to.getDls() * dlFactor);
            }
        }
    }

    public boolean callTrajectoryEngineService(
            ConvertTrajectoryResponse response, Point referencePoint, TrajectoryComputationState state) {
        response.setInputKind(state.getInputKind().toString());
        if (state.isInterpolate()) {
            int before = response.getStations().size();
            response.setStations(interpolateStations(response.getStations()));
            int after = response.getStations().size();
            if (after > before) {
                state.getOperations().add("added interpolated stations");
            }
        }
        // do the trajectory computation
        minimumCurvature(referencePoint, response.getStations());
        state.getOperations().add("computed deflections via minimum curvature method");
        return true;
    }

    public boolean callTrajectoryEngineServiceV4(
            ConvertTrajectoryResponseV4 response, Point referencePoint, TrajectoryComputationStateV4 state) {
        response.setInputKind(state.getInputKind().toString());
        if (state.isInterpolate()) {
            int before = response.getStations().size();
            response.setStations(interpolateStations(response.getStations()));
            int after = response.getStations().size();
            if (after > before) {
                state.getOperations().add("added interpolated stations");
            }
        }
        // do the trajectory computation
        minimumCurvature(referencePoint, response.getStations());
        state.getOperations().add("computed deflections via minimum curvature method");
        return true;
    }

    public void convertToWgs84(ConvertTrajectoryResponse response, TrajectoryComputationState state) {
        double[] xyCoordinates = extractCoordinatesFromResponse(response);
        ICRSConverter crsConverter = new CRSConverter();
        double[] zCoordinates = extractElevationsFromResponse(response);
        try {
            ConvertPointsResponse rsp
                    = crsConverter.convertPoint(state.getSourceCRSAsPersistableReference(), Constants.WGS84, xyCoordinates, zCoordinates);
            int i = 0;
            for (TrajectoryStationOut to : response.getStations()) {
                to.setWgs84Latitude(xyCoordinates[2 * i + 1]);
                to.setWgs84Longitude(xyCoordinates[2 * i]);
                i++;
            }
            for (String op : rsp.getOperationsApplied()) {
                state.getOperations().add("to WGS 84: " + op);
            }
        } catch (IllegalArgumentException e) {
            for (TrajectoryStationOut to : response.getStations()) {
                to.setWgs84Latitude(Double.NaN);
                to.setWgs84Longitude(Double.NaN);
            }
        }
    }

    public void convertToWgs84(ConvertTrajectoryResponseV4 response, TrajectoryComputationStateV4 state) {
        double[] xyCoordinates = extractCoordinatesFromResponse(response);
        ICRSConverter crsConverter = new CRSConverter();
        double[] zCoordinates = extractElevationsFromResponse(response);
        try {
            ConvertPointsResponse rsp
                    = crsConverter.convertPoint(state.getSourceCRSAsPersistableReference(), Constants.WGS84, xyCoordinates, zCoordinates);
            int i = 0;
            for (TrajectoryStationOut to : response.getStations()) {
                to.setWgs84Latitude(xyCoordinates[2 * i + 1]);
                to.setWgs84Longitude(xyCoordinates[2 * i]);
                i++;
            }
            for (String op : rsp.getOperationsApplied()) {
                state.getOperations().add("to WGS 84: " + op);
            }
        } catch (IllegalArgumentException e) {
            for (TrajectoryStationOut to : response.getStations()) {
                to.setWgs84Latitude(Double.NaN);
                to.setWgs84Longitude(Double.NaN);
            }
        }
    }

    public void convertToWgs84V4(ConvertTrajectoryResponseV4 response, TrajectoryComputationStateV4 state) {
        double[] xyCoordinates = extractCoordinatesFromResponseV4(response);
        ICRSConverter crsConverter = new CRSConverter();
        double[] zCoordinates = extractElevationsFromResponseV4(response);
        try {
            ConvertPointsResponse rsp
                    = crsConverter.convertPoint(state.getSourceCRSAsPersistableReference(), Constants.WGS84, xyCoordinates, zCoordinates);
            int i = 0;
            for (TrajectoryStationOut to : response.getStations_i()){
                to.setWgs84Latitude(xyCoordinates[2 * i + 1]);
                to.setWgs84Longitude(xyCoordinates[2 * i]);
                i++;
            }
            for (String op : rsp.getOperationsApplied()) {
                state.getOperations().add("to WGS 84: " + op);
            }
        } catch (IllegalArgumentException e) {
            for (TrajectoryStationOut to : response.getStations_i()) {
                to.setWgs84Latitude(Double.NaN);
                to.setWgs84Longitude(Double.NaN);
            }
        }
    }

    private static final double R360 = 360 * DEG2RAD;
    private static final double R540 = 540 * DEG2RAD;
    private static final double R180 = 180 * DEG2RAD;

    private List<TrajectoryStationOut> interpolateStations(List<TrajectoryStationOut> stations) {
        List<TrajectoryStationOut> interpolated = new ArrayList<>();
        for (int i = 1; i < stations.size(); i++) {
            if (i == 1) {
                interpolated.add(stations.get(i - 1));
            }
            TrajectoryStationOut i0 = stations.get(i - 1);
            TrajectoryStationOut i1 = stations.get(i);
            double azTN = ((((i1.getAzimuthTN() - i0.getAzimuthTN()) % R360) + R540) % R360) - R180;
            double azGN = ((((i1.getAzimuthGN() - i0.getAzimuthGN()) % R360) + R540) % R360) - R180;
            double incl = ((((i1.getInclination() - i0.getInclination()) % R360) + R540) % R360) - R180;
            double dMD = i1.getMd() - i0.getMd();
            if (Math.abs(azTN) > EPS || Math.abs(incl) > EPS) { // need interpolation
                int N = (int) (dMD / 100);
                if (N > 1) {
                    double delta = 1 / (double) N;
                    for (int j = 1; j < N; j++) {
                        double md = i0.getMd() + (j * delta) * dMD;
                        TrajectoryStationOut to = new TrajectoryStationOut();
                        to.setMd(md);
                        to.setInclination((i0.getInclination() + j * delta * incl + R360) % R360);
                        to.setAzimuthGN((i0.getAzimuthGN() + j * delta * azGN + R360) % R360);
                        to.setAzimuthTN((i0.getAzimuthTN() + j * delta * azTN + R360) % R360);
                        to.setOriginal(false);
                        interpolated.add(to);
                    }
                }
            } //else if (dMD > 10000) {
//                int N = (int) (dMD / 10000);
//                if (N > 1) {
//                    double delta = 1 / (double) N;
//                    for (int j = 1; j < N; j++) {
//                        double md = i0.getMd() + (j * delta) * dMD;
//                        TrajectoryStationOut to = new TrajectoryStationOut();
//                        to.setMd(md);
//                        to.setInclination((i0.getInclination() + j * delta * incl + 360) % 360.0);
//                        to.setAzimuthGN((i0.getAzimuthGN() + j * delta * azGN + 360) % 360.0);
//                        to.setAzimuthTN((i0.getAzimuthTN() + j * delta * azTN + 360) % 360.0);
//                        to.setOriginal(false);
//                        interpolated.add(to);
//                    }
//                }
//            }
            interpolated.add(i1);
        }
        return interpolated;
    }

    private double getInvFlattening(ISisCrs crs) {
        CoordinateReferenceSystem coordianteReferenceSystem = crs.getCoordinateReferenceSystem();
        if (coordianteReferenceSystem instanceof GeodeticCRS) {
            GeodeticCRS geoCRS = (GeodeticCRS) coordianteReferenceSystem;
            GeodeticDatum datum = geoCRS.getDatum();
            Ellipsoid ellipsoid = datum.getEllipsoid();
            return ellipsoid.getInverseFlattening();
        }
        throw new IllegalArgumentException("Can't find flattening from crs");
    }

    private double getSemiMajorAxis(ISisCrs crs) {
        CoordinateReferenceSystem coordianteReferenceSystem = crs.getCoordinateReferenceSystem();
        if (coordianteReferenceSystem instanceof GeodeticCRS) {
            GeodeticCRS geoCRS = (GeodeticCRS) coordianteReferenceSystem;
            GeodeticDatum datum = geoCRS.getDatum();
            Ellipsoid ellipsoid = datum.getEllipsoid();
            return ellipsoid.getSemiMajorAxis();
        }
        throw new IllegalArgumentException("Can't find axis from crs");
    }

    public void convertPointsLmp(ConvertTrajectoryResponse response, TrajectoryComputationState state) {
        try {
            int nofPoints = response.getStations().size();
            double[] posXY = new double[2 * nofPoints];
            double[] aziGN = new double[nofPoints];
            double A = getSemiMajorAxis(state.getGeogCS());                         // A is semi-major axis
            double RF = getInvFlattening(state.getGeogCS());                        // RF is reciprocal of flattening
            double rad = 180 / Math.PI;                                             // Degrees in a radian
            double B = A * (1 - 1 / RF);                                              // B is semi-minor axis
            double E2 = 1 - (B * B) / (A * A);                                      // E2 is eccentricity squared

            double xyFactor = state.getHorizontalUnit().scaleToSI();  // ensuring that Edep and Ndep are in meters

            TrajectoryStationOut station = response.getStations().get(0);
            double Edeplast = station.getDxTN();
            double Ndeplast = station.getDyTN();
            double[] surfaceXY = {state.getReferencePoint().getX(), state.getReferencePoint().getY()};
            CoordinateReferenceSystem coordinateReferenceSystem = state.getGeogCS().getCoordinateReferenceSystem();
            if (coordinateReferenceSystem instanceof GeodeticCRS) {
                 surfaceXY = new double[]{state.getReferencePoint().getX()/xyFactor, state.getReferencePoint().getY()/xyFactor};
            }
            posXY[0] = surfaceXY[0];
            posXY[1] = surfaceXY[1];
            double refZ = state.getReferencePoint().getZ();
            response.getStations().get(0).getPoint().setX(posXY[0]);
            response.getStations().get(0).getPoint().setY(posXY[1]);
            response.getStations().get(0).getPoint().setZ(refZ);

            if (state.getProCS() != null) {
                SisTransformations.projToGeog(state.getProCS(), 1, surfaceXY);
                state.getOperations().add(String.format("reference point: conversion from '%s' to '%s'", state.getProCS().getName(), state.getGeogCS().getName()));
            }
            double latlast = surfaceXY[1]; // now latitude
            double lonlast = surfaceXY[0]; // now longitude
            for (int i = 0; i < response.getStations().size(); i++) {
                station = response.getStations().get(i);
                double LATRAD = latlast / rad; //% Degrees to radians
                double sinLATRADsqared = Math.pow(Math.sin(LATRAD), 2);
                double R = A * (1.0 - E2) / (Math.pow(Math.sqrt(1.0 - E2 * sinLATRADsqared), 3)); // % Radius in the meridian
                double N = A / Math.sqrt(1 - E2 * sinLATRADsqared); //% Radius in the prime vertical
                // TVD is the depth below MSL,it is positive downward
                double TVD =  station.getDZ() - refZ; // data(dex,4); //% Grab the TVD
                double Edep = station.getDxTN() * xyFactor; // data(dex,6);
                double Ndep = station.getDyTN() * xyFactor; // data(dex,5); //% Grab the departures
                double dEdep = Edep - Edeplast;  // % Difference the departures
                double dNdep = Ndep - Ndeplast;  // % Difference the departures
                Edeplast = Edep;
                Ndeplast = Ndep; //% Save the current departures
                double dlat = rad * dNdep / (R - TVD); //% Delta geographicals with LMP ...
                double dlon = rad * dEdep / (N - TVD) / Math.cos(LATRAD);

                latlast += dlat;
                lonlast += dlon;
                latlast = (latlast + 90 + 180) % 180 - 90;    // avoid numbers outside [ -90,  90]
                lonlast = (lonlast + 180 + 360) % 360 - 180;  // avoid numbers outside [-180, 180]
                if (state.getProCS() != null) {
                    double[] xy = {lonlast, latlast};
                    SisTransformations.geogToProj(state.getProCS(), 1, xy);
                    station.getPoint().setX(xy[0]);
                    station.getPoint().setY(xy[1]);
                    if (i == 1) {
                        state.getOperations().add(String.format("station points: conversion from '%s' to '%s'", state.getGeogCS().getName(), state.getProCS().getName()));
                    }

                } else {
                    station.getPoint().setX(lonlast);
                    station.getPoint().setY(latlast);
                }
                station.getPoint().setZ(-TVD);
                posXY[2 * i] = station.getPoint().getX();
                posXY[2 * i + 1] = station.getPoint().getY();
                aziGN[i] = station.getAzimuthTN(); // for later conversion to GN
            }
            if (state.getProCS() != null) {
                AzimuthCorrector azimuthCorrector = new AzimuthCorrector();
                int count = azimuthCorrector.correctAzimuth(state.getSourceCRSAsPersistableReference(), AzimuthReferenceType.TRUE_NORTH.toString(), posXY, aziGN);
                if (count == nofPoints) {
                    for (int i = 0; i < response.getStations().size(); i++) {
                        station = response.getStations().get(i);
                        station.setAzimuthGN(aziGN[i]);
                    }
                }
                state.getOperations().add("GN azimuth computed for each station location");
            }
        } catch (Exception e) {
            int err = ESRI_DATUM_ERROR;
            setNaNCoordinates(err, response, state);
        }
    }

    void setNaNCoordinates(int e, ConvertTrajectoryResponse response, TrajectoryComputationState state) {
        if (e == ESRI_PRJ_ERROR) {
            state.getErrors().add("Failure during de-projection/projection. ");
        } else {
            state.getErrors().add("Could not derive ellipsoid parameters. ");
        }
        Point p = new Point();
        Point.setNaN(p);
        for (TrajectoryStationOut station : response.getStations()) {
            station.setPoint(p);
            station.setWgs84Latitude(Double.NaN);
            station.setWgs84Longitude(Double.NaN);
        }
    }

    public void convertPoints(ConvertTrajectoryResponse response, ISisCrs aziEqu, TrajectoryComputationState state) {
        double[] xyCoordinates = extractCoordinatesFromResponse(response);
        int nofPoints = response.getStations().size();
        try {
            SisTransformations.projToGeog(aziEqu, nofPoints, xyCoordinates);
            state.getOperations().add(String.format("conversion from '%s' to '%s'", aziEqu.getName(), aziEqu.getGeogCoordSys().getName()));
            if (state.getProCS() != null) {
                SisTransformations.geogToProj(state.getProCS(), nofPoints, xyCoordinates);
                state.getOperations().add(String.format("conversion from '%s' to '%s'", aziEqu.getGeogCoordSys().getName(), state.getProCS().getName()));
            }
            int i = 0;
            for (TrajectoryStationOut station : response.getStations()) {
                station.getPoint().setX(xyCoordinates[i * 2]);
                station.getPoint().setY(xyCoordinates[i * 2 + 1]);
                // Z stays unchanged
                i++;
            }
        } catch (Exception e) {
            setNaNCoordinates(ESRI_PRJ_ERROR, response, state);
        }
    }

    private List<TrajectoryStationOut> populateResponseFromRequest(ConvertTrajectoryRequest request) {
        List<TrajectoryStationOut> tos = new ArrayList<>();
        for (TrajectoryStationIn ti : request.getInputStations()) {
            TrajectoryStationOut to = new TrajectoryStationOut();
            to.setMd(ti.getMd());
            to.setInclination(ti.getInclination());
            to.setAzimuthTN(ti.getAzimuth());
            to.setAzimuthGN(ti.getAzimuth());
            to.setOriginal(true); // this is original by definition
            if (tos.size() == 0) {
                to.setPoint(request.getReferencePoint()); // copy the reference point into first sample
            }
            tos.add(to);
        }
        return tos;
    }

    private List<TrajectoryStationOut> populateResponseFromRequestV4(ConvertTrajectoryRequestV4 request, boolean isInclOnly) {
        List<TrajectoryStationOut> tos = new ArrayList<>();
        for (TrajectoryStationInV4 ti : request.getInputStations()) {
            TrajectoryStationOut to = new TrajectoryStationOut();
            to.setMd(ti.getMd());
            to.setInclination(ti.getInclination());
            to.setAzimuthTN(ti.getAzimuth());
            to.setAzimuthGN(ti.getAzimuth());
            to.setOriginal(true); // this is original by definition
            if (tos.size() == 0) {
                to.setPoint(request.getReferencePoint()); // copy the reference point into first sample
            }
            tos.add(to);
        }
        return tos;
    }

    public boolean isRequestValid(ConvertTrajectoryRequest request, TrajectoryComputationState state) {
        if (request != null) {
            try {
                IItem raw = parseSpatialReference(request.getTrajectoryCRS());
                ICrs sourceCRS;
                if (raw instanceof ICrs) {
                    state.setSourceCRS((ICrs) raw);
                    sourceCRS = state.getSourceCRS();
                } else {
                    throw new IllegalArgumentException("Invalid type");
                }

                 if (!state.getSourceCRS().isValid()) {
                     state.getErrors().add("Failed to resolve coordinate reference systems from WKT.");
                 } else if (sourceCRS.getType() == CRSType.LATE_BOUND) {
                     state.setProCS(((ILateBoundCrs) sourceCRS).getProjectedCrs());
                     state.setGeogCS(((ILateBoundCrs) sourceCRS).getBaseGeographicCrs());
                 } else if (sourceCRS.getType() == CRSType.EARLY_BOUND) {
                     ILateBoundCrs lbCrs = ((IEarlyBoundCrs) sourceCRS).getLateBoundCrs();
                     state.setProCS(lbCrs.getProjectedCrs());
                     state.setGeogCS(lbCrs.getBaseGeographicCrs());
                 }
                 if (state.getGeogCS() == null && sourceCRS.isValid()) {
                     state.getErrors().add("Invalid coordinate reference system specification.");
                 } else {
                     state.setSourceCRSAsPersistableReference(request.getTrajectoryCRS());
                 }
            } catch (IllegalArgumentException e) {
                state.setSourceCRS(null);
                state.getErrors().add("Invalid coordinate reference system specification.");
            }
            IUnit unit = parseUnitReference(request.getUnitXY());
            state.setHorizontalUnit(unit);
            if (!unit.isValid() || !unit.isLength()) {
                state.getErrors().add("Invalid horizontal unit.");
            }
            unit = parseUnitReference(request.getUnitZ());
            state.setVerticalUnit(unit);
            if (!unit.isValid() || !unit.isLength()) {
                state.getErrors().add("Invalid vertical unit.");
            }
            state.setAzimuthReference(AzimuthReferenceType.getAzimuthReference(request.getAzimuthReference()));
            if (state.getAzimuthReference() == null) {
                state.getErrors().add("Invalid azimuth reference.");
            }
            state.setMethod(TrajectoryComputationMethod.getTrajectoryComputationMethod(request.getMethod()));
            if (state.getMethod() == null) {
                String m = "null";
                if (request.getMethod() != null) {
                    m = request.getMethod();
                }
                state.getErrors().add(String.format("Unsupported trajectory method code: %s.", m));
            }
            TrajectoryInputKind k = TrajectoryInputKind.getTrajectoryInputKind(request.getInputKind());
            if (k == null) {
                state.getErrors().add("Invalid input kind specification.");
            } else if (k != TrajectoryInputKind.MD_INCL_AZIM) {
                state.getErrors().add(k.toString() + " is not yet supported as input kind.");
            } else {
                state.setInputKind(k);
            }
            if (request.getReferencePoint() != null) {
                if (Point.isValid(request.getReferencePoint())) {
                    state.setReferencePoint(request.getReferencePoint());
                } else {
                    state.getErrors().add("Undefined or partially undefined trajectory reference point.");
                }
            } else {
                state.getErrors().add("Missing trajectory reference point.");
            }
            if (request.getInputStations() == null || request.getInputStations().size() == 0) {
                state.getErrors().add("Null or empty input trajectory survey stations.");
            } else {
                boolean ok = true;
                for (TrajectoryStationIn s : request.getInputStations()) {
                    ok = ok && !(s.getMd() == null || s.getAzimuth() == null || s.getInclination() == null);
                    ok = ok && !(Double.isNaN(s.getMd()) || Double.isNaN(s.getAzimuth()) || Double.isNaN(s.getInclination()));
                }
                if (!ok) {
                    state.getErrors().add("Invalid input trajectory survey stations values.");
                }
            }
            state.setInterpolate(request.isInterpolate());
        } else {
            state.getErrors().add("Null request.");
        }
        return state.getErrors().size() == 0;
    }

    public boolean isRequestValidV4(ConvertTrajectoryRequestV4 request, TrajectoryComputationStateV4 state) {
        if (request != null) {
            try {
                IItem raw = parseSpatialReference(request.getTrajectoryCRS());
                ICrs sourceCRS;
                if (raw instanceof ICrs) {
                    state.setSourceCRS((ICrs) raw);
                    sourceCRS = state.getSourceCRS();
                } else {
                    throw new IllegalArgumentException("Invalid type");
                }

                if (!state.getSourceCRS().isValid()) {
                    state.getErrors().add("Failed to resolve coordinate reference systems from WKT.");
                } else if (sourceCRS.getType() == CRSType.LATE_BOUND) {
                    state.setProCS(((ILateBoundCrs) sourceCRS).getProjectedCrs());
                    state.setGeogCS(((ILateBoundCrs) sourceCRS).getBaseGeographicCrs());
                } else if (sourceCRS.getType() == CRSType.EARLY_BOUND) {
                    ILateBoundCrs lbCrs = ((IEarlyBoundCrs) sourceCRS).getLateBoundCrs();
                    state.setProCS(lbCrs.getProjectedCrs());
                    state.setGeogCS(lbCrs.getBaseGeographicCrs());
                }
                if (state.getGeogCS() == null && sourceCRS.isValid()) {
                    state.getErrors().add("Invalid coordinate reference system specification.");
                } else {
                    state.setSourceCRSAsPersistableReference(request.getTrajectoryCRS());
                }
            } catch (IllegalArgumentException e) {
                state.setSourceCRS(null);
                state.getErrors().add("Invalid coordinate reference system specification.");
            }
            IUnit unit = parseUnitReference(request.getUnitXY());
            state.setHorizontalUnit(unit);
            if (!unit.isValid() || !unit.isLength()) {
                state.getErrors().add("Invalid horizontal unit.");
            }
            unit = parseUnitReference(request.getUnitZ());
            state.setVerticalUnit(unit);
            if (!unit.isValid() || !unit.isLength()) {
                state.getErrors().add("Invalid vertical unit.");
            }
            if(request.getUnitMD()!=null){
                unit = parseUnitReference(request.getUnitMD());
                state.setUnitMD(unit);
                if (!unit.isValid() || !unit.isLength()) {
                    state.getErrors().add("Invalid unitMD.");
                }
            }

            if(request.getAzimuthReference() != null){
                state.setAzimuthReference(AzimuthReferenceType.getAzimuthReference(request.getAzimuthReference()));
                if (state.getAzimuthReference() == null && !request.getInputKind().equals(Constants.MD_INCL)) {
                    state.getErrors().add("Invalid azimuth reference.");
                }
            }

            state.setMethod(TrajectoryComputationMethod.getTrajectoryComputationMethod(request.getMethod()));
            if (state.getMethod() == null) {
                String m = "null";
                if (request.getMethod() != null) {
                    m = request.getMethod();
                }
                state.getErrors().add(String.format("Unsupported trajectory method code: %s.", m));
            }
            TrajectoryInputKind k = TrajectoryInputKind.getTrajectoryInputKind(request.getInputKind());
            if (k == null) {
                state.getErrors().add("Invalid input kind specification.");
            } else if (k != TrajectoryInputKind.MD_INCL_AZIM) {
                state.getErrors().add(k.toString() + " is not yet supported as input kind.");
            } else {
                state.setInputKind(k);
            }
            if (request.getReferencePoint() != null) {
                if (Point.isValid(request.getReferencePoint())) {
                    state.setReferencePoint(request.getReferencePoint());
                } else {
                    state.getErrors().add("Undefined or partially undefined trajectory reference point.");
                }
            } else {
                state.getErrors().add("Missing trajectory reference point.");
            }
            if (request.getInputStations() == null || request.getInputStations().size() == 0) {
                state.getErrors().add("Null or empty input trajectory survey stations.");
            } else {
                boolean ok = true;
                for (TrajectoryStationInV4 s : request.getInputStations()) {
                    ok = ok && !(s.getMd() == null || s.getAzimuth() == null || s.getInclination() == null);
                    ok = ok && !(Double.isNaN(s.getMd()) || Double.isNaN(s.getAzimuth()) || Double.isNaN(s.getInclination()));
                }
                if (!ok) {
                    state.getErrors().add("Invalid input trajectory survey stations values.");
                }
            }
            state.setInterpolate(request.isInterpolate());
        } else {
            state.getErrors().add("Null request.");
        }
        return state.getErrors().size() == 0;
    }

    public void minimumCurvature(Point referencePoint, List<TrajectoryStationOut> stations) {
        TrajectoryStationOut to = stations.get(0);
        to.setMd(stations.get(0).getMd());
        to.setAzimuthTN(stations.get(0).getAzimuthTN());
        to.setInclination(stations.get(0).getInclination());
        to.setDxTN(0.0);
        to.setDyTN(0.0);
        to.setDZ(0.0);
        to.setDls(0.0); // this is the same as the dps-trajectory produces
        to.setPoint(new Point(referencePoint.getX(), referencePoint.getY(), referencePoint.getZ()));
        for (int i = 1; i < stations.size(); i++) {
            referencePoint = minimumCurvaturePair(referencePoint, stations, i);
        }
    }

    public Point minimumCurvaturePair(Point prf, List<TrajectoryStationOut> stations, int index) {
        // Taken from http://www.drillingformulas.com/minimum-curvature-method/
        // https://directionaldrillingart.blogspot.com/2015/09/directional-surveying-calculations.html
        double deg2rad = 1.0; // Math.PI / 180.0;
        double rad2deg = 1.0; // 180.0 / Math.PI;
        TrajectoryStationOut t1 = stations.get(index - 1);
        TrajectoryStationOut t2 = stations.get(index);
        double dmd = t2.getMd() - t1.getMd();
        double m2 = (t2.getMd() - t1.getMd()) * 0.5;
        double i1 = t1.getInclination() * deg2rad;
        double i2 = t2.getInclination() * deg2rad;
        double a1 = t1.getAzimuthTN() * deg2rad; // we always compute TN
        double a2 = t2.getAzimuthTN() * deg2rad; // we always compute TN
        double tmp_acos_argument = Math.cos(i2 - i1) - Math.sin(i1) * Math.sin(i2) * (1.0 - Math.cos(a2 - a1));
        if (tmp_acos_argument < -1.0)
            tmp_acos_argument = -1.0;
        else if (tmp_acos_argument > +1.0)
            tmp_acos_argument = +1.0;

        //double b = Math.acos(Math.cos(i2 - i1) - Math.sin(i1) * Math.sin(i2) * (1.0 - Math.cos(a2 - a1)));
        double b = Math.acos(tmp_acos_argument);
        double rf = 1.0;
        if (b != 0.0) {
            rf = 2.0 * Math.tan(b / 2.0) / b;
        }
        double dx = rf * m2 * (Math.sin(i1) * Math.sin(a1) + Math.sin(i2) * Math.sin(a2));
        double dy = rf * m2 * (Math.sin(i1) * Math.cos(a1) + Math.sin(i2) * Math.cos(a2));
        double dz = rf * m2 * (Math.cos(i1) + Math.cos(i2));
        t2.setDxTN(prf.getX() + dx);
        t2.setDyTN(prf.getY() + dy);
        t2.setDZ(t1.getDZ() + dz);  // TVD positive downwards
        Point p = new Point(t2.getDxTN(), t2.getDyTN(), prf.getZ() - dz); // downwards negative
        t2.setPoint(p); // in local azimuthal equidistant CRS
        // taken from http://www.drillingformulas.com/dogleg-severity-calculationbased-on-radius-of-curvature-method/
        // https://directionaldrillingart.blogspot.com/2015/09/directional-surveying-calculations.html
        // {cos-1 [(cos I1 x cos I2) + (sin I1 x sin I2) x cos (Az2 – Az1)]} x (100 ÷ MD)
        //double dls = rad2deg * Math.acos(Math.cos(i1) * Math.cos(i2) + Math.sin(i1) * Math.sin(i2) * Math.cos(a2 - a1)) / dmd;
        double dls = rad2deg*b / dmd;
        t2.setDls(dls);
        return p;
    }
}
