package org.opengroup.osdu.crs.sis.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.geometry.DirectPosition2D;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.crs.AbstractCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.apache.sis.referencing.operation.transform.MathTransforms;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengroup.osdu.crs.model.ISingleTrf;
import org.opengroup.osdu.crs.sis.AreaOfUseUtils;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.operation.OperationResponse;

public class SingleWGS84TransformFromCode implements IWGS84Transform {

    private static final Logger LOGGER = Logger.getLogger(SingleWGS84TransformFromCode.class.getName());

    private boolean longLatOrder = true;
    private CoordinateOperation datumOperation = null;
    private CoordinateReferenceSystem crs;
    private MathTransform xyToWGS84Transform = null;
    private MathTransform wgs84ToXYTransform = null;

    private boolean negativeScale = false;
    private String xyCRSName;
    private ISingleTrf transform;
    private GeographicBoundingBox transformToWGS84BoundingBox;
    private GeographicBoundingBox transformFromWGS84BoundingBox;
    private boolean checkAreaOfUse;

    public SingleWGS84TransformFromCode(ISisCrs sisCrs, ISingleTrf transform, boolean checkAreaOfUse) throws Exception {
        this.transform = transform;
        datumOperation = transform.getTransformOperation().getFromWGS84Operation();

        crs = sisCrs.getCoordinateReferenceSystem();        
        negativeScale = sisCrs.isNegativeScale();

        xyCRSName = sisCrs.getName();

        /**
         * Normalize the X, Y axis directions order.
         */
        crs = AbstractCRS.castOrCopy(crs).forConvention(AxesConvention.DISPLAY_ORIENTED);        
        /*
         * Check if the lat/long are specified in the proper order
         */
        CoordinateSystemAxis firstAxis = datumOperation.getSourceCRS().getCoordinateSystem().getAxis(0);

        if (firstAxis.getAbbreviation().toLowerCase().contains("lat")) {
            longLatOrder = false;
        }
        this.checkAreaOfUse = checkAreaOfUse;
    }

    @Override
    public double[] transformSingleXYPointToWGS84(double x, double y, double z) throws FactoryException, MismatchedDimensionException, TransformException {

        // Temporary workaround because library does not support negative scale factors
        if (negativeScale) {
            x = -x;
            y = -y;
        }

        if (xyToWGS84Transform == null) {
            initializeXYtoWGS84Transform();
        }

        DirectPosition source = new DirectPosition2D(x, y);
        DirectPosition target = xyToWGS84Transform.transform(source, null);
        double[] coordinate = target.getCoordinate();

        /*
         * reorient to Long/Lat if first axis is Latitude
         */
        if (!longLatOrder) {
            double value = coordinate[0];
            coordinate[0] = coordinate[1];
            coordinate[1] = value;
        }

        if (transformToWGS84BoundingBox != null) {
            if (!AreaOfUseUtils.isPointInAreaOfUse(coordinate[0], coordinate[1], transformToWGS84BoundingBox)) {
                throw new TransformException("Point" + coordinate[0] + "," + coordinate[1] + " is outside the area of use: " + transformToWGS84BoundingBox);
            }
        }
        double[] response = new double[3];
        response[0] = coordinate[0];
        response[1] = coordinate[1];
        response[2] = z;

        return response;
    }

    @Override
    public double[] transformSingleWGS84PointToXY(double longitude, double latitude, double z) throws FactoryException, MismatchedDimensionException, TransformException {
        if (this.wgs84ToXYTransform == null) {
            initializeWGS84ToXYTransform();
        }

        /*
         * reorient to Long/Lat if first axis is Latitude
         */
        double x = longitude;
        double y = latitude;
        if (!longLatOrder) {
            x = latitude;
            y = longitude;
        }

        if (transformFromWGS84BoundingBox != null) {
            if (!AreaOfUseUtils.isPointInAreaOfUse(longitude, latitude, transformFromWGS84BoundingBox)) {
                throw new TransformException("Point" + longitude + "," + latitude + " is outside the area of use: " + transformToWGS84BoundingBox);
            }
        }

        DirectPosition source = new DirectPosition2D(x, y);
        DirectPosition target = wgs84ToXYTransform.transform(source, null);
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
    }

    @Override
    public OperationResponse transformWGS84PointsToXY(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        int successfulConversionCount = 0;
        try {
            if (this.wgs84ToXYTransform == null) {
                initializeWGS84ToXYTransform();
            }

            if (transformFromWGS84BoundingBox != null) {
                for (int i = 0; i < numPoints; i++) {
                    double currentX = xyValues[i * 2];
                    double currentY = xyValues[(i * 2) + 1];
                    if (!AreaOfUseUtils.isPointInAreaOfUse(currentX, currentY, transformFromWGS84BoundingBox)) {
                        xyValues[i * 2] =  Double.NaN;
                        xyValues[(i * 2) + 1] = Double.NaN;
                        zValues[i] = Double.NaN;
                    }
                }
            }            
            
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
                //try to convert all points at 
                xyToWGS84Transform.transform(pointsToConvert, 0, convertedPoints, 0, numPoints);
            } catch (Exception ex) {
                //will need to convert each point individually
                return fallbackTransformWGS84PointsToXY(xyValues, zValues);
            }

            if (negativeScale) {
                for (int i = 0; i < convertedPoints.length; i++) {
                    convertedPoints[i] = -convertedPoints[i];
                }
            }
            System.arraycopy(convertedPoints, 0, xyValues, 0, convertedPoints.length);

            for (int i = 0; i < numPoints; i++) {
                double currentX = xyValues[i * 2];
                double currentY = xyValues[(i * 2) + 1];
                if (Double.isNaN(currentX) || Double.isNaN(currentY)) {
                    xyValues[i * 2] = Double.NaN;
                    xyValues[(i * 2) + 1] = Double.NaN;
                    zValues[i] = Double.NaN;
                } else {
                    successfulConversionCount++;
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Can't transform points", ex);
        }

        List<String> operations = new ArrayList<>();
        operations.add(String.format("transformation %s to %s using %s; %d points successfully transformed", "GCS_WGS_1984", xyCRSName,
                transform.getName(), successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);

    }

    private OperationResponse fallbackTransformWGS84PointsToXY(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        int successfulConversionCount = 0;
        for (int i = 0; i < numPoints; i++) {
            double currentX = xyValues[i * 2];
            double currentY = xyValues[(i * 2) + 1];
            double currentZ = zValues[i];
            if (Double.isNaN(currentX) || Double.isNaN(currentY)) {
                xyValues[i * 2] = Double.NaN;
                xyValues[(i * 2) + 1] = Double.NaN;
                zValues[i] = Double.NaN;
                continue;
            }
            try {
                double[] point = transformSingleWGS84PointToXY(currentX, currentY, currentZ);
                xyValues[i * 2] = point[0];
                xyValues[(i * 2) + 1] = point[1];
                zValues[i] = point[2];
                successfulConversionCount++;

            } catch (Exception ex) {
                xyValues[i * 2] = Double.NaN;
                xyValues[(i * 2) + 1] = Double.NaN;
                zValues[i] = Double.NaN;
            }
        }
        List<String> operations = new ArrayList<>();
        operations.add(String.format("transformation %s to %s using %s; %d points successfully transformed", "GCS_WGS_1984", xyCRSName,
                transform.getName(), successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);
    }

    @Override
    public OperationResponse transformXYPointsToWGS84(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        int successfulConversionCount = 0;
        try {
            if (this.xyToWGS84Transform == null) {
                initializeXYtoWGS84Transform();
            }

            double[] pointsToConvert = new double[xyValues.length];
            if (negativeScale) {
                for (int i = 0; i < xyValues.length; i++) {
                    pointsToConvert[i] = -xyValues[i];
                }
            } else {
                System.arraycopy(xyValues, 0, pointsToConvert, 0, xyValues.length);
            }

            double[] convertedPoints = new double[xyValues.length];
            try {
                //try to convert all points at 
                xyToWGS84Transform.transform(pointsToConvert, 0, convertedPoints, 0, numPoints);
            } catch (Exception ex) {
                //will need to convert each point individually
                return fallbackTransformXYPointsToWGS84(xyValues, zValues);
            }

            if (!longLatOrder) {
                for (int i = 0; i < numPoints; i++) {
                    xyValues[i * 2] = convertedPoints[(i * 2) + 1];
                    xyValues[(i * 2) + 1] = convertedPoints[i * 2];
                }
            } else {
                System.arraycopy(convertedPoints, 0, xyValues, 0, convertedPoints.length);
            }
            
            for (int i = 0; i < numPoints; i++) {
                double currentX = xyValues[i * 2];
                double currentY = xyValues[(i * 2) + 1];
                
                if (Double.isNaN(currentX) || Double.isNaN(currentY)) {
                    xyValues[i * 2] = Double.NaN;
                    xyValues[(i * 2) + 1] = Double.NaN;
                    zValues[i] = Double.NaN;
                } else if (transformToWGS84BoundingBox != null && !AreaOfUseUtils.isPointInAreaOfUse(currentX, currentY, transformToWGS84BoundingBox)) {
                    xyValues[i * 2] = Double.NaN;
                    xyValues[(i * 2) + 1] = Double.NaN;
                    zValues[i] = Double.NaN;
                } else {
                    successfulConversionCount++;
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Can't transform points", ex);
        }

        List<String> operations = new ArrayList<>();
        operations.add(String.format("transformation %s to %s using %s; %d points successfully transformed", xyCRSName, "GCS_WGS_1984",
                transform.getName(), successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);
    }

    private OperationResponse fallbackTransformXYPointsToWGS84(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        int successfulConversionCount = 0;
        for (int i = 0; i < numPoints; i++) {
            double currentX = xyValues[i * 2];
            double currentY = xyValues[(i * 2) + 1];
            double currentZ = zValues[i];
            if (Double.isNaN(currentX) || Double.isNaN(currentY)) {
                xyValues[i * 2] = Double.NaN;
                xyValues[(i * 2) + 1] = Double.NaN;
                zValues[i] = Double.NaN;
                continue;
            }
            try {
                double[] point = transformSingleXYPointToWGS84(currentX, currentY, currentZ);
                xyValues[i * 2] = point[0];
                xyValues[(i * 2) + 1] = point[1];
                zValues[i] = point[2];
                successfulConversionCount++;

            } catch (Exception ex) {
                xyValues[i * 2] = Double.NaN;
                xyValues[(i * 2) + 1] = Double.NaN;
                zValues[i] = Double.NaN;
            }
        }
        List<String> operations = new ArrayList<>();
        operations.add(String.format("transformation %s to %s using %s; %d points successfully transformed", xyCRSName, "GCS_WGS_1984",
                transform.getName(), successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);
    }

    private void initializeXYtoWGS84Transform() throws MismatchedDimensionException, FactoryException {
        CoordinateOperation xyToDatumOperation = CRS.findOperation(crs, datumOperation.getSourceCRS(), null);
        if (checkAreaOfUse) {
            transformToWGS84BoundingBox = AreaOfUseUtils.getTransformBoundingBox(datumOperation);
        }
        /*
         * We have two operations: from my source CRS to the CRS expected
         * by the operation as inputs, then the operation itself. We want
         * the concatenation of those two steps:
         */
        MathTransform step1 = xyToDatumOperation.getMathTransform();
        MathTransform step2 = datumOperation.getMathTransform();
        xyToWGS84Transform = MathTransforms.concatenate(step1, step2);
    }

    private void initializeWGS84ToXYTransform() throws MismatchedDimensionException, FactoryException, NoninvertibleTransformException {
        CoordinateOperation datumToXYOperation = CRS.findOperation(datumOperation.getSourceCRS(), crs, null);
        if (checkAreaOfUse) {
            transformFromWGS84BoundingBox = AreaOfUseUtils.getTransformBoundingBox(datumOperation);
        }
        /*
         * We have two operations: from my source CRS to the CRS expected
         * by the operation as inputs, then the operation itself. We want
         * the concatenation of those two steps:
         */
        MathTransform step1 = datumOperation.getMathTransform().inverse();
        MathTransform step2 = datumToXYOperation.getMathTransform();

        wgs84ToXYTransform = MathTransforms.concatenate(step1, step2);
    }
}
