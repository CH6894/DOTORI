// src/main/java/com/cx/naverlogin/security/OAuth2SuccessHandler.java
package com.pingu.DOTORI.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtProvider jwtProvider;

  // ⚠️ 프로퍼티가 비어있으면 기본값 사용 (네 프론트 주소로 맞춰!)
  @Value("${app.oauth2.success-redirect:http://localhost:5173/oauth2/callback}")
  private String frontendCallback;

  public OAuth2SuccessHandler(JwtProvider jwtProvider) {
    this.jwtProvider = jwtProvider;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                      Authentication authentication)
      throws IOException, ServletException {

    System.out.println("[OAuth2SuccessHandler] called");
    System.out.println("[OAuth2SuccessHandler] frontendCallback = " + frontendCallback);

    if (frontendCallback == null || frontendCallback.isBlank()) {
      // 아예 명확한 메시지로 실패시켜 원인 파악 쉽게
      throw new IllegalStateException("app.oauth2.success-redirect 가 비어 있습니다. application.properties 를 확인하세요.");
    }

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    Map<String, Object> attrs = oAuth2User.getAttributes();
    System.out.println("[OAuth2SuccessHandler] attrs = " + attrs);

    // --- 네이버 response 안전 파싱 ---
    String email = null;
    String id    = null;

    Object respObj = attrs.get("response");
    if (respObj instanceof Map<?, ?> respMap) {
      Object emailObj = respMap.get("email");
      Object idObj    = respMap.get("id");
      email = emailObj instanceof String ? (String) emailObj : null;
      id    = idObj    instanceof String ? (String) idObj    : null;
    } else {
      // 혹시 provider 세팅이 달라 최상위에 바로 담겨 올 경우 대비
      Object emailObj = attrs.get("email");
      Object idObj    = attrs.get("id");
      email = emailObj instanceof String ? (String) emailObj : null;
      id    = idObj    instanceof String ? (String) idObj    : null;
    }

    String subject = (email != null && !email.isBlank()) ? email : ("naver:" + id);
    String token   = jwtProvider.createToken(subject);

    String targetUrl = UriComponentsBuilder
        .fromUriString(frontendCallback)    // ← 반드시 null/blank 아님
        .queryParam("token", token)
        .build()
        .toUriString();

    System.out.println("[OAuth2SuccessHandler] redirect -> " + targetUrl);
    getRedirectStrategy().sendRedirect(req, res, targetUrl);
  }
}
