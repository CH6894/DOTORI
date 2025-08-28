package com.pingu.DOTORI.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.util.concurrent.TimeUnit;
import org.springframework.http.CacheControl;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.upload-dir:uploads}")
  private String uploadDir;

  /** 정적 파일 서빙: /static/** → file:<uploadDir>/ */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/static/**")
        .addResourceLocations("file:" + uploadDir + "/")
        .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
  }
}
