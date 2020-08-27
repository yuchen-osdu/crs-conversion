package org.opengroup.osdu.crs.sis.operation;

import java.util.ArrayList;
import java.util.List;
import org.opengroup.osdu.crs.model.CRSType;
import org.opengroup.osdu.crs.model.ICompoundTrf;
import org.opengroup.osdu.crs.model.ICrs;
import org.opengroup.osdu.crs.model.IEarlyBoundCrs;
import org.opengroup.osdu.crs.model.ISingleTrf;
import org.opengroup.osdu.crs.model.ITrf;
import org.opengroup.osdu.crs.model.Impl.AuthorityCode;
import org.opengroup.osdu.crs.sis.AuthorityCodeUtils;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.SisCrsUtils;
import org.opengroup.osdu.crs.sis.transform.CompoundFallbackWGS84TransformWithCode;
import org.opengroup.osdu.crs.sis.transform.SingleWGS84TransformFromCrs;
import org.opengroup.osdu.crs.sis.transform.SingleWGS84TransformFromCode;
import org.opengroup.osdu.crs.util.Constants;
import org.opengroup.osdu.crs.sis.transform.ISisMathTransform;
import org.opengroup.osdu.crs.sis.transform.IWGS84Transform;

public class CRSCoordinateOperationFactory {

    public List<ICRSCoordinateOperation> createOperations(ICrs fromCrs, ICrs toCrs) throws IllegalArgumentException {
        try {
            List<ICRSCoordinateOperation> operations = new ArrayList<>();

            //check if transformations is needed
            boolean fromTransformOperationsNeeded = true;
            boolean toTransformOperationsNeeded = true;

            ISisCrs fromBaseCrs = fromCrs.getBaseGeographicCrs();
            ISisCrs toBaseCrs = toCrs.getBaseGeographicCrs();
            if (!needTransform(fromCrs, toCrs)) {
                fromTransformOperationsNeeded = false;
                toTransformOperationsNeeded = false;
            } else {
                AuthorityCode fromAuthorityCode = fromBaseCrs.getAuthorityCode();
                if (fromAuthorityCode != null && AuthorityCodeUtils.isWGS84(fromAuthorityCode)) {
                    fromTransformOperationsNeeded = false;
                }
                AuthorityCode toAuthorityCode = toBaseCrs.getAuthorityCode();
                if (toAuthorityCode != null && AuthorityCodeUtils.isWGS84(toAuthorityCode)) {
                    toTransformOperationsNeeded = false;
                }
            }

            if (needFromProjection(fromCrs, toCrs, fromTransformOperationsNeeded, toTransformOperationsNeeded)) {
                ISisCrs fromProjectedCRS = fromCrs.getProjectedCrs();
                ISisCrs fromBaseCRS = fromCrs.getBaseGeographicCrs();
                operations.add(new CRSProjectionOperation(fromProjectedCRS, fromBaseCRS, null));
            }

            //add transforms to wgs 84
            if (fromTransformOperationsNeeded) {
                ITrf transform = getTransformation(fromCrs);
                if (transform == null) {
                    throw new IllegalArgumentException(Constants.ERROR_MSG_NO_SUITABLE_CONVERSION);
                }
                ISisMathTransform simpleDatumTransform = getSimpleTransformIfNeeded(transform);
                if (simpleDatumTransform != null) {
                    SingleWGS84TransformFromCrs simpleTransform = new SingleWGS84TransformFromCrs(fromBaseCrs, simpleDatumTransform);
                    operations.add(new CRSTransformToWGS84Operation(simpleTransform));
                } else {
                    if (transform.getType() == CRSType.TRF) {
                        IWGS84Transform transformOperation = new SingleWGS84TransformFromCode(fromBaseCrs, (ISingleTrf) transform, false);
                        operations.add(new CRSTransformToWGS84Operation(transformOperation));
                    } else if (transform.getType() == CRSType.COMPOUND_TRF) {
                        IWGS84Transform transformOperation = new CompoundFallbackWGS84TransformWithCode(fromBaseCrs, (ICompoundTrf) transform);
                        operations.add(new CRSTransformToWGS84Operation(transformOperation));
                    }
                }
            }

            //add transforms from wgs 84
            if (toTransformOperationsNeeded) {
                ITrf transform = getTransformation(toCrs);
                if (transform == null) {
                    throw new IllegalArgumentException(Constants.ERROR_MSG_NO_SUITABLE_CONVERSION);
                }
                ISisMathTransform simpleDatumTransform = getSimpleTransformIfNeeded(transform);
                if (simpleDatumTransform != null) {
                    SingleWGS84TransformFromCrs simpleTransform = new SingleWGS84TransformFromCrs(toBaseCrs, simpleDatumTransform);
                    operations.add(new CRSTransformFromWGS84Operation(simpleTransform));
                } else {
                    if (transform.getType() == CRSType.TRF) {
                        IWGS84Transform transformOperation = new SingleWGS84TransformFromCode(toBaseCrs, (ISingleTrf) transform, false);
                        operations.add(new CRSTransformFromWGS84Operation(transformOperation));
                    } else if (transform.getType() == CRSType.COMPOUND_TRF) {
                        IWGS84Transform transformOperation = new CompoundFallbackWGS84TransformWithCode(toBaseCrs, (ICompoundTrf) transform);
                        operations.add(new CRSTransformFromWGS84Operation(transformOperation));
                    }
                }
            }

            if (needToProjection(fromCrs, toCrs, fromTransformOperationsNeeded, toTransformOperationsNeeded)) {
                ISisCrs toProjectedCrs = toCrs.getProjectedCrs();
                operations.add(new CRSProjectionOperation(toBaseCrs, toProjectedCrs, null));
            }
            return operations;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can't convert points", ex);
        }

    }

    private boolean needFromProjection(ICrs fromCrs, ICrs toCrs, boolean fromTransformNeeded, boolean toTransformNeeded) {
        ISisCrs fromProjectionCrs = fromCrs.getProjectedCrs();
        if (fromProjectionCrs == null) {
            return false;
        }
        if (fromTransformNeeded || toTransformNeeded) {
            return true;
        }
        ISisCrs toProjectionCrs = toCrs.getProjectedCrs();
        if (toProjectionCrs != null && fromProjectionCrs.isEqual(toProjectionCrs)) {
            return false;
        }
        return true;
    }

    private boolean needToProjection(ICrs fromCrs, ICrs toCrs, boolean fromTransformNeeded, boolean toTransformNeeded) {
        ISisCrs toProjectionCrs = toCrs.getProjectedCrs();
        if (toProjectionCrs == null) {
            return false;
        }
        if (fromTransformNeeded || toTransformNeeded) {
            return true;
        }
        ISisCrs fromProjectionCrs = fromCrs.getProjectedCrs();
        if (fromProjectionCrs != null && fromProjectionCrs.isEqual(toProjectionCrs)) {
            return false;
        }
        return true;
    }

    private boolean needTransform(ICrs fromCRS, ICrs toCRS) {
        //isValid has already been called and both fromCRS and toCRS must be valid
        ISisCrs fromBaseSISCrs = fromCRS.getBaseGeographicCrs();
        ISisCrs toBaseSISCrs = toCRS.getBaseGeographicCrs();

        validateRoute(fromCRS, toCRS);
        if (fromCRS.getType() == CRSType.LATE_BOUND && toCRS.getType() == CRSType.LATE_BOUND) {
            return false;
        }

        boolean skip = false;
        ITrf trf1 = getTransformation(fromCRS);
        ITrf trf2 = getTransformation(toCRS);
        if (trf1 != null && trf2 != null) {
            skip = fromBaseSISCrs.isEqual(toBaseSISCrs) && trf1.equalInBehavior(trf2);
        } else if (trf1 != null || trf2 != null) {
            skip = fromBaseSISCrs.isEqual(toBaseSISCrs);
        }
        return !skip;
    }
    
    private ITrf getTransformation(ICrs crs) {
        if (!(crs instanceof IEarlyBoundCrs)) {
            return null;
        }
        return ((IEarlyBoundCrs) crs).getTrf();
    }

    private void validateRoute(ICrs fromCRS, ICrs toCRS) {
        if (fromCRS.getType() == CRSType.LATE_BOUND && toCRS.getType() == CRSType.LATE_BOUND) {
            if (!SisCrsUtils.isValidConversionRoute(fromCRS, toCRS)) {
                throw new IllegalArgumentException(Constants.ERROR_MSG_NO_SUITABLE_CONVERSION + generateFromToMessage(fromCRS, toCRS));
            }
        }
        //source crs for both transform should be WGS84, no need to check transforms
    }

    private ISisMathTransform getSimpleTransformIfNeeded(ITrf transform) {
        if (transform.getType() == CRSType.TRF) {
            ISisMathTransform datumOperation = ((ISingleTrf) transform).getTransformOperation();
            if (datumOperation.getDerivedType() == ISisMathTransform.TransformDerivedType.DERIVED_FROM_CRS) {
                return datumOperation;
            }
        } else if (transform.getType() == CRSType.COMPOUND_TRF) {
            List<ISingleTrf> singleTransforms = ((ICompoundTrf) transform).getTransformations();
            ISisMathTransform firstSimpleTransformFound = null;
            for (ISingleTrf currentSingleTransform : singleTransforms) {
                ISisMathTransform datumOperation = currentSingleTransform.getTransformOperation();
                if (datumOperation.getDerivedType() == ISisMathTransform.TransformDerivedType.DERIVED_FROM_CRS) {
                    firstSimpleTransformFound = datumOperation;
                } else {
                    return null;
                }
            }
            return firstSimpleTransformFound;
        }
        return null;

    }

    private String generateFromToMessage(ICrs from, ICrs to) {
        String fromCRS = "";
        String toCRS = "";
        if (from != null) {
            fromCRS = from.getName();
        }
        if (to != null) {
            toCRS = to.getName();
        }
        return " no transformation '" + fromCRS + "' -> '" + toCRS + "'";
    }

}
