package reactive.demo;

import reactive.demo.pojos.vectors.LiveEvent;
import com.github.lhotari.reactive.pulsar.adapter.EndOfStreamAction;
import com.github.lhotari.reactive.pulsar.adapter.ReactiveMessageReader;
import com.github.lhotari.reactive.pulsar.adapter.ReactivePulsarClient;
import com.github.lhotari.reactive.pulsar.adapter.StartAtSpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.schema.KeyValue;
import org.apache.pulsar.common.schema.KeyValueEncodingType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AltitudeService {

    private static final String LIVE_EVENTS_TOPIC =
            "opensky/astracdc/data-c6473ad6-65e1-44e8-b8cc-8ca336310d71-vectors.live";
    private static final double ALTITUDE_THRESHOLD = 1000.;

    private final ReactiveMessageReader<KeyValue<LiveEvent.Key, LiveEvent.Value>> reactiveMessageReader;

    public AltitudeService(ReactivePulsarClient reactivePulsarClient) {
        reactiveMessageReader = reactivePulsarClient
                .messageReader(
                        Schema.KeyValue(
                                Schema.AVRO(LiveEvent.Key.class),
                                Schema.AVRO(LiveEvent.Value.class),
                                KeyValueEncodingType.SEPARATED)
                )
                .topic(LIVE_EVENTS_TOPIC)
                .startAtSpec(StartAtSpec.ofEarliest())
                .endOfStreamAction(EndOfStreamAction.POLL)
                .build();

        reactiveMessageReader.readMessages().subscribe(message -> {
            var altitude = message.getValue().getValue().getAltitude();
            if (altitude != null && altitude < ALTITUDE_THRESHOLD) {
                log.info("Soon you gonna hear {}", message.getValue().getKey().getCallsign());
            }
        });
    }

}
