package bgaebalja.bsherpa.user.service;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

import bgaebalja.bsherpa.user.domain.UserDTO;
import bgaebalja.bsherpa.user.domain.UserJoinRequest;
import bgaebalja.bsherpa.user.domain.UserRole;
import bgaebalja.bsherpa.user.domain.Users;
import bgaebalja.bsherpa.user.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void saveUser(UserJoinRequest userJoinRequest) {
    String password = passwordEncoder.encode(userJoinRequest.getPassword());
    Users user = Users.createUser(userJoinRequest.getUsername(), password,
    userJoinRequest.getEmail(),userJoinRequest.getClazz(),userJoinRequest.getGrade());
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void saveStudent(UserJoinRequest userJoinRequest) {
    log.info("Saving student: {}" , userJoinRequest);
    String password = passwordEncoder.encode(userJoinRequest.getPassword());
    Users user = Users.createStudent(userJoinRequest.getUsername(), password,userJoinRequest.getEmail(),userJoinRequest.getClazz(),userJoinRequest.getGrade()
        );
    userRepository.save(user);
  }

  @Override
  public Object getKakaoUser(String accessToken) {
    String email = getEmailFromKakaoAccessToken(accessToken);
    Optional<Users> findUser = userRepository.findByUserId(email);
    if (findUser.isPresent()) {
      Users users = findUser.get();
      return new UserDTO(
          users.getUserId(),
          users.getPassword(),
          users.getUsername(),
          users.getClazz(),
          users.getGrade(),
          users.getRoles().stream().map(UserRole::getRole).collect(Collectors.toList())
      );
    }
    return email;
  }

  private String getEmailFromKakaoAccessToken(String accessToken) {
    String kakaoGetUserUrl = "https://kapi.kakao.com/v2/user/me";

    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.add(AUTHORIZATION, "Bearer " + accessToken);
    headers.add(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(kakaoGetUserUrl).build();
    ResponseEntity<LinkedHashMap> response =
        restTemplate.exchange(uriBuilder.toUri(), GET, entity, LinkedHashMap.class);
    log.info(response.toString());
    LinkedHashMap<String, LinkedHashMap> bodyMap = response.getBody();
    log.info(bodyMap.toString());
    LinkedHashMap<String, String> kakaoAccount = bodyMap.get("kakao_account");
    log.info("Kakao account details: {}", kakaoAccount);
    return kakaoAccount.get("email");
  }


}

