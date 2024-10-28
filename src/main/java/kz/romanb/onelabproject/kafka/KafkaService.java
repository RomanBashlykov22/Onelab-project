package kz.romanb.onelabproject.kafka;

import kz.romanb.onelabproject.entities.User;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaService {
    private final KafkaTemplate<String, KafkaEvent> kafkaTemplate;

    public void sendMessage(User user, String message){
        KafkaEvent event = KafkaEvent.builder()
                .userId(user.getId())
                .message(message)
                .build();

        ProducerRecord<String, KafkaEvent> producerRecord = new ProducerRecord<>("onelab-topic", event);

        kafkaTemplate.send(producerRecord);
    }
}
