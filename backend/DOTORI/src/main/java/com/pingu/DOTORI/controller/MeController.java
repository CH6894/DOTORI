package com.pingu.DOTORI.controller;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {
  @GetMapping("/api/me")
  public Map<String, Object> me(@AuthenticationPrincipal OAuth2User user) {
    if (user == null) return Map.of("authenticated", false);
    var res = (Map<String, Object>) user.getAttributes().get("response");
    return Map.of("authenticated", true, "email", res.get("email"), "name", res.get("name"));
  }
}
