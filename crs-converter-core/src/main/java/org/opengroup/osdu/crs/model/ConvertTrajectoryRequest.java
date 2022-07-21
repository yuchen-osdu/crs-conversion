package org.opengroup.osdu.crs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengroup.osdu.crs.util.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Data
@AllArgsConstructor
@ApiModel(description = Constants.SWAGGER_TRJ_REQ_DESCRIPTION)
public class ConvertTrajectoryRequest {
	public ConvertTrajectoryRequest(){
		interpolate = true;
		inputKind = TrajectoryInputKind.MD_INCL_AZIM.toString();
	}
    @JsonProperty("trajectoryCRS")
	@NotEmpty
	@ApiModelProperty(
			value = Constants.SWAGGER_TRJ_REQ_CRS, required = true, dataType = "String",
			example = Constants.SWAGGER_TARGET_CRS_EX)
	private String trajectoryCRS;

    @JsonProperty("azimuthReference")
	@NotEmpty
	@ApiModelProperty(
			value = Constants.SWAGGER_TRJ_REQ_AZIMUTH_REF, required = true, dataType = "String",
			example = Constants.SWAGGER_TRJ_REQ_AZIMUTH_REF_EXAMPLE)
	private String azimuthReference;

	@NotEmpty
    @JsonProperty("unitXY")
	@ApiModelProperty(
			value = Constants.SWAGGER_TRJ_REQ_UNIT_XY, dataType = "String",
			example = Constants.SWAGGER_TRJ_REQ_UNIT_XY_EXAMPLE)
	private String unitXY;

    @JsonProperty("unitZ")
	@NotEmpty
	@ApiModelProperty(
			value = Constants.SWAGGER_TRJ_REQ_UNIT_Z, required = true, dataType = "String",
			example = Constants.SWAGGER_TRJ_REQ_UNIT_Z_EXAMPLE)
	private String unitZ;

    @JsonProperty("referencePoint")
	@ApiModelProperty(value = Constants.SWAGGER_TRF_REQ_REF_POINT, required = true)
	private Point referencePoint;

    @JsonProperty("inputStations")
	@Valid
	@NotEmpty
	@ApiModelProperty(value = Constants.SWAGGER_TRJ_REQ_LIST_OF_INPUT_STATIONS,
			example = Constants.SWAGGER_TRJ_REQ_LIST_OF_INPUT_STATIONS_EX, required = true)
	private List<TrajectoryStationIn> inputStations;

    @JsonProperty("method")
	@NotEmpty
	@ApiModelProperty(
			value = Constants.SWAGGER_TRJ_REQ_METHOD, required = true, dataType = "String",
			example = Constants.SWAGGER_TRJ_REQ_METHOD_EXAMPLE)
	private String method;

	@JsonProperty("inputKind")
	@ApiModelProperty(
			value = Constants.SWAGGER_TRJ_REQ_INPUT_KIND, dataType = "String",
			example = Constants.SWAGGER_TRJ_REQ_INPUT_KIND_EXAMPLE)
	private String inputKind;

    @JsonProperty("interpolate")
	@ApiModelProperty(value=Constants.SWAGGER_TRJ_REQ_INTERPOLATE,
			example = Constants.SWAGGER_TRJ_REQ_INTERPOLATE_EX, dataType = "Boolean")
	private boolean interpolate;

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
