package com.enterprise.ong_pet2.messaging.consumer;

import com.enterprise.ong_pet2.config.rabbitmq.RabbitMQConfig;
import com.enterprise.ong_pet2.model.event.DoacaoCriadaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DoacaoConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DOACAO_CRIADA)
    public void onDoacaoCriada(DoacaoCriadaEvent event) {
        log.info("Doação registrada: doacaoId={} doadorId={} valor={} categoria={}",
                event.doacaoId(), event.doadorId(), event.valor(), event.categoria());
    }
}