package kz.romanb.onelabproject.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaService {
    private final KafkaTemplate<String, KafkaEvent> kafkaTemplate;

    public void sendMessage(Long userId, String message) {
        KafkaEvent event = new KafkaEvent(userId, message);

        ProducerRecord<String, KafkaEvent> producerRecord = new ProducerRecord<>("onelab-topic", event);

        kafkaTemplate.send(producerRecord);
    }
}
