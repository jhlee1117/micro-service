# 📑 테넌트 관리 이벤트 아키텍처 설계서 (EDA)

## 1. 개요 (Overview)
본 문서는 `auth-service`에서 테넌트 생성 시 발생하는 이벤트를 비동기적으로 처리하여, 실제 데이터베이스의 물리적 스키마 생성 및 초기화를 자동화하기 위한 메시징 구조를 정의합니다.

## 2. 메시지 브로커 구성 (Infrastructure)
*   **Broker:** RabbitMQ
*   **Protocol:** AMQP (Advanced Message Queuing Protocol)
*   **Exchange Name:** `tenant.exchange`
*   **Exchange Type:** `topic` (패턴 매칭을 통한 높은 확장성 제공)

## 3. 라우팅 설계 (Routing Strategy)

| 이벤트 종류 (Routing Key) | 전담 큐 (Queue Name) | 목적 (Purpose) |
| :--- | :--- | :--- |
| `tenant.created` | `tenant.schema-create.queue` | 새 테넌트용 DB 스키마 및 기본 테이블 생성 |
| `tenant.created` | `tenant.welcome-mail.queue` | (확장 예정) 가입 환영 메일 발송 |
| `tenant.deleted` | `tenant.schema-delete.queue` | (확장 예정) 테넌트 탈퇴 시 데이터 백업 및 삭제 |
| `tenant.updated` | `tenant.cache-refresh.queue` | (확장 예정) 테넌트 정보 변경 시 전역 캐시 갱신 |

## 4. 메시지 데이터 규격 (Event Payload)
모든 테넌트 관련 이벤트는 다음의 JSON 구조를 따르며, `TenantCreatedEvent` 클래스로 구현됩니다.

```json
{
  "eventId": "uuid-string",
  "actionType": "CREATE",
  "tenantId": 102,
  "tenantName": "company_alpha",
  "adminEmail": "admin@alpha.com",
  "timestamp": "2026-05-29T14:00:00.123",
  "metadata": {
    "planType": "PREMIUM",
    "region": "ap-northeast-2"
  }
}
```

## 5. 서비스 간 워크플로우 (Sequence Flow)

1.  **Auth Service (Publisher):**
    *   관리자 API를 통해 테넌트 정보를 `tenant` 테이블에 저장합니다. (상태: `PROVISIONING`)
    *   `tenant.exchange`로 `tenant.created` 키와 함께 메시지를 발행합니다.
2.  **RabbitMQ (Broker):**
    *   `topic` 매칭 규칙에 따라 메시지를 `tenant.schema-create.queue`로 전달합니다.
3.  **Schema Worker (Consumer):**
    *   큐에서 메시지를 수신합니다.
    *   `CREATE SCHEMA company_alpha;` 명령 및 초기 DDL 스크립트를 실행합니다.
    *   작업 완료 시 `tenant.provisioning.completed` 이벤트를 다시 발행합니다.
4.  **Auth Service (Subscriber):**
    *   완료 이벤트를 수신하여 해당 테넌트의 상태를 `ACTIVE`로 변경합니다.

## 6. 향후 확장 및 고도화 방안 (Future Roadmap)

### 6.1 다중 구독자 패턴 (Fan-out via Topic)
하나의 `tenant.created` 이벤트만 발행해도, RabbitMQ의 Topic 기능을 이용해 스키마 생성, 메일 발송, 빌딩 시스템 연동 등 여러 서비스가 동시에 독립적인 작업을 수행할 수 있습니다.

### 6.2 실패 복구 전략 (Dead Letter Exchange, DLX)
스키마 생성 작업은 외부 요인(DB 부하 등)으로 실패할 수 있습니다.
*   **DLQ 도입:** 처리 실패 시 메시지를 `tenant.dead-letter.queue`로 보내 별도의 알림을 주거나 수동 복구 환경을 제공합니다.
*   **Retry 정책:** 최대 3회 재시도 후 실패 시 관리자에게 대시보드로 리포팅합니다.

### 6.3 분산 트랜잭션 관리 (Saga Pattern)
데이터베이스 저장과 메시지 발행 사이의 원자성을 보장하기 위해 **Transactional Outbox Pattern**을 도입할 수 있습니다. 
*   DB에 테넌트 정보를 넣을 때 '메시지 발송 예정' 상태도 같은 트랜잭션으로 저장한 뒤, 별도 프로세스가 큐로 밀어넣는 방식입니다.

---

## 7. 구현 상세 (Implementation Details) - Publisher (auth-service)

현재 `auth-service`에 구현된 발행자 측 핵심 코드 구조입니다.

### 7.1 의존성 (build.gradle)
```gradle
implementation 'org.springframework.boot:spring-boot-starter-amqp'
```

### 7.2 설정 (RabbitMQConfig.java)
스프링 기동 시 Exchange와 Queue를 자동 생성하고 바인딩하며, JSON 직렬화를 위한 컨버터를 빈으로 등록합니다.
```java
@Configuration
public class RabbitMQConfig {
    public static final String TENANT_EXCHANGE = "tenant.exchange";
    public static final String TENANT_SCHEMA_QUEUE = "tenant.schema-create.queue";
    public static final String TENANT_CREATED_ROUTING_KEY = "tenant.created";

    @Bean
    public Queue tenantSchemaQueue() { return new Queue(TENANT_SCHEMA_QUEUE, true); }

    @Bean
    public TopicExchange tenantExchange() { return new TopicExchange(TENANT_EXCHANGE); }

    @Bean
    public Binding tenantBinding(Queue tenantSchemaQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(tenantSchemaQueue).to(topicExchange).with(TENANT_CREATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() { return new Jackson2JsonMessageConverter(); }
}
```

### 7.3 이벤트 객체 (TenantCreatedEvent.java)
메시지 브로커로 전송될 페이로드입니다.
```java
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantCreatedEvent {
    private String eventId;
    private Long tenantId;
    private String tenantName;
    private LocalDateTime timestamp;

    public TenantCreatedEvent(Tenant savedTenant) {
        this.eventId = UUID.randomUUID().toString();
        this.tenantId = savedTenant.getId();
        this.tenantName = savedTenant.getName();
        this.timestamp = LocalDateTime.now();
    }
}
```

### 7.4 서비스 및 이벤트 리스너 연동
트랜잭션 정합성을 보장하기 위해, DB 저장이 완료된(`AFTER_COMMIT`) 직후에만 메시지를 발행합니다.

**TenantService.java**
```java
@Transactional
public TenantDto createTenant(TenantDto tenantDto) {
    // 1. DB 저장
    Tenant savedTenant = tenantRepository.save(tenant);
    // 2. 스프링 내부 이벤트 발행
    eventPublisher.publishEvent(new TenantCreatedEvent(savedTenant));
    return TenantDto.fromEntity(savedTenant);
}
```

**TenantCreatedEventListener.java**
```java
@Component
@RequiredArgsConstructor
public class TenantCreatedEventListener {
    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTenantCreatedEvent(TenantCreatedEvent event) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TENANT_EXCHANGE,
            RabbitMQConfig.TENANT_CREATED_ROUTING_KEY,
            event
        );
    }
}
```

---

## 8. 구현 상세 (Implementation Details) - Subscriber (tenant-provisioning-worker)

메시지를 수신하고 실제 DB에 스키마를 생성하는 독립적인 워커 서비스의 핵심 코드 구조입니다.

### 8.1 의존성 (build.gradle)
```gradle
// RabbitMQ
implementation 'org.springframework.boot:spring-boot-starter-amqp'
// 스키마 생성을 위한 JDBC 및 DB 드라이버
implementation 'org.springframework.boot:spring-boot-starter-jdbc'
runtimeOnly 'org.postgresql:postgresql'
```

### 8.2 커스텀 설정 (RabbitMQConfig.java)
발행자(auth-service)와 구독자(worker) 간의 패키지 경로 불일치로 인한 `MessageConversionException`을 해결하기 위해, `DefaultClassMapper`를 사용하여 강제 매핑 및 신뢰할 수 있는 패키지 설정을 적용합니다. 또한, `@RabbitListener`가 이 커스텀 컨버터를 반드시 사용하도록 팩토리를 재정의합니다.
```java
@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("*");

        // auth-service의 이벤트 클래스명을 워커의 이벤트 클래스와 강제 매핑
        Map<String, Class<?>> isClassMapping = new HashMap<>();
        isClassMapping.put("com.microservices.auth.event.TenantCreatedEvent", TenantCreatedEvent.class);

        classMapper.setIdClassMapping(isClassMapping);
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, 
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
```

### 8.3 비즈니스 로직 (SchemaProvisioningService.java)
전달받은 테넌트명을 사용하여 실제 데이터베이스에 DDL(Data Definition Language) 쿼리를 실행합니다. SQL 인젝션을 방지하기 위한 정규식 검증이 포함되어 있습니다.
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaProvisioningService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void createTenantSchema(String tenantName) {
        log.info("Starting schema provisioning for tenant: {}", tenantName);

        // 1. 보안 검증 (SQL 인젝션 방지)
        if (!tenantName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid tenant name format: " + tenantName);
        }

        // 2. 스키마 생성 쿼리 실행
        String createSchemaQuery = "CREATE SCHEMA IF NOT EXISTS " + tenantName;
        jdbcTemplate.execute(createSchemaQuery);

        log.info("Successfully created schema: {}", tenantName);
    }
}
```

### 8.4 메시지 리스너 (TenantEventListener.java)
`tenant.schema-create.queue`를 지속적으로 감시하다가 메시지가 들어오면 스키마 생성 로직을 호출합니다.
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantEventListener {

    private final SchemaProvisioningService schemaProvisioningService;

    @RabbitListener(queues = "tenant.schema-create.queue")
    public void onTenantCreated(TenantCreatedEvent event) {
        log.info("Received TenantCreatedEvent: {}", event);

        try {
            schemaProvisioningService.createTenantSchema(event.getTenantName());
            log.info("Finished processing for event: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to provision schema for tenant: {}. Error: {}", event.getTenantName(), e.getMessage());
            throw e;
        }
    }
}
```
