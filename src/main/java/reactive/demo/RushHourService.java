package reactive.demo;

import com.github.lhotari.reactive.pulsar.adapter.EndOfStreamAction;
import com.github.lhotari.reactive.pulsar.adapter.ReactiveMessageReader;
import com.github.lhotari.reactive.pulsar.adapter.ReactivePulsarClient;
import com.github.lhotari.reactive.pulsar.adapter.StartAtSpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.schema.KeyValue;
import org.apache.pulsar.common.schema.KeyValueEncodingType;
import org.springframework.stereotype.Service;
import reactive.demo.pojos.vectors.LiveKey;
import reactive.demo.pojos.vectors.LiveValue;

import java.time.Duration;
import java.util.HashSet;

@Service
@Slf4j
public class RushHourService {

    private static final String LIVE_EVENTS_TOPIC =
            "opensky/astracdc/data-c6473ad6-65e1-44e8-b8cc-8ca336310d71-vectors.live";
    private static final int UNIQUE_FLIGHTS_THRESHOLD = 5;
    private static final int UNIQUE_FLIGHTS_WINDOW_IN_SECONDS = 300;

    private final ReactiveMessageReader<KeyValue<LiveKey, LiveValue>> reactiveMessageReader;

    public RushHourService(ReactivePulsarClient reactivePulsarClient) {
        reactiveMessageReader = reactivePulsarClient
                .messageReader(
                        Schema.KeyValue(
                                Schema.AVRO(LiveKey.class),
                                Schema.AVRO(LiveValue.class),
                                KeyValueEncodingType.SEPARATED)
                )
                .topic(LIVE_EVENTS_TOPIC)
                .startAtSpec(StartAtSpec.ofEarliest())
                .endOfStreamAction(EndOfStreamAction.POLL)
                .build();

        reactiveMessageReader.readMessages()
                .map(this::toCallsign)
                .buffer(Duration.ofSeconds(UNIQUE_FLIGHTS_WINDOW_IN_SECONDS))
                .map(HashSet::new)
                .subscribe(uniqueFlights -> {
                    if (uniqueFlights.size() > UNIQUE_FLIGHTS_THRESHOLD) {
                        log.info(
                                "Rush Hour! Collected more than {} unique flights: {}",
                                UNIQUE_FLIGHTS_THRESHOLD,
                                uniqueFlights
                        );
                    }
                });
    }

    private String toCallsign(Message<KeyValue<LiveKey, LiveValue>> message) {
        return message.getValue().getKey().getCallsign();
    }

}
