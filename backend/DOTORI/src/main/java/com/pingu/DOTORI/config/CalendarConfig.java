
package com.pingu.DOTORI.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.cors.*;

import java.util.List;
import java.util.Map;

// ✅ 캘린더/유저 엔티티 + 캘린더 리포지토리만 스캔하도록 제한
@Configuration
@EntityScan(basePackageClasses = {
        com.pingu.DOTORI.entity.Calendars.class,
        com.pingu.DOTORI.entity.Users.class
})
@EnableJpaRepositories(basePackageClasses = {
        com.pingu.DOTORI.repository.CalendarRepository.class
})
public class CalendarConfig {

    // ✅ 공용 CORS
    @Bean(name = "calendarCorsSource")
    public CorsConfigurationSource calendarCorsSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:8080"
        ));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    // ✅ Hibernate 부팅속성: 스키마 검증으로 죽지 않게 + boolean→BIT 매핑
    @Bean
    public HibernatePropertiesCustomizer calendarHibernateCustomizer() {
        return (Map<String, Object> props) -> {
            // 개발 중 추천: "none" (또는 "update")
            props.put("hibernate.hbm2ddl.auto", "none");
            // 방어적 비활성화 (다른 키로 validate 걸려오는 것 차단)
            props.put("jakarta.persistence.schema-generation.database.action", "none");
            props.put("javax.persistence.schema-generation.database.action", "none");
            // MySQL BIT(1) 컬럼과 boolean 매칭
            props.put("hibernate.type.preferred_boolean_jdbc_type", "BIT");
        };
    }

    // ⛔ SecurityFilterChain 여기서 만들지 말 것
}

