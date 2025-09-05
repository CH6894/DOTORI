package com.pingu.DOTORI.security;

import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UsersRepository userRepository;
    private final OAuth2AuthorizedClientService clientService;

    // Vue 쪽 redirect 주소 (application.yml 에서 app.oauth2.success-redirect 로도 오버라이드
    // 가능)
    @Value("${app.oauth2.success-redirect:http://localhost:5173/oauth2/callback}")
    private String frontendCallback;

    public OAuth2SuccessHandler(JwtProvider jwtProvider, UsersRepository userRepository,
            OAuth2AuthorizedClientService clientService) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.clientService = clientService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attrs = oAuth2User.getAttributes();

        // --- 네이버 response 파싱 ---
        String email = null, name = null, nickname = null, mobile = null, gender = null;
        String birthday = null, birthyear = null, profileImage = null;

        Object respObj = attrs.get("response");
        if (respObj instanceof Map<?, ?> respMap) {
            email = (String) respMap.get("email");
            name = (String) respMap.get("name");
            nickname = (String) respMap.get("nickname");
            mobile = (String) respMap.get("mobile");
            gender = (String) respMap.get("gender");
            birthday = (String) respMap.get("birthday");
            birthyear = (String) respMap.get("birthyear");
            profileImage = (String) respMap.get("profile_image");
        } else {
            email = (String) attrs.get("email");
        }

        String subject = (email != null && !email.isBlank()) ? email : ("naver:" + name);

        // --- DB 저장 로직 ---
        if (email != null) {
            Users existingUser = userRepository.findByEmail(email).orElse(null);

            if (existingUser == null) {
                String uniqueNickName = (nickname != null && !nickname.isBlank()) ? nickname : "사용자";

                // 닉네임 중복 체크
                String finalNickName = uniqueNickName;
                int counter = 1;
                while (userRepository.findByNickName(finalNickName).isPresent()) {
                    finalNickName = uniqueNickName + counter;
                    counter++;
                }

                Users newUser = Users.builder()
                        .email(email)
                        .userName(name != null ? name : "네이버사용자")
                        .nickName(finalNickName)
                        .phone(mobile != null ? mobile : "010-0000-0000")
                        .gender(convertGender(gender))
                        .birthDate(convertBirthday(birthday, birthyear))
                        .birthYear(convertBirthYear(birthyear))
                        .signInDate(LocalDateTime.now())
                        .userType(1)
                        .userImg(profileImage)
                        .build();

                userRepository.save(newUser);
            }
        }

        // --- AccessToken 세션에 저장 (네이버 API 호출용) ---
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName());
            if (client != null) {
                String accessToken = client.getAccessToken().getTokenValue();
                HttpSession session = req.getSession(true);
                session.setAttribute("NAVER_ACCESS_TOKEN", accessToken);
            }
        }

        // --- JWT 발급 ---
        String token = jwtProvider.createToken(subject);

        // ✅ 프론트엔드로 토큰 전달 (쿼리 파라미터로)
        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendCallback) // ex) http://localhost:5173/oauth2/callback
                .queryParam("token", token)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(req, res, targetUrl);
    }

    // --- 유틸 함수 ---
    private Byte convertGender(String gender) {
        if (gender == null)
            return (byte) 1;
        return switch (gender.toUpperCase()) {
            case "M" -> (byte) 1;
            case "F" -> (byte) 2;
            default -> (byte) 1;
        };
    }

    private LocalDate convertBirthday(String birthday, String birthyear) {
        if (birthday == null || birthyear == null) {
            return LocalDate.of(1900, 1, 1);
        }
        try {
            String fullDate = birthyear + "-" + birthday;
            return LocalDate.parse(fullDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return LocalDate.of(1900, 1, 1);
        }
    }

    private LocalDate convertBirthYear(String birthyear) {
        if (birthyear == null) {
            return LocalDate.of(1900, 1, 1);
        }
        try {
            return LocalDate.of(Integer.parseInt(birthyear), 1, 1);
        } catch (NumberFormatException e) {
            return LocalDate.of(1900, 1, 1);
        }
    }
}
