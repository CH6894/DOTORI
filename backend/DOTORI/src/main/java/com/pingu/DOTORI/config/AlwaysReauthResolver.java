package com.pingu.DOTORI.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import jakarta.servlet.http.HttpServletRequest;

public class AlwaysReauthResolver implements OAuth2AuthorizationRequestResolver {

  private final DefaultOAuth2AuthorizationRequestResolver delegate;

  public AlwaysReauthResolver(ClientRegistrationRepository repo) {
    this.delegate = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    return customize(delegate.resolve(request));
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
    return customize(delegate.resolve(request, clientRegistrationId));
  }

  private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest req) {
    if (req == null) return null;
    Map<String, Object> extra = new HashMap<>(req.getAdditionalParameters());
    // ✅ 네이버: 항상 재인증
    extra.put("auth_type", "reauthenticate");
    return OAuth2AuthorizationRequest.from(req)
        .additionalParameters(extra)
        .build();
  }
}
