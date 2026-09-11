package com.microservices.board.listener;

import com.microservices.board.config.RabbitMQConfig;
import com.microservices.board.event.TenantCreatedEvent;
import com.microservices.board.tenant.BoardSchemaMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantSchemaEventListener {

  private final BoardSchemaMigrationService boardSchemaMigrationService;

  @RabbitListener(queues = RabbitMQConfig.BOARD_SCHEMA_QUEUE)
  public void onTenantCreated(TenantCreatedEvent event) {
    log.info("Received TenantCreatedEvent: {}", event);

    try {
      boardSchemaMigrationService.migrate(event.getTenantName());
      log.info("Finished processing for event: {}", event.getEventId());
    } catch (Exception e) {
      log.error(
          "Failed to migrate board schema for tenant: {}. Error: {}",
          event.getTenantName(),
          e.getMessage());
      throw e;
    }
  }
}
