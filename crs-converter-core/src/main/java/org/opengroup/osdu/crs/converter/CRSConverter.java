package org.opengroup.osdu.crs.converter;

import java.util.List;
import org.opengroup.osdu.crs.GeoJson.GeoJsonBase;
import org.opengroup.osdu.crs.GeoJson.GeoJsonCoordinates;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.model.*;
import static org.opengroup.osdu.crs.model.ReferenceConverter.parseSpatialReference;
import org.opengroup.osdu.crs.sis.operation.CRSCoordinateOperationFactory;
import org.opengroup.osdu.crs.sis.operation.ICRSCoordinateOperation;
import org.opengroup.osdu.crs.sis.operation.OperationResponse;
import org.opengroup.osdu.crs.util.Constants;
import org.springframework.stereotype.Service;

@Service
public class CRSConverter implements ICRSConverter {

    private static final String METER = "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}";

    @Override
    public ConvertPointsResponse convertPoint(String from, String to, double[] xyCoordinates, double[] zCoordinates) {

        if ((from == null) || (from.isEmpty())
                || (to == null) || (to.isEmpty())
                || (xyCoordinates == null) || (xyCoordinates.length == 0)
                || (zCoordinates == null) || (zCoordinates.length == 0)) {
            throw new IllegalArgumentException(Constants.ERROR_MSG_BAD_INPUT);
        }

        if (xyCoordinates.length / 2 != zCoordinates.length) {
            throw new IllegalArgumentException(Constants.ERROR_MSG_INPUT_ARRAY_MISMATCH);
        }
        ICrs sourceCRS = null;
        ICrs targetCRS = null;
        IItem raw = parseSpatialReference(from);
        if (raw instanceof ICrs) {
            sourceCRS = (ICrs) raw;
        }
        raw = parseSpatialReference(to);
        if (raw instanceof ICrs) {
            targetCRS = (ICrs) raw;
        }

        if (sourceCRS == null || targetCRS == null || !sourceCRS.isValid() || !targetCRS.isValid()) {
            throw new IllegalArgumentException(Constants.ERROR_MSG_INVALID_INPUT_CRS_SPECIFICATION);
        }
        ConvertOperationState opState = new ConvertOperationState(sourceCRS, targetCRS, xyCoordinates, zCoordinates);
        int successCount = zCoordinates.length;
        CRSCoordinateOperationFactory opFactory = new CRSCoordinateOperationFactory();
        List<ICRSCoordinateOperation> operations = opFactory.createOperations(sourceCRS, targetCRS);
        boolean shouldEnable3DConversion = shouldEnable3DConversion(operations);
        double[] initialZCoordinates = new double[zCoordinates.length];
        System.arraycopy(zCoordinates, 0, initialZCoordinates, 0, zCoordinates.length);
        for (ICRSCoordinateOperation currentOperation : operations) {
            if (shouldEnable3DConversion) {
                currentOperation.enable3DPointConversion(true);
            }
            OperationResponse response = currentOperation.convertPoints(xyCoordinates, zCoordinates);
            opState.getOperations().addAll(response.getOperationsApplied());
            successCount = response.getSuccessCount();
        }

        if (shouldEnable3DConversion) {
            System.arraycopy(initialZCoordinates, 0, zCoordinates, 0, initialZCoordinates.length);
        } 

        ConvertPointsResponse response = new ConvertPointsResponse();
        response.setSuccessCount(successCount);
        if (opState.getOperations().size() == 0) {
            opState.getOperations().add("no operation applied");
        }
        response.setOperationsApplied(opState.getOperations());
        return response;
    }

    @Override
    public ConvertGeoJsonResponse convertGeoJson(GeoJsonFeatureCollection featureCollection, String toCrs, String requestedToUnitZ) {
        ConvertGeoJsonResponse response = new ConvertGeoJsonResponse();
        if (featureCollection.isValid()) {
            String fromCrs, fromUnitZ, toUnitZ = requestedToUnitZ;
            GeoJsonBase.GeoJsonVariant targetVariant;
            GeoJsonCoordinates coordinates = featureCollection.extractCoordinates();
            if (featureCollection.getGeoJsonVariant() == GeoJsonBase.GeoJsonVariant.GEO_JSON) {
                fromCrs = Constants.WGS84;
                fromUnitZ = METER;
            } else {
                fromCrs = featureCollection.getPersistableReferenceCrs();
                fromUnitZ = featureCollection.getPersistableReferenceUnitZ();
            }
            ConvertPointsResponse internal_response = convertPoint(fromCrs, toCrs, coordinates.getXys(), coordinates.getZ_s());
            if (targetIsWGS84(toCrs)) {
                targetVariant = GeoJsonBase.GeoJsonVariant.GEO_JSON;
                featureCollection.setPersistableReferenceCrs(null);
                toUnitZ = METER;
            } else {
                targetVariant = GeoJsonBase.GeoJsonVariant.ANY_CRS_GEO_JSON;
                featureCollection.setPersistableReferenceCrs(toCrs);
            }
            String any_unit_conversion = coordinates.convertUnits(fromUnitZ, toUnitZ);
            coordinates.setIndex(0);
            featureCollection.replaceCoordinates(coordinates);
            featureCollection.setGeoJsonVariant(targetVariant);
            response.setSuccessCount(internal_response.getSuccessCount());
            response.setTotalCount(coordinates.getLength());
            response.setFeatureCollection(featureCollection);
            if (featureCollection.getDimension() > 2) {
                internal_response.getOperationsApplied().add(any_unit_conversion);
                featureCollection.setPersistableReferenceUnitZ(toUnitZ);
            }
            featureCollection.updateBbox();
            response.setOperationsApplied(internal_response.getOperationsApplied());
        } else {
            throw new IllegalArgumentException(Constants.ERROR_MSG_BAD_INPUT);
        }
        return response;
    }

    private boolean targetIsWGS84(String toCrs) {
        ILateBoundCrs wgs84 = (ILateBoundCrs) parseSpatialReference(Constants.WGS84);
        wgs84.isValid();
        IItem raw = parseSpatialReference(toCrs);
        if (raw instanceof ICrs) {
            ICrs targetCRS = (ICrs) raw;
            if (targetCRS.getType() == CRSType.LATE_BOUND && targetCRS instanceof ILateBoundCrs) {
                ILateBoundCrs lb_crs = (ILateBoundCrs) targetCRS;
                if (lb_crs.isGeographicCrs()) {
                    return lb_crs.getBaseGeographicCrs().isEqual(wgs84.getBaseGeographicCrs());
                }
            }
        }
        return false;
    }

    private boolean shouldEnable3DConversion(List<ICRSCoordinateOperation> operations) {
        for (int i = 0; i < operations.size() - 1; i++) {
            ICRSCoordinateOperation currentOperation = operations.get(i);
            ICRSCoordinateOperation nextOperation = operations.get(i + 1);
            if (currentOperation.supports3DPointConversion() && nextOperation.supports3DPointConversion()) {
                return true;
            }
        }
        return false;
    }
}