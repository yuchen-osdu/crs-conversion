package org.opengroup.osdu.crs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.opengroup.osdu.crs.util.StrictBooleanDeserializer;
import org.opengroup.osdu.crs.util.StrictStringDeserializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.opengroup.osdu.crs.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = Constants.SWAGGER_TRJ_REQ_DESCRIPTION)
public class ConvertTrajectoryRequest {
	public ConvertTrajectoryRequest(){
		interpolate = true;
		inputKind = TrajectoryInputKind.MD_INCL_AZIM.toString();
	}
    @JsonProperty("trajectoryCRS")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	@NotEmpty
	@Schema(format = "osdu-crs-reference", description = Constants.SWAGGER_TRJ_REQ_CRS, type = "string",example = Constants.SWAGGER_TARGET_CRS_EX)
	@Parameter(required = true)
	private String trajectoryCRS;

    @JsonProperty("azimuthReference")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	@NotEmpty
	@Schema(allowableValues = {"TRUE_NORTH", "TN", "GRID_NORTH", "GN"}, description = Constants.SWAGGER_TRJ_REQ_AZIMUTH_REF, type = "string",example = Constants.SWAGGER_TRJ_REQ_AZIMUTH_REF_EXAMPLE)
	@Parameter(required = true)
	private String azimuthReference;

    @JsonProperty("unitXY")
    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = StrictStringDeserializer.class)
	@Schema(format = "osdu-unit-reference", description = Constants.SWAGGER_TRJ_REQ_UNIT_XY, type = "string",example = Constants.SWAGGER_TRJ_REQ_UNIT_XY_EXAMPLE)
	private String unitXY;

    @JsonProperty("unitZ")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	@NotEmpty
	@Schema(format = "osdu-unit-reference", description = Constants.SWAGGER_TRJ_REQ_UNIT_Z, type = "string",example = Constants.SWAGGER_TRJ_REQ_UNIT_Z_EXAMPLE)
	@Parameter(required = true)
	private String unitZ;

    @JsonProperty("referencePoint")
    @Valid
    @NotNull
	@Schema(description = Constants.SWAGGER_TRF_REQ_REF_POINT)
	@Parameter(required = true)
	private Point referencePoint;

    @JsonProperty("inputStations")
	@Valid
	@NotEmpty
	@ArraySchema(minItems = 1, schema = @Schema(implementation = TrajectoryStationIn.class),
			arraySchema = @Schema(description = Constants.SWAGGER_TRJ_REQ_LIST_OF_INPUT_STATIONS,
					example = Constants.SWAGGER_TRJ_REQ_LIST_OF_INPUT_STATIONS_EX))
	@Parameter(required = true)
	private List<@NotNull TrajectoryStationIn> inputStations;

    @JsonProperty("method")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	@NotEmpty
	@Schema(allowableValues = {"AzimuthalEquidistant", "LMP", "LeesModifiedProposal", "GNL"}, description = Constants.SWAGGER_TRJ_REQ_METHOD, type = "string",example = Constants.SWAGGER_TRJ_REQ_METHOD_EXAMPLE)
	@Parameter(required = true)
	private String method;

	@JsonProperty("inputKind")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	@Schema(allowableValues = {"MD_Incl_Azim", "MD_Inclination_Azimuth"}, description = Constants.SWAGGER_TRJ_REQ_INPUT_KIND, type = "string",example = Constants.SWAGGER_TRJ_REQ_INPUT_KIND_EXAMPLE)
	public String inputKind;

    @JsonProperty("interpolate")
    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = StrictBooleanDeserializer.class)
	@Schema(description = Constants.SWAGGER_TRJ_REQ_INTERPOLATE, type = "boolean", types = {"boolean"},example = Constants.SWAGGER_TRJ_REQ_INTERPOLATE_EX)
	public boolean interpolate;

    @Override
    public String toString() {
        String result = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            result = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
        	return result;
        }
        return result;
    }
    public static ConvertTrajectoryRequest createInstance(String json) {
        ConvertTrajectoryRequest result = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            JsonNode node = mapper.readTree(json);
            result = mapper.treeToValue(node, ConvertTrajectoryRequest.class);

        } catch (IOException e) {
        	return result;
        }
        return result;
    }
}
