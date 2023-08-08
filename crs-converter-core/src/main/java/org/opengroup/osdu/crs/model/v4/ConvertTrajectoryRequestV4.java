package org.opengroup.osdu.crs.model.v4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.opengroup.osdu.crs.model.ConvertTrajectoryRequest;
import org.opengroup.osdu.crs.model.TrajectoryInputKind;
import org.opengroup.osdu.crs.util.Constants;

import java.io.IOException;

@Data
@AllArgsConstructor
@Schema(description = Constants.SWAGGER_TRJ_REQ_DESCRIPTION)
public class ConvertTrajectoryRequestV4 extends ConvertTrajectoryRequest {
	public ConvertTrajectoryRequestV4(){
		super.interpolate = true;
		super.inputKind = TrajectoryInputKind.MD_INCL_AZIM.toString();
	}

	@JsonProperty("MD_i")
    @Schema(description = Constants.SWAGGER_TRJ_MD_I, example = Constants.SWAGGER_TRJ_REQ_MD_I_EX)
    private MinimumDepthInterval MD_i;

    public static ConvertTrajectoryRequestV4 createInstance(String json) {
        ConvertTrajectoryRequestV4 result = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            JsonNode node = mapper.readTree(json);
            result = mapper.treeToValue(node, ConvertTrajectoryRequestV4.class);

        } catch (IOException e) {
        	return result;
        }
        return result;
    }
}
