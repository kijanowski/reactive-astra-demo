package reactive.demo.pojos.vectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LiveEvent {
    LiveEvent.Key key;
    LiveEvent.Value value;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Key {
        String icao24;
        String callsign;
        Long time_position;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Value {
        Float altitude;
        Float longitude;
        Float latitude;
    }
}