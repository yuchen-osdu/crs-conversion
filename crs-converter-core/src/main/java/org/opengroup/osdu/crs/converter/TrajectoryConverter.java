package org.opengroup.osdu.crs.converter;

import org.opengroup.osdu.crs.model.*;
import org.opengroup.osdu.crs.util.Constants;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeodeticCRS;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.crs.api.exception.BadRequestException;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;

import static org.opengroup.osdu.crs.model.ReferenceConverter.parseSpatialReference;
import static org.opengroup.osdu.crs.model.ReferenceConverter.parseUnitReference;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.SisTransformations;

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
            double gridConvergence, to_gn, to_tn;
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
            computeScaleFactor(response);
            computeConvergence(response);


            ConvertTrajectoryResponse siResponse = normalizeTrajectory(response, state);
            Point referencePoint = new Point(0.0, 0.0, siResponse.getStations().get(0).getPoint().getZ());
            if (callTrajectoryEngineService(siResponse, referencePoint, state)) {
                // add method to compute interpolation based on MD_i input
                computeInterpolationForMDiInput(request,referencePoint,response);
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

    private ConvertTrajectoryResponse computeInterpolationForMDiInput(ConvertTrajectoryRequest request,Point referencePoint,ConvertTrajectoryResponse response){
        /*
        In a second pass, for each desired MD_i[i],
            a. Find the station before and after MD_i[i].
            b. Interpolate the Dog Leg with the equations provided at the desired MD_i.
            c. Compute the interpolated INC_i and AZI_i.
            d. Compute the local offsets dx,dy,dz.
            e. Add those offsets to the previous (real) station.
               Output calculated (incl. interpolated) stations in an array stations_i.

               (DL = 2*sin-1 {sqrt[ sin2((I2-I1)/2) + sin(I1)*sin(I2)* sin2((A2-A1)/2) ]}) // SPE84246 recommended
               DLi = DL * (Mi-M1) / (M2-M1),
                The inclination and azimuth are interpolated using the interpolated Dog Leg DLi as:
                    Ii = I1   									// if DLi=0
                       cos-1 [ (sin(DL-DLi)/sin(DL))*cos(I1) + (sin(DLi)/sin(DL)*cos(I2) ]    	// else
                Ai = A1 									// if DLi=0
                                      ATAN2  (sin(I1)*sin(A1)*sin(DL-DLi) + sin(I2)*sin(A2)* sin(DLi) ,		// else
                         sin(I1)*cos(A1)*sin(DL-DLi) + sin(I2)*cos(A2)* sin(DLi) )


                         RFi = 	1,			if DLi=0
                        2*tan(DLi/2) / DLi,	else
                        Δn1, i = ni – n1 = ΔMi * (RFi/2) * (sin(I1)*cos(A1) + sin(Ii)*cos(Ai))
                        Δe1, i = ei – e1 = ΔMi * (RFi/2) * (sin(I1)*sin(A1) + sin(Ii)*sin(Ai))
                        Δd1, i = di – d1 = ΔMi * (RFi/2) * (cos(I1) + cos(Ii))

        */
        List<MinimumDepthInterval> MD_i = request.getMD_i();
        List<TrajectoryStationOut> stationsList = response.getStations();
        int count=1,innerCount=0;
        for(count=1;count<stationsList.size();count++) {
            TrajectoryStationOut outTrajectoryStation = stationsList.get(count);
                TrajectoryStationOut inTrajectoryStation = stationsList.get(innerCount);

            double i2=outTrajectoryStation.getInclination();
            double i1=inTrajectoryStation.getInclination();
            double a2=outTrajectoryStation.getAzimuthTN();
            double a1=inTrajectoryStation.getAzimuthTN();
            double dl=  2 * Math.asin(Math.sqrt(Math.pow(Math.sin((i2-i1)/2),2) + Math.sin(i1) * Math.sin(i2) * Math.pow(Math.sin((a2-a1)/2),2)));
            double m2= outTrajectoryStation.getMd();
            double m1= inTrajectoryStation.getMd();
            MinimumDepthInterval md_i = MD_i.get(innerCount);
            double dli = dl * (md_i.getMd_i()-m1)/(m2-m1);
            double ii;
            double ai;
            double rfi;
            if(dli==0){
                ii = i1;
                ai = a1;
                rfi = 1;
            }else{
                ii = Math.acos((Math.sin(dl-dli)/Math.sin(dl))*Math.cos(i1) + (Math.sin(dli)/Math.sin(dl))*Math.cos(i2));
                ai = Math.atan2(Math.sin(i1)*Math.sin(a1)*Math.sin(dl-dli) + Math.sin(i2)*Math.sin(a2)*Math.sin(dli) ,
                            Math.sin(i1)*Math.cos(a1)*Math.sin(dl-dli) + Math.sin(i2)*Math.cos(a2)*Math.sin(dli));
                rfi = 2*Math.tan(dli/2)/dli;
            }
            double mi = md_i.getMd_i() - m1;
            double ni = mi * (rfi/2) * (Math.sin(i1)*Math.cos(a1) + Math.sin(ii)*Math.cos(ai));
            double ei = mi * (rfi/2) * (Math.sin(i1)*Math.sin(a1) + Math.sin(ii)*Math.sin(ai));
            double di = mi * (rfi/2) * (Math.cos(i1)*Math.sin(ii));
            outTrajectoryStation.setDxTN(referencePoint.getX()+ni);
            outTrajectoryStation.setDyTN(referencePoint.getY()+ei);
            outTrajectoryStation.setDZ(inTrajectoryStation.getDZ()+di);
            outTrajectoryStation.setPoint(new Point(outTrajectoryStation.getDxTN(), outTrajectoryStation.getDyTN(),referencePoint.getZ()-di));
            outTrajectoryStation.setInclination(ii);
            outTrajectoryStation.setAzimuthTN(ai);
            stationsList.add(outTrajectoryStation);
        }
        response.setStations_i(stationsList);
        return response;
    }

    private ConvertTrajectoryResponse computeScaleFactor(ConvertTrajectoryResponse response){
        List<TrajectoryStationOut> stationsList = response.getStations();
        TrajectoryStationOut first_station = stationsList.get(0);
        TrajectoryStationOut last_station = stationsList.get(stationsList.size()-1);

        Point lastStationPoint = last_station.getPoint();
        double yn = lastStationPoint.getY();
        double xn = lastStationPoint.getX();

        Point firstStationPoint = first_station.getPoint();
        double y0 = firstStationPoint.getY();
        double x0 = firstStationPoint.getX();
        //dGN = sqrt(x[2]-x[1])^2 + y[2]-y[1])^2)
        //dTN = sqrt((dxTNn-dxTN1)^2 + (dyTNn-dyTN1)^2)
        double dGN = Math.sqrt(Math.pow(yn-y0,2) + Math.pow(xn-x0,2));
        double dTN = Math.sqrt(Math.pow(last_station.getDxTN()-first_station.getDxTN(),2) + Math.pow(last_station.getDyTN()-first_station.getDyTN(),2));
        double scaleFactor = dGN/dTN;

        for(int count=0;count<stationsList.size();count++){
            TrajectoryStationOut to = stationsList.get(count);
            to.setScalefactor(scaleFactor);
            stationsList.add(to);
        }
        response.setStations(stationsList);
        return response;
    }

    private ConvertTrajectoryResponse computeConvergence(ConvertTrajectoryResponse response){
        // GC=TN-GN, and wrapped to interval (-180,+180) by "if convergence<-180 then convergence+=360; if convergence>180 then convergence-=360"
        List<TrajectoryStationOut> stationsList = response.getStations();
        for(int count=0;count<stationsList.size();count++){
            TrajectoryStationOut to = stationsList.get(count);
            double gridConvergence = to.getAzimuthTN() - to.getAzimuthGN();
            if(gridConvergence < -180 ){
                 gridConvergence+=360;
            }else if(gridConvergence > 180){
                 gridConvergence-=360;
            }
            to.setConvergence(gridConvergence);
            stationsList.add(to);
        }
        response.setStations(stationsList);
        return response;
    }

    private ConvertTrajectoryResponse computeUnscaledValuesForXAndY(ConvertTrajectoryResponse response){
        //To calculated path can be “unscaled” by applying this factor in reverse
        //as follows for i=1:N:
        //x_unscaled[i] = x[1] + (x[i] - x[1]) / psf
        //y_unscaled[i] = y[1] + (y[i] - y[1]) / psf
        List<TrajectoryStationOut> stationsList = response.getStations();
        int count=0;
        for(count=0;count<stationsList.size();count++) {
            TrajectoryStationOut to = stationsList.get(count);
            double scaleFactor = to.getScalefactor();
            Point firstStationPoint = to.getPoint();
            double y0 = firstStationPoint.getY();
            double x0 = firstStationPoint.getX();
            double x = x0 + (to.getPoint().getX() - x0) / scaleFactor;
            double y = y0 + (to.getPoint().getY() - y0) / scaleFactor;
            to.setPoint(new Point(x, y, to.getPoint().getZ()));
            stationsList.add(to);
        }
        response.setStations(stationsList);
        return response;
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

    private double[] extractElevationsFromResponse(ConvertTrajectoryResponse response) {
        double[] elevations = new double[response.getStations().size()];
        int i = 0;
        for (TrajectoryStationOut item : response.getStations()) {
            elevations[i] = item.getPoint().getZ();
            i++;
        }
        return elevations;
    }

    private ConvertTrajectoryResponse normalizeTrajectory(ConvertTrajectoryResponse response, TrajectoryComputationState state) {
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

    private void deNormalizeTrajectory(ConvertTrajectoryResponse siResponse, ConvertTrajectoryResponse response, TrajectoryComputationState state) {
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

    private boolean callTrajectoryEngineService(
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

    private void convertToWgs84(ConvertTrajectoryResponse response, TrajectoryComputationState state) {
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

    private void convertPointsLmp(ConvertTrajectoryResponse response, TrajectoryComputationState state) {
        try {
            int nofPoints = response.getStations().size();
            double[] posXY = new double[2 * nofPoints];
            double[] aziGN = new double[nofPoints];
            double A = getSemiMajorAxis(state.getGeogCS());                         // A is semi-major axis
            double RF = getInvFlattening(state.getGeogCS());                        // RF is reciprocal of flattening
            double rad = 180 / Math.PI;                                             // Degrees in a radian
            double B = A * (1 - 1 / RF);                                            // B is semi-minor axis
            double E2 = 1 - (B * B) / (A * A);                                      // E2 is eccentricity squared

            TrajectoryStationOut station = response.getStations().get(0);
            double Edeplast = station.getDxTN();
            double Ndeplast = station.getDyTN();
            double[] surfaceXY = {state.getReferencePoint().getX(), state.getReferencePoint().getY()};
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
            for (int i = 1; i < response.getStations().size(); i++) {
                station = response.getStations().get(i);
                double LATRAD = latlast / rad; //% Degrees to radians
                double sinLATRADsqared = Math.pow(Math.sin(LATRAD), 2);
                double R = A * (1.0 - E2) / (Math.pow(Math.sqrt(1.0 - E2 * sinLATRADsqared), 3)); // % Radius in the meridian
                double N = A / Math.sqrt(1 - E2 * sinLATRADsqared); //% Radius in the prime vertical
                double TVD = refZ - station.getPoint().getZ();             // data(dex,4); //% Grab the TVD
                double Edep = station.getDxTN(); // data(dex,6);
                double Ndep = station.getDyTN(); // data(dex,5); //% Grab the departures
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
    
    private void convertPoints(ConvertTrajectoryResponse response, ISisCrs aziEqu, TrajectoryComputationState state) {
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

    boolean isRequestValid(ConvertTrajectoryRequest request, TrajectoryComputationState state) {
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

    private void minimumCurvature(Point referencePoint, List<TrajectoryStationOut> stations) {
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

    private Point minimumCurvaturePair(Point prf, List<TrajectoryStationOut> stations, int index) {
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
        double b = Math.acos(Math.cos(i2 - i1) - Math.sin(i1) * Math.sin(i2) * (1.0 - Math.cos(a2 - a1)));
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
        double dls = rad2deg * Math.acos(Math.cos(i1) * Math.cos(i2) + Math.sin(i1) * Math.sin(i2) * Math.cos(a2 - a1)) / dmd;
        t2.setDls(dls);
        return p;
    }
}
