package com.benny.board_mate.common;

import com.benny.board_mate.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Health", description = "서비스 상태 확인")
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final RedisTemplate<String, String> redisTemplate;

    @Operation(summary = "전체 상태 확인")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "UP");
        status.put("redis", checkRedis());
        return ResponseEntity.ok(ApiResponse.ok(status));
    }

    @Operation(summary = "Redis 연결 상태 확인")
    @GetMapping("/redis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> redisHealth() {
        Map<String, Object> result = new HashMap<>();

        try {
            // ping 테스트
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();

            result.put("status", "UP");
            result.put("ping", pong);

            // 간단한 읽기/쓰기 테스트
            String testKey = "health:test:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, "ok");
            String value = redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);

            result.put("readWrite", "ok".equals(value) ? "OK" : "FAIL");

            log.info("Redis health check: connected successfully");

        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            log.error("Redis health check failed: {}", e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    private String checkRedis() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return "UP";
        } catch (Exception e) {
            return "DOWN: " + e.getMessage();
        }
    }
}
