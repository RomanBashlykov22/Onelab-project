package kz.romanb.onelabproject.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "onelab-topic")
@Slf4j
public class EventsHandler {
    @KafkaHandler
    public void handle(@Payload KafkaEvent event) {
        log.info("Пользователь с id {} совершил действие: {}", event.userId(), event.message());
    }
}
