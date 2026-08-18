package com.microservices.auth.event;

import com.microservices.auth.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TenantDroppedEventListener {

  private final RabbitTemplate rabbitTemplate;

  // phase 를 AFTER_COMMIT으로 설정 시에만 트랜잭션 이후 실행
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleTenantDroppedEvent(TenantDroppedEvent event) {
    // rabbitMq 로 메시지 전달
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.TENANT_EXCHANGE, RabbitMQConfig.TENANT_DROPPED_ROUTING_KEY, event);
  }
}
