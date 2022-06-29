package org.opengroup.osdu.crs.sis;

import java.util.Arrays;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengroup.osdu.crs.sis.operation.CRSProjectionOperation;
import org.opengroup.osdu.crs.sis.operation.OperationResponse;

public class SisTransformations {

    public static void projToGeog(ISisCrs projectionSISCrs, int numberOfPoints, double[] xyPoints) {
        try {
            CoordinateReferenceSystem coordianteReferenceSystem = projectionSISCrs.getCoordinateReferenceSystem();
            if (coordianteReferenceSystem == null) {
                //for now since apache sis does not support azimuth projection
                return;
            }
            if (!(coordianteReferenceSystem instanceof ProjectedCRS)) {
                throw new IllegalArgumentException("crs is not a projection");
            }
            if (xyPoints.length /2 != numberOfPoints) {
                throw new IllegalArgumentException("numberOfPoints does not match what is expected");
            }
            ProjectedCRS projectedCRS = (ProjectedCRS) coordianteReferenceSystem;
            GeographicCRS baseCRS = projectedCRS.getBaseCRS();

            ISisCrs baseSISCrs = new SisCrs(baseCRS, projectionSISCrs.isNegativeScale(), null, null, null);
            CRSProjectionOperation projectionOperation = new CRSProjectionOperation(projectionSISCrs, baseSISCrs, null);

            double[] zValues = new double[numberOfPoints];
            Arrays.fill(zValues, 0, numberOfPoints, 0);

            OperationResponse response = projectionOperation.convertPoints(xyPoints, zValues);
            if (response.getSuccessCount() < numberOfPoints) {
                throw new IllegalArgumentException("Can't convert from proj to geog");
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can't convert from proj to geog", ex);
        }
    }

    public static void geogToProj(ISisCrs projectionSISCrs, int numberOfPoints, double[] xyPoints) {
        try {
            CoordinateReferenceSystem coordianteReferenceSystem = projectionSISCrs.getCoordinateReferenceSystem();
            if (coordianteReferenceSystem == null) {
                //for now since apache sis does not support azimuth projection
                return;
            }
            if (!(coordianteReferenceSystem instanceof ProjectedCRS)) {
                throw new IllegalArgumentException("crs is not a projection");
            }
            if (xyPoints.length /2 != numberOfPoints) {
                throw new IllegalArgumentException("numberOfPoints does not match what is expected");
            }
            ProjectedCRS projectedCRS = (ProjectedCRS) coordianteReferenceSystem;
            GeographicCRS baseCRS = projectedCRS.getBaseCRS();

            ISisCrs baseSISCrs = new SisCrs(baseCRS, projectionSISCrs.isNegativeScale(), null, null, null);
            CRSProjectionOperation projectionOperation = new CRSProjectionOperation(baseSISCrs, projectionSISCrs, null);

            double[] zValues = new double[numberOfPoints];
            Arrays.fill(zValues, 0, numberOfPoints, 0);
            OperationResponse response = projectionOperation.convertPoints(xyPoints, zValues);
            if (response.getSuccessCount() < numberOfPoints) {
                throw new IllegalArgumentException("Can't convert from proj to geog");
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can't convert from geog to proj", ex);
        }
    }
}
