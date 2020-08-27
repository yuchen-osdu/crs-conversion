package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.util.Constants;
import javax.validation.constraints.NotNull;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@ApiModel(description = Constants.SWAGGER_POINT_DESCR)
public class Point {
    public Point() {
        setNaN(this);
    }
    @NotNull
    @ApiModelProperty(value = Constants.SWAGGER_X_COORDINATE, required = true, dataType = "Double", example = Constants.SWAGGER_X_COORDINATE_EXAMPLE)
    private Double x;

    @NotNull
    @ApiModelProperty(value = Constants.SWAGGER_Y_COORDINATE, required = true, dataType = "Double", example = Constants.SWAGGER_Y_COORDINATE_EXAMPLE)
    private Double y;

    @NotNull
    @ApiModelProperty(value = Constants.SWAGGER_Z_COORDINATE, required = true, dataType = "Double", example = Constants.SWAGGER_Z_COORDINATE_EXAMPLE)
    private Double z;

    public static boolean isValid(Point point) {
        if (point == null) return false;
        // if (point.x == null || point.y ==null || point.z == null) return false; // values cannot be null due to lombok constraints
        return !(Double.isNaN(point.x) || Double.isNaN(point.y) || Double.isNaN(point.z));
    }

    public static void setNaN(Point p) {
        p.x = Double.NaN;
        p.y = Double.NaN;
        p.z = Double.NaN;
    }
}
