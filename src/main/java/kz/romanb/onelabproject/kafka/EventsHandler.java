package kz.romanb.onelabproject.kafka;

import kz.romanb.onelabproject.services.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "onelab-topic")
@RequiredArgsConstructor
public class EventsHandler {
    private final EmailNotificationService emailNotificationService;

    @KafkaHandler
    public void handle(@Payload KafkaEvent event) {
        emailNotificationService.sendMessage(event.userEmail(), "Добро пожаловать!", event.message());
    }
}
