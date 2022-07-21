package org.opengroup.osdu.crs.model;

import java.util.List;

public interface ICompoundTrf extends ITrf {
    String getPolicy();
    void setPolicy(String policy);

    List<ISingleTrf> getTransformations();
    void setTransformations(List<ISingleTrf> transformations);

    int[] getForwardDirection();
    void setForwardDirection(int[] directions);

    int[] getInverseDirection();
    void setInverseDirection(int[] directions);

    boolean isFallback();
    boolean isConcatenated();
}
