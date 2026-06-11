package com.microservices.auth_service.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.microservices.auth_service.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TenantCreatedEventListener {

    private final RabbitTemplate rabbitTemplate;

    // phase 를 AFTER_COMMIT으로 설정 시에만 트랜잭션 이후 실행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTenantCreatedEvent(TenantCreatedEvent event) {
        // rabbitMq 로 메시지 전달
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TENANT_EXCHANGE,
            RabbitMQConfig.TENANT_CREATED_ROUTING_KEY,
            event
        );
    }
}
