package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class BinGridJsonMultiPoint extends BinGridJsonBase {
    @JsonProperty("coordinates")
    private BinGridJsonCoordinates coordinates;

    public BinGridJsonMultiPoint() {
        super("AnyCrsMultiPoint");
    }
    
}
