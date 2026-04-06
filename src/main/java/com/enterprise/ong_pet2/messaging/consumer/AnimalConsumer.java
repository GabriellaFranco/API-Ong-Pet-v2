package com.enterprise.ong_pet2.messaging.consumer;

import com.enterprise.ong_pet2.config.rabbitmq.RabbitMQConfig;
import com.enterprise.ong_pet2.model.event.AnimalCadastradoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AnimalConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ANIMAL_CADASTRADO)
    public void onAnimalCadastrado(AnimalCadastradoEvent event) {
        log.info("Novo animal cadastrado: animalId={} nome={} especie={} porte={} responsavelId={}",
                event.animalId(), event.nome(), event.especie(),
                event.porte(), event.responsavelId());
    }
}