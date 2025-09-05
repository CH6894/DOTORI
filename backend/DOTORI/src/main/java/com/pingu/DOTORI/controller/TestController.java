package com.pingu.DOTORI.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    // 기본 연결 테스트
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        log.info("테스트 핑 요청 수신");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", System.currentTimeMillis());
        response.put("server", "Spring Boot");
        return ResponseEntity.ok(response);
    }

    // JWT 토큰 테스트
    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authTest(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("인증 테스트 요청 수신 - Authorization header: {}", authHeader);
        
        Map<String, Object> response = new HashMap<>();
        response.put("hasAuthHeader", authHeader != null);
        response.put("authHeader", authHeader);
        response.put("isBearer", authHeader != null && authHeader.startsWith("Bearer "));
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            response.put("tokenLength", token.length());
            response.put("tokenStart", token.length() > 20 ? token.substring(0, 20) + "..." : token);
        }
        
        return ResponseEntity.ok(response);
    }

    // POST 요청 테스트
    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestBody Map<String, Object> requestData) {
        log.info("에코 테스트 요청 수신 - 데이터: {}", requestData);
        Map<String, Object> response = new HashMap<>();
        response.put("received", requestData);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
