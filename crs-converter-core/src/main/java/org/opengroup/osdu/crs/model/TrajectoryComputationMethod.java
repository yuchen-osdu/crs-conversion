package org.opengroup.osdu.crs.model;

import com.google.common.base.Strings;

public enum TrajectoryComputationMethod {

    AzimuthalEquidistant("AzimuthalEquidistant"),
    LeesModifiedProposal("LeesModifiedProposal");
    TrajectoryComputationMethod(final String method) {
        this.method = method;
    }

    private final String method;

    public static TrajectoryComputationMethod getTrajectoryComputationMethod(String hint) {
        if (!Strings.isNullOrEmpty(hint)) {
            String method = hint.toLowerCase();
            if (method.contains("lmp") || method.contains("lee")) return LeesModifiedProposal;
            if (method.contains("azi")) return AzimuthalEquidistant;
        }
        return null;
    }

    @Override
    public String toString() {
        return this.method;
    }
}
