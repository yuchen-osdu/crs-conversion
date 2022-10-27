package org.opengroup.osdu.crs.converter;

import static org.opengroup.osdu.crs.model.ReferenceConverter.parseSpatialReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.crs.BinGrid.AbstractBinGrid;
import org.opengroup.osdu.crs.BinGrid.AbstractCoordinates;
import org.opengroup.osdu.crs.BinGrid.AbstractSpatialLocation;
import org.opengroup.osdu.crs.BinGrid.InnerBinGridCoordinates;
import org.opengroup.osdu.crs.BinGrid.MaxMinLocation;
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

		if (inBinGrid.getBinGridDefinitionMethodTypeID().equals(BIN_GRID_METHOD_4_CORNERS)) {

			String type = "";
			outBinGrid = new ConvertBinGridResponse();
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
		}

		return outBinGrid;

	}

	private ConvertBinGridResponse binGridComputation(String toCrs, AbstractBinGrid inBinGrid, ConvertBinGridResponse convertBinGridResponse) {
		List<AbstractCoordinates> localCoordinates = inBinGrid.getABCDBinGridLocalCoordinates();
		AbstractSpatialLocation spatialcoordinates = inBinGrid.getABCDBinGridSpatialLocation();
		validateLocalCoordinates(localCoordinates);
		String crsId = spatialcoordinates.getAsingestedcoordinates().getCoordinateReferenceSystemID();
		String persistableReference = spatialcoordinates.getAsingestedcoordinates().getPersistableReferenceCrs();		
		spatialcoordinates.getAsingestedcoordinates().getFeatures().stream().forEach(features -> {
			int size = features.getGeometry().getCoordinates().getAbstractCoordinates().size();
			if (size != 4)
				logger.info("Invalid size for spatial coordinates in the input request");
			else {
				features.getGeometry().getCoordinates().getAbstractCoordinates().stream().forEach(splCoordinates -> {
					double xCoordinate = splCoordinates.getX();
					double yCoordinate = splCoordinates.getY();
					double zCoordinate = splCoordinates.getZ();					
					Map<String, Double> crsParamMap = prepareCrsParam(inBinGrid);
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
						logger.info("CRS Id and Persistence Reference not present in the input request");
					}
					ConvertPointsResponse internal_response = convertPoint(value, toCrs, xys, zs);

					Map<String, Double> spatialCoordinatesComputeMap = RcomputationBetweenPoints(spatialcoordinates);			
					
					Map<String, Double> rDeltaIJMap = rDeltaIandJComputation(
							spatialCoordinatesComputeMap,localCoordinates,crsParamMap);
					
					thetaCalculation(inBinGrid,rDeltaIJMap,crsParamMap,convertBinGridResponse);					
					convertBinGridResponse.setOperationsApplied(internal_response.getOperationsApplied());

				});
			}
		});
		return convertBinGridResponse;
	}
	
	
	private Map<String, Double> RcomputationBetweenPoints(AbstractSpatialLocation spatialcoordinates) {
		
		Map<String, Double> spatialCoordinatesComputeMap = new HashMap<>();
		
		spatialcoordinates.getAsingestedcoordinates().getFeatures().stream().forEach(features -> {
			
			List<InnerBinGridCoordinates> abstractCoordinatesList = features.getGeometry().getCoordinates().getAbstractCoordinates();
			
			    Double coordinateAx = abstractCoordinatesList.get(0).getX();
			    Double coordinateAy = abstractCoordinatesList.get(0).getY();
			    
			    Double coordinateBx = abstractCoordinatesList.get(1).getX();
			    Double coordinateBy = abstractCoordinatesList.get(1).getY();
			    
			    Double coordinateCx = abstractCoordinatesList.get(2).getX();
			    Double coordinateCy = abstractCoordinatesList.get(2).getY();
			    
			    Double coordinateDx = abstractCoordinatesList.get(3).getX();
			    Double coordinateDy = abstractCoordinatesList.get(3).getY();
			    
			    Double rAC = Math.sqrt(Math.pow((coordinateCy - coordinateAy), 2) + Math.pow((coordinateCx - coordinateAx), 2));
			    spatialCoordinatesComputeMap.put("RAC", rAC);
			    Double rBD = Math.sqrt(Math.pow((coordinateDy - coordinateBy), 2) + Math.pow((coordinateDx - coordinateBx), 2));
			    spatialCoordinatesComputeMap.put("RBD", rBD);
                Double rAB = Math.sqrt(Math.pow((coordinateBy - coordinateAy), 2) + Math.pow((coordinateBx - coordinateAx), 2));
                spatialCoordinatesComputeMap.put("RAB", rAB);
			    Double rCD = Math.sqrt(Math.pow((coordinateDy - coordinateCy), 2) + Math.pow((coordinateDx - coordinateCx), 2));
			    spatialCoordinatesComputeMap.put("RCD", rCD);
			});		
		
		return spatialCoordinatesComputeMap;
	}
	
	private Map<String, Double> rDeltaIandJComputation(
			Map<String, Double> spatialCoordinatesComputeMap,List<AbstractCoordinates> localCoordinates,Map<String, Double> crsParamMap) {
		
		Map<String, Double> rDeltaIJMap = new HashMap<>();
		
		Double iA = localCoordinates.get(0).getX();
		Double jA = localCoordinates.get(0).getY();
		
		Double iB = localCoordinates.get(1).getX();
		Double jB = localCoordinates.get(1).getY();
		
		Double iC = localCoordinates.get(2).getX();
		Double jC = localCoordinates.get(2).getY();
		
		Double iD = localCoordinates.get(3).getX();
		Double jD = localCoordinates.get(3).getY();

		
		Double rDeltaI = crsParamMap.get("P6BinNodeIncrementOnIaxis")  *   (( spatialCoordinatesComputeMap.get("RAC") + spatialCoordinatesComputeMap.get("RBD") / 2 )) / (iC - iA);
		
		rDeltaIJMap.put("RDELTAI", rDeltaI);
		
		Double rDeltaJ = crsParamMap.get("P6BinNodeIncrementOnJaxis")  *   (( spatialCoordinatesComputeMap.get("RAB") + spatialCoordinatesComputeMap.get("RCD") / 2 )) / (jB - jA);
		
		rDeltaIJMap.put("RDELTAJ", rDeltaJ);
		
		return rDeltaIJMap;
		
	}
	
		
	private void thetaCalculation(AbstractBinGrid inBinGrid,Map<String, Double> rDeltaIJMap,Map<String, Double> crsParamsMap,ConvertBinGridResponse convertBinGridResponse) {

		// θA,B = atan2(XB-XA, YB-YA),
		// θC,D = atan2(XD-XC, YD-YC),
		// θ = atan2(sin(θA,B)+sin(θC,D), cos(θA,B)+cos(θC,D))
		// If θ<0 then θ += 360;
		// If { (XC-XA)*cos(θ) > (YC-YA)*sin(θ) } then right-handed, else left-handed

		// If right-handed, then handednesssign = 1 else handednesssign = -1		
		
		inBinGrid.getABCDBinGridSpatialLocation().getAsingestedcoordinates().getFeatures().stream().forEach(features -> {

			List<InnerBinGridCoordinates> abstractCoordinatesList = features.getGeometry().getCoordinates()
					.getAbstractCoordinates();

			Double coordinateAx = abstractCoordinatesList.get(0).getX();
			Double coordinateAy = abstractCoordinatesList.get(0).getY();

			Double coordinateBx = abstractCoordinatesList.get(1).getX();
			Double coordinateBy = abstractCoordinatesList.get(1).getY();

			Double coordinateCx = abstractCoordinatesList.get(2).getX();
			Double coordinateCy = abstractCoordinatesList.get(2).getY();

			Double coordinateDx = abstractCoordinatesList.get(3).getX();
			Double coordinateDy = abstractCoordinatesList.get(3).getY();

			Double thetaAB = Math.atan2(coordinateBx - coordinateAx, coordinateBy - coordinateAy);

			Double thetaCD = Math.atan2(coordinateDx - coordinateCx, coordinateDy - coordinateCy);

			Double theta = Math.atan2(Math.sin(thetaAB) + Math.sin(thetaCD), Math.cos(thetaAB) + Math.cos(thetaCD));

			if (Math.toDegrees(theta.doubleValue()) < 0) {

				theta = theta + 360;
			}
			String handedNess;
			if ((coordinateCx - coordinateAx) * Math.cos(theta) > (coordinateCy - coordinateAy) * Math.sin(theta)) {

				handedNess = "rightHandedNess";				
			} else {
				handedNess = "leftHandedNess";
			}
			Integer handedNessValue;
			if (handedNess.equals("rightHandedNess")) {
				convertBinGridResponse.getOutBinGrid().setP6TransformationMethod(CRS_CODE_9666);
				handedNessValue = 1;				
			} else {
				convertBinGridResponse.getOutBinGrid().setP6TransformationMethod(CRS_CODE_1049);
				handedNessValue = -1;
			}			
			
			List<AbstractCoordinates> localCoordinates = inBinGrid.getABCDBinGridLocalCoordinates();
			
			Double iA = localCoordinates.get(0).getX();
			Double jA = localCoordinates.get(0).getY();
			
			Double iB = localCoordinates.get(1).getX();
			Double jB = localCoordinates.get(1).getY();
			
			Double iC = localCoordinates.get(2).getX();
			Double jC = localCoordinates.get(2).getY();
			
			Double iD = localCoordinates.get(3).getX();
			Double jD = localCoordinates.get(3).getY();			
			
			/*
			 * 13. X = X0 + handednesssign* [(I-I0)*cos(θ)*SF*dRΔI/ΔI] +
			 * [(J-J0)*sin(θ)*SF*dRΔJ/ΔJ]
			 * 
			 * 14. Y = Y0 – handednesssign* [(I-I0)*sin(θ)*SF*dRΔI/ΔI] +
			 * [(J-J0)*cos(θ)*SF*dRΔJ/ΔJ]
			 * 
			 * Projected to Bin Grid Calculations
			 * 
			 * 15. I = I0 + handednesssign* { [(X-X0)*cos(θ) – (Y-Y0)*sin(θ) ] * [ΔI /
			 * (SF*dRΔI)] }
			 * 
			 * 16. J = J0 + { [(X-X0)*sin(θ) + (Y-Y0)*cos(θ)] * [ΔJ / (SF*dRΔJ)] }
			 */

			Double valueAX = inBinGrid.getP6BinGridOriginEasting() + handedNessValue * 
					((iA - inBinGrid.getP6BinGridOriginI()) * Math.cos(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAI") / crsParamsMap.get("P6BinNodeIncrementOnIaxis")) +
					((jA - inBinGrid.getP6BinGridOriginJ()) * Math.sin(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAJ") / crsParamsMap.get("P6BinNodeIncrementOnJaxis"));
			
			Double valueAY = inBinGrid.getP6BinGridOriginNorthing() - handedNessValue * 
					((iA - inBinGrid.getP6BinGridOriginI()) * Math.sin(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAI") / crsParamsMap.get("P6BinNodeIncrementOnIaxis")) +
					((jA - inBinGrid.getP6BinGridOriginJ()) * Math.cos(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAJ") / crsParamsMap.get("P6BinNodeIncrementOnJaxis"));
			
			Double valueAI = inBinGrid.getP6BinGridOriginI() + handedNessValue * (( valueAX - inBinGrid.getP6BinGridOriginEasting() ) 
					* Math.cos(theta) - ( valueAY - inBinGrid.getP6BinGridOriginNorthing() * Math.sin(theta) 
					* crsParamsMap.get("P6BinNodeIncrementOnIaxis") / inBinGrid.getP6ScaleFactorOfBinGrid() * rDeltaIJMap.get("RDELTAI")));
			
			Double valueAJ = inBinGrid.getP6BinGridOriginJ() + (( valueAX - inBinGrid.getP6BinGridOriginEasting() ) 
					* Math.sin(theta) - ( valueAY - inBinGrid.getP6BinGridOriginNorthing() * Math.cos(theta) 
					* crsParamsMap.get("P6BinNodeIncrementOnJaxis") / inBinGrid.getP6ScaleFactorOfBinGrid() * rDeltaIJMap.get("RDELTAJ")));
			
			Double valueBX = inBinGrid.getP6BinGridOriginEasting() + handedNessValue * 
					((iB - inBinGrid.getP6BinGridOriginI()) * Math.cos(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAI") / crsParamsMap.get("P6BinNodeIncrementOnIaxis")) +
					((jB - inBinGrid.getP6BinGridOriginJ()) * Math.sin(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAJ") / crsParamsMap.get("P6BinNodeIncrementOnJaxis"));
			
			Double valueBY = inBinGrid.getP6BinGridOriginNorthing() - handedNessValue * 
					((iB - inBinGrid.getP6BinGridOriginI()) * Math.sin(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAI") / crsParamsMap.get("P6BinNodeIncrementOnIaxis")) +
					((jB - inBinGrid.getP6BinGridOriginJ()) * Math.cos(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAJ") / crsParamsMap.get("P6BinNodeIncrementOnJaxis"));
			
			Double valueBI = inBinGrid.getP6BinGridOriginI() + handedNessValue * (( valueBX - inBinGrid.getP6BinGridOriginEasting() ) 
					* Math.cos(theta) - ( valueBY - inBinGrid.getP6BinGridOriginNorthing() * Math.sin(theta) 
					* crsParamsMap.get("P6BinNodeIncrementOnIaxis") / inBinGrid.getP6ScaleFactorOfBinGrid() * rDeltaIJMap.get("RDELTAI")));
			
			Double valueBJ = inBinGrid.getP6BinGridOriginJ() + (( valueBX - inBinGrid.getP6BinGridOriginEasting() ) 
					* Math.sin(theta) - ( valueBY - inBinGrid.getP6BinGridOriginNorthing() * Math.cos(theta) 
					* crsParamsMap.get("P6BinNodeIncrementOnJaxis") / inBinGrid.getP6ScaleFactorOfBinGrid() * rDeltaIJMap.get("RDELTAJ")));
			
			
			Double valueCX = inBinGrid.getP6BinGridOriginEasting() + handedNessValue * 
					((iC - inBinGrid.getP6BinGridOriginI()) * Math.cos(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAI") / crsParamsMap.get("P6BinNodeIncrementOnIaxis")) +
					((jC - inBinGrid.getP6BinGridOriginJ()) * Math.sin(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAJ") / crsParamsMap.get("P6BinNodeIncrementOnJaxis"));
			
			Double valueCY = inBinGrid.getP6BinGridOriginNorthing() - handedNessValue * 
					((iC - inBinGrid.getP6BinGridOriginI()) * Math.sin(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAI") / crsParamsMap.get("P6BinNodeIncrementOnIaxis")) +
					((jC - inBinGrid.getP6BinGridOriginJ()) * Math.cos(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAJ") / crsParamsMap.get("P6BinNodeIncrementOnJaxis"));
			
			Double valueCI = inBinGrid.getP6BinGridOriginI() + handedNessValue * (( valueCX - inBinGrid.getP6BinGridOriginEasting() ) 
					* Math.cos(theta) - ( valueCY - inBinGrid.getP6BinGridOriginNorthing() * Math.sin(theta) 
					* crsParamsMap.get("P6BinNodeIncrementOnIaxis") / inBinGrid.getP6ScaleFactorOfBinGrid() * rDeltaIJMap.get("RDELTAI")));
			
			Double valueCJ = inBinGrid.getP6BinGridOriginJ() + (( valueCX - inBinGrid.getP6BinGridOriginEasting() ) 
					* Math.sin(theta) - ( valueCY - inBinGrid.getP6BinGridOriginNorthing() * Math.cos(theta) 
					* crsParamsMap.get("P6BinNodeIncrementOnJaxis") / inBinGrid.getP6ScaleFactorOfBinGrid() * rDeltaIJMap.get("RDELTAJ")));
			
			Double valueDX = inBinGrid.getP6BinGridOriginEasting() + handedNessValue * 
					((iC - inBinGrid.getP6BinGridOriginI()) * Math.cos(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAI") / crsParamsMap.get("P6BinNodeIncrementOnIaxis")) +
					((jC - inBinGrid.getP6BinGridOriginJ()) * Math.sin(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAJ") / crsParamsMap.get("P6BinNodeIncrementOnJaxis"));
			
			Double valueDY = inBinGrid.getP6BinGridOriginNorthing() - handedNessValue * 
					((iC - inBinGrid.getP6BinGridOriginI()) * Math.sin(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAI") / crsParamsMap.get("P6BinNodeIncrementOnIaxis")) +
					((jC - inBinGrid.getP6BinGridOriginJ()) * Math.cos(theta) * inBinGrid.getP6ScaleFactorOfBinGrid()
					* rDeltaIJMap.get("RDELTAJ") / crsParamsMap.get("P6BinNodeIncrementOnJaxis"));
			
			Double valueDI = inBinGrid.getP6BinGridOriginI() + handedNessValue * (( valueDX - inBinGrid.getP6BinGridOriginEasting() ) 
					* Math.cos(theta) - ( valueDY - inBinGrid.getP6BinGridOriginNorthing() * Math.sin(theta) 
					* crsParamsMap.get("P6BinNodeIncrementOnIaxis") / inBinGrid.getP6ScaleFactorOfBinGrid() * rDeltaIJMap.get("RDELTAI")));
			
			Double valueDJ = inBinGrid.getP6BinGridOriginJ() + (( valueDX - inBinGrid.getP6BinGridOriginEasting() ) 
					* Math.sin(theta) - ( valueDY - inBinGrid.getP6BinGridOriginNorthing() * Math.cos(theta) 
					* crsParamsMap.get("P6BinNodeIncrementOnJaxis") / inBinGrid.getP6ScaleFactorOfBinGrid() * rDeltaIJMap.get("RDELTAJ")));
	
			double dI = Math.max(
					Math.max(Math.abs(Double.valueOf(iA) - Double.valueOf(valueAI)),
							Math.abs(Double.valueOf(iB) - Double.valueOf(valueBI))),
					Math.max(Math.abs(Double.valueOf(iC) - Double.valueOf(valueCI)),
							Math.abs(Double.valueOf(iD) - Double.valueOf(valueDI))));
			double dJ = Math.max(
					Math.max(Math.abs(Double.valueOf(jA) - Double.valueOf(valueAJ)),
							Math.abs(Double.valueOf(jB) - Double.valueOf(valueBJ))),
					Math.max(Math.abs(Double.valueOf(jC) - Double.valueOf(valueCJ)),
							Math.abs(Double.valueOf(jD) - Double.valueOf(valueDJ))));
			MaxMinLocation maxMinLocation = new MaxMinLocation();
			maxMinLocation.setDI(dI);
			maxMinLocation.setDJ(dJ);
			
			convertBinGridResponse.setMaxMinLocation(maxMinLocation);
			
		});		
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
		logger.info("Validating BinGrid input request");
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
			logger.info("BinGrid input request is valid");
		} else
			return false;
		return true;
	}
}