package com.enterprise.ong_pet2.messaging.publisher;

import com.enterprise.ong_pet2.config.rabbitmq.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(Object event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_EVENTS, routingKey, event);
            log.info("Evento publicado: routingKey={} payload={}", routingKey, event);
        } catch (Exception e) {
            log.error("Falha ao publicar evento: routingKey={} erro={}", routingKey, e.getMessage(), e);
        }
    }
}