package com.pingu.DOTORI.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
// 서버 확인용 코드임 무시해도 됨
public class OAuth2ClientLoggerConfig {

    @Bean
    CommandLineRunner showClients(ClientRegistrationRepository repo) {
        return args -> {
            if (repo instanceof InMemoryClientRegistrationRepository m) {
                m.forEach(r -> System.out.println("✅ OAuth client registered: " + r.getRegistrationId()));
            } else {
                System.out.println("⚠️ ClientRegistrationRepository is not InMemory: "
                        + repo.getClass().getName());
            }
        };
    }
}
