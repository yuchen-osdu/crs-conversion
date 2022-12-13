package org.opengroup.osdu.crs.util;

public final class Constants {
    public static final String WGS84 = "{\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\",\"ver\":\"PE_10_3_1\",\"name\":\"GCS_WGS_1984\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"type\":\"LBC\"}";

    public static final String BASE_PATH = "/";
    public static final String SWAGGER_TAG_CRS_CONVERSION = "CRS Point Conversion";
    public static final String SWAGGER_TAG_TRJ_CONVERSION = "Trajectory Computation and Conversion";
    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final String GCLOUD_PROJECT = "GCLOUD_PROJECT";
    public static final String SWAGGER_TARGET_CRS_GEO_EXAMPLE = "\"{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"GCS_WGS_1984\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\"}\"";
    public static final String EMPTY_OBJECT_EXAMPLE = "{}";

    // Exception messages
    static final String ERROR_MSG_FIELD = "error";
    public static final String ERROR_MSG_NO_SUITABLE_CONVERSION = "Could not find a conversion method for the given input.";
    public static final String ERROR_MSG_INVALID_INPUT_CRS_SPECIFICATION = "Invalid source and/or target CRS specification.";
    public static final String ERROR_MSG_INVALID_INPUT_NO_PROJECTED_CRS = "Invalid CRS specification; only projected CRSs are supported.";
    public static final String ERROR_MSG_INCOHERENT_BOUND_TRFS = "Incoherent coordinate transformations; no hub CRS could be identified.";
    public static final String ERROR_MSG_INPUT_ARRAY_MISMATCH = "Internal error; inconsistent array lengths";
    public static final String ERROR_MSG_BAD_INPUT = "Bad input";
    public static final String ERROR_MSG_JSON_PARSE = "Error parsing spatial model; details: ";
    public static final String ERROR_MSG_SPATIAL_ENGINE_ERROR = "Spatial engine error; details: ";
    public static final String ERROR_MSG_UNIT_ERROR = "Unsupported or inconsistent unit.";

    // Swagger
    public static final String JSON_TYPE = "application/json";
    public static final String SWAGGER_TITLE = "CRS Conversion Service";
    public static final String SWAGGER_DESCRIPTION = "Provides conversion/transformation services from a source to a target CRS for points and trajectories. A coordinate operation is a conversion if the source and target CRS share the same datum; the operation is called transformation if datum transformations are involved, i.e. source and target CRS do not share the same datum.";
    public static final String SWAGGER_VERSION = "1.0.0";
    public static final String SWAGGER_BASE_PATH = "/api/crs/v1";
    public static final String SWAGGER_RESOURCE_PACKAGE = "org.opengroup.osdu.crs.api";

    // Swagger - Convert API
    public static final String SWAGGER_CONVERT_TITLE = "Convert a list of points";
    public static final String SWAGGER_CONVERT_NOTES = "Convert a list of points";
    public static final String SWAGGER_CONVERT_SUCCESS_RESPONSE = "Conversion performed successfully";
    public static final String SWAGGER_CONVERT_BAD_INPUT_BASE_PATH = "Bad input format";
    public static final String SWAGGER_CONVERT_OVERLOAD = "CRS-converter overloaded; try again later";
    public static final String SWAGGER_CONVERT_UNKNOWN_ERROR = "Unknown error.";

    public static final String SWAGGER_SOURCE_CRS = "Source CRS as persistable reference string";
    public static final String SWAGGER_SOURCE_CRS_EXAMPLE = "\"{\"lateBoundCRS\":{\"wkt\":\"GEOGCS[\\\"GCS_Provisional_S_American_1956\\\",DATUM[\\\"D_Provisional_S_American_1956\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4248]]\",\"ver\":\"PE_10_3_1\",\"name\":\"GCS_Provisional_S_American_1956\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4248\"},\"type\":\"LBC\"},\"singleCT\":{\"wkt\":\"GEOGTRAN[\\\"PSAD_1956_To_WGS_1984_9\\\",GEOGCS[\\\"GCS_Provisional_S_American_1956\\\",DATUM[\\\"D_Provisional_S_American_1956\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-295.0],PARAMETER[\\\"Y_Axis_Translation\\\",173.0],PARAMETER[\\\"Z_Axis_Translation\\\",-371.0],AUTHORITY[\\\"EPSG\\\",1209]]\",\"ver\":\"PE_10_3_1\",\"name\":\"PSAD_1956_To_WGS_1984_9\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1209\"},\"type\":\"ST\"},\"ver\":\"PE_10_3_1\",\"name\":\"PSAD56 * DMA-Ven [4248,1209]\",\"authCode\":{\"auth\":\"SLB\",\"code\":\"4248009\"},\"type\":\"EBC\"}\"";
    public static final String SWAGGER_TARGET_CRS = "Target CRS as persistable reference string";
    public static final String SWAGGER_CONVERT_TARGET_CRS_EXAMPLE = "\"{\"lateBoundCRS\":{\"wkt\":\"PROJCS[\\\"Trinidad_1903_Trinidad_Grid\\\",GEOGCS[\\\"GCS_Trinidad_1903\\\",DATUM[\\\"D_Trinidad_1903\\\",SPHEROID[\\\"Clarke_1858\\\",6378293.64520876,294.260676369]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Cassini\\\"],PARAMETER[\\\"False_Easting\\\",430000.0],PARAMETER[\\\"False_Northing\\\",325000.0],PARAMETER[\\\"Central_Meridian\\\",-61.3333333333333],PARAMETER[\\\"Scale_Factor\\\",1.0],PARAMETER[\\\"Latitude_Of_Origin\\\",10.4416666666667],UNIT[\\\"Link_Clarke\\\",0.201166195164],AUTHORITY[\\\"EPSG\\\",30200]]\",\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad_1903_Trinidad_Grid\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"30200\"},\"type\":\"LBC\"},\"singleCT\":{\"wkt\":\"GEOGTRAN[\\\"Trinidad_1903_To_WGS_1984_2\\\",GEOGCS[\\\"GCS_Trinidad_1903\\\",DATUM[\\\"D_Trinidad_1903\\\",SPHEROID[\\\"Clarke_1858\\\",6378293.64520876,294.260676369]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-61.0],PARAMETER[\\\"Y_Axis_Translation\\\",285.2],PARAMETER[\\\"Z_Axis_Translation\\\",471.6],AUTHORITY[\\\"EPSG\\\",10085]]\",\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad_1903_To_WGS_1984_2\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"10085\"},\"type\":\"ST\"},\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad 1903 * EOG-Tto Trin / Trinidad Grid [30200,10085]\",\"authCode\":{\"auth\":\"SLB\",\"code\":\"30200002\"},\"type\":\"EBC\"}\"";
    public static final String SWAGGER_TARGET_CRS_EX = "\"{\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_31N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",3.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32631]]\",\"ver\":\"PE_10_3_1\",\"name\":\"WGS_1984_UTM_Zone_31N\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32631\"},\"type\":\"LBC\"}\"";
    public static final String SWAGGER_TARGET_CRS_EXAMPLE = "{\"authCode\":{\"auth\":\"SLB\",\"code\":\"30200002\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"30200\"},\"name\":\"Trinidad_1903_Trinidad_Grid\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"PROJCS[\\\"Trinidad_1903_Trinidad_Grid\\\",GEOGCS[\\\"GCS_Trinidad_1903\\\",DATUM[\\\"D_Trinidad_1903\\\",SPHEROID[\\\"Clarke_1858\\\",6378293.64520876,294.260676369]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Cassini\\\"],PARAMETER[\\\"False_Easting\\\",430000.0],PARAMETER[\\\"False_Northing\\\",325000.0],PARAMETER[\\\"Central_Meridian\\\",-61.3333333333333],PARAMETER[\\\"Scale_Factor\\\",1.0],PARAMETER[\\\"Latitude_Of_Origin\\\",10.4416666666667],UNIT[\\\"Link_Clarke\\\",0.201166195164],AUTHORITY[\\\"EPSG\\\",30200]]\"},\"name\":\"Trinidad 1903 * EOG-Tto Trin / Trinidad Grid [30200,10085]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"10085\"},\"name\":\"Trinidad_1903_To_WGS_1984_2\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"Trinidad_1903_To_WGS_1984_2\\\",GEOGCS[\\\"GCS_Trinidad_1903\\\",DATUM[\\\"D_Trinidad_1903\\\",SPHEROID[\\\"Clarke_1858\\\",6378293.64520876,294.260676369]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-61.0],PARAMETER[\\\"Y_Axis_Translation\\\",285.2],PARAMETER[\\\"Z_Axis_Translation\\\",471.6],AUTHORITY[\\\"EPSG\\\",10085]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
    public static final String SWAGGER_NOF_POINTS_CONVERTED = "Number of points successfully converted. If the number is less than the request array length conversion/transformation failures occurred.";
    public static final String SWAGGER_CONVERTED_POINTS = "Converted points; length and order of the array is the same as in the request. Points, which failed to convert, are returned as NaN.";
    public static final String SWAGGER_CONVERSION_RESPONSE = "Response of a CRS conversion/transformation operation";
    public static final String SWAGGER_CONVERT_AUDIT = "The list of operations performed on the points as a list of strings";
    public static final String SWAGGER_TARGET_Z_UNIT = "Optional: the target Z-unit for the z-axis scaling.";
    public static final String SWAGGER_TARGET_Z_UNIT_EXAMPLE = "\"{\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"scaleOffset\":{\"offset\":0.0,\"scale\":1.0},\"symbol\":\"m\",\"type\":\"USO\"}\"";

    public static final String SWAGGER_GEO_JSON_CONVERT_TITLE = "Convert a GeoJSON or AnyCrsGeoJson structure";
    public static final String SWAGGER_GEO_JSON_CONVERT_NOTES = "Convert a GeoJSON or AnyCrsGeoJson structure";
    public static final String SWAGGER_GEO_JSON_CONVERT_REQUEST_DESCR = "Request to convert a GeoJSON FeatureCollection or AnyCrsFeatureCollection from WGS 84 or 'AnyCrsFeatureCollection.persistableReferenceCrs to a target CRS.' to a target CRS";
    public static final String SWAGGER_GEO_JSON_FEATURE_COLLECTION = "The GeoJSON FeatureCollection or AnyCrsFeatureCollection structure to be converted/transformed. GeoJSON is always based on WGS 84; AnyCrsFeatureCollection carries the CRS context in the persistableReferenceCrs property. GeoJSON WGS 84 or the persistableReferenceCrs are taken as the 'fromCRS'.";
    public static final String SWAGGER_GEO_JSON_FEATURE_COLLECTION_EXAMPLE = "{\"features\":[{\"geometry\":{\"coordinates\":[5.0,59.0,-1000.0],\"type\":\"Point\"},\"properties\":{},\"type\":\"Feature\"}],\"properties\":{},\"type\":\"FeatureCollection\"}";
    public static final String SWAGGER_GEO_JSON_CONVERSION_RESPONSE = "Response of a CRS conversion/transformation operation involving GeoJSON FeatureCollection or AnyCrsFeatureCollection.";
    public static final String SWAGGER_GEO_JSON_SUCCESS_COUNT = "The number of coordinates in the GeoJSON FeatureCollection or AnyCrsFeatureCollection successfully converted/transformed. If this number is less than totalCount then conversion/transformation errors have occurred.";
    public static final String SWAGGER_GEO_JSON_COORDINATE_COUNT = "The total number of coordinates in the GeoJSON FeatureCollection or AnyCrsFeatureCollection.";
    public static final String SWAGGER_GEO_JSON_CONVERTED = "The converted GeoJSON FeatureCollection or AnyCrsFeatureCollection with 'toCRS' context; length and order of the structure is the same as in the request. Points, which failed to convert, are returned as NaN.";

    public static final String SWAGGER_X_COORDINATE = "x coordinate or longitude";
    public static final String SWAGGER_Y_COORDINATE = "y coordinate or latitude";
    public static final String SWAGGER_Z_COORDINATE = "z coordinate";
    public static final String SWAGGER_X_COORDINATE_EXAMPLE = "-61.043406288714543";
    public static final String SWAGGER_Y_COORDINATE_EXAMPLE = "10.673103179456877";
    public static final String SWAGGER_Z_COORDINATE_EXAMPLE = "0.0";
    public static final String SWAGGER_POINT_DESCR = "Point representation for CRS operations";
    public static final String SWAGGER_CONVERT_REQUEST_DESCR = "Request to convert a set of points from a source CRS to a target CRS";
    public static final String SWAGGER_LIST_OF_POINTS_TO_BE_CONVERTED = "List of points to be converted";
    public static final String SWAGGER_LIST_OF_POINTS_TO_BE_CONVERTED_EXAMPLE = "[\n" +
            "    {\n" +
            "      \"x\": -61.04340628871454,\n" +
            "      \"y\": 10.673103179456877,\n" +
            "      \"z\": 0\n" +
            "    }\n" +
            "  ]";

    public static final String SWAGGER_TRJ_CONVERT_TITLE = "Convert trajectory stations";
    public static final String SWAGGER_TRJ_CONVERT_NOTES = "Convert a list of trajectory stations, given the unit and spatial context and a reference point in 3D where MD==0.";
    public static final String SWAGGER_TRJ_CONVERT_SUCCESS_RESPONSE = "Trajectory conversion performed successfully";
    public static final String SWAGGER_TRJ_REQ_DESCRIPTION = "Input trajectory data structure; contains the context (crs, units, azimuth reference, method)";
    public static final String SWAGGER_TRJ_REQ_CRS = "Coordinate reference system for the reference point; typically the CRS is a projected CRS; if a geographic CRS is provided, the unitXY must be defined and the azimuthReference must be TRUE_NORTH.";
    public static final String SWAGGER_TRJ_REQ_AZIMUTH_REF = "azimuth reference for the input trajectory station azimuth values (TRUE_NORTH or GridNorth)";
    public static final String SWAGGER_TRJ_REQ_AZIMUTH_REF_EXAMPLE = "TRUE_NORTH";
    public static final String SWAGGER_TRJ_REQ_LIST_OF_INPUT_STATIONS = "The array of input trajectory stations";
    public static final String SWAGGER_TRJ_REQ_LIST_OF_INPUT_STATIONS_EX = "[\n" +
            "    {\n" +
            "      \"md\": 0,\n" +
            "      \"inclination\": 0,\n" +
            "      \"azimuth\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"md\": 1000,\n" +
            "      \"inclination\": 0,\n" +
            "      \"azimuth\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"md\": 2000,\n" +
            "      \"inclination\": 90,\n" +
            "      \"azimuth\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"md\": 3000,\n" +
            "      \"inclination\": 90,\n" +
            "      \"azimuth\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"md\": 5000,\n" +
            "      \"inclination\": 90,\n" +
            "      \"azimuth\": 90\n" +
            "    },\n" +
            "    {\n" +
            "      \"md\": 6000,\n" +
            "      \"inclination\": 90,\n" +
            "      \"azimuth\": 90\n" +
            "    }\n" +
            "  ]";
    public static final String SWAGGER_TRF_REQ_REF_POINT = "The 3D reference point in the 'trajectoryCRS' where MD==0.";
    public static final String SWAGGER_TRJ_REQ_UNIT_XY = "The horizontal unit of the dx, dy in the input trajectory stations; the unit must be a length unit in 'persistable model' format, see example.";
    public static final String SWAGGER_TRJ_REQ_UNIT_XY_EXAMPLE = "\"{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}\"";
    public static final String SWAGGER_TRJ_REQ_UNIT_Z = "The vertical unit of the dz in the input trajectory stations; the unit must be a length unit in 'persistable model' format, see example.";
    public static final String SWAGGER_TRJ_REQ_UNIT_Z_EXAMPLE = "\"{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}\"";
    public static final String SWAGGER_TRJ_REQ_UNIT_EXAMPLE = "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}";
    public static final String SWAGGER_TRJ_REQ_METHOD = "The computation method - 'AzimuthalEquidistant' (default) or 'LMP' (Lee's modified proposal SPE96813)";
    public static final String SWAGGER_TRJ_REQ_METHOD_EXAMPLE = "AzimuthalEquidistant";
    public static final String SWAGGER_TRJ_REQ_INPUT_KIND = "The kind of input; one of MD_Inclination_Azimuth (default), MD_X_Y_Z, MD_dX_dY_dZ, X_Y_Z, dX_dY_dZ. MD stands for measured depth; MD_X_Y_Z/X_Y_Z stand for absolute coordinates in the reference CRS, MD_dX_dY_dZ/dX_dY_dZ stand for deviations relative to the reference point.";
    public static final String SWAGGER_TRJ_REQ_INPUT_KIND_EXAMPLE = "MD_Inclination_Azimuth";
    public static final String SWAGGER_TRJ_REQ_INTERPOLATE = "Perform trajectory interpolation on demand; default is true.";
    public static final String SWAGGER_TRJ_REQ_INTERPOLATE_EX = "True";

    public static final String SWAGGER_TRJ_STN_IN_DESCRIPTION = "Input trajectory station record; context is provided by the container.";
    public static final String SWAGGER_MD = "MD (measured depth) from vertical reference point in 'unitZ'.";
    public static final String SWAGGER_MD_EXAMPLE = "2563.56";
    public static final String SWAGGER_INC = "Inclination angle in degrees of arc, 0.0 is vertical, 90.0 is horizontal.";
    public static final String SWAGGER_INC_EXAMPLE = "15.0";
    public static final String SWAGGER_AZI = "Azimuth angle in degrees of arc, 0.0/360.0 is North; reference given by azimuthReference (TRUE_NORTH or GridNorth).";
    public static final String SWAGGER_AZI_EXAMPLE = "355.0";
    public static final String SWAGGER_DX = "E-W deviation in the local Cartesian engineering CRS from the well reference point; unit is given by container's 'unitXY' or projected 'trajectoryCRS'.";
    public static final String SWAGGER_DX_EXAMPLE = "55.9";
    public static final String SWAGGER_DY = "N-S deviation in the local Cartesian engineering CRS from the well reference point; Y is aligned with azimuth reference (TRUE_NORTH or projected GridNorth); unit is given by container's 'unitXY' or projected 'trajectoryCRS'.";
    public static final String SWAGGER_DY_EXAMPLE = "-145.3";
    public static final String SWAGGER_DZ = "True vertical deviation in the local Cartesian engineering CRS from the well reference point; unit is given by container's unitZ; downwards positive.";
    public static final String SWAGGER_DZ_EXAMPLE = "1965.6";

    public static final String SWAGGER_TRJ_RSP_DESCRIPTION = "Trajectory response data structure; contains the context (crs, units).";
    public static final String SWAGGER_TRJ_RSP_LIST_OF_STATIONS = "Computed trajectory stations.";
    public static final String SWAGGER_TRJ_RSP_UNIT_XY = "The horizontal unit of the dx, dy in the output trajectory stations.";
    public static final String SWAGGER_TRJ_RSP_UNIT_Z = "The vertical unit of the dz in the output trajectory stations.";
    public static final String SWAGGER_TRJ_RSP_UNIT_DLS = "The unit of the dog leg severity (DLS) in the output trajectory stations.";
    public static final String SWAGGER_TRJ_RSP_UNIT_DLS_EXAMPLE = "%7B%22ScaleOffset%22%3A%7B%22Scale%22%3A5.72614583987641E-4%2C%22Offset%22%3A0.0%7D%2C%22Symbol%22%3A%22deg%2F100ft%22%2C%22BaseMeasurement%22%3A%22%257B%2522Ancestry%2522%253A%2522Rotation_Per_Length%2522%257D%22%7D";
    public static final String SWAGGER_TRJ_RSP_METHOD = "The computation method used - 'AzimuthalEquidistant' (default) or 'LMP' (Lee's modified proposal SPE96813).";
    public static final String SWAGGER_TRJ_RSP_LOCAL_CRS = "Coordinate Reference System for the local, True North oriented, true distance, engineering CRS with origin at the well's surface location.";
    public static final String SWAGGER_TRJ_STN_OUT_DESCRIPTION = "Output trajectory station record; context is provided by the container.";
    public static final String SWAGGER_AZI_TN = "True North azimuth angle in degrees of arc, 0.0/360.0 is North.";
    public static final String SWAGGER_AZI_TN_EXAMPLE = "355.96";
    public static final String SWAGGER_AZI_GN = "Grid North azimuth angle in degrees of arc, 0.0/360.0 is North.";
    public static final String SWAGGER_AZI_GN_EXAMPLE = "355.0";
    public static final String SWAGGER_DX_TN = "True E-W deviation in the local Cartesian engineering CRS from the well reference point; unit is given by container's 'unitXY'.";
    public static final String SWAGGER_DX_TN_EXAMPLE = "55.9";
    public static final String SWAGGER_DY_TN = "True N-S deviation in the local Cartesian engineering CRS from the well reference point; Y is aligned with TRUE_NORTH; unit is given by container's 'unitXY'.";
    public static final String SWAGGER_DY_TN_EXAMPLE = "-145.3";
    public static final String SWAGGER_TRJ_POINT = "Trajectory station point in trajectoryCRS and vertical unit as defined in container's 'unitZ'.";
    public static final String SWAGGER_TRJ_RSP_ORIGINAL = "Original trajectory station if true, interpolated trajectory station if false.";
    public static final String SWAGGER_TRJ_RSP_DLS = "Curvature, Dog Leg Severity, measured in 'unitDls'.";
    public static final String SWAGGER_TRJ_WGS84_LONGITUDE = "WGS 84 longitude in dega";
    public static final String SWAGGER_TRJ_WGS84_LATITUDE = "WGS 84 latitude in dega";
    public static final String SWAGGER_GEO_JSON_FEATURE_EXAMPLES = "[\n" +
            "      {\n" +
            "        \"geometry\": {\n" +
            "          \"coordinates\": [\n" +
            "            313405.9477893702,\n" +
            "            6544797.620047403,\n" +
            "            6.561679790026246\n" +
            "          ],\n" +
            "          \"type\": \"AnyCrsPoint\"\n" +
            "        },\n" +
            "        \"properties\": {},\n" +
            "        \"type\": \"AnyCrsFeature\"\n" +
            "      }\n" +
            "    ]";
    public static final String SWAGGER_GEO_PERSISTABLE_REFERENCE_CRS_EXAMPLE = "\"{\"lateBoundCRS\":{\"wkt\":\"PROJCS[\\\"ED_1950_UTM_Zone_32N\\\",GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",9.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",23032]]\",\"ver\":\"PE_10_3_1\",\"name\":\"ED_1950_UTM_Zone_32N\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"23032\"},\"type\":\"LBC\"},\"singleCT\":{\"wkt\":\"GEOGTRAN[\\\"ED_1950_To_WGS_1984_23\\\",GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-116.641],PARAMETER[\\\"Y_Axis_Translation\\\",-56.931],PARAMETER[\\\"Z_Axis_Translation\\\",-110.559],PARAMETER[\\\"X_Axis_Rotation\\\",0.893],PARAMETER[\\\"Y_Axis_Rotation\\\",0.921],PARAMETER[\\\"Z_Axis_Rotation\\\",-0.917],PARAMETER[\\\"Scale_Difference\\\",-3.52],AUTHORITY[\\\"EPSG\\\",1612]]\",\"ver\":\"PE_10_3_1\",\"name\":\"ED_1950_To_WGS_1984_23\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1612\"},\"type\":\"ST\"},\"ver\":\"PE_10_3_1\",\"name\":\"ED50 * EPSG-Nor N62 2001 / UTM zone 32N [23032,1612]\",\"authCode\":{\"auth\":\"SLB\",\"code\":\"23032023\"},\"type\":\"EBC\"}\"";
    public static final String SWAGGER_GEO_PERSISTABLE_REFERENCE_UNIT_Z = "\"{\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"scaleOffset\":{\"offset\":0.0,\"scale\":0.3048},\"symbol\":\"ft\",\"type\":\"USO\"}\"";
    public static final String SWAGGER_GEO_ANY_CRS_FEATURE_COLLECTION_EXAMPLE = "AnyCrsFeatureCollection";


	public static final String SWAGGER_BIN_GRID_CONVERT_TITLE = "CRS Convert service is an OSDU platform standard method for QC and conversion of Bin Grids, associated in particular with ingested seismic volumes, that describe the “real world” (Easting, Northing) of bin grid centers at (inline, crossline) local coordinates";
	public static final String SWAGGER_BIN_GRID_CONVERT_NOTES = " QC check of the “squareness” of a Bin Grid defined using 4 corner points."
			+ " Coordinate conversion of a Bin Grid to a new CRS and “square it up” (if target CRS is same as original CRS then conversion is omitted, and the squareness test is done in the original CRS)."
			+ " Calculate derived P6 parameters from the input 4 corners."
			+ " Calculate WGS 84 coordinates at the corners"
			+ " Returns converted Bin Grid and a QC of squareness of the bin grid";
	public static final String SWAGGER_BIN_GRID_CONVERT_REQUEST_DESCR = "The input and output of this method use the AbstractBinGrid:1.0.0  definition.  On input a minimum required properties can be given, which are enriched on output as indicated ";
	public static final String SWAGGER_BIN_GRID_CONVERSION_RESPONSE = "The response is essentially a measure of the computed “non-squareness” (dI,dJ) of the input BinGrid, and an output BinGrid which is essentially a copy of the input, but augmented with the derived P6 parameters filled out, and optionally (if a toCrs was given in the request) converted global coordinates that are “squared up” in the new geometry (which can be used in applications that require a square grid in a project CRS geometry; if the “squaring error” is small enough";
	public static final String SWAGGER_BIN_GRID_CONVERTED = " ";




}
