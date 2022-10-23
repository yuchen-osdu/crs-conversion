package org.opengroup.osdu.crs.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.crs.BinGrid.AbstractBinGrid;
import org.opengroup.osdu.crs.BinGrid.AbstractCoordinates;
import org.opengroup.osdu.crs.BinGrid.AbstractSpatialLocation;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.netty.util.internal.StringUtil;


@Service
public class CRSConverter implements ICRSConverter {

	@Autowired
	private JaxRsDpsLog logger;

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
    
	public ConvertBinGridResponse convertBinGrid(String toCrs, AbstractBinGrid inBinGrid) {
		ConvertBinGridResponse outBinGrid = null;

		if (inBinGrid.getBinGridDefinitionMethodTypeID().equals("4Corner")) {

			String type = "";
			outBinGrid = new ConvertBinGridResponse();
			if (toCrs.contains("Projected") && toCrs.contains("4326")) {
				type = "Projected";
				outBinGrid = test(toCrs, inBinGrid, outBinGrid);
			} else if (toCrs.contains("BoundProjected") && !toCrs.contains("4326")) {
				outBinGrid = test(toCrs, inBinGrid, outBinGrid);
			} else {
				if (type.equals("Projected"))
					logger.info("input type is projected but BaseCRS is not WGS 84");
				else
					logger.info(
							"Unexpected. CRS type is BoundProjected but base CRS is WGS 84 which should be of type projected in OSDU");
			}

		} else {

			logger.info(" not a 4Corner");
		}

		return outBinGrid;

	}

	private ConvertBinGridResponse test(String toCrs, AbstractBinGrid inBinGrid, ConvertBinGridResponse outBinGrid) {
		List<AbstractCoordinates> localCoordinates = inBinGrid.getABCDBinGridLocalCoordinates();
		AbstractSpatialLocation spatialcoordinates = inBinGrid.getABCDBinGridSpatialLocation();
		validateLocalCoordinates(localCoordinates);
		String crsId = spatialcoordinates.getAsingestedcoordinates().getCoordinateReferenceSystemID();
		String persistableReference = spatialcoordinates.getAsingestedcoordinates().getPersistableReferenceCrs();

		spatialcoordinates.getAsingestedcoordinates().getFeatures().stream().forEach(features -> {
			int size = features.getGeometry().getCoordinates().getAbstractCoordinates().size();
			if (size != 4)
				logger.info("invalid size for spatial coordinates");
			else {
				features.getGeometry().getCoordinates().getAbstractCoordinates().stream().forEach(splCoordinates -> {
					double xCoordinate = splCoordinates.getX();
					double yCoordinate = splCoordinates.getY();
					double zCoordinate = splCoordinates.getZ();

					localCoordinates.stream().forEach(lclCoordinates -> {
						double iCoordinate = lclCoordinates.getX();
						double jCoordinate = lclCoordinates.getY();
						Map<String, Double> crsParamMap = prepareCrsParam(inBinGrid);
					});

					double xys[] = new double[2];
					xys[0] = xCoordinate;
					xys[1] = yCoordinate;
					double zs[] = new double[1];
					zs[0] = zCoordinate;

					String value = "";
					if (StringUtil.isNullOrEmpty(crsId)) {

						value = crsId;
					} else if (StringUtil.isNullOrEmpty(persistableReference)) {
						value = persistableReference;
					} else {
						logger.info("crsif and PR not given");
					}
					ConvertPointsResponse internal_response = convertPoint(value, toCrs, xys, zs);

					outBinGrid.setOperationsApplied(internal_response.getOperationsApplied());

				});
			}
		});
		return outBinGrid;
	}

	private Map<String, Double> prepareCrsParam(AbstractBinGrid inBinGrid) {

		Map<String, Double> crsParamsMap = new HashMap<>();

		if (inBinGrid.getP6ScaleFactorOfBinGrid() == null)
			crsParamsMap.put("P6ScaleFactorOfBinGrid", 1.00000);
		if (inBinGrid.getP6BinNodeIncrementOnIaxis() == null)

			crsParamsMap.put("P6BinNodeIncrementOnIaxis", 1.0);
		if (inBinGrid.getP6BinNodeIncrementOnJaxis() == null)
			crsParamsMap.put("P6BinNodeIncrementOnJaxis", 1.0);

		return crsParamsMap;

	}

	private boolean validateLocalCoordinates(List<AbstractCoordinates> points) {
		logger.info("Validating BinGrid request");
		if (points != null) {
			if (points.size() != 4)
				return false;

			double tMin = points.get(0).getX();
			double bMin = points.get(0).getY();

			double tMax = points.get(3).getX();
			double bMax = points.get(3).getX();

			AbstractCoordinates secondPoint = points.get(1);
			AbstractCoordinates thirdPoint = points.get(2);
			if (tMax <= tMin || bMax <= bMin) {
				logger.info("Tmax and Bmax are not greater than Tmin and Bmin");
				return false;
			}

			else if (secondPoint.getX() != tMin || secondPoint.getY() != bMax) {
				logger.info("Second point is not Tmin and Bmax");
				return false;
			}

			else if (thirdPoint.getX() != tMax || thirdPoint.getY() != bMin) {
				logger.info("Third point is not Tmax and Bmin");
				return false;
			}
			logger.info("BinGrid request is valid");
		} else
			return false;
		return true;
	}
}