package org.opengroup.osdu.crs.converter;

import static org.opengroup.osdu.crs.model.ReferenceConverter.parseSpatialReference;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.crs.BinGrid.AbstractBinGrid;
import org.opengroup.osdu.crs.BinGrid.AbstractFeature;
import org.opengroup.osdu.crs.BinGrid.AbstractFeatureCollection;
import org.opengroup.osdu.crs.BinGrid.AbstractSpatialLocation;
import org.opengroup.osdu.crs.BinGrid.MaxMisLocation;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
public class CRSConverter implements ICRSConverter {

	@Autowired
	private JaxRsDpsLog logger;

	private static final String METER = "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}";

	private static final String BIN_GRID_METHOD_4_CORNER = "4Corner";
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
	private static final String LABEL_A = "A";
	private static final String LABEL_B = "B";
	private static final String LABEL_C = "C";
	private static final String LABEL_D = "D";
	private static final String FEATURE_TYPE = "Feature";
	private static final String GEOMETRY_TYPE = "Point";

	@Override
	public ConvertPointsResponse convertPoint(String from, String to, double[] xyCoordinates, double[] zCoordinates) {

		if ((from == null) || (from.isEmpty()) || (to == null) || (to.isEmpty()) || (xyCoordinates == null)
				|| (xyCoordinates.length == 0) || (zCoordinates == null) || (zCoordinates.length == 0)) {
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
	public ConvertGeoJsonResponse convertGeoJson(GeoJsonFeatureCollection featureCollection, String toCrs,
			String requestedToUnitZ) {
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
			ConvertPointsResponse internal_response = convertPoint(fromCrs, toCrs, coordinates.getXys(),
					coordinates.getZ_s());
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
		int size = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().size();
		if (size != 4) {
			logger.info("Invalid size for spatial coordinates in the input request.");
			throw new ValidationException("Invalid size for spatial coordinates in the input request. Expected 4 AnyCrsFeatures with geometry “AnyCrsPoint”.  Found "+size+" points");
		} else {
			// sort the point coordinates in the order (min, min), (min, max), (max, min),
			// (max, max)
			inBinGrid = sortAnyCRSFeature(inBinGrid);
			
			// Setting the computed values for P6 SchemaParameters			
			// if the scaleFactor value from the input request is 0.0 , then it is set to
			// 1.0
			if (inBinGrid.getP6ScaleFactorOfBinGrid() == 0.0) {
				inBinGrid.setP6ScaleFactorOfBinGrid(1.0);
			}
			// Setting the default value to 1 , if the value is 0 from the input request
			if (inBinGrid.getP6BinNodeIncrementOnIaxis() == 0) {
				inBinGrid.setP6BinNodeIncrementOnIaxis(1);
			}
			// Setting the default value to 1 , if the value is 0 from the input request
			if (inBinGrid.getP6BinNodeIncrementOnJaxis() == 0) {
				inBinGrid.setP6BinNodeIncrementOnJaxis(1);
			}

			inBinGrid.setP6BinGridOriginI(inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0)
					.getProperties().getPointPropertiesList().get(0).getInline().doubleValue());
			inBinGrid.setP6BinGridOriginJ(inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0)
					.getProperties().getPointPropertiesList().get(0).getCrossline().doubleValue());
			inBinGrid.setP6BinGridOriginEasting(inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0)
					.getGeometry().getCoordinates().get(0));
			inBinGrid.setP6BinGridOriginNorthing(inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0)
					.getGeometry().getCoordinates().get(1));
			
			// performing the bin grid computation
			outBinGrid = binGridComputation(inBinGrid, outBinGrid);
			outBinGrid.getOutBinGrid().getABCDBinGridSpatialLocation().setCoordinateQualityCheckRemarks(
					Arrays.asList("Max. squaring error: dI= " + outBinGrid.getMaxMisLocation().getDI() + ", dJ= "
							+ outBinGrid.getMaxMisLocation().getDJ() + " bin"));

			if (toCrs != null && !StringUtils.isEmpty(toCrs)) {
				// calling the convertPoints to get the applied operations for the toCRS
				outBinGrid.setAppliedOperations(
						appliedOperationsFromConvertPoints(toCrs, inBinGrid.getABCDBinGridSpatialLocation()));
				outBinGrid.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
						.setCoordinateReferenceSystemID(toCrs);
				outBinGrid.getOutBinGrid().getABCDBinGridSpatialLocation().setCoordinateQualityCheckRemarks(
						Arrays.asList("converted from to; squared up: dI= " + outBinGrid.getMaxMisLocation().getDI()
								+ ", dJ= " + outBinGrid.getMaxMisLocation().getDJ() + " (bin)"));
			}
			inBinGrid.setBinGridDefinitionMethodTypeID(BIN_GRID_METHOD_4_CORNER);
			outBinGrid.setOutBinGrid(inBinGrid);
		}
		return outBinGrid;

	}

	private AbstractBinGrid sortAnyCRSFeature(AbstractBinGrid inBinGrid) {

		Integer inLineA = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0)
				.getProperties().getPointPropertiesList().get(0).getInline();
		Integer inLineB = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(1)
				.getProperties().getPointPropertiesList().get(0).getInline();
		Integer inLineC = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(2)
				.getProperties().getPointPropertiesList().get(0).getInline();
		Integer inLineD = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(3)
				.getProperties().getPointPropertiesList().get(0).getInline();

		Integer crossLineA = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0)
				.getProperties().getPointPropertiesList().get(0).getCrossline();
		Integer crossLineB = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(1)
				.getProperties().getPointPropertiesList().get(0).getCrossline();
		Integer crossLineC = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(2)
				.getProperties().getPointPropertiesList().get(0).getCrossline();
		Integer crossLineD = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(3)
				.getProperties().getPointPropertiesList().get(0).getCrossline();

		Integer maxInLine = Stream.of(Arrays.asList(inLineA, inLineB, inLineC, inLineD).toArray(new Integer[4]))
				.mapToInt(Integer::valueOf).max().getAsInt();
		Integer minInLine = Stream.of(Arrays.asList(inLineA, inLineB, inLineC, inLineD).toArray(new Integer[4]))
				.mapToInt(Integer::valueOf).min().getAsInt();
		Integer maxCrossLine = Stream
				.of(Arrays.asList(crossLineA, crossLineB, crossLineC, crossLineD).toArray(new Integer[4]))
				.mapToInt(Integer::valueOf).max().getAsInt();
		Integer minCrossLine = Stream
				.of(Arrays.asList(crossLineA, crossLineB, crossLineC, crossLineD).toArray(new Integer[4]))
				.mapToInt(Integer::valueOf).min().getAsInt();

		if (minInLine != maxInLine && minCrossLine != maxCrossLine) {

			inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0).getProperties()
					.getPointPropertiesList().get(0).setLabel(LABEL_A);
			inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0).getProperties()
					.getPointPropertiesList().get(0).setInline(minInLine);
			inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0).getProperties()
					.getPointPropertiesList().get(0).setCrossline(minCrossLine);
			inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(1).getProperties()
					.getPointPropertiesList().get(0).setLabel(LABEL_B);
			inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(1).getProperties()
					.getPointPropertiesList().get(0).setInline(minInLine);
			inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(1).getProperties()
					.getPointPropertiesList().get(0).setCrossline(maxCrossLine);
			inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(2).getProperties()
					.getPointPropertiesList().get(0).setLabel(LABEL_C);
			inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(2).getProperties()
					.getPointPropertiesList().get(0).setInline(maxInLine);
			inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(2).getProperties()
					.getPointPropertiesList().get(0).setCrossline(minCrossLine);
			inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(3).getProperties()
					.getPointPropertiesList().get(0).setLabel(LABEL_D);
			inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(3).getProperties()
					.getPointPropertiesList().get(0).setInline(maxInLine);
			inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(3).getProperties()
					.getPointPropertiesList().get(0).setCrossline(maxCrossLine);

			return inBinGrid;
		} else {
			logger.info("MaxInLine should always be greater then MinInLine and MaxCrossLine should always be greater then MinCrossLine.");
			throw new ValidationException("MaxInLine should always be greater then MinInLine and MaxCrossLine should always be greater then MinCrossLine.");
		}

	}

	private ConvertBinGridResponse binGridComputation(AbstractBinGrid inBinGrid,
			ConvertBinGridResponse convertBinGridResponse) {
		AbstractSpatialLocation spatialcoordinates = inBinGrid.getABCDBinGridSpatialLocation();

		Map<String, Double> spatialCoordinatesComputeMap = RcomputationBetweenPoints(spatialcoordinates);

		Map<String, Double> rDeltaIJMap = rDeltaIandJComputation(spatialCoordinatesComputeMap,
				spatialcoordinates.getAsIngestedcoordinates().getFeatures(), inBinGrid.getP6BinNodeIncrementOnIaxis(),
				inBinGrid.getP6BinNodeIncrementOnJaxis());

		thetaCalculation(inBinGrid, rDeltaIJMap, convertBinGridResponse);

		return convertBinGridResponse;
	}

	private List<String> appliedOperationsFromConvertPoints(String toCrs, AbstractSpatialLocation spatialcoordinates) {

		String crsId = spatialcoordinates.getAsIngestedcoordinates().getCoordinateReferenceSystemID();
		String persistableReference = spatialcoordinates.getAsIngestedcoordinates().getPersistableReferenceCrs();
		List<String> appliedOperations = new ArrayList<>();
		AtomicReference<String> value = new AtomicReference<>(StringUtils.EMPTY);
		if (StringUtils.isNotEmpty(crsId)) {
			value.set(crsId);
		} else if (StringUtils.isNotEmpty(persistableReference)) {
			value.set(persistableReference);
		} else {
			logger.info("CRS Id and Persistence Reference not present in the input request");
		}
		try {
			AbstractFeature feature = spatialcoordinates.getAsIngestedcoordinates().getFeatures().get(0);
			double xCoordinate = feature.getGeometry().getCoordinates().get(0);
			double yCoordinate = feature.getGeometry().getCoordinates().get(1);
			double zCoordinate = 0.0;
			double xys[] = { xCoordinate, yCoordinate };
			double zs[] = { zCoordinate };
			ConvertPointsResponse internal_response = convertPoint(value.get(), toCrs, xys, zs);
			appliedOperations.addAll(internal_response.getOperationsApplied());
		} catch (IllegalArgumentException illegalArgumentException) {
			logger.info("Got error response from the convertPoint call");
			throw new ValidationException(illegalArgumentException.getMessage());
		}
		return appliedOperations;
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

	private Map<String, Double> rDeltaIandJComputation(Map<String, Double> spatialCoordinatesComputeMap,
			List<AbstractFeature> pointCoordinates, Integer p6BinNodeIncrementOnIaxis,
			Integer p6BinNodeIncrementOnJaxis) {

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

	private void thetaCalculation(AbstractBinGrid inBinGrid, Map<String, Double> rDeltaIJMap,
			ConvertBinGridResponse convertBinGridResponse) {

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

		convertBinGridResponse.getOutBinGrid().setP6MapGridBearingOfBinGridJaxis(theta);

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

		convertBinGridResponse.getOutBinGrid().setP6BinWidthOnIaxis(rDeltaIJMap.get(KEY_RDELTAI));
		convertBinGridResponse.getOutBinGrid().setP6BinWidthOnJaxis(rDeltaIJMap.get(KEY_RDELTAJ));

		Integer iA = abstractCoordinatesList.get(0).getProperties().getPointPropertiesList().get(0).getInline();
		Integer jA = abstractCoordinatesList.get(0).getProperties().getPointPropertiesList().get(0).getCrossline();

		Integer iB = abstractCoordinatesList.get(1).getProperties().getPointPropertiesList().get(0).getInline();
		Integer jB = abstractCoordinatesList.get(1).getProperties().getPointPropertiesList().get(0).getCrossline();

		Integer iC = abstractCoordinatesList.get(2).getProperties().getPointPropertiesList().get(0).getInline();
		Integer jC = abstractCoordinatesList.get(2).getProperties().getPointPropertiesList().get(0).getCrossline();

		Integer iD = abstractCoordinatesList.get(3).getProperties().getPointPropertiesList().get(0).getInline();
		Integer jD = abstractCoordinatesList.get(3).getProperties().getPointPropertiesList().get(0).getCrossline();

		Double p6ScaleFactorOfBinGrid = inBinGrid.getP6ScaleFactorOfBinGrid();
		Integer p6BinNodeIncrementOnIaxis = inBinGrid.getP6BinNodeIncrementOnIaxis();
		Integer p6BinNodeIncrementOnJaxis = inBinGrid.getP6BinNodeIncrementOnJaxis();

		DecimalFormat upto3Decimal = new DecimalFormat("0.000");
		DecimalFormat upto8Decimal = new DecimalFormat("0.00000000");

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

		DecimalFormat upto2Decimal = new DecimalFormat("0.00");

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

		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures()
				.get(0).getGeometry().setCoordinates(Arrays.asList(Double.valueOf(upto3Decimal.format(valueAX)),
						Double.valueOf(upto3Decimal.format(valueAY))));
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures()
				.get(1).getGeometry().setCoordinates(Arrays.asList(Double.valueOf(upto3Decimal.format(valueBX)),
						Double.valueOf(upto3Decimal.format(valueBY))));
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures()
				.get(2).getGeometry().setCoordinates(Arrays.asList(Double.valueOf(upto3Decimal.format(valueCX)),
						Double.valueOf(upto3Decimal.format(valueCY))));
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures()
				.get(3).getGeometry().setCoordinates(Arrays.asList(Double.valueOf(upto3Decimal.format(valueDX)),
						Double.valueOf(upto3Decimal.format(valueDY))));

		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation()
				.setWgs84Coordinates(new AbstractFeatureCollection());
		Gson gson = new Gson();
		Type listType = new TypeToken<ArrayList<AbstractFeature>>() {
		}.getType();
		List<AbstractFeature> deepCopy = gson.fromJson(gson.toJson(convertBinGridResponse.getOutBinGrid()
				.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures()), listType);
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getWgs84Coordinates()
				.setFeatures(deepCopy);

		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getWgs84Coordinates().getFeatures()
				.get(0).getGeometry().setCoordinates(Arrays.asList(Double.valueOf(upto8Decimal.format(valueAX)),
						Double.valueOf(upto8Decimal.format(valueAY))));
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getWgs84Coordinates().getFeatures()
				.get(1).getGeometry().setCoordinates(Arrays.asList(Double.valueOf(upto8Decimal.format(valueBX)),
						Double.valueOf(upto8Decimal.format(valueBY))));
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getWgs84Coordinates().getFeatures()
				.get(2).getGeometry().setCoordinates(Arrays.asList(Double.valueOf(upto8Decimal.format(valueCX)),
						Double.valueOf(upto8Decimal.format(valueCY))));
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getWgs84Coordinates().getFeatures()
				.get(3).getGeometry().setCoordinates(Arrays.asList(Double.valueOf(upto8Decimal.format(valueDX)),
						Double.valueOf(upto8Decimal.format(valueDY))));

		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getWgs84Coordinates().getFeatures()
				.get(0).setType(FEATURE_TYPE);
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getWgs84Coordinates().getFeatures()
				.get(1).setType(FEATURE_TYPE);
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getWgs84Coordinates().getFeatures()
				.get(2).setType(FEATURE_TYPE);
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getWgs84Coordinates().getFeatures()
				.get(3).setType(FEATURE_TYPE);

		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getWgs84Coordinates().getFeatures()
				.get(0).getGeometry().setType(GEOMETRY_TYPE);
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getWgs84Coordinates().getFeatures()
				.get(1).getGeometry().setType(GEOMETRY_TYPE);
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getWgs84Coordinates().getFeatures()
				.get(2).getGeometry().setType(GEOMETRY_TYPE);
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getWgs84Coordinates().getFeatures()
				.get(3).getGeometry().setType(GEOMETRY_TYPE);

		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation()
				.setCoordinateQualityCheckPerformedBy("CRS Convert service, POST convertBinGrid");

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation()
				.setCoordinateQualityCheckDateTime(format.format(new Date()));

		MaxMisLocation maxMisLocation = new MaxMisLocation();
		maxMisLocation.setDI(Double.valueOf(upto2Decimal.format(dI)));
		maxMisLocation.setDJ(Double.valueOf(upto2Decimal.format(dJ)));
		convertBinGridResponse.setMaxMisLocation(maxMisLocation);
	}

}