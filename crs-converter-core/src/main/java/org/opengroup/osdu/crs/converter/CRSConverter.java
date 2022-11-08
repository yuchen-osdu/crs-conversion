package org.opengroup.osdu.crs.converter;

import static org.opengroup.osdu.crs.model.ReferenceConverter.parseSpatialReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.crs.BinGrid.AbstractBinGrid;
import org.opengroup.osdu.crs.BinGrid.AbstractFeature;
import org.opengroup.osdu.crs.BinGrid.AbstractSpatialLocation;
import org.opengroup.osdu.crs.BinGrid.MaxMisLocation;
import org.opengroup.osdu.crs.BinGrid.PointProperties;
import org.opengroup.osdu.crs.GeoJson.GeoJsonBase;
import org.opengroup.osdu.crs.GeoJson.GeoJsonCoordinates;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.model.CRSType;
import org.opengroup.osdu.crs.model.ConvertBinGridResponse;
import org.opengroup.osdu.crs.model.ConvertGeoJsonResponse;
import org.opengroup.osdu.crs.model.ConvertOperationState;
import org.opengroup.osdu.crs.model.ConvertPointsResponse;
import org.opengroup.osdu.crs.model.ICrs;
import org.opengroup.osdu.crs.model.IItem;
import org.opengroup.osdu.crs.model.ILateBoundCrs;
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

    private static final String BIN_GRID_METHOD_4_CORNERS = "4Corner";
    private static final String CRS_TYPE_PROJECTED = "Projected";
    private static final String CRS_CODE_4326 = "4326";
    private static final String CRS_TYPE_BOUND_PROJECTED = "BoundProjected";
    private static final Integer CRS_CODE_9666 = 9666;
    private static final Integer CRS_CODE_1049 = 1049;
    private static final String RIGHT_HANDED_NESS = "rightHandedNess";
    private static final String LEFT_HANDED_NESS = "leftHandedNess";
    private static final String KEY_RDELTAI = "RDELTAI";
    private static final String KEY_RDELTAJ = "RDELTAJ";
    private static final String KEY_RAC = "RAC";
    private static final String KEY_RBD = "RBD";
    private static final String KEY_RAB = "RAB";
    private static final String KEY_RCD = "RCD";
    
    
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
		
		ConvertBinGridResponse outBinGrid = new ConvertBinGridResponse();
		outBinGrid.setOutBinGrid(inBinGrid);
        // checking the binGridDefinitionMethodType
		if (inBinGrid.getBinGridDefinitionMethodTypeID().equals(BIN_GRID_METHOD_4_CORNERS)) {
			// checking the size of the input coordinates
			validatePointCoordinates(inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0).getProperties().getPointPropertiesList());
            // Setting the computed values for SchemaParameters
			inBinGrid = prepareSchemaParameters(inBinGrid);
			Double p6ScaleFactorOfBinGrid = inBinGrid.getP6ScaleFactorOfBinGrid();
			Double p6BinNodeIncrementOnIaxis = inBinGrid.getP6BinNodeIncrementOnIaxis();
			Double p6BinNodeIncrementOnJaxis = inBinGrid.getP6BinNodeIncrementOnJaxis();			
			if (p6ScaleFactorOfBinGrid == 0.0) {
				inBinGrid.setP6ScaleFactorOfBinGrid(1.00000);				
			}
			if (p6BinNodeIncrementOnIaxis == 0.0) {
				inBinGrid.setP6BinNodeIncrementOnIaxis(1.0);
			}
			if (p6BinNodeIncrementOnJaxis == 0.0) {
				inBinGrid.setP6BinNodeIncrementOnJaxis(1.0);
			}
						
			String type = "";			
			if (toCrs.contains(CRS_TYPE_PROJECTED) && toCrs.contains(CRS_CODE_4326)) {
				type = CRS_TYPE_PROJECTED;
				outBinGrid = binGridComputation(toCrs, inBinGrid, outBinGrid);
			} else if (toCrs.contains(CRS_TYPE_BOUND_PROJECTED) && !toCrs.contains(CRS_CODE_4326)) {
				outBinGrid = binGridComputation(toCrs, inBinGrid, outBinGrid);
			} else {
				if (type.equals(CRS_TYPE_PROJECTED))
					logger.info("Input CRS type is projected but BaseCRS is not WGS 84");
				else
					logger.info(
							"Unexpected. CRS type is BoundProjected but BaseCRS is WGS 84 which should be of type projected in OSDU");
			}

		} else {
			logger.info("BinGrid method is not a 4Corner type");
			outBinGrid.setOutBinGrid(inBinGrid);	
		}

		return outBinGrid;

	}

	private AbstractBinGrid prepareSchemaParameters(AbstractBinGrid inBinGrid) {
		
		double inlineA = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0).getProperties().getPointPropertiesList().get(0).getInline();
		double inlineB = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(1).getProperties().getPointPropertiesList().get(0).getInline();
		double inlineC = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(2).getProperties().getPointPropertiesList().get(0).getInline();
		double inlineD = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(3).getProperties().getPointPropertiesList().get(0).getInline();
		
		double p6BinGridOriginI = (inlineA + inlineB + inlineC + inlineD) / 4 ;
		
		double crossLineA = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0).getProperties().getPointPropertiesList().get(0).getCrossline();
		double crossLineB = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(1).getProperties().getPointPropertiesList().get(0).getCrossline();
		double crossLineC = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(2).getProperties().getPointPropertiesList().get(0).getCrossline();
		double crossLineD = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(3).getProperties().getPointPropertiesList().get(0).getCrossline();		
		double p6BinGridOriginJ = (crossLineA + crossLineB + crossLineC + crossLineD) / 4 ;
		
		double eastingA = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0).getGeometry().getCoordinates().get(0);
		double eastingB = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(1).getGeometry().getCoordinates().get(0);
		double eastingC = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(2).getGeometry().getCoordinates().get(0);
		double eastingD = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(3).getGeometry().getCoordinates().get(0);		
		double p6BinGridOriginEasting = (eastingA + eastingB + eastingC + eastingD) / 4 ;
		
		double northingA = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0).getGeometry().getCoordinates().get(1);
		double northingB = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(1).getGeometry().getCoordinates().get(1);
		double northingC = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(2).getGeometry().getCoordinates().get(1);
		double northingD = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(3).getGeometry().getCoordinates().get(1);		
		double p6BinGridOriginNorthing = (northingA + northingB + northingC + northingD) / 4 ;
		
		inBinGrid.setP6BinGridOriginI(p6BinGridOriginI);
		inBinGrid.setP6BinGridOriginJ(p6BinGridOriginJ);
		inBinGrid.setP6BinGridOriginEasting(p6BinGridOriginEasting);
		inBinGrid.setP6BinGridOriginNorthing(p6BinGridOriginNorthing);
		
		return inBinGrid;
		
	}

	private ConvertBinGridResponse binGridComputation(String toCrs, AbstractBinGrid inBinGrid,
			ConvertBinGridResponse convertBinGridResponse) {

		AbstractSpatialLocation spatialcoordinates = inBinGrid.getABCDBinGridSpatialLocation();
		String crsId = spatialcoordinates.getAsIngestedcoordinates().getCoordinateReferenceSystemID();
		String persistableReference = spatialcoordinates.getAsIngestedcoordinates().getPersistableReferenceCrs();
		String value = StringUtils.EMPTY;
		if (!StringUtil.isNullOrEmpty(crsId)) {
			value = crsId;
		} else if (!StringUtil.isNullOrEmpty(persistableReference)) {
			value = persistableReference;
		} else {
			logger.info("CRS Id and Persistence Reference not present in the input request");
		}

		int size = spatialcoordinates.getAsIngestedcoordinates().getFeatures().size();
		if (size != 4)
			logger.info("Invalid size for spatial coordinates in the input request");
		else {
			List<String> appliedOperations = new ArrayList<>();
			spatialcoordinates.getAsIngestedcoordinates().getFeatures().stream().forEach(features -> {
				double xCoordinate = features.getGeometry().getCoordinates().get(0);
				double yCoordinate = features.getGeometry().getCoordinates().get(1);
				double zCoordinate = 0.0;
				double xys[] = new double[2];
				xys[0] = xCoordinate;
				xys[1] = yCoordinate;
				double zs[] = new double[1];
				zs[0] = zCoordinate;

				// To be fixed for the convert point operation for the from and to crs
				// ConvertPointsResponse internal_response = convertPoint(value, toCrs, xys,
				// zs);
				// appliedOperations.addAll(internal_response.getOperationsApplied());
			});

			Map<String, Double> spatialCoordinatesComputeMap = RcomputationBetweenPoints(spatialcoordinates);

			Map<String, Double> rDeltaIJMap = rDeltaIandJComputation(spatialCoordinatesComputeMap,
					spatialcoordinates.getAsIngestedcoordinates().getFeatures(),
					inBinGrid.getP6BinNodeIncrementOnIaxis(), inBinGrid.getP6BinNodeIncrementOnJaxis());

			thetaCalculation(inBinGrid, rDeltaIJMap, convertBinGridResponse);
			// convertBinGridResponse.setAppliedOperations(appliedOperations);
		}
		return convertBinGridResponse;
	}
	
	
	private Map<String, Double> RcomputationBetweenPoints(AbstractSpatialLocation spatialcoordinates) {

		Map<String, Double> spatialCoordinatesComputeMap = new HashMap<>();

		List<AbstractFeature> abstractFeatureList = spatialcoordinates.getAsIngestedcoordinates().getFeatures();

		Double coordinateAx = abstractFeatureList.get(0).getGeometry().getCoordinates().get(0);
		Double coordinateAy = abstractFeatureList.get(0).getGeometry().getCoordinates().get(1);

		Double coordinateBx = abstractFeatureList.get(1).getGeometry().getCoordinates().get(0);
		Double coordinateBy = abstractFeatureList.get(1).getGeometry().getCoordinates().get(1);

		Double coordinateCx = abstractFeatureList.get(2).getGeometry().getCoordinates().get(0);
		Double coordinateCy = abstractFeatureList.get(2).getGeometry().getCoordinates().get(1);

		Double coordinateDx = abstractFeatureList.get(3).getGeometry().getCoordinates().get(0);
		Double coordinateDy = abstractFeatureList.get(3).getGeometry().getCoordinates().get(1);

		Double rAC = Math.sqrt(Math.pow((coordinateCy - coordinateAy), 2) + Math.pow((coordinateCx - coordinateAx), 2));
		spatialCoordinatesComputeMap.put(KEY_RAC, rAC);
		Double rBD = Math.sqrt(Math.pow((coordinateDy - coordinateBy), 2) + Math.pow((coordinateDx - coordinateBx), 2));
		spatialCoordinatesComputeMap.put(KEY_RBD, rBD);
		Double rAB = Math.sqrt(Math.pow((coordinateBy - coordinateAy), 2) + Math.pow((coordinateBx - coordinateAx), 2));
		spatialCoordinatesComputeMap.put(KEY_RAB, rAB);
		Double rCD = Math.sqrt(Math.pow((coordinateDy - coordinateCy), 2) + Math.pow((coordinateDx - coordinateCx), 2));
		spatialCoordinatesComputeMap.put(KEY_RCD, rCD);

		return spatialCoordinatesComputeMap;
	}
	
	private Map<String, Double> rDeltaIandJComputation(
			Map<String, Double> spatialCoordinatesComputeMap, List<AbstractFeature> pointCoordinates,Double p6BinNodeIncrementOnIaxis, Double p6BinNodeIncrementOnJaxis) {

		Map<String, Double> rDeltaIJMap = new HashMap<>();

		Integer iA = pointCoordinates.get(0).getProperties().getPointPropertiesList().get(0).getInline();
		Integer jA = pointCoordinates.get(0).getProperties().getPointPropertiesList().get(0).getCrossline();
		
		Integer jB = pointCoordinates.get(1).getProperties().getPointPropertiesList().get(0).getCrossline();

		Integer iC = pointCoordinates.get(2).getProperties().getPointPropertiesList().get(0).getInline();
				
		Double rDeltaI = p6BinNodeIncrementOnIaxis
				* (((spatialCoordinatesComputeMap.get(KEY_RAC) + spatialCoordinatesComputeMap.get(KEY_RBD)) / 2)
						/ (iC - iA));

		rDeltaIJMap.put(KEY_RDELTAI, rDeltaI);
		
		Double rDeltaJ = p6BinNodeIncrementOnJaxis
				* ((spatialCoordinatesComputeMap.get(KEY_RAB) + spatialCoordinatesComputeMap.get(KEY_RCD)) / 2)
				/ (jB - jA);

		rDeltaIJMap.put(KEY_RDELTAJ, rDeltaJ);

		return rDeltaIJMap;

	}
	
		
	private void thetaCalculation(AbstractBinGrid inBinGrid, Map<String, Double> rDeltaIJMap, ConvertBinGridResponse convertBinGridResponse) {

		List<AbstractFeature> abstractCoordinatesList = inBinGrid.getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures();

		Double coordinateAx = abstractCoordinatesList.get(0).getGeometry().getCoordinates().get(0);
		Double coordinateAy = abstractCoordinatesList.get(0).getGeometry().getCoordinates().get(1);

		Double coordinateBx = abstractCoordinatesList.get(1).getGeometry().getCoordinates().get(0);
		Double coordinateBy = abstractCoordinatesList.get(1).getGeometry().getCoordinates().get(1);

		Double coordinateCx = abstractCoordinatesList.get(2).getGeometry().getCoordinates().get(0);
		Double coordinateCy = abstractCoordinatesList.get(2).getGeometry().getCoordinates().get(1);

		Double coordinateDx = abstractCoordinatesList.get(3).getGeometry().getCoordinates().get(0);
		Double coordinateDy = abstractCoordinatesList.get(3).getGeometry().getCoordinates().get(1);

		Double thetaAB = Math.toDegrees(Math.atan2(coordinateBx - coordinateAx, coordinateBy - coordinateAy));
		Double thetaCD = Math.toDegrees(Math.atan2(coordinateDx - coordinateCx, coordinateDy - coordinateCy));
		Double theta = Math.toDegrees(Math.atan2(Math.sin(Math.toRadians(thetaAB)) + Math.sin(Math.toRadians(thetaCD)),
				Math.cos(Math.toRadians(thetaAB)) + Math.cos(Math.toRadians(thetaCD))));
		if (theta < 0) {
			theta = theta + 360;
		}
		String handedNess;
		if ((coordinateCx - coordinateAx) * Math.cos(Math.toRadians(theta)) > (coordinateCy - coordinateAy)
				* Math.sin(Math.toRadians(theta))) {
			handedNess = RIGHT_HANDED_NESS;
		} else {
			handedNess = LEFT_HANDED_NESS;
		}
		Integer handedNessValue;
		if (handedNess.equals(RIGHT_HANDED_NESS)) {
			convertBinGridResponse.getOutBinGrid().setP6TransformationMethod(CRS_CODE_9666);
			handedNessValue = 1;
		} else {
			convertBinGridResponse.getOutBinGrid().setP6TransformationMethod(CRS_CODE_1049);
			handedNessValue = -1;
		}

		Integer iA = abstractCoordinatesList.get(0).getProperties().getPointPropertiesList().get(0).getInline();
		Integer jA = abstractCoordinatesList.get(0).getProperties().getPointPropertiesList().get(0).getCrossline();

		Integer iB = abstractCoordinatesList.get(1).getProperties().getPointPropertiesList().get(0).getInline();
		Integer jB = abstractCoordinatesList.get(1).getProperties().getPointPropertiesList().get(0).getCrossline();

		Integer iC = abstractCoordinatesList.get(2).getProperties().getPointPropertiesList().get(0).getInline();
		Integer jC = abstractCoordinatesList.get(2).getProperties().getPointPropertiesList().get(0).getCrossline();

		Integer iD = abstractCoordinatesList.get(3).getProperties().getPointPropertiesList().get(0).getInline();
		Integer jD = abstractCoordinatesList.get(3).getProperties().getPointPropertiesList().get(0).getCrossline();
			
		Double p6ScaleFactorOfBinGrid = inBinGrid.getP6ScaleFactorOfBinGrid();
		Double p6BinNodeIncrementOnIaxis = inBinGrid.getP6BinNodeIncrementOnIaxis();
		Double p6BinNodeIncrementOnJaxis = inBinGrid.getP6BinNodeIncrementOnJaxis();		

		Double valueAX = inBinGrid.getP6BinGridOriginEasting()
				+ handedNessValue * ((iA - inBinGrid.getP6BinGridOriginI()) * Math.cos(Math.toRadians(theta))
						* p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAI) / p6BinNodeIncrementOnIaxis)
				+ ((jA - inBinGrid.getP6BinGridOriginJ()) * Math.sin(Math.toRadians(theta)) * p6ScaleFactorOfBinGrid
						* rDeltaIJMap.get(KEY_RDELTAJ) / p6BinNodeIncrementOnJaxis);

		Double valueAY = inBinGrid.getP6BinGridOriginNorthing()
				- handedNessValue * ((iA - inBinGrid.getP6BinGridOriginI()) * Math.sin(Math.toRadians(theta))
						* p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAI) / p6BinNodeIncrementOnIaxis)
				+ ((jA - inBinGrid.getP6BinGridOriginJ()) * Math.cos(Math.toRadians(theta)) * p6ScaleFactorOfBinGrid
						* rDeltaIJMap.get(KEY_RDELTAJ) / p6BinNodeIncrementOnJaxis);

		Double valueAI = inBinGrid.getP6BinGridOriginI() + handedNessValue
				* (((coordinateAx - inBinGrid.getP6BinGridOriginEasting()) * Math.cos(Math.toRadians(theta))
						- (coordinateAy - inBinGrid.getP6BinGridOriginNorthing()) * Math.sin(Math.toRadians(theta)))
						* (p6BinNodeIncrementOnIaxis / (p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAI))));

		Double valueAJ = inBinGrid.getP6BinGridOriginJ()
				+ (((coordinateAx - inBinGrid.getP6BinGridOriginEasting()) * Math.sin(Math.toRadians(theta))
						+ (coordinateAy - inBinGrid.getP6BinGridOriginNorthing()) * Math.cos(Math.toRadians(theta)))
						* (p6BinNodeIncrementOnJaxis / (p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAJ))));

		Double valueBX = inBinGrid.getP6BinGridOriginEasting()
				+ handedNessValue * ((iB - inBinGrid.getP6BinGridOriginI()) * Math.cos(Math.toRadians(theta))
						* p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAI) / p6BinNodeIncrementOnIaxis)
				+ ((jB - inBinGrid.getP6BinGridOriginJ()) * Math.sin(Math.toRadians(theta)) * p6ScaleFactorOfBinGrid
						* rDeltaIJMap.get(KEY_RDELTAJ) / p6BinNodeIncrementOnJaxis);

		Double valueBY = inBinGrid.getP6BinGridOriginNorthing()
				- handedNessValue * ((iB - inBinGrid.getP6BinGridOriginI()) * Math.sin(Math.toRadians(theta))
						* p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAI) / p6BinNodeIncrementOnIaxis)
				+ ((jB - inBinGrid.getP6BinGridOriginJ()) * Math.cos(Math.toRadians(theta)) * p6ScaleFactorOfBinGrid
						* rDeltaIJMap.get(KEY_RDELTAJ) / p6BinNodeIncrementOnJaxis);

		Double valueBI = inBinGrid.getP6BinGridOriginI() + handedNessValue
				* (((coordinateBx - inBinGrid.getP6BinGridOriginEasting()) * Math.cos(Math.toRadians(theta))
						- (coordinateBy - inBinGrid.getP6BinGridOriginNorthing()) * Math.sin(Math.toRadians(theta)))
						* (p6BinNodeIncrementOnIaxis / (p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAI))));

		Double valueBJ = inBinGrid.getP6BinGridOriginJ()
				+ (((coordinateBx - inBinGrid.getP6BinGridOriginEasting()) * Math.sin(Math.toRadians(theta))
						+ (coordinateBy - inBinGrid.getP6BinGridOriginNorthing()) * Math.cos(Math.toRadians(theta)))
						* (p6BinNodeIncrementOnJaxis / (p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAJ))));

		Double valueCX = inBinGrid.getP6BinGridOriginEasting()
				+ handedNessValue * ((iC - inBinGrid.getP6BinGridOriginI()) * Math.cos(Math.toRadians(theta))
						* p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAI) / p6BinNodeIncrementOnIaxis)
				+ ((jC - inBinGrid.getP6BinGridOriginJ()) * Math.sin(Math.toRadians(theta)) * p6ScaleFactorOfBinGrid
						* rDeltaIJMap.get(KEY_RDELTAJ) / p6BinNodeIncrementOnJaxis);

		Double valueCY = inBinGrid.getP6BinGridOriginNorthing()
				- handedNessValue * ((iC - inBinGrid.getP6BinGridOriginI()) * Math.sin(Math.toRadians(theta))
						* p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAI) / p6BinNodeIncrementOnIaxis)
				+ ((jC - inBinGrid.getP6BinGridOriginJ()) * Math.cos(Math.toRadians(theta)) * p6ScaleFactorOfBinGrid
						* rDeltaIJMap.get(KEY_RDELTAJ) / p6BinNodeIncrementOnJaxis);

		Double valueCI = inBinGrid.getP6BinGridOriginI() + handedNessValue
				* (((coordinateCx - inBinGrid.getP6BinGridOriginEasting()) * Math.cos(Math.toRadians(theta))
						- (coordinateCy - inBinGrid.getP6BinGridOriginNorthing()) * Math.sin(Math.toRadians(theta)))
						* (p6BinNodeIncrementOnIaxis / (p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAI))));

		Double valueCJ = inBinGrid.getP6BinGridOriginJ()
				+ (((coordinateCx - inBinGrid.getP6BinGridOriginEasting()) * Math.sin(Math.toRadians(theta))
						+ (coordinateCy - inBinGrid.getP6BinGridOriginNorthing()) * Math.cos(Math.toRadians(theta)))
						* (p6BinNodeIncrementOnJaxis / (p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAJ))));

		Double valueDX = inBinGrid.getP6BinGridOriginEasting()
				+ handedNessValue * ((iD - inBinGrid.getP6BinGridOriginI()) * Math.cos(Math.toRadians(theta))
						* p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAI) / p6BinNodeIncrementOnIaxis)
				+ ((jD - inBinGrid.getP6BinGridOriginJ()) * Math.sin(Math.toRadians(theta)) * p6ScaleFactorOfBinGrid
						* rDeltaIJMap.get(KEY_RDELTAJ) / p6BinNodeIncrementOnJaxis);

		Double valueDY = inBinGrid.getP6BinGridOriginNorthing()
				- handedNessValue * ((iD - inBinGrid.getP6BinGridOriginI()) * Math.sin(Math.toRadians(theta))
						* p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAI) / p6BinNodeIncrementOnIaxis)
				+ ((jD - inBinGrid.getP6BinGridOriginJ()) * Math.cos(Math.toRadians(theta)) * p6ScaleFactorOfBinGrid
						* rDeltaIJMap.get(KEY_RDELTAJ) / p6BinNodeIncrementOnJaxis);

		Double valueDI = inBinGrid.getP6BinGridOriginI() + handedNessValue
				* (((coordinateDx - inBinGrid.getP6BinGridOriginEasting()) * Math.cos(Math.toRadians(theta))
						- (coordinateDy - inBinGrid.getP6BinGridOriginNorthing()) * Math.sin(Math.toRadians(theta)))
						* (p6BinNodeIncrementOnIaxis / (p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAI))));

		Double valueDJ = inBinGrid.getP6BinGridOriginJ()
				+ (((coordinateDx - inBinGrid.getP6BinGridOriginEasting()) * Math.sin(Math.toRadians(theta))
						+ (coordinateDy - inBinGrid.getP6BinGridOriginNorthing()) * Math.cos(Math.toRadians(theta)))
						* (p6BinNodeIncrementOnJaxis / (p6ScaleFactorOfBinGrid * rDeltaIJMap.get(KEY_RDELTAJ))));
		
		double AiI = (iA - valueAI);
		double BiI = (iB - valueBI);
		double CiI = (iC - valueCI);
		double DiI = (iD - valueDI);

		double AjJ = (jA - valueAJ);
		double BjJ = (jB - valueBJ);
		double CjJ = (jC - valueCJ);
		double DjJ = (jD - valueDJ);

		double maxvalueI = Stream.of(Arrays.asList(AiI, BiI, CiI, DiI).toArray(new Double[4]))
				.mapToDouble(Double::valueOf).max().getAsDouble();
		double minvalueI = Stream.of(Arrays.asList(AiI, BiI, CiI, DiI).toArray(new Double[4]))
				.mapToDouble(Double::valueOf).min().getAsDouble();
		double dI = (Math.max(maxvalueI, -minvalueI));

		double maxvalueJ = Stream.of(Arrays.asList(AjJ, BjJ, CjJ, DjJ).toArray(new Double[4]))
				.mapToDouble(Double::valueOf).max().getAsDouble();
		double minvalueJ = Stream.of(Arrays.asList(AjJ, BjJ, CjJ, DjJ).toArray(new Double[4]))
				.mapToDouble(Double::valueOf).min().getAsDouble();
		double dJ = (Math.max(maxvalueJ, -minvalueJ));
		
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0).getGeometry().setCoordinates(Arrays.asList(valueAX,valueAY));
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(1).getGeometry().setCoordinates(Arrays.asList(valueBX,valueBY));
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(2).getGeometry().setCoordinates(Arrays.asList(valueCX,valueCY));
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(3).getGeometry().setCoordinates(Arrays.asList(valueDX,valueDY));

		MaxMisLocation maxMisLocation = new MaxMisLocation();
		maxMisLocation.setDI(dI);
		maxMisLocation.setDJ(dJ);
		convertBinGridResponse.setMaxMisLocation(maxMisLocation);
	}
	
	private boolean validatePointCoordinates(List<PointProperties> points) {
		logger.info("Validating BinGrid input request");
		if (points != null) {
			if (points.size() != 4)
				return false;

			double tMin = points.get(0).getInline();
			double bMin = points.get(0).getCrossline();

			double tMax = points.get(3).getInline();
			double bMax = points.get(3).getInline();

			PointProperties secondPoint = points.get(1);
			PointProperties thirdPoint = points.get(2);
			if (tMax <= tMin || bMax <= bMin) {
				logger.info("Tmax and Bmax are not greater than Tmin and Bmin");
				return false;
			}

			else if (secondPoint.getInline() != tMin || secondPoint.getCrossline() != bMax) {
				logger.info("Second point is not Tmin and Bmax");
				return false;
			}

			else if (thirdPoint.getInline() != tMax || thirdPoint.getCrossline() != bMin) {
				logger.info("Third point is not Tmax and Bmin");
				return false;
			}
			logger.info("BinGrid input request is valid");
		} else
			return false;
		return true;
	}
}