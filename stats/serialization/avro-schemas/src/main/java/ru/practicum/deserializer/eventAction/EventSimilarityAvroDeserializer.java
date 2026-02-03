package ru.practicum.deserializer.eventAction;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public class EventSimilarityAvroDeserializer implements Deserializer<EventSimilarityAvro> {
    private final DecoderFactory decoderFactory;
    private final DatumReader<EventSimilarityAvro> reader;

    public EventSimilarityAvroDeserializer() {
        this.decoderFactory = DecoderFactory.get();
        this.reader = new SpecificDatumReader<>(EventSimilarityAvro.class);
    }


    @Override
    public EventSimilarityAvro deserialize(String topic, byte[] data) {
        try {
            if (data != null) {
                BinaryDecoder decoder = decoderFactory.binaryDecoder(data, null);
                return this.reader.read(null, decoder);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка десериализации данных из топика [" + topic + "]", e);
        }
    }
}
