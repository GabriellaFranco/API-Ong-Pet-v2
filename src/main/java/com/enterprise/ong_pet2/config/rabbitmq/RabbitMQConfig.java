package com.enterprise.ong_pet2.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_EVENTS = "ong.events";
    public static final String EXCHANGE_DLX    = "ong.events.dlx";

    public static final String QUEUE_ADOCAO_CRIADA   = "q.adocao.criada";
    public static final String QUEUE_ADOCAO_STATUS   = "q.adocao.status";
    public static final String QUEUE_ANIMAL_CADASTRADO = "q.animal.cadastrado";
    public static final String QUEUE_DOACAO_CRIADA   = "q.doacao.criada";
    public static final String QUEUE_DLX_FAILED      = "q.dlx.failed";

    public static final String RK_ADOCAO_CRIADA      = "adocao.criada";
    public static final String RK_ADOCAO_STATUS      = "adocao.status.#";
    public static final String RK_ANIMAL_CADASTRADO  = "animal.cadastrado";
    public static final String RK_DOACAO_CRIADA      = "doacao.criada";

    @Bean
    public TopicExchange eventsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_EVENTS).durable(true).build();
    }

    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_DLX).durable(true).build();
    }

    @Bean
    public Queue queueAdocaoCriada() {
        return QueueBuilder.durable(QUEUE_ADOCAO_CRIADA)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_DLX_FAILED)
                .build();
    }

    @Bean
    public Queue queueAdocaoStatus() {
        return QueueBuilder.durable(QUEUE_ADOCAO_STATUS)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_DLX_FAILED)
                .build();
    }

    @Bean
    public Queue queueAnimalCadastrado() {
        return QueueBuilder.durable(QUEUE_ANIMAL_CADASTRADO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_DLX_FAILED)
                .build();
    }

    @Bean
    public Queue queueDoacaoCriada() {
        return QueueBuilder.durable(QUEUE_DOACAO_CRIADA)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_DLX_FAILED)
                .build();
    }

    @Bean
    public Queue queueDlxFailed() {
        return QueueBuilder.durable(QUEUE_DLX_FAILED).build();
    }

    @Bean
    public Binding bindingAdocaoCriada() {
        return BindingBuilder.bind(queueAdocaoCriada())
                .to(eventsExchange()).with(RK_ADOCAO_CRIADA);
    }

    @Bean
    public Binding bindingAdocaoStatus() {
        return BindingBuilder.bind(queueAdocaoStatus())
                .to(eventsExchange()).with(RK_ADOCAO_STATUS);
    }

    @Bean
    public Binding bindingAnimalCadastrado() {
        return BindingBuilder.bind(queueAnimalCadastrado())
                .to(eventsExchange()).with(RK_ANIMAL_CADASTRADO);
    }

    @Bean
    public Binding bindingDoacaoCriada() {
        return BindingBuilder.bind(queueDoacaoCriada())
                .to(eventsExchange()).with(RK_DOACAO_CRIADA);
    }

    @Bean
    public Binding bindingDlxFailed() {
        return BindingBuilder.bind(queueDlxFailed())
                .to(dlxExchange()).with(QUEUE_DLX_FAILED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        var template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}