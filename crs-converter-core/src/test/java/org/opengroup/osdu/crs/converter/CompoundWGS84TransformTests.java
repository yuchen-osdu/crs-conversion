package org.opengroup.osdu.crs.converter;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.crs.model.ConvertPointsResponse;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.offset;

/**
 * CompoundWGS84TransformTests
 *
 * This test class is part of the OSDU Crs Conversion Service and is responsible for validating
 * the functionality of compound transformations involving the WGS84 coordinate system.
 *
 * Compound transformations are two or more transformations that are added together and treated
 * as a single transformation.
 */
public class CompoundWGS84TransformTests {

    /**
     * Test case 0:
     * <p>
     * From NAD27 to WGS84 via concatenated transformation NAD27 to WGS 84 (50) (EPSG: 8603)
     * <p>
     * NAD27: 90W, 30N
     * <p>
     * WGS84: 90.00007010W, 30.00019768N
     * This is the to/from CRS parameters we would use for geographic coordinate:
     * <p>
     * "fromCRS": "{\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"NAD27\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"},\"name\":\"USA NAD27 (NADCON CONUS)\",\"type\":\"EBC\",\"authCode\":{\"auth\":\"Other\",\"code\":\"720218\"},\"compoundCT\":{\"cts\":[{\"name\":\"USA NAD27 (NADCON CONUS) - TRFM\",\"type\":\"ST\",\"wkt\":\"GEOGTRAN[\\\"USA NAD27 (NADCON CONUS) - TRFM\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.0174532925199432]],GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],AUTHORITY[\\\"Other\\\",760217]]\",\"authCode\":{\"auth\":\"Other\",\"code\":\"760217\"},\"ver\":\"PE_10_3_1\"},{\"name\":\"USA NAD83 - TRFM\",\"type\":\"ST\",\"wkt\":\"GEOGTRAN[\\\"USA NAD83 - TRFM\\\",GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",0.0],AUTHORITY[\\\"Other\\\",760219]]\",\"authCode\":{\"auth\":\"Other\",\"code\":\"760219\"},\"ver\":\"PE_10_3_1\"}],\"name\":\"USA NAD27 (NADCON CONUS to WGS84) - TRFM\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"authCode\":{\"auth\":\"Other\",\"code\":\"770003\"},\"ver\":\"PE_10_3_1\"},\"ver\":\"PE_10_3_1\"}",
     * <p>
     * "toCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}",
     */
    @Test
    public void testNad27CompoundTransform() {
        final double DELTA_L = 0.1; //0.1 meters
        final double DELTA_A = 0.00000111111; // 0.004”(arcseconds)

        String fromCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8603\"},\"cts\":[{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1241\"},\"name\":\"NAD27 to NAD83 (86)\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD27 to NAD83 (86)\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset\\\",0.0],OPERATIONACCURACY[0.15],AUTHORITY[\\\"EPSG\\\",1241]]\"},{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1719\"},\"name\":\"NAD83 to WGS 84 (1)\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD83 to WGS 84 (1)\\\",GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",0.0],OPERATIONACCURACY[1.0],AUTHORITY[\\\"EPSG\\\",1719]]\"}],\"name\":\"NAD27 to WGS 84 (50)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"NAD27\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]]\"},\"name\":\"NAD27 to WGS 84 [4267,8603]\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";

        double[] zCoordinates = new double[]{
                0, 0};
        double[] xyCoordinates = new double[]{
                -90, 30, -90, 30
        };
        double[] expectedXYCoordinates = new double[]{
                -90.00007010, 30.00019768, -90.00007010, 30.00019768
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A));
        }
        softly.assertAll();
    }

    /**
     * Test case 1: from NAD27 to WGs84 via concatenated transformation USA NAD27 (NADCON CONUS to WGS84) – TRFM.
     * NAD27: 90W, 30N
     * <p>
     * WGS84: 90.00006962W, 30.00020211E
     * <p>
     * "fromCRS":"{\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"NAD27\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"},\"name\":\"USA NAD27 (NADCON CONUS)\",\"type\":\"EBC\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"720218\"},\"compoundCT\":{\"cts\":[{\"name\":\"USA NAD27 (NADCON CONUS) - TRFM\",\"type\":\"ST\",\"wkt\":\"GEOGTRAN[\\\"USA NAD27 (NADCON CONUS) - TRFM\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.0174532925199432]],GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],AUTHORITY[\\\"Other\\\",760217]]\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"760217\"},\"ver\":\"PE_10_3_1\"},{\"name\":\"USA NAD83 - TRFM\",\"type\":\"ST\",\"wkt\":\"GEOGTRAN[\\\"USA NAD83 - TRFM\\\",GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",0.0],AUTHORITY[\\\"Other\\\",760219]]\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"760219\"},\"ver\":\"PE_10_3_1\"}],\"name\":\"USA NAD27 (NADCON CONUS to WGS84) - TRFM\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"770003\"},\"ver\":\"PE_10_3_1\"},\"ver\":\"PE_10_3_1\"}",
     * <p>
     * "points": [
     * <p>
     * {
     * <p>
     * "x": -90,
     * <p>
     * "y": 30,
     * <p>
     * "z": 0
     * <p>
     * }
     * <p>
     * ],
     * <p>
     * "toCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}"
     * <p>
     * }'
     */
    @Test
    public void testFromNAD27ToWGS84ViaConcatenatedTransformationUSANAD27() {
        //final double DELTA_L = 0.1; //0.1 meters
        final double DELTA_A = 0.00000111111; // 0.004”(arcseconds)

        String fromCRS = "{\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"NAD27\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"},\"name\":\"USA NAD27 (NADCON CONUS)\",\"type\":\"EBC\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"720218\"},\"compoundCT\":{\"cts\":[{\"name\":\"USA NAD27 (NADCON CONUS) - TRFM\",\"type\":\"ST\",\"wkt\":\"GEOGTRAN[\\\"USA NAD27 (NADCON CONUS) - TRFM\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.0174532925199432]],GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],AUTHORITY[\\\"Other\\\",760217]]\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"760217\"},\"ver\":\"PE_10_3_1\"},{\"name\":\"USA NAD83 - TRFM\",\"type\":\"ST\",\"wkt\":\"GEOGTRAN[\\\"USA NAD83 - TRFM\\\",GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",0.0],AUTHORITY[\\\"Other\\\",760219]]\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"760219\"},\"ver\":\"PE_10_3_1\"}],\"name\":\"USA NAD27 (NADCON CONUS to WGS84) - TRFM\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"770003\"},\"ver\":\"PE_10_3_1\"},\"ver\":\"PE_10_3_1\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";

        double[] zCoordinates = new double[]{
                0, 0
        };
        double[] xyCoordinates = new double[]{
                -90, 30, -90, 30
        };
        double[] expectedXYCoordinates = new double[]{
                -90.00006962, 30.00020211, -90.00006962, 30.00020211
        };

        double[] expectedZCoordinates = new double[]{
                0, 0
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A));
        }
        softly.assertAll();
    }

    /**
     * Test case 2: from Egypt1907 to WGS84 via concatenated transformation Egypt 1907 to WGS84 (2) (EPSG 8537)
     * Egypt1907: 31E, 28N
     * <p>
     * WGS84: 31.00164654E, 28.0016427E
     * <p>
     * "fromCRS":"{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"22992002\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8537\"},\"cts\":[{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1545\"},\"name\":\"Egypt_1907_To_WGS_1972\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Egypt_1907_To_WGS_1972\\\",GEOGCS[\\\"GCS_Egypt_1907\\\",DATUM[\\\"D_Egypt_1907\\\",SPHEROID[\\\"Helmert_1906\\\",6378200.0,298.3]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1972\\\",DATUM[\\\"D_WGS_1972\\\",SPHEROID[\\\"WGS_1972\\\",6378135.0,298.26]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-121.8],PARAMETER[\\\"Y_Axis_Translation\\\",98.1],PARAMETER[\\\"Z_Axis_Translation\\\",-15.2],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",1545]]\"},{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1237\"},\"name\":\"WGS_1972_To_WGS_1984_1\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"WGS_1972_To_WGS_1984_1\\\",GEOGCS[\\\"GCS_WGS_1972\\\",DATUM[\\\"D_WGS_1972\\\",SPHEROID[\\\"WGS_1972\\\",6378135.0,298.26]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",4.5],PARAMETER[\\\"X_Axis_Rotation\\\",0.0],PARAMETER[\\\"Y_Axis_Rotation\\\",0.0],PARAMETER[\\\"Z_Axis_Rotation\\\",0.554],PARAMETER[\\\"Scale_Difference\\\",0.2263],OPERATIONACCURACY[2.0],AUTHORITY[\\\"EPSG\\\",1237]]\"}],\"name\":\"Egypt 1907 to WGS 84 (2)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"22992\"},\"name\":\"Egypt_Red_Belt\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"Egypt_Red_Belt\\\",GEOGCS[\\\"GCS_Egypt_1907\\\",DATUM[\\\"D_Egypt_1907\\\",SPHEROID[\\\"Helmert_1906\\\",6378200.0,298.3]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",615000.0],PARAMETER[\\\"False_Northing\\\",810000.0],PARAMETER[\\\"Central_Meridian\\\",31.0],PARAMETER[\\\"Scale_Factor\\\",1.0],PARAMETER[\\\"Latitude_Of_Origin\\\",30.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",22992]]\"},\"name\":\"Egypt 1907 * MCE-Egy / Red Belt [22992,8537]\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
     * <p>
     * "points": [
     * <p>
     * {
     * <p>
     * "x": 31.0,
     * <p>
     * "y": 28.0,
     * <p>
     * "z": 0
     * <p>
     * }
     * <p>
     * ],
     * <p>
     * "toCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}"
     * <p>
     * }'
     */
    @Test
    public void testFromEgypt1907ToWGS84viaConcatenatedTransformation() {
        final double DELTA_L = 0.1; //0.1 meters
        final double DELTA_A = 0.00000111111; // 0.004”(arcseconds)

        String fromCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4229\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8537\"},\"cts\":[{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1545\"},\"name\":\"Egypt_1907_To_WGS_1972\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Egypt_1907_To_WGS_1972\\\",GEOGCS[\\\"GCS_Egypt_1907\\\",DATUM[\\\"D_Egypt_1907\\\",SPHEROID[\\\"Helmert_1906\\\",6378200.0,298.3]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1972\\\",DATUM[\\\"D_WGS_1972\\\",SPHEROID[\\\"WGS_1972\\\",6378135.0,298.26]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-121.8],PARAMETER[\\\"Y_Axis_Translation\\\",98.1],PARAMETER[\\\"Z_Axis_Translation\\\",-15.2],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",1545]]\"},{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1237\"},\"name\":\"WGS_1972_To_WGS_1984_1\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"WGS_1972_To_WGS_1984_1\\\",GEOGCS[\\\"GCS_WGS_1972\\\",DATUM[\\\"D_WGS_1972\\\",SPHEROID[\\\"WGS_1972\\\",6378135.0,298.26]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",4.5],PARAMETER[\\\"X_Axis_Rotation\\\",0.0],PARAMETER[\\\"Y_Axis_Rotation\\\",0.0],PARAMETER[\\\"Z_Axis_Rotation\\\",0.554],PARAMETER[\\\"Scale_Difference\\\",0.2263],OPERATIONACCURACY[2.0],AUTHORITY[\\\"EPSG\\\",1237]]\"}],\"name\":\"Egypt 1907 to WGS 84 (2)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4229\"},\"name\":\"Egypt 1907\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_Egypt_1907\\\",DATUM[\\\"D_Egypt_1907\\\",SPHEROID[\\\"Helmert_1906\\\",6378200.0,298.3]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]]\"},\"name\":\"Egypt 1907 to WGS 84 [4229,8537]\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";

        double[] xyCoordinates = new double[]{
                31, 28, 31, 28
        };
        double[] zCoordinates = new double[]{
                0, 0
        };

        double[] expectedXYCoordinates = new double[]{
                31.00164654, 28.00016427, 31.00164654, 28.00016427
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A));
        }
        softly.assertAll();
    }

    /**
     * Test case 3: from  NAD27 to WGS84 via concatenated transformation NAD27 to WGS84 (79) (EPSG: 15851)
     * NAD27: 102W, 32N
     * <p>
     * WGS84: 102.00041255W, 32.00012411N
     * <p>
     * "fromCRS":"{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"4267079\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"GCS_North_American_1927\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4267]]\"},\"name\":\"NAD27 * OGP-Usa Conus [4267,15851]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
     * <p>
     * "points": [
     * <p>
     * {
     * <p>
     * "x": -102.0,
     * <p>
     * "y": 32,
     * <p>
     * "z": 10
     * <p>
     * }
     * <p>
     * ],
     * <p>
     * "toCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}"
     * <p>
     * }'
     */
    @Test
    public void testFromNAD27ToWGS84ViaConcatenatedTransformation() {
        //final double DELTA_L = 0.1; //0.1 meters
        final double DELTA_A = 0.00000111111; // 0.004”(arcseconds)

        String fromCRS = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"4267079\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"GCS_North_American_1927\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4267]]\"},\"name\":\"NAD27 * OGP-Usa Conus [4267,15851]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";

        double[] zCoordinates = new double[]{
                10, 10
        };
        double[] xyCoordinates = new double[]{
                -102, 32, -102, 32
        };
        double[] expectedXYCoordinates = new double[]{
                -102.00041255, 32.00012411, -102.00041255, 32.00012411
        };

        double[] expectedZCoordinates = new double[]{
                10, 10
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A));
        }
        softly.assertAll();
    }

    /**
     * Tests the conversion from Egypt 1907 (EPSG:4229) to WGS 84 (EPSG:4326) using
     * the default transformation steps specified in the EPSG database.
     *
     * <p>This test is designed to cover the use case where the steps of the compound
     * transformation are not explicitly provided. Instead, the test relies on the
     * default transformation steps as mentioned in the EPSG specification. For the
     * transformation EPSG:8537 (Egypt 1907 to WGS 84 (2)), the steps involved are:
     * <ul>
     *   <li>Transformation code 1545: Egypt 1907 to WGS 1972</li>
     *   <li>Transformation code 1237: WGS 1972 to WGS 84</li>
     * </ul>
     * These steps are defined in the EPSG specification and will be used by the
     * CRSConverter during the conversion process.</p>
     *
     * <p>The test validates the correctness of the transformation by comparing the
     * expected XY and Z coordinates with the actual results produced by the
     * CRSConverter.</p>
     *
     * @throws org.assertj.core.api.SoftAssertions if the transformed coordinates
     * do not match the expected values within the specified tolerance.
     */
    @Test
    public void testFromEgypt1907ToWGS84() {
        final double DELTA_L = 0.1; //0.1 meters
        final double DELTA_A = 0.00000111111; // 0.004”(arcseconds)

        String fromCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4229\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8537\"},\"name\":\"Egypt 1907 to WGS 84 (2)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4229\"},\"name\":\"Egypt_1907\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_Egypt_1907\\\",DATUM[\\\"D_Egypt_1907\\\",SPHEROID[\\\"Helmert_1906\\\",6378200.0,298.3]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]]\"},\"name\":\"Egypt 1907 to WGS 84 [4229,8537]\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";

        double[] xyCoordinates = new double[]{
                31, 28, 31, 28
        };
        double[] zCoordinates = new double[]{
                0, 0
        };

        double[] expectedXYCoordinates = new double[]{
                31.00164654, 28.00016427, 31.00164654, 28.00016427
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A));
        }
        softly.assertAll();
    }

    /* ***************************** Backward direction tests ***************************** */
    @Test
    public void testWGS84toNad27CompoundTransform() {
        //final double DELTA_A = 0.00001; // Standard delta, but we strive to raise precision to target value
        final double DELTA_L = 0.1; //0.1 meters
        final double DELTA_A = 0.00000111111; // 0.004”(arcseconds)

        String fromCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8603\"},\"cts\":[{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1241\"},\"name\":\"NAD27 to NAD83 (86)\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD27 to NAD83 (86)\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset\\\",0.0],OPERATIONACCURACY[0.15],AUTHORITY[\\\"EPSG\\\",1241]]\"},{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1719\"},\"name\":\"NAD83 to WGS 84 (1)\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD83 to WGS 84 (1)\\\",GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",0.0],OPERATIONACCURACY[1.0],AUTHORITY[\\\"EPSG\\\",1719]]\"}],\"name\":\"NAD27 to WGS 84 (50)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"NAD27\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]]\"},\"name\":\"NAD27 to WGS 84 [4267,8603]\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}";

        double[] zCoordinates = new double[]{
                0, 0};
        double[]  xyCoordinates = new double[]{
                -90.00007010, 30.00019768, -90.00007010, 30.00019768
        };
        double[] expectedXYCoordinates = new double[]{
                -90, 30, -90, 30
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A));
        }
        softly.assertAll();
    }

    @Test
    public void testFromWGS84ToNAD27ViaConcatenatedTransformationUSANAD27() {
        //final double DELTA_L = 0.1; //0.1 meters
        final double DELTA_A = 0.00000111111; // 0.004”(arcseconds)

        String fromCRS= "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";
        String toCRS = "{\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"NAD27\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"},\"name\":\"USA NAD27 (NADCON CONUS)\",\"type\":\"EBC\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"720218\"},\"compoundCT\":{\"cts\":[{\"name\":\"USA NAD27 (NADCON CONUS) - TRFM\",\"type\":\"ST\",\"wkt\":\"GEOGTRAN[\\\"USA NAD27 (NADCON CONUS) - TRFM\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.0174532925199432]],GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],AUTHORITY[\\\"Other\\\",760217]]\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"760217\"},\"ver\":\"PE_10_3_1\"},{\"name\":\"USA NAD83 - TRFM\",\"type\":\"ST\",\"wkt\":\"GEOGTRAN[\\\"USA NAD83 - TRFM\\\",GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",0.0],AUTHORITY[\\\"Other\\\",760219]]\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"760219\"},\"ver\":\"PE_10_3_1\"}],\"name\":\"USA NAD27 (NADCON CONUS to WGS84) - TRFM\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"770003\"},\"ver\":\"PE_10_3_1\"},\"ver\":\"PE_10_3_1\"}";

        double[] zCoordinates = new double[]{
                0, 0
        };
        double[] xyCoordinates = new double[]{
                -90.00006962, 30.00020211, -90.00006962, 30.00020211
        };
        double[] expectedXYCoordinates = new double[]{
                -90, 30, -90, 30
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A));
        }
        softly.assertAll();
    }

    @Test
    public void testFromWGS84ToEgypt1907viaConcatenatedTransformation() {
        //final double DELTA_L = 0.1; //0.1 meters
        final double DELTA_A = 0.00000111111; // 0.004”(arcseconds)

        String fromCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4229\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8537\"},\"cts\":[{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1545\"},\"name\":\"Egypt_1907_To_WGS_1972\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Egypt_1907_To_WGS_1972\\\",GEOGCS[\\\"GCS_Egypt_1907\\\",DATUM[\\\"D_Egypt_1907\\\",SPHEROID[\\\"Helmert_1906\\\",6378200.0,298.3]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1972\\\",DATUM[\\\"D_WGS_1972\\\",SPHEROID[\\\"WGS_1972\\\",6378135.0,298.26]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-121.8],PARAMETER[\\\"Y_Axis_Translation\\\",98.1],PARAMETER[\\\"Z_Axis_Translation\\\",-15.2],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",1545]]\"},{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1237\"},\"name\":\"WGS_1972_To_WGS_1984_1\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"WGS_1972_To_WGS_1984_1\\\",GEOGCS[\\\"GCS_WGS_1972\\\",DATUM[\\\"D_WGS_1972\\\",SPHEROID[\\\"WGS_1972\\\",6378135.0,298.26]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",4.5],PARAMETER[\\\"X_Axis_Rotation\\\",0.0],PARAMETER[\\\"Y_Axis_Rotation\\\",0.0],PARAMETER[\\\"Z_Axis_Rotation\\\",0.554],PARAMETER[\\\"Scale_Difference\\\",0.2263],OPERATIONACCURACY[2.0],AUTHORITY[\\\"EPSG\\\",1237]]\"}],\"name\":\"Egypt 1907 to WGS 84 (2)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4229\"},\"name\":\"Egypt 1907\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_Egypt_1907\\\",DATUM[\\\"D_Egypt_1907\\\",SPHEROID[\\\"Helmert_1906\\\",6378200.0,298.3]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]]\"},\"name\":\"Egypt 1907 to WGS 84 [4229,8537]\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}";

        double[] zCoordinates = new double[]{
                0, 0
        };
        double[] xyCoordinates = new double[]{
                31.00164654, 28.00016427, 31.00164654, 28.00016427
        };
        double[] expectedXYCoordinates = new double[]{
                31, 28, 31, 28
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A));
        }
        softly.assertAll();
    }

    @Test
    public void testFromWGS84ToNAD27ViaConcatenatedTransformation() {
        //final double DELTA_L = 0.1; //0.1 meters
        final double DELTA_A = 0.00000111111; // 0.004”(arcseconds)

        String fromCRS  = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"4267079\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"GCS_North_American_1927\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4267]]\"},\"name\":\"NAD27 * OGP-Usa Conus [4267,15851]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}";

        double[] zCoordinates = new double[]{
                10, 10
        };
        double[] xyCoordinates = new double[]{
                -102.00041255, 32.00012411, -102.00041255, 32.00012411
        };
        double[] expectedXYCoordinates  = new double[]{
                -102, 32, -102, 32
        };
        double[] expectedZCoordinates = new double[]{
                10, 10
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A));
        }
        softly.assertAll();
    }

    @Test
    public void testFromWGS84ToEgypt1907() {
        //final double DELTA_L = 0.1; //0.1 meters
        final double DELTA_A = 0.00000111111; // 0.004”(arcseconds)

        String fromCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4229\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8537\"},\"name\":\"Egypt 1907 to WGS 84 (2)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4229\"},\"name\":\"Egypt_1907\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_Egypt_1907\\\",DATUM[\\\"D_Egypt_1907\\\",SPHEROID[\\\"Helmert_1906\\\",6378200.0,298.3]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]]\"},\"name\":\"Egypt 1907 to WGS 84 [4229,8537]\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}";

        double[] zCoordinates = new double[]{
                0, 0
        };
        double[] xyCoordinates  = new double[]{
                31.00164654, 28.00016427, 31.00164654, 28.00016427
        };
        double[] expectedXYCoordinates = new double[]{
                31, 28, 31, 28
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A));
        }
        softly.assertAll();
    }

    /* ***************************** Round-trip tests *****************************
     * For the round-trip calculation, the initial coordinates of the point should change by less than 6mm (0.0002”) after 1000 iterations.
     */
    @Test
    public void roundTripTestNad27CompoundTransform() {
        //final double DELTA_L_ROUNDTRIP = 0.006; //6mm
        final double DELTA_A_ROUNDTRIP = 0.0000000555555;// 0.0002”

        final int ROUND_TRIP_NUMBER = 1000;

        String fromCRS = "{\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"NAD27\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"},\"name\":\"USA NAD27 (NADCON CONUS)\",\"type\":\"EBC\",\"authCode\":{\"auth\":\"Other\",\"code\":\"720218\"},\"compoundCT\":{\"cts\":[{\"name\":\"USA NAD27 (NADCON CONUS) - TRFM\",\"type\":\"ST\",\"wkt\":\"GEOGTRAN[\\\"USA NAD27 (NADCON CONUS) - TRFM\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.0174532925199432]],GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],AUTHORITY[\\\"Other\\\",760217]]\",\"authCode\":{\"auth\":\"Other\",\"code\":\"760217\"},\"ver\":\"PE_10_3_1\"},{\"name\":\"USA NAD83 - TRFM\",\"type\":\"ST\",\"wkt\":\"GEOGTRAN[\\\"USA NAD83 - TRFM\\\",GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",0.0],AUTHORITY[\\\"Other\\\",760219]]\",\"authCode\":{\"auth\":\"Other\",\"code\":\"760219\"},\"ver\":\"PE_10_3_1\"}],\"name\":\"USA NAD27 (NADCON CONUS to WGS84) - TRFM\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"authCode\":{\"auth\":\"Other\",\"code\":\"770003\"},\"ver\":\"PE_10_3_1\"},\"ver\":\"PE_10_3_1\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";

        double[] zCoordinates = new double[]{
                0, 0};
        double[] xyCoordinates = new double[]{
                -90, 30, -90, 30
        };
        double[] expectedXYCoordinates = new double[]{
                -90, 30, -90, 30
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse resultBack = null;

        for(int i = 0; i < ROUND_TRIP_NUMBER; i++) {
            converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
            resultBack = converter.convertPoint(toCRS, fromCRS, xyCoordinates, zCoordinates);
        }

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(resultBack.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A_ROUNDTRIP));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A_ROUNDTRIP));
        }
        softly.assertAll();
    }

    @Test
    public void roundTripTestFromNAD27ToWGS84ViaConcatenatedTransformationUSANAD27() {
        //final double DELTA_L_ROUNDTRIP = 0.006; //6mm
        final double DELTA_A_ROUNDTRIP = 0.0000000555555;// 0.0002”

        final int ROUND_TRIP_NUMBER = 1000;

        String fromCRS = "{\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"NAD27\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"},\"name\":\"USA NAD27 (NADCON CONUS)\",\"type\":\"EBC\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"720218\"},\"compoundCT\":{\"cts\":[{\"name\":\"USA NAD27 (NADCON CONUS) - TRFM\",\"type\":\"ST\",\"wkt\":\"GEOGTRAN[\\\"USA NAD27 (NADCON CONUS) - TRFM\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698214]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.0174532925199432]],GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],AUTHORITY[\\\"Other\\\",760217]]\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"760217\"},\"ver\":\"PE_10_3_1\"},{\"name\":\"USA NAD83 - TRFM\",\"type\":\"ST\",\"wkt\":\"GEOGTRAN[\\\"USA NAD83 - TRFM\\\",GEOGCS[\\\"GCS_North_American_1983\\\",DATUM[\\\"D_North_American_1983\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",0.0],AUTHORITY[\\\"Other\\\",760219]]\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"760219\"},\"ver\":\"PE_10_3_1\"}],\"name\":\"USA NAD27 (NADCON CONUS to WGS84) - TRFM\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"authCode\":{\"auth\":\"CTL\",\"code\":\"770003\"},\"ver\":\"PE_10_3_1\"},\"ver\":\"PE_10_3_1\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";

        double[] zCoordinates = new double[]{
                0, 0
        };
        double[] xyCoordinates = new double[]{
                -90, 30, -90, 30
        };
        double[] expectedXYCoordinates = new double[]{
                -90, 30, -90, 30
        };

        double[] expectedZCoordinates = new double[]{
                0, 0
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse resultBack = null;

        for(int i = 0; i < ROUND_TRIP_NUMBER; i++) {
            converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
            resultBack = converter.convertPoint(toCRS, fromCRS, xyCoordinates, zCoordinates);
        }

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(resultBack.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A_ROUNDTRIP));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A_ROUNDTRIP));
        }
        softly.assertAll();
    }

    @Test
    public void roundTripTestFromEgypt1907ToWGS84viaConcatenatedTransformation() {
        final double DELTA_A_ROUNDTRIP = 0.00001; // Standard delta, but we strive to raise precision to target value

        //final double DELTA_L_ROUNDTRIP = 0.006; //6mm
        //final double DELTA_A_ROUNDTRIP = 0.0000000555555;// 0.0002”

        final int ROUND_TRIP_NUMBER = 1000;

        String fromCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4229\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8537\"},\"cts\":[{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1545\"},\"name\":\"Egypt_1907_To_WGS_1972\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Egypt_1907_To_WGS_1972\\\",GEOGCS[\\\"GCS_Egypt_1907\\\",DATUM[\\\"D_Egypt_1907\\\",SPHEROID[\\\"Helmert_1906\\\",6378200.0,298.3]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1972\\\",DATUM[\\\"D_WGS_1972\\\",SPHEROID[\\\"WGS_1972\\\",6378135.0,298.26]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-121.8],PARAMETER[\\\"Y_Axis_Translation\\\",98.1],PARAMETER[\\\"Z_Axis_Translation\\\",-15.2],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",1545]]\"},{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1237\"},\"name\":\"WGS_1972_To_WGS_1984_1\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"WGS_1972_To_WGS_1984_1\\\",GEOGCS[\\\"GCS_WGS_1972\\\",DATUM[\\\"D_WGS_1972\\\",SPHEROID[\\\"WGS_1972\\\",6378135.0,298.26]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",4.5],PARAMETER[\\\"X_Axis_Rotation\\\",0.0],PARAMETER[\\\"Y_Axis_Rotation\\\",0.0],PARAMETER[\\\"Z_Axis_Rotation\\\",0.554],PARAMETER[\\\"Scale_Difference\\\",0.2263],OPERATIONACCURACY[2.0],AUTHORITY[\\\"EPSG\\\",1237]]\"}],\"name\":\"Egypt 1907 to WGS 84 (2)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4229\"},\"name\":\"Egypt 1907\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_Egypt_1907\\\",DATUM[\\\"D_Egypt_1907\\\",SPHEROID[\\\"Helmert_1906\\\",6378200.0,298.3]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]]\"},\"name\":\"Egypt 1907 to WGS 84 [4229,8537]\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";

        double[] zCoordinates = new double[]{
                0, 0
        };
        double[] xyCoordinates = new double[]{
                28, 31, 28, 31
        };
        double[] expectedXYCoordinates = new double[]{
                28, 31, 28, 31
        };

        double[] expectedZCoordinates = new double[]{
                0, 0
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse resultBack = null;

        for (int i = 0; i < ROUND_TRIP_NUMBER; i++) {
            converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
            resultBack = converter.convertPoint(toCRS, fromCRS, xyCoordinates, zCoordinates);
        }

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(resultBack.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A_ROUNDTRIP));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A_ROUNDTRIP));
        }
        softly.assertAll();
    }

    @Test
    public void roundTripTestFromNAD27ToWGS84ViaConcatenatedTransformation() {
        //final double DELTA_L_ROUNDTRIP = 0.006; //6mm
        final double DELTA_A_ROUNDTRIP = 0.0000000555555;// 0.0002”

        final int ROUND_TRIP_NUMBER = 1000;

        String fromCRS = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"4267079\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"GCS_North_American_1927\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4267]]\"},\"name\":\"NAD27 * OGP-Usa Conus [4267,15851]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";

        double[] zCoordinates = new double[]{
                10, 10
        };
        double[] xyCoordinates = new double[]{
                -102, 32, -102, 32
        };
        double[] expectedXYCoordinates = new double[]{
                -102, 32, -102, 32
        };

        double[] expectedZCoordinates = new double[]{
                10, 10
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse resultBack = null;

        for (int i = 0; i < ROUND_TRIP_NUMBER; i++) {
            converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
            resultBack = converter.convertPoint(toCRS, fromCRS, xyCoordinates, zCoordinates);
        }

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(resultBack.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A_ROUNDTRIP));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A_ROUNDTRIP));
        }
        softly.assertAll();
    }

    @Test
    public void roundTripTestEgypt1907ToWGS84() {

        final double DELTA_A_ROUNDTRIP = 0.00001; // Standard delta, but we strive to raise precision to target value
        //final double DELTA_L_ROUNDTRIP = 0.006; //6mm
        //final double DELTA_A_ROUNDTRIP = 0.0000000555555;// 0.0002”

        final int ROUND_TRIP_NUMBER = 1000;

        String fromCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4229\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8537\"},\"name\":\"Egypt 1907 to WGS 84 (2)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4229\"},\"name\":\"Egypt_1907\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_Egypt_1907\\\",DATUM[\\\"D_Egypt_1907\\\",SPHEROID[\\\"Helmert_1906\\\",6378200.0,298.3]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]]\"},\"name\":\"Egypt 1907 to WGS 84 [4229,8537]\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}";

        double[] zCoordinates = new double[]{
                0, 0
        };
        double[] xyCoordinates = new double[]{
                28, 31, 28, 31
        };

        double[] expectedXYCoordinates = new double[]{
                28, 31, 28, 31
        };

        double[] expectedZCoordinates = new double[]{
                0, 0
        };

        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse resultBack = null;

        for(int i = 0; i < ROUND_TRIP_NUMBER; i++) {
            ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
            resultBack = converter.convertPoint(toCRS, fromCRS, xyCoordinates, zCoordinates);
        }

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(resultBack.getSuccessCount()).isEqualTo(expectedXYCoordinates.length / 2);

        for (int i = 0; i < expectedXYCoordinates.length; i++) {

            softly.assertThat(xyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(expectedXYCoordinates[i], offset(DELTA_A_ROUNDTRIP));

            softly.assertThat(zCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(expectedZCoordinates[i % 2], offset(DELTA_A_ROUNDTRIP));
        }
        softly.assertAll();
    }
}
