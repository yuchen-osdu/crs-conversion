package org.opengroup.osdu.crs.interfaces;

import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.model.ConvertGeoJsonResponse;
import org.opengroup.osdu.crs.model.ConvertPointsResponse;

public interface ICRSConverter {

	ConvertPointsResponse convertPoint(String from, String to, double[] xyCoordinates, double[] zCoordinates);

	ConvertGeoJsonResponse convertGeoJson(GeoJsonFeatureCollection featureCollection, String toCrs, String toUnitZ);
}
