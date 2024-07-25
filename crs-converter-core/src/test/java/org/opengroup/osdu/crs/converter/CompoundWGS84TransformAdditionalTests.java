package org.opengroup.osdu.crs.converter;

import java.util.Arrays;
import java.util.Collection;
import static org.assertj.core.api.Assertions.offset;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opengroup.osdu.crs.model.ConvertPointsResponse;

@RunWith(Parameterized.class)
public class CompoundWGS84TransformAdditionalTests {

    static final double DELTA_L = 0.1; //0.1 meters
    static final double DELTA_A = 0.00000111111; // 0.004”(arcseconds)

    private final String fromCRS;
    private final String toCRS;
    private final double delta;
    private final double[] nonWgs84XyCoordinates;
    private final double[] nonWgs84ZCoordinates;
    private final double[] wgs84XYCoordinates;
    private final double[] wgs84ZCoordinates;

    public CompoundWGS84TransformAdditionalTests(String fromCRS, String toCRS, double delta, double[] nonWgs84XyCoordinates, double[] nonWgs84ZCoordinates, double[] wgs84XYCoordinates, double[] wgs84ZCoordinates) {
        this.fromCRS = fromCRS;
        this.toCRS = toCRS;
        this.delta = delta;
        this.nonWgs84XyCoordinates = nonWgs84XyCoordinates;
        this.nonWgs84ZCoordinates = nonWgs84ZCoordinates;
        this.wgs84XYCoordinates = wgs84XYCoordinates;
        this.wgs84ZCoordinates = wgs84ZCoordinates;
    }

    // Define the parameters for the test
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        /* Parameters: {fromCRS, toCRS, delta, nonWgs84ZCoordinates, nonWgs84XyCoordinates, wgs84XYCoordinates, wgs84ZCoordinates} */

        /*
            Test 1 (run this from WGS 84 to Chos Malal, and then from Chos Malal to WGS 84)
            and confirm rountrip is zero error to 8 decimal places):

            WGS 84 lon=x= -69.0; lat=y= -38.0
            Chos Malal lon=x= -69.00077939; lat=y= -38.00089782
        */

        Object[] test1 = {
                "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4160\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8517\"},\"cts\":[{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1528\"},\"name\":\"Chos_Malal_1914_To_Campo_Inchauspe\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Chos_Malal_1914_To_Campo_Inchauspe\\\",GEOGCS[\\\"GCS_Chos_Malal_1914\\\",DATUM[\\\"D_Chos_Malal_1914\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_Campo_Inchauspe\\\",DATUM[\\\"D_Campo_Inchauspe\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",160.0],PARAMETER[\\\"Y_Axis_Translation\\\",26.0],PARAMETER[\\\"Z_Axis_Translation\\\",41.0],OPERATIONACCURACY[10.0],AUTHORITY[\\\"EPSG\\\",1528]]\"},{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1527\"},\"name\":\"Campo_Inchauspe_To_WGS_1984_2\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Campo_Inchauspe_To_WGS_1984_2\\\",GEOGCS[\\\"GCS_Campo_Inchauspe\\\",DATUM[\\\"D_Campo_Inchauspe\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-154.5],PARAMETER[\\\"Y_Axis_Translation\\\",150.7],PARAMETER[\\\"Z_Axis_Translation\\\",100.4],OPERATIONACCURACY[0.5],AUTHORITY[\\\"EPSG\\\",1527]]\"}],\"name\":\"Chos Malal 1914 to WGS 84 (1)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4160\"},\"name\":\"GCS_Chos_Malal_1914\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_Chos_Malal_1914\\\",DATUM[\\\"D_Chos_Malal_1914\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4160]]\"},\"name\":\"Chos Malal 1914 to WGS 84 (1)\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
                "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"GCS_WGS_1984\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\"}",
                DELTA_A,
                new double[]{-69.0, -38.0, -69.0, -38.0},
                new double[]{0, 0},
                new double[]{-69.00077939, -38.00089782, -69.00077939, -38.00089782},
                new double[]{0, 0}
        };
        /*
            Test 2 (run this from Amersfoort to ED50 and then also from ED50 to Amersfoort to ensure it works both ways):

            Amersfoort lon=x= +5.0; lat=y= +53.0
            ED50 lon=x= +5.00094486; lat=y= +52.99967068
        */
        Object[] test2 = {
                "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4289\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4837\"},\"cts\":[{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1672\"},\"name\":\"Amersfoort_To_WGS_1984_2\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Amersfoort_To_WGS_1984_2\\\",GEOGCS[\\\"GCS_Amersfoort\\\",DATUM[\\\"D_Amersfoort\\\",SPHEROID[\\\"Bessel_1841\\\",6377397.155,299.1528128]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Coordinate_Frame\\\"],PARAMETER[\\\"X_Axis_Translation\\\",565.04],PARAMETER[\\\"Y_Axis_Translation\\\",49.91],PARAMETER[\\\"Z_Axis_Translation\\\",465.84],PARAMETER[\\\"X_Axis_Rotation\\\",0.4093943874392368],PARAMETER[\\\"Y_Axis_Rotation\\\",-0.3597051956143113],PARAMETER[\\\"Z_Axis_Rotation\\\",1.868491000350572],PARAMETER[\\\"Scale_Difference\\\",4.0772],OPERATIONACCURACY[1.1],AUTHORITY[\\\"EPSG\\\",1672]]\"},{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1311\"},\"name\":\"ED_1950_To_WGS_1984_18\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"ED_1950_To_WGS_1984_18\\\",GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-89.5],PARAMETER[\\\"Y_Axis_Translation\\\",-93.8],PARAMETER[\\\"Z_Axis_Translation\\\",-123.1],PARAMETER[\\\"X_Axis_Rotation\\\",0.0],PARAMETER[\\\"Y_Axis_Rotation\\\",0.0],PARAMETER[\\\"Z_Axis_Rotation\\\",-0.156],PARAMETER[\\\"Scale_Difference\\\",1.2],OPERATIONACCURACY[1.0],AUTHORITY[\\\"EPSG\\\",1311]]\"}],\"name\":\"Amersfoort to ED50 (1)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4289\"},\"name\":\"GCS_Amersfoort\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_Amersfoort\\\",DATUM[\\\"D_Amersfoort\\\",SPHEROID[\\\"Bessel_1841\\\",6377397.155,299.1528128]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4289]]\"},\"name\":\"Amersfoort to ED50 (1)\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
                "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"GCS_WGS_1984\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\"}",
                DELTA_A,
                new double[]{5.0, 53.0, 5.0, 53.0},
                new double[]{0, 0},
                new double[]{5.00094486, 52.99967068, 5.00094486, 52.99967068},
                new double[]{0, 0}
        };
        /*
            Test 3: CT EPSG::8571 Accra to WGS 84 (2) (epsg.org)

            Accra lon=x= +1.0; lat=y= +2.0
            WGS 84 lon=x= +1.00040835; lat=y= +2.00289032
        */
        Object[] test3 = {
                "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4168\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8571\"},\"cts\":[{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1570\"},\"name\":\"Accra_To_WGS_1972_BE\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Accra_To_WGS_1972_BE\\\",GEOGCS[\\\"GCS_Accra\\\",DATUM[\\\"D_Accra\\\",SPHEROID[\\\"War_Office\\\",6378300.0,296.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1972_BE\\\",DATUM[\\\"D_WGS_1972_BE\\\",SPHEROID[\\\"WGS_1972\\\",6378135.0,298.26]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-171.16],PARAMETER[\\\"Y_Axis_Translation\\\",17.29],PARAMETER[\\\"Z_Axis_Translation\\\",323.31],OPERATIONACCURACY[25.0],AUTHORITY[\\\"EPSG\\\",1570]]\"},{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1240\"},\"name\":\"WGS_1972_BE_To_WGS_1984_1\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"WGS_1972_BE_To_WGS_1984_1\\\",GEOGCS[\\\"GCS_WGS_1972_BE\\\",DATUM[\\\"D_WGS_1972_BE\\\",SPHEROID[\\\"WGS_1972\\\",6378135.0,298.26]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",1.9],PARAMETER[\\\"X_Axis_Rotation\\\",0.0],PARAMETER[\\\"Y_Axis_Rotation\\\",0.0],PARAMETER[\\\"Z_Axis_Rotation\\\",0.814],PARAMETER[\\\"Scale_Difference\\\",-0.38],OPERATIONACCURACY[2.0],AUTHORITY[\\\"EPSG\\\",1240]]\"}],\"name\":\"Accra to WGS 84 (2)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4168\"},\"name\":\"GCS_Accra\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_Accra\\\",DATUM[\\\"D_Accra\\\",SPHEROID[\\\"War_Office\\\",6378300.0,296.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4168]]\"},\"name\":\"Accra to WGS 84 (2)\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
                "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}",
                DELTA_A,
                new double[]{1.0, 2.0, 1.0, 2.0},
                new double[]{0, 0},
                new double[]{1.00040835, 2.00289032, 1.00040835, 2.00289032},
                new double[]{0, 0}
        };
        /*
            Test 4: CT EPSG::8633 Yoff to WGS 84 (1) (epsg.org)

            Yoff lon=x= +15.0; lat=y= -15.0

            WGS 84 lon= +15.00165293; lat=y= -14.99763248
        */
        Object[] test4 = {
                "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4310\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8633\"},\"cts\":[{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1828\"},\"name\":\"Yoff_To_WGS_1972_1\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Yoff_To_WGS_1972_1\\\",GEOGCS[\\\"GCS_Yoff\\\",DATUM[\\\"D_Yoff\\\",SPHEROID[\\\"Clarke_1880_IGN\\\",6378249.2,293.4660212936265]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1972\\\",DATUM[\\\"D_WGS_1972\\\",SPHEROID[\\\"WGS_1972\\\",6378135.0,298.26]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-37.0],PARAMETER[\\\"Y_Axis_Translation\\\",157.0],PARAMETER[\\\"Z_Axis_Translation\\\",85.0],OPERATIONACCURACY[25.0],AUTHORITY[\\\"EPSG\\\",1828]]\"},{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1238\"},\"name\":\"WGS_1972_To_WGS_1984_2\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"WGS_1972_To_WGS_1984_2\\\",GEOGCS[\\\"GCS_WGS_1972\\\",DATUM[\\\"D_WGS_1972\\\",SPHEROID[\\\"WGS_1972\\\",6378135.0,298.26]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",4.5],PARAMETER[\\\"X_Axis_Rotation\\\",0.0],PARAMETER[\\\"Y_Axis_Rotation\\\",0.0],PARAMETER[\\\"Z_Axis_Rotation\\\",0.554],PARAMETER[\\\"Scale_Difference\\\",0.219],OPERATIONACCURACY[2.0],AUTHORITY[\\\"EPSG\\\",1238]]\"}],\"name\":\"Yoff to WGS 84 (1)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4310\"},\"name\":\"GCS_Yoff\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_Yoff\\\",DATUM[\\\"D_Yoff\\\",SPHEROID[\\\"Clarke_1880_IGN\\\",6378249.2,293.4660212936265]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4310]]\"},\"name\":\"Yoff to WGS 84 (1)\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
                "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}",
                DELTA_A,
                new double[]{15.0, -15.0, 15.0, -15.0},
                new double[]{0, 0},
                new double[]{15.00165293, -14.99763248, 15.00165293, -14.99763248},
                new double[]{0, 0}
        };
        /*
            TEST 5 CT EPSG::8174 Bogota to WGS 84 (1) (epsg.org)

            Bogota (Bogota) lon=x= +3.07742010; lat=y= -0.00287589

            WGS 84 lon= -71.0; lat=y=  +0.0
        */
        Object[] test5 = {
                "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4802\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8174\"},\"cts\":[{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1755\"},\"name\":\"Bogota_Bogota_To_Bogota\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Bogota_Bogota_To_Bogota\\\",GEOGCS[\\\"GCS_Bogota_Bogota\\\",DATUM[\\\"D_Bogota\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Bogota\\\",-74.08091666666667],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_Bogota\\\",DATUM[\\\"D_Bogota\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Longitude_Rotation\\\"],OPERATIONACCURACY[0.0],AUTHORITY[\\\"EPSG\\\",1755]]\"},{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1125\"},\"name\":\"Bogota_To_WGS_1984\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Bogota_To_WGS_1984\\\",GEOGCS[\\\"GCS_Bogota\\\",DATUM[\\\"D_Bogota\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",307.0],PARAMETER[\\\"Y_Axis_Translation\\\",304.0],PARAMETER[\\\"Z_Axis_Translation\\\",-318.0],OPERATIONACCURACY[10.0],AUTHORITY[\\\"EPSG\\\",1125]]\"}],\"name\":\"Bogota 1975 (Bogota) to WGS 84 (1)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4802\"},\"name\":\"GCS_Bogota_Bogota\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_Bogota_Bogota\\\",DATUM[\\\"D_Bogota\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Bogota\\\",-74.08091666666667],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4802]]\"},\"name\":\"Bogota 1975 (Bogota) to WGS 84 (1)\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
                "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"WGS 84\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"Degree\\\",0.017453292519943295]]\"}",
                DELTA_A,
                new double[]{3.07742010, -0.00287589, 3.07742010, -0.00287589},
                new double[]{0, 0},
                new double[]{-71.0, 0.0, -71.0, 0.0},
                new double[]{0, 0}
        };
        return Arrays.asList(test1, test2, test3, test4, test5);
    }

    @Test
    public void testToWgs84Transform() {
        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, nonWgs84XyCoordinates, nonWgs84ZCoordinates);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getSuccessCount()).isEqualTo(wgs84XYCoordinates.length / 2);

        for (int i = 0; i < wgs84XYCoordinates.length; i++) {

            softly.assertThat(nonWgs84XyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(wgs84XYCoordinates[i], offset(delta));

            softly.assertThat(nonWgs84ZCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(wgs84ZCoordinates[i % 2], offset(delta));
        }
        softly.assertAll();
    }


    @Test
    public void testReversedWgs84Transform() {
        CRSConverter converter = new CRSConverter();

        ConvertPointsResponse result = converter.convertPoint(toCRS, fromCRS, wgs84XYCoordinates, wgs84ZCoordinates);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getSuccessCount()).isEqualTo(nonWgs84XyCoordinates.length / 2);

        for (int i = 0; i < nonWgs84XyCoordinates.length; i++) {

            softly.assertThat(nonWgs84XyCoordinates[i])
                    .as("XY Coordinate at index " + i)
                    .isCloseTo(wgs84XYCoordinates[i], offset(delta));

            softly.assertThat(nonWgs84ZCoordinates[i % 2])
                    .as("Z Coordinate at index " + (i % 2))
                    .isCloseTo(wgs84ZCoordinates[i % 2], offset(delta));
        }
        softly.assertAll();

    }

}
