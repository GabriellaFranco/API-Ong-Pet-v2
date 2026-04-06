package com.enterprise.ong_pet2.messaging.consumer;

import com.enterprise.ong_pet2.config.rabbitmq.RabbitMQConfig;
import com.enterprise.ong_pet2.model.event.AdocaoCriadaEvent;
import com.enterprise.ong_pet2.model.event.AdocaoStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdocaoConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ADOCAO_CRIADA)
    public void onAdocaoCriada(AdocaoCriadaEvent event) {
        log.info("Pedido de adoção criado: pedidoId={} adotanteId={} animalId={} voluntarioId={} score={}",
                event.pedidoId(), event.adotanteId(), event.animalId(),
                event.voluntarioId(), event.scoreMatching());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ADOCAO_STATUS)
    public void onAdocaoStatusChanged(AdocaoStatusChangedEvent event) {
        log.info("Status do pedido alterado: pedidoId={} {} -> {}",
                event.pedidoId(), event.statusAnterior(), event.statusNovo());
    }
}