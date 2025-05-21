import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import com.microservices.api_gateway.ApiGatewayApplication;

@SpringBootTest(classes = ApiGatewayApplication.class) // RedisConfig 클래스를 사용하여 테스트 컨텍스트를 로드합니다.
public class RedisConnectionTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void redisConnectionTest() {
        // Redis에 연결 테스트
        String key = "testKey";
        String value = "testValue";

        // Redis에 데이터 저장
        redisTemplate.opsForValue().set(key, value);

        // Redis에서 데이터 조회
        String retrievedValue = (String) redisTemplate.opsForValue().get(key);

        // 결과 출력
        assertThat(retrievedValue).isEqualTo(value);
    }
    
}
