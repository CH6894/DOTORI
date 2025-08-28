package com.pingu.DOTORI.security;

import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.repository.UsersRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // 추가
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtProvider jwtProvider;
  private final UsersRepository userRepository;

  @Value("${app.oauth2.success-redirect:http://localhost:5173/oauth2/callback}")
  private String frontendCallback;

  public OAuth2SuccessHandler(JwtProvider jwtProvider, UsersRepository userRepository) {
    this.jwtProvider = jwtProvider;
    this.userRepository = userRepository;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
      Authentication authentication)
      throws IOException, ServletException {

    System.out.println("[OAuth2SuccessHandler] called");
    System.out.println("[OAuth2SuccessHandler] frontendCallback = " + frontendCallback);

    if (frontendCallback == null || frontendCallback.isBlank()) {
      throw new IllegalStateException("app.oauth2.success-redirect 가 비어 있습니다. application.properties 를 확인하세요.");
    }

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    Map<String, Object> attrs = oAuth2User.getAttributes();

    System.out.println("[OAuth2SuccessHandler] attrs = " + attrs);

    // --- 네이버 response 안전 파싱 ---
    String email = null;
    String name = null;
    String nickname = null;
    String mobile = null;
    String gender = null;
    String birthday = null;
    String birthyear = null;
    String profileImage = null;

    Object respObj = attrs.get("response");
    if (respObj instanceof Map<?, ?> respMap) {
      Object emailObj = respMap.get("email");
      Object nameObj = respMap.get("name");
      Object nicknameObj = respMap.get("nickname");
      Object mobileObj = respMap.get("mobile");
      Object genderObj = respMap.get("gender");
      Object birthdayObj = respMap.get("birthday");
      Object birthyearObj = respMap.get("birthyear");
      Object profileImageObj = respMap.get("profile_image");

      email = emailObj instanceof String ? (String) emailObj : null;
      name = nameObj instanceof String ? (String) nameObj : null;
      nickname = nicknameObj instanceof String ? (String) nicknameObj : null;
      mobile = mobileObj instanceof String ? (String) mobileObj : null;
      gender = genderObj instanceof String ? (String) genderObj : null;
      birthday = birthdayObj instanceof String ? (String) birthdayObj : null;
      birthyear = birthyearObj instanceof String ? (String) birthyearObj : null;
      profileImage = profileImageObj instanceof String ? (String) profileImageObj : null;
    } else {
      // 혹시 provider 세팅이 달라 최상위에 바로 담겨 올 경우 대비
      Object emailObj = attrs.get("email");
      email = emailObj instanceof String ? (String) emailObj : null;
    }

    String subject = (email != null && !email.isBlank()) ? email : ("naver:" + name);

    // --- DB 저장 로직 ---
    if (email != null) {
      Users existingUser = userRepository.findByEmail(email).orElse(null);

      if (existingUser == null) {
        // 새 사용자 생성
        String uniqueNickName = (nickname != null && !nickname.isBlank()) ? nickname : "사용자";

        // 닉네임 중복 체크 및 유니크 생성
        String finalNickName = uniqueNickName;
        int counter = 1;
        while (userRepository.findByNickName(finalNickName).isPresent()) {
          finalNickName = uniqueNickName + counter;
          counter++;
        }

        // LocalDate 변환 로직 (네이버 응답 값을 사용)
        LocalDate userBirthDate = convertBirthday(birthday, birthyear);
        LocalDate userBirthYear = convertBirthYear(birthyear);

        Users newUser = Users.builder()
            .email(email)
            .userName(name != null ? name : "네이버사용자")
            .nickName(finalNickName)
            .phone(mobile != null ? mobile : "010-0000-0000")
            .gender(convertGender(gender))
            .birthDate(userBirthDate) // 변환된 LocalDate 값 할당
            .birthYear(userBirthYear) // 변환된 LocalDate 값 할당
            .signInDate(LocalDateTime.now())
            .userType(1)
            .userImg(profileImage)
            .build();

        userRepository.save(newUser);
        System.out.println("[DB] 새 사용자 저장: " + email + ", 이름: " + name);
      } else {
        System.out.println("[DB] 기존 사용자 로그인: " + email);
      }
    }

    String token = jwtProvider.createToken(subject);

    String targetUrl = UriComponentsBuilder
        .fromUriString(frontendCallback)
        .queryParam("token", token)
        .build()
        .toUriString();

    System.out.println("[OAuth2SuccessHandler] redirect -> " + targetUrl);
    getRedirectStrategy().sendRedirect(req, res, targetUrl);
  }

  // 성별 변환 (F/M -> 1/2)
  private Byte convertGender(String gender) {
    if (gender == null)
      return (byte) 0;
    switch (gender.toUpperCase()) {
      case "M":
        return (byte) 1;
      case "F":
        return (byte) 2;
      default:
        return (byte) 0;
    }
  }

  // 생일 변환 (MM-DD -> LocalDate)
  private LocalDate convertBirthday(String birthday, String birthyear) {
    if (birthday == null || birthyear == null) {
      return LocalDate.of(1900, 1, 1); // 기본값 설정
    }
    try {
      String fullDate = birthyear + "-" + birthday;
      return LocalDate.parse(fullDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    } catch (Exception e) {
      return LocalDate.of(1900, 1, 1); // 변환 실패 시 기본값 설정
    }
  }

  // 출생년도 변환 (YYYY -> LocalDate)
  private LocalDate convertBirthYear(String birthyear) {
    if (birthyear == null) {
      return LocalDate.of(1900, 1, 1); // 기본값 설정
    }
    try {
      return LocalDate.of(Integer.parseInt(birthyear), 1, 1);
    } catch (NumberFormatException e) {
      return LocalDate.of(1900, 1, 1); // 변환 실패 시 기본값 설정
    }
  }
}