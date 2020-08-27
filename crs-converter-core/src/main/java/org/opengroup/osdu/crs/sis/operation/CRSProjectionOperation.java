package org.opengroup.osdu.crs.sis.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.geometry.DirectPosition2D;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.crs.AbstractCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.opengis.geometry.DirectPosition;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengroup.osdu.crs.sis.ISisCrs;

public class CRSProjectionOperation implements ICRSCoordinateOperation {

    private static final Logger LOGGER = Logger.getLogger(CRSProjectionOperation.class.getName());

    private CoordinateReferenceSystem fromCRS;
    private CoordinateReferenceSystem toCRS;
    private boolean negativeScale = false;
    private final CoordinateOperation projectionOperation;
    private boolean longLatOrder = true;
    private String fromCRSName = "Unknown";
    private String toCRSName = "Unknown";

    public CRSProjectionOperation(ISisCrs fromSisCrs, ISisCrs toSisCrs, GeographicBoundingBox bb) throws Exception {
        this.fromCRS = fromSisCrs.getCoordinateReferenceSystem();
        this.toCRS = toSisCrs.getCoordinateReferenceSystem();
        this.negativeScale = fromSisCrs.isNegativeScale();
        if (toSisCrs.isNegativeScale()) {
            this.negativeScale = !this.negativeScale;
        }

        this.fromCRS = AbstractCRS.castOrCopy(fromCRS).forConvention(AxesConvention.DISPLAY_ORIENTED);
        this.toCRS = AbstractCRS.castOrCopy(toCRS).forConvention(AxesConvention.DISPLAY_ORIENTED);

        this.projectionOperation = CRS.findOperation(fromCRS, toCRS, bb);

        /*
         * Check if the lat/long are specified in the proper order
         */
        CoordinateSystemAxis firstAxis = projectionOperation.getSourceCRS().getCoordinateSystem().getAxis(0);
        if (firstAxis.getAbbreviation().toLowerCase().contains("lat")) {
            longLatOrder = false;
        }
        fromCRSName = fromSisCrs.getName();
        toCRSName = toSisCrs.getName();
    }

    @Override
    public double[] convertSinglePoint(double x, double y, double z) {
        try {
            /*
         * reorient to Long/Lat if first axis is Latitude
             */
            if (!longLatOrder) {
                double temp = x;
                x = y;
                y = temp;
            }

            DirectPosition source = new DirectPosition2D(x, y);
            DirectPosition target = projectionOperation.getMathTransform().transform(source, null);
            double[] coordinate = target.getCoordinate();
            // Temporary workaround because library does not support negative scale factors
            if (negativeScale) {
                coordinate[0] = -coordinate[0];
                coordinate[1] = -coordinate[1];
            }
            double[] response = new double[3];
            response[0] = coordinate[0];
            response[1] = coordinate[1];
            response[2] = z;
            return response;
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Can't convert point", ex);
            return null;
        }
    }

    @Override
    public OperationResponse convertPoints(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        double[] pointsToConvert = new double[xyValues.length];
        if (!longLatOrder) {
            for (int i = 0; i < numPoints; i++) {
                pointsToConvert[i * 2] = xyValues[(i * 2) + 1];
                pointsToConvert[(i * 2) + 1] = xyValues[i * 2];
            }
        } else {
            System.arraycopy(xyValues, 0, pointsToConvert, 0, xyValues.length);
        }
        double[] convertedPoints = new double[xyValues.length];
        try {
            projectionOperation.getMathTransform().transform(pointsToConvert, 0, convertedPoints, 0, numPoints);
        } catch (Exception ex) {
            //will need to convert each point individually
            return fallbackConvertPoints(xyValues, zValues, numPoints);
        }
        // Temporary workaround because library does not support negative scale factors
        if (negativeScale) {
            for (int i = 0; i < convertedPoints.length; i++) {
                convertedPoints[i] = -convertedPoints[i];
            }
        }

        int successfulConversionCount = 0;
        System.arraycopy(convertedPoints, 0, xyValues, 0, convertedPoints.length);
        for (int i = 0; i < numPoints; i++) {
            double currentX = xyValues[i * 2];
            double currentY = xyValues[(i * 2) + 1];
            if (Double.isNaN(currentX) || Double.isNaN(currentY)) {
                xyValues[i * 2] = Double.NaN;
                xyValues[(i * 2) + 1] = Double.NaN;
                zValues[i] =  Double.NaN;
            } else {
                successfulConversionCount++;
            }
        }

        List<String> operations = new ArrayList<>();
        operations.add(String.format("conversion from %s to %s; %d points converted", fromCRSName, toCRSName, successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);
    }

    private OperationResponse fallbackConvertPoints(double[] xyValues, double[] zValues, int numPoints) {
        int successfulConversionCount = 0;
        for (int i = 0; i < numPoints; i++) {
            try {
                double x = xyValues[i * 2];
                double y = xyValues[i * 2 + 1];
                double z = zValues[i];
                double[] convertedPoint = convertSinglePoint(x, y, z);
                xyValues[i * 2] = convertedPoint[0];
                xyValues[i * 2 + 1] = convertedPoint[1];
                successfulConversionCount++;
            } catch (Exception ex) {
                xyValues[i * 2] = Double.NaN;
                xyValues[i * 2 + 1] = Double.NaN;
            }
        }
        List<String> operations = new ArrayList<>();
        operations.add(String.format("conversion from %s to %s; %d points converted", fromCRSName, toCRSName, successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);
    }
}
